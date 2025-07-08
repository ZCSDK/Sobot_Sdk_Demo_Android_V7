package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

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
    private LinearLayout ll_expand_btn;

    public NoticeMessageHolder(Context context, View convertView) {
        super(context, convertView);
        expandable_text = convertView.findViewById(R.id.expandable_text);
        expand_text_btn = convertView.findViewById(R.id.expand_text_btn);
        ll_expand_btn = convertView.findViewById(R.id.ll_expand_btn);
        expand_text_btn.setText(R.string.sobot_notice_expand);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
            String noticeMsg = message.getAnswer().getMsg().trim();
            HtmlTools.getInstance(mContext).setRichText(expandable_text, noticeMsg, getLinkTextColor());
            try {
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        //通告内容长度大于4行，或者显示的内容和接口返回的不一样 设置渐变色
                        if (message.getNoticeExceedStatus() == 0) {
                            if (expandable_text.getLineCount() > 4) {
                                ll_expand_btn.setVisibility(View.VISIBLE);
                                int lineEndIndex = expandable_text.getLayout().getLineEnd(3);
                                String text = "";
                                if ((lineEndIndex - 3) > 0 && (lineEndIndex - 3) <= noticeMsg.length()) {
                                    text = noticeMsg.subSequence(0, lineEndIndex - 3) + "…";
                                } else {
                                    text = noticeMsg;
                                }
                                HtmlTools.getInstance(mContext).setRichText(expandable_text, text, getLinkTextColor());
//                                setTextColorGradient(expandable_text, R.color.sobot_color_text_first, R.color.sobot_announcement_bgcolor);
                                message.setNoticeExceedStatus(1);
                                message.setNoticeNoExceedContent(text);
                            } else {
                                ll_expand_btn.setVisibility(View.GONE);
                            }
                        }
                    }
                });
                showNoticeExceed(noticeMsg);
                ll_expand_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (message.getNoticeExceedStatus() == 2) {
                            message.setNoticeExceedStatus(1);
                        } else if (message.getNoticeExceedStatus() == 1) {
                            message.setNoticeExceedStatus(2);
                        }
                        showNoticeExceed(noticeMsg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshReadStatus();
    }

    void showNoticeExceed(String noticeMsg) {
        try {
            if (message.getNoticeExceedStatus() == 1) {
                HtmlTools.getInstance(mContext).setRichText(expandable_text, message.getNoticeNoExceedContent(), getLinkTextColor());
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
//                        setTextColorGradient(expandable_text, R.color.sobot_announcement_title_color, R.color.sobot_announcement_bgcolor);
                    }
                });
                expand_text_btn.setText(R.string.sobot_notice_expand);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_notice_arrow_down);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    expand_text_btn.setCompoundDrawables(null, null, img, null);
                }
                ll_expand_btn.setVisibility(View.VISIBLE);
            } else if (message.getNoticeExceedStatus() == 2) {
                expand_text_btn.setText(R.string.sobot_notice_collapse);
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_notice_arrow_up);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    expand_text_btn.setCompoundDrawables(null, null, img, null);
                }
                HtmlTools.getInstance(mContext).setRichText(expandable_text, noticeMsg, getLinkTextColor());
                expandable_text.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = expandable_text.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        clearTextColorGradient(expandable_text);
                    }
                });
                ll_expand_btn.setVisibility(View.VISIBLE);
            } else {
                ll_expand_btn.setVisibility(View.GONE);
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
        LinearGradient gradient = new LinearGradient(0, 0, 0, textView.getMeasuredHeight(), sc, ec, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(gradient);
        textView.invalidate();
    }

    public static void clearTextColorGradient(TextView textView) {
        textView.getPaint().setShader(null);
        textView.invalidate();
    }
}
