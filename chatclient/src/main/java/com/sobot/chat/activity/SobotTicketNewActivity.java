package com.sobot.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
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
import com.sobot.chat.activity.halfdialog.SobotZoneActivity;
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
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.api.model.UploadInitModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.listener.SobotCusFieldListener;
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
import com.sobot.chat.utils.SobotChunkedUploadManager;
import com.sobot.chat.utils.SobotJsonUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.LoadingView.SobotLoadingView;
import com.sobot.chat.widget.SobotGridSpacingItemDecoration;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.SobotUploadView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.dialog.SobotSelectPicAndFileDialog;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建留言工单
 */
public class SobotTicketNewActivity extends SobotChatBaseActivity implements View.OnClickListener, SobotCusFieldListener {

    private SobotLeaveMsgConfig mConfig;
    private String mUid = "";
    private String mGroupId = "";
    private String mCustomerId = "";
    private String mCompanyId = "";

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
    private TextView sobot_btn_file, sobot_file_hite, sobot_file_error;//上传按钮、提示、错误提示
    private RecyclerView sobot_reply_msg_pic;
    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();
    private ArrayList<SobotFileModel> cus_pic_list = new ArrayList<>();
    private SobotUploadFileAdapter adapter;
    private SobotSelectPicDialog selectPicDialog;
    private SobotSelectPicAndFileDialog selectPicAndFileDialog;
    //上传图片成功后知道是那个字段，上传成功或失败后清空变量
    private SobotCusFieldConfig uploadFieldConfig;
    //上传图片成功或失败后显示的view
    private SobotUploadView uploadView;
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

    private LinearLayout mllLoading;//加载中
    private SobotLoadingView loading;//加载中

    //滚动
    private ScrollView sobot_sv_root;

