package com.sobot.chat.widget.horizontalscroll;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.BusinessLineRespVo;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.utils.SobotStringUtils;

import java.util.List;


/**
 * 常见问题 业务adapter
 */
public class IssueViewPagerdAdapter {
    private Context mContext;
    private LayoutInflater mInflate;
    private List<BusinessLineRespVo> mDatas;

    public IssueViewPagerdAdapter(Context context, List<BusinessLineRespVo> mDatas) {
        this.mContext = context;
        mInflate = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    public int getCount() {
        return mDatas.size();
    }

    public Object getItem(int positon) {
        return mDatas.get(positon);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflate.inflate(R.layout.sobot_chat_msg_item_issue_item, parent, false);
            viewHolder.mImg = convertView.findViewById(R.id.sobot_hot_item_icon);
            viewHolder.mText = (TextView) convertView.findViewById(R.id.sobot_hot_item_title);
            viewHolder.mTempText = (TextView) convertView.findViewById(R.id.sobot_hot_item_title_zhanwei);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (!TextUtils.isEmpty(mDatas.get(position).getTitleImgUrl())) {
            viewHolder.mImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
            viewHolder.mImg.setImageUrl(CommonUtils.encode(mDatas.get(position).getTitleImgUrl()));
        }
        viewHolder.mText.setText(SobotStringUtils.checkStringIsNull(mDatas.get(position).getBusinessLineName()));
        viewHolder.mTempText.setText(SobotStringUtils.checkStringIsNull(mDatas.get(position).getTempBusinessLineName()));
        return convertView;
    }

    private class ViewHolder {
        SobotProgressImageView mImg;
        TextView mText;
        TextView mTempText;
    }
}
