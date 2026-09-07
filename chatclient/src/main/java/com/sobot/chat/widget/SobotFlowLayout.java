package com.sobot.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.sobot.chat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 左对齐自动换行布局，用于大模型推荐问题等需要按可用宽度换行的轻量按钮组。
 * 改动测量规则会影响所有使用该布局的动态按钮容器。
 */
public class SobotFlowLayout extends ViewGroup {

    private int mHorizontalGap;
    private int mVerticalGap;
    private boolean mSingleColumn;
    private final List<LineInfo> mLineInfoList = new ArrayList<>();

    public SobotFlowLayout(Context context) {
        super(context);
    }

    public SobotFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SobotFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_autoWrapLineLayout);
        mHorizontalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_horizontalGap, 0);
        mVerticalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_verticalGap, 0);
        ta.recycle();
    }

    /**
     * 控制每个子控件是否独占一行。竖屏推荐问题需要一行一个，横屏保持自动换行。
     */
    public void setSingleColumn(boolean singleColumn) {
        if (mSingleColumn != singleColumn) {
            mSingleColumn = singleColumn;
            requestLayout();
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mLineInfoList.clear();
        int availableWidth = Math.max(0, MeasureSpec.getSize(widthMeasureSpec) - getPaddingStart() - getPaddingEnd());
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int lineWidth = 0;
        int lineHeight = 0;
        int maxLineWidth = 0;
        List<View> lineViews = new ArrayList<>();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            boolean needNewLine = !lineViews.isEmpty()
                    && (mSingleColumn
                    || (widthMode != MeasureSpec.UNSPECIFIED
                    && lineWidth + mHorizontalGap + childWidth > availableWidth));
            if (needNewLine) {
                addLine(lineViews, lineWidth, lineHeight);
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                lineViews = new ArrayList<>();
                lineWidth = 0;
                lineHeight = 0;
            }
            if (!lineViews.isEmpty()) {
                lineWidth += mHorizontalGap;
            }
            lineViews.add(child);
            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
        }
        if (!lineViews.isEmpty()) {
            addLine(lineViews, lineWidth, lineHeight);
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }

        int totalHeight = getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < mLineInfoList.size(); i++) {
            totalHeight += mLineInfoList.get(i).height;
            if (i < mLineInfoList.size() - 1) {
                totalHeight += mVerticalGap;
            }
        }
        int measuredWidth = widthMode == MeasureSpec.EXACTLY
                ? MeasureSpec.getSize(widthMeasureSpec)
                : maxLineWidth + getPaddingStart() + getPaddingEnd();
        setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec), resolveSize(totalHeight, heightMeasureSpec));
    }

    private void addLine(List<View> views, int width, int height) {
        LineInfo lineInfo = new LineInfo();
        lineInfo.views = views;
        lineInfo.width = width;
        lineInfo.height = height;
        mLineInfoList.add(lineInfo);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean isRtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int top = getPaddingTop();
        int contentWidth = Math.max(0, getWidth() - getPaddingStart() - getPaddingEnd());
        for (LineInfo lineInfo : mLineInfoList) {
            if (isRtl) {
                layoutLineRtl(lineInfo, top, contentWidth);
            } else {
                layoutLineLtr(lineInfo, top);
            }
            top += lineInfo.height + mVerticalGap;
        }
    }

    private void layoutLineLtr(LineInfo lineInfo, int top) {
        int left = getPaddingStart();
        for (View child : lineInfo.views) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            left += lp.leftMargin;
            int childTop = top + lp.topMargin;
            child.layout(left, childTop, left + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            left += child.getMeasuredWidth() + lp.rightMargin + mHorizontalGap;
        }
    }

    private void layoutLineRtl(LineInfo lineInfo, int top, int contentWidth) {
        int right = getPaddingStart() + contentWidth;
        for (View child : lineInfo.views) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            right -= lp.rightMargin;
            int childTop = top + lp.topMargin;
            child.layout(right - child.getMeasuredWidth(), childTop, right, childTop + child.getMeasuredHeight());
            right -= child.getMeasuredWidth() + lp.leftMargin + mHorizontalGap;
        }
    }

    private static class LineInfo {
        List<View> views;
        int width;
        int height;
    }
}
