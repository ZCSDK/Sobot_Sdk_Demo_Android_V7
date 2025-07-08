package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotRegionAdapter;
import com.sobot.chat.adapter.SobotSearchRegionAdapter;
import com.sobot.chat.api.model.PlaceModel;
import com.sobot.chat.api.model.RegionModel;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.dialog.DialogItemOnClick;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.utils.SobotLogUtils;
import com.sobot.widget.loading.SobotLoadingLayout;
import com.sobot.widget.refresh.layout.SobotRefreshLayout;
import com.sobot.widget.refresh.layout.api.RefreshLayout;
import com.sobot.widget.refresh.layout.footer.ClassicsFooter;
import com.sobot.widget.refresh.layout.header.ClassicsHeader;
import com.sobot.widget.refresh.layout.listener.OnLoadMoreListener;
import com.sobot.widget.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义字段--地区
 */
public class SobotPostRegionActivity extends SobotDialogBaseActivity implements View.OnClickListener {

    private LinearLayout  ll_level;
    private TextView sobot_tv_title;
    private TextView btnSubmit;
    private String[] selectedIdArr;
    private String[] selectedTextArr;
    private int maxLeve = 1;//最大层级


    private LinearLayout ll_data;
    private RecyclerView rv_list;
    private SobotRegionAdapter adapter;
    private Map<Integer, List<PlaceModel>> listMap;//层级中的列表
    private Map<Integer, PlaceModel> checkList;//已选中的
    private List<PlaceModel> provinceList;//省
    private int curLevel = 0;//当前层级
    private String pId = "0";//请求省县区用的pid

    //搜索框
    private LinearLayout ll_search;
    private EditText et_search;//搜索
    private ImageView iv_clear;

    //搜索内容
    private LinearLayout ll_search_data;
    private RecyclerView search_list;
    private SobotSearchRegionAdapter searchAdapter;
    private SobotRefreshLayout refreshLayout;//上下刷新，搜索用到加载更多
    private SobotLoadingLayout loading_layout;//无数据时显示
    private List<RegionModel> searchList;//搜索列表
    private RegionModel selectRegion;//搜索列表选中的地区
    private int pageNo;//搜索用到的分页
    private TextView tv_search_text_level;//搜索级别提示
    private TextView tv_select_text;//已选内容

