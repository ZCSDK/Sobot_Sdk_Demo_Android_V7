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
import com.alibaba.fastjson.JSON
import com.sobot.chat.MarkConfig
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.ToastUtil
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.model.SobotDemoOtherModel
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveObject

class SobotLeaveMsgFunctionActivity : AppCompatActivity(), View.OnClickListener {
    private var sobot_et_leaveCusFieldMap: EditText? = null
    private var sobot_et_leaveMsgGroupId: EditText? = null
    private var sobot_et_leaveTemplateId: EditText? = null
    private var sobot_tv_left: RelativeLayout? = null
    private var sobot_rl_4_3_5: RelativeLayout? = null
    private var sobot_rl_4_3_7: RelativeLayout? = null
    private var sobotImage435: ImageView? = null
    private var sobotImage437: ImageView? = null
    private var status435 = false
    private var status437 = false
    private var tv_leavemsg_fun_4_3_5: TextView? = null
    private var tv_leavemsg_fun_4_3_7: TextView? = null
    private var tv_leavemsg_fun_4_3_8: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_start_leavemsg_tv: TextView? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_demo_leavemsg_func_activity)
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findvViews()
    }

    private fun findvViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        sobot_tv_left!!.setOnClickListener { finish() }
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "留言工单相关"
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        sobot_start_leavemsg_tv = findViewById(R.id.sobot_start_leavemsg_tv)
        sobot_start_leavemsg_tv!!.setOnClickListener(this)
        sobot_rl_4_3_5 = findViewById<View>(R.id.sobot_rl_4_3_5) as RelativeLayout
        sobot_rl_4_3_5!!.setOnClickListener(this)
        sobotImage435 = findViewById<View>(R.id.sobot_image_4_3_5) as ImageView
        sobot_rl_4_3_7 = findViewById<View>(R.id.sobot_rl_4_3_7) as RelativeLayout
        sobot_rl_4_3_7!!.setOnClickListener(this)
        sobotImage437 = findViewById<View>(R.id.sobot_image_4_3_7) as ImageView
        sobot_et_leaveCusFieldMap = findViewById(R.id.sobot_et_leaveCusFieldMap)
        sobot_et_leaveMsgGroupId = findViewById(R.id.sobot_et_leaveMsgGroupId)
        sobot_et_leaveTemplateId = findViewById(R.id.sobot_et_leaveTemplateId)
        if (information != null) {
            sobot_et_leaveMsgGroupId!!.setText(if (TextUtils.isEmpty(information!!.leaveMsgGroupId)) "" else information!!.leaveMsgGroupId)
            sobot_et_leaveTemplateId!!.setText(if (TextUtils.isEmpty(information!!.leaveTemplateId)) "" else information!!.leaveTemplateId)
            if (information!!.leaveCusFieldMap != null) {
                try {
                    val str = JSON.toJSONString(information!!.leaveCusFieldMap)
                    sobot_et_leaveCusFieldMap!!.setText(if (TextUtils.isEmpty(str)) "" else str)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            status435 = getBooleanData(this, "leave_complete_can_reply", false)
            setImageShowStatus(status435, sobotImage435)
            status437 = information!!.isShowLeaveDetailBackEvaluate
            setImageShowStatus(status437, sobotImage437)
        }
        tv_leavemsg_fun_4_3_5 = findViewById(R.id.tv_leavemsg_fun_4_3_5)
        tv_leavemsg_fun_4_3_5!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-5-已完成状态的留言详情界面的回复按钮可通过参数配置是否显示")
        setOnClick(
            tv_leavemsg_fun_4_3_5,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-5-%E5%B7%B2%E5%AE%8C%E6%88%90%E7%8A%B6%E6%80%81%E7%9A%84%E7%95%99%E8%A8%80%E8%AF%A6%E6%83%85%E7%95%8C%E9%9D%A2%E7%9A%84%E5%9B%9E%E5%A4%8D%E6%8C%89%E9%92%AE%E5%8F%AF%E9%80%9A%E8%BF%87%E5%8F%82%E6%95%B0%E9%85%8D%E7%BD%AE%E6%98%AF%E5%90%A6%E6%98%BE%E7%A4%BA"
        )
        tv_leavemsg_fun_4_3_7 = findViewById(R.id.tv_leavemsg_fun_4_3_7)
        tv_leavemsg_fun_4_3_7!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-7-添加留言评价主动提醒开关")
        setOnClick(
            tv_leavemsg_fun_4_3_7,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-7-%E6%B7%BB%E5%8A%A0%E7%95%99%E8%A8%80%E8%AF%84%E4%BB%B7%E4%B8%BB%E5%8A%A8%E6%8F%90%E9%86%92%E5%BC%80%E5%85%B3"
        )
        tv_leavemsg_fun_4_3_8 = findViewById(R.id.tv_leavemsg_fun_4_3_8)
        tv_leavemsg_fun_4_3_8!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-8-添加留言扩展参数")
        setOnClick(
            tv_leavemsg_fun_4_3_8,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-3-8-%E6%B7%BB%E5%8A%A0%E7%95%99%E8%A8%80%E6%89%A9%E5%B1%95%E5%8F%82%E6%95%B0"
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
                    val leaveCusFieldMap =
                        sobot_et_leaveCusFieldMap!!.text.toString().trim { it <= ' ' }
                    try {
                        val lcMap: Map<*, *> = JSON.parse(leaveCusFieldMap) as Map<String?, String?>
                        information!!.setLeaveCusFieldMap(lcMap as MutableMap<String, String>?)
                    } catch (e: Exception) {
                    }
                    val leaveMsgGroupId =
                        sobot_et_leaveMsgGroupId!!.text.toString().trim { it <= ' ' }
                    information!!.leaveMsgGroupId = leaveMsgGroupId
                    information!!.leaveTemplateId =
                        sobot_et_leaveTemplateId!!.text.toString().trim { it <= ' ' }
                    //已完成状态的留言，是否可持续回复 true 持续回复 ，false 不可继续回复 ；默认 true 用户可一直持续回复
                    ZCSobotApi.setSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY, status435)
                    saveBooleanData(this, "leave_complete_can_reply", status435)
                    //添加留言评价主动提醒开关
                    information!!.isShowLeaveDetailBackEvaluate = status437
                    saveObject(this, "sobot_demo_infomation", information!!)
                }
                ToastUtil.showToast(context, "已保存")
                finish()
            }
            R.id.sobot_rl_4_3_5 -> {
                status435 = !status435
                setImageShowStatus(status435, sobotImage435)
            }
            R.id.sobot_rl_4_3_7 -> {
                status437 = !status437
                setImageShowStatus(status437, sobotImage437)
            }
            R.id.sobot_start_leavemsg_tv -> {
                information!!.leaveTemplateId =
                    sobot_et_leaveTemplateId!!.text.toString().trim { it <= ' ' }
                ZCSobotApi.openLeave(context, information, false)
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