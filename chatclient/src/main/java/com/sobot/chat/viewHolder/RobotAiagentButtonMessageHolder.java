package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

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

    private String[] inputContent; // 保存输入内容数组
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
            String msgStr = message.getContent();
            if (!TextUtils.isEmpty(msgStr)) {
                HtmlTools.getInstance(context).setRichText(tvMsg, msgStr, getLinkTextColor());
                tvMsg.setVisibility(View.VISIBLE);
            } else {
                tvMsg.setVisibility(View.GONE);
            }
            checkShowTransferBtn();

            // 保存输入内容数组
            this.inputContent = message.getVariableValueEnums();

            if (inputContent.length > 0) {
                Drawable moreDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_aiagent_button_more_bg, null);
                if (moreDrawable != null) {
                    moreLL.setBackground(ThemeUtils.applyColorToDrawable(moreDrawable, ThemeUtils.getThemeColor(mContext)));
                }
                Drawable arrowDownDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_notice_arrow_down, null);
                if (arrowDownDrawable != null) {
                    moreIV.setBackground(ThemeUtils.applyColorToDrawable(arrowDownDrawable, ThemeUtils.getThemeColor(mContext)));
                }
                moreTV.setTextColor(ThemeUtils.getThemeColor(mContext));

                // 重置状态
                alButton.removeAllViews();
                currentLoadedCount = 0;
                resetMaxWidth();
                if (message.isHideVariableValueEnums()) {
                    llButtonRoot.setVisibility(View.GONE);
                } else {
                    llButtonRoot.setVisibility(View.VISIBLE);
                }
                // 首次加载最多8个
                loadMoreItems();

                // 设置更多按钮点击事件
                moreLL.setOnClickListener(v -> loadMoreItems());
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
    private void loadMoreItems() {
        try {
            if (message == null || inputContent == null || currentLoadedCount >= inputContent.length) {
                if (moreLL != null) {
                    moreLL.setVisibility(View.GONE);
                }
                return;
            }
            if (msgCallBack != null) {
                msgCallBack.goToLastIndexItem();
            }
            int loadCount = Math.min(itemsPerLoad, inputContent.length - currentLoadedCount);

            for (int i = 0; i < loadCount; i++) {
                int index = currentLoadedCount + i;
                if (index < inputContent.length && alButton != null) {
                    String str = inputContent[index];
                    try {
                        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_aiagent_item_bottom_layout, null);
                        TextView nameTv = view.findViewById(R.id.tv_lable_name);
                        if (nameTv != null) {
                            nameTv.setTextColor(ThemeUtils.getThemeColor(mContext));
                            nameTv.setText(StringUtils.isEmpty(str) ? "" : str + "");
                            alButton.addView(view);
                        }
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (msgCallBack != null) {
                                    ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                                    msgObj.setNodeId(message.getNodeId());
                                    msgObj.setProcessId(message.getProcessId());
                                    msgObj.setVariableId(message.getVariableId());
                                    msgObj.setContent(StringUtils.checkStringIsNull(str));
                                    msgCallBack.sendMessageToRobot(msgObj, 6, 1, "");
                                    //隐藏按钮
                                    message.setHideVariableValueEnums(true);
                                    llButtonRoot.setVisibility(View.GONE);
                                }
                            }
                        });
                    } catch (Exception e) {
                        // 处理 inflate 或 findViewById 异常
                        e.printStackTrace();
                    }
                }
            }

            currentLoadedCount += loadCount;

            // 检查是否还有更多数据
            if (moreLL != null && currentLoadedCount >= inputContent.length) {
                moreLL.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            if (moreLL != null) {
                moreLL.setVisibility(View.GONE);
            }
        }
    }

}