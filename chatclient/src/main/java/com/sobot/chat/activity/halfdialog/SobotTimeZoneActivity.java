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
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.timePicker.view.SobotWheelTime;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.SobotResultCallBack;

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

    private TextView btnSubmit;//确定
    private TextView sobot_tv_title;

    private SobotWheelTime wheelTime; //自定义控件
    private Calendar date;//当前选中时间
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
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTimeZoneActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        type = new boolean[]{true, true, true, true, true, false};//显示类型 默认全部显示
        btnSubmit = findViewById(R.id.btnSubmit);
        v_top = findViewById(R.id.v_top);
        v_top.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

// 时间转轮 自定义控件
        LinearLayout timePickerView = (LinearLayout) findViewById(R.id.timepicker);

        wheelTime = new SobotWheelTime(timePickerView, type, gravity, Size_Content,true);
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
        if (StringUtils.isNoEmpty(cusFieldConfig.getShowName())) {
            timsStr = cusFieldConfig.getShowName();
            Date date1 = DateUtil.parse(timsStr, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            wheelTime.setPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
        } else {
            setTime();
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
        btnSubmit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
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


    @Override
    public void onClick(View v) {

        if (v == btnSubmit) {
            //去掉秒
            String time = wheelTime.getDateTime();
            time = time.substring(0, time.lastIndexOf(":"));
            //点击确定
            Intent intent = new Intent();
            intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
            intent.putExtra("fieldType", ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_ZONE_TIME);
            intent.putExtra("category_typeValue", time);
            intent.putExtra("category_typeName", time);
            intent.putExtra("category_fieldId", cusFieldConfig.getFieldId() + "");
            setResult(cusFieldConfig.getFieldType(), intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
