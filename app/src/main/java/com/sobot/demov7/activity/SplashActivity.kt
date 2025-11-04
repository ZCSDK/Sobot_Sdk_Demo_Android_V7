package com.sobot.demov7.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.sobot.demov7.R

/**
 * 闪屏界面
 *
 * @author Eric
 */
class SplashActivity : AppCompatActivity() {
    @SuppressLint("HandlerLeak")
    private val handler = Handler()
    private val splashMin = 2000 //在闪屏界面等待的时间
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_splash_activity)
        goActivity(SobotDemoNewActivity::class.java, splashMin.toLong(), true)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            com.sobot.chat.R.anim.sobot_push_left_in,
            com.sobot.chat.R.anim.sobot_push_left_out
        )
    }

    private fun goActivity(clz: Class<*>, delMin: Long, isSuccess: Boolean) {
        handler.postDelayed({
            val intent = Intent()
            intent.setClass(this@SplashActivity, clz)
            intent.putExtra("isSuccess", isSuccess)
            startActivity(intent)
            finish()
        }, delMin)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}