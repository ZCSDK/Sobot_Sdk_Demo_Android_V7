package com.sobot.chat.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiInitModeBase;

/**
 * @author: Sobot
 * 2022/9/15
 */
public class ThemeUtils {
    /**
     * 是否更改了主题色
     *
     * @return true 更改了， false 未更改使用默认的主题色
     */
    public static boolean isChangedThemeColor(Context context) {
        //判断是否更改了主题色
        if (context.getResources().getColor(R.color.sobot_color) != Color.parseColor("#0DAEAF")) {
            return true;
        }else {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null && !StringUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
                String[] rebotTheme = initMode.getVisitorScheme().getRebotTheme().split(",");
                if (rebotTheme != null && rebotTheme.length >= 1 && Color.parseColor("#0daeaf") != Color.parseColor(rebotTheme[rebotTheme.length - 1])) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 返回当前主题色
     *
     * @return 返回的是color int 值
     */
    public static int getThemeColor(Context context) {
        if (context.getResources().getColor(R.color.sobot_color) != Color.parseColor("#0daeaf")) {
            return context.getResources().getColor(R.color.sobot_color);
        }
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_initModel);
        if (initMode != null && initMode.getVisitorScheme() != null && !StringUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
            String[] rebotTheme = initMode.getVisitorScheme().getRebotTheme().split(",");
            if (rebotTheme != null && rebotTheme.length >= 1) {
                if (Color.parseColor(rebotTheme[rebotTheme.length - 1]) != Color.parseColor("#0daeaf")) {
                    return Color.parseColor(rebotTheme[rebotTheme.length - 1]);
                }
            }
        }
        return context.getResources().getColor(R.color.sobot_color);
    }
    /**
     * 修改图片颜色
     *@param drawable 图片
     *@param colorName 颜色值 例如：#909090
     * btn_model_voice.setBackground(ImageUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_vioce_button_selector),"#909090"));
     *
     */
    public static Drawable applyColorToDrawable(Drawable drawable, String colorName) {
        if (drawable != null) {
            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(Color.parseColor(colorName),
                    PorterDuff.Mode.SRC_ATOP);
            drawable.setColorFilter(porterDuffColorFilter);
        }
        return drawable;
    }
    /**
     * 修改图片颜色
     *@param drawable 图片
     *@param color 颜色
     * btn_model_voice.setBackground(ImageUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_vioce_button_selector),R.color.sobot_color));
     *
     */
    public static Drawable applyColorToDrawable(Drawable drawable, int color) {
        if (drawable != null) {
            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(color,
                    PorterDuff.Mode.SRC_ATOP);
            drawable.setColorFilter(porterDuffColorFilter);
        }
        return drawable;
    }
    /**
     * 修改图片颜色
     *@param drawable 图片
     *@param colorId 颜色-资源文件 R.color.
     * btn_model_edit.setBackground(ImageUtils.applyColorToDrawable(getSobotActivity(),getResources().getDrawable(R.drawable.sobot_keyboard_button_selector),R.color.sobot_color));
     *
     */
    public static Drawable applyColorToDrawable(Context context, Drawable drawable, int colorId) {
        if (drawable != null) {
            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(context.getResources().getColor(colorId),
                    PorterDuff.Mode.SRC_ATOP);
            drawable.setColorFilter(porterDuffColorFilter);
        }
        return drawable;
    }
}
