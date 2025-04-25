package com.sobot.chat.adapter;

import android.content.Context;
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
import com.sobot.chat.api.model.SobotPhoneCode;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.utils.SobotStringUtils;

import java.util.List;


/**
 * 手机区号
 */
public class SobotPhoneCodeAdapter extends RecyclerView.Adapter {
    private List<SobotPhoneCode> list;
    private Context mContext;
    private SobotItemListener listener;
    private SobotPhoneCode chatStatus;
    private String searchText;
    private int STYLE_TITLE_TEXT = 1;

    public SobotPhoneCodeAdapter(Context context, List<SobotPhoneCode> list, SobotPhoneCode chatStatus, SobotItemListener listener) {
        this.mContext = context;
        this.list = list;
        this.listener = listener;
        this.chatStatus = chatStatus;
    }

    public void setList(List<SobotPhoneCode> date, String searchText) {
        list.clear();
        list.addAll(date);
        this.searchText = searchText;
        notifyDataSetChanged();
    }

    public List<SobotPhoneCode> getList() {
        return list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == STYLE_TITLE_TEXT) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_phone_code_t, parent, false);
        } else {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_activity_cusfield_listview_items, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotPhoneCode checkin = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if (checkin != null) {
            if (SobotStringUtils.isNoEmpty(checkin.getPhone_code())) {
                String data = checkin.getPhone_code();
                if (SobotStringUtils.isNoEmpty(data)) {
                    SpannableString spannableString = new SpannableString(data);
                    if (SobotStringUtils.isNoEmpty(searchText)) {
                        if (data.toLowerCase().contains(searchText.toLowerCase())) {
                            int index = data.toLowerCase().indexOf(searchText.toLowerCase());
                            if (index >= 0) {
                                spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                    vh.tv_title.setText(spannableString);
                } else {
                    vh.tv_title.setText("");
                }
                if (null != chatStatus && chatStatus.getPhone_code().equals(checkin.getPhone_code())) {
                    vh.iv_img.setVisibility(View.VISIBLE);
                    if (ThemeUtils.isChangedThemeColor(mContext)) {
                        int themeColor = ThemeUtils.getThemeColor(mContext);
                        Drawable bg = mContext.getResources().getDrawable(R.drawable.sobot_cur_selected);
                        if (bg != null) {
                            vh.iv_img.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                        }
                    }
                } else {
                    vh.iv_img.setVisibility(View.GONE);
                }
                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chatStatus = checkin;
                        notifyDataSetChanged();
                        if (listener != null) {
                            listener.selectItem(checkin);
                        }
                    }
                });
            } else {
                vh.tv_title.setText(checkin.getPinyin());
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (SobotStringUtils.isEmpty(list.get(position).getPhone_code())) {
            return STYLE_TITLE_TEXT;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_img;
        private TextView tv_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_title);
            iv_img = itemView.findViewById(R.id.sobot_activity_cusfield_listview_items_ishave);

        }
    }

    public interface SobotItemListener {
        void selectItem(SobotPhoneCode model);
    }
}
