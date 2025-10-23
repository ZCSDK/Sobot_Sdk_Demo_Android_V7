package com.sobot.chat.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.base.SobotBaseHelpCenterActivity;
import com.sobot.chat.adapter.SobotHelpCenterAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.StCategoryModel;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.widget.SobotAutoGridView;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帮助中心
 */
public class SobotHelpCenterActivity extends SobotBaseHelpCenterActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //空态页面
    private View mEmptyView;
    private SobotAutoGridView mGridView;
    private SobotHelpCenterAdapter mAdapter;
    private TextView tvNoData;
    private TextView tvNoDataDescribe;
    public LinearLayout ll_bottom, ll_bottom_h, ll_bottom_v;
    public TextView tv_sobot_layout_online_tel, tv_sobot_layout_online_tel_v;
    public View view_split_online_tel;
    public TextView tvOnlineService;
    public String tel;
    public LinearLayout ll_sobot_layout_online_service, ll_sobot_layout_online_service_v;
    public LinearLayout ll_sobot_layout_online_tel, ll_sobot_layout_online_tel_v;

    public HelpConfigModel configModel;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_help_center;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotHelpCenterActivity";
    }

    @Override
    protected void initView() {
        setTitle(R.string.sobot_help_center_title);
        showLeftMenu(true);
        mEmptyView = findViewById(R.id.ll_empty_view);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_bottom_h = findViewById(R.id.ll_bottom_h);
        ll_bottom_v = findViewById(R.id.ll_bottom_v);
        ll_sobot_layout_online_service = findViewById(R.id.ll_sobot_layout_online_service);
        ll_sobot_layout_online_service_v = findViewById(R.id.ll_sobot_layout_online_service_v);
        ll_sobot_layout_online_tel = findViewById(R.id.ll_sobot_layout_online_tel);
        ll_sobot_layout_online_tel_v = findViewById(R.id.ll_sobot_layout_online_tel_v);
        tv_sobot_layout_online_tel = findViewById(R.id.tv_sobot_layout_online_tel);
        tv_sobot_layout_online_tel_v = findViewById(R.id.tv_sobot_layout_online_tel_v);
        view_split_online_tel = findViewById(R.id.view_split_online_tel);
        mGridView = findViewById(R.id.sobot_gv);
        mGridView.setSelector(android.R.color.transparent);
        tvNoData = findViewById(R.id.tv_sobot_help_center_no_data);
        tvNoData.setText(R.string.sobot_help_center_no_data);
        tvNoDataDescribe = findViewById(R.id.tv_sobot_help_center_no_data_describe);
        tvNoDataDescribe.setText(R.string.sobot_help_center_no_data_describe);
        ll_sobot_layout_online_service.setOnClickListener(this);
        ll_sobot_layout_online_tel.setOnClickListener(this);
        ll_sobot_layout_online_service_v.setOnClickListener(this);
        ll_sobot_layout_online_tel_v.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        configModel = (HelpConfigModel) SharedPreferencesUtil.getObject(getSobotBaseActivity(), "SobotHelpConfigModel");
        if (configModel != null) {
            setToolBarDefBg();
            setBottomBtnUI();
        }
        Map<String, Object> param = new HashMap();
        param.put("appId", mInfo.getApp_key());
        param.put("partnerId", mInfo.getPartnerid());
        if (!TextUtils.isEmpty(mInfo.getMulti_params())) {
            param.put("multiParams", mInfo.getMulti_params());
        }
        if (!TextUtils.isEmpty(mInfo.getIsVip())) {
            param.put("isVip", mInfo.getIsVip());
        }
        if (!TextUtils.isEmpty(mInfo.getVip_level())) {
            param.put("vipLevel", mInfo.getVip_level());
        }
        if (!TextUtils.isEmpty(mInfo.getUser_label())) {
            param.put("userLabel", mInfo.getUser_label());
        }
        if (!TextUtils.isEmpty(mInfo.getParams())) {
            param.put("params", mInfo.getParams());
        }
        if (!TextUtils.isEmpty(mInfo.getCustomer_fields())) {
            param.put("customerFields", mInfo.getCustomer_fields());
        }
        SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi().getVisitorAndHelpConfig(this, param, new SobotResultCallBack<HelpConfigModel>() {

            @Override
            public void onSuccess(HelpConfigModel o) {
                if (configModel != null) {
                    try {
                        int rebotThemeStyle = configModel.getRebotThemeStyle();
                        int appCompatDelegate;
                        //后台返回的主题模式RebotThemeStyle 0-浅色，1-深色，2-跟随系统
                        if (rebotThemeStyle == 2) {
                            appCompatDelegate = -1;
                        } else if (rebotThemeStyle == 0) {
                            appCompatDelegate = 1;
                        } else if (rebotThemeStyle == 1) {
                            appCompatDelegate = 2;
                        } else {
                            appCompatDelegate = -1;
                        }
                        SharedPreferencesUtil.saveIntData(getSobotBaseActivity(), "local_night_mode", appCompatDelegate);
                    } catch (Exception e) {
                    }
                }
                SharedPreferencesUtil.saveObject(getSobotBaseActivity(), "SobotHelpConfigModel", o);
                configModel = o;
                setToolBarDefBg();
                setBottomBtnUI();
            }

            @Override
            public void onFailure(Exception e, String s) {

            }
        });
        displayInNotch(mGridView);
        displayInNotch(ll_bottom);
    }

    //设置底部 按钮（在线客服和客服电话）
    public void setBottomBtnUI() {
        if (mInfo != null && StringUtils.isNoEmpty(mInfo.getHelpCenterTelTitle()) && StringUtils.isNoEmpty(mInfo.getHelpCenterTel())) {
            tel = mInfo.getHelpCenterTel();
            tv_sobot_layout_online_tel.setText(mInfo.getHelpCenterTelTitle());
            ll_sobot_layout_online_tel.setVisibility(View.VISIBLE);
            tv_sobot_layout_online_tel_v.setText(mInfo.getHelpCenterTelTitle());
            view_split_online_tel.setVisibility(View.VISIBLE);
            if (StringUtils.calculateTextLines(14, configModel.getHotlineName(), ScreenUtils.getScreenWidth(getSobotBaseActivity()) / 2 - ScreenUtils.dip2px(getSobotBaseActivity(), 16 + 4 + 20 + 14 + 8), getSobotBaseActivity()) < 2) {
                ll_bottom_h.setVisibility(View.VISIBLE);
                ll_bottom_v.setVisibility(View.GONE);
            } else {
                ll_bottom_h.setVisibility(View.GONE);
                ll_bottom_v.setVisibility(View.VISIBLE);
            }
        } else {
            if (!TextUtils.isEmpty(configModel.getHotlineName()) && !TextUtils.isEmpty(configModel.getHotlineTel())) {
                tel = configModel.getHotlineTel();
                tv_sobot_layout_online_tel.setText(configModel.getHotlineName());
                tv_sobot_layout_online_tel_v.setText(configModel.getHotlineName());
                ll_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                view_split_online_tel.setVisibility(View.VISIBLE);
                if (StringUtils.calculateTextLines(14, configModel.getHotlineName(), ScreenUtils.getScreenWidth(getSobotBaseActivity()) / 2 - ScreenUtils.dip2px(getSobotBaseActivity(), 16 + 4 + 20 + 14 + 8), getSobotBaseActivity()) < 2) {
                    ll_bottom_h.setVisibility(View.VISIBLE);
                    ll_bottom_v.setVisibility(View.GONE);
                } else {
                    ll_bottom_h.setVisibility(View.GONE);
                    ll_bottom_v.setVisibility(View.VISIBLE);
                }
            } else {
                ll_sobot_layout_online_tel.setVisibility(View.GONE);
                view_split_online_tel.setVisibility(View.GONE);
            }
        }
    }


    @Override
    protected void initData() {
        ZhiChiApi api = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        api.getCategoryList(SobotHelpCenterActivity.this, mInfo.getApp_key(), new StringResultCallBack<List<StCategoryModel>>() {
            @Override
            public void onSuccess(List<StCategoryModel> datas) {
                ll_bottom.setVisibility(View.VISIBLE);
                if (datas != null && !datas.isEmpty()) {
                    mEmptyView.setVisibility(View.GONE);
                    mGridView.setVisibility(View.VISIBLE);
                    if (mAdapter == null) {
                        mAdapter = new SobotHelpCenterAdapter(getSobotBaseActivity(), datas);
                        mGridView.setAdapter(mAdapter);
                    } else {
                        List<StCategoryModel> list = mAdapter.getDatas();
                        list.clear();
                        list.addAll(datas);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ll_bottom.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == ll_sobot_layout_online_service || v == ll_sobot_layout_online_service_v) {
            if (SobotOption.openChatListener != null) {
                boolean isIntercept = SobotOption.openChatListener.onOpenChatClick(getSobotBaseActivity(), mInfo);
                if (isIntercept) {
                    return;
                }
            }
            ZCSobotApi.openZCChat(getApplicationContext(), mInfo);
        }
        if (v == ll_sobot_layout_online_tel || v == ll_sobot_layout_online_tel_v) {
            if (tel != null && !TextUtils.isEmpty(tel)) {
                if (SobotOption.functionClickListener != null) {
                    SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_PhoneCustomerService);
                }
                if (SobotOption.newHyperlinkListener != null) {
                    boolean isIntercept = SobotOption.newHyperlinkListener.onPhoneClick(getSobotBaseActivity(), "tel:" + tel);
                    if (isIntercept) {
                        return;
                    }
                }
                ChatUtils.callUp(tel, getSobotBaseActivity());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<StCategoryModel> datas = mAdapter.getDatas();
        StCategoryModel data = datas.get(position);
        Intent intent = SobotProblemCategoryActivity.newIntent(getApplicationContext(), mInfo, data, configModel);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_CloseHelpCenter);
        }
    }
}