package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotSkillAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotConnCusParam;
import com.sobot.chat.api.model.ZhiChiGroup;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.horizontalgridpage.SobotRecyclerCallBack;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择技能组
 */
public class SobotSkillGroupActivity extends SobotDialogBaseActivity {

    private RecyclerView sobot_rcy_skill;

    private TextView sobot_tv_title;
    private SobotSkillAdapter sobotSkillAdapter;
    private List<ZhiChiGroupBase> list_skill = new ArrayList<ZhiChiGroupBase>();
    private boolean flag_exit_sdk;
    private String uid = null;
    private String appkey = null;
    private int transferType;
    private ZhiChiApi zhiChiApi;
    private int msgFlag = 0;
    private SobotConnCusParam param;

    private StPostMsgPresenter mPressenter;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotSkillGroupActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        mPressenter = StPostMsgPresenter.newInstance(SobotSkillGroupActivity.this, SobotSkillGroupActivity.this);
        sobot_rcy_skill = (RecyclerView) findViewById(R.id.rv_list);
        sobotSkillAdapter = new SobotSkillAdapter(this, list_skill, msgFlag, new SobotRecyclerCallBack() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (list_skill != null && !list_skill.isEmpty()) {
                    if (getInitModel() != null && getInitModel().getAssignmentMode() == 1) {
                        //异步接待 进行转人工
                        Intent intent = new Intent();
                        intent.putExtra("groupIndex", position);
                        intent.putExtra("transferType", transferType);
                        if (param != null) {
                            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM, param);
                        }
                        setResult(ZhiChiConstant.REQUEST_COCE_TO_GRROUP, intent);
                        finish();
                    } else {
                        if ("true".equals(list_skill.get(position).isOnline())) {
                            if (!TextUtils.isEmpty(list_skill.get(position).getGroupName())) {
                                Intent intent = new Intent();
                                intent.putExtra("groupIndex", position);
                                intent.putExtra("transferType", transferType);
                                if (param != null) {
                                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM, param);
                                }
                                setResult(ZhiChiConstant.REQUEST_COCE_TO_GRROUP, intent);
                                finish();
                            }
                        } else {
                            if (msgFlag == ZhiChiConstant.sobot_msg_flag_open) {
                                Intent intent = new Intent();
                                intent.putExtra("toLeaveMsg", true);
                                intent.putExtra("groupIndex", position);
                                setResult(ZhiChiConstant.REQUEST_COCE_TO_GRROUP, intent);
                                finish();
                            }
                        }
                    }

                }
            }

            @Override
            public void onItemLongClickListener(View view, int position) {

            }
        });
        sobot_rcy_skill.setAdapter(sobotSkillAdapter);

        displayInNotch(this, sobot_rcy_skill);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getY() <= 0) {
                finishPageOrSDK();
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        mPressenter.destory();
        HttpUtils.getInstance().cancelTag(SobotSkillGroupActivity.this);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }


    protected void initData() {
        if (getIntent() != null) {
            uid = getIntent().getStringExtra("uid");
            appkey = getIntent().getStringExtra("appkey");
            flag_exit_sdk = getIntent().getBooleanExtra(
                    ZhiChiConstant.FLAG_EXIT_SDK, false);
            msgFlag = getIntent().getIntExtra("msgFlag", 0);
            transferType = getIntent().getIntExtra("transferType", 0);
            param = (SobotConnCusParam) getIntent().getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM);
        }
        if (getInitModel() != null && getInitModel().getAssignmentMode() == 1) {
            //异步接待 屏蔽留言
            msgFlag = ZhiChiConstant.sobot_msg_flag_close;
        }

        zhiChiApi = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        zhiChiApi.getGroupList(SobotSkillGroupActivity.this, appkey, uid, new StringResultCallBack<ZhiChiGroup>() {
            @Override
            public void onSuccess(ZhiChiGroup zhiChiGroup) {
                list_skill = zhiChiGroup.getData();
                if (list_skill != null && list_skill.size() > 0 && sobotSkillAdapter != null) {
                    if (list_skill.get(0).getGroupStyle() == 1) {
                        //图文样式
                        GridLayoutManager gridlayoutmanager = new GridLayoutManager(SobotSkillGroupActivity.this, 3);
                        sobot_rcy_skill.setLayoutManager(gridlayoutmanager);
                        sobot_rcy_skill.setPadding(ScreenUtils.dip2px(SobotSkillGroupActivity.this, 24), ScreenUtils.dip2px(SobotSkillGroupActivity.this, 24), ScreenUtils.dip2px(SobotSkillGroupActivity.this, 24), ScreenUtils.dip2px(SobotSkillGroupActivity.this, 24));
                    } else if (list_skill.get(0).getGroupStyle() == 2) {
                        //图文加描述
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SobotSkillGroupActivity.this);
                        sobot_rcy_skill.setLayoutManager(linearLayoutManager);
                    } else {
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SobotSkillGroupActivity.this);
                        sobot_rcy_skill.setLayoutManager(linearLayoutManager);
                    }

                    sobotSkillAdapter.setList(list_skill);
                    sobotSkillAdapter.setMsgFlag(msgFlag);
                    sobotSkillAdapter.notifyDataSetChanged();
                    if (TextUtils.isEmpty(list_skill.get(0).getGroupGuideDoc())) {
                        sobot_tv_title.setText(R.string.sobot_switch_robot_title_2);
                    } else {
                        sobot_tv_title.setText(list_skill.get(0).getGroupGuideDoc());
                    }
                } else {
                    sobot_tv_title.setText(R.string.sobot_switch_robot_title_2);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                sobot_tv_title.setText(R.string.sobot_switch_robot_title_2);
            }
        });
    }

    private void finishPageOrSDK() {
        int initType = SharedPreferencesUtil.getIntData(
                getApplicationContext(), appkey + "_" + ZhiChiConstant.initType, -1);
        if (initType == ZhiChiConstant.type_custom_only) {
            finish();
            sendCloseIntent(1);
        } else {
            if (!flag_exit_sdk) {
                finish();
                sendCloseIntent(2);
            } else {
                MyApplication.getInstance().exit();
            }
        }
    }

    private void sendCloseIntent(int type) {
        Intent intent = new Intent();
        if (type == 1) {
            intent.setAction(ZhiChiConstants.sobot_close_now_clear_cache);
        } else {
            intent.setAction(ZhiChiConstants.sobot_click_cancle);
        }
        CommonUtils.sendLocalBroadcast(getApplicationContext(), intent);
    }

    @Override
    public void onBackPressed() {
        finishPageOrSDK();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200) {
            finish();
        }
    }

    /**
     * 获取会话初始化返回的ZhiChiInitModeBase对象
     *
     * @return
     */
    public ZhiChiInitModeBase getInitModel() {
        ZhiChiInitModeBase initModel = null;
        if (getSobotBaseActivity() != null) {
            initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseActivity(),
                    ZhiChiConstant.sobot_last_current_initModel);
        }
        return initModel;
    }
}