    //数据
    private SobotCusFieldConfig cusFieldConfig;//自定义字段

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_select_region;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            String selectedIds = bundle.getString("selectedIds");
            String selectedText = bundle.getString("selectedText");
            cusFieldConfig = (SobotCusFieldConfig) bundle.getSerializable("cusFieldConfig");
            if (!TextUtils.isEmpty(selectedIds)) {
                selectedIdArr = selectedIds.split(",");
            }
            if (!TextUtils.isEmpty(selectedText)) {
                selectedTextArr = selectedText.split(",");
            }
            if (cusFieldConfig != null) {
                maxLeve = cusFieldConfig.getRegionalLevel();
            }
        }
    }

    @Override
    protected void initView() {

        listMap = new HashMap<>();
        provinceList = new ArrayList<>();
        checkList = new HashMap<>();
        searchList = new ArrayList<>();

        sobot_tv_title = findViewById(R.id.sobot_tv_title);
        btnSubmit = findViewById(R.id.btn_save);

        if (ThemeUtils.isChangedThemeColor(this)) {
            int themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = btnSubmit.getBackground();
            if (bg != null) {
                btnSubmit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
        }
        et_search = findViewById(R.id.et_search);
        ll_search = findViewById(R.id.ll_search);
        tv_search_text_level = findViewById(R.id.tv_search_text_level);
        tv_select_text = findViewById(R.id.tv_select_text);
        ll_search_data = findViewById(R.id.ll_search_data);
        ll_data = findViewById(R.id.ll_data);
        iv_clear = findViewById(R.id.sobot_iv_clear);
        iv_clear.setOnClickListener(this);
        refreshLayout = findViewById(R.id.sobot_srl_workorder_search);
        loading_layout = findViewById(R.id.sobot_loading_layout);
        loading_layout.setEmpty(R.layout.sobot_list_no_data);
        ll_level = findViewById(R.id.ll_level);
        rv_list = findViewById(R.id.rv_list);
        search_list = findViewById(R.id.search_list);
        if (cusFieldConfig != null) {
            sobot_tv_title.setText(cusFieldConfig.getFieldName());
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        rv_list.setLayoutManager(layoutManager);
        search_list.setLayoutManager(new LinearLayoutManager(SobotPostRegionActivity.this, LinearLayoutManager.VERTICAL, false));
        searchAdapter = new SobotSearchRegionAdapter(SobotPostRegionActivity.this, searchList, new SobotSearchRegionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RegionModel regionModel) {
                selectRegion = regionModel;
                checkList.clear();
                tv_select_text.setVisibility(View.GONE);
            }
        });
        search_list.setAdapter(searchAdapter);

        refreshLayout.setRefreshHeader(new ClassicsHeader(SobotPostRegionActivity.this));
        refreshLayout.setRefreshFooter(new ClassicsFooter(SobotPostRegionActivity.this));
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                pageNo = pageNo + 1;
                searchDate();
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageNo = 1;
                provinceList.clear();
                searchDate();
            }
        });
        loading_layout.showContent();
        btnSubmit.setOnClickListener(this);
        et_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnSubmit.setVisibility(View.VISIBLE);
                    KeyboardUtil.showKeyboard(et_search);
                    //列表中显示搜索
                    listMap.put(curLevel, provinceList);
                    ll_search_data.setVisibility(View.VISIBLE);
                    ll_data.setVisibility(View.GONE);
                    if (checkList.size() > 0 && checkList.size() == maxLeve) {
                        StringBuilder sd = new StringBuilder("已选：");
                        for (int key : checkList.keySet()) {
                            if (checkList.get(key) != null) {
                                sd.append(checkList.get(key).getName());
                                sd.append("/");
                            }
                        }
                        if (sd.length() > 0) {
                            String tmp = sd.toString();
                            if (tmp.endsWith("/")) {
                                tmp = tmp.substring(0, tmp.length() - 1);
                            }
                            tv_select_text.setText(tmp);
                            tv_select_text.setVisibility(View.VISIBLE);
                        }
                        refreshLayout.setNoMoreData(false);
                    } else {
                        //如果选中的
                        if (selectedTextArr != null && selectedTextArr.length > 0) {
                            StringBuilder sd = new StringBuilder("已选：");
                            for (int i = 0; i < selectedTextArr.length; i++) {
                                sd.append(selectedTextArr[i]);
                                sd.append("/");
                            }
                            if (sd.length() > 0) {
                                String tmp = sd.toString();
                                if (tmp.endsWith("/")) {
                                    tmp = tmp.substring(0, tmp.length() - 1);
                                }
                                tv_select_text.setText(tmp);
                                tv_select_text.setVisibility(View.VISIBLE);
                            } else {
                                tv_select_text.setVisibility(View.GONE);
                            }
                        } else {
                            tv_select_text.setVisibility(View.GONE);
                        }
                    }
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
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++) {
                        str1 += str[i];
                    }
                    et_search.setText(str1);
                    et_search.setSelection(start);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int inputCount = s.length();
                if (inputCount > 0) {
                    iv_clear.setVisibility(View.VISIBLE);
                } else {
                    iv_clear.setVisibility(View.GONE);
                }
                pageNo = 1;
                searchDate();
            }
        });
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    //键盘上的搜索
                    pageNo = 1;
                    searchDate();
                    return true;
                }
                return false;
            }
        });
        if (maxLeve == 1) {
            tv_search_text_level.setText(R.string.sobot_region_level1);
        } else if (maxLeve == 2) {
            tv_search_text_level.setText(R.string.sobot_region_level2);
        } else if (maxLeve == 3) {
            tv_search_text_level.setText(R.string.sobot_region_level3);
        } else if (maxLeve == 4) {
            tv_search_text_level.setText(R.string.sobot_region_level4);
        } else {
            tv_search_text_level.setVisibility(View.GONE);
        }
    }


    @Override
    protected void initData() {
        adapter = new SobotRegionAdapter(SobotPostRegionActivity.this, provinceList, new DialogItemOnClick() {
            @Override
            public void selectItem(Object selectObj) {
                int index = (int) selectObj;
                if (index < provinceList.size() && provinceList.get(index) != null) {
                    List<PlaceModel> list = new ArrayList<>(provinceList);
                    checkList.put(curLevel, list.get(index));
                    if (provinceList.get(index).isHasChild()) {
                        addLevelText();
                        //记录上一级的id
                        pId = provinceList.get(index).getId();
                        //缓存列表，已选的置顶显示
                        for (int i = 0; i < list.size(); i++) {
                            PlaceModel selectP = list.get(index);
                            if (selectP != null && selectP.getId().equals(list.get(i).getId())) {
                                PlaceModel p = list.get(i);
                                list.remove(i);
                                list.add(0, p);
                                break;
                            }
                        }
                        listMap.put(curLevel, list);
                        curLevel++;
                        requestDate();
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
        rv_list.setAdapter(adapter);
        requestDate();
    }

    //升级
    private void addLevelText() {
        ll_level.setVisibility(View.VISIBLE);
        ll_level.removeAllViews();
        TextView view = (TextView) View.inflate(SobotPostRegionActivity.this, R.layout.sobot_item_select_level, null);
        StringBuilder title = new StringBuilder("");
        for (int key : checkList.keySet()) {
            if (checkList.get(key) != null) {
                title.append(checkList.get(key).getName());
            }
            title.append("/");
        }
        String titleStr = title.toString();
        if (titleStr.endsWith("/")) {
            titleStr = titleStr.substring(0, titleStr.length() - 1);
        }
        view.setText(titleStr);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                levelLow();
            }
        });
        ll_level.addView(view);
    }

    //降级
    private void levelLow() {
        checkList.remove(curLevel);
        curLevel--;
        if (checkList.get(curLevel) != null) {
            pId = checkList.get(curLevel).getId();
            adapter.setSelectId(pId);
        }
        btnSubmit.setVisibility(View.GONE);
        ll_level.removeAllViews();
        SobotLogUtils.d("=====checkList====" + checkList.size());
        if (curLevel == 0) {
            ll_level.setVisibility(View.GONE);
        } else {
            ll_level.setVisibility(View.VISIBLE);
            TextView view = (TextView) View.inflate(SobotPostRegionActivity.this, R.layout.sobot_item_select_level, null);
            StringBuilder title = new StringBuilder("");
            for (int key : checkList.keySet()) {
                if (key < curLevel) {
                    if (checkList.get(key) != null) {
                        title.append(checkList.get(key).getName());
                    }
                    title.append("/");
                }
            }
            String titleStr = title.toString();
            if (titleStr.endsWith("/")) {
                titleStr = titleStr.substring(0, titleStr.length() - 1);
            }
            view.setText(titleStr);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    levelLow();
                }
            });
            ll_level.addView(view);
        }
        List<PlaceModel> list = listMap.get(curLevel);
        provinceList.clear();
        if (list != null) {
            provinceList.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 根据级别查询，不需要分页，
     */
    private void requestDate() {
        provinceList.clear();
        //最后一级才显示确定按钮
        if (curLevel == maxLeve - 1) {
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            btnSubmit.setVisibility(View.GONE);
        }
        zhiChiApi.getTicketRegion(getContext(), pId, new SobotResultCallBack<List<PlaceModel>>() {
            @Override
            public void onSuccess(List<PlaceModel> placeModels) {
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                if (null != placeModels && placeModels.size() > 0) {
                    provinceList.clear();
                    PlaceModel selectP = checkList.get(curLevel);
                    for (int i = 0; i < placeModels.size(); i++) {
                        if (placeModels.get(i).getLevel() < maxLeve) {
                            placeModels.get(i).setHasChild(true);
                        } else {
                            placeModels.get(i).setHasChild(false);
                        }
                        if (selectP != null && selectP.getId().equals(placeModels.get(i).getId())) {
                            provinceList.add(0, placeModels.get(i));
                        } else if (selectedIdArr != null && selectedIdArr.length > 0 && curLevel < selectedIdArr.length && selectedIdArr[curLevel].equals(placeModels.get(i).getId())) {
                            provinceList.add(0, placeModels.get(i));
                        } else {
                            provinceList.add(placeModels.get(i));
                        }
                    }
                    if (selectedIdArr != null && curLevel < selectedIdArr.length) {
                        adapter.setSelectId(selectedIdArr[curLevel]);
                    }
                    adapter.notifyDataSetChanged();
                    refreshLayout.setNoMoreData(false);
                    loading_layout.showContent();
                } else {
                    loading_layout.showEmpty();
                    refreshLayout.setNoMoreData(true);
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                requestDate();
            }
        });
    }

    /**
     * 搜索 需要分页查询
     */
    private void searchDate() {
        if (pageNo == 1) {
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
        }
        final String text = et_search.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            zhiChiApi.searchTicketRegion(getContext(), pageNo, text, maxLeve, new SobotResultCallBack<List<RegionModel>>() {
                @Override
                public void onSuccess(List<RegionModel> placeModels) {
                    refreshLayout.finishLoadMore();
                    if (null != placeModels && placeModels.size() > 0) {
                        loading_layout.showContent();
                        searchList.addAll(placeModels);
                        searchAdapter.setDate(text);
                        if (placeModels.size() < 15) {
                            refreshLayout.setNoMoreData(true);
                        }
                    } else {
                        if (pageNo == 1) {
                            loading_layout.showEmpty();
                        }
                    }
                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    private void backPressed() {
        finish();
    }


    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            // 搜索
            if (selectRegion != null) {
                StringBuilder tempCode = new StringBuilder(selectRegion.getProvinceCode());
                StringBuilder texts = new StringBuilder(selectRegion.getProvince());
                if (!TextUtils.isEmpty(selectRegion.getCityCode())) {
                    tempCode.append(",");
                    texts.append(",");
                    tempCode.append(selectRegion.getCityCode());
                    texts.append(selectRegion.getCity());

                }
                if (!TextUtils.isEmpty(selectRegion.getAreaCode())) {
                    tempCode.append(",");
                    texts.append(",");
                    tempCode.append(selectRegion.getAreaCode());
                    texts.append(selectRegion.getArea());
                }
                if (!TextUtils.isEmpty(selectRegion.getStreetCode())) {
                    tempCode.append(",");
                    texts.append(",");
                    tempCode.append(selectRegion.getStreetCode());
                    texts.append(selectRegion.getStreet());
                }
                cusFieldConfig.setValue(tempCode.toString());
                cusFieldConfig.setText(texts.toString());
                Intent data = new Intent();
                data.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
                data.putExtra("fieldType", cusFieldConfig.getFieldType());
                data.putExtra("category_typeName", texts.toString());
                data.putExtra("category_fieldId", cusFieldConfig.getFieldId());
                data.putExtra("category_typeValue", tempCode.toString());
                setResult(cusFieldConfig.getFieldType(), data);
//                    listener.selectCity(tempCode.toString(), texts.toString());
                finish();
            } else {
                //选择
                if (curLevel == (maxLeve - 1) && checkList.get(curLevel) != null) {
                    StringBuilder codes = new StringBuilder("");
                    StringBuilder texts = new StringBuilder("");
                    for (int key : checkList.keySet()) {
                        if (checkList.get(key) != null) {
                            codes.append(checkList.get(key).getId());
                            texts.append(checkList.get(key).getName());
                            if (checkList.size() > 1 && key < checkList.size() - 1) {
                                codes.append(",");
                                texts.append(",");
                            }
                        }
                    }
                    cusFieldConfig.setValue(codes.toString());
                    cusFieldConfig.setText(texts.toString());
                    Intent data = new Intent();
                    data.putExtra("CATEGORYSMALL", "CATEGORYSMALL");
                    data.putExtra("fieldType", cusFieldConfig.getFieldType());
                    data.putExtra("category_typeName", texts.toString());
                    data.putExtra("category_fieldId", cusFieldConfig.getFieldId());
                    data.putExtra("category_typeValue", codes.toString());
                    setResult(cusFieldConfig.getFieldType(), data);
//                        listener.selectCity(codes.toString(), codes.toString());
                    finish();
                }
            }
        } else if (v == iv_clear) {
            //清空输入，返回按时区显示
            et_search.setText("");
            et_search.clearFocus();
            //列表中显示搜索
            selectRegion = null;
            ll_search_data.setVisibility(View.GONE);
            ll_data.setVisibility(View.VISIBLE);
//        } else if (v == iv_search) {
//            pageNo = 1;
//            et_search.clearFocus();
//            searchDate();
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(getContext());
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }
}