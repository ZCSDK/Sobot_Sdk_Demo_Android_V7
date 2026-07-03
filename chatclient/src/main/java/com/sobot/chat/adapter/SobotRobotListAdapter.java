package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

public class SobotRobotListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<SobotRobot> list;
    private RobotItemOnClick itemOnClick;
    private int selectPosition = -1;
    private int themeColor;

    public SobotRobotListAdapter(Context context, List<SobotRobot> list, int mRobotFlag, RobotItemOnClick listener) {
        this.mContext = context;
        this.list = list;
        this.itemOnClick = listener;
        this.selectPosition = mRobotFlag;
        themeColor = ThemeUtils.getThemeColor(mContext);
    }

    public List<SobotRobot> getList() {
        return list;
    }

    public void setList(List<SobotRobot> date) {
        list.clear();
        list.addAll(date);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_robot, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (i >= list.size()) {
            return;
        }

        final SobotRobot data = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if (data != null) {
            final boolean isSelected = selectPosition != -1 && data.getRobotFlag() == selectPosition;
            if (isSelected) {
                final GradientDrawable normalDrawable = new GradientDrawable();
                int radius = ScreenUtils.dip2px(mContext, 8f);
                normalDrawable.setColor(mContext.getResources().getColor(R.color.sobot_item_hot_bg));
                normalDrawable.setStroke(ScreenUtils.dip2px(mContext, 1), themeColor);
                normalDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
                vh.sobot_ll_content.setBackground(normalDrawable);

                if (ThemeUtils.isChangedThemeColor(mContext)) {
                    Drawable bgg = mContext.getResources().getDrawable(R.drawable.sobot_icon_item_selected);
                    if (bgg != null) {
                        vh.iv_select.setImageDrawable(ThemeUtils.applyColorToDrawable(bgg, themeColor));
                    }
                }
                vh.iv_select.setVisibility(View.VISIBLE);
                vh.sobot_tv_content.setTypeface(Typeface.DEFAULT_BOLD);

                vh.itemView.setOnTouchListener(new View.OnTouchListener() {
                    private boolean isPressed = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (!isPressed) {
                                    isPressed = true;
                                    GradientDrawable pressedDrawable = new GradientDrawable();
                                    pressedDrawable.setColor(Color.argb(31, Color.red(themeColor), Color.green(themeColor), Color.blue(themeColor)));
                                    pressedDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
                                    GradientDrawable borderDrawable = new GradientDrawable();
                                    borderDrawable.setColor(Color.TRANSPARENT);
                                    borderDrawable.setStroke(ScreenUtils.dip2px(mContext, 1), themeColor);
                                    borderDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
                                    Drawable[] layers = new Drawable[]{borderDrawable, pressedDrawable};
                                    LayerDrawable layerDrawable = new LayerDrawable(layers);
                                    vh.sobot_ll_content.setBackground(layerDrawable);
                                }
                                return true;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_OUTSIDE:
                                if (isPressed) {
                                    isPressed = false;
                                    GradientDrawable restoreDrawable = new GradientDrawable();
                                    restoreDrawable.setColor(mContext.getResources().getColor(R.color.sobot_item_hot_bg));
                                    restoreDrawable.setStroke(ScreenUtils.dip2px(mContext, 1), themeColor);
                                    restoreDrawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
                                    vh.sobot_ll_content.setBackground(restoreDrawable);
                                }
                                return true;
                        }
                        return false;
                    }
                });

            } else {
                vh.sobot_ll_content.setBackgroundResource(R.drawable.sobot_bg_dialog_item);
                if (ChatUtils.isRtl(mContext)) {
                    vh.iv_select.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                } else {
                    vh.iv_select.setImageResource(R.drawable.sobot_icon_right_arrow);
                }
                vh.iv_select.setVisibility(View.VISIBLE);
            }
            vh.sobot_tv_content.setText(data.getOperationRemark());

            if (!isSelected) {
                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyDataSetChanged();
                        if (itemOnClick != null) {
                            itemOnClick.onItemClick(data);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView sobot_tv_content;
        private LinearLayout sobot_ll_content;
        private ImageView iv_select;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            sobot_tv_content = itemView.findViewById(R.id.sobot_tv_content);
            sobot_ll_content = itemView.findViewById(R.id.sobot_ll_content);
            iv_select = itemView.findViewById(R.id.iv_select);
        }
    }

    public interface RobotItemOnClick {
        void onItemClick(SobotRobot itemBeen);
    }

}