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
import com.sobot.chat.adapter.SobotRobotTemplate3PageAdater;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.robottemplate.RobotTemplate3ViewPager;

import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder3 extends MsgHolderBase {

    private TextView tvTip;
    private LinearLayout llPreviousPage;//上一页
    private ImageView ivPreviousPage;//上一页
    private LinearLayout llLastPage;//下一页
    private ImageView ivLastPage;//下一页
    private TextView tvCusPageCount;//当前页数
    private LinearLayout llPage;//分页ll
    private RobotTemplate3ViewPager pvTemplateThird;
    private SobotRobotTemplate3PageAdater templatePageAdater;

    public ZhiChiMessageBase message;

    public RobotTemplateMessageHolder3(Context context, View convertView) {
        super(context, convertView);
        tvTip = (TextView) convertView.findViewById(R.id.tv_template_tip);
        tvCusPageCount = convertView.findViewById(R.id.tv_cus_page_count);
        ivPreviousPage = convertView.findViewById(R.id.iv_previous_page);
        ivLastPage = convertView.findViewById(R.id.iv_next_page);
        llPreviousPage = convertView.findViewById(R.id.ll_previous_page);
        llLastPage = convertView.findViewById(R.id.ll_next_page);
        llPage = convertView.findViewById(R.id.ll_pre_next_page);
        pvTemplateThird = convertView.findViewById(R.id.pv_template_third);
        this.mContext = context;
    }

    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tvTip, msgStr.replaceAll("\n", "<br/>"), getLinkTextColor());
                tvTip.setVisibility(View.VISIBLE);
            } else {
                tvTip.setVisibility(View.INVISIBLE);
            }
            checkShowTransferBtn();
            llPage.setVisibility(View.GONE);
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                if (interfaceRetList != null && !interfaceRetList.isEmpty()) {
                    resetMaxWidth();
                    //+10 左右阴影间距5
                    templatePageAdater = new SobotRobotTemplate3PageAdater(mContext, msgMaxWidth + ScreenUtils.dip2px(mContext, 10), pvTemplateThird, interfaceRetList, message, msgCallBack);
                    //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                    pvTemplateThird.setTemplatePageAdater(templatePageAdater, message);
                    pvTemplateThird.setAdapter(templatePageAdater);
                    pvTemplateThird.setCurrentItem(message.getCurrentPageNum());
                    if (interfaceRetList.size() > 3) {
                        // 每页3个，计算总页数
                        int totalPageCount = (int) Math.ceil(interfaceRetList.size() / 3.0);
                        tvCusPageCount.setText((message.getCurrentPageNum() + 1) + "/" + totalPageCount);
                        llPage.setVisibility(View.VISIBLE);
                        updatePreAndLastUI();
                    } else {
                        llPage.setVisibility(View.GONE);
                    }
                    pvTemplateThird.setVisibility(View.VISIBLE);
                } else {
                    pvTemplateThird.setVisibility(View.GONE);
                }
            } else {
                pvTemplateThird.setVisibility(View.GONE);
            }
        }
        pvTemplateThird.setLayoutParams(new LinearLayout.LayoutParams(msgCardWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        pvTemplateThird.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pvTemplateThird.updateMessageSelectItem(i);
                setCountText();
                updatePreAndLastUI();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        llPreviousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTemplateThird.selectPreviousPage();
                setCountText();
                updatePreAndLastUI();
            }
        });
        llLastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pvTemplateThird.selectLastPage();
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
        if (tvCusPageCount != null && pvTemplateThird != null) {
            int currentPage = pvTemplateThird.getCurrentItem() + 1;
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
        if (pvTemplateThird.isFirstPage()) {
            ivPreviousPage.setAlpha(0.3f);
        } else {
            ivPreviousPage.setAlpha(1f);
        }
        if (pvTemplateThird.isLastPage()) {
            ivLastPage.setAlpha(0.3f);
        } else {
            ivLastPage.setAlpha(1f);
        }
    }
}