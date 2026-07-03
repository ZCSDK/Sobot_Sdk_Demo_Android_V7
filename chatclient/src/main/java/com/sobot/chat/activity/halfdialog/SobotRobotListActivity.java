package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotRobotListAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 切换机器人
 * Created by jinxl on 2018/3/5.
 */
public class SobotRobotListActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private TextView sobot_tv_title;
    private TextView tv_nodata;
    private RecyclerView rv_list;
    private String mUid;
    private int mRobotFlag = -1;

    private SobotRobotListAdapter mListAdapter;
    private List<SobotRobot> sobotRobotList;
    private ImageView iv_closes;//关闭

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_dialog_list;
    }

    @Override
    protected void initView() {
        super.initView();
        sobotRobotList = new ArrayList<>();
        sobot_tv_title = (TextView) findViewById(R.id.sobot_tv_title);
        tv_nodata = (TextView) findViewById(R.id.tv_nodata);
        sobot_tv_title.setText(getSafeStringResource(R.string.sobot_switch_robot_title));
        rv_list = findViewById(R.id.rv_list);
        iv_closes = findViewById(R.id.iv_closes);
        iv_closes.setOnClickListener(this);
        setupRecyclerLayout();
    }

    /**
     * 列数由资源限定符决定：values/ 默认 1（手机竖屏单列）；values-land/ 与 values-w600dp/ 覆盖为 2 列，附 10dp 列/行间距。
     */
    private void setupRecyclerLayout() {
        int spanCount = getResources().getInteger(R.integer.sobot_list_span_count);
        if (spanCount > 1) {
            int spacing = ScreenUtils.dip2px(this, 10);
            rv_list.setLayoutManager(new GridLayoutManager(this, spanCount));
            rv_list.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        } else {
            rv_list.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotRobotListActivity";
    }

    @Override
    protected void initData() {
        mUid = getIntent().getStringExtra("uid");
        mRobotFlag = getIntent().getIntExtra("robotFlag", -1);
        List<SobotRobot> passedList = (ArrayList<SobotRobot>) getIntent().getSerializableExtra("robotList");
        if (passedList != null && passedList.size() > 0) {
            for (SobotRobot bean : passedList) {
                if (bean.getRobotFlag() == mRobotFlag) {
                    bean.setSelected(true);
                    break;
                }
            }
            sobotRobotList.clear();
            sobotRobotList.addAll(passedList);
            mListAdapter = new SobotRobotListAdapter(getContext(), sobotRobotList, mRobotFlag, new SobotRobotListAdapter.RobotItemOnClick() {
                @Override
                public void onItemClick(SobotRobot item) {
                    if (item.getRobotFlag() != mRobotFlag) {
                        if (!item.isCheckFormSubmitOver() && item.getFormSubmitInfos() != null && !item.getFormSubmitInfos().isEmpty()) {
                            //显示变量收集
                            Intent intent = new Intent(SobotRobotListActivity.this, SobotFormVariableActivity.class);
                            intent.putExtra("sobotRobot", item);
                            startActivity(intent);
                        } else {
                            //选择成功 发送广播：限定本进程，避免 SobotRobot 序列化对象泄漏 (CWE-927)
                            Intent intent = new Intent();
                            intent.setAction(ZhiChiConstants.SOBOT_SWICH_ROBOT);
                            intent.setPackage(getPackageName());
                            intent.putExtra("sobotRobot", item);
                            sendBroadcast(intent);
                        }
                        finish();
                    } else {
                        finish();
                    }
                }
            });
            rv_list.setAdapter(mListAdapter);
            displayInNotch(rv_list);
        } else {
            tv_nodata.setVisibility(View.VISIBLE);
            rv_list.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if (iv_closes == v) {
            finish();
        }
    }

    /**
     * 多列模式下的列间距与行间距。
     * 不在最外侧加边距，外边距由布局的 sobot_modal_content_padding 负责。
     */
    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;

        GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            int column = position % spanCount;
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }

}