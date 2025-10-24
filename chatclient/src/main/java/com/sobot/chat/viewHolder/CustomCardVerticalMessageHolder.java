package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.api.model.customcard.SobotChatCustomMenu;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义卡片--单个商品卡片
 */
public class CustomCardVerticalMessageHolder extends MsgHolderBase implements View.OnClickListener{
    private SobotChatCustomCard customCard;

    //竖向订单商品信息
    private SobotProgressImageView sobot_goods_pic;
    private TextView sobot_goods_title;
    private TextView sobot_goods_des;
    private TextView sobot_goods_price;
    private TextView sobot_goods_btn;

    private int themeColor;
    private boolean changeThemeColor;

    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();

    public CustomCardVerticalMessageHolder(Context context, View convertView) {
        super(context, convertView);
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(context);
        }
        sobot_goods_btn = convertView.findViewById(R.id.sobot_goods_sendBtn);
        sobot_goods_pic = convertView.findViewById(R.id.sobot_goods_pic);
        sobot_goods_title = convertView.findViewById(R.id.sobot_goods_title);
        sobot_goods_title.setLines(1);
        sobot_goods_des = convertView.findViewById(R.id.sobot_goods_des);
        sobot_goods_price = convertView.findViewById(R.id.sobot_goods_label);
    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        customCard = message.getCustomCard();
        if (customCard != null && customCard.getCustomCards() != null && !customCard.getCustomCards().isEmpty()) {
            SobotChatCustomGoods customGoods = customCard.getCustomCards().get(0);

            sobot_goods_title.setText(customGoods.getCustomCardName());
            if (!TextUtils.isEmpty(customGoods.getCustomCardThumbnail())) {
                sobot_goods_pic.setImageUrl(CommonUtils.encode(customGoods.getCustomCardThumbnail()));
                sobot_goods_pic.setVisibility(View.VISIBLE);
            } else {
                sobot_goods_pic.setVisibility(View.GONE);
            }
            sobot_goods_des.setText(customGoods.getCustomCardDesc());
            //金额显示
            if (!StringUtils.isEmpty(customGoods.getCustomCardAmount())) {
                String price = "";
                boolean hasF = false;
                if (!StringUtils.isEmpty(customGoods.getCustomCardAmountSymbol())) {
                    hasF = true;
                    price = customGoods.getCustomCardAmountSymbol();
                }
                if (!StringUtils.isEmpty(customGoods.getCustomCardAmount())) {
                    price += StringUtils.getMoney(customGoods.getCustomCardAmount());
                }
                //第一个字符小
                SpannableString spannableString = new SpannableString(price);
                if (hasF) {
                    spannableString.setSpan(new RelativeSizeSpan(0.6f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (price.contains(".")) {
                    spannableString.setSpan(new RelativeSizeSpan(0.6f), price.indexOf("."), price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                sobot_goods_price.setVisibility(View.VISIBLE);
                sobot_goods_price.setText(spannableString);
            } else {
                sobot_goods_price.setVisibility(View.GONE);
            }
            sobot_real_ll_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(customGoods.getCustomCardLink())) {
                        LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
                        return;
                    }
                    if (SobotOption.hyperlinkListener != null) {
                        SobotOption.hyperlinkListener.onUrlClick(customGoods.getCustomCardLink());
                        return;
                    }

                    if (SobotOption.newHyperlinkListener != null) {
                        //如果返回true,拦截;false 不拦截
                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(context, customGoods.getCustomCardLink());
                        if (isIntercept) {
                            return;
                        }
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("url", customGoods.getCustomCardLink());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            List<SobotChatCustomMenu> menusList = customGoods.getCustomMenus();
                //列表
                if (menusList!=null && !menusList.isEmpty()) {
                    final SobotChatCustomMenu menu = menusList.get(0);
                    sobot_goods_btn.setText(menu.getMenuName());
                    sobot_goods_btn.setTag(0);
                    sobot_goods_btn.setVisibility(View.VISIBLE);
                    if (changeThemeColor) {
                        sobot_goods_btn.setTextColor(themeColor);
                    }
                    if (menu.isDisable()) {
                        sobot_goods_btn.setEnabled(false);
                        sobot_goods_btn.setClickable(false);
                    } else {
                        sobot_goods_btn.setEnabled(true);
                        sobot_goods_btn.setClickable(true);
                        sobot_goods_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //如果是发送按钮，需要发送
                                if (menu.getMenuType() == 2) {
                                    SobotChatCustomCard customCard = new SobotChatCustomCard();
                                    customCard.setCardType(1);
                                    customCard.setCardStyle(customCard.getCardStyle());
                                    customCard.setCardLink(customGoods.getCustomCardLink());
                                    List<SobotChatCustomGoods> goodsList = new ArrayList<>();
                                    goodsList.add(customGoods);
                                    customCard.setCustomCards(goodsList);
                                    customCard.setTicketPartnerField(customCard.getTicketPartnerField());
//                                            customCard.setCustomField(customField);
                                    msgCallBack.sendCardMsg(menu, customCard);
                                } else {
                                    msgCallBack.clickCardMenu(menu);
                                    if (menu.getMenuType() == 1) {
                                        menu.setDisable(true);
                                        v.setEnabled(false);
                                        v.setClickable(false);
                                    }
                                }
                            }
                        });
                    }
                }else{
                    sobot_goods_btn.setVisibility(View.GONE);
                }
        }
    }

    @Override
    public void onClick(View view) {

    }
}
