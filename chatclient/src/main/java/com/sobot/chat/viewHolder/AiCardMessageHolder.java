package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.activity.halfdialog.SobotAiCardMoreActivity;
import com.sobot.chat.adapter.SobotAiCardAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义卡片
 */
public class AiCardMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView mTitle;//卡片标题
    private SobotChatCustomCard customCard;
    private RecyclerView goods_list;//商品列表

    private LinearLayout ll_expand;
    private TextView tv_expand;
    private int themeColor;

    public AiCardMessageHolder(Context context, View convertView) {
        super(context, convertView);
        themeColor = ThemeUtils.getThemeColor(context);
        mTitle = convertView.findViewById(R.id.tv_title);
        tv_expand = convertView.findViewById(R.id.tv_expand);
        if (tv_expand != null) {
            tv_expand.setTextColor(themeColor);
        }
        goods_list = convertView.findViewById(R.id.rv_goods_list);
        sobot_msg_content_ll = convertView.findViewById(R.id.sobot_msg_content_ll);
        ll_expand = convertView.findViewById(R.id.ll_expand);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        if(goods_list!=null) {
            goods_list.setLayoutManager(layoutManager);
        }

    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        resetMaxWidth();
        customCard = message.getCustomCard();
        if (customCard != null) {
            if (mTitle != null) {
                if (StringUtils.isNoEmpty(customCard.getCardGuide())) {
                    mTitle.setText(customCard.getCardGuide());
                    mTitle.setVisibility(View.VISIBLE);
                } else {
                    mTitle.setVisibility(View.GONE);
                }
            }
            if (initMode != null && !isRight) {
                if (initMode.getVisitorScheme().getShowFace() == 1) {
                    //显示头像
                    headIV.setVisibility(View.VISIBLE);
                    headIV.setImageUrl(CommonUtils.encode(message.getSenderFace()));
                } else {
                    //隐藏头像
                    headIV.setVisibility(View.GONE);
                }
                if (initMode.getVisitorScheme().getShowStaffNick() == 1) {
                    //显示昵称
                    nameTv.setVisibility(View.VISIBLE);
                    headIV.setImageUrl(CommonUtils.encode(message.getSenderFace()));
                } else {
                    //隐藏昵称
                    nameTv.setVisibility(View.GONE);
                }
            }
            List<SobotChatCustomGoods> list = new ArrayList<>();

            if (customCard.getCustomCards().size() > 3) {
                if (ll_expand != null) {
                    //显示查看更多
                    ll_expand.setVisibility(View.VISIBLE);
                    ll_expand.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //显示对话框
                            Intent intent = new Intent(mContext, SobotAiCardMoreActivity.class);
                            intent.putExtra("isHistoy", (message.getSugguestionsFontColor() == 1));
                            intent.putExtra("customCard", customCard);
                            intent.putExtra("title", customCard.getCardGuide());
                            mContext.startActivity(intent);
                        }
                    });
                }
                list = customCard.getCustomCards().subList(0, 3);
            } else {
                list.addAll(customCard.getCustomCards());
                if (ll_expand != null) {
                    ll_expand.setVisibility(View.GONE);
                }
            }
            SobotAiCardAdapter aiCardAdapter = new SobotAiCardAdapter(mContext, list, isRight,  message.getSugguestionsFontColor() == 1);
            aiCardAdapter.setOnItemClickListener(new SobotAiCardAdapter.OnItemListener() {
                @Override
                public void onSendClick(String menuName, SobotChatCustomGoods goods) {
                    SobotChatCustomCard showCustomCard = new SobotChatCustomCard();
                    showCustomCard.setCustomCards(customCard.customCards);
                    showCustomCard.setTicketPartnerField(customCard.getTicketPartnerField());
                    showCustomCard.setOriginalInfo(customCard.getOriginalInfo());
                    showCustomCard.setCardDesc(customCard.getCardDesc());
                    showCustomCard.setCardId(customCard.getCardId());
                    showCustomCard.setCardGuide(customCard.getCardGuide());
                    showCustomCard.setCardStyle(customCard.getCardStyle());
                    showCustomCard.setCardType(customCard.getCardType());
                    showCustomCard.setCardImg(customCard.getCardImg());
                    showCustomCard.setCustomField(customCard.getCustomField());
                    showCustomCard.setHistory(customCard.isHistory());
                    showCustomCard.setInterfaceInfo(customCard.getInterfaceInfo());
                    showCustomCard.setCustomCardLink(customCard.getCustomCardLink());
                    showCustomCard.setCardLink(customCard.getCardLink());
                    showCustomCard.setCardMenus(customCard.getCardMenus());
                    showCustomCard.setIsCustomerIdentity(customCard.getIsCustomerIdentity());
                    showCustomCard.setNodeId(customCard.getNodeId());
                    showCustomCard.setProcessId(customCard.getProcessId());
                    showCustomCard.setShowCustomCardAllMode(customCard.isShowCustomCardAllMode());
                    showCustomCard.setCardForm(customCard.getCardForm());
                    //发送
                    Intent intent = new Intent();
                    intent.setAction(ZhiChiConstants.SOBOT_SEND_AI_CARD_MSG);
                    intent.putExtra("btnText", menuName);
                    intent.putExtra("SobotCustomGoods", goods);
                    intent.putExtra("SobotCustomCard", showCustomCard);
                    CommonUtils.sendLocalBroadcast(mContext, intent);
                }

                @Override
                public void onItemClick(String menuName, SobotChatCustomGoods goods) {

                }
            });

            goods_list.setAdapter(aiCardAdapter);
        }
        sobot_msg_content_ll.setOnClickListener(this);
        refreshReadStatus();
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
