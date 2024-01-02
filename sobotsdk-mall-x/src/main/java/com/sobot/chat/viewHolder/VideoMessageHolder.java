package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageMsgModel;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.gson.SobotGsonUtil;
import com.sobot.network.http.model.SobotProgress;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.network.http.upload.SobotUploadListener;
import com.sobot.network.http.upload.SobotUploadModelBase;
import com.sobot.network.http.upload.SobotUploadTask;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 视频
 */
public class VideoMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private ImageView st_tv_play;
    private SobotRCImageView st_iv_pic;
    private int sobot_bg_default_pic;

    private ZhiChiMessageBase mData;
    private String mTag;

    private int mResNetError;
    private int mResRemove;

    public VideoMessageHolder(Context context, View convertView) {
        super(context, convertView);
        st_tv_play = (ImageView) convertView.findViewById(R.id.st_tv_play);
        st_iv_pic = (SobotRCImageView) convertView.findViewById(R.id.st_iv_pic);
        sobot_bg_default_pic = R.drawable.sobot_bg_default_pic;
        answersList = (LinearLayout) convertView
                .findViewById(R.id.sobot_answersList);
        st_tv_play.setOnClickListener(this);
        mResNetError = R.drawable.sobot_re_send_selector;
        mResRemove = R.drawable.sobot_icon_remove;
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        if (message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
            SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
            SobotBitmapUtil.display(mContext, cacheFile.getSnapshot(), st_iv_pic, sobot_bg_default_pic, sobot_bg_default_pic);
            mTag = cacheFile.getMsgId();
            if (isRight()) {
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
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding((int) mContext.getResources().getDimension(R.dimen.sobot_msg_left_right_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_left_right_padding_edge), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_padding_edge));
                }
            } else {
                hideAnswers();
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding(0, 0, 0, 0);
                }
            }
        } else {
            if (msgStatus != null) {
                msgStatus.setOnClickListener(this);
            }
        }
        setLongClickListener(st_iv_pic);
    }

    @Override
    public void onClick(View v) {
        if (mData != null) {
            if (st_tv_play == v) {
                if (mData.getAnswer() != null && mData.getAnswer().getCacheFile() != null) {
                    SobotCacheFile cacheFile = mData.getAnswer().getCacheFile();
                    // 播放视频
                    Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
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
                break;
            case SobotProgress.ERROR:
                msgStatus.setVisibility(View.VISIBLE);
                msgStatus.setBackgroundResource(mResNetError);
                msgStatus.setSelected(true);
                //  sobot_progress.setProgress(100);
                msgProgressBar.setVisibility(View.GONE);
                goneReadStatus();
                break;
            case SobotProgress.PAUSE:
            case SobotProgress.WAITING:
            case SobotProgress.LOADING:
                msgProgressBar.setVisibility(View.VISIBLE);
                goneReadStatus();
                msgStatus.setVisibility(View.GONE);
                msgStatus.setBackgroundResource(mResRemove);
                msgStatus.setSelected(false);
                //    sobot_progress.setProgress(progress.fraction * 100);
                break;
        }
    }

    private static class ListUploadListener extends SobotUploadListener {

        private VideoMessageHolder holder;
        private SobotMsgAdapter.SobotMsgCallBack msgCallBack;
        private ZhiChiInitModeBase initModel;
        private ZhiChiMessageBase message;

        ListUploadListener(Object tag, VideoMessageHolder holder, ZhiChiInitModeBase initModel, ZhiChiMessageBase message, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
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
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
            }
        }

        @Override
        public void onFinish(SobotUploadModelBase result, SobotProgress progress) {
            if (tag == holder.getTag()) {
                holder.refreshUploadUi(progress);
                if (initModel != null && initModel.getMsgAppointFlag() == 1 && message != null) {
                    //引用开启
                    ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
                    messageMsgModel.setMsgType(3);
                    SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
                    cacheFile.setUrl(progress.url);
                    messageMsgModel.setContent(cacheFile);
                    message.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
                }
                if (msgCallBack != null && message!=null) {
                    msgCallBack.sendFileToRobot(message.getMsgId(),"3", progress.url);
                }
            }
        }

        @Override
        public void onRemove(SobotProgress progress) {

        }
    }
}
