package com.sobot.chat.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.SobotAiAgentNoSpeakMessage;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.network.http.callback.StringResultCallBack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 大模型机器人静默提醒进程级管理器。
 *
 * <p>独立管理静默 v2 轮询，不复用原答案 delay 轮询状态。任务使用
 * {@code aiAgentCid + roundId + silenceEpoch} 幂等，generation 用于拦截停止后的延迟任务和晚到回调。</p>
 */
public class SobotAiAgentNoSpeakManager {

    private static final int MSG_REQUEST = 1;
    private static final int POLLING_COUNT_BUFFER = 2;
    private static final long POLLING_DELAY_BUFFER_SECONDS = 2L;
    private static final long[] RETRY_DELAYS_MS = {3000L, 6000L, 12000L};
    private static final String PUSH_TYPE_SESSION_OVER_TIME = "SESSION_OVER_TIME_MSG";
    private static final String POLLING_START = "POLLING_START";
    private static final String POLLING_RUNNING = "POLLING_RUNNING";
    private static final String POLLING_END = "POLLING_END";
    private static final String MISSING_MSG_ID = "missing_msg_id";
    private static final int MAX_PENDING_MESSAGE_COUNT = 20;

    private static volatile SobotAiAgentNoSpeakManager sInstance;

    private final Context mAppContext;
    private final NoSpeakHandler mHandler;
    private final Set<String> mDeliveredKeys = new HashSet<>();
    private final ArrayList<ZhiChiMessageBase> mPendingMessages = new ArrayList<>();
    private ZhiChiApi mZhiChiApi;
    private WeakReference<NoSpeakListener> mListenerReference;

    private String mMsgId;
    private String mPollingId;
    private String mRoundId;
    private String mTaskKey;
    private String mOwnerUid;
    private String mOwnerCid;
    private String mRobotName;
    private String mRobotLogo;
    private long mSilenceEpoch;
    private long mPollingIntervalMillis;
    private boolean mIsActive;
    private boolean mIsRequestInFlight;
    private int mRetryAttempt;
    private int mMaxPollingCount;
    private int mPollingCount;
    private long mGeneration;

    public interface NoSpeakListener {

        /**
         * 校验静默任务是否仍属于当前机器人会话。
         * 修改此判断会影响人工状态、用户切换和跨 cid 消息隔离。
         */
        boolean isNoSpeakPollingAvailable(String ownerUid, String ownerCid);

        /**
         * 将静默话术交给现有聊天消息列表展示。
         */
        void showNoSpeakMessage(ZhiChiMessageBase message);
    }

    private SobotAiAgentNoSpeakManager(Context context) {
        mAppContext = context == null ? null : context.getApplicationContext();
        mHandler = new NoSpeakHandler(this);
    }

