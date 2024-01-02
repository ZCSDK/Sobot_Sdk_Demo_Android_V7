package com.sobot.chat.viewHolder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.RoundProgressBar;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 图片消息
 */
public class ImageMessageHolder extends MsgHolderBase {

    SobotRCImageView image;
    private RelativeLayout sobot_rl_real_pic;
    public RoundProgressBar sobot_pic_progress_round;
    TextView isGif;
    RelativeLayout sobot_pic_progress_rl;

    public ImageMessageHolder(Context context, View convertView) {
        super(context, convertView);
        isGif = (TextView) convertView.findViewById(R.id.sobot_pic_isgif);
        image = (SobotRCImageView) convertView.findViewById(R.id.sobot_iv_picture);
        sobot_rl_real_pic = convertView.findViewById(R.id.sobot_rl_real_pic);
        sobot_pic_progress_round = (RoundProgressBar) convertView.findViewById(R.id.sobot_pic_progress_round);
        sobot_pic_progress_rl = (RelativeLayout) convertView.findViewById(R.id.sobot_pic_progress_rl);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        isGif.setVisibility(View.GONE);
        image.setVisibility(View.VISIBLE);
        if (isRight) {
            sobot_pic_progress_round.setVisibility(View.GONE);
            sobot_pic_progress_rl.setVisibility(View.VISIBLE);
            if (ZhiChiConstant.MSG_SEND_STATUS_ERROR == message.getSendSuccessState()) {
                msgStatus.setVisibility(View.VISIBLE);
                msgProgressBar.setVisibility(View.GONE);
                sobot_pic_progress_round.setVisibility(View.GONE);
                sobot_pic_progress_rl.setVisibility(View.GONE);
                // 点击重新发送按钮
                msgStatus.setOnClickListener(new RetrySendImageLisenter(context, message
                        .getId(), message.getAnswer().getMsg(), msgStatus, msgCallBack));
            } else if (ZhiChiConstant.MSG_SEND_STATUS_SUCCESS == message.getSendSuccessState()) {
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                sobot_pic_progress_round.setVisibility(View.GONE);
                sobot_pic_progress_rl.setVisibility(View.GONE);
                refreshReadStatus();
            } else if (ZhiChiConstant.MSG_SEND_STATUS_LOADING == message.getSendSuccessState()) {
                msgProgressBar.setVisibility(View.VISIBLE);
                msgStatus.setVisibility(View.GONE);
                goneReadStatus();
            } else {
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                sobot_pic_progress_round.setVisibility(View.GONE);
                sobot_pic_progress_rl.setVisibility(View.GONE);
            }
        } else {
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
        }

//        String picPath = message.getAnswer().getMsg();
//        if(!TextUtils.isEmpty(picPath) && (picPath.endsWith("gif") || picPath.endsWith("GIF"))){
//            isGif.setVisibility(View.VISIBLE);
//        }else{
//            isGif.setVisibility(View.GONE);
//        }
        SobotBitmapUtil.display(context, message.getAnswer().getMsg(), image);
        image.setOnClickListener(new ImageClickLisenter(context, message.getAnswer().getMsg(), isRight));
        setLongClickListener(image);
    }

    // 图片的重新发送监听
    public static class RetrySendImageLisenter implements View.OnClickListener {
        private String id;
        private String imageUrl;
        private ImageView img;

        private Context context;
        SobotMsgAdapter.SobotMsgCallBack mMsgCallBack;

        public RetrySendImageLisenter(final Context context, String id, String imageUrl,
                                      ImageView image, final SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            super();
            this.id = id;
            this.imageUrl = imageUrl;
            this.img = image;
            this.context = context;
            mMsgCallBack = msgCallBack;
        }

        @Override
        public void onClick(View view) {
            if (img != null) {
                img.setClickable(false);
            }
            showReSendPicDialog(context, imageUrl, id, img);
        }

        private void showReSendPicDialog(final Context context, final String mimageUrl, final String mid, final ImageView msgStatus) {

            showReSendDialog(context, msgStatus, new ReSendListener() {

                @Override
                public void onReSend() {
                    // 获取图片的地址url
                    // 上传url
                    // 采用广播进行重发
                    if (context != null) {
                        ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                        msgObj.setContent(mimageUrl);
                        msgObj.setId(mid);
                        msgObj.setSendSuccessState(ZhiChiConstant.MSG_SEND_STATUS_LOADING);
                        if (mMsgCallBack != null) {
                            mMsgCallBack.sendMessageToRobot(msgObj, 3, 3, "");
                        }
                    }
                }
            });
        }
    }
}
