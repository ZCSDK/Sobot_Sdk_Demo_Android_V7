package com.sobot.chat.viewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.adapter.SobotGoodsAdapter;
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
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.ReceivingLinearLayout;
import com.sobot.chat.widget.SobotAntoLineEquidistanceLayout;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 自定义卡片
 */
public class CustomCardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView mTitle;//卡片标题
    private TextView mDesc;//卡片描述
    private ImageView mPic;//卡片图片
    private LinearLayout mParam;//卡片自定义字段
    //  气泡内容显示区
    private LinearLayout sobot_msg_content_ll;
    private int defaultPicResId;
    private SobotChatCustomCard customCard;
    private RecyclerView recyclerView;//商品列表
    private SobotAntoLineEquidistanceLayout menuLin;//按钮显示
    private LinearLayout sobot_card_menu_h;//按钮显示

    //竖向订单商品信息
    private LinearLayout ll_order_good_info;
    private ImageView sobot_order_good_pic;
    private TextView sobot_order_good_title;
    private TextView sobot_order_good_des;
    private TextView sobot_order_good_count;

    //横向订单商品信息
    private LinearLayout ll_order_good_info_h;
    private ImageView sobot_order_good_pic_h;
    private TextView sobot_order_good_title_h;
    private TextView sobot_order_good_des_h;
    private TextView sobot_order_good_count_h;
    private TextView sobot_goods_price_h;

    //订单固定字段
    private LinearLayout ll_order_param;
    private TextView sobot_order_code;
    private TextView sobot_order_status;
    private TextView sobot_order_time;
    private int themeColor;
    private boolean changeThemeColor;
    private ReceivingLinearLayout sobot_card_rll;



    public CustomCardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) * 60 / 100;
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(context);
        }
        sobot_card_rll = convertView.findViewById(R.id.sobot_card_rll);
        mPic = convertView.findViewById(R.id.sobot_card_pic);
        sobot_card_menu_h = convertView.findViewById(R.id.sobot_card_menu_h);
        mTitle = convertView.findViewById(R.id.sobot_card_title);
        mDesc = convertView.findViewById(R.id.sobot_card_desc);
        mParam = convertView.findViewById(R.id.sobot_card_param);
        menuLin = convertView.findViewById(R.id.sobot_card_menu);
        recyclerView = convertView.findViewById(R.id.rv_goods_list);
        defaultPicResId = R.drawable.sobot_icon_consulting_default_pic;
        sobot_msg_content_ll = convertView.findViewById(R.id.sobot_msg_content_ll);
        ll_order_good_info = convertView.findViewById(R.id.ll_order_good_info);
        sobot_order_good_pic = convertView.findViewById(R.id.sobot_order_good_pic);
        sobot_order_good_title = convertView.findViewById(R.id.sobot_order_good_title);
        sobot_order_good_des = convertView.findViewById(R.id.sobot_order_good_des);
        sobot_order_good_count = convertView.findViewById(R.id.sobot_order_good_count);
        ll_order_good_info_h = convertView.findViewById(R.id.ll_order_good_info_h);
        sobot_order_good_pic_h = convertView.findViewById(R.id.sobot_order_good_pic_h);
        sobot_order_good_title_h = convertView.findViewById(R.id.sobot_order_good_title_h);
        sobot_order_good_des_h = convertView.findViewById(R.id.sobot_order_good_des_h);
        sobot_order_good_count_h = convertView.findViewById(R.id.sobot_order_good_count_h);
        sobot_goods_price_h = convertView.findViewById(R.id.sobot_goods_price_h);
        ll_order_param = convertView.findViewById(R.id.ll_order_param);
        sobot_order_code = convertView.findViewById(R.id.sobot_order_code);
        sobot_order_status = convertView.findViewById(R.id.sobot_order_status);
        sobot_order_time = convertView.findViewById(R.id.sobot_order_time);
        if (sobot_card_rll != null) {
            sobot_card_rll.bindExpandButton((ImageView) convertView.findViewById(R.id.iv_expand_icon), (TextView) convertView.findViewById(R.id.tv_expand_icon), R.drawable.sobot_notice_arrow_down, R.drawable.sobot_notice_arrow_up);
            sobot_card_rll.setLimitHeight(ScreenUtils.dip2px(mContext, 468));//设置折叠的临界高度
            //view加载完成时回调
            sobot_card_rll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (sobot_card_rll.isIsExpand()) {
                        if (customCard != null) {
                            customCard.setOpen(true);
                        }
                        sobot_card_rll.setExpandBtnVisiable(View.GONE);
                    }
                }
            });
        }
        if (recyclerView != null) {
            recyclerView.setItemViewCacheSize(10);
        }
        resetMaxWidth();

    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        customCard = message.getCustomCard();
        boolean isOnlyOne = false;
        if (customCard != null) {

            if (initMode != null && !isRight) {
                if (initMode.getVisitorScheme().getShowFace() == 1) {
                    //显示头像
                    imgHead.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(mContext, CommonUtils.encode(message.getSenderFace()),
                            imgHead, R.drawable.sobot_def_admin, R.drawable.sobot_def_admin);
                } else {
                    //隐藏头像
                    imgHead.setVisibility(View.GONE);
                }
                if (initMode.getVisitorScheme().getShowStaffNick() == 1) {
                    //显示昵称
                    name.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(mContext, CommonUtils.encode(message.getSenderFace()),
                            imgHead, R.drawable.sobot_def_admin, R.drawable.sobot_def_admin);
                } else {
                    //隐藏昵称
                    name.setVisibility(View.GONE);
                }
            }
            if (sobot_card_rll != null && !StringUtils.isEmpty(customCard.getCardId())) {
                if (customCard.isOpen()) {
                    sobot_card_rll.setSupportExpand(false);
                }
            }
            if (customCard.getCardStyle() == 0) {
                //横向滑动，除了列表，其他都不展示
                recyclerView.setVisibility(View.VISIBLE);
                mPic.setVisibility(View.GONE);
                mTitle.setVisibility(View.GONE);
                mDesc.setVisibility(View.GONE);
                mParam.setVisibility(View.GONE);
                menuLin.setVisibility(View.GONE);

                ll_order_param.setVisibility(View.GONE);
                // 0, "订单卡片",1, "商品卡片"
                if (customCard.getCardType() == 0 || isRight || (customCard.getCardType() == 1 && customCard.getCustomCards().size() == 1)) {
                    recyclerView.setVisibility(View.GONE);
                    ll_order_good_info.setVisibility(View.GONE);
                    ll_order_good_info_h.setVisibility(View.VISIBLE);
                    ll_order_good_info_h.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(customCard!=null &&customCard.getCustomCards() !=null && customCard.getCustomCards().size()>0 ) {
                                if (TextUtils.isEmpty(customCard.getCustomCards().get(0).getCustomCardLink())) {
                                    LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
                                    return;
                                }
                                if (SobotOption.hyperlinkListener != null) {
                                    SobotOption.hyperlinkListener.onUrlClick(customCard.getCustomCards().get(0).getCustomCardLink());
                                    return;
                                }

                                if (SobotOption.newHyperlinkListener != null) {
                                    //如果返回true,拦截;false 不拦截
                                    boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, customCard.getCustomCards().get(0).getCustomCardLink());
                                    if (isIntercept) {
                                        return;
                                    }
                                }
                                Intent intent = new Intent(mContext, WebViewActivity.class);
                                intent.putExtra("url", customCard.getCustomCards().get(0).getCustomCardLink());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    resetMaxWidth();
//                    if (isRight) {
                    sobot_msg_content_ll.setBackground(null);
//                    } else {
//                        if (sobot_msg_content_ll.getBackground() == null) {
//                            sobot_msg_content_ll.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_card_background_shadow));
//                        }
//                    }
                    //商品列表
                    if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                        SobotChatCustomGoods goods = customCard.getCustomCards().get(0);
                        if(!TextUtils.isEmpty(goods.getCustomCardThumbnail())) {
                            SobotBitmapUtil.display(context, CommonUtils.encode(goods.getCustomCardThumbnail())
                                    , sobot_order_good_pic_h);
                            sobot_order_good_pic_h.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            sobot_order_good_pic_h.setVisibility(View.VISIBLE);
                        }else{
                            sobot_order_good_pic_h.setVisibility(View.GONE);
                        }
//                        sobot_order_good_pic_h.setOnClickListener(new MsgHolderBase.ImageClickLisenter(context, goods.getCustomCardThumbnail(), isRight));
                        sobot_order_good_title_h.setText(goods.getCustomCardName());
                        sobot_order_good_des_h.setText(goods.getCustomCardDesc());
                        if (customCard.getCardType() == 0) {
                            if (sobot_goods_price_h != null) {
                                sobot_goods_price_h.setVisibility(View.GONE);
                            }
                            sobot_order_good_count_h.setVisibility(View.VISIBLE);
                            String s = "";
                            if (!TextUtils.isEmpty(goods.getCustomCardCount())) {
                                s =  context.getResources().getString(R.string.sobot_order_total_money) + " "  + goods.getCustomCardCount() + context.getResources().getString(R.string.sobot_how_goods);
                            }
                            if (!TextUtils.isEmpty(goods.getCustomCardAmount())) {
                                s +=  context.getResources().getString(R.string.sobot_order_total_money) + " " + goods.getCustomCardAmountSymbol() + StringUtils.getMoney(goods.getCustomCardAmount());
                            }
                            sobot_order_good_count_h.setText(s) ;
//                            + context.getResources().getString(R.string.sobot_order_total_money) + " " + goods.getCustomCardAmountSymbol() + StringUtils.getMoney(goods.getCustomCardAmount()));
                        } else {
                            sobot_order_good_count_h.setVisibility(View.GONE);
                            if (sobot_goods_price_h != null) {
                                if(!TextUtils.isEmpty(goods.getCustomCardAmount())) {
                                    sobot_goods_price_h.setVisibility(View.VISIBLE);
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
                                    sobot_goods_price_h.setText(spannableString);
                                }else{
                                    sobot_goods_price_h.setVisibility(View.GONE);
                                }
                            }
                        }
                        //按钮显示
                        menuLin.setVisibility(View.GONE);
                        if (sobot_card_menu_h != null) {
                            if (goods.getCustomMenus() != null && goods.getCustomMenus().size() > 0) {
                                List<SobotChatCustomMenu> menus = new ArrayList<>();
                                for (int i = 0; i < goods.getCustomMenus().size(); i++) {
                                    goods.getCustomMenus().get(i).setMenuId(i);
                                    if (goods.getCustomMenus().get(i).getMenuType() == 0 && goods.getCustomMenus().get(i).getMenuLinkType() == 1) {
                                        //客服端的按钮不显示
                                    } else {
                                        menus.add(goods.getCustomMenus().get(i));
                                    }
                                }
                                sobot_card_menu_h.setVisibility(View.VISIBLE);
                                sobot_card_menu_h.removeAllViews();
                                //按钮最多显示3个
                                if (menus.size() > 3) {
                                    createMenuView(sobot_card_menu_h, menus.subList(0, 3), customCard);
                                } else {
                                    createMenuView(sobot_card_menu_h, menus, customCard);
                                }
                            } else {
                                ll_order_good_info_h.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 15));
                                sobot_card_menu_h.setVisibility(View.GONE);
                            }
                        }
                        ll_order_good_info_h.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                showAppointPopWindows(mContext, v, 0, 18, message);
                                return false;
                            }
                        });
                    } else {
                        ll_order_good_info_h.setVisibility(View.GONE);
                    }
                } else {
                    name.setVisibility(View.GONE);
                    imgHead.setVisibility(View.GONE);
                    sobot_msg_content_ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    sobot_msg_content_ll.setPadding(0, 0, 0, 0);
                    sobot_msg_content_ll.setBackground(null);
                    ll_order_good_info.setVisibility(View.GONE);
                    ll_order_good_info_h.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    //平铺
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                    // 设置RecyclerView的LayoutManager
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setPadding(0, 0, ScreenUtils.dip2px(mContext, 15), 0);
                    recyclerView.setBackgroundColor(Color.TRANSPARENT);
                    ViewGroup.MarginLayoutParams marginLayoutParams =
                            (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                    marginLayoutParams.setMargins(0, 0, 0, 0);
                    recyclerView.setLayoutParams(marginLayoutParams);
                    //商品列表
                    if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        List<SobotChatCustomGoods> goods = new ArrayList<>();
                        if (customCard.getCustomCards().size() > 10) {
                            goods.addAll(customCard.getCustomCards().subList(0, 10));
                        } else {
                            goods.addAll(customCard.getCustomCards());
                        }
                        SobotGoodsAdapter goodsAdapter = new SobotGoodsAdapter(mContext,goods, customCard.getCardStyle(), isRight, msgCallBack, message.getSugguestionsFontColor() == 1, isOnlyOne);
                        goodsAdapter.setOnLongClickListener(new SobotGoodsAdapter.OnLongClickListener() {
                            @Override
                            public void onLongClick(View view) {
                                showAppointPopWindows(mContext, view, 0, 18, message);
                            }
                        });
                        recyclerView.setAdapter(goodsAdapter);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            } else {
                //竖屏
                if (sobot_msg_content_ll.getBackground() == null) {
                    sobot_msg_content_ll.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_card_background_shadow));
                }
