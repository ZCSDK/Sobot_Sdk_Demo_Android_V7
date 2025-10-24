package com.sobot.chat.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.halfdialog.SobotChooseCityActivity;
import com.sobot.chat.activity.halfdialog.SobotCusFieldActivity;
import com.sobot.chat.activity.halfdialog.SobotDateTimeActivity;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.api.model.SobotProvinInfo;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotInputView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by jinxl on 2018/1/3.
 */
public class StCusFieldPresenter {

    /**
     * 获取要提交给接口的自定义字段的json
     * 留言接口使用
     *
     * @param field
     * @return
     */
    public static String getSaveFieldVal(ArrayList<SobotFieldModel> field) {
        List<Map<String, String>> listModel = null;
        if (field != null && field.size() > 0) {
            listModel = new ArrayList<>();
            for (int i = 0; i < field.size(); i++) {
                Map<String, String> model = new HashMap<>();
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null && !StringUtils.isEmpty(cusFieldConfig.getFieldId())
                        && !StringUtils.isEmpty(cusFieldConfig.getValue())) {
                    model.put("id", field.get(i).getCusFieldConfig().getFieldId());
                    model.put("value", field.get(i).getCusFieldConfig().getValue());
                    if (cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE) {
                        model.put("text", field.get(i).getCusFieldConfig().getText());
                    } else {
                        model.put("text", field.get(i).getCusFieldConfig().getShowName());
                    }
                    listModel.add(model);
                }
            }
        }
        if (listModel != null && listModel.size() > 0) {
            JSONArray jsonArray = new JSONArray(listModel);//把  List 对象  转成json数据
            return jsonArray.toString();
        }
        return null;
    }


    /**
     * 获取要提交给接口的自定义字段的json
     * 留言接口使用
     *
     * @param field
     * @return
     */
    public static Map getSaveFieldNameAndVal(ArrayList<SobotFieldModel> field) {
        if (field != null && field.size() > 0) {
            Map<String, String> model = new HashMap<>();
            for (int i = 0; i < field.size(); i++) {
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null) {
                    model.put(field.get(i).getCusFieldConfig().getFieldName(), TextUtils.isEmpty(field.get(i).getCusFieldConfig().getShowName()) ? field.get(i).getCusFieldConfig().getValue() : field.get(i).getCusFieldConfig().getShowName());
                }
            }
            return model;
        }
        return null;
    }

    /**
     * 打开时间或日期选择器的逻辑
     *
     * @param act
     */
    public static void openTimePicker(Activity act, Fragment fragment, SobotCusFieldConfig cusFieldConfig) {
//        TextView textClick = (TextView) view.findViewById(R.id.work_order_customer_date_text_click);
//        String content = textClick.getText().toString();
        Intent intent = new Intent(act, SobotDateTimeActivity.class);
        intent.putExtra("cusFieldConfig", cusFieldConfig);
        if (fragment != null) {
            fragment.startActivityForResult(intent, cusFieldConfig.getFieldType());
        } else {
            act.startActivityForResult(intent, cusFieldConfig.getFieldType());
        }
    }

    /**
     * 获取要提交给接口的自定义字段的json
     * 询前表单使用
     *
     * @param field
     * @return
     */
    public static String getCusFieldVal(ArrayList<SobotFieldModel> field, final SobotProvinInfo.SobotProvinceModel finalData) {
        Map<String, String> tmpMap = new HashMap<>();
        if (field != null && field.size() > 0) {
            for (int i = 0; i < field.size(); i++) {
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                if (cusFieldConfig != null && !StringUtils.isEmpty(cusFieldConfig.getFieldId())
                        && !StringUtils.isEmpty(cusFieldConfig.getValue())) {
                    tmpMap.put(field.get(i).getCusFieldConfig().getFieldId(), field.get(i).getCusFieldConfig().getValue());
                }
            }
        }
        if (finalData != null) {
            tmpMap.put("proviceId", finalData.provinceId);
            tmpMap.put("proviceName", finalData.provinceName);
            tmpMap.put("cityId", finalData.cityId);
            tmpMap.put("cityName", finalData.cityName);
            tmpMap.put("areaId", finalData.areaId);
            tmpMap.put("areaName", finalData.areaName);
        }
        if (tmpMap.size() > 0) {
            return GsonUtil.map2Json(tmpMap);
        }
        return null;
    }

    /**
     * 启动自定义字段下一级选择的逻辑
     *
     * @param act
     * @param cusFieldList
     */
    public static void startSobotCusFieldActivity(Activity act, SobotFieldModel cusFieldList) {
        startSobotCusFieldActivity(act, null, cusFieldList);
    }

    /**
     * 启动自定义字段下一级选择的逻辑
     *
     * @param act
     * @param cusFieldList
     */
    public static void startSobotCusFieldActivity(Activity act, Fragment fragment, SobotFieldModel cusFieldList) {
        SobotCusFieldConfig cusFieldConfig = cusFieldList.getCusFieldConfig();
        Intent intent = new Intent(act, SobotCusFieldActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("fieldType", cusFieldConfig.getFieldType());
        bundle.putSerializable("cusFieldConfig", cusFieldConfig);
        bundle.putSerializable("cusFieldList", cusFieldList);
        intent.putExtra("bundle", bundle);
        if (fragment != null) {
            fragment.startActivityForResult(intent, cusFieldConfig.getFieldType());
        } else {
            act.startActivityForResult(intent, cusFieldConfig.getFieldType());
        }
    }

    /**
     * 启动城市选择的act
     *
     * @param act
     * @param info     省的信息
     * @param cusField 字段的信息
     */
    public static void startChooseCityAct(Activity act, SobotProvinInfo info, SobotFieldModel cusField) {
        Intent intent = new Intent(act, SobotChooseCityActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("cusFieldConfig", cusField.getCusFieldConfig());
        bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_PROVININFO, info);
        SobotCusFieldConfig cusFieldConfig = cusField.getCusFieldConfig();
        if (cusFieldConfig != null) {
            bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_FIELD_ID, cusFieldConfig.getFieldId());
        }
        intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA, bundle);
        act.startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CITY_INFO);
    }

    /**
     * 选择子集的回调
     *
     * @param context
     * @param data
     * @param field
     * @param post_customer_field
     */
    public static void onStCusFieldActivityResult(Context context, Intent data, ArrayList<SobotFieldModel> field, ViewGroup post_customer_field) {
        if (data != null && "CATEGORYSMALL".equals(data.getStringExtra("CATEGORYSMALL")) && -1 != data.getIntExtra("fieldType", -1)) {
            String value = data.getStringExtra("category_typeName");
            String id = data.getStringExtra("category_fieldId");
            if ("null".equals(id) || TextUtils.isEmpty(id)) {
                return;
            }
            String dataValue = data.getStringExtra("category_typeValue");
            if (field != null && !StringUtils.isEmpty(value) && !StringUtils.isEmpty(dataValue)) {
                for (int i = 0; i < field.size(); i++) {
                    SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                    if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                        model.setChecked(true);
                        model.setValue(dataValue);
                        model.setId(id);
                        model.setShowName(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                        SobotInputView view = post_customer_field.findViewWithTag(model.getFieldId());
                        view.setInputValue(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                    }
                }
            } else {
                //还原样式
                SobotInputView view = post_customer_field.findViewWithTag(id);
                if (view != null) {
                    view.setInputValue(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                }
                if (StringUtils.isEmpty(dataValue)) {
                    for (int i = 0; i < field.size(); i++) {
                        //清空上次选中
                        SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                        if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                            model.setChecked(false);
                            model.setValue(dataValue);
                            model.setId(id);

                        }
                    }
                }


            }
        }
    }

    /**
     * 提交前将数据同步到最外层属性中
     *
     * @param sobot_container
     * @param field
     * @return String 自定义表单校验结果:为空,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
     */
    public static String formatCusFieldVal(Context context, ViewGroup sobot_container, List<SobotFieldModel> field) {
        String errorStr ="";
        if (field != null && field.size() != 0) {
            for (int j = 0; j < field.size(); j++) {
                if (field.get(j).getCusFieldConfig() == null) {
                    continue;
                }
                SobotInputView view = sobot_container.findViewWithTag(field.get(j).getCusFieldConfig().getFieldId());

                if (view != null) {
                    if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SINGLE_LINE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        String content = view.getSingleValue();
                        field.get(j).getCusFieldConfig().setValue(content);
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("7")) {
                            if (!ScreenUtils.isEmail(content)) {
                                errorStr = field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_email_dialog_hint);
                            }
                        }
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("8")) {
                            if (!ScreenUtils.isMobileNO(content)) {
                                errorStr =  field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_phone) + context.getResources().getString(R.string.sobot_input_type_err);
                            }
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        field.get(j).getCusFieldConfig().setValue(view.getManyValue());
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == field.get(j).getCusFieldConfig().getFieldType()
                            || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        field.get(j).getCusFieldConfig().setValue(view.getSelectValue());
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                        field.get(j).getCusFieldConfig().setValue(view.getSingleValue());
                        if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("3")) {
                            if (!StringUtils.isNumber(view.getSingleValue())) {
                                errorStr = field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_input_type_err);
                            }
                        }
                    }
                    if(StringUtils.isNoEmpty(errorStr)){
                        view.showError(errorStr);
                    }else{
                        view.hideError();
                    }
                }
            }
        }
        return errorStr;
    }

    public static void displayInNotch(Activity activity, final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(activity, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            view.setPadding((rect.right > 110 ? 110 : rect.right), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                        }
                    }
                }
            });

        }
    }
    //新版 自定义字段
    public static void addWorkOrderCusFieldsNew(final Context context, final ArrayList<SobotFieldModel> cusFieldList, ViewGroup containerLayout, final ISobotCusField callBack) {
        if (containerLayout != null) {
            containerLayout.setVisibility(View.VISIBLE);
            containerLayout.removeAllViews();
            if (cusFieldList != null && cusFieldList.size() != 0) {
                for (int i = 0; i < cusFieldList.size(); i++) {
                    final SobotFieldModel model = cusFieldList.get(i);
                    final SobotCusFieldConfig cusFieldConfig = model.getCusFieldConfig();
                   if (cusFieldConfig == null) {
                        continue;
                    }
                    SobotInputView view = new SobotInputView(context);
                    view.setTag(cusFieldConfig.getFieldId());
                    view.setCusFieldConfig(cusFieldConfig);
                    view.setCusFields(model);
                    view.setCusCallBack(callBack);
                    //设置标题
                    view.setTitle(cusFieldConfig.getFieldName(),1 == cusFieldConfig.getFillFlag());
                    if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SINGLE_LINE_TYPE == cusFieldConfig.getFieldType()||ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == cusFieldConfig.getFieldType()) {
                        //单行文本
                        view.setInputType("single_line");

                        //限制方式  1禁止输入空格   2 禁止输入小数点  3 小数点后只允许2位  4 禁止输入特殊字符  5只允许输入数字 6最多允许输入字符  7判断邮箱格式  8判断手机格式
                        if (!StringUtils.isEmpty(cusFieldConfig.getLimitOptions())) {
                            if (cusFieldConfig.getLimitOptions().contains("5")) {
                                view.setViweType("number");
                            }
                            if (cusFieldConfig.getLimitOptions().contains("7")) {
                                view.setViweType("email");
                            }
                            if (cusFieldConfig.getLimitOptions().contains("8")) {
                                view.setViweType("phone");
                            }
                            if (cusFieldConfig.getLimitOptions().contains("6")) {
                                view.setInputLengthLimit(Integer.parseInt(cusFieldConfig.getLimitChar()));
                            }
                        }

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == cusFieldConfig.getFieldType()) {
                        //多行文本
                        view.setInputType("many_lines");

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == cusFieldConfig.getFieldType()) {
                        //日期
                        view.setInputType("select");
                        Drawable img = context.getResources().getDrawable(R.drawable.sobot_cur_data);
                        view.setSelectIcon(img);
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == cusFieldConfig.getFieldType()) {
                        //时间
                        view.setInputType("select");
                        Drawable img = context.getResources().getDrawable(R.drawable.sobot_cur_time);
                        view.setSelectIcon(img);

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE == cusFieldConfig.getFieldType() || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE == cusFieldConfig.getFieldType()) {
                        //下拉列表和单选框
                        view.setInputType("select");

                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE == cusFieldConfig.getFieldType()) {
                        //复选框
                        view.setInputType("select");
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE == cusFieldConfig.getFieldType()) {
                        //级联
                        view.setInputType("select");
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE == cusFieldConfig.getFieldType()) {
                        //地区级联
                        view.setInputType("select");
                        //赋值
                        if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                            view.setInputValue(cusFieldConfig.getText());
                            view.getTvSelect().setTag(cusFieldConfig.getValue());
                        }
                    } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE == cusFieldConfig.getFieldType()) {
                        //时区
                        view.setInputType("select");
                        //赋值
                        if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                            view.setInputValue(cusFieldConfig.getText());
                            view.getTvSelect().setTag(cusFieldConfig.getValue());
                        }
                    }

                    containerLayout.addView(view);
                }
            }
        }
    }
}
