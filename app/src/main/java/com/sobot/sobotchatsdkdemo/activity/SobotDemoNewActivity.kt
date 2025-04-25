package com.sobot.sobotchatsdkdemo.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sobot.chat.utils.LogUtils
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.sobotchatsdkdemo.R


class SobotDemoNewActivity : AppCompatActivity() {
    private var rl_root: RelativeLayout? = null
    private var unReadMsgReceiver //获取未读消息数的广播接收者
            : SobotUnReadMsgReceiver? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sobot_demo_new_activity)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        LogUtils.isDebug = true
        rl_root = findViewById<View>(R.id.rl_root) as RelativeLayout?
        rl_root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updatePadding(
                    left = systemBars.left,
                    right = systemBars.right,
                    top = systemBars.top,
                    bottom = systemBars.bottom
                )
                WindowInsetsCompat.CONSUMED
            }
        }
        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_jieshao -> selectedFragment = SobotDemoWelcomeFragment()
                R.id.navigation_more -> selectedFragment = SobotDemoNewSettingFragment()
            }

            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                selectedFragment!!
            ).commit()
            true
        }


        // Load the default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SobotDemoWelcomeFragment()).commit()

        regReceiver()
    }

    private fun regReceiver() {
        val filter = IntentFilter()
        if (unReadMsgReceiver == null) {
            unReadMsgReceiver = SobotUnReadMsgReceiver()
        }
        filter.addAction(ZhiChiConstant.sobot_unreadCountBrocast)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(unReadMsgReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(unReadMsgReceiver, filter);
        }
    }

    override fun onDestroy() {
        try {
            if (unReadMsgReceiver != null) {
                unregisterReceiver(unReadMsgReceiver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}