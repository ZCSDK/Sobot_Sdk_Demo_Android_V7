package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotRobotTemplate2PageAdater;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;
import com.sobot.chat.widget.robottemplate.RobotTemplateViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder2 extends MsgHolderBase {
    // 聊天的消息内容
    private TextView tvTip;
    public ZhiChiMessageBase message;
    private ImageView ivPreviousPage;//上一页
    private ImageView ivLastPage;//下一页
    private TextView tvCusPageCount;//当前页数
    private LinearLayout llPage;//分页ll

    private RobotTemplateViewPager pvTemplateSecond;
    private SobotRobotTemplate2PageAdater templatePageAdater;

    private Context mContext;

    public RobotTemplateMessageHolder2(Context context, View convertView) {
        super(context, convertView);
        tvCusPageCount = convertView.findViewById(R.id.tv_cus_page_count);
        tvTip = convertView.findViewById(R.id.tv_template_tip);
        ivPreviousPage = convertView.findViewById(R.id.iv_previous_page);
        ivLastPage = convertView.findViewById(R.id.iv_next_page);
        llPage = convertView.findViewById(R.id.ll_pre_next_page);
        pvTemplateSecond = convertView.findViewById(R.id.pv_template_second);
        this.mContext = context;
    }


    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tvTip, msgStr, getLinkTextColor());
                tvTip.setVisibility(View.VISIBLE);
            } else {
                tvTip.setVisibility(View.GONE);
            }
            llPage.setVisibility(View.GONE);
            checkShowTransferBtn();
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                String[] inputContent = multiDiaRespInfo.getInputContentList();
                ArrayList<SobotLablesViewModel> label = new ArrayList<>();
                if (interfaceRetList != null && !interfaceRetList.isEmpty()) {
                    resetMaxWidth();
                    for (int i = 0; i < interfaceRetList.size(); i++) {
                        Map<String, String> interfaceRet = interfaceRetList.get(i);
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(interfaceRet.get("title"));
                        lablesViewModel.setAnchor(interfaceRet.get("anchor"));
                        label.add(lablesViewModel);
                    }
                    templatePageAdater = new SobotRobotTemplate2PageAdater(mContext, pvTemplateSecond,"0", label, message, msgCallBack);
                    //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                    pvTemplateSecond.setTemplatePageAdater(templatePageAdater, message);
                    pvTemplateSecond.setAdapter(templatePageAdater);
                    pvTemplateSecond.setCurrentItem(message.getCurrentPageNum());
                    if (label.size() > 6) {
                        // 每页6个，计算总页数
                        int totalPageCount = (int) Math.ceil(label.size() / 6.0);
                        tvCusPageCount.setText((message.getCurrentPageNum() + 1) + "/" + totalPageCount);
                        llPage.setVisibility(View.VISIBLE);
                        updatePreAndLastUI();
                    } else {
                        llPage.setVisibility(View.GONE);
                    }
                } else if (inputContent != null && inputContent.length > 0) {
                    resetMaxWidth();
                    for (int i = 0; i < inputContent.length; i++) {
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(inputContent[i]);
                        label.add(lablesViewModel);
                    }
                    templatePageAdater = new SobotRobotTemplate2PageAdater(mContext,pvTemplateSecond, multiDiaRespInfo.getTemplate(), label, message, msgCallBack);
                    //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                    pvTemplateSecond.setTemplatePageAdater(templatePageAdater, message);
                    pvTemplateSecond.setAdapter(templatePageAdater);
                    pvTemplateSecond.setCurrentItem(message.getCurrentPageNum());
                    if (label.size() > 6) {
                        //每页6个
                        int totalPageCount = (int) Math.ceil(label.size() / 6.0);
                        tvCusPageCount.setText(((message.getCurrentPageNum() + 1) + "/" + totalPageCount));
                        llPage.setVisibility(View.VISIBLE);
                        updatePreAndLastUI();
                    } else {
                        llPage.setVisibility(View.GONE);
                    }
                    pvTemplateSecond.setVisibility(View.VISIBLE);
                } else {
                    pvTemplateSecond.setVisibility(View.GONE);
                }
            } else {
                pvTemplateSecond.setVisibility(View.GONE);
            }
        }
        pvTemplateSecond.setLayoutParams(new LinearLayout.LayoutParams(msgCardWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        pvTemplateSecond.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pvTemplateSecond.updateMessageSelectItem(i);
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
                pvTemplateSecond.selectPreviousPage();
                setCountText();
                updatePreAndLastUI();
            }
        });
        ivLastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTemplateSecond.selectLastPage();
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
        if (tvCusPageCount != null && pvTemplateSecond != null) {
            int currentPage = pvTemplateSecond.getCurrentItem() + 1;
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
        if (pvTemplateSecond.isFirstPage()) {
            ivPreviousPage.setAlpha(0.3f);
        } else {
            ivPreviousPage.setAlpha(1f);
        }
        if (pvTemplateSecond.isLastPage()) {
            ivLastPage.setAlpha(0.3f);
        } else {
            ivLastPage.setAlpha(1f);
        }
    }

}