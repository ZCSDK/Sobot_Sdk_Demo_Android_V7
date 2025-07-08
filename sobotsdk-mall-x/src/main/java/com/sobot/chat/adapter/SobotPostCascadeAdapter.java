package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotCusFieldDataInfo;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.utils.SobotStringUtils;

import java.util.ArrayList;
import java.util.List;

//留言级联字段 adapter
public class SobotPostCascadeAdapter extends SobotBaseAdapter<SobotCusFieldDataInfo> {

    private Context mContext;
    private ViewHolder myViewHolder;
    private Activity mActivity;
    //输入的内容
    private String searchText;

    //过滤时候的总数据 这个是不变的数据
    private List<SobotCusFieldDataInfo> adminList;
    private MyFilter mFilter;

    public SobotPostCascadeAdapter(Activity activity, Context context, List list) {
        super(context, list);
        this.mContext = context;
        this.mActivity = activity;
        adminList = list;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.sobot_activity_post_category_items, null);
            myViewHolder = new ViewHolder(mActivity, mContext, convertView);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (ViewHolder) convertView.getTag();
        }
        String data = list.get(position).getDataName();
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
            myViewHolder.categoryTitle.setText(spannableString);
        } else {
            myViewHolder.categoryTitle.setText("");
        }
        if (!list.get(position).isHasNext()) {
            myViewHolder.categoryIshave.setVisibility(View.GONE);
        } else {
            myViewHolder.categoryIshave.setVisibility(View.VISIBLE);
            myViewHolder.categoryIshave.setImageResource(R.drawable.sobot_right_arrow_icon);
        }


        return convertView;
    }

    static class ViewHolder {
        private TextView categoryTitle;
        private ImageView categoryIshave;
        private Activity mActivity;

        ViewHolder(Activity activity, Context context, View view) {
            mActivity = activity;
            categoryTitle = (TextView) view.findViewById(R.id.work_order_category_title);
            categoryIshave = (ImageView) view.findViewById(R.id.work_order_category_ishave);
            displayInNotch(categoryTitle);
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

    //返回过滤器
    public MyFilter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }

    public class MyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = adminList;
                results.count = adminList.size();
            } else {
                String prefixString = prefix.toString();

                final ArrayList<SobotCusFieldDataInfo> newValues = new ArrayList<>();

                for (int i = 0; i < adminList.size(); i++) {
                    final String value = adminList.get(i).getDataName();
                    if (SobotStringUtils.isNoEmpty(value) && value.toLowerCase().contains(prefixString.toLowerCase())) {//我这里的规则就是筛选出和prefix相同的元素
                        newValues.add(adminList.get(i));
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            list = (List<SobotCusFieldDataInfo>) results.values;
            if (list.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

}