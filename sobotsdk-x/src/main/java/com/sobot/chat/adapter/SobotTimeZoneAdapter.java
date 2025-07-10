package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.utils.SobotStringUtils;

import java.util.List;


/**
 * 时区选中
 */
public class SobotTimeZoneAdapter extends RecyclerView.Adapter {
    private List<SobotTimezone> list;
    private Context mContext;
    private SobotTimezoneListener listener;
    private SobotTimezone chatStatus;
    private String searchText;
    public SobotTimeZoneAdapter(Context context, List<SobotTimezone> list, SobotTimezone chatStatus, SobotTimezoneListener listener){
        this.mContext = context;
        this.list = list;
        this.listener = listener;
        this.chatStatus=chatStatus;
    }

    public List<SobotTimezone> getList() {
        return list;
    }
    public void setList(List<SobotTimezone> date,String searchText){
        list.clear();
        list.addAll(date);
        this.searchText = searchText;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_activity_cusfield_listview_items, viewGroup, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotTimezone checkin = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if(checkin!=null){
            String data = checkin.getTimezoneValue();
            if(SobotStringUtils.isNoEmpty(data)){
                SpannableString spannableString = new SpannableString(data);
                if(SobotStringUtils.isNoEmpty(searchText)) {
                    if ( data.toLowerCase().contains(searchText.toLowerCase()) ) {
                        int index = data.toLowerCase().indexOf(searchText.toLowerCase());
                        if(index>=0) {
                            spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                vh.tv_title.setText(spannableString);
            }else {
                vh.tv_title.setText("");
            }

            if(null!= chatStatus && chatStatus.getTimezoneId().equals(checkin.getTimezoneId())){
                vh.iv_img.setVisibility(View.VISIBLE);
                if (ThemeUtils.isChangedThemeColor(mContext)) {
                    int themeColor = ThemeUtils.getThemeColor(mContext);
                    Drawable bg = mContext.getResources().getDrawable(R.drawable.sobot_cur_selected);
                    if (bg != null) {
                        vh.iv_img.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
            }else{
                vh.iv_img.setVisibility(View.GONE);
            }
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatStatus = checkin;
                    notifyDataSetChanged();
                    if(listener!=null){
                        listener.selectStatus(checkin);
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
        private ImageView iv_img;
        private TextView tv_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_title);
            iv_img = itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_ishave);

        }
    }
    public interface  SobotTimezoneListener {
        void selectStatus(SobotTimezone model);
    }
}
