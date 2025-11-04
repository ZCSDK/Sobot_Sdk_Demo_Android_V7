package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotRCImageView;

/**
 * 提醒消息
 */
public class RemindMessageHolder extends MsgHolderBase {
    private TextView center_Remind_Info; // 中间提醒消息
    private TextView center_Remind_Info1; // 已无更多记录
    private TextView center_Remind_Info2; // simple tip
    private RelativeLayout rl_not_read; //以下为新消息
    private TextView sobot_center_Remind_note5;
    //客服接受用户的请求布局
    private FrameLayout rl_connect_service_card;
    private SobotRCImageView sobot_msg_tip_face_iv;
    private TextView sobot_msg_tip_nike_name_tv;
    private TextView sobot_msg_accept_tip_tv;
    //有边框的边框
    private LinearLayout ll_notice;
    private TextView sobot_center_Remind_note3;
    private ImageView iv_notice_left,iv_notice_right;

    public RemindMessageHolder(Context context, View convertView) {
        super(context, convertView);
        center_Remind_Info = (TextView) convertView
                .findViewById(R.id.sobot_center_Remind_note);
        center_Remind_Info1 = (TextView) convertView
                .findViewById(R.id.sobot_center_Remind_note1);
        center_Remind_Info2 = (TextView) convertView
                .findViewById(R.id.sobot_center_Remind_note2);
        rl_not_read = (RelativeLayout) convertView
                .findViewById(R.id.rl_not_read);
        sobot_center_Remind_note5 = (TextView) convertView
                .findViewById(R.id.sobot_center_Remind_note5);
        sobot_center_Remind_note5.setText(R.string.sobot_no_read);
        rl_connect_service_card = convertView
                .findViewById(R.id.rl_connect_service_card);
        sobot_msg_tip_face_iv = convertView
                .findViewById(R.id.sobot_msg_tip_face_iv);
        sobot_msg_tip_nike_name_tv = convertView
                .findViewById(R.id.sobot_msg_tip_nike_name_tv);
        sobot_msg_accept_tip_tv = convertView
                .findViewById(R.id.sobot_msg_accept_tip_tv);
        ll_notice = convertView.findViewById(R.id.ll_notice);
        sobot_center_Remind_note3 = convertView.findViewById(R.id.sobot_center_Remind_note3);
        iv_notice_left = convertView.findViewById(R.id.iv_notice_left);
        iv_notice_right = convertView.findViewById(R.id.iv_notice_right);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        rl_connect_service_card.setVisibility(View.GONE);
        iv_notice_left.setVisibility(View.GONE);
        if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
            if (message.getAnswer().getRemindType() == ZhiChiConstant.sobot_remind_type_nomore) {
                rl_not_read.setVisibility(View.GONE);
                center_Remind_Info2.setVisibility(View.GONE);
                center_Remind_Info.setVisibility(View.GONE);
                ll_notice.setVisibility(View.GONE);
                center_Remind_Info1.setVisibility(View.VISIBLE);
                center_Remind_Info1.setText(message.getAnswer().getMsg());
            } else if (message.getAnswer().getRemindType() == ZhiChiConstant.sobot_remind_type_below_unread) {
                rl_not_read.setVisibility(View.VISIBLE);
                center_Remind_Info.setVisibility(View.GONE);
                center_Remind_Info1.setVisibility(View.GONE);
                center_Remind_Info2.setVisibility(View.GONE);
                ll_notice.setVisibility(View.GONE);
            } else if (message.getAnswer().getRemindType() == ZhiChiConstant.sobot_remind_type_simple_tip) {
                rl_not_read.setVisibility(View.GONE);
                ll_notice.setVisibility(View.GONE);
                center_Remind_Info.setVisibility(View.GONE);
                center_Remind_Info1.setVisibility(View.GONE);
                center_Remind_Info2.setVisibility(View.VISIBLE);
                HtmlTools.getInstance(context).setRichText(center_Remind_Info2, message
                        .getAnswer().getMsg(), getRemindLinkTextColor());
            } else if (message.getAnswer().getRemindType() == ZhiChiConstant.sobot_have_new_leavemsg) {
                rl_not_read.setVisibility(View.GONE);
                center_Remind_Info.setVisibility(View.GONE);
                center_Remind_Info1.setVisibility(View.GONE);
                center_Remind_Info2.setVisibility(View.GONE);
                ll_notice.setVisibility(View.VISIBLE);
                iv_notice_left.setVisibility(View.VISIBLE);
                HtmlTools.getInstance(context).setRichText(sobot_center_Remind_note3, message
                        .getAnswer().getMsg(), getRemindLinkTextColor());
            } else {
                rl_not_read.setVisibility(View.GONE);
                center_Remind_Info2.setVisibility(View.GONE);
                center_Remind_Info.setVisibility(View.VISIBLE);
                center_Remind_Info1.setVisibility(View.GONE);
                ll_notice.setVisibility(View.GONE);
                int remindType = message.getAnswer().getRemindType();
                if (ZhiChiConstant.action_remind_info_post_msg.equals(message.getAction())) {
                    if (remindType == ZhiChiConstant.sobot_remind_type_customer_offline) {
                        //暂无客服在线   和 暂时无法转接人工客服
                        if (message.isShake()) {
                            sobot_center_Remind_note3.setAnimation(shakeAnimation(5));
                        }
                        center_Remind_Info.setVisibility(View.GONE);
                        ll_notice.setVisibility(View.VISIBLE);
                        setRemindPostMsg(context, sobot_center_Remind_note3, message, false);
                    }
                    if (remindType == ZhiChiConstant.sobot_remind_type_unable_to_customer) {
                        //暂无客服在线   和 暂时无法转接人工客服
                        if (message.isShake()) {
                            sobot_center_Remind_note3.setAnimation(shakeAnimation(5));
                        }
                        center_Remind_Info.setVisibility(View.GONE);
                        ll_notice.setVisibility(View.VISIBLE);
                        setRemindPostMsg(context, sobot_center_Remind_note3, message, true);
                    }
                } else if (ZhiChiConstant.action_remind_info_paidui.equals(message.getAction())) {
                    if (remindType == ZhiChiConstant.sobot_remind_type_paidui_status) {
                        //您在队伍中的第...
                        if (message.isShake()) {
                            center_Remind_Info.setAnimation(shakeAnimation(5));
                        }
                        setRemindPostMsg(context, center_Remind_Info, message, false);
                    }
                } else if (ZhiChiConstant.action_remind_connt_success.equals(message.getAction())) {
//                    center_Remind_Info.setVisibility(View.GONE);
//                    if (remindType == ZhiChiConstant.sobot_remind_type_accept_request) {
//                        //接受了您的请求
//                        rl_connect_service_card.setVisibility(View.VISIBLE);
//                        if (!TextUtils.isEmpty(message.getSenderName())) {
//                            if (message.getSenderName().length() > 20) {
//                                sobot_msg_tip_nike_name_tv.setText(message.getSenderName().substring(0, 20) + "...");
//                            } else {
//                                sobot_msg_tip_nike_name_tv.setText(message.getSenderName());
//                            }
//                        }
//                        if (!TextUtils.isEmpty(message.getSenderFace())) {
//                            SobotBitmapUtil.display(mContext, CommonUtils.encode(message.getSenderFace()),
//                                    sobot_msg_tip_face_iv, R.drawable.sobot_robot, R.drawable.sobot_default_pic_err);
//                        }
//                        sobot_msg_accept_tip_tv.setText(R.string.sobot_service_for_you);
//                    }
                    if (remindType == ZhiChiConstant.sobot_remind_type_accept_request) {
                        //接受了您的请求
                        center_Remind_Info.setText(Html.fromHtml(message.getAnswer().getMsg()));
                    }

                } else if (ZhiChiConstant.sobot_outline_leverByManager.equals(message
                        .getAction()) || ZhiChiConstant.action_remind_past_time.equals(message.getAction())) {
                    //结束了本次会话  有事离开 超时下线 ....的提醒
                    HtmlTools.getInstance(context).setRichText(center_Remind_Info, message
                            .getAnswer().getMsg(), R.color.sobot_color_link_remind);
                } else if (remindType == ZhiChiConstant.sobot_remind_type_tip || remindType == ZhiChiConstant.sobot_remind_type_accept_request) {
                    center_Remind_Info.setText(message.getAnswer().getMsg());
                }
            }

            if (message.isShake()) {
                center_Remind_Info.setAnimation(shakeAnimation(5));
                message.setShake(false);
            }
        } else if (ZhiChiConstant.action_remind_info_zhuanrengong.equals(message.getAction())) {
            rl_not_read.setVisibility(View.GONE);
            center_Remind_Info2.setVisibility(View.GONE);
            ll_notice.setVisibility(View.VISIBLE);
            center_Remind_Info.setVisibility(View.GONE);
            center_Remind_Info1.setVisibility(View.GONE);
            setRemindToCustom(context, sobot_center_Remind_note3);
        } else if (ZhiChiConstant.action_sensitive_auth_agree.equals(message.getAction())) {
            rl_not_read.setVisibility(View.GONE);
            center_Remind_Info2.setVisibility(View.GONE);
            center_Remind_Info.setVisibility(View.VISIBLE);
            center_Remind_Info1.setVisibility(View.GONE);
            center_Remind_Info.setText(R.string.sobot_agree_sentisive_tip);
        } else if (ZhiChiConstant.action_sensitive_auth_agree.equals(message.getAction())) {
            rl_not_read.setVisibility(View.GONE);
            center_Remind_Info2.setVisibility(View.GONE);
            center_Remind_Info.setVisibility(View.VISIBLE);
            center_Remind_Info1.setVisibility(View.GONE);
            center_Remind_Info.setText(ResourceUtils.getResStrId(context, "sobot_agree_sentisive_tip"));
        } else if (ZhiChiConstant.action_mulit_postmsg_tip_can_click.equals(message.getAction()) || ZhiChiConstant.action_mulit_postmsg_tip_nocan_click.equals(message.getAction())) {
            rl_not_read.setVisibility(View.GONE);
            center_Remind_Info.setVisibility(View.GONE);
            center_Remind_Info1.setVisibility(View.GONE);
            center_Remind_Info2.setVisibility(View.VISIBLE);
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(mContext,
                    ZhiChiConstant.sobot_last_current_initModel);
            //47可以点击，48不可点击
            if (ZhiChiConstant.action_mulit_postmsg_tip_can_click.equals(message.getAction()) && initMode != null && initMode.getCid().equals(message.getCid())) {
                HtmlTools.getInstance(context).setRichText(center_Remind_Info2, message.getMsg().replace("<a>", "<a href='sobot:SobotMuItiPostMsgActivty?" + message.getDeployId() + "::" + message.getMsgId() + "'>"), getLinkTextColor());
            } else {
                HtmlTools.getInstance(context).setRichText(center_Remind_Info2, message.getMsg(), getLinkTextColor());
            }
        } else if (ZhiChiConstant.action_card_mind_msg.equals(message.getAction())) {
            //卡片确认消息
            rl_not_read.setVisibility(View.GONE);
            center_Remind_Info.setVisibility(View.GONE);
            center_Remind_Info1.setVisibility(View.GONE);
            center_Remind_Info2.setVisibility(View.VISIBLE);
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(mContext,
                    ZhiChiConstant.sobot_last_current_initModel);
            HtmlTools.getInstance(context).setRichText(center_Remind_Info2, message.getMsg(), ResourceUtils.getIdByName(context, "color", "sobot_color"));
        }
        refreshReadStatus();
    }

    /**
     * @param context
     * @param remindInfo
     * @param message
     * @param haveDH     是否有 逗号
     */
    private void setRemindPostMsg(Context context, TextView remindInfo, ZhiChiMessageBase message, boolean haveDH) {
        int isLeaveMsg = SharedPreferencesUtil.getIntData(context, ZhiChiConstant.sobot_msg_flag, ZhiChiConstant.sobot_msg_flag_open);
        String postMsg = (haveDH ? "，" : " ") + context.getResources().getString(R.string.sobot_can) + " <a href='sobot:SobotPostMsgActivity'> " + context.getResources().getString(R.string.sobot_str_bottom_message) + "</a>";
        String content = message.getAnswer().getMsg().replace("<p>", "").replace("</p>", "").replace("\n", "<br/>");
        if (isLeaveMsg == ZhiChiConstant.sobot_msg_flag_open) {
            content = content + postMsg;
        }
        HtmlTools.getInstance(context).setRichText(remindInfo, content, getRemindLinkTextColor());
        remindInfo.setEnabled(true);
        message.setShake(false);
    }

    /**
     * @param context
     * @param remindInfo
     */
    private void setRemindToCustom(Context context, TextView remindInfo) {
        String content = context.getResources().getString(R.string.sobot_cant_solve_problem_new);
        String click = "<a href='sobot:SobotToCustomer'> " + context.getResources().getString(R.string.sobot_customer_service) + "</a>";
        HtmlTools.getInstance(context).setRichText(remindInfo, String.format(content,click), getRemindLinkTextColor());
        remindInfo.setEnabled(true);
    }

    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }
}
