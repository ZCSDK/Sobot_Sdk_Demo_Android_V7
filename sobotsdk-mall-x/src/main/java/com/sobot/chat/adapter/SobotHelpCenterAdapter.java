package com.sobot.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.StCategoryModel;
import com.sobot.chat.widget.SobotImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

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

        viewHolder.bindData(position, list.get(position));
        return convertView;
    }

    private static class ViewHolder {
        private Context mContext;
        private LinearLayout sobot_container;
        private RelativeLayout sobot_rl;
        private SobotImageView sobot_tv_icon;
        private TextView sobot_tv_title;
        private TextView sobot_tv_descripe;
        private int sobot_bg_default_pic;

        public ViewHolder(Context context, View view) {
            mContext = context;
            sobot_container = (LinearLayout) view.findViewById(R.id.sobot_container);
            sobot_rl = (RelativeLayout) view.findViewById(R.id.sobot_rl);
            sobot_tv_icon = (SobotImageView) view.findViewById(R.id.sobot_tv_icon);
            sobot_tv_title = (TextView) view.findViewById(R.id.sobot_tv_title);
            sobot_tv_descripe = (TextView) view.findViewById(R.id.sobot_tv_descripe);
            sobot_bg_default_pic = R.drawable.sobot_bg_default_pic_img;
        }

        public void bindData(int position, StCategoryModel data) {
            sobot_rl.setSelected(position % 2 == 0);
            SobotBitmapUtil.display(mContext, data.getCategoryUrl(), sobot_tv_icon, sobot_bg_default_pic, sobot_bg_default_pic);
            sobot_tv_title.setText(data.getCategoryName());
            sobot_tv_descripe.setText(data.getCategoryDetail());
        }
    }
}