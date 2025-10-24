package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotGoodsAdapter;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义卡片--横向多张卡片
 */
public class CustomCardHorizontalMessageHolder extends MsgHolderBase  {
    private RecyclerView goods_list_h;//横向商品列表
    private SobotChatCustomCard customCard;
    public CustomCardHorizontalMessageHolder(Context context, View convertView) {
        super(context, convertView);
        goods_list_h = convertView.findViewById(R.id.rv_goods_list_h);
        sobot_msg_content_ll = convertView.findViewById(R.id.sobot_msg_content_ll);
    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        customCard = message.getCustomCard();
        if (customCard != null) {
            //平铺
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
//                    // 设置RecyclerView的LayoutManager
            goods_list_h.setLayoutManager(layoutManager);
            goods_list_h.setBackgroundColor(Color.TRANSPARENT);
            //商品列表
            if (null != customCard.getCustomCards() && customCard.getCustomCards().size() > 0) {
                goods_list_h.setVisibility(View.VISIBLE);
                List<SobotChatCustomGoods> goods = new ArrayList<>();
                if (customCard.getCustomCards().size() > 10) {
                    goods.addAll(customCard.getCustomCards().subList(0, 10));
                } else {
                    goods.addAll(customCard.getCustomCards());
                }
                SobotGoodsAdapter goodsAdapter = new SobotGoodsAdapter(mContext, goods, customCard.getCardStyle(), customCard.getTicketPartnerField(), customCard.getCustomField(), isRight, msgCallBack, message.getSugguestionsFontColor() == 1);
                goodsAdapter.setOnLongClickListener(new SobotGoodsAdapter.OnLongClickListener() {
                    @Override
                    public void onLongClick(View view) {
                        showAppointPopWindows(mContext, view, 0, 18, message);
                    }
                });
                goods_list_h.setAdapter(goodsAdapter);
            } else {
                goods_list_h.setVisibility(View.GONE);
            }
        }
        refreshReadStatus();
    }

}
