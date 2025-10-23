package com.sobot.chat.widget.switchkeyboardlib.util;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SequentialClickListener implements View.OnClickListener {
    public interface OnSequentialClickListener {
        /**
         * 点击事件处理
         * @param v View
         * @return true 继续执行，false 中断执行
         */
        boolean onSequentialClick(View v);
    }

    private List<Object> listeners = new ArrayList<>();

    public void addListener(View.OnClickListener listener) {
        listeners.add(listener);
    }

    public void addListener(OnSequentialClickListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onClick(View v) {
        for (Object listener : listeners) {
            boolean continueExecution = true;

            if (listener instanceof OnSequentialClickListener) {
                continueExecution = ((OnSequentialClickListener) listener).onSequentialClick(v);
            } else if (listener instanceof View.OnClickListener) {
                ((View.OnClickListener) listener).onClick(v);
            }

            if (!continueExecution) {
                break;
            }
        }
    }
}