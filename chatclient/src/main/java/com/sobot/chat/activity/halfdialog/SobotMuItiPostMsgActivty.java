package com.sobot.chat.activity.halfdialog;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotUploadFileAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.CommonModel;
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
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.listener.ISobotCusField;
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
import com.sobot.chat.utils.SobotSerializableMap;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 多伦 工单节点对应的 留言弹窗界面
 */
public class SobotMuItiPostMsgActivty extends SobotDialogBaseActivity implements View.OnClickListener, ISobotCusField {
    private TextView sobot_tv_title;
    private EditText sobot_post_email, sobot_et_content, sobot_post_phone, sobot_post_title;
    private TextView sobot_tv_post_msg, sobot_post_email_lable, sobot_post_phone_lable, sobot_post_lable, sobot_post_title_lable, sobot_post_question_type, sobot_post_question_lable, sobot_tv_problem_description, tv_problem_description_required;
    private TextView sobot_btn_submit;
    private LinearLayout sobot_post_customer_field, sobot_ll_content_img, ll_problem_description_title;
    private RelativeLayout sobot_post_question_ll;
    private LinearLayout sobot_post_title_rl, sobot_post_email_rl, sobot_post_phone_rl;
    private LinearLayout sobot_ll_edit_phone;//编辑的手机号
    private TextView sobot_tv_phone_code;//手机区号

    private LinearLayout ll_upload_file;//上传附件
    private TextView sobot_btn_file, sobot_file_hite;//上传按钮
    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();
    private SobotUploadFileAdapter adapter;
    private RecyclerView sobot_reply_msg_pic;
    private SobotSelectPicDialog menuWindow;
    private String mUid = "";

    //临时 回显提示语时使用
    private String templateId = "";
    private String tipMsgId = "";
    /**
     * 删除图片弹窗
     */
    protected SobotDeleteWorkOrderDialog seleteMenuWindow;

    private ArrayList<SobotFieldModel> mFields;

    private LinearLayout sobot_post_msg_layout;

    private SobotLeaveMsgConfig mConfig;
    private Information information;
    private String uid = "";
    private String mGroupId = "";
    private boolean flag_exit_sdk;

