package com.sobot.chat.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.base.SobotBaseHelpCenterActivity;
import com.sobot.chat.adapter.SobotHelpCenterAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.StCategoryModel;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.widget.SobotAutoGridView;
import com.sobot.chat.widget.statusbar.StatusBarUtil;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.utils.SobotSharedPreferencesUtil;
import com.sobot.utils.SobotStringUtils;

import java.util.List;

/**
 * 帮助中心
 */
public class SobotHelpCenterActivity extends SobotBaseHelpCenterActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    //空态页面
    private View mEmptyView;
    private LinearLayout ll_bottom, ll_bottom_h, ll_bottom_v;
    private TextView tv_sobot_layout_online_service, tv_sobot_layout_online_service_v;
    private TextView tv_sobot_layout_online_tel, tv_sobot_layout_online_tel_v;
    private View view_split_online_tel;
    private SobotAutoGridView mGridView;
    private SobotHelpCenterAdapter mAdapter;
    private TextView tvNoData;
    private TextView tvNoDataDescribe;
    private TextView tvOnlineService;
    private String tel;
    private HelpConfigModel configModel;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_help_center;
    }

    @Override
    protected void initView() {
        setTitle(R.string.sobot_help_center_title);
        showLeftMenu(true);
        mEmptyView = findViewById(R.id.ll_empty_view);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_bottom_h = findViewById(R.id.ll_bottom_h);
        ll_bottom_v = findViewById(R.id.ll_bottom_v);
        tv_sobot_layout_online_service = findViewById(R.id.tv_sobot_layout_online_service);
        tv_sobot_layout_online_service_v = findViewById(R.id.tv_sobot_layout_online_service_v);
        tv_sobot_layout_online_tel = findViewById(R.id.tv_sobot_layout_online_tel);
        tv_sobot_layout_online_tel_v = findViewById(R.id.tv_sobot_layout_online_tel_v);
        view_split_online_tel = findViewById(R.id.view_split_online_tel);
        mGridView = findViewById(R.id.sobot_gv);
        mGridView.setSelector(android.R.color.transparent);
        tvNoData = findViewById(R.id.tv_sobot_help_center_no_data);
        tvNoData.setText(R.string.sobot_help_center_no_data);
        tvNoDataDescribe = findViewById(R.id.tv_sobot_help_center_no_data_describe);
        tvNoDataDescribe.setText(R.string.sobot_help_center_no_data_describe);
        tvOnlineService = findViewById(R.id.tv_sobot_layout_online_service);
        tvOnlineService.setText(R.string.sobot_help_center_online_service);
        tv_sobot_layout_online_service.setOnClickListener(this);
        tv_sobot_layout_online_tel.setOnClickListener(this);
        tv_sobot_layout_online_service_v.setOnClickListener(this);
        tv_sobot_layout_online_tel_v.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        configModel = (HelpConfigModel) SharedPreferencesUtil.getObject(getSobotBaseActivity(), "SobotHelpConfigModel");
        try {
            View decorView = getWindow().getDecorView();
            ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                    LogUtils.d("SobotHelpCenterActivity 状态栏高度: " + statusBarHeight);
                    StatusBarUtil.SOBOT_STATUS_HIGHT = statusBarHeight;
                    if (SobotApp.getApplicationContext() != null) {
                        SobotSharedPreferencesUtil.getInstance(SobotApp.getApplicationContext()).put("SobotStatusBarHeight", statusBarHeight);
                    }
                    setTool();
                    return insets;
                }
            });
        } catch (Exception e) {
            setTool();
        }
        displayInNotch(mGridView);
        displayInNotch(ll_bottom);
    }

    private void setTool() {
        if (configModel != null) {
            setToobar(configModel);
        }
        SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi().getHelpConfig(this, mInfo.getApp_key(), mInfo.getPartnerid(), new SobotResultCallBack<HelpConfigModel>() {

            @Override
            public void onSuccess(HelpConfigModel o) {
                configModel = o;
                setToobar(o);
            }

            @Override
            public void onFailure(Exception e, String s) {

            }
        });
    }

    //设置导航条颜色
    private void setToobar(HelpConfigModel configModel) {
        this.configModel = configModel;
        if (configModel != null) {
            SharedPreferencesUtil.saveObject(getSobotBaseActivity(), "SobotHelpConfigModel", configModel);
            if (mInfo != null && SobotStringUtils.isNoEmpty(mInfo.getHelpCenterTelTitle()) && SobotStringUtils.isNoEmpty(mInfo.getHelpCenterTel())) {
                tel = mInfo.getHelpCenterTel();
                tv_sobot_layout_online_tel.setText(mInfo.getHelpCenterTelTitle());
                tv_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                tv_sobot_layout_online_tel_v.setText(mInfo.getHelpCenterTelTitle());
                view_split_online_tel.setVisibility(View.VISIBLE);
                tv_sobot_layout_online_tel.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = tv_sobot_layout_online_tel.getLineCount();
                        if (lineCount > 1) {
                            ll_bottom_h.setVisibility(View.GONE);
                            ll_bottom_v.setVisibility(View.VISIBLE);
                        } else {
                            ll_bottom_h.setVisibility(View.VISIBLE);
                            ll_bottom_v.setVisibility(View.GONE);
                        }
                    }
                }, 100);
            } else {
                if (!TextUtils.isEmpty(configModel.getHotlineName()) && !TextUtils.isEmpty(configModel.getHotlineTel())) {
                    tel = configModel.getHotlineTel();
                    tv_sobot_layout_online_tel.setText(configModel.getHotlineName());
                    tv_sobot_layout_online_tel_v.setText(configModel.getHotlineName());
                    tv_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                    view_split_online_tel.setVisibility(View.VISIBLE);
                    tv_sobot_layout_online_tel.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int lineCount = tv_sobot_layout_online_tel.getLineCount();
                            if (lineCount > 1) {
                                ll_bottom_h.setVisibility(View.GONE);
                                ll_bottom_v.setVisibility(View.VISIBLE);
                            } else {
                                ll_bottom_h.setVisibility(View.VISIBLE);
                                ll_bottom_v.setVisibility(View.GONE);
                            }
                        }
                    }, 100);
                } else {
                    tv_sobot_layout_online_tel.setVisibility(View.GONE);
                    view_split_online_tel.setVisibility(View.GONE);
                }
            }
            //服务端返回的导航条背景颜色
            if (!TextUtils.isEmpty(configModel.getTopBarColor())) {
                String topBarColor[] = configModel.getTopBarColor().split(",");
                if (topBarColor.length > 1) {
                    int[] colors = new int[topBarColor.length];
                    for (int i = 0; i < topBarColor.length; i++) {
                        colors[i] = Color.parseColor(topBarColor[i]);
                    }
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColors(colors); //添加颜色组
                    gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                    gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                    getToolBar().setBackground(gradientDrawable);
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
                    } else {
                        StatusBarUtil.setColor(getSobotBaseActivity(), aDrawable);
                    }
                }
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
                if (datas != null && datas.size() > 0) {
                    mEmptyView.setVisibility(View.GONE);
                    mGridView.setVisibility(View.VISIBLE);
                    if (mAdapter == null) {
                        mAdapter = new SobotHelpCenterAdapter(getApplicationContext(), datas);
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
        if (v == tv_sobot_layout_online_service || v == tv_sobot_layout_online_service_v) {
            if (SobotOption.openChatListener != null) {
                boolean isIntercept = SobotOption.openChatListener.onOpenChatClick(getSobotBaseActivity(), mInfo);
                if (isIntercept) {
                    return;
                }
            }
            ZCSobotApi.openZCChat(getApplicationContext(), mInfo);
        }
        if (v == tv_sobot_layout_online_tel || v == tv_sobot_layout_online_tel_v) {
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