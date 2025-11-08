package com.sobot.chat.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.sobot.chat.api.model.FormNodeInfo;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;


/**
 *
 */
public class SobotFromSearchAdapter extends RecyclerView.Adapter {
    private List<FormNodeInfo> list;
    private Activity mActivity;
    private SobotListener listener;
    private String select = "";
    public SobotFromSearchAdapter(Activity context, List<FormNodeInfo> list,  SobotListener listener){
        this.mActivity = context;
        this.list = list;
        this.listener = listener;
    }
    public void setDate(String select) {
        this.select = select;
        notifyDataSetChanged();
    }
    public List<FormNodeInfo> getList() {
        return list;
    }
    public void setList(List<FormNodeInfo> date){
        list.clear();
        list.addAll(date);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mActivity).inflate(R.layout.sobot_item_cusfield_listview, viewGroup, false);
        return  new MyViewHolder(mActivity,v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final FormNodeInfo checkin = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if(checkin!=null){
            String name = checkin.getName();
            SpannableString spannableString = new SpannableString(name);
            if (name.contains(select)) {
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#09AEB0")), name.indexOf(select), name.indexOf(select) + select.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            vh.categorySmallTitle.setText(spannableString);
            if(checkin.isChecked()){
                vh.categorySmallIshave.setVisibility(View.VISIBLE);
                if (ThemeUtils.isChangedThemeColor(mActivity)) {
                    int themeColor = ThemeUtils.getThemeColor(mActivity);
                    Drawable bg = mActivity.getResources().getDrawable(R.drawable.sobot_icon_item_selected);
                    if (bg != null) {
                        vh.categorySmallIshave.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
                vh.categorySmallTitle.setTypeface(null, Typeface.BOLD);
            }else{
                vh.categorySmallTitle.setTypeface(null, Typeface.NORMAL);
                vh.categorySmallIshave.setVisibility(View.GONE);
            }
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkin.setChecked(true);
                    notifyDataSetChanged();
                    if(listener!=null){
                        listener.select(checkin);
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
        private TextView categorySmallTitle;
        private ImageView categorySmallIshave;
        private Activity mActivity;

        public MyViewHolder(Activity activity,@NonNull View itemView) {
            super(itemView);
            this.mActivity = activity;
            categorySmallTitle = (TextView) itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_title);
            categorySmallIshave = (ImageView) itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_ishave);

        }
    }
    public interface  SobotListener {
        void select(FormNodeInfo model);
    }
}
