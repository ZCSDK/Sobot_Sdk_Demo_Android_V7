package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotRobotTemplatePageAdater;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.RobotTemplateViewPager;
import com.sobot.chat.widget.horizontalgridpage.PageBuilder;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RobotTemplateMessageHolder2 extends MsgHolderBase {
    // 聊天的消息内容
    private TextView tv_msg;
    public ZhiChiMessageBase message;
    private TextView sobot_template2_item_previous_page;//上一页
    private TextView sobot_template2_item_last_page;//下一页
    private LinearLayout ll_sobot_template2_item_page;//分页ll

    private RobotTemplateViewPager view_pager;
    private SobotRobotTemplatePageAdater templatePageAdater;

    private static final int PAGE_SIZE = 30;

    private Context mContext;
    private PageBuilder pageBuilder;

    public RobotTemplateMessageHolder2(Context context, View convertView) {
        super(context, convertView);
        tv_msg = (TextView) convertView.findViewById(R.id.sobot_template2_msg);
        sobot_template2_item_previous_page = (TextView) convertView.findViewById(R.id.sobot_template2_item_previous_page);
        sobot_template2_item_last_page = (TextView) convertView.findViewById(R.id.sobot_template2_item_last_page);
        ll_sobot_template2_item_page = (LinearLayout) convertView.findViewById(R.id.ll_sobot_template2_item_page);
        view_pager = convertView.findViewById(R.id.view_pager);
        this.mContext = context;
    }


    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        this.message = message;
        if (message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null) {
            final SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
            String msgStr = ChatUtils.getMultiMsgTitle(multiDiaRespInfo);
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tv_msg, msgStr, getLinkTextColor());
                tv_msg.setVisibility(View.VISIBLE);
            } else {
                tv_msg.setVisibility(View.GONE);
            }
            checkShowTransferBtn();
            if ("000000".equals(multiDiaRespInfo.getRetCode())) {
                List<Map<String, String>> interfaceRetList = multiDiaRespInfo.getInterfaceRetList();
                String[] inputContent = multiDiaRespInfo.getInputContentList();
                ArrayList<SobotLablesViewModel> label = new ArrayList<>();
                if (interfaceRetList != null && interfaceRetList.size() > 0) {
                    resetMaxWidth();
                    for (int i = 0; i < getDisplayNum(multiDiaRespInfo, interfaceRetList.size()); i++) {
                        Map<String, String> interfaceRet = interfaceRetList.get(i);
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(interfaceRet.get("title"));
                        lablesViewModel.setAnchor(interfaceRet.get("anchor"));
                        label.add(lablesViewModel);
                    }
                    if (label.size() >= 10) {
                        ll_sobot_template2_item_page.setVisibility(View.VISIBLE);
                    } else {
                        ll_sobot_template2_item_page.setVisibility(View.GONE);
                    }
                    templatePageAdater = new SobotRobotTemplatePageAdater(mContext, "0",label, message, msgCallBack);
                    //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                    view_pager.setTemplatePageAdater(templatePageAdater, message);
                    view_pager.setAdapter(templatePageAdater);
                    view_pager.setCurrentItem(message.getCurrentPageNum());
                    initPreAndLastBtn(mContext);
                } else if (inputContent != null && inputContent.length > 0) {
                    resetMaxWidth();
                    for (int i = 0; i < getDisplayNum(multiDiaRespInfo, inputContent.length); i++) {
                        SobotLablesViewModel lablesViewModel = new SobotLablesViewModel();
                        lablesViewModel.setTitle(inputContent[i]);
                        label.add(lablesViewModel);
                    }
                    if (label.size() >= 10) {
                        ll_sobot_template2_item_page.setVisibility(View.VISIBLE);
                    } else {
                        ll_sobot_template2_item_page.setVisibility(View.GONE);
                    }
                    templatePageAdater = new SobotRobotTemplatePageAdater(mContext, multiDiaRespInfo.getTemplate(),label, message, msgCallBack);
                    //绑定adapter 判断上一页下一页 使用 message  缓存当前页，下次加载时滚动上次选中页使用
                    view_pager.setTemplatePageAdater(templatePageAdater, message);
                    view_pager.setAdapter(templatePageAdater);
                    view_pager.setCurrentItem(message.getCurrentPageNum());
                    initPreAndLastBtn(mContext);
                } else {
                    view_pager.setVisibility(View.GONE);
                }
            } else {
                view_pager.setVisibility(View.GONE);
            }
        }
        view_pager.setLayoutParams(new LinearLayout.LayoutParams(msgCardWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                view_pager.updateMessageSelectItem(i);
                if (view_pager.isFirstPage()) {
                    sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
                    Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_no_pre_page);
                    if (img != null) {
                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                        sobot_template2_item_previous_page.setCompoundDrawables(null, null, img, null);
                    }
                } else {
                    sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
                    Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_pre_page);
                    if (img != null) {
                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                        sobot_template2_item_previous_page.setCompoundDrawables(null, null, img, null);
                    }
                }

                if (view_pager.isLastPage()) {
                    sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
                    Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_no_last_page);
                    if (img != null) {
                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                        sobot_template2_item_last_page.setCompoundDrawables(null, null, img, null);
                    }
                } else {
                    sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
                    Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_last_page);
                    if (img != null) {
                        img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                        sobot_template2_item_last_page.setCompoundDrawables(null, null, img, null);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        sobot_template2_item_previous_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager.selectPreviousPage();
                updatePreBtn(context);
            }
        });
        sobot_template2_item_last_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_pager.selectLastPage();
                updateLastBtn(context);
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

    //上一页下一页ui初始化
    public void initPreAndLastBtn(Context context) {
        sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        Drawable lastImg = mContext.getResources().getDrawable(R.drawable.sobot_last_page);
        lastImg.setBounds(0, 0, lastImg.getMinimumWidth(), lastImg.getMinimumHeight());
        sobot_template2_item_last_page.setCompoundDrawables(null, null, lastImg, null);

        sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        Drawable preImg = mContext.getResources().getDrawable(R.drawable.sobot_pre_page);
        preImg.setBounds(0, 0, preImg.getMinimumWidth(), preImg.getMinimumHeight());
        sobot_template2_item_previous_page.setCompoundDrawables(null, null, preImg, null);

        if (view_pager.isFirstPage()) {
            sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
            Drawable npPreImg = mContext.getResources().getDrawable(R.drawable.sobot_no_pre_page);
            if (npPreImg != null) {
                npPreImg.setBounds(0, 0, npPreImg.getMinimumWidth(), npPreImg.getMinimumHeight());
                sobot_template2_item_previous_page.setCompoundDrawables(null, null, npPreImg, null);
            }
        }

        if (view_pager.isLastPage()) {
            sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
            Drawable noLastImg = mContext.getResources().getDrawable(R.drawable.sobot_no_last_page);
            if (noLastImg != null) {
                noLastImg.setBounds(0, 0, noLastImg.getMinimumWidth(), noLastImg.getMinimumHeight());
                sobot_template2_item_last_page.setCompoundDrawables(null, null, noLastImg, null);
            }
        }
    }

    public void updatePreBtn(Context context) {
        sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        Drawable lastImg = mContext.getResources().getDrawable(R.drawable.sobot_last_page);
        lastImg.setBounds(0, 0, lastImg.getMinimumWidth(), lastImg.getMinimumHeight());
        sobot_template2_item_last_page.setCompoundDrawables(null, null, lastImg, null);

        Drawable img = null;
        img = mContext.getResources().getDrawable(R.drawable.sobot_pre_page);
        sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        if (view_pager.isFirstPage()) {
            sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
            img = mContext.getResources().getDrawable(R.drawable.sobot_no_pre_page);
        }
        if (img != null) {
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            sobot_template2_item_previous_page.setCompoundDrawables(null, null, img, null);
        }
    }

    public void updateLastBtn(Context context) {
        sobot_template2_item_previous_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        Drawable preImg = mContext.getResources().getDrawable(R.drawable.sobot_pre_page);
        preImg.setBounds(0, 0, preImg.getMinimumWidth(), preImg.getMinimumHeight());
        sobot_template2_item_previous_page.setCompoundDrawables(null, null, preImg, null);

        sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_second));
        Drawable img = null;
        img = mContext.getResources().getDrawable(R.drawable.sobot_last_page);
        if (view_pager.isLastPage()) {
            sobot_template2_item_last_page.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_third));
            img = mContext.getResources().getDrawable(R.drawable.sobot_no_last_page);
        }
        if (img != null) {
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            sobot_template2_item_last_page.setCompoundDrawables(null, null, img, null);
        }
    }


    private int getDisplayNum(SobotMultiDiaRespInfo multiDiaRespInfo, int maxSize) {
        if (multiDiaRespInfo == null) {
            return 0;
        }
        return Math.min(multiDiaRespInfo.getPageNum() * PAGE_SIZE, maxSize);
    }

}