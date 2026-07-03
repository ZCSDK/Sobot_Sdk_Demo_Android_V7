package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.activity.halfdialog.SobotReplyActivity;
import com.sobot.chat.activity.halfdialog.SobotTicketEvaluateActivity;
import com.sobot.chat.adapter.SobotTicketDetailAdapter;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.StTicketDetailInfo;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 留言详情页面
 * <p>
 * 展示单条留言工单的详细信息，包括工单内容、回复列表等。
 * 底部操作栏根据工单状态动态显示：
 * - 未完成工单：显示"回复"按钮
 * - 已完成且可评价工单：显示"回复"和"评价"按钮（根据按钮文字长度自动切换水平/垂直布局）
 * - 已完成且不可回复工单：仅显示"评价"按钮
 * <p>
 * 支持功能：
 * 1. 查看留言工单详情及回复列表
 * 2. 回复工单（跳转 {@link SobotReplyActivity}，支持临时保存草稿）
 * 3. 评价工单（跳转 {@link SobotTicketEvaluateActivity}）
 * 4. 返回时弹出评价弹窗（通过 {@link Information#isShowLeaveDetailBackEvaluate()} 控制，每个工单仅弹一次）
 */
public class SobotTicketDetailActivity extends SobotChatBaseActivity implements View.OnClickListener {
    /**
     * 回复页面请求码
     */
    private static final int REQUEST_REPLY_CODE = 0x1001;

    private String mUid = "";           // 用户ID
    private String mCompanyId = "";     // 企业ID
    private String mTicketId = "";      // 工单ID
    private boolean refresh;            // 回复或评价后需要重新刷新标记，用于返回列表页时通知刷新
    private Information information;     // 用户配置信息，包含留言相关开关设置

    private List<Object> mList = new ArrayList<>();  // 详情数据列表，第一项为工单信息，后续为回复列表
    private SobotTicketDetailAdapter mAdapter;       // 详情列表适配器
    private RecyclerView recyclerView;               // 详情列表
    private LinearLayout sobot_ticket_bottom_ll;            // 底部操作栏：仅显示回复按钮（未完成工单）
    private LinearLayout sobot_ticket_success_bottom_ll;    // 底部操作栏：工单完成后的评价+回复区域
    private LinearLayout v_bottom_btns, sobot_evaluate_v, sobot_reply_v;  // 垂直布局的评价和回复按钮（按钮文字过长时使用）
    private LinearLayout h_bottom_btns, sobot_evaluate_h, sobot_reply_h;  // 水平布局的评价和回复按钮（默认布局）
    private TextView sobot_evaluate_tv;  // 评价按钮文字，用于判断文字行数以切换水平/垂直布局

    // 横屏右侧面板 4 个圆形图标按钮（Figma node 658:22370 / 745:57673），仅 layout-w600dp 引用，
    // 竖屏 / Pad / 折叠屏内屏 findViewById 返回 null，所有访问都包在 R.integer.sobot_list_span_count > 1 分支内
    private LinearLayout ll_side_action;    // 整块右侧面板，回复弹窗弹起时整体 GONE
    private ImageView iv_side_scroll_up;    // ↑ 滚动到列表顶
    private ImageView iv_side_evaluate;     // 👍 评价
    private ImageView iv_side_reply;        // 💬 回复（主题色填充）
    private ImageView iv_side_scroll_down;  // ↓ 滚动到列表底

    private SobotUserTicketEvaluate mEvaluate;  // 当前工单的评价配置信息
    private StTicketDetailInfo mTicketInfo;      // 当前工单详情数据

    private String replyTempContent;                              // 回复页面临时保存的文本内容（草稿）
    private ArrayList<SobotFileModel> picTempList = new ArrayList<>();  // 回复页面临时保存的图片列表（草稿）
    private List<SobotTicketStatus> statusList;                   // 工单状态列表，用于在详情中显示状态标签

    /**
     * 创建启动留言详情页的 Intent
     *
     * @param context   上下文
     * @param companyId 企业ID
     * @param uid       用户ID
     * @param ticketId  工单ID
     * @return 配置好参数的 Intent
     */
    public static Intent newIntent(Context context, String companyId, String uid, String ticketId) {
        Intent intent = new Intent(context, SobotTicketDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(ChatUtils.INTENT_KEY_UID, uid);
        bundle.putString(ChatUtils.INTENT_KEY_COMPANYID, companyId);
        bundle.putSerializable(ChatUtils.INTENT_KEY_TICKET_ID, ticketId);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_detail;
    }

    /**
     * 从 Intent 中解析传递的参数：用户ID、企业ID、工单ID、工单状态列表
     */
    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(ChatUtils.INTENT_KEY_UID);
            mCompanyId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_COMPANYID);
            mTicketId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_TICKET_ID);
            statusList = ChatUtils.getStatusList();

        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketDetailActivity";
    }

    /**
     * 初始化视图控件
     * - 设置标题栏返回按钮（返回时判断是否需要弹出评价弹窗）
     * - 初始化详情列表 RecyclerView
     * - 初始化底部操作栏（回复、评价按钮的水平/垂直两种布局）
     */
    @Override
    protected void initView() {
        showLeftMenu(true);
        // 返回按钮点击：已完成工单首次返回时弹出评价弹窗
        getLeftMenu().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List ticketIds = (List) SharedPreferencesUtil.getObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds");
                //已完成留言详情界面：返回时是否弹出服务评价窗口(只会第一次返回弹，下次返回不会再弹)
                if (information != null && information.isShowLeaveDetailBackEvaluate() && sobot_ticket_success_bottom_ll.getVisibility() == View.VISIBLE) {
                    if (ticketIds != null && ticketIds.contains(mTicketId)) {
                        finish();
                    } else {
                        if (ticketIds == null) {
                            ticketIds = new ArrayList();
                        }
                        ticketIds.add(mTicketId);
                        SharedPreferencesUtil.saveObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds", ticketIds);
                        Intent intent = new Intent(SobotTicketDetailActivity.this, SobotTicketEvaluateActivity.class);
                        intent.putExtra("sobotUserTicketEvaluate", mEvaluate);
                        startActivityForResult(intent, ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE);
                    }
                } else {
                    finish();
                }
            }
        });
        setTitle(R.string.sobot_message_details);
        recyclerView = findViewById(R.id.sobot_listview);
        displayInNotch(recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // 设置RecyclerView的LayoutManager
        recyclerView.setLayoutManager(layoutManager);

        sobot_ticket_success_bottom_ll = findViewById(R.id.sobot_ticket_success_bottom_ll);
        sobot_ticket_bottom_ll = findViewById(R.id.sobot_ticket_bottom_ll);
        sobot_ticket_bottom_ll.setOnClickListener(this);

        h_bottom_btns = findViewById(R.id.h_bottom_btns);
        sobot_reply_h = findViewById(R.id.sobot_reply_h);
        sobot_evaluate_h = findViewById(R.id.sobot_evaluate_h);

        sobot_evaluate_tv = findViewById(R.id.sobot_evaluate_tv);
        sobot_reply_h.setOnClickListener(this);
        sobot_evaluate_h.setOnClickListener(this);

        v_bottom_btns = findViewById(R.id.v_bottom_btns);
        sobot_evaluate_v = findViewById(R.id.sobot_evaluate_v);
        sobot_reply_v = findViewById(R.id.sobot_reply_v);
        sobot_reply_v.setOnClickListener(this);
        sobot_evaluate_v.setOnClickListener(this);

        mAdapter = new SobotTicketDetailAdapter(SobotTicketDetailActivity.this, mList);
        recyclerView.setAdapter(mAdapter);

        // 横屏右侧面板 4 个圆形图标按钮（layout-w600dp 才有，竖屏返回 null）
        ll_side_action = findViewById(R.id.ll_side_action);
        iv_side_scroll_up = findViewById(R.id.iv_side_scroll_up);
        iv_side_evaluate = findViewById(R.id.iv_side_evaluate);
        iv_side_reply = findViewById(R.id.iv_side_reply);
        iv_side_scroll_down = findViewById(R.id.iv_side_scroll_down);
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1 && iv_side_reply != null) {
            // 主题色染色回复按钮背景
            Drawable replyBg = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_side_btn_primary, null);
            if (replyBg != null) {
                iv_side_reply.setBackground(ThemeUtils.applyColorWithMultiplyMode(replyBg, ThemeUtils.getThemeColor(this)));
            }
            // 回复图标染白（图标在主题色背景上需要反白可见）
            Drawable replyIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_ic_side_reply, null);
            if (replyIcon != null) {
                iv_side_reply.setImageDrawable(ThemeUtils.applyColorToDrawable(replyIcon, ThemeUtils.getThemeTextAndIconColor(this)));
            }
            iv_side_scroll_up.setOnClickListener(this);
            iv_side_evaluate.setOnClickListener(this);
            iv_side_reply.setOnClickListener(this);
            iv_side_scroll_down.setOnClickListener(this);
        }
    }

    /**
     * 初始化数据
     * 1. 从 SharedPreferences 获取用户配置信息
     * 2. 隐藏所有底部操作栏（等待数据加载后根据工单状态显示）
     * 3. 如果工单状态列表为空，先请求状态列表再请求详情；否则直接请求详情
     */
    @Override
    protected void initData() {
        information = (Information) SharedPreferencesUtil.getObject(SobotTicketDetailActivity.this, "sobot_last_current_info");

        h_bottom_btns.setVisibility(View.GONE);
        v_bottom_btns.setVisibility(View.GONE);

        sobot_ticket_bottom_ll.setVisibility(View.GONE);
        sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
        if (statusList == null || statusList.size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(this,
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(this, companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                    if (statusList == null) {
                        statusList = new ArrayList<>();
                    } else {
                        statusList.clear();
                    }
                    statusList.addAll(sobotTicketStatuses);
                    mAdapter.setStatusList(statusList);
                    requestDate();
                }

                @Override
                public void onFailure(Exception e, String s) {
                    requestDate();
                }
            });
        } else {
            mAdapter.setStatusList(statusList);
            requestDate();
        }

    }

    /**
     * 请求留言工单详情数据
     * 成功后：标记工单回复已读，刷新列表数据，根据工单状态显示底部操作栏，滚动到底部
     */
    public void requestDate() {
        SobotDialogUtils.startProgressDialog(this);
        zhiChiApi.getUserTicketDetail(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, new StringResultCallBack<StTicketDetailInfo>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(StTicketDetailInfo datas) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseContext());
                //工单回复标记已读
                zhiChiApi.updateUserTicketReplyInfo(SobotTicketDetailActivity.this, mCompanyId, information.getPartnerid(), mTicketId);
                //留言详情
                if (datas != null) {
                    mTicketInfo = datas;
                    mEvaluate = datas.getCusNewSatisfactionVO();
                    mList.clear();
                    mList.add(datas);
                    if (datas.getReplyList() != null && !datas.getReplyList().isEmpty()) {
                        mList.addAll(datas.getReplyList());
//                    } else {
//                        mList.add(true);
                    }

                    int type = 0;//只显示回复
                    // 是否显示已评价
                    if (mTicketInfo.getIsShowSatisfactionButton() == 1 && mTicketInfo.getIsEvaluated() == 0 && datas.getCusNewSatisfactionVO() != null) {
                        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY)) {
                            type = 1;//显示评价
                        } else {
                            type = 2;//显示不显示回复
                        }
                    }
                    showBottom(type);
