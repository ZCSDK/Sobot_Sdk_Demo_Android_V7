package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.adapter.SobotTicketInfoAdapter;
import com.sobot.chat.adapter.SobotTicketTmpsAdapter;
import com.sobot.chat.api.model.SobotPostMsgTemplate;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.LoadingView.SobotLoadingView;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.refresh.layout.util.SmartUtil;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言记录列表界面
 * 从+号中过来的，如果没有留言记录，请求模板，显示模板。选择模板后跳转到新建工单页面
 */
public class SobotTicketListActivity extends SobotChatBaseActivity implements View.OnClickListener {
    private final static int REQUEST_CODE = 0x001;
    private boolean isOnlyShowTicket;
    private String mUid = "";
    private String mGroupId = "";
    private String mCustomerId = "";
    private String mCompanyId = "";
    private int mFrom = 0;//0新建留言，1留言列表

    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;
    private ArrayList<SobotUserTicketInfo> mList ;
    private LinearLayout mllContent,mllLoading;
    private RecyclerView recyclerView;
    private TextView mNewTicket;
    private TextView sobotEmpty;
    private SobotLoadingView loading;
    private SobotTicketInfoAdapter mAdapter;

    //选择模板
    private SobotTicketTmpsAdapter tmpAdapter;
    private ArrayList<SobotPostMsgTemplate> templates;


    private MessageReceiver mReceiver;
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ChatUtils.SOBOT_ACTION_CLOSE_TIKET.equals(intent.getAction())) {
                finish();
            }
        }
    }
    private void initReceiver() {
        if (mReceiver == null) {
            mReceiver = new MessageReceiver();
        }
        // 创建过滤器，并指定action，使之用于接收同action的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChatUtils.SOBOT_ACTION_CLOSE_TIKET);
        LocalBroadcastManager.getInstance(getSobotBaseActivity()).registerReceiver(mReceiver, filter);
    }
    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_list;
    }

    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_UID);
            isOnlyShowTicket =getIntent().getBooleanExtra("isOnlyShowTicket",false);
            mGroupId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_GROUPID);
            mCustomerId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID);
            mCompanyId = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID);
            mFrom = getIntent().getIntExtra(StPostMsgPresenter.INTENT_KEY_FROM, StPostMsgPresenter.TICKET_TO_LIST);
            mList = (ArrayList<SobotUserTicketInfo>) getIntent().getSerializableExtra(StPostMsgPresenter.INTENT_KEY_TICKET_LIST);
            templates = (ArrayList<SobotPostMsgTemplate>) getIntent().getSerializableExtra(StPostMsgPresenter.INTENT_KEY_TEMP_LIST);
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketListActivity";
    }

    @Override
    protected void initView() {
        initReceiver();
        if(null == mList){
            mList = new ArrayList<>();
        }
        if(null == templates) {
            templates = new ArrayList<>();
        }
        if (ChatUtils.getStatusList() == null || ChatUtils.getStatusList().size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(this,
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(this, companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                }

                @Override
                public void onFailure(Exception e, String s) {
                }
            });
        }
        mllContent = findViewById(R.id.ll_content);
        mllLoading = findViewById(R.id.ll_loading);
        mllContent.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.sobot_listview);
        mNewTicket = findViewById(R.id.sobot_new_ticket);
        sobotEmpty = findViewById(R.id.sobot_empty);
        loading = findViewById(R.id.iv_loading);
        loading.setProgressColor(ThemeUtils.getThemeColor(this));
        Drawable bg = getResources().getDrawable(R.drawable.sobot_bg_theme_color_20dp);
        if (bg != null) {
            mNewTicket.setBackground(ThemeUtils.applyColorToDrawable(bg, ThemeUtils.getThemeColor(getSobotBaseContext())));
        }
        mNewTicket.setOnClickListener(this);
        mNewTicket.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
