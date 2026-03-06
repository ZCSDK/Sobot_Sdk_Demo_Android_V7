package com.sobot.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotAiLinkInfo;
import com.sobot.chat.utils.StringUtils;

import java.util.List;

public class SobotAIFromListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<SobotAiLinkInfo> list;
    private RobotItemOnClick itemOnClick;

    public SobotAIFromListAdapter(Context context, List<SobotAiLinkInfo> list, RobotItemOnClick listener) {
        this.mContext = context;
        this.list = list;
        this.itemOnClick = listener;
    }

    public List<SobotAiLinkInfo> getList() {
        return list;
    }

    public void setList(List<SobotAiLinkInfo> date) {
        list.clear();
        list.addAll(date);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_ai_from, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotAiLinkInfo data = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if (data != null) {
            if (StringUtils.isNoEmpty(data.getReferenceTitle())) {
                vh.sobot_tv_content.setText(StringUtils.checkStringIsNull(data.getReferenceTitle()));
            } else {
                vh.sobot_tv_content.setText(StringUtils.checkStringIsNull(data.getReferenceUrl()));
            }

            if (StringUtils.isNoEmpty(data.getSectionTypeEnum())) {
                int drawableResId = getDrawableResourceId(data.getSectionTypeEnum());
                vh.iv_select.setImageResource(drawableResId);
            }
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemOnClick != null) {
                        itemOnClick.onItemClick(data);
                    }
                }
            });
        }
    }

    private int getDrawableResourceId(String type) {
        if (TextUtils.isEmpty(type)) {
            return 0; // 类型为空时返回默认图标ID
        }

        String lowerCaseType = type.toLowerCase();
        if (lowerCaseType.contains("web")) {
            return R.drawable.sobot_icon_goto_web; // 圆形 网页链接图标
        } else if (lowerCaseType.contains("file")) {
            return R.drawable.sobot_icon_goto_file; // 文件图标
        } else {
            return R.drawable.sobot_icon_goto_web; // 圆形 网页链接图标
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
        void onItemClick(SobotAiLinkInfo itemBeen);
    }

}