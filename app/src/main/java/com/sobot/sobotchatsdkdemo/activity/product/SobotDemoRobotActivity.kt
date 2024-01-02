package com.sobot.sobotchatsdkdemo.activity.product

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.api.model.Information
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getStringData

class SobotDemoRobotActivity : AppCompatActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var sobot_demo_bottom_layout: RelativeLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_demo_robot_activity)
        findvViews()
    }

    private fun findvViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "智能机器人"
        sobot_tv_left!!.setOnClickListener(this)
        sobot_demo_bottom_layout =
            findViewById<View>(R.id.sobot_demo_bottom_layout) as RelativeLayout
        sobot_demo_bottom_layout!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_demo_bottom_layout) {
            val information = getObject(this, "sobot_demo_infomation") as Information?
            if (information != null) {
                val sobot_custom_language_value = getStringData(this, "custom_language_value", "")
                if (!TextUtils.isEmpty(sobot_custom_language_value)) {
                    ZCSobotApi.setInternationalLanguage(
                        this,
                        sobot_custom_language_value,
                        true
                    )
                }
                ZCSobotApi.openZCChat(this, information)
            }
        }
    }
}