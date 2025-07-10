package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotTimeZoneAdapter;
import com.sobot.chat.api.model.SobotTimezone;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.utils.SobotStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义字段 --日期时间--时区
 */
public class SobotTimeZoneDialog extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private ArrayList<SobotTimezone> list;
    private SobotTimeZoneAdapter adapter;
    private SobotTimezone selectStauts;
    private RecyclerView rv_list;
    private TextView sobot_sureButton;

    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear;
    private TextView tv_nodata;

    @Override
    public void onClick(View v) {
        if (v == sobot_sureButton) {
            Intent intent = new Intent();
            intent.putExtra("selectStauts", selectStauts);
            setResult(30003, intent);
            finish();
        } else if (v == iv_clear) {
            et_search.setText("");
            showAll();
        }
    }

    @Override
    protected void initData() {
        list = (ArrayList<SobotTimezone>) getIntent().getSerializableExtra("zoneList");
        if (list == null) {
            list = new ArrayList<>();
        }
        List<SobotTimezone> temList = new ArrayList();
        temList.addAll(list);
        adapter = new SobotTimeZoneAdapter(this, temList, selectStauts, new SobotTimeZoneAdapter.SobotTimezoneListener() {
            @Override
            public void selectStatus(SobotTimezone model) {
                selectStauts = model;
            }
        }
        );
        rv_list.setAdapter(adapter);
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_time_zone;
    }

    @Override
    protected void initView() {
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        rv_list = findViewById(R.id.rv_list);
        sobot_sureButton = findViewById(R.id.btnSubmit);
        if (ThemeUtils.isChangedThemeColor(this)) {
            int themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = sobot_sureButton.getBackground();
            if (bg != null) {
                sobot_sureButton.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
        }
        sobot_sureButton.setOnClickListener(this);
        rv_list.setLayoutManager(new LinearLayoutManager(this));

        ll_search = findViewById(R.id.ll_search);
        et_search = findViewById(R.id.et_search);
        iv_clear = findViewById(R.id.sobot_iv_clear);
        tv_nodata = findViewById(R.id.tv_nodata);
        iv_clear.setOnClickListener(this);
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    KeyboardUtil.showKeyboard(et_search);
                } else {
                    KeyboardUtil.hideKeyboard(v);
                }
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputCount = s.length();
                if (inputCount > 0) {
                    iv_clear.setVisibility(View.VISIBLE);
                } else {
                    iv_clear.setVisibility(View.GONE);
                }
                //搜索
                setIv_search();
            }
        });
    }

    private void setIv_search() {
        final String searchText = et_search.getText().toString();
        if (SobotStringUtils.isEmpty(searchText)) {
            showAll();
        } else {
            List<SobotTimezone> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (SobotStringUtils.isNoEmpty(list.get(i).getTimezoneValue()) && list.get(i).getTimezoneValue().toLowerCase().contains(searchText.toLowerCase())) {
                    temList.add(list.get(i));
                }
            }
            adapter.setList(temList,searchText);
            if (temList.size() > 0) {
                showList();
            } else {
                showEmpt();
            }
        }
    }

    private void showEmpt() {
        tv_nodata.setVisibility(View.VISIBLE);
        rv_list.setVisibility(View.GONE);
    }

    private void showList() {
        tv_nodata.setVisibility(View.GONE);
        rv_list.setVisibility(View.VISIBLE);
    }
    private void showAll() {
        //显示全部数据
        List<SobotTimezone> temList = new ArrayList();
        temList.addAll(list);
        adapter.setList(temList,"");
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }
}
