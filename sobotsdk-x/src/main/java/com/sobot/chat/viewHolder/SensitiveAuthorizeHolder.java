package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.network.http.callback.StringResultCallBack;

/**
 * 敏感词授权消息
 */
public class SensitiveAuthorizeHolder extends MsgHolderBase {

    LinearLayout sobot_ll_yinsi;
    TextView sobot_msg_temp; // 聊天的消息内容 临时的，隐私确认发送卡片时用到
    TextView sobot_sentisiveExplain;//隐私提示语
    LinearLayout sobot_msg_temp_see_all;//展开消息，可以看全部
    Button sobot_sentisive_ok_send_h; //继续发送
    Button sobot_sentisive_cancle_send_h;//拒绝发送
    TextView sobot_sentisive_cancle_tip_h;//点击拒绝发送后的提示语
    LinearLayout ll_btn_h, ll_btn_v;
    Button sobot_sentisive_ok_send_v; //继续发送
    Button sobot_sentisive_cancle_send_v;//拒绝发送
    TextView sobot_sentisive_cancle_tip_v;//点击拒绝发送后的提示语

    public SensitiveAuthorizeHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_ll_yinsi = convertView.findViewById(R.id.sobot_ll_yinsi);
        sobot_msg_temp = (TextView) convertView.findViewById(R.id.sobot_msg_temp);
        sobot_sentisiveExplain = (TextView) convertView.findViewById(R.id.sobot_sentisiveExplain);
        sobot_msg_temp_see_all = convertView.findViewById(R.id.sobot_msg_temp_see_all);
        ll_btn_h = convertView.findViewById(R.id.ll_btn_h);
        sobot_sentisive_ok_send_h = (Button) convertView.findViewById(R.id.sobot_sentisive_ok_send_h);
        sobot_sentisive_cancle_send_h = (Button) convertView.findViewById(R.id.sobot_sentisive_cancle_send_h);
        sobot_sentisive_cancle_tip_h = (TextView) convertView.findViewById(R.id.sobot_sentisive_cancle_tip_h);
        ll_btn_v = convertView.findViewById(R.id.ll_btn_v);
        sobot_sentisive_ok_send_v = (Button) convertView.findViewById(R.id.sobot_sentisive_ok_send_v);
        sobot_sentisive_cancle_send_v = (Button) convertView.findViewById(R.id.sobot_sentisive_cancle_send_v);
        sobot_sentisive_cancle_tip_v = (TextView) convertView.findViewById(R.id.sobot_sentisive_cancle_tip_v);
        if (context != null) {
            sobot_sentisive_ok_send_v.setTextColor(ThemeUtils.getThemeColor(context));
            sobot_sentisive_ok_send_h.setTextColor(ThemeUtils.getThemeColor(context));
        }
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null) {
            final String content = message.getAnswer().getMsg();
            if (message.getSentisive() == 1) {
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
                        if (sobot_msg_temp.getLineCount() >= 5 && !message.isShowSentisiveSeeAll()) {
                            sobot_msg_temp.setMaxLines(5);
                            sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 8), ScreenUtils.dip2px(context, 10), 0);
                            sobot_msg_temp_see_all.setVisibility(View.VISIBLE);
                        } else {
                            sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 8), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 8));
                            sobot_msg_temp_see_all.setVisibility(View.GONE);
                            sobot_msg_temp.setMaxLines(100);
                        }
                    }
                });
                sobot_msg_temp_see_all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sobot_msg_temp.setPadding(ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 8), ScreenUtils.dip2px(context, 10), ScreenUtils.dip2px(context, 8));
                        sobot_msg_temp.setMaxLines(100);
                        sobot_msg_temp_see_all.setVisibility(View.GONE);
                        message.setShowSentisiveSeeAll(true);
                    }
                });
                sobot_sentisive_ok_send_h.post(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = sobot_sentisive_ok_send_h.getLineCount();
                        updateUi(lineCount);
                    }
                });
                sobot_sentisive_ok_send_h.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueSend(context, message, content);
                    }
                });
                sobot_sentisive_ok_send_v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueSend(context, message, content);
                    }
                });
                if (message.isClickCancleSend()) {
                    sobot_sentisive_cancle_tip_h.setVisibility(View.VISIBLE);
                    sobot_sentisive_cancle_send_h.setVisibility(View.GONE);
                    sobot_sentisive_cancle_tip_v.setVisibility(View.VISIBLE);
                    sobot_sentisive_cancle_send_v.setVisibility(View.GONE);
                } else {
                    sobot_sentisive_cancle_tip_h.setVisibility(View.GONE);
                    sobot_sentisive_cancle_send_h.setVisibility(View.VISIBLE);
                    sobot_sentisive_cancle_tip_v.setVisibility(View.GONE);
                    sobot_sentisive_cancle_send_v.setVisibility(View.VISIBLE);
                }
                sobot_sentisive_cancle_send_h.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refuseSend(context, content, message);
                    }
                });
                sobot_sentisive_cancle_send_v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refuseSend(context, content, message);
                    }
                });
            }
        }
    }

    private void updateUi(int lineCount) {
        if (lineCount > 1) {
            ll_btn_h.setVisibility(View.GONE);
            ll_btn_v.setVisibility(View.VISIBLE);
        } else {
            ll_btn_h.setVisibility(View.VISIBLE);
            ll_btn_v.setVisibility(View.GONE);
        }
    }

    //拒绝发送
    private void refuseSend(final Context context, final String content, final ZhiChiMessageBase message) {
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_initModel);
        SobotMsgManager.getInstance(context).getZhiChiApi().authSensitive(content, initMode.getPartnerid(), "0", new StringResultCallBack<CommonModel>() {
            @Override
            public void onSuccess(CommonModel baseCode) {
                if (baseCode.getData() != null && !TextUtils.isEmpty(baseCode.getData().getStatus())) {
                    //  返回值：status 1 成功 status 2 会话已结束 status 3 已授权 status 0 失败
                    if (!"0".equals(baseCode.getData().getStatus())) {
                        message.setClickCancleSend(true);
                        sobot_sentisive_cancle_tip_h.setVisibility(View.VISIBLE);
                        sobot_sentisive_cancle_send_h.setVisibility(View.GONE);
                        sobot_sentisive_cancle_tip_v.setVisibility(View.VISIBLE);
                        sobot_sentisive_cancle_send_v.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {

            }
        });
    }

    //继续发送
    private void continueSend(final Context context, final ZhiChiMessageBase message, final String content) {
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_initModel);
        message.setMsgId(System.currentTimeMillis() + "");
        message.setContent(content);
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
}
