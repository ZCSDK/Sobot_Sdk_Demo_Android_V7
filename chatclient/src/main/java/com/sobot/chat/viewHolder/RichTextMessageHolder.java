package com.sobot.chat.viewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.activity.halfdialog.SobotAIFromListActivity;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.SobotAiAnalysisInfo;
import com.sobot.chat.api.model.SobotAiButtonInfo;
import com.sobot.chat.api.model.SobotAiLinkInfo;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotRightAlignLineLayout;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 富文本消息
 */
public class RichTextMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView msg; // 聊天的消息内容
    private LinearLayout sobot_rich_ll;//拆分的富文本消息
    private TextView sobot_msgStripe; // 多轮会话中配置的引导语


    private LinearLayout sobot_ll_switch;//换一组按钮
    private TextView sobot_tv_switch;
    private View sobot_view_split;//换一组和查看详情分割线
    private LinearLayout ll_ai_reference_count;//大模型来源 底部区域
    private TextView tv_ai_reference_count;//大模型来源 参考几篇资料
    private View view_ai_reference_split;//大模型来源 分割线

    private ProgressBar progressbarLoading;//大模型答案返回中

    private LinearLayout llButtonRoot;//按钮列表根节点
    private SobotRightAlignLineLayout alButton;//按钮列表
    private LinearLayout moreLL;//更多
    private TextView moreTV;
    private ImageView moreIV;

    private List<SobotAiButtonInfo> inputContentList; // 保存输入内容数组
    private int currentLoadedCount = 0; // 当前已加载的数量
    private int itemsPerLoad = 8; // 每次加载的数量
    private ProgressBar plAiButton; //大模型答案有按钮返回
    private String type = "-1";//接待模式， 1仅机器人时 转人工不能点


    public RichTextMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msg = (TextView) convertView.findViewById(R.id.sobot_msg);
        sobot_rich_ll = (LinearLayout) convertView.findViewById(R.id.sobot_rich_ll);
        sobot_msgStripe = (TextView) convertView.findViewById(R.id.sobot_msgStripe);
        sobot_ll_switch = (LinearLayout) convertView.findViewById(R.id.sobot_ll_switch);
        sobot_tv_switch = (TextView) convertView.findViewById(R.id.sobot_tv_switch);
        sobot_tv_switch.setText(R.string.sobot_switch);
        sobot_view_split = convertView.findViewById(R.id.sobot_view_split);
        ll_ai_reference_count = convertView.findViewById(R.id.ll_ai_reference_count);
        tv_ai_reference_count = convertView.findViewById(R.id.tv_ai_reference_count);
        view_ai_reference_split = convertView.findViewById(R.id.view_ai_reference_split);
        answersList = (LinearLayout) convertView.findViewById(R.id.sobot_answersList);
        progressbarLoading = (ProgressBar) convertView.findViewById(R.id.progressbar_loading);
        sobot_ll_switch.setOnClickListener(this);
        moreTV = convertView.findViewById(R.id.tv_more);
        moreLL = convertView.findViewById(R.id.ll_more);
        moreIV = convertView.findViewById(R.id.iv_more);
        llButtonRoot = convertView.findViewById(R.id.ll_button_root);
        alButton = convertView.findViewById(R.id.al_button);
        plAiButton = (ProgressBar) convertView.findViewById(R.id.progressbar_loading_ai_button);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        this.mContext = context;
        this.message = message;
        if (initMode != null) {
            type = initMode.getType();
        }
        //隐藏来源和大模型生成按钮
        hideAIRefetenceUI();
        if (llButtonRoot != null) {
            llButtonRoot.setVisibility(View.GONE);
        }
        // 设置plAiButton的颜色为主题色
        if (plAiButton != null && moreLL != null) {
            Drawable moreDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_aiagent_button_more_bg, null);
            if (moreDrawable != null) {
                moreLL.setBackground(moreDrawable);
                moreLL.setElevation(12f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    moreLL.setOutlineSpotShadowColor(ContextCompat.getColor(context, R.color.sobot_color_shado));
                }
                moreLL.setTranslationY(6f);
            }
            plAiButton.getIndeterminateDrawable().setColorFilter(
                    ThemeUtils.getThemeColor(context),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        }
        if (message != null && StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant()) && progressbarLoading != null) {
            if (message.getSugguestionsFontColor() == 0 && StringUtils.isNoEmpty(message.getSendStatus()) && "0".equals(message.getSendStatus())) {
                //大模型实时问 刚发消息时，显示3个点加载效果
                progressbarLoading.setVisibility(View.VISIBLE);
            }
            if (message.isAiAgentReceiveMsgEnd() || message.getSugguestionsFontColor() == 1 || (StringUtils.isNoEmpty(message.getSendStatus()) && ("1".equals(message.getSendStatus()) || "2".equals(message.getSendStatus())))) {
                //历史记录 大模型结束或者大模型返回有数据（sendStatus=1或者2） 都需要隐藏
                progressbarLoading.setVisibility(View.GONE);
            }
            //如果是aiagent 答案
            if (message.getAnswer() != null && message.getAnswer().getRichList() != null && !message.getAnswer().getRichList().isEmpty() && (StringUtils.isNoEmpty(message.getAnswer().getRichList().get(0).getMsg()))) {
                if (message.isAiAgentReceiveMsgEnd() || message.getSugguestionsFontColor() == 1) {
                    plAiButton.setVisibility(View.GONE);//先隐藏快捷回复加载按钮
                    //如果大模型答案结束了 或者历史记录 有快捷回复按钮返回
                    if (message.getButtonInfos() != null && !message.getButtonInfos().isEmpty()) {
                        llButtonRoot.setVisibility(View.VISIBLE);
                        if (inputContentList == null) {
                            inputContentList = new ArrayList<>();
                        }
                        // 重置状态
                        alButton.removeAllViews();
                        inputContentList.clear();
                        boolean isShow = false;
                        if (message.getAnswer() != null && StringUtils.isNoEmpty(message.getAnswer().toString()) && !isMatchButtonContentTags(message.getAnswer().toString())) {
                            isShow = true;
                        }
                        for (int i = 0; i < message.getButtonInfos().size(); i++) {
                            if (message.getButtonInfos().get(i) != null) {
                                if (isShow) {
                                    if ("MSG".equals(message.getButtonInfos().get(i).getButtonAction()) || "TRANSFER".equals(message.getButtonInfos().get(i).getButtonAction()) || "URL".equals(message.getButtonInfos().get(i).getButtonAction())) {
                                        //只显示消息 url 转人工 三种
                                        inputContentList.add(message.getButtonInfos().get(i));
                                    }
                                } else {
                                    if ("MSG".equals(message.getButtonInfos().get(i).getButtonAction())) {
                                        //只显示消息类型按钮
                                        inputContentList.add(message.getButtonInfos().get(i));
                                    }
                                }
                            }
                        }
                        if (!inputContentList.isEmpty()) {
                            // 按照 TRANSFER、URL、MSG 的顺序排序
                            Collections.sort(inputContentList, new Comparator<SobotAiButtonInfo>() {
                                @Override
                                public int compare(SobotAiButtonInfo o1, SobotAiButtonInfo o2) {
                                    return getActionPriority(o1.getButtonAction()) - getActionPriority(o2.getButtonAction());
                                }

                                private int getActionPriority(String action) {
                                    if ("URL".equals(action)) return 0;
                                    if ("TRANSFER".equals(action)) return 1;
                                    if ("MSG".equals(action)) return 2;
                                    return 3;
                                }
                            });
                            if (inputContentList.size() > itemsPerLoad) {
                                //多余8个才显示更多
                                moreLL.setVisibility(View.VISIBLE);
                                moreIV.setVisibility(View.VISIBLE);
                                moreTV.setVisibility(View.VISIBLE);
                                Drawable moreDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_aiagent_button_more_bg, null);
                                Drawable arrowDownDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_notice_arrow_down, null);
                                if (moreDrawable != null) {
                                    moreLL.setBackground(ThemeUtils.applyColorToDrawable(moreDrawable, ThemeUtils.getThemeColor(mContext)));
                                }
                                moreLL.setElevation(0f);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    moreLL.setOutlineSpotShadowColor(ContextCompat.getColor(context, R.color.sobot_color_transparent));
                                }
                                moreLL.setTranslationY(0f);
                                if (arrowDownDrawable != null) {
                                    moreIV.setBackground(ThemeUtils.applyColorToDrawable(arrowDownDrawable, ThemeUtils.getThemeColor(mContext)));
                                }
                                moreTV.setTextColor(ThemeUtils.getThemeColor(mContext));
                                // 设置更多按钮点击事件
                                moreLL.setOnClickListener(v -> loadMoreItems(true));
                            } else {
                                moreLL.setVisibility(View.GONE);
                            }
                            // 首次加载最多8个
                            loadMoreItems(false);
                        } else {
                            llButtonRoot.setVisibility(View.GONE);
                        }
                    } else {
                        llButtonRoot.setVisibility(View.GONE);
                    }
                } else {
                    if (hasButtonActionMsg(message)) {
                        //实时返回过程中 有快捷回复按钮返回，先显示加载按钮，等结束后显示更多;同时这个按钮不能点，知道结束后才能点击
                        if (llButtonRoot != null) {
                            llButtonRoot.setVisibility(View.VISIBLE);
                            alButton.setVisibility(View.GONE);//隐藏快捷回复加载按钮
                            moreLL.setVisibility(View.VISIBLE);
                            plAiButton.setVisibility(View.VISIBLE);//显示快捷回复加载按钮
                            moreIV.setVisibility(View.GONE);//隐藏更多
                            moreTV.setVisibility(View.GONE);
                        }
                    } else {
                        //隐藏大模型按钮快捷回复控件
                        if (llButtonRoot != null) {
                            llButtonRoot.setVisibility(View.GONE);
                        }
                    }
                    //隐藏大模型来源数量控件
                    hideAIRefetenceUI();
                }
            } else {
                //隐藏大模型来源数量和按钮快捷回复控件
                if (llButtonRoot != null) {
                    llButtonRoot.setVisibility(View.GONE);
                }
                if (sobot_msg_content_ll != null && mContext != null) {
                    //四个角弧度一样
                    Drawable bg = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_chat_msg_bg_left_second, null);
                    if (bg != null) {
                        sobot_msg_content_ll.setBackground(bg);
                    }
                }
            }
        } else {
            //隐藏大模型来源数量和按钮快捷回复控件ui
            if (llButtonRoot != null) {
                llButtonRoot.setVisibility(View.GONE);
            }
            hideAIRefetenceUI();
            if (progressbarLoading != null) {
                progressbarLoading.setVisibility(View.GONE);
            }
        }
        // 更具消息类型进行对布局的优化
        if (message.getAnswer() != null) {
            setupMsgContent(context, message);
            sobot_msgStripe.setVisibility(View.GONE);
        }

        if (message.isGuideGroupFlag()//有分组
                && message.getListSuggestions() != null//有分组问题列表
                && message.getGuideGroupNum() > -1//分组不是全部
                && !message.getListSuggestions().isEmpty()//问题数量大于0
                && message.getGuideGroupNum() < message.getListSuggestions().size()//分组数量小于问题数量
        ) {
            sobot_ll_switch.setVisibility(View.VISIBLE);
            sobot_view_split.setVisibility(View.VISIBLE);
        } else {
            sobot_ll_switch.setVisibility(View.GONE);
            sobot_view_split.setVisibility(View.GONE);
        }

        if (!isRight) {
            msg.setMinHeight(0);
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
            } else {
                hideAnswers();
            }
            msg.setMaxWidth(msgMaxWidth);
