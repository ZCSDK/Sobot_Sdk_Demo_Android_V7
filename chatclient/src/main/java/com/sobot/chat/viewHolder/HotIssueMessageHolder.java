package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.BusinessLineRespVo;
import com.sobot.chat.api.model.FaqDocRespVo;
import com.sobot.chat.api.model.GroupRespVo;
import com.sobot.chat.api.model.SobotFaqDetailModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.horizontalscroll.IssueViewPagerdAdapter;
import com.sobot.chat.widget.horizontalscroll.MyHorizontalScrollView;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 常见问题
 * Created by guoqf on 2021.06.25
 */
public class HotIssueMessageHolder extends MsgHolderBase {

    private Context mContext;
    //业务类
    private MyHorizontalScrollView fastMenu;
    private IssueViewPagerdAdapter fastMenuAdapter;

    private SobotRCImageView sobot_hot_pic;
    private HorizontalScrollView tab_hot_title;//问题分类
    private View v_tab_hot_title_split;//分割线
    private LinearLayout horizontalScrollView_ll, lin_question_list;//分体分类
    private LinearLayout sobot_ll_switch_list;
    private int blockIndex = 0, groupIndex = 0;
    private int PAGE_NUM = 5;
    private int curPageNum = 0;

    private List<FaqDocRespVo> faqDocRespVoList = new ArrayList<>();


