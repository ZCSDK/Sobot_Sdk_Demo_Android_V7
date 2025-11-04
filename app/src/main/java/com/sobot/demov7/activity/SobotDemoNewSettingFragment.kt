package com.sobot.demov7.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.api.apiUtils.SobotVerControl
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.demov7.R
import com.sobot.demov7.activity.function.*
import com.sobot.demov7.model.SobotDemoOtherModel
import com.sobot.demov7.util.SobotSPUtil.getObject

class SobotDemoNewSettingFragment : Fragment(), View.OnClickListener {
    private var view: View? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = View.inflate(activity, R.layout.sobot_demo_new_setting_fragment, null)
        findViewsById()
        return view
    }

    private fun findViewsById() {
        val sobot_text_title = view!!.findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "功能说明"
        val rl_1 = view!!.findViewById<View>(R.id.rl_1) as RelativeLayout
        rl_1.setOnClickListener(this)
        val rl_2 = view!!.findViewById<View>(R.id.rl_2) as RelativeLayout
        rl_2.setOnClickListener(this)
        val rl_3 = view!!.findViewById<View>(R.id.rl_3) as RelativeLayout
        rl_3.setOnClickListener(this)
        if (SobotVerControl.isPlatformVer) {
            rl_3.visibility = View.VISIBLE
        } else {
            rl_3.visibility = View.GONE
        }
        val rl_4 = view!!.findViewById<View>(R.id.rl_4) as RelativeLayout
        rl_4.setOnClickListener(this)
        val rl_5 = view!!.findViewById<View>(R.id.rl_5) as RelativeLayout
        rl_5.setOnClickListener(this)
        val rl_6 = view!!.findViewById<View>(R.id.rl_6) as RelativeLayout
        rl_6.setOnClickListener(this)
        val rl_7 = view!!.findViewById<View>(R.id.rl_7) as RelativeLayout
        rl_7.setOnClickListener(this)
        val rl_8 = view!!.findViewById<View>(R.id.rl_8) as RelativeLayout
        rl_8.setOnClickListener(this)
        val rl_9 = view!!.findViewById<View>(R.id.rl_9) as RelativeLayout
        rl_9.setOnClickListener(this)
        val rl_10 = view!!.findViewById<View>(R.id.rl_10) as RelativeLayout
        rl_10.setOnClickListener(this)
        val rl_11 = view!!.findViewById<View>(R.id.rl_11) as RelativeLayout
        rl_11.setOnClickListener(this)
        val rl_12 = view!!.findViewById<View>(R.id.rl_12) as RelativeLayout
        rl_12.setOnClickListener(this)
        val rl_13 = view!!.findViewById<View>(R.id.rl_13) as RelativeLayout
        rl_13.setOnClickListener(this)
        val rl_14 = view!!.findViewById<View>(R.id.rl_14) as RelativeLayout
        rl_14.setOnClickListener(this)
        val rl_15 = view!!.findViewById<View>(R.id.rl_15) as RelativeLayout
        rl_15.setOnClickListener(this)
        val rl_116 = view!!.findViewById<View>(R.id.rl_116) as RelativeLayout
        rl_116.setOnClickListener(this)
        val rl_16 = view!!.findViewById<View>(R.id.rl_16) as RelativeLayout
        rl_16.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onClick(v: View) {
        val initSdk = SharedPreferencesUtil.getBooleanData(
            activity,
            ZhiChiConstant.SOBOT_CONFIG_INITSDK,
            false
        )
        information = getObject(context!!, "sobot_demo_infomation") as Information?
        otherModel = getObject(context!!, "sobot_demo_otherModel") as SobotDemoOtherModel?
        val intent: Intent
        when (v.id) {
            R.id.rl_1 -> {
                intent = Intent(activity, SobotBaseFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_2 -> {
                intent = Intent(activity, SobotInitSobotFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_3 -> {
                intent = Intent(activity, SobotInitPlatformSobotFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_4 -> {
                if (!initSdk) {
                    ToastUtil.showCustomToast(activity, "请先初始化再启动")
                    return
                }
                if (information != null) {
                    if (TextUtils.isEmpty(information!!.app_key)) {
                        ToastUtil.showCustomToast(activity, "appkey不能为空,请前往基础设置中设置")
                        return
                    }
                    intent = Intent(activity, SobotStartSobotFunctionActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.rl_5 -> if (information != null) {
                ZCSobotApi.openZCChatListView(activity, information!!.partnerid)
            }
            R.id.rl_6 -> {
                if (!initSdk) {
                    ToastUtil.showCustomToast(activity, "请先初始化再启动")
                    return
                }
                if (information != null) {
                    if (TextUtils.isEmpty(information!!.app_key)) {
                        ToastUtil.showCustomToast(activity, "appkey不能为空,请前往基础设置中设置")
                        return
                    }
                    intent = Intent(activity, SobotStartHelpCenterFunctionActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.rl_7 -> {
                intent = Intent(activity, SobotReobotFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_8 -> {
                intent = Intent(activity, SobotManualFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_9 -> {
                intent = Intent(activity, SobotLeaveMsgFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_10 -> {
                intent = Intent(activity, SobotSatisfactionFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_11 -> {
                intent = Intent(activity, SobotMessageFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_12 -> {
                intent = Intent(activity, SobotCustomUiFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_13 -> {
                intent = Intent(activity, SobotOtherFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_14 -> {
                intent = Intent(activity, SobotInfomationFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_15 -> {
                intent = Intent(activity, SobotEndSobotFunctionActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_16 -> {
                intent = Intent(activity, SobotDuolunActivity::class.java)
                startActivity(intent)
            }
            R.id.rl_116 -> {
                intent = Intent(activity, SobotUnReadMsgActivity::class.java)
                startActivity(intent)
            }
            else -> {}
        }
    }
}