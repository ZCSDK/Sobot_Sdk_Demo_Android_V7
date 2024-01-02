package com.sobot.chat.widget.dialog;

import android.content.Intent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotRobotListAdapter;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.List;

/**
 * 切换机器人
 * Created by jinxl on 2018/3/5.
 */
public class SobotRobotListActivity extends SobotDialogBaseActivity implements  View.OnClickListener {
    private LinearLayout sobot_negativeButton;
    private GridView sobot_gv;
    private TextView sobot_tv_title;

    private String mUid;
    private String mRobotFlag;

    private SobotRobotListAdapter mListAdapter;
    private int themeColor=0;
    private boolean changeThemeColor ;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_switch_robot;
    }

    @Override
    protected void initView() {
        sobot_negativeButton = (LinearLayout) findViewById(R.id.sobot_negativeButton);
        sobot_tv_title= (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_switch_robot_title);
        sobot_gv = (GridView) findViewById(R.id.sobot_gv);
        sobot_negativeButton.setOnClickListener(this);
        displayInNotch(this,sobot_gv);
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if(changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
        }
    }

    @Override
    protected void initData() {
        mUid =  getIntent().getStringExtra("uid");
        mRobotFlag = getIntent().getStringExtra("robotFlag");
        ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(getContext()).getZhiChiApi();
        zhiChiApi.getRobotSwitchList(SobotRobotListActivity.this, mUid, new StringResultCallBack<List<SobotRobot>>() {

            @Override
            public void onSuccess(List<SobotRobot> sobotRobots) {
                for (SobotRobot bean : sobotRobots) {
                    if (bean.getRobotFlag() != null && bean.getRobotFlag().equals(mRobotFlag)) {
                        bean.setSelected(true);
                        break;
                    }
                }
                if (mListAdapter == null) {
                    mListAdapter = new SobotRobotListAdapter(getContext(), sobotRobots, new SobotRobotListAdapter.RobotItemOnClick() {
                        @Override
                        public void onItemClick(SobotRobot item) {
                            if (item.getRobotFlag() != null && !item.getRobotFlag().equals(mRobotFlag)) {
                                //选择留言模版成功 发送广播
                                Intent intent = new Intent();
                                intent.putExtra("sobotRobot", item);
                                CommonUtils.sendLocalBroadcast(getContext(), intent);
                                setResult(ZCSobotConstant.EXTRA_SWITCH_ROBOT_REQUEST_CODE,intent);
                                finish();
                            }
                        }
                    },themeColor);
                    sobot_gv.setAdapter(mListAdapter);
                } else {
                    List<SobotRobot> datas = mListAdapter.getDatas();
                    datas.clear();
                    datas.addAll(sobotRobots);
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v == sobot_negativeButton) {
            finish();
        }
    }


}