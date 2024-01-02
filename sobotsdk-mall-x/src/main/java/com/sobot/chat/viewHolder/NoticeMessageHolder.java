package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 非置顶公告消息
 */
public class NoticeMessageHolder extends MsgHolderBase {
    private TextView expandable_text;
    private TextView expand_text_btn;

    public NoticeMessageHolder(Context context, View convertView) {
        super(context, convertView);
        expandable_text = convertView.findViewById(R.id.expandable_text);
        expand_text_btn = convertView.findViewById(R.id.expand_text_btn);
        expand_text_btn.setText(R.string.sobot_notice_expand);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
            HtmlTools.getInstance(mContext).setRichText(expandable_text, message.getAnswer().getMsg(), getLinkTextColor());
            try {
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        //通告内容长度大于2行，或者显示的内容和接口返回的不一样 设置渐变色
                        if (message.getNoticeExceedStatus() == 0) {
                            if (expandable_text.getLineCount() > 2) {
                                expand_text_btn.setVisibility(View.VISIBLE);
                                int lineEndIndex = expandable_text.getLayout().getLineEnd(1);
                                String text = message.getAnswer().getMsg().subSequence(0, lineEndIndex - 3) + "...";
                                HtmlTools.getInstance(mContext).setRichText(expandable_text, text, getLinkTextColor());
                                setTextColorGradient(expandable_text, R.color.sobot_common_gray1, R.color.sobot_announcement_bgcolor);
                                message.setNoticeExceedStatus(1);
                                message.setNoticeNoExceedContent(text);
                            } else {
                                expand_text_btn.setVisibility(View.GONE);
                            }
                        }
                    }
                });
                showNoticeExceed();
                expand_text_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (message.getNoticeExceedStatus() == 2) {
                            message.setNoticeExceedStatus(1);
                        } else if (message.getNoticeExceedStatus() == 1) {
                            message.setNoticeExceedStatus(2);
                        }
                        showNoticeExceed();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshReadStatus();
    }

    void showNoticeExceed() {
        try {
            if (message.getNoticeExceedStatus() == 1) {
                HtmlTools.getInstance(mContext).setRichText(expandable_text, message.getNoticeNoExceedContent(), getLinkTextColor());
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        setTextColorGradient(expandable_text, R.color.sobot_announcement_title_color, R.color.sobot_announcement_bgcolor);
                    }
                });
                expand_text_btn.setText(R.string.sobot_notice_expand);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_notice_arrow_down);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    expand_text_btn.setCompoundDrawables(null, null, img, null);
                }
                expand_text_btn.setVisibility(View.VISIBLE);
            } else if (message.getNoticeExceedStatus() == 2) {
                expand_text_btn.setText(R.string.sobot_notice_collapse);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_notice_arrow_up);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    expand_text_btn.setCompoundDrawables(null, null, img, null);
                }
                HtmlTools.getInstance(mContext).setRichText(expandable_text, message.getAnswer().getMsg(), getLinkTextColor());
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        clearTextColorGradient(expandable_text); }
                });
                expand_text_btn.setVisibility(View.VISIBLE);
            } else {
                expand_text_btn.setVisibility(View.GONE);
                clearTextColorGradient(expandable_text);
            }
        } catch (Exception e) {
        }
    }

    public static void setTextColorGradient(TextView textView, @ColorRes int startColor, @ColorRes int endColor) {
        if (textView == null || textView.getContext() == null) {
            return;
        }
        Context context = textView.getContext();
        @ColorInt int sc = context.getResources().getColor(startColor);
        @ColorInt int ec = context.getResources().getColor(endColor);
        int n=textView.getMeasuredHeight();
        LinearGradient gradient = new LinearGradient(0, 0, 0, textView.getMeasuredHeight(), sc, ec, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(gradient);
        textView.invalidate();
    }

    public static void clearTextColorGradient(TextView textView) {
        textView.getPaint().setShader(null);
        textView.invalidate();
    }
}
