package com.sobot.chat.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 设置recycle行间距
 */
public class SobotGridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount; // 列数
    private int spacing; // 间距（单位：px）
    private boolean includeEdge; // 是否包含边缘（父布局与第一个/最后一个item的间距）

    // 构造方法：列数、间距（px）、是否包含边缘
    public SobotGridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 获取当前item的位置
        int position = parent.getChildAdapterPosition(view);
        // 计算当前item所在的列（0-based）
        int column = position % spanCount;

        if (includeEdge) {
            // 包含边缘：左右间距 = spacing - column * spacing / spanCount
            outRect.left = spacing - column * spacing / spanCount;
            // 右间距 = (column + 1) * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount;

            // 第一行的item顶部有间距
            if (position < spanCount) {
                outRect.top = spacing;
            }
            // 所有item底部都有间距
            outRect.bottom = spacing;
        } else {
            // 不包含边缘：左右间距按列分配
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;

            // 第一行的item顶部无间距
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}

