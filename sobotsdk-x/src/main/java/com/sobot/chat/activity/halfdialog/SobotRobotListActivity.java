package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotRobotListAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 切换机器人
 * Created by jinxl on 2018/3/5.
 */
public class SobotRobotListActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private TextView sobot_tv_title;
    private RecyclerView rv_list;
    private String mUid;
    private int mRobotFlag = -1;

    private SobotRobotListAdapter mListAdapter;
    private List<SobotRobot> sobotRobotList;
    private int themeColor = 0;
    private boolean changeThemeColor;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void initView() {
        sobotRobotList = new ArrayList<>();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_switch_robot_title);
        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
        }
        mListAdapter = new SobotRobotListAdapter(getContext(), sobotRobotList, new SobotRobotListAdapter.RobotItemOnClick() {
            @Override
            public void onItemClick(SobotRobot item) {
                if ( item.getRobotFlag() != mRobotFlag) {
                    //选择留言模版成功 发送广播
                    Intent intent = new Intent();
                    intent.putExtra("sobotRobot", item);
                    CommonUtils.sendLocalBroadcast(getContext(), intent);
                    setResult(ZCSobotConstant.EXTRA_SWITCH_ROBOT_REQUEST_CODE, intent);
                    finish();
                }
            }
        });
        rv_list.setAdapter(mListAdapter);
        displayInNotch(this, rv_list);
    }

    @Override
    protected void initData() {
        mUid = getIntent().getStringExtra("uid");
        mRobotFlag = getIntent().getIntExtra("robotFlag",-1);
        ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(getContext()).getZhiChiApi();
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getContext(),
                ZhiChiConstant.sobot_last_current_initModel);
        if (initMode != null && initMode.isAiAgent()) {
            //Ai
            zhiChiApi.AiRobotList(SobotRobotListActivity.this, mUid, new StringResultCallBack<List<SobotRobot>>() {

                @Override
                public void onSuccess(List<SobotRobot> sobotRobots) {
                    for (SobotRobot bean : sobotRobots) {
                        if (bean.getRobotFlag()==mRobotFlag) {
                            bean.setSelected(true);
                            break;
                        }
                    }
                    sobotRobotList.clear();
                    sobotRobotList.addAll(sobotRobots);
                    mListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e, String des) {

                }
            });
        } else {
            zhiChiApi.getRobotSwitchList(SobotRobotListActivity.this, mUid, new StringResultCallBack<List<SobotRobot>>() {

                @Override
                public void onSuccess(List<SobotRobot> sobotRobots) {
                    for (SobotRobot bean : sobotRobots) {
                        if (bean.getRobotFlag()==mRobotFlag) {
                            bean.setSelected(true);
                            break;
                        }
                    }
                    sobotRobotList.clear();
                    sobotRobotList.addAll(sobotRobots);
                    mListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e, String des) {

                }
            });
        }
    }


    @Override
    public void onClick(View v) {
    }


}