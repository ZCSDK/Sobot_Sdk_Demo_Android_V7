package com.sobot.chat.listener;

import android.widget.TextView;

import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;

/**
 * 上传字段事件
 * @author Created by jinxl on 2018/1/3.
 */
public interface SobotCusFieldUploadListener {
    /**
     * 点击删除
     * @param view  点击的View
     * @param fieldConfig 包含fieldType和fieldName  自定义字段的类型
     * @param cusField 点击这个字段的bean
     */
    void onClickDelete(TextView view , SobotCusFieldConfig fieldConfig, SobotFieldModel cusField);

    /**
     * 预览
     * @param fieldConfig
     */
    void onClickPreview(TextView view , SobotCusFieldConfig fieldConfig);

    /**
     * 上传
     * @param fieldConfig
     */
    void onClickUpload(TextView view , SobotCusFieldConfig fieldConfig);
}
