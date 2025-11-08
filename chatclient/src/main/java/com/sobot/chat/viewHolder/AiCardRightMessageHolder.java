package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 自定义卡片
 */
public class AiCardRightMessageHolder extends MsgHolderBase {
    private TextView tv_head;
    private ImageView sobot_goods_pic;
    private TextView sobot_goods_title;
    private TextView sobot_goods_des;
    private TextView sobot_count;
    private TextView sobot_price;
    private GridLayout cusrLayout;
    private View line;
    private int themeColor;
    private SobotChatCustomCard customCard;


    public AiCardRightMessageHolder(Context context, View itemView) {
        super(context, itemView);
        themeColor = ThemeUtils.getThemeColor(context);
        sobot_goods_pic = itemView.findViewById(R.id.sobot_goods_pic);
        tv_head = itemView.findViewById(R.id.tv_head);
        sobot_goods_title = itemView.findViewById(R.id.sobot_goods_title);
        sobot_goods_des = itemView.findViewById(R.id.sobot_goods_des);
        sobot_count = itemView.findViewById(R.id.sobot_count);
        sobot_price = itemView.findViewById(R.id.sobot_price);
        cusrLayout = itemView.findViewById(R.id.curs_grid_layout);
        line = itemView.findViewById(R.id.v_line_bottom);
    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        resetMaxWidth();
        customCard = message.getCustomCard();
        if (customCard != null && customCard.getCustomCards() != null && customCard.getCustomCards().size() > 0) {

            final SobotChatCustomGoods customGoods = customCard.customCards.get(0);
            if (customGoods != null) {
                if (customGoods.getCustomCardHeader() != null && customGoods.getCustomCardHeader().size() > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String key :
                            customGoods.getCustomCardHeader().keySet()) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(key);
                        stringBuilder.append(customGoods.getCustomCardHeader().get(key));
                    }
                    tv_head.setText(stringBuilder.toString());
                    tv_head.setVisibility(View.VISIBLE);
                } else {
                    tv_head.setVisibility(View.GONE);
                }

                if (StringUtils.isNoEmpty(customGoods.getCustomCardName())) {
                    sobot_goods_title.setText(customGoods.getCustomCardName());
                    sobot_goods_title.setVisibility(View.VISIBLE);
                } else {
                    sobot_goods_title.setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(customGoods.getCustomCardThumbnail())) {
                    SobotBitmapUtil.display(context, CommonUtils.encode(customGoods.getCustomCardThumbnail())
                            , sobot_goods_pic);
                    sobot_goods_pic.setVisibility(View.VISIBLE);
                } else {
                    sobot_goods_pic.setVisibility(View.GONE);
                }
                if (StringUtils.isNoEmpty(customGoods.getCustomCardDesc())) {
                    sobot_goods_des.setText(customGoods.getCustomCardDesc());
                    sobot_goods_des.setVisibility(View.VISIBLE);
                } else {
                    sobot_goods_des.setVisibility(View.GONE);
                }
                if (StringUtils.isNoEmpty(customGoods.getCustomCardNum())) {
                    sobot_count.setText("X" + customGoods.getCustomCardNum());
                    sobot_count.setVisibility(View.VISIBLE);
                } else {
                    sobot_count.setVisibility(View.GONE);
                }
                //
                if (!StringUtils.isEmpty(customGoods.getCustomCardAmount())) {
                    String price = "";
                    if (!StringUtils.isEmpty(customGoods.getCustomCardAmountSymbol())) {
                        price += customGoods.getCustomCardAmountSymbol();
                    }
                    if (!StringUtils.isEmpty(customGoods.getCustomCardAmount())) {
                        price += StringUtils.getMoney(customGoods.getCustomCardAmount());
                    }
                    sobot_price.setVisibility(View.VISIBLE);
                    sobot_price.setText(price);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            int lineCount = sobot_price.getLineCount();
                            if (lineCount > 1) {
                                if (sobot_count.getVisibility() == View.VISIBLE) {
                                    String s1 = sobot_count.getText().toString();
                                    String s2 = sobot_price.getText().toString();
                                    sobot_count.setText(s1 + "\n" + s2);
                                }
                                sobot_price.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    sobot_price.setVisibility(View.GONE);
                }
                //自定义

                if (customGoods.getCustomField() != null && customGoods.getCustomField().size() > 0) {
                    cusrLayout.removeAllViews();
                    cusrLayout.setColumnCount(2);

                    for (String key :
                            customGoods.getCustomField().keySet()) {
                        TextView title = new TextView(context);
                        title.setText(key);
                        title.setMaxLines(3);
                        title.setTextColor(context.getResources().getColor(R.color.sobot_color_text_first));
                        title.setTextSize(12);
                        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        title.setPadding(0, ScreenUtils.dip2px(context, 8), ScreenUtils.dip2px(context, 8), 0);
                        title.setMaxWidth(ScreenUtils.dip2px(context, 150));
                        cusrLayout.addView(title);

                        TextView value = new TextView(context);
                        value.setMaxLines(3);
                        value.setTextColor(context.getResources().getColor(R.color.sobot_color_text_first));
                        value.setTextSize(12);
                        value.setPadding(0, 0, 0, 0);
                        value.setText(customGoods.getCustomField().get(key));
                        cusrLayout.addView(value);
                    }

                    cusrLayout.setVisibility(View.VISIBLE);
                    line.setVisibility(View.VISIBLE);
                } else {
                    cusrLayout.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                }
            }
            refreshReadStatus();
        }
    }
}
