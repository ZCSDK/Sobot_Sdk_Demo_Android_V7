package com.sobot.chat.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 留言列表 / 模板列表 宽屏双列网格间距处理。
 * <p>
 * 仅用于 {@code GridLayoutManager(spanCount = 2)} 场景：
 * - 列间距由参数 {@code horizontalSpace} 控制（左右各分一半，让两列等宽）
 * - 行间距由参数 {@code verticalSpace} 控制（除首行外，每个 item 顶部加间距）
 * - 自动识别 RTL，镜像左右偏移
 */
public class SobotTicketGridItemDecoration extends RecyclerView.ItemDecoration {

    private final int horizontalSpace;
    private final int verticalSpace;

    public SobotTicketGridItemDecoration(int horizontalSpace, int verticalSpace) {
        this.horizontalSpace = horizontalSpace;
        this.verticalSpace = verticalSpace;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        int half = horizontalSpace / 2;
        boolean isLeftCol = (position % 2) == 0;
        boolean isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
        // 左列：右内边距 half；右列：左内边距 half。RTL 翻转
        if (isLeftCol) {
            outRect.left = isRtl ? half : 0;
            outRect.right = isRtl ? 0 : half;
        } else {
            outRect.left = isRtl ? 0 : half;
            outRect.right = isRtl ? half : 0;
        }
        // 第一行不加 top，其余行加 verticalSpace
        outRect.top = position < 2 ? 0 : verticalSpace;
        outRect.bottom = 0;
    }
}
