package com.sobot.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sobot.chat.R;

/**
 * 支持最大宽高
 */
public class SobotMaxSizeFrameLayout extends FrameLayout {

    //最大宽
    private int mMaxWidth = -1;
    //最大高
    private int mMaxHeight = -1;

    public SobotMaxSizeFrameLayout(Context context) {
        super(context);

    }

    public SobotMaxSizeFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_maxsize_layout);
        mMaxWidth = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_width, 0);
        mMaxHeight = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_height, 0);
        ta.recycle();
    }

    public SobotMaxSizeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_maxsize_layout);
        mMaxWidth = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_width, 0);
        mMaxHeight = ta.getDimensionPixelSize(R.styleable.sobot_maxsize_layout_sobot_max_height, 0);
        ta.recycle();
    }

    /**
     * 设置最大高
     * @param maxHeight
     */
    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
        requestLayout();
    }

    /**
     * 设置最大宽
     * @param maxWidth
     */
    public void setMaxWidth(int maxWidth) {
        this.mMaxWidth = maxWidth;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mMaxWidth > 0 && measureWidth > mMaxWidth) {
            measureWidth = mMaxWidth;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(measureWidth, MeasureSpec.EXACTLY);
        }
        if (mMaxHeight > 0 && measureHeight > mMaxHeight) {
            measureHeight = mMaxHeight;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}