    /**
     * 获取 SDK 内部静默管理器。单例只保存 ApplicationContext，不持有客户页面 Context。
     */
    public static SobotAiAgentNoSpeakManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SobotAiAgentNoSpeakManager.class) {
                if (sInstance == null) {
                    Context appContext = context == null ? null : context.getApplicationContext();
                    if (appContext == null) {
                        LogUtils.e("静默提醒管理器初始化失败：ApplicationContext 为空");
                        return null;
                    }
                    sInstance = new SobotAiAgentNoSpeakManager(appContext);
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取已创建的管理器，供会话终止入口使用；未启用过静默功能时不会因此创建单例。
     */
    public static SobotAiAgentNoSpeakManager getExistingInstance() {
        return sInstance;
    }

    /**
     * 绑定当前聊天页并补发同 uid/cid 下的缓存消息。页面监听仅使用弱引用，
     * 修改此入口会影响 SDK 子页面返回、Fragment 重建和跨会话消息隔离。
     */
    public void bind(NoSpeakListener listener, String ownerUid, String ownerCid, ZhiChiApi zhiChiApi) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final NoSpeakListener targetListener = listener;
            final String targetUid = ownerUid;
            final String targetCid = ownerCid;
            final ZhiChiApi targetApi = zhiChiApi;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    bindOnMain(targetListener, targetUid, targetCid, targetApi);
                }
            });
            return;
        }
        bindOnMain(listener, ownerUid, ownerCid, zhiChiApi);
    }

    private void bindOnMain(NoSpeakListener listener, String ownerUid, String ownerCid,
                            ZhiChiApi zhiChiApi) {
        if (listener == null) {
            return;
        }
        if (hasOwner() && (!TextUtils.equals(mOwnerUid, ownerUid)
                || !TextUtils.equals(mOwnerCid, ownerCid))) {
            stopInternal(true, true);
        }
        mZhiChiApi = zhiChiApi;
        mListenerReference = new WeakReference<>(listener);
        if (hasOwner() && TextUtils.equals(mOwnerUid, ownerUid)
                && TextUtils.equals(mOwnerCid, ownerCid)
                && !listener.isNoSpeakPollingAvailable(ownerUid, ownerCid)) {
            //页面已经绑定但业务状态不再允许机器人静默（如已转人工），不能继续保留离页任务。
            stopInternal(true, true);
            return;
        }
        dispatchPendingMessages();
    }

    /**
     * 页面离开时只解绑 UI，不停止轮询；后续消息会暂存在管理器中等待同会话页面重新绑定。
     */
    public void unbind(NoSpeakListener listener) {
        NoSpeakListener currentListener = getListener();
        if (currentListener == listener && mListenerReference != null) {
            mListenerReference.clear();
        }
    }

    /**
     * 根据 SSE 返回的静默配置幂等启动任务。
     * 同一 taskKey 不重复启动；新轮次会使旧请求回调失效。
     */
    public void startIfNeeded(ZhiChiMessageBase triggerMessage, String ownerUid, String ownerCid,
                              ZhiChiInitModeBase initModel, ZhiChiApi zhiChiApi) {
        if (triggerMessage == null || initModel == null) {
            return;
        }
        final int noSpeakTimes = triggerMessage.getNoSpeakTimes();
        final int noSpeakLength = triggerMessage.getNoSpeakLength();
        final String msgId = triggerMessage.getMsgId();
        final String pollingId = triggerMessage.getAiAgentCid();
        final String roundId = triggerMessage.getRoundId();
        final long silenceEpoch = triggerMessage.getSilenceEpoch();
        final String uid = ownerUid;
        final String cid = ownerCid;
        final String robotName = initModel.getRobotName();
        final String robotLogo = initModel.getRobotLogo();
        final ZhiChiApi targetApi = zhiChiApi;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startIfNeededOnMain(noSpeakTimes, noSpeakLength, msgId, pollingId, roundId, silenceEpoch,
                            uid, cid, robotName, robotLogo, targetApi);
                }
            });
            return;
        }
        startIfNeededOnMain(noSpeakTimes, noSpeakLength, msgId, pollingId, roundId, silenceEpoch,
                uid, cid, robotName, robotLogo, targetApi);
    }

    private void startIfNeededOnMain(int noSpeakTimes, int noSpeakLength, String msgId,
                                     String pollingId, String roundId, long silenceEpoch, String ownerUid,
                                     String ownerCid, String robotName, String robotLogo, ZhiChiApi zhiChiApi) {
        if (noSpeakTimes <= 0 || noSpeakLength <= 0) {
            return;
        }
        if (!isValidTaskIdentifier(pollingId) || !isValidTaskIdentifier(roundId)) {
            LogUtils.e("静默提醒未启动：aiAgentCid 或 roundId 为空");
            return;
        }
        if (zhiChiApi == null) {
            LogUtils.e("静默提醒未启动：ZhiChiApi 为空");
            return;
        }
        //epoch 参与幂等键，避免相同问答标识下的新静默周期被旧任务误判为重复启动。
        String taskKey = pollingId + "_" + roundId + "_" + silenceEpoch;
        if (mIsActive && taskKey.equals(mTaskKey)) {
            return;
        }

        stopInternal(false, true);
        mGeneration++;
        mIsActive = true;
        mZhiChiApi = zhiChiApi;
        mMsgId = msgId;
        mPollingId = pollingId;
        mRoundId = roundId;
        mSilenceEpoch = silenceEpoch;
        mTaskKey = taskKey;
        mOwnerUid = ownerUid;
        mOwnerCid = ownerCid;
        mRobotName = robotName;
        mRobotLogo = robotLogo;
        //配置值大于 0 才启用：请求上限额外预留 2 次，正常续轮在服务端等待时长后再缓冲 2 秒。
        mMaxPollingCount = noSpeakTimes > Integer.MAX_VALUE - POLLING_COUNT_BUFFER
                ? Integer.MAX_VALUE : noSpeakTimes + POLLING_COUNT_BUFFER;
        mPollingIntervalMillis = (noSpeakLength + POLLING_DELAY_BUFFER_SECONDS) * 1000L;
        mPollingCount = 0;
        mRetryAttempt = 0;
        mDeliveredKeys.clear();
        scheduleRequest(mGeneration, mPollingIntervalMillis);
    }

    /**
     * 停止当前静默任务。调用后旧 Runnable 和网络回调均不能恢复任务。
     */
    public void stop() {
        stopInternal(true, true);
    }

    /**
     * owner 更新后检查任务归属；修改这里会影响重新初始化和新会话隔离。
     */
    public void checkOwner(String ownerUid, String ownerCid) {
        if (hasOwner() && (!TextUtils.equals(mOwnerUid, ownerUid)
                || !TextUtils.equals(mOwnerCid, ownerCid))) {
            stop();
        }
    }

    public boolean isActive() {
        return mIsActive;
    }

    private void request(long generation) {
        if (!isGenerationActive(generation) || mIsRequestInFlight) {
            return;
        }
        if (!isTaskAvailable() || mAppContext == null || mZhiChiApi == null) {
            stop();
            return;
        }
        if (mPollingCount >= mMaxPollingCount) {
            LogUtils.d("静默提醒达到客户端轮询次数上限，任务停止");
            finishPolling(false);
            return;
        }
        mPollingCount++;
        mIsRequestInFlight = true;
        final long requestGeneration = generation;
        mZhiChiApi.getAiAgentNoSpeakPushList(mAppContext, mPollingId,
                new StringResultCallBack<ArrayList<SobotAiAgentNoSpeakMessage>>() {
                    @Override
                    public void onSuccess(final ArrayList<SobotAiAgentNoSpeakMessage> messages) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleSuccess(requestGeneration, messages);
                            }
                        });
                    }

                    @Override
                    public void onFailure(final Exception e, final String des) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleFailure(requestGeneration, e, des);
                            }
                        });
                    }
                });
    }

    /**
     * 顺序消费一个 v2 批次；整批只允许调度一次续轮。
     */
    private void handleSuccess(long generation, ArrayList<SobotAiAgentNoSpeakMessage> messages) {
        mIsRequestInFlight = false;
        if (!isGenerationActive(generation)) {
            scheduleCurrentTaskAfterOldRequest();
            return;
        }
        if (!isTaskAvailable()) {
            stop();
            return;
        }
        mRetryAttempt = 0;
        if (containsOnlyStaleEpochMessages(messages) && mPollingCount > 0) {
            //旧周期残留只负责出队，不得挤占当前周期 noSpeakTimes + 2 的有效请求额度。
            mPollingCount--;
        }
        if (messages != null) {
            for (SobotAiAgentNoSpeakMessage message : messages) {
                if (message == null) {
                    continue;
                }
                if (!consumeMessage(generation, message)) {
                    return;
                }
            }
        }
        if (isGenerationActive(generation)) {
            if (mPollingCount >= mMaxPollingCount) {
                LogUtils.d("静默提醒达到客户端轮询次数上限，任务停止");
                finishPolling(false);
            } else {
                scheduleRequest(generation, mPollingIntervalMillis);
            }
        }
    }

    /**
     * 判断成功批次是否只包含旧周期消息；修改这里会影响当前周期请求额度与残留队列清理。
     */
    private boolean containsOnlyStaleEpochMessages(ArrayList<SobotAiAgentNoSpeakMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return false;
        }
        boolean hasMessage = false;
        for (SobotAiAgentNoSpeakMessage message : messages) {
            if (message == null) {
                continue;
            }
            hasMessage = true;
            if (message.getSilenceEpoch() == mSilenceEpoch) {
                return false;
            }
        }
        return hasMessage;
    }

    private void handleFailure(long generation, Exception exception, String description) {
        mIsRequestInFlight = false;
        if (!isGenerationActive(generation)) {
            scheduleCurrentTaskAfterOldRequest();
            return;
        }
        if (mPollingCount >= mMaxPollingCount) {
            LogUtils.e("静默提醒达到客户端轮询次数上限，任务停止");
            finishPolling(false);
            return;
        }
        mRetryAttempt++;
        if (mRetryAttempt > RETRY_DELAYS_MS.length) {
            LogUtils.e("静默提醒连续请求失败，任务停止: "
                    + (exception == null ? description : exception.getMessage()));
            finishPolling(false);
            return;
        }
        scheduleRequest(generation, RETRY_DELAYS_MS[mRetryAttempt - 1]);
    }

    /**
     * 校验单条消息的类型、归属和状态，并在需要时回显或结束任务。
     */
    private boolean consumeMessage(long generation, SobotAiAgentNoSpeakMessage payload) {
        //必须先过滤静默代际，再处理 pollingId 和 END；否则旧周期残留 END 会提前终止新任务。
        if (payload.getSilenceEpoch() != mSilenceEpoch) {
            LogUtils.d("静默提醒忽略旧周期消息，继续当前轮询");
            return true;
        }
        if (!TextUtils.isEmpty(payload.getPushType())
                && !PUSH_TYPE_SESSION_OVER_TIME.equals(payload.getPushType())) {
            LogUtils.e("静默提醒忽略非静默 pushType");
            return true;
        }
        if (!TextUtils.equals(mPollingId, payload.getPollingId())) {
            LogUtils.e("静默提醒 pollingId 与当前任务不匹配，任务停止");
            stop();
            return false;
        }
        String pollingStatus = payload.getPollingStatus();
        boolean isRunning = POLLING_START.equals(pollingStatus)
                || POLLING_RUNNING.equals(pollingStatus);
        boolean isEnd = POLLING_END.equals(pollingStatus);
        if (!isRunning && !isEnd) {
            LogUtils.e("静默提醒 pollingStatus 异常，任务停止");
            stop();
            return false;
        }

        //END 仅作为同代际任务的结束信号，不展示其中携带的内容。
        if (isEnd) {
            finishPolling(false);
            return false;
        }

        if (!TextUtils.isEmpty(payload.getMsg())) {
            String deliveredKey = (TextUtils.isEmpty(payload.getMsgId())
                    ? MISSING_MSG_ID : payload.getMsgId())
                    + "_" + pollingStatus + "_" + payload.getMsg();
            if (mDeliveredKeys.add(deliveredKey)) {
                dispatchOrCache(createRobotMessage(payload));
                if (!isGenerationActive(generation)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将静默 payload 转换为现有普通机器人文本消息，避免新增专用 Holder。
     */
    private ZhiChiMessageBase createRobotMessage(SobotAiAgentNoSpeakMessage payload) {
        String localMsgId = TextUtils.isEmpty(payload.getMsgId())
                ? UUID.randomUUID().toString().replace("-", "") : payload.getMsgId();
        ZhiChiMessageBase message = new ZhiChiMessageBase();
        message.setId(localMsgId);
        message.setMsgId(localMsgId);
        message.setCid(mOwnerCid);
        message.setAiAgentCid(TextUtils.isEmpty(payload.getAiAgentCid())
                ? mPollingId : payload.getAiAgentCid());
        message.setRoundId(TextUtils.isEmpty(payload.getRoundId()) ? mRoundId : payload.getRoundId());
        message.setSilenceEpoch(payload.getSilenceEpoch());
        //静默提醒是一次性完整消息，聊天页不能按流式分片做固定距离滚动。
        message.setAiAgentNoSpeakMessage(true);
        message.setSenderType(ZhiChiConstant.message_sender_type_robot);
        String robotName = TextUtils.isEmpty(payload.getRobotName()) ? mRobotName : payload.getRobotName();
        message.setSender(robotName);
        message.setSenderName(robotName);
        message.setSenderFace(TextUtils.isEmpty(payload.getRobotLogo()) ? mRobotLogo : payload.getRobotLogo());
        message.setContent(payload.getMsg());
        message.setRobotAnswerMessageType(payload.getPollingStatus());
        message.setT(System.currentTimeMillis() + "");
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsgType(ZhiChiConstant.message_type_text);
        answer.setMsg(payload.getMsg());
        message.setAnswer(answer);
        return message;
    }

    private boolean isTaskAvailable() {
        return isValidTaskIdentifier(mPollingId)
                && isValidTaskIdentifier(mRoundId)
                && hasOwner();
    }

    /**
     * 校验任务标识是否可用于换代和发起轮询；修改这里会影响结束帧兼容与任务启动边界。
     */
    private boolean isValidTaskIdentifier(String identifier) {
        return !TextUtils.isEmpty(identifier)
                && !"null".equalsIgnoreCase(identifier.trim());
    }

    private boolean isGenerationActive(long generation) {
        return mIsActive && generation == mGeneration;
    }

    private NoSpeakListener getListener() {
        return mListenerReference == null ? null : mListenerReference.get();
    }

    private void scheduleRequest(long generation, long delayMillis) {
        mHandler.removeMessages(MSG_REQUEST);
        Message message = mHandler.obtainMessage(MSG_REQUEST, Long.valueOf(generation));
        mHandler.sendMessageDelayed(message, delayMillis);
    }

    /**
     * 新任务替换旧任务时，物理单飞要求等待旧请求真实结算；结算后按当前任务间隔启动新世代。
     */
    private void scheduleCurrentTaskAfterOldRequest() {
        if (mIsActive && !mIsRequestInFlight) {
            scheduleRequest(mGeneration, mPollingIntervalMillis);
        }
    }

    /**
     * 自然结束轮询。离页期间已获取的消息默认保留，等待同会话页面重新绑定后展示。
     */
    private void finishPolling(boolean clearPendingMessages) {
        stopInternal(true, clearPendingMessages);
    }

    private void stopInternal(boolean increaseGeneration, boolean clearPendingMessages) {
        if (increaseGeneration) {
            mGeneration++;
        }
        mIsActive = false;
        mRetryAttempt = 0;
        //只取消尚未发起的轮询。网络结算 Runnable 必须保留，用于释放物理单飞标记；
        //旧回调由 generation 拦截业务消费，结算后可为已替换的新任务重新安排请求。
        mHandler.removeMessages(MSG_REQUEST);
        if (clearPendingMessages) {
            mDeliveredKeys.clear();
            mPendingMessages.clear();
        }
        mMsgId = null;
        mPollingId = null;
        mRoundId = null;
        mTaskKey = null;
        if (clearPendingMessages) {
            mOwnerUid = null;
            mOwnerCid = null;
        }
        mRobotName = null;
        mRobotLogo = null;
        mSilenceEpoch = 0L;
        mPollingIntervalMillis = 0L;
        mMaxPollingCount = 0;
        mPollingCount = 0;
        if (!clearPendingMessages && mPendingMessages.isEmpty()) {
            mDeliveredKeys.clear();
            mOwnerUid = null;
            mOwnerCid = null;
        }
    }

    private boolean hasOwner() {
        return !TextUtils.isEmpty(mOwnerUid) && !TextUtils.isEmpty(mOwnerCid);
    }

    /**
     * 页面可展示时直接回显，否则进入有界内存缓存；不会持久化或跨进程保活。
     */
    private void dispatchOrCache(ZhiChiMessageBase message) {
        NoSpeakListener listener = getListener();
        if (listener != null && listener.isNoSpeakPollingAvailable(mOwnerUid, mOwnerCid)) {
            listener.showNoSpeakMessage(message);
            return;
        }
        if (mPendingMessages.size() >= MAX_PENDING_MESSAGE_COUNT) {
            mPendingMessages.remove(0);
        }
        mPendingMessages.add(message);
        LogUtils.d("静默提醒页面未绑定，消息已缓存等待同会话重新进入");
    }

    /**
     * 按接收顺序补发缓存；仅监听器确认当前仍是同一机器人会话时执行。
     */
    private void dispatchPendingMessages() {
        NoSpeakListener listener = getListener();
        if (listener == null || mPendingMessages.isEmpty()
                || !listener.isNoSpeakPollingAvailable(mOwnerUid, mOwnerCid)) {
            return;
        }
        ArrayList<ZhiChiMessageBase> pendingMessages = new ArrayList<>(mPendingMessages);
        mPendingMessages.clear();
        LogUtils.d("静默提醒同会话重新进入，补显缓存消息数量: " + pendingMessages.size());
        for (ZhiChiMessageBase message : pendingMessages) {
            listener.showNoSpeakMessage(message);
        }
        if (!mIsActive && mPendingMessages.isEmpty()) {
            mDeliveredKeys.clear();
            mOwnerUid = null;
            mOwnerCid = null;
        }
    }

    private static class NoSpeakHandler extends Handler {

        private final WeakReference<SobotAiAgentNoSpeakManager> mManagerReference;

        NoSpeakHandler(SobotAiAgentNoSpeakManager manager) {
            super(Looper.getMainLooper());
            mManagerReference = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            SobotAiAgentNoSpeakManager manager = mManagerReference.get();
            if (manager == null || msg.what != MSG_REQUEST || !(msg.obj instanceof Long)) {
                return;
            }
            manager.request((Long) msg.obj);
        }
    }
}
