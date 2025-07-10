package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
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
import com.sobot.chat.adapter.SobotPostCascadeAdapter;
import com.sobot.chat.api.model.SobotCusFieldDataInfo;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * 级联自定义字段
 */
public class SobotPostCascadeActivity extends SobotDialogBaseActivity {

    private SobotPostCascadeAdapter categoryAdapter;
    private ListView listView;
    private TextView sobot_tv_title;
    private ImageView sobot_iv_clear;
    private EditText sobot_et_search;
    private HorizontalScrollView horizontalScrollView_ll;
    private LinearLayout ll_level;
    private View sobot_v,v_search_line;

    private SparseArray<List<SobotCusFieldDataInfo>> tmpMap;//显示的列表
    private List<SobotCusFieldDataInfo> tmpDatas;//选中的数据
    private List<SobotCusFieldDataInfo> selectCusFieldDataInfos;//选中的数据
    private int currentLevel = 0;
    private String fieldId;

    private List<SobotCusFieldDataInfo> cusFieldDataInfoList;//全部的列表
    private SobotFieldModel cusField;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_post_category;
    }

    @Override
    protected void initView() {
        tmpMap = new SparseArray<>();
        tmpDatas = new ArrayList<>();
        selectCusFieldDataInfos = new ArrayList<>();
        SobotCusFieldDataInfo all = new SobotCusFieldDataInfo();
        all.setDataName(getResources().getString(R.string.sobot_cus_level_all));
        all.setParentDataId("");
        all.setDataId("1");
        selectCusFieldDataInfos.add(all);
        horizontalScrollView_ll = findViewById(R.id.sobot_level);
        sobot_v = findViewById(R.id.sobot_v);
        v_search_line = findViewById(R.id.v_search_line);
        sobot_v.setVisibility(View.VISIBLE);
        ll_level = findViewById(R.id.ll_level);
        sobot_et_search = (EditText) findViewById(R.id.sobot_et_search);
        sobot_iv_clear = findViewById(R.id.sobot_iv_clear);
        sobot_iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sobot_et_search.setText("");
                sobot_iv_clear.setVisibility(View.GONE);
            }
        });
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        listView = (ListView) findViewById(R.id.sobot_activity_post_category_listview);
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
                SobotPostCascadeAdapter.MyFilter m = categoryAdapter.getFilter();
                m.filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sobot_et_search.setText("");
                selectCusFieldDataInfos.add(categoryAdapter.getDatas().get(position));
                if (getNextLevelList(categoryAdapter.getDatas().get(position).getDataId()).size() > 0) {
                    currentLevel++;
                    showDataWithLevel(position, categoryAdapter.getDatas().get(position).getDataId());
                } else {
                    //回显回去
                    Intent intent = new Intent();
                    intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
                    intent.putExtra("fieldType", ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE);
                    String typeName = "";
                    String typeValue = "";
                    for (int i = 1; i < selectCusFieldDataInfos.size(); i++) {
                        if (i == (selectCusFieldDataInfos.size() - 1)) {
                            typeName = typeName + selectCusFieldDataInfos.get(i).getDataName();
                            typeValue = typeValue + selectCusFieldDataInfos.get(i).getDataValue();
                        } else {
                            typeName = typeName + selectCusFieldDataInfos.get(i).getDataName() + ",";
                            typeValue = typeValue + selectCusFieldDataInfos.get(i).getDataValue() + ",";
                        }
                    }
                    intent.putExtra("category_typeName", typeName);
                    intent.putExtra("category_fieldId", fieldId);
                    intent.putExtra("category_typeValue", typeValue);
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
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            fieldId = bundle.getString("fieldId");
            cusField = (SobotFieldModel) bundle.getSerializable("cusField");
        }

        sobot_tv_title.setText(R.string.sobot_choice_classification);

        if (cusField != null && cusField.getCusFieldDataInfoList() != null) {
            cusFieldDataInfoList = cusField.getCusFieldDataInfoList();
        } else {
            cusFieldDataInfoList = new ArrayList<>();
        }

        //存贮一级List
        currentLevel = 0;
        tmpMap.put(0, getNextLevelList(""));
        if (cusFieldDataInfoList != null && cusFieldDataInfoList.size() != 0) {
            showDataWithLevel(-1, "");
        }
    }

    private void showDataWithLevel(int position, String dataId) {
        if (position >= 0) {
            tmpMap.put(currentLevel, getNextLevelList(dataId));
        }

        ArrayList<SobotCusFieldDataInfo> currentList = (ArrayList<SobotCusFieldDataInfo>) tmpMap.get(currentLevel);
        if (currentList != null) {
            notifyListData(currentList);
        }
    }

    private void notifyListData(List<SobotCusFieldDataInfo> currentList) {
        tmpDatas.clear();
        tmpDatas.addAll(currentList);
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        } else {
            categoryAdapter = new SobotPostCascadeAdapter(SobotPostCascadeActivity.this, SobotPostCascadeActivity.this, tmpDatas);
            listView.setAdapter(categoryAdapter);
        }
        updateIndicator();
    }

    /**
     * 点击标题
     */
    private void clickLevel() {
        if (currentLevel == 0) {
            finish();
        } else {
            currentLevel--;
            if (currentLevel == 0) {
                sobot_v.setVisibility(View.VISIBLE);
                horizontalScrollView_ll.setVisibility(View.GONE);
                v_search_line.setVisibility(View.GONE);
            }
            if (currentLevel >= 1) {
                sobot_v.setVisibility(View.GONE);
                horizontalScrollView_ll.setVisibility(View.VISIBLE);
                v_search_line.setVisibility(View.VISIBLE);
            }
            selectCusFieldDataInfos.remove(selectCusFieldDataInfos.size() - 1);
            List<SobotCusFieldDataInfo> sobotTypeModels = tmpMap.get(currentLevel);
            notifyListData(sobotTypeModels);
        }
    }

    //获取下一级显示数据
    private List<SobotCusFieldDataInfo> getNextLevelList(String parentDataId) {
        List<SobotCusFieldDataInfo> curLevelList = new ArrayList<>();
        curLevelList.clear();
        for (int i = 0; i < cusFieldDataInfoList.size(); i++) {
            if (StringUtils.isEmpty(parentDataId)) {
                if (StringUtils.isEmpty(cusFieldDataInfoList.get(i).getParentDataId())) {
                    cusFieldDataInfoList.get(i).setHasNext(isHasNext(cusFieldDataInfoList.get(i).getDataId()));
                    curLevelList.add(cusFieldDataInfoList.get(i));
                }
            } else {
                if (parentDataId.equals(cusFieldDataInfoList.get(i).getParentDataId())) {
                    cusFieldDataInfoList.get(i).setHasNext(isHasNext(cusFieldDataInfoList.get(i).getDataId()));
                    curLevelList.add(cusFieldDataInfoList.get(i));
                }
            }
        }
        return curLevelList;
    }

    //是否还有下一级数据，有的话显示右箭头
    private boolean isHasNext(String parentDataId) {
        for (int i = 0; i < cusFieldDataInfoList.size(); i++) {
            if (parentDataId.equals(cusFieldDataInfoList.get(i).getParentDataId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新头部
     */
    private void updateIndicator() {
        ll_level.removeAllViews();
        if (currentLevel > 0) {
            sobot_v.setVisibility(View.GONE);
            horizontalScrollView_ll.setVisibility(View.VISIBLE);
            v_search_line.setVisibility(View.VISIBLE);
            for (int i = 0; i < selectCusFieldDataInfos.size(); i++) {
                View view = View.inflate(this, R.layout.sobot_item_cus_level, null);
                TextView titleTv = view.findViewById(R.id.tv_level);
                ImageView iv_right = view.findViewById(R.id.iv_right);
                if (ThemeUtils.isChangedThemeColor(this)) {
                    int themeColor = ThemeUtils.getThemeColor(this);
                    Drawable bg = getResources().getDrawable(R.drawable.sobot_cur_level);
                    if (bg != null) {
                        iv_right.setImageDrawable(ThemeUtils.applyColorToDrawable(bg, themeColor));
                    }
                }
                if (i == selectCusFieldDataInfos.size() - 1) {
                    //最后一项
                    iv_right.setVisibility(View.GONE);
                } else {
                    titleTv.setTextColor(ThemeUtils.getThemeColor(this));
                    //可以点击
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            currentLevel = (int) v.getTag();//1
                            if (currentLevel>0) {
                                SobotCusFieldDataInfo select = selectCusFieldDataInfos.get(currentLevel);
                                showDataWithLevel(currentLevel, select.getDataId());
                                if(selectCusFieldDataInfos.size()>1){
                                    int count = selectCusFieldDataInfos.size()-currentLevel-1;
                                    if(count>0) {
                                        for (int j = 0; j <count; j++) {
                                            selectCusFieldDataInfos.remove(selectCusFieldDataInfos.size()-1);
                                        }
                                    }
                                }
                            }else{
                                //显示全部
                                if(selectCusFieldDataInfos.size()>1){
                                    int count = selectCusFieldDataInfos.size()-1;
                                    for (int j = 0; j <count; j++) {
                                        selectCusFieldDataInfos.remove(selectCusFieldDataInfos.size()-1);
                                    }
                                }
                                showDataWithLevel(-1, "");
                            }
                            updateIndicator();
                        }
                    });
                }
                view.setTag(i);
                if (selectCusFieldDataInfos.get(i) != null) {
                    titleTv.setText(selectCusFieldDataInfos.get(i).getDataName());
                    ll_level.addView(view);

                }
            }
            horizontalScrollView_ll.post(new Runnable() {
                @Override
                public void run() {
                    horizontalScrollView_ll.fullScroll(View.FOCUS_RIGHT);
                }
            });

        } else {
            horizontalScrollView_ll.setVisibility(View.GONE);
            v_search_line.setVisibility(View.GONE);
            sobot_v.setVisibility(View.VISIBLE);
        }

    }
}