package com.sobot.chat.conversation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.KeyEvent;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotBaseActivity;
import com.sobot.chat.utils.ZhiChiConstant;

public class SobotChatActivity extends SobotBaseActivity {

    Bundle informationBundle;
    SobotChatFragment chatFragment;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_chat_act;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            informationBundle = getIntent().getBundleExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        } else {
            informationBundle = savedInstanceState.getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //销毁前缓存数据
        outState.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, informationBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initView() {
        chatFragment = (SobotChatFragment) getSupportFragmentManager()
                .findFragmentById(R.id.sobot_contentFrame);
        if (chatFragment == null) {
            chatFragment = SobotChatFragment.newInstance(informationBundle);

            addFragmentToActivity(getSupportFragmentManager(),
                    chatFragment, R.id.sobot_contentFrame);
        }
    }

    public static void addFragmentToActivity(FragmentManager fragmentManager,
                                             Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commit();
    }

    @Override
    protected void initData() {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(chatFragment.dispatchkeyEvent(event)){
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (chatFragment != null) {
            chatFragment.onLeftMenuClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}