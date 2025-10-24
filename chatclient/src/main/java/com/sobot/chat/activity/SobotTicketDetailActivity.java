package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 留言详情
 */
public class SobotTicketDetailActivity extends SobotChatBaseActivity implements View.OnClickListener {
    private static final int REQUEST_REPLY_CODE = 0x1001;

    private String mUid = "";
    private String mCompanyId = "";
    private String mTicketId = "";
    private boolean refresh;//回复、评价后需要重新刷新
    private Information information;

    private List<Object> mList = new ArrayList<>();
    private SobotTicketDetailAdapter mAdapter;
    private RecyclerView recyclerView;
    private LinearLayout sobot_ticket_bottom_ll;//仅回复
    private LinearLayout sobot_ticket_success_bottom_ll;//工单完成，有评价
    private LinearLayout  v_bottom_btns,sobot_evaluate_v,sobot_reply_v;
    private LinearLayout h_bottom_btns,sobot_evaluate_h,sobot_reply_h;
    private TextView sobot_evaluate_tv;

    private SobotUserTicketEvaluate mEvaluate;
    private StTicketDetailInfo mTicketInfo;

    //进入回复界面弹窗界面 把 上次回复的临时内容传过去
    private String replyTempContent;
    private ArrayList<SobotFileModel> picTempList = new ArrayList<>();
    private List<SobotTicketStatus> statusList;

    /**
     * @param context 应用程序上下文
     * @return
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

    @Override
    protected void initView() {
        showLeftMenu(true);
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

    }

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

    public void requestDate() {
        zhiChiApi.getUserTicketDetail(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, new StringResultCallBack<StTicketDetailInfo>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(StTicketDetailInfo datas) {
                zhiChiApi.updateUserTicketReplyInfo(SobotTicketDetailActivity.this, mCompanyId, information.getPartnerid(), mTicketId);
                if (datas != null) {
                    mTicketInfo = datas;
                    mEvaluate = datas.getCusNewSatisfactionVO();
                    mList.clear();
                    mList.add(datas);
                    if (datas.getReplyList() != null && !datas.getReplyList().isEmpty()) {
                        mList.addAll(datas.getReplyList());
                    } else {
                        mList.add(true);
                    }

                    int type = 0;//只显示回复
                    // 是否显示已评价
                    if(mTicketInfo.getIsShowSatisfactionButton()==1 && mTicketInfo.getIsEvaluated() == 0 && datas.getCusNewSatisfactionVO()!=null){
                        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY)) {
                            type = 1;//显示评价
                        }else{
                            type = 2;//显示不显示回复
                        }
                    }
                    showBottom(type);
                } else {
                    mList.add(true);
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
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    /**
     * 底部显示的类型
     * @param type 0 只有回复，1 完成有评价回复,2 隐藏底部
     */
    private void showBottom(int type) {
        if (type == 0) {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            sobot_ticket_success_bottom_ll.setVisibility(View.VISIBLE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
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
                            }else{
                                v_bottom_btns.setVisibility(View.GONE);
                                h_bottom_btns.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });

        } else if(type ==2){
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

    @Override
    public void onClick(View v) {
        if (v == sobot_reply_h || v == sobot_reply_v ||v==sobot_ticket_bottom_ll) {
            //回复
            Intent intent = new Intent(SobotTicketDetailActivity.this, SobotReplyActivity.class);
            intent.putExtra(ChatUtils.INTENT_KEY_UID, mUid);
            intent.putExtra(ChatUtils.INTENT_KEY_COMPANYID, mCompanyId);
            intent.putExtra(ChatUtils.INTENT_KEY_TICKET_ID, mTicketId);
            intent.putExtra("picTempList", (Serializable) picTempList);
            intent.putExtra("replyTempContent", replyTempContent);
            startActivityForResult(intent, REQUEST_REPLY_CODE);
        } else if (v == sobot_evaluate_h || v == sobot_evaluate_v) {
            if (mEvaluate != null) {
                Intent intent = new Intent(SobotTicketDetailActivity.this, SobotTicketEvaluateActivity.class);
                intent.putExtra("sobotUserTicketEvaluate", mEvaluate);
                startActivityForResult(intent, ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE);
            }
        }
    }


    @Override
    public void onBackPressed() {//返回

        if (mTicketInfo != null && refresh) {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    refresh=true;
                    requestDate();
                }
            } else if (requestCode == ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE) {
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

    //评价成功后移除工单id
    public void removeTicketId() {
        List ticketIds = (List) SharedPreferencesUtil.getObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds");
        if (StringUtils.isNoEmpty(mTicketId) && ticketIds != null)
            ticketIds.remove(mTicketId);
        SharedPreferencesUtil.saveObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds", ticketIds);

    }

    public void updateUIByThemeColor(TextView view) {
        if (ThemeUtils.isChangedThemeColor(getSobotBaseContext())) {
            Drawable bg = getResources().getDrawable(R.drawable.sobot_normal_btn_bg);
            if (bg != null) {
                view.setBackground(ThemeUtils.applyColorToDrawable(bg, ThemeUtils.getThemeColor(getSobotBaseActivity())));
                view.setTextColor(getResources().getColor(R.color.sobot_color_white));
            }
        }
    }


}