package com.sobot.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
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
 * 大模型卡片
 */
public class SobotAiCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SobotChatCustomGoods> mData;
    private Context context;
    private boolean isRight;
    private boolean isHistory;
    private int themeColor;


    public SobotAiCardAdapter(Context context, List<SobotChatCustomGoods> list, boolean isRight, boolean isHistory) {
        this.context = context;
        themeColor = ThemeUtils.getThemeColor(context);
        this.isRight = isRight;
        this.isHistory = isHistory;
        if (mData == null) {
            mData = new ArrayList<>();
        } else {
            mData.clear();
        }
        this.mData.addAll(list);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_ai_card, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int index) {
        final SobotChatCustomGoods customGoods = mData.get(index);
        final MyHolder holder = (MyHolder) viewHolder;
        if (customGoods != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isRight && !isHistory && OnItemListener != null) {
                        if (customGoods.getCustomCardType() == 0) {
                            //发送
                            OnItemListener.onSendClick("", customGoods);
                        } else if (customGoods.getCustomCardType() == 1) {
                            //打开连接
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
                    }
                }
            });
            if (customGoods.getCustomCardHeader() != null && customGoods.getCustomCardHeader().size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (String key :
                        customGoods.getCustomCardHeader().keySet()) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append(key);
                    stringBuilder.append(":");
                    stringBuilder.append(customGoods.getCustomCardHeader().get(key));
                }
                holder.tv_head.setText(stringBuilder.toString());
                holder.tv_head.setVisibility(View.VISIBLE);
            } else {
                holder.tv_head.setVisibility(View.GONE);
            }
            if (StringUtils.isNoEmpty(customGoods.getCustomCardName())) {
                holder.sobot_goods_title.setText(customGoods.getCustomCardName());
                holder.sobot_goods_title.setVisibility(View.VISIBLE);
            } else {
                holder.sobot_goods_title.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(customGoods.getCustomCardThumbnail())) {
                SobotBitmapUtil.display(context, CommonUtils.encode(customGoods.getCustomCardThumbnail())
                        , holder.sobot_goods_pic);
                holder.sobot_goods_pic.setVisibility(View.VISIBLE);
            } else {
                holder.sobot_goods_pic.setVisibility(View.GONE);
            }
            if (StringUtils.isNoEmpty(customGoods.getCustomCardDesc())) {
                holder.sobot_goods_des.setText(customGoods.getCustomCardDesc());
                holder.sobot_goods_des.setVisibility(View.VISIBLE);
            } else {
                holder.sobot_goods_des.setVisibility(View.GONE);
            }
            if (StringUtils.isNoEmpty(customGoods.getCustomCardNum())) {
                holder.sobot_count.setText("X" + customGoods.getCustomCardNum());
                holder.sobot_count.setVisibility(View.VISIBLE);
            } else {
                holder.sobot_count.setVisibility(View.GONE);
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
                holder.sobot_price.setVisibility(View.VISIBLE);
                holder.sobot_price.setText(price);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = holder.sobot_price.getLineCount();
                        if (lineCount > 1) {
                            if (holder.sobot_count.getVisibility() == View.VISIBLE) {
                                String s1 = holder.sobot_count.getText().toString();
                                String s2 = holder.sobot_price.getText().toString();
                                holder.sobot_count.setText(s1 + "\n" + s2);
                            }
                            holder.sobot_price.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                holder.sobot_price.setVisibility(View.GONE);
            }
            //自定义
            if (customGoods.getCustomField() != null && customGoods.getCustomField().size() > 0) {
                holder.cusrLayout.setColumnCount(2);
                holder.cusrLayout.setRowCount(customGoods.getCustomField().size());
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
                    holder.cusrLayout.addView(title);

                    TextView value = new TextView(context);
                    value.setMaxLines(3);
                    value.setTextColor(context.getResources().getColor(R.color.sobot_color_text_first));
                    value.setTextSize(12);
                    value.setPadding(0, 0, 0, 0);
                    value.setText(customGoods.getCustomField().get(key));
                    holder.cusrLayout.addView(value);
                }
                holder.cusrLayout.setVisibility(View.VISIBLE);
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.cusrLayout.setVisibility(View.GONE);
                holder.line.setVisibility(View.GONE);
            }

            if (!isRight && holder.sobot_ll_btns != null) {
                holder.sobot_ll_btns.setVisibility(View.VISIBLE);
                if (customGoods.getCustomMenus() != null && customGoods.getCustomMenus().size() > 0) {
                    List<SobotChatCustomMenu> menusList = customGoods.getCustomMenus();
                    for (int i = 0; i < menusList.size(); i++) {
                        final SobotChatCustomMenu menu = menusList.get(i);
                        if (menu != null) {
                            View view = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_text_btn, null);
                            TextView answer = view.findViewById(R.id.sobot_tv_name);
                            answer.setText(menu.getMenuName());
                            answer.setTextColor(themeColor);
                            if (isHistory) {
                                answer.setAlpha(0.5f);
                            } else {
                                answer.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (menu.getMenuType() == 0) {
                                            //发送
                                            if (OnItemListener != null) {
                                                OnItemListener.onSendClick(menu.getMenuName(), customGoods);
                                            }
                                        } else {
                                            if (OnItemListener != null) {
                                                OnItemListener.onItemClick(menu.getMenuName(), customGoods);
                                            }
                                            //自定义
                                            if (StringUtils.isNoEmpty(menu.getMenuLink())) {
                                                Intent intent = new Intent(context, WebViewActivity.class);
                                                intent.putExtra("url", menu.getMenuLink());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(intent);
                                            }
                                        }
                                    }
                                });
                            }
                            holder.sobot_ll_btns.addView(view);
                        }
                    }
                }
            } else {
                if (holder.cusrLayout.getVisibility() == View.VISIBLE) {
                    holder.sobot_ll_btns.setVisibility(View.INVISIBLE);
                } else {
                    holder.sobot_ll_btns.setVisibility(View.GONE);
                }
            }
            if (holder.cusrLayout.getVisibility() == View.GONE && holder.sobot_ll_btns.getVisibility() == View.GONE) {
                holder.line.setVisibility(View.INVISIBLE);
            }
        }
    }

    // 手动添加长按事件
    public interface OnItemListener {
        void onSendClick(String menuName, SobotChatCustomGoods goods);

        void onItemClick(String menuName, SobotChatCustomGoods goods);
    }

    private OnItemListener OnItemListener = null;

    public void setOnItemClickListener(OnItemListener listener) {
        OnItemListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private ImageView sobot_goods_pic;
        private TextView tv_head;
        private TextView sobot_goods_title;
        private TextView sobot_goods_des;
        private TextView sobot_count;
        private TextView sobot_price;
        private LinearLayout sobot_ll_btns;
        private View line;
        private GridLayout cusrLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            sobot_goods_pic = itemView.findViewById(R.id.sobot_goods_pic);
            tv_head = itemView.findViewById(R.id.tv_head);
            sobot_ll_btns = itemView.findViewById(R.id.sobot_ll_btns);
            sobot_goods_title = itemView.findViewById(R.id.sobot_goods_title);
            sobot_goods_des = itemView.findViewById(R.id.sobot_goods_des);
            sobot_count = itemView.findViewById(R.id.sobot_count);
            sobot_price = itemView.findViewById(R.id.sobot_price);
            line = itemView.findViewById(R.id.v_line_bottom);
            cusrLayout = itemView.findViewById(R.id.curs_grid_layout);
        }
    }

}
