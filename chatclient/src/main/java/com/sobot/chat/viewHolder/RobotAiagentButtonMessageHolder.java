package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotRightAlignLineLayout;

//大模型机器人 按钮卡片消息
public class RobotAiagentButtonMessageHolder extends MsgHolderBase {
    private TextView tvMsg;// 聊天提示语，显示在气泡里边
    public ZhiChiMessageBase message;
    private Context mContext;
    private LinearLayout llButtonRoot;//按钮列表根节点
    private SobotRightAlignLineLayout alButton;//按钮列表
    private LinearLayout moreLL;//更多
    private TextView moreTV;
    private ImageView moreIV;

    private String[] inputContentList; // 保存输入内容数组
    private int currentLoadedCount = 0; // 当前已加载的数量
    private int itemsPerLoad = 8; // 每次加载的数量


    public RobotAiagentButtonMessageHolder(Context context, View convertView) {
        super(context, convertView);
        tvMsg = convertView.findViewById(R.id.sobot_template2_msg);
        alButton = convertView.findViewById(R.id.al_button);
        moreTV = convertView.findViewById(R.id.tv_more);
        moreLL = convertView.findViewById(R.id.ll_more);
        moreIV = convertView.findViewById(R.id.iv_more);
        llButtonRoot = convertView.findViewById(R.id.ll_button_root);
        this.mContext = context;
    }

    @Override
    public void bindData(final Context context, ZhiChiMessageBase message) {
        this.message = message;
        if (message != null && message.getVariableValueEnums() != null) {
            // 设置plAiButton的颜色为主题色
            if (moreLL != null) {
                Drawable moreDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_aiagent_button_more_bg, null);
                if (moreDrawable != null) {
                    moreLL.setBackground(moreDrawable);
                    moreLL.setElevation(12f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        moreLL.setOutlineSpotShadowColor(ContextCompat.getColor(context, R.color.sobot_color_shado));
                    }
                    moreLL.setTranslationY(6f);
                }

            }
            String msgStr = message.getContent();
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tvMsg, msgStr, getLinkTextColor());
                tvMsg.setVisibility(View.VISIBLE);
            } else {
                tvMsg.setVisibility(View.GONE);
            }
            checkShowTransferBtn();

            // 保存输入内容数组
            this.inputContentList = message.getVariableValueEnums();

            if (inputContentList.length > 0) {
                if (inputContentList.length > itemsPerLoad) {
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
                // 重置状态
                alButton.removeAllViews();
                currentLoadedCount = 0;
                resetMaxWidth();
                // 首次加载最多8个
                loadMoreItems(false);
                if (message.isHideVariableValueEnums()) {
                    llButtonRoot.setVisibility(View.GONE);
                } else {
                    llButtonRoot.setVisibility(View.VISIBLE);
                }
            } else {
                alButton.setVisibility(View.GONE);
                moreLL.setVisibility(View.GONE);
            }
        }

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

            // 计算需要加载的起始位置和数量
            int startLoadIndex;
            int loadCount;

            if (isClickMore) {
                // 点击"更多"按钮：从当前已加载位置继续加载itemsPerLoad个
                startLoadIndex = currentLoadedCount;
                loadCount = Math.min(itemsPerLoad, inputContentList.length - currentLoadedCount);
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
                loadCount = Math.min(itemsPerLoad, inputContentList.length);
                currentLoadedCount = loadCount;
                message.setLoadedButtonCount(currentLoadedCount);
                // 第一次加载时清理之前的视图
                alButton.removeAllViews();
            }

            // 加载指定范围的项
            for (int i = startLoadIndex; i < startLoadIndex + loadCount; i++) {
                if (i < inputContentList.length && alButton != null) {
                    String str = StringUtils.checkStringIsNull(inputContentList[i]);
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
                                        msgCallBack.sendMessageToRobot(msgObj, 0, 0, "");
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
                if (currentLoadedCount >= inputContentList.length) {
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

}