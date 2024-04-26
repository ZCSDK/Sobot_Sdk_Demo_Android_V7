package com.sobot.chat.conversation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotQueryFromActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.apiUtils.SobotVerControl;
import com.sobot.chat.api.enumtype.CustomerState;
import com.sobot.chat.api.enumtype.SobotAutoSendMsgMode;
import com.sobot.chat.api.model.BaseCode;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.CommonModelBase;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.OrderCardContentModel;
import com.sobot.chat.api.model.SatisfactionSet;
import com.sobot.chat.api.model.SobotConnCusParam;
import com.sobot.chat.api.model.SobotFaqDetailModel;
import com.sobot.chat.api.model.SobotLocationModel;
import com.sobot.chat.api.model.SobotQueryFormModel;
import com.sobot.chat.api.model.SobotQuestionRecommend;
import com.sobot.chat.api.model.SobotRealuateConfigInfo;
import com.sobot.chat.api.model.SobotSessionPhaseMode;
import com.sobot.chat.api.model.SobotUserTicketInfoFlag;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageCardModel;
import com.sobot.chat.api.model.ZhiChiMessageMsgModel;
import com.sobot.chat.api.model.ZhiChiMessageObjectModel;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.Const;
import com.sobot.chat.core.channel.LimitQueue;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.fragment.SobotBaseFragment;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.AudioTools;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.FileOpenHelper;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.NotificationUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.Util;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.gson.SobotGsonUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Created by jinxl on 2018/2/9.
 */
public abstract class SobotChatBaseFragment extends SobotBaseFragment implements SensorEventListener {

    protected Context mAppContext;

    protected SobotMsgAdapter messageAdapter;

    //消息发送状态
    protected static final int SEND_VOICE = 0;
    protected static final int UPDATE_VOICE = 1;
    protected static final int CANCEL_VOICE = 2;
    protected static final int SEND_TEXT = 0;
    protected static final int UPDATE_TEXT = 1;
    protected static final int UPDATE_TEXT_VOICE = 2;

    //当前客户端模式
    protected int current_client_model = ZhiChiConstant.client_model_robot;
    //客服在线状态
    protected CustomerState customerState = CustomerState.Offline;
    protected ZhiChiInitModeBase initModel;/*初始化成功服务器返回的实体对象*/
    protected Information info;

    protected String currentUserName = "";
    private String adminFace = "";
    private String adminName = "";

    protected boolean isAboveZero = false;//是否咨询过（给机器人或这客服发过消息）
    protected int remindRobotMessageTimes = 0;//机器人的提醒次数
    protected boolean isRemindTicketInfo;//是否已经进行过工单状态提醒

    //
    //防止询前表单接口重复执行
    private boolean isQueryFroming = false;
    //防止重复弹出询前表单
    protected boolean isHasRequestQueryFrom = false;

    //定时器
    protected boolean customTimeTask = false;
    protected boolean userInfoTimeTask = false;
    protected boolean is_startCustomTimerTask = false;
    protected int noReplyTimeUserInfo = 0; // 用户已经无应答的时间
    public int paseReplyTimeUserInfo = 0; // 会话返回山歌界面时  用户计时器暂停时间

    //会话是否锁定 0:默认未收到消息  1: true 锁定；2: false 解锁
    protected int isChatLock = 0;

    private Timer timerUserInfo;
    private TimerTask taskUserInfo;
    /**
     * 客服的定时任务
     */
    protected Timer timerCustom;
    protected TimerTask taskCustom;
    protected int noReplyTimeCustoms = 0;// 客服无应答的时间
    public int paseReplyTimeCustoms = 0;// 会话返回山歌界面时 客服计时器暂停时间
    protected int serviceOutTimeTipCount = 0; // 客服无应答超时提醒次数


    //正在输入监听
    private Timer inputtingListener = null;//用于监听正在输入的计时器
    private boolean isSendInput = false;//防止同时发送正在输入
    private String lastInputStr = "";
    private TimerTask inputTimerTask = null;

    //语音相关
    // 听筒模式转换
    public AudioManager audioManager = null; // 声音管理器
    private AudioFocusRequest mFocusRequest;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;
    private AudioAttributes mAttribute;
    public SensorManager _sensorManager = null; // 传感器管理器
    public Sensor mProximiny = null; // 传感器实例

    //快捷菜单，请求类型
    public int quick_menu_service = 2;
    public int quick_menu_robot = 1;
    public int quick_menu_all = 0;

    public LinearLayout ll_appoint;//聊天界面底部  引用布局 不忍不显示
    public TextView tv_appoint_temp_content;//聊天界面底部布局 引用显示内容
    public ImageView iv_appoint_clear;

    public ZhiChiAppointMessage appointMessage;//引用缓存对象，发送后 清空

    public boolean isOpenUnread;//是否开启已读未读
    public Map<String, ZhiChiMessageBase> unReadMsgIds;//    未读消息的id集合
    public SatisfactionSet mSatisfactionSet;//评价配置
    public SobotRealuateConfigInfo mRealuateConfig;//点踩配置

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAppContext = getContext().getApplicationContext();
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(getActivity());
            // 设置Activity全屏
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(getActivity(), new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding((rect.right > 110 ? 110 : rect.right) + view.getPaddingLeft(), view.getPaddingTop(), (rect.right > 110 ? 110 : rect.right) + view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (initModel != null && customerState == CustomerState.Online && current_client_model == ZhiChiConstant
                .client_model_customService) {
            restartInputListener();
            CommonUtils.sendLocalBroadcast(mAppContext, new Intent(Const.SOBOT_CHAT_CHECK_CONNCHANNEL));
        }
        NotificationUtils.cancleAllNotification(mAppContext);

        if (_sensorManager != null) {
            _sensorManager.registerListener(this, mProximiny, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void finish() {
        if (isActive() && getSobotActivity() != null) {
            getSobotActivity().finish();
        }
    }

    /**
     * fragment是否有效
     *
     * @return
     */
    protected boolean isActive() {
        return isAdded();
    }

    /**
     * 用户的定时任务的处理
     */
    public void startUserInfoTimeTask(final Handler handler) {
        LogUtils.i("--->  startUserInfoTimeTask=====" + isChatLock);
        if (isChatLock == 1) {
            return;
        }
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            if (initModel.isCustomOutTimeFlag()) {
                stopUserInfoTimeTask();
                userInfoTimeTask = true;
                timerUserInfo = new Timer();
                taskUserInfo = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        //sendHandlerUserInfoTimeTaskMessage(handler);
                    }
                };
                timerUserInfo.schedule(taskUserInfo, 1000, 1000);
            }
        }
    }

    public void stopUserInfoTimeTask() {
        userInfoTimeTask = false;
        if (timerUserInfo != null) {
            timerUserInfo.cancel();
            timerUserInfo = null;
        }
        if (taskUserInfo != null) {
            taskUserInfo.cancel();
            taskUserInfo = null;
        }
        noReplyTimeUserInfo = 0;

    }

    /**
     * 设置定时任务
     */
    public void setTimeTaskMethod(Handler handler) {
        if (customerState == CustomerState.Online) {
            //LogUtils.i(" 定时任务的计时的操作：" + current_client_model);
            // 断开我的计时任务
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                if (!is_startCustomTimerTask) {
                    stopUserInfoTimeTask();
                    startCustomTimeTask(handler);
                }
            }
        } else {
            stopCustomTimeTask();
            stopUserInfoTimeTask();
        }
    }