//            sobot_rich_ll.setLayoutParams(new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        setLongClickListener(msg);
        setLongClickListener(sobot_msg_content_ll);
        refreshReadStatus();
    }

    private void hideAIRefetenceUI() {
        //隐藏大模型来源数量ui
        if (ll_ai_reference_count != null) {
            view_ai_reference_split.setVisibility(View.GONE);
            ll_ai_reference_count.setVisibility(View.GONE);
            tv_ai_reference_count.setText("");
        }
    }

    /**
     * 加载更多项
     */
    private void loadMoreItems(boolean isClickMore) {
        try {
            if (message == null || inputContentList == null) {
                if (moreLL != null) {
                    moreLL.setVisibility(View.GONE);
                }
                return;
            }

            // 从message中获取缓存的已加载数量
            int cachedCount = message.getLoadedButtonCount();
            if (cachedCount > 0) {
                currentLoadedCount = cachedCount;
            }

            llButtonRoot.setVisibility(View.VISIBLE);
            alButton.setVisibility(View.VISIBLE); // 显示快捷回复加载按钮
            plAiButton.setVisibility(View.GONE); // 隐藏快捷回复加载按钮

            // 计算需要加载的起始位置和数量
            int startLoadIndex;
            int loadCount;

            if (isClickMore) {
                // 点击"更多"按钮：从当前已加载位置继续加载itemsPerLoad个
                startLoadIndex = currentLoadedCount;
                loadCount = Math.min(itemsPerLoad, inputContentList.size() - currentLoadedCount);
                if (loadCount > 0) {
                    currentLoadedCount += loadCount;
                    message.setLoadedButtonCount(currentLoadedCount);
                }
            } else if (cachedCount > 0) {
                // 从缓存恢复：已经加载了cachedCount个，不需要重新加载
                startLoadIndex = 0;
                loadCount = cachedCount;
                // 清理之前的视图
                alButton.removeAllViews();
            } else {
                // 第一次加载：从0开始
                startLoadIndex = 0;
                loadCount = Math.min(itemsPerLoad, inputContentList.size());
                currentLoadedCount = loadCount;
                message.setLoadedButtonCount(currentLoadedCount);
                // 第一次加载时清理之前的视图
                alButton.removeAllViews();
            }

            // 加载指定范围的项
            for (int i = startLoadIndex; i < startLoadIndex + loadCount; i++) {
                if (i < inputContentList.size() && alButton != null) {
                    String str = StringUtils.checkStringIsNull(inputContentList.get(i).getLabel());
                    try {
                        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_aiagent_item_bottom_layout, null);
                        TextView nameTv = view.findViewById(R.id.tv_lable_name);
                        if (nameTv != null) {
                            if (message.getSugguestionsFontColor() == 1) {
                                // 历史记录 - 使用半透明主题色
                                nameTv.setTextColor(ThemeUtils.getThemeColor(mContext));
                                int themeColor = ThemeUtils.getThemeColor(mContext);
                                int semiTransparentColor = ColorUtils.setAlphaComponent(themeColor, 128); // 50%透明度
                                nameTv.setTextColor(semiTransparentColor);
                                nameTv.setBackground(ContextCompat.getDrawable(mContext, R.drawable.sobot_aiagent_button_item_bg_no_click));
                                if (StringUtils.isNoEmpty(inputContentList.get(i).getButtonAction())) {
                                    if ("URL".equals(inputContentList.get(i).getButtonAction())) {
                                        Drawable moreDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_aiagent_button_item_bg, null);
                                        if (moreDrawable != null) {
                                            nameTv.setBackground(moreDrawable);
                                        }
                                        nameTv.setTextColor(ThemeUtils.getThemeColor(mContext));
                                        nameTv.setBackground(moreDrawable);
                                        nameTv.setElevation(12f);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            nameTv.setOutlineSpotShadowColor(ContextCompat.getColor(mContext, R.color.sobot_color_shado));
                                        }
                                        nameTv.setTranslationY(6f);
                                        SobotRightAlignLineLayout.setChildType(view, 0);
                                        String url = StringUtils.checkStringIsNull(inputContentList.get(i).getUrl());
                                        view.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(mContext, WebViewActivity.class);
                                                intent.putExtra("url", url);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                mContext.startActivity(intent);
                                            }
                                        });
                                    } else if ("TRANSFER".equals(inputContentList.get(i).getButtonAction())) {
                                        SobotRightAlignLineLayout.setChildType(view, 1);
                                    } else {
                                        SobotRightAlignLineLayout.setChildType(view, 2);
                                    }
                                }
                            } else {
                                nameTv.setTextColor(ThemeUtils.getThemeColor(mContext));
                                nameTv.setBackground(ContextCompat.getDrawable(mContext, R.drawable.sobot_aiagent_button_item_bg));
                            }
                            nameTv.setText(StringUtils.isEmpty(str) ? "" : str + "");
                            alButton.addView(view);
                        }
                        if (message.getSugguestionsFontColor() == 0) {
                            //实时的能点击
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (msgCallBack != null) {
                                        ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                                        msgObj.setNodeId(message.getNodeId());
                                        msgObj.setProcessId(message.getProcessId());
                                        msgObj.setVariableId(message.getVariableId());
                                        msgObj.setContent(StringUtils.checkStringIsNull(str));
                                        msgCallBack.sendMessageToRobot(msgObj, 0, 1, "");
                                        //隐藏按钮
                                        message.setHideVariableValueEnums(true);
                                        llButtonRoot.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // 检查是否还有更多数据
            if (moreLL != null) {
                if (currentLoadedCount >= inputContentList.size()) {
                    moreLL.setVisibility(View.GONE);
                } else {
                    moreLL.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            if (moreLL != null) {
                moreLL.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 显示 顶踩 按钮
     */
    public void showRevaluateBtn() {
        super.showRevaluateBtn();
        if (dingcaiIsShowRight()) {
            //有顶和踩时显示信息显示两行 64-12-12=40 总高度减去上下内间距
            msg.setMinHeight(ScreenUtils.dip2px(mContext, 44));
            //有顶和踩时,拆分后的富文本消息如果只有一个并且是文本类型设置最小高度 64-12-12=40 总高度减去上下内间距
            if (sobot_rich_ll != null && sobot_rich_ll.getChildCount() == 1) {
                for (int i = 0; i < sobot_rich_ll.getChildCount(); i++) {
                    View view = sobot_rich_ll.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setMinHeight(ScreenUtils.dip2px(mContext, 44));
                    }
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == sobot_ll_switch) {
            // 换一组
            if (message != null && message.getListSuggestions() != null && message.getListSuggestions().size() > 0) {
                int pageNum = message.getCurrentPageNum() + 1;
                int total = message.getListSuggestions().size();
                int pre = message.getGuideGroupNum();
                if (pre == 0) {
                    pre = 5;
                }
                int maxNum = (total % pre == 0) ? (total / pre) : (total / pre + 1);
                pageNum = (pageNum >= maxNum) ? 0 : pageNum;
                message.setCurrentPageNum(pageNum);
                resetAnswersList();
            }


        }
    }


    private void setupMsgContent(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && message.getAnswer().getRichList() != null && message.getAnswer().getRichList().size() > 0) {
            sobot_rich_ll.removeAllViews();
            try {
                if (message.getAnswer().getRichList().size() > 1) {
                    //richList 数量大于1个，如果里边有不是卡片的超链接，超链接的上个又是文本的情况，需要单独处理（合并到上个文本后边）
                    List<ChatMessageRichListModel> tempRichList = new ArrayList<>();
                    for (int i = 0; i < message.getAnswer().getRichList().size(); i++) {
                        //处理后的临时richList,替换旧的richList
                        ChatMessageRichListModel richListModel = message.getAnswer().getRichList().get(i);
                        if (richListModel != null) {
                            //如果当前是文本,文本又不是卡片，需要处理
                            if (richListModel.getType() == 0 && richListModel.getShowType() != 1) {
                                ChatMessageRichListModel model = new ChatMessageRichListModel();
                                model.setType(0);
                                if (!tempRichList.isEmpty()) {
                                    //如果上一个是文本,需要合并当前文本到上个文本后边
                                    ChatMessageRichListModel tempRichListModel = tempRichList.get(tempRichList.size() - 1);
                                    if (tempRichListModel != null && tempRichListModel.getType() == 0) {
                                        if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                            model.setMsg(tempRichListModel.getMsg() + "<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                        } else {
                                            model.setMsg(tempRichListModel.getMsg() + richListModel.getMsg());
                                        }
                                        tempRichList.remove(tempRichList.size() - 1);
                                        tempRichList.add(model);
                                    } else {
                                        tempRichList.add(richListModel);
                                    }
                                } else {
                                    if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                        //当前是超链接，同时又不是卡片
                                        model.setMsg("<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                    } else {
                                        model.setMsg(richListModel.getMsg());
                                    }
                                    tempRichList.add(model);
                                }
                            } else {
                                tempRichList.add(richListModel);
                            }
                        }
                    }
                    if (!tempRichList.isEmpty()) {
                        message.getAnswer().setRichList(tempRichList);
                    }
                }
            } catch (Exception e) {
            }
            for (int i = 0; i < message.getAnswer().getRichList().size(); i++) {
                final ChatMessageRichListModel richListModel = message.getAnswer().getRichList().get(i);
                if (richListModel != null) {
                    //如果最后一个是空行，直接过滤掉不显示
                    if (TextUtils.isEmpty(richListModel.getMsg()) && i == (message.getAnswer().getRichList().size() - 1)) {
                        continue;
                    }
                    // 0：文本，1：图片，2：音频，3：视频，4：文件
                    if (richListModel.getType() == 0) {
                        TextView textView = new TextView(mContext);
                        textView.setGravity(Gravity.CENTER_VERTICAL);
                        textView.setIncludeFontPadding(false);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                        textView.setLineSpacing(mContext.getResources().getDimension(R.dimen.sobot_text_line_spacing_extra), 1);
                        if (i != 0) {
                            LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            wlayoutParams.setMargins(0, ScreenUtils.dip2px(context, 10), 0, 0);
                            textView.setLayoutParams(wlayoutParams);
                        } else {
                            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        }
                        textView.setMaxWidth(msgMaxWidth);
                        setLongClickListener(textView);
                        if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                            try {
                                textView.setTextColor(getLinkTextColor());
                            } catch (Exception e) {
                            }
//                            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
//                            textView.getPaint().setAntiAlias(true);//抗锯齿
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (SobotOption.newHyperlinkListener != null) {
                                        //如果返回true,拦截;false 不拦截
                                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, richListModel.getMsg());
                                        if (isIntercept) {
                                            return;
                                        }
                                    }
                                    Intent intent = new Intent(context, WebViewActivity.class);
                                    intent.putExtra("url", richListModel.getMsg());
                                    context.startActivity(intent);
                                }
                            });
                            textView.setText(richListModel.getName());
                            sobot_rich_ll.addView(textView);
                            if (richListModel.getShowType() == 1) {
                                //超链接，并且是卡片形式才显示卡片
                                int minWidth = ScreenUtils.dip2px(mContext, 240);
                                int maxWidth = ScreenUtils.dip2px(mContext, 400);
                                int constrainedWidth = Math.min(maxWidth, msgMaxWidth);
                                int cardWidth = Math.min(minWidth, constrainedWidth);
                                final View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_link_card, null);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, ScreenUtils.dip2px(mContext, 4));
                                view.setLayoutParams(layoutParams);
                                TextView tv_title = view.findViewById(R.id.tv_title);
                                tv_title.setText(R.string.sobot_parsing);
                                if (richListModel.getSobotLink() != null) {
                                    tv_title = view.findViewById(R.id.tv_title);
                                    TextView tv_des = view.findViewById(R.id.tv_des);
                                    ImageView image_link = view.findViewById(R.id.image_link);
                                    if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle())) {
                                        tv_title.setVisibility(View.GONE);
                                    } else {
                                        tv_title.setText(richListModel.getSobotLink().getTitle());
                                        tv_title.setVisibility(View.VISIBLE);
                                    }
                                    tv_des.setText(TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) ? richListModel.getMsg() : richListModel.getSobotLink().getDesc());
                                    SobotBitmapUtil.display(mContext, richListModel.getSobotLink().getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                    if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle()) && TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) && TextUtils.isEmpty(richListModel.getSobotLink().getImgUrl())) {
                                        view.setVisibility(View.GONE);
                                    }
                                } else {
                                    SobotMsgManager.getInstance(mContext).getZhiChiApi().getHtmlAnalysis(context, richListModel.getMsg(), new StringResultCallBack<SobotLink>() {
                                        @Override
                                        public void onSuccess(SobotLink link) {
                                            if (link != null) {
                                                richListModel.setSobotLink(link);
                                                TextView tv_title = view.findViewById(R.id.tv_title);
                                                TextView tv_des = view.findViewById(R.id.tv_des);
                                                ImageView image_link = view.findViewById(R.id.image_link);
                                                if (TextUtils.isEmpty(link.getTitle())) {
                                                    tv_title.setVisibility(View.VISIBLE);
                                                    tv_title.setText(richListModel.getName());
                                                } else {
                                                    tv_title.setText(link.getTitle());
                                                    tv_title.setVisibility(View.VISIBLE);
                                                }
                                                tv_des.setText(TextUtils.isEmpty(link.getDesc()) ? richListModel.getMsg() : link.getDesc());
                                                if (mContext != null && !isActivityDestroyed(mContext)) {
                                                    SobotBitmapUtil.display(mContext, link.getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e, String s) {
                                            if (view != null) {
                                                TextView tv_title = view.findViewById(R.id.tv_title);
                                                tv_title.setText(richListModel.getMsg());
                                                ImageView image_link = view.findViewById(R.id.image_link);
                                                SobotBitmapUtil.display(mContext, "", image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                            }
                                        }
                                    });
                                }
                                sobot_rich_ll.addView(view);
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (SobotOption.newHyperlinkListener != null) {
                                            //如果返回true,拦截;false 不拦截
                                            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, richListModel.getMsg());
                                            if (isIntercept) {
                                                return;
                                            }
                                        }
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("url", richListModel.getMsg());
                                        context.startActivity(intent);
                                    }
                                });
                            }
                        } else {
                            textView.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_left_msg_text_color));
                            if (!TextUtils.isEmpty(richListModel.getMsg()) && i == (message.getAnswer().getRichList().size() - 1)) {
                                String content = richListModel.getMsg().trim();
                                if ((message.getAnalysisInfo() != null || message.getButtonInfos() != null) && (content.contains("]$$") || content.contains("]##"))) {
                                    textView.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 7));//防止最后一行有图标按钮显示不全
                                    textView.setLineSpacing(mContext.getResources().getDimension(R.dimen.sobot_text_line_spacing_aianswar_button), 1);
                                    content = processContentWithButtonTags(content, message);//替换来源： ##[x]$$##[x]$$ 或者%%[0]##
                                }
                                if (StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant())) {
                                    content = ChatUtils.parseMarkdownData(content);//历史记录中大模型消息 解析成html
                                }
                                boolean isHistoryMsg;//
                                if (StringUtils.isNoEmpty(type) && (ZhiChiConstant.type_robot_only + "").equals(type)) {
                                    //仅机器人 变成历史记录 不能点
                                    isHistoryMsg = true;
                                } else {
                                    isHistoryMsg = message.getSugguestionsFontColor() == 1;
                                }
                                HtmlTools.getInstance(mContext).setRichText(textView, content, getLinkTextColor(), isHistoryMsg, message.isAiAgentReceiveMsgEnd(), message.isAiAgentReceiveMsgEnd());
                            } else {
                                String tempContent = richListModel.getMsg().trim();
                                if ((message.getAnalysisInfo() != null || message.getButtonInfos() != null) && (tempContent.contains("]$$") || tempContent.contains("]##"))) {
                                    textView.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 7));//防止最后一行有图标按钮显示不全
                                    textView.setLineSpacing(mContext.getResources().getDimension(R.dimen.sobot_text_line_spacing_aianswar_button), 1);
                                    tempContent = processContentWithButtonTags(tempContent, message);//替换来源： ##[x]$$ 或者%%[0]##
                                }
                                if (StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant())) {
                                    tempContent = ChatUtils.parseMarkdownData(tempContent);//历史记录中大模型消息 解析历史记录中大模型消息 解析成html
                                }
                                boolean isHistoryMsg;//
                                if (StringUtils.isNoEmpty(type) && (ZhiChiConstant.type_robot_only + "").equals(type)) {
                                    //仅机器人 变成历史记录 不能点
                                    isHistoryMsg = true;
                                } else {
                                    isHistoryMsg = message.getSugguestionsFontColor() == 1;
                                }
                                HtmlTools.getInstance(mContext).setRichText(textView, tempContent, getLinkTextColor(), isHistoryMsg, message.isAiAgentReceiveMsgEnd(), message.isAiAgentReceiveMsgEnd());
                            }
                            sobot_rich_ll.addView(textView);
                        }
                    } else if (richListModel.getType() == 1) {
                        int imgHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
                        if (message.getSugguestionsFontColor() == 0 && StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant()) && !message.isAiAgentReceiveMsgEnd()) {
                            //实时消息 如果是大模型消息里，同时消息还在接收，图片高度固定
                            imgHeight = 500;
                        }
                        View imageView = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_rich_image_view, sobot_rich_ll, false);
                        SobotProgressImageView image = imageView.findViewById(R.id.sobot_iv_picture);
                        if (!TextUtils.isEmpty(richListModel.getMsg())) {
                            image.setImageUrlWithScaleType(richListModel.getMsg(), ImageView.ScaleType.FIT_START);
                            image.setImageWidthAndHeight(msgMaxWidth, imgHeight);
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, imgHeight);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        image.setOnClickListener(new ImageClickLisenter(context, richListModel.getMsg(), isRight));
                        sobot_rich_ll.addView(imageView, layoutParams);
                        setLongClickListener(imageView);
                    } else if (richListModel.getType() == 3) {
                        View videoView = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_rich_vedio_view, sobot_rich_ll, false);
                        SobotProgressImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
                        if (!TextUtils.isEmpty(richListModel.getVideoImgUrl())) {
                            sobot_video_first_image.setImageUrl(richListModel.getVideoImgUrl());
                            sobot_video_first_image.setImageWidthAndHeight(msgMaxWidth, msgMaxWidth * 146 / 246);
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, msgMaxWidth * 146 / 246);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        sobot_rich_ll.addView(videoView, layoutParams);
                        videoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                String name = MD5Util.encode(richListModel.getMsg());
                                int dotIndex = richListModel.getMsg().lastIndexOf('.');
                                if (dotIndex == -1) {
                                    name = name + ".mp4";
                                } else {
                                    name = name + richListModel.getMsg().substring(dotIndex + 1);
                                }
                                cacheFile.setFileName(name);
                                cacheFile.setUrl(richListModel.getMsg());
                                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                cacheFile.setMsgId(message.getMsgId());
                                Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
                                mContext.startActivity(intent);
                            }
                        });
                        setLongClickListener(videoView);
                    } else if ((richListModel.getType() == 4 || richListModel.getType() == 2)) {
                        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_file_l, null);
                        TextView sobot_file_name = (TextView) view.findViewById(R.id.sobot_file_name);
                        TextView sobot_file_size = (TextView) view.findViewById(R.id.sobot_file_size);
                        SobotSectorProgressView sobot_progress = (SobotSectorProgressView) view.findViewById(R.id.sobot_progress);
                        sobot_file_name.setText(richListModel.getName());
                        sobot_file_size.setText(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                        SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg()))), sobot_progress);
                        float textWidth = sobot_file_name.getPaint().measureText(richListModel.getName());
                        int minWidth = ScreenUtils.dip2px(mContext, 240);
                        int maxWidth = ScreenUtils.dip2px(mContext, 400);
                        int constrainedWidth = Math.min(maxWidth, msgMaxWidth);
                        int finalWidth = Math.max(minWidth, Math.min((int) Math.ceil(textWidth), constrainedWidth));
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(finalWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        view.setLayoutParams(layoutParams);
                        sobot_rich_ll.addView(view);
                        setLongClickListener(view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (richListModel.getType() == 2) {
                                    Intent intent = new Intent(context, WebViewActivity.class);
                                    intent.putExtra("url", richListModel.getMsg());
                                    context.startActivity(intent);
                                } else {
                                    // 打开详情页面
                                    Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                                    SobotCacheFile cacheFile = new SobotCacheFile();
                                    cacheFile.setFileName(richListModel.getName());
                                    cacheFile.setFileSize(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                                    cacheFile.setUrl(richListModel.getMsg());
                                    cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                    cacheFile.setMsgId(message.getMsgId() + richListModel.getMsg());
                                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                    } else if ((richListModel.getType() == 5)) {
                        try {
                            //md 表格
                            WebView webview = new WebView(mContext);
                            int color = ContextCompat.getColor(mContext, R.color.sobot_color_bg_second);
                            String bgColorString = String.format("#%06X", (0xFFFFFF & color));
                            String htmlContent = "<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset='utf-8'>" + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" + "<style>" + "body { background: " + bgColorString + "; margin: 0; padding: 0px; overflow-x: scroll; }" +  // 添加 overflow-x: scroll
                                    "table { background: #f0f0f0; border: 1px solid #e6e6e6; " + "border-radius: 12px; border-collapse: separate; border-spacing: 0; " + "display: table; table-layout: auto; " + "margin: 0; overflow-x: scroll; overflow-y: hidden; }" + "th, td { min-width: 80px; max-width: 300px; padding: 10px 10px; font-size: 14px; " + "color: #161616; line-height: 22px; word-break: break-word; " + "border-width: 0 0 1px 1px; border-style: solid; border-color: #e6e6e6; " + "text-align: left; }" + "th { font-weight: 600; background-color: #f0f0f0; }" + "td { background-color: #fff; }" + "th:first-child, td:first-child { border-left-width: 0; }" + "tr:last-child td { border-bottom-width: 0; }" + "th:first-child { border-top-left-radius: 12px; }" + "th:last-child { border-top-right-radius: 12px; }" + "tr:last-child td:first-child { border-bottom-left-radius: 12px; }" + "tr:last-child td:last-child { border-bottom-right-radius: 12px; }" + "</style>" + "</head>" + "<body>" + richListModel.getMsg() + "</body>" + "</html>";
                            webview.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            if (i != 0) {
                                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                            }
                            webview.setLayoutParams(layoutParams);
                            sobot_rich_ll.addView(webview);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            sobot_rich_ll.setVisibility(View.VISIBLE);
            msg.setVisibility(View.GONE);
        } else {
            sobot_rich_ll.setVisibility(View.GONE);
            if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
                msg.setVisibility(View.VISIBLE);
                String robotAnswer = "";
                if ("9".equals(message.getAnswer().getMsgType())) {
                    if (message.getAnswer().getMultiDiaRespInfo() != null) {
                        robotAnswer = message.getAnswer().getMultiDiaRespInfo().getAnswer();
                    }

                } else {
                    robotAnswer = message.getAnswer().getMsg();
                }
                HtmlTools.getInstance(context).setRichText(msg, robotAnswer, getLinkTextColor());
            } else {
                msg.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 判断context 所属的activity是否销毁了
     *
     * @param context
     * @return
     */
    public boolean isActivityDestroyed(Context context) {
        if (context == null) {
            return true;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.isDestroyed();
        }

        // 如果context不是Activity实例，则认为它没有被销毁
        return false;
    }

    /**
     * 处理内容中的特殊标记，并替换成对应的sobotbutton标签
     *
     * @param content 原始内容
     * @return 替换后的字符串
     */
    public String processContentWithButtonTags(String content, ZhiChiMessageBase message) {
        if (content == null || StringUtils.isEmpty(content) || message == null) {
            return content;
        }
        try {
            SobotAiAnalysisInfo analysisInfo = message.getAnalysisInfo();
            List<SobotAiButtonInfo> buttonInfos = message.getButtonInfos();
            // 处理 @@#[数字]$$ 格式，从 fileContents 取值
            if (analysisInfo != null && analysisInfo.isShowKbSource() && analysisInfo.getFileContents() != null && !analysisInfo.getFileContents().isEmpty()) {
                content = processFromContentTags(content, analysisInfo.getFileContents());
            }
            // 处理 %%[数字]## 格式，从 buttonInfos 取值
            if (buttonInfos != null && !buttonInfos.isEmpty()) {
                content = processButtonContentTags(content, buttonInfos);
            }
            if (StringUtils.isNoEmpty(content)) {
                //不能显示特殊字符
                content = content.replaceAll("@@\\[\\d+(?:,\\d+)*\\]\\$\\$", "");
                content = content.replaceAll("%%\\[[^\\]]*\\]##", "");
            }
            return content;
        } catch (Exception ignored) {
        }
        return content;
    }

    /**
     * **标识符格式：** `@@[知识1,2]$$` `@@[1,2]$$` `@@[1]$$`
     * **索引规则：** 从 **1** 开始计数，支持逗号分隔的多个数字
     * - `@@[知识1,2]$$` 对应 `fileContents[0]` 和 `fileContents[1]`
     * - `@@[1,2]$$` 对应 `fileContents[0]` 和 `fileContents[1]`
     * - `@@[1]$$` 对应 `fileContents[0]`
     * - 以此类推。
     * 参考来源总数以答案中使用的来源为基础，去重后显示数量
     */
    public String processFromContentTags(String content, List<SobotAiLinkInfo> fileContents) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        if (fileContents == null || fileContents.isEmpty()) {
            if (StringUtils.isNoEmpty(content)) {
                //不能显示特殊字符
                content = content.replaceAll("@@\\[\\]\\$\\$", "");
            }
            return content;
        }
        try {
            // 先过滤 usedFlag 为 true  的数据
            List<SobotAiLinkInfo> filteredFileContents = new ArrayList<>();
            for (SobotAiLinkInfo linkInfo : fileContents) {
                if (linkInfo != null && linkInfo.isUsedFlag()) {
                    filteredFileContents.add(linkInfo);
                }
            }
            if (filteredFileContents.isEmpty()) {
                return content;
            }
            // 创建一个新的集合用于保存去重后的 SobotAiLinkInfo 对象(来源总数显示的是从这个有效集合里边获取的数量，弹窗也是)
            ArrayList<SobotAiLinkInfo> uniqueFileContents = new ArrayList<>();

            // 只处理相邻的 @@[...]$$ 标记，将它们合并成逗号分隔的形式
            Pattern mergePattern = Pattern.compile("\\]\\$\\$\\s*(@@|@)\\["); // 匹配 ]$$ @@[ 或 ]$$ @[ 格式
            String mergedContent = mergePattern.matcher(content).replaceAll(","); // 将相邻的标记中间替换为逗号

            // 现在处理所有 @@[...]$$ 标记
            Pattern pattern = Pattern.compile("(@@)\\[([^\\]]+)\\]\\$\\$");
            Matcher matcher = pattern.matcher(mergedContent);

            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String bracketContent = matcher.group(2); // 获取方括号内的所有内容
                LogUtils.d("获取方括号内的所有内容-----------" + bracketContent);

                // 为每个匹配项创建独立的已处理fileId集合
                Set<String> processedFileIdsForCurrentMatch = new HashSet<>();

                // 提取所有数字序列
                Pattern numberPattern = Pattern.compile("\\d+");
                Matcher numberMatcher = numberPattern.matcher(bracketContent);

                StringBuilder individualReplacement = new StringBuilder();

                while (numberMatcher.find()) {
                    String numberStr = numberMatcher.group();
                    try {
                        int number = Integer.parseInt(numberStr);

                        int index = number - 1; // 转换为0基索引（从0开始）
                        if (index >= 0 && index < filteredFileContents.size()) {
                            SobotAiLinkInfo linkInfo = filteredFileContents.get(index);
                            if (linkInfo != null) {
                                if ((linkInfo.getSectionTypeEnum() != null && ("WEB".equalsIgnoreCase(linkInfo.getSectionTypeEnum()) || "FILE".equalsIgnoreCase(linkInfo.getSectionTypeEnum())))) {
                                    String fileId = linkInfo.getFileId();
                                    // 检查fileId是否已经在当前匹配项中处理过（局部去重）
                                    if (!TextUtils.isEmpty(fileId) && !processedFileIdsForCurrentMatch.contains(fileId)) {
                                        String buttonTag = SobotAiLinkInfo.convertTosobotbuttonTag(linkInfo);
                                        individualReplacement.append(StringUtils.checkStringIsNull(buttonTag));
                                        // 添加到当前匹配项的已处理fileId集合中
                                        processedFileIdsForCurrentMatch.add(fileId);
                                        // 同时也添加到uniqueFileContents中（如果尚未添加）
                                        if (!containsFileId(uniqueFileContents, fileId)) {
                                            uniqueFileContents.add(linkInfo);
                                        }
                                    } else if (TextUtils.isEmpty(fileId)) {
                                        // 如果fileId为空，仍要处理但不加入去重集合
                                        String buttonTag = SobotAiLinkInfo.convertTosobotbuttonTag(linkInfo);
                                        individualReplacement.append(StringUtils.checkStringIsNull(buttonTag));
                                        // 仍然添加到uniqueFileContents中
                                        uniqueFileContents.add(linkInfo);
                                    }
                                } else {
                                    LogUtils.d("跳过了非WEB或FILE类型的链接: " + linkInfo.getSectionTypeEnum());
                                }
                            } else {
                                LogUtils.d("linkInfo为null，跳过索引: " + index);
                            }
                        } else {
                            LogUtils.d("索引超出范围: " + index + ", 集合大小: " + filteredFileContents.size());
                        }
                    } catch (NumberFormatException e) {
                        LogUtils.d("无法解析数字: " + numberStr);
                        // 忽略无效数字
                    }
                }

                // 将当前匹配项的替换内容添加到最终结果中
                matcher.appendReplacement(result, Matcher.quoteReplacement(individualReplacement.toString()));
            }
            matcher.appendTail(result);

            String processedContent = result.toString();

            if (message != null && (message.isAiAgentReceiveMsgEnd() || message.getSugguestionsFontColor() == 1)) {
                //如果大模型答案结束了 或者历史记录 显示大模型先对引用材料数量
                if (!uniqueFileContents.isEmpty() && ll_ai_reference_count != null) {
                    view_ai_reference_split.setVisibility(View.VISIBLE);
                    ll_ai_reference_count.setVisibility(View.VISIBLE);
                    String tempStr = mContext.getResources().getString(R.string.sobot_reference_materials_quantity);
                    if (tempStr.contains("%d")) {
                        tv_ai_reference_count.setText(String.format(tempStr, uniqueFileContents.size()));
                    } else {
                        tv_ai_reference_count.setText(tempStr); // 直接显示原文本
                    }
                    ll_ai_reference_count.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (FastClickUtils.isCanClick(1000)) {
                                Intent intent = new Intent(mContext, SobotAIFromListActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("aiLinkInfoList", uniqueFileContents);
                                intent.putExtras(bundle);
                                Activity activity = MyApplication.getInstance().getLastActivity();
                                if (activity != null) {
                                    activity.startActivity(intent);
                                }
                            }
                        }
                    });
                } else {
                    hideAIRefetenceUI();
                }
            }
            // 兜底 将 @@[]$$ 替换为空字符串
            processedContent = processedContent.replaceAll("@@\\[\\d+(?:,\\d+)*\\]\\$\\$", "");
            return processedContent;
        } catch (Exception e) {
            LogUtils.d("processFromContentTags异常: " + e.getMessage());
            return content;
        }
    }

    /**
     * 检查uniqueFileContents中是否已包含指定fileId
     */
    private boolean containsFileId(List<SobotAiLinkInfo> list, String fileId) {
        if (TextUtils.isEmpty(fileId) || list == null) {
            return false;
        }
        for (SobotAiLinkInfo item : list) {
            if (fileId.equals(item.getFileId())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 处理 %%[数字]## 格式的标记，从 buttonInfos 取值 ,值是索引 从0开始
     */
    private static String processButtonContentTags(String content, List<SobotAiButtonInfo> buttonInfos) {
        if (content == null || buttonInfos == null || buttonInfos.isEmpty()) {
            return content;
        }
        try {
            // 正则表达式匹配 %%[数字]## 模式
            Pattern pattern = Pattern.compile("%%\\[(\\d+)\\]##");
            Matcher matcher = pattern.matcher(content);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String indexStr = matcher.group(1); // 获取括号内的数字
                if (indexStr != null && StringUtils.isNoEmpty(indexStr)) {
                    try {
                        int index = Integer.parseInt(indexStr); // indexStr 是从0开始
                        // 确保索引在有效范围内
                        if (index >= 0 && index < buttonInfos.size()) {
                            SobotAiButtonInfo buttonInfo = buttonInfos.get(index);
                            if (buttonInfo != null) {
                                // 检查按钮类型，只对URL和TRANSFER类型生成按钮标签，其他类型（如MSG）替换为空字符串
                                if ("URL".equals(buttonInfo.getButtonAction()) || "TRANSFER".equals(buttonInfo.getButtonAction())) {
                                    // 将按钮信息转换为适当的HTML标签或按钮标记
                                    String buttonTag = SobotAiButtonInfo.convertTosobotbuttonTag(buttonInfo);
                                    // 替换匹配的部分为按钮标签
                                    matcher.appendReplacement(result, Matcher.quoteReplacement(buttonTag));
                                } else {
                                    // 如果不是这两种类型（如MSG），用空字符代替
                                    matcher.appendReplacement(result, "%%[]##");
                                }
                            } else {
                                // 如果按钮信息为null，用空字符代替
                                matcher.appendReplacement(result, "%%[]##");
                            }
                        } else {
                            // 如果索引超出范围，保留原始标记
                            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                        }
                    } catch (NumberFormatException e) {
                        // 如果无法解析数字，保留原始标记
                        matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
                    }
                }
            }
            matcher.appendTail(result);
            //兜底  使用正则表达式替换所有 %%[任意内容]## 格式的标记
            return result.toString().replaceAll("%%\\[[^\\]]*\\]##", "");
        } catch (Exception e) {
            return content;
        }
    }

    /**
     * 检测 message.getButtonInfos() 里边是否包含 buttonAction=MSG
     *
     * @param message ZhiChiMessageBase 消息对象
     * @return true: 包含 buttonAction=MSG 的按钮, false: 不包含
     */
    public static boolean hasButtonActionMsg(ZhiChiMessageBase message) {
        if (message == null || message.getButtonInfos() == null || message.getButtonInfos().isEmpty()) {
            return false;
        }

        for (SobotAiButtonInfo buttonInfo : message.getButtonInfos()) {
            if (buttonInfo != null && "MSG".equals(buttonInfo.getButtonAction())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断字符串是否满足 "%%[数字]##" 格式
     * 例如：%%[123]##、%%[0]##、%%[999999]##
     *
     * @param str 待检测的字符串
     * @return true 符合格式，false 不符合
     */
    public static boolean isMatchButtonContentTags(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.contains("%%[") && str.contains("]##"); // 简单判断
    }
}