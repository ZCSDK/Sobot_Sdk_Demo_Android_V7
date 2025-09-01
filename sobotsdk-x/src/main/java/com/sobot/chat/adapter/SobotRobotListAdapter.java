package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotRobot;
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_dialog_list_item, viewGroup, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotRobot data = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if(data!=null){
            if(selectPosition!=-1 && data.getRobotFlag()==selectPosition){
                if (ThemeUtils.isChangedThemeColor(mContext)) {
                    Drawable bg = mContext.getResources().getDrawable(R.drawable.sobot_cur_selected);
                    if (bg != null) {
                        vh.iv_select.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
                vh.iv_select.setVisibility(View.VISIBLE);
            } else {
                vh.iv_select.setVisibility(View.GONE);
            }
            vh.sobot_tv_content.setText(data.getOperationRemark());
//            if(null!= chatStatus && chatStatus.getTimezoneId().equals(checkin.getTimezoneId())){
//                vh.sobot_tv_content_detail.setVisibility(View.VISIBLE);
//                vh.sobot_tv_content_detail.setText();
//            }else{
//                vh.sobot_tv_content_detail.setVisibility(View.GONE);
//            }
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
        private TextView sobot_tv_content_detail;
        private ImageView iv_select;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            sobot_tv_content = itemView.findViewById(R.id.sobot_tv_content);
            sobot_tv_content_detail = itemView.findViewById(R.id.sobot_tv_content_detail);
            iv_select = itemView.findViewById(R.id.iv_select);
        }
    }
    public interface RobotItemOnClick{
        void onItemClick(SobotRobot itemBeen);
    }

}