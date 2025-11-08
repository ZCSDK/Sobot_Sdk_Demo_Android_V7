package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

public class SobotRobotListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private  List<SobotRobot> list;
    private RobotItemOnClick itemOnClick;
    private int selectPosition = -1;
    private int themeColor ;
    public SobotRobotListAdapter(Context context, List<SobotRobot> list ,int mRobotFlag, RobotItemOnClick listener){
        this.mContext = context;
        this.list = list;
        this.itemOnClick = listener;
        this.selectPosition = mRobotFlag;
        themeColor = ThemeUtils.getThemeColor(mContext);
    }

    public List<SobotRobot> getList() {
        return list;
    }
    public void setList(List<SobotRobot> date){
        list.clear();
        list.addAll(date);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_robot, viewGroup, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotRobot data = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if(data!=null){

            if(selectPosition!=-1 && data.getRobotFlag()==selectPosition){
                Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_item_hot_press, null);
                if (drawable instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable;
                    gradientDrawable.setStroke(ScreenUtils.dip2px(mContext, 1), ThemeUtils.getThemeColor(mContext)); // 1dp边框，主题颜色
                }
                vh.sobot_ll_content.setBackground(drawable);
                if (ThemeUtils.isChangedThemeColor(mContext)) {
                    Drawable bgg = mContext.getResources().getDrawable(R.drawable.sobot_icon_item_selected);
                    if (bgg != null) {
                        vh.iv_select.setImageDrawable(ThemeUtils.applyColorToDrawable(bgg, themeColor));
                    }
                }
                vh.iv_select.setVisibility(View.VISIBLE);
                vh.sobot_tv_content.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                vh.sobot_ll_content.setBackgroundResource(R.drawable.sobot_bg_dialog_item);
                vh.iv_select.setVisibility(View.GONE);
            }
            vh.sobot_tv_content.setText(data.getOperationRemark());
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyDataSetChanged();
                    if(itemOnClick!=null){
                        itemOnClick.onItemClick(data);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
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
    public interface RobotItemOnClick{
        void onItemClick(SobotRobot itemBeen);
    }

}