package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
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
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();

    public TextMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msg = (TextView) convertView.findViewById(R.id.sobot_msg);
        sobot_ll_card = convertView.findViewById(R.id.sobot_ll_card);
        sobot_tv_icon = (TextView) convertView.findViewById(R.id.sobot_tv_icon);
        if (sobot_tv_icon != null) {
            sobot_tv_icon.setText(R.string.sobot_leavemsg_title);
        }
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        try {
            sobot_tv_icon.setTextColor(ThemeUtils.getThemeColor(mContext));
        } catch (Exception e) {
        }
        if (message.getAnswer() != null && (!TextUtils.isEmpty(message.getAnswer().getMsg()) || !TextUtils.isEmpty(message.getAnswer().getMsgTransfer()))) {// 纯文本消息
            final String content = !TextUtils.isEmpty(message.getAnswer().getMsgTransfer()) ? message.getAnswer().getMsgTransfer() : message.getAnswer().getMsg();
            msg.setVisibility(View.VISIBLE);

            HtmlTools.getInstance(context).setRichText(msg, content, isRight ? getLinkTextColor() : getLinkTextColor());
            if (!TextUtils.isEmpty(content) && HtmlTools.isHasPatterns(content)) {
                //只有一个，是超链接，并且是卡片形式才显示卡片
                final View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_link_card, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgCardWidth - 12, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, ScreenUtils.dip2px(mContext, 4));
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
                                TextView tv_title = view.findViewById(R.id.tv_title);
                                tv_title.setText(content);
                                ImageView image_link = view.findViewById(R.id.image_link);
                                SobotBitmapUtil.display(mContext, "", image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
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
                    msg.setTextColor(ThemeUtils.getThemeTextAndIconColor(mContext));
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        if (!StringUtils.isEmpty(message.getDesensitizationWord())) {
                            HtmlTools.getInstance(context).setRichText(msg, message.getDesensitizationWord(), isRight ? getLinkTextColor() : getLinkTextColor());
                        }
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                        sobot_msg_content_ll.setVisibility(View.VISIBLE);
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
                        msgStatus.setOnClickListener(new ReSendTextLisenter(context, message
                                .getId(), content, msgStatus, msgCallBack));
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                        loadingRunnable = new Runnable() {
                            @Override
                            public void run() {
                                msgProgressBar.setVisibility(View.VISIBLE);
                                msgStatus.setVisibility(View.GONE);
                            }
                        };
                        handler.postDelayed(loadingRunnable, ZCSobotConstant.LOADING_TIME);
                    }
                    if (sobot_tv_icon != null) {
                        sobot_tv_icon.setVisibility(message.isLeaveMsgFlag() ? View.VISIBLE : View.GONE);
                    }
                } catch (Exception e) {
                    // 当状态变为成功或失败时，移除延迟任务
                    if (handler != null && loadingRunnable != null) {
                        handler.removeCallbacks(loadingRunnable);
                    }
                }
            }
        } else {
            // msg.setText(CommonUtils.getResString(context, "sobot_data_wrong_hint"));
            msg.setVisibility(View.GONE);
            if (stripe != null) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) stripe.getLayoutParams();
                lp.topMargin = 0;
            }
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
