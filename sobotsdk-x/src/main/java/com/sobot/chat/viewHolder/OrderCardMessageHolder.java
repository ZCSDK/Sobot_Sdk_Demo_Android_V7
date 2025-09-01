package com.sobot.chat.viewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.OrderCardContentModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotMaxSizeLinearLayout;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 订单卡片
 */
public class OrderCardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private SobotProgressImageView mPic;
    private TextView mTitle;
    private TextView mGoodsCount;
    private TextView mOrderStatus;
    private TextView mOrderNumber;
    private TextView mOrderCreatetime;
    private LinearLayout mOrderExtentFieldsLL;
    private View mGoodsOrderSplit;
    private View mSeeAllSplitTV;
    private TextView mSeeAllTV;
    private OrderCardContentModel orderCardContent;

    public OrderCardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mPic = (SobotProgressImageView) convertView.findViewById(R.id.sobot_goods_pic);
        mTitle = (TextView) convertView.findViewById(R.id.sobot_goods_title);
        mGoodsCount = (TextView) convertView.findViewById(R.id.sobot_goods_count);
        mGoodsOrderSplit = (View) convertView.findViewById(R.id.sobot_goods_order_split);
        mOrderStatus = (TextView) convertView.findViewById(R.id.sobot_order_status);
        mOrderNumber = (TextView) convertView.findViewById(R.id.sobot_order_number);
        mOrderExtentFieldsLL = convertView.findViewById(R.id.sobot_order_ll_extent_fields);
        mOrderCreatetime = (TextView) convertView.findViewById(R.id.sobot_order_createtime);
        mSeeAllSplitTV = convertView.findViewById(R.id.sobot_see_all_split);
        mSeeAllTV = convertView.findViewById(R.id.sobot_order_see_all);
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

            if (orderCardContent.getOrderStatus() == 0) {
                if (!TextUtils.isEmpty(orderCardContent.getStatusCustom())) {
                    mOrderStatus.setVisibility(View.VISIBLE);
                    mOrderStatus.setText(orderCardContent.getStatusCustom());
                } else {
                    mOrderStatus.setVisibility(View.GONE);
                }
            } else {
                mOrderStatus.setVisibility(View.VISIBLE);
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
                mOrderStatus.setText(statusStr);
            }

            StringBuilder s = new StringBuilder();
            StringBuilder s1 = new StringBuilder();
            if (!TextUtils.isEmpty(orderCardContent.getGoodsCount())) {
                s .append(context.getResources().getString(R.string.sobot_card_order_num) + " " +orderCardContent.getGoodsCount() +" " + context.getResources().getString(R.string.sobot_how_goods));
                s1 .append(context.getResources().getString(R.string.sobot_card_order_num) + " " +orderCardContent.getGoodsCount() +" " + context.getResources().getString(R.string.sobot_how_goods));
            }
            if(s.length()>0){
                s.append(" ");
                s1.append("\n");
            }
            if (!TextUtils.isEmpty(getMoney(orderCardContent.getTotalFee()))) {
                s.append( context.getResources().getString(R.string.sobot_order_total_money) + " " + getMoney(orderCardContent.getTotalFee()) + " " + context.getResources().getString(R.string.sobot_money_format));
                s1.append( context.getResources().getString(R.string.sobot_order_total_money) + " "  + getMoney(orderCardContent.getTotalFee()) + " " + context.getResources().getString(R.string.sobot_money_format));
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

            if (!TextUtils.isEmpty(orderCardContent.getOrderCode())) {
                mOrderNumber.setText(context.getResources().getString(R.string.sobot_order_code_lable) + "：" + orderCardContent.getOrderCode());
                mOrderNumber.setVisibility(View.VISIBLE);
            } else {
                mOrderNumber.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(orderCardContent.getCreateTime())) {
                mOrderCreatetime.setText(context.getResources().getString(R.string.sobot_order_time_lable) + "：" + DateUtil.longToDateStr(Long.parseLong(orderCardContent.getCreateTime()), "yyyy-MM-dd HH:mm:ss"));
                mOrderCreatetime.setVisibility(View.VISIBLE);
            } else {
                mOrderCreatetime.setVisibility(View.GONE);
            }

            if (orderCardContent.getExtendFields() != null) {
                mOrderExtentFieldsLL.removeAllViews();
                for (int i = 0; i < orderCardContent.getExtendFields().size(); i++) {
                    TextView textView = new TextView(mContext);
                    textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelOffset(R.dimen.sobot_text_font_12));
                    LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    wlayoutParams.setMargins(0, ScreenUtils.dip2px(context, 2), 0, 0);
                    textView.setLayoutParams(wlayoutParams);
                    textView.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_order_label_text_color));
                    textView.setText(orderCardContent.getExtendFields().get(i).getFieldName() + "：" + orderCardContent.getExtendFields().get(i).getFieldValue());
                    mOrderExtentFieldsLL.addView(textView);
                }
                mOrderExtentFieldsLL.setVisibility(View.VISIBLE);

            } else {
                mOrderExtentFieldsLL.setVisibility(View.GONE);
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
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
//                        msgStatus.setOnClickListener(new TextMessageHolder.ReSendTextLisenter(context, message
//                                .getId(), content, msgStatus, msgCallBack));
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        msgProgressBar.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        sobot_msg_content_ll.setOnClickListener(this);
        refreshReadStatus();
        if (sobot_msg_content_ll != null && sobot_msg_content_ll instanceof SobotMaxSizeLinearLayout) {
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 +16));
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMinimumWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16+16));
        }
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
