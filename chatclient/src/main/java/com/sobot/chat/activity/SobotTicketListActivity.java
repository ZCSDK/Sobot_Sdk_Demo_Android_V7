package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.LoadingView.SobotLoadingView;
import com.sobot.chat.widget.SobotTicketGridItemDecoration;
import com.sobot.chat.widget.dialog.SobotFreeAccountTipDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 留言记录列表 / 留言模板选择页面（复用同一个 Activity）
 * <p>
 * 根据 {@code mFrom} 参数区分两种模式：
 * <ul>
 *   <li>{@link StPostMsgPresenter#TICKET_TO_LIST} — 留言记录列表模式：
 *       显示用户的历史留言工单列表，点击进入详情页 {@link SobotTicketDetailActivity}。
 *       底部有"新建留言"按钮，点击后请求模板列表，根据模板数量决定下一步：
 *       - 0个模板：直接跳转新建留言页 {@link SobotTicketNewActivity}
 *       - 1个模板：自动选择该模板，直接跳转新建留言页
 *       - 多个模板：打开新的模板选择页面
 *   </li>
 *   <li>{@link StPostMsgPresenter#TICKET_TO_NEW} — 模板选择模式：
 *       从聊天界面"+"号入口进入，显示模板列表供用户选择，选择后跳转新建留言页
 *   </li>
 * </ul>
 * <p>
 * 特殊逻辑：
 * - 免费账号会弹出提示弹窗，不显示留言列表
 * - 支持 {@code isOnlyShowTicket} 模式，仅显示留言记录不显示新建按钮
 * - 监听 {@link ChatUtils#SOBOT_ACTION_CLOSE_TIKET} 广播，用于关联页面联动关闭
 */
public class SobotTicketListActivity extends SobotChatBaseActivity implements View.OnClickListener {
    /**
     * 跳转详情页的请求码，用于详情页返回后刷新列表
     */
    private final static int REQUEST_CODE = 0x001;
    private boolean isOnlyShowTicket;   // 是否仅显示留言记录（不显示新建按钮）
    private String mUid = "";           // 用户ID
    private String mGroupId = "";       // 技能组ID
    private String mCustomerId = "";    // 客户ID
    private String mCompanyId = "";     // 企业ID
    private int mFrom = 0;             // 页面模式：0-留言记录列表，1-模板选择（新建留言入口）

    private SobotFreeAccountTipDialog sobotFreeAccountTipDialog;  // 免费账号提示弹窗
    private ArrayList<SobotUserTicketInfo> mList;   // 留言工单列表数据
    private LinearLayout mllContent, mllLoading;    // 内容区域、加载中区域
    private RecyclerView recyclerView;              // 列表控件（复用于工单列表和模板列表）
    private TextView mNewTicket;                    // "新建留言"按钮文字
    private LinearLayout ll_new_ticket;             // "新建留言"按钮容器
    private ImageView iv_new_ticket;                // "新建留言"按钮图标
    private TextView sobotEmpty;                    // 空态提示文字
    private SobotLoadingView loading;               // 加载动画
    private SobotTicketInfoAdapter mAdapter;        // 留言工单列表适配器

    private SobotTicketTmpsAdapter tmpAdapter;               // 模板列表适配器
    private ArrayList<SobotPostMsgTemplate> templates;       // 模板列表数据


    /**
     * 本地广播接收器，监听关闭留言页面的广播事件
     */
    private MessageReceiver mReceiver;

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ChatUtils.SOBOT_ACTION_CLOSE_TIKET.equals(intent.getAction())) {
                finish();
            }
        }
    }

    /**
     * 注册本地广播接收器，监听关闭留言页面事件
     */
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

    /**
     * 从 Intent 中解析传递的参数
     * 包括用户ID、企业ID、客户ID、技能组ID、页面模式、已有工单列表、模板列表等
     */
    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(StPostMsgPresenter.INTENT_KEY_UID);
            isOnlyShowTicket = getIntent().getBooleanExtra("isOnlyShowTicket", false);
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

    /**
     * 初始化视图控件
     * 注册广播接收器、初始化列表和加载动画、设置"新建留言"按钮样式和点击事件
     */
    @Override
    protected void initView() {
        initReceiver();
        if (null == mList) {
            mList = new ArrayList<>();
        }
        if (null == templates) {
            templates = new ArrayList<>();
        }
        showLeftMenu(true);
        mllContent = findViewById(R.id.ll_content);
        mllLoading = findViewById(R.id.ll_loading);
        mllContent.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.sobot_listview);
        displayInNotch(recyclerView);
        mNewTicket = findViewById(R.id.sobot_new_ticket);
        ll_new_ticket = findViewById(R.id.ll_new_ticket);
        iv_new_ticket = findViewById(R.id.iv_new_ticket);
        sobotEmpty = findViewById(R.id.sobot_empty);
        loading = findViewById(R.id.iv_loading);
        loading.setProgressColor(ThemeUtils.getThemeColor(this));
        Drawable bg = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_theme_color_20dp, null);
        if (bg != null) {
            ll_new_ticket.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, ThemeUtils.getThemeColor(getSobotBaseContext())));
        }
        ll_new_ticket.setOnClickListener(this);
        mNewTicket.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_ticket_add, null);
        iv_new_ticket.setImageDrawable(ThemeUtils.applyColorToDrawable(drawable, ThemeUtils.getThemeTextAndIconColor(this)));
        // 列数从 R.integer.sobot_list_span_count 取（values/=1, values-w600dp/=2, values-w600dp-h600dp/=1）：
        // 仅手机横屏走 2 列网格；手机竖屏 / Pad / 折叠屏内屏走 1 列
        int spanCount = getResources().getInteger(R.integer.sobot_list_span_count);
        if (spanCount > 1) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
            recyclerView.setLayoutManager(gridLayoutManager);
            int leftRight = getResources().getDimensionPixelSize(R.dimen.sobot_Left_right_margin_edge);
            // 模板模式下 item 自带 elevation+translationY 阴影，给 RecyclerView 底部留出阴影绘制空间，
            // 同时关闭 clip 让阴影不被边界裁切；记录模式下 padding 为 0dp，不影响视觉
            int extraBottom = (mFrom == StPostMsgPresenter.TICKET_TO_NEW)
                    ? getResources().getDimensionPixelSize(R.dimen.sobot_ticket_grid_template_shadow_padding)
                    : 0;
            recyclerView.setPadding(leftRight, recyclerView.getPaddingTop(),
                    leftRight, recyclerView.getPaddingBottom() + extraBottom);
            recyclerView.setClipToPadding(false);
            recyclerView.setClipChildren(false);
            // 两种模式列/行间距不同（记录 20dp、模板 12dp），由 ensureGridDecoration() 在 setAdapter 前注入
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            // 设置RecyclerView的LayoutManager
            recyclerView.setLayoutManager(layoutManager);
        }
    }

    /**
     * 宽屏下根据当前模式注入对应间距的网格 ItemDecoration。
     * 同一 RecyclerView 实例多次 setAdapter 时只注入一次，单列布局（竖屏 / Pad）不处理。
     */
    private void ensureGridDecoration() {
        if (recyclerView == null || recyclerView.getItemDecorationCount() > 0) {
            return;
        }
        if (getResources().getInteger(R.integer.sobot_list_span_count) <= 1) {
            return;
        }
        int hSpace, vSpace;
        if (mFrom == StPostMsgPresenter.TICKET_TO_LIST) {
            // 留言记录：列间距 20，上下间距 20（依据 留言记录页面横竖屏改版.md §3.2）
            hSpace = getResources().getDimensionPixelSize(R.dimen.sobot_ticket_grid_record_h_space);
            vSpace = getResources().getDimensionPixelSize(R.dimen.sobot_ticket_grid_record_v_space);
        } else {
            // 留言模板：列间距 12，上下间距 12（依据 留言模版页面横竖屏改版.md §3.2）
            hSpace = getResources().getDimensionPixelSize(R.dimen.sobot_ticket_grid_template_h_space);
            vSpace = getResources().getDimensionPixelSize(R.dimen.sobot_ticket_grid_template_v_space);
        }
        recyclerView.addItemDecoration(new SobotTicketGridItemDecoration(hSpace, vSpace));
    }

    /**
     * 初始化数据
     * 如果工单状态列表为空，先请求状态列表再执行 init()；否则直接执行 init()
     */
    @Override
    protected void initData() {

        if (ChatUtils.getStatusList() == null || ChatUtils.getStatusList().size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(this,
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(this, companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                    init();
                }

                @Override
                public void onFailure(Exception e, String s) {
                    init();
                }
            });
        } else {
            init();
        }
    }

    /**
     * 根据 mFrom 模式执行不同的初始化逻辑
     * - TICKET_TO_LIST：留言记录模式，检查免费账号，有数据直接显示，无数据则请求列表
     * - TICKET_TO_NEW：模板选择模式，有模板直接显示列表，无模板则请求模板
     */
    private void init() {
        if (mFrom == StPostMsgPresenter.TICKET_TO_LIST) {
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
                if (mList != null && !mList.isEmpty()) {
                    loading.stopSpinning();
                    mllLoading.setVisibility(View.GONE);
                    sobotEmpty.setVisibility(View.GONE);
                    mllContent.setVisibility(View.VISIBLE);
                    if (isOnlyShowTicket) {
                        //仅显示留言记录，不显示新建
                        ll_new_ticket.setVisibility(View.GONE);
                    } else {
                        ll_new_ticket.setVisibility(View.VISIBLE);
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
                    ensureGridDecoration();
                    recyclerView.setAdapter(mAdapter);
                } else {
                    loading.startSpinning();
                    //新建完需要延迟刷新
                    boolean delayRefresh = getIntent().getBooleanExtra("delayRefresh", false);
                    if (delayRefresh) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestDate();
                            }
                        }, 1000);
                    } else {
                        requestDate();
                    }
                }
            }
        } else {
            setTitle(R.string.sobot_please_leave_a_message);
            if (null != templates && !templates.isEmpty()) {
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
                ensureGridDecoration();
                recyclerView.setAdapter(tmpAdapter);
            } else {
                loading.startSpinning();
                //新建请求模板
                requestTemps(false);
            }
        }
    }

    /**
     * 请求留言模板列表
     * 根据模板数量决定下一步操作：
     * - 0个模板：直接跳转新建工单页
     * - 1个模板：自动选择该模板，跳转新建工单页
     * - 多个模板：如果是新建按钮触发，打开新的模板选择页；否则在当前页显示模板列表
     *
     * @param isNewBtn 是否由"新建留言"按钮触发（true-打开新页面展示模板，false-当前页展示模板）
     */
    private void requestTemps(boolean isNewBtn) {
        zhiChiApi.getWsTemplate(REQUEST_TAG, mUid, mGroupId, new StringResultCallBack<ArrayList<SobotPostMsgTemplate>>() {
            @Override
            public void onSuccess(ArrayList<SobotPostMsgTemplate> datas) {
                isClickAble = true;
                loading.stopSpinning();
                mllLoading.setVisibility(View.GONE);
                mllContent.setVisibility(View.VISIBLE);
                if (datas != null && datas.size() > 0) {
                    if (datas.size() == 1) {
                        //只有一个 自动点选，跳转到留言页面
                        gotoNewTicket(datas.get(0).getTemplateId());
                        finish();
                    } else {
                        if (isNewBtn) {
                            //打开下一个页面
                            Intent intent2 = new Intent(SobotTicketListActivity.this, SobotTicketListActivity.class);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_UID, mUid);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, mCompanyId);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, mCustomerId);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, mGroupId);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_FROM, StPostMsgPresenter.TICKET_TO_NEW);
                            intent2.putExtra(StPostMsgPresenter.INTENT_KEY_TEMP_LIST, datas);
                            startActivity(intent2);
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
                            ensureGridDecoration();
                            recyclerView.setAdapter(tmpAdapter);
                        }
                    }
                } else {
                    //跳转到新建工单页面
                    gotoNewTicket("");
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                isClickAble = true;
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
                    if (isOnlyShowTicket) {
                        //仅显示留言记录，不显示新建
                        ll_new_ticket.setVisibility(View.GONE);
                    } else {
                        ll_new_ticket.setVisibility(View.VISIBLE);
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
                    ensureGridDecoration();
                    recyclerView.setAdapter(mAdapter);
                } else {
                    if (isOnlyShowTicket) {
                        //显示空态
                        mllLoading.setVisibility(View.GONE);
                        sobotEmpty.setVisibility(View.GONE);
                        mllContent.setVisibility(View.VISIBLE);
                        setTitle(R.string.sobot_message_record);
                        recyclerView.setVisibility(View.GONE);
                        sobotEmpty.setVisibility(View.VISIBLE);
                    } else {
                        //新建请求模板
                        setTitle(R.string.sobot_please_leave_a_message);
                        requestTemps(false);
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

    /**
     * 页面销毁时注销广播接收器，并通知外部留言页面已关闭
     */
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getSobotBaseActivity()).unregisterReceiver(mReceiver);
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_CloseLeave);
        }
        super.onDestroy();
    }

    boolean isClickAble = true;  // 防止重复点击标记

    /**
     * 点击事件处理
     * "新建留言"按钮：请求模板列表，根据模板数量决定跳转逻辑（防止重复点击）
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_new_ticket) {
            if (isClickAble) {
                isClickAble = false;
                //跳转到新建工单,请求模板，根据返回的模板个数判断是显示模板还是新建
                loading.startSpinning();
                requestTemps(true);
            }
        }
    }

    public void showHint(String content) {
        if (!TextUtils.isEmpty(content)) {
            ToastUtil.showToast(getSobotBaseContext(), content);
        }
    }
}