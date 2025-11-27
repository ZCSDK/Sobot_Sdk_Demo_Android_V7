package com.sobot.chat.activity.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.sobot.chat.api.model.Information;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.io.Serializable;
import java.util.Locale;

/**
 * 帮助中心基类
 */
public abstract class SobotBaseHelpCenterActivity extends SobotChatBaseActivity {
    protected Bundle mInformationBundle;
    protected Information mInfo;

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                mInformationBundle = getIntent().getBundleExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            } else {
                mInformationBundle = savedInstanceState.getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            }
            if (mInformationBundle != null) {
                Serializable sobot_info = mInformationBundle.getSerializable(ZhiChiConstant.SOBOT_BUNDLE_INFO);
                if (sobot_info instanceof Information) {
                    mInfo = (Information) sobot_info;
                    boolean isUseLanguage = SharedPreferencesUtil.getBooleanData(getSobotBaseActivity(), ZhiChiConstant.SOBOT_USE_LANGUAGE, false);
                    if (isUseLanguage) {
                        //客户指定语言了
                        String settingLanguage = SharedPreferencesUtil.getStringData(getSobotBaseActivity(), ZhiChiConstant.SOBOT_USER_SETTTINNG_LANGUAGE, "");
                        if (StringUtils.isNoEmpty(settingLanguage)) {
                            mInfo.setLocale(settingLanguage);
                        }
                    }
                    //手机系统语言
                    String sysLanguae = ChatUtils.getCurrentLanguage();
                    if (StringUtils.isNoEmpty(sysLanguae)) {
                        mInfo.setSystemLanguage(sysLanguae);
                    }
                    SharedPreferencesUtil.saveObject(getSobotBaseActivity(),
                            ZhiChiConstant.sobot_last_current_info, mInfo);
                }
            }
        } catch (Exception e) {
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //销毁前缓存数据
        outState.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, mInformationBundle);
        super.onSaveInstanceState(outState);
    }
}