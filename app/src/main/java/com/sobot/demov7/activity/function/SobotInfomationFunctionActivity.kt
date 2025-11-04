package com.sobot.demov7.activity.function

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.demov7.R
import com.sobot.demov7.activity.SobotDemoBaseActivity
import com.sobot.demov7.util.SobotSPUtil.getObject

class SobotInfomationFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_info_fun_5: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var information: Information? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_infomation_func_activity

    override fun initView() {
        information = getObject(context, "sobot_demo_infomation") as Information?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "Information类说明"
        sobot_tv_left!!.setOnClickListener(this)
        tv_info_fun_5 = findViewById<View>(R.id.tv_info_fun_5) as TextView
        tv_info_fun_5!!.text =
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_5-Information类说明"
        tv_info_fun_5!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_5-information%E7%B1%BB%E8%AF%B4%E6%98%8E"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.INVISIBLE)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
    }

    val context: Context
        get() = this
}