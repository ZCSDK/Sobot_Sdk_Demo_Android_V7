package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotRobotAiAgentButtonPageAdater;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.RobotAiAgentButtonViewPager;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;

import java.util.ArrayList;

//大模型机器人 按钮消息 类似模板二样式
public class RobotAiagentButtonMessageHolder extends MsgHolderBase {
    // 聊天的消息内容
    private TextView tv_msg;
    public ZhiChiMessageBase message;
    private ImageView ivPreviousPage;//上一页
    private ImageView ivLastPage;//下一页
    private LinearLayout llPage;//分页ll
    private TextView tvCusPageCount;//当前页数

    private RobotAiAgentButtonViewPager view_pager;
    private SobotRobotAiAgentButtonPageAdater templatePageAdater;

    private Context mContext;

    public RobotAiagentButtonMessageHolder(Context context, View convertView) {
        super(context, convertView);
        tv_msg = convertView.findViewById(R.id.sobot_template2_msg);
        tvCusPageCount = convertView.findViewById(R.id.tv_cus_page_count);
        ivPreviousPage = convertView.findViewById(R.id.iv_previous_page);
        ivLastPage = convertView.findViewById(R.id.iv_next_page);
        llPage = convertView.findViewById(R.id.ll_pre_next_page);
        view_pager = convertView.findViewById(R.id.view_pager);
        this.mContext = context;
    }


    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        this.message = message;
        if (message != null && message.getVariableValueEnums() != null) {
            String msgStr = message.getContent();
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tv_msg, msgStr, getLinkTextColor());
                tv_msg.setVisibility(View.VISIBLE);
            } else {
                tv_msg.setVisibility(View.GONE);
            }
            checkShowTransferBtn();
            if (message.getVariableValueEnums().length > 0) {
                String[] inputContent = message.getVariableValueEnums();
                resetMaxWidth();
                ArrayList<SobotLablesViewModel> label = new ArrayList<>();
                for (int i = 0; i < inputContent.length; i++) {
                    String str = inputContent[i];
                    SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                    lablesViewModel.setTitle(str);
                    label.add(lablesViewModel);
                }
                templatePageAdater = new SobotRobotAiAgentButtonPageAdater(mContext, label, message, msgCallBack);
                //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                view_pager.setTemplatePageAdater(templatePageAdater, message);
                view_pager.setAdapter(templatePageAdater);
                view_pager.setCurrentItem(message.getCurrentPageNum());
                if (label.size() >= 6) {
                    // 每页6个，计算总页数
                    int totalPageCount = (int) Math.ceil(label.size() / 6.0);
                    tvCusPageCount.setText((message.getCurrentPageNum() + 1) + "/" + totalPageCount);
                    llPage.setVisibility(View.VISIBLE);
                    updatePreAndLastUI();
                } else {
                    llPage.setVisibility(View.GONE);
                }
            } else {
                view_pager.setVisibility(View.GONE);
            }
        }
        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                view_pager.updateMessageSelectItem(i);
                setCountText();
                updatePreAndLastUI();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        ivPreviousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager.selectPreviousPage();
                setCountText();
                updatePreAndLastUI();
            }
        });
        ivLastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager.selectLastPage();
                setCountText();
                updatePreAndLastUI();
            }
        });

        refreshItem();//左侧消息刷新顶和踩布局

        checkShowTransferBtn();//检查转人工逻辑
        //关联问题显示逻辑
        if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
            resetAnswersList();
        } else {
            hideAnswers();
        }
        refreshReadStatus();
    }

    private void setCountText() {
        if (tvCusPageCount != null && view_pager != null) {
            int currentPage = view_pager.getCurrentItem() + 1;
            int totalPages = 0;

            // 通过适配器获取总页数
            if (templatePageAdater != null) {
                totalPages = templatePageAdater.getCount();
            }

            tvCusPageCount.setText(currentPage + "/" + totalPages);
        }
    }

    //上一页下一页 UI
    public void updatePreAndLastUI() {
        if (view_pager.isFirstPage()) {
            ivPreviousPage.setAlpha(0.3f);
        } else {
            ivPreviousPage.setAlpha(1f);
        }
        if (view_pager.isLastPage()) {
            ivLastPage.setAlpha(0.3f);
        } else {
            ivLastPage.setAlpha(1f);
        }
    }

}