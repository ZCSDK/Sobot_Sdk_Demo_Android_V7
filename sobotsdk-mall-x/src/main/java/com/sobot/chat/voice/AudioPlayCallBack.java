package com.sobot.chat.voice;

import android.widget.ImageView;

import com.sobot.chat.api.model.ZhiChiMessageBase;

/**
 * Created by jinxl on 2017/3/11.
 */
public interface AudioPlayCallBack {

    void onPlayStart(ZhiChiMessageBase mCurrentMsg);

    void onPlayEnd(ZhiChiMessageBase mCurrentMsg);
}