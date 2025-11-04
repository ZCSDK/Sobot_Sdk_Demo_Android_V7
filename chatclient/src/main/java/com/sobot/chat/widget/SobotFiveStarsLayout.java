package com.sobot.chat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.sobot.chat.R;
import com.sobot.chat.utils.ScreenUtils;

/**
 * 五星自定义
 */
public class SobotFiveStarsLayout extends LinearLayout {

    private int selectContent;//选中的值
    private OnClickItemListener onClickItemListener;

    private LinearLayout line1;

    public SobotFiveStarsLayout(Context context) {
        super(context);
    }

    public SobotFiveStarsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SobotFiveStarsLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isInit() {
        return null == line1;
    }

    /**
     * @param defScore    显示个数
     * @param isCanChange 点击后是否变色
     */
    public void init(int defScore, final boolean isCanChange,int itemW) {
        if (null == line1) {
            line1 = new LinearLayout(getContext());
            line1.setOrientation(LinearLayout.HORIZONTAL);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            line1.setLayoutParams(layoutParams);
            addView(line1);
            LayoutParams lp = null;
            selectContent = defScore - 1;
            for (int i = 0; i < 5; i++) {
                ImageView imageView = new ImageView(getContext());
                lp = new LayoutParams(ScreenUtils.dip2px(getContext(), itemW),
                        ScreenUtils.dip2px(getContext(), itemW));
                imageView.setBackgroundResource(R.drawable.sobot_dialog_close_bg);
                int p= ScreenUtils.dip2px(getContext(), 3);
                imageView.setPadding(p,p,p,p);
                if (i != 4) {
                    lp.rightMargin = ScreenUtils.dip2px(getContext(), 10f);
                } else {
                    lp.rightMargin = 0;
                }
                imageView.setLayoutParams(lp);
                if (i < defScore) {
                    imageView.setImageResource(R.drawable.sobot_evaluate_star_full);
                } else {
                    imageView.setImageResource(R.drawable.sobot_evaluate_star_empty);
                }
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                final int position = i;
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onClickItemListener != null) {
                            if (isCanChange) {
                                updateUI(position);
                            }
                            onClickItemListener.onClickItem(position);
                            selectContent = position;
                        }
                    }
                });
                line1.addView(imageView);
            }
        }

    }

    public void updateUI(int selIndex) {
        int totalNum = line1.getChildCount();
        for (int i = 0; i < totalNum; i++) {
            ImageView imageView = (ImageView) line1.getChildAt(i);
            if (i <= selIndex) {
                imageView.setImageResource(R.drawable.sobot_evaluate_star_full);
            } else {
                imageView.setImageResource(R.drawable.sobot_evaluate_star_empty);
            }
        }

    }

    //选中item的回调,返回选中的分值
    public interface OnClickItemListener {
        void onClickItem(int selectIndex);
    }

    public OnClickItemListener getOnClickItemListener() {
        return onClickItemListener;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public int getSelectContent() {
        return selectContent + 1;
    }

    public void setSelectContent(int selectContent) {
        this.selectContent = selectContent - 1;
    }
}