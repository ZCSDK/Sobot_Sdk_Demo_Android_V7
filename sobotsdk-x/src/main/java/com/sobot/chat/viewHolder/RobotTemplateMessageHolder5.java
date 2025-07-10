package com.sobot.chat.viewHolder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder5 extends MsgHolderBase {

    private TextView sobot_template5_title;
    private TextView sobot_template5_msg;

    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder5(Context context, View convertView) {
        super(context, convertView);
        sobot_template5_msg = (TextView) convertView.findViewById(R.id.sobot_template5_msg);
        sobot_template5_title = (TextView) convertView.findViewById(R.id.sobot_template5_title);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        sobot_template5_title.setMaxWidth(msgMaxWidth);
        sobot_template5_msg.setMaxWidth(msgMaxWidth);
        this.message=message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            checkShowTransferBtn();
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            HtmlTools.getInstance(context).setRichText(sobot_template5_msg, ChatUtils.getMultiMsgTitle(multiDiaRespInfo).replaceAll("\n", "<br/>"), getLinkTextColor());
            final List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
            if ("000000".equals(multiDiaRespInfo.getRetCode()) && interfaceRetList != null && interfaceRetList.size() > 0) {
                final Map<String, String> interfaceRet = interfaceRetList.get(0);
                if (interfaceRet != null && interfaceRet.size() > 0) {
                    setSuccessView();
                    HtmlTools.getInstance(context).setRichText(sobot_template5_title, interfaceRet.get("title"), getLinkTextColor());
                }
            } else {
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
        refreshReadStatus();
    }

    private void setSuccessView() {
        sobot_template5_msg.setVisibility(View.VISIBLE);
        sobot_template5_title.setVisibility(View.VISIBLE);
    }

    private void setFailureView() {
        sobot_template5_msg.setVisibility(View.VISIBLE);
        sobot_template5_title.setVisibility(View.GONE);
    }

}