    private int flag_exit_type = -1;
    private String phoneCode;


//    public static SobotMuItiPostMsgActivty newInstance(Bundle data) {
//        Bundle arguments = new Bundle();
//        arguments.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, data);
//        SobotMuItiPostMsgActivty muItiPostMsgActivty = new SobotMuItiPostMsgActivty();
//
//        return fragment;
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);//竖屏
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//横屏

            }
        }
        super.onCreate(savedInstanceState);

        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_muit_post_msg;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotMuItiPostMsgActivty";
    }

    @Override
    protected void initView() {
        super.initView();
        list = new ArrayList<>();
        ll_upload_file = findViewById(R.id.ll_upload_file);
        sobot_btn_file = findViewById(R.id.sobot_btn_file);
        sobot_file_hite = findViewById(R.id.sobot_file_hite);
        sobot_btn_file.setOnClickListener(this);
        String hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        sobot_file_hite.setText(String.format(hideTxt, "15", "50M"));
        sobot_reply_msg_pic = findViewById(R.id.sobot_reply_msg_pic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        sobot_reply_msg_pic.setLayoutManager(layoutManager);

        sobot_ll_edit_phone = findViewById(R.id.sobot_ll_edit_phone);
        sobot_tv_phone_code = findViewById(R.id.sobot_tv_phone_code);
        findViewById(R.id.ll_select_code).setOnClickListener(this);
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_write_info_string);
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseContext(),
                ZhiChiConstant.sobot_last_current_initModel);
        templateId = getIntent().getStringExtra("templateId");
        tipMsgId = getIntent().getStringExtra("tipMsgId");
        mUid = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_UID);
        mConfig = (SobotLeaveMsgConfig) getIntent().getSerializableExtra(StPostMsgPresenter.INTENT_KEY_CONFIG);
        mGroupId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_GROUPID);
        if (mConfig == null) {
            //如果mConfig 为空，直接从初始化接口获取配置信息
            Information info = (Information) SharedPreferencesUtil.getObject(getSobotBaseContext(), "sobot_last_current_info");
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
        }
        Bundle bundle = new Bundle();
        bundle.putString(StPostMsgPresenter.INTENT_KEY_UID, mUid);
        bundle.putString(StPostMsgPresenter.INTENT_KEY_GROUPID, mGroupId);
        bundle.putInt(ZhiChiConstant.FLAG_EXIT_TYPE, flag_exit_type);
        bundle.putBoolean(ZhiChiConstant.FLAG_EXIT_SDK, flag_exit_sdk);
        bundle.putSerializable(StPostMsgPresenter.INTENT_KEY_CONFIG, mConfig);

        if (bundle != null) {
            uid = bundle.getString(StPostMsgPresenter.INTENT_KEY_UID);
            mGroupId = bundle.getString(StPostMsgPresenter.INTENT_KEY_GROUPID);
            flag_exit_type = bundle.getInt(ZhiChiConstant.FLAG_EXIT_TYPE, -1);
            flag_exit_sdk = bundle.getBoolean(ZhiChiConstant.FLAG_EXIT_SDK, false);
            mConfig = (SobotLeaveMsgConfig) bundle.getSerializable(StPostMsgPresenter.INTENT_KEY_CONFIG);
        }
        sobot_ll_content_img = (LinearLayout) findViewById(R.id.sobot_ll_content_img);
        sobot_post_phone = (EditText) findViewById(R.id.sobot_post_phone);
        sobot_post_email = (EditText) findViewById(R.id.sobot_post_email);
        sobot_post_title = (EditText) findViewById(R.id.sobot_post_title);
        sobot_et_content = (EditText) findViewById(R.id.sobot_post_et_content);
        sobot_tv_post_msg = (TextView) findViewById(R.id.sobot_tv_post_msg);
        sobot_post_email_lable = (TextView) findViewById(R.id.sobot_post_email_lable);
        sobot_post_phone_lable = (TextView) findViewById(R.id.sobot_post_phone_lable);
        sobot_post_title_lable = (TextView) findViewById(R.id.sobot_post_title_lable);
        sobot_post_lable = (TextView) findViewById(R.id.sobot_post_question_lable);
        String test = getResources().getString(R.string.sobot_problem_types) + "<font color='#f9676f'>&nbsp;*</font>";
        sobot_post_lable.setText(Html.fromHtml(test));
        sobot_post_question_lable = (TextView) findViewById(R.id.sobot_post_question_lable);
        sobot_post_question_type = (TextView) findViewById(R.id.sobot_post_question_type);
        sobot_post_msg_layout = (LinearLayout) findViewById(R.id.sobot_post_msg_layout);
        sobot_post_customer_field = (LinearLayout) findViewById(R.id.sobot_post_customer_field);
        sobot_post_email_rl = findViewById(R.id.sobot_post_email_rl);
        sobot_post_phone_rl = findViewById(R.id.sobot_post_phone_rl);
        sobot_post_title_rl = findViewById(R.id.sobot_post_title_rl);
        sobot_post_question_ll = findViewById(R.id.sobot_post_question_ll);
        sobot_post_question_ll.setOnClickListener(this);
        ll_problem_description_title = findViewById(R.id.ll_problem_description_title);
        sobot_tv_problem_description = findViewById(R.id.sobot_tv_problem_description);
        tv_problem_description_required = findViewById(R.id.tv_problem_description_required);
        sobot_tv_problem_description.setText(R.string.sobot_problem_description);
        if (mConfig.isTicketContentShowFlag()) {
            //问题描述是否显示
            ll_problem_description_title.setVisibility(View.VISIBLE);
            sobot_et_content.setVisibility(View.VISIBLE);
            //问题描述是否必填
            if (mConfig.isTicketContentFillFlag()) {
                tv_problem_description_required.setVisibility(View.VISIBLE);

            } else {
                tv_problem_description_required.setVisibility(View.GONE);
            }
        } else {
            ll_problem_description_title.setVisibility(View.GONE);
            sobot_et_content.setVisibility(View.GONE);
        }
        sobot_btn_submit = findViewById(R.id.sobot_btn_submit);
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        sobot_btn_submit.setOnClickListener(this);
        Drawable d = ResourcesCompat.getDrawable(getResources(),R.drawable.sobot_bg_theme_color_20dp,null);
        sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(d, ThemeUtils.getThemeColor(this)));
        sobot_btn_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        sobot_post_customer_field.setVisibility(View.GONE);

        if (mConfig.isEmailShowFlag()) {
            sobot_post_email_rl.setVisibility(View.VISIBLE);
            sobot_post_email_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sobot_post_email.setVisibility(View.VISIBLE);
                    sobot_post_email_lable.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_second));
                    sobot_post_email_lable.setTextSize(12);

                    sobot_post_email.setFocusable(true);
                    sobot_post_email.setFocusableInTouchMode(true);
                    sobot_post_email.requestFocus();
                    showSoftKeyboard();
                }
            });
        } else {
            sobot_post_email_rl.setVisibility(View.GONE);
        }

        if (mConfig.isTelShowFlag()) {
            sobot_post_phone_rl.setVisibility(View.VISIBLE);
            sobot_post_phone_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sobot_ll_edit_phone.setVisibility(View.VISIBLE);
                    sobot_post_phone_lable.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_second));
                    sobot_post_phone_lable.setTextSize(12);
                    sobot_post_phone.setFocusable(true);
                    sobot_post_phone.setFocusableInTouchMode(true);
                    sobot_post_phone.requestFocus();
                    showSoftKeyboard();
                }
            });
        } else {
            sobot_post_phone_rl.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title_rl.setVisibility(View.VISIBLE);
            sobot_post_title_rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sobot_post_title.setVisibility(View.VISIBLE);
                    sobot_post_title_lable.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_second));
                    sobot_post_title_lable.setTextSize(12);
                    sobot_post_title.setFocusable(true);
                    sobot_post_title.setFocusableInTouchMode(true);
                    sobot_post_title.requestFocus();
                    showSoftKeyboard();
                }
            });
        } else {
            sobot_post_title_rl.setVisibility(View.GONE);
        }


        String sobotUserPhone = (information != null ? information.getUser_tels() : "");
        if (mConfig.isTelShowFlag() && !TextUtils.isEmpty(sobotUserPhone)) {
            sobot_ll_edit_phone.setVisibility(View.VISIBLE);
            sobot_post_phone.setText(sobotUserPhone);
        }
        String sobotUserEmail = (information != null ? information.getUser_emails() : "");
        if (mConfig.isEmailShowFlag() && !TextUtils.isEmpty(sobotUserEmail)) {
            sobot_post_email.setVisibility(View.VISIBLE);
            sobot_post_email.setText(sobotUserEmail);
        }

        if (mConfig.isEnclosureShowFlag()) {
            ll_upload_file.setVisibility(View.VISIBLE);
            initPicListView();
        } else {
            ll_upload_file.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTypeFlag() && mConfig.getType() != null && mConfig.getType().size() > 0) {
            sobot_post_question_ll.setVisibility(View.VISIBLE);
        } else {
            sobot_post_question_ll.setVisibility(View.GONE);
            sobot_post_question_type.setTag(mConfig.getTicketTypeId());
        }

        displayInNotch(sobot_tv_post_msg);
        displayInNotch(sobot_post_email_lable);
        displayInNotch(sobot_post_phone_lable);
        displayInNotch(sobot_post_title_lable);
        displayInNotch(sobot_post_question_type);
        displayInNotch(sobot_post_question_lable);
        displayInNotch(sobot_ll_content_img);
        displayInNotch(sobot_post_email);
        displayInNotch(sobot_post_phone);
        displayInNotch(sobot_post_title);
    }

    @Override
    protected void initData() {
        information = (Information) SharedPreferencesUtil.getObject(getSobotBaseActivity(), "sobot_last_current_info");
        zhiChiApi.getTemplateFieldsInfo(SobotMuItiPostMsgActivty.this, uid, mConfig.getTemplateId(), new StringResultCallBack<SobotLeaveMsgParamModel>() {

            @Override
            public void onSuccess(SobotLeaveMsgParamModel result) {
                if (result != null) {
                    if (result.getField() != null && result.getField().size() != 0) {
                        mFields = result.getField();
                        StCusFieldPresenter.addWorkOrderCusFieldsNew(getSobotBaseActivity(), mFields, sobot_post_customer_field, SobotMuItiPostMsgActivty.this);
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

        msgFilter();
        editTextSetHint();
    }

    /**
     * 提交
     */
    private void setCusFieldValue() {
        //自定义表单校验结果:为空,校验通过,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
        String checkCustomFieldResult = StCusFieldPresenter.formatCusFieldVal(getSobotBaseActivity(), sobot_post_customer_field, mFields);
        if (TextUtils.isEmpty(checkCustomFieldResult)) {
            checkSubmit();
        } else {
            showHint(checkCustomFieldResult);
        }
    }


    private void checkSubmit() {
        String userPhone = "", userEamil = "", title = "";

        if (mConfig.isTicketTitleShowFlag()) {
            if (TextUtils.isEmpty(sobot_post_title.getText().toString().trim())) {
                showHint(getResources().getString(R.string.sobot_title) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
            } else {
                title = sobot_post_title.getText().toString();
            }
        }

        if (sobot_post_question_ll.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(sobot_post_question_type.getText().toString())) {
                showHint(getResources().getString(R.string.sobot_problem_types) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
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
            if (TextUtils.isEmpty(sobot_et_content.getText().toString().trim())) {
                showHint(getResources().getString(R.string.sobot_problem_description) + "  " + getResources().getString(R.string.sobot__is_null));
                return;
            }
        }
        if (mConfig.isEnclosureShowFlag() && mConfig.isEnclosureFlag()) {
            if (TextUtils.isEmpty(getFileStr())) {
                showHint(getResources().getString(R.string.sobot_please_load));
                return;
            }
        }

        if (mConfig.isEmailShowFlag()) {
            if (mConfig.isEmailFlag()) {
                if (TextUtils.isEmpty(sobot_post_email.getText().toString().trim())) {
                    showHint(getResources().getString(R.string.sobot_email_no_empty));
                    return;
                }
                if (!TextUtils.isEmpty(sobot_post_email.getText().toString().trim())
                        && ScreenUtils.isEmail(sobot_post_email.getText().toString().trim())) {
                    userEamil = sobot_post_email.getText().toString().trim();
                } else {
                    showHint(getResources().getString(R.string.sobot_email_dialog_hint));
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(sobot_post_email.getText().toString().trim())) {
                    String emailStr = sobot_post_email.getText().toString().trim();
                    if (ScreenUtils.isEmail(emailStr)) {
                        userEamil = sobot_post_email.getText().toString().trim();
                    } else {
                        showHint(getResources().getString(R.string.sobot_email_dialog_hint));
                        return;
                    }
                }
            }
        }

        if (mConfig.isTelShowFlag()) {
            if (mConfig.isTelFlag()) {
                if (StringUtils.isEmpty(phoneCode)) {
                    showHint(getContext().getResources().getString(R.string.sobot_phone_code_hint));
                    return;
                }
                if (TextUtils.isEmpty(sobot_post_phone.getText().toString().trim())) {
                    showHint(getContext().getResources().getString(R.string.sobot_phone_hint));
                    return;
                }
                userPhone = phoneCode + sobot_post_phone.getText().toString();
            } else {
                String phoneStr = sobot_post_phone.getText().toString().trim();
                if (StringUtils.isNoEmpty(phoneCode) && StringUtils.isEmpty(phoneStr)) {
                    showHint(getContext().getResources().getString(R.string.sobot_phone_hint));
                    return;
                }
                if (!TextUtils.isEmpty(sobot_post_phone.getText().toString().trim())) {

                    userPhone = phoneCode + phoneStr;
                }
            }
        }

        postMsg(userPhone, userEamil, title);
    }

    public void showHint(String content) {
        if (!TextUtils.isEmpty(content)) {
            ToastUtil.showToast(getApplicationContext(), content);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == sobot_post_question_ll) {
            if (mConfig.getType() != null && mConfig.getType().size() != 0) {
                Intent intent = new Intent(getSobotBaseActivity(), SobotPostCategoryActivity.class);
                Bundle bundle = new Bundle();
                ChatUtils.setTypeList( mConfig.getType());
                if (sobot_post_question_type != null &&
                        !TextUtils.isEmpty(sobot_post_question_type.getText().toString()) &&
                        sobot_post_question_type.getTag() != null &&
                        !TextUtils.isEmpty(sobot_post_question_type.getTag().toString())) {
                    bundle.putString("typeName", sobot_post_question_type.getText().toString());
                    bundle.putString("typeId", sobot_post_question_type.getTag().toString());
                }
                intent.putExtra("bundle", bundle);
                startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_category);
            }
        } else if (view == sobot_btn_file) {
            if (pic_list.size() >= 15) {
                //图片上限15张
                ToastUtil.showToast(this, getResources().getString(R.string.sobot_ticket_update_file_max_hite));
            } else {
                menuWindow = new SobotSelectPicDialog(this, itemsOnClick);
                menuWindow.show();
            }
        } else if (R.id.ll_select_code == view.getId()) {
            //选择区号
            Intent intent = new Intent(getSobotBaseActivity(), SobotPhoneCodeDialog.class);
            startActivityForResult(intent, 4001);
        }

        if (view == sobot_btn_submit) {
            setCusFieldValue();
        }
    }

    private void postMsg(String userPhone, String userEamil, String title) {

        final PostParamModel postParam = new PostParamModel();
        postParam.setTemplateId(mConfig.getTemplateId());
        postParam.setPartnerId(information.getPartnerid());
        postParam.setUid(uid);
        postParam.setTicketContent(sobot_et_content.getText().toString());
        postParam.setCustomerEmail(userEamil);
        postParam.setCustomerPhone(userPhone);
        postParam.setTicketTitle(title);
        postParam.setCompanyId(mConfig.getCompanyId());
        postParam.setFileStr(getFileStr());
        postParam.setGroupId(mGroupId);
        postParam.setTicketFrom("21");
        if (sobot_post_question_type.getTag() != null && !TextUtils.isEmpty(sobot_post_question_type.getTag().toString())) {
            postParam.setTicketTypeId(sobot_post_question_type.getTag().toString());
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
        if (information != null && information.getLeaveParamsExtends() != null) {
            postParam.setParamsExtends(SobotJsonUtils.toJson(information.getLeaveParamsExtends()));
        }
        final LinkedHashMap tempMap = new LinkedHashMap();
        if (mConfig.isTicketTitleShowFlag()) {
            tempMap.put(sobot_post_title_lable.getText().toString().replace(" *", ""), StringUtils.isEmpty(title) ? " - -" : title);
        }

        if (mConfig.isTicketTypeFlag() && mConfig.getType() != null && mConfig.getType().size() > 0) {
            tempMap.put(sobot_post_question_lable.getText().toString().replace(" *", ""), StringUtils.isEmpty(sobot_post_question_type.getText().toString()) ? " - -" : sobot_post_question_type.getText().toString());
        }
        if (mFields != null && mFields.size() > 0) {
            Map map = StCusFieldPresenter.getSaveFieldNameAndVal((mFields));
            if (map != null) {
                tempMap.putAll(map);
            }
        }
        if (mConfig.isTicketContentShowFlag()) {
            tempMap.put(getResources().getString(R.string.sobot_problem_description), StringUtils.isEmpty(sobot_et_content.getText().toString()) ? " - -" : sobot_et_content.getText().toString());
        }
        if (mConfig.isEnclosureShowFlag()) {
            tempMap.put(getResources().getString(R.string.sobot_enclosure_string), StringUtils.isEmpty(getFileNameStr()) ? " - -" : getFileNameStr());
        }

        if (mConfig.isEmailShowFlag()) {
            tempMap.put(sobot_post_email_lable.getText().toString().replace(" *", ""), StringUtils.isEmpty(userEamil) ? " - -" : userEamil);
        }
        if (mConfig.isTelShowFlag()) {
            tempMap.put(sobot_post_phone_lable.getText().toString().replace(" *", ""), StringUtils.isEmpty(userPhone) ? " - -" : userPhone);
        }

        zhiChiApi.postMsg(SobotMuItiPostMsgActivty.this, postParam, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase base) {
                try {
                    if (Integer.parseInt(base.getStatus()) == 0) {
                        showHint(base.getMsg());
                    } else if (Integer.parseInt(base.getStatus()) == 1) {
                        if (getSobotBaseActivity() == null) {
                            return;
                        }
                        hideKeyboard();
                        Intent intent = new Intent();
                        intent.setAction(ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_TO_CHATLIST);
                        Bundle bundle = new Bundle();
                        SobotSerializableMap sobotSerializableMap = new SobotSerializableMap();
                        sobotSerializableMap.setMap(tempMap);
                        bundle.putSerializable("leaveMsgData", sobotSerializableMap);
                        bundle.putString("tipMsgId", tipMsgId);
                        intent.putExtras(bundle);
                        CommonUtils.sendLocalBroadcast(getSobotBaseActivity(), intent);
                        if (!TextUtils.isEmpty(tipMsgId)) {
                            final ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseContext(),
                                    ZhiChiConstant.sobot_last_current_initModel);
                            Map map = new HashMap();
                            map.put("uid", initMode.getPartnerid());
                            map.put("cid", initMode.getCid());
                            map.put("msg", getString(R.string.sobot_re_commit) + " <a>" + getString(R.string.sobot_re_write) + "</a>");
                            map.put("msgId", tipMsgId);
                            map.put("deployId", templateId);
                            map.put("updateStatus", 1);//0表示插入 1表示更新
                            zhiChiApi.infoCollection(SobotMuItiPostMsgActivty.this, map, new StringResultCallBack<CommonModel>() {
                                @Override
                                public void onSuccess(CommonModel commonModel) {

                                }

                                @Override
                                public void onFailure(Exception e, String s) {

                                }
                            });
                        }
                        finish();
                    }
                } catch (Exception e) {
                    showHint(base.getMsg());
                    e.printStackTrace();
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
    }


    @Override
    public void onDestroy() {
        HttpUtils.getInstance().cancelTag(getContext());
        MyApplication.getInstance().deleteActivity(this);
        SobotDialogUtils.stopProgressDialog(getSobotBaseActivity());
        super.onDestroy();
    }

    /**
     * 初始化图片选择的控件
     */
    private void initPicListView() {
        adapter = new SobotUploadFileAdapter(getContext(), pic_list, true, new SobotUploadFileAdapter.Listener() {
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
                Intent intent = SobotVideoActivity.newIntent(SobotMuItiPostMsgActivty.this, cacheFile);
                startActivity(intent);
            }

            @Override
            public void deleteFile(final SobotFileModel fileModel) {
                String popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_picture);
                if (fileModel != null) {
                    if (!TextUtils.isEmpty(fileModel.getFileUrl()) && MediaFileUtils.isVideoFileType(fileModel.getFileUrl())) {
                        popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_video);
                    }
                }
                if (seleteMenuWindow != null) {
                    seleteMenuWindow.dismiss();
                    seleteMenuWindow = null;
                }
                if (seleteMenuWindow == null) {
                    seleteMenuWindow = new SobotDeleteWorkOrderDialog(SobotMuItiPostMsgActivty.this, popMsg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seleteMenuWindow.dismiss();
                            if (v.getId() == R.id.btn_pick_photo) {
                                Log.e("onClick: ", seleteMenuWindow.getPosition() + "");
                                pic_list.remove(fileModel);
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
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(SobotMuItiPostMsgActivty.this, fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                Intent intent = new Intent(SobotMuItiPostMsgActivty.this, SobotPhotoActivity.class);
                intent.putExtra("imageUrL", fileUrl);
                startActivity(intent);
            }
        });

        sobot_reply_msg_pic.setAdapter(adapter);
    }

    //对msg过滤
    private void msgFilter() {
        if (information != null && information.getLeaveMsgTemplateContent() != null) {
            sobot_et_content.setHint(Html.fromHtml(information.getLeaveMsgTemplateContent().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>")));
        } else {
            if (!TextUtils.isEmpty(mConfig.getMsgTmp())) {
                mConfig.setMsgTmp(mConfig.getMsgTmp().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"));
                sobot_et_content.setHint(Html.fromHtml(mConfig.getMsgTmp()));
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

        sobot_post_msg_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
    }

    //设置editText的hint提示字体
    private void editTextSetHint() {
        String mustFill = "<font color='#f9676f'>&nbsp;*</font>";

        if (mConfig.isEmailFlag()) {
            sobot_post_email_lable.setText(Html.fromHtml(getResources().getString(R.string.sobot_email) + mustFill));
        } else {
            sobot_post_email_lable.setText(Html.fromHtml(getResources().getString(R.string.sobot_email)));
        }

        if (mConfig.isTelFlag()) {
            sobot_post_phone_lable.setText(Html.fromHtml(getResources().getString(R.string.sobot_phone) + mustFill));
        } else {
            sobot_post_phone_lable.setText(Html.fromHtml(getResources().getString(R.string.sobot_phone)));
        }
        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title_lable.setText(Html.fromHtml(getResources().getString(R.string.sobot_title) + mustFill));
        }

    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotMuItiPostMsgActivty.this, mConfig.getCompanyId(), uid, filePath, new ResultCallBack<ZhiChiMessage>() {
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

    public void addPicView(SobotFileModel item) {
        if (sobot_reply_msg_pic.getVisibility() == View.GONE) {
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        }
        pic_list.add(item);
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
                                        ToastUtil.showToast(getContext(), getResources().getString(R.string.sobot_file_upload_failed));
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
                        SobotDialogUtils.startProgressDialog(SobotMuItiPostMsgActivty.this);
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(SobotMuItiPostMsgActivty.this);
                        ChatUtils.sendPicByFilePath(SobotMuItiPostMsgActivty.this, tmpPic.getAbsolutePath(), sendFileListener, true);
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
                            sobot_post_question_type.setText(typeName);
                            sobot_post_question_type.setTag(typeId);
                            sobot_post_question_type.setVisibility(View.VISIBLE);
                            sobot_post_question_lable.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_second));
                            sobot_post_question_lable.setTextSize(12);
                        }
                    }
                    break;
                case 4001:
                    //区号
                    phoneCode = data.getStringExtra("selectCode");
                    sobot_tv_phone_code.setText(phoneCode);
                    break;
                default:
                    break;
            }
        }
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
    public void onClickCusField(TextView view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
        if (cusField == null) return;
        final SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
        switch (fieldConfig.getFieldType()) {
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE:
                StCusFieldPresenter.openTimePicker(getSobotBaseActivity(), null, fieldConfig);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE:
                StCusFieldPresenter.startSobotCusFieldActivity(getSobotBaseActivity(), null, cusField);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE:
                if (cusField.getCusFieldDataInfoList() != null && cusField.getCusFieldDataInfoList().size() > 0) {
                    Intent intent = new Intent(getContext(), SobotPostCascadeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cusField", cusField);
                    bundle.putSerializable("fieldId", cusField.getCusFieldConfig().getFieldId());
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, ZhiChiConstant.work_order_list_display_type_category);
                }
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE:
                if (cusFieldConfig != null) {
                    Intent intent = new Intent(SobotMuItiPostMsgActivty.this, SobotPostRegionActivity.class);
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
        Intent intent = new Intent(this, SobotPhoneCodeDialog.class);
        startActivityForResult(intent, 4001);
    }

    private ArrayList<SobotTimezone> list;
    private int requestCount = 0;//请求的次数
    @Override
    public void selectLeftOnclick(TextView view , SobotCusFieldConfig fieldConfig) {
        //时区
        if (list == null || list.size() == 0) {
            requestZone(true,fieldConfig);
        } else {
            showZoneDialog(fieldConfig);
        }
    }

    @Override
    public void selectRightOnclick(TextView view , SobotCusFieldConfig fieldConfig) {
        //时间
        if (fieldConfig != null) {
            Intent intent = new Intent(getSobotBaseActivity(), SobotTimeZoneActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("cusFieldConfig", fieldConfig);
            intent.putExtras(bundle);
            startActivityForResult(intent, fieldConfig.getFieldType());
        }
    }
    /**
     * 请求时区
     * @param showDialog
     */
    private void requestZone(final boolean showDialog,SobotCusFieldConfig fieldConfig) {
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
                requestZone(showDialog,fieldConfig);
            }
        });
    }

    private void showZoneDialog(SobotCusFieldConfig fieldConfig) {
        Intent intent = new Intent(this, SobotZoneActivity.class);
        intent.putExtra("cusFieldConfig", fieldConfig);
        intent.putExtra("zoneList", list);
        startActivityForResult(intent, fieldConfig.getFieldType());
    }

    @Override
    public void onBackPressed() {
        infoCollection();
        super.onBackPressed();
    }

    //关闭按钮关闭界面和点击空白区域关闭界面,插入系统消息
    private void infoCollection() {
        if (TextUtils.isEmpty(tipMsgId)) {
            final ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseContext(),
                    ZhiChiConstant.sobot_last_current_initModel);
            Map map = new HashMap();
            map.put("uid", initMode.getPartnerid());
            map.put("cid", initMode.getCid());
            map.put("msg", getString(R.string.sobot_re_commit) + " <a>" + getString(R.string.sobot_re_write) + "</a>");
            String msgId = UUID.randomUUID().toString();
            if (!TextUtils.isEmpty(msgId)) {
                msgId = msgId.replace("-", "") + System.currentTimeMillis();
            } else {
                msgId = System.currentTimeMillis() + "";
            }
            map.put("msgId", msgId);
            map.put("deployId", templateId);
            map.put("updateStatus", 0);
            Intent intent = new Intent();
            intent.setAction(ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_TO_CHATLIST);
            intent.putExtra("msgId", msgId);
            intent.putExtra("deployId", templateId);
            intent.putExtra("msg", getString(R.string.sobot_re_commit) + " <a>" + getString(R.string.sobot_re_write) + "</a>");
            CommonUtils.sendLocalBroadcast(getSobotBaseActivity(), intent);
            zhiChiApi.infoCollection(SobotMuItiPostMsgActivty.this, map, new StringResultCallBack<CommonModel>() {
                @Override
                public void onSuccess(CommonModel commonModel) {

                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }
}
