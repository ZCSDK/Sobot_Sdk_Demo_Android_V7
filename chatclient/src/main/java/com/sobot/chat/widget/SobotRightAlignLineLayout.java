package com.sobot.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.sobot.chat.R;
import com.sobot.chat.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 右对齐自动换行布局（子控件从左到右排列，但整体靠右对齐）
 * 支持RTL：阿语环境下子控件从右到左排列，整行内容靠左对齐
 */
public class SobotRightAlignLineLayout extends ViewGroup {

    private int mVerticalGap = 0;
    private int mHorizontalGap = 0;

    private List<Integer> childOfLine; //保存每行的子视图数量
    private List<Integer> lineWidths;  //保存每行的实际宽度

    public SobotRightAlignLineLayout(Context context) {
        super(context);
    }

    public SobotRightAlignLineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SobotRightAlignLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_autoWrapLineLayout);
        mHorizontalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_horizontalGap, 0);
        mVerticalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_verticalGap, 0);
        ta.recycle();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childOfLine = new ArrayList<>();
        lineWidths = new ArrayList<>();

        int childCount = getChildCount();
        int totalHeight = 0;
        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int curLineChildCount = 0;
        int curLineWidth = 0;
        int maxHeight = 0;

        for (int i = 0; i < childCount; i++) {
            View childItem = getChildAt(i);
            measureChild(childItem, widthMeasureSpec, heightMeasureSpec);

            int childHeight = childItem.getMeasuredHeight();
            int childWidth = childItem.getMeasuredWidth();

            if (childItem.getVisibility() == GONE) {
                childHeight = 0;
                childWidth = 0;
                continue; // 修复：GONE视图不计入当前行计数
            }

            // 检查是否需要换行
            if (curLineWidth + childWidth + ((curLineChildCount > 0) ? mHorizontalGap : 0) <= totalWidth) {
                curLineWidth += childWidth + ((curLineChildCount > 0) ? mHorizontalGap : 0);
                maxHeight = Math.max(childHeight, maxHeight);
                curLineChildCount++;
            } else {
                // 换行处理
                if (curLineChildCount > 0) { // 添加保护条件
                    childOfLine.add(curLineChildCount);
                    lineWidths.add(curLineWidth);
                }
                curLineWidth = childWidth;
                curLineChildCount = 1;
                totalHeight += maxHeight + mVerticalGap;
                maxHeight = childHeight;
            }
        }

        // 添加最后一行的信息
        if (curLineChildCount > 0) { // 添加保护条件
            childOfLine.add(curLineChildCount);
            lineWidths.add(curLineWidth);
        }

        // 移除空行
        for (int i = childOfLine.size() - 1; i >= 0; i--) { // 从后往前遍历避免索引问题
            if (childOfLine.get(i) == 0) {
                childOfLine.remove(i);
                lineWidths.remove(i);
            }
        }

        totalHeight += maxHeight;
        setMeasuredDimension(totalWidth, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean isRtl = isRtl();
        if (isRtl) {
            layoutRtlMode();
        } else {
            layoutLtrMode();
        }
    }

    /**
     * LTR模式：子控件从左到右排列，整行靠右对齐
     */
    private void layoutLtrMode() {
        // 添加边界检查
        if (childOfLine == null || childOfLine.isEmpty() || lineWidths == null || lineWidths.isEmpty()) {
            return;
        }

        int index = 0;
        int curHeight = 0;
        int layoutWidth = getMeasuredWidth();

        for (int i = 0; i < childOfLine.size(); i++) {
            int childCount = childOfLine.get(i);
            int lineHeight = 0;
            int lineWidth = lineWidths.get(i);

            // 计算右边距（使整行右对齐）
            int rightMargin = layoutWidth - lineWidth;
            int currentX = rightMargin; // 从右边界开始的位置

            // 从左到右放置子控件，但整体靠右对齐
            for (int j = 0; j < childCount; j++) {
                View item = getChildAt(index + j);
                if (item != null && item.getVisibility() == VISIBLE) {
                    lineHeight = Math.max(lineHeight, item.getMeasuredHeight());

                    // 从左到右放置，但起点在右侧
                    item.layout(currentX, curHeight,
                            currentX + item.getMeasuredWidth(),
                            curHeight + item.getMeasuredHeight());

                    // 移动到下一个位置
                    currentX += item.getMeasuredWidth() + mHorizontalGap;
                }
            }

            curHeight += lineHeight + mVerticalGap;
            index += childCount;
        }
    }

    /**
     * RTL模式：子控件从右到左排列，整行靠左对齐
     */
    private void layoutRtlMode() {
        // 添加边界检查
        if (childOfLine == null || childOfLine.isEmpty() || lineWidths == null || lineWidths.isEmpty()) {
            return;
        }

        int index = 0;
        int curHeight = 0;
        int layoutWidth = getMeasuredWidth();

        for (int i = 0; i < childOfLine.size(); i++) {
            int childCount = childOfLine.get(i);
            int lineHeight = 0;
            int lineWidth = lineWidths.get(i);

            // RTL模式下整行靠左对齐，所以起始位置应该是lineWidth（行宽度）
            int currentX = lineWidth; // 从行宽度位置开始，向左放置控件

            // 从右到左放置子控件，保持从右到左的视觉顺序
            for (int j = 0; j < childCount; j++) {
                View item = getChildAt(index + j);
                if (item != null && item.getVisibility() == VISIBLE) {
                    lineHeight = Math.max(lineHeight, item.getMeasuredHeight());

                    // 从右向左放置
                    currentX -= item.getMeasuredWidth();
                    item.layout(currentX, curHeight,
                            currentX + item.getMeasuredWidth(),
                            curHeight + item.getMeasuredHeight());

                    // 继续向左移动（包括间距）
                    currentX -= mHorizontalGap;
                }
            }

            curHeight += lineHeight + mVerticalGap;
            index += childCount;
        }
    }

    // 添加RTL判断方法
    private boolean isRtl() {
        return ChatUtils.isRtl(getContext());
    }

    public void setHorizontalGap(int horizontalGap) {
        this.mHorizontalGap = horizontalGap;
    }

    public void setVerticalGap(int verticalGap) {
        this.mVerticalGap = verticalGap;
    }
}