//        mNewTicket.setCompoundDrawables();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void initData() {
        if (mFrom == StPostMsgPresenter.TICKET_TO_LIST ) {
            //留言记录
            setTitle(R.string.sobot_message_record);
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(SobotTicketListActivity.this,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && ChatUtils.isFreeAccount(initMode.getAccountStatus())) {
                sobotFreeAccountTipDialog = new SobotFreeAccountTipDialog(SobotTicketListActivity.this, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sobotFreeAccountTipDialog.dismiss();
                        finish();
                    }
                });
                if (sobotFreeAccountTipDialog != null && !sobotFreeAccountTipDialog.isShowing()) {
                    sobotFreeAccountTipDialog.show();
                }
            } else {
                if(mList!=null && !mList.isEmpty()){
                    loading.stopSpinning();
                    mllLoading.setVisibility(View.GONE);
                    sobotEmpty.setVisibility(View.GONE);
                    mllContent.setVisibility(View.VISIBLE);
                    if(isOnlyShowTicket){
                        //仅显示留言记录，不显示新建
                        mNewTicket.setVisibility(View.GONE);
                    }else {
                        mNewTicket.setVisibility(View.VISIBLE);
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    //直接显示数据
                    mAdapter = new SobotTicketInfoAdapter(SobotTicketListActivity.this, mList, new SobotTicketInfoAdapter.SobotItemListener() {
                        @Override
                        public void onItemClick(SobotUserTicketInfo model) {
                            //详情
                            Intent intent = SobotTicketDetailActivity.newIntent(getSobotBaseActivity(), mCompanyId, mUid, model.getTicketId());
                            startActivityForResult(intent, REQUEST_CODE);
                        }
                    });
                    recyclerView.setAdapter(mAdapter);
                }else {
                    loading.startSpinning();
                    requestDate();
                }
            }
        }else{
            setTitle(R.string.sobot_please_leave_a_message);
            recyclerView.setPadding(0, SmartUtil.dp2px(12), 0, 0);
            if(null != templates && !templates.isEmpty()) {
                loading.stopSpinning();
                mllLoading.setVisibility(View.GONE);
                mllContent.setVisibility(View.VISIBLE);
                tmpAdapter = new SobotTicketTmpsAdapter(getSobotBaseActivity(), templates, new SobotTicketTmpsAdapter.ItemOnClick() {
                    @Override
                    public void onItemClick(SobotPostMsgTemplate itemBeen) {
                        //跳转到留言页面
                        gotoNewTicket(itemBeen.getTemplateId());
                    }
                });
                recyclerView.setAdapter(tmpAdapter);
            }else{
                loading.startSpinning();
                //新建请求模板
                requestTemps();
            }
        }

    }
    /**
     * 请求模板
     */
    private void requestTemps() {
        zhiChiApi.getWsTemplate(REQUEST_TAG, mUid, mGroupId, new StringResultCallBack<ArrayList<SobotPostMsgTemplate>>() {
            @Override
            public void onSuccess(ArrayList<SobotPostMsgTemplate> datas) {
                loading.stopSpinning();
                mllLoading.setVisibility(View.GONE);
                mllContent.setVisibility(View.VISIBLE);
                recyclerView.setPadding(ScreenUtils.dip2px(SobotTicketListActivity.this, 20),ScreenUtils.dip2px(SobotTicketListActivity.this, 16),ScreenUtils.dip2px(SobotTicketListActivity.this, 20),ScreenUtils.dip2px(SobotTicketListActivity.this, 24));
                if (datas != null && datas.size() > 0) {
                    if (datas.size() == 1) {
                        //只有一个 自动点选，跳转到留言页面
                        gotoNewTicket(datas.get(0).getTemplateId());
                        finish();
                    } else {
                        //显示列表
                        templates.clear();
                        templates.addAll(datas);
                        tmpAdapter = new SobotTicketTmpsAdapter(getSobotBaseActivity(), templates, new SobotTicketTmpsAdapter.ItemOnClick() {
                            @Override
                            public void onItemClick(SobotPostMsgTemplate itemBeen) {
                                //跳转到留言页面
                                gotoNewTicket(itemBeen.getTemplateId());
                            }
                        });
                        recyclerView.setAdapter(tmpAdapter);
                    }
                }else{
                    //跳转到新建工单页面
                    gotoNewTicket("");
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                //请求失败
                showHint(des);
            }
        });
    }

    /**
     * 请求留言列表
     */
    private void requestDate() {
        loading.startSpinning();
        zhiChiApi.getUserTicketInfoList(REQUEST_TAG, mUid, mCompanyId, mCustomerId, new StringResultCallBack<ArrayList<SobotUserTicketInfo>>() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(ArrayList<SobotUserTicketInfo> datas) {
                loading.stopSpinning();
                if (datas != null && datas.size() > 0) {
                    mllLoading.setVisibility(View.GONE);
                    sobotEmpty.setVisibility(View.GONE);
                    mllContent.setVisibility(View.VISIBLE);
                    if(isOnlyShowTicket){
                        //仅显示留言记录，不显示新建
                        mNewTicket.setVisibility(View.GONE);
                    }else {
                        mNewTicket.setVisibility(View.VISIBLE);
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    mList.clear();
                    mList.addAll(datas);
                    mAdapter = new SobotTicketInfoAdapter(SobotTicketListActivity.this, mList, new SobotTicketInfoAdapter.SobotItemListener() {
                        @Override
                        public void onItemClick(SobotUserTicketInfo model) {
                            //详情
                            Intent intent = SobotTicketDetailActivity.newIntent(getSobotBaseActivity(), mCompanyId, mUid, model.getTicketId());
                            startActivityForResult(intent, REQUEST_CODE);
                        }
                    });
                    recyclerView.setAdapter(mAdapter);
                } else {
                    if(isOnlyShowTicket){
                        //显示空态
                        mllLoading.setVisibility(View.GONE);
                        sobotEmpty.setVisibility(View.GONE);
                        mllContent.setVisibility(View.VISIBLE);
                        setTitle(R.string.sobot_message_record);
                        recyclerView.setVisibility(View.GONE);
                        sobotEmpty.setVisibility(View.VISIBLE);
                    }else{
                        //新建请求模板
                        setTitle(R.string.sobot_please_leave_a_message);
                        requestTemps();
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                LogUtils.i(des);
            }

        });
    }

    /**
     * 跳转到新建工单页面
     */
    private void gotoNewTicket(String tempId) {
        Intent intent = new Intent(SobotTicketListActivity.this, SobotTicketNewActivity.class);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, mGroupId);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_UID, mUid);
        intent.putExtra(StPostMsgPresenter.INTENT_KEY_TEMPID, tempId);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getSobotBaseActivity()).unregisterReceiver(mReceiver);
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_CloseLeave);
        }
        super.onDestroy();
    }
    long submitTime;//防止重复点击
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sobot_new_ticket) {
            if((System.currentTimeMillis()-submitTime)<5000){
                submitTime = System.currentTimeMillis();
            }else {
                submitTime = System.currentTimeMillis();
                //跳转到新建工单
                Intent intent2 = new Intent(this, SobotTicketListActivity.class);
                intent2.putExtra(StPostMsgPresenter.INTENT_KEY_UID, mUid);
                intent2.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
                intent2.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
                intent2.putExtra(ZhiChiConstant.FLAG_EXIT_SDK, false);
                intent2.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, mGroupId);
                intent2.putExtra(StPostMsgPresenter.INTENT_KEY_FROM, StPostMsgPresenter.TICKET_TO_NEW);
                startActivity(intent2);
            }
        }
    }
    public void showHint(String content) {
        if (!TextUtils.isEmpty(content)) {
            ToastUtil.showToast(getSobotBaseContext(), content);
        }
    }
}