    public void restartMyTimeTask(Handler handler) {
        if (customerState == CustomerState.Online) {
            // 断开我的计时任务
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                if (!is_startCustomTimerTask) {
                    stopUserInfoTimeTask();
                    startCustomTimeTask(handler);
                }
            }
        }
    }

    /**
     * 客服的定时处理
     */
    public void startCustomTimeTask(final Handler handler) {
        if (isChatLock == 1) {
            return;
        }
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            if (initModel.isServiceOutTimeFlag()) {
                if (initModel.isServiceOutCountRule() && serviceOutTimeTipCount >= 1) {
                    //开启 客服超时推送规则，只显示一次 时并且已经提醒过一次 屏蔽之后的操作
                    stopCustomTimeTask();
                    return;
                }
                if (!is_startCustomTimerTask) {
                    stopCustomTimeTask();
                    customTimeTask = true;
                    is_startCustomTimerTask = true;
                    timerCustom = new Timer();
                    taskCustom = new TimerTask() {
                        @Override
                        public void run() {
                            // 需要做的事:发送消息
                            //sendHandlerCustomTimeTaskMessage(handler);
                        }
                    };
                    timerCustom.schedule(taskCustom, 1000, 1000);
                }
            }
        }
    }

    public void stopCustomTimeTask() {
        customTimeTask = false;
        is_startCustomTimerTask = false;
        if (timerCustom != null) {
            timerCustom.cancel();
            timerCustom = null;
        }
        if (taskCustom != null) {
            taskCustom.cancel();
            taskCustom = null;
        }
        noReplyTimeCustoms = 0;

    }


    // ##################### 更新界面的ui ###############################

    /**
     * handler 消息实体message 更新ui界面
     *
     * @param messageAdapter
     * @param msg
     */
    protected void updateUiMessage(SobotMsgAdapter messageAdapter, Message msg) {
        ZhiChiMessageBase myMessage = (ZhiChiMessageBase) msg.obj;
        updateUiMessage(messageAdapter, myMessage);
    }

    protected void updateMessageStatus(SobotMsgAdapter messageAdapter, Message msg) {
        ZhiChiMessageBase myMessage = (ZhiChiMessageBase) msg.obj;
        if (myMessage.getSendSuccessState() == 1 && isOpenUnread) {
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                myMessage.setReadStatus(1);
            }
        }
        messageAdapter.updateDataStateById(myMessage.getId(), myMessage);
        messageAdapter.notifyDataSetChanged();
    }

    protected void updateVoiceStatusMessage(SobotMsgAdapter messageAdapter, Message msg) {
        ZhiChiMessageBase myMessage = (ZhiChiMessageBase) msg.obj;
        if (isOpenUnread) {
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                myMessage.setReadStatus(1);
            }
        }
        messageAdapter.updateVoiceStatusById(myMessage.getId(),
                myMessage.getSendSuccessState(), myMessage.getAnswer().getDuration(), myMessage.getAnswer().getVoiceText(), myMessage.getAnswer().getState(), myMessage.getMessage(), myMessage.getReadStatus());
        messageAdapter.notifyDataSetChanged();
    }

    protected void cancelUiVoiceMessage(SobotMsgAdapter messageAdapter, Message msg) {
        ZhiChiMessageBase myMessage = (ZhiChiMessageBase) msg.obj;
        messageAdapter.cancelVoiceUiById(myMessage.getId());
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * 通过消息实体 zhiChiMessage进行封装
     *
     * @param messageAdapter
     * @param zhichiMessage
     */
    protected void updateUiMessage(SobotMsgAdapter messageAdapter, ZhiChiMessageBase zhichiMessage) {
        messageAdapter.addData(zhichiMessage);
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * 通过消息实体 zhiChiMessage进行封装
     *
     * @param messageAdapter
     * @param zhichiMessage
     */
    protected void updateUiMessageBefore(SobotMsgAdapter messageAdapter, ZhiChiMessageBase zhichiMessage) {
        messageAdapter.addDataBefore(zhichiMessage);
        messageAdapter.notifyDataSetChanged();
    }

    /**
     * @param messageAdapter
     * @param id
     * @param status
     * @param progressBar
     */
    protected void updateUiMessageStatus(SobotMsgAdapter messageAdapter,
                                         String id, int status, int progressBar) {
        int readStatus = 0;
        if (isOpenUnread) {
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                readStatus = 1;
            } else {
                readStatus = 2;
            }
        }
        messageAdapter.updateMsgInfoById(id, status, progressBar, readStatus);
        messageAdapter.notifyDataSetChanged();
    }

    // ##################### 更新界面的ui ###############################

    protected String getAdminFace() {
        return this.adminFace;
    }

    protected void setAdminFace(String str) {
        LogUtils.i("客服头像地址是" + str);
        this.adminFace = str;
    }

    protected String getAdminName() {
        return this.adminName;
    }

    protected void setAdminName(String str) {
        LogUtils.i("客服名字是" + str);
        this.adminName = str;
    }

    /**
     * @param context
     * @param initModel
     * @param handler
     * @param current_client_model
     */
    protected void sendMessageWithLogic(String msgId, String context,
                                        ZhiChiInitModeBase initModel, final Handler handler, int current_client_model, int questionFlag, String question) {
        if (ZhiChiConstant.client_model_robot == current_client_model) { // 客户和机械人进行聊天
            sendHttpRobotMessage(msgId, context, initModel.getPartnerid(),
                    initModel.getCid(), "", handler, questionFlag, question, info.getLocale(), "");
            LogUtils.i("机器人模式");
        } else if (ZhiChiConstant.client_model_customService == current_client_model) {
            sendHttpCustomServiceMessage(context, initModel.getPartnerid(),
                    initModel.getCid(), handler, msgId);
            LogUtils.i("客服模式");
        }
    }

    // 人与机械人进行聊天
    protected void sendHttpRobotMessage(final String msgId, String requestText,
                                        String uid, String cid, final String fromEnum, final Handler handler, int questionFlag, String question, String serverInternationalLanguage, final String fromQuickMenuType) {
        sendHttpRobotMessage("", msgId, requestText,
                uid, cid, fromEnum, handler, questionFlag, question, serverInternationalLanguage, fromQuickMenuType);
    }

    // 人与机械人进行聊天 发送图片、视频、文件
    protected void sendHttpRobotMessage(String msgType, final String msgId, final String requestText,
                                        String uid, final String cid, final String fromEnum, final Handler handler, final int questionFlag, final String question, String serverInternationalLanguage, final String fromQuickMenuType) {
        final Map<String, String> params = new HashMap<>();
        String appointMessageStr = "";
        if (appointMessage != null) {
            appointMessageStr = SobotGsonUtil.beanToJson(appointMessage);
            if (!TextUtils.isEmpty(appointMessageStr)) {
                params.put("appointMessageVO", appointMessageStr);
            }
        }
        params.put("msgType", msgType);
        params.put("adminId", info.getChoose_adminid());//指定客服
        params.put("tranFlag", info.getTranReceptionistFlag() + "");//是否必转该指定客服
        params.put("groupId", info.getGroupid());//指定技能组
        params.put("transferAction", info.getTransferAction());//指定溢出策略
        //快捷菜单自定义
        if (!StringUtils.isEmpty(fromEnum)) {
            params.put("fromEnum", fromEnum);//快捷菜单来源
        }
        String platformUnionCode = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(platformUnionCode)) {
            String flowCompanyId = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_FLOW_COMPANYID, "");
            if (!TextUtils.isEmpty(flowCompanyId)) {
                //是否可以溢出
                String flowType = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_FLOW_TYPE, "");
                // 是否溢出到主商户 0-不溢出 , 1-全部溢出，2-忙碌时溢出，3-不在线时溢出,默认不溢出
                params.put("flowType", flowType);
                //溢出公司id
                params.put("flowCompanyId", flowCompanyId);
                String flowGroupId = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_FLOW_GROUPID, "");
                //溢出groupid
                params.put("flowGroupId", flowGroupId);
            }
        }
        if ("28".equals(msgType)) {
            params.put("question", question);
            params.put("requestText", requestText);
        }
        zhiChiApi.chatSendMsgToRoot(initModel.getAdminReadFlag(), initModel.getAiStatus() == 1 ? 30 : 10, initModel.getRobotid(), requestText, questionFlag, question, uid, cid, msgId, params,
                new StringResultCallBack<ZhiChiMessageBase>() {
                    @Override
                    public void onSuccess(ZhiChiMessageBase simpleMessage) {
                        if (!isActive()) {
                            return;
                        }
                        if (isOpenUnread) {
                            simpleMessage.setReadStatus(1);
                            unReadMsgIds.put(simpleMessage.getMsgId(), simpleMessage);
                        }
                        if (initModel != null && initModel.getMsgAppointFlag() == 1 && simpleMessage.getAnswer() != null && StringUtils.isNoEmpty(simpleMessage.getAnswer().getMessage())) {
                            //引用开启
                            simpleMessage.setMessage(simpleMessage.getAnswer().getMessage());
                        }
                        if (!StringUtils.isEmpty(fromEnum)) {
                            simpleMessage.setFromQuickMenuType(fromQuickMenuType);
                        }
                        if (simpleMessage != null && simpleMessage.getSentisive() == 1) {
                            isAboveZero = true;
                            sendTextMessageToHandler(msgId, null, handler, 1, UPDATE_TEXT, simpleMessage.getSentisive(), simpleMessage.getSentisiveExplain());
                        } else {
                            String id = System.currentTimeMillis() + "";
                            if (StringUtils.isNoEmpty(simpleMessage.getMsgId())) {
                                id = simpleMessage.getMsgId();
                            }
                            if (simpleMessage.getUstatus() == ZhiChiConstant.result_fail_code) {
                                sendTextMessageToHandler(msgId, null, handler, 1, UPDATE_TEXT);
                                //机器人超时下线
                                customerServiceOffline(initModel, 4);
                            } else if (simpleMessage.getUstatus() == 1) {
                                // 发送失败
                                sendTextMessageToHandler(msgId, null, handler, 0, UPDATE_TEXT);
                                LogUtils.i("应该是人工状态给机器人发消息拦截,连接通道，修改当前模式为人工模式");
                                ZCSobotApi.checkIMConnected(getSobotActivity(), info.getPartnerid());
                                current_client_model = ZhiChiConstant.client_model_customService;
                            } else {
                                clearAppointUI();
                                // 机械人的回答语
                                sendTextMessageToHandler(msgId, null, simpleMessage.getDesensitizationWord(), handler, 1, UPDATE_TEXT, 0, "");
                                isAboveZero = true;
                                simpleMessage.setId(id);
                                simpleMessage.setSenderName(initModel.getRobotName());
                                simpleMessage.setSender(initModel.getRobotName());
                                simpleMessage.setSenderFace(initModel.getRobotLogo());
                                simpleMessage.setSenderType(ZhiChiConstant.message_sender_type_robot);
                                Message message = handler.obtainMessage();
                                message.what = ZhiChiConstant.hander_robot_message;
                                message.obj = simpleMessage;
                                handler.sendMessage(message);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        if (!isActive()) {
                            return;
                        }
                        //LogUtils.e("sendHttpRobotMessage:e= " +e.toString() + des);
                        // 显示信息发送失败
                        sendTextMessageToHandler(msgId, null, handler, 0, UPDATE_TEXT);
                    }
                });
    }

    protected void sendHttpCustomServiceMessage(final String content, final String uid,
                                                String cid, final Handler handler, final String mid) {
        String appointMessageStr = "";
        if (appointMessage != null) {
            appointMessageStr = SobotGsonUtil.beanToJson(appointMessage);
        }

        zhiChiApi.sendMsgToCoutom(initModel.getReadFlag(), content, appointMessageStr, mid, uid, cid, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase commonModelBase) {
                if (!isActive()) {
                    return;
                }
                Boolean switchFlag = Boolean.valueOf(commonModelBase.getSwitchFlag()).booleanValue();
                //如果switchFlag 为true，就会断开通道走轮询
                if (switchFlag) {
                    Map<String, String> map = new HashMap<>();
                    if (!CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
                        map.put("TCPServer 运行情况", "没运行，直接走fragment 界面的轮询");
                    } else {
                        map.put("TCPServer 运行情况", "在运行");
                    }
                    map.put("commonModelBase", commonModelBase.toString());
                    LogUtils.i2Local("开启轮询 fragment ", "switchFlag=" + switchFlag + " " + map.toString());
                    //不管是什么方式（service 还是定时器里边的轮询），至少先执行一次轮询接口
                    pollingMsgForOne();
                    try {
                        //上传日志
                        SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().logCollect(getSobotActivity(), SharedPreferencesUtil.getAppKey(getSobotActivity(), ""), true);
                    } catch (Exception e) {
                    }
                    if (!CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
                        LogUtils.i2Local("开启轮询", "SobotTCPServer 没运行，直接走fragment 界面的轮询");
                        SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().disconnChannel();
                        //SobotTCPServer不存在，直接走定时器轮询
                        if (!inPolling) {
                            startPolling();
                        }
                    } else {
                        LogUtils.i2Local("开启轮询", "SobotTCPServer 在运行");
                        // SobotTCPServer存在，通过广播方式告知要切换轮询
                        CommonUtils.sendLocalBroadcast(mAppContext, new Intent(Const.SOBOT_CHAT_CHECK_SWITCHFLAG));
                    }
                } else {
                    if (!CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
//                        LogUtils.i("----人工状态 SobotTCPServer 被杀死了");
                        zhiChiApi.reconnectChannel();
                    } else {
                        CommonUtils.sendLocalBroadcast(mAppContext, new Intent(Const.SOBOT_CHAT_CHECK_CONNCHANNEL));
                    }
                }
                if (commonModelBase.getSentisive() == 1) {
                    isAboveZero = true;
                    sendTextMessageToHandler(mid, null, handler, 1, UPDATE_TEXT, commonModelBase.getSentisive(), commonModelBase.getSentisiveExplain());
                } else {
                    if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(commonModelBase.getStatus())) {
                        sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
                        customerServiceOffline(initModel, 1);
                    } else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(commonModelBase.getStatus())) {
                        clearAppointUI();
                        if (!TextUtils.isEmpty(mid)) {
                            isAboveZero = true;
                            // 当发送成功的时候更新ui界面
                            sendTextMessageToHandler(mid, null, commonModelBase.getDesensitizationWord(), handler, 1, UPDATE_TEXT, 0, "");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                //LogUtils.e("sendHttpCustomServiceMessage:e= " + e.toString());
                sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
            }
        });
    }

    /**
     * 发送卡片信息
     *
     * @param consultingContent
     * @param uid
     * @param cid
     * @param handler
     * @param msgId
     */
    protected void sendHttpCardMsg(final String uid,
                                   String cid, final Handler handler, final String msgId, final ConsultingContent consultingContent) {
        zhiChiApi.sendCardMsg(initModel.getReadFlag(), consultingContent, uid, cid, msgId, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase commonModelBase) {
                if (!isActive()) {
                    return;
                }
                if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(commonModelBase.getStatus())) {
//                    sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
                    customerServiceOffline(initModel, 1);
                } else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(commonModelBase.getStatus())) {
                    if (!TextUtils.isEmpty(msgId)) {
                        isAboveZero = true;
                        // 当发送成功的时候更新ui界面
                        ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
                        myMessage.setId(msgId);
                        myMessage.setMsgId(msgId);
                        myMessage.setSenderName(info.getUser_nick());
                        myMessage.setSenderFace(info.getFace());
                        myMessage.setConsultingContent(consultingContent);
                        myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                        myMessage.setSendSuccessState(1);
                        myMessage.setReadStatus(isOpenUnread ? 1 : 0);
                        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
                        answer.setMsgType(ZhiChiConstant.message_type_card);
                        myMessage.setAnswer(answer);
                        myMessage.setT(Calendar.getInstance().getTime().getTime() + "");
                        if (initModel != null && initModel.getMsgAppointFlag() == 1) {
                            //引用开启
                            ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
                            messageMsgModel.setMsgType(5);
                            ZhiChiMessageObjectModel objectModel = new ZhiChiMessageObjectModel();
                            objectModel.setType(3);
                            ZhiChiMessageCardModel cardModel = new ZhiChiMessageCardModel();
                            cardModel.setDescription(consultingContent.getSobotGoodsDescribe());
                            cardModel.setLabel(consultingContent.getSobotGoodsLable());
                            cardModel.setThumbnail(consultingContent.getSobotGoodsImgUrl());
                            cardModel.setUrl(consultingContent.getSobotGoodsFromUrl());
                            cardModel.setTitle(consultingContent.getSobotGoodsTitle());
                            objectModel.setMsg(cardModel);
                            messageMsgModel.setContent(objectModel);
                            myMessage.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
                        }
                        Message handMyMessage = handler.obtainMessage();
                        handMyMessage.what = ZhiChiConstant.hander_send_msg;
                        handMyMessage.obj = myMessage;
                        handler.sendMessage(handMyMessage);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                final Map<String, String> map = new HashMap<>();
                String cotent = e.toString() + des;
                map.put("sendHttpCardMsg", cotent);
                LogUtils.i("sendHttpCardMsg error:" + e.toString());
//                sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
            }
        });
    }

    /**
     * 发送信息给客服,可以指定消息类型
     *
     * @param handler
     * @param msgContent
     * @param msgType
     * @param myMessage
     */
    protected void sendMsgToCustomService(final Handler handler, String msgContent, String msgType,String msgId, final ZhiChiMessageBase myMessage) {
        if (initModel == null) {
            return;
        }
        zhiChiApi.sendMsgToCustomService(initModel.getReadFlag(), msgContent, msgType, initModel.getPartnerid(), initModel.getCid(), msgId,new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase commonModelBase) {
                if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(commonModelBase.getStatus())) {
                    customerServiceOffline(initModel, 1);
                } else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(commonModelBase.getStatus())) {
                    // 当发送成功的时候更新ui界面
                    if (myMessage != null) {
                        updateMsgToHandler(myMessage.getMsgId(), handler, ZhiChiConstant.MSG_SEND_STATUS_SUCCESS);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (myMessage != null) {
                    updateMsgToHandler(myMessage.getMsgId(), handler, ZhiChiConstant.MSG_SEND_STATUS_ERROR);
                }
                LogUtils.i("sendMsgToCustomService error:" + e.toString());
            }
        });
    }

    /**
     * 发送订单卡片信息
     *
     * @param orderCardContent
     * @param uid
     * @param cid
     * @param handler
     * @param mid
     */
    protected void sendHttpOrderCardMsg(final String uid,
                                        String cid, final Handler handler, final String mid, final OrderCardContentModel orderCardContent) {
        zhiChiApi.sendOrderCardMsg(initModel.getReadFlag(), orderCardContent, uid, cid, mid, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase commonModelBase) {
                if (!isActive()) {
                    return;
                }
                if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(commonModelBase.getStatus())) {
//                    sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
                    customerServiceOffline(initModel, 1);
                } else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(commonModelBase.getStatus())) {
                    if (!TextUtils.isEmpty(mid)) {
                        isAboveZero = true;
                        // 当发送成功的时候更新ui界面
                        ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
                        myMessage.setSenderName(info.getUser_nick());
                        myMessage.setSenderFace(info.getFace());
                        myMessage.setId(mid);
                        myMessage.setMsgId(mid);
                        myMessage.setOrderCardContent(orderCardContent);
                        myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                        myMessage.setSendSuccessState(1);
                        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
                        answer.setMsgType(ZhiChiConstant.message_type_ordercard);
                        myMessage.setAnswer(answer);
                        if (isOpenUnread) {
                            myMessage.setReadStatus(1);
                        }
                        myMessage.setT(Calendar.getInstance().getTime().getTime() + "");
                        Message handMyMessage = handler.obtainMessage();
                        handMyMessage.what = ZhiChiConstant.hander_send_msg;
                        handMyMessage.obj = myMessage;
                        handler.sendMessage(handMyMessage);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                LogUtils.i("sendHttpOrderCardMsg error:" + e.toString());
//                sendTextMessageToHandler(mid, null, handler, 0, UPDATE_TEXT);
            }
        });
    }


    protected void uploadFile(File selectedFile, Handler handler, final ListView lv_message,
                              final SobotMsgAdapter messageAdapter, boolean isCamera) {
        if (selectedFile != null && selectedFile.exists()) {
            // 发送文件
            int readFlag;
            if (current_client_model == ZhiChiConstant.client_model_customService) {
                readFlag = initModel.getReadFlag();
            } else {
                readFlag = initModel.getAdminReadFlag();
            }
            LogUtils.i(selectedFile.toString());
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".gif") || fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                ChatUtils.sendPicLimitBySize(isOpenUnread ? 1 : 0, selectedFile.getAbsolutePath(), initModel.getCid(),
                        initModel.getPartnerid(), handler, getSobotActivity(), lv_message, messageAdapter, isCamera, current_client_model, initModel);
            } else {
                if (selectedFile.length() > 50 * 1024 * 1024) {
                    ToastUtil.showToast(getContext(), getResources().getString(R.string.sobot_file_upload_failed));
                    return;
                }
                //不能上传可执行文件 （.exe、.sys、 .com、.bat、.dll、.sh、.py）
                if (FileOpenHelper.checkEndsWithInStringArray(fileName, getContext(), "sobot_fileEndingAll")) {
                    return;
                }
                String tmpMsgId = getMsgId();
                LogUtils.i("tmpMsgId:" + tmpMsgId);
                zhiChiApi.addUploadFileTask(readFlag, false, tmpMsgId, initModel.getPartnerid(), initModel.getCid(), selectedFile.getAbsolutePath(), null, current_client_model);
                updateUiMessage(messageAdapter, ChatUtils.getUploadFileModel(getContext(), readFlag, tmpMsgId, selectedFile, info));
                isAboveZero = true;
            }
        }
    }

    protected void sendLocation(String msgId, SobotLocationModel data, final Handler handler, boolean isNewMsg) {
        if (!isActive() || initModel == null
                || current_client_model != ZhiChiConstant.client_model_customService) {
            return;
        }
        if (isNewMsg) {
            msgId = getMsgId();
            sendNewMsgToHandler(ChatUtils.getLocationModel(initModel.getReadFlag(), msgId, data, info, initModel), handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
        } else {
            if (TextUtils.isEmpty(msgId)) {
                return;
            }
            updateMsgToHandler(msgId, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
        }
        final String finalMsgId = msgId;
        zhiChiApi.sendLocation(initModel.getReadFlag(), SobotChatBaseFragment.this, data, initModel.getPartnerid(), initModel.getCid(), finalMsgId, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase commonModelBase) {
                if (!isActive()) {
                    return;
                }
                if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(commonModelBase.getStatus())) {
                    updateMsgToHandler(finalMsgId, handler, ZhiChiConstant.MSG_SEND_STATUS_ERROR);
                    customerServiceOffline(initModel, 1);
                } else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(commonModelBase.getStatus())) {
                    if (!TextUtils.isEmpty(finalMsgId)) {
                        isAboveZero = true;
                        updateMsgToHandler(finalMsgId, handler, ZhiChiConstant.MSG_SEND_STATUS_SUCCESS);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                updateMsgToHandler(finalMsgId, handler, ZhiChiConstant.MSG_SEND_STATUS_ERROR);
            }
        });

    }

    protected void sendMuitidiaLeaveMsg(String msgId, String data, final Handler handler, boolean isNewMsg) {
        if (!isActive() || initModel == null) {
            return;
        }
        if (isNewMsg) {
            msgId = System.currentTimeMillis() + "";
            sendNewMsgToHandler(ChatUtils.getMuitidiaLeaveMsgModel(msgId, data, info), handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
        } else {
            if (TextUtils.isEmpty(msgId)) {
                return;
            }
            updateMsgToHandler(msgId, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
        }
        final String finalMsgId = msgId;
        zhiChiApi.insertSysMsg(SobotChatBaseFragment.this, initModel.getCid(), initModel.getPartnerid(), data.replace("\n", "<br/>"), "多轮对话工单提交确认提示", new StringResultCallBack<BaseCode>() {
            @Override
            public void onSuccess(BaseCode baseCode) {
                if (!isActive()) {
                    return;
                }
                updateMsgToHandler(finalMsgId, handler, ZhiChiConstant.MSG_SEND_STATUS_SUCCESS);
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                updateMsgToHandler(finalMsgId, handler, ZhiChiConstant.MSG_SEND_STATUS_ERROR);
            }
        });

    }

    protected void uploadVideo(File videoFile, Uri fileUri, SobotMsgAdapter messageAdapter) {
        String tmpMsgId = getMsgId();
        LogUtils.i("tmpMsgId:" + tmpMsgId);
        String fName = MD5Util.encode(videoFile.getAbsolutePath());
        String filePath = null;
        try {
            filePath = FileUtil.saveImageFile(getSobotActivity(), fileUri, fName + FileUtil.getFileEndWith(videoFile.getAbsolutePath()), videoFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(getSobotActivity(), getResources().getString(R.string.sobot_pic_type_error));
            return;
        }
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(filePath);//path 本地视频的路径
        Bitmap bitmap = media.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        String snapshotPath = "";
        if (bitmap != null) {
            snapshotPath = FileUtil.saveBitmap(100, bitmap);
        }
        int readFlag;
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            readFlag = initModel.getReadFlag();
        } else {
            readFlag = initModel.getAdminReadFlag();
        }
        zhiChiApi.addUploadFileTask(readFlag, true, tmpMsgId, initModel.getPartnerid(), initModel.getCid(), filePath, snapshotPath, current_client_model);
        updateUiMessage(messageAdapter, ChatUtils.getUploadVideoModel(getContext(), readFlag, tmpMsgId, new File(filePath), snapshotPath, info));
        isAboveZero = true;
    }


    /**
     * 文本通知
     *
     * @param id
     * @param msgContent
     * @param handler
     * @param isSendStatus 0 失败  1成功  2 正在发送
     * @param updateStatus
     */
    protected void sendTextMessageToHandler(String id, String msgContent,
                                            Handler handler, int isSendStatus, int updateStatus) {
        sendTextMessageToHandler(id, msgContent,
                handler, isSendStatus, updateStatus, 0, "");
    }

    /**
     * 文本通知
     *
     * @param id
     * @param msgContent
     * @param handler
     * @param isSendStatus 0 失败  1成功  2 正在发送
     * @param updateStatus
     */
    protected void sendTextMessageToHandler(String id, String msgContent,
                                            Handler handler, int isSendStatus, int updateStatus, int sentisive, String sentisiveExplain) {
        sendTextMessageToHandler(id, msgContent, "",
                handler, isSendStatus, updateStatus, sentisive, sentisiveExplain);
    }

    /**
     * 文本通知
     *
     * @param msgId
     * @param msgContent
     * @param handler
     * @param isSendStatus 0 失败  1成功  2 正在发送
     * @param updateStatus
     */
    protected void sendTextMessageToHandler(String msgId, String msgContent, String desensitizationWord,
                                            Handler handler, int isSendStatus, int updateStatus, int sentisive, String sentisiveExplain) {
        ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
        myMessage.setId(msgId);
        myMessage.setMsgId(msgId);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        if (!TextUtils.isEmpty(msgContent)) {
            msgContent = msgContent.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace
                    ("\n", "<br/>").replace("&lt;br/&gt;", "<br/>");
            reply.setMsg(msgContent);
        } else {
            reply.setMsg(msgContent);
        }
        myMessage.setAnswer(reply);
        myMessage.setDesensitizationWord(desensitizationWord);
        myMessage.setSenderName(info.getUser_nick());
        myMessage.setSenderFace(info.getFace());
        myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);

        myMessage.setSendSuccessState(isSendStatus);
        if (initModel != null && initModel.getMsgAppointFlag() == 1) {
            //引用开启
            ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
            messageMsgModel.setMsgType(0);
            messageMsgModel.setContent(msgContent);
            myMessage.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
        }
        if (appointMessage != null) {
            reply.setMsgType(ZhiChiConstant.message_type_appoint_msg);
            myMessage.setAppointMessage(appointMessage);
        } else {
            reply.setMsgType(ZhiChiConstant.message_type_text);
        }
        myMessage.setT(Calendar.getInstance().getTime().getTime() + "");
        myMessage.setSentisive(sentisive);
        myMessage.setSentisiveExplain(sentisiveExplain);
        Message handMyMessage = handler.obtainMessage();
        switch (updateStatus) {
            case SEND_TEXT:
                handMyMessage.what = ZhiChiConstant.hander_send_msg;
                break;
            case UPDATE_TEXT:
                handMyMessage.what = ZhiChiConstant.hander_update_msg_status;
                break;
            case UPDATE_TEXT_VOICE:
                handMyMessage.what = ZhiChiConstant.update_send_data;
                break;
        }

        handMyMessage.obj = myMessage;
        handler.sendMessage(handMyMessage);
    }

    /**
     * 新增消息
     *
     * @param messageData
     * @param handler
     * @param updateStatus ZhiChiConstant.MSG_SEND_STATUS_SUCCESS
     *                     ZhiChiConstant.MSG_SEND_STATUS_LOADING
     *                     ZhiChiConstant.MSG_SEND_STATUS_ERROR
     */
    protected void sendNewMsgToHandler(ZhiChiMessageBase messageData, Handler handler, int updateStatus) {
        if (messageData == null) {
            return;
        }

        Message message = handler.obtainMessage();
        messageData.setSendSuccessState(updateStatus);
        message.what = ZhiChiConstant.hander_send_msg;
        message.obj = messageData;
        handler.sendMessage(message);
    }

    /**
     * 更新消息状态
     *
     * @param id
     * @param handler
     * @param updateStatus ZhiChiConstant.MSG_SEND_STATUS_SUCCESS
     *                     ZhiChiConstant.MSG_SEND_STATUS_LOADING
     *                     ZhiChiConstant.MSG_SEND_STATUS_ERROR
     */
    protected void updateMsgToHandler(String id, Handler handler, int updateStatus) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        ZhiChiMessageBase messageData = new ZhiChiMessageBase();
        messageData.setId(id);
        messageData.setMsgId(id);
        messageData.setSendSuccessState(updateStatus);
        Message message = handler.obtainMessage();
        message.what = ZhiChiConstant.hander_update_msg_status;
        message.obj = messageData;
        handler.sendMessage(message);
    }

    /**
     * 发送语音消息
     *
     * @param voiceMsgId
     * @param voiceTimeLongStr
     * @param cid
     * @param uid
     * @param filePath
     * @param handler
     */
    protected void sendVoice(final String voiceMsgId, final String voiceTimeLongStr,
                             String cid, String uid, final String filePath, final Handler handler) {
        if (current_client_model == ZhiChiConstant.client_model_robot) {
            zhiChiApi.sendVoiceToRobot(filePath, voiceMsgId, uid, cid, initModel.getRobotid(), voiceTimeLongStr, "msgType", new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    if (!isActive()) {
                        return;
                    }
                    LogUtils.i("发送给机器人语音---sobot---" + zhiChiMessage.getMsg());
                    // 语音发送成功
                    String id = System.currentTimeMillis() + "";
                    isAboveZero = true;
                    restartMyTimeTask(handler);
                    if (!TextUtils.isEmpty(zhiChiMessage.getMsg())) {
                        sendTextMessageToHandler(voiceMsgId, zhiChiMessage.getMsg(), handler, 1, UPDATE_TEXT_VOICE);//语音通过服务器转为文字，发送给页面
                    } else {
                        sendVoiceMessageToHandler(voiceMsgId, filePath, voiceTimeLongStr, 1, UPDATE_VOICE, handler);
                    }

                    ZhiChiMessageBase simpleMessage = zhiChiMessage.getData();
                    if (simpleMessage.getUstatus() == ZhiChiConstant.result_fail_code) {
                        //机器人超时下线
                        customerServiceOffline(initModel, 4);
                    } else {
                        isAboveZero = true;
                        simpleMessage.setId(id);
                        simpleMessage.setMsgId(id);
                        simpleMessage.setSenderName(initModel.getRobotName());
                        simpleMessage.setSender(initModel.getRobotName());
                        simpleMessage.setSenderFace(initModel.getRobotLogo());
                        simpleMessage.setSenderType(ZhiChiConstant.message_sender_type_robot);
                        Message message = handler.obtainMessage();
                        message.what = ZhiChiConstant.hander_robot_message;
                        message.obj = simpleMessage;
                        handler.sendMessage(message);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    if (!isActive()) {
                        return;
                    }
                    LogUtils.i("发送语音error:" + des + "exception:" + e.toString());
                    sendVoiceMessageToHandler(voiceMsgId, filePath, voiceTimeLongStr, 0, UPDATE_VOICE, handler);
                }

                @Override
                public void onLoading(long total, long current,
                                      boolean isUploading) {

                }
            });
        } else if (current_client_model == ZhiChiConstant.client_model_customService) {
            LogUtils.i("发送给人工语音---sobot---" + filePath);
            zhiChiApi.sendFile(initModel.getReadFlag(), voiceMsgId, cid, uid, filePath, voiceTimeLongStr, current_client_model,
                    new ResultCallBack<ZhiChiMessage>() {
                        @Override
                        public void onSuccess(ZhiChiMessage zhiChiMessage) {
                            if (!isActive()) {
                                return;
                            }
                            // 语音发送成功
                            isAboveZero = true;
                            restartMyTimeTask(handler);
                            if (ZhiChiConstant.result_success_code == Integer
                                    .parseInt(zhiChiMessage.getCode())) {
                                if (1 == Integer
                                        .parseInt(zhiChiMessage.getData().getStatus())) {
                                    String voiceText = "";
                                    int changeState = -1;
                                    if (null != zhiChiMessage.getData().getSdkMsg() && null != zhiChiMessage.getData().getSdkMsg().getAnswer()) {
                                        voiceText = zhiChiMessage.getData().getSdkMsg().getAnswer().getVoiceText();
                                        changeState = zhiChiMessage.getData().getSdkMsg().getAnswer().getState();
                                    }
                                    sendVoiceMessageSuccessToHandler(voiceMsgId, filePath, voiceTimeLongStr, 1, UPDATE_VOICE, voiceText, changeState, handler, zhiChiMessage.getData().getMessage());
                                } else {
                                    sendVoiceMessageToHandler(voiceMsgId, filePath, voiceTimeLongStr, 0, UPDATE_VOICE, handler);
                                    if (!TextUtils.isEmpty(zhiChiMessage.getMsg())) {
                                        ToastUtil.showToast(getSobotActivity(), zhiChiMessage.getMsg());
                                    }
                                }
                            } else {
                                sendVoiceMessageToHandler(voiceMsgId, filePath, voiceTimeLongStr, 0, UPDATE_VOICE, handler);
                                if (!TextUtils.isEmpty(zhiChiMessage.getMsg())) {
                                    ToastUtil.showToast(getSobotActivity(), zhiChiMessage.getMsg());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String des) {
                            if (!isActive()) {
                                return;
                            }
                            LogUtils.i("发送语音error:" + des + "exception:" + e.toString());
                            sendVoiceMessageToHandler(voiceMsgId, filePath, voiceTimeLongStr, 0, UPDATE_VOICE, handler);
                        }

                        @Override
                        public void onLoading(long total, long current,
                                              boolean isUploading) {

                        }
                    });
        }

    }

    /**
     * @param voiceMsgId       语音暂时产生唯一标识符
     * @param voiceUrl         语音的地址
     * @param voiceTimeLongStr 语音的时长
     * @param isSendSuccess
     * @param state            发送状态
     * @param handler
     */
    protected void sendVoiceMessageToHandler(String voiceMsgId, String voiceUrl,
                                             String voiceTimeLongStr, int isSendSuccess, int state,
                                             final Handler handler) {

        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(voiceUrl);
        reply.setDuration(voiceTimeLongStr);
        zhichiMessage.setT(Calendar.getInstance().getTime().getTime() + "");
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_send_voice);
        zhichiMessage.setId(voiceMsgId);
        zhichiMessage.setMsgId(voiceMsgId);
        zhichiMessage.setSendSuccessState(isSendSuccess);
        Message message = handler.obtainMessage();
        if (state == UPDATE_VOICE) {// 更新界面布局
            message.what = ZhiChiConstant.message_type_update_voice;
        } else if (state == CANCEL_VOICE) {
            message.what = ZhiChiConstant.message_type_cancel_voice;
        } else if (state == SEND_VOICE) {
            message.what = ZhiChiConstant.hander_send_msg;
        }

        message.obj = zhichiMessage;
        handler.sendMessage(message);
    }

    /**
     * @param voiceMsgId       语音暂时产生唯一标识符
     * @param voiceUrl         语音的地址
     * @param voiceTimeLongStr 语音的时长
     * @param isSendSuccess
     * @param state            发送状态
     * @param handler
     */
    protected void sendVoiceMessageSuccessToHandler(String voiceMsgId, String voiceUrl,
                                                    String voiceTimeLongStr, int isSendSuccess, int state, String voiceText, int changeState,
                                                    final Handler handler, String messageStr) {

        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(voiceUrl);
        reply.setDuration(voiceTimeLongStr);
        reply.setVoiceText(voiceText);
        reply.setState(changeState);
        zhichiMessage.setT(Calendar.getInstance().getTime().getTime() + "");
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_send_voice);
        zhichiMessage.setId(voiceMsgId);
        zhichiMessage.setMsgId(voiceMsgId);
        zhichiMessage.setSendSuccessState(isSendSuccess);
        if (initModel != null && initModel.getMsgAppointFlag() == 1) {
            //引用开启
            if (StringUtils.isNoEmpty(messageStr)) {
                zhichiMessage.setMessage(messageStr);
            }
        }
        Message message = handler.obtainMessage();
        if (state == UPDATE_VOICE) {// 更新界面布局
            message.what = ZhiChiConstant.message_type_update_voice;
        } else if (state == CANCEL_VOICE) {
            message.what = ZhiChiConstant.message_type_cancel_voice;
        } else if (state == SEND_VOICE) {
            message.what = ZhiChiConstant.hander_send_msg;
        }

        message.obj = zhichiMessage;
        handler.sendMessage(message);
    }

    /**
     * 重置输入预知
     */
    protected void restartInputListener() {
        stopInputListener();
        startInputListener();
    }

    //开启正在输入的监听
    protected void startInputListener() {
        inputtingListener = new Timer();
        inputTimerTask = new TimerTask() {
            @Override
            public void run() {
                //人工模式并且没有发送的时候
                if (customerState == CustomerState.Online && current_client_model == ZhiChiConstant.client_model_customService && !isSendInput) {
                    //获取对话
                    try {
                        String content = getSendMessageStr();
                        if (!TextUtils.isEmpty(content) && !content.equals(lastInputStr)) {
                            lastInputStr = content;
                            isSendInput = true;
                            //发送接口
                            zhiChiApi.input(initModel.getPartnerid(), content, new StringResultCallBack<CommonModel>() {
                                @Override
                                public void onSuccess(CommonModel result) {
                                    isSendInput = false;
                                }

                                @Override
                                public void onFailure(Exception e, String des) {
                                    isSendInput = false;
                                }
                            });
                        }
                    } catch (Exception e) {
//						e.printStackTrace();
                    }
                }
            }
        };
        // 500ms进行定时任务
        inputtingListener.schedule(inputTimerTask, 0, initModel.getInputTime() * 1000);
    }

    protected void stopInputListener() {
        if (inputtingListener != null) {
            inputtingListener.cancel();
            inputtingListener = null;
        }
    }

    // 设置听筒模式或者是正常模式的转换
    public void initAudioManager() {
        if (audioManager == null)
            audioManager = (AudioManager) getSobotActivity().getSystemService(Context.AUDIO_SERVICE);
        if (_sensorManager == null)
            _sensorManager = (SensorManager) getSobotActivity().getSystemService(Context.SENSOR_SERVICE);

        if (_sensorManager != null) {
            mProximiny = _sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            _sensorManager.registerListener(this, mProximiny, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(true);// 打开扬声器
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        // TBD 继续播放
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // TBD 停止播放
                        if (AudioTools.getInstance().isPlaying()) {
                            AudioTools.getInstance().stop();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // TBD 暂停播放
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // TBD 混音播放
                        break;
                    default:
                        break;
                }

            }
        };
        //android 版本 5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAttribute = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
        }
        //android 版本 8.0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setWillPauseWhenDucked(true)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener, new Handler())
                    .setAudioAttributes(mAttribute)
                    .build();
        }
    }

    //请求音频焦点
    public void requestAudioFocus() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
            return;
        }
        if (audioManager == null)
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mFocusRequest != null)
                    audioManager.requestAudioFocus(mFocusRequest);
            } else {
                if (audioFocusChangeListener != null)
                    //AUDIOFOCUS_GAIN_TRANSIENT 只是短暂获得，一会就释放焦点
                    audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        }

    }

    //放弃音频焦点
    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1) {
            return;
        }
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mFocusRequest != null)
                    audioManager.abandonAudioFocusRequest(mFocusRequest);
            } else {
                if (audioFocusChangeListener != null)
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
            }
            audioManager = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isHeadphonesPlugged()) {
            // 如果耳机已插入，设置距离传感器失效
//            ToastUtil.showToast(getSobotActivity(),"耳机模式");
            return;
        }
        try {
            // 当前传感器距离
            float f_proximiny = event.values[0];
            if (f_proximiny >= mProximiny.getMaximumRange()) {
                audioManager.setSpeakerphoneOn(true);// 打开扬声器
                audioManager.setMode(AudioManager.MODE_NORMAL);
                LogUtils.i("监听模式的转换：" + "正常模式");
            } else {
                LogUtils.i("监听模式的转换：" + "听筒模式");
                audioManager.setSpeakerphoneOn(false);// 关闭扬声器
                // 把声音设定成Earpiece（听筒）出来，设定为正在通话中
                //5.0以上
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            }
        } catch (Exception e) {
//			e.printStackTrace();
        }
    }

    /**
     * 判断是否是耳机播放
     */
    private boolean isHeadphonesPlugged() {
        if (audioManager == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET || deviceInfo.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || deviceInfo.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    /**
     * 设置播放模式
     */
    public void setAudioStreamType(boolean speaker) {
        if (speaker) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            //5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 判断用户是否为黑名单
     *
     * @return
     */
    protected boolean isUserBlack() {
        if (initModel != null && "1".equals(initModel.getIsblack())) {
            return true;
        }
        return false;
    }

    /**
     * 重置内存中保存的数据
     */
    protected void clearCache() {
        SobotMsgManager.getInstance(mAppContext).clearAllConfig();
    }

    /**
     * 检查是否有询前表单，这个方法在转人工时 会首先检查是否需要填写询前表单，
     * 如果有那么将会弹出询前表单填写界面，之后会调用转人工
     */
    protected void requestQueryFrom(final SobotConnCusParam param, final boolean isCloseInquiryFrom) {
        if (customerState == CustomerState.Queuing || isHasRequestQueryFrom) {
            //如果在排队中就不需要填写询前表单 、或者之前弹过询前表单
            connectCustomerService(param);
            return;
        }
        if (isQueryFroming) {
            return;
        }
        isHasRequestQueryFrom = true;
        isQueryFroming = true;
        zhiChiApi.queryFormConfig(SobotChatBaseFragment.this, initModel.getPartnerid(), new StringResultCallBack<SobotQueryFormModel>() {
            @Override
            public void onSuccess(SobotQueryFormModel sobotQueryFormModel) {
                isQueryFroming = false;
                if (!isActive()) {
                    return;
                }
                if (sobotQueryFormModel.isOpenFlag() && !isCloseInquiryFrom && sobotQueryFormModel.getField() != null && sobotQueryFormModel.getField().size() > 0) {
                    // 打开询前表单
                    Intent intent = new Intent(mAppContext, SobotQueryFromActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_FIELD, sobotQueryFormModel);
                    bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_UID, initModel.getPartnerid());
                    bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM,param);
                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA, bundle);
                    startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_QUERY_FROM);
                } else {
                    connectCustomerService(param);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                isQueryFroming = false;
                if (!isActive()) {
                    return;
                }
                ToastUtil.showToast(mAppContext, des);
            }

        });
    }

    public void remindRobotMessage(final Handler handler, final ZhiChiInitModeBase initModel, final Information info) {
        //true代表结束会话，重新显示机器人提示语
        if (initModel.getAdminReadFlag() == 1) {
            isOpenUnread = true;
        } else {
            isOpenUnread = false;
        }
        boolean flag = SharedPreferencesUtil.getBooleanData(mAppContext, ZhiChiConstant.SOBOT_IS_EXIT, false);
        if (initModel == null) {
            return;
        }
        // 修改提醒的信息
        remindRobotMessageTimes = remindRobotMessageTimes + 1;
        if (remindRobotMessageTimes == 1) {
            if ((initModel.getUstatus() == ZhiChiConstant.ustatus_robot) && !flag) {
                processNewTicketMsg(handler);
                return;
            }
            /* 首次的欢迎语 */
            ZhiChiMessageBase robot = new ZhiChiMessageBase();
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();

            if (initModel.isRobotHelloWordFlag()) {
                String robotHolloWord = ZCSobotApi.getCurrentInfoSetting(mAppContext) != null ? ZCSobotApi.getCurrentInfoSetting(mAppContext).getRobot_hello_word() : "";
                if (!TextUtils.isEmpty(robotHolloWord) || !TextUtils.isEmpty(initModel.getRobotHelloWord())) {
                    if (!TextUtils.isEmpty(robotHolloWord)) {
                        reply.setMsg(robotHolloWord);
                    } else {
                        if (TextUtils.isEmpty(initModel.getRobotHelloWord())) {
                            //如果提示语为空，直接返回，不然会显示错误数据
                            return;
                        }
                        String msgHint = initModel.getRobotHelloWord().replace("\n", "<br/>");
                        if (msgHint.startsWith("<br/>")) {
                            msgHint = msgHint.substring(5, msgHint.length());
                        }

                        if (msgHint.endsWith("<br/>")) {
                            msgHint = msgHint.substring(0, msgHint.length() - 5);
                        }
                        reply.setMsg(msgHint);
                    }
                    reply.setMsgType(ZhiChiConstant.message_type_text);
                    robot.setAnswer(reply);
                    robot.setSenderFace(initModel.getRobotLogo());
                    robot.setSender(initModel.getRobotName());
                    robot.setSenderType(ZhiChiConstant.message_sender_type_robot_welcome_msg);
                    robot.setSenderName(initModel.getRobotName());
                    Message message = handler.obtainMessage();
                    message.what = ZhiChiConstant.hander_robot_message;
                    message.obj = robot;
                    handler.sendMessage(message);
                }
            }


            //获取机器人带引导与的欢迎语
//            if (1 == initModel.getGuideFlag() && (initModel.getSessionPhaseAndFaqIdRespVos() == null || (initModel.getSessionPhaseAndFaqIdRespVos() != null && initModel.getSessionPhaseAndFaqIdRespVos().size() == 0))) {
            //v1常见问题
                /*zhiChiApi.robotGuide(SobotChatBaseFragment.this, initModel.getPartnerid(), initModel.getRobotid(), info.getFaqId(), new
                        StringResultCallBack<ZhiChiMessageBase>() {
                            @Override
                            public void onSuccess(ZhiChiMessageBase robot) {
                                if (!isActive()) {
                                    return;
                                }
                                if (current_client_model == ZhiChiConstant.client_model_robot) {
                                    robot.setSenderFace(initModel.getRobotLogo());
                                    robot.setSenderName(initModel.getRobotName());
                                    robot.setSenderType(ZhiChiConstant.message_sender_type_robot_guide );
                                    Message message = handler.obtainMessage();
                                    message.what = ZhiChiConstant.hander_robot_message;
                                    message.obj = robot;
                                    handler.sendMessage(message);

                                    questionRecommend(handler, initModel, info);
                                    processAutoSendMsg(info);
                                    processNewTicketMsg(handler);
                                }
                            }

                            @Override
                            public void onFailure(Exception e, String des) {
                            }
                        });*/
//            } else {
            //v6常见问题
            sobotHotIssue(handler, 1);
            processAutoSendMsg(info);
            processNewTicketMsg(handler);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //机器人模式下展示自定义卡片
                    if (info.getCustomCard() != null && info.getCustomCard().isShowCustomCardAllMode() == true) {
                        checkSendCardContent(handler);
                    }
                }
            }, 1000);

