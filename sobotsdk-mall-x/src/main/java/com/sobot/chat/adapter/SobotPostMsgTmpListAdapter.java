package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.base.SobotBaseGvAdapter;
import com.sobot.chat.api.model.SobotPostMsgTemplate;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

/**
 * 留言模版适配器
 */
public class SobotPostMsgTmpListAdapter extends SobotBaseGvAdapter<SobotPostMsgTemplate> {

    private static int themeColor;
    private TemItemOnClick itemOnClick;
    public SobotPostMsgTmpListAdapter(Context context, List<SobotPostMsgTemplate> list,TemItemOnClick click) {
        super(context, list);
        itemOnClick = click;
        themeColor = ThemeUtils.getThemeColor(context);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.sobot_list_item_robot;
    }

    @Override
    protected SobotBaseGvAdapter.BaseViewHolder getViewHolder(Context context, View view) {
        return new SobotPostMsgTmpListAdapter.ViewHolder(context,view);
    }

    private class ViewHolder extends SobotBaseGvAdapter.BaseViewHolder<SobotPostMsgTemplate> {
        private TextView sobot_tv_content;
        private LinearLayout sobot_ll_content;

        private ViewHolder(Context context, View view) {
            super(context,view);
            sobot_ll_content = (LinearLayout) view.findViewById(R.id.sobot_ll_content);
            sobot_tv_content = (TextView) view.findViewById(R.id.sobot_tv_content);
            if(ThemeUtils.isChangedThemeColor(mContext)) {
                sobot_tv_content.setTextColor(themeColor);
            }
        }

        public void bindData(final SobotPostMsgTemplate data, int position) {
            if (data != null && !TextUtils.isEmpty(data.getTemplateName())) {
                sobot_ll_content.setVisibility(View.VISIBLE);
                sobot_tv_content.setText(data.getTemplateName());
                if(ThemeUtils.isChangedThemeColor(mContext)){
                    sobot_ll_content.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_dialog_button_selector);
                            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                                view.setBackground(ThemeUtils.applyColorToDrawable(drawable,themeColor));
                                sobot_tv_content.setTextColor(mContext.getResources().getColor(R.color.sobot_color_white));
                            }else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL||motionEvent.getAction() == motionEvent.ACTION_OUTSIDE||motionEvent.getAction() == motionEvent.ACTION_POINTER_DOWN || motionEvent.getAction() == motionEvent.ACTION_POINTER_UP ){
                                view.setBackground(drawable);
                                sobot_tv_content.setTextColor(themeColor);
                                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                                    //点击事件
                                    if(itemOnClick!=null){
                                        itemOnClick.onItemClick(data);
                                    }
                                }
                            }
                            return true;
                        }
                    });
                }else {
                    sobot_ll_content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //点击事件
                            if(itemOnClick!=null){
                                itemOnClick.onItemClick(data);
                            }
                        }
                    });
                }
            } else {
                sobot_ll_content.setVisibility(View.INVISIBLE);
                sobot_ll_content.setSelected(false);
                sobot_tv_content.setText("");
            }
        }
    }
    public interface TemItemOnClick {
        void onItemClick(SobotPostMsgTemplate item);
    }
}