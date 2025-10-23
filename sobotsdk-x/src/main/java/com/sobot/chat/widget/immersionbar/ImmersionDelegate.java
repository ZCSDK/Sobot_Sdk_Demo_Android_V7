package com.sobot.chat.widget.immersionbar;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.view.Surface;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * @author geyifeng
 * @date 2019/4/12 4:01 PM
 */
class ImmersionDelegate implements Runnable {

    private SobotImmersionBar mImmersionBar;
    private BarProperties mBarProperties;
    private OnBarListener mOnBarListener;
    private int mNotchHeight;

    ImmersionDelegate(Object o) {
        if (o instanceof Activity) {
            if (mImmersionBar == null) {
                mImmersionBar = new SobotImmersionBar((Activity) o);
            }
        } else if (o instanceof Fragment) {
            if (mImmersionBar == null) {
                if (o instanceof DialogFragment) {
                    mImmersionBar = new SobotImmersionBar((DialogFragment) o);
                } else {
                    mImmersionBar = new SobotImmersionBar((Fragment) o);
                }
            }
        } else if (o instanceof android.app.Fragment) {
            if (mImmersionBar == null) {
                if (o instanceof android.app.DialogFragment) {
                    mImmersionBar = new SobotImmersionBar((android.app.DialogFragment) o);
                } else {
                    mImmersionBar = new SobotImmersionBar((android.app.Fragment) o);
                }
            }
        }
    }

    ImmersionDelegate(Activity activity, Dialog dialog) {
        if (mImmersionBar == null) {
            mImmersionBar = new SobotImmersionBar(activity, dialog);
        }
    }

    public SobotImmersionBar get() {
        return mImmersionBar;
    }

    void onActivityCreated(Configuration configuration) {
        barChanged(configuration);
    }

    void onResume() {
        if (mImmersionBar != null) {
            mImmersionBar.onResume();
        }
    }

    void onDestroy() {
        mBarProperties = null;
        mOnBarListener = null;
        if (mImmersionBar != null) {
            mImmersionBar.onDestroy();
            mImmersionBar = null;
        }
    }

    void onConfigurationChanged(Configuration newConfig) {
        if (mImmersionBar != null) {
            mImmersionBar.onConfigurationChanged(newConfig);
            barChanged(newConfig);
        }
    }

    /**
     * 横竖屏切换监听
     * Orientation change.
     *
     * @param configuration the configuration
     */
    private void barChanged(Configuration configuration) {
        if (mImmersionBar != null && mImmersionBar.initialized()) {
            mOnBarListener = mImmersionBar.getBarParams().onBarListener;
            if (mOnBarListener != null) {
                final Activity activity = mImmersionBar.getActivity();
                if (mBarProperties == null) {
                    mBarProperties = new BarProperties();
                }
                mBarProperties.setPortrait(configuration.orientation == Configuration.ORIENTATION_PORTRAIT);
                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == Surface.ROTATION_90) {
                    mBarProperties.setLandscapeLeft(true);
                    mBarProperties.setLandscapeRight(false);
                } else if (rotation == Surface.ROTATION_270) {
                    mBarProperties.setLandscapeLeft(false);
                    mBarProperties.setLandscapeRight(true);
                } else {
                    mBarProperties.setLandscapeLeft(false);
                    mBarProperties.setLandscapeRight(false);
                }
                activity.getWindow().getDecorView().post(this);
            }
        }
    }

    @Override
    public void run() {
        if (mImmersionBar != null && mImmersionBar.getActivity() != null) {
            Activity activity = mImmersionBar.getActivity();
            BarConfig barConfig = new BarConfig(activity);
            mBarProperties.setStatusBarHeight(barConfig.getStatusBarHeight());
            mBarProperties.setNavigationBar(barConfig.hasNavigationBar());
            mBarProperties.setNavigationBarHeight(barConfig.getNavigationBarHeight());
            mBarProperties.setNavigationBarWidth(barConfig.getNavigationBarWidth());
            mBarProperties.setActionBarHeight(barConfig.getActionBarHeight());
            boolean notchScreen = NotchUtils.hasNotchScreen(activity);
            mBarProperties.setNotchScreen(notchScreen);
            if (notchScreen && mNotchHeight == 0) {
                mNotchHeight = NotchUtils.getNotchHeight(activity);
                mBarProperties.setNotchHeight(mNotchHeight);
            }
            mOnBarListener.onBarChange(mBarProperties);
        }
    }
}
