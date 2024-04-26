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
import com.sobot.chat.SobotUIConfig
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.ZCSobotConstant
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.enumtype.SobotChatAvatarDisplayMode
import com.sobot.chat.api.enumtype.SobotChatTitleDisplayMode
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.utils.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.model.SobotDemoOtherModel
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getIntData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getStringData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveBooleanData
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveStringData

class SobotCustomUiFunctionActivity() : AppCompatActivity(), View.OnClickListener {
    private var sobot_et_custom_title: EditText? = null
    private var sobot_et_custom_avatar: EditText? = null
    private var sobot_et_custom_right_button_call: EditText? = null
    private var sobot_et_local_model: EditText? = null
    private var sobot_tv_left: RelativeLayout? = null
    private var sobot_rl_4_6_2: RelativeLayout? = null
    private var sobot_rl_4_6_2_2: RelativeLayout? = null
    private var sobot_rl_4_6_3: RelativeLayout? = null
    private var sobot_rl_4_6_4: RelativeLayout? = null
    private var sobot_rl_4_6_1_1: RelativeLayout? = null
    private var sobot_rl_4_6_1_2: RelativeLayout? = null
    private var sobot_rl_4_6_1_3: RelativeLayout? = null
    private var sobotImage462: ImageView? = null
    private var sobotImage4622: ImageView? = null
    private var sobotImage463: ImageView? = null
    private var sobotImage464: ImageView? = null
    private var sobotImage4611: ImageView? = null
    private var sobotImage4612: ImageView? = null
    private var sobotImage4613: ImageView? = null
    private var status462 = false
    private var status4622 = false
    private var status463 = false
    private var status464 = false
    private var status4611 = false
    private var status4612 = false
    private var status4613 = false
    private var tv_customui_fun_4_6_2: TextView? = null
    private var tv_customui_fun_4_6_3: TextView? = null
    private var tv_customui_fun_4_6_4: TextView? = null
    private var tv_customui_fun_4_6_5: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.sobot_demo_customui_func_activity)
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findvViews()
    }

    private fun findvViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        sobot_tv_left!!.setOnClickListener(View.OnClickListener { finish() })
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "会话页面自定义UI设置"
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        sobot_rl_4_6_2 = findViewById<View>(R.id.sobot_rl_4_6_2) as RelativeLayout
        sobot_rl_4_6_2!!.setOnClickListener(this)
        sobotImage462 = findViewById<View>(R.id.sobot_image_4_6_2) as ImageView
        sobot_rl_4_6_2_2 = findViewById<View>(R.id.sobot_rl_4_6_2_2) as RelativeLayout
        sobot_rl_4_6_2_2!!.setOnClickListener(this)
        sobotImage4622 = findViewById<View>(R.id.sobot_image_4_6_2_2) as ImageView
        sobot_rl_4_6_3 = findViewById<View>(R.id.sobot_rl_4_6_3) as RelativeLayout
        sobot_rl_4_6_3!!.setOnClickListener(this)
        sobotImage463 = findViewById<View>(R.id.sobot_image_4_6_3) as ImageView
        sobot_rl_4_6_4 = findViewById<View>(R.id.sobot_rl_4_6_4) as RelativeLayout
        sobot_rl_4_6_4!!.setOnClickListener(this)
        sobotImage464 = findViewById<View>(R.id.sobot_image_4_6_4) as ImageView
        sobot_rl_4_6_1_1 = findViewById<View>(R.id.sobot_rl_4_6_1_1) as RelativeLayout
        sobot_rl_4_6_1_1!!.setOnClickListener(this)
        sobotImage4611 = findViewById<View>(R.id.sobot_image_4_6_1_1) as ImageView
        sobot_rl_4_6_1_2 = findViewById<View>(R.id.sobot_rl_4_6_1_2) as RelativeLayout
        sobot_rl_4_6_1_2!!.setOnClickListener(this)
        sobotImage4612 = findViewById<View>(R.id.sobot_image_4_6_1_2) as ImageView
        sobot_rl_4_6_1_3 = findViewById<View>(R.id.sobot_rl_4_6_1_3) as RelativeLayout
        sobot_rl_4_6_1_3!!.setOnClickListener(this)
        sobotImage4613 = findViewById<View>(R.id.sobot_image_4_6_1_3) as ImageView
        sobot_et_custom_title = findViewById(R.id.sobot_et_custom_title)
        sobot_et_custom_avatar = findViewById(R.id.sobot_et_custom_avatar)
        sobot_et_custom_right_button_call = findViewById(R.id.sobot_et_custom_right_button_call)
        sobot_et_local_model = findViewById(R.id.sobot_et_local_model)
        val title = SharedPreferencesUtil.getStringData(
            context, ZhiChiConstant.SOBOT_CHAT_TITLE_DISPLAY_CONTENT,
            ""
        )
        sobot_et_custom_title!!.setText(title)
        val avatar = SharedPreferencesUtil.getStringData(
            context, ZhiChiConstant.SOBOT_CHAT_AVATAR_DISPLAY_CONTENT,
            ""
        )
        sobot_et_custom_avatar!!.setText(avatar)
        val callNum = getStringData(
            context,
            "sobot_et_custom_right_button_call",
            sobot_et_custom_avatar!!.getText().toString().trim { it <= ' ' })
        sobot_et_custom_right_button_call!!.setText(callNum)
        val localModel = SharedPreferencesUtil.getIntData(
            context,
            ZCSobotConstant.LOCAL_NIGHT_MODE,
            -1
        )
        sobot_et_local_model!!.setText(localModel.toString())
        status462 = SharedPreferencesUtil.getBooleanData(
            context,
            ZhiChiConstant.SOBOT_CHAT_TITLE_IS_SHOW,
            false
        )
        setImageShowStatus(status462, sobotImage462)
        status4622 = SharedPreferencesUtil.getBooleanData(
            context, ZhiChiConstant.SOBOT_CHAT_AVATAR_IS_SHOW,
            true
        )
        setImageShowStatus(status4622, sobotImage4622)
        status463 = getBooleanData(this, "landscape_screen", false)
        setImageShowStatus(status463, sobotImage463)
        status464 = getBooleanData(this, "display_innotch", false)
        setImageShowStatus(status464, sobotImage464)
        status4611 = getBooleanData(this, "sobot_title_right_menu1_display", false)
        setImageShowStatus(status4611, sobotImage4611)
        status4612 = getBooleanData(this, "sobot_title_right_menu2_display", false)
        setImageShowStatus(status4612, sobotImage4612)
        status4613 = getBooleanData(this, "sobot_title_right_menu3_display", false)
        setImageShowStatus(status4613, sobotImage4613)
        tv_customui_fun_4_6_2 = findViewById(R.id.tv_customui_fun_4_6_2)
        tv_customui_fun_4_6_2!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-2-动态控制显示标题栏的头像和昵称")
        setOnClick(
            tv_customui_fun_4_6_2,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-2-%E5%8A%A8%E6%80%81%E6%8E%A7%E5%88%B6%E6%98%BE%E7%A4%BA%E6%A0%87%E9%A2%98%E6%A0%8F%E7%9A%84%E5%A4%B4%E5%83%8F%E5%92%8C%E6%98%B5%E7%A7%B0"
        )
        tv_customui_fun_4_6_3 = findViewById(R.id.tv_customui_fun_4_6_3)
        tv_customui_fun_4_6_3!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-3-控制横竖屏显示开关")
        setOnClick(
            tv_customui_fun_4_6_3,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-3-%E6%8E%A7%E5%88%B6%E6%A8%AA%E7%AB%96%E5%B1%8F%E6%98%BE%E7%A4%BA%E5%BC%80%E5%85%B3"
        )
        tv_customui_fun_4_6_4 = findViewById(R.id.tv_customui_fun_4_6_4)
        tv_customui_fun_4_6_4!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-4-横屏下是否打开刘海屏开关")
        setOnClick(
            tv_customui_fun_4_6_4,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-4-%E6%A8%AA%E5%B1%8F%E4%B8%8B%E6%98%AF%E5%90%A6%E6%89%93%E5%BC%80%E5%88%98%E6%B5%B7%E5%B1%8F%E5%BC%80%E5%85%B3"
        )
        tv_customui_fun_4_6_5 = findViewById(R.id.tv_customui_fun_4_6_5)
        tv_customui_fun_4_6_5!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-5-ui样式通过同名资源替换")
        setOnClick(
            tv_customui_fun_4_6_5,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-6-5-ui%E6%A0%B7%E5%BC%8F%E9%80%9A%E8%BF%87%E5%90%8C%E5%90%8D%E8%B5%84%E6%BA%90%E6%9B%BF%E6%8D%A2"
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
                    val custom_title = sobot_et_custom_title!!.text.toString().trim { it <= ' ' }
                    if (TextUtils.isEmpty(custom_title)) {
                        ZCSobotApi.setChatTitleDisplayMode(
                            applicationContext,
                            SobotChatTitleDisplayMode.Default, "", status462
                        )
                    } else {
                        ZCSobotApi.setChatTitleDisplayMode(
                            applicationContext,
                            SobotChatTitleDisplayMode.ShowFixedText, custom_title, status462
                        )
                    }
                    val custom_avatar = sobot_et_custom_avatar!!.text.toString().trim { it <= ' ' }
                    if (TextUtils.isEmpty(custom_avatar)) {
                        ZCSobotApi.setChatAvatarDisplayMode(
                            applicationContext,
                            SobotChatAvatarDisplayMode.Default,
                            "",
                            status4622
                        )
                    } else {
                        ZCSobotApi.setChatAvatarDisplayMode(
                            applicationContext,
                            SobotChatAvatarDisplayMode.ShowFixedAvatar,
                            custom_avatar,
                            status4622
                        )
                    }
                    //true 横屏 , false 竖屏; 默认 false 竖屏
                    ZCSobotApi.setSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN, status463)
                    saveBooleanData(this, "landscape_screen", status463)
                    //只有在横屏下才有用;竖屏已适配，可修改状态栏颜色
                    //true 打开 ,false 关闭; 默认 false 关闭
                    ZCSobotApi.setSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH, status464)
                    saveBooleanData(this, "display_innotch", status464)
                    saveBooleanData(this, "sobot_title_right_menu1_display", status4611)
                    saveBooleanData(this, "sobot_title_right_menu2_display", status4612)
                    saveBooleanData(this, "sobot_title_right_menu3_display", status4613)
                    saveStringData(
                        context,
                        "sobot_et_custom_right_button_call",
                        sobot_et_custom_right_button_call!!.text.toString().trim { it <= ' ' })
                    (sobot_et_local_model!!.text.toString().trim { it <= ' ' }).toIntOrNull()?.let {
                        SharedPreferencesUtil.saveIntData(
                            context,
                            ZCSobotConstant.LOCAL_NIGHT_MODE,
                            it
                        )
                    }
                    //设置 toolbar右边第一个按钮是否显示（更多）
                    SobotUIConfig.sobot_title_right_menu1_display = status4611
                    //设置 toolbar右边第二个按钮是否显示（评价）
                    SobotUIConfig.sobot_title_right_menu2_display = status4612
                    //设置 toolbar右边第三个按钮是否显示（电话）
                    SobotUIConfig.sobot_title_right_menu3_display = status4613
                    // toolbar右边第三个按钮电话对应的电话号
                    SobotUIConfig.sobot_title_right_menu3_call_num =
                        sobot_et_custom_right_button_call!!.text.toString().trim { it <= ' ' }
                    (sobot_et_local_model!!.text.toString().trim { it <= ' ' }).toIntOrNull()?.let {
                        ZCSobotApi.setLocalNightMode(
                            this,
                            it
                        )
                    }

                }
                ToastUtil.showToast(context, "已保存")
                finish()
            }

            R.id.sobot_rl_4_6_2 -> {
                status462 = !status462
                setImageShowStatus(status462, sobotImage462)
            }

            R.id.sobot_rl_4_6_2_2 -> {
                status4622 = !status4622
                setImageShowStatus(status4622, sobotImage4622)
            }

            R.id.sobot_rl_4_6_3 -> {
                status463 = !status463
                setImageShowStatus(status463, sobotImage463)
            }

            R.id.sobot_rl_4_6_4 -> {
                status464 = !status464
                setImageShowStatus(status464, sobotImage464)
            }

            R.id.sobot_rl_4_6_1_1 -> {
                status4611 = !status4611
                setImageShowStatus(status4611, sobotImage4611)
            }

            R.id.sobot_rl_4_6_1_2 -> {
                status4612 = !status4612
                setImageShowStatus(status4612, sobotImage4612)
            }

            R.id.sobot_rl_4_6_1_3 -> {
                status4613 = !status4613
                setImageShowStatus(status4613, sobotImage4613)
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