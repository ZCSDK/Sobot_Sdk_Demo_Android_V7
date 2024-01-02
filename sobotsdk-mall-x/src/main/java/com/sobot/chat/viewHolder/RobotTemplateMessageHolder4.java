package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder4 extends MsgHolderBase {

    private TextView sobot_template4_temp_title;
    private ImageView sobot_template4_thumbnail;
    private TextView sobot_template4_title;
    private TextView sobot_template4_summary;
    private TextView sobot_template4_anchor;

    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder4(Context context, View convertView) {
        super(context, convertView);
        sobot_template4_temp_title = (TextView) convertView.findViewById(R.id.sobot_template4_temp_title);
        sobot_template4_thumbnail = (ImageView) convertView.findViewById(R.id.sobot_template4_thumbnail);
        sobot_template4_title = (TextView) convertView.findViewById(R.id.sobot_template4_title);
        sobot_template4_summary = (TextView) convertView.findViewById(R.id.sobot_template4_summary);
        sobot_template4_anchor = (TextView) convertView.findViewById(R.id.sobot_template4_anchor);
        sobot_template4_anchor.setText(R.string.sobot_see_detail);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message=message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {

            checkShowTransferBtn();
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(sobot_template4_temp_title, msgStr.replaceAll("\n", "<br/>"), getLinkTextColor());
                sobot_template4_temp_title.setVisibility(View.VISIBLE);
            } else {
                sobot_template4_temp_title.setVisibility(View.INVISIBLE);
            }
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                if (interfaceRetList != null && interfaceRetList.size() > 0) {
                    final Map<String, String> interfaceRet = interfaceRetList.get(0);
                    if (interfaceRet != null && interfaceRet.size() > 0) {
                        setSuccessView();
                        HtmlTools.getInstance(context).setRichText(sobot_template4_title, interfaceRet.get("title"), getLinkTextColor());
                        if (!TextUtils.isEmpty(interfaceRet.get("thumbnail"))) {
                            SobotBitmapUtil.display(context, interfaceRet.get("thumbnail"), sobot_template4_thumbnail, R.drawable.sobot_bg_default_long_pic, R.drawable.sobot_bg_default_long_pic);
                            sobot_template4_thumbnail.setVisibility(View.VISIBLE);
                        } else {
                            sobot_template4_thumbnail.setVisibility(View.GONE);
                        }
                        sobot_template4_summary.setText(interfaceRet.get("summary"));

                        if (multiDiaRespInfo.getEndFlag() && interfaceRet.get("anchor") != null) {
                            sobot_template4_anchor.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (SobotOption.newHyperlinkListener != null) {
                                        //如果返回true,拦截;false 不拦截
                                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext,interfaceRet.get("anchor"));
                                        if (isIntercept) {
                                            return;
                                        }
                                    }
                                    Intent intent = new Intent(context, WebViewActivity.class);
                                    intent.putExtra("url", interfaceRet.get("anchor"));
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                } else {
                    sobot_template4_title.setText(multiDiaRespInfo.getAnswerStrip());
                    setFailureView();
                }
            } else {
                sobot_template4_title.setText(multiDiaRespInfo.getRetErrorMsg());
                setFailureView();
            }
        }
        refreshItem();//左侧消息刷新顶和踩布局
        checkShowTransferBtn();//检查转人工逻辑
        //关联问题显示逻辑
        if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
            resetAnswersList();
        } else {
            hideAnswers();
        }
        resetMaxWidth();
        refreshReadStatus();
    }

    private void setSuccessView() {
        sobot_template4_title.setVisibility(View.VISIBLE);
        sobot_template4_thumbnail.setVisibility(View.VISIBLE);
        sobot_template4_summary.setVisibility(View.VISIBLE);
        sobot_template4_anchor.setVisibility(View.VISIBLE);
    }

    private void setFailureView() {
        sobot_template4_title.setVisibility(View.VISIBLE);
        sobot_template4_thumbnail.setVisibility(View.GONE);
        sobot_template4_temp_title.setVisibility(View.GONE);
        sobot_template4_summary.setVisibility(View.GONE);
        sobot_template4_anchor.setVisibility(View.GONE);
    }

}