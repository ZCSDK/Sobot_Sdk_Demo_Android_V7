package com.sobot.chat.activity.halfdialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.api.model.FormInfoModel;
import com.sobot.chat.api.model.FormNodeInfo;
import com.sobot.chat.api.model.FormNodeRelInfo;
import com.sobot.chat.api.model.SobotConnCusParam;
import com.sobot.chat.api.model.SobotQueryFormModel;
import com.sobot.chat.api.model.SobotTransferOperatorParam;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.network.http.callback.StringResultCallBack;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 新版--询前表单
 */
public class SobotFormInfoActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private TextView sobot_tv_title;
    private List<FormNodeInfo> allData;//数据
    private List<FormNodeInfo> datas;//数据
    private List<FormNodeRelInfo> relationshipList;//数据关系
    private FormInfoModel formInfoModel;//原数据
    private LinearLayout ll_list;
    private TextView topView,bottomView;
    private ScrollView sobot_scroll_v;
    private TextView btnSubmit, tv_nodata;
    private String formExplain = "";
    /// /表单说明
    private String cid, uid, schemeId;//
    private SobotConnCusParam param;//用于返回后转人工
    private SobotTransferOperatorParam tparam;//用于返回后转人工
    private boolean isInit;//是否是进入会话的询前表单

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_form_info;
    }

    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            View view = getCurrentFocus();
            if (view != null) {
                // 失去焦点
                view.clearFocus();
            }
            //提交
            submit();
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotFormInfoActivity";
    }

    @Override
    protected void initData() {
        formInfoModel = (FormInfoModel) getIntent().getSerializableExtra("formInfoModels");
        param = (SobotConnCusParam) getIntent().getSerializableExtra("param");
        tparam = (SobotTransferOperatorParam) getIntent().getSerializableExtra("tparam");
        formExplain = getIntent().getStringExtra("FormExplain");
        cid = getIntent().getStringExtra("cid");
        uid = getIntent().getStringExtra("uid");
        schemeId = getIntent().getStringExtra("schemeId");
        isInit = getIntent().getBooleanExtra("isInit", false);

        if (formInfoModel != null) {
            allData = new ArrayList<>();
            datas = new ArrayList<>();
            relationshipList = new ArrayList<>();
            relationshipList.addAll(formInfoModel.getFormNodeRelRespVos());
            if (formInfoModel.getFormNodeRespVos() != null && formInfoModel.getFormNodeRespVos().size() > 0) {
                for (int i = 0; i < formInfoModel.getFormNodeRespVos().size(); i++) {
                    if (formInfoModel.getFormNodeRespVos().get(i).getStatus() == 0) {
                        allData.add(formInfoModel.getFormNodeRespVos().get(i));
                    }
                }
                if (allData.size() > 0 && StringUtils.isNoEmpty(allData.get(0).getTips())) {
                    topView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sobot_from_info_top, null);
                    topView.setText(allData.get(0).getTips());
                    //第一个节点是开始
                    showStartData(allData.get(0).getId());
                }
            }

        }
        //隐私引导语不为空
        if (StringUtils.isNoEmpty(formExplain)) {
            //获取多语言的的隐私引导语
            zhiChiApi.queryFormConfig(this, uid, new StringResultCallBack<SobotQueryFormModel>() {
                @Override
                public void onSuccess(SobotQueryFormModel sobotQueryFormModel) {
                    if (sobotQueryFormModel != null && StringUtils.isNoEmpty(sobotQueryFormModel.getFormSafety())) {
                        formExplain = sobotQueryFormModel.getFormSafety();
                    }
                    if (StringUtils.isNoEmpty(formExplain)) {
                        bottomView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sobot_from_info_top, null);
                        bottomView.setText(formExplain);
                        bottomView.setTextColor(getResources().getColor(R.color.sobot_color_text_third));
                    }
                }

                @Override
                public void onFailure(Exception e, String s) {
                    if (StringUtils.isNoEmpty(formExplain)) {
                        bottomView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.sobot_from_info_top, null);
                        bottomView.setText(formExplain);
                        bottomView.setTextColor(getResources().getColor(R.color.sobot_color_text_third));
                    }
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInit) {
            //进入会话时
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getY() <= 0) {
                    finish();
                }
            }
        }
        return true;
    }


    @Override
    protected void initView() {
        super.initView();
        coustom_pop_layout = findViewById(R.id.sobot_container);
        coustom_pop_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // 隐藏软键盘并清除所有输入框的焦点
                hideAllEditTextFocus();
            }
        });
        sobot_scroll_v = findViewById(R.id.sobot_scroll_v);
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_from_title);
        ll_list = findViewById(R.id.ll_list);
        ll_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏软键盘并清除所有输入框的焦点
                hideAllEditTextFocus();
            }
        });