//                } else {
//                    mList.add(true);
                }
                mAdapter.notifyDataSetChanged();
                // 数据更新后滚动到底部
                int newItemCount = mAdapter.getItemCount();
                if (newItemCount > 0) {
                    recyclerView.scrollToPosition(newItemCount - 1);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseContext());
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    /**
     * 底部显示的类型
     *
     * @param type 0 只有回复，1 完成有评价回复,2 隐藏底部
     */
    private void showBottom(int type) {
        // 横屏：使用右侧 4 图标面板（Figma node 658-22370 / 745-57673）
        // type=0 显示 滚顶 + 回复 + 滚底；type=1 全显；type=2 显示 滚顶 + 评价 + 滚底（无回复）
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1 && iv_side_reply != null) {
            iv_side_scroll_up.setVisibility(View.VISIBLE);
            iv_side_scroll_down.setVisibility(View.VISIBLE);
            // 评价：type=1 或 type=2 且有评价对象
            iv_side_evaluate.setVisibility((type == 1 || type == 2) && mEvaluate != null ? View.VISIBLE : View.GONE);
            // 回复：type=0 或 type=1 显示；type=2 隐藏
            iv_side_reply.setVisibility(type == 2 ? View.GONE : View.VISIBLE);
            return;
        }
        if (type == 0) {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            sobot_ticket_success_bottom_ll.setVisibility(View.VISIBLE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
            if (getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
                // 宽屏右侧面板较窄：固定走垂直布局，跳过 lineCount 自动切换
                v_bottom_btns.setVisibility(View.VISIBLE);
                h_bottom_btns.setVisibility(View.GONE);
            } else {
                // 竖屏：按 lineCount 自动切换水平/垂直
                v_bottom_btns.setVisibility(View.GONE);
                h_bottom_btns.setVisibility(View.VISIBLE);
                sobot_evaluate_tv.post(new Runnable() {
                    // 在视图布局完成后执行的代码
                    @Override
                    public void run() {
                        Layout layout = sobot_evaluate_tv.getLayout();
                        if (layout != null) {
                            int lineCount = layout.getLineCount();
                            LogUtils.d("=======1====lineCount==" + lineCount);
                            if (lineCount > 1) {
                                v_bottom_btns.setVisibility(View.VISIBLE);
                                h_bottom_btns.setVisibility(View.GONE);
                            } else {
                                v_bottom_btns.setVisibility(View.GONE);
                                h_bottom_btns.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }

        } else if (type == 2) {
            //不显示回复
            sobot_ticket_success_bottom_ll.setVisibility(View.VISIBLE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
            v_bottom_btns.setVisibility(View.VISIBLE);
            h_bottom_btns.setVisibility(View.GONE);
            sobot_reply_v.setVisibility(View.GONE);

        } else {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
        }
    }

    /**
     * 横屏：切换右侧 4 图标面板整体可见性。
     * 用于回复弹窗弹起 / 关闭时联动隐藏 / 恢复（避免与回复栏并排干扰）。
     * 竖屏（iv_side_reply == null）直接 no-op。
     *
     * @param visible true 恢复显示（按当前 type 由 showBottom 决定具体哪几个），false 全隐藏
     */
    private void setSidePanelVisibility(boolean visible) {
        if (getResources().getInteger(R.integer.sobot_list_span_count) <= 1 || ll_side_action == null) {
            return;
        }
        // 整块右侧面板 GONE / VISIBLE：GONE 时左侧 RecyclerView（weight=1）自动扩展为全宽
        ll_side_action.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * 提交工单满意度评价
     *
     * @param score               评分
     * @param remark              评价内容
     * @param labelTag            评价标签
     * @param defaultQuestionFlag 默认问题标记
     */
    public void submitEvaluate(final int score, final String remark, final String labelTag, final int defaultQuestionFlag) {
        zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, score, remark, labelTag, defaultQuestionFlag, new StringResultCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                ToastUtil.showCustomToast(SobotTicketDetailActivity.this, getResources().getString(R.string.sobot_leavemsg_success_tip), R.drawable.sobot_icon_success);
                requestDate();
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    /**
     * 点击事件处理
     * - 回复按钮（水平/垂直布局）或底部回复栏：跳转到回复页面，携带草稿数据
     * - 评价按钮（水平/垂直布局）：跳转到评价页面
     */
    @Override
    public void onClick(View v) {
        if (v == sobot_reply_h || v == sobot_reply_v || v == sobot_ticket_bottom_ll || v == iv_side_reply) {
            //回复（含横屏右侧面板回复图标）
            // 横屏：弹起回复前隐藏右侧 4 图标，避免与底部回复栏并排干扰；onActivityResult 中恢复
            setSidePanelVisibility(false);
            Intent intent = new Intent(SobotTicketDetailActivity.this, SobotReplyActivity.class);
            intent.putExtra(ChatUtils.INTENT_KEY_UID, mUid);
            intent.putExtra(ChatUtils.INTENT_KEY_COMPANYID, mCompanyId);
            intent.putExtra(ChatUtils.INTENT_KEY_TICKET_ID, mTicketId);
            intent.putExtra("picTempList", (Serializable) picTempList);
            intent.putExtra("replyTempContent", replyTempContent);
            startActivityForResult(intent, REQUEST_REPLY_CODE);
        } else if (v == sobot_evaluate_h || v == sobot_evaluate_v || v == iv_side_evaluate) {
            //评价（含横屏右侧面板评价图标）
            if (mEvaluate != null) {
                Intent intent = new Intent(SobotTicketDetailActivity.this, SobotTicketEvaluateActivity.class);
                intent.putExtra("sobotUserTicketEvaluate", mEvaluate);
                startActivityForResult(intent, ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE);
            }
        } else if (v == iv_side_scroll_up) {
            //横屏右侧面板：滚动列表到顶
            if (recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
                recyclerView.smoothScrollToPosition(0);
            }
        } else if (v == iv_side_scroll_down) {
            //横屏右侧面板：滚动列表到底
            if (recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }


    /**
     * 物理返回键处理：如果有回复或评价操作，设置 RESULT_OK 通知列表页刷新
     */
    @Override
    public void onBackPressed() {
        if (mTicketInfo != null && refresh) {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();
    }

    /**
     * 处理子页面返回结果
     * - REQUEST_REPLY_CODE：回复页面返回，如果是临时保存则缓存草稿，否则刷新详情
     * - EXTRA_TICKET_EVALUATE_REQUEST_CODE：评价页面返回，提交评价并刷新
     * - EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE：返回时弹出的评价页面返回，提交评价后关闭当前页面
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 回复弹窗关闭后恢复右侧 4 图标（无论 OK / CANCEL，避免取消时图标永久消失）
        if (requestCode == REQUEST_REPLY_CODE) {
            setSidePanelVisibility(true);
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_REPLY_CODE) {
                boolean isTemp = false;
                if (data != null) {
                    isTemp = data.getBooleanExtra("isTemp", false);
                    //回复临时保存数据
                    replyTempContent = data.getStringExtra("replyTempContent");
                    picTempList = (ArrayList<SobotFileModel>) data.getSerializableExtra("picTempList");
                }
                if (!isTemp) {
                    refresh = true;
                    requestDate();
                }
            } else if (requestCode == ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE) {
                //提交评价
                submitEvaluate(data.getIntExtra("score", 0), data.getStringExtra("content"), data.getStringExtra("labelTag"), data.getIntExtra("defaultQuestionFlag", -1));
            } else if (requestCode == ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE && null != data) {
                final int score = data.getIntExtra("score", 0);
                final String remark = data.getStringExtra("content");
                final String labelTag = data.getStringExtra("labelTag");
                final int defaultQuestionFlag = data.getIntExtra("defaultQuestionFlag", -1);
                zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, score, remark, labelTag, defaultQuestionFlag, new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        ToastUtil.showCustomToastWithListenr(SobotTicketDetailActivity.this, getResources().getString(R.string.sobot_leavemsg_success_tip), 1000, new ToastUtil.OnAfterShowListener() {
                            @Override
                            public void doAfter() {
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        ToastUtil.showToast(getApplicationContext(), des);
                    }
                });
            }

        }
    }


}