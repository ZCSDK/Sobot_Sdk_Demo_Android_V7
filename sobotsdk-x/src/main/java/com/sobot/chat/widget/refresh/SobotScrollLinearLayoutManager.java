package com.sobot.chat.widget.refresh;

import android.content.Context;
import android.graphics.PointF;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.utils.LogUtils;

/**
 * 调整RecyclerView的滚动速度 滚动距离越远，速度越快
 */
public class SobotScrollLinearLayoutManager extends LinearLayoutManager {

    public SobotScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SobotScrollLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                // 第一个可见位置
                int firstItem = findFirstVisibleItemPosition();
                int diff = Math.abs(position - firstItem);
                // 将 diff 作分母：滚动距离越远，速度越快。 (100f/diff) 数值如果过小会导致速度过快, 可以再乘一个速度因子变量(speedFactor)来调整
                int speedFactor = 2;
                float speed = (100f / diff) * speedFactor;
                if (speed > 20) {
                    speed = 20f;
                }
                LogUtils.i("滚动速率" + speed);
                return speed / displayMetrics.densityDpi;
            }

            @Override
            protected int getHorizontalSnapPreference() {
                return SNAP_TO_START;
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;  // 将子view与父view顶部对齐
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}