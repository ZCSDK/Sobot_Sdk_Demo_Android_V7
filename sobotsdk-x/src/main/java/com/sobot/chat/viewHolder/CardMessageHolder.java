package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotMaxSizeLinearLayout;
import com.sobot.chat.widget.image.SobotProgressImageView;

/**
 * 商品卡片
 */
public class CardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private SobotProgressImageView mPic;
    private TextView mTitle;
    private TextView mLabel;
    private TextView mDes;
    private ConsultingContent mConsultingContent;
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();

    public CardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mPic = (SobotProgressImageView) convertView.findViewById(R.id.sobot_goods_pic);
        mTitle = (TextView) convertView.findViewById(R.id.sobot_goods_title);
        mLabel = (TextView) convertView.findViewById(R.id.sobot_goods_label);
        mDes = (TextView) convertView.findViewById(R.id.sobot_goods_des);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mConsultingContent = message.getConsultingContent();

        if (message.getConsultingContent() != null) {
            if (!TextUtils.isEmpty(CommonUtils.encode(message.getConsultingContent().getSobotGoodsImgUrl()))) {
                mPic.setVisibility(View.VISIBLE);
                mDes.setMaxLines(1);
                mDes.setEllipsize(TextUtils.TruncateAt.END);
                mPic.setImageUrl(CommonUtils.encode(message.getConsultingContent().getSobotGoodsImgUrl()));
            } else {
                mPic.setVisibility(View.GONE);
            }
            if (!StringUtils.isEmpty(message.getConsultingContent().getSobotGoodsTitle())) {
                mTitle.setText(message.getConsultingContent().getSobotGoodsTitle());
            } else {
                mTitle.setText("");
            }
            if (!StringUtils.isEmpty(message.getConsultingContent().getSobotGoodsLable())) {
                mLabel.setText(message.getConsultingContent().getSobotGoodsLable());
            } else {
                mLabel.setText("");
            }
            if (!StringUtils.isEmpty(message.getConsultingContent().getSobotGoodsDescribe())) {
                mDes.setText(message.getConsultingContent().getSobotGoodsDescribe());
            } else {
                mDes.setText("");
            }
            if (isRight) {
                try {
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        // 当状态变为成功或失败时，移除延迟任务
                        if (handler != null && loadingRunnable != null) {
                            handler.removeCallbacks(loadingRunnable);
                        }
                        loadingRunnable = new Runnable() {
                            @Override
                            public void run() {
                                msgProgressBar.setVisibility(View.VISIBLE);
                                msgStatus.setVisibility(View.GONE);
                            }
                        };
                        handler.postDelayed(loadingRunnable, ZCSobotConstant.LOADING_TIME);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        sobot_msg_content_ll.setOnClickListener(this);
        setLongClickListener(sobot_msg_content_ll);
        refreshReadStatus();
        if (sobot_msg_content_ll != null && sobot_msg_content_ll instanceof SobotMaxSizeLinearLayout) {
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 + 16));
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMinimumWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 + 16));
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_msg_content_ll && mConsultingContent != null) {
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(mConsultingContent.getSobotGoodsFromUrl());
                return;
            }

            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, mConsultingContent.getSobotGoodsFromUrl());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", mConsultingContent.getSobotGoodsFromUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
