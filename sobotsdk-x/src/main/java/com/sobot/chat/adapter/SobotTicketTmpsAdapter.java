package com.sobot.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotPostMsgTemplate;

import java.util.List;

/**
 * 留言模板适配器
 */
public class SobotTicketTmpsAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private  List<SobotPostMsgTemplate> list;
    private ItemOnClick itemOnClick;
    public SobotTicketTmpsAdapter(Context context, List<SobotPostMsgTemplate> list , ItemOnClick listener){
        this.mContext = context;
        this.list = list;
        this.itemOnClick = listener;
    }

    public List<SobotPostMsgTemplate> getList() {
        return list;
    }
    public void setList(List<SobotPostMsgTemplate> date){
        list.clear();
        list.addAll(date);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_ticket_template, viewGroup, false);
        return  new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotPostMsgTemplate data = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if(data!=null){
            vh.sobot_tv_content.setText(data.getTemplateName());
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            sobot_tv_content = itemView.findViewById(R.id.sobot_tv_content);
            sobot_tv_content_detail = itemView.findViewById(R.id.sobot_tv_content_detail);

        }
    }
    public interface ItemOnClick {
        void onItemClick(SobotPostMsgTemplate itemBeen);
    }

}