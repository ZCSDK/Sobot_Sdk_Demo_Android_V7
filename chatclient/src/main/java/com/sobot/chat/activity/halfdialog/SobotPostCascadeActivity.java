package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
    private LinearLayout sobot_ll_search;
    private EditText sobot_et_search;
    private HorizontalScrollView horizontalScrollView_ll;
    private LinearLayout ll_level;
    private View sobot_v, v_search_line;

    private SparseArray<List<SobotCusFieldDataInfo>> tmpMap;//显示的列表
    private List<SobotCusFieldDataInfo> tmpDatas;//选中的数据
    private List<SobotCusFieldDataInfo> selectCusFieldDataInfos;//选中的数据
    private List<SobotCusFieldDataInfo> searchList;//搜索的全量
    private int currentLevel = 0;
    private String fieldId;

    private List<SobotCusFieldDataInfo> cusFieldDataInfoList;//全部的列表
    private SobotFieldModel cusField;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_post_category;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotPostCascadeActivity";
    }

    @Override
    protected void initView() {
        super.initView();
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
        sobot_ll_search = findViewById(R.id.sobot_ll_search);
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
        Drawable bgDrawable =  ContextCompat.getDrawable(this,R.drawable.sobot_bg_line_4);
        sobot_et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    sobot_ll_search.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(SobotPostCascadeActivity.this)));
                } else {
                    sobot_ll_search.setBackground(ContextCompat.getDrawable(SobotPostCascadeActivity.this,R.drawable.sobot_bg_line_4));
                }
            }
        });

        searchList = new ArrayList<>();
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
        List<SobotCusFieldDataInfo> fristList = getNextLevelList("");
        //初始化搜索的数据
        initSearchMap("","",fristList);
        tmpMap.put(0, fristList);
        if (cusFieldDataInfoList != null && !cusFieldDataInfoList.isEmpty()) {
            showDataWithLevel(-1, "");
        }
    }

    private void initSearchMap(String pathName,String pathId,List<SobotCusFieldDataInfo> fristList){
        //遍历全量数据，整合到map中
        List<SobotCusFieldDataInfo> list = new ArrayList<>();
        for (int i = 0; i < fristList.size(); i++) {
            fristList.get(i).setPathName((StringUtils.isNoEmpty(pathName)?(pathName+" / "):"")+fristList.get(i).getDataName());
            fristList.get(i).setPathId((StringUtils.isNoEmpty(pathId)?(pathId+","):"")+fristList.get(i).getDataId());
            if(fristList.get(i).isHasNext()){
                list.add(fristList.get(i));
            }else{
                searchList.add(fristList.get(i));
            }
        }
        if(!list.isEmpty()){
            for (int i = 0; i < list.size(); i++) {
                initSearchMap(list.get(i).getPathName(),list.get(i).getPathId(), getNextLevelList(list.get(i).getDataId()));
            }
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
            categoryAdapter = new SobotPostCascadeAdapter(SobotPostCascadeActivity.this, SobotPostCascadeActivity.this, tmpDatas, new SobotPostCascadeAdapter.SobotCallBack() {
                @Override
                public void itemClick(SobotCusFieldDataInfo info) {
                    sobot_et_search.clearFocus();
                    if(StringUtils.isNoEmpty(sobot_et_search.getText().toString())){
                        sobot_et_search.setText("");
                        //回显回去
                        Intent intent = new Intent();
                        intent.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
                        intent.putExtra("fieldType", ZhiChiConstant.WORK_ORDER_CUSTOMER_FIELD_CASCADE_TYPE);

                        intent.putExtra("category_typeName", info.getPathName());
                        intent.putExtra("category_fieldId", fieldId);
                        intent.putExtra("category_typeValue", info.getPathId());
                        setResult(ZhiChiConstant.work_order_list_display_type_category, intent);
                        finish();
                    }else {
                        selectCusFieldDataInfos.add(info);
                        if (getNextLevelList(info.getDataId()).size() > 0) {
                            currentLevel++;
                            showDataWithLevel(1, info.getDataId());
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
                                    typeName = typeName + selectCusFieldDataInfos.get(i).getDataName() + " / ";
                                    typeValue = typeValue + selectCusFieldDataInfos.get(i).getDataValue() + ",";
                                }
                            }
                            intent.putExtra("category_typeName", typeName);
                            intent.putExtra("category_fieldId", fieldId);
                            intent.putExtra("category_typeValue", typeValue);
                            setResult(ZhiChiConstant.work_order_list_display_type_category, intent);
                            for (int i = 0; i < tmpMap.get(currentLevel).size(); i++) {
                                tmpMap.get(currentLevel).get(i).setChecked(tmpMap.get(currentLevel).get(i).getDataId().equals(info.getDataId()));
                            }
                            categoryAdapter.notifyDataSetChanged();
                            finish();
                        }
                    }
                }
            });
            categoryAdapter.setSearchList(searchList);
            listView.setAdapter(categoryAdapter);
        }
        updateIndicator();
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
                            if (currentLevel > 0) {
                                SobotCusFieldDataInfo select = selectCusFieldDataInfos.get(currentLevel);
                                showDataWithLevel(currentLevel, select.getDataId());
                                if (selectCusFieldDataInfos.size() > 1) {
                                    int count = selectCusFieldDataInfos.size() - currentLevel - 1;
                                    if (count > 0) {
                                        for (int j = 0; j < count; j++) {
                                            selectCusFieldDataInfos.remove(selectCusFieldDataInfos.size() - 1);
                                        }
                                    }
                                }
                            } else {
                                //显示全部
                                if (selectCusFieldDataInfos.size() > 1) {
                                    int count = selectCusFieldDataInfos.size() - 1;
                                    for (int j = 0; j < count; j++) {
                                        selectCusFieldDataInfos.remove(selectCusFieldDataInfos.size() - 1);
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