package com.sobot.chat.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class HorizontalItemSpacingDecoration extends RecyclerView.ItemDecoration {
    private final int spacing; // 间距大小（单位：px）

    // 构造方法：传入间距（建议在代码中用dp转px，避免适配问题）
    public HorizontalItemSpacingDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 获取当前item的位置
        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        // 横向滑动时，左右间距控制item之间的距离
        // 第一个item左侧无间距，最后一个item右侧无间距（可选，根据需求调整）
        if (position == 0) {
            // 第一个item：只设置右侧间距
            outRect.left = 0;
            outRect.right = spacing / 2; // 与下一个item平分间距（避免总间距过大）
        } else if (position == itemCount - 1) {
            // 最后一个item：只设置左侧间距
            outRect.left = spacing / 2;
            outRect.right = 0;
        } else {
            // 中间item：左右各占一半间距
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }

        // 如需上下间距，可添加：
        // outRect.top = spacing / 2;
        // outRect.bottom = spacing / 2;
    }
}
