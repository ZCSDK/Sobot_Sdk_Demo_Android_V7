package com.sobot.demov7.activity.function

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.demov7.R
import com.sobot.demov7.activity.SobotDemoBaseActivity
import com.sobot.demov7.model.SobotDemoOtherModel
import com.sobot.demov7.util.AndroidBug5497Workaround.Companion.assistActivity
import com.sobot.demov7.util.SobotSPUtil.getObject
import com.sobot.demov7.util.SobotSPUtil.saveObject

class SobotDuolunActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_base_fun_16_1: TextView? = null
    private var update_appkey: TextView? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_duolun_func_activity

    override fun initView() {
        assistActivity(this)
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "多轮会话"
        sobot_tv_left!!.setOnClickListener(this)
        if (otherModel != null) {
        }
        tv_base_fun_16_1 = findViewById(R.id.tv_base_fun_16_1)
        tv_base_fun_16_1!!.setText("https://www.sobot.com/developerdocs/service/knowledge_base.html#_4、多轮会话接口调用")
        tv_base_fun_16_1!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/service/knowledge_base.html#_4%E3%80%81%E5%A4%9A%E8%BD%AE%E4%BC%9A%E8%AF%9D%E6%8E%A5%E5%8F%A3%E8%B0%83%E7%94%A8"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        update_appkey = findViewById(R.id.update_appkey)
        update_appkey!!.setOnClickListener(View.OnClickListener {
            if (information != null) {
                information!!.app_key = "2497bd56d5ec42479f3b0bc0cf199ba2"
                saveObject(this@SobotDuolunActivity, "sobot_demo_infomation", information!!)
            }
            if (otherModel != null) {
                otherModel!!.api_host = "https://api.sobot.com"
                saveObject(this@SobotDuolunActivity, "sobot_demo_otherModel", otherModel!!)
            }
            ToastUtil.showCustomToastWithListenr(
                this@SobotDuolunActivity,
                "已替换成体验appkey，请重新初始化体验！",
                3000
            ) { finish() }
        })
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
    }

    val context: Context
        get() = this
}