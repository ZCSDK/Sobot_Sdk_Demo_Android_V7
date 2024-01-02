package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import androidx.core.content.ContextCompat;
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
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotQuoteDetailActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.ArticleModel;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.MiniProgramModel;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.SobotLocationModel;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageLocationModel;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StMapOpenHelper;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.gson.SobotGsonUtil;
import com.sobot.gson.reflect.TypeToken;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 带有引用的文本消息
 */
public class AppointTextMessageHolder extends MsgHolderBase {
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
    private LinearLayout sobot_appoint_content_ll;//引用内容的view
    private LinearLayout sobot_rich_ll;
    private TextView tv_appoint_type;

    public AppointTextMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msg = (TextView) convertView.findViewById(R.id.sobot_msg);
        sobot_ll_card = convertView.findViewById(R.id.sobot_ll_card);
        sobot_appoint_content_ll = convertView.findViewById(R.id.sobot_appoint_content_ll);
        sobot_rich_ll = convertView.findViewById(R.id.sobot_rich_ll);
        sobot_tv_icon = (TextView) convertView.findViewById(R.id.sobot_tv_icon);
        tv_appoint_type = (TextView) convertView.findViewById(R.id.tv_appoint_type);
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
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_link_card, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, 240), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, ScreenUtils.dip2px(mContext, 10));
                view.setLayoutParams(layoutParams);
                showLinkUI(context, message, content, view);
                if (sobot_ll_card != null && sobot_ll_card instanceof LinearLayout) {
                    sobot_ll_card.setVisibility(View.VISIBLE);
                    sobot_ll_card.removeAllViews();
                    sobot_ll_card.addView(view);
                } else {
                    sobot_ll_card.setVisibility(View.GONE);
                }
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
            msg.setText("");
        }
        if (sobot_msg_content_ll != null) {
            setCopyAndAppointView(context, msg);
            setCopyAndAppointView(context, sobot_msg_content_ll);
            setCopyAndAppointView(context, sobot_appoint_content_ll);
        }
        if (sobot_appoint_content_ll != null && sobot_rich_ll != null && message.getAppointMessage() != null && tv_appoint_type != null) {
//            resetMaxWidth(sobot_appoint_content_ll);
            showAppointMessage(message.getAppointMessage(), sobot_rich_ll, mContext, isRight);
            String tempStr = "";
            // -1-未知 0-客服 1-客户 2-引用机器人
            if (message.getAppointMessage().getAppointType() == 0) {
                tempStr = mContext.getResources().getString(R.string.sobot_cus_service);
            } else if (message.getAppointMessage().getAppointType() == 1) {
                tempStr = mContext.getResources().getString(R.string.sobot_str_my);
            } else if (message.getAppointMessage().getAppointType() == 2) {
                tempStr = mContext.getResources().getString(R.string.sobot_cus_service);
            }
            tv_appoint_type.setText(tempStr);
            sobot_appoint_content_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //跳转到引用详情
                    Intent intent = new Intent(mContext, SobotQuoteDetailActivity.class);
                    intent.putExtra("AppointMessage", message.getAppointMessage());
                    mContext.startActivity(intent);
                }
            });
            sobot_appoint_content_ll.setVisibility(View.VISIBLE);
        } else {
            sobot_appoint_content_ll.setVisibility(View.GONE);
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

    private void showLinkUI(final Context context, final ZhiChiMessageBase message, final String content, final View view) {
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

    public void showAppointMessage(final ZhiChiAppointMessage appointMessage, LinearLayout sobot_rich_ll, final Context context, final boolean isRight) {
        if (appointMessage != null && context != null && sobot_rich_ll != null) {
            sobot_rich_ll.removeAllViews();
            //0文本,1图片,2音频,3视频,4文件,5对象,当msgType=5 时，根据content里边的 type 判断具体的时哪种消息 0-富文本 1-多伦会话 2-位置 3-小卡片 4-订单卡片 6-小程序 17-文章 21-自定义卡片
            int msgType = appointMessage.getMsgType();
            if (msgType == 0) {
                //文本
                String text = appointMessage.getContent();
                showTextView(sobot_rich_ll, context, isRight, text, 3);
            } else if (msgType == 1) {
                //图片
                final String url = appointMessage.getContent();
                showImageView(sobot_rich_ll, context, url, true);
            } else if (msgType == 2) {
                //音频、文件
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("url") && !TextUtils.isEmpty(contentJsonObject.optString("url"))) {
                        if (contentJsonObject.has("voiceType") && contentJsonObject.optInt("voiceType") == 1) {
                            //音频，知识库返回的
                            final SobotCacheFile cacheFile = new SobotCacheFile();
                            cacheFile.setUrl(contentJsonObject.optString("url"));
                            cacheFile.setFileName(contentJsonObject.optString("fileName"));
                            cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                            cacheFile.setMsgId(appointMessage.getMsgId());
                            showFileView(sobot_rich_ll, context, cacheFile, false, true);
                        } else {
                            //0 语音
                            View view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_audio, null);
                            TextView voiceTimeLong = view.findViewById(R.id.sobot_voiceTimeLong);
                            final ImageView voiceIV = view.findViewById(R.id.sobot_iv_voice);
                            final String duration = StringUtils.checkStringIsNull(contentJsonObject.optString("duration"));
                            voiceTimeLong.setText(duration == null ?
                                    "" : (DateUtil.stringToLongMs(duration) == 0 ? "" : (DateUtil.stringToLongMs(duration) + "''")));
                            if (isRight) {
                                voiceIV.setImageDrawable(context.getResources().getDrawable(R.drawable.sobot_pop_voice_send_anime_appoint_3));
                                view.setBackgroundResource(R.drawable.sobot_chat_msg_bg_transparent);
                                voiceTimeLong.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
                            } else {
                                voiceIV.setImageDrawable(context.getResources().getDrawable(R.drawable.sobot_pop_voice_send_anime_appoint_left_3));
                                view.setBackgroundResource(R.drawable.sobot_chat_msg_left_bg_transparent);
                                voiceTimeLong.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_card_text_color));
                            }

                            final String url = StringUtils.checkStringIsNull(contentJsonObject.optString("url"));
                            view.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (msgCallBack != null) {
                                        ZhiChiMessageBase messageBase = new ZhiChiMessageBase();
                                        messageBase.setMsgId(url);
                                        messageBase.setSugguestionsFontColor(1);
                                        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                                        //0 语音
                                        reply.setMsgType(ZhiChiConstant.message_type_voice);
                                        reply.setDuration(duration);
                                        reply.setMsg(url);
                                        messageBase.setAnswer(reply);
                                        messageBase.setVoiceIV(voiceIV);
                                        messageBase.setRight(isRight);
                                        msgCallBack.clickAudioItem(messageBase, null, isRight);
                                    }
                                }
                            });
                            setCopyAndAppointView(context, view);
                            sobot_rich_ll.addView(view);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msgType == 4) {
                //文件
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("url") && !TextUtils.isEmpty(contentJsonObject.optString("url"))) {
                        final SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setUrl(contentJsonObject.optString("url"));
                        cacheFile.setFileName(contentJsonObject.optString("fileName"));
                        cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                        cacheFile.setMsgId(appointMessage.getMsgId());
                        showFileView(sobot_rich_ll, context, cacheFile, false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msgType == 3) {
                //视频
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("url") && !TextUtils.isEmpty(contentJsonObject.optString("url"))) {
                        final SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setUrl(contentJsonObject.optString("url"));
                        cacheFile.setFileName(contentJsonObject.optString("fileName"));
                        cacheFile.setFileType(GsonUtil.changeFileType(contentJsonObject.optInt("type")));
                        cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                        cacheFile.setSnapshot(contentJsonObject.optString("snapshot"));
                        cacheFile.setMsgId(appointMessage.getMsgId());
                        showVideoView(sobot_rich_ll, context, cacheFile, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msgType == 5) {
                //对象， 当msgType=5 时，根据content里边的 type 判断具体的时哪种消息0-富文本 1-多伦会话 2-位置 3-小卡片 4-订单卡片 6-小程序 17-文章 21-自定义卡片
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("type") && !TextUtils.isEmpty(contentJsonObject.optString("type"))) {
                        if ("0".equals(contentJsonObject.optString("type"))) {
                            //富文本类型
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                JSONObject msgJsonObject = new JSONObject(contentJsonObject.optString("msg"));
                                if (msgJsonObject.has("richList") && !GsonUtil.isEmpty(msgJsonObject.optString("richList"))) {
                                    JSONArray data = msgJsonObject.getJSONArray("richList");
                                    if (data != null) {
                                        List<ChatMessageRichListModel> list = new ArrayList<>();
                                        for (int i = 0; i < data.length(); i++) {
                                            ChatMessageRichListModel base = new ChatMessageRichListModel();
                                            JSONObject obj = data.getJSONObject(i);
                                            if (obj != null) {
                                                if (obj.has("type")) {
                                                    base.setType(obj.optInt("type"));
                                                }
                                                if (obj.has("name")) {
                                                    base.setName(StringUtils.checkStringIsNull(obj.optString("name")));
                                                }
                                                if (obj.has("msg")) {
                                                    base.setMsg(StringUtils.checkStringIsNull(obj.optString("msg")));
                                                }
                                                if (obj.has("showType")) {
                                                    base.setShowType(obj.optInt("showType"));
                                                }
                                                if (obj.has("fileSize")) {
                                                    base.setFileSize(StringUtils.checkStringIsNull(obj.optString("fileSize")));
                                                }
                                                if (obj.has("videoImgUrl")) {
                                                    base.setFileSize(StringUtils.checkStringIsNull(obj.optString("videoImgUrl")));
                                                }
                                            }
                                            list.add(base);
                                        }
                                        if (list.size() > 1) {
                                            //richList 数量大于1个，如果里边有不是卡片的超链接，超链接的上个又是文本的情况，需要单独处理（合并到上个文本后边）
                                            List<ChatMessageRichListModel> tempRichList = new ArrayList<>();
                                            for (int i = 0; i < list.size(); i++) {
                                                //处理后的临时richList,替换旧的richList
                                                ChatMessageRichListModel richListModel = list.get(i);
                                                if (richListModel != null) {
                                                    //如果当前是文本,文本又不是卡片，需要处理
                                                    if (richListModel.getType() == 0 && richListModel.getShowType() != 1) {
                                                        ChatMessageRichListModel model = new ChatMessageRichListModel();
                                                        model.setType(0);
                                                        if (tempRichList.size() > 0) {
                                                            //如果上一个是文本,需要合并当前文本到上个文本后边
                                                            ChatMessageRichListModel tempRichListModel = tempRichList.get(tempRichList.size() - 1);
                                                            if (tempRichListModel != null && tempRichListModel.getType() == 0) {
                                                                if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                                                    model.setMsg(tempRichListModel.getMsg() + "<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                                                } else {
                                                                    model.setMsg(tempRichListModel.getMsg() + richListModel.getMsg());
                                                                }
                                                                tempRichList.remove(tempRichList.size() - 1);
                                                                tempRichList.add(model);
                                                            } else {
                                                                tempRichList.add(richListModel);
                                                            }
                                                        } else {
                                                            if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                                                //当前是超链接，同时又不是卡片
                                                                model.setMsg("<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                                            } else {
                                                                model.setMsg(richListModel.getMsg());
                                                            }
                                                            tempRichList.add(model);
                                                        }
                                                    } else {
                                                        tempRichList.add(richListModel);
                                                    }
                                                }
                                            }
                                            if (tempRichList != null && tempRichList.size() > 0) {
                                                message.getAnswer().setRichList(tempRichList);
                                            }
                                            if (tempRichList != null && tempRichList.size() > 0) {
                                                list.clear();
                                                list.addAll(tempRichList);
                                            }
                                        }

                                        if (list != null && list.size() > 0) {
                                            ChatMessageRichListModel richListModel1 = list.get(0);
                                            if (list.size() > 1) {
                                                ChatMessageRichListModel richListModel2 = list.get(1);
                                                if (richListModel1.getType() != 0 && richListModel2.getType() != 0) {
                                                    //前二个都是不是文本
                                                    showRichView(sobot_rich_ll, context, richListModel1, isRight, 1);
                                                    showTextView(sobot_rich_ll, context, isRight, "...", 1);
                                                } else {
                                                    showRichView(sobot_rich_ll, context, richListModel1, isRight, 1);
                                                    showRichView(sobot_rich_ll, context, richListModel2, isRight, 1);
                                                }
                                            } else if (list.size() == 1) {
                                                showRichView(sobot_rich_ll, context, richListModel1, isRight, 3);
                                            }
                                        }
                                    }
                                }
                            }
                            sobot_rich_ll.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //跳转到引用详情
                                    Intent intent = new Intent(context, SobotQuoteDetailActivity.class);
                                    intent.putExtra("AppointMessage", message.getAppointMessage());
                                    context.startActivity(intent);
                                }
                            });
                            setCopyAndAppointView(context, sobot_rich_ll);
                        } else if ("1".equals(contentJsonObject.optString("type"))) {
                            //多伦会话类型 只有历史记录有，机器人实时接口返回没有message
                        } else if ("2".equals(contentJsonObject.optString("type"))) {
                            //位置
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                JSONObject jsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                final ZhiChiMessageLocationModel locationData = new ZhiChiMessageLocationModel();
                                locationData.setLat(StringUtils.checkStringIsNull(jsonObj.optString("lat")));
                                locationData.setLng(StringUtils.checkStringIsNull(jsonObj.optString("lng")));
                                locationData.setDesc(StringUtils.checkStringIsNull(jsonObj.optString("desc")));
                                locationData.setTitle(StringUtils.checkStringIsNull(jsonObj.optString("title")));
                                locationData.setUrl(StringUtils.checkStringIsNull(jsonObj.optString("url")));
                                View otherView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_other, null);
                                TextView tv_title = otherView.findViewById(R.id.tv_title);
                                ImageView iv_type = otherView.findViewById(R.id.iv_type);
                                ImageView iv_type_left = otherView.findViewById(R.id.iv_type_left);
                                if (isRight) {
                                    iv_type_left.setBackgroundResource(R.drawable.sobon_icon_map);
                                    otherView.setBackgroundResource(R.drawable.sobot_chat_msg_bg_transparent);
                                    tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
                                } else {
                                    iv_type_left.setBackgroundResource(R.drawable.sobon_icon_map_left);
                                    otherView.setBackgroundResource(R.drawable.sobot_chat_msg_left_bg_transparent);
                                    tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_card_text_color));
                                }

                                iv_type_left.setVisibility(View.VISIBLE);
                                SobotBitmapUtil.display(context, R.drawable.sobot_bg_default_map, iv_type);
                                tv_title.setText(StringUtils.checkStringIsNull(locationData.getTitle()));
                                otherView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 217), ViewGroup.LayoutParams.WRAP_CONTENT));
                                sobot_rich_ll.addView(otherView);
                                otherView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (locationData != null) {
                                            SobotLocationModel sobotLocationModel = new SobotLocationModel();
                                            sobotLocationModel.setLat(locationData.getLat());
                                            sobotLocationModel.setLng(locationData.getLng());
                                            sobotLocationModel.setSnapshot(locationData.getPicUrl());
                                            sobotLocationModel.setLocalName(locationData.getTitle());
                                            sobotLocationModel.setLocalLabel(locationData.getDesc());
                                            if (SobotOption.mapCardListener != null) {
                                                //如果返回true,拦截;false 不拦截
                                                boolean isIntercept = SobotOption.mapCardListener.onClickMapCradMsg(context, sobotLocationModel);
                                                if (isIntercept) {
                                                    return;
                                                }
                                            }
                                            StMapOpenHelper.openMap(context, sobotLocationModel);
                                        }
                                    }
                                });
                                setCopyAndAppointView(context, otherView);
                            }
                        } else if ("3".equals(contentJsonObject.optString("type"))) {
                            //商品卡片
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                try {
                                    JSONObject cardJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                    final ConsultingContent consultingContent = new ConsultingContent();
                                    consultingContent.setSobotGoodsTitle(StringUtils.checkStringIsNull(cardJsonObj.optString("title")));
                                    consultingContent.setSobotGoodsFromUrl(StringUtils.checkStringIsNull(cardJsonObj.optString("url")));
                                    consultingContent.setSobotGoodsDescribe(StringUtils.checkStringIsNull(cardJsonObj.optString("description")));
                                    consultingContent.setSobotGoodsLable(StringUtils.checkStringIsNull(cardJsonObj.optString("label")));
                                    consultingContent.setSobotGoodsImgUrl(StringUtils.checkStringIsNull(cardJsonObj.optString("thumbnail")));
                                    View cardView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_card, null);
                                    ImageView mPic = cardView.findViewById(R.id.sobot_goods_pic);
                                    TextView mTitle = cardView.findViewById(R.id.sobot_goods_title);
                                    TextView mLabel = cardView.findViewById(R.id.sobot_goods_label);
                                    TextView mDes = cardView.findViewById(R.id.sobot_goods_des);
                                    int defaultPicResId = R.drawable.sobot_icon_consulting_default_pic;
                                    if (consultingContent != null) {
                                        if (!TextUtils.isEmpty(CommonUtils.encode(consultingContent.getSobotGoodsImgUrl()))) {
                                            mPic.setVisibility(View.VISIBLE);
                                            mDes.setMaxLines(1);
                                            mDes.setEllipsize(TextUtils.TruncateAt.END);
                                            SobotBitmapUtil.display(context, CommonUtils.encode(consultingContent.getSobotGoodsImgUrl())
                                                    , mPic, defaultPicResId, defaultPicResId);
                                        } else {
                                            mPic.setVisibility(View.GONE);
                                        }

                                        mTitle.setText(consultingContent.getSobotGoodsTitle());
                                        mLabel.setText(consultingContent.getSobotGoodsLable());
                                        mDes.setText(consultingContent.getSobotGoodsDescribe());
                                        mLabel.setTextColor(ThemeUtils.getThemeColor(context));
                                    }
                                    cardView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 182), ViewGroup.LayoutParams.WRAP_CONTENT));
                                    sobot_rich_ll.addView(cardView);
                                    cardView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (SobotOption.newHyperlinkListener != null) {
                                                //如果返回true,拦截;false 不拦截
                                                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(context, consultingContent.getSobotGoodsFromUrl());
                                                if (isIntercept) {
                                                    return;
                                                }
                                            }
                                            Intent intent = new Intent(context, WebViewActivity.class);
                                            intent.putExtra("url", consultingContent.getSobotGoodsFromUrl());
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                        }
                                    });
                                    setCopyAndAppointView(context, cardView);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("4".equals(contentJsonObject.optString("type"))) {
                            //订单卡片

                        } else if ("6".equals(contentJsonObject.optString("type"))) {
                            //小程序卡片
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                try {
                                    JSONObject miniJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                    final MiniProgramModel model = new MiniProgramModel();
                                    if (miniJsonObj.has("title")) {
                                        model.setTitle(StringUtils.checkStringIsNull(miniJsonObj.optString("title")));
                                    }
                                    if (miniJsonObj.has("describe")) {
                                        model.setDescribe(StringUtils.checkStringIsNull(miniJsonObj.optString("describe")));
                                    }
                                    if (miniJsonObj.has("appId")) {
                                        model.setAppId(StringUtils.checkStringIsNull(miniJsonObj.optString("appId")));
                                    }
                                    if (miniJsonObj.has("pagepath")) {
                                        model.setPagepath(StringUtils.checkStringIsNull(miniJsonObj.optString("pagepath")));
                                    }
                                    if (miniJsonObj.has("thumbUrl")) {
                                        model.setThumbUrl(StringUtils.checkStringIsNull(miniJsonObj.optString("thumbUrl")));
                                    }
                                    if (miniJsonObj.has("logo")) {
                                        model.setLogo(StringUtils.checkStringIsNull(miniJsonObj.optString("logo")));
                                    }
                                    View otherView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_other, null);
                                    TextView tv_title = otherView.findViewById(R.id.tv_title);
                                    ImageView iv_type = otherView.findViewById(R.id.iv_type);
                                    if (StringUtils.isNoEmpty(model.getThumbUrl())) {
                                        SobotBitmapUtil.display(context, model.getThumbUrl(), iv_type);
                                    }
                                    ImageView iv_type_left = otherView.findViewById(R.id.iv_type_left);
                                    iv_type_left.setVisibility(View.VISIBLE);
                                    if (isRight) {
                                        iv_type_left.setBackgroundResource(R.drawable.sobot_mini_program_logo_white);
                                        otherView.setBackgroundResource(R.drawable.sobot_chat_msg_bg_transparent);
                                        tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
                                    } else {
                                        iv_type_left.setBackgroundResource(R.drawable.sobot_mini_program_logo_gray);
                                        otherView.setBackgroundResource(R.drawable.sobot_chat_msg_left_bg_transparent);
                                        tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_card_text_color));
                                    }
                                    tv_title.setText(StringUtils.checkStringIsNull(model.getTitle()));
                                    otherView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 217), ViewGroup.LayoutParams.WRAP_CONTENT));
                                    sobot_rich_ll.addView(otherView);
                                    otherView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (SobotOption.miniProgramClickListener != null) {
                                                SobotOption.miniProgramClickListener.onClick(context, model);
                                            } else {
                                                ToastUtil.showToast(context, context.getResources().getString(R.string.sobot_mini_program_only_open_by_weixin));
                                            }
                                        }
                                    });
                                    setCopyAndAppointView(context, otherView);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("17".equals(contentJsonObject.optString("type"))) {
                            //文章卡片
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                try {
                                    JSONObject miniJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                    ArticleModel model = new ArticleModel();
                                    if (miniJsonObj.has("content")) {
                                        model.setContent(StringUtils.checkStringIsNull(miniJsonObj.optString("content")));
                                    }
                                    if (miniJsonObj.has("articleBody")) {
                                        model.setArticleBody(StringUtils.checkStringIsNull(miniJsonObj.optString("articleBody")));
                                    }
                                    if (miniJsonObj.has("desc")) {
                                        model.setDesc(StringUtils.checkStringIsNull(miniJsonObj.optString("desc")));
                                    }
                                    if (miniJsonObj.has("richMoreUrl")) {
                                        model.setRichMoreUrl(StringUtils.checkStringIsNull(miniJsonObj.optString("richMoreUrl")));
                                    }
                                    if (miniJsonObj.has("snapshot")) {
                                        model.setSnapshot(StringUtils.checkStringIsNull(miniJsonObj.optString("snapshot")));
                                    }
                                    if (miniJsonObj.has("title")) {
                                        model.setTitle(StringUtils.checkStringIsNull(miniJsonObj.optString("title")));
                                    }
                                    View otherView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_other, null);
                                    TextView tv_title = otherView.findViewById(R.id.tv_title);
                                    ImageView iv_type = otherView.findViewById(R.id.iv_type);
                                    if (StringUtils.isNoEmpty(model.getSnapshot())) {
                                        SobotBitmapUtil.display(context, model.getSnapshot(), iv_type);
                                    }
                                    if (isRight) {
                                        otherView.setBackgroundResource(R.drawable.sobot_chat_msg_bg_transparent);
                                        tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
                                    } else {
                                        otherView.setBackgroundResource(R.drawable.sobot_chat_msg_left_bg_transparent);
                                        tv_title.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_card_text_color));
                                    }
                                    tv_title.setText(StringUtils.checkStringIsNull(model.getTitle()));
                                    otherView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 217), ViewGroup.LayoutParams.WRAP_CONTENT));
                                    sobot_rich_ll.addView(otherView);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if ("21".equals(contentJsonObject.optString("type"))) {
                            //自定义卡片类型
                            SobotChatCustomCard model = SobotGsonUtil.jsonToBeans(contentJsonObject.optString("msg"),
                                    new TypeToken<SobotChatCustomCard>() {
                                    }.getType());
                            if (model != null && model.getCustomCards() != null && model.getCardType() == 1 && model.getCustomCards().size() == 1) {
                                View cardView = null;
                                if (model.getCardStyle() == 1) {
                                    //竖向
                                    cardView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_card, null);
                                    cardView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 182), ViewGroup.LayoutParams.WRAP_CONTENT));
                                } else {
                                    cardView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_card_h, null);
                                    cardView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 133), ViewGroup.LayoutParams.WRAP_CONTENT));
                                }

                                ImageView mPic = cardView.findViewById(R.id.sobot_goods_pic);
                                TextView mTitle = cardView.findViewById(R.id.sobot_goods_title);
                                TextView mLabel = cardView.findViewById(R.id.sobot_goods_label);
                                TextView mDes = cardView.findViewById(R.id.sobot_goods_des);
                                int defaultPicResId = R.drawable.sobot_icon_consulting_default_pic;
                                final SobotChatCustomGoods chatCustomGoods = model.getCustomCards().get(0);
                                if (chatCustomGoods != null) {
                                    if (!TextUtils.isEmpty(CommonUtils.encode(chatCustomGoods.getCustomCardThumbnail()))) {
                                        mPic.setVisibility(View.VISIBLE);
                                        mDes.setMaxLines(1);
                                        mDes.setEllipsize(TextUtils.TruncateAt.END);
                                        SobotBitmapUtil.display(context, CommonUtils.encode(chatCustomGoods.getCustomCardThumbnail())
                                                , mPic, defaultPicResId, defaultPicResId);
                                    } else {
                                        mPic.setVisibility(View.GONE);
                                    }

                                    mTitle.setText(chatCustomGoods.getCustomCardName());
                                    mLabel.setText(chatCustomGoods.getCustomCardAmount());
                                    mDes.setText(chatCustomGoods.getCustomCardDesc());
                                    mLabel.setTextColor(ThemeUtils.getThemeColor(context));
                                }
                                sobot_rich_ll.addView(cardView);
                                cardView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (StringUtils.isEmpty(chatCustomGoods.getCustomCardLink())) {
                                            return;
                                        }
                                        if (SobotOption.newHyperlinkListener != null) {
                                            //如果返回true,拦截;false 不拦截
                                            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(context, chatCustomGoods.getCustomCardLink());
                                            if (isIntercept) {
                                                return;
                                            }
                                        }
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("url", chatCustomGoods.getCustomCardLink());
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                });
                                setCopyAndAppointView(context, cardView);
                            }
                        }
                    }
                } catch (JSONException e) {
                }
            }
        }
    }


    private void showRichView(LinearLayout sobot_rich_ll, Context
            context, ChatMessageRichListModel richListModel1, boolean isRight, int maxLines) {
        if (richListModel1.getType() == 0) {
            showTextView(sobot_rich_ll, context, isRight, richListModel1.getMsg(), maxLines);
        } else if (richListModel1.getType() == 1) {
            showImageView(sobot_rich_ll, context, richListModel1.getMsg(), false);
        } else if (richListModel1.getType() == 3) {
            SobotCacheFile cacheFile = new SobotCacheFile();
            cacheFile.setFileName(richListModel1.getName());
            cacheFile.setUrl(richListModel1.getMsg());
            cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel1.getMsg())));
            cacheFile.setMsgId(message.getMsgId());
            showVideoView(sobot_rich_ll, context, cacheFile, false);
        } else if (richListModel1.getType() == 4 || richListModel1.getType() == 2) {
            SobotCacheFile cacheFile = new SobotCacheFile();
            cacheFile.setFileName(richListModel1.getName());
            cacheFile.setUrl(richListModel1.getMsg());
            cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel1.getMsg())));
            cacheFile.setMsgId(message.getMsgId());
            if (richListModel1.getType() == 2) {
                showFileView(sobot_rich_ll, context, cacheFile, true, false);
            } else {
                showFileView(sobot_rich_ll, context, cacheFile, false, false);
            }
        }
    }

    private void showVideoView(LinearLayout sobot_rich_ll, final Context context,
                               final SobotCacheFile cacheFile, boolean isCanClick) {
        View videoView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_vedio, null);
        ImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
        SobotBitmapUtil.display(context, cacheFile.getSnapshot(), sobot_video_first_image, R.drawable.sobot_rich_item_vedoi_default, R.drawable.sobot_rich_item_vedoi_default);
        videoView.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 71), ScreenUtils.dip2px(context, 40)));
        sobot_rich_ll.addView(videoView);
        if (isCanClick) {
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(cacheFile.getUrl())));
                    Intent intent = SobotVideoActivity.newIntent(context, cacheFile);
                    context.startActivity(intent);
                }
            });
            setCopyAndAppointView(context, videoView);
        }
    }

    private void showFileView(LinearLayout sobot_rich_ll, final Context context,
                              final SobotCacheFile cacheFile, boolean isAudio, boolean isCanClick) {
        if (cacheFile == null) {
            return;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_appoint_file, null);
        TextView sobot_file_name = view.findViewById(R.id.sobot_file_name);
        SobotSectorProgressView sobot_progress = view.findViewById(R.id.sobot_progress);
        sobot_file_name.setText(cacheFile.getFileName());
        if (isRight) {
            view.setBackgroundResource(R.drawable.sobot_chat_msg_bg_transparent);
            sobot_file_name.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
        } else {
            view.setBackgroundResource(R.drawable.sobot_chat_msg_left_bg_transparent);
            sobot_file_name.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_card_text_color));
        }
        SobotBitmapUtil.display(context, ChatUtils.getFileIcon(context, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(cacheFile.getUrl()))), sobot_progress);
        view.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 217), ViewGroup.LayoutParams.WRAP_CONTENT));
        sobot_rich_ll.addView(view);
        if (isCanClick) {
            if (isAudio) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("url", cacheFile.getUrl());
                        context.startActivity(intent);
                    }
                });
            } else {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 打开详情页面
                        Intent intent = new Intent(context, SobotFileDetailActivity.class);
                        intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            }
            setCopyAndAppointView(context, view);
        }
    }

    private void showImageView(LinearLayout sobot_rich_ll, final Context context,
                               final String url, boolean isCanClick) {
        LinearLayout.LayoutParams mlayoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 40),
                ScreenUtils.dip2px(context, 40));
        SobotRCImageView imageView = new SobotRCImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(mlayoutParams);
        imageView.setRadius(ScreenUtils.dip2px(context, 2));
        SobotBitmapUtil.display(context, url, imageView);
        if (isCanClick) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SobotOption.imagePreviewListener != null) {
                        //如果返回true,拦截;false 不拦截
                        boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(context, url);
                        if (isIntercept) {
                            return;
                        }
                    }
                    Intent intent = new Intent(context, SobotPhotoActivity.class);
                    intent.putExtra("imageUrL", url);
                    context.startActivity(intent);
                }
            });
            setCopyAndAppointView(context, imageView);
        }
        sobot_rich_ll.addView(imageView);
    }

    private void setCopyAndAppointView(final Context context, View view) {
        if (sobot_msg_content_ll != null) {
            if (initMode != null && initMode.getMsgAppointFlag() == 1) {
                //引用开启
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
    }

    private void showTextView(LinearLayout sobot_rich_ll, Context context,
                              boolean isRight, String content, int maxLines) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setMaxLines(maxLines);
        textView.setMaxWidth(msgMaxWidth - ScreenUtils.dip2px(context, 8));
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(13);
        if (isRight) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.sobot_right_appoint_msg_text_color));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.sobot_left_appoint_msg_text_color));
        }
        textView.setLayoutParams(wlayoutParams);


        if (!TextUtils.isEmpty(content) && HtmlTools.isHasPatterns(content)) {
            //只有一个，是超链接，并且是卡片形式才显示卡片
            View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_appoint_linkcard, null);
            view.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 217), ViewGroup.LayoutParams.WRAP_CONTENT));
            showLinkUI(context, message, content, view);
            if (view != null) {
                sobot_rich_ll.addView(view);
            }
        } else {
            if (!TextUtils.isEmpty(content)) {
                textView.setText(StringUtils.stripHtml(content));
                sobot_rich_ll.addView(textView);
            }
        }
    }

}
