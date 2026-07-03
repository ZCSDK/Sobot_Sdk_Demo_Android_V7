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
import android.widget.LinearLayout;

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
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.listener.SobotCusFieldListener;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotInputView;
import com.sobot.chat.widget.SobotUploadView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        if (field != null && field.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < field.size(); i++) {
                JSONObject model = new JSONObject();
                SobotCusFieldConfig cusFieldConfig = field.get(i).getCusFieldConfig();
                try {
                    if (cusFieldConfig != null && !StringUtils.isEmpty(cusFieldConfig.getFieldId())) {
                        if (cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_UPLOAD) {
                            //附件类型：将多文件列表序列化为 JSONArray
                            JSONArray array = new JSONArray();
                            java.util.List<com.sobot.chat.api.model.SobotCacheFile> files =
                                    field.get(i).getCusFieldConfig().getCacheFileList();
                            if (files != null) {
                                for (com.sobot.chat.api.model.SobotCacheFile f : files) {
                                    if (f != null) {
                                        try {
                                            JSONObject value = new JSONObject();
                                            value.put("fileUrl", f.getUrl());
                                            value.put("fileName", f.getFileName());
                                            array.put(value);
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                            model.put("id", field.get(i).getCusFieldConfig().getFieldId());
                            model.put("value", array);
                            jsonArray.put(model);
                        } else if (!StringUtils.isEmpty(cusFieldConfig.getValue())) {
                            model.put("id", field.get(i).getCusFieldConfig().getFieldId());
                            model.put("value", field.get(i).getCusFieldConfig().getValue());
                            if (cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE) {
                                model.put("text", field.get(i).getCusFieldConfig().getText());
                            } else if (cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE) {
                                //时区
                                model.put("text", field.get(i).getCusFieldConfig().getText());
                                model.put("value", field.get(i).getCusFieldConfig().getText());
                            } else {
                                model.put("text", field.get(i).getCusFieldConfig().getShowName());
                            }
                            jsonArray.put(model);
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            if (jsonArray.length() > 0) {
                return jsonArray.toString();
            }
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
            int fieldType = data.getIntExtra("fieldType", -1);//自定义类型
            String id = data.getStringExtra("category_fieldId");//自定义变量
            if ("null".equals(id) || TextUtils.isEmpty(id)) {
                return;
            }
            if (fieldType == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_ZONE) {
                //时区
                SobotTimezone timezone = (SobotTimezone) data.getSerializableExtra("selectStauts");
                if (null != timezone) {
                    for (int i = 0; i < field.size(); i++) {
                        SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                        if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                            model.setChecked(true);
                            model.setTimezone(timezone);
                            SobotInputView view = post_customer_field.findViewWithTag(model.getFieldId());
                            view.setSelectLeftValue(timezone.getTimezoneValue());
                        }
                    }
                }
            } else {
                String value = data.getStringExtra("category_typeName");//选项的名字
                String dataValue = data.getStringExtra("category_typeValue");//选项的值
                if (field != null && !StringUtils.isEmpty(value) && !StringUtils.isEmpty(dataValue)) {
                    for (int i = 0; i < field.size(); i++) {
                        SobotCusFieldConfig model = field.get(i).getCusFieldConfig();
                        if (model != null && model.getFieldId() != null && model.getFieldId().equals(id)) {
                            model.setChecked(true);
                            model.setValue(dataValue);
                            model.setId(id);
                            model.setShowName(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                            SobotInputView view = post_customer_field.findViewWithTag(model.getFieldId());
                            if (fieldType == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_ZONE_TIME) {
                                Date date1 = DateUtil.parse(value, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()));
                                //时区中的时间
                                Locale locale = (Locale) SharedPreferencesUtil.getObject(context, ZhiChiConstant.SOBOT_LANGUAGE);
                                String fomamet = DateUtil.getDateTimeFormatByLanguage(locale);
                                view.setSelectRightValue(DateUtil.dateToString(context, date1, fomamet));
                                view.getTv_select_two_right().setTag(dataValue);
                            } else {
                                view.setInputValue(value.endsWith(",") ? value.substring(0, value.length() - 1) : value);
                            }
                            view.getTvSelect().setSelected(true);
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
    }

    /**
     * 提交前将数据同步到最外层属性中
     *
     * @param sobot_container
     * @param field
     * @return String 自定义表单校验结果:为空,可以提交;不为空,说明自定义字段校验不通过，不能提交留言表单;
     */
    public static boolean formatCusFieldVal(Context context, ViewGroup sobot_container, List<SobotFieldModel> field) {
        boolean isError = false;
        if (field != null && field.size() != 0) {
            for (int j = 0; j < field.size(); j++) {
                String errorStr = "";
                if (field.get(j).getCusFieldConfig() == null) {
                    continue;
                }
                if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_UPLOAD == field.get(j).getCusFieldConfig().getFieldType()) {
                    SobotUploadView view = sobot_container.findViewWithTag(field.get(j).getCusFieldConfig().getFieldId());
                    java.util.List<com.sobot.chat.api.model.SobotCacheFile> files =
                            field.get(j).getCusFieldConfig().getCacheFileList();
                    boolean isEmpty = (files == null || files.isEmpty());
                    if (1 == field.get(j).getCusFieldConfig().getFillFlag() && isEmpty) {
                        errorStr = field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_required);
                    }
                    if (StringUtils.isNoEmpty(errorStr)) {
                        view.showError(errorStr);
                        isError = true;
                    } else {
                        view.hideError();
                    }
                } else {
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
                                    errorStr = field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_phone) + context.getResources().getString(R.string.sobot_input_type_err);
                                }
                            }
                        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                            field.get(j).getCusFieldConfig().setValue(view.getManyValue());
                        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == field.get(j).getCusFieldConfig().getFieldType()
                                || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                            field.get(j).getCusFieldConfig().setValue(view.getSelectValue());
                        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE == field.get(j).getCusFieldConfig().getFieldType()) {
                            SobotTimezone timezone = field.get(j).getCusFieldConfig().getTimezone();
                            //时区
                            String time = "";
                            if (null != view.getTv_select_two_right().getTag()) {
                                time = (String) view.getTv_select_two_right().getTag();
                            }
                            if (timezone != null && StringUtils.isEmpty(time)) {
                                errorStr = context.getResources().getString(R.string.sobot_please_choice);//请选择时间
                                errorStr = errorStr.replace(".", "");
                            } else if (timezone == null && StringUtils.isNoEmpty(time)) {
                                errorStr = context.getResources().getString(R.string.sobot_time_zone_hint);//请选择时区
                            }
                            String value = "";
                            if (timezone != null) {
                                value = timezone.getTimezoneId();
                            }
                            if (StringUtils.isNoEmpty(time)) {
                                value = value + time;
                            }
                            field.get(j).getCusFieldConfig().setValue(value);
                        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == field.get(j).getCusFieldConfig().getFieldType()) {
                            field.get(j).getCusFieldConfig().setValue(view.getSingleValue());
                            if (StringUtils.isNumber(field.get(j).getCusFieldConfig().getLimitOptions()) && field.get(j).getCusFieldConfig().getLimitOptions().contains("3")) {
                                if (!StringUtils.isNumber(view.getSingleValue())) {
                                    errorStr = field.get(j).getCusFieldConfig().getFieldName() + context.getResources().getString(R.string.sobot_input_type_err);
                                }
                            }
                        }
                        if (StringUtils.isNoEmpty(errorStr)) {
                            view.showError(errorStr);
                            isError = true;
                        } else {
                            view.hideError();
                        }
                    }
                }
            }
        }
        return isError;
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

    //新版 自定义字段（兼容旧调用：竖屏模式，单列铺满）
    public static void addWorkOrderCusFieldsNew(final Context context, final ArrayList<SobotFieldModel> cusFieldList, ViewGroup containerLayout, final SobotCusFieldListener callBack) {
        addWorkOrderCusFieldsNew(context, cusFieldList, containerLayout, callBack, false);
    }

    /**
     * 新版自定义字段渲染（带横屏两列支持）
     * <p>
     * 当 {@code isWideScreen=true} 时，按以下规则两列排版：
     * <ul>
     *   <li>附件（{@link ZhiChiConstant#WORK_ORDER_CUSTOMER_FIELD_UPLOAD}）和
     *       多行文本（{@link ZhiChiConstant#WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE}）始终铺满</li>
     *   <li>其余类型尝试两两配对放入横向行容器，列间距取 {@code R.dimen.sobot_ticket_form_h_space}</li>
     *   <li>若与下一字段无法配对（下一为铺满 或 为最后一个 eligible），当前字段占左半（weight=1，右侧留空）</li>
     * </ul>
     * 竖屏（{@code isWideScreen=false}）和其他调用点保持原有单列铺满行为。
     */
    public static void addWorkOrderCusFieldsNew(final Context context, final ArrayList<SobotFieldModel> cusFieldList, ViewGroup containerLayout, final SobotCusFieldListener callBack, boolean isWideScreen) {
        if (containerLayout == null) {
            return;
        }
        containerLayout.setVisibility(View.VISIBLE);
        containerLayout.removeAllViews();
        if (cusFieldList == null || cusFieldList.size() == 0) {
            return;
        }
        // 横屏两列间距，竖屏取 0
        int hGap = isWideScreen
                ? context.getResources().getDimensionPixelSize(R.dimen.sobot_ticket_form_h_space)
                : 0;
        int halfGap = hGap / 2;

        int i = 0;
        while (i < cusFieldList.size()) {
            SobotFieldModel model1 = cusFieldList.get(i);
            SobotCusFieldConfig cfg1 = model1 == null ? null : model1.getCusFieldConfig();
            if (cfg1 == null) {
                i++;
                continue;
            }
            boolean fullWidth1 = isFullWidthCusField(cfg1.getFieldType());

            // 横屏 且 当前可两列 → 进入两列分支
            if (isWideScreen && !fullWidth1) {
                SobotFieldModel model2 = null;
                SobotCusFieldConfig cfg2 = null;
                if (i + 1 < cusFieldList.size()) {
                    model2 = cusFieldList.get(i + 1);
                    cfg2 = model2 == null ? null : model2.getCusFieldConfig();
                }
                boolean canPair = cfg2 != null && !isFullWidthCusField(cfg2.getFieldType());

                if (canPair) {
                    // 两个普通字段配对成双列
                    LinearLayout row = new LinearLayout(context);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    View v1 = buildCusFieldView(context, model1, cfg1, callBack);
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    lp1.setMarginEnd(halfGap);
                    v1.setLayoutParams(lp1);
                    row.addView(v1);

                    View v2 = buildCusFieldView(context, model2, cfg2, callBack);
                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    lp2.setMarginStart(halfGap);
                    v2.setLayoutParams(lp2);
                    row.addView(v2);

                    containerLayout.addView(row);
                    i += 2;
                } else {
                    // 单字段无法配对（下一字段通栏 或 已是最后一个）→ 当前字段通栏，避免出现单列占位
                    View v1 = buildCusFieldView(context, model1, cfg1, callBack);
                    containerLayout.addView(v1);
                    i += 1;
                }
                continue;
            }

            // 单字段铺满：竖屏，或 横屏附件/多行文本
            View v = buildCusFieldView(context, model1, cfg1, callBack);
            containerLayout.addView(v);
            i++;
        }
    }

    /**
     * 横屏两列规则下哪些字段类型仍需铺满整行
     */
    private static boolean isFullWidthCusField(int fieldType) {
        return fieldType == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_UPLOAD
                || fieldType == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE;
    }

    /**
     * 构建单个自定义字段视图：附件 → {@link SobotUploadView}，其余 → {@link SobotInputView}。
     * 抽取此方法只为支持两列配对，业务逻辑（限制、图标、回填）与原实现完全一致。
     */
    private static View buildCusFieldView(Context context, SobotFieldModel model,
                                          SobotCusFieldConfig cusFieldConfig,
                                          SobotCusFieldListener callBack) {
        if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_UPLOAD == cusFieldConfig.getFieldType()) {
            //文件上传
            SobotUploadView view = new SobotUploadView(context);
            view.setTag(cusFieldConfig.getFieldId());
            view.setCusFieldConfig(cusFieldConfig);
            //设置标题
            view.setTitle(cusFieldConfig.getFieldName(), 1 == cusFieldConfig.getFillFlag(), cusFieldConfig.getFinalExplain());
            //设置提示语
            view.setTipText(cusFieldConfig.getMaxStorage());
            view.setCusCallBack(callBack, view);
            return view;
        }

        SobotInputView view = new SobotInputView(context);
        view.setTag(cusFieldConfig.getFieldId());
        view.setCusFieldConfig(cusFieldConfig);
        view.setCusFields(model);
        view.setCusCallBack(callBack);
        //设置标题
        view.setTitle(cusFieldConfig.getFieldName(), 1 == cusFieldConfig.getFillFlag(), cusFieldConfig.getFinalExplain());
        int t = cusFieldConfig.getFieldType();
        if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SINGLE_LINE_TYPE == t
                || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_NUMBER_TYPE == t) {
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
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_MORE_LINE_TYPE == t) {
            //多行文本
            view.setInputType("many_lines");
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE == t) {
            //日期
            view.setInputType("select");
            Drawable img = context.getResources().getDrawable(R.drawable.sobot_cur_data);
            view.setSelectIcon(img);
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_TYPE == t) {
            //时间
            view.setInputType("select");
            Drawable img = context.getResources().getDrawable(R.drawable.sobot_cur_time);
            view.setSelectIcon(img);
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_SPINNER_TYPE == t
                || ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_RADIO_TYPE == t) {
            //下拉列表和单选框
            view.setInputType("select");
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CHECKBOX_TYPE == t) {
            //复选框
            view.setInputType("select");
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE == t) {
            //级联
            view.setInputType("select");
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_REGION_TYPE == t) {
            //地区级联
            view.setInputType("select");
            //赋值
            if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                view.setInputValue(cusFieldConfig.getText());
                view.getTvSelect().setTag(cusFieldConfig.getValue());
            }
        } else if (ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_TIME_ZONE == t) {
            //时区
            view.setInputType("timezone");
            //赋值
            if (!TextUtils.isEmpty(cusFieldConfig.getText())) {
                view.setInputValue(cusFieldConfig.getText());
                view.getTvSelect().setTag(cusFieldConfig.getValue());
            }
        }
        return view;
    }
}
