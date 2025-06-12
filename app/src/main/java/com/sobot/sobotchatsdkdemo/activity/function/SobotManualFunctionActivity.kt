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
import com.sobot.chat.activity.WebViewActivity
import com.sobot.chat.api.enumtype.SobotAutoSendMsgMode
import com.sobot.chat.api.model.ConsultingContent
import com.sobot.chat.api.model.Information
import com.sobot.chat.api.model.OrderCardContentModel
import com.sobot.chat.api.model.OrderCardContentModel.Goods
import com.sobot.chat.utils.CommonUtils
import com.sobot.chat.utils.ToastUtil
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.activity.SobotDemoBaseActivity
import com.sobot.sobotchatsdkdemo.model.SobotDemoOtherModel
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.saveObject
import java.io.File

class SobotManualFunctionActivity : SobotDemoBaseActivity(), View.OnClickListener {
    private var sobot_et_groupid: EditText? = null
    private var sobot_et_choose_adminid: EditText? = null
    private var sobot_et_tranReceptionistFlag: EditText? = null
    private var sobot_et_customer_fields: EditText? = null
    private var sobot_et_customer_params: EditText? = null
    private var sobot_et_autoSendMsgMode: EditText? = null
    private var sobot_et_autoSendMsgcontent: EditText? = null
    private var sobot_et_autoSendMsgtype: EditText? = null
    private var sobot_et_queue_First: EditText? = null
    private var sobot_et_summary_params: EditText? = null
    private var sobot_et_multi_params: EditText? = null
    private var sobot_et_vip_level: EditText? = null
    private var sobot_et_user_label: EditText? = null
    private var sobot_et_autoSendMsg_count: EditText? = null
    private var sobot_tv_left: RelativeLayout? = null
    private var sobot_rl_4_2_8: RelativeLayout? = null
    private var sobot_rl_4_2_9: RelativeLayout? = null
    private var sobot_rl_4_2_11: RelativeLayout? = null
    private var sobot_rl_4_2_13_1: RelativeLayout? = null
    private var sobot_rl_4_2_13_2: RelativeLayout? = null
    private var sobot_rl_4_2_13_3: RelativeLayout? = null
    private var sobot_rl_4_2_13_4: RelativeLayout? = null
    private var sobot_rl_4_2_13_5: RelativeLayout? = null
    private var sobot_rl_4_2_13_6: RelativeLayout? = null
    private var sobot_rl_4_2_13_7: RelativeLayout? = null
    private var sobotImage428: ImageView? = null
    private var sobotImage429: ImageView? = null
    private var sobotImage4211: ImageView? = null
    private var sobotImage42131: ImageView? = null
    private var sobotImage42132: ImageView? = null
    private var sobotImage42133: ImageView? = null
    private var sobotImage42134: ImageView? = null
    private var sobotImage42135: ImageView? = null
    private var sobotImage42136: ImageView? = null
    private var sobotImage42137: ImageView? = null
    private var status428 = false
    private var status429 = false
    private var status4211 = false
    private var status42131 = false
    private var status42132 = false
    private var status42133 = false
    private var status42134 = false
    private var status42135 = false
    private var status42136 = false
    private var status42137 = false
    private var tv_manual_fun_4_2_1: TextView? = null
    private var tv_manual_fun_4_2_2: TextView? = null
    private var tv_manual_fun_4_2_8: TextView? = null
    private var tv_manual_fun_4_2_9: TextView? = null
    private var tv_manual_fun_4_2_13: TextView? = null
    private var sobot_tv_save: TextView? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null

    override val contentViewResId: Int
        get() = R.layout.sobot_demo_manual_func_activity

    override fun initView() {
        information = getObject(context, "sobot_demo_infomation") as Information?
        otherModel = getObject(context, "sobot_demo_otherModel") as SobotDemoOtherModel?
        findViews()
    }

