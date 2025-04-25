package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.timePicker.view.SobotWheelTime;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.utils.SobotDateUtil;
import com.sobot.utils.SobotStringUtils;
import com.sobot.widget.ui.toast.SobotToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 自定义字段 --时区 时间
 */
public class SobotTimeZoneActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private View v_top;

    private TextView tv_time_zone;//跳转的时区选择
    private TextView btnSubmit;//确定
    private TextView sobot_tv_title;
    private ArrayList<SobotTimezone> list;
    private SobotTimezone selectTimeZone;//选中的时区

    private SobotWheelTime wheelTime; //自定义控件
    private Calendar date;//当前选中时间
    private Calendar startDate;//开始时间
    private Calendar endDate;//终止时间
    private boolean[] type;// 显示类型
    private int gravity = Gravity.CENTER;//内容显示位置 默认居中
    private int Size_Content = 18;//内容字体大小
    private SobotCusFieldConfig cusFieldConfig;//当前自定义字段
    private int themeColor;
    private boolean changeThemeColor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initData() {
        list = new ArrayList<>();
        //显示时区
        requestZone(false);
        tv_time_zone.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_time_zone;
    }

    @Override
    protected void initView() {
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        tv_time_zone = findViewById(R.id.tv_time_zone);
        type = new boolean[]{true, true, true, true, true, false};//显示类型 默认全部显示
        btnSubmit = findViewById(R.id.btnSubmit);
        v_top = findViewById(R.id.v_top);
        v_top.setOnClickListener(this);
        tv_time_zone.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

// 时间转轮 自定义控件
        LinearLayout timePickerView = (LinearLayout) findViewById(R.id.timepicker);

        wheelTime = new SobotWheelTime(timePickerView, type, gravity, Size_Content);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        if (intent.getSerializableExtra("cusFieldConfig") != null) {
            cusFieldConfig = (SobotCusFieldConfig) intent.getSerializableExtra("cusFieldConfig");
        }
        if (cusFieldConfig == null) {
            finish();
        }
        sobot_tv_title.setText(cusFieldConfig.getFieldName());
        String timsStr;
        //设置默认值
        if (SobotStringUtils.isNoEmpty(cusFieldConfig.getShowName())) {
            String[] zoneName = cusFieldConfig.getShowName().split(",");
            String[] zoneValue = cusFieldConfig.getValue().split(",");
            if (zoneName.length == 2) {
                selectTimeZone = new SobotTimezone();
                selectTimeZone.setTimezoneId(zoneValue[0]);
                selectTimeZone.setTimezoneValue(zoneName[0]);
                tv_time_zone.setText(zoneName[0]);

                timsStr = zoneName[1];
            } else {
                timsStr = cusFieldConfig.getText();
            }
            Date date1 = SobotDateUtil.parse(timsStr, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            wheelTime.setPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
        } else {
            setTime();
        }

        if (startDate != null && endDate != null) {
            if (startDate.getTimeInMillis() <= endDate.getTimeInMillis()) {
                setRangDate();
            }
        } else if (startDate != null && endDate == null) {
            setRangDate();
        } else if (startDate == null && endDate != null) {
            setRangDate();
        }
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
        }
        if (changeThemeColor) {
            Drawable bg = btnSubmit.getBackground();
            if (bg != null) {
                btnSubmit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
        }
    }

    /**
     * 设置选中时间,默认选中当前时间
     */
    private void setTime() {
        int year, month, day, hours, minute, seconds;

        Calendar calendar = Calendar.getInstance();
        if (date == null) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
            seconds = calendar.get(Calendar.SECOND);
        } else {
            year = date.get(Calendar.YEAR);
            month = date.get(Calendar.MONTH);
            day = date.get(Calendar.DAY_OF_MONTH);
            hours = date.get(Calendar.HOUR_OF_DAY);
            minute = date.get(Calendar.MINUTE);
        }
        wheelTime.setPicker(year, month, day, hours, minute, 0);
    }

    /**
     * 设置可以选择的时间范围, 要在setTime之前调用才有效果
     */
    private void setRangDate() {
        wheelTime.setRangDate(startDate, endDate);
        //如果设置了时间范围
        if (startDate != null && endDate != null) {
            //判断一下默认时间是否设置了，或者是否在起始终止时间范围内
            if (date == null || date.getTimeInMillis() < startDate.getTimeInMillis()
                    || date.getTimeInMillis() > endDate.getTimeInMillis()) {
                date = startDate;
            }
        } else if (startDate != null) {
            //没有设置默认选中时间,那就拿开始时间当默认时间
            date = startDate;
        } else if (endDate != null) {
            date = endDate;
        }
    }

    private int requestCount = 0;//请求的次数

    private void requestZone(final boolean showDialog) {
        if (requestCount > 5) {
            //显示暂无数据
            if(showDialog) {
                showDialog();
            }
            return;
        }
        requestCount++;
        String languageCode = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
        zhiChiApi.getTimezone(this, languageCode, new SobotResultCallBack<List<SobotTimezone>>() {
            @Override
            public void onSuccess(List<SobotTimezone> placeModels) {
                list.clear();
                if (placeModels != null) {
                    list.addAll(placeModels);
                }
                if (showDialog) {
                    showDialog();
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                requestZone(showDialog);
            }
        });
    }

    @Override
    public void onClick(View v) {
         if (v == tv_time_zone) {
            //显示dialog
            if (list == null || list.size() == 0) {
                requestZone(true);
            } else {
                showDialog();
            }
        } else if (v == btnSubmit) {
            //如果自定义字段是必填的，时区就是必填的
            if (selectTimeZone == null) {
                CustomToast.makeText(SobotTimeZoneActivity.this, getResources().getString(R.string.sobot_time_zone_hint), 1000).show();
                return;
            }
            //去掉秒
            String time = wheelTime.getDateTime();
            time = time.substring(0, time.lastIndexOf(":"));
            String text = "";
            String value = "";
            if (selectTimeZone != null) {
                text = selectTimeZone.getTimezoneValue() + "," + time;
                value = selectTimeZone.getTimezoneId() + "," + time;
            } else {
                text = time;//显示的值
                value = time;//保存的值
            }
            //点击确定
            Intent intent = new Intent();
            intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
            intent.putExtra("fieldType", cusFieldConfig.getFieldType());
            intent.putExtra("category_typeValue", value);
            intent.putExtra("category_typeName", text);
            intent.putExtra("category_fieldId", cusFieldConfig.getFieldId() + "");
            setResult(3001, intent);
            finish();
        }
    }

    private void showDialog() {
        Intent intent = new Intent(this, SobotTimeZoneDialog.class);
        intent.putExtra("zoneList", list);
        startActivityForResult(intent, 30003);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 30003 && data != null) {
            selectTimeZone = (SobotTimezone) data.getSerializableExtra("selectStauts");
            if (selectTimeZone != null && SobotStringUtils.isNoEmpty(selectTimeZone.getTimezoneValue())) {
                tv_time_zone.setText(selectTimeZone.getTimezoneValue());
            } else {
                tv_time_zone.setText("");
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
