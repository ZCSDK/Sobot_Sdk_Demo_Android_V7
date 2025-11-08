package com.sobot.chat.widget.horizontalscroll;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.model.BusinessLineRespVo;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.List;


/**
 * 常见问题 业务adapter
 */
public class IssueViewPagerdAdapter {
    private Context mContext;
    private LayoutInflater mInflate;
    private List<BusinessLineRespVo> mDatas;
    private int selectIndex = 0;
    private int businessSetType = 0;//0 图文，1 仅图

    public IssueViewPagerdAdapter(Context context, List<BusinessLineRespVo> mDatas, int selectIndex,int businessSetType) {
        this.mContext = context;
        mInflate = LayoutInflater.from(context);
        this.mDatas = mDatas;
        this.selectIndex = selectIndex;
        this.businessSetType = businessSetType;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
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
            viewHolder.sobot_issue_text = convertView.findViewById(R.id.sobot_issue_text);
            viewHolder.line = convertView.findViewById(R.id.v_line);
            viewHolder.ll_issue = convertView.findViewById(R.id.ll_issue);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (ThemeUtils.isAppNightMode(mContext)) {
            if (!TextUtils.isEmpty(mDatas.get(position).getTitleDarkImgUrl())) {
                viewHolder.mImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                SobotBitmapUtil.display(mContext, CommonUtils.encode(mDatas.get(position).getTitleDarkImgUrl()),
                        viewHolder.mImg);
            } else if (!TextUtils.isEmpty(mDatas.get(position).getTitleImgUrl())) {
                viewHolder.mImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                SobotBitmapUtil.display(mContext, CommonUtils.encode(mDatas.get(position).getTitleImgUrl()),
                        viewHolder.mImg);
            }
        } else {
            if (!TextUtils.isEmpty(mDatas.get(position).getTitleImgUrl())) {
                viewHolder.mImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
                SobotBitmapUtil.display(mContext, CommonUtils.encode(mDatas.get(position).getTitleImgUrl()),
                        viewHolder.mImg);
            }
        }
        viewHolder.mText.setText(StringUtils.checkStringIsNull(mDatas.get(position).getBusinessLineName()));
        viewHolder.mTempText.setText(StringUtils.checkStringIsNull(mDatas.get(position).getTempBusinessLineName()));
        if(businessSetType==1){
            //仅图片
            viewHolder.sobot_issue_text.setVisibility(View.GONE);
            viewHolder.ll_issue.getLayoutParams().height= ScreenUtils.dip2px(mContext, 72);
            viewHolder.ll_issue.getLayoutParams().width= ScreenUtils.dip2px(mContext, 72);
        }else{
            viewHolder.sobot_issue_text.setVisibility(View.VISIBLE);

        }

        viewHolder.line.setVisibility(View.VISIBLE);
        if (position < mDatas.size()) {
            viewHolder.line.setVisibility(View.VISIBLE);
        } else {
            viewHolder.line.setVisibility(View.GONE);
        }
        try {
            if (selectIndex == position) {
                Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_item_hot_press, null);
                if (drawable instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable;
                    gradientDrawable.setStroke(ScreenUtils.dip2px(mContext, 1), ThemeUtils.getThemeColor(mContext)); // 1dp边框，主题颜色
                }
                viewHolder.ll_issue.setBackground(drawable);
            } else {
                // 可以设置默认背景
                viewHolder.ll_issue.setBackgroundResource(R.drawable.sobot_item_hot_default);
            }
        } catch (Resources.NotFoundException e) {
        }
        return convertView;
    }

    private class ViewHolder {
        LinearLayout ll_issue;
        SobotRCImageView mImg;
        TextView mText;
        TextView mTempText;
        View line;
        RelativeLayout sobot_issue_text;
    }
}
