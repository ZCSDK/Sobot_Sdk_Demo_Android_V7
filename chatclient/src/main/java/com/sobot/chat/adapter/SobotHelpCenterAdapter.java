package com.sobot.chat.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.StCategoryModel;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.util.List;

public class SobotHelpCenterAdapter extends SobotBaseAdapter<StCategoryModel> {
    private LayoutInflater mInflater;

    public SobotHelpCenterAdapter(Context context, List<StCategoryModel> list) {
        super(context, list);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sobot_list_item_help_center, null);
            viewHolder = new ViewHolder(context, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (list != null)
            viewHolder.bindData(position, list.get(position));
        return convertView;
    }

    private static class ViewHolder {
        private Context mContext;
        private LinearLayout ll_title;
        private SobotProgressImageView sobot_tv_icon;
        private TextView sobot_tv_title;
        private TextView sobot_tv_descripe;

        public ViewHolder(Context context, View view) {
            mContext = context;
            ll_title = (LinearLayout) view.findViewById(R.id.ll_title);
            sobot_tv_icon = view.findViewById(R.id.sobot_tv_icon);
            sobot_tv_title = (TextView) view.findViewById(R.id.sobot_tv_title);
            sobot_tv_descripe = (TextView) view.findViewById(R.id.sobot_tv_descripe);
        }

        public void bindData(int position, StCategoryModel data) {
            if (data != null) {
                sobot_tv_icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                sobot_tv_icon.setImageUrl(data.getCategoryUrl());
                sobot_tv_title.setText(data.getCategoryName());
                sobot_tv_descripe.setText(data.getCategoryDetail());
                if (StringUtils.calculateTextLinesWithSpacing(14, data.getCategoryName(), ScreenUtils.getScreenWidth(mContext) / 2 - ScreenUtils.dip2px(mContext, 32 + 32 + 8 + 10), 0, 0.8f, mContext) <= 2) {
                    ll_title.setGravity(Gravity.CENTER_VERTICAL);
                } else {
                    ll_title.setGravity(Gravity.TOP);
                }
            }
        }
    }
}