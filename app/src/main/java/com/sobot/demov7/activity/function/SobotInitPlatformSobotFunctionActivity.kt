package com.sobot.demov7.activity.function

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.demov7.R
import com.sobot.demov7.activity.SobotDemoBaseActivity
import com.sobot.demov7.model.SobotDemoOtherModel
import com.sobot.demov7.util.SobotSPUtil.getObject

class SobotInitPlatformSobotFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_start_fun_3_3_2: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_tv_init_platform: TextView? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_initplatform_sobot_func_activity

    override fun initView() {
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "初始化电商版"
        sobot_tv_left!!.setOnClickListener(this)
        tv_start_fun_3_3_2 = findViewById<View>(R.id.tv_start_fun_3_3_2) as TextView
        tv_start_fun_3_3_2!!.text =
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-3-2-电商版"
        tv_start_fun_3_3_2!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://www.sobot.com/developerdocs/app_sdk/android.html#_3-3-2-%E7%94%B5%E5%95%86%E7%89%88"
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        })
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.INVISIBLE)
        sobot_tv_init_platform = findViewById(R.id.sobot_tv_init_platform)
        sobot_tv_init_platform!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
        if (v === sobot_tv_init_platform) {
            if (otherModel != null) {
                if (TextUtils.isEmpty(otherModel!!.platformUnionCode)) {
                    ToastUtil.showCustomToast(context, "平台id不能为空,请前往基础设置中设置")
                    return
                }
            }
            if (information != null) {
                if (TextUtils.isEmpty(information!!.app_key)) {
                    ToastUtil.showCustomToast(context, "appkey不能为空,请前往基础设置中设置")
                    return
                }
                ZCSobotApi.initSobotSDK(context, information!!.app_key, information!!.partnerid)
                ZCSobotApi.initPlatformUnion(
                    context,
                    otherModel!!.platformUnionCode,
                    otherModel!!.platformSecretkey
                )
                ToastUtil.showCustomToast(context, "已初始化")
                finish()
            }
        }
    }

    val context: Context
        get() = this
}