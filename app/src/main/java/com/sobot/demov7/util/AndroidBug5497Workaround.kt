package com.sobot.demov7.util

import android.R
import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout

class AndroidBug5497Workaround private constructor(activity: Activity) {
    private val mChildOfContent: View
    private var usableHeightPrevious = 0
    private val frameLayoutParams: FrameLayout.LayoutParams

    init {
        //Decorview里分为title和content，content即是承载我们setContentView方法的布局的根布局
        val content = activity.findViewById<View>(R.id.content) as FrameLayout
        //mChildOfContent我们setContentView方法的布局
        mChildOfContent = content.getChildAt(0)
        //监听布局变化，任何界面变化都会触发该监听
        //软键盘弹起同样也会触发该监听
        mChildOfContent.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
        frameLayoutParams = mChildOfContent.layoutParams as FrameLayout.LayoutParams
    }

    private fun possiblyResizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = mChildOfContent.rootView.height
            //计算布局变化的高度
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                //如果布局变化的高度大于全屏高度的4分之一，则认为可能是键盘弹出，需要改变我们setContentView的布局高度
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard
            }
            //布局改变后重绘
            mChildOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    //计算去掉键盘高度后的可用高度
    private fun computeUsableHeight(): Int {
        val r = Rect()
        mChildOfContent.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top // 全屏模式下： return r.bottom
        //        return r.bottom;
    }

    companion object {
        // For more information, see https://code.google.com/p/android/issues/detail?id=5497
        // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
        fun assistActivity(activity: Activity) {
            AndroidBug5497Workaround(activity)
        }
    }
}