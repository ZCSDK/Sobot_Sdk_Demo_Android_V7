package com.sobot.chat.listener;

import com.sobot.chat.api.model.SobotLeaveReplyModel;

import java.util.List;

/**
 * 获取未读留言回复列表的监听
 */

public interface SobotNoReadLeaveReplyListener {

    //请求成功
    void onNoReadLeaveReplyListener(List<SobotLeaveReplyModel> sobotLeaveReplyModelList);

    //请求失败（网络原因导致的）
    void onFailureListener(String erroMsg);
}