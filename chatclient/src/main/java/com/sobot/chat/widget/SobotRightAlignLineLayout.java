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
 * 支持多类型子控件：每种类型独立换行，按类型顺序显示
 */
public class SobotRightAlignLineLayout extends ViewGroup {

    private int mVerticalGap = 0;
    private int mHorizontalGap = 0;

    // 按类型存储每行的子视图信息
    private List<LineInfo> lineInfoList;

    // 类型分隔信息：记录每种类型从哪一行开始
    private List<TypeInfo> typeInfoList;

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

    /**
     * 设置子控件的类型标识
     *
     * @param child 子控件
     * @param type  类型标识（从0开始，按类型顺序显示）
     */
    public static void setChildType(View child, int type) {
        child.setTag(R.id.sobot_child_type_tag, type);
    }

    /**
     * 获取子控件的类型标识
     */
    private int getChildType(View child) {
        Object tag = child.getTag(R.id.sobot_child_type_tag);
        return tag != null ? (int) tag : 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        lineInfoList = new ArrayList<>();
        typeInfoList = new ArrayList<>();

        int childCount = getChildCount();
        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int totalHeight = 0;

        if (childCount == 0) {
            setMeasuredDimension(totalWidth, totalHeight);
            return;
        }

        // 按类型分组收集子控件
        List<List<View>> viewsByType = new ArrayList<>();
        int currentType = -1;
        List<View> currentTypeViews = null;

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;

            int type = getChildType(child);
            if (type != currentType) {
                currentType = type;
                currentTypeViews = new ArrayList<>();
                viewsByType.add(currentTypeViews);
            }
            currentTypeViews.add(child);
        }

        // 对每种类型分别进行测量和换行计算
        for (int typeIndex = 0; typeIndex < viewsByType.size(); typeIndex++) {
            List<View> typeViews = viewsByType.get(typeIndex);
            int typeStartLine = lineInfoList.size();

            int curLineWidth = 0;
            int curLineChildCount = 0;
            int maxHeight = 0;
            List<View> curLineViews = new ArrayList<>();

            for (int i = 0; i < typeViews.size(); i++) {
                View child = typeViews.get(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);

                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                // 关键修改：每个类型的第一个子控件强制从新行开始
                boolean needNewLine = (i == 0) ||
                        (curLineChildCount > 0 && (curLineWidth + mHorizontalGap + childWidth > totalWidth));

                if (needNewLine) {
                    // 如果不是该类型的第一个元素，保存前一行
                    if (i > 0 && curLineChildCount > 0) {
                        LineInfo lineInfo = new LineInfo();
                        lineInfo.views = new ArrayList<>(curLineViews);
                        lineInfo.lineWidth = curLineWidth;
                        lineInfo.lineHeight = maxHeight;
                        lineInfo.typeIndex = typeIndex;
                        lineInfoList.add(lineInfo);

                        totalHeight += maxHeight + mVerticalGap;
                    }

                    // 开始新行（包括该类型的第一个元素）
                    curLineWidth = childWidth;
                    curLineChildCount = 1;
                    maxHeight = childHeight;
                    curLineViews.clear();
                    curLineViews.add(child);
                } else {
                    // 继续当前行
                    if (curLineChildCount > 0) {
                        curLineWidth += mHorizontalGap;
                    }
                    curLineWidth += childWidth;
                    maxHeight = Math.max(maxHeight, childHeight);
                    curLineChildCount++;
                    curLineViews.add(child);
                }
            }

            // 保存最后一行
            if (curLineChildCount > 0) {
                LineInfo lineInfo = new LineInfo();
                lineInfo.views = new ArrayList<>(curLineViews);
                lineInfo.lineWidth = curLineWidth;
                lineInfo.lineHeight = maxHeight;
                lineInfo.typeIndex = typeIndex;
                lineInfoList.add(lineInfo);

                totalHeight += maxHeight;
            }

            // 记录类型信息
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.startLine = typeStartLine;
            typeInfo.endLine = lineInfoList.size() - 1;
            typeInfoList.add(typeInfo);

            // 类型之间添加额外间距（除了最后一个类型）
            if (typeIndex < viewsByType.size() - 1) {
                totalHeight += mVerticalGap;
            }
        }

        setMeasuredDimension(totalWidth, totalHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean isRtl = isRtl();

        int curHeight = 0;

        for (int i = 0; i < lineInfoList.size(); i++) {
            LineInfo lineInfo = lineInfoList.get(i);

            if (isRtl) {
                layoutLineRtl(lineInfo, curHeight);
            } else {
                layoutLineLtr(lineInfo, curHeight);
            }

            curHeight += lineInfo.lineHeight + mVerticalGap;
        }
    }

    /**
     * LTR模式布局单行：子控件从左到右，整行靠右对齐
     */
    private void layoutLineLtr(LineInfo lineInfo, int top) {
        int layoutWidth = getMeasuredWidth();
        int rightMargin = layoutWidth - lineInfo.lineWidth;
        int currentX = rightMargin;

        for (View child : lineInfo.views) {
            if (child.getVisibility() != VISIBLE) continue;

            child.layout(currentX, top,
                    currentX + child.getMeasuredWidth(),
                    top + child.getMeasuredHeight());

            currentX += child.getMeasuredWidth() + mHorizontalGap;
        }
    }

    /**
     * RTL模式布局单行：子控件从右到左，整行靠左对齐
     */
    private void layoutLineRtl(LineInfo lineInfo, int top) {
        int currentX = lineInfo.lineWidth;

        for (View child : lineInfo.views) {
            if (child.getVisibility() != VISIBLE) continue;

            currentX -= child.getMeasuredWidth();
            child.layout(currentX, top,
                    currentX + child.getMeasuredWidth(),
                    top + child.getMeasuredHeight());

            currentX -= mHorizontalGap;
        }
    }

    // 添加RTL判断方法
    private boolean isRtl() {
        return ChatUtils.isRtl(getContext());
    }

    public void setHorizontalGap(int horizontalGap) {
        this.mHorizontalGap = horizontalGap;
        requestLayout();
    }

    public void setVerticalGap(int verticalGap) {
        this.mVerticalGap = verticalGap;
        requestLayout();
    }

    /**
     * 行信息
     */
    private static class LineInfo {
        List<View> views;
        int lineWidth;
        int lineHeight;
        int typeIndex; // 所属类型索引
    }

    /**
     * 类型信息
     */
    private static class TypeInfo {
        int startLine;
        int endLine;
    }
}