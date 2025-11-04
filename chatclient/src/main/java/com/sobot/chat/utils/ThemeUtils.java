package com.sobot.chat.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.api.model.ZhiChiInitModeBase;

import java.util.Locale;

/**
 * 主题工具类
 */
public class ThemeUtils {
    /**
     * 是否更改了主题色
     *
     * @return true 更改了， false 未更改使用默认的主题色
     */
    public static boolean isChangedThemeColor(Context context) {
        return true;
    }

    /**
     * 返回当前主题色
     *
     * @return 返回的是color int 值
     */
    public static int getThemeColor(Context context) {
        if (context == null) {
            return Color.parseColor("#4ADABE");
        }
        try {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null && !StringUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
                String[] rebotTheme = initMode.getVisitorScheme().getRebotTheme().split(",");
                if (rebotTheme.length >= 1) {
                    return Color.parseColor(rebotTheme[rebotTheme.length - 1]);
                } else {
                    return Color.parseColor(rebotTheme[0]);
                }
            }
        } catch (Exception ignored) {
        }
        return context.getResources().getColor(R.color.sobot_color);
    }


    /**
     * 返回是颜色 int
     * 获取主题色气泡或者按钮上边的文字 图标颜色 0:自适应，1：白色#FFFFFF：2：黑色 #1616161
     */
    public static int getThemeTextAndIconColor(Context context) {
        if (context == null) {
            return Color.WHITE;
        }
        try {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null) {
                if (StringUtils.isNoEmpty(initMode.getVisitorScheme().getRebotThemeBack())) {
                    return Color.parseColor(initMode.getVisitorScheme().getRebotThemeBack());
                }
            }
        } catch (Exception ignored) {
        }
        return context.getResources().getColor(R.color.sobot_color_white);
    }


    /**
     * 返回是类型,不是颜色
     * 获取导航条上边的文字 图标颜色
     *
     * @return 0 白色 1黑色 默认是白色
     */
    public static int getToolBarTextAndIconColorType(Context context) {
        if (context == null) {
            return 0;
        }
        try {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null) {
                if (StringUtils.isNoEmpty(initMode.getVisitorScheme().getTopBarFontIconColor())) {
                    if ("#ffffff".equals(initMode.getVisitorScheme().getTopBarFontIconColor().toLowerCase())||"#FFFFFF".equals(initMode.getVisitorScheme().getTopBarFontIconColor())) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * 修改主题模式 跟随系统、浅色、深色
     *
     * @param context
     * @return 是否和本地不一致，不一致的话，页面还需要进行修改后才能用
     */
    public static boolean updateThemeStyle(Context context) {
        if (context != null) {
            try {
                ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                        ZhiChiConstant.sobot_last_current_initModel);
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //后台返回的主题模式RebotThemeStyle 0-浅色，1-深色，2-跟随系统
                    int rebotThemeStyle = initMode.getVisitorScheme().getRebotThemeStyle();
                    if (rebotThemeStyle == 2) {
                        rebotThemeStyle = -1;
                    } else if (rebotThemeStyle == 0) {
                        rebotThemeStyle = 1;
                    } else if (rebotThemeStyle == 1) {
                        rebotThemeStyle = 2;
                    } else {
                        rebotThemeStyle = -1;
                    }
                    //本地模式
                    int local_night_mode = SharedPreferencesUtil.getIntData(context, ZCSobotConstant.LOCAL_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    if (rebotThemeStyle != local_night_mode) {
                        int appCompatDelegate;
                        if (rebotThemeStyle == 1) {
                            appCompatDelegate = AppCompatDelegate.MODE_NIGHT_NO;//强制使用浅色
                        } else if (rebotThemeStyle == 2) {
                            appCompatDelegate = AppCompatDelegate.MODE_NIGHT_YES;//强制使用深色
                        } else {
                            appCompatDelegate = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        }
                        SharedPreferencesUtil.saveIntData(context, ZCSobotConstant.LOCAL_NIGHT_MODE, appCompatDelegate);
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }


    /**
     * 返回当前超链接颜色
     *
     * @return 返回的是color int 值
     */
    public static int getLinkColor(Context context) {
        try {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null && !StringUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
            }
        } catch (Exception ignored) {
        }
        return context.getResources().getColor(R.color.sobot_color_link);
    }

    /**
     * 返回半透明
     *
     * @param alpha 128半透明 1-255之间
     * @return 返回的是color int 值
     */
    public static int modifyAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * 修改图片颜色
     *
     * @param drawable  图片
     * @param colorName 颜色值 例如：#909090
     *                  btn_model_voice.setBackground(ImageUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_vioce_button_selector),"#909090"));
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
     *
     * @param drawable 图片
     * @param color    颜色
     *                 btn_model_voice.setBackground(ImageUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_vioce_button_selector),R.color.sobot_color));
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
     *
     * @param drawable 图片
     * @param colorId  颜色-资源文件 R.color.
     *                 btn_model_edit.setBackground(ImageUtils.applyColorToDrawable(getSobotActivity(),getResources().getDrawable(R.drawable.sobot_keyboard_button_selector),R.color.sobot_color));
     */
    public static Drawable applyColorToDrawable(Context context, Drawable drawable, int colorId) {
        if (drawable != null) {
            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(context.getResources().getColor(colorId),
                    PorterDuff.Mode.SRC_ATOP);
            drawable.setColorFilter(porterDuffColorFilter);
        }
        return drawable;
    }

    /**
     * 为颜色 添加透明度
     *
     * @param alpha 十六机制透明度
     */
    public static int addAlphaToColor(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * 自定义绘制字符串为图像
     *
     * @param name            输入的字符串
     * @param width           图像宽度
     * @param height          图像高度
     * @param backgroundColor 背景颜色
     * @param textColor       文字颜色
     * @return Drawable对象
     */
    public static Drawable createTextImageDrawable(Context context, String name, int width, int height, int backgroundColor, int textColor) {
        try {
            if (TextUtils.isEmpty(name) || context == null) {
                return null;
            }

            // 获取首字母并转为大写
            String firstLetter = name.substring(0, 1).toUpperCase(Locale.getDefault());

            // 创建一个Bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // 创建画笔
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);

            // 绘制背景
            paint.setColor(backgroundColor);
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);

            // 绘制文字
            paint.setColor(textColor);
            paint.setTextSize(height / 2);
            paint.setTextAlign(Paint.Align.CENTER);

            // 计算文字居中绘制的Y坐标
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float top = fontMetrics.top;
            float bottom = fontMetrics.bottom;
            int baseLineY = (int) (height / 2 - top / 2 - bottom / 2);

            canvas.drawText(firstLetter, width / 2, baseLineY, paint);

            // 将Bitmap转换为Drawable
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            LogUtils.e("createTextImageDrawable error: " + e.getMessage());
            // 出现异常时返回null或者默认的drawable
            return null;
        }
    }


    /**
     * 判断当前APP是否为夜间模式
     *
     * @return true为夜间模式，false为白天模式
     */
    public static boolean isAppNightMode(Context activity) {
        if (activity != null) {
            // 检查本地存储的夜间模式设置
            int localNightMode = SharedPreferencesUtil.getIntData(
                    activity,
                    ZCSobotConstant.LOCAL_NIGHT_MODE,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            );
            switch (localNightMode) {
                case AppCompatDelegate.MODE_NIGHT_YES:
                    return true;
                case AppCompatDelegate.MODE_NIGHT_NO:
                    return false;
                case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                default:
                    return isSystemNightMode(activity);
            }
        }
        return false;
    }

    /**
     * 判断系统是否为夜间模式
     *
     * @return true为夜间模式，false为白天模式
     */
    public static boolean isSystemNightMode(Context activity) {
        if (activity != null) {
            int uiMode = activity.getResources().getConfiguration().uiMode;
            return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }
}
