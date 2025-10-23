
package com.sobot.chat.widget.switchkeyboardlib.panel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.widget.emoji.EmojiconNew;

import java.util.ArrayList;
import java.util.List;

//加号 表情面板 适配器
public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {
    private List<EmojiconNew> emojiList;
    private Context mContext;

    public EmojiAdapter(Context context, List<EmojiconNew> emojiList) {
        this.mContext = context;
        this.emojiList = emojiList;
        if (this.emojiList == null) {
            this.emojiList = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_item_emoticon, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        EmojiconNew emojicon = emojiList.get(position);
        holder.textView.setText(emojicon.getEmojiCode()); // 显示表情符号

        // 添加点击回调
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiClickListener != null) {
                    emojiClickListener.onEmojiClick(emojicon);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (this.emojiList != null) {
            return emojiList.size();
        } else {
            return 0;
        }
    }

    // 添加表情点击回调接口
    public interface OnEmojiClickListener {
        void onEmojiClick(EmojiconNew emoji);
    }

    private OnEmojiClickListener emojiClickListener;

    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.emojiClickListener = listener;
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public EmojiViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.sobot_tv_emoticon);
        }
    }
}