//            }
        }
    }

    //4.1.4 检查卡片是否重复发送
    public void checkSendCardContent(final Handler handler) {
        final SobotChatCustomCard cardContent = info.getCustomCard();
        //如果配置了商品卡片
        if (cardContent != null) {
            //查询是否可以发送
            zhiChiApi.checkCardSendRepeat(getSobotActivity(), initModel.getCid(), cardContent.getCardId(), new StringResultCallBack() {
                @Override
                public void onSuccess(Object o) {
                    createCustomCardContent(handler);
                }

                @Override
                public void onFailure(Exception e, String s) {
                }
            });
        }
    }

    //创建客户自定商品、订单卡片
    //4.0.5
    public void createCustomCardContent(final Handler handler) {
        final SobotChatCustomCard cardContent = info.getCustomCard();
        //如果配置了商品卡片
        if (cardContent != null) {
            //以什么消息发送
            if (cardContent.getIsCustomerIdentity() == 1) {
                //以用户身份发送给机器人、人工，在右边 chat send
                //调用发送
                if (current_client_model == ZhiChiConstant.client_model_customService) {
                    //发送给人工
                    String msgId = getMsgId() + "";
                    ZhiChiMessageBase messageBase = ChatUtils.getCustomerCard(initModel.getReadFlag(), msgId, cardContent, info, initModel);
                    if (messageBase != null) {
                        sendNewMsgToHandler(messageBase, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
                        sendMsgToCustomService(handler, SobotGsonUtil.beanToJson(cardContent), "28",msgId, messageBase);
                    }
                } else {
                    //发送给机器人
                    String msgId = getMsgId() + "";
                    String customCardQuestion = "";
                    if (cardContent.getCustomCards() != null && cardContent.getCustomCards().size() > 0) {
                        for (SobotChatCustomGoods goods :
                                cardContent.getCustomCards()) {
                            if (!TextUtils.isEmpty(goods.getCustomCardQuestion())) {
                                customCardQuestion = goods.getCustomCardQuestion();
                                break;
                            }
                        }
                    }
                    if (TextUtils.isEmpty(customCardQuestion)) {
                        customCardQuestion = SobotGsonUtil.beanToJson(cardContent);
                    }
                    ZhiChiMessageBase messageBase = ChatUtils.getCustomerCard(initModel.getAdminReadFlag(), msgId, cardContent, info, initModel);
                    if (messageBase != null) {
                        sendNewMsgToHandler(messageBase, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
                        sendHttpRobotMessage("28", messageBase.getMsgId(), SobotGsonUtil.beanToJson(cardContent), initModel.getPartnerid(),
                                initModel.getCid(), "", handler, 0, customCardQuestion, info.getLocale(), "");
                    }
                }
            } else {
                //以系统身份发送卡片给”用户“，在左边
                zhiChiApi.insertCardInfoToSessionRecord(getSobotActivity(), initModel.getCid(), initModel.getPartnerid(), initModel.getCompanyId(), cardContent, new StringResultCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        ZhiChiMessageBase messageBase = new ZhiChiMessageBase();
                        messageBase.setMsgId(getMsgId());
                        //在机器人阶段用机器人头像、昵称，在人工阶段用客服的头像、昵称。
                        if (current_client_model == ZhiChiConstant.client_model_customService) {
                            messageBase.setSenderFace(getAdminFace());
                            messageBase.setSenderName(getAdminName());
                        } else {
                            messageBase.setSenderFace(initModel.getRobotLogo());
                            messageBase.setSenderName(initModel.getRobotName());
                        }
                        messageBase.setT(System.currentTimeMillis() + "");
                        messageBase.setSenderType(ZhiChiConstant.message_sender_type_system);
                        messageBase.setCustomCard(cardContent);
                        updateUiMessage(messageAdapter, messageBase);
                    }

                    @Override
                    public void onFailure(Exception e, String s) {
                    }
                });
            }


        }
    }

    /**
     * 获取用户是否有新留言回复
     */
    protected void processNewTicketMsg(final Handler handler) {
        if (initModel.getMsgFlag() == ZhiChiConstant.sobot_msg_flag_open
                && !TextUtils.isEmpty(initModel.getCustomerId())) {
            isRemindTicketInfo = true;
            //留言开关打开并且 customerId不为空时获取最新的工单信息
            zhiChiApi.checkUserTicketInfo(SobotChatBaseFragment.this, initModel.getPartnerid(), initModel.getCompanyId(), initModel.getCustomerId(), new StringResultCallBack<SobotUserTicketInfoFlag>() {
                @Override
                public void onSuccess(SobotUserTicketInfoFlag data) {
                    if (data.isExistFlag()) {
                        ZhiChiMessageBase base = new ZhiChiMessageBase();

                        base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);

                        ZhiChiReplyAnswer reply1 = new ZhiChiReplyAnswer();
                        reply1.setRemindType(ZhiChiConstant.sobot_remind_type_simple_tip);
                        reply1.setMsg("<font color='#ffacb5c4'>" + getResources().getString(R.string.sobot_new_ticket_info) + " </font>" + " <a href='sobot:SobotTicketInfo'  target='_blank' >" + getResources().getString(R.string.sobot_new_ticket_info_update) + "</a> ");
                        base.setAnswer(reply1);
                        Message message = handler.obtainMessage();
                        message.what = ZhiChiConstant.hander_send_msg;
                        message.obj = base;
                        handler.sendMessage(message);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {

                }
            });
        }
    }

    protected void processAutoSendMsg(final Information info) {
        if (info.getAutoSendMsgMode() == null) {
            return;
        }
        if (info.getAutoSendMsgMode() == SobotAutoSendMsgMode.Default) {
            return;
        }
        SobotAutoSendMsgMode autoSendMsgMode = info.getAutoSendMsgMode();
        if (TextUtils.isEmpty(autoSendMsgMode.getContent())) {
            return;
        }
        if (current_client_model == ZhiChiConstant.client_model_robot) {
            if (autoSendMsgMode == SobotAutoSendMsgMode.SendToRobot
                    || autoSendMsgMode == SobotAutoSendMsgMode.SendToAll) {
                sendMsg(autoSendMsgMode.getContent());
            }
        } else if (current_client_model == ZhiChiConstant.client_model_customService) {
            if ((autoSendMsgMode == SobotAutoSendMsgMode.SendToOperator
                    || autoSendMsgMode == SobotAutoSendMsgMode.SendToAll) && customerState == CustomerState.Online) {
                sendMsg(autoSendMsgMode.getContent());
            }
        }
    }

    /**
     * 查询常见问题条件
     */
    public void sobotHotIssue(final Handler handler, int type) {
        if (initModel.getSessionPhaseAndFaqIdRespVos() == null) return;
        List<SobotSessionPhaseMode> sessionPhaseModeList = initModel.getSessionPhaseAndFaqIdRespVos();
        if (type == ZhiChiConstant.type_robot_only) {
            boolean reqested = false;
            //仅机器人
            for (int i = 0; i < sessionPhaseModeList.size(); i++) {
                //会话阶段：1 进入会话，2机器人，3人工
                if (sessionPhaseModeList.get(i).getSessionPhase() == 2) {
                    requeIssue(handler, sessionPhaseModeList.get(i));
                    reqested = true;
                    return;
                }
            }
            //进入会话
            if (!reqested) {
                for (int i = 0; i < sessionPhaseModeList.size(); i++) {
                    //会话阶段：1 进入会话，2机器人，3人工
                    if (sessionPhaseModeList.get(i).getSessionPhase() == 1) {
                        requeIssue(handler, sessionPhaseModeList.get(i));
                        return;
                    }
                }
            }
        } else if (type == ZhiChiConstant.type_custom_only) {
            //仅人工
            for (int i = 0; i < sessionPhaseModeList.size(); i++) {
                //会话阶段：1 进入会话，2机器人，3人工
                if (sessionPhaseModeList.get(i).getSessionPhase() == 3) {
                    requeIssue(handler, sessionPhaseModeList.get(i));
                    return;
                }
            }
        } else {
            //进入会话
            for (int i = 0; i < sessionPhaseModeList.size(); i++) {
                //会话阶段：1 进入会话，2机器人，3人工
                if (sessionPhaseModeList.get(i).getSessionPhase() == 1) {
                    requeIssue(handler, sessionPhaseModeList.get(i));
                    return;
                }
            }
        }
    }

    //一个会话只显示一次常见问题
    private Map<String, String> showIssue = new HashMap<>();

    /**
     * v6常见问题
     *
     * @param sessionPhaseMode
     */
    private void requeIssue(final Handler handler, SobotSessionPhaseMode sessionPhaseMode) {
        if (!showIssue.containsKey(initModel.getCid())) {
            //热门问题
            zhiChiApi.getCusFaqDetailResult(SobotChatBaseFragment.this, initModel.getPartnerid(), initModel.getCid(), initModel.getCompanyId(), sessionPhaseMode.getCusFaqId(), sessionPhaseMode.getSessionPhase(), new StringResultCallBack<SobotFaqDetailModel>() {
                @Override
                public void onSuccess(SobotFaqDetailModel bean) {
                    showIssue.put(initModel.getCid(), "show");
                    //展示类型:1-问题列表,2-分组加问题列表,3-业务加分组加问题列表
                    ZhiChiMessageBase base = new ZhiChiMessageBase();
                    base.setT(Calendar.getInstance().getTime().getTime() + "");
                    base.setFaqDetailModel(bean);
                    base.setAction(ZhiChiConstant.action_sensitive_hot_issue);
                    base.setMsgId(System.currentTimeMillis() + "");
                    Message handMyMessage = handler.obtainMessage();
                    handMyMessage.what = ZhiChiConstant.hander_send_msg;
                    handMyMessage.obj = base;
                    handler.sendMessage(handMyMessage);
                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }

    private void questionRecommend(final Handler handler, final ZhiChiInitModeBase initModel, final Information info) {
        if (info.getMargs() == null || info.getMargs().size() == 0) {
            return;
        }
        zhiChiApi.questionRecommend(SobotChatBaseFragment.this, initModel.getPartnerid(), info.getMargs(), new StringResultCallBack<SobotQuestionRecommend>() {
            @Override
            public void onSuccess(SobotQuestionRecommend data) {
                if (!isActive()) {
                    return;
                }
                if (data != null && current_client_model == ZhiChiConstant.client_model_robot) {
                    ZhiChiMessageBase robot = ChatUtils.getQuestionRecommendData(initModel, data);
                    Message message = handler.obtainMessage();
                    message.what = ZhiChiConstant.hander_robot_message;
                    message.obj = robot;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
            }
        });
    }


    //-------------以下由子类实现-----------------------
    protected abstract String getSendMessageStr();

//    protected void connectCustomerService(String groupId, String groupName) {
//        connectCustomerService(groupId, groupName, 0);
//    }
//
//    protected void connectCustomerService(String groupId, String groupName, boolean isShowTips) {
//        connectCustomerService(groupId, groupName, null, null, isShowTips, 0);
//    }
//
//    protected void connectCustomerService(String groupId, String groupName, final String keyword, final String keywordId, final boolean isShowTips) {
//        connectCustomerService(groupId, groupName, keyword, keywordId, isShowTips, 0);
//    }
//
//    protected void connectCustomerService(String groupId, String groupName, int transferType) {
//        connectCustomerService(groupId, groupName, null, null, true, transferType);
//    }

    protected void connectCustomerService(SobotConnCusParam param) {
        connectCustomerService(param, true);
    }

    protected void connectCustomerService(SobotConnCusParam param, boolean isShowTips) {
    }

    protected void customerServiceOffline(ZhiChiInitModeBase initModel, int outLineType) {
    }

    protected void sendMsg(String content) {
    }


    //轮询接口需要的参数
    private Map<String, String> pollingParams = new HashMap<>();
    //ack需要的参数
    private Map<String, String> ackParams = new HashMap<>();
    private PollingHandler pollingHandler;

    //轮询需要的handler
    private PollingHandler getPollingHandler() {
        if (this.pollingHandler == null) {
            this.pollingHandler = new PollingHandler();
        }
        return this.pollingHandler;
    }

    private static class PollingHandler extends Handler {

        public PollingHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    /**
     * 开启轮询
     */
    public void startPolling() {
        uid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_UID, "");
        puid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_PUID, "");
        getPollingHandler().removeCallbacks(pollingRun);
        getPollingHandler().postDelayed(pollingRun, 5 * 1000);
        String platformUnionCode = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(platformUnionCode)) {
            LogUtils.i2Local("开启轮询", "SobotChatBaseFragment 轮询开始：参数" + "{platformUserId:" + uid + "}");
        } else {
            LogUtils.i2Local("开启轮询", "SobotChatBaseFragment 轮询开始：参数" + "{uid:" + uid + ",puid:" + puid + "}");
        }
    }

    private String uid;
    private String puid;
    public boolean inPolling = false;//表示轮询接口是否在跑
    public boolean isWritePollingLog = true;//轮询只把第一次结果写到日志里

    private Runnable pollingRun = new Runnable() {
        @Override
        public void run() {
            inPolling = true;
            pollingMsg();
        }
    };

    private void pollingMsg() {
        String platformUnionCode = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(platformUnionCode)) {
            pollingParams.put("platformUserId", CommonUtils.getPlatformUserId(getSobotActivity()));
        } else {
            pollingParams.put("uid", uid);
            pollingParams.put("puid", puid);
        }
        pollingParams.put("tnk", System.currentTimeMillis() + "");
        zhiChiApi.pollingMsg(SobotChatBaseFragment.this, pollingParams, platformUnionCode, new StringResultCallBack<BaseCode>() {

            @Override
            public void onSuccess(BaseCode baseCode) {
                if (isWritePollingLog) {
                    LogUtils.i2Local("SobotChatBaseFragment 轮询结果", baseCode.toString());
                    try {
                        //上传日志
                        SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().logCollect(getSobotActivity(), SharedPreferencesUtil.getAppKey(getSobotActivity(), ""), true);
                    } catch (Exception e) {
                    }
                }
                isWritePollingLog = false;
                LogUtils.i("fragment pollingMsg 轮询请求结果:" + baseCode.getData().toString());
                getPollingHandler().removeCallbacks(pollingRun);
                if (baseCode != null) {
                    if ("0".equals(baseCode.getCode()) && "210021".equals(baseCode.getData())) {
                        //{"code":0,"data":"210021","msg":"当前用户被验证为非法用户，不能接入客服中心"}
                        //非法用户，停止轮询
                        LogUtils.i2Local("fragment 轮询结果异常", baseCode.toString() + " 非法用户，停止轮询");
                    } else if ("0".equals(baseCode.getCode()) && "200003".equals(baseCode.getData())) {
                        //{"code":0,"data":"200003","msg":"访客信息不存在"}
                        //找不到用户，停止轮询
                        LogUtils.i2Local("fragment 轮询结果异常", baseCode.toString() + " 找不到用户，停止轮询");
                    } else {
                        getPollingHandler().postDelayed(pollingRun, 5 * 1000);
                        if (baseCode.getData() != null) {
                            responseAck(getSobotActivity(), baseCode.getData().toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                getPollingHandler().removeCallbacks(pollingRun);
                getPollingHandler().postDelayed(pollingRun, 10 * 1000);
                LogUtils.i("msg::::" + des);
                LogUtils.i2Local("轮询接口失败", "SobotChatBaseFragment 轮询:" + "请求参数 " + pollingParams != null ? GsonUtil.map2Json(pollingParams) : "" + e.toString());
                try {
                    //上传日志
                    SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().logCollect(getSobotActivity(), SharedPreferencesUtil.getAppKey(getSobotActivity(), ""), true);
                } catch (Exception exception) {
                }
            }
        });
    }

    //只请求一次轮询接口
    public void pollingMsgForOne() {
        uid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_UID, "");
        puid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_PUID, "");
        String platformUnionCode = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(platformUnionCode)) {
            pollingParams.put("platformUserId", CommonUtils.getPlatformUserId(getSobotActivity()));
        } else {
            pollingParams.put("uid", uid);
            pollingParams.put("puid", puid);
        }
        pollingParams.put("tnk", System.currentTimeMillis() + "");
        LogUtils.i2Local("开启轮询", "SobotChatBaseFragment 至少只请求一次轮询接口 参数:" + pollingParams.toString());
        zhiChiApi.pollingMsg(SobotChatBaseFragment.this, pollingParams, platformUnionCode, new StringResultCallBack<BaseCode>() {

            @Override
            public void onSuccess(BaseCode baseCode) {
                LogUtils.i2Local("SobotChatBaseFragment至少只请求一次轮询接口", " 轮询请求结果:" + baseCode.toString());
                LogUtils.i("fragment pollingMsgForOne 轮询请求结果:" + baseCode.getData().toString());
                if (baseCode != null) {
                    if ("0".equals(baseCode.getCode()) && "210021".equals(baseCode.getData())) {
                        //{"code":0,"data":"210021","msg":"当前用户被验证为非法用户，不能接入客服中心"}
                        //非法用户，停止轮询
                    } else if ("0".equals(baseCode.getCode()) && "200003".equals(baseCode.getData())) {
                        //{"code":0,"data":"200003","msg":"访客信息不存在"}
                        //找不到用户，停止轮询
                    } else {
                        if (baseCode.getData() != null) {
                            responseAck(getSobotActivity(), baseCode.getData().toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                LogUtils.i("msg::::" + des);
                LogUtils.i2Local("轮询接口失败", "请求参数 " + pollingParams != null ? GsonUtil.map2Json(pollingParams) : "" + e.toString());
                try {
                    //上传日志
                    SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().logCollect(getSobotActivity(), SharedPreferencesUtil.getAppKey(getSobotActivity(), ""), true);
                } catch (Exception exception) {
                }
            }
        });
    }

    /**
     * 已收到消息的msgId队列
     */
    private LimitQueue<String> receiveMsgQueue = new LimitQueue<>(50);


    private void responseAck(Context mContext, String result) {
//        LogUtils.i("msg::::"+result);
        // 解析数据后给ack
        if (!TextUtils.isEmpty(result)) {
            JSONArray jsonArray = null;
            JSONArray acks = null;
            try {
                jsonArray = new JSONArray(result);
                acks = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String data = jsonArray.getString(i);
                    String msgId = Util.getMsgId(data);
                    if (!TextUtils.isEmpty(msgId)) {
                        if (receiveMsgQueue.indexOf(msgId) == -1) {
                            //队列中没有 表示是新数据
                            //新数据就添加进队列中
                            receiveMsgQueue.offer(msgId);
                            Util.notifyMsg(mContext, data, "fragment 轮询： 新数据插入到receiveMsgQueue中  msgId: " + msgId);
                        } else {
                            LogUtils.i2Local("fragment 轮询", "已经插入过receiveMsgQueue,不操作  msgId: " + msgId);
                        }
                        //生成 回执
                        acks.put(new JSONObject("{msgId:" + msgId + "}"));
                    } else {
                        Util.notifyMsg(mContext, data, "fragment 轮询： receiveMsgQueue为空，不缓存直接广播   msgId: " + msgId);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (acks != null && acks.length() > 0) {
                ackParams.put("content", acks.toString());
                ackParams.put("tnk", System.currentTimeMillis() + "");
                zhiChiApi.msgAck(SobotChatBaseFragment.this, ackParams, new StringResultCallBack<BaseCode>() {
                    @Override
                    public void onSuccess(BaseCode baseCode) {

                    }

                    @Override
                    public void onFailure(Exception e, String des) {

                    }
                });
            }
        }
    }


    /**
     * 关闭轮询
     */
    public void stopPolling() {
        if (pollingRun != null && getPollingHandler() != null) {
            getPollingHandler().removeCallbacks(pollingRun);
            inPolling = false;
        }
    }


    @Override
    public void onDestroy() {
        stopPolling();
        HttpUtils.getInstance().cancelTag(SobotChatBaseFragment.this);
        super.onDestroy();
    }

    //用户发送接口生成msgId传给接口
    public String getMsgId() {
        String msgId = "";
        if (initModel != null) {
            msgId = initModel.getCid();
        }
        msgId = msgId + System.currentTimeMillis();
        return msgId;
    }

    //删除引用缓存
    public void clearAppointUI() {
        appointMessage = null;
        if (ll_appoint != null) {
            ll_appoint.setVisibility(View.GONE);
        }
        if (tv_appoint_temp_content != null) {
            tv_appoint_temp_content.setText("");
        }
    }
}
