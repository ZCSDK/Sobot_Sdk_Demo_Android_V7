package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ArticleModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotProgressImageView;

/**
 * 文章卡片
 */
public class ArticleMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private LinearLayout answersList;
    private TextView stripe;
    private SobotProgressImageView iv_snapshot;
    private TextView tv_title;
    private TextView tv_desc;
    private ArticleModel articleModel;
    private RelativeLayout rl_name_des;

    public ArticleMessageHolder(Context context, View convertView) {
        super(context, convertView);
        iv_snapshot = (SobotProgressImageView) convertView.findViewById(R.id.iv_snapshot);
        tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
        sobot_tv_transferBtn = (TextView) convertView.findViewById(R.id.sobot_tv_transferBtn);
        sobot_tv_transferBtn.setText(R.string.sobot_transfer_to_customer_service);
        answersList = (LinearLayout) convertView
                .findViewById(R.id.sobot_answersList);
        stripe = (TextView) convertView.findViewById(R.id.sobot_stripe);
        rl_name_des= convertView.findViewById(R.id.rl_name_des);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        resetMaxWidth();
        articleModel = message.getArticleModel();
        if (articleModel != null) {
            if (!TextUtils.isEmpty(articleModel.getSnapshot())) {
                iv_snapshot.setVisibility(View.VISIBLE);
                 iv_snapshot.setImageUrlWithScaleType(articleModel.getSnapshot(), ImageView.ScaleType.CENTER_CROP);
            } else {
                iv_snapshot.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(articleModel.getTitle())) {
                tv_title.setText(articleModel.getTitle());
                tv_title.setVisibility(View.VISIBLE);
            } else {
                tv_title.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(articleModel.getDesc())) {
                tv_desc.setText(articleModel.getDesc());
                tv_desc.setVisibility(View.VISIBLE);
            } else {
                tv_desc.setVisibility(View.GONE);
            }
        }
        sobot_msg_content_ll.setOnClickListener(this);
        refreshItem();
        checkShowTransferBtn();
        if (message.getSugguestions() != null && message.getSugguestions().length > 0) {
            resetAnswersList();
            if (stripe != null) {
                // 回复语的答复
                String stripeContent = message.getStripe() != null ? message.getStripe().trim() : "";
                if (!TextUtils.isEmpty(stripeContent)) {
                    //去掉p标签
                    stripeContent = stripeContent.replace("<p>", "").replace("</p>", "");
                    // 设置提醒的内容
                    stripe.setVisibility(View.VISIBLE);
                    HtmlTools.getInstance(context).setRichText(stripe, stripeContent, getLinkTextColor());
                } else {
                    stripe.setText(null);
                    stripe.setVisibility(View.GONE);
                }
            }
        } else {
            answersList.setVisibility(View.GONE);
        }
        setLongClickListener(sobot_msg_content_ll);
        refreshReadStatus();
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_msg_content_ll && articleModel != null && !TextUtils.isEmpty(articleModel.getRichMoreUrl())) {
            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, articleModel.getRichMoreUrl());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", articleModel.getRichMoreUrl());
            mContext.startActivity(intent);
        }
    }


}
