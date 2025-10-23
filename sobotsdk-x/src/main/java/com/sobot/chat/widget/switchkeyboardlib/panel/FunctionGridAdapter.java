package com.sobot.chat.widget.switchkeyboardlib.panel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.switchkeyboardlib.model.SobotPlusEntity;

import java.util.List;

/**
 * 点击加号后 功能菜单 网格布局 项适配器
 */
public class FunctionGridAdapter extends RecyclerView.Adapter<FunctionGridAdapter.FunctionItemViewHolder> {
    private List<SobotPlusEntity> items;
    private FunctionMenuPageView.OnFunctionItemClickListener itemClickListener;
    private Context mContext;

    public FunctionGridAdapter(Context context, List<SobotPlusEntity> items, FunctionMenuPageView.OnFunctionItemClickListener listener) {
        this.mContext = context;
        this.items = items;
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public FunctionItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_plus_menu, parent, false);
        return new FunctionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionItemViewHolder holder, int position) {
        SobotPlusEntity item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FunctionItemViewHolder extends RecyclerView.ViewHolder {
        SobotProgressImageView iconView;
        TextView textView;

        FunctionItemViewHolder(@NonNull View itemView) {
            super(itemView);
            LinearLayout container = (LinearLayout) itemView;
            iconView = itemView.findViewById(R.id.sobot_plus_menu_icon);
            textView = itemView.findViewById(R.id.sobot_plus_menu);
        }

        void bind(SobotPlusEntity item, int position) {
            if (item != null) {
                textView.setText(item.name);
                if (item.iconResId != 0) {
                    iconView.setImageLocal(item.iconResId);
                } else {
                    iconView.setImageUrl(item.iconUrl);
                }
                // 设置点击事件
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) {
                        itemClickListener.onFunctionItemClick(item, position);
                    }
                });
            }
        }
    }
}
