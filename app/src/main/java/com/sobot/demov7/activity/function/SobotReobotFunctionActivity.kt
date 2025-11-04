package com.sobot.demov7.activity.function

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.demov7.R
import com.sobot.demov7.activity.SobotDemoBaseActivity
import com.sobot.demov7.util.AndroidBug5497Workaround.Companion.assistActivity
import com.sobot.demov7.util.SobotSPUtil.getObject
import com.sobot.demov7.util.SobotSPUtil.saveObject

class SobotReobotFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null

    private var tv_rebot_fun_4_1_1: TextView? = null
    private var tv_rebot_fun_4_1_2: TextView? = null
    private var tv_rebot_fun_4_1_4: TextView? = null
    private var tv_rebot_fun_4_1_5: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_et_rebot_id: EditText? = null
    private var sobot_et_rebot_alise: EditText? = null
    private var sobot_et_service_mode: EditText? = null
    private var sobot_et_rebot_faqid: EditText? = null
    private var information: Information? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_rebot_func_activity

    override fun initView() {
        assistActivity(this)
        information = getObject(context, "sobot_demo_infomation") as Information?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "机器人客服"
        sobot_tv_left!!.setOnClickListener(this)
        sobot_et_rebot_id = findViewById(R.id.sobot_et_rebot_id)
        sobot_et_rebot_alise = findViewById(R.id.sobot_et_rebot_alise)
        sobot_et_service_mode = findViewById(R.id.sobot_et_service_mode)
        sobot_et_rebot_faqid = findViewById(R.id.sobot_et_rebot_faqid)
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        if (information != null) {
            sobot_et_rebot_id!!.setText(if (TextUtils.isEmpty(information!!.robot_code)) "" else information!!.robot_code)
            sobot_et_rebot_alise!!.setText(if (TextUtils.isEmpty(information!!.robot_alias)) "" else information!!.robot_alias)
            sobot_et_service_mode!!.setText(information!!.service_mode.toString() + "")
            sobot_et_rebot_faqid!!.setText(information!!.faqId.toString() + "")
        }
        tv_rebot_fun_4_1_1 = findViewById(R.id.tv_rebot_fun_4_1_1)
        setOnClick(
            tv_rebot_fun_4_1_1,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-1-1-%E5%AF%B9%E6%8E%A5%E6%8C%87%E5%AE%9A%E6%9C%BA%E5%99%A8%E4%BA%BA"
        )
        tv_rebot_fun_4_1_2 = findViewById(R.id.tv_rebot_fun_4_1_2)
        setOnClick(
            tv_rebot_fun_4_1_2,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-1-2-%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8E%A5%E5%85%A5%E6%A8%A1%E5%BC%8F"
        )
        tv_rebot_fun_4_1_4 = findViewById(R.id.tv_rebot_fun_4_1_4)
        setOnClick(
            tv_rebot_fun_4_1_4,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-1-4-%E8%AE%BE%E7%BD%AE%E8%BD%AC%E4%BA%BA%E5%B7%A5%E6%BA%A2%E5%87%BA"
        )
        tv_rebot_fun_4_1_5 = findViewById(R.id.tv_rebot_fun_4_1_5)
        setOnClick(
            tv_rebot_fun_4_1_5,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-1-5-%E6%9C%BA%E5%99%A8%E4%BA%BA%E5%92%A8%E8%AF%A2%E6%A8%A1%E5%BC%8F%E4%B8%8B%E5%8F%AF%E9%9A%90%E8%97%8F%E5%8A%A0%E5%8F%B7%E8%8F%9C%E5%8D%95%E6%A0%8F%E7%9A%84%E6%8C%89%E9%92%AE"
        )
    }

    fun setOnClick(view: TextView?, url: String?) {
        view!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra("url", url)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        } else if (v === sobot_tv_save) {
            val rebotId = sobot_et_rebot_id!!.text.toString().trim { it <= ' ' }
            val rebotAlise = sobot_et_rebot_alise!!.text.toString().trim { it <= ' ' }
            val serviceMode = sobot_et_service_mode!!.text.toString().trim { it <= ' ' }
            val faqid = sobot_et_rebot_faqid!!.text.toString().trim { it <= ' ' }
            if (information != null) {
                information!!.robot_code = rebotId
                information!!.robot_alias = rebotAlise
                information!!.service_mode =
                    if (TextUtils.isEmpty(serviceMode)) -1 else serviceMode.toInt()
                if (!TextUtils.isEmpty(faqid)) {
                    information!!.faqId = faqid.toInt()
                } else {
                    information!!.faqId = 0
                }
                saveObject(this, "sobot_demo_infomation", information!!)
            }
            ToastUtil.showToast(context, "已保存")
            finish()
        }
    }

    val context: Context
        get() = this
}