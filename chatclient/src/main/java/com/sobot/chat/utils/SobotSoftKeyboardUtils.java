package com.sobot.chat.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * 软件键盘管理
 * 如果需要按钮控制键盘显示或隐藏状态，前提条件必须要有焦点，不然则无效
 */
public class SobotSoftKeyboardUtils {


    /**
     * 键盘状态
     *
     * @param activity
     * @return true显示 false隐藏
     */
    public static boolean getSoftKeyboardStatus(Activity activity) {
        //获取当前屏幕内容的高度
        int screenHeight = activity.getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return screenHeight - rect.bottom != 0;
    }

    /**
     * 根据键盘状态显示或隐藏键盘
     *
     * @param activity
     */
    public static void showOrHideSoftKeyboard(Activity activity) {
        if (getSoftKeyboardStatus(activity)) {
            hideKeyboard(activity);
        } else {
            showSoftKeyboard(activity);
        }
    }

    /**
     * 软键盘显示
     *
     * @param activity
     */

    public static void showSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 软键盘隐藏
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * edit获取焦点但不显示软键盘
     *
     * @param editText
     */
    public static void disableShowInput(EditText editText) {
        if (Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {//TODO: handle exception
            }
        }
    }
}
