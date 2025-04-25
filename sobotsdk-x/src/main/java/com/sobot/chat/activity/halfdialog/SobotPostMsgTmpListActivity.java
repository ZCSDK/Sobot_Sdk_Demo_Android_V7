package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotPostMsgTmpAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotLeaveMsgConfig;
import com.sobot.chat.api.model.SobotPostMsgTemplate;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;

/**
 * 选择留言模板
 * Created by jinxl on 2018/3/5.
 */
public class SobotPostMsgTmpListActivity extends SobotDialogBaseActivity implements View.OnClickListener {

    private TextView sobot_tv_title;
    private RecyclerView rv_list;
    private ArrayList<SobotPostMsgTemplate> mDatas;
    private SobotPostMsgTmpAdapter mListAdapter;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void initView() {
        rv_list = findViewById(R.id.rv_list);
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(R.string.sobot_choice_business);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void initData() {
        mDatas = (ArrayList<SobotPostMsgTemplate>) getIntent().getSerializableExtra("sobotPostMsgTemplateList");
        if (mListAdapter == null) {
            mListAdapter = new SobotPostMsgTmpAdapter(getContext(), mDatas, new SobotPostMsgTmpAdapter.ItemOnClick() {
                @Override
                public void onItemClick(SobotPostMsgTemplate itemBeen) {
                    zhiChiApi.getMsgTemplateConfig(getContext(), getIntent().getStringExtra("uid"), itemBeen.getTemplateId(), new StringResultCallBack<SobotLeaveMsgConfig>() {
                        @Override
                        public void onSuccess(SobotLeaveMsgConfig data) {
                            if (data != null) {
                                //选择留言模版成功 发送广播
                                Intent intent = new Intent();
                                intent.setAction(ZhiChiConstants.SOBOT_POST_MSG_TMP_BROCAST);
                                intent.putExtra("sobotLeaveMsgConfig", data);
                                intent.putExtra("uid", getIntent().getStringExtra("uid"));
                                intent.putExtra("mflag_exit_sdk", getIntent().getBooleanExtra("flag_exit_sdk", false));
                                CommonUtils.sendLocalBroadcast(getContext(), intent);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String des) {

                        }
                    });
                }
            });
        }
        rv_list.setAdapter(mListAdapter);
    }


    @Override
    public void onClick(View v) {
    }


}