    //隐私协议
    private TextView sobot_tv_policy;
    private ImageView cb_policy;
    private LinearLayout sobot_ll_policy;
    private boolean flag_policy = false;
    private SobotChunkedUploadManager uploadManager;

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
            if (StringUtils.isEmpty(mCompanyId)) {
                mCompanyId = SharedPreferencesUtil.getStringData(this,
                        ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            }
            mTempId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_TEMPID);
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketNewActivity";
    }

    @Override
    protected void initView() {
        list = new ArrayList<>();
        // 使用分片上传管理器上传文件
        uploadManager = new SobotChunkedUploadManager(
                SobotTicketNewActivity.this, zhiChiApi);

        sobot_ll_policy = findViewById(R.id.sobot_ll_policy);
        sobot_tv_policy = findViewById(R.id.sobot_tv_policy);
        cb_policy = findViewById(R.id.iv_policy);
        sobot_ll_policy.setOnClickListener(this);

        sobot_sv_root = findViewById(R.id.sobot_sv_root);
        mllLoading = findViewById(R.id.ll_loading);
        loading = findViewById(R.id.iv_loading);
        loading.setProgressColor(ThemeUtils.getThemeColor(this));
        mllContainer = (LinearLayout) findViewById(R.id.sobot_ll_container);
        mLlCompleted = findViewById(R.id.sobot_ll_completed);
        mTvTicket = (TextView) findViewById(R.id.sobot_tv_ticket);
        mTvTicket.setText(R.string.sobot_leaveMsg_to_ticket);
        if (ChatUtils.isRtl(getSobotBaseActivity())) {
            Drawable rigthDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_icon_right_arrow_rtl, null);
            if (rigthDrawable != null) {
                rigthDrawable.setBounds(0, 0, rigthDrawable.getMinimumWidth(), rigthDrawable.getMinimumHeight());
                mTvTicket.setCompoundDrawables(rigthDrawable, null, null, null);
            }
        }
        mTvCompleted = (TextView) findViewById(R.id.sobot_tv_completed);
        mTvCompleted.setText(R.string.sobot_leaveMsg_create_complete);
        mTvLeaveMsgCreateSuccess = (TextView) findViewById(R.id.sobot_tv_leaveMsg_create_success);
        mTvLeaveMsgCreateSuccess.setText(R.string.sobot_leavemsg_success_tip);
        mTvLeaveMsgCreateSuccessDes = (TextView) findViewById(R.id.sobot_tv_leaveMsg_create_success_des);
        mTvLeaveMsgCreateSuccessDes.setText(R.string.sobot_leaveMsg_create_success_des_new);
        mIvLeaveMsgCreateSuccessDes = (ImageView) findViewById(R.id.sobot_iv_leaveMsg_create_success);
        mTvTicket.setOnClickListener(this);
        mTvCompleted.setOnClickListener(this);
        mTvCompleted.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
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
        sobot_reply_msg_pic.addItemDecoration(new SobotGridSpacingItemDecoration(1, ScreenUtils.dip2px(this, 4), false, ChatUtils.isRtl(getSobotBaseActivity())));

        sobot_post_phone = findViewById(R.id.sobot_post_phone);
        sobot_post_phone.setCusCallBack(this);
        sobot_tv_post_msg = (TextView) findViewById(R.id.sobot_tv_post_msg);
        sobot_post_type.setTitle(getResources().getString(R.string.sobot_problem_types), true);
        sobot_post_customer_field = (LinearLayout) findViewById(R.id.sobot_post_customer_field);

        sobot_post_type.getLlSelectOne().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFocus();
                if (null != mConfig) {
                    if (mConfig.getType() != null && mConfig.getType().size() != 0) {
                        ChatUtils.setTypeList(mConfig.getType());
                        Intent intent = new Intent(SobotTicketNewActivity.this, SobotPostCategoryActivity.class);
                        Bundle bundle = new Bundle();
                        if (sobot_post_type.getValue() != null &&
                                !TextUtils.isEmpty(sobot_post_type.getValue())) {
                            bundle.putString("typeName", sobot_post_type.getValue());
                            bundle.putString("typeId", sobot_post_type.getValueId());
                        }
                        intent.putExtra("bundle", bundle);
                        startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_category);
                    }
                }
            }
        });

        updateUIByThemeColor();
        View rootView = findViewById(R.id.view_root);
        rootView.setOnClickListener(hideKeyboardOnClickListener);
        ll_upload_file.setOnClickListener(hideKeyboardOnClickListener);
        sobot_post_title.setOnClickListener(hideKeyboardOnClickListener);
        sobot_post_type.setOnClickListener(hideKeyboardOnClickListener);
        sobot_post_customer_field.setOnClickListener(hideKeyboardOnClickListener);
    }

    /**
     * 创建统一的点击监听器，用于隐藏软键盘并清除输入框焦点
     */
    private View.OnClickListener hideKeyboardOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 隐藏软键盘并清除所有输入框的焦点
            hideAllEditTextFocus();
        }
    };


    private void clearFocus() {
        View view = getCurrentFocus();
        if (view != null) {
            // 失去焦点
            view.clearFocus();
        }
    }

    @Override
    protected void onLeftMenuClick(View view) {
        if (mLlCompleted.getVisibility() == View.VISIBLE) {
            //用广播关闭
            Intent intent = new Intent();
            intent.setAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
            CommonUtils.sendLocalBroadcast(SobotTicketNewActivity.this, intent);
        } else {
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
        if (mConfig != null) {
            showTempConfig();
        } else if (StringUtils.isNoEmpty(mTempId)) {
            //请求模板配置
            requestTempConfig(mTempId);
        }
    }

    /**
     * 获取模板配置
     */
    private void requestTempConfig(String tempateId) {
        //显示加载中
        loading.startSpinning();
        zhiChiApi.getMsgTemplateConfig(REQUEST_TAG, mUid, tempateId, new StringResultCallBack<SobotLeaveMsgConfig>() {
            @Override
            public void onSuccess(SobotLeaveMsgConfig data) {
                mllContainer.setVisibility(View.VISIBLE);
                mLlCompleted.setVisibility(View.GONE);
                mllLoading.setVisibility(View.GONE);
                loading.stopSpinning();
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
                loading.stopSpinning();
                mllContainer.setVisibility(View.VISIBLE);
                mLlCompleted.setVisibility(View.GONE);
                mllLoading.setVisibility(View.GONE);
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
        mllContainer.setVisibility(View.VISIBLE);
        mLlCompleted.setVisibility(View.GONE);
        mllLoading.setVisibility(View.GONE);
        if (mConfig.isTicketContentShowFlag()) {
            String desText = getResources().getString(R.string.sobot_problem_description);

            //问题描述是否显示
            sobot_post_description.setVisibility(View.VISIBLE);
            //问题描述是否必填
            sobot_post_description.setTitle(desText, mConfig.isTicketContentFillFlag());
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
            if (mConfig.getTelCheckRule() == 1) {
                //显示区号
                sobot_post_phone.setInputType("phone");
            } else {
                //显示普通输入
                sobot_post_phone.setInputType("single_line");
            }
            //键盘类型
            sobot_post_phone.setInputHint(getResources().getString(R.string.sobot_please_input));
            boolean isRtl = ChatUtils.isRtl(getSobotBaseActivity());
            if (!isRtl) {
                sobot_post_phone.setViweType("phone");//阿语下不反转
            }
            sobot_post_phone.setVisibility(View.VISIBLE);
        } else {
            sobot_post_phone.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title.setVisibility(View.VISIBLE);
        } else {
            sobot_post_title.setVisibility(View.GONE);
        }
        if (mConfig.getSubmitTicket() == 1 && StringUtils.isNoEmpty(mConfig.getPolicyName())) {
            String agreementText = getResources().getString(R.string.sobot_agree_agreement);
            SpannableString spannableString = new SpannableString(String.format(agreementText, mConfig.getPolicyName()));

            // 设置可点击部分
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // 处理点击事件，例如跳转到用户协议页面
                    Intent intent = new Intent(SobotTicketNewActivity.this, SobotPrivacyAgreementActivity.class);
                    intent.putExtra("policyContent", mConfig.getPolicyContent());
                    intent.putExtra("policyName", mConfig.getPolicyName());
                    startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_policy);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ThemeUtils.getLinkColor(SobotTicketNewActivity.this)); // 设置链接颜色
                    ds.setUnderlineText(true); // 设置下划线
                }
            };

            // 计算"《用户协议》"在字符串中的位置
            int start = agreementText.indexOf("%s");
            int end = start + mConfig.getPolicyName().length();

            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 应用到TextView
            sobot_tv_policy.setText(spannableString);
            sobot_tv_policy.setMovementMethod(LinkMovementMethod.getInstance());

            sobot_ll_policy.setVisibility(View.VISIBLE);
            sobot_btn_submit.setClickable(false);
            sobot_btn_submit.setEnabled(false);
            sobot_btn_submit.getBackground().setAlpha(102);
        } else {
            sobot_ll_policy.setVisibility(View.GONE);
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

        sobot_post_email.setTitle(getResources().getString(R.string.sobot_email), mConfig.isEmailFlag());

        sobot_post_phone.setTitle(getResources().getString(R.string.sobot_phone), mConfig.isTelFlag());
        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title.setTitle(getResources().getString(R.string.sobot_title), true);
        }
    }

    /**
     * 显示留言记录
     */
    private void showTicketInfo() {
        //创建完查询，查询不到新建的数据，需要延迟
        Intent intent = new Intent(SobotTicketNewActivity.this, SobotTicketListActivity.class);
//        intent2.putExtra(StPostMsgPresenter.INTENT_KEY_TICKET_LIST, datas);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_UID, mUid);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, mGroupId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_FROM, StPostMsgPresenter.TICKET_TO_LIST);
        intent.putExtra("delayRefresh", true);
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
        SobotDialogUtils.stopTipDialog(this);
        //页面异常结束时，调用接口清空缓存
        if (null != uploadFieldConfig && null != uploadManager) {
            uploadManager.finishUpload();
        }
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
        } else if (v == mTvCompleted) {
            Intent intent = new Intent();
            intent.setAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
            CommonUtils.sendLocalBroadcast(SobotTicketNewActivity.this, intent);
            //完成
            onBackPressed();
        } else if (v == sobot_btn_file) {
            if (pic_list.size() >= 15) {
                //图片上限15张
                ToastUtil.showToast(this, getResources().getString(R.string.sobot_ticket_update_file_max_hite));
            } else {
                selectPicDialog = new SobotSelectPicDialog(this, itemsOnClick);
                selectPicDialog.show();
            }
        } else if (v == sobot_ll_policy) {
            //隐私协议
            flag_policy = !flag_policy;
            updateFlagPolicy();
            if (flag_policy) {
                sobot_btn_submit.setEnabled(true);
                sobot_btn_submit.setClickable(true);
                sobot_btn_submit.getBackground().setAlpha(255);
            } else {
                sobot_btn_submit.setClickable(false);
                sobot_btn_submit.setEnabled(false);
                sobot_btn_submit.getBackground().setAlpha(102);
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
        boolean isError = StCusFieldPresenter.formatCusFieldVal(this, sobot_post_customer_field, mFields);
        if (!isError) {
            checkSubmit();
        }
    }


    private void checkSubmit() {
        String userPhone = "", userEamil = "", title = "";

        if (mConfig.isTicketTitleShowFlag()) {
            if (TextUtils.isEmpty(sobot_post_title.getSingleValue())) {
                sobot_post_title.showError(getResources().getString(R.string.sobot_title) + "  " + getResources().getString(R.string.sobot__is_null));
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
            } else {
                sobot_post_type.hideError();
            }
        }

        if (mFields != null && mFields.size() != 0) {
            for (int i = 0; i < mFields.size(); i++) {
                if (1 == mFields.get(i).getCusFieldConfig().getFillFlag()) {
                    if (mFields.get(i).getCusFieldConfig().getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_UPLOAD) {
                        if (null == mFields.get(i).getCusFieldConfig().getCacheFile()) {
                            showHint(mFields.get(i).getCusFieldConfig().getFieldName() + "  " + getResources().getString(R.string.sobot__is_null));
                            return;
                        }
                    } else {
                        if (TextUtils.isEmpty(mFields.get(i).getCusFieldConfig().getValue())) {
                            showHint(mFields.get(i).getCusFieldConfig().getFieldName() + "  " + getResources().getString(R.string.sobot__is_null));
                            return;
                        }
                    }
                }
            }
        }
        if (mConfig.isTicketContentShowFlag() && mConfig.isTicketContentFillFlag()) {
            //问题描述 显示 必填才校验
            if (TextUtils.isEmpty(sobot_post_description.getManyValue())) {
                sobot_post_description.showError(getResources().getString(R.string.sobot_problem_description) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
            } else {
                sobot_post_description.hideError();
            }
        }

        if (mConfig.isEnclosureShowFlag() && mConfig.isEnclosureFlag()) {
            if (TextUtils.isEmpty(getFileStr())) {
                sobot_file_error.setText(getResources().getString(R.string.sobot_please_load));
                sobot_file_error.setVisibility(View.VISIBLE);
                return;
            } else {
                sobot_file_error.setVisibility(View.GONE);
            }
        }

        if (mConfig.isEmailShowFlag()) {
            String emailStr = sobot_post_email.getSingleValue();
            if (mConfig.isEmailFlag()) {
                if (TextUtils.isEmpty(emailStr)) {
                    sobot_post_email.showError(getResources().getString(R.string.sobot_email_no_empty));
                    //滚到到底部
                    sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                    return;
                } else {
                    sobot_post_email.hideError();
                }
                if (!TextUtils.isEmpty(emailStr)
                        && ScreenUtils.isEmail(emailStr)) {
                    userEamil = emailStr;
                    sobot_post_email.hideError();
                } else {
                    sobot_post_email.showError(getResources().getString(R.string.sobot_email_dialog_hint));
                    //滚到到底部
                    sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(emailStr)) {
                    if (ScreenUtils.isEmail(emailStr)) {
                        userEamil = emailStr;
                        sobot_post_email.hideError();
                    } else {
                        sobot_post_email.showError(getResources().getString(R.string.sobot_email_dialog_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    }
                }
            }
        }
        //是否显示手机号
        if (mConfig.isTelShowFlag()) {
            if (mConfig.getTelCheckRule() == 1) {
                //获取区号
                phoneCode = sobot_post_phone.getTv_input_two_left().getText().toString();
                userPhone = sobot_post_phone.getPhontValue();
                //是否必填
                if (mConfig.isTelFlag()) {
                    //验证区号
                    if (StringUtils.isEmpty(phoneCode)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_code_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    }
                    if (TextUtils.isEmpty(userPhone)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    }
                    sobot_post_phone.hideError();
                } else {
                    if (StringUtils.isNoEmpty(phoneCode) && StringUtils.isEmpty(userPhone)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    } else if (StringUtils.isEmpty(phoneCode) && StringUtils.isNoEmpty(userPhone)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_code_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    }
                    sobot_post_phone.hideError();
                }
            } else {
                //不显示区号
                phoneCode = "";
                userPhone = sobot_post_phone.getSingleValue();
                //是否必填
                if (mConfig.isTelFlag()) {
                    if (StringUtils.isEmpty(userPhone)) {
                        sobot_post_phone.showError(getResources().getString(R.string.sobot_phone_hint));
                        //滚到到底部
                        sobot_sv_root.fullScroll(ScrollView.FOCUS_DOWN);
                        return;
                    }
                }
                sobot_post_phone.hideError();
            }
        }
        postMsg(userPhone, userEamil, title, flag_policy);
    }

    private void postMsg(String userPhone, String userEamil, String title, boolean authorizeAgree) {
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
        postParam.setRegionCode(phoneCode);
        postParam.setTicketTitle(title);
        postParam.setCompanyId(mConfig.getCompanyId());
        postParam.setFileStr(getFileStr());
        postParam.setGroupId(mGroupId);
        postParam.setTicketFrom("4");
        postParam.setAuthorizeAgree(authorizeAgree);
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
            selectPicDialog.dismiss();
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
    private View.OnClickListener itemsOnClick2 = new View.OnClickListener() {
        public void onClick(View v) {
            selectPicAndFileDialog.dismiss();
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
            if (v.getId() == R.id.btn_pick_file) {
                LogUtils.i("选择文件");
                selectFile();
            }
            if (v.getId() == R.id.btn_cancel) {
                LogUtils.i("取消");
                uploadFieldConfig = null;
            }

        }
    };

    @Override
    public void onClickCusField(TextView view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
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
                    startActivityForResult(intent, cusFieldConfig.getFieldType());
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
            default:
                break;
        }
    }

    @Override
    public void inputLeftOnclick() {
        //选择区号
        Intent intent = new Intent(SobotTicketNewActivity.this, SobotPhoneCodeDialog.class);
        startActivityForResult(intent, 4001);
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
            Drawable bg = getResources().getDrawable(R.drawable.sobot_bg_theme_color_20dp);
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, color));
                mTvCompleted.setBackground(ThemeUtils.applyColorToDrawable(bg, color));
            }
            sobot_btn_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
            mTvCompleted.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
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
                                if (pic_list.size() > 0) {
                                    sobot_reply_msg_pic.setVisibility(View.VISIBLE);
                                } else {
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

    private ChatUtils.SobotSendFileListener sendPicListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotTicketNewActivity.this, mConfig.getCompanyId(), mUid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    if (zhiChiMessage.getData() != null) {
                        SobotFileModel item = new SobotFileModel();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
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
    /**
     * 分片上传文件
     */
    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            File file = new File(filePath);
            if (!file.exists()) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                return;
            }
            if(null == uploadManager){
                uploadManager = new SobotChunkedUploadManager(getSobotBaseActivity(), zhiChiApi);
            }
            uploadManager.uploadFile(file, mCompanyId, new SobotChunkedUploadManager.UploadCallback() {
                @Override
                public void onSuccess(UploadInitModel result) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    if (uploadFieldConfig != null && result != null) {
                        uploadFieldConfig.setText(result.getFileName());
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setFileType(ChatUtils.getFileType(result.getFileName()));
                        cacheFile.setFilePath(filePath);
                        cacheFile.setUrl(result.getFileUrl());
                        cacheFile.setFileName(result.getFileName());
                        uploadFieldConfig.setCacheFile(cacheFile);
                        uploadView.addPicView(uploadFieldConfig);

                    }
                    uploadFieldConfig = null;
                }

                @Override
                public void onFailure(String errorMsg) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    if (uploadView != null && errorMsg != null) {
                        uploadView.showError(errorMsg);
                    }
                    uploadFieldConfig = null;
                }

                @Override
                public void onProgress(int currentChunk, int totalChunks) {
                    // 可以在这里更新进度条
                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
        }
    };

    public void addPicView(SobotFileModel item) {
        if (sobot_reply_msg_pic.getVisibility() == View.GONE) {
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        }
        pic_list.add(item);
        if (pic_list.size() > 0) {
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        } else {
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
        if (resultCode == Activity.RESULT_OK && requestCode == ZhiChiConstant.work_order_list_display_type_policy) {
            flag_policy = data.getBooleanExtra("policyAgree", false);
            updateFlagPolicy();
            if (flag_policy) {
                sobot_btn_submit.setEnabled(true);
                sobot_btn_submit.setClickable(true);
                sobot_btn_submit.getBackground().setAlpha(255);
            } else {
                sobot_btn_submit.setClickable(false);
                sobot_btn_submit.setEnabled(false);
                sobot_btn_submit.getBackground().setAlpha(102);
            }
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                sobot_file_error.setVisibility(View.GONE);
                if (data != null && data.getData() != null) {
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, getSobotBaseActivity());
                    }
                    Uri finalSelectedImage = selectedImage;
                    ImageUtils.getPathAsync(getSobotBaseActivity(), selectedImage, new ImageUtils.OnPathCallback() {
                        @Override
                        public void onResult(String path) {
                            if (!StringUtils.isEmpty(path)) {
                                if (uploadFieldConfig != null) {
                                    File selectedFile = new File(path);
                                    ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, uploadFieldConfig.getMaxStorage(), sendFileListener);
                                } else {
                                    if (MediaFileUtils.isVideoFileType(path)) {
                                        try {

                                            File selectedFile = new File(path);
                                            if (selectedFile.exists()) {
                                                if (selectedFile.length() > 50 * 1024 * 1024) {
                                                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_file_upload_failed));
                                                    return;
                                                }
                                            }
                                            String fName = MD5Util.encode(path);
                                            String filePath = null;
                                            try {
                                                filePath = FileUtil.saveImageFile(getSobotBaseActivity(), finalSelectedImage, fName + FileUtil.getFileEndWith(path), path);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_pic_type_error));
                                                return;
                                            }
                                            sendPicListener.onSuccess(filePath);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        ChatUtils.sendPicByUriPost(getSobotBaseActivity(), finalSelectedImage, sendPicListener, false);
                                    }
                                }
                            } else {
                                SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                                showHint(getResources().getString(R.string.sobot_did_not_get_picture_path));
                            }
                        }
                    });//耗时

                } else {
                    showHint(getResources().getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                sobot_file_error.setVisibility(View.GONE);
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    if (uploadFieldConfig != null) {
                        ChatUtils.sendByFilePost(getSobotBaseActivity(), cameraFile, uploadFieldConfig.getMaxStorage(), sendFileListener);
                    } else {
                        ChatUtils.sendPicByFilePath(getSobotBaseActivity(), cameraFile.getAbsolutePath(), sendPicListener, true);
                    }
                } else {
                    showHint(getResources().getString(R.string.sobot_pic_select_again));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE) {
                //上传文件
                Uri selectedImage = data.getData();
                if (null == selectedImage) {
                    File selectedFile = (File) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE);
                    //文件的上传
                    if (uploadFieldConfig != null && selectedFile != null) {
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, uploadFieldConfig.getMaxStorage(), sendFileListener);
                    }
                } else {
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, getSobotBaseActivity());
                    }
                    String path = ImageUtils.getPath(getSobotBaseActivity(), selectedImage);//耗时
                    if (TextUtils.isEmpty(path)) {
                        ToastUtil.showToast(getSobotBaseActivity(), getResources().getString(R.string.sobot_cannot_open_file));
                        return;
                    }
                    File selectedFile = new File(path);
                    //文件的上传
                    if (uploadFieldConfig != null && selectedFile != null) {
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, uploadFieldConfig.getMaxStorage(), sendFileListener);
                    }
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                sobot_file_error.setVisibility(View.GONE);
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        sendPicListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        if (uploadFieldConfig != null) {
                            ChatUtils.sendByFilePost(getSobotBaseActivity(), cameraFile, uploadFieldConfig.getMaxStorage(), sendFileListener);
                        } else {
                            ChatUtils.sendPicByFilePath(getSobotBaseActivity(), tmpPic.getAbsolutePath(), sendPicListener, true);
                        }
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                }
            }

        }
        if (data != null) {
            StCusFieldPresenter.onStCusFieldActivityResult(getSobotBaseActivity(), data, mFields, sobot_post_customer_field);
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

    //修改我已经阅读勾选框
    private void updateFlagPolicy() {
        if (flag_policy) {
            // 应用主题色到选中状态的图标
            Drawable checkedDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_icon_checkbox_bg, null);
            if (checkedDrawable != null) {
                cb_policy.setBackground(ThemeUtils.applyColorToDrawable(checkedDrawable, ThemeUtils.getThemeColor(getSobotBaseActivity())));
            }
            cb_policy.setImageResource(R.drawable.sobot_icon_checkbox_s);
        } else {
            // 未选中状态
            Drawable uncheckedDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_icon_checkbox_n, null);
            if (uncheckedDrawable != null) {
                cb_policy.setBackground(uncheckedDrawable);
            }
            cb_policy.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * 隐藏所有EditText的焦点并收起软键盘
     */
    private void hideAllEditTextFocus() {
        try {
            // 获取当前具有焦点的视图
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                // 清除焦点
                currentFocus.clearFocus();
                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }

            // 遍历ll_list中的所有子视图，确保所有SobotInputView失去焦点
            if (sobot_post_customer_field != null) {
                for (int i = 0; i < sobot_post_customer_field.getChildCount(); i++) {
                    View child = sobot_post_customer_field.getChildAt(i);
                    if (child instanceof SobotInputView) {
                        child.clearFocus();
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private ArrayList<SobotTimezone> list;
    private int requestCount = 0;//请求的次数

    @Override
    public void selectLeftOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        //时区
        if (list == null || list.size() == 0) {
            requestZone(true, fieldConfig);
        } else {
            showZoneDialog(fieldConfig);
        }
    }

    @Override
    public void selectRightOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        //时间
        if (fieldConfig != null) {
            Intent intent = new Intent(getSobotBaseActivity(), SobotTimeZoneActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("cusFieldConfig", fieldConfig);
            intent.putExtras(bundle);
            startActivityForResult(intent, fieldConfig.getFieldType());
        }
    }

    //================自定义字段上传======start===========
    @Override
    public void onClickDelete(SobotUploadView view, SobotCusFieldConfig fieldConfig) {

    }

    @Override
    public void onClickPreview(SobotUploadView view, SobotCusFieldConfig fieldConfig) {
        //预览，加载本地文件
        if (fieldConfig != null && fieldConfig.getCacheFile() != null) {
            if (fieldConfig.getCacheFile().getFileType() == ZhiChiConstant.MSGTYPE_FILE_PIC) {
                //图片预览
                Intent intent = new Intent(SobotTicketNewActivity.this, SobotPhotoActivity.class);
                intent.putExtra("imageUrL", fieldConfig.getCacheFile().getFilePath());
                startActivity(intent);
            } else {
                //文件预览
                Intent intent = new Intent(SobotTicketNewActivity.this, SobotFileDetailActivity.class);
                fieldConfig.getCacheFile().setMsgId("" + System.currentTimeMillis());
                intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, fieldConfig.getCacheFile());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClickUpload(SobotUploadView view, SobotCusFieldConfig fieldConfig) {
        if (uploadFieldConfig != null) {
            //清空上次的上传数据
        }
        uploadView = view;
        uploadFieldConfig = fieldConfig;
        selectPicAndFileDialog = new SobotSelectPicAndFileDialog(this, itemsOnClick2);
        selectPicAndFileDialog.show();
    }
//================自定义字段上传======end===========

    /**
     * 请求时区
     *
     * @param showDialog
     */
    private void requestZone(final boolean showDialog, SobotCusFieldConfig fieldConfig) {
        if (requestCount > 5) {
            //显示暂无数据
            if (showDialog) {
                showZoneDialog(fieldConfig);
            }
            return;
        }
        requestCount++;
        String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
        zhiChiApi.getTimezone(this, languageCode, new SobotResultCallBack<List<SobotTimezone>>() {
            @Override
            public void onSuccess(List<SobotTimezone> placeModels) {
                list.clear();
                if (placeModels != null) {
                    list.addAll(placeModels);
                }
                if (showDialog) {
                    showZoneDialog(fieldConfig);
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                requestZone(showDialog, fieldConfig);
            }
        });
    }

    private void showZoneDialog(SobotCusFieldConfig fieldConfig) {
        Intent intent = new Intent(this, SobotZoneActivity.class);
        intent.putExtra("cusFieldConfig", fieldConfig);
        intent.putExtra("zoneList", list);
        startActivityForResult(intent, fieldConfig.getFieldType());
    }
}