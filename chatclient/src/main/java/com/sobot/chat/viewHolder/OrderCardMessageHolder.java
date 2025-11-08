package com.sobot.chat.viewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.OrderCardContentModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotMaxSizeLinearLayout;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * 订单卡片
 */
public class OrderCardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private SobotProgressImageView mPic;
    private TextView mTitle;
    private TextView mGoodsCount;
    private View mGoodsOrderSplit;
    private View mSeeAllSplitTV;
    private TextView mSeeAllTV;
    private OrderCardContentModel orderCardContent;
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();
    private GridLayout cusrLayout;


    public OrderCardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mPic = (SobotProgressImageView) convertView.findViewById(R.id.sobot_goods_pic);
        mTitle = (TextView) convertView.findViewById(R.id.sobot_goods_title);
        mGoodsCount = (TextView) convertView.findViewById(R.id.sobot_goods_count);
        mGoodsOrderSplit = (View) convertView.findViewById(R.id.sobot_goods_order_split);
        mSeeAllSplitTV = convertView.findViewById(R.id.sobot_see_all_split);
        mSeeAllTV = convertView.findViewById(R.id.sobot_order_see_all);
        cusrLayout = convertView.findViewById(R.id.curs_grid_layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        orderCardContent = message.getOrderCardContent();
        if (orderCardContent != null) {
            if (orderCardContent.getGoods() != null && orderCardContent.getGoods().size() > 0) {
                mTitle.setVisibility(View.VISIBLE);
                OrderCardContentModel.Goods firstGoods = orderCardContent.getGoods().get(0);
                if (firstGoods != null) {
                    if (TextUtils.isEmpty(firstGoods.getPictureUrl())) {
                        mPic.setVisibility(View.GONE);
                    } else {
                        mPic.setVisibility(View.VISIBLE);
                        mPic.setImageUrl(CommonUtils.encode(firstGoods.getPictureUrl()));
                    }
                    if (TextUtils.isEmpty(firstGoods.getName())) {
                        mTitle.setVisibility(View.GONE);
                    } else {
                        mTitle.setVisibility(View.VISIBLE);
                        mTitle.setText(firstGoods.getName());
                    }
                }
            } else {
                mPic.setVisibility(View.GONE);
                mTitle.setVisibility(View.GONE);
            }

            if ((orderCardContent.getGoods() != null && orderCardContent.getGoods().size() > 0) || !TextUtils.isEmpty(orderCardContent.getGoodsCount()) || orderCardContent.getTotalFee() > 0) {
                mGoodsOrderSplit.setVisibility(View.VISIBLE);
            } else {
                mGoodsOrderSplit.setVisibility(View.GONE);
            }

            StringBuilder s = new StringBuilder();
            StringBuilder s1 = new StringBuilder();
            if (!TextUtils.isEmpty(orderCardContent.getGoodsCount())) {
                s.append(context.getResources().getString(R.string.sobot_card_order_num) + " " + orderCardContent.getGoodsCount() + " " + context.getResources().getString(R.string.sobot_how_goods));
                s1.append(context.getResources().getString(R.string.sobot_card_order_num) + " " + orderCardContent.getGoodsCount() + " " + context.getResources().getString(R.string.sobot_how_goods));
            }
            if (s.length() > 0) {
                s.append(" ");
                s1.append("\n");
            }
            if (!TextUtils.isEmpty(getMoney(orderCardContent.getTotalFee()))) {
                s.append(context.getResources().getString(R.string.sobot_order_total_money) + " " + getMoney(orderCardContent.getTotalFee()) + " " + context.getResources().getString(R.string.sobot_money_format));
                s1.append(context.getResources().getString(R.string.sobot_order_total_money) + " " + getMoney(orderCardContent.getTotalFee()) + " " + context.getResources().getString(R.string.sobot_money_format));
            }
            mGoodsCount.setText(s);
            final StringBuilder finalS = s1;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int lineCount = mGoodsCount.getLineCount();
                    if (lineCount > 1) {
                        mGoodsCount.setText(finalS);
                    }
                }
            });


            cusrLayout.setColumnCount(2);
            cusrLayout.removeAllViews();

            if (orderCardContent.getOrderStatus() == 0) {
                if (!TextUtils.isEmpty(orderCardContent.getStatusCustom())) {
                    cusrLayout.addView(getLeftText(mContext.getResources().getString(R.string.sobot_order_status_lable)));
                    cusrLayout.addView(getRightText(orderCardContent.getStatusCustom()));
                }
            } else {
                //待付款: 1   待发货: 2   运输中: 3   派送中: 4   已完成: 5   待评价: 6   已取消: 7
                String statusStr = "";
                switch (orderCardContent.getOrderStatus()) {
                    case 1:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_1);
                        break;
                    case 2:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_2);
                        break;
                    case 3:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_3);
                        break;
                    case 4:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_4);
                        break;
                    case 5:
                        statusStr = context.getResources().getString(R.string.sobot_completed);
                        break;
                    case 6:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_6);
                        break;
                    case 7:
                        statusStr = context.getResources().getString(R.string.sobot_order_status_7);
                        break;
                }
                cusrLayout.addView(getLeftText(mContext.getResources().getString(R.string.sobot_order_status_lable)));
                TextView status=getRightText(statusStr);
                status.setTextColor(mContext.getResources().getColor(R.color.sobot_order_status_text_color));
                cusrLayout.addView(status);
            }

            if (!TextUtils.isEmpty(orderCardContent.getOrderCode())) {
                cusrLayout.addView(getLeftText(mContext.getResources().getString(R.string.sobot_order_code_lable)));
                cusrLayout.addView(getRightText(orderCardContent.getOrderCode()));
            }

            if (!TextUtils.isEmpty(orderCardContent.getCreateTime())) {
                Locale locale = (Locale) SharedPreferencesUtil.getObject(context, ZhiChiConstant.SOBOT_LANGUAGE);
                String formatString = DateUtil.getDateTimePatternByLanguage(locale, true);
                cusrLayout.addView(getLeftText(mContext.getResources().getString(R.string.sobot_order_time_lable)));
                cusrLayout.addView(getRightText( DateUtil.longStrToDateStr(orderCardContent.getCreateTime(), formatString, locale)));
            }

            if (orderCardContent.getExtendFields() != null) {
                for (int i = 0; i < orderCardContent.getExtendFields().size(); i++) {
                    cusrLayout.addView(getLeftText(orderCardContent.getExtendFields().get(i).getFieldName()));
                    cusrLayout.addView(getRightText(orderCardContent.getExtendFields().get(i).getFieldValue()));
                }
            }
            if (!TextUtils.isEmpty(orderCardContent.getOrderUrl())) {
                mSeeAllSplitTV.setVisibility(View.VISIBLE);
                mSeeAllTV.setVisibility(View.VISIBLE);
            } else {
                mSeeAllSplitTV.setVisibility(View.GONE);
                mSeeAllTV.setVisibility(View.GONE);
            }
            mSeeAllTV.setTextColor(ThemeUtils.getThemeColor(mContext));

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
        refreshReadStatus();
        if (sobot_msg_content_ll != null && sobot_msg_content_ll instanceof SobotMaxSizeLinearLayout) {
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 + 16));
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMinimumWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 + 16));
        }
    }

    /**
     * 自定义字段左侧的view
     * @param textStr 显示的文字
     */
    private TextView getLeftText(String textStr){
        TextView title = new TextView(mContext);
        title.setText(textStr);
        title.setMaxLines(3);
        title.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_first));
        title.setTextSize(12);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setPadding(0, ScreenUtils.dip2px(mContext, 8), ScreenUtils.dip2px(mContext, 8), 0);
        title.setMaxWidth(ScreenUtils.dip2px(mContext, 150));
        return title;
    }

    /**
     * 自定义字段右侧的view
     * @param textStr 文本
     */
    private TextView getRightText(String textStr){
        TextView value = new TextView(mContext);
        value.setMaxLines(3);
        value.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_first));
        value.setTextSize(12);
        value.setPadding(0, 0, 0, 0);
        value.setText(textStr);
        return value;
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_msg_content_ll && orderCardContent != null) {
            if (TextUtils.isEmpty(orderCardContent.getOrderUrl())) {
                LogUtils.i("订单卡片跳转链接为空，不跳转，不拦截");
                return;
            }
            if (SobotOption.orderCardListener != null) {
                SobotOption.orderCardListener.onClickOrderCradMsg(orderCardContent);
                return;
            }
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(orderCardContent.getOrderUrl());
                return;
            }
            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, orderCardContent.getOrderUrl());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", orderCardContent.getOrderUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /**
     * 获取钱的数量
     *
     * @param money
     * @return
     */
    private String getMoney(int money) {
        if (mContext == null) {
            return "";
        }
        try {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(BigDecimal.valueOf(Double.valueOf(money * 1.0)).divide(new BigDecimal(100)).doubleValue());
        } catch (Throwable e) {
            return "" + money / 100.0f;
        }
    }

    /**
     * 分转元，转换为bigDecimal在toString
     *
     * @return
     */
    public static double changeF2Y(int price) {
        return BigDecimal.valueOf(Double.valueOf(price * 1.0)).divide(new BigDecimal(100)).doubleValue();
    }

}
