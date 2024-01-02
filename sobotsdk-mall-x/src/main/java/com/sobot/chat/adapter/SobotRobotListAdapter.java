package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.base.SobotBaseGvAdapter;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.utils.ThemeUtils;

import java.util.List;

public class SobotRobotListAdapter extends SobotBaseGvAdapter<SobotRobot> {

    private Context mContext;
    private static int themeColor;
    private RobotItemOnClick itemOnClick;
    public SobotRobotListAdapter(Context context, List<SobotRobot> list,RobotItemOnClick itemOnClick,int themeColor) {
        super(context, list);
        mContext = context;
        this.itemOnClick = itemOnClick;
        this.themeColor = themeColor;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.sobot_list_item_robot;
    }

    @Override
    protected BaseViewHolder getViewHolder(Context context, View view) {
        return new ViewHolder(context, view);
    }

    private class ViewHolder extends BaseViewHolder<SobotRobot> {
        private TextView sobot_tv_content;
        private LinearLayout sobot_ll_content;

        private ViewHolder(Context context, View view) {
            super(context, view);
            sobot_ll_content = (LinearLayout) view.findViewById(R.id.sobot_ll_content);
            sobot_tv_content = (TextView) view.findViewById(R.id.sobot_tv_content);
            if(ThemeUtils.isChangedThemeColor(mContext)) {
                sobot_tv_content.setTextColor(themeColor);
            }

        }

        public void bindData(final SobotRobot sobotRobot, final int position) {
            if (sobotRobot != null && !TextUtils.isEmpty(sobotRobot.getOperationRemark())) {
                sobot_ll_content.setVisibility(View.VISIBLE);

                sobot_tv_content.setText(sobotRobot.getOperationRemark());
                if(themeColor!=0){
                    Drawable drawable = context.getResources().getDrawable(R.drawable.sobot_oval_green_bg);
                    if (sobotRobot.isSelected()) {
                        sobot_ll_content.setBackground(ThemeUtils.applyColorToDrawable(drawable, themeColor));
                        sobot_tv_content.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_common_white));
                    } else {
                        sobot_ll_content.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_oval_gray_bg));
                        sobot_tv_content.setTextColor(themeColor);
                    }

                    sobot_ll_content.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                                Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_oval_green_bg);
                                view.setBackground(ThemeUtils.applyColorToDrawable(drawable,themeColor));
                                sobot_tv_content.setTextColor(mContext.getResources().getColor(R.color.sobot_common_white));
                            }else if(motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL||motionEvent.getAction() == motionEvent.ACTION_OUTSIDE||motionEvent.getAction() == motionEvent.ACTION_POINTER_DOWN || motionEvent.getAction() == motionEvent.ACTION_POINTER_UP ){
                                view.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_oval_gray_bg));
                                sobot_tv_content.setTextColor(themeColor);
                                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                                    //点击事件
                                    if(itemOnClick!=null){
                                        itemOnClick.onItemClick(sobotRobot);
                                    }
                                }
                            }
                            return true;
                        }
                    });
                }else{
                    if (sobotRobot.isSelected()) {
                        sobot_ll_content.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_oval_green_bg));
                        sobot_tv_content.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_common_white));
                    } else {
                        sobot_ll_content.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_oval_gray_bg));
                        sobot_tv_content.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_common_wenzi_green_white));
                    }
                    sobot_ll_content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //点击事件
                            if(itemOnClick!=null){
                                itemOnClick.onItemClick(sobotRobot);
                            }
                        }
                    });
                }
            } else {
                sobot_ll_content.setVisibility(View.INVISIBLE);
                sobot_tv_content.setText("");
            }
        }
    }
    public interface RobotItemOnClick{
        void onItemClick(SobotRobot itemBeen);
    }

}