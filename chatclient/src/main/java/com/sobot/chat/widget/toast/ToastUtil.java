package com.sobot.chat.widget.toast;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ToastUtil {
    private static Toast toast;

    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof OnAfterShowListener) {
                OnAfterShowListener listener = (OnAfterShowListener) msg.obj;
                listener.doAfter();
            }
        }
    };

    /**
     * @param text
     */
    public static void showToast(Context context, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
            CustomToast.makeText(context, text, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param text
     */
    public static void showLongToast(Context context, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (context == null) {
            return;
        }
        context = context.getApplicationContext();
        if (toast == null) {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        } else {
            toast.setText(text);//如果不为空，则直接改变当前toast的文本
        }
        try {
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义的土司
     *
     * @param context
     * @param str
     */
    public static void showCustomToast(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
            CustomToast.makeText(context, str, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param text
     */
    public static void showCustomLongToast(Context context, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
            CustomToast.makeText(context, text, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 自定义的带图片的土司
     *
     * @param context
     * @param str
     * @param resId
     */
    public static void showCustomToast(Context context, String str, int resId) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
            CustomToast.makeText(context, str, Toast.LENGTH_SHORT, resId).show();
        } catch (Exception e) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 自定义土司，显示固定时间,然后执行监听方法
     *
     * @param context
     * @param str
     */
    public static void showCustomToastWithListenr(Context context, String str, long showTime, final OnAfterShowListener onAfterShowListener) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        if (context == null) {
            return;
        }
        try {
            CustomToast.makeText(context, str, Toast.LENGTH_SHORT).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (onAfterShowListener != null)
                        doListener(onAfterShowListener);
                }
            }, showTime);//延时执行
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }

    public static void doListener(OnAfterShowListener onAfterShowListener) {
        Message message = mHandler.obtainMessage();
        message.obj = onAfterShowListener;
        mHandler.sendMessage(message);
    }


    public interface OnAfterShowListener {
        void doAfter();
    }
}