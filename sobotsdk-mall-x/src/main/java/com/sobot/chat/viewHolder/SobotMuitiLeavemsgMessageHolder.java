package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 工单节点留言回显
 */
public class SobotMuitiLeavemsgMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private LinearLayout sobot_text_ll;
    private ImageView sobot_msgStatus;
    private ProgressBar sobot_msgProgressBar;
    private ZhiChiMessageBase mMessage;

    public SobotMuitiLeavemsgMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_text_ll = (LinearLayout) convertView.findViewById(R.id.sobot_text_ll);
        sobot_msgProgressBar = (ProgressBar) convertView.findViewById(R.id.sobot_msgProgressBar);
        sobot_msgStatus = (ImageView) convertView.findViewById(R.id.sobot_msgStatus);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        mMessage = message;
        setMsgContent(mContext, message);
        refreshUi();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        sobot_text_ll.setLayoutParams(layoutParams);
        refreshReadStatus();
    }

    private void refreshUi() {
        try {
            if (mMessage == null) {
                return;
            }
            if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                sobot_msgStatus.setVisibility(View.GONE);
                sobot_msgProgressBar.setVisibility(View.GONE);
            } else if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                sobot_msgStatus.setVisibility(View.VISIBLE);
                sobot_msgProgressBar.setVisibility(View.GONE);
                sobot_msgProgressBar.setClickable(true);
                sobot_msgStatus.setOnClickListener(this);
            } else if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                sobot_msgStatus.setVisibility(View.GONE);
                sobot_msgProgressBar.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setMsgContent(final Context context, final ZhiChiMessageBase message) {
        sobot_text_ll.removeAllViews();
        if (!TextUtils.isEmpty(message.getAnswer().getMsg())) {
            String[] arr = message.getAnswer().getMsg().split("\n");
            for (int i = 0; i < arr.length; i++) {
                TextView textView = new TextView(mContext);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i != 0) {
                    wlayoutParams.setMargins(0, ScreenUtils.dip2px(context, 8), 0, 0);
                }else{
                    wlayoutParams.setMargins(0, 0, 0, 0);
                }
                textView.setLayoutParams(wlayoutParams);

                sobot_text_ll.addView(textView);
                if ((i + 1) % 2 == 0) {
                    if (StringUtils.isEmpty(arr[i])) {
                        textView.setText(" - -");
                    } else {
                        textView.setText(Html.fromHtml(arr[i]).toString().trim());
                    }
                    textView.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_first));
                } else {
                    textView.setText(Html.fromHtml(arr[i]).toString().trim() + ":");
                    textView.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_second));
                }
                if ((i + 1) % 2 == 0 && i < (arr.length - 1)) {
                    View lineView = new View(mContext);
                    LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            ScreenUtils.dip2px(context, 0.4f));
                    lineLayoutParams.setMargins(0, ScreenUtils.dip2px(context, 8), 0, 0);
                    lineView.setLayoutParams(lineLayoutParams);
                    lineView.setBackgroundColor(ContextCompat.getColor(context, R.color.sobot_color_line_divider));
                    sobot_text_ll.addView(lineView);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_msgStatus) {
            showReSendDialog(mContext, msgStatus, new ReSendListener() {

                @Override
                public void onReSend() {
                    if (msgCallBack != null && mMessage != null && mMessage.getAnswer() != null) {
                        // msgCallBack.sendMessageToRobot(mMessage, 5, 0, null);
                    }
                }
            });
        }
    }
}
