package com.sobot.chat.activity.base;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;

/**
 * 从界面下方弹出的activity 内容高度自适应
 */
public abstract class SobotDialogBaseActivity extends SobotChatBaseActivity {
    private ImageView iv_closes;

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
            //去掉dialog 的标题栏（不然弹窗会显示app名字）
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);

            //窗口对齐屏幕宽度
            Window win = this.getWindow();
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            win.setAttributes(lp);
        } catch (Exception e) {
        }
    }

    @Override
    protected void initView() {
        iv_closes = findViewById(R.id.iv_closes);
        if (iv_closes != null) {
            //关闭
            iv_closes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        // 刘海屏/挖孔避让（横屏生效）——避让"白色背景里面的内容"，而不是白色背景层：
        // 底部弹窗通用结构 = sobot_container（透明根）> 白色背景层 > 内容行。
        // 走查修正：之前对 childAt(0) 直接加 padding —— 时区弹窗等结构的 childAt(0)
        // 是无背景的透明包装层（白底在孙节点），padding 会把白色背景整体内缩（"白色背景避让"）。
        // 改为对 childAt(0) 的每个子 view 加 padding：白底层要么是 childAt(0) 本身（不被 pad，
        // 背景保持全宽，内容行内缩），要么是其子 view（自带背景，padding 不缩背景）——
        // 两种结构下白色背景都不再被内缩，仅内容避让。
        View container = findViewById(R.id.sobot_container);
        if (container instanceof ViewGroup && ((ViewGroup) container).getChildCount() > 0) {
            View whiteLayer = ((ViewGroup) container).getChildAt(0);
            if (whiteLayer instanceof ViewGroup && ((ViewGroup) whiteLayer).getChildCount() > 0) {
                // 常规结构：对白底层内的每个内容行（标题/搜索框/列表/提交按钮）避让
                ViewGroup whiteGroup = (ViewGroup) whiteLayer;
                for (int i = 0; i < whiteGroup.getChildCount(); i++) {
                    displayInNotchSingleSide(whiteGroup.getChildAt(i));
                }
            } else {
                // 白底层无子 view 的退化结构：直接对白底层避让（自带背景，背景不缩）
                displayInNotchSingleSide(whiteLayer);
            }
        } else {
            // 无 sobot_container 的弹窗（如评价弹窗）→ 沿用标题栏 ll_title_bar 避让兜底
            View titleBar = findViewById(R.id.ll_title_bar);
            if (titleBar != null) {
                displayInNotchSingleSide(titleBar);
            }
        }
    }

    /**
     * 单侧挖孔/刘海避让（横屏半弹窗专用）：只在挖孔所在的一侧加 padding。
     * 替换旧版 displayInNotch —— 旧版只 pad 左侧且用 rect.right 绝对坐标钳 110px，
     * 反向横屏（SENSOR_LANDSCAPE，挖孔转到右侧）时 padding 加错边，内容顶进挖孔。
     * API 28+ 走系统 DisplayCutout safeInset，旋转后系统自动重派发、左右自动切换；
     * API < 28 走 NotchScreenManager + rotation 判定（与 SobotChatBaseFragment#displayInNotchSingleSide 同款实现）。
     * 注意：不做 isForceEdgeToEdge 短路 —— 半弹窗布局没有 view_root，Android 15 强制 e2e 下
     * root 统一避让覆盖不到弹窗，弹窗必须靠 view 层自避让。
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
                    // 用 rect.width() 表示刘海宽度；rect.right 是绝对 x 坐标，
                    // 刘海在右侧时 ≈ 屏幕宽度，会把 padding 撑爆导致控件坍缩
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
        });
    }

    /**
     * API 28+ 的挖孔避让：通过 OnApplyWindowInsetsListener 让系统自动派发 DisplayCutout，
     * 旋转后由系统重新触发 insets 派发，自动切换左右侧避让方向。
     * 与 SobotChatBaseFragment#applyDisplayCutoutPaddingListener 同款实现。
     */
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

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getY() <= 0) {
                finish();
            }
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePending();
    }

    private void overridePending() {
        overridePendingTransition(R.anim.sobot_popupwindow_in,
                R.anim.sobot_popupwindow_out);
    }

    public Activity getContext() {
        return SobotDialogBaseActivity.this;
    }
}
