package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotAiCardAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;

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
    private SobotChatCustomCard customCard, showCustomCard;
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
        // rv_list 挖孔避让由基类统一处理（布局根 sobot_container id 已定位白底层内容行）
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotAiCardMoreActivity";
    }

    @Override
    protected void initData() {
        mDatas = new ArrayList<>();
        boolean isHistoy = getIntent().getBooleanExtra("isHistoy", false);
        boolean isSendActionEnabled = getIntent().getBooleanExtra(
                ZhiChiConstants.SOBOT_AI_CARD_SEND_ENABLED, true);
        final String sourceMessageId = getIntent().getStringExtra(
                ZhiChiConstants.SOBOT_AI_CARD_SOURCE_MSG_ID);
        final String sourceRoundId = getIntent().getStringExtra(
                ZhiChiConstants.SOBOT_AI_CARD_ROUND_ID);
        showCustomCard = (SobotChatCustomCard) getIntent().getSerializableExtra("customCard");
        if (showCustomCard == null) {
            finish();
        }
        if (StringUtils.isNoEmpty(showCustomCard.getCardGuide())) {
            HtmlTools.getInstance(this).setRichText(sobot_tv_title,showCustomCard.getCardGuide(),getLinkTextColor());
            sobot_tv_title.setVisibility(View.VISIBLE);
        } else {
            //cardGuide 为空时显示默认标题「查看更多」，保证标题栏始终可见，关闭按钮位于右侧
            sobot_tv_title.setText(R.string.sobot_see_more);
            sobot_tv_title.setVisibility(View.VISIBLE);
        }
        customCard = (SobotChatCustomCard) getIntent().getSerializableExtra("customCard");
        mDatas.addAll(showCustomCard.getCustomCards());
        mListAdapter = new SobotAiCardAdapter(this, mDatas, false, isHistoy, isSendActionEnabled);
        mListAdapter.setOnItemClickListener(new SobotAiCardAdapter.OnItemListener() {
            @Override
            public void onSendClick(String menuName, SobotChatCustomGoods goods) {
                //发送
                Intent intent = new Intent();
                intent.setAction(ZhiChiConstants.SOBOT_SEND_AI_CARD_MSG);
                intent.putExtra("btnText", menuName);
                intent.putExtra("SobotCustomGoods", goods);
                intent.putExtra("SobotCustomCard", customCard);
                intent.putExtra(ZhiChiConstants.SOBOT_AI_CARD_SOURCE_MSG_ID, sourceMessageId);
                intent.putExtra(ZhiChiConstants.SOBOT_AI_CARD_ROUND_ID, sourceRoundId);
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
    //左右两边气泡内链接文字的字体颜色
    protected int getLinkTextColor() {
        ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(this,
                ZhiChiConstant.sobot_last_current_initModel);
            if (getResources().getColor(R.color.sobot_color_link) == getResources().getColor(R.color.sobot_common_blue)) {
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //服务端返回的气泡中超链接背景颜色
                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                        return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                    }
                }
                return R.color.sobot_color_link;
            } else {
                return R.color.sobot_color_link;
            }
    }

}
