package com.sobot.chat.viewHolder.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotAiRobotRealuateConfigInfo;
import com.sobot.chat.api.model.Suggestions;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.ReSendDialog;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotMaxSizeLinearLayout;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.toast.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * view基类
 */
public abstract class MsgHolderBase extends RecyclerView.ViewHolder {

    public Context mContext;
    //左侧右侧气泡 标识 默认false左侧
    public boolean isRight = false;
    //消息cell 根节点 （1分钟相邻两条消息是同一个人发的，调整间距变小）
    public View sobot_real_ll_content;
    //气泡父控件
    public View mItemView;
    // 用户姓名
    public TextView nameTv;
    // 头像
    public SobotProgressImageView headIV;
    //时间提醒
    public TextView reminde_time_Text;

    //左侧 转人工、顶踩控件
    public SobotAntoLineLayout sobot_chat_more_action;//包含以下所有控件
    private final LinearLayout sobot_ll_transferBtn;//转人工按钮
    public TextView sobot_tv_transferBtn;//机器人转人工按钮
    public ImageView sobot_right_likebtn_iv;//机器人评价 顶 的按钮
    public ImageView sobot_right_dislikegtn_iv;//机器人评价 踩 的按钮
    public RelativeLayout rightEmptyRL;//左侧消息右边的空白区域
    //底部顶踩显示
    protected ImageView sobot_iv_bottom_likeBtn;//气泡下边 机器人评价 顶 的按钮 图标
    protected ImageView sobot_iv_bottom_dislikeBtn;//气泡下边 机器人评价 踩 的按钮 图标
    public TextView stripe;//关联问题提示语
    public LinearLayout answersList;//关联问题

    public LinearLayout sobot_msg_ll;//左侧消息布局
    public LinearLayout ll_status;// 右侧消息布局
    public View sobot_msg_content_ll;//气泡内容显示区

    //右侧 发送消息状态控件 发送中（菊花转）、发送失败（红色叹号，点击重新发送）
    public ImageView msgStatus;// 消息发送的状态
    public ImageView msgReadStatus;// 消息已读未读状态
    public ProgressBar msgProgressBar; // 重新发送的进度条的信信息；

    //消息体
    public ZhiChiMessageBase message;
    public ZhiChiInitModeBase initMode;
    public Information information;
    //大模型机器人顶踩配置
    public SobotAiRobotRealuateConfigInfo aiRobotRealuateConfigInfo;
    //回调事件
    public SobotMsgAdapter.SobotMsgCallBack msgCallBack;

    public RelativeLayout sobot_rl_hollow_container;//文件类型的气泡

    public int msgMaxWidth;//气泡里边的内容最大宽度

    //接口返回左侧消息是否显示头像，默认显示
    private boolean isShowFace = true;
    private boolean isShowNickName = true;

    //用户设置的右侧消息是否显示头像昵称,默认不显示
    private boolean isShowRightFace = false;
    private boolean isShowRightNickName = false;

    //气泡里边卡片宽度，默认288
    public int msgCardWidth = 288;

