package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 文本消息
 */
public class TextMessageHolder extends MsgHolderBase {
    TextView msg; // 聊天的消息内容
    LinearLayout sobot_ll_card;//超链接显示的卡片
    //离线留言信息标志
    TextView sobot_tv_icon;

    RelativeLayout sobot_ll_yinsi;
    TextView sobot_msg_temp; // 聊天的消息内容 临时的，隐私确认发送卡片时用到
    TextView sobot_sentisiveExplain;//隐私提示语
    TextView sobot_msg_temp_see_all;//展开消息，可以看全部
    Button sobot_sentisive_ok_send; //继续发送
    Button sobot_sentisive_cancle_send;//拒绝发送
    TextView sobot_sentisive_cancle_tip;//点击拒绝发送后的提示语

    public TextMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msg = (TextView) convertView.findViewById(R.id.sobot_msg);
        sobot_ll_card = convertView.findViewById(R.id.sobot_ll_card);
        sobot_tv_icon = (TextView) convertView.findViewById(R.id.sobot_tv_icon);
        if (sobot_tv_icon != null) {
            sobot_tv_icon.setText(R.string.sobot_leavemsg_title);
        }
        sobot_ll_yinsi = (RelativeLayout) convertView.findViewById(R.id.sobot_ll_yinsi);
        sobot_msg_temp = (TextView) convertView.findViewById(R.id.sobot_msg_temp);
        sobot_sentisiveExplain = (TextView) convertView.findViewById(R.id.sobot_sentisiveExplain);
        sobot_msg_temp_see_all = (TextView) convertView.findViewById(R.id.sobot_msg_temp_see_all);
        sobot_sentisive_ok_send = (Button) convertView.findViewById(R.id.sobot_sentisive_ok_send);
        sobot_sentisive_cancle_send = (Button) convertView.findViewById(R.id.sobot_sentisive_cancle_send);
        sobot_sentisive_cancle_tip = (TextView) convertView.findViewById(R.id.sobot_sentisive_cancle_tip);
        msg.setMaxWidth(msgMaxWidth);
        try {
            sobot_tv_icon.setTextColor(ThemeUtils.getThemeColor(mContext));
        } catch (Exception e) {
        }
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && (!TextUtils.isEmpty(message.getAnswer().getMsg()) || !TextUtils.isEmpty(message.getAnswer().getMsgTransfer()))) {// 纯文本消息
            final String content = !TextUtils.isEmpty(message.getAnswer().getMsgTransfer()) ? message.getAnswer().getMsgTransfer() : message.getAnswer().getMsg();
            msg.setVisibility(View.VISIBLE);

            HtmlTools.getInstance(context).setRichText(msg, content, isRight ? getLinkTextColor() : getLinkTextColor());
            if (!TextUtils.isEmpty(content) && HtmlTools.isHasPatterns(content)) {
                //只有一个，是超链接，并且是卡片形式才显示卡片
                final View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_link_card, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 240), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, ScreenUtils.dip2px(mContext, 10));
                view.setLayoutParams(layoutParams);
                TextView tv_title = view.findViewById(R.id.tv_title);
                tv_title.setText(R.string.sobot_parsing);
                if (message.getSobotLink() != null) {
                    tv_title = view.findViewById(R.id.tv_title);
                    TextView tv_des = view.findViewById(R.id.tv_des);
                    ImageView image_link = view.findViewById(R.id.image_link);
                    tv_title.setText(message.getSobotLink().getTitle());
                    tv_des.setText(TextUtils.isEmpty(message.getSobotLink().getDesc()) ? content : message.getSobotLink().getDesc());
                    if (!TextUtils.isEmpty(message.getSobotLink().getImgUrl())) {
                        SobotBitmapUtil.display(mContext, message.getSobotLink().getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                    }
                    if (TextUtils.isEmpty(message.getSobotLink().getTitle())) {
                        tv_title.setVisibility(View.GONE);
                    } else {
                        tv_title.setText(message.getSobotLink().getTitle());
                        tv_title.setVisibility(View.VISIBLE);
                    }
                    if (TextUtils.isEmpty(message.getSobotLink().getTitle()) && TextUtils.isEmpty(message.getSobotLink().getDesc()) && TextUtils.isEmpty(message.getSobotLink().getImgUrl())) {
                        view.setVisibility(View.GONE);
                    }
                } else {
                    SobotMsgManager.getInstance(mContext).getZhiChiApi().getHtmlAnalysis(context, content, new StringResultCallBack<SobotLink>() {
                        @Override
                        public void onSuccess(SobotLink link) {
                            if (link != null) {
                                message.setSobotLink(link);
                                TextView tv_title = view.findViewById(R.id.tv_title);
                                TextView tv_des = view.findViewById(R.id.tv_des);
                                ImageView image_link = view.findViewById(R.id.image_link);
                                tv_des.setText(TextUtils.isEmpty(link.getDesc()) ? content : link.getDesc());
                                if (!TextUtils.isEmpty(link.getImgUrl())) {
                                    SobotBitmapUtil.display(mContext, link.getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                }
                                if (TextUtils.isEmpty(link.getTitle())) {
                                    tv_title.setVisibility(View.GONE);
                                } else {
                                    tv_title.setText(link.getTitle());
                                    tv_title.setVisibility(View.VISIBLE);
                                }
                                if (TextUtils.isEmpty(link.getTitle()) && TextUtils.isEmpty(link.getDesc()) && TextUtils.isEmpty(link.getImgUrl())) {
                                    view.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String s) {
                            if (view != null) {
                                //   view.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                if (sobot_ll_card != null && sobot_ll_card instanceof LinearLayout) {
                    sobot_ll_card.setVisibility(View.VISIBLE);
                    sobot_ll_card.removeAllViews();
                    sobot_ll_card.addView(view);
                } else {
                    sobot_ll_card.setVisibility(View.GONE);
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (SobotOption.newHyperlinkListener != null) {
                            //如果返回true,拦截;false 不拦截
                            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, content);
                            if (isIntercept) {
                                return;
                            }
                        }
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("url", content);
                        context.startActivity(intent);
                    }
                });
            } else {
                sobot_ll_card.setVisibility(View.GONE);
            }

            if (isRight) {
                try {
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        if (!StringUtils.isEmpty(message.getDesensitizationWord())) {
                            HtmlTools.getInstance(context).setRichText(msg, message.getDesensitizationWord(), isRight ? getLinkTextColor() : getLinkTextColor());
                        }
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                        if (message.getSentisive() == 1) {
                            sobot_msg_content_ll.setVisibility(View.GONE);
                            sobot_ll_yinsi.setVisibility(View.VISIBLE);
                            if (!StringUtils.isEmpty(message.getDesensitizationWord())) {
                                HtmlTools.getInstance(context).setRichText(sobot_msg_temp, message.getDesensitizationWord(), getLinkTextColor());
                            } else {
                                HtmlTools.getInstance(context).setRichText(sobot_msg_temp, content, getLinkTextColor());
                            }
                            sobot_sentisiveExplain.setText(message.getSentisiveExplain());
                            sobot_msg_temp.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (sobot_msg_temp.getLineCount() >= 3 && !message.isShowSentisiveSeeAll()) {
                                        sobot_msg_temp.setMaxLines(3);
                                        sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), 0);
                                        sobot_msg_temp_see_all.setVisibility(View.VISIBLE);
                                        LinearGradient mLinearGradient = new LinearGradient(0, 0, 0, sobot_msg_temp.getMeasuredHeight(), new int[]{R.color.sobot_common_gray2, R.color.sobot_common_gray2, R.color.sobot_common_gray3}, new float[]{0, 0.5f, 1.0f}, Shader.TileMode.CLAMP);
                                        sobot_msg_temp.getPaint().setShader(mLinearGradient);
                                        sobot_msg_temp.invalidate();
                                    } else {
                                        sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10));
                                        sobot_msg_temp_see_all.setVisibility(View.GONE);
                                        sobot_msg_temp.setMaxLines(100);
                                    }
                                }
                            });
                            sobot_msg_temp_see_all.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 10));
                                    sobot_msg_temp.setMaxLines(100);
                                    sobot_msg_temp.getPaint().setShader(null);
                                    sobot_msg_temp_see_all.setVisibility(View.GONE);
                                    message.setShowSentisiveSeeAll(true);
                                }
                            });
                            sobot_sentisive_ok_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                                            ZhiChiConstant.sobot_last_current_initModel);
                                    message.setMsgId(System.currentTimeMillis() + "");
                                    message.setContent(content);
                                    //继续发送
                                    SobotMsgManager.getInstance(context).getZhiChiApi().authSensitive(content, initMode.getPartnerid(), "1", new StringResultCallBack<CommonModel>() {
                                        @Override
                                        public void onSuccess(CommonModel baseCode) {
                                            if (baseCode.getData() != null && !TextUtils.isEmpty(baseCode.getData().getStatus())) {
                                                //  返回值：status 1 成功 status 2 会话已结束 status 3 已授权 status 0 失败
                                                if ("1".equals(baseCode.getData().getStatus()) || "2".equals(baseCode.getData().getStatus()) || "3".equals(baseCode.getData().getStatus())) {
                                                    msgCallBack.removeMessageByMsgId(message.getId());
                                                    msgCallBack.sendMessage(content);
                                                    if (!"3".equals(baseCode.getData().getStatus())) {
                                                        //显示系统提示 "您已同意发送个人敏感信息，本次授权有效期"
                                                        ZhiChiMessageBase base = new ZhiChiMessageBase();
                                                        base.setAction(ZhiChiConstant.action_sensitive_auth_agree + "");
                                                        base.setMsgId(System.currentTimeMillis() + "");
                                                        msgCallBack.addMessage(base);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e, String des) {

                                        }
                                    });
                                }
                            });
                            if (message.isClickCancleSend()) {
                                sobot_sentisive_cancle_tip.setVisibility(View.VISIBLE);
                                sobot_sentisive_cancle_send.setVisibility(View.GONE);
                            } else {
                                sobot_sentisive_cancle_tip.setVisibility(View.GONE);
                                sobot_sentisive_cancle_send.setVisibility(View.VISIBLE);
                            }
                            sobot_sentisive_cancle_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //拒绝发送
                                    ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                                            ZhiChiConstant.sobot_last_current_initModel);
                                    SobotMsgManager.getInstance(context).getZhiChiApi().authSensitive(content, initMode.getPartnerid(), "0", new StringResultCallBack<CommonModel>() {
                                        @Override
                                        public void onSuccess(CommonModel baseCode) {
                                            if (baseCode.getData() != null && !TextUtils.isEmpty(baseCode.getData().getStatus())) {
                                                //  返回值：status 1 成功 status 2 会话已结束 status 3 已授权 status 0 失败
                                                if (!"0".equals(baseCode.getData().getStatus())) {
                                                    message.setClickCancleSend(true);
                                                    sobot_sentisive_cancle_tip.setVisibility(View.VISIBLE);
                                                    sobot_sentisive_cancle_send.setVisibility(View.GONE);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e, String des) {

                                        }
                                    });
                                }
                            });
                        } else {
                            sobot_msg_content_ll.setVisibility(View.VISIBLE);
                            sobot_ll_yinsi.setVisibility(View.GONE);
                        }
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
                        msgStatus.setOnClickListener(new ReSendTextLisenter(context, message
                                .getId(), content, msgStatus, msgCallBack));
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        msgProgressBar.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.GONE);
                    }
                    if (sobot_tv_icon != null) {
                        sobot_tv_icon.setVisibility(message.isLeaveMsgFlag() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // msg.setText(CommonUtils.getResString(context, "sobot_data_wrong_hint"));
            msg.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp= (LinearLayout.LayoutParams) stripe.getLayoutParams();
            lp.topMargin=0;
        }
        if (sobot_msg_content_ll != null) {
            setCopyAndAppointView(context, msg);
            setCopyAndAppointView(context, sobot_msg_content_ll);
        }

        //没有顶和踩时显示信息显示一行 42-10-10=22 总高度减去上下内间距
        //msg.setMinHeight(ScreenUtils.dip2px(mContext, 22));
        //有顶和踩时显示信息显示两行 72-10-10=52 总高度减去上下内间距
        // msg.setMinHeight(ScreenUtils.dip2px(mContext, 52));
        if (!isRight) {
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
            } else {
                hideAnswers();
            }
        }
        refreshReadStatus();
    }

    private void setCopyAndAppointView(final Context context, View view) {
        if (sobot_msg_content_ll != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!TextUtils.isEmpty(msg.getText().toString())) {
                        showCopyAndAppointPopWindows(context, sobot_msg_content_ll, msg.getText().toString().replace("&amp;", "&"), 0, 18);
                    }
                    return true;
                }
            });
        }
    }

    public static class ReSendTextLisenter implements View.OnClickListener {

        private String id;
        private String msgContext;
        private ImageView msgStatus;
        private Context context;
        private SobotMsgAdapter.SobotMsgCallBack msgCallBack;

        public ReSendTextLisenter(final Context context, String id, String msgContext, ImageView
                msgStatus, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            super();
            this.context = context;
            this.msgCallBack = msgCallBack;
            this.id = id;
            this.msgContext = msgContext;
            this.msgStatus = msgStatus;
        }

        @Override
        public void onClick(View arg0) {
            if (msgStatus != null) {
                msgStatus.setClickable(false);
            }
            showReSendTextDialog(context, id, msgContext, msgStatus);
        }

        private void showReSendTextDialog(final Context context, final String mid,
                                          final String mmsgContext, final ImageView msgStatus) {
            showReSendDialog(context, msgStatus, new ReSendListener() {

                @Override
                public void onReSend() {
                    sendTextBrocast(context, mid, mmsgContext);
                }
            });
        }

        private void sendTextBrocast(Context context, String id, String msgContent) {
            if (msgCallBack != null) {
                ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                msgObj.setContent(msgContent);
                msgObj.setId(id);
                msgCallBack.sendMessageToRobot(msgObj, 1, 0, "");
            }
        }
    }
}
