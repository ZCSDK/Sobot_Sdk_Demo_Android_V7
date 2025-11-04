package com.sobot.demov7.activity.function

import android.content.Context
import android.content.Intent
import android.os.Process
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.demov7.R
import com.sobot.demov7.activity.SobotDemoBaseActivity
import com.sobot.demov7.activity.SplashActivity
import com.sobot.demov7.model.SobotDemoOtherModel
import com.sobot.demov7.util.AndroidBug5497Workaround.Companion.assistActivity
import com.sobot.demov7.util.SobotSPUtil.getObject
import com.sobot.demov7.util.SobotSPUtil.saveObject

class SobotBaseFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_base_fun_3_1: TextView? = null
    private var tv_base_fun_3_2: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_et_yuming: EditText? = null
    private var sobot_et_appkey: EditText? = null
    private var sobot_et_sign: EditText? = null
    private var sobot_et_pingtaibiaoshi: EditText? = null
    private var sobot_et_pingtaimiyao: EditText? = null
    private var sobot_et_partnerid: EditText? = null
    private var sobot_et_country: EditText? = null
    private var sobot_et_shiqu: EditText? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_base_func_activity


    override fun initView() {
        assistActivity(this)
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "基础设置"

        sobot_tv_left!!.setOnClickListener(this)
        sobot_et_yuming = findViewById(R.id.sobot_et_yuming)
        sobot_et_sign = findViewById(R.id.sobot_et_sign)
        sobot_et_appkey = findViewById(R.id.sobot_et_appkey)
        sobot_et_pingtaibiaoshi = findViewById(R.id.sobot_et_pingtaibiaoshi)
        sobot_et_pingtaimiyao = findViewById(R.id.sobot_et_pingtaimiyao)
        sobot_et_partnerid = findViewById(R.id.sobot_et_partnerid)
        sobot_et_shiqu = findViewById(R.id.sobot_et_shiqu)
        sobot_et_country = findViewById(R.id.sobot_et_country)
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        if (information != null) {
            sobot_et_appkey!!.setText(information!!.app_key)
            sobot_et_sign !!.setText(information!!.sign)
            sobot_et_partnerid!!.setText(information!!.partnerid)
            sobot_et_shiqu!!.setText(information!!.timezoneId)
            sobot_et_country!!.setText(information!!.countryName)
        }
        if (otherModel != null) {
            if (!TextUtils.isEmpty(otherModel!!.api_host)) {
                sobot_et_yuming!!.setText(otherModel!!.api_host)
            }
            sobot_et_pingtaibiaoshi!!.setText(otherModel!!.platformUnionCode)
            sobot_et_pingtaimiyao!!.setText(otherModel!!.platformSecretkey)
        }
        tv_base_fun_3_1 = findViewById(R.id.tv_base_fun_3_1)
        tv_base_fun_3_1!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_3-1-域名设置")
        tv_base_fun_3_1!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-1-%E5%9F%9F%E5%90%8D%E8%AE%BE%E7%BD%AE"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        tv_base_fun_3_2 = findViewById(R.id.tv_base_fun_3_2)
        tv_base_fun_3_2!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_3-2-获取appkey")
        tv_base_fun_3_2!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-2-%E8%8E%B7%E5%8F%96appkey"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_tv_save) {
            val yuming = sobot_et_yuming!!.text.toString().trim { it <= ' ' }
            if (information != null) {
                information!!.app_key = sobot_et_appkey!!.text.toString().trim { it <= ' ' }
                information!!.partnerid = sobot_et_partnerid!!.text.toString().trim { it <= ' ' }
                information!!.countryName = sobot_et_country!!.text.toString().trim { it <= ' ' }
                information!!.timezoneId = sobot_et_shiqu!!.text.toString().trim { it <= ' ' }
                information!!.sign =sobot_et_sign!!.text.toString().trim { it <= ' ' }
                saveObject(this, "sobot_demo_infomation", information!!)
            }
            if (otherModel != null) {
                val oldYUming = otherModel!!.api_host
                if (!TextUtils.isEmpty(yuming)) {
                    if (yuming != oldYUming) {
                        otherModel!!.api_host = yuming
                        saveObject(this, "sobot_demo_otherModel", otherModel!!)
                        val intent = Intent(this, SplashActivity::class.java) // 替换为你的欢迎页 Activity
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                        Process.killProcess(Process.myPid()) // 强制结束当前进程（可选）

                    }
                } else {
                    otherModel!!.api_host = "https://api.sobot.com"
                }
                otherModel!!.platformSecretkey =
                    sobot_et_pingtaimiyao!!.text.toString().trim { it <= ' ' }
                otherModel!!.platformUnionCode =
                    sobot_et_pingtaibiaoshi!!.text.toString().trim { it <= ' ' }
                saveObject(this, "sobot_demo_otherModel", otherModel!!)
            }
            ToastUtil.showToast(context, "已保存")
            SharedPreferencesUtil.saveBooleanData(this, ZhiChiConstant.SOBOT_CONFIG_INITSDK, false)
            finish()
        }
    }

    val context: Context
        get() = this
}