package com.sobot.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.base.SobotBaseHelpCenterActivity;
import com.sobot.chat.adapter.SobotCategoryAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.StCategoryModel;
import com.sobot.chat.api.model.StDocModel;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.statusbar.StatusBarUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.List;

/**
 * 帮助中心问题分类
 */
public class SobotProblemCategoryActivity extends SobotBaseHelpCenterActivity implements AdapterView.OnItemClickListener {
    public static final String EXTRA_KEY_CATEGORY = "EXTRA_KEY_CATEGORY";
    private StCategoryModel mCategory;
    private ListView mListView;
    private TextView mEmpty;
    private SobotCategoryAdapter mAdapter;
    private  HelpConfigModel configModel;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_problem_category;
    }

    public static Intent newIntent(Context context, Information information, StCategoryModel data, HelpConfigModel configModel) {
        Intent intent = new Intent(context, SobotProblemCategoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ZhiChiConstant.SOBOT_BUNDLE_INFO, information);
        intent.putExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, bundle);
        intent.putExtra(EXTRA_KEY_CATEGORY, data);
        intent.putExtra("configModel", configModel);
        return intent;
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        super.initBundleData(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mCategory = (StCategoryModel) intent.getSerializableExtra(EXTRA_KEY_CATEGORY);
            configModel= (HelpConfigModel) intent.getSerializableExtra("configModel");
        }
    }

    @Override
    protected void initView() {
        showLeftMenu( true);
        mListView = (ListView) findViewById(R.id.sobot_listview);
        mEmpty = (TextView) findViewById(R.id.sobot_tv_empty);
        mEmpty.setText(R.string.sobot_no_content);
        setTitle(mCategory.getCategoryName());
        mListView.setOnItemClickListener(this);
    }

    //设置导航条颜色
    private void setToobar(HelpConfigModel configModel) {
        if (configModel != null) {
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
        configModel= (HelpConfigModel) getIntent().getSerializableExtra("configModel");
        if (configModel!=null){
            setToobar(configModel);
        }
        ZhiChiApi api = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        api.getHelpDocByCategoryId(SobotProblemCategoryActivity.this, mCategory.getAppId(), mCategory.getCategoryId(), new StringResultCallBack<List<StDocModel>>() {
            @Override
            public void onSuccess(List<StDocModel> datas) {
                if (datas != null) {
                    if (mAdapter == null) {
                        mAdapter = new SobotCategoryAdapter(getApplicationContext(),SobotProblemCategoryActivity.this, datas);
                        mListView.setAdapter(mAdapter);
                    } else {
                        List<StDocModel> list = mAdapter.getDatas();
                        list.clear();
                        list.addAll(datas);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                if (datas == null || datas.size() == 0) {
                    mEmpty.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                } else {
                    mEmpty.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<StDocModel> datas = mAdapter.getDatas();
        StDocModel stDocModel = datas.get(position);
        Intent intent = SobotProblemDetailActivity.newIntent(getApplicationContext(), mInfo, stDocModel,configModel);
        startActivity(intent);
    }
}