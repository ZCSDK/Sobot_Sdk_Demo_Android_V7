package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 消息撤回
 */
public class RetractedMessageHolder extends MsgHolderBase {
    TextView sobot_tv_tip; // 中间提醒消息
    String tipStr;

    public RetractedMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_tv_tip = (TextView) convertView
                .findViewById(R.id.sobot_tv_tip);
        tipStr = context.getResources().getString(R.string.sobot_retracted_msg_tip_end);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        if (message != null) {
            sobot_tv_tip.setText(TextUtils.isEmpty(message.getSenderName()) ? "" : (message.getSenderName() + " ") + tipStr);
        }
        refreshReadStatus();
    }

}
