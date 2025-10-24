package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.StDocModel;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.CommonUtils;

import java.util.List;

public class SobotCategoryAdapter extends SobotBaseAdapter<StDocModel> {
    private LayoutInflater mInflater;
    private Activity mActivity;

    public SobotCategoryAdapter(Context context, Activity activity, List<StDocModel> list) {
        super(context, list);
        this.mActivity = activity;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sobot_list_item_help_category, null);
            viewHolder = new ViewHolder(context, mActivity, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.bindData(position, list.get(position));

        return convertView;
    }

    private static class ViewHolder {
        private TextView tv_title;
        private ImageView iv_arrow;
        private Activity mActivity;

        public ViewHolder(Context context, Activity activity, View view) {
            this.mActivity = activity;
            tv_title = view.findViewById(R.id.tv_title);
            iv_arrow = view.findViewById(R.id.iv_arrow);
        }

        public void bindData(int position, StDocModel data) {
            tv_title.setText(data.getQuestionTitle());
            displayInNotch(tv_title);
            if (mActivity != null) {
                if (CommonUtils.checkSDKIsAr(mActivity)) {
                    iv_arrow.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                } else {
                    iv_arrow.setImageResource(R.drawable.sobot_icon_right_arrow);
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