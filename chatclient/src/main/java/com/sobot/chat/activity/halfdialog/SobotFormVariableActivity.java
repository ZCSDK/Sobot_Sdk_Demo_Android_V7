package com.sobot.chat.activity.halfdialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotOptionModel;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.api.model.SobotVariableModel;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.dialog.SobotSelectDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 变量收集
 */
public class SobotFormVariableActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private TextView sobot_tv_title;
    private ArrayList<SobotVariableModel> list;//数据
    private LinearLayout ll_list;
    private ScrollView sobot_scroll_v;
    private TextView btnSubmit;
    private SobotRobot robot;
    //广播
    private SobotFormVariableBroadcast receiver;
    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_form_info;
    }

    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            btnSubmit.setClickable(false);
            btnSubmit.setEnabled(false);
            btnSubmit.getBackground().setAlpha(102);
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
        robot = (SobotRobot) getIntent().getSerializableExtra("sobotRobot");
        if(robot!=null) {
            list = robot.getFormSubmitInfos();
            if (list == null) {
                list = new ArrayList<>();
            }
            addList();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }


    @Override
    protected void initView() {
        super.initView();
        ChatUtils.isOpenVariableDialog = true;
        if (receiver == null) {
            receiver = new SobotFormVariableBroadcast();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_SUCCESS);
        filter.addAction(ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_ERROR);
        filter.addAction(ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_FAIL);
        // 注册广播接收器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }

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
        sobot_tv_title.setText(R.string.sobot_variable_title);
        ll_list = findViewById(R.id.ll_list);
        ll_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏软键盘并清除所有输入框的焦点
                hideAllEditTextFocus();
            }
        });
        btnSubmit = findViewById(R.id.btnSubmit);
        findViewById(R.id.tv_nodata).setVisibility(View.GONE);
        btnSubmit.setText(getContext().getResources().getString(R.string.sobot_start_chat));
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



    /**
     *  获取变量的值
     * @return 是否有空值
     */
    private ArrayList<SobotVariableModel> getvalue() {
        for (int i = 0; i < list.size(); i++) {
            SobotInputView view = ll_list.findViewWithTag(list.get(i).getVariableId());
            String value = view.getValue();
            if (StringUtils.isNoEmpty(value)) {
                list.get(i).setVariableValue(value);
                view.hideError();
            }
        }
        return list;
    }
    public void submit() {
        ArrayList<SobotVariableModel> list = getvalue();
        robot.setFormSubmitInfos(list);
        //提交
        Intent intent = new Intent();
        intent.setAction(ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_FORM);
        intent.putExtra("robot", robot);
        sendBroadcast(intent);
    }

    /**
     * 显示数据
     *
     */
    private void addList() {
        ll_list.setVisibility(View.VISIBLE);
        ll_list.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            SobotInputView inputView = new SobotInputView(getContext());
            inputView.setTag(list.get(i).getVariableId());
            inputView.setTitle(list.get(i).getVariableName(), false);
            if (list.get(i).getVariableType().equals("CHARACTER")) {
                //单行文本
                inputView.setInputType("single_line");
            } else if (list.get(i).getVariableType().equals("NUMBER")) {
                //数字
                inputView.setViweType("number");
            } else if (list.get(i).getVariableType().equals("ENUMERATION")) {
                //单选
                inputView.setTitle(list.get(i).getVariableName(), false);
                inputView.setInputType("select");
                SobotVariableModel variableModel = list.get(i);
                List<SobotOptionModel> list1;
                String lan = SharedPreferencesUtil.getStringData(SobotApp.getApplicationContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh-Hans");
                if (lan.equals("zh")) {
                    lan = "zh-Hans";
                }
                Map<String, List<SobotOptionModel>> map = variableModel.getEnumListMap();
                if(null!=map) {
                    if (map.containsKey(lan) && null != map.get(lan)) {
                        list1=map.get(lan);
                    } else {
                        // 获取第一个key对应的list
                        String firstKey = variableModel.getEnumListMap().keySet().iterator().next();
                        list1=variableModel.getEnumListMap().get(firstKey);
                    }
                } else {
                    list1 = new ArrayList<>();
                }
                inputView.getLlSelectOne().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SobotSelectDialog selectDialog = new SobotSelectDialog(SobotFormVariableActivity.this, variableModel.getVariableName(), list1, new SobotSelectDialog.OnSelectListener() {
                            @Override
                            public void onSelect(SobotOptionModel optionModel) {
                                inputView.getTvSelect().setText(optionModel.getLabel());
                                inputView.hideError();
                            }

                        });
                        selectDialog.show();
                    }
                });
            }
            if(StringUtils.isNoEmpty(list.get(i).getVariableValue())){
                inputView.setInputValue(list.get(i).getVariableValue());
            }
            if(StringUtils.isNoEmpty(list.get(i).getErrorMsg())){
                inputView.showError(list.get(i).getErrorMsg());
            }else{
                inputView.hideError();
            }
            ll_list.addView(inputView);
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
    class SobotFormVariableBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LogUtils.i("广播是  :" + intent.getAction());
                if (ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_SUCCESS.equals(intent.getAction())) {
                    //关闭对话框
//                    Intent robotIntent = new Intent();
//                    robot.setCheckFormSubmitOver(true);
//                    robotIntent.setAction(ZhiChiConstants.SOBOT_SWICH_ROBOT);
//                    robotIntent.putExtra("sobotRobot", robot);
//                    sendBroadcast(robotIntent);
                    finish();
                }else if (ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_ERROR.equals(intent.getAction())){
                    finish();
                }else if (ZhiChiConstants.SOBOT_SUBMIT_VAIABLE_FAIL.equals(intent.getAction())){
                    btnSubmit.setClickable(true);
                    btnSubmit.setEnabled(true);
                    btnSubmit.getBackground().setAlpha(255);
                    list = (ArrayList<SobotVariableModel>) intent.getSerializableExtra("formInfoModels");
                    addList();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        ChatUtils.isOpenVariableDialog = false;
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
