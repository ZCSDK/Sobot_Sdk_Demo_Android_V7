package com.sobot.chat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.sobot.chat.utils.ChatUtils;

/**
 * 阿语布局下 从右到左阅读
 */
public class SobotTextView extends AppCompatTextView {
    public SobotTextView(Context context) {
        super(context);
        init();
    }

    public SobotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        try {
            boolean isRtl = ChatUtils.isRtl(getContext());

            // 获取当前的gravity值
            int currentGravity = getGravity();

            // 分离水平和垂直对齐方式
            int horizontalGravity = currentGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            int verticalGravity = currentGravity & Gravity.VERTICAL_GRAVITY_MASK;

            // 处理水平对齐
            if (isRtl) {
                // 如果原来不是居中或特定对齐，才改为END对齐
                if (horizontalGravity == 0 || horizontalGravity == Gravity.LEFT || horizontalGravity == Gravity.START) {
                    horizontalGravity = Gravity.END;
                }
            } else {
                // 如果原来不是居中或特定对齐，才改为START对齐
                if (horizontalGravity == 0 || horizontalGravity == Gravity.RIGHT || horizontalGravity == Gravity.END) {
                    horizontalGravity = Gravity.START;
                }
            }

            // 处理垂直对齐：只在没有显式设置时使用TOP
            // 关键改进：检查是否是用户显式设置的vertical gravity
            // 如果verticalGravity是0（默认）或者系统默认值，才设置为TOP
            // 如果用户显式设置了center_vertical、top、bottom等，就保留原设置
            if (verticalGravity == 0) {
                // 没有设置任何vertical gravity，使用TOP
                verticalGravity = Gravity.TOP;
            }
            // 注意：不再强制覆盖 Gravity.CENTER_VERTICAL，让用户设置生效

            // 设置最终的gravity
            setGravity(horizontalGravity | verticalGravity);

            setTextDirection(isRtl ? View.TEXT_DIRECTION_ANY_RTL : View.TEXT_DIRECTION_LTR);
            setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        } catch (Exception ignored) {
        }
    }
}
