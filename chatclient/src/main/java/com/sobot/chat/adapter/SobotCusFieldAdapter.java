package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotCusFieldDataInfo;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/25.
 */

public class SobotCusFieldAdapter extends SobotBaseAdapter<SobotCusFieldDataInfo> implements Filterable {

    private MyViewHolder myViewHolder;
    private Context mContext;
    private Activity mActivity;
    private int fieldType;
    private MyFilter mFilter;

    //满足过滤条件的数据
    private List<SobotCusFieldDataInfo> displayList;
    //过滤时候的总数据 这个是不变的数据
    private List<SobotCusFieldDataInfo> adminList;
    //输入的内容
    private String searchText;

    public SobotCusFieldAdapter(Activity activity, Context context, List<SobotCusFieldDataInfo> list, int fieldType) {
        super(context, list);
        this.mContext = context;
        this.mActivity = activity;
        this.fieldType = fieldType;

        this.adminList = list;
        displayList = list;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return displayList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.sobot_item_cusfield_listview, null);
            myViewHolder = new MyViewHolder(mActivity, convertView);
            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }
        if (position < displayList.size()) {
            String data = displayList.get(position).getDataName();
            if (StringUtils.isNoEmpty(data)) {
                SpannableString spannableString = new SpannableString(data);
                if (StringUtils.isNoEmpty(searchText)) {
                    if (data.toLowerCase().contains(searchText.toLowerCase()) ) {
                        int index = data.toLowerCase().indexOf(searchText.toLowerCase());
                        if(index>=0) {
                            spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                myViewHolder.categorySmallTitle.setText(spannableString);
            } else {
                myViewHolder.categorySmallTitle.setText("");
            }
            if(fieldType == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE){
                myViewHolder.categorySmallIshave.setVisibility(View.GONE);
                myViewHolder.categoryDuoIshave.setVisibility(View.VISIBLE);
                //多选
                if (displayList.get(position).isChecked()) {
                    myViewHolder.categorySmallTitle.setTypeface(null, Typeface.BOLD);
                    myViewHolder.categoryDuoIshave.setSelected(true);
                    if (ThemeUtils.isChangedThemeColor(context)) {
                        int themeColor = ThemeUtils.getThemeColor(context);
                        Drawable bg = context.getResources().getDrawable(R.drawable.sobot_icon_radio_btn_selected);
                        if (bg != null) {
                            myViewHolder.categoryDuoIshave.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                        }
                    }
                }else{
                    Drawable bg = context.getResources().getDrawable(R.drawable.sobot_icon_radio_btn_normal);
                    if (bg != null) {
                        myViewHolder.categoryDuoIshave.setImageDrawable(bg);
                    }
                    myViewHolder.categorySmallTitle.setTypeface(null, Typeface.NORMAL);
                    myViewHolder.categoryDuoIshave.setSelected(false);
                }
            }else {
                myViewHolder.categoryDuoIshave.setVisibility(View.GONE);
                if (displayList.get(position).isChecked()) {
                    myViewHolder.categorySmallTitle.setTypeface(null, Typeface.BOLD);
                    myViewHolder.categorySmallIshave.setVisibility(View.VISIBLE);

                    if (ThemeUtils.isChangedThemeColor(context)) {
                        int themeColor = ThemeUtils.getThemeColor(context);
                        Drawable bg = context.getResources().getDrawable(R.drawable.sobot_icon_item_selected);
                        if (bg != null) {
                            myViewHolder.categorySmallIshave.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                        }
                    }
                } else {
                    myViewHolder.categorySmallTitle.setTypeface(null, Typeface.NORMAL);
                    myViewHolder.categorySmallIshave.setVisibility(View.GONE);
                }
            }
        } else {
            myViewHolder.categorySmallTitle.setText("");
            myViewHolder.categorySmallIshave.setVisibility(View.GONE);
            myViewHolder.categoryDuoIshave.setVisibility(View.GONE);
        }

        return convertView;
    }

    class MyViewHolder {

        private TextView categorySmallTitle;
        private ImageView categorySmallIshave, categoryDuoIshave;
        private Activity mActivity;

        MyViewHolder(Activity activity, View view) {
            this.mActivity = activity;
            // 在 getView 方法中
            boolean isRtl = ChatUtils.isRtl(getContext());
            LinearLayout container = view.findViewById(R.id.sobot_ll_item_container);
            container.setLayoutDirection(isRtl ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
            categorySmallTitle = (TextView) view.findViewById(R.id.sobot_activity_cusfield_listview_items_title);
            categorySmallIshave = (ImageView) view.findViewById(R.id.sobot_activity_cusfield_listview_items_ishave);
            categoryDuoIshave = (ImageView) view.findViewById(R.id.sobot_duo_cusfield_listview_items_ishave);
            displayInNotch(categorySmallTitle);
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
                    if (value.toLowerCase().contains(prefixString.toLowerCase())) {//我这里的规则就是筛选出和prefix相同的元素
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
            displayList = (List<SobotCusFieldDataInfo>) results.values;
            if (displayList.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }


}