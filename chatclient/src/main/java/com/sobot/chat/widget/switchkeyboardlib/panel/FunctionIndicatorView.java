package com.sobot.chat.widget.switchkeyboardlib.panel;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.sobot.chat.R;

//加号 扩展菜单 分页 圆点指示器
public class FunctionIndicatorView extends LinearLayout {
    private int mIndicatorCount = 0;
    private int mCurrentIndex = 0;
    private int mIndicatorSize = 6; // dp
    private int mIndicatorMargin = 4; // dp
    private int mSelectedColor = 0xFF777474; // 默认蓝色
    private int mUnselectedColor = 0xFFE6E6E6; // 默认灰色

    public FunctionIndicatorView(Context context) {
        this(context, null);
    }

    public FunctionIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        mSelectedColor = getResources().getColor(R.color.sobot_color_round_dot_sel_color);
        mUnselectedColor = getResources().getColor(R.color.sobot_color_round_dot_def_color);
    }

    /**
     * 设置指示器数量
     *
     * @param count 指示器数量
     */
    public void setIndicatorCount(int count) {
        if (count <= 0) {
            setVisibility(GONE);
            return;
        }

        mIndicatorCount = count;
        mCurrentIndex = 0;
        removeAllViews();

        if (count == 1) {
            // 只有一页时不显示指示器
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
            initIndicators();
        }
    }

    /**
     * 初始化指示器
     */
    private void initIndicators() {
        for (int i = 0; i < mIndicatorCount; i++) {
            View indicator = new View(getContext());
            LayoutParams params = new LayoutParams(
                    dip2px(mIndicatorSize),
                    dip2px(mIndicatorSize));
            params.setMargins(dip2px(mIndicatorMargin), 0, dip2px(mIndicatorMargin), 0);
            indicator.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            if (i == mCurrentIndex) {
                drawable.setColor(mSelectedColor);
            } else {
                drawable.setColor(mUnselectedColor);
            }
            indicator.setBackground(drawable);

            addView(indicator);
        }
    }

    /**
     * 设置当前选中位置
     *
     * @param index 选中位置
     */
    public void setCurrentIndex(int index) {
        if (index < 0 || index >= mIndicatorCount) return;

        mCurrentIndex = index;
        updateIndicators();
    }

    /**
     * 更新指示器状态
     */
    private void updateIndicators() {
        for (int i = 0; i < getChildCount(); i++) {
            View indicator = getChildAt(i);
            if (indicator instanceof View) {
                GradientDrawable drawable = (GradientDrawable) indicator.getBackground();
                if (drawable != null) {
                    if (i == mCurrentIndex) {
                        drawable.setColor(mSelectedColor);
                    } else {
                        drawable.setColor(mUnselectedColor);
                    }
                }
            }
        }
    }

    /**
     * 设置选中颜色
     *
     * @param color 颜色值
     */
    public void setSelectedColor(int color) {
        mSelectedColor = color;
        updateIndicators();
    }

    /**
     * 设置未选中颜色
     *
     * @param color 颜色值
     */
    public void setUnselectedColor(int color) {
        mUnselectedColor = color;
        updateIndicators();
    }

    /**
     * dp转px
     *
     * @param dp dp值
     * @return px值
     */
    private int dip2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
