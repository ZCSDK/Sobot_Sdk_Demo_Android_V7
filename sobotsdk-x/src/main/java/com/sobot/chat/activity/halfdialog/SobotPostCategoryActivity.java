package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotPostCategoryAdapter;
import com.sobot.chat.api.model.SobotTypeModel;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言问题分类 选择
 * Created by Administrator on 2017/7/13.
 */
public class SobotPostCategoryActivity extends SobotDialogBaseActivity {

    private SobotPostCategoryAdapter categoryAdapter;
    private ListView listView;
    private TextView sobot_tv_title;
    private ImageView sobot_iv_clear;
    private EditText sobot_et_search;
    private LinearLayout sobot_ll_search;

    private HorizontalScrollView horizontalScrollView_ll;
    private LinearLayout ll_level;

    private List<SobotTypeModel> types = new ArrayList<>();
    private SparseArray<List<SobotTypeModel>> tmpMap = new SparseArray<>();
    private List<SobotTypeModel> tmpDatas = new ArrayList<>();
    private int currentLevel = 1;
    private String typeName;
    private String typeId;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_post_category;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotPostCategoryActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        listView = (ListView) findViewById(R.id.sobot_activity_post_category_listview);
        sobot_ll_search = findViewById(R.id.sobot_ll_search);
        sobot_ll_search.setVisibility(View.GONE);
        sobot_et_search = (EditText) findViewById(R.id.sobot_et_search);
        sobot_iv_clear = findViewById(R.id.sobot_iv_clear);
        sobot_iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sobot_et_search.setText("");
                sobot_iv_clear.setVisibility(View.GONE);
            }
        });
//搜索
        sobot_et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (categoryAdapter == null) {
                    return;
                }
                categoryAdapter.setSearchText(s.toString());
                if (s.length() > 0) {
                    sobot_iv_clear.setVisibility(View.VISIBLE);
                } else {
                    sobot_iv_clear.setVisibility(View.GONE);
                }
                SobotPostCategoryAdapter.MyFilter m = categoryAdapter.getFilter();
                m.filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                sobot_et_search.setText("");
                if (ZhiChiConstant.WORK_WORK_ORDER_CATEGORY_NODEFLAG_YES == tmpMap.get(currentLevel).get(position).getNodeFlag()) {
                    currentLevel++;
                    showDataWithLevel(position);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("category_typeName", tmpMap.get(currentLevel).get(position).getTypeName());
                    intent.putExtra("category_typeId", tmpMap.get(currentLevel).get(position).getTypeId());
                    setResult(ZhiChiConstant.work_order_list_display_type_category, intent);
                    for (int i = 0; i < tmpMap.get(currentLevel).size(); i++) {
                        tmpMap.get(currentLevel).get(i).setChecked(i == position);
                    }
                    categoryAdapter.notifyDataSetChanged();
                    finish();
                }
            }
        });

    }

    @Override
    protected void initData() {
        types.clear();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        ArrayList<SobotTypeModel> typeTemp = null;
        if (bundle != null) {
            typeName = bundle.getString("typeName");
            typeId = bundle.getString("typeId");
            typeTemp = (ArrayList<SobotTypeModel>) bundle.getSerializable("types");
        }
        if (typeTemp != null) {
            types.addAll(typeTemp);
        }
        sobot_tv_title.setText(R.string.sobot_choice_classification);


        //存贮一级List
        currentLevel = 1;
        tmpMap.put(1, types);
        if (types != null && types.size() != 0) {
            showDataWithLevel(-1);
        }
    }

    private void showDataWithLevel(int position) {
        if (position >= 0) {
            tmpMap.put(currentLevel, tmpMap.get(currentLevel - 1).get(position).getItems());
        }

        ArrayList<SobotTypeModel> currentList = (ArrayList<SobotTypeModel>) tmpMap.get(currentLevel);
        if (currentList != null) {
            resetChecked(currentList);
            notifyListData(currentList);
        }
    }

    private void notifyListData(List<SobotTypeModel> currentList) {
        tmpDatas.clear();
        tmpDatas.addAll(currentList);
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        } else {
            categoryAdapter = new SobotPostCategoryAdapter(SobotPostCategoryActivity.this, SobotPostCategoryActivity.this, tmpDatas);
            listView.setAdapter(categoryAdapter);
        }
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed() {
        if (currentLevel <= 1) {
            finish();
        } else {
            currentLevel--;
            if (currentLevel == 1) {
//                sobot_btn_back.setVisibility(View.GONE);
            }
            if (currentLevel > 1) {
//                sobot_btn_back.setVisibility(View.VISIBLE);
            }
            List<SobotTypeModel> sobotTypeModels = tmpMap.get(currentLevel);
            notifyListData(sobotTypeModels);
        }
    }

    private void resetChecked(ArrayList<SobotTypeModel> type) {
        for (int i = 0; i < type.size(); i++) {
            if (!TextUtils.isEmpty(typeId) && typeId.equals(type.get(i).getTypeId())) {
                type.get(i).setChecked(true);
            }
        }
    }
}