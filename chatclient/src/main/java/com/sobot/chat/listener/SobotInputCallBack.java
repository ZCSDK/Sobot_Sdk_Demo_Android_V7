package com.sobot.chat.listener;

import android.view.View;

import com.sobot.chat.api.model.SobotCusFieldConfig;

/**
 * 打开自定义字段选择的接口
 * @author Created by jinxl on 2018/1/3.
 */
public interface SobotInputCallBack {
    /**
     * 点击回调
     * @param view  点击的View
     * @param fieldConfig 包含fieldType和fieldName  自定义字段的类型
     */
    void onClickCusField(View view , SobotCusFieldConfig fieldConfig);
}
