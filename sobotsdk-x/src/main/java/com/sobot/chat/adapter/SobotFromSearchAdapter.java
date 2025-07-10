package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.List;


/**
 *
 */
public class SobotFromSearchAdapter extends RecyclerView.Adapter {
    private List<FormNodeInfo> list;
    private Context mContext;
    private SobotListener listener;
    private FormNodeInfo chatStatus;
    private String select = "";
    public SobotFromSearchAdapter(Context context, List<FormNodeInfo> list, FormNodeInfo chatStatus, SobotListener listener){
        this.mContext = context;
        this.list = list;
        this.listener = listener;
        this.chatStatus=chatStatus;
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
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_from_search, viewGroup, false);
        return  new MyViewHolder(v);
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
            vh.tv_title.setText(spannableString);
            if(null!= chatStatus && chatStatus.getId().equals(checkin.getId())){
                vh.iv_img.setVisibility(View.VISIBLE);
                vh.tv_title.setTypeface(null, Typeface.BOLD);
            }else{
                vh.tv_title.setTypeface(null, Typeface.NORMAL);
                vh.iv_img.setVisibility(View.GONE);
            }
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatStatus = checkin;
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
        private ImageView iv_img;
        private TextView tv_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            iv_img = itemView.findViewById(R.id.iv_img);

        }
    }
    public interface  SobotListener {
        void select(FormNodeInfo model);
    }
}