    public HotIssueMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mContext = context;
        fastMenu = convertView.findViewById(R.id.sobot_fast_menu);
        tab_hot_title = convertView.findViewById(R.id.tab_hot_title);
        v_tab_hot_title_split = convertView.findViewById(R.id.v_tab_hot_title_split);
        horizontalScrollView_ll = convertView.findViewById(R.id.horizontalScrollView_ll);
        sobot_hot_pic = convertView.findViewById(R.id.sobot_hot_pic);
        lin_question_list = convertView.findViewById(R.id.lin_question_list);
        sobot_ll_switch_list = convertView.findViewById(R.id.sobot_ll_switch_list);
        sobot_ll_switch_list.setVisibility(View.GONE);
        if (ChatUtils.isRtl(mContext)) {
            sobot_hot_pic.setTopRightRadius(ScreenUtils.dip2px(mContext, 12));
            sobot_hot_pic.setBottomRightRadius(ScreenUtils.dip2px(mContext, 12));
            sobot_hot_pic.setTopLeftRadius(0);
            sobot_hot_pic.setBottomLeftRadius(0);
        } else {
            sobot_hot_pic.setTopRightRadius(0);
            sobot_hot_pic.setBottomRightRadius(0);
            sobot_hot_pic.setTopLeftRadius(ScreenUtils.dip2px(mContext, 12));
            sobot_hot_pic.setBottomLeftRadius(ScreenUtils.dip2px(mContext, 12));
        }
        sobot_ll_switch_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 换一批
                if (faqDocRespVoList != null && faqDocRespVoList.size() > 0) {
                    curPageNum = curPageNum + 1;
                    int total = faqDocRespVoList.size();
                    int maxNum = (total % PAGE_NUM == 0) ? (total / PAGE_NUM) : (total / PAGE_NUM + 1);
                    curPageNum = (curPageNum >= maxNum) ? 0 : curPageNum;
                    showList();
                }
            }
        });
    }

    @Override
    public void bindData(Context context, final ZhiChiMessageBase message) {
        SobotFaqDetailModel bean = message.getFaqDetailModel();
        PAGE_NUM = bean.getGuidePageCount();
        //图片
        //ShowType : 展示类型:1-问题列表,2-分组加问题列表,3-业务加分组加问题列表
        if (bean.getShowType() == 1) {
            PAGE_NUM = bean.getGuidePageCount();
            sobot_hot_pic.setVisibility(View.GONE);
            //只显示列表
            curPageNum = 0;
            faqDocRespVoList = bean.getFaqDocRespVos();
            if (StringUtils.isNoEmpty(bean.getGuideWords())) {
                //显示引导语
                tab_hot_title.setVisibility(View.VISIBLE);
                v_tab_hot_title_split.setVisibility(View.VISIBLE);
                horizontalScrollView_ll.removeAllViews();
                TextView titleTv = new TextView(mContext);
                titleTv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                titleTv.setTextSize(14);
                titleTv.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_first));
                titleTv.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 15));
                titleTv.setText(bean.getGuideWords());
                titleTv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                horizontalScrollView_ll.addView(titleTv);
            } else {
                tab_hot_title.setVisibility(View.GONE);
                v_tab_hot_title_split.setVisibility(View.GONE);
            }
            setList(faqDocRespVoList);

        } else if (bean.getShowType() == 2) {
            PAGE_NUM = bean.getGuidePageCount();
            List<GroupRespVo> groupRespVoList = bean.getGroupRespVos();

            if (!TextUtils.isEmpty(bean.getImgUrl())) {
                ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
                params.width = (int) ScreenUtils.dpToPixel(mContext, 80);
                params.height = (int) ScreenUtils.dpToPixel(mContext, 294);
                sobot_hot_pic.setLayoutParams(params);
                sobot_hot_pic.setVisibility(View.VISIBLE);
                SobotBitmapUtil.display(mContext, CommonUtils.encode(bean.getImgUrl()), sobot_hot_pic);
            } else {
                sobot_hot_pic.setVisibility(View.GONE);
            }
            //显示分组和列表,防止滑动后，永远显示第一个
            if (groupIndex == 0) {
                showTab(groupRespVoList);
            }
        } else if (bean.getShowType() == 3) {
            //显示豆腐块、分组、列表
            List<BusinessLineRespVo> businessLineRespVoList = bean.getBusinessLineRespVos();
            showBlock(businessLineRespVoList, bean.getBusinessSetType());
        }
        refreshReadStatus();
    }

    /**
     * 换一换，分页显示文图列表
     */
    private void showList() {
        lin_question_list.removeAllViews();
        if (faqDocRespVoList != null && faqDocRespVoList.size() > 0) {
            int startNum = 0;
            int endNum = faqDocRespVoList.size();
            if (endNum > PAGE_NUM) {//有分组且不是全部
                startNum = curPageNum * PAGE_NUM;
//                endNum = Math.min(startNum + PAGE_NUM, faqList.size());
                endNum = (curPageNum + 1) * PAGE_NUM;
            }
            for (int i = startNum; i < endNum && i < faqDocRespVoList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad, null);
                if (ChatUtils.isRtl(mContext)) {
                    ImageView arrowIV = view.findViewById(R.id.sobot_im_icon_right);
                    if (arrowIV != null)
                        arrowIV.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                }
                TextView answer = view.findViewById(R.id.sobot_tv_name);
                final FaqDocRespVo info = faqDocRespVoList.get(i);
                answer.setText(info.getQuestionName());
                if (sobot_hot_pic.getVisibility() == View.VISIBLE) {
                    answer.setLines(1);
                } else {
                    answer.setMaxLines(2);
                }
                answer.setEllipsize(TextUtils.TruncateAt.END);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //questionType 问题类型：0-单轮，1-多轮，2-内部知识库文章，3-内部知识库普通问题
                        msgCallBack.clickIssueItem(info, "");
                    }
                });
                view.setBackgroundResource(R.drawable.sobot_item_issue_selector);
                lin_question_list.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            int childCount = lin_question_list.getChildCount();
            if (childCount < PAGE_NUM && faqDocRespVoList.size() > PAGE_NUM) {
                for (int i = childCount; i < PAGE_NUM; i++) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad, null);
                    TextView answer = view.findViewById(R.id.sobot_tv_name);
                    ImageView rightIV = view.findViewById(R.id.sobot_im_icon_right);
                    if (ChatUtils.isRtl(mContext)) {
                        if (rightIV != null)
                            rightIV.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                    }
                    answer.setText("");
                    rightIV.setVisibility(View.GONE);
                    view.setBackgroundResource(R.drawable.sobot_item_issue_selector);
                    lin_question_list.addView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            }
        }
    }

    /**
     * 创建问题列表
     *
     * @param faqList
     * @return
     */
    private void setList(List<FaqDocRespVo> faqList) {
        faqDocRespVoList = faqList;
        lin_question_list.removeAllViews();
        if (faqList != null && faqList.size() > PAGE_NUM) {
            //显示换一换
            sobot_ll_switch_list.setVisibility(View.VISIBLE);
        } else {
            //隐藏换一换
            sobot_ll_switch_list.setVisibility(View.GONE);
        }
        curPageNum = 0;
        if (faqList != null && faqList.size() > 0) {
            int startNum = 0;
            int endNum = faqList.size();
            if (endNum > PAGE_NUM) {//有分组且不是全部
                startNum = curPageNum * PAGE_NUM;
                endNum = (curPageNum + 1) * PAGE_NUM;
            }
            for (int i = startNum; i < endNum && i < faqList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_fad, null);
                if (ChatUtils.isRtl(mContext)) {
                    ImageView arrowIV = view.findViewById(R.id.sobot_im_icon_right);
                    if (arrowIV != null)
                        arrowIV.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                }
                TextView answer = view.findViewById(R.id.sobot_tv_name);
                final FaqDocRespVo info = faqList.get(i);
                answer.setText(info.getQuestionName());
                if (sobot_hot_pic.getVisibility() == View.VISIBLE) {
                    answer.setLines(1);
                } else {
                    answer.setMaxLines(2);
                }
                answer.setEllipsize(TextUtils.TruncateAt.END);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //questionType 问题类型：0-单轮，1-多轮，2-内部知识库文章，3-内部知识库普通问题
                        msgCallBack.clickIssueItem(info, "");
                    }
                });
                view.setBackgroundResource(R.drawable.sobot_item_issue_selector);
                lin_question_list.addView(view);
            }
        }

    }

    private void showTab(final List<GroupRespVo> groupRespVoList) {
        if (groupRespVoList != null && groupRespVoList.size() > 0) {
            groupIndex = 0;
            tab_hot_title.setVisibility(View.VISIBLE);
            v_tab_hot_title_split.setVisibility(View.VISIBLE);
            horizontalScrollView_ll.removeAllViews();
            for (int i = 0; i < groupRespVoList.size(); i++) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_hot_tab, null);
                if (view != null) {
                    TextView titleTv = view.findViewById(R.id.sobot_tab_item_name);
                    titleTv.setText(groupRespVoList.get(i).getGroupName());
                    horizontalScrollView_ll.addView(view);
                    final int position = i;
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            groupIndex = position;
                            List<FaqDocRespVo> datas = groupRespVoList.get(position).getFaqDocRespVos();
                            if (datas != null) {
                                setList(datas);
                                updateIndicator(position);
                            }
                        }
                    });
                }
            }
            tab_hot_title.scrollTo(0, 0);
            List<FaqDocRespVo> datas = groupRespVoList.get(groupIndex).getFaqDocRespVos();
            if (datas != null) {
                setList(datas);
                updateIndicator(groupIndex);
            }

        } else {
            tab_hot_title.setVisibility(View.GONE);
            v_tab_hot_title_split.setVisibility(View.GONE);
        }
    }

    private void updateIndicator(int index) {
        if (horizontalScrollView_ll.getChildCount() > 0) {
            for (int i = 0; i < horizontalScrollView_ll.getChildCount(); i++) {
                View view = horizontalScrollView_ll.getChildAt(i);
                TextView titleTv = view.findViewById(R.id.sobot_tab_item_name);
                View line = view.findViewById(R.id.sobot_tab_line);
                if (index == i) {
                    titleTv.setTypeface(null, Typeface.BOLD);
                    titleTv.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_first));
                    line.setBackgroundColor(ThemeUtils.getThemeColor(mContext));
                    line.setVisibility(View.VISIBLE);
                } else {
                    titleTv.setTypeface(null, Typeface.NORMAL);
                    titleTv.setTextColor(mContext.getResources().getColor(R.color.sobot_color_issue_text));
                    line.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    /**
     * 显示豆腐块
     *
     * @param businessLineList 业务
     * @param businessSetType  0 图文，1 仅图
     */
    private void showBlock(List<BusinessLineRespVo> businessLineList, int businessSetType) {
        if (businessLineList != null && businessLineList.size() > 0) {
            final List<BusinessLineRespVo> businessLineRespVoList = changeBusinessTitleMaxLength(businessLineList);
            fastMenuAdapter = new IssueViewPagerdAdapter(mContext, businessLineRespVoList, blockIndex, businessSetType);
            fastMenu.setOnItemClickListener(new MyHorizontalScrollView.OnItemClickListener() {
                @Override
                public void onClick(View view, int pos) {
                    PAGE_NUM = businessLineRespVoList.get(blockIndex).getGuidePageCount();
                    blockIndex = pos;
                    fastMenuAdapter.setSelectIndex(blockIndex);
                    fastMenu.initDatas(fastMenuAdapter);
                    if (businessLineRespVoList.get(blockIndex).getHasGroup() != 2) {
                        //图片
                        if (!TextUtils.isEmpty(businessLineRespVoList.get(blockIndex).getImgUrl())) {
                            ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
//                            params.width = (int) ScreenUtils.dpToPixel(mContext,80);
//                            params.width = (int) ScreenUtils.dpToPixel(mContext,70);
                            params.width = (int) ScreenUtils.dpToPixel(mContext, 80);
                            if (businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                                //有tab，设置高度，为(44+10)+(150+10)+28=242
//                                params.height = (int)ScreenUtils.dpToPixel(mContext,294);
                                params.height = (int) ScreenUtils.dpToPixel(mContext, 294);
                            } else {
                                //无tab,设置高度为(150+10)+28=188
//                                params.height = (int)ScreenUtils.dpToPixel(mContext,252);
                                params.height = (int) ScreenUtils.dpToPixel(mContext, 252);
                            }
                            sobot_hot_pic.setLayoutParams(params);
                            sobot_hot_pic.setVisibility(View.VISIBLE);

                            SobotBitmapUtil.display(mContext, CommonUtils.encode(businessLineRespVoList.get(blockIndex).getImgUrl()), sobot_hot_pic);
                        } else {
                            sobot_hot_pic.setVisibility(View.GONE);
                        }
                    }
                    //是否有分组：0-有，1-无 2-链接
                    if (businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                        tab_hot_title.setVisibility(View.VISIBLE);
                        v_tab_hot_title_split.setVisibility(View.VISIBLE);
                        groupIndex = 0;
                        curPageNum = 0;
                        showTab(businessLineRespVoList.get(blockIndex).getGroupRespVos());
                    } else if (businessLineRespVoList.get(blockIndex).getHasGroup() == 1) {
                        tab_hot_title.setVisibility(View.GONE);
                        v_tab_hot_title_split.setVisibility(View.GONE);
                        groupIndex = 0;
                        curPageNum = 0;
                        faqDocRespVoList = businessLineRespVoList.get(blockIndex).getFaqDocRespVos();
                        setList(faqDocRespVoList);
                    } else if (businessLineRespVoList.get(blockIndex).getHasGroup() == 2) {
                        //打开网页
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra("url", businessLineRespVoList.get(blockIndex).getBusinessLineUrl());
                        mContext.startActivity(intent);
                    }
                }
            });
            fastMenu.initDatas(fastMenuAdapter);
            PAGE_NUM = businessLineRespVoList.get(blockIndex).getGuidePageCount();
            if (blockIndex == 0) {
                //图片
                if (!TextUtils.isEmpty(businessLineRespVoList.get(blockIndex).getImgUrl())) {
                    ViewGroup.LayoutParams params = sobot_hot_pic.getLayoutParams();
                    params.width = (int) ScreenUtils.dpToPixel(mContext, 80);
                    if (businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                        //有tab，设置高度，为(44+10)+(150+10)+28=242
                        params.height = (int) ScreenUtils.dpToPixel(mContext, 294);
                    } else {
                        //无tab,设置高度为(150+10)+28=188
                        params.height = (int) ScreenUtils.dpToPixel(mContext, 252);
                    }
                    sobot_hot_pic.setLayoutParams(params);
                    sobot_hot_pic.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(mContext, CommonUtils.encode(businessLineRespVoList.get(blockIndex).getImgUrl()), sobot_hot_pic);
                } else {
                    sobot_hot_pic.setVisibility(View.GONE);
                }
            }
            if (businessLineRespVoList.get(blockIndex).getHasGroup() == 0) {
                if (groupIndex == 0) {
                    showTab(businessLineRespVoList.get(blockIndex).getGroupRespVos());
                }
            } else if (businessLineRespVoList.get(blockIndex).getHasGroup() == 1) {
                if (tab_hot_title.getVisibility() == View.VISIBLE) {
                    tab_hot_title.setVisibility(View.GONE);
                    v_tab_hot_title_split.setVisibility(View.GONE);
                }
                groupIndex = 0;
                curPageNum = 0;
                faqDocRespVoList = businessLineRespVoList.get(blockIndex).getFaqDocRespVos();
                setList(faqDocRespVoList);

            } else if (businessLineRespVoList.get(blockIndex).getHasGroup() == 2) {
                //网页的不直接打开，点击时才显示
//                    Intent intent = new Intent(mContext, WebViewActivity.class);
//                    intent.putExtra("url", businessLineRespVoList.get(blockIndex).getBusinessLineUrl());
//                    mContext.startActivity(intent);
            }
        }
    }

    //获取业务里边的文字内容的最长字符，用于占位位置的显示（visibility="invisible"），保证item 高度一致
    private List<BusinessLineRespVo> changeBusinessTitleMaxLength(List<BusinessLineRespVo> businessLineRespVoList) {
        String maxlenghtTitle = "";
        for (int i = 0; i < businessLineRespVoList.size(); i++) {
            if (StringUtils.isNoEmpty(businessLineRespVoList.get(i).getBusinessLineName()) && businessLineRespVoList.get(i).getBusinessLineName().length() > maxlenghtTitle.length()) {
                maxlenghtTitle = businessLineRespVoList.get(i).getBusinessLineName();
            }
        }
        for (int i = 0; i < businessLineRespVoList.size(); i++) {
            businessLineRespVoList.get(i).setTempBusinessLineName(maxlenghtTitle);
        }
        return businessLineRespVoList;
    }
}