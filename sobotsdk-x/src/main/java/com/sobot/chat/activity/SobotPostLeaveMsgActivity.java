package com.sobot.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.api.model.BaseCode;
import com.sobot.chat.api.model.SobotOfflineLeaveMsgModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.network.http.callback.StringResultCallBack;

/**
 * 留言转离线消息
 */
public class SobotPostLeaveMsgActivity extends SobotChatBaseActivity implements View.OnClickListener {

    private static final String EXTRA_MSG_UID = "EXTRA_MSG_UID";
    private static final String EXTRA_MSG_LEAVE_TXT = "EXTRA_MSG_LEAVE_TXT";
    private static final String EXTRA_MSG_LEAVE_CONTENT_TXT = "EXTRA_MSG_LEAVE_CONTENT_TXT";
    private static final String EXTRA_MSG_LEAVE_CONTENT = "EXTRA_MSG_LEAVE_CONTENT";
    public static final int EXTRA_MSG_LEAVE_REQUEST_CODE = 109;

    private String mUid;
    private TextView sobot_tv_post_msg;
    private EditText sobot_post_et_content;
    private TextView sobot_tv_problem_description;
    private TextView sobot_btn_submit;
    private String skillGroupId = "";
    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;
    private TextView sobot_tv_leaveExplain;

    public static Intent newIntent(Context context, String msgLeaveTxt, String msgLeaveContentTxt, String uid) {
        Intent intent = new Intent(context, SobotPostLeaveMsgActivity.class);
        intent.putExtra(EXTRA_MSG_LEAVE_TXT, msgLeaveTxt);
        intent.putExtra(EXTRA_MSG_LEAVE_CONTENT_TXT, msgLeaveContentTxt);
        intent.putExtra(EXTRA_MSG_UID, uid);
        return intent;
    }

    public static String getResultContent(Intent intent) {
        if (intent != null) {
            return intent.getStringExtra(EXTRA_MSG_LEAVE_CONTENT);
        }
        return null;
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_post_leave_msg;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(EXTRA_MSG_UID);
        }
    }

    @Override
    protected void initView() {
        showLeftMenu(  true);
        setTitle(R.string.sobot_leavemsg_title);
        sobot_tv_post_msg = (TextView) findViewById(R.id.sobot_tv_post_msg);
        sobot_post_et_content = (EditText) findViewById(R.id.sobot_post_et_content);
        sobot_tv_problem_description = (TextView) findViewById(R.id.sobot_tv_problem_description);
        String test = "<font color='#f9676f'>*&nbsp;</font>" + getResources().getString(R.string.sobot_problem_description);
        sobot_tv_problem_description.setText(Html.fromHtml(test));
        sobot_btn_submit = findViewById(R.id.sobot_btn_submit);
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        sobot_btn_submit.setOnClickListener(this);
        sobot_tv_leaveExplain= (TextView) findViewById(R.id.sobot_tv_leaveExplain);
        if(ThemeUtils.isChangedThemeColor(this)){
            Drawable bg= getResources().getDrawable(R.drawable.sobot_bg_theme_color_4dp);
            if(bg!=null){
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable( bg,ThemeUtils.getThemeColor(this)));
            }
        }
    }

    @Override
    protected void initData() {
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotPostLeaveMsgActivity.this,
                ZhiChiConstant.sobot_last_current_initModel);
        if (initMode != null && ChatUtils.isFreeAccount(initMode.getAccountStatus())) {
            sobotFreeAccountTipDialog = new SobotFreeAccountTipDialog(SobotPostLeaveMsgActivity.this, new View.OnClickListener() {
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
        skillGroupId = SharedPreferencesUtil.getStringData(SobotPostLeaveMsgActivity.this, ZhiChiConstant.sobot_connect_group_id, "");
        zhiChiApi.getLeavePostOfflineConfig(SobotPostLeaveMsgActivity.class, mUid, skillGroupId, new StringResultCallBack<SobotOfflineLeaveMsgModel>() {
            @Override
            public void onSuccess(SobotOfflineLeaveMsgModel offlineLeaveMsgModel) {
                if (offlineLeaveMsgModel != null) {
                    sobot_tv_post_msg.setText(TextUtils.isEmpty(offlineLeaveMsgModel.getMsgLeaveTxt()) ? "" : Html.fromHtml(offlineLeaveMsgModel.getMsgLeaveTxt()));
                    sobot_post_et_content.setHint(TextUtils.isEmpty(offlineLeaveMsgModel.getMsgLeaveContentTxt()) ? "" : offlineLeaveMsgModel.getMsgLeaveContentTxt());
                    if (!TextUtils.isEmpty(offlineLeaveMsgModel.getLeaveExplain())){
                        sobot_tv_leaveExplain.setVisibility(View.VISIBLE);
                        sobot_tv_leaveExplain.setText(offlineLeaveMsgModel.getLeaveExplain());
                    }else{
                        sobot_tv_leaveExplain.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_btn_submit) {
            final String content = sobot_post_et_content.getText().toString().trim();
            if (TextUtils.isEmpty(content) || TextUtils.isEmpty(mUid)) {
                CustomToast.makeText(SobotPostLeaveMsgActivity.this, getResources().getString(R.string.sobot_problem_description) + getResources().getString(R.string.sobot__is_null), 1000).show();
                return;
            }
            KeyboardUtil.hideKeyboard(sobot_post_et_content);
            zhiChiApi.leaveMsg(SobotPostLeaveMsgActivity.class, mUid, skillGroupId,content,"0", new StringResultCallBack<BaseCode>() {
                @Override
                public void onSuccess(BaseCode baseCode) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_MSG_LEAVE_CONTENT, content);
                    setResult(EXTRA_MSG_LEAVE_REQUEST_CODE, intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e, String des) {
                    ToastUtil.showToast(getApplicationContext(), des);
                }
            });
        }
    }

}