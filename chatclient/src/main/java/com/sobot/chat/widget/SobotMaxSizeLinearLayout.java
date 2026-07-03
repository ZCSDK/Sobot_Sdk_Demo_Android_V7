package com.sobot.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.sobot.chat.R;

/**
 * 支持最大宽高
 */
public class SobotMaxSizeLinearLayout extends LinearLayout {

    //最大宽（0 = 不启用）；默认"上限"语义（父可用宽超过 mMaxWidth 才限到 mMaxWidth，保留原 mode）；配合 mFillToMaxWidth=true 切换为"主动撑宽"
    private int mMaxWidth = 0;
    //最大高
    private int mMaxHeight = 0;
    //宽度占屏宽百分比（0~1，0 = 不启用）；默认"上限"语义（屏宽×百分比作为上限，超过才限，保留原 mode）；配合 mFillToMaxWidth=true 切换为"主动撑宽"
    private float mMaxWidthPercent = 0f;
    //配合 mMaxWidth / mMaxWidthPercent 使用：true=按 EXACTLY 主动撑宽到目标值，让 match_parent 子 View 跟随撑开；false（默认）=上限语义
    private boolean mFillToMaxWidth = false;

    public SobotMaxSizeLinearLayout(Context context) {
        super(context);
    }

    public SobotMaxSizeLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SobotMaxSizeLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_maxsize_layout);
        try {
            mMaxWidth = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_width, 0);
            mMaxHeight = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_height, 0);
            mMaxWidthPercent = ta.getFloat(R.styleable.sobot_maxsize_layout_sobot_max_width_percent, 0f);
            mFillToMaxWidth = ta.getBoolean(R.styleable.sobot_maxsize_layout_sobot_fill_to_max_width, false);
        } finally {
            ta.recycle();
        }
    }

    /**
     * 设置最大高
     *
     * @param maxHeight
     */
    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
        requestLayout();
    }

    /**
     * 设置最大宽
     *
     * @param maxWidth
     */
    public void setMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
        requestLayout();
    }

    /**
     * 设置宽度占屏宽百分比（0~1，0 表示不启用；默认"上限"语义，配合 setFillToMaxWidth(true) 切换为"主动撑宽"）
     *
     * @param percent
     */
    public void setMaxWidthPercent(float percent) {
        if (percent < 0f) {
            percent = 0f;
        } else if (percent > 1f) {
            percent = 1f;
        }
        this.mMaxWidthPercent = percent;
        requestLayout();
    }

    /**
     * 设置是否主动撑宽到目标值
     *
     * @param fillToMaxWidth true=按 EXACTLY 主动撑宽（让 match_parent 子 View 跟随撑开）；false=上限语义
     */
    public void setFillToMaxWidth(boolean fillToMaxWidth) {
        this.mFillToMaxWidth = fillToMaxWidth;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        // 屏宽百分比目标宽：屏宽 × percent，上限不超过父可用宽，与 mMaxWidth 同时存在取较小
        if (mMaxWidthPercent > 0f && widthMode != MeasureSpec.UNSPECIFIED) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int targetWidth = (int) (screenWidth * mMaxWidthPercent);
            if (targetWidth > measureWidth) {
                targetWidth = measureWidth;
            }
            if (mMaxWidth > 0) {
                targetWidth = Math.min(targetWidth, mMaxWidth);
            }
            if (mFillToMaxWidth) {
                // 主动撑宽：EXACTLY 撑到 targetWidth，让 match_parent 子 View 跟随撑开
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY);
            } else if (measureWidth > targetWidth) {
                // 上限：父可用宽超过 targetWidth 才限到 targetWidth，保留原 mode（按内容自适应）
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth, widthMode);
            }
        } else if (mMaxWidth > 0 && widthMode != MeasureSpec.UNSPECIFIED) {
            if (mFillToMaxWidth) {
                // 像素主动撑宽：EXACTLY 撑宽到 min(父可用宽, mMaxWidth)
                int targetWidth = Math.min(measureWidth, mMaxWidth);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY);
            } else if (measureWidth > mMaxWidth) {
                // 像素上限：父可用宽超过 mMaxWidth 才限到 mMaxWidth，保留原 mode
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, widthMode);
            }
        }
        if (mMaxHeight > 0 && measureHeight > mMaxHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }
}