//                sobot_msg_content_ll.setPadding(ScreenUtils.dip2px(mContext,12), ScreenUtils.dip2px(mContext,12), ScreenUtils.dip2px(mContext,12), ScreenUtils.dip2px(mContext,12));
                //是否只有一个商品,只有商品用，订单样式不用, // 0, cardType"订单卡片",1, "商品卡片"

                boolean isTop = true;
                if (null != customCard.getCustomCards() && customCard.getCustomCards().size() == 1 && customCard.getCardType() == 1) {
                    isOnlyOne = true;
                }
                //标题
                if (!TextUtils.isEmpty(CommonUtils.encode(customCard.getCardGuide()))) {
                    mTitle.setText(customCard.getCardGuide());
                    mTitle.setVisibility(View.VISIBLE);
                    isOnlyOne = false;
                    isTop = false;
                } else {
                    mTitle.setVisibility(View.GONE);
                }
                //备注
                if (!TextUtils.isEmpty(CommonUtils.encode(customCard.getCardDesc()))) {
                    mDesc.setText(customCard.getCardDesc());
                    mDesc.setVisibility(View.VISIBLE);
                    isOnlyOne = false;
                    isTop = false;
                } else {
                    mDesc.setVisibility(View.GONE);
                }
                //图片
                if (!TextUtils.isEmpty(CommonUtils.encode(customCard.getCardImg()))) {
                    mPic.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(context, CommonUtils.encode(customCard.getCardImg())
                            , mPic, defaultPicResId, defaultPicResId);
//                    mPic.setOnClickListener(new MsgHolderBase.ImageClickLisenter(context, customCard.getCardImg(), isRight));
                    isOnlyOne = false;
                    isTop = false;
                } else {
                    mPic.setVisibility(View.GONE);
                }
