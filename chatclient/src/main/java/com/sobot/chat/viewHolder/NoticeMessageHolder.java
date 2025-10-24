package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

/**
 * 非置顶公告消息
 */
public class NoticeMessageHolder extends MsgHolderBase {
    private TextView tv_expandable;
    private ImageView iv_expand;
    private LinearLayout ll_expand;

    public NoticeMessageHolder(Context context, View convertView) {
        super(context, convertView);
        tv_expandable = convertView.findViewById(R.id.tv_expandable);
        iv_expand = convertView.findViewById(R.id.iv_expand);
        ll_expand = convertView.findViewById(R.id.ll_expand);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
            String noticeMsg = message.getAnswer().getMsg().trim();
            HtmlTools.getInstance(mContext).setRichText(tv_expandable, noticeMsg, getLinkTextColor());
            try {
                tv_expandable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = tv_expandable.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                        //通告内容长度大于3行，或者显示的内容和接口返回的不一样 设置渐变色
                        if (message.getNoticeExceedStatus() == 0) {
                            if (tv_expandable.getLineCount() > 4) {
                                ll_expand.setVisibility(View.VISIBLE);
                                int lineEndIndex = tv_expandable.getLayout().getLineEnd(2);
                                String text = "";
                                if ((lineEndIndex - 2) > 0 && (lineEndIndex - 2) <= noticeMsg.length()) {
                                    text = noticeMsg.subSequence(0, lineEndIndex - 2) + "…";
                                } else {
                                    text = noticeMsg;
                                }
                                HtmlTools.getInstance(mContext).setRichText(tv_expandable, text, getLinkTextColor());
//                                setTextColorGradient(expandable_text, R.color.sobot_color_text_first, R.color.sobot_announcement_bgcolor);
                                message.setNoticeExceedStatus(1);
                                message.setNoticeNoExceedContent(text);
                            } else {
                                ll_expand.setVisibility(View.GONE);
                            }
                        }
                    }
                });
                showNoticeExceed(noticeMsg);
                iv_expand.setOnClickListener(new View.OnClickListener() {
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
            } catch (Exception ignored) {
            }
        }
        refreshReadStatus();
    }

    void showNoticeExceed(String noticeMsg) {
        if (mContext == null) {
            return;
        }
        try {
            if (message.getNoticeExceedStatus() == 1) {
                //收起
                ll_expand.setVisibility(View.VISIBLE);
                iv_expand.setImageResource(R.drawable.sobot_notice_arrow_down);
                tv_expandable.setPadding(ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 30));
                HtmlTools.getInstance(mContext).setRichText(tv_expandable, message.getNoticeNoExceedContent(), getLinkTextColor());
                tv_expandable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = tv_expandable.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                    }
                });
                Drawable drawable = AppCompatResources.getDrawable(mContext, R.drawable.sobot_notify_gradient_bg);
                if (drawable != null) {
                    ll_expand.setBackground(drawable);
                }
            } else if (message.getNoticeExceedStatus() == 2) {
                //展开
                ll_expand.setVisibility(View.VISIBLE);
                iv_expand.setImageResource(R.drawable.sobot_notice_arrow_up);
                tv_expandable.setPadding(ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 30));
                HtmlTools.getInstance(mContext).setRichText(tv_expandable, noticeMsg, getLinkTextColor());
                tv_expandable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ViewTreeObserver obs = tv_expandable.getViewTreeObserver();
                        obs.removeOnGlobalLayoutListener(this);
                    }
                });
                ll_expand.setBackground(null);
            } else {
                tv_expandable.setPadding(ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 10));
                ll_expand.setVisibility(View.GONE);
                ll_expand.setBackground(null);
            }
        } catch (Exception ignored) {
        }
    }
}
