package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.RegionModel;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

/**
 * @author: Sobot
 * 通话记录
 * 2022/7/12
 */
public class SobotSearchRegionAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private List<RegionModel> mData;
    private OnItemClickListener itemListener;
    private String select ="",tempCode="";

    public SobotSearchRegionAdapter(Context mContext, List<RegionModel> mData, OnItemClickListener itemListener) {
        this.mContext = mContext;
        this.mData = mData;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_select_region, viewGroup, false);
        RecyclerView.ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    public void setDate(String select) {
        this.select = select;
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int i) {
        ViewHolder viewHolder = (ViewHolder) vh;
        final RegionModel info = mData.get(i);

        String text = info.getProvince();
        String code=info.getProvinceCode();
        if(!TextUtils.isEmpty(info.getCity())){
            text+="/"+info.getCity();
            code+=info.getCityCode();
        }
        if(!TextUtils.isEmpty(info.getArea())){
            text+="/"+info.getArea();
            code+=info.getAreaCode();
        }
        if(!TextUtils.isEmpty(info.getStreet())){
            text+="/"+info.getStreet();
            code+=info.getStreetCode();
        }
        if(tempCode.equals(code)){
            if (ThemeUtils.isChangedThemeColor(mContext)) {
                int themeColor = ThemeUtils.getThemeColor(mContext);
                Drawable bg = mContext.getResources().getDrawable(R.drawable.sobot_cur_selected);
                if (bg != null) {
                    viewHolder.iv_exts.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                }
            }
            viewHolder.iv_exts.setVisibility(View.VISIBLE);
        }else{
            viewHolder.iv_exts.setVisibility(View.GONE);
        }
        SpannableString spannableString = new SpannableString(text);
        if(text.contains(select)){
            spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), text.lastIndexOf(select),text.lastIndexOf(select)+select.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        viewHolder.tv_ext.setText(spannableString);
        viewHolder.tv_selected.setVisibility(View.GONE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempCode = info.getProvinceCode();
                if(!TextUtils.isEmpty(info.getCity())){
                    tempCode+=info.getCityCode();
                }
                if(!TextUtils.isEmpty(info.getArea())){
                    tempCode+=info.getAreaCode();
                }
                if(!TextUtils.isEmpty(info.getStreet())){
                    tempCode+=info.getStreetCode();
                }
                notifyDataSetChanged();
                if (itemListener != null) {
                    itemListener.onItemClick(info);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_exts;
        private TextView tv_ext;
        private TextView tv_selected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_ext = itemView.findViewById(R.id.tv_ext);
            iv_exts = itemView.findViewById(R.id.iv_exts);
            tv_selected = itemView.findViewById(R.id.tv_selected);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RegionModel record);
    }

}
