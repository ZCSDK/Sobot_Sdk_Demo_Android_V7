package com.sobot.sobotchatsdkdemo.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sobot.chat.ZCSobotApi
import com.sobot.chat.api.apiUtils.SobotBaseUrl
import com.sobot.chat.api.model.Information
import com.sobot.chat.utils.SharedPreferencesUtil
import com.sobot.chat.utils.ToastUtil
import com.sobot.chat.utils.ZhiChiConstant
import com.sobot.gson.SobotGsonUtil
import com.sobot.sobotchatsdkdemo.R
import com.sobot.sobotchatsdkdemo.model.SobotDemoOtherModel
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil
import com.sobot.sobotchatsdkdemo.util.SobotSPUtil.getObject

class SobotAutomationFragment : Fragment(), View.OnClickListener {
    private var view: View? = null
    private var sobot_tv_save: TextView? = null
    private var sobot_et_yuming: EditText? = null
    private var et_sobot_json: EditText? = null
    private var information: Information? = null
    private var otherModel: SobotDemoOtherModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = View.inflate(activity, R.layout.sobot_demo_automation_fragment, null)
        findViewsById()
        return view
    }

    private fun findViewsById() {
        information = context?.let { getObject(it, "sobot_demo_infomation") } as Information?
        otherModel = context?.let { getObject(it, "sobot_demo_otherModel") } as SobotDemoOtherModel?
        sobot_et_yuming = view!!.findViewById<View>(R.id.sobot_et_yuming) as EditText
        et_sobot_json = view!!.findViewById<View>(R.id.et_sobot_json) as EditText
        sobot_tv_save = view!!.findViewById<View>(R.id.sobot_tv_save) as TextView
        sobot_tv_save!!.setVisibility(View.VISIBLE)
        sobot_tv_save!!.setOnClickListener(this)
        if (information != null) {

        }
        if (otherModel != null) {
            if (!TextUtils.isEmpty(otherModel!!.api_host)) {
                sobot_et_yuming!!.setText(otherModel!!.api_host)
            }
            if (!TextUtils.isEmpty(otherModel!!.infoJson)) {
                et_sobot_json!!.setText(otherModel!!.infoJson)
            } else {
                et_sobot_json!!.setText(
                    "{\"app_key\":\"d631507699104ce8aceb63a6ac8855b7\",\"appkey\":\"d631507699104ce8aceb63a6ac8855b7\",\"artificialIntelligenceNum\":1,\"autoSendMsgMode\":\"Default\",\"canBackWithNotEvaluation\":false,\"consultingContent\":{\"isAutoSend\":true,\"isEveryTimeAutoSend\":false,\"sobotGoodsDescribe\":\"乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）\",\"sobotGoodsFromUrl\":\"https://www.baidu.com/admin/order/detail/302\",\"sobotGoodsImgUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\",\"sobotGoodsLable\":\"￥2150\",\"sobotGoodsTitle\":\"乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）\"},\"content\":{\"isAutoSend\":true,\"isEveryTimeAutoSend\":false,\"sobotGoodsDescribe\":\"乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）\",\"sobotGoodsFromUrl\":\"https://www.baidu.com/admin/order/detail/302\",\"sobotGoodsImgUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\",\"sobotGoodsLable\":\"￥2150\",\"sobotGoodsTitle\":\"乐视超级电视 S50 Air 全配版50英寸2D智能LED黑色（Letv S50 Air）\"},\"createTime\":\"1706181835175\",\"customCard\":{\"cardMenus\":[{\"isDisable\":false,\"isUnEnabled\":false,\"menuId\":0,\"menuLink\":\"胜多负少士大夫胜多负少士大夫胜多负少士大夫胜多负少士大夫\",\"menuLinkType\":0,\"menuName\":\"发送\",\"menuTip\":\"发送提示\",\"menuType\":2},{\"isDisable\":false,\"isUnEnabled\":false,\"menuId\":0,\"menuLink\":\"胜多负少士大夫\",\"menuLinkType\":2,\"menuName\":\"确认\",\"menuTip\":\"发送提示\",\"menuType\":1}],\"cardStyle\":0,\"cardType\":0,\"customCards\":[{\"customCardAmount\":\"222.9\",\"customCardAmountSymbol\":\"￥\",\"customCardCode\":\"sobot121u321u3\",\"customCardCount\":\"5\",\"customCardDesc\":\"测试邮箱测试邮箱手动阀手动阀测试邮箱我是的就就的的的是是我我我的的的嗡嗡嗡嗡嗡嗡嗡嗡嗡问问二位问问额嗡嗡嗡嗡嗡嗡为微软微软微软微软微软微软\",\"customCardId\":\"cardId_44444\",\"customCardName\":\"测试邮箱我是的就就的的的是是我我我的的的嗡嗡嗡嗡嗡嗡嗡嗡嗡问问二位问问额嗡嗡嗡嗡嗡嗡为微软微软微软微软微软微软\",\"customCardStatus\":\"待收货\",\"customCardThumbnail\":\"https://hk.sobot.com/auth/_next/static/media/sideZh.74024132.png\",\"customCardTime\":\"2023-06-25 14:32:21\",\"customMenus\":[{\"isDisable\":false,\"isUnEnabled\":false,\"menuId\":0,\"menuLink\":\"胜多负少士大夫胜多负少士大夫胜多负少士大夫胜多负少士大夫\",\"menuLinkType\":0,\"menuName\":\"发送\",\"menuTip\":\"发送提示\",\"menuType\":2},{\"isDisable\":false,\"isUnEnabled\":false,\"menuId\":0,\"menuLink\":\"胜多负少士大夫\",\"menuLinkType\":2,\"menuName\":\"确认\",\"menuTip\":\"发送提示\",\"menuType\":1}]}],\"isHistory\":false,\"isOpen\":false,\"showCustomCardAllMode\":true},\"customInfo\":\"\",\"custom_title_url\":0,\"customerFields\":\"\",\"customer_fields\":\"\",\"email\":\"122675799@qq.com\",\"face\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/logo/72ffea49d5564a30b061f90b9f7968f2.jpg\",\"faqId\":0,\"group_name\":\"\",\"groupid\":\"\",\"helpCenterTel\":\"18510518890\",\"helpCenterTelTitle\":\"联系电话\",\"hideManualEvaluationLabels\":false,\"hideMenuCamera\":true,\"hideMenuFile\":true,\"hideMenuLeave\":true,\"hideMenuManualLeave\":false,\"hideMenuPicture\":true,\"hideMenuSatisfaction\":true,\"hideMenuVedio\":true,\"hideRototEvaluationLabels\":false,\"initModeType\":-1,\"isArtificialIntelligence\":false,\"isCloseInquiryForm\":false,\"isFirstEntry\":1,\"isQueueFirst\":true,\"isSetCloseShowSatisfaction\":1,\"isSetShowSatisfaction\":1,\"isShow\":false,\"isShowCloseSatisfaction\":false,\"isShowEveryLeftMsgFaceNickName\":false,\"isShowLeftBackPop\":false,\"isShowRightMsgFace\":false,\"isShowRightMsgNickName\":false,\"isShowSatisfaction\":false,\"isUseRobotVoice\":false,\"isUseVoice\":true,\"leaveCusFieldMap\":{\"6cea963acefb43c591a2916f77848374\":\"zzz\"},\"leaveParamsExtends\":[{\"id\":\"d93847a05710483893fd2d05e16a2b82\",\"params\":\"msgid\",\"value\":\"数据1\"}],\"mulitParams\":\"\",\"multi_params\":\"\",\"orderGoodsInfo\":{\"createTime\":\"1706181835158\",\"goods\":[{\"name\":\"苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果苹果\",\"pictureUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\"},{\"name\":\"苹果1111111\",\"pictureUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\"},{\"name\":\"苹果2222\",\"pictureUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\"},{\"name\":\"苹果33333333\",\"pictureUrl\":\"https://img.sobot.com/chatres/66a522ea3ef944a98af45bac09220861/msg/20190930/7d938872592345caa77eb261b4581509.png\"}],\"goodsCount\":\"4\",\"isAutoSend\":true,\"isEveryTimeAutoSend\":false,\"orderCode\":\"zc32525235425\",\"orderStatus\":0,\"orderUrl\":\"https://item.jd.com/1765513297.html\",\"statusCustom\":\"待检验\",\"totalFee\":1234},\"params\":\"\",\"partnerid\":\"1q1q2w16\",\"qq\":\"\",\"queue_first\":true,\"realname\":\"\",\"remark\":\"这个人很勤奋，留下很多知识\",\"robotCode\":\"\",\"robot_code\":\"\",\"secretKey\":\"sobot*\\u0026^%\$#@!\",\"service_mode\":-1,\"showLeaveDetailBackEvaluate\":true,\"sign\":\"5012d1057551e9bf4d65deb1b8feb048\",\"skillSetId\":\"\",\"skillSetName\":\"\",\"summaryParams\":\"\",\"summary_params\":\"\",\"tel\":\"13613440946\",\"titleImgId\":0,\"tranReceptionistFlag\":0,\"uid\":\"1q1q2w16\",\"uname\":\"用户昵称\",\"user_emails\":\"122675799@qq.com\",\"user_name\":\"\",\"user_nick\":\"用户昵称\",\"user_tels\":\"13613440946\",\"visitUrl\":\"\",\"visit_url\":\"\"}" )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (otherModel != null) {
            if (!TextUtils.isEmpty(otherModel!!.api_host)) {
                sobot_et_yuming!!.setText(otherModel!!.api_host)
            }
        }
    }

    override fun onClick(v: View) {
        if (v === sobot_tv_save && context != null) {
            val infoJson = et_sobot_json!!.text.toString().trim { it <= ' ' }
            if (!TextUtils.isEmpty(infoJson)) {
                information = SobotGsonUtil.jsonToBean(infoJson, Information::class.java)
                SobotSPUtil.saveObject(context!!, "sobot_demo_infomation", information!!)
            }
            val yuming = sobot_et_yuming!!.text.toString().trim { it <= ' ' }
            if (otherModel != null) {
                if (!TextUtils.isEmpty(yuming)) {
                    otherModel!!.api_host = yuming
                } else {
                    otherModel!!.api_host = "https://www.soboten.com"
                }
                SobotSPUtil.saveObject(context!!, "sobot_demo_otherModel", otherModel!!)
                SobotBaseUrl.setApi_Host(otherModel!!.api_host)
            }
            ToastUtil.showToast(context, "已保存")
            SharedPreferencesUtil.saveBooleanData(
                context,
                ZhiChiConstant.SOBOT_CONFIG_INITSDK,
                false
            )
            ZCSobotApi.initSobotSDK(context, information!!.app_key, information!!.partnerid)
        }
    }
}