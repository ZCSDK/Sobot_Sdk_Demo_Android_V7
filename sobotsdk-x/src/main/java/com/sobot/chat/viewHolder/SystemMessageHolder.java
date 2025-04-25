package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 系统提醒消息
 */
public class SystemMessageHolder extends MsgHolderBase {
    TextView center_Remind_Info; // 中间提醒消息

    public SystemMessageHolder(Context context, View convertView) {
        super(context, convertView);
        center_Remind_Info = (TextView) convertView
                .findViewById(R.id.sobot_center_Remind_note);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        if (!TextUtils.isEmpty(message.getTempMsg())) {
            center_Remind_Info.setVisibility(View.VISIBLE);
            HtmlTools.getInstance(context).setRichText(center_Remind_Info, message.getTempMsg(), getLinkTextColor());
        }
        refreshReadStatus();
    }
}
