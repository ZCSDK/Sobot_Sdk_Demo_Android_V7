package com.sobot.sobotchatsdkdemo.model

import java.io.Serializable

class SobotDemoOtherModel : Serializable {
    // 域名
    var api_host: String? = null

    // 平台标识 请联系对应的客服申请
    var platformUnionCode: String? = null

    // 平台秘钥 请联系对应的客服申请
    var platformSecretkey: String? = null

    //是否使用商品卡片demo
    var isUserConsultingContentDemo = false

    //是否使用订单卡片demo
    var isUserOrderCardContentModelDemo = false
}