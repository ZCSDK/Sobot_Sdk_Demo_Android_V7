package com.sobot.demov7.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.demov7.R
import com.sobot.demov7.activity.product.SobotDemoCloudCallActivity
import com.sobot.demov7.activity.product.SobotDemoCustomActivity
import com.sobot.demov7.activity.product.SobotDemoRobotActivity
import com.sobot.demov7.activity.product.SobotDemoWorkOrderActivity
import com.sobot.demov7.util.SobotSPUtil.getObject
import com.sobot.demov7.util.SobotSPUtil.getStringData

class SobotDemoWelcomeFragment : Fragment(), View.OnClickListener {
    private var view: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = View.inflate(activity, R.layout.sobot_demo_welcome_fragment, null)
        findViewsById()
        return view
    }

    private fun findViewsById() {
        val sobot_robot_layout =
            view!!.findViewById<View>(R.id.sobot_demo_robot_layout) as LinearLayout
        val sobot_demo_custom_service_layout =
            view!!.findViewById<View>(R.id.sobot_demo_custom_service_layout) as LinearLayout
        val sobot_demo_cloud_call_layout =
            view!!.findViewById<View>(R.id.sobot_demo_cloud_call_layout) as LinearLayout
        val sobot_demo_work_roder_layout =
            view!!.findViewById<View>(R.id.sobot_demo_work_roder_layout) as LinearLayout
        val sobot_tv_right = view!!.findViewById<View>(R.id.sobot_tv_right) as TextView
        sobot_tv_right.setOnClickListener(this)
        sobot_robot_layout.setOnClickListener(this)
        sobot_demo_custom_service_layout.setOnClickListener(this)
        sobot_demo_cloud_call_layout.setOnClickListener(this)
        sobot_demo_work_roder_layout.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val intent: Intent
        when (v.id) {
            R.id.sobot_demo_robot_layout -> {
                intent = Intent(activity, SobotDemoRobotActivity::class.java)
                startActivity(intent)
            }
            R.id.sobot_demo_custom_service_layout -> {
                intent = Intent(activity, SobotDemoCustomActivity::class.java)
                startActivity(intent)
            }
            R.id.sobot_demo_cloud_call_layout -> {
                intent = Intent(activity, SobotDemoCloudCallActivity::class.java)
                startActivity(intent)
            }
            R.id.sobot_demo_work_roder_layout -> {
                intent = Intent(activity, SobotDemoWorkOrderActivity::class.java)
                startActivity(intent)
            }
            R.id.sobot_tv_right -> {
                val information = getObject(context!!, "sobot_demo_infomation") as Information?
                if (information != null) {
                    if (TextUtils.isEmpty(information.app_key)) {
                        ToastUtil.showCustomToast(activity, "appkey不能为空,请前往基础设置中设置")
                        return
                    }
                    val initSdk = SharedPreferencesUtil.getBooleanData(
                        activity,
                        ZhiChiConstant.SOBOT_CONFIG_INITSDK,
                        false
                    )
                    if (!initSdk) {
                        ToastUtil.showCustomToast(activity, "请前往基础设置中初始化后再启动")
                        return
                    }
                    val sobot_custom_language_value =
                        getStringData(activity!!, "custom_language_value", "")
//                    if (!TextUtils.isEmpty(sobot_custom_language_value)) {
//                        ZCSobotApi.setInternationalLanguage(
//                            activity,
//                            sobot_custom_language_value,
//                            true )
//                    }
                    information.isHideManualEvaluationLabels = true
                    information.isShowLeftBackPop = true
                    information.isShowSatisfaction = true
                    information.isCanBackWithNotEvaluation = true
                    information.isShowCloseBtn = true
                    information.isShowCloseSatisfaction = true
                    ZCSobotApi.openZCChat(context, information)
                }
            }
        }
    }
}