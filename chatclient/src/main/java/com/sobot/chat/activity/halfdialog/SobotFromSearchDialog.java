package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
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
import com.sobot.chat.adapter.SobotFromSearchAdapter;
import com.sobot.chat.api.model.FormNodeInfo;
import com.sobot.chat.utils.SobotSoftKeyboardUtils;
import com.sobot.chat.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 询前表单搜索
 */
public class SobotFromSearchDialog extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private ArrayList<FormNodeInfo> list;
    private SobotFromSearchAdapter adapter;
    private FormNodeInfo selectStauts;
    private String defualtValue;
    private RecyclerView rv_list;
    private TextView sobot_tv_title;

    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear;
    private TextView tv_nodata;
    private int type;

    @Override
    public void onClick(View v) {
        if (v == iv_clear) {
            et_search.setText("");
            showAll();
//        } else if (v == iv_search) {
//            et_search.clearFocus();
//            //搜索
//            setIv_search();
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotFromSearchDialog";
    }

    @Override
    protected void initData() {
        list = (ArrayList<FormNodeInfo>) getIntent().getSerializableExtra("List");
        String title = getIntent().getStringExtra("title");
        type = getIntent().getIntExtra("type",0);
        defualtValue = getIntent().getStringExtra("defualtValue");
        sobot_tv_title.setText(title);
        if (list == null) {
            list = new ArrayList<>();
        }
        List<FormNodeInfo> temList = new ArrayList();
        if(StringUtils.isNoEmpty(defualtValue)){
            for (int i = 0; i < list.size(); i++) {
               if(list.get(i).getName().equals(defualtValue)) {
                   list.get(i).setChecked(true);
               }else{
                   list.get(i).setChecked(false);
               }
            }
        }
        temList.addAll(list);
        adapter = new SobotFromSearchAdapter(this, temList,  new SobotFromSearchAdapter.SobotListener() {
            @Override
            public void select(FormNodeInfo model) {
                selectStauts = model;
                Intent intent = new Intent();
                intent.putExtra("select", selectStauts);
                setResult(30005, intent);
                finish();
            }
        }
        );
        rv_list.setAdapter(adapter);
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
        //是否显示搜索框
        if (type != 8) {
            ll_search.setVisibility(View.VISIBLE);
        } else {
//            单选不显示搜索
            ll_search.setVisibility(View.GONE);
        }
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_from_search;
    }

    @Override
    protected void initView() {
        super.initView();
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(this));

        sobot_tv_title = findViewById(R.id.sobot_tv_title);
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
                    showAll();
                }
                //搜索
                setIv_search();
            }
        });
    }

    private void setIv_search() {
        final String searchText = et_search.getText().toString();
        if (StringUtils.isEmpty(searchText)) {
            showAll();
        } else {
            List<FormNodeInfo> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().contains(searchText)) {
                    temList.add(list.get(i));
                }
            }
            adapter.setDate(searchText);
            adapter.setList(temList);
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
        List<FormNodeInfo> temList = new ArrayList();
        temList.addAll(list);
        adapter.setList(temList);
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }
}
