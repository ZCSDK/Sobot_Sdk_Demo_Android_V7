package com.sobot.chat.widget.kpswitch.widget.adpater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.kpswitch.view.ChattingPanelMoreMenuView;
import com.sobot.chat.widget.kpswitch.widget.data.PlusPageEntity;
import com.sobot.chat.widget.kpswitch.widget.interfaces.PlusDisplayListener;

import java.util.ArrayList;

/**
 * 更多菜单中的 适配器
 */
public class PlusAdapter<T> extends BaseAdapter {

    protected final int DEF_HEIGHTMAXTATIO = 2;
    protected final int mDefalutItemHeight;

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected ArrayList<T> mData = new ArrayList<>();
    protected PlusPageEntity mPlusPageEntity;
    protected double mItemHeightMaxRatio;
    protected int mItemHeightMax;
    protected int mItemHeightMin;
    protected int mItemHeight;
    protected PlusDisplayListener mOnDisPlayListener;
    protected ChattingPanelMoreMenuView.SobotPlusClickListener mOnItemClickListener;

    public PlusAdapter(Context context, PlusPageEntity pageEntity, ChattingPanelMoreMenuView.SobotPlusClickListener onItemClickListener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mPlusPageEntity = pageEntity;
        this.mOnItemClickListener = onItemClickListener;
        this.mItemHeightMaxRatio = DEF_HEIGHTMAXTATIO;
        this.mDefalutItemHeight = this.mItemHeight = 80;
        this.mData.addAll(pageEntity.getDataList());
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlusAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new PlusAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.sobot_list_item_plus_menu, null);
            viewHolder.rootView = convertView;
            viewHolder.ly_root = convertView.findViewById(R.id.sobot_ly_root);
            viewHolder.mMenu = convertView.findViewById(R.id.sobot_plus_menu);
            viewHolder.mMenuIcon = convertView.findViewById(R.id.sobot_plus_menu_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PlusAdapter.ViewHolder) convertView.getTag();
        }

        bindView(position, parent, viewHolder);
        updateUI(viewHolder, parent);
        return convertView;
    }

    protected void bindView(int position, ViewGroup parent, PlusAdapter.ViewHolder viewHolder) {
        if (mOnDisPlayListener != null) {
            mOnDisPlayListener.onBindView(position, parent, viewHolder, mData.get(position));
        }
    }


    protected void updateUI(PlusAdapter.ViewHolder viewHolder, ViewGroup parent) {
//        mItemHeightMax = this.mItemHeightMax != 0 ? this.mItemHeightMax : (int) (mItemHeight * mItemHeightMaxRatio);
//        mItemHeightMin = this.mItemHeightMin != 0 ? this.mItemHeightMin : mItemHeight;
//        int realItemHeight = ((View) parent.getParent()).getMeasuredHeight() / mPlusPageEntity.getLine();
//        realItemHeight = Math.min(realItemHeight, mItemHeightMax);
//        realItemHeight = Math.max(realItemHeight, mItemHeightMin);
//        viewHolder.ly_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, realItemHeight));
    }

    public void setOnDisPlayListener(PlusDisplayListener mOnDisPlayListener) {
        this.mOnDisPlayListener = mOnDisPlayListener;
    }

    public void setItemHeightMaxRatio(double mItemHeightMaxRatio) {
        this.mItemHeightMaxRatio = mItemHeightMaxRatio;
    }

    public void setItemHeightMax(int mItemHeightMax) {
        this.mItemHeightMax = mItemHeightMax;
    }

    public void setItemHeightMin(int mItemHeightMin) {
        this.mItemHeightMin = mItemHeightMin;
    }

    public void setItemHeight(int mItemHeight) {
        this.mItemHeight = mItemHeight;
    }


    public static class ViewHolder {
        public View rootView;
        public LinearLayout ly_root;
        public TextView mMenu;
        public SobotProgressImageView mMenuIcon;
    }
}