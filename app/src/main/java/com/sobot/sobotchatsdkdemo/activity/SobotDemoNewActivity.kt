package com.sobot.sobotchatsdkdemo.activity

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.lzy.widget.AlphaIndicator
import com.sobot.chat.utils.LogUtils
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.sobotchatsdkdemo.R

class SobotDemoNewActivity : AppCompatActivity() {
    private var unReadMsgReceiver //获取未读消息数的广播接收者
            : SobotUnReadMsgReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sobot_demo_new_activity)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        LogUtils.isDebug = true
        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        viewPager.adapter = MainAdapter(supportFragmentManager)
        val alphaIndicator = findViewById<View>(R.id.alphaIndicator) as AlphaIndicator
        alphaIndicator.setViewPager(viewPager)
        regReceiver()
    }

    private inner class MainAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!
    ) {
        private val fragments: MutableList<Fragment> = ArrayList()

        init {
            fragments.add(SobotDemoWelcomeFragment())
            fragments.add(SobotDemoNewSettingFragment())
            fragments.add(SobotAutomationFragment())
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    private fun regReceiver() {
        val filter = IntentFilter()
        if (unReadMsgReceiver == null) {
            unReadMsgReceiver = SobotUnReadMsgReceiver()
        }
        filter.addAction(ZhiChiConstant.sobot_unreadCountBrocast)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(unReadMsgReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(unReadMsgReceiver, filter)
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