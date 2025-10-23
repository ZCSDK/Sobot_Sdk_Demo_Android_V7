package com.sobot.chat.widget.recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 网格布局间距装饰器
 * 支持分别设置水平和垂直方向的间距
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;        // 网格列数
    private int horizontalSpacing; // 水平间距
    private int verticalSpacing;   // 垂直间距
    private boolean includeEdge;   // 是否包含边缘间距

    /**
     * 构造函数 - 使用相同的水平和垂直间距
     *
     * @param spanCount   网格列数
     * @param spacing     间距（水平和垂直方向相同）
     * @param includeEdge 是否包含边缘间距
     */
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.horizontalSpacing = spacing;
        this.verticalSpacing = spacing;
        this.includeEdge = includeEdge;
    }

    /**
     * 构造函数 - 分别设置水平和垂直间距
     *
     * @param spanCount         网格列数
     * @param horizontalSpacing 水平间距
     * @param verticalSpacing   垂直间距
     * @param includeEdge       是否包含边缘间距
     */
    public GridSpacingItemDecoration(int spanCount, int horizontalSpacing, int verticalSpacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // 获取item位置
        int column = position % spanCount; // 获取item所在列

        if (includeEdge) {
            // 包含边缘间距的计算方式
            // 左间距 = 水平间距 - (当前列 * 水平间距 / 列数)
            outRect.left = horizontalSpacing - column * horizontalSpacing / spanCount;
            // 右间距 = (当前列 + 1) * 水平间距 / 列数
            outRect.right = (column + 1) * horizontalSpacing / spanCount;

            // 顶部间距 - 第一行顶部需要间距
            if (position < spanCount) {
                outRect.top = verticalSpacing;
            }
            // 底部间距 - 所有item底部都有间距
            outRect.bottom = verticalSpacing;
        } else {
            // 不包含边缘间距的计算方式
            // 左间距 = 当前列 * 水平间距 / 列数
            outRect.left = column * horizontalSpacing / spanCount;
            // 右间距 = 水平间距 - (当前列 + 1) * 水平间距 / 列数
            outRect.right = horizontalSpacing - (column + 1) * horizontalSpacing / spanCount;

            // 顶部间距 - 非第一行的item顶部需要间距
            if (position >= spanCount) {
                outRect.top = verticalSpacing;
            }
        }
    }
}