    private fun findViews() {
        sobot_tv_left = findViewById<View>(R.id.sobot_demo_tv_left) as RelativeLayout
        val sobot_text_title = findViewById<View>(R.id.sobot_demo_tv_title) as TextView
        sobot_text_title.text = "人工客服"
        sobot_tv_left!!.setOnClickListener { finish() }
        sobot_tv_save = findViewById(R.id.sobot_tv_save)
        sobot_tv_save!!.setOnClickListener(this)
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_rl_4_2_8 = findViewById<View>(R.id.sobot_rl_4_2_8) as RelativeLayout
        sobot_rl_4_2_8!!.setOnClickListener(this)
        sobotImage428 = findViewById<View>(R.id.sobot_image_4_2_8) as ImageView
        sobot_rl_4_2_9 = findViewById<View>(R.id.sobot_rl_4_2_9) as RelativeLayout
        sobot_rl_4_2_9!!.setOnClickListener(this)
        sobotImage429 = findViewById<View>(R.id.sobot_image_4_2_9) as ImageView
        sobot_rl_4_2_11 = findViewById<View>(R.id.sobot_rl_4_2_11) as RelativeLayout
        sobot_rl_4_2_11!!.setOnClickListener(this)
        sobotImage4211 = findViewById<View>(R.id.sobot_image_4_2_11) as ImageView
        sobot_rl_4_2_13_1 = findViewById<View>(R.id.sobot_rl_4_2_13_1) as RelativeLayout
        sobot_rl_4_2_13_1!!.setOnClickListener(this)
        sobotImage42131 = findViewById<View>(R.id.sobot_image_4_2_13_1) as ImageView
        sobot_rl_4_2_13_2 = findViewById<View>(R.id.sobot_rl_4_2_13_2) as RelativeLayout
        sobot_rl_4_2_13_2!!.setOnClickListener(this)
        sobotImage42132 = findViewById<View>(R.id.sobot_image_4_2_13_2) as ImageView
        sobot_rl_4_2_13_3 = findViewById<View>(R.id.sobot_rl_4_2_13_3) as RelativeLayout
        sobot_rl_4_2_13_3!!.setOnClickListener(this)
        sobotImage42133 = findViewById<View>(R.id.sobot_image_4_2_13_3) as ImageView
        sobot_rl_4_2_13_4 = findViewById<View>(R.id.sobot_rl_4_2_13_4) as RelativeLayout
        sobot_rl_4_2_13_4!!.setOnClickListener(this)
        sobotImage42134 = findViewById<View>(R.id.sobot_image_4_2_13_4) as ImageView
        sobot_rl_4_2_13_5 = findViewById<View>(R.id.sobot_rl_4_2_13_5) as RelativeLayout
        sobot_rl_4_2_13_5!!.setOnClickListener(this)
        sobotImage42135 = findViewById<View>(R.id.sobot_image_4_2_13_5) as ImageView
        sobot_rl_4_2_13_6 = findViewById<View>(R.id.sobot_rl_4_2_13_6) as RelativeLayout
        sobot_rl_4_2_13_6!!.setOnClickListener(this)
        sobotImage42136 = findViewById<View>(R.id.sobot_image_4_2_13_6) as ImageView
        sobot_rl_4_2_13_7 = findViewById<View>(R.id.sobot_rl_4_2_13_7) as RelativeLayout
        sobot_rl_4_2_13_7!!.setOnClickListener(this)
        sobotImage42137 = findViewById<View>(R.id.sobot_image_4_2_13_7) as ImageView
        sobot_et_groupid = findViewById(R.id.sobot_et_groupid)
        sobot_et_choose_adminid = findViewById(R.id.sobot_et_choose_adminid)
        sobot_et_tranReceptionistFlag = findViewById(R.id.sobot_et_tranReceptionistFlag)
        sobot_et_customer_fields = findViewById(R.id.sobot_et_customer_fields)
        sobot_et_customer_params = findViewById(R.id.sobot_et_customer_params)
        sobot_et_autoSendMsgMode = findViewById(R.id.sobot_et_autoSendMsgMode)
        sobot_et_autoSendMsgcontent = findViewById(R.id.sobot_et_autoSendMsgcontent)
        sobot_et_autoSendMsgtype = findViewById(R.id.sobot_et_autoSendMsgtype)
        sobot_et_autoSendMsg_count = findViewById(R.id.sobot_et_autoSendMsg_count)
        sobot_et_queue_First = findViewById(R.id.sobot_et_queue_First)
        sobot_et_summary_params = findViewById(R.id.sobot_et_summary_params)
        sobot_et_multi_params = findViewById(R.id.sobot_et_multi_params)
        sobot_et_vip_level = findViewById(R.id.sobot_et_vip_level)
        sobot_et_user_label = findViewById(R.id.sobot_et_user_label)
        if (information != null) {
            sobot_et_groupid!!.setText(if (TextUtils.isEmpty(information!!.groupid)) "" else information!!.groupid)
            sobot_et_choose_adminid!!.setText(if (TextUtils.isEmpty(information!!.choose_adminid)) "" else information!!.choose_adminid)
            sobot_et_tranReceptionistFlag!!.setText(information!!.tranReceptionistFlag.toString() + "")
            sobot_et_customer_fields!!.setText(if (TextUtils.isEmpty(information!!.customer_fields)) "" else information!!.customer_fields)
            sobot_et_customer_params!!.setText(if (TextUtils.isEmpty(information!!.params)) "" else information!!.params)
            if (information!!.autoSendMsgMode != null) {
                sobot_et_autoSendMsgMode!!.setText(information!!.autoSendMsgMode.value.toString() + "")
                sobot_et_autoSendMsgcontent!!.setText(if (TextUtils.isEmpty(information!!.autoSendMsgMode.content)) "" else information!!.autoSendMsgMode.content + "")
                sobot_et_autoSendMsgtype!!.setText(information!!.autoSendMsgMode.auto_send_msgtype.toString() + "")
                sobot_et_autoSendMsg_count!!.setText(if (information!!.autoSendMsgMode.geIsEveryTimeAutoSend()) "0" else "1")
            }
            sobot_et_queue_First!!.setText(if (information!!.is_queue_first) "1" else "0")
            sobot_et_summary_params!!.setText(if (TextUtils.isEmpty(information!!.summary_params)) "" else information!!.summary_params)
            sobot_et_multi_params!!.setText(if (TextUtils.isEmpty(information!!.multi_params)) "" else information!!.multi_params)
            if (!TextUtils.isEmpty(information!!.isVip)) {
                setImageShowStatus(if ("1" == information!!.isVip) true else false, sobotImage4211)
            }
            sobot_et_vip_level!!.setText(if (TextUtils.isEmpty(information!!.vip_level)) "" else information!!.vip_level)
            sobot_et_user_label!!.setText(if (TextUtils.isEmpty(information!!.user_label)) "" else information!!.user_label)
            status42131 = information!!.isHideMenuSatisfaction
            setImageShowStatus(status42131, sobotImage42131)
            status42132 = information!!.isHideMenuLeave
            setImageShowStatus(status42132, sobotImage42132)
            status42133 = information!!.isHideMenuPicture
            setImageShowStatus(status42133, sobotImage42133)
            status42134 = information!!.isHideMenuVedio
            setImageShowStatus(status42134, sobotImage42134)
            status42135 = information!!.isHideMenuCamera
            setImageShowStatus(status42135, sobotImage42135)
            status42136 = information!!.isHideMenuFile
            setImageShowStatus(status42136, sobotImage42136)
            status42137 = information!!.isHideMenuManualLeave
            setImageShowStatus(status42137, sobotImage42137)
        }
        if (otherModel != null) {
            status428 = otherModel!!.isUserConsultingContentDemo
            setImageShowStatus(status428, sobotImage428)
            status429 = otherModel!!.isUserOrderCardContentModelDemo
            setImageShowStatus(status429, sobotImage429)
        }
        tv_manual_fun_4_2_1 = findViewById(R.id.tv_manual_fun_4_2_1)
        tv_manual_fun_4_2_1!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-1-对接指定技能组")
        setOnClick(
            tv_manual_fun_4_2_1,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-1-%E5%AF%B9%E6%8E%A5%E6%8C%87%E5%AE%9A%E6%8A%80%E8%83%BD%E7%BB%84"
        )
        tv_manual_fun_4_2_2 = findViewById(R.id.tv_manual_fun_4_2_2)
        tv_manual_fun_4_2_2!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-2-对接指定客服")
        setOnClick(
            tv_manual_fun_4_2_2,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-2-%E5%AF%B9%E6%8E%A5%E6%8C%87%E5%AE%9A%E5%AE%A2%E6%9C%8D"
        )
        tv_manual_fun_4_2_8 = findViewById(R.id.tv_manual_fun_4_2_8)
        tv_manual_fun_4_2_8!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-8-商品的咨询信息并支持直接发送消息卡片，仅人工模式下支持")
        setOnClick(
            tv_manual_fun_4_2_8,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-8-%E5%95%86%E5%93%81%E7%9A%84%E5%92%A8%E8%AF%A2%E4%BF%A1%E6%81%AF%E5%B9%B6%E6%94%AF%E6%8C%81%E7%9B%B4%E6%8E%A5%E5%8F%91%E9%80%81%E6%B6%88%E6%81%AF%E5%8D%A1%E7%89%87%EF%BC%8C%E4%BB%85%E4%BA%BA%E5%B7%A5%E6%A8%A1%E5%BC%8F%E4%B8%8B%E6%94%AF%E6%8C%81"
        )
        tv_manual_fun_4_2_9 = findViewById(R.id.tv_manual_fun_4_2_9)
        tv_manual_fun_4_2_9!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-9-发送订单卡片，仅人工模式下支持,订单卡片点击事件可拦截")
        setOnClick(
            tv_manual_fun_4_2_9,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-9-%E5%8F%91%E9%80%81%E8%AE%A2%E5%8D%95%E5%8D%A1%E7%89%87%EF%BC%8C%E4%BB%85%E4%BA%BA%E5%B7%A5%E6%A8%A1%E5%BC%8F%E4%B8%8B%E6%94%AF%E6%8C%81-%E8%AE%A2%E5%8D%95%E5%8D%A1%E7%89%87%E7%82%B9%E5%87%BB%E4%BA%8B%E4%BB%B6%E5%8F%AF%E6%8B%A6%E6%88%AA"
        )
        tv_manual_fun_4_2_13 = findViewById(R.id.tv_manual_fun_4_2_13)
        tv_manual_fun_4_2_13!!.setText("https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-13-转人工后可隐藏“+”号菜单栏中的按钮")
        setOnClick(
            tv_manual_fun_4_2_13,
            "https://www.sobot.com/developerdocs/app_sdk/android.html#_4-2-13-%E8%BD%AC%E4%BA%BA%E5%B7%A5%E5%90%8E%E5%8F%AF%E9%9A%90%E8%97%8F%E2%80%9C-%E2%80%9D%E5%8F%B7%E8%8F%9C%E5%8D%95%E6%A0%8F%E4%B8%AD%E7%9A%84%E6%8C%89%E9%92%AE"
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
                    val groupid = sobot_et_groupid!!.text.toString().trim { it <= ' ' }
                    information!!.groupid = groupid
                    val choose_adminid =
                        sobot_et_choose_adminid!!.text.toString().trim { it <= ' ' }
                    information!!.choose_adminid = choose_adminid
                    val tranReceptionistFlag =
                        sobot_et_tranReceptionistFlag!!.text.toString().trim { it <= ' ' }
                    information!!.tranReceptionistFlag = tranReceptionistFlag.toInt()
                    val customer_fields =
                        sobot_et_customer_fields!!.text.toString().trim { it <= ' ' }
                    information!!.customer_fields = customer_fields
                    val customer_params =
                        sobot_et_customer_params!!.text.toString().trim { it <= ' ' }
                    information!!.params = customer_params
                    val autoSendMsgMode =
                        sobot_et_autoSendMsgMode!!.text.toString().trim { it <= ' ' }
                    val autoSendMsgcontent =
                        sobot_et_autoSendMsgcontent!!.text.toString().trim { it <= ' ' }
                    val autoSendMsgtype =
                        sobot_et_autoSendMsgtype!!.text.toString().trim { it <= ' ' }
                    val autoSendMsgCount =
                        sobot_et_autoSendMsg_count!!.text.toString().trim { it <= ' ' }
                    if (!TextUtils.isEmpty(autoSendMsgMode) && !TextUtils.isEmpty(autoSendMsgcontent) && "0" != autoSendMsgMode) {
                        var sobotAutoSendMsgMode: SobotAutoSendMsgMode? = null
                        if ("1" == autoSendMsgMode) {
                            sobotAutoSendMsgMode = SobotAutoSendMsgMode.SendToRobot
                        } else if ("2" == autoSendMsgMode) {
                            sobotAutoSendMsgMode = SobotAutoSendMsgMode.SendToOperator
                        } else if ("3" == autoSendMsgMode) {
                            sobotAutoSendMsgMode = SobotAutoSendMsgMode.SendToAll
                        }
                        if (sobotAutoSendMsgMode != null) {
                            sobotAutoSendMsgMode.setIsEveryTimeAutoSend("0" == autoSendMsgCount)
                            if (TextUtils.isEmpty(autoSendMsgtype)) {
                                information!!.autoSendMsgMode =
                                    sobotAutoSendMsgMode.setContent(autoSendMsgcontent)
                            } else {
                                if ("1" == autoSendMsgtype || "12" == autoSendMsgtype || "23" == autoSendMsgtype) {
                                    information!!.autoSendMsgMode =
                                        sobotAutoSendMsgMode.setContent(
                                            CommonUtils.getSDCardRootPath(
                                                this@SobotManualFunctionActivity
                                            ) + File.separator + autoSendMsgcontent
                                        )
                                            .setAuto_send_msgtype(autoSendMsgtype.toInt())
                                } else {
                                    information!!.autoSendMsgMode =
                                        sobotAutoSendMsgMode.setContent(autoSendMsgcontent)
                                            .setAuto_send_msgtype(0)
                                }
                            }
                        }
                    } else {
                        information!!.autoSendMsgMode =
                            SobotAutoSendMsgMode.Default.setContent(autoSendMsgcontent)
                                .setAuto_send_msgtype(0)
                                .setIsEveryTimeAutoSend("0" == autoSendMsgCount)
                    }
                    val queue_First = sobot_et_queue_First!!.text.toString().trim { it <= ' ' }
                    information!!.setIs_Queue_First(if ("1" == queue_First) true else false)
                    val summary_params =
                        sobot_et_summary_params!!.text.toString().trim { it <= ' ' }
                    information!!.summary_params = summary_params
                    val multi_params = sobot_et_multi_params!!.text.toString().trim { it <= ' ' }
                    information!!.multi_params = multi_params
                    if (status428) {
                        //咨询内容
                        val consultingContent = ConsultingContent()
                        //咨询内容标题，必填
                        consultingContent.sobotGoodsTitle = "XXX超级电视50英寸2D智能LED黑色"
                        //咨询内容图片，选填 但必须是图片地址
                        consultingContent.sobotGoodsImgUrl = "http://www.li7.jpg"
                        //咨询来源页，必填
                        consultingContent.sobotGoodsFromUrl = "www.sobot.com"
                        //描述，选填
                        consultingContent.sobotGoodsDescribe = "XXX超级电视 S5"
                        //标签，选填
                        consultingContent.sobotGoodsLable = "￥2150"
                        //转人工后是否自动发送
                        consultingContent.isAutoSend = true
                        //启动智齿客服页面 在Information 添加,转人工发送卡片消息
                        information!!.consultingContent = consultingContent
                    } else {
                        information!!.consultingContent = null
                    }
                    if (status429) {
                        val goodsList: MutableList<Goods> = ArrayList()
                        goodsList.add(
                            Goods(
                                "苹果",
                                "https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png"
                            )
                        )
                        goodsList.add(
                            Goods(
                                "苹果1111111",
                                "https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png"
                            )
                        )
                        goodsList.add(
                            Goods(
                                "苹果2222",
                                "https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png"
                            )
                        )
                        goodsList.add(
                            Goods(
                                "苹果33333333",
                                "https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png"
                            )
                        )
                        val orderCardContent = OrderCardContentModel()
                        //订单编号（必填）
                        orderCardContent.orderCode = "zc32525235425"
                        //订单状态
                        //待付款:1 待发货:2 运输中:3  派送中:4  已完成:5  待评价:6 已取消:7
                        orderCardContent.orderStatus = 1
                        //订单总金额(单位 分)
                        orderCardContent.totalFee = 1234
                        //订单商品总数
                        orderCardContent.goodsCount = "4"
                        //订单链接
                        orderCardContent.orderUrl = "https://item.jd.com/1765513297.html"
                        //订单创建时间
                        orderCardContent.createTime = System.currentTimeMillis().toString() + ""
                        //转人工后是否自动发送
                        orderCardContent.isAutoSend = true
                        //订单商品集合
                        orderCardContent.goods = goodsList
                        //订单卡片内容
                        information!!.orderGoodsInfo = orderCardContent
                    } else {
                        information!!.orderGoodsInfo = null
                    }
                    information!!.isVip = if (status4211) "1" else "0"
                    val vip_level = sobot_et_vip_level!!.text.toString().trim { it <= ' ' }
                    information!!.vip_level = vip_level
                    val user_label = sobot_et_user_label!!.text.toString().trim { it <= ' ' }
                    information!!.user_label = user_label
                    information!!.isHideMenuSatisfaction = status42131
                    information!!.isHideMenuLeave = status42132
                    information!!.isHideMenuPicture = status42133
                    information!!.isHideMenuVedio = status42134
                    information!!.isHideMenuCamera = status42135
                    information!!.isHideMenuFile = status42136
                    information!!.isHideMenuManualLeave = status42137
                    saveObject(this, "sobot_demo_infomation", information!!)
                }
                ToastUtil.showToast(context, "已保存")
                finish()
            }
            R.id.sobot_rl_4_2_8 -> {
                status428 = !status428
                setImageShowStatus(status428, sobotImage428)
                if (otherModel != null) {
                    otherModel!!.isUserConsultingContentDemo = status428
                    saveObject(this, "sobot_demo_otherModel", otherModel!!)
                }
            }
            R.id.sobot_rl_4_2_9 -> {
                status429 = !status429
                setImageShowStatus(status429, sobotImage429)
                if (otherModel != null) {
                    otherModel!!.isUserOrderCardContentModelDemo = status429
                    saveObject(this, "sobot_demo_otherModel", otherModel!!)
                }
            }
            R.id.sobot_rl_4_2_11 -> {
                status4211 = !status4211
                setImageShowStatus(status4211, sobotImage4211)
            }
            R.id.sobot_rl_4_2_13_1 -> {
                status42131 = !status42131
                setImageShowStatus(status42131, sobotImage42131)
            }
            R.id.sobot_rl_4_2_13_2 -> {
                status42132 = !status42132
                setImageShowStatus(status42132, sobotImage42132)
            }
            R.id.sobot_rl_4_2_13_3 -> {
                status42133 = !status42133
                setImageShowStatus(status42133, sobotImage42133)
            }
            R.id.sobot_rl_4_2_13_4 -> {
                status42134 = !status42134
                setImageShowStatus(status42134, sobotImage42134)
            }
            R.id.sobot_rl_4_2_13_5 -> {
                status42135 = !status42135
                setImageShowStatus(status42135, sobotImage42135)
            }
            R.id.sobot_rl_4_2_13_6 -> {
                status42136 = !status42136
                setImageShowStatus(status42136, sobotImage42136)
            }
            R.id.sobot_rl_4_2_13_7 -> {
                status42137 = !status42137
                setImageShowStatus(status42137, sobotImage42137)
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