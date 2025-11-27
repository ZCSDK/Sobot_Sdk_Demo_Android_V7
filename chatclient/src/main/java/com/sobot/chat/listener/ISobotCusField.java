package com.sobot.chat.listener;

import android.widget.TextView;

import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;

/**
 * 打开自定义字段选择的接口
 * @author Created by jinxl on 2018/1/3.
 */
public interface ISobotCusField {
    /**
     * 点击回调
     * @param view  点击的View
     * @param fieldConfig 包含fieldType和fieldName  自定义字段的类型
     * @param cusField 点击这个字段的bean
     */
    void onClickCusField(TextView view , SobotCusFieldConfig fieldConfig, SobotFieldModel cusField);

    /**
     * 手机区号
     */
    void inputLeftOnclick();
    /**
     * 时区
     * @param fieldConfig
     */
    void selectLeftOnclick(TextView view , SobotCusFieldConfig fieldConfig);

    /**
     * 时区+时间
     * @param fieldConfig
     */
    void selectRightOnclick(TextView view , SobotCusFieldConfig fieldConfig);
}
