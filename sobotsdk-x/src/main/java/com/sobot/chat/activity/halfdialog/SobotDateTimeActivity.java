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
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.timePicker.view.SobotWheelTime;

import java.util.Calendar;

/**
 * 自定义字段 --日期时间
 */
public class SobotDateTimeActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private View v_top;

    private TextView btnSubmit;//确定
    private TextView sobot_tv_title;
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
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_time_zone;
    }

    @Override
    protected void initView() {
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        findViewById(R.id.tv_time_zone).setVisibility(View.GONE);
        btnSubmit = findViewById(R.id.btnSubmit);
        v_top = findViewById(R.id.v_top);
        v_top.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        // 时间转轮 自定义控件
        LinearLayout timePickerView = (LinearLayout) findViewById(R.id.timepicker);


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
        if(cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE) {
            type = new boolean[]{true, true, true, false, false, false};//显示类型 默认全部显示
        }else{
            type = new boolean[]{false, false, false, true, true, false};//显示类型 默认全部显示
        }
        wheelTime = new SobotWheelTime(timePickerView, type, gravity, Size_Content);
        String timsStr;
        //设置默认值
//        if (SobotStringUtils.isNoEmpty(cusFieldConfig.getShowName())) {
//            String[] zoneName = cusFieldConfig.getShowName().split(",");
//            String[] zoneValue = cusFieldConfig.getValue().split(",");
//            if (zoneName.length == 2) {
//                timsStr = zoneName[1];
//            } else {
//                timsStr = cusFieldConfig.getText();
//            }
//            Date date1 = SobotDateUtil.parse(timsStr, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()));
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date1);
//            wheelTime.setPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
//        } else {
            setTime();
//        }

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


    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            //去掉秒
            String time = "";
            //点击确定
            if(cusFieldConfig.getFieldType() == ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_DATE_TYPE) {
                time  =wheelTime.getDate();
            }else{
                time  =wheelTime.getTime();
            }
            Intent intent = new Intent();
            intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
            intent.putExtra("fieldType", cusFieldConfig.getFieldType());
            intent.putExtra("category_typeValue", time);
            intent.putExtra("category_typeName", time);
            intent.putExtra("category_fieldId", cusFieldConfig.getFieldId() + "");
            setResult(ZhiChiConstant.work_order_list_display_type_category, intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 30003 && data != null) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
