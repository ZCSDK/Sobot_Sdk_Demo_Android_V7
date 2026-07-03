package com.sobot.chat.camera.util;


import com.sobot.chat.utils.LogUtils;

public class StCmeraLog {

    private static final String DEFAULT_TAG = "sobotCamera";
    private static final boolean isDebug = false;

    public static void i(String tag, String msg) {
        if (isDebug)
            LogUtils.i(msg);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            LogUtils.v(msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            LogUtils.d(msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            LogUtils.e(msg);
    }

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    /*public static void v(String msg) {
        v(DEFAULT_TAG, msg);
    }

    public static void d(String msg) {
        d(DEFAULT_TAG, msg);
    }

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }*/
}
