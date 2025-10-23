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
import com.sobot.chat.adapter.SobotChooseLanguaeAdapter;
import com.sobot.chat.api.model.SobotlanguaeModel;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户进线 选择语言 ，点击其它语言 弹窗
 */
public class SobotChooseLanguaeActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private LinearLayout coustom_pop_layout;
    private ArrayList<SobotlanguaeModel> list;
    private String removeMsgId;//选中语言后，通过这个msgid从消息列表里边移除选择语言的cell
    private SobotChooseLanguaeAdapter adapter;
    private RecyclerView rv_list;

    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear;
    private TextView tv_nodata;
    private TextView sobot_tv_title;

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
    protected void initData() {
        list = (ArrayList<SobotlanguaeModel>) getIntent().getSerializableExtra("SobotlanguaeModelList");
        removeMsgId = getIntent().getStringExtra("removeMsgId");
        if (list == null) {
            list = new ArrayList<>();
        }
        List<SobotlanguaeModel> temList = new ArrayList();
        temList.addAll(list);
        adapter = new SobotChooseLanguaeAdapter(this, temList, new SobotChooseLanguaeAdapter.SobotChooseLanguaeListener() {

            @Override
            public void selectLanguae(SobotlanguaeModel model) {
                Intent intent = new Intent();
                intent.putExtra("selectLanguaeModel", model);
                intent.putExtra("removeMsgId", removeMsgId);
                setResult(ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_LANGUAE, intent);
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
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_choose_languae;
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_select_language);
        //根布局
        if (coustom_pop_layout == null) {
            coustom_pop_layout = findViewById(R.id.sobot_container);
        }
        rv_list = findViewById(R.id.rv_list);
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
                    showSoftKeyboard();
                } else {
                    hideKeyboard();
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

    @Override
    protected void setRequestTag() {
        REQUEST_TAG ="SobotChooseLanguaeActivity";
    }

    private void setIv_search() {
        final String searchText = et_search.getText().toString();
        if (StringUtils.isEmpty(searchText)) {
            showAll();
        } else {
            List<SobotlanguaeModel> temList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getName().toLowerCase().contains(searchText.toLowerCase())) {
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
        List<SobotlanguaeModel> temList = new ArrayList();
        temList.addAll(list);
        adapter.setList(temList,"");
        if (temList.size() > 0) {
            showList();
        } else {
            showEmpt();
        }
    }
}
