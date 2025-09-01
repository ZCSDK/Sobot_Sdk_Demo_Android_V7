package com.sobot.sobotchatsdkdemo.activity.function

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.api.model.Information
import com.sobot.chat.api.model.NureadMsgModel
import com.sobot.chat.utils.DateUtil
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.widget.toast.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.network.http.callback.StringResultCallBack
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.activity.SobotDemoBaseActivity
import com.sobot.sobotchatsdkdemo.util.AndroidBug5497Workaround.Companion.assistActivity
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject

class SobotUnReadMsgActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_tv_left: RelativeLayout? = null
    private var tv_base_fun_16_1: TextView? = null
    private var update_appkey: TextView? = null
    private var information: Information? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_unread_msg_activity

    override fun initView() {
        assistActivity(this)
        information = getObject(this, "sobot_demo_infomation") as Information?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "未读消息数"
        sobot_tv_left!!.setOnClickListener(this)
        tv_base_fun_16_1 = findViewById(R.id.tv_base_fun_16_1)
//        tv_base_fun_16_1!!.setText("https://www.sobot.com/developerdocs/service/knowledge_base.html#_4、多轮会话接口调用")
//        tv_base_fun_16_1!!.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View) {
//                val intent = Intent(context, WebViewActivity::class.java)
//                intent.putExtra(
//                    "url",
//                    "https://www.sobot.com/developerdocs/service/knowledge_base.html#_4%E3%80%81%E5%A4%9A%E8%BD%AE%E4%BC%9A%E8%AF%9D%E6%8E%A5%E5%8F%A3%E8%B0%83%E7%94%A8"
//                )
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                context.startActivity(intent)
//            }
//        })
        update_appkey = findViewById(R.id.update_appkey)
        update_appkey!!.setOnClickListener(View.OnClickListener {
            val initSdk = SharedPreferencesUtil.getBooleanData(
                this,
                ZhiChiConstant.SOBOT_CONFIG_INITSDK,
                false
            )
            if (!initSdk) {
                ToastUtil.showCustomToast(this, "请先初始化再启动")
                return@OnClickListener
            }
            if (information != null) {
//                if(SobotStringUtils.isEmpty(information!!.app_key) ||SobotStringUtils.isEmpty(information!!.partnerid) ) {
//                    ToastUtil.showToast(
//                        this@SobotUnReadMsgActivity,
//                        "初始化appkey、partnerId"
//                    )
//                    return@OnClickListener
//                }
                ZCSobotApi.offlineMsgSize(
                    this,
                    information?.app_key ?: "",
                    information?.partnerid ?: "",
                    object : StringResultCallBack<NureadMsgModel> {
                        override fun onSuccess(nureadMsgModel: NureadMsgModel) {
                            //弹窗提示
                            val builder = StringBuilder()
                            builder.append("返回结果\n")
                            builder.append("未读消息数：").append(nureadMsgModel.unReadSize)
                                .append("条\n")
                            builder.append("离线消息数：").append(nureadMsgModel.offlineSize)
                                .append("条\n")
                            builder.append("未确认消息数：").append(nureadMsgModel.unAckSize)
                                .append("条\n")
                            builder.append("最近消息内容：").append(nureadMsgModel.message)
                                .append("条\n")
                            if (nureadMsgModel.time > 0) {
                                builder.append("最近消息时间：")
                                    .append(DateUtil.DATE_FORMAT.format(nureadMsgModel.time))
                                    .append("条\n")
                            }

                            tv_base_fun_16_1!!.text = builder.toString()
//                        Toast.makeText(
//                            this@SobotStartActivity,
//                            builder.toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
                        }

                        override fun onFailure(e: Exception, des: String) {
                            ToastUtil.showCustomToast(this@SobotUnReadMsgActivity, des)
                        }
                    })
            }

        })
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_left) {
            finish()
        }
    }

    val context: Context
        get() = this
}