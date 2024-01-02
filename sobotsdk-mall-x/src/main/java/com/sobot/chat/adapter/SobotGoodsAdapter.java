package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.api.model.customcard.SobotChatCustomMenu;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义卡片--商品列表（横竖屏）
 */
public class SobotGoodsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SobotChatCustomGoods> mData;
    private int cardStyle;
    private Context context;
    private boolean isRight;
    private SobotMsgAdapter.SobotMsgCallBack msgCallBack;
    private int maxBtnNum = 0;
    private boolean isHistory;
    private int itemWidth;
    private int themeColor;
    private boolean changeThemeColor;
    private boolean isOne = false;//是否是一个商品


    public SobotGoodsAdapter(Context context, List<SobotChatCustomGoods> list) {
        this.mData = list;
        this.context = context;
        itemWidth = ScreenUtils.getScreenWidth((Activity) context) * 60 / 100 + ScreenUtils.dip2px(context, 36);
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(context);
        }
    }

    public void setData(List<SobotChatCustomGoods> list, int cardStyle, boolean isRight, SobotMsgAdapter.SobotMsgCallBack msgCallBack, boolean isHistory, boolean isOne) {
        this.cardStyle = cardStyle;
        this.isRight = isRight;
        this.msgCallBack = msgCallBack;
        this.isHistory = isHistory;
        this.isOne = isOne;
        if (mData == null) {
            mData = new ArrayList<>();
        } else {
            mData.clear();
        }
        for (int i = 0; i < list.size(); i++) {
            SobotChatCustomGoods goods = list.get(i);
            if (goods.getCustomMenus() != null && goods.getCustomMenus().size() > 0) {
                List<SobotChatCustomMenu> menusList = new ArrayList<>();
                for (int j = 0; j < goods.getCustomMenus().size(); j++) {
                    goods.getCustomMenus().get(j).setMenuId((i + 1) * 10 + j);
                    if (goods.getCustomMenus().get(j).getMenuType() == 0 && goods.getCustomMenus().get(j).getMenuLinkType() == 1) {
                        //客服端的按钮不显示
                    } else {
                        SobotChatCustomMenu temMenu = new SobotChatCustomMenu();
                        temMenu.setMenuId(goods.getCustomMenus().get(j).getMenuId());
                        temMenu.setMenuName(goods.getCustomMenus().get(j).getMenuName());
                        temMenu.setMenuLink(goods.getCustomMenus().get(j).getMenuLink());
                        temMenu.setMenuLinkType(goods.getCustomMenus().get(j).getMenuLinkType());
                        temMenu.setMenuType(goods.getCustomMenus().get(j).getMenuType());
                        temMenu.setMenuTip(goods.getCustomMenus().get(j).getMenuTip());
                        temMenu.setDisable(goods.getCustomMenus().get(j).isDisable());
                        menusList.add(temMenu);
                    }
                }
                if (menusList.size() > maxBtnNum) {
                    maxBtnNum = menusList.size();
                }
                goods.setCustomMenus(menusList);
            }
            this.mData.add(goods);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (cardStyle == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_card_goods_h, viewGroup, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_card_goods_v, viewGroup, false);
        }

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotChatCustomGoods goods = mData.get(i);

        MyHolder holder = (MyHolder) viewHolder;
        if (goods != null) {
            if (cardStyle == 0) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                holder.sobot_real_ll_content.setLayoutParams(layoutParams);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(goods.getCustomCardLink())) {
                        LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
                        return;
                    }
                    if (SobotOption.hyperlinkListener != null) {
                        SobotOption.hyperlinkListener.onUrlClick(goods.getCustomCardLink());
                        return;
                    }

                    if (SobotOption.newHyperlinkListener != null) {
                        //如果返回true,拦截;false 不拦截
                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(context, goods.getCustomCardLink());
                        if (isIntercept) {
                            return;
                        }
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("url", goods.getCustomCardLink());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            holder.sobot_goods_title.setText(goods.getCustomCardName());
            SobotBitmapUtil.display(context, CommonUtils.encode(goods.getCustomCardThumbnail())
                    , holder.sobot_goods_pic);
//            holder.sobot_goods_pic.setOnClickListener(new MsgHolderBase.ImageClickLisenter(context, goods.getCustomCardThumbnail(), isRight));
            holder.sobot_goods_des.setText(goods.getCustomCardDesc());
            String price = "";
            boolean hasF = false;
            if (!StringUtils.isEmpty(goods.getCustomCardAmountSymbol())) {
                hasF = true;
                price = goods.getCustomCardAmountSymbol();
            }
            if (!StringUtils.isEmpty(goods.getCustomCardAmount())) {
                price += StringUtils.getMoney(goods.getCustomCardAmount());
            }
            //第一个字符小
            SpannableString spannableString = new SpannableString(price);
            if (hasF) {
                spannableString.setSpan(new RelativeSizeSpan(0.6f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (price.contains(".")) {
                spannableString.setSpan(new RelativeSizeSpan(0.6f), price.indexOf("."), price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            holder.sobot_goods_price.setText(spannableString);

            if (!isRight) {
                if (goods.getCustomMenus() != null && goods.getCustomMenus().size() > 0) {
                    List<SobotChatCustomMenu> menusList = goods.getCustomMenus();
                    //横屏最多显示3个，竖屏最多显示一个
                    if (cardStyle == 0) {
                        //平铺
                        if (menusList.size() >= 3) {
                            final SobotChatCustomMenu menu = menusList.get(2);
                            holder.sobot_goods_btn3.setText(menu.getMenuName());
                            holder.sobot_goods_btn3.setTag(i);
                            holder.sobot_goods_btn3.setVisibility(View.VISIBLE);
                            if (menu.isDisable()) {
                                holder.sobot_goods_btn3.setEnabled(false);
                                holder.sobot_goods_btn3.setClickable(false);
                                holder.sobot_goods_btn3.setTextColor(context.getResources().getColor(R.color.sobot_goods_des_text_color));
                            } else {
                                holder.sobot_goods_btn3.setEnabled(true);
                                holder.sobot_goods_btn3.setClickable(true);
                                holder.sobot_goods_btn3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        //如果是发送按钮，需要发送
                                        if (menu.getMenuType() == 2) {
                                            SobotChatCustomCard customCard = new SobotChatCustomCard();
                                            customCard.setCardType(1);
                                            customCard.setCardStyle(cardStyle);
                                            customCard.setCardLink(goods.getCustomCardLink());
                                            List<SobotChatCustomGoods> goodsList = new ArrayList<>();
                                            goodsList.add(goods);
                                            customCard.setCustomCards(goodsList);
                                            msgCallBack.sendCardMsg(menu, customCard);
                                        } else {
                                            msgCallBack.clickCardMenu(menu);
                                            if (menu.getMenuType() == 1) {
//                                            menu.setDisable(true);
                                                setMenuDisableById(goods, 2);
                                                v.setEnabled(false);
                                                v.setClickable(false);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            if (maxBtnNum >= 3) {
                                holder.sobot_goods_btn3.setVisibility(View.INVISIBLE);
                            } else {
                                holder.sobot_goods_btn3.setVisibility(View.GONE);
                            }
                        }
                        if (menusList.size() >= 2) {
                            final SobotChatCustomMenu menu = menusList.get(1);
                            holder.sobot_goods_btn2.setText(menu.getMenuName());
                            holder.sobot_goods_btn2.setTag(i);
                            holder.sobot_goods_btn2.setVisibility(View.VISIBLE);
                            if (menu.isDisable()) {
                                holder.sobot_goods_btn2.setEnabled(false);
                                holder.sobot_goods_btn2.setClickable(false);
                            } else {
                                holder.sobot_goods_btn2.setEnabled(true);
                                holder.sobot_goods_btn2.setClickable(true);
                                holder.sobot_goods_btn2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //如果是发送按钮，需要发送
                                        if (menu.getMenuType() == 2) {
                                            SobotChatCustomCard customCard = new SobotChatCustomCard();
                                            customCard.setCardType(1);
                                            customCard.setCardStyle(cardStyle);
                                            customCard.setCardLink(goods.getCustomCardLink());
                                            List<SobotChatCustomGoods> goodsList = new ArrayList<>();
                                            goodsList.add(goods);
                                            customCard.setCustomCards(goodsList);
                                            msgCallBack.sendCardMsg(menu, customCard);
                                        } else {
                                            msgCallBack.clickCardMenu(menu);
                                            if (menu.getMenuType() == 1) {
//                                            menu.setDisable(true);
                                                setMenuDisableById(goods, 1);
                                                v.setEnabled(false);
                                                v.setClickable(false);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            if (maxBtnNum >= 2) {
                                holder.sobot_goods_btn2.setVisibility(View.INVISIBLE);
                            } else {
                                holder.sobot_goods_btn2.setVisibility(View.GONE);
                            }
                        }
                        if (menusList.size() >= 1) {
                            final SobotChatCustomMenu menu = menusList.get(0);
                            holder.sobot_goods_btn.setVisibility(View.VISIBLE);
                            holder.sobot_goods_btn.setText(menu.getMenuName());
                            holder.sobot_goods_btn.setTag(i);
                            if (menu.isDisable()) {
                                holder.sobot_goods_btn.setEnabled(false);
                                holder.sobot_goods_btn.setClickable(false);
                            } else {
                                holder.sobot_goods_btn.setEnabled(true);
                                holder.sobot_goods_btn.setClickable(true);
                                holder.sobot_goods_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //如果是发送按钮，需要发送
                                        if (menu.getMenuType() == 2) {
                                            SobotChatCustomCard customCard = new SobotChatCustomCard();
                                            customCard.setCardType(1);
                                            customCard.setCardStyle(cardStyle);
                                            customCard.setCardLink(goods.getCustomCardLink());
                                            List<SobotChatCustomGoods> goodsList = new ArrayList<>();
                                            goodsList.add(goods);
                                            customCard.setCustomCards(goodsList);
                                            msgCallBack.sendCardMsg(menu, customCard);
                                        } else {
                                            msgCallBack.clickCardMenu(menu);
                                            if (menu.getMenuType() == 1) {
//                                            menu.setDisable(true);
                                                setMenuDisableById(goods, 0);
                                                v.setEnabled(false);
                                                v.setClickable(false);
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            if (maxBtnNum >= 1) {
                                holder.sobot_goods_btn.setVisibility(View.INVISIBLE);
                            } else {
                                holder.sobot_goods_btn.setVisibility(View.GONE);
                            }
                        }
//                        holder.sobot_goods_btn.setText(Color.WHITE);
                    } else {
                        //列表
                        final SobotChatCustomMenu menu = menusList.get(0);
                        holder.sobot_goods_btn.setText(menu.getMenuName());
                        holder.sobot_goods_btn.setTag(i);
                        holder.sobot_goods_btn.setVisibility(View.VISIBLE);
                        if (isOne) {
                            Drawable drawable = context.getResources().getDrawable(R.drawable.sobot_evaluate_commit_selector);
                            drawable = ThemeUtils.applyColorToDrawable(drawable, themeColor);
                            holder.sobot_goods_btn.setBackground(drawable);
                            holder.sobot_goods_btn.setTextColor(context.getResources().getColor(R.color.sobot_common_white));
                        } else {
                            if (changeThemeColor) {
                                holder.sobot_goods_btn.setTextColor(themeColor);
                            }
                        }
                        if (menu.isDisable()) {
                            holder.sobot_goods_btn.setEnabled(false);
                            holder.sobot_goods_btn.setClickable(false);
                        } else {
                            holder.sobot_goods_btn.setEnabled(true);
                            holder.sobot_goods_btn.setClickable(true);
                            holder.sobot_goods_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //如果是发送按钮，需要发送
                                    if (menu.getMenuType() == 2) {
                                        SobotChatCustomCard customCard = new SobotChatCustomCard();
                                        customCard.setCardType(1);
                                        customCard.setCardStyle(cardStyle);
                                        customCard.setCardLink(goods.getCustomCardLink());
                                        List<SobotChatCustomGoods> goodsList = new ArrayList<>();
                                        goodsList.add(goods);
                                        customCard.setCustomCards(goodsList);
                                        msgCallBack.sendCardMsg(menu, customCard);
                                    } else {
                                        msgCallBack.clickCardMenu(menu);
                                        if (menu.getMenuType() == 1) {
                                            menu.setDisable(true);
                                            setMenuDisableById(goods, 0);
                                            v.setEnabled(false);
                                            v.setClickable(false);
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else {
                    if (holder.sobot_goods_btn3 != null && maxBtnNum == 3) {
                        holder.sobot_goods_btn3.setVisibility(View.INVISIBLE);
                        holder.sobot_goods_btn2.setVisibility(View.INVISIBLE);
                        holder.sobot_goods_btn.setVisibility(View.INVISIBLE);
                    } else if (holder.sobot_goods_btn2 != null && maxBtnNum == 2) {
                        holder.sobot_goods_btn3.setVisibility(View.GONE);
                        holder.sobot_goods_btn2.setVisibility(View.INVISIBLE);
                        holder.sobot_goods_btn.setVisibility(View.INVISIBLE);
                    } else if (holder.sobot_goods_btn != null && maxBtnNum == 1) {
                        holder.sobot_goods_btn3.setVisibility(View.GONE);
                        holder.sobot_goods_btn2.setVisibility(View.GONE);
                        holder.sobot_goods_btn.setVisibility(View.INVISIBLE);
                    } else {
                        if (holder.sobot_goods_btn3 != null) {
                            holder.sobot_goods_btn3.setVisibility(View.GONE);
                        }
                        if (holder.sobot_goods_btn2 != null) {
                            holder.sobot_goods_btn2.setVisibility(View.GONE);
                        }
                        if (holder.sobot_goods_btn != null) {
                            holder.sobot_goods_btn.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mOnLongClickListener != null) {
                            mOnLongClickListener.onLongClick(v);
                        }
                        return false;
                    }
                });
                holder.sobot_goods_btn.setVisibility(View.GONE);
                if (cardStyle == 0) {
                    if (holder.sobot_goods_btn2 != null) {
                        holder.sobot_goods_btn2.setVisibility(View.GONE);
                    }
                    if (holder.sobot_goods_btn3 != null) {
                        holder.sobot_goods_btn3.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

    // 手动添加长按事件
    public interface OnLongClickListener {
        void onLongClick(View view);
    }

    private OnLongClickListener mOnLongClickListener = null;

    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private ImageView sobot_goods_pic;
        private TextView sobot_goods_title;
        private TextView sobot_goods_des;
        private TextView sobot_goods_price;
        private TextView sobot_goods_btn, sobot_goods_btn2, sobot_goods_btn3;
        private LinearLayout sobot_real_ll_content;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            sobot_real_ll_content = itemView.findViewById(R.id.sobot_real_ll_content);
            sobot_goods_pic = itemView.findViewById(R.id.sobot_goods_pic);
            sobot_goods_title = itemView.findViewById(R.id.sobot_goods_title);
            sobot_goods_des = itemView.findViewById(R.id.sobot_goods_des);
            sobot_goods_price = itemView.findViewById(R.id.sobot_goods_price);
            sobot_goods_btn = itemView.findViewById(R.id.sobot_goods_btn);
            sobot_goods_btn2 = itemView.findViewById(R.id.sobot_goods_btn2);
            sobot_goods_btn3 = itemView.findViewById(R.id.sobot_goods_btn3);
            if (changeThemeColor) {
                if (sobot_goods_btn != null) {
                    Drawable bg = sobot_goods_btn.getBackground();
                    if (bg != null) {
                        sobot_goods_btn.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
            }
        }
    }

    private void setMenuDisableById(SobotChatCustomGoods goods, int menuIndex) {
        for (int i = 0; i < mData.size(); i++) {
            if (goods.getCustomCardId().equals(mData.get(i).getCustomCardId())) {
                for (int j = 0; j < mData.get(i).getCustomMenus().size(); j++) {
                    if (j == menuIndex) {
                        mData.get(i).getCustomMenus().get(j).setDisable(true);
                    }
                }
            }
        }
    }
}
