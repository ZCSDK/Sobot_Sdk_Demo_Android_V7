package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.base.SobotBaseAdapter;
import com.sobot.chat.api.model.SobotOptionModel;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class SobotSelectAdapter extends SobotBaseAdapter<SobotOptionModel> {

    private Context mContext;
    private ViewHolder myViewHolder;
    private Activity mActivity;
    //输入的内容
    private String searchText;
    //过滤时候的总数据 这个是不变的数据
    private List<SobotOptionModel> datas;
    private MyFilter mFilter;


    public SobotSelectAdapter(Context context, List<SobotOptionModel> list) {
        super(context, list);
        this.mContext = context;
        this.datas = list;
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
        String data = list.get(position).getLabel();
        if (StringUtils.isNoEmpty(data)) {
            SpannableString spannableString = new SpannableString(data);
            if (StringUtils.isNoEmpty(searchText)) {
                if (data.toLowerCase().contains(searchText.toLowerCase())) {
                    int index = data.indexOf(searchText.toLowerCase());
                    if (index >= 0) {
                        spannableString.setSpan(new ForegroundColorSpan(ThemeUtils.getThemeColor(mContext)), index, index + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            myViewHolder.categoryTitle.setText(spannableString);
        } else {
            myViewHolder.categoryTitle.setText("");
        }
        myViewHolder.categoryIshave.setVisibility(View.GONE);

        if (list.get(position).isChecked()) {
            myViewHolder.categoryIshave.setVisibility(View.VISIBLE);
            if (ThemeUtils.isChangedThemeColor(context)) {
                int themeColor = ThemeUtils.getThemeColor(context);
                Drawable bg = context.getResources().getDrawable(R.drawable.sobot_icon_item_selected);
                if (bg != null) {
                    myViewHolder.categoryIshave.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                }
            }
        }

        return convertView;
    }

    static class ViewHolder {
        private TextView categoryTitle;
        private ImageView categoryIshave;
        private Activity mActivity;
        private final Context mContext;

        ViewHolder(Activity activity, Context context, View view) {
            mActivity = activity;
            mContext = context;
            categoryTitle = (TextView) view.findViewById(R.id.work_order_category_title);
            categoryIshave = (ImageView) view.findViewById(R.id.work_order_category_ishave);
            displayInNotch(categoryTitle);
        }

        public void displayInNotch(final View view) {
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
                // 修复闪退（走查：信息收集点选项 NPE）：
                // ① adapter 构造器只传 Context，mActivity 恒为 null，直接 mActivity.getWindow() 必崩；
                // ② 此处位于 Dialog 的 item ViewHolder 内，也不该对宿主 Activity window
                //    重复设置 cutout mode / FLAG_FULLSCREEN —— 宿主基类 onCreate 已统一处理过。
                // 因此这里只保留真正需要的"按刘海信息给 item 文字加避让 padding"，
                // activity 从 context（Dialog context 的 ContextWrapper 链）提取，取不到直接跳过。
                Activity activity = scanForActivity(mActivity != null ? mActivity : mContext);
                if (activity == null) {
                    return;
                }
                // 获取刘海屏信息
                NotchScreenManager.getInstance().getNotchInfo(activity, new INotchScreen.NotchScreenCallback() {
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

    /**
     * 从 Context 沿 ContextWrapper 链提取宿主 Activity（Dialog context 是
     * ContextThemeWrapper 包装的 activity context）。取不到（如 Application
     * context）返回 null，调用方自行跳过 UI 避让逻辑。
     */
    private static Activity scanForActivity(Context context) {
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((android.content.ContextWrapper) context).getBaseContext();
        }
        return null;
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
                results.values = datas;
                results.count = datas.size();
            } else {
                String prefixString = prefix.toString();

                final ArrayList<SobotOptionModel> newValues = new ArrayList<>();

                for (int i = 0; i < datas.size(); i++) {
                    final String value = datas.get(i).getLabel();
                    if (value.toLowerCase().contains(prefixString.toLowerCase())) {//我这里的规则就是筛选出和prefix相同的元素
                        newValues.add(datas.get(i));
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
            list = (List<SobotOptionModel>) results.values;
            if (list != null && list.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}