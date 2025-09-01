package com.sobot.chat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.sobot.chat.R;
import com.sobot.chat.utils.ScreenUtils;

/**
 * 工单评价五星 回显
 */
public class SobotFiveStarsSmallLayout extends LinearLayout {


    private LinearLayout line1;

    public SobotFiveStarsSmallLayout(Context context) {
        super(context);
    }

    public SobotFiveStarsSmallLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SobotFiveStarsSmallLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isInit() {
        return null == line1;
    }

    /**
     * @param defScore    显示个数
     */
    public void init(int defScore) {
        if (null == line1) {
            line1 = new LinearLayout(getContext());
            line1.setOrientation(LinearLayout.HORIZONTAL);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.START;
            line1.setLayoutParams(layoutParams);
            addView(line1);
        }

        LayoutParams lp = null;
        for (int i = 0; i < 5; i++) {
            ImageView imageView = new ImageView(getContext());
            lp = new LayoutParams(ScreenUtils.dip2px(getContext(), 15),
                    ScreenUtils.dip2px(getContext(), 15));
            if (i != 4) {
                lp.setMarginEnd(ScreenUtils.dip2px(getContext(), 10f));
            } else {
                lp.setMarginEnd(0);
            }
            imageView.setLayoutParams(lp);
            if (i < defScore) {
                imageView.setImageResource(R.drawable.sobot_evaluate_star_full);
            } else {
                imageView.setImageResource(R.drawable.sobot_evaluate_star_empty);
            }
            final int position = i;
            line1.addView(imageView);
        }

    }

}