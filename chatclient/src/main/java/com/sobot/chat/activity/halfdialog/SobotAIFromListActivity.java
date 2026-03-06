package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotAIFromListAdapter;
import com.sobot.chat.api.model.SobotAiLinkInfo;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.FileTypeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型机器人来源 弹窗
 */
public class SobotAIFromListActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private TextView sobot_tv_title;
    private RecyclerView rv_list;

    private SobotAIFromListAdapter mListAdapter;
    private List<SobotAiLinkInfo> aiLinkInfoList;
    private ImageView iv_closes;//关闭

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        sobot_tv_title.setText(getSafeStringResource(R.string.sobot_reference_materials));
        rv_list = findViewById(R.id.rv_list);
        iv_closes = findViewById(R.id.iv_closes);
        iv_closes.setOnClickListener(this);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotAIFromListActivity";
    }

    @Override
    protected void initData() {
        aiLinkInfoList = (List<SobotAiLinkInfo>) getIntent().getSerializableExtra("aiLinkInfoList");
        if (aiLinkInfoList == null) {
            aiLinkInfoList = new ArrayList<>();
        }
        mListAdapter = new SobotAIFromListAdapter(getContext(), aiLinkInfoList, new SobotAIFromListAdapter.RobotItemOnClick() {
            @Override
            public void onItemClick(SobotAiLinkInfo item) {
                if (item != null) {
                    if ("web".equalsIgnoreCase(item.getSectionTypeEnum())) {
                        // 打开网页
                        Intent intent = new Intent(SobotAIFromListActivity.this, WebViewActivity.class);
                        intent.putExtra("url", StringUtils.checkStringIsNull(item.getReferenceUrl()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if ("file".equalsIgnoreCase(item.getSectionTypeEnum())) {
                        // 打开文件详情页
                        Intent intent = new Intent(SobotAIFromListActivity.this, SobotFileDetailActivity.class);
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setFileName(StringUtils.checkStringIsNull(item.getReferenceTitle()));
                        cacheFile.setFileSize("");
                        cacheFile.setUrl(StringUtils.checkStringIsNull(item.getReferenceUrl()));
                        cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(StringUtils.checkStringIsNull(item.getReferenceUrl()))));
                        cacheFile.setMsgId(StringUtils.checkStringIsNull(item.getReferenceTitle()));
                        intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
        rv_list.setAdapter(mListAdapter);
        displayInNotch(this, rv_list);

    }


    @Override
    public void onClick(View v) {
        if (iv_closes == v) {
            finish();
        }
    }


}