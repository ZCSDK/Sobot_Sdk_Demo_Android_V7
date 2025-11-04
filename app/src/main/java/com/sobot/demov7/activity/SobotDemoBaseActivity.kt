package com.sobot.demov7.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import com.sobot.chat.utils.CommonUtils
import com.sobot.chat.utils.LogUtils
import com.sobot.chat.utils.ScreenUtils
import com.sobot.demov7.R

abstract class SobotDemoBaseActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewResId)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        initView()
        val view_root = findViewById<View>(R.id.view_root)
        if (view_root != null) {
            val targetSdkVersion = CommonUtils.getTargetSdkVersion(this)
            //            LogUtils.d(" app targetSdkVersion版本号：" + targetSdkVersion);
            if (Build.VERSION.SDK_INT >= 35 && targetSdkVersion >= 35) {
                view_root.setOnApplyWindowInsetsListener { v, insets ->
                    val topInset = insets.getInsets(WindowInsets.Type.systemBars()).top
                    val bottomInset =
                        insets.getInsets(WindowInsets.Type.systemBars()).bottom
                    LogUtils.d("状态栏高度：$topInset 底部导航：$bottomInset")
                    //每个页面底部添加高度，避让底部导航栏
                    view_root.setPadding(0, 0, 0, bottomInset)
                    //每个页面底部添加高度，避让头部状态栏
                    val sobot_demo_titlebar =
                        findViewById<View>(R.id.sobot_demo_titlebar)
                    if (sobot_demo_titlebar != null && sobot_demo_titlebar.layoutParams != null) {
                        sobot_demo_titlebar.layoutParams.height =
                            ScreenUtils.dip2px(
                                this@SobotDemoBaseActivity,
                                44f
                            ) + topInset
                        if (findViewById<View?>(R.id.view_empty) != null && findViewById<View>(
                                R.id.view_empty
                            ).layoutParams != null
                        ) {
                            findViewById<View>(R.id.view_empty).layoutParams.height =
                                topInset
                        }
                    }
                    insets
                }
            }
        }
    }

    protected abstract val contentViewResId: Int
    protected abstract fun initView()
}
