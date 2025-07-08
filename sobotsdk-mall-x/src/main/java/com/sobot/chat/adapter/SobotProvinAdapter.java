package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotProvinInfo;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

/**
 * @author Created by jinxl on 2018/1/8.
 */
public class SobotProvinAdapter extends SobotBaseAdapter<SobotProvinInfo.SobotProvinceModel> {

    private Context mContext;
    private Activity mActivity;
    private SobotProvinAdapter.ViewHolder myViewHolder;

    public SobotProvinAdapter(Activity activity, Context context, List list) {
        super(context, list);
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.sobot_activity_post_category_items, null);
            myViewHolder = new SobotProvinAdapter.ViewHolder(mActivity, mContext, convertView);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (SobotProvinAdapter.ViewHolder) convertView.getTag();
        }
        myViewHolder.bindData(list.get(position));


        return convertView;
    }

    static class ViewHolder {
        private TextView categoryTitle;
        private ImageView categoryIshave;
        private Context mContext;
        private Activity mActivity;

        ViewHolder(Activity activity, Context context, View view) {
            mActivity = activity;
            mContext = context;
            categoryTitle = (TextView) view.findViewById(R.id.work_order_category_title);
            categoryIshave = (ImageView) view.findViewById(R.id.work_order_category_ishave);
            displayInNotch(categoryTitle);

        }

        void bindData(SobotProvinInfo.SobotProvinceModel data) {
            if (data == null) {
                return;
            }
            switch (data.level) {
                case 0:
                    categoryTitle.setText(data.provinceName);
                    break;
                case 1:
                    categoryTitle.setText(data.cityName);
                    break;
                case 2:
                    categoryTitle.setText(data.areaName);
                    break;
            }

            if (data.nodeFlag) {
                categoryIshave.setVisibility(View.VISIBLE);
                categoryIshave.setImageResource(R.drawable.sobot_right_arrow_icon);
            } else {
                categoryIshave.setVisibility(View.GONE);
            }


            if (data.isChecked) {
                categoryIshave.setVisibility(View.VISIBLE);
                if (ThemeUtils.isChangedThemeColor(mActivity)) {
                    int themeColor = ThemeUtils.getThemeColor(mActivity);
                    Drawable bg = mActivity.getResources().getDrawable(R.drawable.sobot_cur_selected);
                    if (bg != null) {
                        categoryIshave.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
            }


        }

        public void displayInNotch(final View view) {
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
                // 支持显示到刘海区域
                NotchScreenManager.getInstance().setDisplayInNotch(mActivity);
                // 设置Activity全屏
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                // 获取刘海屏信息
                NotchScreenManager.getInstance().getNotchInfo(mActivity, new INotchScreen.NotchScreenCallback() {
                    @Override
                    public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                        if (notchScreenInfo.hasNotch) {
                            for (Rect rect : notchScreenInfo.notchRects) {
                                view.setPadding((rect.right > 110 ? 110 : rect.right), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                });

            }
        }
    }
}