//自定义字段
                if (null != customCard.getCustomField() && customCard.getCustomField().size() > 0) {
                    Map<String, Object> map = customCard.getCustomField();
                    mParam.removeAllViews();
                    mParam.setVisibility(View.VISIBLE);
                    for (String key : map.keySet()) {
                        TextView textView = new TextView(mContext);
                        textView.setTextColor(context.getResources().getColor(R.color.sobot_goods_title_text_color));
                        textView.setTextSize(12);
                        LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        wlayoutParams.topMargin = ScreenUtils.dip2px(mContext, 6);
                        textView.setLayoutParams(wlayoutParams);
                        textView.setMaxWidth(msgMaxWidth);
                        textView.setText(key + "：" + map.get(key).toString());
                        mParam.addView(textView);
                    }
                    isOnlyOne = false;
                } else {
                    mParam.setVisibility(View.GONE);
                }
                //按钮显示
                if (customCard.getCardMenus() != null && customCard.getCardMenus().size() > 0) {
                    List<SobotChatCustomMenu> menus = new ArrayList<>();
                    for (int i = 0; i < customCard.getCardMenus().size(); i++) {
                        customCard.getCardMenus().get(i).setMenuId(i);
                        if ( customCard.getCardMenus().get(i).getMenuLinkType() == 1) {
                            //客服端的按钮不显示
                        } else {
                            menus.add(customCard.getCardMenus().get(i));
                        }
                    }
                    if (menuLin != null) {
                        menuLin.setMaxWight(msgMaxWidth + ScreenUtils.dip2px(mContext, 6));
                    }
                    menuLin.setVisibility(View.VISIBLE);
                    menuLin.removeAllViews();
                    //按钮最多显示3个
                    if (menus.size() > 3) {
                        createMenuView(menuLin, menus.subList(0, 3), customCard);
                    } else {
                        createMenuView(menuLin, menus, customCard);
                    }
                    isOnlyOne = false;
                } else {
                    menuLin.setVisibility(View.GONE);
                }
                // 0, "订单卡片",1, "商品卡片"
                if (customCard.getCardType() == 0) {
                    isOnlyOne = false;
                    resetMaxWidth();
                    ll_order_good_info.setVisibility(View.VISIBLE);
                    ll_order_good_info_h.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    ll_order_param.setVisibility(View.VISIBLE);
                    if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                        SobotChatCustomGoods goods = customCard.getCustomCards().get(0);
                        ll_order_good_info.setVisibility(View.VISIBLE);
                        ll_order_good_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(customCard!=null &&customCard.getCustomCards() !=null && customCard.getCustomCards().size()>0 ) {
                                    if (TextUtils.isEmpty(customCard.getCustomCards().get(0).getCustomCardLink())) {
                                        LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
                                        return;
                                    }
                                    if (SobotOption.hyperlinkListener != null) {
                                        SobotOption.hyperlinkListener.onUrlClick(customCard.getCustomCards().get(0).getCustomCardLink());
                                        return;
                                    }

                                    if (SobotOption.newHyperlinkListener != null) {
                                        //如果返回true,拦截;false 不拦截
                                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, customCard.getCustomCards().get(0).getCustomCardLink());
                                        if (isIntercept) {
                                            return;
                                        }
                                    }
                                    Intent intent = new Intent(mContext, WebViewActivity.class);
                                    intent.putExtra("url", customCard.getCustomCards().get(0).getCustomCardLink());
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                        if(!TextUtils.isEmpty(goods.getCustomCardThumbnail())) {
                            SobotBitmapUtil.display(context, CommonUtils.encode(goods.getCustomCardThumbnail())
                                    , sobot_order_good_pic);
                            sobot_order_good_pic.setVisibility(View.VISIBLE);
                        }else {
                            sobot_order_good_pic.setVisibility(View.GONE);
                        }
//                        sobot_order_good_pic.setOnClickListener(new MsgHolderBase.ImageClickLisenter(context, goods.getCustomCardThumbnail(), isRight));
                        sobot_order_good_title.setText(goods.getCustomCardName());
                        sobot_order_good_des.setText(goods.getCustomCardDesc());
                        String s = "";
                        if (!TextUtils.isEmpty(goods.getCustomCardCount())) {
                            s = context.getResources().getString(R.string.sobot_card_order_num) + goods.getCustomCardCount() + context.getResources().getString(R.string.sobot_how_goods);
                        }
                        if (!TextUtils.isEmpty(goods.getCustomCardAmount())) {
                            s += "  " + context.getResources().getString(R.string.sobot_order_total_money) + " " + goods.getCustomCardAmountSymbol() + StringUtils.getMoney(goods.getCustomCardAmount());
                        }
                        if (!TextUtils.isEmpty(s)) {
                            sobot_order_good_count.setVisibility(View.VISIBLE);
                            sobot_order_good_count.setText(s);
                        } else {
                            sobot_order_good_count.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(goods.getCustomCardCode())) {
                            sobot_order_code.setText(context.getResources().getString(R.string.sobot_card_order_id) + "：" + goods.getCustomCardCode());
                            sobot_order_code.setVisibility(View.VISIBLE);
                        } else {
                            sobot_order_code.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(goods.getCustomCardStatus())) {
                            sobot_order_status.setText(context.getResources().getString(R.string.sobot_card_order_status) + "：" + goods.getCustomCardStatus());
                            sobot_order_status.setVisibility(View.VISIBLE);
                        } else {
                            sobot_order_status.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(goods.getCustomCardTime())) {
                            sobot_order_time.setText(context.getResources().getString(R.string.sobot_card_order_time) + "：" + goods.getCustomCardTime());
                            sobot_order_time.setVisibility(View.VISIBLE);
                        } else {
                            sobot_order_time.setVisibility(View.GONE);
                        }
                    } else {
                        ll_order_good_info.setVisibility(View.GONE);
                    }
                    resetMaxWidth();
                    //竖屏订单 宽度显示
                    if (isRight) {
                        sobot_msg_content_ll.setGravity(Gravity.CENTER);
                        ViewGroup.MarginLayoutParams marginLayoutParams =
                                (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                        marginLayoutParams.setMargins(0, 0, 0, 0);
                        recyclerView.setLayoutParams(marginLayoutParams);
                    } else if (isTop) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_order_good_info.getLayoutParams();
                        params.topMargin = ScreenUtils.dip2px(mContext, 15);
                        ll_order_good_info.setLayoutParams(params);
                    }
                } else {
                    ll_order_param.setVisibility(View.GONE);
                    ll_order_good_info.setVisibility(View.GONE);
                    ll_order_good_info_h.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    if (isRight) {
                        recyclerView.setPadding(ScreenUtils.dip2px(context, 12), 0, ScreenUtils.dip2px(context, 12), 0);
                    } else {
                        recyclerView.setPadding(ScreenUtils.dip2px(context, 15), 0, ScreenUtils.dip2px(context, 15), 0);
                    }
                    //列表，设置左右间距
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                    // 设置RecyclerView的LayoutManager
                    recyclerView.setLayoutManager(layoutManager);

                    //竖屏商品 宽度显示
                    if (isOnlyOne || isRight) {

                        ViewGroup.MarginLayoutParams marginLayoutParams =
                                (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                        marginLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;

                        if (isOnlyOne) {
                            name.setVisibility(View.GONE);
                            imgHead.setVisibility(View.GONE);
                            marginLayoutParams.setMargins(0, 0, 0, 0);
                            if (isRight) {
                                recyclerView.setPadding(ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 4), ScreenUtils.dip2px(mContext, 12), 0);
                            } else {
                                recyclerView.setPadding(ScreenUtils.dip2px(mContext, 16), ScreenUtils.dip2px(mContext, 4), ScreenUtils.dip2px(mContext, 9), ScreenUtils.dip2px(mContext, 8));
                            }
                        } else {
                            if (isTop) {
                                marginLayoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 15), 0, 0);
                            } else {
                                marginLayoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 8), 0, 0);
                            }
                        }
                        recyclerView.setLayoutParams(marginLayoutParams);

                        //商品列表
                        if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            List<SobotChatCustomGoods> goods = new ArrayList<>();
                            if (customCard.getCustomCards().size() > 10) {
                                goods.addAll(customCard.getCustomCards().subList(0, 10));
                            } else {
                                goods.addAll(customCard.getCustomCards());
                            }
                            SobotGoodsAdapter goodsAdapter = new SobotGoodsAdapter(mContext,goods, customCard.getCardStyle(), isRight, msgCallBack, message.getSugguestionsFontColor() == 1, isOnlyOne);
                            goodsAdapter.setOnLongClickListener(new SobotGoodsAdapter.OnLongClickListener() {
                                @Override
                                public void onLongClick(View view) {
                                    showAppointPopWindows(mContext, view, 0, 18, message);
                                }
                            });
                            recyclerView.setAdapter(goodsAdapter);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        resetMaxWidth();
                        //商品列表
                        ViewGroup.MarginLayoutParams marginLayoutParams =
                                (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                        marginLayoutParams.setMargins(ScreenUtils.dip2px(mContext, 15), ScreenUtils.dip2px(mContext, 8), ScreenUtils.dip2px(mContext, 15), 0);
                        recyclerView.setLayoutParams(marginLayoutParams);
                        recyclerView.setPadding(0, 0, 0, 0);
                        if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            List<SobotChatCustomGoods> goods = customCard.getCustomCards();
                            SobotGoodsAdapter goodsAdapter = new SobotGoodsAdapter(mContext,goods, customCard.getCardStyle(), isRight, msgCallBack, message.getSugguestionsFontColor() == 1, isOnlyOne);
                            goodsAdapter.setOnLongClickListener(new SobotGoodsAdapter.OnLongClickListener() {
                                @Override
                                public void onLongClick(View view) {
                                    showAppointPopWindows(mContext, view, 0, 18, message);
                                }
                            });
                            recyclerView.setAdapter(goodsAdapter);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                }

            }
            if (isRight) {
                resetMaxWidth();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                sobot_msg_content_ll.setLayoutParams(lp);
                sobot_msg_content_ll.setGravity(Gravity.CENTER);
                try {
                    if (customCard.getCardStyle() == 1) {
                        sobot_msg_content_ll.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_card_background_shadow_common));
                    }
                    menuLin.setVisibility(View.GONE);
                    msgStatus.setClickable(true);
                    if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.GONE);
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                        msgStatus.setVisibility(View.VISIBLE);
                        msgProgressBar.setVisibility(View.GONE);
                    } else if (message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                        msgProgressBar.setVisibility(View.VISIBLE);
                        msgStatus.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isOnlyOne) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                if (isRight) {
                    //正常显示
                    lp.setMargins(0, 0, 0, 0);
                    lp.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                    sobot_msg_content_ll.setPadding(0, 0, 0, 0);
                } else {
                    lp.setMargins(0, 0, ScreenUtils.dip2px(mContext, 15), 0);
                    sobot_msg_content_ll.setGravity(Gravity.CENTER);
                    sobot_msg_content_ll.setPadding(0, ScreenUtils.dip2px(mContext, 3), ScreenUtils.dip2px(mContext, 3), 0);
                }
                resetMaxWidth();
                sobot_msg_content_ll.setLayoutParams(lp);
            } else if (customCard.getCardStyle() == 0 && customCard.getCustomCards().size() > 1) {
                sobot_msg_content_ll.setPadding(0, 0, 0, 0);
            } else {
                sobot_msg_content_ll.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 15));
            }
        }
        sobot_msg_content_ll.setOnClickListener(this);


        refreshReadStatus();
    }

    private void createMenuView(SobotAntoLineEquidistanceLayout antoLineLayout, final List<SobotChatCustomMenu> tmpData, final SobotChatCustomCard customCard) {
        if (antoLineLayout != null) {
            for (int i = 0; i < tmpData.size(); i++) {
                final SobotChatCustomMenu menu = tmpData.get(i);
                final TextView view = (TextView) View.inflate(mContext, R.layout.sobot_chat_msg_item_card_btn, null);
                if (view instanceof TextView) {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_evaluate_commit_selector);
                    drawable = ThemeUtils.applyColorToDrawable(drawable, themeColor);
                    if (i == 0) {
                        view.setBackground(drawable);
                        ((TextView) view).setTextColor(mContext.getResources().getColor(R.color.sobot_common_white));
                    } else {
                        view.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_btn_bg_white_22));
                        ((TextView) view).setTextColor(mContext.getResources().getColor(R.color.sobot_goods_title_text_color));
                    }
                }
                view.setText(menu.getMenuName());
                if (!menu.isDisable()) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //如果是发送按钮，需要发送
                            if (menu.getMenuType() == 2) {
                                msgCallBack.sendCardMsg(menu, customCard);
                            } else {
                                msgCallBack.clickCardMenu(menu);
                                if (menu.getMenuType() == 1) {
                                    menu.setDisable(true);
                                    setMenuDisableById(menu.getMenuId());
                                    v.setEnabled(false);
                                    v.setClickable(false);
                                    view.setTextColor(mContext.getResources().getColor(R.color.sobot_goods_des_text_color));
                                }
                            }
                        }
                    });
                } else {
                    view.setEnabled(false);
                    view.setClickable(false);
                    view.setTextColor(mContext.getResources().getColor(R.color.sobot_goods_des_text_color));
                }
                antoLineLayout.addView(view);
            }
        }
    }

    private void createMenuView(LinearLayout antoLineLayout, final List<SobotChatCustomMenu> tmpData, final SobotChatCustomCard customCard) {
        if (antoLineLayout != null) {
            for (int i = 0; i < tmpData.size(); i++) {
                final SobotChatCustomMenu menu = tmpData.get(i);
                final TextView view = (TextView) View.inflate(mContext, R.layout.sobot_chat_msg_item_card_btn, null);
                if (view instanceof TextView) {
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_evaluate_commit_selector);
                    drawable = ThemeUtils.applyColorToDrawable(drawable, themeColor);
                    if (i == 0) {
                        view.setBackground(drawable);
                        ((TextView) view).setTextColor(mContext.getResources().getColor(R.color.sobot_common_white));
                    } else {
                        view.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_btn_bg_white_22));
                        ((TextView) view).setTextColor(mContext.getResources().getColor(R.color.sobot_goods_title_text_color));
                    }
                }
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(mContext, 32));
                params.topMargin = ScreenUtils.dip2px(mContext, 10);
                view.setLayoutParams(params);
                view.setText(menu.getMenuName());
                if (!menu.isDisable()) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //如果是发送按钮，需要发送
                            if (menu.getMenuType() == 2) {
                                msgCallBack.sendCardMsg(menu, customCard);
                            } else {
                                msgCallBack.clickCardMenu(menu);
                                if (menu.getMenuType() == 1) {
                                    menu.setDisable(true);
                                    setMenuDisableById(menu.getMenuId());
                                    v.setEnabled(false);
                                    v.setClickable(false);
                                    view.setTextColor(mContext.getResources().getColor(R.color.sobot_goods_des_text_color));
                                }
                            }
                        }
                    });
                } else {
                    view.setEnabled(false);
                    view.setClickable(false);
                    view.setTextColor(mContext.getResources().getColor(R.color.sobot_goods_des_text_color));
                }
                antoLineLayout.addView(view);
            }
        }
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
    public void onClick(View v) {
        if (v == sobot_msg_content_ll) {
            if (TextUtils.isEmpty(customCard.getCardLink())) {
                LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
                return;
            }
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(customCard.getCardLink());
                return;
            }

            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, customCard.getCardLink());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", customCard.getCardLink());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
