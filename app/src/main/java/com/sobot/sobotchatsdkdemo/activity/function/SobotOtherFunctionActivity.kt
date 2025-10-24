package com.sobot.sobotchatsdkdemo.activity.function

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sobot.chat.MarkConfig
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.activity.SobotDemoBaseActivity
import com.sobot.sobotchatsdkdemo.util.AndroidBug5497Workaround.Companion.assistActivity
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getStringData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveStringData

class SobotOtherFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var sobot_rl_4_7_6_1: RelativeLayout? = null
    private var sobot_rl_4_7_6_2: RelativeLayout? = null
    private var sobot_rl_4_7_8: RelativeLayout? = null
    private var sobotImage4761: ImageView? = null
    private var sobotImage4762: ImageView? = null
    private var sobotImage478: ImageView? = null
    private var status4761 = false
    private var status4762 = false
    private var status478 = false
    private var tv_other_fun_4_7_2: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_et_scope_time: EditText? = null
    private var sobot_et_langue: EditText? = null
    private var information: Information? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_other_func_activity

    override fun initView() {
        assistActivity(this)
        findViews()
    }

    private fun findViews() {
        information = getObject(context, "sobot_demo_infomation") as Information?
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        sobot_tv_left!!.setOnClickListener { finish() }
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "其它配置"
        sobot_et_scope_time = findViewById(R.id.sobot_et_scope_time)
        sobot_et_langue = findViewById(R.id.sobot_et_langue)
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        sobot_rl_4_7_6_1 = findViewById<View>(R.id.sobot_rl_4_7_6_1) as RelativeLayout
        sobot_rl_4_7_6_1!!.setOnClickListener(this)
        sobotImage4761 = findViewById<View>(R.id.sobot_image_4_7_6_1) as ImageView
        sobot_rl_4_7_6_2 = findViewById<View>(R.id.sobot_rl_4_7_6_2) as RelativeLayout
        sobot_rl_4_7_6_2!!.setOnClickListener(this)
        sobotImage4762 = findViewById<View>(R.id.sobot_image_4_7_6_2) as ImageView
        sobot_rl_4_7_8 = findViewById<View>(R.id.sobot_rl_4_7_8) as RelativeLayout
        sobot_rl_4_7_8!!.setOnClickListener(this)
        sobotImage478 = findViewById<View>(R.id.sobot_image_4_7_8) as ImageView
        if (information != null) {
            status4761 = information!!.isHideRototEvaluationLabels
            setImageShowStatus(status4761, sobotImage4761)
            status4762 = information!!.isHideManualEvaluationLabels
            setImageShowStatus(status4762, sobotImage4762)
        }
        status478 = getBooleanData(this, "auto_match_timezone", false)
        setImageShowStatus(status478, sobotImage478)
        sobot_et_scope_time!!.setText(
            SharedPreferencesUtil.getLongData(
                context,
                ZhiChiConstant.SOBOT_SCOPE_TIME,
                0
            ).toString() + ""
        )
        val sobot_custom_language_value = getStringData(this, "custom_language_value", "")
        if (!TextUtils.isEmpty(sobot_custom_language_value)) {
            sobot_et_langue!!.setText(sobot_custom_language_value)
        }
        tv_other_fun_4_7_2 = findViewById(R.id.tv_other_fun_4_7_2)
        tv_other_fun_4_7_2!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-7-2-自定义聊天记录显示时间范围")
        setOnClick(
            tv_other_fun_4_7_2,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-7-2-%E8%87%AA%E5%AE%9A%E4%B9%89%E8%81%8A%E5%A4%A9%E8%AE%B0%E5%BD%95%E6%98%BE%E7%A4%BA%E6%97%B6%E9%97%B4%E8%8C%83%E5%9B%B4"
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
        when (v.id) {
            R.id.sobot_tv_save -> {
                if (information != null) {
                    information!!.isHideManualEvaluationLabels = status4762
                    information!!.isHideRototEvaluationLabels = status4761
                    saveObject(this, "sobot_demo_infomation", information!!)
                }
                   saveBooleanData(this, "auto_match_timezone", status478)
                val scope_time = sobot_et_scope_time!!.text.toString().trim { it <= ' ' }
                if (!TextUtils.isEmpty(scope_time)) {
                    ZCSobotApi.setScope_time(context, scope_time.toLong())
                }
                ToastUtil.showToast(context, "已保存")
                if (TextUtils.isEmpty(sobot_et_langue!!.text.toString().trim { it <= ' ' })) {
                    ZCSobotApi.setInternationalLanguage(
                        applicationContext,
                        sobot_et_langue!!.text.toString().trim { it <= ' ' },
                        false
                    )
                    ZCSobotApi.hideTimemsgForMessageList(applicationContext, false)
                } else {
                    ZCSobotApi.setInternationalLanguage(
                        applicationContext,
                        sobot_et_langue!!.text.toString().trim { it <= ' ' },
                        true
                    )
                    ZCSobotApi.hideTimemsgForMessageList(applicationContext, false)
                }
                saveStringData(
                    this,
                    "custom_language_value",
                    sobot_et_langue!!.text.toString().trim { it <= ' ' })
                ZCSobotApi.outCurrentUserZCLibInfo(context,"")
                finish()
            }
            R.id.sobot_rl_4_7_6_1 -> {
                status4761 = !status4761
                setImageShowStatus(status4761, sobotImage4761)
            }
            R.id.sobot_rl_4_7_6_2 -> {
                status4762 = !status4762
                setImageShowStatus(status4762, sobotImage4762)
            }
            R.id.sobot_rl_4_7_8 -> {
                status478 = !status478
                setImageShowStatus(status478, sobotImage478)
            }
        }
    }

    private fun setImageShowStatus(status: Boolean, imageView: ImageView?) {
        if (status) {
            imageView!!.setBackgroundResource(R.drawable.sobot_demo_icon_open)
        } else {
            imageView!!.setBackgroundResource(R.drawable.sobot_demo_icon_close)
        }
    }

    val context: Context
        get() = this
}