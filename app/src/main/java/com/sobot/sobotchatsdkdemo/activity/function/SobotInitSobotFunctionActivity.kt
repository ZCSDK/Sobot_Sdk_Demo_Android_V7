package com.sobot.sobotchatsdkdemo.activity.function

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.activity.SobotDemoBaseActivity
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject

class SobotInitSobotFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_start_fun_3_3_1: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_tv_init: TextView? = null
    private var information: Information? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_initsobot_func_activity

    override fun initView() {
        information = getObject(context, "sobot_demo_infomation") as Information?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "初始化普通版"
        sobot_tv_left!!.setOnClickListener(this)
        tv_start_fun_3_3_1 = findViewById<View>(R.id.tv_start_fun_3_3_1) as TextView
        tv_start_fun_3_3_1!!.text =
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-3-1-普通版"
        tv_start_fun_3_3_1!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-3-1-%E6%99%AE%E9%80%9A%E7%89%88"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.INVISIBLE)
        sobot_tv_init = findViewById(R.id.sobot_tv_init)
        sobot_tv_init!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_tv_init) {
            if (information != null) {
                if (TextUtils.isEmpty(information!!.app_key)) {
                    ToastUtil.showCustomToast(context, "appkey不能为空,请前往基础设置中设置")
                    return
                }
                ZCSobotApi.initSobotSDK(context, information!!.app_key, information!!.partnerid)
                ZCSobotApi.initPlatformUnion(context, "", "")
                ToastUtil.showCustomToast(context, "已初始化")
                finish()
            }
        }
    }

    val context: Context
        get() = this
}