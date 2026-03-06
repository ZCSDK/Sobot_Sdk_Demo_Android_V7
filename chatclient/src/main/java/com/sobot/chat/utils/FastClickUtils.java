package com.sobot.chat.utils;

public class FastClickUtils {
    // 优化后的默认防重复点击时间间隔
    public static final int DEFAULT_CLICK_DELAY = 500; // 从2000ms优化为500ms
    public static final int LONG_OPERATION_DELAY = 1000; // 长操作使用1000ms

    private static long lastClickTime;

    /**
     * 使用默认时间间隔检查是否快速点击
     *
     * @return true 可以点击 false 不可以（被防重复机制拦截）
     */
    public static boolean isCanClick() {
        return isCanClick(DEFAULT_CLICK_DELAY);
    }

    /**
     * 检查是否快速点击（自定义时间间隔）
     *
     * @param delayTime 延迟时间（毫秒）
     * @return true 可以点击 false 不可以
     */
    public static boolean isCanClick(int delayTime) {
        long curClickTime = System.currentTimeMillis();
        boolean canClick = (curClickTime - lastClickTime) >= delayTime;
        if (canClick) {
            lastClickTime = curClickTime;
        }
        return canClick;
    }
}
