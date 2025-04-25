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
import com.sobot.chat.utils.MD5Util
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getStringData
import com.sobot.utils.SobotStringUtils

class SobotStartSobotFunctionActivity : AppCompatActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_start_fun_3_4: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_tv_start: TextView? = null
    private var information: Information? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_demo_startsobot_func_activity)
        information = getObject(context, "sobot_demo_infomation") as Information?
        findvViews()
    }

    private fun findvViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "启动客服"
        sobot_tv_left!!.setOnClickListener(this)
        tv_start_fun_3_4 = findViewById<View>(R.id.tv_start_fun_3_4) as TextView
        tv_start_fun_3_4!!.text =
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-4-1-启动智齿页面"
        tv_start_fun_3_4!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-4-1-%E5%90%AF%E5%8A%A8%E6%99%BA%E9%BD%BF%E9%A1%B5%E9%9D%A2"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.GONE)
        sobot_tv_start = findViewById(R.id.sobot_tv_start)
        sobot_tv_start!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_tv_start) {
            if (information != null) {
                information!!.isUseRobotVoice = true
                val sobot_custom_language_value = getStringData(this, "custom_language_value", "")
                if (!TextUtils.isEmpty(sobot_custom_language_value)) {
                    ZCSobotApi.setInternationalLanguage(
                        applicationContext,
                        sobot_custom_language_value,
                        true
                    )
                }
                if(SobotStringUtils.isNoEmpty(information!!.sign)){
                    information!!.sign = MD5Util.encode(information!!.app_key + information!!.partnerid + information!!.sign + System.currentTimeMillis())
                }
                ZCSobotApi.openZCChat(context, information)
            }
        }
    }

    val context: Context
        get() = this
}