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
import com.sobot.chat.api.model.PlaceModel;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.dialog.DialogItemOnClick;

import java.util.List;

public class SobotRegionAdapter extends RecyclerView.Adapter {
    private List<PlaceModel> list;
    private Context mContext;
    private DialogItemOnClick listener;
    private String selectId ="";
    private int themeColor ;
    public SobotRegionAdapter(Context context, List<PlaceModel> list, DialogItemOnClick listener) {
        this.mContext = context;
        this.list = list;
        this.listener = listener;
        themeColor = ThemeUtils.getThemeColor(mContext);
    }

    public void setSelectId(String selectId) {
        this.selectId = selectId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_select_region, viewGroup, false);
        return new MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final int findIndex=i;
        final PlaceModel checkin = list.get(findIndex);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if (checkin != null) {
            vh.tv_ext.setText(checkin.getName());

            if(checkin.isHasChild()){
                vh.iv_exts.setImageResource(R.drawable.sobot_item_arrow);
                vh.iv_exts.setVisibility(View.VISIBLE);
                if (selectId.equals(checkin.getId())) {
                    if(themeColor!=0) {
                        vh.tv_selected.setTextColor(themeColor);
                    }
                    vh.tv_selected.setVisibility(View.VISIBLE);
                }else{
                    vh.tv_selected.setVisibility(View.GONE);
                }
            }else{
                vh.tv_selected.setVisibility(View.GONE);
                if (selectId.equals(checkin.getId())) {
                    if (ThemeUtils.isChangedThemeColor(mContext)) {
                        Drawable bg = mContext.getResources().getDrawable(R.drawable.sobot_cur_selected);
                        if (bg != null) {
                            vh.iv_exts.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                        }
                    }
                    vh.iv_exts.setVisibility(View.VISIBLE);
                } else {
                    vh.iv_exts.setVisibility(View.GONE);
                }
            }
            if(selectId.equals("")){

            }
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        selectId = checkin.getId();
                        listener.selectItem(findIndex);
                        notifyDataSetChanged();
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
        private ImageView iv_exts;
        private TextView tv_ext;
        private TextView tv_selected;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_ext = itemView.findViewById(R.id.tv_ext);
            iv_exts = itemView.findViewById(R.id.iv_exts);
            tv_selected = itemView.findViewById(R.id.tv_selected);
        }
    }
}