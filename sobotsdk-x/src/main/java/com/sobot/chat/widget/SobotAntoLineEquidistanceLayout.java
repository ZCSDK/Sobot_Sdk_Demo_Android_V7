package com.sobot.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动换行布局,等距，一行只有一个按钮，
 * 用于商品卡片中的按钮菜单
 */
public class SobotAntoLineEquidistanceLayout extends ViewGroup {

    public static final int MODE_FILL_PARENT = 0;
    public static final int MODE_WRAP_CONTENT = 1;

    private int mVerticalGap = 16;
    private int mHorizontalGap = 10;

    private int mFillMode = MODE_FILL_PARENT;

    private List<Integer> childOfLine; //Save the count of child views of each line;
    private List<Integer> mOriginWidth;
    private int maxWight=0;//最大宽度
    private boolean isChange;// 是否需要换行
    private int themeColor ;

    public SobotAntoLineEquidistanceLayout(Context context) {
        super(context);
        mOriginWidth = new ArrayList<>();
        themeColor = ThemeUtils.getThemeColor(getContext());
    }

    public SobotAntoLineEquidistanceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOriginWidth = new ArrayList<>();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_autoWrapLineLayout);
        mHorizontalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_horizontalGap, 0);
        mVerticalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_verticalGap, 0);
        mFillMode = ta.getInteger(R.styleable.sobot_autoWrapLineLayout_sobot_fillMode, 0);
        ta.recycle();
    }

    public SobotAntoLineEquidistanceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOriginWidth = new ArrayList<>();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_autoWrapLineLayout);
        mHorizontalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_horizontalGap, 0);
        mVerticalGap = ta.getDimensionPixelSize(R.styleable.sobot_autoWrapLineLayout_sobot_verticalGap, 0);
        mFillMode = ta.getInteger(R.styleable.sobot_autoWrapLineLayout_sobot_fillMode, 0);
        ta.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mFillMode == MODE_FILL_PARENT) {
            layoutModeFillParent();
        } else {
            layoutWrapContent();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childOfLine = new ArrayList<>();
        int childCount = getChildCount();
        int totalHeight = 0,tempTotalH=0;
        int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int curLineChildCount = 0;
        int curLineWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View childItem = getChildAt(i);
            if (mFillMode == MODE_FILL_PARENT) {
                if (mOriginWidth.size() <= i) {
                    measureChild(childItem, widthMeasureSpec, heightMeasureSpec);
                    mOriginWidth.add(childItem.getMeasuredWidth());
                } else {
                    childItem.measure(MeasureSpec.makeMeasureSpec(mOriginWidth.get(i), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(childItem.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
            } else {
                measureChild(childItem, widthMeasureSpec, heightMeasureSpec);
            }
            int childHeight = childItem.getMeasuredHeight();
            int childWidth = childItem.getMeasuredWidth();
            if (curLineWidth + childWidth <= totalWidth) {
                curLineWidth += childWidth;
                maxHeight = Math.max(childHeight, maxHeight);
                curLineChildCount++;
            } else {
                childOfLine.add(curLineChildCount);
                curLineWidth = childWidth;
                curLineChildCount = 1;
                totalHeight += maxHeight;
                maxHeight = childHeight;
            }
            if(i>0)
                tempTotalH += childHeight;
        }
        childOfLine.add(curLineChildCount);
        for (int i = 0; i < childOfLine.size(); i++) {
            if (childOfLine.get(i) == 0) {
                childOfLine.remove(i);
            }
        }
        List<Integer> childLines = new ArrayList<>();
        if(childOfLine.size()>1) {
            for (int i = 0; i < childOfLine.size(); i++) {
                int tempCount = childOfLine.get(i);
                if (tempCount > 1) {
                    for (int j = 0; j < tempCount; j++) {
                        childLines.add(1);
                    }
                } else {
                    childLines.add(1);
                }
            }
        }
        if(childLines.size()>childOfLine.size()) {
            childOfLine.clear();
            childOfLine.addAll(childLines);
            totalHeight += (mVerticalGap * (childOfLine.size() - 1) + tempTotalH);
        }else {
            totalHeight += (mVerticalGap * (childOfLine.size() - 1) + maxHeight);
        }
        setMeasuredDimension(totalWidth, totalHeight);
    }

    private void layoutModeFillParent() {
        int index = 0;
        int width = getMeasuredWidth();
        int curHeight = 0;
        for (int i = 0; i < childOfLine.size(); i++) {
            int childCount = childOfLine.get(i);
            int maxHeight = 0;
            int lineWidth = 0;
            for (int j = 0; j < childCount; j++) {
                lineWidth += getChildAt(j + index).getMeasuredWidth();
            }
            int padding = (width - lineWidth - mHorizontalGap * (childCount - 1)) / childCount / 2;
            lineWidth = 0;
            int target = index + childCount;
            for (; index < target; index++) {
                View item = getChildAt(index);
                maxHeight = Math.max(maxHeight, item.getMeasuredHeight());
                item.setPadding(padding, item.getPaddingTop(),
                        padding, item.getPaddingBottom());
                item.measure(MeasureSpec.makeMeasureSpec(item.getMeasuredWidth() + padding * 2, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(item.getMeasuredHeight(), MeasureSpec.EXACTLY));
                item.layout(lineWidth, curHeight, lineWidth + item.getMeasuredWidth(), curHeight + item.getMeasuredHeight());
                lineWidth += item.getMeasuredWidth() + mHorizontalGap;
            }
            curHeight += maxHeight + mVerticalGap;
        }
    }

    private void layoutWrapContent() {
        int index = 0;
        int curHeight = 0;
        int wight = getWidth();
        if(childOfLine.size() == 1){
            //如果一行超过1个，宽度平分
            int childCount = childOfLine.get(0);
            int maxHeight = 0;
            int lineWidth = 0;
            int curWidth = (maxWight - (mHorizontalGap * (childCount - 1))) / childCount;
            int target = index + childCount;
            for (; index < target; index++) {
                View item = getChildAt(index);
                maxHeight = Math.max(maxHeight, item.getMeasuredHeight());
                item.layout(lineWidth, curHeight, lineWidth + curWidth, curHeight + item.getMeasuredHeight());
                lineWidth += curWidth + mHorizontalGap;
            }
            curHeight += maxHeight + mVerticalGap;

        }else {
            //如果多行，每行显示一个，宽度是最大宽度
            for (int i = 0; i < childOfLine.size(); i++) {
                int childCount = childOfLine.get(i);
                int maxHeight = 0;
                int lineWidth = 0;
                int target = index + childCount;
                for (; index < target; index++) {
                    View item = getChildAt(index);
                    maxHeight = Math.max(maxHeight, item.getMeasuredHeight());
                    item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    item.layout(lineWidth, curHeight, maxWight, curHeight + item.getMeasuredHeight());
                    lineWidth += item.getMeasuredWidth() + mHorizontalGap;
                }
                curHeight += maxHeight + mVerticalGap;
            }
        }
    }
    private void setBtnBg(View view, boolean isFrist){
        if(view instanceof TextView) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.sobot_evaluate_commit_selector);
            drawable = ThemeUtils.applyColorToDrawable(drawable, themeColor);
            if (isFrist) {
                view.setBackground(drawable);
                ((TextView) view).setTextColor(getContext().getResources().getColor(R.color.sobot_color_white));
            } else {
                view.setBackground(getContext().getResources().getDrawable(R.drawable.sobot_btn_bg_white_4));
                ((TextView) view).setTextColor(getContext().getResources().getColor(R.color.sobot_goods_title_text_color));
            }
        }
    }

    public void setMaxWight(int maxWight) {
        this.maxWight = maxWight;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    public void setFillMode(int fillMode) {
        this.mFillMode = fillMode;
    }

    public void setHorizontalGap(int horizontalGap) {
        this.mHorizontalGap = horizontalGap;
    }

    public void setVerticalGap(int verticalGap) {
        this.mVerticalGap = verticalGap;
    }

}