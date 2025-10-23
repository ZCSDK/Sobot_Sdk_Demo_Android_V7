package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 语音条目
 */
public class VoiceMessageHolder extends MsgHolderBase {
    TextView voiceTimeLong;
    ImageView voicePlay;
    LinearLayout ll_voice_layout;
    LinearLayout ll_text_root;
    TextView sobot_voice_change_text;
    TextView sobot_voice_change_state;
    public ZhiChiMessageBase message;

    public VoiceMessageHolder(Context context, View convertView) {
        super(context, convertView);
        voicePlay = (ImageView) convertView.findViewById(R.id.sobot_iv_voice);
        voiceTimeLong = (TextView) convertView
                .findViewById(R.id.sobot_voiceTimeLong);
        ll_voice_layout = (LinearLayout) convertView
                .findViewById(R.id.sobot_msg_content_ll);
        msgProgressBar = (ProgressBar) convertView
                .findViewById(R.id.sobot_msgProgressBar);
        //语音
        ll_text_root = convertView.findViewById(R.id.sobot_ll_voice_text_root);
        sobot_voice_change_text = convertView.findViewById(R.id.sobot_voice_change_text);
        sobot_voice_change_state = convertView.findViewById(R.id.sobot_voice_change_state);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        try {
            this.message = message;
            voiceTimeLong.setText(message.getAnswer().getDuration() == null ?
                    "" : (DateUtil.stringToLongMs(message.getAnswer().getDuration()) == 0 ? "" : (DateUtil.stringToLongMs(message.getAnswer().getDuration()) + "''")));
            voiceTimeLong.setTextColor(ThemeUtils.getThemeTextAndIconColor(mContext));
            checkBackground();
            ll_voice_layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (msgCallBack != null) {
                        msgCallBack.clickAudioItem(message, null, true);
                    }
                }
            });

            if (isRight) {
                goneReadStatus();
                if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {
                    msgStatus.setVisibility(View.GONE);
                    msgProgressBar.setVisibility(View.GONE);
                    voiceTimeLong.setVisibility(View.VISIBLE);
                    voicePlay.setVisibility(View.VISIBLE);
                    //历史数据
                    if (message.getSdkMsg() != null && message.getSdkMsg().getAnswer() != null) {
                        int changeState = message.getSdkMsg().getAnswer().getState();
                        if (message.getSdkMsg().getAnswer().getState() == 0) {
                            message.getSdkMsg().getAnswer().setState(-1);
                        }
                        setVoiceText(changeState, message.getSdkMsg().getAnswer().getVoiceText());
                    } else {
                        ll_text_root.setVisibility(View.GONE);
                    }
                    //当时聊天
                    if (null != message.getAnswer()) {
                        int changeState = message.getAnswer().getState();
                        setVoiceText(changeState, message.getAnswer().getVoiceText());
                    } else {
                        ll_text_root.setVisibility(View.GONE);
                    }
                    refreshReadStatus();
                } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                    ll_text_root.setVisibility(View.GONE);
                    msgStatus.setVisibility(View.VISIBLE);
                    msgProgressBar.setVisibility(View.GONE);
                    voicePlay.setVisibility(View.VISIBLE);
                    voiceTimeLong.setVisibility(View.VISIBLE);
                    stopAnim();
                    // 语音的重新发送
                    msgStatus.setOnClickListener(new RetrySendVoiceLisenter(context, message.getId(),
                            message.getAnswer().getMsg(), message.getAnswer().getDuration(), msgStatus, msgCallBack));
                } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {// 发送中
                    ll_text_root.setVisibility(View.GONE);
                    msgProgressBar.setVisibility(View.VISIBLE);
                    msgStatus.setVisibility(View.GONE);
                    voiceTimeLong.setVisibility(View.VISIBLE);
                    voicePlay.setVisibility(View.VISIBLE);
                } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ANIM) {
                    ll_text_root.setVisibility(View.GONE);
                    msgProgressBar.setVisibility(View.GONE);
                    msgStatus.setVisibility(View.GONE);
                    voiceTimeLong.setVisibility(View.VISIBLE);
                    voicePlay.setVisibility(View.VISIBLE);
                } else {
                    ll_text_root.setVisibility(View.GONE);
                }

                //根据语音长短设置长度
                long duration = DateUtil.stringToLongMs(message.getAnswer().getDuration());
                duration = duration < 1 ? 1 : duration;
                if (context != null) {
                    int min = ScreenUtils.dip2px(context, 80);
                    int max = ScreenUtils.dip2px(context, 200);
                    int temp = ScreenUtils.dip2px(context, 3 * duration);
                    if ((temp + min) > max) {
                        ll_voice_layout.getLayoutParams().width = max;
                    } else {
                        ll_voice_layout.getLayoutParams().width = temp + min;
                    }
                }
            }
            setLongClickListener(ll_voice_layout);
        } catch (Exception e) {
        }
    }

    public void setVoiceText(int changeState, String voiceText) {
        if (changeState == -1) {
            //未转换
            ll_text_root.setVisibility(View.GONE);
        } else {
            ll_text_root.setVisibility(View.VISIBLE);
            if (changeState == 1) {
                //成功
                sobot_voice_change_text.setText(voiceText);
                sobot_voice_change_text.setVisibility(View.VISIBLE);
                sobot_voice_change_state.setText(R.string.sobot_conversion_done);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_voice_change_success);
                if (img != null) {
                    img.setBounds(0, 0, ScreenUtils.dip2px(mContext, 14), ScreenUtils.dip2px(mContext, 14));
                    sobot_voice_change_state.setCompoundDrawables(img, null, null, null);
                }
            } else {
                //失败
                sobot_voice_change_text.setText("");
                sobot_voice_change_text.setVisibility(View.GONE);
                sobot_voice_change_state.setText(R.string.sobot_conversion_failed);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_voice_change_fail);
                if (img != null) {
                    img.setBounds(0, 0, ScreenUtils.dip2px(mContext, 14), ScreenUtils.dip2px(mContext, 14));
                    sobot_voice_change_state.setCompoundDrawables(img, null, null, null);
                }
            }
        }
    }

    public void checkBackground() {
        if (message.isVoideIsPlaying()) {
            resetAnim();
        } else {
            voicePlay.setImageResource(isRight ? R.drawable.sobot_pop_voice_send_anime_3 :
                    R.drawable.sobot_pop_voice_receive_anime_3);
            Drawable playDrawable = voicePlay.getDrawable();
            ThemeUtils.applyColorToDrawable(playDrawable, ThemeUtils.getThemeTextAndIconColor(mContext));
        }
    }

    private void resetAnim() {
        voicePlay.setImageResource(isRight ? R.drawable.sobot_voice_to_icon :
                R.drawable.sobot_voice_appoint_right_icon);
        Drawable playDrawable = voicePlay.getDrawable();
        if (playDrawable != null
                && playDrawable instanceof AnimationDrawable) {
            ThemeUtils.applyColorToDrawable(playDrawable, ThemeUtils.getThemeTextAndIconColor(mContext));
            ((AnimationDrawable) playDrawable).start();
        }
    }

    // 开始播放
    public void startAnim() {
        message.setVoideIsPlaying(true);

        Drawable playDrawable = voicePlay.getDrawable();
        if (playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).start();
        } else {
            resetAnim();
        }
    }

    // 关闭播放
    public void stopAnim() {
        message.setVoideIsPlaying(false);

        Drawable playDrawable = voicePlay.getDrawable();
        if (playDrawable != null
                && playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).stop();
            ((AnimationDrawable) playDrawable).selectDrawable(2);
        }
    }

    // 语音的重新发送
    public static class RetrySendVoiceLisenter implements View.OnClickListener {
        private String voicePath;
        private String id;
        private String duration;
        private ImageView img;
        private Context context;
        private SobotMsgAdapter.SobotMsgCallBack msgCallBack;

        public RetrySendVoiceLisenter(Context context, String id, String voicePath,
                                      String duration, ImageView image, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            super();
            this.context = context;
            this.msgCallBack = msgCallBack;
            this.voicePath = voicePath;
            this.id = id;
            this.duration = duration;
            this.img = image;
        }

        @Override
        public void onClick(View arg0) {

            if (img != null) {
                img.setClickable(false);
            }
            showReSendVoiceDialog(context, voicePath, id, duration, img);
        }

        private void showReSendVoiceDialog(final Context context, final String mvoicePath,
                                           final String mid, final String mduration, final ImageView msgStatus) {
            showReSendDialog(context, msgStatus, new ReSendListener() {

                @Override
                public void onReSend() {
                    if (context != null) {
                        ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
                        answer.setDuration(mduration);
                        msgObj.setContent(mvoicePath);
                        msgObj.setId(mid);
                        msgObj.setAnswer(answer);
                        if (msgCallBack != null) {
                            msgCallBack.sendMessageToRobot(msgObj, 2, 3, "");
                        }
                    }
                }
            });
        }
    }
}