    public MsgHolderBase(Context context, View convertView) {
        super(convertView);
        mItemView = convertView;
        mContext = context;
        initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_initModel);
        aiRobotRealuateConfigInfo = (SobotAiRobotRealuateConfigInfo) SharedPreferencesUtil.getObject(mContext,
                ZhiChiConstant.sobot_last_current_airobotrealuateconfiginfo);
        information = (Information) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_info);
        reminde_time_Text = convertView.findViewById(R.id.sobot_reminde_time_Text);
        sobot_real_ll_content = convertView.findViewById(R.id.sobot_real_ll_content);
        headIV = convertView.findViewById(R.id.sobot_msg_face_iv);
        if (headIV != null) {
            float width = mContext.getResources().getDimension(R.dimen.sobot_msg_face_width_heigth);
            headIV.setImageWidthAndHeight(Math.round(width), Math.round(width));
        }
        nameTv = convertView.findViewById(R.id.sobot_msg_nike_name_tv);

        sobot_msg_ll = convertView.findViewById(R.id.sobot_msg_ll);
        sobot_chat_more_action = convertView.findViewById(R.id.sobot_chat_more_action);
        sobot_ll_transferBtn = convertView.findViewById(R.id.sobot_ll_transferBtn);
        sobot_tv_transferBtn = convertView.findViewById(R.id.sobot_tv_transferBtn);
        rightEmptyRL = (RelativeLayout) convertView.findViewById(R.id.sobot_left_msg_right_empty_rl);
        sobot_right_likebtn_iv = convertView.findViewById(R.id.sobot_right_likebtn_iv);
        sobot_right_dislikegtn_iv = convertView.findViewById(R.id.sobot_right_dislikegtn_iv);

        sobot_iv_bottom_likeBtn = convertView.findViewById(R.id.sobot_iv_bottom_likeBtn);
        sobot_iv_bottom_dislikeBtn = convertView.findViewById(R.id.sobot_iv_bottom_dislikeBtn);

        stripe = convertView
                .findViewById(R.id.sobot_stripe);
        answersList = convertView
                .findViewById(R.id.sobot_answersList);

        msgProgressBar = convertView.findViewById(R.id.sobot_msgProgressBar);// 重新发送的进度条信息
        // 消息的状态
        msgStatus = convertView.findViewById(R.id.sobot_msgStatus);
        msgReadStatus = convertView.findViewById(R.id.sobot_msg_read_status);
        ll_status = convertView.findViewById(R.id.ll_status);

        sobot_msg_content_ll = convertView.findViewById(R.id.sobot_msg_content_ll);

        sobot_rl_hollow_container = convertView.findViewById(R.id.sobot_rl_hollow_container);
    }

    public abstract void bindData(Context context, final ZhiChiMessageBase message);

    /**
     * 发送中隐藏已读未读，用于文件、视频发送
     */
    public void goneReadStatus() {
        //不显示已读未读
        if (msgReadStatus != null) {
            msgReadStatus.setVisibility(View.GONE);
        }
    }

    /**
     * 设置已读未读
     */
    public void refreshReadStatus() {
        if (message != null && message.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS && message.getReadStatus() > 0) {
            if (msgReadStatus != null) {
                //0-未标记，1-未读，2-已读
                if (message.getReadStatus() == 1) {
                    msgReadStatus.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_icon_no_read, null));
                    msgReadStatus.setVisibility(View.VISIBLE);
                } else if (message.getReadStatus() == 2) {
                    msgReadStatus.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_icon_already_read, null));
                    msgReadStatus.setVisibility(View.VISIBLE);
                } else {
                    msgReadStatus.setVisibility(View.GONE);
                }
            } else {
                if (ll_status != null) {
                    ll_status.setGravity(Gravity.CENTER_VERTICAL);
                }
            }
        } else {
            if (ll_status != null) {
                ll_status.setGravity(Gravity.CENTER_VERTICAL);
            }
            //不显示已读未读
            if (msgReadStatus != null) {
                msgReadStatus.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置客服客户的头像和昵称
     */
    public void initNameAndFace(int itemType) {
        try {
            int msgFaceWidth = (int) mContext.getResources().getDimension(R.dimen.sobot_msg_face_width_heigth);//气泡头像大小
            int msgFaceMarginEnd = (int) mContext.getResources().getDimension(R.dimen.sobot_chat_msg_nick_bubble_margin_edge);//气泡头像间距
            int msgPaddingStartRight = (int) mContext.getResources().getDimension(R.dimen.sobot_msg_left_right_padding_edge);//气泡内间距
            int msgEdgeStartRight = (int) mContext.getResources().getDimension(R.dimen.sobot_msg_margin_edge);//气泡到边沿的间距
            int msgRightEmptyWidth = (int) mContext.getResources().getDimension(R.dimen.sobot_chat_msg_boundary_to_empty_width);//气泡右侧空白宽度

            //屏幕宽度 - 气泡边界到屏幕边上的空白宽度60-气泡内间距16*2-气泡外间距20*2 — 头像大小16 -头像到气泡间距 8
            msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) - msgRightEmptyWidth - msgPaddingStartRight * 2 - msgEdgeStartRight - msgFaceWidth - msgFaceMarginEnd;
            if (headIV == null || nameTv == null || mContext == null) {
                //头像昵称控件不能为空
                return;
            }
            applyCustomHeadUI();
            if (!isRight() && sobot_tv_transferBtn != null) {
                sobot_tv_transferBtn.setText(R.string.sobot_transfer_to_customer_service);
            }
            if (initMode != null && initMode.getVisitorScheme() != null) {
                if (headIV != null) {
                    if (initMode.getVisitorScheme().getShowFace() == 1) {
                        isShowFace = true;
                        //显示头像
                        headIV.setVisibility(View.VISIBLE);
                    } else {
                        isShowFace = false;
                        //隐藏头像
                        headIV.setVisibility(View.GONE);
                    }
                }
                if (nameTv != null) {
                    if (initMode.getVisitorScheme().getShowStaffNick() == 1) {
                        isShowNickName = true;
                        //显示昵称
                        nameTv.setVisibility(View.VISIBLE);
                    } else {
                        isShowNickName = false;
                        //隐藏昵称
                        nameTv.setVisibility(View.GONE);
                    }
                }
            } else {
                if (headIV != null) {
                    isShowFace = true;
                    //显示头像
                    headIV.setVisibility(View.VISIBLE);
                }
                if (nameTv != null) {
                    isShowNickName = true;
                    //显示昵称
                    nameTv.setVisibility(View.VISIBLE);
                }
            }
            if (isRight()) {
                //设置右侧头像昵称是否显示
                if (information != null) {
                    isShowRightNickName = information.isShowRightMsgNickName();
                    isShowRightFace = information.isShowRightMsgFace();
                    //右侧头像、昵称布局(只有头像显示时，左右布局；默认上下布局)（布局是按照左侧消息头像昵称显示决定的）
                    if (ll_status != null) {
                        if (isShowFace) {
                            if (!isShowNickName) {
                                //头像显示，昵称不显示
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ll_status.getLayoutParams();
                                layoutParams.addRule(RelativeLayout.START_OF, headIV.getId());
                                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                                ll_status.setLayoutParams(layoutParams);
                            } else {
                                //头像显示，昵称显示
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ll_status.getLayoutParams();
                                layoutParams.addRule(RelativeLayout.BELOW, nameTv.getId());
                                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                                ll_status.setLayoutParams(layoutParams);
                            }
                        } else {
                            //不显示头像,昵称显示
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ll_status.getLayoutParams();
                            layoutParams.addRule(RelativeLayout.BELOW, nameTv.getId());
                            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                            ll_status.setLayoutParams(layoutParams);
                        }
                    }
                    if (isShowRightNickName) {
                        nameTv.setVisibility(View.VISIBLE);
                        if (message != null && nameTv != null) {
                            if (!TextUtils.isEmpty(message.getSenderName())) {
                                nameTv.setText(message.getSenderName().trim());
                            }
                        }
                    } else {
                        nameTv.setVisibility(View.GONE);
                    }
                    if (isShowRightFace) {
                        headIV.setVisibility(View.VISIBLE);
                        if (message != null && headIV != null) {
                            if (ZhiChiConstant.ALLOCATED_FACE.equals(message.getSenderFace())) {
                                Drawable afaceDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_face, null);
                                if (afaceDrawable != null) {
                                    headIV.setImageDrawable(afaceDrawable);
                                }
                            } else {
                                headIV.setImageUrl(CommonUtils.encode(message.getSenderFace()));
                            }
                        }
                    } else {
                        headIV.setVisibility(View.GONE);
                    }
                }
            } else {
                //左侧头像、昵称布局(只有头像显示时，左右布局；默认上下布局)
                if (sobot_msg_ll != null) {
                    if (isShowFace) {
                        if (!isShowNickName) {
                            //头像显示，昵称不显示
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) sobot_msg_ll.getLayoutParams();
                            layoutParams.addRule(RelativeLayout.END_OF, headIV.getId());
                            sobot_msg_ll.setLayoutParams(layoutParams);
                        } else {
                            //头像显示，昵称显示
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) sobot_msg_ll.getLayoutParams();
                            layoutParams.addRule(RelativeLayout.BELOW, nameTv.getId());
                            sobot_msg_ll.setLayoutParams(layoutParams);
                        }
                    } else {
                        //不显示头像,昵称显示
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) sobot_msg_ll.getLayoutParams();
                        layoutParams.addRule(RelativeLayout.BELOW, nameTv.getId());
                        sobot_msg_ll.setLayoutParams(layoutParams);
                    }
                }
            }
            if (isRight()) {
                if (information != null && information.isShowRightMsgFace() && !information.isShowRightMsgNickName()) {
                    //只带有客服头像 左右布局
                    //屏幕宽度 - 气泡边界到屏幕边上的空白宽度60-气泡内间距16*2-气泡外间距20*2 — 头像大小16 -头像到气泡间距 8
                    msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) - msgRightEmptyWidth - msgPaddingStartRight * 2 - msgEdgeStartRight - msgFaceWidth - msgFaceMarginEnd;
                    msgCardWidth = ScreenUtils.dip2px(mContext, 288 - 40);
                } else {
                    //不带客服头像和昵称 或者都带
                    //屏幕宽度 - 气泡边界到屏幕边上的空白宽度60-气泡内间距16*2-气泡外间距20*2
                    msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) - msgRightEmptyWidth - msgPaddingStartRight * 2 - msgEdgeStartRight;
                    msgCardWidth = ScreenUtils.dip2px(mContext, 288);
                }
            } else {
                if (isShowFace && !isShowNickName) {
                    //只带有客服头像 左右布局
                    //屏幕宽度 - 气泡边界到屏幕边上的空白宽度60-气泡内间距16*2-气泡外间距20*2 — 头像大小16 -头像到气泡间距 8
                    msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) - msgRightEmptyWidth - msgPaddingStartRight * 2 - msgEdgeStartRight - msgFaceWidth - msgFaceMarginEnd;
                    msgCardWidth = ScreenUtils.dip2px(mContext, 288 - 40);
                } else {
                    //不带客服头像和昵称 或者都带
                    //屏幕宽度 - 气泡边界到屏幕边上的空白宽度60-气泡内间距16*2-气泡外间距20*2
                    msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) - msgRightEmptyWidth - msgPaddingStartRight * 2 - msgEdgeStartRight;
                    msgCardWidth = ScreenUtils.dip2px(mContext, 288);
                }
            }
            nameTv.setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 36));
            float msgSmallRadius = mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius); // 小圆角
            float msgBigRadius = mContext.getResources().getDimension(R.dimen.sobot_msg_big_corner_radius); // 大圆角
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.IS_CLOSE_SYSTEMRTL)) {
                //禁止镜像 四个圆角不变
            } else {
                if (ChatUtils.isRtl(mContext)) {
                    //是阿语，圆角需要镜像
                    msgSmallRadius = mContext.getResources().getDimension(R.dimen.sobot_msg_big_corner_radius); // 小圆角
                    msgBigRadius = mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius); // 大圆角
                }
            }
            if (isRight()) {
                boolean isTextAudioMsgType = (itemType == SobotMsgAdapter.MSG_TYPE_TXT_R || itemType == SobotMsgAdapter.MSG_TYPE_APPOINT_R || itemType == SobotMsgAdapter.MSG_TYPE_AUDIO_R || itemType == SobotMsgAdapter.MSG_TYPE_MULTI_ROUND_R);
                //右侧文本、语音气泡=渐变主题色背景
                //图片视频=无边框气泡背景 不处理
                //文件、商品卡片、订单卡片、文件类型其它的气泡=线框气泡 不处理
                if (mContext.getResources().getColor(R.color.sobot_gradient_end) == mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_end)) {
                    if (initMode != null && initMode.getVisitorScheme() != null) {
                        //服务端返回的导航条背景颜色
                        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
                            String themeColorStr = initMode.getVisitorScheme().getRebotTheme();
                            if (!themeColorStr.contains(",")) {
                                //单色 需要变成两个一样
                                themeColorStr = themeColorStr + "," + themeColorStr;
                            }
                            String themeColor[] = themeColorStr.split(",");
                            if (themeColor.length > 1) {

                                int[] colors = new int[themeColor.length];
                                for (int i = 0; i < themeColor.length; i++) {
                                    colors[i] = Color.parseColor(themeColor[i]);
                                }
                                if (isTextAudioMsgType) {
                                    float[] cornerRadii = null;
                                    //修改文本语音的圆角弧度 连续消息 右侧圆角弧度需要调整
                                    if (message.isShowFaceAndNickname()) {
                                        if (message.isNextIsShowFaceAndNickname()) {
                                            //本条显示头像昵称，下条也显示 圆角弧度正常 只有右上角是4dp
                                            cornerRadii = new float[]{
                                                    msgBigRadius, msgBigRadius, // 左上角
                                                    msgSmallRadius, msgSmallRadius, // 右上角
                                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), // 右下角
                                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius)    // 左下角
                                            };
                                        } else {
                                            //本条显示头像昵称，下条不显示 左侧圆角弧度正常，右侧弧度都是小的 4dp
                                            cornerRadii = new float[]{
                                                    msgBigRadius, msgBigRadius, // 左上角
                                                    msgSmallRadius, msgSmallRadius, // 右上角
                                                    msgSmallRadius, msgSmallRadius, // 右下角
                                                    msgBigRadius, msgBigRadius    // 左下角
                                            };
                                        }
                                    } else {
                                        if (message.isNextIsShowFaceAndNickname()) {
                                            //本条不显示头像昵称，下条也显示 相当于连续消息结束 只有右上角是4dp
                                            cornerRadii = new float[]{
                                                    msgBigRadius, msgBigRadius, // 左上角
                                                    msgSmallRadius, msgSmallRadius, // 右上角
                                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), // 右下角
                                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius)    // 左下角
                                            };
                                        } else {
                                            //本条不显示头像昵称，下条也不显示 还是连续消息，右侧弧度都是小的 4dp
                                            cornerRadii = new float[]{
                                                    msgBigRadius, msgBigRadius, // 左上角
                                                    msgSmallRadius, msgSmallRadius, // 右上角
                                                    msgSmallRadius, msgSmallRadius, // 右下角
                                                    msgBigRadius, msgBigRadius    // 左下角
                                            };
                                        }
                                    }
                                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                                    aDrawable.setCornerRadii(cornerRadii);
                                    if (sobot_msg_content_ll != null) {
                                        sobot_msg_content_ll.setBackground(aDrawable);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                //是否是文本 富文本消息
                boolean isTextRichMsgType = (itemType == SobotMsgAdapter.MSG_TYPE_TXT_L || itemType == SobotMsgAdapter.MSG_TYPE_RICH);
                if (isTextRichMsgType) {
                    int[] colors = new int[]{mContext.getResources().getColor(R.color.sobot_chat_left_bgColor), mContext.getResources().getColor(R.color.sobot_chat_left_bgColor)};
                    float[] cornerRadii = null;
                    //修改文本语音的圆角弧度 连续消息 左侧圆角弧度需要调整
                    if (message.isShowFaceAndNickname()) {
                        if (message.isNextIsShowFaceAndNickname()) {
                            //本条显示头像昵称，下条也显示 圆角弧度正常
                            //文本语音气泡 左上角是小角度 4dp
                            cornerRadii = new float[]{
                                    msgSmallRadius, msgSmallRadius, // 左上角
                                    msgBigRadius, msgBigRadius, // 右上角
                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), // 右下角
                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius)    // 左下角
                            };
                        } else {
                            //本条显示头像昵称，下条不显示 左侧圆角弧度都是小的 4dp
                            cornerRadii = new float[]{
                                    msgSmallRadius, msgSmallRadius, // 左上角
                                    msgBigRadius, msgBigRadius, // 右上角
                                    msgBigRadius, msgBigRadius, // 右下角
                                    msgSmallRadius, msgSmallRadius    // 左下角
                            };
                        }
                    } else {
                        if (message.isNextIsShowFaceAndNickname()) {
                            //本条不显示头像昵称，下条也显示 相当于连续消息结束,只有左上角弧度是4dp
                            cornerRadii = new float[]{
                                    msgSmallRadius, msgSmallRadius, // 左上角
                                    msgBigRadius, msgBigRadius, // 右上角
                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), // 右下角
                                    mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius), mContext.getResources().getDimension(R.dimen.sobot_msg_def_corner_radius)    // 左下角
                            };
                        } else {
                            //本条不显示头像昵称，下条也不显示 还是连续消息，左侧圆角弧度都需要调小
                            cornerRadii = new float[]{
                                    msgSmallRadius, msgSmallRadius, // 左上角
                                    msgBigRadius, msgBigRadius, // 右上角
                                    msgBigRadius, msgBigRadius, // 右下角
                                    msgSmallRadius, msgSmallRadius    // 左下角
                            };
                        }
                    }
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    aDrawable.setCornerRadii(cornerRadii);
                    if (sobot_msg_content_ll != null) {
                        sobot_msg_content_ll.setBackground(aDrawable);
                    }
                }
            }
            if (message != null) {
                if (nameTv != null) {
                    if (StringUtils.isNoEmpty(message.getSenderName())) {
                        nameTv.setText(message.getSenderName().trim());
                    }
                    if (isRight()) {
                        if (isShowRightNickName) {
                            // 相邻两条消息同一个人，1分钟内不显示
                            nameTv.setVisibility(message.isShowFaceAndNickname() ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        if (isShowNickName) {
                            // 相邻两条消息同一个人，1分钟内不显示
                            nameTv.setVisibility(message.isShowFaceAndNickname() ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                if (headIV != null) {
                    if (StringUtils.isNoEmpty(message.getSenderFace()) && ChatUtils.isDefaultFace(message.getSenderFace())) {
                        //默认头像
                        try {
                            Drawable afaceDrawable = ThemeUtils.createTextImageDrawable(mContext, message.getSenderName(), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_face_width_heigth), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_face_width_heigth), ThemeUtils.getThemeColor(mContext), ThemeUtils.getThemeTextAndIconColor(mContext));
                            if (afaceDrawable != null) {
                                headIV.setImageDrawable(afaceDrawable);
                            } else {
                                headIV.setImageUrl(CommonUtils.encode(message.getSenderFace()));
                            }
                        } catch (Resources.NotFoundException e) {
                        }
                    } else {
                        if (StringUtils.isNoEmpty(message.getSenderFace())) {
                            if (ZhiChiConstant.ALLOCATED_FACE.equals(message.getSenderFace())) {
                                Drawable afaceDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_face, null);
                                if (afaceDrawable != null) {
                                    headIV.setImageDrawable(afaceDrawable);
                                }
                            } else {
                                headIV.setImageUrl(CommonUtils.encode(message.getSenderFace()));
                            }
                        }
                    }
                    if (isRight()) {
                        if (isShowRightFace) {
                            // 相邻两条消息同一个人，1分钟内不显示
                            headIV.setVisibility(message.isShowFaceAndNickname() ? View.VISIBLE : View.INVISIBLE);
                        }
                    } else {
                        if (isShowFace) {
                            // 相邻两条消息同一个人，1分钟内不显示
                            headIV.setVisibility(message.isShowFaceAndNickname() ? View.VISIBLE : View.INVISIBLE);
                        }
                    }
                }
                if (sobot_real_ll_content != null) {
                    boolean isCard = ChatUtils.isMsgCard(itemType);
                    //修改消息之间的间距
                    if (message.isNextIsShowFaceAndNickname()) {
                        //下条显示头像昵称 消息之间间距是默认的20dp
                        if (isCard) {
                            //本条是卡片类型
                            sobot_real_ll_content.setPadding(0, 0, 0, ScreenUtils.dip2px(mContext, 4));
                        } else {
                            if (message.isNextIsCard()) {
                                //下条是卡片类型，本条不是卡片
                                sobot_real_ll_content.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_margin) - ScreenUtils.dip2px(mContext, 10));
                            } else {
                                //相邻两条都不是卡片类型
                                sobot_real_ll_content.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_margin));
                            }
                        }
                    } else {
                        if (message.isNextIsCard()) {
                            //下条是卡片消息，不设置下间距，因为有阴影
//                            LogUtils.d("下条是卡片消息，不设置下间距，因为有阴影");
                            sobot_real_ll_content.setPadding(0, 0, 0, 0);
                        } else {
                            //下条不显示头像昵称 消息之间间距是小的4dp
                            sobot_real_ll_content.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.sobot_msg_top_bottom_small_margin));
                        }
                    }
                    if (isCard && isShowFace && !isShowNickName) {
                        //本条是阴影卡片类消息 显示头像 不显示昵称
                        if (headIV.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                            RelativeLayout.LayoutParams imageLp = (RelativeLayout.LayoutParams) headIV.getLayoutParams();
                            imageLp.setMargins(imageLp.getMarginStart(), ScreenUtils.dip2px(mContext, 10), imageLp.getMarginEnd(), 0);
                        }
                    }
                }
            }
            if (information != null && information.isShowEveryLeftMsgFaceNickName()) {
                //永远显示
                nameTv.setVisibility(View.VISIBLE);
                headIV.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
        resetMaxWidth();
    }


    //左右两边气泡内链接文字的字体颜色
    protected int getLinkTextColor() {
        if (isRight()) {
            if (mContext.getResources().getColor(R.color.sobot_color_rlink) == mContext.getResources().getColor(R.color.sobot_common_blue)) {
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //服务端返回的气泡中超链接背景颜色
                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                        return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                    }
                }
                return R.color.sobot_color_rlink;
            } else {
                return R.color.sobot_color_rlink;
            }
        } else {
            if (mContext.getResources().getColor(R.color.sobot_color_link) == mContext.getResources().getColor(R.color.sobot_common_blue)) {
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //服务端返回的气泡中超链接背景颜色
                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                        return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                    }
                }
                return R.color.sobot_color_link;
            } else {
                return R.color.sobot_color_link;
            }
        }
    }

    //中间提醒消息中超链接文字的字体颜色
    protected int getRemindLinkTextColor() {
        if (mContext.getResources().getColor(R.color.sobot_color_link_remind) == mContext.getResources().getColor(R.color.sobot_common_green)) {
            if (initMode != null && initMode.getVisitorScheme() != null) {
                //服务端返回的气泡中超链接背景颜色
                if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                    return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                }
            }
            return R.color.sobot_color_link_remind;
        } else {
            return R.color.sobot_color_link_remind;
        }
    }

    public boolean isRight() {
        return isRight;
    }

    public void setRight(boolean right) {
        isRight = right;
    }

    public void setMsgCallBack(SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        this.msgCallBack = msgCallBack;
    }

    /**
     * 显示重新发送dialog
     */
    public static void showReSendDialog(Context context, ImageView msgStatus, ReSendListener reSendListener) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int widths = 0;
        if (width == 480) {
            widths = 80;
        } else {
            widths = 120;
        }
        final ReSendDialog reSendDialog = new ReSendDialog(context);
        reSendDialog.setOnClickListener(new ReSendDialog.OnItemClick() {
            @Override
            public void OnClick(int type) {
                if (type == 0) {// 0：确定 1：取消
                    reSendListener.onReSend();
                }
                reSendDialog.dismiss();
            }
        });
        reSendDialog.show();
        msgStatus.setClickable(true);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        if (reSendDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = reSendDialog.getWindow().getAttributes();
            lp.width = (int) (display.getWidth() - widths); // 设置宽度
            reSendDialog.getWindow().setAttributes(lp);
        }
    }

    public interface ReSendListener {
        void onReSend();
    }

    // 图片的事件监听
    public static class ImageClickLisenter implements View.OnClickListener {
        private Context context;
        private String imageUrl;
        private boolean isRight;

        public ImageClickLisenter(Context context, String imageUrl) {
            super();
            this.imageUrl = imageUrl;
            this.context = context;
        }

        // isRight: 我发送的图片显示时，gif当一般图片处理
        public ImageClickLisenter(Context context, String imageUrl, boolean isRight) {
            this(context, imageUrl);
            this.isRight = isRight;
        }

        @Override
        public void onClick(View arg0) {
            if (TextUtils.isEmpty(imageUrl)) {
                ToastUtil.showToast(context, context.getResources().getString(R.string.sobot_pic_type_error));
                return;
            }
            if (SobotOption.imagePreviewListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(context, imageUrl);
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(context, SobotPhotoActivity.class);
            intent.putExtra("imageUrL", imageUrl);
            if (isRight) {
                intent.putExtra("isRight", isRight);
            }
            context.startActivity(intent);
        }
    }

    public void bindZhiChiMessageBase(ZhiChiMessageBase zhiChiMessageBase) {
        this.message = zhiChiMessageBase;
    }


    /**
     * 设置头像UI
     */
    private void applyCustomHeadUI() {
        if (headIV != null) {
//            imgHead.setCornerRadius(4);
            headIV.setRoundAsCircle(true);
        }
    }

    public String processPrefix(final ZhiChiMessageBase message, int num) {
        if (message != null && message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null
                && message.getAnswer().getMultiDiaRespInfo().getIcLists() != null) {
            return "•";
        }
        return num + ". ";
    }

    // ------ 左侧公共方法 顶踩 转人工 气泡里边控件最大宽度 ------

    //转人工 显示 逻辑
    public void checkShowTransferBtn() {
        if (message == null) {
            return;
        }
        if (isRight()) {
            return;
        }
        if (message.getTransferType() == 4) {
            //4 多次命中 显示转人工
            showTransferBtn();
        } else {
            if (message.isShowTransferBtn()) {
                showTransferBtn();
            } else {
                hideTransferBtn();
            }
        }
    }

    public void hideContainer() {
        if (!message.isShowTransferBtn()) {
            sobot_ll_transferBtn.setVisibility(View.GONE);
        } else {
            sobot_ll_transferBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏转人工按钮
     */
    public void hideTransferBtn() {
        hideContainer();
        sobot_ll_transferBtn.setVisibility(View.GONE);
        sobot_tv_transferBtn.setVisibility(View.GONE);
        if (message != null) {
            message.setShowTransferBtn(false);
        }
    }

    /**
     * 显示转人工按钮
     */
    public void showTransferBtn() {
        sobot_chat_more_action.setVisibility(View.VISIBLE);
        sobot_ll_transferBtn.setVisibility(View.VISIBLE);
        sobot_tv_transferBtn.setVisibility(View.VISIBLE);
        if (message != null) {
            message.setShowTransferBtn(true);
        }
        sobot_tv_transferBtn.setOnClickListener(new NoDoubleClickListener() {

            @Override
            public void onNoDoubleClick(View v) {
                if (msgCallBack != null) {
                    msgCallBack.doClickTransfer(message);
                }
            }
        });
    }

    //顶踩 显示 点击 逻辑
    public void refreshItem() {
        if (message == null) {
            return;
        }
        //找不到顶和踩就返回
        if (sobot_right_likebtn_iv == null ||
                sobot_right_dislikegtn_iv == null || sobot_iv_bottom_likeBtn == null && sobot_iv_bottom_dislikeBtn == null) {
            return;
        }
        if (isRight()) {
            //右侧消息没有顶踩按钮
            return;
        }
        if (initMode != null) {
            if (initMode.isAiAgent()) {
                if (aiRobotRealuateConfigInfo == null) {
                    return;
                }
                //大模型机器人
                if (dingcaiIsShowRight()) {
                    //右侧样式
                    if (sobot_right_likebtn_iv != null && sobot_right_dislikegtn_iv != null) {
                        if (aiRobotRealuateConfigInfo.getRealuateButtonStyle() == 1) {
                            //心型
                            sobot_right_likebtn_iv.setImageResource(R.drawable.sobot_btn_heart_shaped_zan_selector);
                            sobot_right_dislikegtn_iv.setImageResource(R.drawable.sobot_btn_heart_shaped_cai_selector);
                        } else {
                            //手势
                            sobot_right_likebtn_iv.setImageResource(R.drawable.sobot_evaluate_btn_zan_selector);
                            sobot_right_dislikegtn_iv.setImageResource(R.drawable.sobot_evaleuate_btn_cai_selector);
                        }
                    }
                } else {
                    if (sobot_chat_more_action != null) {
                        sobot_chat_more_action.setHorizontalGap(ScreenUtils.dip2px(mContext, 12));
                    }
                    //气泡下边顶踩样式
                    if (sobot_iv_bottom_likeBtn != null && sobot_iv_bottom_dislikeBtn != null && aiRobotRealuateConfigInfo != null) {
                        if (aiRobotRealuateConfigInfo.getRealuateButtonStyle() == 1) {
                            //心型
                            sobot_iv_bottom_likeBtn.setImageResource(R.drawable.sobot_btn_heart_shaped_zan_selector);
                            sobot_iv_bottom_dislikeBtn.setImageResource(R.drawable.sobot_btn_heart_shaped_cai_selector);
                        } else {
                            //手势
                            sobot_iv_bottom_likeBtn.setImageResource(R.drawable.sobot_evaluate_btn_zan_selector);
                            sobot_iv_bottom_dislikeBtn.setImageResource(R.drawable.sobot_evaleuate_btn_cai_selector);
                        }
                    }
                }
            }
        }
        //顶 踩的状态 0 不显示顶踩按钮  1显示顶踩 按钮  2 显示顶之后的view  3显示踩之后view
        switch (message.getRevaluateState()) {
            case 1:
                showRevaluateBtn();
                break;
            case 2:
                showLikeWordView();
                break;
            case 3:
                showDislikeWordView();
                break;
            default:
                hideRevaluateBtn();
                break;
        }
    }

    /**
     * 显示 顶踩 按钮
     */
    public void showRevaluateBtn() {
        if (dingcaiIsShowRight()) {
            sobot_right_likebtn_iv.setVisibility(View.VISIBLE);
            sobot_right_dislikegtn_iv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
            if (sobot_iv_bottom_likeBtn != null) {
                sobot_iv_bottom_likeBtn.setVisibility(View.GONE);
                sobot_iv_bottom_dislikeBtn.setVisibility(View.GONE);
            }
        } else {
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            if (sobot_iv_bottom_likeBtn != null) {
                sobot_iv_bottom_likeBtn.setVisibility(View.VISIBLE);
                sobot_iv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            }
            sobot_right_likebtn_iv.setVisibility(View.GONE);
            sobot_right_dislikegtn_iv.setVisibility(View.GONE);
        }

        sobot_right_likebtn_iv.setEnabled(true);
        sobot_right_dislikegtn_iv.setEnabled(true);
        sobot_right_likebtn_iv.setSelected(false);
        sobot_right_dislikegtn_iv.setSelected(false);
        sobot_right_likebtn_iv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doRevaluate(true);
            }
        });
        sobot_right_dislikegtn_iv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doRevaluate(false);
            }
        });
        if (sobot_iv_bottom_likeBtn != null) {
            sobot_iv_bottom_likeBtn.setEnabled(true);
            sobot_iv_bottom_dislikeBtn.setEnabled(true);
            sobot_iv_bottom_likeBtn.setSelected(false);
            sobot_iv_bottom_dislikeBtn.setSelected(false);
            sobot_iv_bottom_likeBtn.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(true);
                }
            });
            sobot_iv_bottom_dislikeBtn.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(false);
                }
            });
        }
    }

    /**
     * 隐藏 顶踩 按钮
     */
    public void hideRevaluateBtn() {
        hideContainer();
        sobot_right_likebtn_iv.setVisibility(View.GONE);
        sobot_right_dislikegtn_iv.setVisibility(View.GONE);
        rightEmptyRL.setVisibility(View.GONE);
        if (sobot_iv_bottom_likeBtn != null) {
            sobot_iv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_iv_bottom_dislikeBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 显示顶之后的view
     */
    public void showLikeWordView() {
        if (dingcaiIsShowRight()) {
            sobot_right_likebtn_iv.setSelected(true);
            sobot_right_likebtn_iv.setEnabled(false);
            sobot_right_dislikegtn_iv.setEnabled(false);
            sobot_right_dislikegtn_iv.setSelected(false);
            sobot_right_likebtn_iv.setVisibility(View.VISIBLE);
            sobot_right_dislikegtn_iv.setVisibility(View.GONE);
            rightEmptyRL.setVisibility(View.VISIBLE);
        } else {
            sobot_iv_bottom_likeBtn.setSelected(true);
            sobot_iv_bottom_likeBtn.setEnabled(false);
            sobot_iv_bottom_dislikeBtn.setEnabled(false);
            sobot_iv_bottom_dislikeBtn.setSelected(false);
            sobot_iv_bottom_likeBtn.setVisibility(View.VISIBLE);
            sobot_iv_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_right_likebtn_iv.setVisibility(View.GONE);
            sobot_right_dislikegtn_iv.setVisibility(View.GONE);
            sobot_chat_more_action.requestLayout();
        }
    }

    /**
     * 显示踩之后的view
     */
    public void showDislikeWordView() {
        if (dingcaiIsShowRight()) {
            sobot_right_dislikegtn_iv.setSelected(true);
            sobot_right_dislikegtn_iv.setEnabled(false);
            sobot_right_likebtn_iv.setEnabled(false);
            sobot_right_likebtn_iv.setSelected(false);
            sobot_right_likebtn_iv.setVisibility(View.GONE);
            sobot_right_dislikegtn_iv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
        } else {
            sobot_iv_bottom_dislikeBtn.setSelected(true);
            sobot_iv_bottom_dislikeBtn.setEnabled(false);
            sobot_iv_bottom_likeBtn.setEnabled(false);
            sobot_iv_bottom_likeBtn.setSelected(false);
            sobot_iv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_iv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_right_likebtn_iv.setVisibility(View.GONE);
            sobot_right_dislikegtn_iv.setVisibility(View.GONE);
            sobot_chat_more_action.requestLayout();
        }
    }

    /**
     * 顶踩 操作
     *
     * @param revaluateFlag true 顶  false 踩
     */
    private void doRevaluate(boolean revaluateFlag) {
        if (msgCallBack != null) {
            msgCallBack.doRevaluate(revaluateFlag, message);
        }
    }

    //隐藏关联问题布局
    public void hideAnswers() {
        if (answersList != null) {
            answersList.setVisibility(View.GONE);
        }
        if (stripe != null) {
            stripe.setVisibility(View.GONE);
        }
    }

    //设置关联问题列表
    public void resetAnswersList() {
        if (message == null) {
            return;
        }
        try {
            if (stripe != null) {
                stripe.setMaxWidth(msgMaxWidth);
                // 回复语的答复
                String stripeContent = message.getStripe() != null ? message.getStripe().trim() : "";
                if (!TextUtils.isEmpty(stripeContent)) {
                    //去掉p标签
                    stripeContent = stripeContent.replace("<p>", "").replace("</p>", "");
                    // 设置提醒的内容
                    stripe.setVisibility(View.VISIBLE);
                    HtmlTools.getInstance(mContext).setRichText(stripe, stripeContent, getLinkTextColor());
                } else {
                    stripe.setText(null);
                    stripe.setVisibility(View.GONE);
                }
            }
            if (answersList != null) {
                if (message.getListSuggestions() != null && !message.getListSuggestions().isEmpty()) {
                    ArrayList<Suggestions> listSuggestions = message.getListSuggestions();
                    answersList.removeAllViews();
                    answersList.setVisibility(View.VISIBLE);
                    int startNum = 0;
                    int endNum = listSuggestions.size();
                    if (message.isGuideGroupFlag() && message.getGuideGroupNum() > -1) {//有分组且不是全部
                        startNum = message.getCurrentPageNum() * message.getGuideGroupNum();
                        endNum = Math.min(startNum + message.getGuideGroupNum(), listSuggestions.size());
                    }
                    for (int i = startNum; i < endNum; i++) {
                        TextView answer = ChatUtils.initAnswerItemTextView(mContext, false);
                        if (i == 0 && StringUtils.isEmpty(message.getStripe())) {
                            answer.setPadding(0, 0, 0, 0);
                        }
                        int currentItem = i + 1;
                        answer.setOnClickListener(new AnsWerClickLisenter(mContext, null,
                                listSuggestions.get(i).getQuestion(), null, listSuggestions.get(i).getDocId(), msgCallBack));
                        String tempStr = processPrefix(message, currentItem) + listSuggestions.get(i).getQuestion();
                        answer.setText(tempStr);
                        setLongClickListener(answer);
                        answersList.addView(answer);
                    }
                } else {
                    String[] answerStringList = message.getSugguestions();
                    answersList.removeAllViews();
                    answersList.setVisibility(View.VISIBLE);
                    for (int i = 0; i < answerStringList.length; i++) {
                        TextView answer = ChatUtils.initAnswerItemTextView(mContext, true);
                        if (i == 0 && StringUtils.isEmpty(message.getStripe())) {
                            answer.setPadding(0, 0, 0, 0);
                        }
                        int currentItem = i + 1;
                        String tempStr = processPrefix(message, currentItem) + answerStringList[i];
                        answer.setText(tempStr);
                        answersList.addView(answer);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    // 问题的回答监听
    public static class AnsWerClickLisenter implements View.OnClickListener {

        private String msgContent;
        private String id;
        private ImageView img;
        private String docId;
        private Context context;
        private SobotMsgAdapter.SobotMsgCallBack mMsgCallBack;

        public AnsWerClickLisenter(Context context, String id, String msgContent, ImageView image,
                                   String docId, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
            super();
            this.context = context;
            this.msgContent = msgContent;
            this.id = id;
            this.img = image;
            this.docId = docId;
            mMsgCallBack = msgCallBack;
        }

        @Override
        public void onClick(View arg0) {
            if (img != null) {
                img.setVisibility(View.GONE);
            }

            if (mMsgCallBack != null) {
                mMsgCallBack.hidePanelAndKeyboard();
                ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                msgObj.setContent(msgContent);
                msgObj.setId(id);
                mMsgCallBack.sendMessageToRobot(msgObj, 0, 1, docId);
            }
        }
    }

    //气泡设置最大宽度
    public void resetMaxWidth() {
        if (sobot_msg_content_ll != null && sobot_msg_content_ll instanceof SobotMaxSizeLinearLayout) {
            ((SobotMaxSizeLinearLayout) sobot_msg_content_ll).setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 16 + 16));
        }
    }

    /**
     * 显示复制和引用的提示
     */
    public void showCopyAndAppointPopWindows(final Context context, View v, final String str, int x, int y) {
        if (v == null) {
            return;
        }
        if (message == null) {
            return;
        }
        /** pop view */
        View mPopView = LayoutInflater.from(context).inflate(R.layout.sobot_pop_chat_room_long_press, null);
        if (initMode == null || initMode.getMsgAppointFlag() == 0 || StringUtils.isEmpty(message.getMessage())) {
            //引用未开启
            mPopView.findViewById(R.id.ll_click_appoint).setVisibility(View.GONE);
            mPopView.findViewById(R.id.view_split).setVisibility(View.GONE);
        } else {
            if (answersList != null && ((message.getListSuggestions() != null && !message.getListSuggestions().isEmpty()) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
                //只要带有关联问题都不能引用
                mPopView.findViewById(R.id.ll_click_appoint).setVisibility(View.GONE);
                mPopView.findViewById(R.id.view_split).setVisibility(View.GONE);
            } else {
                mPopView.findViewById(R.id.ll_click_appoint).setVisibility(View.VISIBLE);
                mPopView.findViewById(R.id.view_split).setVisibility(View.VISIBLE);
            }
        }
        final PopupWindow mPopWindow = new PopupWindow(mPopView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        /** set */
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        /** 这个很重要 ,获取弹窗的长宽度 */
        mPopView.measure(150, 150);
        mPopWindow.setOutsideTouchable(true);
        int popupWidth = mPopView.getMeasuredWidth();
        int popupHeight = mPopView.getMeasuredHeight() + 20;
        /** 获取父控件的位置 */
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        /** 显示位置 */
        mPopWindow.showAtLocation(v, Gravity.NO_GRAVITY,
                location[0] + v.getWidth() / 2 - popupWidth / 2 + x,
                location[1] - popupHeight + y);// + v.getWidth() / 2) -

        mPopWindow.update();
        mPopView.findViewById(R.id.ll_click_copy).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cmb != null) {
                            cmb.setText(str);
                        }
                        ToastUtil.showCustomToast(context, context.getResources().getString(R.string.sobot_ctrl_v_success), R.drawable.sobot_icon_success);
                        mPopWindow.dismiss();
                    }
                });
        mPopView.findViewById(R.id.ll_click_appoint).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appoinitClick(message, context);
                        mPopWindow.dismiss();
                    }
                });
    }

    //设置控件长按事件，弹出引用提示框
    public void setLongClickListener(View view) {
        if (view == null || sobot_msg_content_ll == null) {
            return;
        }
        if (initMode == null || initMode.getMsgAppointFlag() == 0) {
            //引用未开启
            return;
        }
        if (answersList != null && ((message.getListSuggestions() != null && !message.getListSuggestions().isEmpty()) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
            //只要带有关联问题都不能引用
            return;
        }
        sobot_msg_content_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAppointPopWindows(mContext, sobot_msg_content_ll, 0, 18, message);
                return true;
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showAppointPopWindows(mContext, sobot_msg_content_ll, 0, 18, message);
                return true;
            }
        });
        if (answersList != null) {
            answersList.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAppointPopWindows(mContext, sobot_msg_content_ll, 0, 18, message);
                    return true;
                }
            });
        }
        if (stripe != null) {
            stripe.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showAppointPopWindows(mContext, sobot_msg_content_ll, 0, 18, message);
                    return true;
                }
            });
        }
    }

    /**
     * 显示引用的提示
     */
    public void showAppointPopWindows(final Context context, View v, int x, int y, final ZhiChiMessageBase message) {
        if (v == null) {
            return;
        }
        if (initMode == null || initMode.getMsgAppointFlag() == 0) {
            //引用未开启
            return;
        }
        if (message == null || StringUtils.isEmpty(message.getMessage())) {
            return;
        }
        if (answersList != null && ((message.getListSuggestions() != null && !message.getListSuggestions().isEmpty()) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
            //只要带有关联问题都不能引用
            return;
        }
        /** pop view */
        View mPopView = LayoutInflater.from(context).inflate(R.layout.sobot_pop_chat_room_long_press, null);
        mPopView.findViewById(R.id.ll_click_copy).setVisibility(View.GONE);
        mPopView.findViewById(R.id.view_split).setVisibility(View.GONE);
        final PopupWindow mPopWindow = new PopupWindow(mPopView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        /** set */
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopWindow.setOutsideTouchable(true);
        /** 这个很重要 ,获取弹窗的长宽度 */
        mPopView.measure(150, 150);
        int popupWidth = mPopView.getMeasuredWidth();
        int popupHeight = mPopView.getMeasuredHeight() + 20;
        /** 获取父控件的位置 */
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        /** 显示位置 */
        mPopWindow.showAtLocation(v, Gravity.NO_GRAVITY,
                location[0] + v.getWidth() / 2 - popupWidth / 2 + x,
                location[1] - popupHeight + y);// + v.getWidth() / 2) -

        mPopWindow.update();
        mPopView.findViewById(R.id.ll_click_appoint).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appoinitClick(message, context);
                        mPopWindow.dismiss();
                    }
                });
    }

    private void appoinitClick(final ZhiChiMessageBase message, final Context context) {
        if (message == null || StringUtils.isEmpty(message.getMessage())) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(ZhiChiConstants.SOBOT_POST_MSG_APPOINT_BROCAST);
        Bundle bundle = new Bundle();
        ZhiChiAppointMessage appointMessage = new ZhiChiAppointMessage();
        appointMessage.setMsgId(message.getMsgId());
        appointMessage.setCid(message.getCid());
        //appointType 0-客服 1-客户 2-引用机器人
        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
            appointMessage.setAppointType(1);
        } else if (ZhiChiConstant.message_sender_type_service == message.getSenderType()) {
            appointMessage.setAppointType(0);
        } else if (ZhiChiConstant.message_sender_type_robot == message.getSenderType()) {
            appointMessage.setAppointType(2);
        } else {
            appointMessage.setAppointType(1);
        }

        JSONObject messageJsonObject = null;
        try {
            messageJsonObject = new JSONObject(message.getMessage());
            if (messageJsonObject.has("msgType") && !TextUtils.isEmpty(messageJsonObject.optString("msgType"))) {
                String msgType = messageJsonObject.optString("msgType");
                String content = messageJsonObject.optString("content");
                appointMessage.setContent(content);
                appointMessage.setMsgType(Integer.parseInt(msgType));
                bundle.putSerializable("appointMessage", appointMessage);
                intent.putExtras(bundle);
                CommonUtils.sendLocalBroadcast(context, intent);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //顶踩是否显示在气泡右侧，默认是的
    public boolean dingcaiIsShowRight() {
        if (initMode != null) {
            if (initMode.isAiAgent()) {
                //大模型机器人
                if (aiRobotRealuateConfigInfo != null && aiRobotRealuateConfigInfo.getRealuateStyle() == 1) {
                    //大模型机器人顶踩样式 0-右侧展示 1-下方展示 默认0
                    return false;
                } else {
                    return true;
                }
            } else {
                if (initMode.getRealuateStyle() == 1) {
                    return false;
                }
            }
        }
        return true;
    }
}