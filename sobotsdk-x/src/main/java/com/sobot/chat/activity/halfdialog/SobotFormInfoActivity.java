package com.sobot.chat.activity.halfdialog;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.utils.SobotStringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 新版询前表单
 */
public class SobotFormInfoActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private List<FormNodeInfo> allData;//数据
    private List<FormNodeInfo> datas;//数据
    private List<FormNodeRelInfo> relationshipList;//数据关系
    private FormInfoModel formInfoModel;//原数据
    private LinearLayout ll_list;
    private ScrollView sobot_scroll_v;
    private TextView tv_start_tip, tv_permission_tip, btnSubmit, tv_nodata;
    private String formExplain="";////表单说明
    private String cid,uid,schemeId;//
    private SobotConnCusParam param;//用于返回后转人工
    private SobotTransferOperatorParam tparam;//用于返回后转人工

    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            //提交
            submit();
        }
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
                if (allData.size() > 0 && SobotStringUtils.isNoEmpty(allData.get(0).getTips())) {
                    tv_start_tip.setText(allData.get(0).getTips());
                    //第一个节点是开始
                    showStartData(allData.get(0).getId());
                }
            }

        }
        //获取多语言的的隐私引导语
        zhiChiApi.queryFormConfig(this, uid, new StringResultCallBack<SobotQueryFormModel>() {
            @Override
            public void onSuccess(SobotQueryFormModel sobotQueryFormModel) {
                if(sobotQueryFormModel!=null && SobotStringUtils.isNoEmpty(sobotQueryFormModel.getFormSafety())) {
                    formExplain = sobotQueryFormModel.getFormSafety();
                }
                if (SobotStringUtils.isNoEmpty(formExplain)) {
                    tv_permission_tip.setVisibility(View.VISIBLE);
                    tv_permission_tip.setText(formExplain);
                } else {
                    tv_permission_tip.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                if (SobotStringUtils.isNoEmpty(formExplain)) {
                    tv_permission_tip.setVisibility(View.VISIBLE);
                    tv_permission_tip.setText(formExplain);
                } else {
                    tv_permission_tip.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_form_info;
    }

    @Override
    protected void initView() {
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        sobot_scroll_v = findViewById(R.id.sobot_scroll_v);
        ll_list = findViewById(R.id.ll_list);
        tv_start_tip = findViewById(R.id.tv_start_tip);
        tv_permission_tip = findViewById(R.id.tv_permission_tip);
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
    }

    private void showList() {
        tv_nodata.setVisibility(View.GONE);
        ll_list.setVisibility(View.VISIBLE);
        ll_list.removeAllViews();
        addList(datas);

    }



    private void delectList(String pid){
        int index =0;
        for (int i = 0; i < ll_list.getChildCount(); i++) {
            View view1 = ll_list.getChildAt(i);
            if(view1.getTag().toString().equals(pid)){
                index=i;
            }
        }
        for (int i = ll_list.getChildCount()-1; i > index; i--) {
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
                        if(allData.get(j).getNodeType()==1) {
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

    private void showSelectData(String id,String fieldDataId) {
        //从第一个节点开始，至选择类型的结束
        List<FormNodeInfo> tmpDatas = new ArrayList<>();
        for (int i = 0; i < relationshipList.size(); i++) {
            if ((!id.equals("-1") && relationshipList.get(i).getPreNodeId().equals(id))||(!fieldDataId.equals("-1") && relationshipList.get(i).getFieldDataId() != null && relationshipList.get(i).getFieldDataId().equals(fieldDataId))) {
                String xiageid = relationshipList.get(i).getNextNodeId();
                for (int j = 0; j < allData.size(); j++) {
                    if (allData.get(j).getId().equals(xiageid)) {
                        if(allData.get(j).getNodeType()==1) {
                            tmpDatas.add(allData.get(j));
                            datas.add(allData.get(j));
                        }
                        if (allData.get(j).getFieldType() == 8) {
                            addList(tmpDatas);
                            return;
                        } else {
                            showSelectData(allData.get(j).getId(),"-1");
                            break;
                        }
                    }
                }
            }
        }
        addList(tmpDatas);

    }

    private FormNodeInfo selectNode;

    private void showDialog(FormNodeInfo nodeInfo) {
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
        intent.putExtra("title", nodeInfo.getName());
        startActivityForResult(intent, 30005);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 30005 && data != null) {
            final FormNodeInfo formNodeInfo = (FormNodeInfo) data.getSerializableExtra("select");
            if (formNodeInfo != null && SobotStringUtils.isNoEmpty(formNodeInfo.getName())) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        View view = ll_list.findViewWithTag(selectNode.getId());
                        //删除选项之后的view
                        delectList(selectNode.getId());
                        //找到下个节点的线
                        if (view != null) {
                            TextView tv_value = view.findViewById(R.id.work_order_customer_field_text_single);
                            tv_value.setText(formNodeInfo.getName());//
                            tv_value.setTag(formNodeInfo);
                        }
                        showSelectData("-1",formNodeInfo.getId());

                    }
                });
            }
        }
    }

    public void submit(){
        List<FormNodeInfo> submitData = new ArrayList<>();
        for (int i = 0; i < ll_list.getChildCount(); i++) {
            FormNodeInfo info = new FormNodeInfo();
            View view  = ll_list.getChildAt(i);
            TextView lable = view.findViewById(R.id.work_order_customer_field_text_lable);
            FormNodeInfo oldInfo = (FormNodeInfo) lable.getTag();
            info.setId(oldInfo.getId());
            info.setName(oldInfo.getName());
            info.setFieldId(oldInfo.getFieldId());
            info.setFieldName(oldInfo.getFieldName());
            info.setFieldType(oldInfo.getFieldType());
            info.setFieldFrom(oldInfo.getFieldFrom());
            if(view.findViewById(R.id.work_order_customer_field_text_single) instanceof TextView){
                //选择
                TextView tv=view.findViewById(R.id.work_order_customer_field_text_single);
                FormNodeInfo value = (FormNodeInfo) tv.getTag();
                if(value!=null) {
                    info.setFieldValues(value.getName());
                    info.setFieldId(value.getId());
                }else{
                    String valueTxt = tv.getText().toString();
                    info.setFieldValues(valueTxt);
                }
            }
            String value = info.getFieldValues();
            if(SobotStringUtils.isNoEmpty(value)){
                submitData.add(info);
            }else{
                //都是必填
                ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                return;
            }
            if(oldInfo.getFieldFrom()==12 && oldInfo.getFieldVariable()!=null){
                ///固定字段校验内容是否合符标准
                String match = "";
                if(oldInfo.getFieldVariable().equals("uname")) {
                    match = "^.+$";
                }else if(oldInfo.getFieldVariable().equals("source")){
                    match = "^.+$";
                }else if(oldInfo.getFieldVariable().equals("tel")){
                    match = "^[0-9+,]{3,16}$";
                }else if(oldInfo.getFieldVariable().equals("email")){
                    match = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
                }else if(oldInfo.getFieldVariable().equals("qq")){
                    match = "^[1-9][0-9]{4,14}$";
                }else if(oldInfo.getFieldVariable().equals("wx")){
                    match = "^[a-zA-Z]{1}[-_a-zA-Z0-9]{5,19}$";
                }
                Pattern p = Pattern.compile(match);
                Matcher m = p.matcher(info.getFieldValues());
                if(SobotStringUtils.isNoEmpty(match) && !m.matches()) {
                    ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                    return;
                }
            }else if(oldInfo.getFieldFrom()==22 && oldInfo.getFieldVariable()!=null){

                String match = "";
                if(oldInfo.getFieldVariable().equals("enterpriseName")) {
                    match = "^.+$";
                }else if(oldInfo.getFieldVariable().equals("enterpriseDomain")) {
                    match = "^.+$";
                }
                Pattern p = Pattern.compile(match);
                Matcher m = p.matcher(info.getFieldValues());
                if(SobotStringUtils.isNoEmpty(match) && !m.matches()) {
                    ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                    return;
                }
            }else{
                //限制方式  1禁止输入空格   2 禁止输入小数点  3 小数点后只允许2位  4 禁止输入特殊字符  5只允许输入数字 6最多允许输入字符  7判断邮箱格式  8判断手机格式  9 请输入 3～16 位数字、英文符号, +
                String LimitOptions = oldInfo.getLimitOptions();
                String LimitChar = oldInfo.getLimitChar();
                if ( LimitOptions.contains("1")) {
                    if (value.contains(" ")) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if ( LimitOptions.contains("2")) {
                    if (value.contains(".")) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if (LimitOptions.contains("3")) {
                    if (!StringUtils.isNumber(value) && value.length()<=2) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if ( LimitOptions.contains("4")) {
                    String regex = "^[a-zA-Z0-9\u4E00-\u9FA5]+$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if ( LimitOptions.contains("5")) {
                    String regex = "[0-9]*";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if ( LimitOptions.contains("7")) {
                    if (!ScreenUtils.isEmail(value)) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if (LimitOptions.contains("8")) {
                    if (!ScreenUtils.isMobileNO(value)) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if ( LimitOptions.contains("9")) {
                    String regex = "^[A-Za-z0-9+]{3,16}$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher match = pattern.matcher(value);
                    boolean b = match.matches();
                    if (!b) {
                        ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                        return;
                    }
                }
                if (!StringUtils.isEmpty(LimitChar) && value.length()>Integer.parseInt(LimitChar)) {
                    ToastUtil.showToast(getContext(), oldInfo.getErrorTips());
                    return;
                }
            }
        }

        zhiChiApi.submitFormInfo(getContext(), cid, uid,schemeId, formInfoModel.getId(),submitData, new StringResultCallBack<FormInfoModel>() {
            @Override
            public void onSuccess(FormInfoModel formInfoModel) {
                Intent intent = new Intent();
                intent.putExtra("param", param);
                intent.putExtra("tparam", tparam);
                setResult(ZhiChiConstant.REQUEST_COCE_TO_FORMINFO,intent);
                finish();
            }

            @Override
            public void onFailure(Exception e, String s) {
                finish();
            }
        });
    }
    private void addList(List<FormNodeInfo> tmpDatas) {
        tv_nodata.setVisibility(View.GONE);
        ll_list.setVisibility(View.VISIBLE);
        for (int i = 0; i < tmpDatas.size(); i++) {
            View v;
            final FormNodeInfo nodeInfo = tmpDatas.get(i);
            if (nodeInfo.getFieldType() == 8) {
                v = LayoutInflater.from(this).inflate(R.layout.sobot_item_form_info_select, ll_list, false);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        KeyboardUtil.hideKeyboard(SobotFormInfoActivity.this.getCurrentFocus());
                        //显示对话框
                        showDialog(nodeInfo);
                    }
                });
                TextView value = v.findViewById(R.id.work_order_customer_field_text_single);
                if (SobotStringUtils.isNoEmpty(nodeInfo.getTips())) {
                    value.setHint(nodeInfo.getTips());
                }
            } else {
                v = LayoutInflater.from(this).inflate(R.layout.sobot_item_form_info_text, ll_list, false);
                EditText value = v.findViewById(R.id.work_order_customer_field_text_single);
                if (SobotStringUtils.isNoEmpty(nodeInfo.getTips())) {
                    value.setHint(nodeInfo.getTips());
                }
                //类型
                if (nodeInfo.getLimitOptions().contains("5")) {
                    value.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                }
                if (nodeInfo.getLimitOptions().contains("7")) {
                    value.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                }
                if (nodeInfo.getLimitOptions().contains("8")) {
                    value.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                }
                //不允许输入换行
                value.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        return (keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER);
                    }
                });
            }
            v.setTag(nodeInfo.getId());
            TextView lable = v.findViewById(R.id.work_order_customer_field_text_lable);
            lable.setText(nodeInfo.getFieldName());
            lable.setTag(nodeInfo);
            ll_list.addView(v);
        }
    }
}
