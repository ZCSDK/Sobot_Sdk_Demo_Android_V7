package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotAiCardAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型卡片--查看更多
 * Created by gqf on 2025/3/25.
 */
public class SobotAiCardMoreActivity extends SobotDialogBaseActivity implements View.OnClickListener {

    private TextView sobot_tv_title;
    private RecyclerView rv_list;
    private List<SobotChatCustomGoods> mDatas;
    private SobotChatCustomCard customCard,showCustomCard;
    private SobotAiCardAdapter mListAdapter;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void initView() {
        super.initView();
        rv_list = findViewById(R.id.rv_list);
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG="SobotAiCardMoreActivity";
    }

    @Override
    protected void initData() {
        mDatas = new ArrayList<>();
        boolean isHistoy = getIntent().getBooleanExtra("isHistoy",false);
        showCustomCard = (SobotChatCustomCard) getIntent().getSerializableExtra("customCard");
        if(showCustomCard==null ){
            finish();
        }
        sobot_tv_title.setText(StringUtils.isNoEmpty(showCustomCard.getCardGuide())?showCustomCard.getCardGuide():"");
        customCard = (SobotChatCustomCard) getIntent().getSerializableExtra("customCard");
        mDatas.addAll(showCustomCard.getCustomCards());
        mListAdapter = new SobotAiCardAdapter(this, mDatas,false, true,isHistoy );
        mListAdapter.setOnItemClickListener(new SobotAiCardAdapter.OnItemListener() {
            @Override
            public void onSendClick(String menuName, SobotChatCustomGoods goods) {
                //发送
                Intent intent = new Intent();
                intent.setAction(ZhiChiConstants.SOBOT_SEND_AI_CARD_MSG);
                intent.putExtra("btnText", menuName);
                intent.putExtra("SobotCustomGoods", goods);
                intent.putExtra("SobotCustomCard", customCard);
                CommonUtils.sendLocalBroadcast(SobotAiCardMoreActivity.this, intent);
                finish();
            }

            @Override
            public void onItemClick(String menuName, SobotChatCustomGoods goods) {
                finish();
            }
        });
        rv_list.setAdapter(mListAdapter);
    }

    @Override
    public void onClick(View v) {
    }


}