package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.api.model.customcard.SobotChatCustomMenu;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
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
public class CustomCardVerticalMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private SobotChatCustomCard customCard;

    //竖向订单商品信息
    private SobotProgressImageView sobot_goods_pic;
    private TextView sobot_goods_title;
    private TextView sobot_goods_des;
    private TextView sobot_goods_price;
    private TextView sobot_goods_btn;
    private LinearLayout sobot_card_menu;
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
        sobot_card_menu = convertView.findViewById(R.id.sobot_card_menu);
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
                if (!StringUtils.isEmpty(customGoods.getCustomCardAmountSymbol())) {
                    price = customGoods.getCustomCardAmountSymbol();
                }
                if (!StringUtils.isEmpty(customGoods.getCustomCardAmount())) {
                    price += customGoods.getCustomCardAmount();
                }

                sobot_goods_price.setVisibility(View.VISIBLE);
                sobot_goods_price.setText(price);
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
                    if (SobotOption.dispatchUrlClick(context, customGoods.getCustomCardLink())) {
                        return;
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("url", customGoods.getCustomCardLink());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            List<SobotChatCustomMenu> menusList = customGoods.getCustomMenus();
            //列表
            if (menusList != null && !menusList.isEmpty()) {
                //如果列表只有一个
                if (menusList.size() == 1) {
                    final SobotChatCustomMenu menu = menusList.get(0);
                    sobot_goods_btn.setText(menu.getMenuName());
                    sobot_goods_btn.setTag(0);
                    sobot_goods_btn.setVisibility(View.VISIBLE);
                    if (changeThemeColor) {
                        sobot_goods_btn.setTextColor(themeColor);
                    }
                    if (menu.isDisable() || (message.getSugguestionsFontColor() == 1 && menu.getMenuType() == 2)) {
                        //历史消息不可点击
                        sobot_goods_btn.setAlpha(0.5f);
                        sobot_goods_btn.setEnabled(false);
                        sobot_goods_btn.setClickable(false);
                    } else {
                        sobot_goods_btn.setAlpha(1f);
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
                } else {
                    sobot_goods_btn.setVisibility(View.GONE);
                    sobot_card_menu.setVisibility(View.VISIBLE);

                    //按钮最多显示3个
                    if (menusList.size() > 3) {
                        createMenuView(sobot_card_menu, menusList.subList(0, 3), customCard);
                    } else {
                        createMenuView(sobot_card_menu, menusList, customCard);
                    }
                }
            } else {
                sobot_goods_btn.setVisibility(View.GONE);
            }
        }
    }

    private void createMenuView(LinearLayout antoLineLayout, final List<SobotChatCustomMenu> tmpData, final SobotChatCustomCard customCard) {
        if (antoLineLayout == null || tmpData == null || tmpData.isEmpty()) {
            return;
        }

        antoLineLayout.removeAllViews();

        // 获取容器宽度
        int containerWidth = antoLineLayout.getWidth();
        if (containerWidth <= 0) {
            containerWidth = ScreenUtils.getScreenWidth(mContext) - ScreenUtils.dip2px(mContext, 40);
        }

        // 判断是否需要横向布局
        boolean needHorizontalLayout = shouldUseHorizontalLayout(tmpData, containerWidth);
        antoLineLayout.setOrientation(needHorizontalLayout ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        // 预加载常量和资源
        int margin = ScreenUtils.dip2px(mContext, 10);
        int colorWhite = mContext.getResources().getColor(R.color.sobot_color_white);
        int colorTitleText = mContext.getResources().getColor(R.color.sobot_goods_title_text_color);
        int colorDesText = mContext.getResources().getColor(R.color.sobot_goods_des_text_color);

        Drawable primaryDrawable = ThemeUtils.applyColorWithMultiplyMode(
                mContext.getResources().getDrawable(R.drawable.sobot_bg_theme_color_28dp), themeColor);
        Drawable secondaryDrawable = mContext.getResources().getDrawable(R.drawable.sobot_btn_bg_line_28);

        for (int i = 0; i < tmpData.size(); i++) {
            final SobotChatCustomMenu menu = tmpData.get(i);
            final TextView view = (TextView) View.inflate(mContext, R.layout.sobot_chat_msg_item_card_btn, null);

            // 设置按钮样式
            if (i == 0) {
                view.setBackground(primaryDrawable);
                view.setTextColor(colorWhite);
            } else {
                view.setBackground(secondaryDrawable);
                view.setTextColor(colorTitleText);
            }

            // 设置布局参数
            LinearLayout.LayoutParams params;
            if (needHorizontalLayout) {
                params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                params.rightMargin = margin;
            } else {
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.topMargin = margin;
            }
            params.gravity = Gravity.CENTER;
            view.setLayoutParams(params);
            view.setText(menu.getMenuName());

            // 设置按钮状态和点击事件
            //历史消息不可点击
            if (menu.isDisable() || (message.getSugguestionsFontColor() == 1 && menu.getMenuType() == 2)) {
                view.setEnabled(false);
                view.setClickable(false);
                view.setAlpha(0.5f);
                view.setTextColor(colorDesText);
            } else {
                view.setAlpha(1f);
                view.setOnClickListener(v -> {
                    if (menu.getMenuType() == 2) {
                        msgCallBack.sendCardMsg(menu, customCard);
                    } else {
                        msgCallBack.clickCardMenu(menu);
                        if (menu.getMenuType() == 1) {
                            menu.setDisable(true);
                            setMenuDisableById(menu.getMenuId());
                            v.setEnabled(false);
                            v.setClickable(false);
                            v.setAlpha(0.5f);
                            view.setTextColor(colorDesText);
                        }
                    }
                });
            }

            antoLineLayout.addView(view);
        }
    }

    /**
     * 判断是否应该使用横向布局
     * 如果所有按钮的总宽度（包含间距）小于等于容器宽度，则横向布局；否则纵向布局
     */
    private boolean shouldUseHorizontalLayout(List<SobotChatCustomMenu> menus, int containerWidth) {
        if (menus == null || menus.isEmpty() || containerWidth <= 0) {
            return false;
        }

        // 使用Paint进行文本测量，性能更优
        android.text.TextPaint paint = new android.text.TextPaint();
        paint.setTextSize(14 * mContext.getResources().getDisplayMetrics().scaledDensity);

        int buttonPadding = ScreenUtils.dip2px(mContext, 40); // 左右各20dp
        int marginBetweenButtons = ScreenUtils.dip2px(mContext, 10);
        int totalWidth = 0;

        for (int i = 0; i < menus.size(); i++) {
            float textWidth = paint.measureText(menus.get(i).getMenuName());
            int buttonWidth = (int) Math.ceil(textWidth) + buttonPadding;
            totalWidth += buttonWidth;

            if (i < menus.size() - 1) {
                totalWidth += marginBetweenButtons;
            }
        }

        return totalWidth <= containerWidth;
    }

    private void setMenuDisableById(int menuId) {
        if (customCard != null && customCard.getCardMenus() != null) {
            for (int i = 0; i < customCard.getCardMenus().size(); i++) {
                if (customCard.getCardMenus().get(i).getMenuId() == menuId) {
                    customCard.getCardMenus().get(i).setDisable(true);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {

    }
}
