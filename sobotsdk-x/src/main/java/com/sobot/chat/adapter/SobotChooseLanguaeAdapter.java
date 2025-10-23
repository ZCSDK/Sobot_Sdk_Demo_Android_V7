package com.sobot.chat.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotlanguaeModel;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.List;
import java.util.Locale;


/**
 * 选择语言列表
 */
public class SobotChooseLanguaeAdapter extends RecyclerView.Adapter {
    private List<SobotlanguaeModel> list;
    private Context mContext;
    private SobotChooseLanguaeListener listener;
    private String searchText;

    public SobotChooseLanguaeAdapter(Context context, List<SobotlanguaeModel> list, SobotChooseLanguaeListener listener) {
        this.mContext = context;
        this.list = list;
        this.listener = listener;
    }

    public List<SobotlanguaeModel> getList() {
        return list;
    }

    public void setList(List<SobotlanguaeModel> date,String searchText) {
        list.clear();
        list.addAll(date);
        this.searchText = searchText;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.sobot_item_choose_languae, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final SobotlanguaeModel model = list.get(i);
        MyViewHolder vh = (MyViewHolder) viewHolder;
        if (model != null) {
            String data = model.getName();
            if(StringUtils.isNoEmpty(data)){
                SpannableString spannableString = new SpannableString(data);
                if(StringUtils.isNoEmpty(searchText)) {
                    if (data.toLowerCase().contains(searchText.toLowerCase())|| data.contains(searchText.toLowerCase()) ) {
                        int index = data.toLowerCase().indexOf(searchText);
                        if(index<0){
                            index = data.toLowerCase().indexOf(searchText.toLowerCase());
                        }
                        if(index<0){
                            index = data.toLowerCase().indexOf(searchText.toUpperCase());
                        }
                        if(index>=0) {
                            spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                vh.tv_title.setText(spannableString);
            }else {
                vh.tv_title.setText("");
            }
            if (mContext != null) {
                Locale language = (Locale) SharedPreferencesUtil.getObject(mContext, ZhiChiConstant.SOBOT_LANGUAGE);
                if (language != null) {
                    if ("ar".equals(language.getLanguage())) {
                        vh.tv_title.setTextDirection(View.TEXT_DIRECTION_RTL);
                    } else {
                        vh.tv_title.setTextDirection(View.TEXT_DIRECTION_LTR);
                    }
                }
            }
            vh.tv_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FastClickUtils.isCanClick()) {
                        if (listener != null) {
                            listener.selectLanguae(model);
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_title;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);

        }
    }

    public interface SobotChooseLanguaeListener {
        void selectLanguae(SobotlanguaeModel model);
    }
}
