package com.sobot.sobotchatsdkdemo.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sobot.chat.utils.LogUtils
import com.sobot.chat.utils.ZhiChiConstant

/**
 * 获取未读消息的广播接收者
 */
class SobotUnReadMsgReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ZhiChiConstant.sobot_unreadCountBrocast == intent.action) {
            val noReadNum = intent.getIntExtra("noReadCount", 0)
            val content = intent.getStringExtra("content")
            LogUtils.i("未读消息数是：$noReadNum\t最新一条消息内容是：$content")
        }
    }
}