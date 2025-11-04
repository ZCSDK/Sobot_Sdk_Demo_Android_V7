package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageFileModel;
import com.sobot.chat.api.model.ZhiChiMessageMsgModel;
import com.sobot.chat.gson.SobotGsonUtil;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.model.SobotProgress;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.network.http.upload.SobotUploadListener;
import com.sobot.network.http.upload.SobotUploadModelBase;
import com.sobot.network.http.upload.SobotUploadTask;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 文件消息
 */
public class FileMessageHolder extends MsgHolderBase implements View.OnClickListener {

    private final SobotSectorProgressView sobot_file_icon;
    private final TextView sobot_file_name;
    private final TextView sobot_file_size;
    private final RelativeLayout sobot_ll_file_container;

    private ZhiChiMessageBase mData;
    private String mTag;
    private final int mResNetError;
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();


    public FileMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_file_icon = convertView.findViewById(R.id.sobot_file_icon);
        sobot_file_name = convertView.findViewById(R.id.sobot_file_name);
        sobot_file_size = convertView.findViewById(R.id.sobot_file_size);
        sobot_ll_file_container = convertView.findViewById(R.id.sobot_ll_file_container);
        mResNetError = R.drawable.sobot_icon_send_fail;
        sobot_ll_file_container.setOnClickListener(this);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        if (message != null && message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
            SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
            if (StringUtils.isNoEmpty(cacheFile.getFileName())) {
                sobot_file_name.setText(cacheFile.getFileName().replace("\r\n\t", ""));
            }
            sobot_file_size.setText(cacheFile.getFileSize());
            SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, cacheFile.getFileType()), sobot_file_icon);
            mTag = cacheFile.getMsgId();
            if (isRight) {
                if (SobotUpload.getInstance().hasTask(mTag)) {
                    SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                    uploadTask.register(new ListUploadListener(mTag, this, initMode, message, msgCallBack));
                    refreshUploadUi(uploadTask.progress);
                } else {
                    refreshReadStatus();
                    refreshUploadUi(null);
                }
            } else {
                refreshUploadUi(null);
            }
        }
        if (!isRight()) {
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
            } else {
                hideAnswers();
            }
        } else {
            if (msgStatus != null) {
                msgStatus.setOnClickListener(this);
            }
        }
        setLongClickListener(sobot_ll_file_container);
    }


    @Override
    public void onClick(View v) {
        if (mData != null) {
            if (sobot_ll_file_container == v) {
                if (mData.getAnswer() != null && mData.getAnswer().getCacheFile() != null) {
                    // 打开详情页面
                    Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, mData.getAnswer().getCacheFile());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }

            if (msgStatus == v) {
                if (msgStatus.isSelected()) {
                    //下载失败
                    showReSendDialog(mContext, msgStatus, new ReSendListener() {

                        @Override
                        public void onReSend() {
                            SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                            if (uploadTask != null) {
                                uploadTask.restart();
                            } else {
                                notifyFileTaskRemove();
                            }
                        }
                    });
                } else {
                    //取消
                    if (SobotUpload.getInstance().hasTask(mTag)) {
                        SobotUploadTask uploadTask = SobotUpload.getInstance().getTask(mTag);
                        uploadTask.remove();
                    }
                    notifyFileTaskRemove();
                }
            }
        }
    }

    private void notifyFileTaskRemove() {
        Intent intent = new Intent(ZhiChiConstants.SOBOT_BROCAST_REMOVE_FILE_TASK);
        intent.putExtra("sobot_msgId", mTag);
        CommonUtils.sendLocalBroadcast(mContext, intent);
    }

    private String getTag() {
        return mTag;
    }

    private void refreshUploadUi(SobotProgress progress) {
        if (progress == null) {
            if (msgStatus != null) {
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
            }
            // sobot_progress.setProgress(100);
            return;
        }
        if (msgStatus == null) {
            return;
        }
        switch (progress.status) {
            case SobotProgress.NONE:
            case SobotProgress.FINISH:
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                refreshReadStatus();
                // sobot_progress.setProgress(progress.fraction * 100);
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
                break;
            case SobotProgress.ERROR:
                msgStatus.setVisibility(View.VISIBLE);
                msgStatus.setBackgroundResource(mResNetError);
                msgStatus.setSelected(true);
                //  sobot_progress.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                goneReadStatus();
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
                break;
            case SobotProgress.PAUSE:
            case SobotProgress.WAITING:
            case SobotProgress.LOADING:
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
                loadingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        msgProgressBar.setVisibility(View.VISIBLE);
                        goneReadStatus();
                        msgStatus.setVisibility(View.GONE);
                        msgStatus.setSelected(false);
                        //    sobot_progress.setProgress(progress.fraction * 100);
                    }
                };
                handler.postDelayed(loadingRunnable, ZCSobotConstant.LOADING_TIME);
                break;
        }
    }

    private static class ListUploadListener extends SobotUploadListener {

        private FileMessageHolder holder;
        private SobotMsgAdapter.SobotMsgCallBack msgCallBack;
        private ZhiChiInitModeBase initModel;
        private ZhiChiMessageBase message;

        ListUploadListener(Object tag, FileMessageHolder holder, ZhiChiInitModeBase initModel, ZhiChiMessageBase message, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            super(tag);
            this.holder = holder;
            this.msgCallBack = msgCallBack;
            this.initModel = initModel;
            this.message = message;
        }

        @Override
        public void onStart(SobotProgress progress) {

        }

        @Override
        public void onProgress(SobotProgress progress) {
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onError(SobotProgress progress) {
            if (holder.mContext != null && progress != null && progress.exception != null && !TextUtils.isEmpty(progress.exception.getMessage())) {
                ToastUtil.showToast(holder.mContext, progress.exception.getMessage());
            }
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onFinish(SobotUploadModelBase result, SobotProgress progress) {
            LogUtils.d("tag="+tag+"--------------holder.getTag()="+holder.getTag());
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
                if (initModel != null && initModel.getMsgAppointFlag() == 1 && message != null && message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
                    //引用开启
                    ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
                    messageMsgModel.setMsgType(4);
                    SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
                    ZhiChiMessageFileModel fileModel = new ZhiChiMessageFileModel();
                    fileModel.setFileName(cacheFile.getFileName());
                    fileModel.setFileSize(cacheFile.getFileSize());
                    fileModel.setType(cacheFile.getFileType());
                    fileModel.setUrl(progress.url);
                    fileModel.setSize(progress.totalSize + "");
                    messageMsgModel.setContent(fileModel);
                    message.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
                }
                LogUtils.d("msgCallBack="+msgCallBack+"--------------message="+message);
                if (msgCallBack != null && message != null) {
                    msgCallBack.sendFileToRobot(message.getMsgId(), "4", progress.url);
                }
            }
        }

        @Override
        public void onRemove(SobotProgress progress) {

        }
    }
}