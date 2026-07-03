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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.SobotUploadView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotCusFieldImagePreviewDialog;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.dialog.SobotSelectPicAndFileDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建留言工单页面
 * <p>
 * 根据模板配置动态生成工单表单，包含以下字段（均可通过后台配置显示/隐藏/必填）：
 * - 标题、问题分类、问题描述、邮箱、手机号（支持区号选择）
 * - 自定义字段（文本、下拉、单选、多选、级联、地区、日期、时间、时区、文件上传等）
 * - 附件上传（支持图片、视频、文件，最多15个，支持分片上传大文件）
 * - 隐私协议勾选（后台配置是否开启）
 * <p>
 * 页面流程：
 * 1. 获取企业是否开通大文件上传 → 2. 获取模板配置 → 3. 渲染表单 → 4. 用户填写提交
 * <p>
 * 提交成功后显示完成页面，用户可选择"前往留言记录"或"完成"返回。
 * 监听 {@link ChatUtils#SOBOT_ACTION_CLOSE_TIKET} 广播，用于关联页面联动关闭。
 */
public class SobotTicketNewActivity extends SobotChatBaseActivity implements View.OnClickListener, SobotCusFieldListener {

    private SobotLeaveMsgConfig mConfig;    // 留言模板配置（字段显示/隐藏/必填规则）
    private String mUid = "";               // 用户ID
    private String mGroupId = "";           // 技能组ID
    private String mCustomerId = "";        // 客户ID
    private String mCompanyId = "";         // 企业ID

    // ==================== 新建工单完成页面控件 ====================
    private LinearLayout mLlCompleted;              // 完成页面容器
    private TextView mTvTicket;                     // "前往留言记录"按钮
    private TextView mTvCompleted;                  // "完成"按钮
    private TextView mTvLeaveMsgCreateSuccess;      // 提交成功提示文字
    private TextView mTvLeaveMsgCreateSuccessDes;   // 提交成功描述文字
    private ImageView mIvLeaveMsgCreateSuccessDes;  // 提交成功图标

    // ==================== 新建工单表单控件 ====================
    private SobotInputView sobot_post_title;        // 标题输入
    private SobotInputView sobot_post_type;         // 问题分类选择
    private SobotInputView sobot_post_description;  // 问题描述输入（多行）
    private SobotInputView sobot_post_email;        // 邮箱输入
    private SobotInputView sobot_post_phone;        // 手机号输入（支持区号）
    private LinearLayout mllContainer;              // 表单容器
    private TextView sobot_tv_post_msg;             // 留言引导文案（支持富文本）
    private TextView sobot_btn_submit;              // 提交按钮
    private LinearLayout sobot_post_customer_field; // 自定义字段容器

    // ==================== 附件上传相关 ====================
    /** 附件上传张数上限 */
    private static final int MAX_FILE_COUNT = 15;
    /**
     * 上传附件 widget（复用自定义字段附件 widget，统一视觉与交互）。
     * 字段名沿用 ll_upload_file 保持其他文件 setVisibility 等调用兼容；类型由 LinearLayout 升级为 SobotUploadView。
     */
    private SobotUploadView ll_upload_file;
    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();  // 已上传文件数据列表（提交时拼 fileStr 的真相源；widget 仅 UI 投影）
    private SobotSelectPicAndFileDialog selectPicAndFileDialog;      // 选择图片/文件弹窗
    private SobotCusFieldConfig uploadFieldConfig;  // 当前正在上传的自定义字段配置（上传完成后清空）
    private SobotUploadView uploadView;             // 当前正在上传的自定义字段视图（用于回显上传结果）
    private String phoneCode;                       // 手机区号

    /**
     * 删除附件确认弹窗
     */
    protected SobotDeleteWorkOrderDialog seleteMenuWindow;

    private ArrayList<SobotFieldModel> mFields;     // 自定义字段列表
    private Information information;                // 用户配置信息
    private MessageReceiver mReceiver;              // 本地广播接收器
    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;  // 免费账号提示弹窗
    private String mTempId;                         // 模板ID（从模板选择页传入）
    private LinearLayout mllLoading;                // 加载中容器
    private SobotLoadingView loading;               // 加载动画

    private ScrollView sobot_sv_root;               // 页面根滚动视图（用于校验失败时滚动到对应位置）

    // ==================== 隐私协议相关 ====================
    private TextView sobot_tv_policy;               // 协议文案（含可点击链接）
    private ImageView cb_policy;                    // 协议勾选框图标
    private LinearLayout sobot_ll_policy;           // 协议区域容器
    private boolean flag_policy = false;            // 是否已勾选协议
    private SobotChunkedUploadManager uploadManager;  // 分片上传管理器

    private boolean hasBigFileUpload = false;       // 企业是否开通大文件上传（开通后上限为500M，否则50M）
    private String hideTxt = "";                    // 附件提示文案模板

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_new;
    }

    /**
     * 从 Intent 中解析传递的参数
     * 包括用户ID、企业ID、客户ID、技能组ID、模板配置、模板ID等
     */
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

    /**
     * 初始化视图控件
     * - 初始化分片上传管理器
     * - 初始化隐私协议区域、完成页面控件、表单输入控件
     * - 设置问题分类选择点击事件、附件上传点击事件
     * - 设置主题色和键盘隐藏监听
     */
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
        displayInNotch(mllContainer);
        mLlCompleted = findViewById(R.id.sobot_ll_completed);
        displayInNotch(mLlCompleted);
        mTvTicket = (TextView) findViewById(R.id.sobot_tv_ticket);
        mTvTicket.setText(R.string.sobot_leaveMsg_to_ticket);
        // 设计稿要求："前往留言记录"按钮无箭头 — 原 setCompoundDrawables RTL 镜像箭头逻辑已删除
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
        // 完成态按钮排列：宽屏（横屏/折叠内屏/平板）走横向、翻转按钮顺序；竖屏走 XML 默认 vertical
        bindCompletedActionsByOrientation();

        //新建工单
        sobot_post_title = findViewById(R.id.sobot_post_title);
        sobot_post_type = findViewById(R.id.sobot_post_type);
        sobot_post_description = findViewById(R.id.sobot_post_description);
        sobot_post_email = findViewById(R.id.sobot_post_email);
        sobot_post_email.setViweType("email");
        sobot_btn_submit = findViewById(R.id.sobot_btn_submit);
        ll_upload_file = findViewById(R.id.ll_upload_file);
        hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        // 复用自定义字段附件 widget：默认上限 50 → 改 15，文案先按 50M（getProductByCode 异步回调若开通大文件再改 500M）
        ll_upload_file.setMaxCount(MAX_FILE_COUNT);
        ll_upload_file.setTipText(50);
        ll_upload_file.setCusCallBack(uploadCallback, ll_upload_file);

        sobot_post_phone = findViewById(R.id.sobot_post_phone);
        sobot_post_phone.setCusCallBack(this);
        sobot_tv_post_msg = (TextView) findViewById(R.id.sobot_tv_post_msg);
        sobot_post_type.setTitle(getResources().getString(R.string.sobot_problem_types), true);
        sobot_post_customer_field = (LinearLayout) findViewById(R.id.sobot_post_customer_field);
        displayInNotch(sobot_btn_submit);
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

    /**
     * 完成态按钮排列：宽屏（横屏 / 折叠屏内屏 / 平板）下切换为水平布局并翻转顺序
     * （前往留言记录左、完成右，间距由 marginStart 控制）；竖屏沿用 XML 默认 vertical。
     * 设计稿要求两按钮在横屏下等宽并排：把 XML 的 match_parent 改成 0dp + weight=1。
     */
    private void bindCompletedActionsByOrientation() {
        LinearLayout actions = findViewById(R.id.sobot_completed_actions);
        if (actions == null) {
            return;
        }
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
            actions.setOrientation(LinearLayout.HORIZONTAL);
            // 翻转按钮顺序：前往留言记录在前、完成在后
            actions.removeView(mTvCompleted);
            actions.addView(mTvCompleted);
            int gap = getResources().getDimensionPixelSize(R.dimen.sobot_completed_btn_gap);
            // "前往留言记录"按钮：等宽两列左项，top margin 清零，无 start margin
            LinearLayout.LayoutParams ticketLp = (LinearLayout.LayoutParams) mTvTicket.getLayoutParams();
            ticketLp.width = 0;
            ticketLp.weight = 1;
            ticketLp.topMargin = 0;
            ticketLp.setMarginStart(0);
            mTvTicket.setLayoutParams(ticketLp);
            // "完成"按钮：等宽两列右项，间距由 marginStart 控制
            LinearLayout.LayoutParams completedLp = (LinearLayout.LayoutParams) mTvCompleted.getLayoutParams();
            completedLp.width = 0;
            completedLp.weight = 1;
            completedLp.topMargin = 0;
            completedLp.setMarginStart(gap);
            mTvCompleted.setLayoutParams(completedLp);
        }
    }

    /**
     * 横屏 两列行容器整行可见性兜底：
     * 仅在 layout-w600dp XML 下 findViewById 才能命中行容器；
     * - 两个 cell 都隐藏：整行 GONE
     * - 仅一个 cell 隐藏：把对应 Space 也 GONE，剩余 cell 通过 weight=1 撑满整行
     */
    private void applyTwoColRowVisibility() {
        View rowTitleType = findViewById(R.id.sobot_row_title_type);
        if (rowTitleType != null && sobot_post_title != null && sobot_post_type != null) {
            boolean titleVisible = sobot_post_title.getVisibility() == View.VISIBLE;
            boolean typeVisible = sobot_post_type.getVisibility() == View.VISIBLE;
            rowTitleType.setVisibility(titleVisible || typeVisible ? View.VISIBLE : View.GONE);
            View spaceTitleType = findViewById(R.id.sobot_space_title_type);
            if (spaceTitleType != null) {
                spaceTitleType.setVisibility(titleVisible && typeVisible ? View.VISIBLE : View.GONE);
            }
        }
        View rowEmailPhone = findViewById(R.id.sobot_row_email_phone);
        if (rowEmailPhone != null && sobot_post_email != null && sobot_post_phone != null) {
            boolean emailVisible = sobot_post_email.getVisibility() == View.VISIBLE;
            boolean phoneVisible = sobot_post_phone.getVisibility() == View.VISIBLE;
            rowEmailPhone.setVisibility(emailVisible || phoneVisible ? View.VISIBLE : View.GONE);
            View spaceEmailPhone = findViewById(R.id.sobot_space_email_phone);
            if (spaceEmailPhone != null) {
                spaceEmailPhone.setVisibility(emailVisible && phoneVisible ? View.VISIBLE : View.GONE);
            }
        }
    }


    private void clearFocus() {
        View view = getCurrentFocus();
        if (view != null) {
            // 失去焦点
            view.clearFocus();
        }
    }

    /**
     * 标题栏返回按钮点击处理
     * 如果当前显示的是完成页面，发送广播关闭所有留言相关页面；否则执行默认返回
     */
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

    /**
     * 初始化数据
     * 1. 获取用户配置信息和初始化模型
     * 2. 检测免费账号并弹出提示
     * 3. 查询企业是否开通大文件上传，然后加载模板配置
     */
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
        getIsOpenBigFileUpload();
    }

    /**
     * 获取企业是否开通大文件上传
     */
    private void getIsOpenBigFileUpload() {
        zhiChiApi.getProductByCode(REQUEST_TAG, mCompanyId, new SobotResultCallBack<Boolean>() {
            @Override
            public void onSuccess(Boolean bigFileUpload) {
                hasBigFileUpload = bigFileUpload;
                if (bigFileUpload) {
                    ll_upload_file.setTipText(500);
                }
                if (mConfig != null) {
                    showTempConfig();
                } else if (StringUtils.isNoEmpty(mTempId)) {
                    //请求模板配置
                    requestTempConfig(mTempId);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (mConfig != null) {
                    showTempConfig();
                } else if (StringUtils.isNoEmpty(mTempId)) {
                    //请求模板配置
                    requestTempConfig(mTempId);
                }
            }
        });
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
            displayInNotch(sobot_ll_policy);
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
                        // 横屏：非铺满类型字段两列展示（附件 / 多行文本始终单列），由 Presenter 内部处理配对
                        StCusFieldPresenter.addWorkOrderCusFieldsNew(getSobotBaseContext(), mFields, sobot_post_customer_field, SobotTicketNewActivity.this, getResources().getInteger(R.integer.sobot_list_span_count) > 1);
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
        // 横屏 两列行容器整行可见性兜底（仅 layout-w600dp 下生效，竖屏 findViewById 为 null 跳过）
        applyTwoColRowVisibility();
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

    /**
     * 页面销毁时：
     * 1. 注销广播接收器
     * 2. 通知外部留言页面已关闭
     * 3. 关闭进度弹窗和提示弹窗
     * 4. 如果有正在上传的文件，调用 finishUpload() 清理缓存
     */
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

    /**
     * 点击事件处理
     * - "前往留言记录"按钮：发送关闭广播，跳转到留言记录列表
     * - "完成"按钮：发送关闭广播，返回上一页
     * - 上传附件按钮：检查附件数量上限，弹出选择图片/文件弹窗
     * - 隐私协议区域：切换勾选状态，控制提交按钮可用性
     * - 提交按钮：执行自定义字段校验并提交工单
     */
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
     * 提交前的自定义字段校验
     * 先校验自定义字段，校验通过后执行 checkSubmit() 校验标准字段
     */
    private void setCusFieldValue() {
        //自定义表单校验结果:为空,校验通过,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
        boolean isError = StCusFieldPresenter.formatCusFieldVal(this, sobot_post_customer_field, mFields);
        if (!isError) {
            checkSubmit();
        }
    }


    /**
     * 校验标准字段（标题、分类、描述、附件、邮箱、手机号）
     * 校验顺序按表单从上到下排列，校验不通过时显示错误提示并 return。
     * 全部校验通过后调用 postMsg() 提交工单
     */
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
                        java.util.List<SobotCacheFile> files = mFields.get(i).getCusFieldConfig().getCacheFileList();
                        if (files == null || files.isEmpty()) {
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
                ll_upload_file.showError(getResources().getString(R.string.sobot_please_load));
                return;
            } else {
                ll_upload_file.hideError();
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

    /**
     * 提交留言工单
     * 组装所有表单数据（包括自定义字段、扩展参数等）调用接口提交。
     * 提交期间禁用提交按钮防止重复提交。
     * 成功后隐藏表单，显示完成页面。
     *
     * @param userPhone      手机号
     * @param userEamil      邮箱
     * @param title          标题
     * @param authorizeAgree 是否同意隐私协议
     */
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
                    LogUtils.e("uncaught", e);
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

    /**
     * 选择图片/文件弹窗的按钮点击监听（拍照、选择照片、选择视频、选择文件、取消）
     */
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

    /**
     * 自定义字段点击回调
     * 根据字段类型打开对应的选择页面：日期/时间选择器、下拉/单选/多选、级联选择、地区选择
     */
    @Override
    public void onClickCusField(TextView view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
        clearFocus();
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

    /**
     * 手机号输入框左侧区号点击回调，打开区号选择页面
     */
    @Override
    public void inputLeftOnclick() {
        Intent intent = new Intent(SobotTicketNewActivity.this, SobotPhoneCodeDialog.class);
        startActivityForResult(intent, 4001);
    }

    /**
     * 本地广播接收器，监听关闭留言页面的广播事件
     */
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ChatUtils.SOBOT_ACTION_CLOSE_TIKET.equals(intent.getAction())) {
                finish();
            }
        }
    }

    /**
     * 根据主题色更新提交按钮、完成按钮、成功图标的颜色
     */
    public void updateUIByThemeColor() {
        if (ThemeUtils.isChangedThemeColor(getSobotBaseContext())) {

            int color = ThemeUtils.getThemeColor(getSobotBaseContext());
            Drawable bg = getResources().getDrawable(R.drawable.sobot_bg_theme_color_20dp);
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, color));
                mTvCompleted.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, color));
            }
            sobot_btn_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
            mTvCompleted.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
            mIvLeaveMsgCreateSuccessDes.setImageDrawable(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_icon_completed), color));
        }
    }

    /**
     * 初始化图片选择的控件。
     * 改造后 SobotUploadView 已在 onCreate 初始化（setMaxCount + setTipText + setCusCallBack），
     * 此方法保留为兼容入口（旧调用点 showTempConfig 仍调用），方法体为空。
     */
    private void initPicListView() {
        // no-op：SobotUploadView 自管 UI，pic_list 由 addPicView 维护，删除走 uploadCallback.onClickDelete
    }

    /**
     * 上传 widget 回调（onClickUpload / onClickPreview / onClickDelete 三个核心，其余空实现）。
     * D0：删除直接生效，不弹"是否删除"确认 dialog，与自定义字段附件交互完全一致。
     */
    private final SobotCusFieldListener uploadCallback = new SobotCusFieldListener() {
        @Override
        public void onClickUpload(SobotUploadView view, SobotCusFieldConfig fieldConfig) {
            // 这里的 onClickUpload 是 ticket_new 主附件的上传按钮触发的（fieldConfig 为 null）
            // 自定义字段附件的上传走 SobotCustomFieldUtils 的另一条调用链，不复用此回调
            if (fieldConfig != null) {
                return;
            }
            if (pic_list.size() >= MAX_FILE_COUNT) {
                // 附件上限兜底（widget 内部已隐藏上传按钮，此处仅防御）
                ToastUtil.showToast(SobotTicketNewActivity.this,
                        String.format(getResources().getString(R.string.sobot_ticket_update_file_max_hite), MAX_FILE_COUNT));
                return;
            }
            selectPicAndFileDialog = new SobotSelectPicAndFileDialog(SobotTicketNewActivity.this, itemsOnClick2);
            selectPicAndFileDialog.show();
        }

        @Override
        public void onClickPreview(SobotUploadView view, SobotCusFieldConfig fieldConfig, SobotCacheFile cacheFile) {
            // 自定义字段附件预览走自己的回调链，这里只处理主附件区域
            if (fieldConfig != null || cacheFile == null) {
                return;
            }
            String url = cacheFile.getUrl();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            if (MediaFileUtils.isVideoFileType(url)) {
                Intent intent = SobotVideoActivity.newIntent(getSobotBaseActivity(), cacheFile);
                startActivity(intent);
            } else {
                if (SobotOption.imagePreviewListener != null) {
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(getSobotBaseActivity(), url);
                    if (isIntercept) {
                        return;
                    }
                }
                Intent intent = new Intent(getSobotBaseActivity(), SobotPhotoActivity.class);
                intent.putExtra("imageUrL", url);
                startActivity(intent);
            }
        }

        @Override
        public void onClickDelete(SobotUploadView view, SobotCusFieldConfig fieldConfig, SobotCacheFile cacheFile) {
            // 自定义字段附件删除走自己的 cusFieldConfig.removeCacheFile，这里只处理主附件区域
            if (fieldConfig != null || cacheFile == null) {
                return;
            }
            // widget 已自动移除 UI（D0）；这里只同步 pic_list（按 fileUrl 匹配定位）
            String url = cacheFile.getUrl();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            java.util.Iterator<SobotFileModel> it = pic_list.iterator();
            while (it.hasNext()) {
                SobotFileModel m = it.next();
                if (url.equals(m.getFileUrl())) {
                    it.remove();
                    break;
                }
            }
        }

        // 以下 4 个回调与附件无关（手机区号 / 时区 / 自定义字段点击），主附件场景空实现
        @Override
        public void onClickCusField(TextView view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
        }

        @Override
        public void inputLeftOnclick() {
        }

        @Override
        public void selectLeftOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        }

        @Override
        public void selectRightOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        }
    };

    /**
     * SobotFileModel → SobotCacheFile 转换（widget 内部用 SobotCacheFile 渲染 chip）。
     * pic_list 仍以 SobotFileModel 为真相，widget 仅作 UI 镜像。
     */
    private SobotCacheFile toCacheFile(SobotFileModel item) {
        SobotCacheFile cacheFile = new SobotCacheFile();
        cacheFile.setUrl(item.getFileUrl());
        cacheFile.setFilePath(!TextUtils.isEmpty(item.getFileLocalPath()) ? item.getFileLocalPath() : item.getFileUrl());
        cacheFile.setFileName(item.getFileName());
        // SobotFileModel.fileType 是 String（"jpg"/"mp4"...），SobotCacheFile.fileType 是 int（ZhiChiConstant.MSGTYPE_FILE_*）
        // 必须用 ChatUtils.getFileType（返回 ZhiChiConstant 体系，PIC=22）；
        // 不能用 FileTypeConfig.getFileType（返回 FileTypeConfig 体系，PIC=1）——否则 onClickPreview / SobotUploadView.addPicView
        // 中按 ZhiChiConstant.MSGTYPE_FILE_PIC 的判断永远不成立，图片附件会被当文件打开
        cacheFile.setFileType(ChatUtils.getFileType(item.getFileType()));
        cacheFile.setMsgId("" + System.currentTimeMillis());
        return cacheFile;
    }

    public void showHint(String content) {
        if (!TextUtils.isEmpty(content)) {
            ToastUtil.showToast(getSobotBaseContext(), content);
        }
    }

    /**
     * 文件上传回调监听
     * 文件选择/压缩完成后触发分片上传，上传成功后：
     * - 如果是自定义字段的上传：回显到对应的自定义字段视图
     * - 如果是附件上传：添加到附件列表并刷新
     */
    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            File file = new File(filePath);
            if (!file.exists()) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                return;
            }
            if (null == uploadManager) {
                uploadManager = new SobotChunkedUploadManager(getSobotBaseActivity(), zhiChiApi);
            }
            uploadManager.uploadFile(file, mCompanyId, new SobotChunkedUploadManager.UploadCallback() {
                @Override
                public void onSuccess(UploadInitModel result) {
                    SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                    if (uploadFieldConfig != null && result != null) {
                        //自定义字段：追加到多文件列表
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setFileType(ChatUtils.getFileType(result.getFileName()));
                        cacheFile.setFilePath(filePath);
                        cacheFile.setUrl(result.getFileUrl());
                        cacheFile.setFileName(result.getFileName());
                        uploadFieldConfig.addCacheFile(cacheFile);
                        uploadView.addPicView(cacheFile);

                        uploadFieldConfig = null;
                    } else {
                        //附件
                        SobotFileModel item = new SobotFileModel();
                        item.setFileUrl(result.getFileUrl());
                        item.setFileLocalPath(filePath);
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                        item.setFileName(fileName);
                        item.setFileType(fileType);
                        addPicView(item);
                    }
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

    /**
     * 添加附件到已上传列表并刷新显示。
     * pic_list 是真相（提交时拼 fileStr 用），widget 仅 UI 投影。
     */
    public void addPicView(SobotFileModel item) {
        pic_list.add(item);
        // widget 内部自动管理上传按钮 / 列表显隐（addPicView 内调用 updateUploadButtonVisibility）
        ll_upload_file.addPicView(toCacheFile(item));
        // 上传成功后清除"请上传附件"红字错误提示（widget 的 addPicView 内部已隐藏，但此处显式调用更明确）
        ll_upload_file.hideError();
    }

    /**
     * 获取所有已上传附件的 URL 拼接字符串（分号分隔），用于提交工单
     */
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


    /**
     * 处理子页面返回结果
     * - 隐私协议页面返回：更新勾选状态
     * - 图片/视频/文件选择返回：执行文件上传
     * - 问题分类选择返回：更新分类显示
     * - 区号选择返回：更新区号显示
     * - 自定义字段选择返回：更新自定义字段值
     */
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
                ll_upload_file.hideError();
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
                                File selectedFile = new File(path);
                                int maxStorage = 50;
                                if (uploadFieldConfig != null) {
                                    maxStorage = uploadFieldConfig.getMaxStorage();
                                } else if (hasBigFileUpload) {
                                    maxStorage = 500;
                                }
                                ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, maxStorage, sendFileListener);
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
                ll_upload_file.hideError();
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    int maxStorage = 50;
                    if (uploadFieldConfig != null) {
                        maxStorage = uploadFieldConfig.getMaxStorage();
                    } else if (hasBigFileUpload) {
                        maxStorage = 500;
                    }
                    ChatUtils.sendByFilePost(getSobotBaseActivity(), cameraFile, maxStorage, sendFileListener);
                } else {
                    showHint(getResources().getString(R.string.sobot_pic_select_again));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE) {
                //上传文件
                ll_upload_file.hideError();
                Uri selectedImage = data.getData();
                if (null == selectedImage) {
                    File selectedFile = (File) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE);
                    //文件的上传
                    int maxStorage = 50;
                    if (uploadFieldConfig != null) {
                        maxStorage = uploadFieldConfig.getMaxStorage();
                    } else if (hasBigFileUpload) {
                        maxStorage = 500;
                    }
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, maxStorage, sendFileListener);
                } else {
                    // 先弹 loading，再走异步解析路径（大文件 content:// 复制到 cache 耗时 1~3s，
                    // 同步 getPath 会阻塞主线程白屏）
                    SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                    final int maxStorage;
                    if (uploadFieldConfig != null) {
                        maxStorage = uploadFieldConfig.getMaxStorage();
                    } else if (hasBigFileUpload) {
                        maxStorage = 500;
                    } else {
                        maxStorage = 50;
                    }
                    ImageUtils.getPathAsync(getSobotBaseActivity(), selectedImage, new ImageUtils.OnPathCallback() {
                        @Override
                        public void onResult(String path) {
                            if (TextUtils.isEmpty(path)) {
                                SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
                                ToastUtil.showToast(getSobotBaseActivity(), getResources().getString(R.string.sobot_cannot_open_file));
                                return;
                            }
                            File selectedFile = new File(path);
                            ChatUtils.sendByFilePost(getSobotBaseActivity(), selectedFile, maxStorage, sendFileListener);
                        }
                    });
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                ll_upload_file.hideError();
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        int maxStorage = 50;
                        if (uploadFieldConfig != null) {
                            maxStorage = uploadFieldConfig.getMaxStorage();
                        } else if (hasBigFileUpload) {
                            maxStorage = 500;
                        }
                        ChatUtils.sendByFilePost(getSobotBaseActivity(), cameraFile, maxStorage, sendFileListener);
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(getSobotBaseActivity());
                        int maxStorage = 50;
                        if (uploadFieldConfig != null) {
                            maxStorage = uploadFieldConfig.getMaxStorage();
                        } else if (hasBigFileUpload) {
                            maxStorage = 500;
                        }
                        ChatUtils.sendByFilePost(getSobotBaseActivity(), cameraFile, maxStorage, sendFileListener);
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

    private ArrayList<SobotTimezone> list;       // 时区列表数据缓存
    private int requestCount = 0;               // 时区请求重试次数（最多5次）

    /**
     * 自定义字段时区选择左侧（时区）点击回调
     */
    @Override
    public void selectLeftOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        if (list == null || list.size() == 0) {
            requestZone(true, fieldConfig);
        } else {
            showZoneDialog(fieldConfig);
        }
    }

    /**
     * 自定义字段时区选择右侧（时间）点击回调
     */
    @Override
    public void selectRightOnclick(TextView view, SobotCusFieldConfig fieldConfig) {
        if (fieldConfig != null) {
            Intent intent = new Intent(getSobotBaseActivity(), SobotTimeZoneActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("cusFieldConfig", fieldConfig);
            intent.putExtras(bundle);
            startActivityForResult(intent, fieldConfig.getFieldType());
        }
    }

    // ==================== 自定义字段上传回调 ====================

    /**
     * 自定义字段删除附件回调（列表已在 SobotUploadView 内部更新，此处无需额外处理）
     */
    @Override
    public void onClickDelete(SobotUploadView view, SobotCusFieldConfig fieldConfig, SobotCacheFile cacheFile) {

    }

    /**
     * 自定义字段附件预览回调，根据文件类型跳转图片预览或文件预览页面
     */
    @Override
    public void onClickPreview(SobotUploadView view, SobotCusFieldConfig fieldConfig, SobotCacheFile cacheFile) {
        if (cacheFile == null) return;
        if (cacheFile.getFileType() == ZhiChiConstant.MSGTYPE_FILE_PIC) {
            // 收集字段中所有图片，支持左右滑动预览
            List<SobotCacheFile> imageList = new ArrayList<>();
            int startIndex = 0;
            if (fieldConfig != null && fieldConfig.getCacheFileList() != null) {
                for (SobotCacheFile f : fieldConfig.getCacheFileList()) {
                    if (f.getFileType() == ZhiChiConstant.MSGTYPE_FILE_PIC) {
                        if (f == cacheFile) {
                            startIndex = imageList.size();
                        }
                        imageList.add(f);
                    }
                }
            }
            if (imageList.isEmpty()) {
                imageList.add(cacheFile);
            }
            SobotCusFieldImagePreviewDialog dialog =
                    new SobotCusFieldImagePreviewDialog(SobotTicketNewActivity.this, imageList, startIndex);
            dialog.show();
        } else {
            Intent intent = new Intent(SobotTicketNewActivity.this, SobotFileDetailActivity.class);
            cacheFile.setMsgId("" + System.currentTimeMillis());
            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * 自定义字段上传按钮点击回调，记录当前上传的字段和视图，弹出文件选择弹窗
     */
    @Override
    public void onClickUpload(SobotUploadView view, SobotCusFieldConfig fieldConfig) {
        uploadView = view;
        uploadFieldConfig = fieldConfig;
        selectPicAndFileDialog = new SobotSelectPicAndFileDialog(this, itemsOnClick2);
        selectPicAndFileDialog.show();
    }
//================自定义字段上传======end===========

    /**
     * 请求时区列表数据
     * 请求失败时自动重试，最多重试5次
     *
     * @param showDialog  请求成功后是否弹出时区选择弹窗
     * @param fieldConfig 关联的自定义字段配置
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

    /**
     * 弹出时区选择页面
     */
    private void showZoneDialog(SobotCusFieldConfig fieldConfig) {
        Intent intent = new Intent(this, SobotZoneActivity.class);
        intent.putExtra("cusFieldConfig", fieldConfig);
        intent.putExtra("zoneList", list);
        startActivityForResult(intent, fieldConfig.getFieldType());
    }
}