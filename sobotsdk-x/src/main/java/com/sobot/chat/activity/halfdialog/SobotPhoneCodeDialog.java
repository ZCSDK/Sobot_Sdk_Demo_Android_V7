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
import com.sobot.chat.adapter.SobotPhoneCodeAdapter;
import com.sobot.chat.api.model.SobotPhoneCode;
import com.sobot.chat.utils.SobotPhoneCodeUtil;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义字段 --手机号--区号
 */
public class SobotPhoneCodeDialog extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private List<SobotPhoneCode> list;
    private SobotPhoneCodeAdapter adapter;
    private SobotPhoneCode selectStauts;
    private RecyclerView rv_list;
    private TextView sobot_sureButton;
    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear;
    private TextView tv_nodata, sobot_tv_title;

    @Override
    public void onClick(View v) {
        if (v == sobot_sureButton) {
            if (selectStauts != null) {
                Intent intent = new Intent();
                intent.putExtra("selectCode", selectStauts.getPhone_code());
                setResult(4001, intent);
            }
            finish();
        } else if (v == iv_clear) {
            et_search.setText("");
            //显示全部数据
            adapter.setList(list, "");
            showList();
//        } else if (v == iv_search) {
//            et_search.clearFocus();
//            //搜索
//            setIv_search();
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotPhoneCodeDialog";
    }

    @Override
    protected void initData() {
        list = new ArrayList<>();
        List<SobotPhoneCode> templist = SobotPhoneCodeUtil.initCurrenty(this);
        if (templist == null) {
            templist = new ArrayList<>();
        }
        String pinyin = "";
        for (int i = 0; i < templist.size(); i++) {
            String frist = templist.get(i).getPinyin().substring(0, 1);
            if (!frist.equals(pinyin)) {
                pinyin = frist;
                SobotPhoneCode code = new SobotPhoneCode();
                code.setPinyin(frist);
                list.add(code);
            }
            list.add(templist.get(i));
        }
        adapter = new SobotPhoneCodeAdapter(this, templist, selectStauts, new SobotPhoneCodeAdapter.SobotItemListener() {
            @Override
            public void selectItem(SobotPhoneCode model) {
                selectStauts = model;
            }
        }
        );
        rv_list.setAdapter(adapter);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_time_zone;
    }

    @Override
    protected void initView() {
        super.initView();
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_phone_code);
        rv_list = findViewById(R.id.rv_list);
        sobot_sureButton = findViewById(R.id.btnSubmit);
        sobot_sureButton.setOnClickListener(this);
        if (ThemeUtils.isChangedThemeColor(this)) {
            int themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = sobot_sureButton.getBackground();
            if (bg != null) {
                sobot_sureButton.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
        }
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
                    SobotSoftKeyboardUtils.showSoftKeyboard(getSobotBaseActivity());
                } else {
                    SobotSoftKeyboardUtils.hideKeyboard(getSobotBaseActivity());
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
        if (StringUtils.isEmpty(searchText)) {
            showList();
            adapter.setList(list, searchText);
        } else {
            List<SobotPhoneCode> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (StringUtils.isNoEmpty(list.get(i).getPhone_code()) && list.get(i).getPhone_code().contains(searchText)) {
                    temList.add(list.get(i));
                }
            }
            adapter.setList(temList, searchText);
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
}
