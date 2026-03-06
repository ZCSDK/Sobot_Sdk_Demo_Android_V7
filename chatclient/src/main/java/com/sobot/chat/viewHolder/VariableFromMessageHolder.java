package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.api.model.SobotOptionModel;
import com.sobot.chat.api.model.SobotVariableModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.dialog.SobotSelectDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 变量收集页面
 */
public class VariableFromMessageHolder extends MsgHolderBase implements View.OnTouchListener {
    private LinearLayout sobot_ll_variable;
    private TextView sobot_submit;//提交

    public ZhiChiMessageBase message;
    private boolean changeThemeColor;

    public VariableFromMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_ll_variable = convertView.findViewById(R.id.ll_content);
        sobot_submit = convertView.findViewById(R.id.sobot_submit);
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            sobot_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(mContext));
            Drawable drawable =
                    ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_btn_bg_28, null);
            if (drawable != null) {
                sobot_submit.setBackground(ThemeUtils.applyColorToDrawable(drawable, ThemeUtils.getThemeColor(mContext)));
            }
        }
    }

    /**
     *
     */

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        resetMaxWidth();
        this.message = message;
        List<SobotVariableModel> list = message.getVariableModels();
        if (null != list) {
            sobot_ll_variable.removeAllViews();
            if (message.isCheckFormSubmitOver()) {
                //已收集完毕，回显
                sobot_submit.setVisibility(View.GONE);
                for (int i = 0; i < list.size(); i++) {
                    View view = View.inflate(context, R.layout.sobot_item_input_show, null);
                    TextView title = view.findViewById(R.id.sobot_title_lable);
                    title.setText(list.get(i).getVariableName());
                    TextView value = view.findViewById(R.id.sobot_value);
                    value.setText(list.get(i).getVariableValue());
                    sobot_ll_variable.addView(view);
                }
            } else {
                //未收集完毕，显示变量收集
                sobot_submit.setVisibility(View.VISIBLE);
                sobot_ll_variable.removeAllViews();
                for (int i = 0; i < list.size(); i++) {
                    SobotInputView inputView = new SobotInputView(context);
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
                        String lan = SharedPreferencesUtil.getStringData(SobotApp.getApplicationContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh-Hans");
                        if (lan.equals("zh")) {
                            lan = "zh-Hans";
                        }
                        List<SobotOptionModel> list1;
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
                                v.requestFocus();
                                SobotSelectDialog selectDialog = new SobotSelectDialog(context, variableModel.getVariableName(), list1, new SobotSelectDialog.OnSelectListener() {
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
                    sobot_ll_variable.addView(inputView);
                }
            }
            if(this.message.isVariableSubmiting()){
                sobot_submit.setClickable(false);
                sobot_submit.setEnabled(false);
                sobot_submit.getBackground().setAlpha(102);
            }else{
                sobot_submit.setClickable(true);
                sobot_submit.setEnabled(true);
                sobot_submit.getBackground().setAlpha(255);
            }
            sobot_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    msgCallBack.variableFrom(null, getvalue());
                    message.setVariableSubmiting(true);
                    sobot_submit.setClickable(false);
                    sobot_submit.setEnabled(false);
                    sobot_submit.getBackground().setAlpha(102);

                }
            });
        }
    }

    /**
     *  获取变量的值
     * @return 是否有空值
     */
    private List<SobotVariableModel> getvalue() {
        message.setVariableSubmiting(true);
        List<SobotVariableModel> list = message.getVariableModels();
        for (int i = 0; i < list.size(); i++) {
            SobotInputView view = sobot_ll_variable.findViewWithTag(list.get(i).getVariableId());
            String value = view.getValue();
            if (StringUtils.isNoEmpty(value)) {
                list.get(i).setVariableValue(value);
                list.get(i).setErrorMsg("");
                view.hideError();
            }
        }
        return list;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        if (view == ed_describe) {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_MOVE:
//                    view.getParent().requestDisallowInterceptTouchEvent(true);
//                    break;
//            }
//        }
        return false;
    }

}