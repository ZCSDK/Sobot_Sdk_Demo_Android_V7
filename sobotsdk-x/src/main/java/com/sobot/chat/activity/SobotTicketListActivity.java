package com.sobot.chat.activity;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.fragment.SobotTicketInfoFragment;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.List;

/**
 * 留言列表
 */
public class SobotTicketListActivity extends SobotChatBaseActivity implements View.OnClickListener {

    private SobotLeaveMsgConfig mConfig;
    private String mUid = "";
    private String mGroupId = "";
    private String mCustomerId = "";
    private String mCompanyId = "";

    private SobotTicketInfoFragment mFragment;

    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_list;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_UID);
            mConfig = (SobotLeaveMsgConfig) getIntent().getSerializableExtra(StPostMsgPresenter.INTENT_KEY_CONFIG);
            mGroupId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_GROUPID);
            mCustomerId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID);
            mCompanyId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID);
//            flag_exit_type = getIntent().getIntExtra(ZhiChiConstant.FLAG_EXIT_TYPE, -1);
//            flag_exit_sdk = getIntent().getBooleanExtra(ZhiChiConstant.FLAG_EXIT_SDK, false);
        }
    }

    @Override
    protected void initView() {
        setTitle(R.string.sobot_message_record);
        if (ChatUtils.getStatusList() == null || ChatUtils.getStatusList().size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(this,
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(this, companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                }

                @Override
                public void onFailure(Exception e, String s) {
                }
            });
        }
    }

    @Override
    protected void initData() {
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotTicketListActivity.this,
                ZhiChiConstant.sobot_last_current_initModel);
        if (initMode != null && ChatUtils.isFreeAccount(initMode.getAccountStatus())) {
            sobotFreeAccountTipDialog = new SobotFreeAccountTipDialog(SobotTicketListActivity.this, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sobotFreeAccountTipDialog.dismiss();
                    finish();
                }
            });
            if (sobotFreeAccountTipDialog != null && !sobotFreeAccountTipDialog.isShowing()) {
                sobotFreeAccountTipDialog.show();
            }
        }

        Bundle bundle2 = new Bundle();
        bundle2.putString(StPostMsgPresenter.INTENT_KEY_UID, mUid);
        bundle2.putString(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
        bundle2.putString(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
        mFragment =SobotTicketInfoFragment.newInstance(bundle2);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.sobot_contentFrame,mFragment);
        transaction.commit();
    }



    @Override
    protected void onDestroy() {
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_CloseLeave);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

    }
}