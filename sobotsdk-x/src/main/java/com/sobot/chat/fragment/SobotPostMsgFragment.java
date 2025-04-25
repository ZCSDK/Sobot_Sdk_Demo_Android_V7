package com.sobot.chat.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.halfdialog.SobotPostCategoryActivity;
import com.sobot.chat.activity.SobotPostMsgActivity;
import com.sobot.chat.activity.halfdialog.SobotPostRegionActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.halfdialog.SobotPostCascadeActivity;
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
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.presenter.StCusFieldPresenter;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotJsonUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.activity.halfdialog.SobotPhoneCodeDialog;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.utils.SobotStringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 留言界面
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotPostMsgFragment extends SobotChatBaseFragment implements View.OnClickListener, ISobotCusField {
    private View mRootView;

    private EditText sobot_post_email, sobot_et_content, sobot_post_phone, sobot_post_title;
    private TextView sobot_tv_post_msg, sobot_post_email_lable, sobot_post_phone_lable, sobot_post_lable, sobot_post_title_lable, sobot_post_question_type, sobot_post_question_lable, sobot_tv_problem_description;
    private TextView sobot_btn_submit;
    private LinearLayout sobot_post_customer_field;
    private LinearLayout sobot_post_email_rl, sobot_post_phone_rl, sobot_post_title_rl,sobot_ll_content_img;
    private RelativeLayout sobot_post_question_ll;
    private TextView sobot_tv_phone_code;//手机区号
    private LinearLayout ll_upload_file;//上传附件
    private TextView sobot_btn_file, sobot_file_hite;//上传按钮
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

    private SobotLeaveMsgConfig mConfig;
    private Information information;
    private String uid = "";
    private String mGroupId = "";
    private boolean flag_exit_sdk;

    private int flag_exit_type = -1;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    if (flag_exit_type == 1) {
                        finishPageOrSDK(true);
                    } else if (flag_exit_type == 2) {
                        getSobotActivity().setResult(200);
                        finishPageOrSDK(false);
                    } else {
                        finishPageOrSDK(flag_exit_sdk);
                    }
                    break;
            }
        }
    };

    public static SobotPostMsgFragment newInstance(Bundle data) {
        Bundle arguments = new Bundle();
        arguments.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, data);
        SobotPostMsgFragment fragment = new SobotPostMsgFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments().getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            if (bundle != null) {
                uid = bundle.getString(StPostMsgPresenter.INTENT_KEY_UID);
                mGroupId = bundle.getString(StPostMsgPresenter.INTENT_KEY_GROUPID);
                flag_exit_type = bundle.getInt(ZhiChiConstant.FLAG_EXIT_TYPE, -1);
                flag_exit_sdk = bundle.getBoolean(ZhiChiConstant.FLAG_EXIT_SDK, false);
                mConfig = (SobotLeaveMsgConfig) bundle.getSerializable(StPostMsgPresenter.INTENT_KEY_CONFIG);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.sobot_fragment_post_msg, container, false);
        initView(mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        initData();
        super.onActivityCreated(savedInstanceState);
    }

    protected void initView(View rootView) {
        ll_upload_file = rootView.findViewById(R.id.ll_upload_file);
        sobot_btn_file = rootView.findViewById(R.id.sobot_btn_file);
        sobot_btn_file.setOnClickListener(this);
        sobot_file_hite = rootView.findViewById(R.id.sobot_file_hite);
        String hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        sobot_file_hite.setText(String.format(hideTxt, "15", "50M"));
        sobot_reply_msg_pic = rootView.findViewById(R.id.sobot_reply_msg_pic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        sobot_reply_msg_pic.setLayoutManager(layoutManager);
        sobot_tv_phone_code = rootView.findViewById(R.id.sobot_tv_phone_code);
        rootView.findViewById(R.id.ll_select_code).setOnClickListener(this);
        sobot_post_phone = (EditText) rootView.findViewById(R.id.sobot_post_phone);
        sobot_post_email = (EditText) rootView.findViewById(R.id.sobot_post_email);
        sobot_post_title = (EditText) rootView.findViewById(R.id.sobot_post_title);
        sobot_et_content = (EditText) rootView.findViewById(R.id.sobot_post_et_content);
        sobot_ll_content_img =  rootView.findViewById(R.id.sobot_ll_content_img);
        sobot_tv_post_msg = (TextView) rootView.findViewById(R.id.sobot_tv_post_msg);
        sobot_post_email_lable = (TextView) rootView.findViewById(R.id.sobot_post_email_lable);
        sobot_post_phone_lable = (TextView) rootView.findViewById(R.id.sobot_post_phone_lable);
        sobot_post_title_lable = (TextView) rootView.findViewById(R.id.sobot_post_title_lable);
        sobot_post_lable = (TextView) rootView.findViewById(R.id.sobot_post_question_lable);
        String test = "<font color='#f9676f'>*&nbsp;</font>" + getContext().getResources().getString(R.string.sobot_problem_types);
        sobot_post_lable.setText(Html.fromHtml(test));
        sobot_post_question_lable = (TextView) rootView.findViewById(R.id.sobot_post_question_lable);
        sobot_post_question_type = (TextView) rootView.findViewById(R.id.sobot_post_question_type);
        sobot_post_customer_field = (LinearLayout) rootView.findViewById(R.id.sobot_post_customer_field);
        sobot_post_email_rl = rootView.findViewById(R.id.sobot_post_email_rl);
        sobot_post_phone_rl = rootView.findViewById(R.id.sobot_post_phone_rl);
        sobot_post_title_rl = rootView.findViewById(R.id.sobot_post_title_rl);
        sobot_post_question_ll = rootView.findViewById(R.id.sobot_post_question_ll);
        sobot_post_question_ll.setOnClickListener(this);
        sobot_tv_problem_description = rootView.findViewById(R.id.sobot_tv_problem_description);

        if (mConfig.isTicketContentShowFlag()) {
            String desText = getResources().getString(R.string.sobot_problem_description);

            //问题描述是否显示
            sobot_ll_content_img.setVisibility(View.VISIBLE);
            //问题描述是否必填
            if (mConfig.isTicketContentFillFlag()) {
                desText = "<font color='#f9676f'>*&nbsp;</font>" + desText;
            }
            sobot_tv_problem_description.setText(Html.fromHtml(desText));
        } else {
            sobot_ll_content_img.setVisibility(View.GONE);
        }
        sobot_btn_submit = rootView.findViewById(R.id.sobot_btn_submit);
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        if (ThemeUtils.isChangedThemeColor(getSobotActivity())) {
            Drawable bg = getResources().getDrawable(R.drawable.sobot_normal_btn_bg);
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, ThemeUtils.getThemeColor(getSobotActivity())));
            }
        }
        sobot_btn_submit.setOnClickListener(this);
        sobot_post_customer_field.setVisibility(View.GONE);

        if (mConfig.isEmailShowFlag()) {
            sobot_post_email_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_email_rl.setVisibility(View.GONE);
        }

        if (mConfig.isTelShowFlag()) {
            sobot_post_phone_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_phone_rl.setVisibility(View.GONE);
        }

        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title_rl.setVisibility(View.VISIBLE);
        } else {
            sobot_post_title_rl.setVisibility(View.GONE);
        }


        String sobotUserPhone = (information != null ? information.getUser_tels() : "");
        if (mConfig.isTelShowFlag() && !TextUtils.isEmpty(sobotUserPhone)) {
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
    }

    protected void initData() {
        information = (Information) SharedPreferencesUtil.getObject(getSobotActivity(), "sobot_last_current_info");
        zhiChiApi.getTemplateFieldsInfo(SobotPostMsgFragment.this, uid, mConfig.getTemplateId(), new StringResultCallBack<SobotLeaveMsgParamModel>() {

            @Override
            public void onSuccess(SobotLeaveMsgParamModel result) {
                if (result != null) {
                    if (result.getField() != null && result.getField().size() != 0) {
                        mFields = result.getField();
                        StCusFieldPresenter.addWorkOrderCusFields(getSobotActivity(), SobotPostMsgFragment.this.getSobotActivity(), mFields, sobot_post_customer_field, SobotPostMsgFragment.this);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                try {
                    showHint(getContext().getResources().getString(R.string.sobot_try_again));
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
        String checkCustomFieldResult = StCusFieldPresenter.formatCusFieldVal(getSobotActivity(), sobot_post_customer_field, mFields);
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
                showHint(getContext().getResources().getString(R.string.sobot_title) + "  " + getContext().getResources().getString(R.string.sobot__is_null));
                return;
            } else {
                title = sobot_post_title.getText().toString();
            }
        }

        if (sobot_post_question_ll.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(sobot_post_question_type.getText().toString())) {
                showHint(getContext().getResources().getString(R.string.sobot_problem_types) + "  " + getContext().getResources().getString(R.string.sobot__is_null));
                return;
            }
        }

        if (mFields != null && mFields.size() != 0) {
            for (int i = 0; i < mFields.size(); i++) {
                if (1 == mFields.get(i).getCusFieldConfig().getFillFlag()) {
                    if (TextUtils.isEmpty(mFields.get(i).getCusFieldConfig().getValue())) {
                        showHint(mFields.get(i).getCusFieldConfig().getFieldName() + "  " + getContext().getResources().getString(R.string.sobot__is_null));
                        return;
                    }
                }
            }
        }
        if (mConfig.isTicketContentShowFlag() && mConfig.isTicketContentFillFlag()) {
            //问题描述 显示 必填才校验
            if (TextUtils.isEmpty(sobot_et_content.getText().toString().trim())) {
                showHint(getContext().getResources().getString(R.string.sobot_problem_description) + "  " + getContext().getResources().getString(R.string.sobot__is_null));
                return;
            }
        }

        if (mConfig.isEnclosureShowFlag() && mConfig.isEnclosureFlag()) {
            if (TextUtils.isEmpty(getFileStr())) {
                showHint(getContext().getResources().getString(R.string.sobot_please_load));
                return;
            }
        }

        if (mConfig.isEmailShowFlag()) {
            if (mConfig.isEmailFlag()) {
                if (TextUtils.isEmpty(sobot_post_email.getText().toString().trim())) {
                    showHint(getContext().getResources().getString(R.string.sobot_email_no_empty));
                    return;
                }
                if (!TextUtils.isEmpty(sobot_post_email.getText().toString().trim())
                        && ScreenUtils.isEmail(sobot_post_email.getText().toString().trim())) {
                    userEamil = sobot_post_email.getText().toString().trim();
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_email_dialog_hint));
                    return;
                }
            } else {
                if (!TextUtils.isEmpty(sobot_post_email.getText().toString().trim())) {
                    String emailStr = sobot_post_email.getText().toString().trim();
                    if (ScreenUtils.isEmail(emailStr)) {
                        userEamil = sobot_post_email.getText().toString().trim();
                    } else {
                        showHint(getContext().getResources().getString(R.string.sobot_email_dialog_hint));
                        return;
                    }
                }
            }
        }

        if (mConfig.isTelShowFlag()) {
            if (mConfig.isTelFlag()) {
                if (SobotStringUtils.isEmpty(phoneCode)) {
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
                if (SobotStringUtils.isNoEmpty(phoneCode) && SobotStringUtils.isEmpty(phoneStr)) {
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
            CustomToast.makeText(getSobotActivity(), content, 1000).show();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == sobot_post_question_ll) {
            if (mConfig.getType() != null && mConfig.getType().size() != 0) {
                Intent intent = new Intent(getSobotActivity(), SobotPostCategoryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("types", mConfig.getType());
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
        } else if (R.id.ll_select_code == view.getId()) {
            //选择区号
            Intent intent = new Intent(getSobotActivity(), SobotPhoneCodeDialog.class);
            startActivityForResult(intent, 4001);
        } else if (view == sobot_btn_file) {
            if(pic_list.size()>=15){
                //图片上限15张
                ToastUtil.showToast(getSobotActivity(), getResources().getString(R.string.sobot_ticket_update_file_max_hite));
            }else {
                menuWindow = new SobotSelectPicDialog(getSobotActivity(), itemsOnClick);
                menuWindow.show();
            }
        } else if (view == sobot_btn_submit) {
            setCusFieldValue();
        }
    }

    public void onBackPressed() {
        if (flag_exit_type == 1 || flag_exit_type == 2) {
            finishPageOrSDK(false);
        } else {
            finishPageOrSDK(flag_exit_sdk);
        }
    }

    private void postMsg(String userPhone, String userEamil, String title) {
        PostParamModel postParam = new PostParamModel();
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
        postParam.setTicketFrom("4");
        if (information != null && information.getLeaveParamsExtends() != null) {
            postParam.setParamsExtends(SobotJsonUtils.toJson(information.getLeaveParamsExtends()));
        }
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

        zhiChiApi.postMsg(SobotPostMsgFragment.this, postParam, new StringResultCallBack<CommonModelBase>() {
            @Override
            public void onSuccess(CommonModelBase base) {
                try {
                    if (Integer.parseInt(base.getStatus()) == 0) {
                        showHint(base.getMsg());
                    } else if (Integer.parseInt(base.getStatus()) == 1) {
                        if (getSobotActivity() == null) {
                            return;
                        }
                        KeyboardUtil.hideKeyboard(getSobotActivity().getCurrentFocus());
                        Intent intent = new Intent();
                        intent.setAction(SobotPostMsgActivity.SOBOT_ACTION_SHOW_COMPLETED_VIEW);
                        CommonUtils.sendLocalBroadcast(getSobotActivity(), intent);
                    }
                } catch (Exception e) {
                    showHint(base.getMsg());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                try {
                    showHint(getContext().getResources().getString(R.string.sobot_try_again));
                } catch (Exception e1) {

                }
            }
        });
    }

    /**
     * 返回键监听
     */
    public void onBackPress() {
        if (getView() != null && getView().getContext() != null) {
            KeyboardUtil.hideKeyboard(((ViewGroup) getView()).getFocusedChild());
        }

        if (flag_exit_type == 1 || flag_exit_type == 2) {
            finishPageOrSDK(false);
        } else {
            finishPageOrSDK(flag_exit_sdk);
        }
    }

    private void finishPageOrSDK(boolean flag) {
        if (!flag) {
            if (getSobotActivity() == null) {
                Activity tempActivity = MyApplication.getInstance().getLastActivity();
                if (tempActivity != null && tempActivity instanceof SobotPostMsgActivity) {
                    tempActivity.finish();
                    tempActivity.overridePendingTransition(R.anim.sobot_push_right_in,
                            R.anim.sobot_push_right_out);
                }
            } else {
                getSobotActivity().finish();
                getSobotActivity().overridePendingTransition(R.anim.sobot_push_right_in,
                        R.anim.sobot_push_right_out);
            }
        } else {
            MyApplication.getInstance().exit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        HttpUtils.getInstance().cancelTag(getContext());
        SobotDialogUtils.stopProgressDialog(getSobotActivity());
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
//                KeyboardUtil.hideKeyboard(sobotReplyEdit);
                File file = new File(fileModel.getFileUrl());
                SobotCacheFile cacheFile = new SobotCacheFile();
                cacheFile.setFileName(file.getName());
                cacheFile.setUrl(fileModel.getFileUrl());
                cacheFile.setFilePath(fileModel.getFileUrl());
                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(fileModel.getFileUrl())));
                cacheFile.setMsgId("" + System.currentTimeMillis());
                Intent intent = SobotVideoActivity.newIntent(getActivity(), cacheFile);
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
                    seleteMenuWindow = new SobotDeleteWorkOrderDialog(getSobotActivity(), popMsg, new View.OnClickListener() {
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
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(getSobotActivity(), fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                Intent intent = new Intent(getSobotActivity(), SobotPhotoActivity.class);
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
            HtmlTools.getInstance(getSobotActivity().getApplicationContext()).setRichText(sobot_tv_post_msg, information.getLeaveMsgGuideContent().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"),
                    R.color.sobot_postMsg_url_color);
        } else {
            if (!TextUtils.isEmpty(mConfig.getMsgTxt())) {
                mConfig.setMsgTxt(mConfig.getMsgTxt().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>"));
                HtmlTools.getInstance(getSobotActivity().getApplicationContext()).setRichText(sobot_tv_post_msg, mConfig.getMsgTxt(),
                        R.color.sobot_postMsg_url_color);
            } else {
                sobot_tv_post_msg.setVisibility(View.GONE);
            }
        }

        sobot_tv_post_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(sobot_tv_post_msg);
            }
        });
    }

    //设置editText的hint提示字体
    private void editTextSetHint() {
        String mustFill = "<font color='#f9676f'>*&nbsp;</font>";

        if (mConfig.isEmailFlag()) {
            sobot_post_email_lable.setText(Html.fromHtml(mustFill + getContext().getResources().getString(R.string.sobot_email)));
        } else {
            sobot_post_email_lable.setText(Html.fromHtml(getContext().getResources().getString(R.string.sobot_email)));
        }

        if (mConfig.isTelFlag()) {
            sobot_post_phone_lable.setText(Html.fromHtml(mustFill + getContext().getResources().getString(R.string.sobot_phone)));
        } else {
            sobot_post_phone_lable.setText(Html.fromHtml(getContext().getResources().getString(R.string.sobot_phone)));
        }
        if (mConfig.isTicketTitleShowFlag()) {
            sobot_post_title_lable.setText(Html.fromHtml( mustFill+getContext().getResources().getString(R.string.sobot_title) ));
        }

    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotPostMsgFragment.this, mConfig.getCompanyId(), uid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(getSobotActivity());
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
                    SobotDialogUtils.stopProgressDialog(getSobotActivity());
                    showHint(TextUtils.isEmpty(des) ? getResources().getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(getSobotActivity());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, getSobotActivity());
                    }
                    String path = ImageUtils.getPath(getSobotActivity(), selectedImage);
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
                                SobotDialogUtils.startProgressDialog(getSobotActivity());
//                            ChatUtils.sendPicByFilePath(getSobotActivity(),path,sendFileListener,false);
                                String fName = MD5Util.encode(path);
                                String filePath = null;
                                try {
                                    filePath = FileUtil.saveImageFile(getSobotActivity(), selectedImage, fName + FileUtil.getFileEndWith(path), path);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtil.showToast(getSobotActivity(), getContext().getResources().getString(R.string.sobot_pic_type_error));
                                    return;
                                }
                                sendFileListener.onSuccess(filePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            SobotDialogUtils.startProgressDialog(getSobotActivity());
                            ChatUtils.sendPicByUriPost(getSobotActivity(), selectedImage, sendFileListener, false);
                        }
                    } else {
                        showHint(getContext().getResources().getString(R.string.sobot_did_not_get_picture_path));
                    }
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(getSobotActivity());
                    ChatUtils.sendPicByFilePath(getSobotActivity(), cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_pic_select_again));
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(getSobotActivity());
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(getSobotActivity());
                        ChatUtils.sendPicByFilePath(getSobotActivity(), tmpPic.getAbsolutePath(), sendFileListener, true);
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                }
            }
        }
        StCusFieldPresenter.onStCusFieldActivityResult(getSobotActivity(), data, mFields, sobot_post_customer_field);
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
    public void onClickCusField(View view, SobotCusFieldConfig fieldConfig, SobotFieldModel cusField) {
        if (cusField == null) return;
        final SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
        switch (fieldConfig.getFieldType()) {
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE:
                //时间或日期
                StCusFieldPresenter.openTimePicker(getSobotActivity(), SobotPostMsgFragment.this, fieldConfig);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE:
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE:
                StCusFieldPresenter.startSobotCusFieldActivity(getSobotActivity(), SobotPostMsgFragment.this, cusField);
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE:
                if (cusField.getCusFieldDataInfoList() != null && cusField.getCusFieldDataInfoList().size() > 0) {
                    Intent intent = new Intent(getSobotActivity(), SobotPostCascadeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cusField", cusField);
                    bundle.putSerializable("fieldId", cusField.getCusFieldConfig().getFieldId());
                    intent.putExtra("bundle", bundle);
                    startActivityForResult(intent, ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE);
                }
                break;
            case ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE:
                if (cusFieldConfig != null) {
                    Intent intent = new Intent(getSobotActivity(), SobotPostRegionActivity.class);
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
                    Intent intent = new Intent(getSobotActivity(), SobotTimeZoneActivity.class);
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
}