//        tv_permission_tip = findViewById(R.id.tv_permission_tip);
        btnSubmit = findViewById(R.id.btnSubmit);
        tv_nodata = findViewById(R.id.tv_nodata);
        btnSubmit.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sobot_scroll_v.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    // 当ScrollView向上滚动并且软键盘可见时隐藏软键盘
                    if (scrollY > oldScrollY) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null && imm.isActive()) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    }
                }
            });
        }
        //根据主题色更改背景色
        if (btnSubmit != null && ThemeUtils.isChangedThemeColor(this)) {
            int themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = btnSubmit.getBackground();
            if (bg != null) {
                btnSubmit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
            btnSubmit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        }
    }

    private void showList() {
        tv_nodata.setVisibility(View.GONE);
        ll_list.setVisibility(View.VISIBLE);
        ll_list.removeAllViews();
        if(topView !=null){
            ll_list.addView(topView,0);
        }
        if(bottomView !=null){
            ll_list.addView(bottomView);
        }
        addList(datas);

    }


    private void delectList(String pid) {
        int index = 0;
        for (int i = 0; i < ll_list.getChildCount(); i++) {
            View view1 = ll_list.getChildAt(i);
            if (view1.getTag()!=null && view1.getTag().toString().equals(pid)) {
                index = i;
            }
        }
        for (int i = ll_list.getChildCount() - 1; i > index; i--) {
            ll_list.removeViewAt(i);
        }
    }

    private void showStartData(String pid) {
        //从第一个节点开始，至选择类型的结束
        for (int i = 0; i < relationshipList.size(); i++) {
            if (relationshipList.get(i).getPreNodeId().equals(pid)) {
                String xiageid = relationshipList.get(i).getNextNodeId();
                for (int j = 0; j < allData.size(); j++) {

                    if (allData.get(j).getId().equals(xiageid)) {
                        if (allData.get(j).getNodeType() == 1) {
                            datas.add(allData.get(j));
                        }
                        if (allData.get(j).getFieldType() == 8) {
                            showList();
                            return;
                        } else {
                            showStartData(allData.get(j).getId());
                            break;
                        }
                    }
                }
            }
        }
        showList();

    }

    private void showSelectData(String id, String fieldDataId) {
        //从第一个节点开始，至选择类型的结束
        List<FormNodeInfo> tmpDatas = new ArrayList<>();
        for (int i = 0; i < relationshipList.size(); i++) {
            if ((!id.equals("-1") && relationshipList.get(i).getPreNodeId().equals(id)) || (!fieldDataId.equals("-1") && relationshipList.get(i).getFieldDataId() != null && relationshipList.get(i).getFieldDataId().equals(fieldDataId))) {
                String xiageid = relationshipList.get(i).getNextNodeId();
                for (int j = 0; j < allData.size(); j++) {
                    if (allData.get(j).getId().equals(xiageid)) {
                        if (allData.get(j).getNodeType() == 1) {
                            tmpDatas.add(allData.get(j));
                            datas.add(allData.get(j));
                        }
                        if (allData.get(j).getFieldType() == 8) {
                            addList(tmpDatas);
                            return;
                        } else {
                            showSelectData(allData.get(j).getId(), "-1");
                            break;
                        }
                    }
                }
            }
        }
        addList(tmpDatas);

    }

    private FormNodeInfo selectNode;

    private void showDialog(FormNodeInfo nodeInfo, String defualtValue) {
        selectNode = nodeInfo;
        ArrayList<FormNodeInfo> list = new ArrayList();
        try {
            JSONArray arrayid = new JSONArray(nodeInfo.getFieldDataIds());
            JSONArray arrayvalue = new JSONArray(nodeInfo.getFieldDataValues());
            if (arrayid != null && arrayvalue != null && arrayid.length() == arrayvalue.length()) {
                for (int i = 0; i < arrayid.length(); i++) {
                    FormNodeInfo info = new FormNodeInfo();
                    info.setId(arrayid.getString(i));
                    info.setName(arrayvalue.getString(i));
                    list.add(info);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Intent intent = new Intent(this, SobotFromSearchDialog.class);
        intent.putExtra("List", list);
        intent.putExtra("defualtValue", defualtValue);
        intent.putExtra("type", nodeInfo.getFieldType());
        intent.putExtra("title", nodeInfo.getName());
        startActivityForResult(intent, 30005);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 30005 && data != null) {
            final FormNodeInfo formNodeInfo = (FormNodeInfo) data.getSerializableExtra("select");
            if (formNodeInfo != null && StringUtils.isNoEmpty(formNodeInfo.getName())) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        SobotInputView view = ll_list.findViewWithTag(selectNode.getId());
                        //删除选项之后的view
                        delectList(selectNode.getId());
                        if(bottomView!=null){
                            ll_list.addView(bottomView);
                        }
                        //找到下个节点的线
                        if (view != null) {
                            view.setInputValue(formNodeInfo.getName());//
                            view.getTvTitle().setTag(formNodeInfo);
                        }
                        showSelectData("-1", formNodeInfo.getId());

                    }
                });
            }
        }
    }

    public void submit() {
        List<FormNodeInfo> submitData = new ArrayList<>();
        for (int i = 0; i < ll_list.getChildCount(); i++) {
            FormNodeInfo info = new FormNodeInfo();
            SobotInputView view = (SobotInputView) ll_list.getChildAt(i);
            FormNodeInfo oldInfo = (FormNodeInfo) view.getTvTitle().getTag();
            info.setId(oldInfo.getId());
            info.setName(oldInfo.getName());
            info.setFieldId(oldInfo.getFieldId());
            info.setFieldName(oldInfo.getFieldName());
            info.setFieldType(oldInfo.getFieldType());
            info.setFieldFrom(oldInfo.getFieldFrom());
            if (oldInfo.getFieldType() == 8) {
                //选择
                info.setFieldValues(view.getSelectValue());
                FormNodeInfo value = (FormNodeInfo) view.getTvTitle().getTag();
                if (value != null) {
                    info.setFieldId(value.getId());
                }
            } else {
                info.setFieldValues(view.getSingleValue());
            }
            String value = info.getFieldValues();
            String errorStr = "";
            if (StringUtils.isNoEmpty(value)) {
                submitData.add(info);
            } else {
                //都是必填
                errorStr = oldInfo.getErrorTips();
                view.showError(errorStr);
                return;
            }
            if (oldInfo.getFieldFrom() == 12 && oldInfo.getFieldVariable() != null) {
                ///固定字段校验内容是否合符标准
                String match = "";
                if (oldInfo.getFieldVariable().equals("uname")) {
                    match = "^.+$";
                } else if (oldInfo.getFieldVariable().equals("source")) {
                    match = "^.+$";
                } else if (oldInfo.getFieldVariable().equals("tel")) {
                    match = "^[0-9+,]{3,16}$";
                } else if (oldInfo.getFieldVariable().equals("email")) {
                    match = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
                } else if (oldInfo.getFieldVariable().equals("qq")) {
                    match = "^[1-9][0-9]{4,14}$";
                } else if (oldInfo.getFieldVariable().equals("wx")) {
                    match = "^[a-zA-Z]{1}[-_a-zA-Z0-9]{5,19}$";
                }
                Pattern p = Pattern.compile(match);
                Matcher m = p.matcher(info.getFieldValues());
                if (StringUtils.isNoEmpty(match) && !m.matches()) {
                    errorStr = oldInfo.getErrorTips();
                }
            } else if (oldInfo.getFieldFrom() == 22 && oldInfo.getFieldVariable() != null) {

                String match = "";
                if (oldInfo.getFieldVariable().equals("enterpriseName")) {
                    match = "^.+$";
                } else if (oldInfo.getFieldVariable().equals("enterpriseDomain")) {
                    match = "^.+$";
                }
                Pattern p = Pattern.compile(match);
                Matcher m = p.matcher(info.getFieldValues());
                if (StringUtils.isNoEmpty(match) && !m.matches()) {
                    errorStr = oldInfo.getErrorTips();
                }
            } else if (StringUtils.isNoEmpty(oldInfo.getLimitOptions())) {
                //限制方式  1禁止输入空格   2 禁止输入小数点  3 小数点后只允许2位  4 禁止输入特殊字符  5只允许输入数字 6最多允许输入字符  7判断邮箱格式  8判断手机格式  9 请输入 3～16 位数字、英文符号, +
                String LimitOptions = oldInfo.getLimitOptions();
                String LimitChar = oldInfo.getLimitChar();
                if (LimitOptions.contains("1")) {
                    if (value.contains(" ")) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("2")) {
                    if (value.contains(".")) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("3")) {
                    if (!StringUtils.isNumber(value) && value.length() <= 2) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("4")) {
                    String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("5")) {
                    String regex = "[0-9]*";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("7")) {
                    if (!ScreenUtils.isEmail(value)) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("8")) {
                    if (!ScreenUtils.isMobileNO(value)) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (LimitOptions.contains("9")) {
                    String regex = "^[A-Za-z0-9+]{3,16}$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        errorStr = oldInfo.getErrorTips();
                    }
                }
                if (!StringUtils.isEmpty(LimitChar) && value.length() > Integer.parseInt(LimitChar)) {
                    errorStr = oldInfo.getErrorTips();
                }
            }
            if (StringUtils.isNoEmpty(errorStr)) {
                view.showError(errorStr);
                return;
            } else {
                view.hideError();
            }
        }

        zhiChiApi.submitFormInfo(getContext(), cid, uid, schemeId, formInfoModel.getId(), submitData, new StringResultCallBack<FormInfoModel>() {
            @Override
            public void onSuccess(FormInfoModel formInfoModel) {
                Intent intent = new Intent();
                intent.putExtra("isInit", isInit);
                intent.putExtra("param", param);
                intent.putExtra("tparam", tparam);
                setResult(ZhiChiConstant.REQUEST_COCE_TO_FORMINFO, intent);
                finish();
            }

            @Override
            public void onFailure(Exception e, String s) {
                finish();
            }
        });
    }

    /**
     * 新版UI
     *
     * @param tmpDatas
     */
    private void addList(List<FormNodeInfo> tmpDatas) {
        tv_nodata.setVisibility(View.GONE);
        ll_list.setVisibility(View.VISIBLE);
        for (int i = 0; i < tmpDatas.size(); i++) {
            final FormNodeInfo nodeInfo = tmpDatas.get(i);
            SobotInputView v = new SobotInputView(this);
            v.setTag(nodeInfo.getId());
            v.getTvTitle().setTag(nodeInfo);
            v.setTitle(nodeInfo.getFieldName(), true);
            if (StringUtils.isNoEmpty(nodeInfo.getTips())) {
                v.setInputHint(nodeInfo.getTips());
            }
            if (nodeInfo.getFieldType() == 8) {
                v.setInputType("select");
                //单选
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboard();
                        //显示对话框
                        showDialog(nodeInfo, v.getSelectValue());
                    }
                });
            } else {
                //输入框
                v.setInputType("single_line");
                //类型
                if (nodeInfo.getLimitOptions().contains("5")) {
                    v.setViweType("number");
                }
                if (nodeInfo.getLimitOptions().contains("7")) {
                    v.setViweType("email");
                }
                if (nodeInfo.getLimitOptions().contains("8")) {
                    v.setViweType("phone");
                }
            }
            if(bottomView!=null){
                ll_list.addView(v,ll_list.getChildCount()-1);
            }else {
                ll_list.addView(v);
            }
        }
    }
    /**b
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
            if (ll_list != null) {
                for (int i = 0; i < ll_list.getChildCount(); i++) {
                    View child = ll_list.getChildAt(i);
                    if (child instanceof SobotInputView) {
                        child.clearFocus();
                    }
                }
            }
        } catch (Exception e) {
        }
    }

}
