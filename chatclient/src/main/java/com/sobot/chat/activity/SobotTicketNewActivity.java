package com.sobot.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.activity.halfdialog.SobotPhoneCodeDialog;
import com.sobot.chat.activity.halfdialog.SobotPostCascadeActivity;
import com.sobot.chat.activity.halfdialog.SobotPostCategoryActivity;
import com.sobot.chat.activity.halfdialog.SobotPostRegionActivity;
import com.sobot.chat.activity.halfdialog.SobotTimeZoneActivity;
import com.sobot.chat.adapter.SobotUploadFileAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.CommonModelBase;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.PostParamModel;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.api.model.SobotLeaveMsgParamModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.presenter.StCusFieldPresenter;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotJsonUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotGridSpacingItemDecoration;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.util.ArrayList;

/**
 * 新建留言工单
 */
public class SobotTicketNewActivity extends SobotChatBaseActivity implements View.OnClickListener, ISobotCusField {

    private SobotLeaveMsgConfig mConfig;
    private String mUid = "";
    private String mGroupId = "";
    private String mCustomerId = "";
    private String mCompanyId = "";
    private boolean flag_exit_sdk;
    private int flag_exit_type = -1;


    //新建工单完成
    private LinearLayout mLlCompleted;
    private TextView mTvTicket;
    private TextView mTvCompleted;
    private TextView mTvLeaveMsgCreateSuccess;
    private TextView mTvLeaveMsgCreateSuccessDes;
    private ImageView mIvLeaveMsgCreateSuccessDes;

    //新建工单
    private SobotInputView sobot_post_title;//标题
    private SobotInputView sobot_post_type;//分类
    private SobotInputView sobot_post_description;//描述
    private SobotInputView sobot_post_email;//邮箱
    private SobotInputView sobot_post_phone;//手机
    private LinearLayout mllContainer;
    private TextView sobot_tv_post_msg;
    private TextView sobot_btn_submit;
    private LinearLayout sobot_post_customer_field;
    private LinearLayout ll_upload_file;//上传附件
    private TextView sobot_btn_file, sobot_file_hite,sobot_file_error;//上传按钮、提示、错误提示
    private RecyclerView sobot_reply_msg_pic;
    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();
    private SobotUploadFileAdapter adapter;
    private SobotSelectPicDialog menuWindow;
    private String phoneCode;

    /**
     * 删除图片弹窗
     */
    protected SobotDeleteWorkOrderDialog seleteMenuWindow;

    private ArrayList<SobotFieldModel> mFields;

    private Information information;


    private MessageReceiver mReceiver;


    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;

