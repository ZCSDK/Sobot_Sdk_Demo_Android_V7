package com.sobot.sobotchatsdkdemo.activity.function

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.ToastUtil
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject

class SobotEndSobotFunctionActivity : AppCompatActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_start_fun_3_5: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_tv_end: TextView? = null
    private var information: Information? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_demo_endsobot_func_activity)
        information = getObject(context, "sobot_demo_infomation") as Information?
        findvViews()
    }

    private fun findvViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "结束会话"
        sobot_tv_left!!.setOnClickListener(this)
        tv_start_fun_3_5 = findViewById<View>(R.id.tv_start_fun_3_5) as TextView
        tv_start_fun_3_5!!.text =
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-5-结束会话"
        tv_start_fun_3_5!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-5-%E7%BB%93%E6%9D%9F%E4%BC%9A%E8%AF%9D"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.GONE)
        sobot_tv_end = findViewById(R.id.sobot_tv_end)
        sobot_tv_end!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_tv_end) {
            if (information != null) {
                ZCSobotApi.outCurrentUserZCLibInfo(context,"")
                ToastUtil.showToast(context, "已结束会话")
                finish()
            }
        }
    }

    val context: Context
        get() = this
}