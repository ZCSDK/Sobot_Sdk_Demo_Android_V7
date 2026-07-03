package com.sobot.chat.utils;

import android.content.Context;

import com.sobot.chat.listener.NewHyperlinkListener;
import com.sobot.chat.listener.SobotChatStatusListener;
import com.sobot.chat.listener.SobotConversationListCallback;
import com.sobot.chat.listener.SobotFunctionClickListener;
import com.sobot.chat.listener.SobotHelpPageOpenChatListener;
import com.sobot.chat.listener.SobotImagePreviewListener;
import com.sobot.chat.listener.SobotLeaveMsgListener;
import com.sobot.chat.listener.SobotMapCardListener;
import com.sobot.chat.listener.SobotMiniProgramClickListener;
import com.sobot.chat.listener.SobotOrderCardListener;
import com.sobot.chat.listener.SobotTransferOperatorInterceptor;
import com.sobot.chat.listener.SobotViewListener;

public class SobotOption {
    public static NewHyperlinkListener newHyperlinkListener;//超链接的监听(动态分开拦截（帮助中心、留言、聊天、留言记录、商品卡片，订单卡片）点击事件)
    public static SobotViewListener sobotViewListener;//页面的监听
    public static SobotLeaveMsgListener sobotLeaveMsgListener;//留言按钮监听
    public static SobotConversationListCallback sobotConversationListCallback;//消息中心点击会话的回调
    public static SobotTransferOperatorInterceptor transferOperatorInterceptor;//转人工拦截器
    public static SobotOrderCardListener orderCardListener;//订单卡片的监听
    public static SobotChatStatusListener sobotChatStatusListener;//聊天状态变化的监听
    public static SobotMapCardListener mapCardListener;//订单卡片的监听
    public static SobotFunctionClickListener functionClickListener;//智齿功能点击的监听，可用于客户埋点
    public static SobotImagePreviewListener imagePreviewListener;//点击图片预览拦截器，客户可自己处理
    public static SobotMiniProgramClickListener miniProgramClickListener;//小程序点击的监听，客户可自己处理
    public static SobotHelpPageOpenChatListener openChatListener;//帮助中心打开在线帮助是否拦截监听

    /**
     * 分发URL点击事件，优先使用旧版监听器，再使用新版监听器。
     *
     * @return true 表示已拦截，false 表示未拦截
     */
    public static boolean dispatchUrlClick(Context context, String url) {
        if (newHyperlinkListener != null) {
            return newHyperlinkListener.onUrlClick(context, url);
        }
        return false;
    }

    /**
     * 分发电话点击事件，优先使用旧版监听器，再使用新版监听器。
     *
     * @return true 表示已拦截，false 表示未拦截
     */
    public static boolean dispatchPhoneClick(Context context, String phone) {
        if (newHyperlinkListener != null) {
            return newHyperlinkListener.onPhoneClick(context, phone);
        }
        return false;
    }

    /**
     * 分发邮箱点击事件，优先使用旧版监听器，再使用新版监听器。
     *
     * @return true 表示已拦截，false 表示未拦截
     */
    public static boolean dispatchEmailClick(Context context, String email) {
        if (newHyperlinkListener != null) {
            return newHyperlinkListener.onEmailClick(context, email);
        }
        return false;
    }
}