    private String mTempId;//模板id

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_new;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_UID);
            mConfig = (SobotLeaveMsgConfig) getIntent().getSerializableExtra(StPostMsgPresenter.INTENT_KEY_CONFIG);
            mGroupId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_GROUPID);
            mCustomerId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID);
            mCompanyId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID);
            mTempId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_TEMPID);
            flag_exit_type = getIntent().getIntExtra(ZhiChiConstant.FLAG_EXIT_TYPE, -1);
            flag_exit_sdk = getIntent().getBooleanExtra(ZhiChiConstant.FLAG_EXIT_SDK, false);
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketNewActivity";
    }

    @Override
    protected void initView() {

        mllContainer = (LinearLayout) findViewById(R.id.sobot_ll_container);
        mLlCompleted = findViewById(R.id.sobot_ll_completed);
        mTvTicket = (TextView) findViewById(R.id.sobot_tv_ticket);
        mTvTicket.setText(R.string.sobot_leaveMsg_to_ticket);
        mTvCompleted = (TextView) findViewById(R.id.sobot_tv_completed);
        mTvCompleted.setText(R.string.sobot_leaveMsg_create_complete);
        mTvLeaveMsgCreateSuccess = (TextView) findViewById(R.id.sobot_tv_leaveMsg_create_success);
        mTvLeaveMsgCreateSuccess.setText(R.string.sobot_leavemsg_success_tip);
        mTvLeaveMsgCreateSuccessDes = (TextView) findViewById(R.id.sobot_tv_leaveMsg_create_success_des);
        mTvLeaveMsgCreateSuccessDes.setText(R.string.sobot_leaveMsg_create_success_des_new);
        mIvLeaveMsgCreateSuccessDes = (ImageView) findViewById(R.id.sobot_iv_leaveMsg_create_success);
        mTvTicket.setOnClickListener(this);
        mTvCompleted.setOnClickListener(this);
        initReceiver();
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTvCompleted.getLayoutParams();
            lp.topMargin = ScreenUtils.dip2px(SobotTicketNewActivity.this, 40);
        }

        //新建工单
        sobot_post_title = findViewById(R.id.sobot_post_title);
        sobot_post_type = findViewById(R.id.sobot_post_type);
        sobot_post_description = findViewById(R.id.sobot_post_description);
        sobot_post_email = findViewById(R.id.sobot_post_email);
        sobot_post_email.setViweType("email");
        sobot_btn_submit = findViewById(R.id.sobot_btn_submit);
        ll_upload_file = findViewById(R.id.ll_upload_file);
        sobot_btn_file = findViewById(R.id.sobot_btn_file);
        sobot_btn_file.setOnClickListener(this);
        sobot_file_hite = findViewById(R.id.sobot_file_hite);
        sobot_file_error = findViewById(R.id.sobot_file_error);
        String hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        sobot_file_hite.setText(String.format(hideTxt, "15", "50M"));
        sobot_reply_msg_pic = findViewById(R.id.sobot_reply_msg_pic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        sobot_reply_msg_pic.setLayoutManager(layoutManager);
        sobot_reply_msg_pic.addItemDecoration(new SobotGridSpacingItemDecoration(1, ScreenUtils.dip2px(this, 4),false));

        sobot_post_phone = findViewById(R.id.sobot_post_phone);
        sobot_post_phone.getTv_input_two_left().setText("+86");
        sobot_post_phone.setClickLister(new SobotInputView.InputListen() {
            @Override
            public void inputLeftOnclick() {
                //选择区号
                Intent intent = new Intent(SobotTicketNewActivity.this, SobotPhoneCodeDialog.class);
                startActivityForResult(intent, 4001);
            }

            @Override
            public void selectLeftOnclick() {

            }

            @Override
            public void selectRightOnclick() {

            }
        });
        sobot_tv_post_msg = (TextView) findViewById(R.id.sobot_tv_post_msg);
        sobot_post_type.setTitle(getResources().getString(R.string.sobot_problem_types),true);
        sobot_post_customer_field = (LinearLayout) findViewById(R.id.sobot_post_customer_field);

        sobot_post_type.getTvSelect().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocus();
                if (mConfig.getType() != null && mConfig.getType().size() != 0) {
                    Intent intent = new Intent(SobotTicketNewActivity.this, SobotPostCategoryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("types", mConfig.getType());
                    if (sobot_post_type.getValue() != null &&
                            !TextUtils.isEmpty(sobot_post_type.getValue()) ) {
                        bundle.putString("typeName", sobot_post_type.getValue());
                        bundle.putString("typeId", sobot_post_type.getValueId());
                    }
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_category);
                }
            }
        });

        updateUIByThemeColor();

    }

    private void clearFocus(){
        View view = getCurrentFocus();
        if (view != null) {
            // 失去焦点
            view.clearFocus();
        }
    }
    @Override
    protected void onLeftMenuClick(View view) {
        if(mLlCompleted.getVisibility()==View.VISIBLE){
            //用广播关闭
            Intent intent = new Intent();
            intent.setAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
            CommonUtils.sendLocalBroadcast(SobotTicketNewActivity.this, intent);
        }else {
            super.onLeftMenuClick(view);
        }
    }

    @Override
    protected void initData() {
        information = (Information) SharedPreferencesUtil.getObject(this, "sobot_last_current_info");
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotTicketNewActivity.this,
                ZhiChiConstant.sobot_last_current_initModel);
        if (initMode != null && ChatUtils.isFreeAccount(initMode.getAccountStatus())) {
            sobotFreeAccountTipDialog = new SobotFreeAccountTipDialog(SobotTicketNewActivity.this, new View.OnClickListener() {
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
        setTitle(R.string.sobot_please_leave_a_message);

        showLeftMenu(true);
        if(mConfig!=null){
            showTempConfig();
        }else if(StringUtils.isNoEmpty(mTempId)){
            //请求模板配置
            requestTempConfig(mTempId);
        }
    }

    /**
     * 获取模板配置
     */
    private void requestTempConfig(String tempateId) {
        mllContainer.setVisibility(View.VISIBLE);
        mLlCompleted.setVisibility(View.GONE);
        zhiChiApi.getMsgTemplateConfig(REQUEST_TAG, mUid, tempateId, new StringResultCallBack<SobotLeaveMsgConfig>() {
            @Override
            public void onSuccess(SobotLeaveMsgConfig data) {
                if (data != null) {
                    //选择留言模版
                    mConfig = data;
                    showTempConfig();
                } else {
                    ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotTicketNewActivity.this,
                            ZhiChiConstant.sobot_last_current_initModel);
                    //如果mConfig 为空，直接从初始化接口获取配置信息
                    Information info = (Information) SharedPreferencesUtil.getObject(SobotTicketNewActivity.this, "sobot_last_current_info");
                    mConfig = new SobotLeaveMsgConfig();
                    mConfig.setEmailFlag(initMode.isEmailFlag());
                    mConfig.setEmailShowFlag(initMode.isEmailShowFlag());
                    mConfig.setEnclosureFlag(initMode.isEnclosureFlag());
                    mConfig.setEnclosureShowFlag(initMode.isEnclosureShowFlag());
                    mConfig.setTelFlag(initMode.isTelFlag());
                    mConfig.setTelShowFlag(initMode.isTelShowFlag());
                    mConfig.setTicketStartWay(initMode.isTicketStartWay());
                    mConfig.setTicketShowFlag(initMode.isTicketShowFlag());
                    mConfig.setCompanyId(initMode.getCompanyId());
                    if (!TextUtils.isEmpty(info.getLeaveMsgTemplateContent())) {
                        mConfig.setMsgTmp(info.getLeaveMsgTemplateContent());
                    } else {
                        mConfig.setMsgTmp(initMode.getMsgTmp());
                    }
                    if (!TextUtils.isEmpty(info.getLeaveMsgGuideContent())) {
                        mConfig.setMsgTxt(info.getLeaveMsgGuideContent());
                    } else {
                        mConfig.setMsgTxt(initMode.getMsgTxt());
                    }
                    mTvTicket.setVisibility(mConfig.isTicketShowFlag() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotTicketNewActivity.this,
                        ZhiChiConstant.sobot_last_current_initModel);
                //如果mConfig 为空，直接从初始化接口获取配置信息
                Information info = (Information) SharedPreferencesUtil.getObject(SobotTicketNewActivity.this, "sobot_last_current_info");
                mConfig = new SobotLeaveMsgConfig();
                mConfig.setEmailFlag(initMode.isEmailFlag());
                mConfig.setEmailShowFlag(initMode.isEmailShowFlag());
                mConfig.setEnclosureFlag(initMode.isEnclosureFlag());
                mConfig.setEnclosureShowFlag(initMode.isEnclosureShowFlag());
                mConfig.setTelFlag(initMode.isTelFlag());
                mConfig.setTelShowFlag(initMode.isTelShowFlag());
                mConfig.setTicketStartWay(initMode.isTicketStartWay());
                mConfig.setTicketShowFlag(initMode.isTicketShowFlag());
                mConfig.setCompanyId(initMode.getCompanyId());
                if (!TextUtils.isEmpty(info.getLeaveMsgTemplateContent())) {
                    mConfig.setMsgTmp(info.getLeaveMsgTemplateContent());
                } else {
                    mConfig.setMsgTmp(initMode.getMsgTmp());
                }
                if (!TextUtils.isEmpty(info.getLeaveMsgGuideContent())) {
                    mConfig.setMsgTxt(info.getLeaveMsgGuideContent());
                } else {
                    mConfig.setMsgTxt(initMode.getMsgTxt());
                }
                mTvTicket.setVisibility(mConfig.isTicketShowFlag() ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * 显示模板字段，创建工单
     */
    private void showTempConfig() {
        if (mConfig.isTicketContentShowFlag()) {
            String desText = getResources().getString(R.string.sobot_problem_description);

            //问题描述是否显示
            sobot_post_description.setVisibility(View.VISIBLE);
            //问题描述是否必填
            sobot_post_description.setTitle(desText,mConfig.isTicketContentFillFlag());
        } else {
            sobot_post_description.setVisibility(View.GONE);
        }
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        sobot_btn_submit.setOnClickListener(this);
        sobot_post_customer_field.setVisibility(View.GONE);

        if (mConfig.isEmailShowFlag()) {
            sobot_post_email.setVisibility(View.VISIBLE);
        } else {
            sobot_post_email.setVisibility(View.GONE);
        }

        if (mConfig.isTelShowFlag()) {
            if(mConfig.getTelCheckRule()==1){
                //显示区号
                sobot_post_phone.setInputType("phone");
            }else{
                //显示普通输入
                sobot_post_phone.setInputType("single_line");
            }
            //键盘类型
            sobot_post_phone.setViweType("phone");
            sobot_post_phone.setVisibility(View.VISIBLE);
        } else {
            sobot_post_phone.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title.setVisibility(View.VISIBLE);
        } else {
            sobot_post_title.setVisibility(View.GONE);
        }


        String sobotUserPhone = (information != null ? information.getUser_tels() : "");
        if (mConfig.isTelShowFlag() && !TextUtils.isEmpty(sobotUserPhone)) {
            sobot_post_phone.setInputValue(sobotUserPhone);
        }
        String sobotUserEmail = (information != null ? information.getUser_emails() : "");
        if (mConfig.isEmailShowFlag() && !TextUtils.isEmpty(sobotUserEmail)) {
            sobot_post_email.setVisibility(View.VISIBLE);
            sobot_post_email.setInputValue(sobotUserEmail);
        }

        if (mConfig.isEnclosureShowFlag()) {
            ll_upload_file.setVisibility(View.VISIBLE);
            initPicListView();
        } else {
            ll_upload_file.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTypeFlag() && mConfig.getType() != null && mConfig.getType().size() > 0) {
            sobot_post_type.setVisibility(View.VISIBLE);
        } else {
            sobot_post_type.setVisibility(View.GONE);
            sobot_post_type.setTag(mConfig.getTicketTypeId());
        }
        zhiChiApi.getTemplateFieldsInfo(REQUEST_TAG, mUid, mConfig.getTemplateId(), new StringResultCallBack<SobotLeaveMsgParamModel>() {

            @Override
            public void onSuccess(SobotLeaveMsgParamModel result) {
                if (result != null) {
                    if (result.getField() != null && result.getField().size() != 0) {
                        mFields = result.getField();
                        StCusFieldPresenter.addWorkOrderCusFieldsNew(getSobotBaseContext(), mFields, sobot_post_customer_field, SobotTicketNewActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                try {
                    showHint(getResources().getString(R.string.sobot_try_again));
                } catch (Exception e1) {

                }
            }

        });

        if (information != null && information.getLeaveMsgTemplateContent() != null) {
            sobot_post_description.setInputHint(information.getLeaveMsgTemplateContent().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"));
        } else {
            if (!TextUtils.isEmpty(mConfig.getMsgTmp())) {
                mConfig.setMsgTmp(mConfig.getMsgTmp().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"));
                sobot_post_description.setInputHint(mConfig.getMsgTmp());
            }
        }

        if (information != null && information.getLeaveMsgGuideContent() != null) {
            if (TextUtils.isEmpty(information.getLeaveMsgGuideContent())) {
                sobot_tv_post_msg.setVisibility(View.GONE);
            }
            HtmlTools.getInstance(getSobotBaseActivity().getApplicationContext()).setRichText(sobot_tv_post_msg, information.getLeaveMsgGuideContent().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"),
                    R.color.sobot_postMsg_url_color);
        } else {
            if (!TextUtils.isEmpty(mConfig.getMsgTxt())) {
                mConfig.setMsgTxt(mConfig.getMsgTxt().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"));
                HtmlTools.getInstance(getSobotBaseActivity().getApplicationContext()).setRichText(sobot_tv_post_msg, mConfig.getMsgTxt(),
                        R.color.sobot_postMsg_url_color);
            } else {
                sobot_tv_post_msg.setVisibility(View.GONE);
            }
        }

        sobot_tv_post_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        String mustFill = "<font color='#f9676f'>*&nbsp;</font>";

        sobot_post_email.setTitle(getResources().getString(R.string.sobot_email),mConfig.isEmailFlag());

        sobot_post_phone.setTitle( getResources().getString(R.string.sobot_phone),mConfig.isTelFlag());
        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title.setTitle(getResources().getString(R.string.sobot_title),true);
        }
    }

    /**
     * 显示留言记录
     */
    private void showTicketInfo() {
        Intent intent = new Intent(SobotTicketNewActivity.this, SobotTicketListActivity.class);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_UID, mUid);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
        startActivity(intent);
        finish();
    }

    private void initReceiver() {
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
        }
        // 创建过滤器，并指定action，使之用于接收同action的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
        LocalBroadcastManager.getInstance(getSobotBaseActivity()).registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getSobotBaseActivity()).unregisterReceiver(mReceiver);
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_CloseLeave);
        }
        SobotDialogUtils.stopProgressDialog(this);
        super.onDestroy();
    }
    @Override
    public void onClick(View v) {
        clearFocus();
        if (v == mTvTicket) {
            Intent intent = new Intent();
            intent.setAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
            CommonUtils.sendLocalBroadcast(SobotTicketNewActivity.this, intent);
            //前往留言记录
            showTicketInfo();
        }else if (v == mTvCompleted) {
            Intent intent = new Intent();
            intent.setAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
            CommonUtils.sendLocalBroadcast(SobotTicketNewActivity.this, intent);
            //完成
            onBackPressed();
        }else if(v == sobot_btn_file){
            if(pic_list.size()>=15){
                //图片上限15张
                ToastUtil.showToast(this, getResources().getString(R.string.sobot_ticket_update_file_max_hite));
            }else {
                menuWindow = new SobotSelectPicDialog(this, itemsOnClick);
                menuWindow.show();
            }
        } else if (v == sobot_btn_submit) {
            setCusFieldValue();
        }
    }

    /**
     * 提交
     */
    private void setCusFieldValue() {
        //自定义表单校验结果:为空,校验通过,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
        String errorMsg = StCusFieldPresenter.formatCusFieldVal(this, sobot_post_customer_field, mFields);
        if (TextUtils.isEmpty(errorMsg)) {
            checkSubmit();
        }
    }


    private void checkSubmit() {
        String userPhone = "", userEamil = "", title = "";

        if (mConfig.isTicketTitleShowFlag()) {
            if (TextUtils.isEmpty(sobot_post_title.getSingleValue())) {
                sobot_post_title.showError(getResources().getString(R.string.sobot_title) + "  " +getResources().getString(R.string.sobot__is_null));
                return;
            } else {
                sobot_post_title.hideError();
                title = sobot_post_title.getSingleValue();
            }
        }

        if (sobot_post_type.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(sobot_post_type.getSelectValue())) {
                sobot_post_type.showError(getResources().getString(R.string.sobot_problem_types) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
            }else{
                sobot_post_type.hideError();
            }
        }

         if (mFields != null && mFields.size() != 0) {
            for (int i = 0; i < mFields.size(); i++) {
                if (1 == mFields.get(i).getCusFieldConfig().getFillFlag()) {
                    if (TextUtils.isEmpty(mFields.get(i).getCusFieldConfig().getValue())) {
                        showHint(mFields.get(i).getCusFieldConfig().getFieldName() + "  " + getResources().getString(R.string.sobot__is_null));
                        return;
                    }
                }
            }
        }
        if (mConfig.isTicketContentShowFlag() && mConfig.isTicketContentFillFlag()) {
            //问题描述 显示 必填才校验
            if (TextUtils.isEmpty(sobot_post_description.getManyValue())) {
                sobot_post_description.showError(getResources().getString(R.string.sobot_problem_description) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
            }else{
                sobot_post_description.hideError();
            }
        }

        if (mConfig.isEnclosureShowFlag() && mConfig.isEnclosureFlag()) {
            if (TextUtils.isEmpty(getFileStr())) {
                sobot_file_error.setText(getResources().getString(R.string.sobot_please_load));
                sobot_file_error.setVisibility(View.VISIBLE);
                return;
            }else{
                sobot_file_error.setVisibility(View.GONE);
            }
        }

        if (mConfig.isEmailShowFlag()) {
            String emailStr = sobot_post_email.getSingleValue();
            if (mConfig.isEmailFlag()) {
                if (TextUtils.isEmpty(emailStr)) {
                    sobot_post_email.showError(getResources().getString(R.string.sobot_email_no_empty));
                    return;
                }else{
                    sobot_post_email.hideError();
                }
                if (!TextUtils.isEmpty(emailStr)
                        && ScreenUtils.isEmail(emailStr)) {
                    userEamil = emailStr;
                    sobot_post_email.hideError();
                } else {
                    sobot_post_email.showError(getResources().getString(R.string.sobot_email_dialog_hint));
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(emailStr)) {
                    if (ScreenUtils.isEmail(emailStr)) {
                        userEamil = emailStr;
                        sobot_post_email.hideError();
                    } else {
                        sobot_post_email.showError(getResources().getString(R.string.sobot_email_dialog_hint));
                        return;
                    }
                }
            }
        }

        if (mConfig.isTelShowFlag()) {
            String phone = "";//输入的手机号
            if(mConfig.getTelCheckRule()==1){
                //获取区号
                phoneCode = sobot_post_phone.getTv_input_two_left().getText().toString();
                phone =sobot_post_phone.getPhontValue();
            }else{
                //不显示区号
                phoneCode="";
                phone =sobot_post_phone.getSingleValue();
            }

            if (mConfig.isTelFlag()) {
                //验证区号
                if (mConfig.getTelCheckRule()==1 && StringUtils.isEmpty(phoneCode)) {
                    sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_code_hint));
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_hint));
                    return;
                }
                userPhone = phoneCode + phone;
                sobot_post_phone.hideError();
            } else {
                if(mConfig.getTelCheckRule()==1) {
                    if (StringUtils.isNoEmpty(phoneCode) && StringUtils.isEmpty(phone)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_hint));
                        return;
                    }
                }
                if (!TextUtils.isEmpty(phone)) {
                    userPhone = phoneCode + phone;
                }
                sobot_post_phone.hideError();
            }
        }

        postMsg(userPhone, userEamil, title);
    }
    private void postMsg(String userPhone, String userEamil, String title) {
        sobot_btn_submit.setAlpha(0.5f);
        sobot_btn_submit.setEnabled(false);
        sobot_btn_submit.setClickable(false);
        PostParamModel postParam = new PostParamModel();
        postParam.setTemplateId(mConfig.getTemplateId());
        postParam.setPartnerId(information.getPartnerid());
        postParam.setUid(mUid);
        postParam.setTicketContent(sobot_post_description.getManyValue());
        postParam.setCustomerEmail(userEamil);
        postParam.setCustomerPhone(userPhone);
        postParam.setTicketTitle(title);
        postParam.setCompanyId(mConfig.getCompanyId());
        postParam.setFileStr(getFileStr());
        postParam.setGroupId(mGroupId);
        postParam.setTicketFrom("4");
        if (information != null && information.getLeaveParamsExtends() != null) {
            postParam.setParamsExtends(SobotJsonUtils.toJson(information.getLeaveParamsExtends()));
        }
        if (sobot_post_type.getTvTitle().getTag() != null && !TextUtils.isEmpty(sobot_post_type.getTvTitle().getTag().toString())) {
            postParam.setTicketTypeId(sobot_post_type.getTvTitle().getTag().toString());
        }
        if (mFields == null) {
            mFields = new ArrayList<>();
        }

        if (information.getLeaveCusFieldMap() != null && information.getLeaveCusFieldMap().size() > 0) {
            for (String key :
                    information.getLeaveCusFieldMap().keySet()) {
                SobotFieldModel sobotFieldModel = new SobotFieldModel();
                SobotCusFieldConfig sobotCusFieldConfig = new SobotCusFieldConfig();
                sobotCusFieldConfig.setFieldId(key);
                sobotCusFieldConfig.setValue(information.getLeaveCusFieldMap().get(key));
                sobotFieldModel.setCusFieldConfig(sobotCusFieldConfig);
                mFields.add(sobotFieldModel);
            }
        }
        postParam.setExtendFields(StCusFieldPresenter.getSaveFieldVal(mFields));

        zhiChiApi.postMsg(REQUEST_TAG, postParam, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase base) {
                sobot_btn_submit.setAlpha(1f);
                sobot_btn_submit.setEnabled(true);
                sobot_btn_submit.setClickable(true);
                try {
                    if (Integer.parseInt(base.getStatus()) == 0) {
                        showHint(base.getMsg());
                    } else if (Integer.parseInt(base.getStatus()) == 1) {
                        //显示提交完成后的页面
                        mllContainer.setVisibility(View.GONE);
                        mLlCompleted.setVisibility(View.VISIBLE);
                        SobotSoftKeyboardUtils.hideKeyboard(SobotTicketNewActivity.this);
                    }
                } catch (Exception e) {
                    showHint(base.getMsg());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                sobot_btn_submit.setAlpha(1f);
                sobot_btn_submit.setEnabled(true);
                sobot_btn_submit.setClickable(true);
                try {
                    showHint(getResources().getString(R.string.sobot_try_again));
                } catch (Exception e1) {

                }
            }
        });
    }
    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == R.id.btn_take_photo) {
                LogUtils.i("拍照");
                selectPicFromCamera();
            }
            if (v.getId() == R.id.btn_pick_photo) {
                LogUtils.i("选择照片");
                selectPicFromLocal();
            }
            if (v.getId() == R.id.btn_pick_vedio) {
                LogUtils.i("选择视频");
                selectVedioFromLocal();
            }

        }
    };
    @Override
    public void onClickCusField(View view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
        clearFocus();
        //清空获得焦点
        if (cusField == null) return;
        final SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
        switch (fieldConfig.getFieldType()) {
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE:
                //时间或日期
                StCusFieldPresenter.openTimePicker(getSobotBaseActivity(), null, fieldConfig);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE:
                StCusFieldPresenter.startSobotCusFieldActivity(getSobotBaseActivity(), null, cusField);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE:
                if (cusField.getCusFieldDataInfoList() != null && cusField.getCusFieldDataInfoList().size() > 0) {
                    Intent intent = new Intent(getSobotBaseActivity(), SobotPostCascadeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cusField", cusField);
                    bundle.putSerializable("fieldId", cusField.getCusFieldConfig().getFieldId());
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE);
                }
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE:
                if (cusFieldConfig != null) {
                    Intent intent = new Intent(getSobotBaseActivity(), SobotPostRegionActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedIds", cusFieldConfig.getValue());
                    bundle.putString("selectedText", cusFieldConfig.getShowName());
                    bundle.putSerializable("cusFieldConfig", cusFieldConfig);
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, cusFieldConfig.getFieldType());
                }
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE:
                if (cusFieldConfig != null) {
                    Intent intent = new Intent(getSobotBaseActivity(), SobotTimeZoneActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cusFieldConfig", cusFieldConfig);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, cusFieldConfig.getFieldType());
                }
                break;
            default:
                break;
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ChatUtils.SOBOT_ACTION_CLOSE_TIKET.equals(intent.getAction())) {
                finish();
            }
        }
    }

    public void updateUIByThemeColor() {
        if (ThemeUtils.isChangedThemeColor(getSobotBaseContext())) {

            int color = ThemeUtils.getThemeColor(getSobotBaseContext());
            mTvTicket.setTextColor(color);
            Drawable bg = getResources().getDrawable(R.drawable.sobot_normal_btn_bg);
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, color));
                mTvCompleted.setBackground(ThemeUtils.applyColorToDrawable(bg, color));
            }
            mIvLeaveMsgCreateSuccessDes.setImageDrawable(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_icon_completed), color));
        }
    }

    /**
     * 初始化图片选择的控件
     */
    private void initPicListView() {
        adapter = new SobotUploadFileAdapter(SobotTicketNewActivity.this, pic_list, true, new SobotUploadFileAdapter.Listener() {
            @Override
            public void downFileLister(SobotFileModel model) {

            }

            @Override
            public void previewMp4(SobotFileModel fileModel) {
                File file = new File(fileModel.getFileUrl());
                SobotCacheFile cacheFile = new SobotCacheFile();
                cacheFile.setFileName(file.getName());
                cacheFile.setUrl(fileModel.getFileUrl());
                cacheFile.setFilePath(fileModel.getFileUrl());
                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(fileModel.getFileUrl())));
                cacheFile.setMsgId("" + System.currentTimeMillis());
                Intent intent = SobotVideoActivity.newIntent(getSobotBaseActivity(), cacheFile);
                startActivity(intent);
            }

            @Override
            public void deleteFile(final SobotFileModel fileModel) {
                String popMsg = getResources().getString(R.string.sobot_do_you_delete_picture);
                if (fileModel != null) {
                    if (!TextUtils.isEmpty(fileModel.getFileUrl()) && MediaFileUtils.isVideoFileType(fileModel.getFileUrl())) {
                        popMsg = getResources().getString(R.string.sobot_do_you_delete_video);
                    }
                }
                if (seleteMenuWindow != null) {
                    seleteMenuWindow.dismiss();
                    seleteMenuWindow = null;
                }
                if (seleteMenuWindow == null) {
                    seleteMenuWindow = new SobotDeleteWorkOrderDialog(getSobotBaseActivity(), popMsg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seleteMenuWindow.dismiss();
                            if (v.getId() == R.id.btn_pick_photo) {
                                Log.e("onClick: ", seleteMenuWindow.getPosition() + "");
                                pic_list.remove(fileModel);
                                if(pic_list.size()>0) {
                                    sobot_reply_msg_pic.setVisibility(View.VISIBLE);
                                }else{
                                    sobot_reply_msg_pic.setVisibility(View.GONE);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                seleteMenuWindow.show();
            }

            @Override
            public void previewPic(String fileUrl, String fileName) {
                if (SobotOption.imagePreviewListener != null) {
                    //如果返回true,拦截;false 不拦截
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(getSobotBaseActivity(), fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                Intent intent = new Intent(getSobotBaseActivity(), SobotPhotoActivity.class);
                intent.putExtra("imageUrL", fileUrl);
                startActivity(intent);
            }
        });

        sobot_reply_msg_pic.setAdapter(adapter);
    }

    public void showHint(String content) {
        if (!TextUtils.isEmpty(content)) {
            ToastUtil.showToast(getSobotBaseContext(), content);
        }
    }
    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(REQUEST_TAG, mConfig.getCompanyId(), mUid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    if (zhiChiMessage.getData() != null) {
                        SobotFileModel item = new SobotFileModel();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                        String fileType = fileName.substring(fileName.lastIndexOf(".")+1);
                        item.setFileName(fileName);
                        item.setFileType(fileType);
                        addPicView(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    showHint(TextUtils.isEmpty(des) ? getResources().getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
        }
    };
    public void addPicView(SobotFileModel item) {
        if(sobot_reply_msg_pic.getVisibility()==View.GONE){
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        }
        pic_list.add(item);
        if(pic_list.size()>0) {
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        }else{
            sobot_reply_msg_pic.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }
    public String getFileStr() {
        String tmpStr = "";
        if (!mConfig.isEnclosureShowFlag()) {
            return tmpStr;
        }

        for (int i = 0; i < pic_list.size(); i++) {
            tmpStr += pic_list.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }

    public String getFileNameStr() {
        String tmpStr = "";
        if (!mConfig.isEnclosureShowFlag()) {
            return tmpStr;
        }
        for (int i = 0; i < pic_list.size(); i++) {
            if (!TextUtils.isEmpty(pic_list.get(i).getFileLocalPath())) {
                tmpStr += pic_list.get(i).getFileLocalPath().substring(pic_list.get(i).getFileLocalPath().lastIndexOf("/") + 1);
            }
            if (i != (pic_list.size() - 1)) {
                tmpStr += "<br/>";
            }
        }
        return tmpStr;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, getSobotBaseActivity());
                    }
                    String path = ImageUtils.getPath(getSobotBaseActivity(), selectedImage);
                    if (!StringUtils.isEmpty(path)) {
                        if (MediaFileUtils.isVideoFileType(path)) {
                            try {
                                File selectedFile = new File(path);
                                if (selectedFile.exists()) {
                                    if (selectedFile.length() > 50 * 1024 * 1024) {
                                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_file_upload_failed));
                                        return;
                                    }
                                }
                                SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
//                            ChatUtils.sendPicByFilePath(getSobotBaseActivity(),path,sendFileListener,false);
                                String fName = MD5Util.encode(path);
                                String filePath = null;
                                try {
                                    filePath = FileUtil.saveImageFile(getSobotBaseActivity(), selectedImage, fName + FileUtil.getFileEndWith(path), path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_pic_type_error));
                                    return;
                                }
                                sendFileListener.onSuccess(filePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                            ChatUtils.sendPicByUriPost(getSobotBaseActivity(), selectedImage, sendFileListener, false);
                        }
                    } else {
                        showHint(getResources().getString(R.string.sobot_did_not_get_picture_path));
                    }
                } else {
                    showHint(getResources().getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    ChatUtils.sendPicByFilePath(getSobotBaseActivity(), cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getResources().getString(R.string.sobot_pic_select_again));
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(this);
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(this);
                        ChatUtils.sendPicByFilePath(this, tmpPic.getAbsolutePath(), sendFileListener, true);
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                }
            }
        }
        StCusFieldPresenter.onStCusFieldActivityResult(getSobotBaseActivity(), data, mFields, sobot_post_customer_field);
        if (data != null) {
            switch (requestCode) {
                case ZhiChiConstant.work_order_list_display_type_category:
                    if (!TextUtils.isEmpty(data.getStringExtra("category_typeId"))) {
                        String typeName = data.getStringExtra("category_typeName");
                        String typeId = data.getStringExtra("category_typeId");

                        if (!TextUtils.isEmpty(typeName)) {
                            sobot_post_type.setInputValue(typeName);
                            sobot_post_type.getTvTitle().setTag(typeId);
                        }
                    }
                    break;
                case 4001:
                    //区号
                    phoneCode = data.getStringExtra("selectCode");
                    sobot_post_phone.setInputLeftValue(phoneCode);
                    break;
                default:
                    break;
            }
        }
    }
}