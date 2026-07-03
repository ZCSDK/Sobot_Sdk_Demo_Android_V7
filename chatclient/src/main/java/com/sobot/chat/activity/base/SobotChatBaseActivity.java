package com.sobot.chat.activity.base;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotSelectPicAndVideoActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.PermissionListener;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.IOUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotPathManager;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.immersionbar.BarHide;
import com.sobot.chat.widget.immersionbar.SobotImmersionBar;
import com.sobot.chat.widget.toast.ToastUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

public abstract class SobotChatBaseActivity extends AppCompatActivity {

    public ZhiChiApi zhiChiApi;

    protected File cameraFile;

    //权限回调
    public PermissionListener permissionListener;
    private int initMode;
    private View overlay;//权限用途提示蒙层
    private ViewGroup viewGroup;//根view content
    //是否横屏
    public boolean isLandscapeScreen = false;
    public String REQUEST_TAG = "Sobot";
    public boolean isContinueShooting = false;//是否点击过继续拍摄
    //上次的屏幕宽度 dp，用于检测折叠屏折叠/展开切换，触发 recreate 让 w 资源限定符生效
    private int mLastScreenWidthDp = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);//竖屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//横屏
                }
            }
            if (getSobotBaseContext() != null && getDelegate() != null) {
                //暗夜模式设置：默认跟随系统，可以根据设置切换
                int local_night_mode = SharedPreferencesUtil.getIntData(getSobotBaseContext(), ZCSobotConstant.LOCAL_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                if (local_night_mode != 0) {
                    getDelegate().setLocalNightMode(local_night_mode); //切换模式
                }
            }
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            initMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            mLastScreenWidthDp = getResources().getConfiguration().screenWidthDp;
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                isLandscapeScreen = true;
                // 支持显示到刘海区域
                NotchScreenManager.getInstance().setDisplayInNotch(this);
                // 设置Activity全屏
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            setContentView(getContentViewResId());
            String host = SharedPreferencesUtil.getStringData(getSobotBaseContext(), ZhiChiConstant.SOBOT_SAVE_HOST_AFTER_INITSDK, SobotBaseUrl.getApi_Host());
            if (!host.equals(SobotBaseUrl.getApi_Host())) {
                SobotBaseUrl.setApi_Host(host);
            }
            int targetSdkVersion = CommonUtils.getTargetSdkVersion(getSobotBaseActivity());
            //Android 15 底部避让
            if (Build.VERSION.SDK_INT >= 35 && targetSdkVersion >= 35) {
                try {
                    View decorView = getWindow().getDecorView();
                    ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                            View rootView = findViewById(R.id.view_root);

                            if (rootView != null) {
                                //android 15 api 35 全屏沉侵式 底部避让
                                rootView.setPadding(0, 0, 0, bottomInset);
                            }
                            LogUtils.d("底部状态栏高度:========" + bottomInset);
                            return insets;
                        }
                    });
                } catch (Exception e) {
                }
            }
            zhiChiApi = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
            MyApplication.getInstance().addActivity(this);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            View toolBar = findViewById(R.id.tl_titlebar);
            //横屏时隐藏状态栏
            if (toolBar != null) {
                SobotImmersionBar.with(this).hideBar(isLandscapeScreen ? BarHide.FLAG_HIDE_STATUS_BAR : BarHide.FLAG_SHOW_BAR).titleBar(toolBar).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
            }
            if (toolBar != null) {
                setUpToolBar();
                setUpToolBarLeftMenu();
                setUpToolBarRightMenu();
            }
            initBundleData(savedInstanceState);
            initView();
            initData();
            //左上角返回按钮水滴屏适配
            if (getLeftMenu() != null) {
                displayInNotch(getLeftMenu());
            }
        } catch (Exception e) {
            LogUtils.e("uncaught", e);
//            LogUtils.e(e.getMessage());
        }
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(this, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            // 用 rect.width() 表示刘海宽度；rect.right 是绝对 x 坐标，
                            // 刘海在右侧时 ≈ 屏幕宽度，会把 padding 撑爆导致控件坍缩
                            int notchWidth = Math.max(rect.width(), 90);
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.setMarginEnd(notchWidth + 44);
                                layoutParams.setMarginStart(notchWidth + 44);
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.setMarginEnd(notchWidth + 44);
                                layoutParams.setMarginStart(notchWidth + 44);
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding(notchWidth + view.getPaddingLeft(), view.getPaddingTop(), notchWidth + view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    /**
     * 单侧刘海屏适配：根据当前横屏方向自动只在带刘海的一侧（左或右）加 padding/margin。
     * API 28+ 走系统 {@link android.view.DisplayCutout}，旋转后由系统自动重新派发 insets；
     * API < 28 走旧 NotchScreenManager + rotation 判定，区分 home 键在左 / 在右两种横屏方向。
     */
    public void displayInNotchSingleSide(final View view) {
        if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)
                || !ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)
                || view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+：用系统 WindowInsets.getDisplayCutout()，系统会在旋转后自动派发新的 insets，
            // 自动适配双向横屏（ROTATION_90↔ROTATION_270），无需手动检测 rotation
            applyDisplayCutoutPaddingListener(view);
            return;
        }
        // API < 28：用 NotchScreenManager + rotation 判定（区分 home 键左右两种横屏方向）
        NotchScreenManager.getInstance().getNotchInfo(this, new INotchScreen.NotchScreenCallback() {
            @Override
            public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                if (!notchScreenInfo.hasNotch || notchScreenInfo.notchRects == null || notchScreenInfo.notchRects.isEmpty()) {
                    return;
                }
                // rotation == 270 时刘海在物理右侧（home 键在左），其余横屏（90）刘海在物理左侧
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                boolean notchOnRight = (rotation == Surface.ROTATION_270);
                for (Rect rect : notchScreenInfo.notchRects) {
                    int inset = Math.max(rect.width(), 90) + 14;
                    if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                        if (notchOnRight) {
                            lp.setMarginEnd(inset);
                        } else {
                            lp.setMarginStart(inset);
                        }
                        view.setLayoutParams(lp);
                    } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        if (notchOnRight) {
                            lp.setMarginEnd(inset);
                        } else {
                            lp.setMarginStart(inset);
                        }
                        view.setLayoutParams(lp);
                    } else {
                        int padInset = Math.max(rect.width(), 90);
                        if (notchOnRight) {
                            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                                    padInset + view.getPaddingRight(), view.getPaddingBottom());
                        } else {
                            view.setPadding(padInset + view.getPaddingLeft(), view.getPaddingTop(),
                                    view.getPaddingRight(), view.getPaddingBottom());
                        }
                    }
                }
            }
        });
    }

    /**
     * API 28+ 的刘海屏适配：通过 OnApplyWindowInsetsListener 让系统自动派发 DisplayCutout，
     * 旋转后由系统重新触发 insets 派发，自动切换左右侧适配方向。
     */
    /**
     * 固定宽度 view 的刘海屏避让（用 marginStart/marginEnd 推开整个 view，而非 setPadding 撑大内部）。
     *
     * 适用场景：layout_width 固定（如返回按钮 36dp）+ 内含图标的 ImageView。
     * 这类 view 用 setPadding 会导致：
     *   - paddingLeft+paddingRight 超出 view 宽 → 图标可绘制区域 ≤ 0 → 图标被压缩或消失
     *   - 即使没溢出，scaleType 仍让图标居中显示 → 视觉上图标位置不变 → 没避开刘海
     *
     * 用 marginStart 推开整个 view 才能真正让按钮（含触摸区域）避开刘海。
     * 与 displayInNotch / displayInNotchSingleSide 区别：那两个用 padding 适合自适应宽度 view（WebView、列表）。
     */
    public void applyNotchMarginToFixedSizeView(final View view) {
        if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)
                || !ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)
                || view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // API < 28：暂走 displayInNotchSingleSide 路径（rare），不再单独实现
            displayInNotchSingleSide(view);
            return;
        }
        if (!(view.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams)) {
            return;
        }
        // 用带 key 的 setTag 缓存原始 marginStart/marginEnd，避免旋转 / 重复调用时累加
        int[] base = (int[]) view.getTag(R.id.sobot_tag_origin_margin);
        if (base == null) {
            android.view.ViewGroup.MarginLayoutParams lp =
                    (android.view.ViewGroup.MarginLayoutParams) view.getLayoutParams();
            base = new int[]{lp.getMarginStart(), lp.getMarginEnd()};
            view.setTag(R.id.sobot_tag_origin_margin, base);
        }
        final int origStart = base[0];
        final int origEnd = base[1];
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public android.view.WindowInsets onApplyWindowInsets(View v, android.view.WindowInsets insets) {
                android.view.DisplayCutout cutout = insets.getDisplayCutout();
                android.view.ViewGroup.MarginLayoutParams params =
                        (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
                if (cutout != null) {
                    params.setMarginStart(origStart + cutout.getSafeInsetLeft());
                    params.setMarginEnd(origEnd + cutout.getSafeInsetRight());
                } else {
                    params.setMarginStart(origStart);
                    params.setMarginEnd(origEnd);
                }
                v.setLayoutParams(params);
                return insets;
            }
        });
        // 首次进入时主动请求一次派发；若 view 尚未 attach，requestApplyInsets 会被忽略，用 attach 监听兜底
        if (view.isAttachedToWindow()) {
            view.requestApplyInsets();
        } else {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    v.requestApplyInsets();
                    v.removeOnAttachStateChangeListener(this);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    private void applyDisplayCutoutPaddingListener(final View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        // 用带 key 的 setTag 缓存原始 padding，避免占用全局 tag 槽位被业务/三方库覆盖（覆盖会导致旋转时累加）
        int[] base = (int[]) view.getTag(R.id.sobot_tag_origin_padding);
        if (base == null) {
            base = new int[]{view.getPaddingLeft(), view.getPaddingRight()};
            view.setTag(R.id.sobot_tag_origin_padding, base);
        }
        final int origLeft = base[0];
        final int origRight = base[1];
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    v.setPadding(origLeft + cutout.getSafeInsetLeft(),
                            v.getPaddingTop(),
                            origRight + cutout.getSafeInsetRight(),
                            v.getPaddingBottom());
                } else {
                    v.setPadding(origLeft, v.getPaddingTop(), origRight, v.getPaddingBottom());
                }
                return insets;
            }
        });
        // 首次进入时主动请求一次派发；若 view 尚未 attach，requestApplyInsets 会被忽略，因此用 attach 监听兜底
        if (view.isAttachedToWindow()) {
            view.requestApplyInsets();
        } else {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    v.requestApplyInsets();
                    v.removeOnAttachStateChangeListener(this);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                }
            });
        }
    }

    public void changeAppLanguage() {
        Locale locale = (Locale) SharedPreferencesUtil.getObject(SobotChatBaseActivity.this, ZhiChiConstant.SOBOT_LANGUAGE);
        if (locale != null) {
            try {
                updateLayoutDirections(locale);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 更新布局方向以支持RTL/LTR语言
     * 根据指定的locale更新应用的资源配置和视图布局方向
     *
     * @param locale 需要应用的语言区域设置
     */
    private void updateLayoutDirections(Locale locale) {
        if (locale == null) {
            return;
        }
        try {
            // 更新资源配置
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            // 保留 screenWidthDp / densityDpi / uiMode 等原配置，避免清空引起副作用
            Configuration conf = new Configuration(res.getConfiguration());
            conf.setLocale(locale);
            // Android 13+ 部分 OEM ROM 优先读 LocaleList[0]，与 attachBaseContext 保持一致
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conf.setLocales(new LocaleList(locale));
            }
            if (!ChatUtils.isRtl(getSobotBaseActivity())) {
                //禁止镜像
                conf.setLayoutDirection(Locale.ENGLISH);//表示英语语言环境。在这个上下文中，它的作用是强制设置应用的布局方向为从左到右(LTR)。
            } else {
                conf.setLayoutDirection(locale);
            }
            res.updateConfiguration(conf, dm);

            // 更新Activity布局方向
            if (getSobotBaseActivity() != null && getSobotBaseActivity().getWindow() != null) {
                getSobotBaseActivity().getWindow().getDecorView().setLayoutDirection(conf.getLayoutDirection());
            }
            // 更新重要的子视图布局方向
            updateImportantChildViewsLayoutDirection(conf.getLayoutDirection());
        } catch (Exception e) {
        }
    }


    // 更新重要的子视图布局方向
    private void updateImportantChildViewsLayoutDirection(int layoutDirection) {
        if (layoutDirection == View.LAYOUT_DIRECTION_RTL && ChatUtils.isRtl(getSobotBaseActivity())) {
            //没有禁止镜像 同时语言对应布局是阿语镜像布局
            if (getLeftMenu() != null) {
                getLeftMenu().setImageResource(R.drawable.sobot_icon_titlebar_back_rtl);
            }

        } else {
            if (getLeftMenu() != null) {
                getLeftMenu().setImageResource(R.drawable.sobot_icon_titlebar_back);
            }
        }
    }


    protected void setUpToolBarRightMenu() {
        if (getRightMenu() != null) {
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getRightMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRightMenuClick(v);
                }
            });
        }
    }

    protected void setUpToolBarLeftMenu() {
        if (getLeftMenu() != null) {
            if (ChatUtils.isRtl(getSobotBaseActivity())) {
                getLeftMenu().setImageResource(R.drawable.sobot_icon_titlebar_back_rtl);
            } else {
                getLeftMenu().setImageResource(R.drawable.sobot_icon_titlebar_back);
            }
            getLeftMenu().setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    updateMoreBtnUi(true, getLeftMenu());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    updateMoreBtnUi(false, getLeftMenu());
                }
                return false;
            });
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getLeftMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLeftMenuClick(v);
                }
            });
        }
    }

    /**
     * 按钮的背景
     *
     * @param isShowBg 是否显示背景色 true 显示 ;false 不显示
     */
    private void updateMoreBtnUi(boolean isShowBg, ImageView imageView) {
        if (imageView != null) {
            if (isShowBg) {
                boolean isBlack = (ThemeUtils.getToolBarTextAndIconColorType(getSobotBaseActivity()) == 1);
                if (isBlack) {
                    //黑色
                    Drawable moreBgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_chat_titlebar_more_bg_black, null);
                    if (moreBgDrawable != null) {
                        imageView.setBackground(moreBgDrawable);
                    }
                } else {
                    Drawable whiteMoreBgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_chat_titlebar_more_bg_white, null);
                    if (whiteMoreBgDrawable != null) {
                        imageView.setBackground(whiteMoreBgDrawable);
                    }
                }
            } else {
                imageView.setBackground(null);
            }
        }
    }


    protected void setUpToolBar() {
        View toolBar = getToolBar();
        if (toolBar == null) {
            return;
        }
        updateToolBarBg();
    }

    protected View getToolBar() {
        return findViewById(R.id.tl_titlebar);
    }

    protected ImageView getLeftMenu() {
        return findViewById(R.id.sobot_iv_left);
    }

    protected View getTitleLine() {
        return findViewById(R.id.title_line);
    }

    protected ImageView getRightImageMenu() {
        return findViewById(R.id.iv_right);
    }

    protected TextView getRightMenu() {
        return findViewById(R.id.sobot_tv_right);
    }

    protected TextView getTitleView() {
        return findViewById(R.id.sobot_text_title);
    }


    /**
     * @param resourceId
     * @param textId
     * @param isShow
     */
    protected void showRightMenu(int resourceId, String textId, boolean isShow) {
        View tmpMenu = getRightMenu();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView rightMenu = (TextView) tmpMenu;
        if (!TextUtils.isEmpty(textId)) {
            rightMenu.setText(textId);
        } else {
            rightMenu.setText("");
        }

        if (resourceId != 0) {
            Drawable img = getResources().getDrawable(resourceId);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            rightMenu.setCompoundDrawables(null, null, img, null);
        } else {
            rightMenu.setCompoundDrawables(null, null, null, null);
        }

        if (isShow) {
            rightMenu.setVisibility(View.VISIBLE);
        } else {
            rightMenu.setVisibility(View.GONE);
        }
    }

    /**
     * @param isShow
     */
    protected void showLeftMenu(boolean isShow) {
        ImageView tmpMenu = getLeftMenu();
        if (tmpMenu == null) {
            return;
        }
        if (ChatUtils.isRtl(getSobotBaseActivity())) {
            tmpMenu.setImageResource(R.drawable.sobot_icon_titlebar_back_rtl);
        } else {
            tmpMenu.setImageResource(R.drawable.sobot_icon_titlebar_back);
        }
        if (isShow) {
            tmpMenu.setVisibility(View.VISIBLE);
            // 返回按钮是固定 36dp 宽 ImageView，用 setPadding 视觉上不生效（图标 scaleType 仍居中）。
            // 改用 marginStart/marginEnd 推开整个 view，才能真正避开刘海。
            applyNotchMarginToFixedSizeView(tmpMenu);
        } else {
            tmpMenu.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(REQUEST_TAG);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }

    /**
     * 导航栏左边点击事件
     *
     * @param view
     */
    protected void onLeftMenuClick(View view) {
        onBackPressed();
    }

    /**
     * 导航栏右边点击事件
     *
     * @param view
     */
    protected void onRightMenuClick(View view) {

    }

    public void setTitle(CharSequence title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setText(title);
    }

    public void setTitle(int title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    //返回布局id
    protected abstract int getContentViewResId();

    protected void initBundleData(Bundle savedInstanceState) {
    }

    protected abstract void initView();

    protected abstract void setRequestTag();

    protected abstract void initData();

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE:
                try {
                    //android 14 api 34 以上 部分权限直接打开“选择图片和视频界面”
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && grantResults.length > 1) {
                        boolean isAllGranted = true;
                        for (int i = 0; i < grantResults.length; i++) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                isAllGranted = false;
                            }
                        }
                        if (!isAllGranted) {
                            //如果android 14 有部分权限，直接启动“选择图片和视频界面”
                            for (int i = 0; i < grantResults.length; i++) {
                                if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    int selectType;
                                    if (Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_IMAGES) && Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                                        selectType = 3;//部分视频和图片
                                    } else if (Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                                        selectType = 2;//部分视频
                                    } else {
                                        selectType = 1;//部分图片
                                    }
                                    openSelectPic(selectType);
                                    return;
                                }
                            }
                        }
                    }

                    for (int i = 0; i < grantResults.length; i++) {
                        //判断权限的结果，如果有被拒绝，就return
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_VIDEO)) {
                                showPerssionSettingUi();
                                return;
                            }
                            if (permissions[i] != null && !permissions[i].equals(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                                //不处理 READ_MEDIA_VISUAL_USER_SELECTED权限，如果是Android13 全部允许权限时，这个权限返回的还是-1
                                return;
                            }
                        }
                    }
                    if (permissionListener != null) {
                        permissionListener.onPermissionSuccessListener();
                    }
                    removePerssionUi();
                } catch (Exception e) {
//                    LogUtils.e("uncaught", e);
                }
                break;
        }
    }

    /**
     * 检测是否没有对应的权限，没有权限显示提示蒙层
     *
     * @param type                1：文件 2：麦克风 3：相机
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true :有权限 false:没有权限
     */
    public boolean isHasPermission(int type, int checkPermissionType) {
        boolean isHasPermission = false;
        if (type == 1) {
            int result = checkStoragePermission(checkPermissionType);
            if (result == 0) {
                isHasPermission = true;
            } else if (result == 2) {
                //部分权限
                isHasPermission = false;
                if (checkPermissionType == 3) {
                    //1:部分图片 2:部分视频 3:部分视频和图片
                    openSelectPic(3);
                } else if (checkPermissionType == 1) {
                    openSelectPic(2);
                } else {
                    openSelectPic(1);
                }
            } else {
                isHasPermission = false;
                if (checkPermissionType == 3) {
                    showPerssionUi(1);
                } else {
                    showPerssionUi(0);
                }
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请权限
                            requestStoragePermission(checkPermissionType);
                        }
                    }, 2000);
                } else {
                    //申请权限
                    requestStoragePermission(checkPermissionType);
                }
            }
        } else if (type == 2) {
            isHasPermission = checkAudioPermission();
            if (!isHasPermission) {
                showPerssionUi(2);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请麦克风权限
                            requestAudioPermission();
                        }
                    }, 2000);
                } else {
                    //申请麦克风权限
                    requestAudioPermission();
                }
            }
        } else if (type == 3) {
            isHasPermission = checkCameraPermission();
            if (!isHasPermission) {
                showPerssionUi(3);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请相机权限
                            requestCameraPermission();
                        }
                    }, 2000);
                } else {
                    //申请相机权限
                    requestCameraPermission();
                }
            }
        } else if (type == 4) {
            isHasPermission = checkAudioPermission();
            if (!isHasPermission) {
                showPerssionUi(4);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请麦克风权限
                            requestAudioPermission();
                        }
                    }, 2000);
                } else {
                    //申请麦克风权限
                    requestAudioPermission();
                }
            }
        }
        return isHasPermission;
    }

    /**
     * 显示权限蒙层
     *
     * @param type 0：照片和视频 1：文件 2：麦克风 3：相机
     */
    public void showPerssionUi(int type) {
        isContinueShooting = false;
        overlay = LayoutInflater.from(getSobotBaseActivity()).inflate(R.layout.sobot_layout_overlay, null);
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
            LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            Button btn_left = overlay.findViewById(R.id.btn_left);
            Button btn_right = overlay.findViewById(R.id.btn_right);
            TextView tv_setting_title = overlay.findViewById(R.id.tv_setting_title);
            TextView tv_setting_content = overlay.findViewById(R.id.tv_setting_content);
            if (type == 0) {
                tv_content.setText("\"" + getAppName() + "\" " + getResources().getString(R.string.sobot_album_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_album));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_album));
            } else if (type == 1) {
                tv_content.setText("\"" + getAppName() + "\" " + getResources().getString(R.string.sobot_storage_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_storage));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_storage));
            } else if (type == 2) {
                tv_content.setText("\"" + getAppName() + "\" " + getResources().getString(R.string.sobot_microphone_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_microphone));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_microphone));
            } else if (type == 3) {
                tv_content.setText("\"" + getAppName() + "\" " + getResources().getString(R.string.sobot_camera_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_camera));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_camera));
            } else if (type == 4) {
                tv_content.setText("\"" + getAppName() + "\" " + getResources().getString(R.string.sobot_microphone_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_no_microphone));
                String tempStr = getResources().getString(R.string.sobot_no_microphone_des);
                try {
                    if (tempStr.contains("%s")) {
                        tv_setting_content.setText(String.format(tempStr, CommonUtils.getAppName(getSobotBaseActivity())));
                    } else {
                        tv_setting_content.setText(tempStr);
                    }
                } catch (Exception e) {
                    // 记录错误并使用默认文本
                    LogUtils.e("格式化字符串出错: " + e.getMessage());
                    tv_setting_content.setText(tempStr);
                }
                btn_left.setText(getResources().getString(R.string.sobot_continue_shooting));
            }
            viewGroup = findViewById(android.R.id.content);
            if (viewGroup != null) {
                viewGroup.addView(overlay);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ll_setting.getVisibility() == View.GONE) {
                        ll_info.setVisibility(View.VISIBLE);
                    }
                }
            }, 200);//延迟0.3s 是避免多次拒绝后ll_info 隐藏会出现闪一下的问题
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                }
            });
            btn_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                    if (getResources().getString(R.string.sobot_continue_shooting).equals(btn_left.getText().toString())) {
                        isContinueShooting = true;
                    }
                }
            });
            btn_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                    Uri packageURI = Uri.parse("package:" + getSobotBaseActivity().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 获取app 名字
     *
     * @return
     */
    private String getAppName() {
        return CommonUtils.getAppName(getSobotBaseActivity());
    }

    //拒绝权限后显示 去设置UI
    public void showPerssionSettingUi() {
        String permissionTitle = "";
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
            LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            ll_info.setVisibility(View.GONE);
            ll_setting.setVisibility(View.VISIBLE);
            if (tv_content != null && StringUtils.isNoEmpty(tv_content.getText().toString())) {
                permissionTitle = tv_content.getText().toString();
            }
        }
        if (permissionListener != null) {
            permissionListener.onPermissionErrorListener(getSobotBaseActivity(), permissionTitle);
        }
    }

    //移除权限提示蒙层
    public void removePerssionUi() {
        if (overlay != null) {
            if (viewGroup == null) {
                viewGroup = findViewById(android.R.id.content);
            }
            viewGroup.removeView(overlay);
        }
    }

    //隐藏权限提示蒙层
    public void hidePerssionUi() {
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
        }
    }


    /**
     * 申请存储权限
     *
     * @param checkType 0：图片权限 1：视频权限，3，所有细分的权限， android 13 使用
     */
    public void requestStoragePermission(int checkType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (checkType == 0) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                } else if (checkType == 1) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                }
            } else {
                if (checkType == 0) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                } else if (checkType == 1) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                }
            }
        } else {
            //申请READ_EXTERNAL_STORAGE权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
            }
        }
    }

    /**
     * 检查存储权限
     *
     * @param checkType 0：图片权限 1：视频权限，3，所有细分的权限， android 13 使用
     * @return int  0：有权限，1：没有权限，2:有部分权限
     */
    public int checkStoragePermission(int checkType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //android 13 api 33 以上
            if (checkType == 0 || checkType == 1) {
                //检测是否有图片权限
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        == PackageManager.PERMISSION_GRANTED) {
                    //有权限
                    return 0;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED) {
                    //android 14 有部分权限
                    return 2;
                } else {
                    //没有权限
                    return 1;
                }
            } else {
                //检测是否有图片权限
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        == PackageManager.PERMISSION_GRANTED) {
                    //有权限
                    return 0;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED) {
                    //android 14 有部分权限
                    return 2;
                } else {
                    //没有权限
                    return 1;
                }
            }
        } else {
            // android 13 api33 以前
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return 0; //有权限
            } else {
                return 1; //没有权限
            }
        }
    }


    /**
     * android 14 部分权限情况下，回显照片或者视频
     *
     * @param selectType 1:部分图片 2:部分视频 3:部分视频和图片
     */
    private void openSelectPic(int selectType) {
        //隐藏权限提示蒙层
        removePerssionUi();
        Intent intent = new Intent(getSobotBaseActivity(), SobotSelectPicAndVideoActivity.class);
        intent.putExtra("selectType", selectType);
        startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
    }


    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请录音权限
     */
    protected void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
            }
        }
    }


    /**
     * 检查相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请相机权限
     */
    protected boolean requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}
                        , ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                return false;
            }
        }
        return true;
    }


    /**
     * 通过照相上传图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            ToastUtil.showCustomToast(getSobotBaseActivity().getApplicationContext(), getString(R.string.sobot_sdcard_does_not_exist),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                //如果有拍照所需的权限，跳转到拍照界面
                startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), ChatUtils.REQUEST_CODE_CAMERA);
            }
        };

        if (!isHasPermission(3, 3)) {
            return;
        }

        // 打开拍摄页面
        startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), ChatUtils.REQUEST_CODE_CAMERA);
    }

    //判断相机是否可用
    public boolean isCameraCanUse() {
        if (getSobotBaseActivity() == null) return false;

        CameraManager cameraManager = (CameraManager) getSobotBaseActivity().getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) return false;

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                // 只检测后置或前置摄像头是否存在
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true; // 存在后置摄像头
                }
            }
        } catch (CameraAccessException e) {
        }

        return false;
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectPic(getSobotBaseActivity());
            }
        };
        if (!isHasPermission(1, 0)) {
            return;
        }
        ChatUtils.openSelectPic(getSobotBaseActivity());
    }


    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectVedio(getSobotBaseActivity());
            }
        };
        if (!isHasPermission(1, 1)) {
            return;
        }
        ChatUtils.openSelectVedio(getSobotBaseActivity());
    }

    /**
     * 打开文件系统选取文件上传
     */
    public void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE);
    }

    public SobotChatBaseActivity getSobotBaseActivity() {
        return this;
    }

    public Context getSobotBaseContext() {
        return this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (initMode != currentNightMode) {
            initMode = currentNightMode;
            recreate();
            return;
        }
        //折叠屏折叠/展开切换：screenWidthDp 变化时 recreate，让 w 资源限定符生效
        //横竖屏不会触发此分支（方向已通过 setRequestedOrientation 在 onCreate 中锁定）
        if (mLastScreenWidthDp != newConfig.screenWidthDp) {
            mLastScreenWidthDp = newConfig.screenWidthDp;
            recreate();
        }
    }

    // 在 attachBaseContext 方法中处理布局方向
    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = newBase;
        try {
            Locale language = (Locale) SharedPreferencesUtil.getObject(newBase, ZhiChiConstant.SOBOT_LANGUAGE);
            // 兜底：SOBOT_LANGUAGE 未生效（接入方未调 setInternationalLanguage / config 接口失败 / 反序列化失败）时，
            // 退而从 Information.locale 读取接入方期望的语言，避免回落系统语言导致 UI 文案与会话语言不一致。
            if (language == null) {
                language = resolveFallbackLocale(newBase);
            }
            if (language != null) {
                Configuration config = new Configuration(newBase.getResources().getConfiguration());
                config.setLocale(language);
                // Android 13+ 部分 OEM ROM（华为 / 小米 / 三星 / vivo）优先读 LocaleList[0]，
                // 仅 setLocale 不 setLocales 会导致资源仍回落系统语言。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocales(new LocaleList(language));
                }
                if (ChatUtils.isRtl(newBase)) {
                    config.setLayoutDirection(language);
                } else {
                    config.setLayoutDirection(Locale.ENGLISH);
                }
                context = newBase.createConfigurationContext(config);
            }
        } catch (Exception e) {
            LogUtils.e("attachBaseContext apply locale failed", e);
        }
        super.attachBaseContext(context);
    }

    // 从 sobot_last_current_info.locale 兜底解析 SDK 用 Locale，映射规则与 ZCSobotApi.setInternationalLanguage 保持一致。
    private static Locale resolveFallbackLocale(Context ctx) {
        try {
            Object obj = SharedPreferencesUtil.getObject(ctx, ZhiChiConstant.sobot_last_current_info);
            if (!(obj instanceof Information)) {
                return null;
            }
            String code = ((Information) obj).getLocale();
            if (TextUtils.isEmpty(code)) {
                return null;
            }
            if ("he".equals(code)) {
                return new Locale("iw");
            } else if ("zh-Hans".equals(code)) {
                return new Locale("zh");
            } else if ("zh-Hant".equals(code)) {
                return new Locale("zh", "TW");
            }
            return new Locale(code);
        } catch (Exception e) {
            LogUtils.e("resolveFallbackLocale failed", e);
            return null;
        }
    }

    /**
     * 导航栏渐变逻辑
     * 先判断客户开发是否设置，如果设置了 直接使用；如果没有修改（和系统默认一样），就就绪判断后端接口返回的颜色；
     * 如果接口返回的也和系统一样，就不处理（默认渐变色）；如果不一样，直接按照接口的设置渐变色
     */
    private void updateToolBarBg() {
        try {
            ZhiChiInitModeBase initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseActivity(),
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initModel == null) {
                setToolBarDefBg();
                return;
            }
            if (getToolBar() == null) {
                return;
            }
            if (initModel.getVisitorScheme() != null) {
                //服务端返回的导航条背景颜色
                if (initModel.getVisitorScheme().getTopBarBackStyle() == 0) {
                    //导航条无颜色,显示线
                    if (getTitleLine() != null) {
                        getTitleLine().setVisibility(View.VISIBLE);
                    }
                    //导航条无颜色,修改导航栏昵称和描述的颜色
                    if (getTitleView() != null) {
                        updateTitleColor(R.color.sobot_color_text_first);
                    }
                    updateViewColor(getLeftMenu(), false, ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                    SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(!isSystemNightMode()).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
                } else {
                    if (!TextUtils.isEmpty(initModel.getVisitorScheme().getTopBarColor())) {
                        String topBarColorStr = initModel.getVisitorScheme().getTopBarColor();
                        if (!topBarColorStr.contains(",")) {
                            //单色 需要变成两个一样
                            topBarColorStr = topBarColorStr + "," + topBarColorStr;
                        }
                        String[] topBarColor = topBarColorStr.split(",");
                        if (topBarColor.length > 1) {
                            int[] colors = new int[topBarColor.length];
                            for (int i = 0; i < topBarColor.length; i++) {
                                colors[i] = Color.parseColor(topBarColor[i]);
                            }
                            GradientDrawable gradientDrawable = new GradientDrawable();
                            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                            gradientDrawable.setColors(colors); //添加颜色组
                            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                            getToolBar().setBackground(gradientDrawable);
                        }
                        //导航条有颜色,取返回的文字颜色修改导航栏昵称和描述的颜色
                        boolean isBlack = (ThemeUtils.getToolBarTextAndIconColorType(getSobotBaseActivity()) == 1);
                        if (isBlack) {
                            //黑色
                            updateTitleColor(R.color.sobot_color_black);
                            SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(true).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
                        } else {
                            updateTitleColor(R.color.sobot_color_white);
                            SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(false).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
                        }
                        updateViewColor(getLeftMenu(), isBlack, -1);
                    }
                }
            } else {
                setToolBarDefBg();
            }
        } catch (Exception e) {
        }
    }

    //修改导航条标题颜色
    private void updateTitleColor(int sobot_color_white) {
        if (getTitleView() != null) {
            getTitleView().setTextColor(ContextCompat.getColor(getSobotBaseActivity(), sobot_color_white));
        }
    }


    /**
     * 修改图标颜色为黑色
     *
     * @param iv
     * @param isBlack true 黑色; false 白色
     * @param selCol  -1 = 不指定颜色  ; 其它=指定了，有指定优先用指定
     */
    private void updateViewColor(ImageView iv, boolean isBlack, int selCol) {
        // 增加空值检查和延迟执行机制
        if (iv == null) {
            return;
        }

        // 使用 post 确保在 UI 线程执行且视图已准备就绪
        iv.post(() -> {
            try {
                Drawable drawable = iv.getDrawable();
                if (drawable == null) {
                    // 如果 drawable 为空，尝试重新获取
                    drawable = iv.getDrawable();
                    if (drawable == null) {
                        // 尝试从资源中获取默认 drawable
                        int resId = iv.getId(); // 这里可能需要根据实际需求调整
                        if (resId != View.NO_ID) {
                            drawable = ContextCompat.getDrawable(getSobotBaseActivity(), resId);
                        }
                    }
                }

                if (drawable != null) {
                    int color;
                    if (selCol == -1) {
                        color = isBlack ?
                                ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_black) :
                                ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_white);
                    } else {
                        color = selCol;
                    }

                    // 添加额外的安全检查
                    if (color != 0) {
                        Drawable coloredDrawable = ThemeUtils.applyColorToDrawable(drawable, color);
                        if (coloredDrawable != null) {
                            iv.setImageDrawable(coloredDrawable);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * 设置默认导航栏渐变色
     */
    public void setToolBarDefBg() {
        try {
            HelpConfigModel configModel = (HelpConfigModel) SharedPreferencesUtil.getObject(getSobotBaseActivity(), ZhiChiConstant.SOBOT_HELP_CONFIG_MODEL);
            if (getToolBar() == null) {
                return;
            }
            if (configModel == null) {
                return;
            }

            //服务端返回的导航条背景颜色
            if (configModel.getTopBarBackStyle() == 0) {
                //导航条无颜色,修改导航栏昵称和描述的颜色
                if (getTitleView() != null) {
                    updateTitleColor(R.color.sobot_color_text_first);
                }
                updateViewColor(getLeftMenu(), false, ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(!isSystemNightMode()).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
            } else {
                if (!TextUtils.isEmpty(configModel.getTopBarColor())) {
                    String topBarColorStr = configModel.getTopBarColor();
                    if (!topBarColorStr.contains(",")) {
                        //单色 需要变成两个一样
                        topBarColorStr = topBarColorStr + "," + topBarColorStr;
                    }
                    String[] topBarColor = topBarColorStr.split(",");
                    if (topBarColor.length > 1) {
                        int[] colors = new int[topBarColor.length];
                        for (int i = 0; i < topBarColor.length; i++) {
                            colors[i] = Color.parseColor(topBarColor[i]);
                        }
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColors(colors); //添加颜色组
                        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                        getToolBar().setBackground(gradientDrawable);
                    }
                    //导航条有颜色,取返回的文字颜色修改导航栏昵称和描述的颜色
                    boolean isBlack = (ThemeUtils.getToolBarTextAndIconColorType(getSobotBaseActivity()) == 1);
                    if (isBlack) {
                        //黑色
                        updateTitleColor(R.color.sobot_color_black);
                        SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(true).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
                    } else {
                        updateTitleColor(R.color.sobot_color_white);
                        SobotImmersionBar.with(this).titleBar(getToolBar()).statusBarDarkFont(false).navigationBarDarkIcon(!isSystemNightMode()).fitsLayoutOverlapEnable(isLandscapeScreen ? false : true).init();
                    }
                    updateViewColor(getLeftMenu(), isBlack, -1);
                }
            }
        } catch (Exception e) {
        }
    }

    //显示键盘
    public void showSoftKeyboard() {
        if (getSobotBaseActivity() != null)
            SobotSoftKeyboardUtils.showSoftKeyboard(getSobotBaseActivity());
    }

    //隐藏键盘
    public void hideKeyboard() {
        if (getSobotBaseActivity() != null)
            SobotSoftKeyboardUtils.hideKeyboard(getSobotBaseActivity());
    }

    /**
     * 判断当前是否为夜间模式
     *
     * @return true: 夜间模式, false: 日间模式
     */
    public boolean isSystemNightMode() {
        if (getSobotBaseActivity() != null) {
            int nightModeFlags = getSobotBaseActivity().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            boolean isNightMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
//            LogUtils.d(isNightMode ? "夜间模式" : "日间模式");
            return isNightMode;
        } else {
            return false;
        }
    }

    /**
     * activity打开相机
     *
     * @param act
     * @return
     */
    public static File openCamera(Activity act) {
        return openCamera(act, null);
    }

    /**
     * Fragment打开相机
     *
     * @param act
     * @param childFragment 打开相机的fragment
     * @return
     */
    public static File openCamera(Activity act, Fragment childFragment) {
        String path = SobotPathManager.getInstance().getPicDir() + System.currentTimeMillis() + ".jpg";
        // 创建图片文件存放的位置
        File cameraFile = new File(path);
        try {
            IOUtils.createFolder(cameraFile.getParentFile());
            // 相机临时图统一走 FileProvider，minSdk=21 全版本可用，
            // 不再回退 Uri.fromFile（Android 7+ 跨进程 file:// URI 会抛 FileUriExposedException）。
            Uri uri = ChatUtils.getUri(act, cameraFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore
                    .EXTRA_OUTPUT, uri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // 防御：无相机 App / FileProvider 配置错误 / SobotPathManager 返回 null 等场景，
            // 启动失败时记日志但不杀进程；调用方 onActivityResult 不触发，cameraFile 自然不会被读。
            if (childFragment != null) {
                childFragment.startActivityForResult(intent, ZCSobotConstant.REQUEST_CODE_OPENCAMERA);
            } else {
                act.startActivityForResult(intent, ZCSobotConstant.REQUEST_CODE_OPENCAMERA);
            }
        } catch (Exception e) {
            LogUtils.e("openCamera failed", e);
        }

        return cameraFile;
    }

    /**
     * 安全获取字符串资源，支持多语言，避免资源不存在时抛出异常
     *
     * @param resId 字符串资源ID
     * @return 字符串值或空字符串（当资源不存在时）
     */
    public String getSafeStringResource(int resId) {
        try {
            return getString(resId);
        } catch (Exception e) {
            LogUtils.e("获取字符串资源失败: " + e.getMessage());
            return "";
        }
    }

    /**
     * 安全获取字符串资源，支持多语言，避免资源不存在时抛出异常
     *
     * @param resId        字符串资源ID
     * @param defaultValue 默认值
     * @return 字符串值或默认值（当资源不存在时）
     */
    public String getSafeStringResource(int resId, String defaultValue) {
        try {
            return getString(resId);
        } catch (Exception e) {
            LogUtils.e("获取字符串资源失败: " + e.getMessage());
            return defaultValue;
        }
    }

}