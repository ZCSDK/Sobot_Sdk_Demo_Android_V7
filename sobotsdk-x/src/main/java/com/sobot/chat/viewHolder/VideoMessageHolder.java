package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageMsgModel;
import com.sobot.chat.gson.SobotGsonUtil;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.image.SobotProgressView;
import com.sobot.chat.widget.image.SobotRCRelativeLayout;
import com.sobot.network.http.model.SobotProgress;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.network.http.upload.SobotUploadListener;
import com.sobot.network.http.upload.SobotUploadModelBase;
import com.sobot.network.http.upload.SobotUploadTask;

/**
 * 视频
 */
public class VideoMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private final ImageView st_tv_play;
    private final SobotProgressImageView st_iv_pic;

    private ZhiChiMessageBase mData;
    private String mTag;

    private final int mResNetError;
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();
    private final SobotRCRelativeLayout rl_upload;//上传中UI控件
    private final TextView tv_progress;//上传进度
    private final SobotProgressView progress_view;

    public VideoMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_rl_hollow_container = convertView.findViewById(R.id.sobot_rl_hollow_container);
        st_tv_play = convertView.findViewById(R.id.st_tv_play);
        st_iv_pic = convertView.findViewById(R.id.st_iv_pic);
        rl_upload = convertView.findViewById(R.id.rl_upload);
        tv_progress = convertView.findViewById(R.id.tv_progress);
        progress_view = convertView.findViewById(R.id.progress_view);
        answersList = convertView
                .findViewById(R.id.sobot_answersList);
        st_tv_play.setOnClickListener(this);
        if (st_iv_pic != null) {
            st_iv_pic.setOnClickListener(this);
        }
        mResNetError = R.drawable.sobot_icon_send_fail;
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        if (message.getAnswer() != null && message.getAnswer().getCacheFile() != null) {
            SobotCacheFile cacheFile = message.getAnswer().getCacheFile();
            st_iv_pic.setListener(new SobotProgressImageView.OnDisplayImageListener() {
                @Override
                public void onSuccess(View imgView) {
                    if (rl_upload != null && imgView != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
//                                LogUtils.d("图片宽高：" + imgView.getWidth() + "---" + imgView.getHeight());
                                rl_upload.setLayoutParams(new RelativeLayout.LayoutParams(imgView.getWidth(), imgView.getHeight()));
                            }
                        }, 10);
                    }
                }

                @Override
                public void onFail(View view) {

                }
            });
            st_iv_pic.setImageUrlWithScaleType(cacheFile.getSnapshot(), ImageView.ScaleType.CENTER_INSIDE);
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
        longClickListener(st_iv_pic);
    }

    //设置控件长按事件，弹出引用提示框
    public void longClickListener(final View view) {
        if (view == null || mContext == null) {
            return;
        }
        if (initMode == null || initMode.getMsgAppointFlag() == 0) {
            //引用未开启
            return;
        }
        if (answersList != null && ((message.getListSuggestions() != null && message.getListSuggestions().size() > 0) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
            //只要带有关联问题都不能引用
            return;
        }
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAppointPopWindows(mContext, view, 0, 18, message);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mData != null) {
            if (st_tv_play == v || st_iv_pic == v) {
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
                if (rl_upload != null) {
                    rl_upload.setVisibility(View.GONE);
                }
                if (st_tv_play != null) {
                    st_tv_play.setVisibility(View.VISIBLE);
                }
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
                if (rl_upload != null) {
                    rl_upload.setVisibility(View.GONE);
                }
                if (st_tv_play != null) {
                    st_tv_play.setVisibility(View.VISIBLE);
                }
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
                if (rl_upload != null) {
                    rl_upload.setVisibility(View.GONE);
                }
                if (st_tv_play != null) {
                    st_tv_play.setVisibility(View.VISIBLE);
                }
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
//                        msgProgressBar.setVisibility(View.VISIBLE);
                        if (rl_upload != null) {
                            //显示上传按钮
                            rl_upload.setVisibility(View.VISIBLE);
                        }
                        try {
                            if (progress_view != null) {
                                // 设置进度值
                                progress_view.setProgress((int) (progress.fraction * 100));
                            }
                        } catch (Exception e) {
                        }
                        if (tv_progress != null) {
                            tv_progress.setText(String.valueOf((int) (progress.fraction * 100) + "%"));
                        }
                        if (st_tv_play != null) {
                            //隐藏播放按钮
                            st_tv_play.setVisibility(View.GONE);
                        }
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
                    cacheFile.setSnapshot(progress.snapshot);
                    messageMsgModel.setContent(cacheFile);
                    message.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
                }
                if (msgCallBack != null && message != null) {
                    msgCallBack.sendFileToRobot(message.getMsgId(), "3", progress.url);
                }
            }
        }

        @Override
        public void onRemove(SobotProgress progress) {

        }
    }
}
