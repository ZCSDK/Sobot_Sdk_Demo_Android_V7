package com.sobot.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.FaqDocRespVo;
import com.sobot.chat.api.model.SobotAiRobotRealuateInfo;
import com.sobot.chat.api.model.SobotEvaluateModel;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.SobotRealuateInfo;
import com.sobot.chat.api.model.SobotlanguaeModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomMenu;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.VersionUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.AiCardMessageHolder;
import com.sobot.chat.viewHolder.AiCardRightMessageHolder;
import com.sobot.chat.viewHolder.AiRobotCaiReasonMessageHolder;
import com.sobot.chat.viewHolder.AppointTextMessageHolder;
import com.sobot.chat.viewHolder.ArticleMessageHolder;
import com.sobot.chat.viewHolder.CaiReasonMessageHolder;
import com.sobot.chat.viewHolder.CardMessageHolder;
import com.sobot.chat.viewHolder.ChangeLanguaeMessageHolder;
import com.sobot.chat.viewHolder.ConsultMessageHolder;
import com.sobot.chat.viewHolder.CusEvaluateMessageHolder;
import com.sobot.chat.viewHolder.CustomCardMessageHolder;
import com.sobot.chat.viewHolder.FileMessageHolder;
import com.sobot.chat.viewHolder.HotIssueMessageHolder;
import com.sobot.chat.viewHolder.ImageMessageHolder;
import com.sobot.chat.viewHolder.LocationMessageHolder;
import com.sobot.chat.viewHolder.MiniProgramMessageHolder;
import com.sobot.chat.viewHolder.NoticeMessageHolder;
import com.sobot.chat.viewHolder.OrderCardMessageHolder;
import com.sobot.chat.viewHolder.RemindMessageHolder;
import com.sobot.chat.viewHolder.RetractedMessageHolder;
import com.sobot.chat.viewHolder.RichTextMessageHolder;
import com.sobot.chat.viewHolder.RobotAiagentButtonMessageHolder;
import com.sobot.chat.viewHolder.RobotAnswerItemsMsgHolder;
import com.sobot.chat.viewHolder.RobotKeyWordMessageHolder;
import com.sobot.chat.viewHolder.RobotQRMessageHolder;
import com.sobot.chat.viewHolder.RobotSemanticsKeyWordMessageHolder;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder1;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder2;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder3;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder4;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder5;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder6;
import com.sobot.chat.viewHolder.SensitiveAuthorizeHolder;
import com.sobot.chat.viewHolder.SobotChatMsgItemSDKHistoryR;
import com.sobot.chat.viewHolder.SobotMuitiLeavemsgMessageHolder;
import com.sobot.chat.viewHolder.SystemMessageHolder;
import com.sobot.chat.viewHolder.TextMessageHolder;
import com.sobot.chat.viewHolder.VideoMessageHolder;
import com.sobot.chat.viewHolder.VoiceMessageHolder;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.utils.SobotDensityUtil;
import com.sobot.utils.SobotStringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

public class SobotMsgAdapter extends RecyclerView.Adapter<MsgHolderBase> {

    private static final int[] layoutRes = {
            R.layout.sobot_chat_msg_item_txt_l,//0文本消息左边的布局文件
            R.layout.sobot_chat_msg_item_txt_r,//1文本消息右边的布局文件
            R.layout.sobot_chat_msg_item_tip,//2消息提醒的布局文件
            R.layout.sobot_chat_msg_item_rich,//3富文本消息布局文件
            R.layout.sobot_chat_msg_item_imgt_l,//4图片消息左边的布局文件
            R.layout.sobot_chat_msg_item_imgt_r,//5图片消息右边的布局文件
            R.layout.sobot_chat_msg_item_audiot_r,//6语音消息右边的布局文件
            R.layout.sobot_chat_msg_item_consult,//7商品咨询内容的布局文件
            R.layout.sobot_chat_msg_item_evaluate,//8客服邀请评价的布局文件
            R.layout.sobot_chat_msg_item_template1_l,//9机器人  多轮会话模板 1
            R.layout.sobot_chat_msg_item_template2_l,//10机器人  多轮会话模板 2
            R.layout.sobot_chat_msg_item_template3_l,//11机器人  多轮会话模板 3
            R.layout.sobot_chat_msg_item_sdk_history_r,//12 SDK  历史记录中多轮会话使用的布局
            R.layout.sobot_chat_msg_item_template4_l,//13机器人  多轮会话模板 4
            R.layout.sobot_chat_msg_item_template5_l,//14机器人  多轮会话模板 5
            R.layout.sobot_chat_msg_item_question_recommend,//15热点问题列表
            R.layout.sobot_chat_msg_item_retracted_msg,//16消息撤回
            R.layout.sobot_chat_msg_item_robot_answer_items_l,//17多轮会话模板 1511类型  显示的view
            R.layout.sobot_chat_msg_item_robot_keyword_items_l,//18机器人关键字转人工 布局
            R.layout.sobot_chat_msg_item_file_l,//19文件消息左边的布局文件
            R.layout.sobot_chat_msg_item_file_r,//20文件消息右边的布局文件
            R.layout.sobot_chat_msg_item_video_r,//21视频消息右边的布局文件
            R.layout.sobot_chat_msg_item_location_r,//22位置信息的布局文件
            R.layout.sobot_chat_msg_item_notice,//23通告消息的布局文件
            R.layout.sobot_chat_msg_item_card_r,//24商品卡片右侧信息的布局文件
            R.layout.sobot_chat_msg_item_order_card_r,//25订单卡片右侧消息
            R.layout.sobot_chat_msg_item_order_card_l,//26订单卡片左侧消息
            R.layout.sobot_chat_msg_item_card_l,//27商品卡片左侧信息的布局文件
            R.layout.sobot_chat_msg_item_template6_l,//28机器人  多轮会话模板 6
            R.layout.sobot_chat_msg_item_system_tip,//29防诈骗系统消息的布局文件
            R.layout.sobot_chat_msg_item_video_l,//30小视频左边的布局文件
            R.layout.sobot_chat_msg_item_muiti_leave_msg,//31工单节点留言的布局文件
            R.layout.sobot_chat_msg_item_mini_program_card_l,//32小程序卡片
            R.layout.sobot_chat_msg_item_hot_issue,//33多业务热门问题
            R.layout.sobot_chat_msg_item_article_card_l,//34文章卡片
            R.layout.sobot_chat_msg_item_custom_card_l,//35自定义卡片 左侧 系统或者客服发送的
            R.layout.sobot_chat_msg_item_custom_card_r,//36自定义卡片 右侧 用户发送的
            R.layout.sobot_chat_msg_item_appoint_l,//37引用消息 左侧
            R.layout.sobot_chat_msg_item_appoint_r,//38引用消息  右侧
            R.layout.sobot_chat_msg_item_cai_reason_card,//39点踩回答消息
            R.layout.sobot_chat_msg_item_robot_keyword_items_l,//40机器人语义关键字转人工 布局
            R.layout.sobot_chat_msg_item_change_languae,//41切换语言消息 布局
            R.layout.sobot_chat_msg_item_sensitive_authorize,//42敏感词授权卡片 布局
            R.layout.sobot_chat_msg_item_ai_card_l,//43大模型卡片
            R.layout.sobot_chat_msg_item_ai_card_r,//44大模型卡片
            R.layout.sobot_chat_msg_item_aiagent_button_type,//45大模型机器人 按钮消息
            R.layout.sobot_chat_msg_item_cai_reason_card//46大模型机器人 点踩 原因卡片
    };

    /**
     * 非法消息类型
     */
    private static final int MSG_TYPE_ILLEGAL = 0;
    /**
     * 收到的文本消息
     */
    public static final int MSG_TYPE_TXT_L = 0;
    /**
     * 发送的文本消息
     */
    public static final int MSG_TYPE_TXT_R = 1;
    /**
     * 发送的消息提醒
     */
    public static final int MSG_TYPE_TIP = 2;
    /**
     * 收到富文本消息
     */
    public static final int MSG_TYPE_RICH = 3;
    /**
     * 收到图片消息
     */
    public static final int MSG_TYPE_IMG_L = 4;
    /**
     * 发送图片消息
     */
    public static final int MSG_TYPE_IMG_R = 5;
    /**
     * 语音消息
     */
    public static final int MSG_TYPE_AUDIO_R = 6;
    /**
     * 发送商品咨询
     */
    public static final int MSG_TYPE_CONSULT = 7;
    /**
     * 客服主动邀请客户评价
     */
    public static final int MSG_TYPE_CUSTOM_EVALUATE = 8;
    /**
     * 机器人  多轮会话模板 1
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE1 = 9;
    /**
     * 机器人  多轮会话模板 2
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE2 = 10;
    /**
     * 机器人  多轮会话模板 3
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE3 = 11;
    /**
     * 多轮会话  右边类型
     */
    public static final int MSG_TYPE_MULTI_ROUND_R = 12;
    /**
     * 机器人  多轮会话模板 4
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE4 = 13;
    /**
     * 机器人  多轮会话模板 5  无模版情况下显示的view
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE5 = 14;
    /**
     * 机器人热点问题引导
     */
    public static final int MSG_TYPE_ROBOT_QUESTION_RECOMMEND = 15;
    /**
     * 消息撤回
     */
    public static final int MSG_TYPE_RETRACTED_MSG = 16;
    /**
     * 机器人  多轮会话模板 1511类型  显示的view
     */
    public static final int MSG_TYPE_ROBOT_ANSWER_ITEMS = 17;
    /**
     * 机器人关键字转人工 布局类型
     */
    public static final int MSG_TYPE_ROBOT_KEYWORD_ITEMS = 18;
    /**
     * 收到的文件消息
     */
    public static final int MSG_TYPE_FILE_L = 19;
    /**
     * 发送的文件消息
     */
    public static final int MSG_TYPE_FILE_R = 20;
    /**
     * 发送的小视频
     */
    public static final int MSG_TYPE_VIDEO_R = 21;
    /**
     * 发送的位置信息
     */
    public static final int MSG_TYPE_LOCATION_R = 22;
    /**
     * 通告信息
     */
    public static final int MSG_TYPE_NOTICE = 23;
    /**
     * 商品卡片右侧
     */
    public static final int MSG_TYPE_CARD_R = 24;
    /**
     * 订单卡片右侧消息
     */
    public static final int MSG_TYPE_ROBOT_ORDERCARD_R = 25;
    /**
     * 订单卡片左侧侧消息
     */
    public static final int MSG_TYPE_ROBOT_ORDERCARD_L = 26;
    /**
     * 商品卡片左侧
     */
    public static final int MSG_TYPE_CARD_L = 27;
    /**
     * 机器人  多轮会话模板 6(为松果添加的)
     */
    public static final int MSG_TYPE_ROBOT_TEMPLATE6 = 28;

    /**
     * 系统消息 防诈骗
     */
    public static final int MSG_TYPE_FRAUD_PREVENTION = 29;

    /**
     * 客服发送的视频  左侧
     */
    public static final int MSG_TYPE_VIDEO_L = 30;

    /**
     * 多伦节点留言
     */
    public static final int MSG_TYPE_MUITI_LEAVE_MSG_R = 31;

    /**
     * 小程序卡片左侧侧消息
     */
    public static final int MSG_TYPE_MINIPROGRAM_CARD_L = 32;

    /**
     * 多业务--热门问题消息
     */
    public static final int MSG_TYPE_HOT_ISSUE = 33;
    /**
     * 文章卡片左侧消息
     */
    public static final int MSG_TYPE_ARTICLE_CARD_L = 34;

    /**
     * 特殊系统消息 卡片 左侧
     */
    public static final int MSG_TYPE_CUSTOMER_CARD_L = 35;

    /**
     * 卡片 右侧
     */
    public static final int MSG_TYPE_CUSTOMER_CARD_R = 36;


    /**
     * 引用消息 左侧
     */
    public static final int MSG_TYPE_APPOINT_L = 37;

    /**
     * 引用消息 右侧
     */
    public static final int MSG_TYPE_APPOINT_R = 38;
    /**
     * 点踩回答
     */
    public static final int MSG_TYPE_CAI = 39;

    /**
     * 机器人语义关键字转人工 布局类型
     */
    public static final int MSG_TYPE_ROBOT_SEMANTICS_KEYWORD_ITEMS = 40;

    /**
     * 用户进线切换语言 布局类型
     */
    public static final int MSG_TYPE_CHANGE_LANGUAE = 41;

    /**
     * 敏感词授权卡片 布局类型
     */
    public static final int MSG_TYPE_SENSITIVE_AUTHORIZE = 42;
    /**
     * 大模型卡片
     */
    public static final int MSG_TYPE_AI_CARD_L = 43;

    /**
     * 大模型卡片
     */
    public static final int MSG_TYPE_AI_CARD_R = 44;
    /**
     * 大模型机器人 按钮消息
     */
    public static final int MSG_TYPE_ROBOT_AIAGENT_BUTTON = 45;

    /**
     * 大模型机器人点踩 原因卡片
     */
    public static final int MSG_TYPE_AI_ROBOT_CAI = 46;
    private SobotMsgCallBack mMsgCallBack;
    private List<ZhiChiMessageBase> list;//数据源
    private Context context;

    public SobotMsgAdapter(Context context, List<ZhiChiMessageBase> list, SobotMsgCallBack msgCallBack) {
        if (list == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = list;
        }
        this.context = context;
        mMsgCallBack = msgCallBack;
    }

    @NonNull
    @Override
    public MsgHolderBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createMessageViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgHolderBase holder, int position) {
        if (getItem(position) != null) {
            ZhiChiMessageBase zhiChiMessageBase = getItem(position);
            if (zhiChiMessageBase != null) {
                holder.setMsgCallBack(mMsgCallBack);
                holder.bindZhiChiMessageBase(zhiChiMessageBase);//设置message数据
                holder.initNameAndFace(getItemViewType(position));
                handerRemindTiem(holder, position);
                holder.bindData(context, zhiChiMessageBase);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            ZhiChiMessageBase message = getItem(position);
            if (message == null) {
                return MSG_TYPE_ILLEGAL;
            } else if (message.isRetractedMsg()) {
                return MSG_TYPE_RETRACTED_MSG;
            }
            int senderType = message.getSenderType();
            if (senderType == -1) {
                if (ZhiChiConstant.message_type_fraud_prevention == Integer
                        .parseInt(message.getAction())) {
                    //防诈骗消息
                    return MSG_TYPE_FRAUD_PREVENTION;
                }
            }
            if (ZhiChiConstant.message_sender_type_customer == senderType
                    || ZhiChiConstant.message_sender_type_robot == senderType
                    || ZhiChiConstant.message_sender_type_service == senderType) {
                // 发送人类型 0是SDK客户  1是机器人  2 是客服
                // 这些都是平台传过来的消息
                if (message.getAnswer() != null) {
                    if (ZhiChiConstant.message_type_text == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_robot == message.getSenderType()) {
                            if (SobotStringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant())) {
                                //大模型文本消息
                                return MSG_TYPE_TXT_L;
                            } else {
                                return MSG_TYPE_RICH;
                            }
                        } else if (ZhiChiConstant.message_sender_type_service == message.getSenderType()) {
                            return MSG_TYPE_TXT_L;
                        } else if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            return MSG_TYPE_TXT_R;
                        }
                    } else if (ZhiChiConstant.message_type_pic == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            return MSG_TYPE_IMG_R;
                        } else {
                            return MSG_TYPE_IMG_L;
                        }
                    } else if (ZhiChiConstant.message_type_voice == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsgTransfer())) {
                                return MSG_TYPE_TXT_R;
                            }
                            return MSG_TYPE_AUDIO_R;
                        } else {
                            return MSG_TYPE_ILLEGAL;
                        }

                    } else if (ZhiChiConstant.message_type_emoji == message.getAnswer().getMsgType()) {
                        // 富文本格式
                        if (ZhiChiConstant.message_sender_type_robot == message.getSenderType()
                                || ZhiChiConstant.message_sender_type_service == message.getSenderType()) {
                            return MSG_TYPE_RICH;
                        }
                    } else if (ZhiChiConstant.message_type_textAndPic == message.getAnswer().getMsgType()) {
                        //富文本中有图片
                        if (ZhiChiConstant.message_sender_type_robot == message.getSenderType()
                                || ZhiChiConstant.message_sender_type_service == message.getSenderType()) {
                            return MSG_TYPE_RICH;
                        }
                    } else if (ZhiChiConstant.message_type_textAndText == message.getAnswer().getMsgType()) {
                        //富文本中纯文字
                        if (ZhiChiConstant.message_sender_type_robot == message.getSenderType()
                                || ZhiChiConstant.message_sender_type_service == message.getSenderType()) {
                            return MSG_TYPE_RICH;
                        }
                    } else if (message.getAnswer().getMsgType() == ZhiChiConstant.message_type_reply) {
                        return MSG_TYPE_RICH;
                    } else if (message.getAnswer().getMsgType() == ZhiChiConstant.message_type_reply_multi_round) {
                        return MSG_TYPE_RICH;
                    } else if (ZhiChiConstant.message_type_history_custom == message.getAnswer().getMsgType()) {
                        return MSG_TYPE_MULTI_ROUND_R;
                    } else if (ZhiChiConstant.message_type_history_robot == message.getAnswer().getMsgType()) {
                        if (GsonUtil.isMultiRoundSession(message) && message.getAnswer().getMultiDiaRespInfo() != null) {
                            SobotMultiDiaRespInfo multiDiaRespInfo = message.getAnswer().getMultiDiaRespInfo();
                            if ("1511".equals(message.getAnswerType())) {
                                return MSG_TYPE_ROBOT_ANSWER_ITEMS;
                            }
                            if ("1522".equals(message.getAnswerType())) {
                                return MSG_TYPE_RICH;
                            }
                            if (multiDiaRespInfo.getInputContentList() != null && multiDiaRespInfo.getInputContentList().length > 0) {
                                return MSG_TYPE_ROBOT_TEMPLATE2;
                            }
                            if (!TextUtils.isEmpty(multiDiaRespInfo.getTemplate())) {
                                if ("0".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE1;
                                } else if ("1".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE2;
                                } else if ("2".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE3;
                                } else if ("3".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE4;
                                } else if ("4".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE5;
                                } else if ("99".equals(multiDiaRespInfo.getTemplate())) {
                                    return MSG_TYPE_ROBOT_TEMPLATE6;
                                }
                            } else {
                                if ((multiDiaRespInfo.getInterfaceRetList() == null || multiDiaRespInfo.getInterfaceRetList().size() <= 0) && (multiDiaRespInfo.getInputContentList() == null || multiDiaRespInfo.getInputContentList().length <= 0)) {
                                    return MSG_TYPE_ROBOT_TEMPLATE5;
                                }
                                return MSG_TYPE_ROBOT_TEMPLATE2;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_file == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            return MSG_TYPE_FILE_R;
                        } else {
                            return MSG_TYPE_FILE_L;
                        }
                    } else if (ZhiChiConstant.message_type_video == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            if (message.getAnswer().getCacheFile() != null) {
                                return MSG_TYPE_VIDEO_R;
                            }
                        } else {
                            return MSG_TYPE_VIDEO_L;
                        }
                    } else if (ZhiChiConstant.message_type_location == message.getAnswer().getMsgType()) {
                        if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                            if (message.getAnswer().getLocationData() != null) {
                                return MSG_TYPE_LOCATION_R;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_card == message.getAnswer().getMsgType()) {
                        if (message.getConsultingContent() != null) {
                            if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                                return MSG_TYPE_CARD_R;
                            } else {
                                return MSG_TYPE_CARD_L;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_ordercard == message.getAnswer().getMsgType()) {
                        if (message.getOrderCardContent() != null) {
                            if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                                return MSG_TYPE_ROBOT_ORDERCARD_R;
                            } else {
                                return MSG_TYPE_ROBOT_ORDERCARD_L;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_miniprogram_card == message.getAnswer().getMsgType()) {
                        if (message.getMiniProgramModel() != null) {
                            return MSG_TYPE_MINIPROGRAM_CARD_L;
                        }
                    } else if (ZhiChiConstant.message_type_article_card_msg == message.getAnswer().getMsgType()) {
                        return MSG_TYPE_ARTICLE_CARD_L;
                    } else if (ZhiChiConstant.message_type_muiti_leave_msg == message.getAnswer().getMsgType()) {
                        return MSG_TYPE_MUITI_LEAVE_MSG_R;
                    } else if (ZhiChiConstant.message_type_card_msg == message.getAnswer().getMsgType()) {
                        if (message.getCustomCard() != null) {
                            if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                                return MSG_TYPE_CUSTOMER_CARD_R;
                            } else {
                                return MSG_TYPE_CUSTOMER_CARD_L;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_ai_card_msg == message.getAnswer().getMsgType()) {
                        if (message.getCustomCard() != null) {
                            if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                                return MSG_TYPE_AI_CARD_R;
                            } else {
                                return MSG_TYPE_AI_CARD_L;
                            }
                        }
                    } else if (ZhiChiConstant.message_type_appoint_msg == message.getAnswer().getMsgType()) {
                        if (message.getAppointMessage() != null) {
                            if (ZhiChiConstant.message_sender_type_customer == message.getSenderType()) {
                                return MSG_TYPE_APPOINT_R;
                            } else {
                                return MSG_TYPE_APPOINT_L;
                            }
                        }
                    }
                } else {
                    return MSG_TYPE_ILLEGAL;
                }
            } else if (ZhiChiConstant.message_sender_type_system == senderType && null != message.getCustomCard()) {
                if (message.getCustomCard().getCardForm() == 1) {
                    //大模型卡片
                    return MSG_TYPE_AI_CARD_L;
                } else {
                    //自定义卡片
                    return MSG_TYPE_CUSTOMER_CARD_L;
                }
            } else if (ZhiChiConstant.message_sender_type_remide_info == senderType) {
                //提醒的消息
                return MSG_TYPE_TIP;
            } else if (ZhiChiConstant.message_sender_type_customer_sendImage == senderType) {
                // 与我的图片消息
                return MSG_TYPE_IMG_R;
            } else if (ZhiChiConstant.message_sender_type_send_voice == senderType) {
                // 发送语音消息
                return MSG_TYPE_AUDIO_R;
            } else if (ZhiChiConstant.message_sender_type_consult_info == senderType) {
                return MSG_TYPE_CONSULT;
            } else if (ZhiChiConstant.message_sender_type_robot_guide == senderType) {
                return MSG_TYPE_RICH;
            } else if (ZhiChiConstant.message_sender_type_custom_evaluate == senderType) {
                return MSG_TYPE_CUSTOM_EVALUATE;
            } else if (ZhiChiConstant.message_sender_type_questionRecommend == senderType) {
                return MSG_TYPE_ROBOT_QUESTION_RECOMMEND;
            } else if (ZhiChiConstant.message_sender_type_robot_welcome_msg == senderType) {
                return MSG_TYPE_RICH;
            } else if (ZhiChiConstant.message_sender_type_robot_keyword_msg == senderType) {
                return MSG_TYPE_ROBOT_KEYWORD_ITEMS;
            } else if (ZhiChiConstant.message_sender_type_robot_semantics_keyword_msg == senderType) {
                return MSG_TYPE_ROBOT_SEMANTICS_KEYWORD_ITEMS;
            } else if (ZhiChiConstant.message_sender_type_change_languae == senderType) {
                return MSG_TYPE_CHANGE_LANGUAE;
            } else if (ZhiChiConstant.message_sender_type_sensitive_authorize == senderType) {
                return MSG_TYPE_SENSITIVE_AUTHORIZE;
            } else if (ZhiChiConstant.message_sender_type_notice == senderType) {
                return MSG_TYPE_NOTICE;
            } else if (ZhiChiConstant.message_sender_type_aiagent_button == senderType) {
                //大模型按钮卡片消息
                return MSG_TYPE_ROBOT_AIAGENT_BUTTON;
            } else if (ZhiChiConstant.message_sender_type_ai_tobot_cai_card == senderType) {
                //大模型点踩问答
                return MSG_TYPE_AI_ROBOT_CAI;
            } else if ((ZhiChiConstant.message_type_fraud_prevention + "").equals(message.getAction())) {
                //防诈骗消息
                return MSG_TYPE_FRAUD_PREVENTION;
            } else if (ZhiChiConstant.action_sensitive_auth_agree.equals(message.getAction())) {
                //发送消息触发隐私，同意后的系统消息
                return MSG_TYPE_TIP;
            } else if (ZhiChiConstant.action_sensitive_hot_issue.equals(message.getAction())) {
                return MSG_TYPE_HOT_ISSUE;
            } else if (ZhiChiConstant.action_mulit_postmsg_tip_can_click.equals(message.getAction())) {
                //多轮收集节点提醒消息 可以点击
                return MSG_TYPE_TIP;
            } else if (ZhiChiConstant.action_mulit_postmsg_tip_nocan_click.equals(message.getAction())) {
                //多轮收集节点提醒消息 不可以点击
                return MSG_TYPE_TIP;
            } else if (ZhiChiConstant.action_card_mind_msg.equals(message.getAction())) {
                //多轮收集节点提醒消息 不可以点击
                return MSG_TYPE_TIP;
            } else if (!TextUtils.isEmpty(message.getAction()) && message.getAction().equals("25")) {
                //点踩问答
                return MSG_TYPE_CAI;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MSG_TYPE_ILLEGAL;
        }

        return MSG_TYPE_ILLEGAL;
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public void addData(List<ZhiChiMessageBase> moreList) {
        if (moreList == null) {
            return;
        }
        List<ZhiChiMessageBase> msgLists = new ArrayList<>();
        for (ZhiChiMessageBase base : moreList) {
            //过滤不显示的消息
            //49 自定义卡片按钮点击事件不显示提示语
            if (!("49").equals(base.getAction())) {
                msgLists.add(base);
            }
        }

        setDefaultCid(msgLists);
        long previousMsgTime = 0;
        int previousMsgSenderName = -1;
        int unReadIndex = 0;
        for (int i = 0; i < msgLists.size(); i++) {
            ZhiChiMessageBase base = msgLists.get(i);
            //相邻两条消息是同一个人发的，并且时间相隔1分钟，不显示头像昵称
            if (previousMsgTime != 0 &&
                    !TextUtils.isEmpty(base.getT())
                    && ((Long.parseLong(base.getT()) - previousMsgTime) < (1000 * 60))
                    && previousMsgSenderName == base.getSenderType()) {
                base.setShowFaceAndNickname(false);
            } else {
                base.setShowFaceAndNickname(true);
            }
            if (base.getT() != null) {
                try {
                    previousMsgTime = Long.parseLong(base.getT());
                } catch (Exception e) {

                }
            }
            previousMsgSenderName = base.getSenderType();
            //判断未读消息的位置
            if (ZhiChiConstant.message_sender_type_remide_info == base.getSenderType() && base.getAnswer() != null && base.getAnswer().getRemindType() == ZhiChiConstant.sobot_remind_type_below_unread) {
                unReadIndex = i;
            }
            if (SobotStringUtils.isNoEmpty(base.getServant()) && "aiagent".equals(base.getServant())
                    && base.getAnswer() != null && base.getAnswer().getMsgType() == ZhiChiConstant.message_type_text && base.getSenderType() == ZhiChiConstant.message_sender_type_robot) {
                //如果是aiagent 答案
                doMarkDownData(base.getAnswer().getMsg(), base.getAnswer());
            }
        }
        list.addAll(0, msgLists);
        notifyDataSetChanged();
        if (mMsgCallBack != null) {
            mMsgCallBack.checkUnReadMsg();
            mMsgCallBack.unReadMsgIndex(unReadIndex);
        }
    }

    public void addData(ZhiChiMessageBase message) {
        if (message == null) {
            return;
        }
        if (message.getAction() != null && ZhiChiConstant.action_remind_connt_success.equals(message
                .getAction())) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getSugguestionsFontColor() != 1) {
                    if (list.get(i).getCustomCard() == null) {
                        list.get(i).setSugguestionsFontColor(1);
                    }
                }
            }
        }

        removeByAction(message, ZhiChiConstant.action_remind_no_service, ZhiChiConstant
                .action_remind_no_service, true);

        removeByAction(message, ZhiChiConstant.action_remind_info_paidui, ZhiChiConstant.action_remind_info_paidui, true);

        removeByAction(message, ZhiChiConstant.action_remind_info_paidui, ZhiChiConstant.action_remind_info_post_msg, true);

        removeByAction(message, ZhiChiConstant.action_remind_connt_success, ZhiChiConstant
                .action_remind_info_paidui, false);

        removeByAction(message, ZhiChiConstant.action_remind_info_post_msg, ZhiChiConstant.action_remind_info_post_msg, true);

        removeByAction(message, ZhiChiConstant.action_remind_connt_success, ZhiChiConstant
                .action_remind_info_post_msg, false);

        removeByAction(message, ZhiChiConstant.action_consultingContent_info, ZhiChiConstant
                .action_consultingContent_info, false);

        removeByAction(message, ZhiChiConstant.sobot_outline_leverByManager, ZhiChiConstant.sobot_outline_leverByManager, true);

        removeByAction(message, ZhiChiConstant.action_custom_evaluate, ZhiChiConstant.action_custom_evaluate, true);

        //  转人工后移除点踩后出现的转人工提示语消息
        removeByAction(message, ZhiChiConstant.action_remind_connt_success, ZhiChiConstant
                .action_remind_info_zhuanrengong, false);

        if (message.getAction() != null && message.getAction().equals(ZhiChiConstant.action_remind_past_time)
                && message.getAnswer() != null && ZhiChiConstant.sobot_remind_type_outline == message.getAnswer().getRemindType()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getAction() != null) {
                    if (list.get(i).getAction().equals(ZhiChiConstant.action_remind_past_time) && message.getAnswer() != null
                            && ZhiChiConstant.sobot_remind_type_outline == message.getAnswer().getRemindType()) {
                        list.remove(i);
                        notifyItemRemoved(i);
                        message.setShake(true);
                    }
                }
            }
        }
        justAddData(message);
    }

    public void justAddData(ZhiChiMessageBase message) {
        if (message == null) {
            return;
        }
        if (TextUtils.isEmpty(message.getT())) {
            message.setT(System.currentTimeMillis() + "");
        }
        String lastCid = SharedPreferencesUtil.getStringData(context, "lastCid", "");
        setDefaultCid(lastCid, message);
        if (!TextUtils.isEmpty(message.getMultiGuideStrip())) {
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setT(Calendar.getInstance().getTime().getTime() + "");
            base.setSenderName(TextUtils.isEmpty(message.getSenderName()) ? "" : message.getSenderName());
            base.setSenderFace(TextUtils.isEmpty(message.getSenderFace()) ? "" : message.getSenderFace());
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            reply.setMsgType(ZhiChiConstant.message_type_text);
            base.setSenderType(ZhiChiConstant.message_sender_type_robot);
            reply.setMsg(message.getMultiGuideStrip());
            base.setAnswer(reply);
            addData(base);
        }
        if (message.getAnswers() != null && !message.getAnswers().isEmpty()) {
            for (int i = 0; i < message.getAnswers().size(); i++) {
                ZhiChiMessageBase zhiChiMessageBase = ChatUtils.clone(message);
                if (zhiChiMessageBase != null) {
                    ZhiChiReplyAnswer answer = message.getAnswers().get(i);
                    if (answer != null) {
                        //多消息需要把里边的单消息赋给上级，用于引用
                        zhiChiMessageBase.setMessage(answer.getMessage());
                        //重新设置msgId,用服务的msgId
                        if (!TextUtils.isEmpty(answer.getMsgId())) {
                            zhiChiMessageBase.setMsgId(answer.getMsgId());
                        }
                    }
                    zhiChiMessageBase.setAnswer(answer);
                    if (i != (message.getAnswers().size() - 1)) {
                        //只有最后一个显示顶踩
                        zhiChiMessageBase.setRevaluateState(0);
                        //只有最后一个显示转人工按钮
                        zhiChiMessageBase.setShowTransferBtn(false);
                        //只有最后一个显示关联问题
                        zhiChiMessageBase.setSugguestions(null);
                        zhiChiMessageBase.setListSuggestions(null);
                    }
                    addMsg(zhiChiMessageBase);
                }
            }
        } else {
            addMsg(message);
        }
    }

    private void addMsg(ZhiChiMessageBase message) {
        if (message == null) {
            return;
        }
        try {
            if (!list.isEmpty() && (list.get(list.size() - 1) != null) && !TextUtils.isEmpty(list.get(list.size() - 1).getT())) {
                long previousMsgTime = Long.parseLong(list.get(list.size() - 1).getT());
                String previousMsgSenderName = list.get(list.size() - 1).getSenderName();
                int previousMsgSenderType = list.get(list.size() - 1).getSenderType();
                if (ZhiChiConstant.message_sender_type_system == message.getSenderType() || ZhiChiConstant.message_sender_type_ai_tobot_cai_card == message.getSenderType()) {
                    //系统消息 点踩原因卡片 都不显示头像昵称
                    message.setShowFaceAndNickname(false);
                } else {
                    //相邻两条消息是同一个人发的，并且时间相隔1分钟，不显示头像昵称
                    if (previousMsgTime != 0 &&
                            !TextUtils.isEmpty(message.getT())
                            && ((Long.parseLong(message.getT()) - previousMsgTime) <= (1000 * 60))
                            && !TextUtils.isEmpty(previousMsgSenderName)
                            && previousMsgSenderName.equals(message.getSenderName()) && previousMsgSenderType == message.getSenderType()) {
                        message.setShowFaceAndNickname(false);
                    } else {
                        message.setShowFaceAndNickname(true);
                    }
                }
            }
        } catch (Exception e) {
        }
        list.add(message);
        notifyDataSetChanged();
//        notifyItemInserted(list.size() - 1);
        if (mMsgCallBack != null) {
            mMsgCallBack.checkUnReadMsg();
        }
    }

    /**
     * 删除已有的数据
     *
     * @param message 当前的数据
     * @param when    当前数据类型（action）=when时   才进行删除操作
     * @param element 删除元素类型（action）
     */
    private void removeByAction(ZhiChiMessageBase message, String when, String element, boolean
            isShake) {
        if (list != null && message != null && SobotStringUtils.isNoEmpty(element) && message.getAction() != null && message.getAction().equals(when)) {
            //倒叙判断，然后删
            ListIterator<ZhiChiMessageBase> iterator = list.listIterator(list.size());
            while (iterator.hasPrevious()) {
                ZhiChiMessageBase messageBase = iterator.previous();
                int posttion = iterator.previousIndex();
                if (SobotStringUtils.isNoEmpty(messageBase.getAction()) && element.equals(messageBase.getAction())) {
                    iterator.remove();
                    notifyItemRemoved(posttion);
                    message.setShake(isShake);
                }
            }
        }
    }

    /**
     * 会话结束或者转人工后
     * 删除或者修改大模型机器人回答ui 未点顶踩的隐藏 未提交的点踩原因卡片也删除
     */
    public void removeOrUpdateAIRobotMsg() {
        if (list != null) {
            //倒叙判断，然后删和改
            ListIterator<ZhiChiMessageBase> iterator = list.listIterator(list.size());
            while (iterator.hasPrevious()) {
                ZhiChiMessageBase messageBase = iterator.previous();
                int posttion = iterator.previousIndex();
                if (SobotStringUtils.isNoEmpty(messageBase.getServant()) && "aiagent".equals(messageBase.getServant())) {
                    if (messageBase.getSenderType() == ZhiChiConstant.message_sender_type_ai_tobot_cai_card) {
                        //删除未提交的点踩原因卡片消息
                        iterator.remove();
                        notifyItemRemoved(posttion);
                    } else if (messageBase.getRevaluateState() == 1) {
                        //未点顶踩的隐藏顶踩按钮
                        messageBase.setRevaluateState(0);
                        iterator.set(messageBase);
                        notifyItemChanged(posttion);
                    } else if (messageBase.getAnswer() != null && ZhiChiConstant.message_type_ai_card_msg == messageBase.getAnswer().getMsgType()) {
                        //大模型卡片转人工后按钮不可点
                        messageBase.setSugguestionsFontColor(1);
                        iterator.set(messageBase);
                        notifyItemChanged(posttion);
                    }
                }
            }
        }
    }

    public void addDataBefore(ZhiChiMessageBase message) {
        String lastCid = SharedPreferencesUtil.getStringData(context, "lastCid", "");
        setDefaultCid(lastCid, message);
        list.add(0, message);
        notifyItemInserted(0);
    }

    /**
     * 给没有cid的消息添加默认的cid
     */
    private void setDefaultCid(String lastCid, ZhiChiMessageBase message) {
        ZhiChiReplyAnswer answer = message.getAnswer();
        //没有更多记录的提醒不用添加
        if (!(answer != null && answer.getRemindType() == ZhiChiConstant.sobot_remind_type_nomore)) {
            if (message.getCid() == null) {
                message.setCid(lastCid);
            }
        }
    }

    /**
     * 给没有cid的消息添加默认的cid
     */
    private void setDefaultCid(List<ZhiChiMessageBase> messages) {
        String lastCid = SharedPreferencesUtil.getStringData(context, "lastCid", "");
        for (int i = 0; i < messages.size(); i++) {
            setDefaultCid(lastCid, messages.get(i));
        }
    }

    public void updateMsgInfoById(String id, int senderState, int progressBar, int readStatus) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            if (info.getSendSuccessState() != ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {
                info.setSendSuccessState(senderState);
                info.setProgressBar(progressBar);
            } else {
                info.setReadStatus(readStatus);
            }
            notifyItemByMsgId(id);
        }
    }

    //刷新数据
    private void notifyItemByMsgId(String msgId) {
        int updatePostion = getMsgInfoPosition(msgId);
        if (updatePostion >= 0 && list != null && updatePostion < list.size()) {
            notifyItemChanged(updatePostion, 300);
        }
    }

    public void updateVoiceStatusById(String id, int sendStatus, String duration, String voiceText, int changeState, String message, int readStatus) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setSendSuccessState(sendStatus);
            info.setReadStatus(readStatus);
            if (!TextUtils.isEmpty(duration) && info.getAnswer() != null) {
                info.getAnswer().setDuration(duration);
                info.getAnswer().setVoiceText(voiceText);
                info.getAnswer().setState(changeState);
            }
            if (!TextUtils.isEmpty(message)) {
                info.setMessage(message);
            }
            notifyItemByMsgId(id);
        }
    }

    public void updateDataStateById(String id, ZhiChiMessageBase data) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setSendSuccessState(data.getSendSuccessState());
            info.setSentisive(data.getSentisive());
            info.setSentisiveExplain(data.getSentisiveExplain());
            info.setClickCancleSend(data.isClickCancleSend());
            info.setShowSentisiveSeeAll(data.isShowSentisiveSeeAll());
            info.setDesensitizationWord(data.getDesensitizationWord());
            info.setReadStatus(data.getReadStatus());
            notifyItemByMsgId(id);
        }
    }

    //更新指定msgid的消息
    public void updateMsgDataByMsgId(String id, ZhiChiMessageBase data) {
        if (data == null) {
            return;
        }
        int pos = getMsgInfoPosition(id);
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            list.set(pos, data);
            notifyItemChanged(pos);
        }
    }

    //更新消息对象里边的message数据
    public void updateMessageByMsgId(String id, ZhiChiMessageBase data) {
        ZhiChiMessageBase zhiChiMessageBase = getMsgInfo(id);
        if (zhiChiMessageBase != null && data != null) {
            zhiChiMessageBase.setMessage(data.getMessage());
            notifyItemByMsgId(id);
        }
    }

    public void updateDataById(String id, ZhiChiMessageBase data) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setAnswer(data.getAnswer());
            info.setSenderType(data.getSenderType());
            info.setSendSuccessState(data.getSendSuccessState());
            notifyItemByMsgId(id);
        }
    }

    //删除指定msgid的aiagent消息，同时在该位置添加新的aiagent消息
    public void removeAndAddAIMsgDataByMsgId(String id, final ZhiChiMessageBase data) {
        if (data == null) {
            return;
        }
        final int pos = getMsgInfoPosition(id);
        if (pos >= 0) {
            list.remove(pos);
            notifyItemRemoved(pos);
            list.add(pos, data);
            notifyItemRangeInserted(pos, 1);
        }
    }

    public void updateAIDataById(String id, ZhiChiMessageBase data, boolean isEnd) {
        if (data == null) {
            return;
        }
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            if (StringUtils.isNoEmpty(data.getRobotAnswerMessageType()) && !"SENSITIVE_WORD".equals(data.getRobotAnswerType())) {
                //SENSITIVE_WORD 如果是敏感词，就拼接，直接覆盖显示
                //拼接消息
                String oldDate = info.getContent();
                String newDate = data.getContent();
                String content = oldDate + newDate;
                info.setContent(content);
                ZhiChiReplyAnswer answer = info.getAnswer();
                if (answer != null) {
                    doMarkDownData(content, answer);
                    if (!isEnd && answer.getRichList() != null) {
                        //如果是aiagent 答案 因为没有结束，结尾加个|
                        ChatMessageRichListModel richListModel = new ChatMessageRichListModel();
                        richListModel.setMsg("  |");
                        richListModel.setType(0);
                        answer.getRichList().add(richListModel);
                    }
                    answer.setMsg(content);
                    info.setAnswer(answer);
                }
                info.setRevaluateState(data.getRevaluateState());
                if (isEnd && (answer == null || answer.getRichList() == null || answer.getRichList().isEmpty())) {
                    //空消息 直接删除
                    removeByMsgId(id);
                } else {
                    notifyItemByMsgId(id);
                }
            } else {
                if (isEnd && (StringUtils.isEmpty(data.getContent()))) {
                    //空消息 直接删除
                    removeByMsgId(id);
                } else {
                    //不是拼接消息，直接展示
                    updateMsgDataByMsgId(id, data);
                }
            }
        } else {
            if (data.getAnswer() != null && data.getAnswer().getMsgType() == ZhiChiConstant.message_type_text) {
                doMarkDownData(data.getAnswer().getMsg(), data.getAnswer());
            }
            justAddData(data);
        }
    }

    //处理markdown数据
    private static void doMarkDownData(String content, ZhiChiReplyAnswer answer) {
        if (ChatUtils.isHasPictureInMarkdown(content) && ChatUtils.parseMarkdownToArr(content) != null) {
            String[] temp = ChatUtils.parseMarkdownToArr(content);
            List<ChatMessageRichListModel> richList = new ArrayList<>();
            for (int i = 0; i < temp.length; i++) {
                ChatMessageRichListModel model = new ChatMessageRichListModel();
                if (SobotStringUtils.isNoEmpty(temp[0])) {
                    if (temp[i].startsWith("http")) {
                        //图片
                        model.setMsg(temp[i]);
                        model.setType(1);
                    } else {
                        //文本
                        model.setMsg(ChatUtils.parseMarkdownData(temp[i]));
                        model.setType(0);
                    }
                    richList.add(model);
                }
            }
            answer.setRichList(richList);
            answer.setMsgType(ZhiChiConstant.message_type_emoji);//富文本消息
        } else {
            List<ChatMessageRichListModel> richList = new ArrayList<>();
            ChatMessageRichListModel model = new ChatMessageRichListModel();
            model.setMsg(ChatUtils.parseMarkdownData(content));
            model.setType(0);
            richList.add(model);
            answer.setRichList(richList);
            answer.setMsgType(ZhiChiConstant.message_type_emoji);//富文本消息
        }
    }

    public void updateDataById(String id, SobotRealuateInfo data) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setRealuateInfo(data);
            notifyItemByMsgId(id);
        }
    }

    public void updateDataById(String id, SobotAiRobotRealuateInfo data) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setAiRobotRealuateInfo(data);
            notifyItemByMsgId(id);
        }
    }

    public void updateReadStatus(List<String> messageBases) {
        if (list != null) {
            for (int i = 0; i < messageBases.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j) != null && messageBases.get(i).equals(list.get(j).getMsgId())) {
                        //设置为已读
                        list.get(j).setReadStatus(2);
                        notifyItemChanged(j);
                    }
                }
            }
        }
    }

    public void cancelVoiceUiById(String id) {
        if (list != null) {
            ZhiChiMessageBase info = getMsgInfo(id);
            if (info != null && info.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ANIM) {
                int position = getMsgInfoPosition(id);
                list.remove(position);
                notifyItemRemoved(position);
            }
        }
    }


    //通过msgid 移除该消息
    public void removeByMsgId(String id) {
        if (list != null) {
            ZhiChiMessageBase info = getMsgInfo(id);
            if (info != null) {
                int position = getMsgInfoPosition(id);
                list.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    public void removeCaiNoSubmitMsg() {
        if (list != null) {
            int count = list.size() - 1;
            for (int i = count; i >= 0; i--) {
                if (i > list.size() - 1) {
                    i = list.size() - 1;
                }
                ZhiChiMessageBase msgInfo = list.get(i);
                if (msgInfo == null) {
                    continue;
                }
                //删除未提交的点踩
                if ((msgInfo.getRealuateInfo() != null && msgInfo.getRealuateInfo().getSubmitStatus() == 1) || msgInfo.getSubmitStatus() == 1) {
                    list.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
    }

    public void updatePicStatusById(String id, int sendStatus) {
        ZhiChiMessageBase info = getMsgInfo(id);
        if (info != null) {
            info.setSendSuccessState(sendStatus);
            notifyItemByMsgId(id);
        }
    }

    //通过msgid 找消息对象
    private ZhiChiMessageBase getMsgInfo(String id) {
        if (SobotStringUtils.isEmpty(id)) {
            return null;
        }
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                ZhiChiMessageBase msgInfo = list.get(i);
                if (msgInfo == null) {
                    continue;
                }
                if (msgInfo.getMsgId() != null && msgInfo.getMsgId().equals(id)) {
                    return msgInfo;
                }
                if (msgInfo.getId() != null && msgInfo.getId().equals(id)) {
                    return msgInfo;
                }
            }
        }
        return null;
    }

    //通过msgid 找索引位置
    public int getMsgInfoPosition(String id) {
        if (list == null) {
            return -1;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            ZhiChiMessageBase msgInfo = list.get(i);
            if (msgInfo == null) {
                continue;
            }
            if (msgInfo.getMsgId() != null && msgInfo.getMsgId().equals(id)) {
                return i;
            }
            if (msgInfo.getId() != null && msgInfo.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }


    //检查itemType是否越界后获取layout
    private int getLayoutByItemViewType(int itemType) {
        if (itemType < 0) {
            itemType = 0;
        }
        if (itemType >= layoutRes.length) {
            itemType = 0;
        }
        return layoutRes[itemType];
    }

    //创建消息viewholeer
    private MsgHolderBase createMessageViewHolder(ViewGroup parent, int itemType) {
        MsgHolderBase holder;
        View convertView = LayoutInflater.from(context).inflate(getLayoutByItemViewType(itemType), parent, false);
        switch (itemType) {
            case MSG_TYPE_TXT_L:
            case MSG_TYPE_TXT_R: {
                holder = new TextMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_TXT_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_TIP: {
                holder = new RemindMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_RICH: {
                holder = new RichTextMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_IMG_L:
            case MSG_TYPE_IMG_R: {
                holder = new ImageMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_IMG_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_AUDIO_R: {
                holder = new VoiceMessageHolder(context, convertView);
                holder.setRight(true);
                break;
            }
            case MSG_TYPE_CONSULT: {
                holder = new ConsultMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_CUSTOM_EVALUATE: {
                holder = new CusEvaluateMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_ROBOT_TEMPLATE1: {
                holder = new RobotTemplateMessageHolder1(context, convertView);
                break;
            }
            case MSG_TYPE_ROBOT_TEMPLATE2: {
                holder = new RobotTemplateMessageHolder2(context, convertView);
                break;
            }
            case MSG_TYPE_ROBOT_TEMPLATE3: {
                holder = new RobotTemplateMessageHolder3(context, convertView);
                break;
            }
            case MSG_TYPE_ROBOT_TEMPLATE4:
                holder = new RobotTemplateMessageHolder4(context, convertView);
                break;
            case MSG_TYPE_ROBOT_TEMPLATE5:
                holder = new RobotTemplateMessageHolder5(context, convertView);
                break;
            case MSG_TYPE_ROBOT_TEMPLATE6:
                holder = new RobotTemplateMessageHolder6(context, convertView);
                break;
            case MSG_TYPE_ROBOT_KEYWORD_ITEMS:
                holder = new RobotKeyWordMessageHolder(context, convertView);
                break;
            case MSG_TYPE_ROBOT_SEMANTICS_KEYWORD_ITEMS:
                holder = new RobotSemanticsKeyWordMessageHolder(context, convertView);
                break;
            case MSG_TYPE_ROBOT_ANSWER_ITEMS:
                holder = new RobotAnswerItemsMsgHolder(context, convertView);
                break;
            case MSG_TYPE_MULTI_ROUND_R:
                holder = new SobotChatMsgItemSDKHistoryR(context, convertView);
                break;
            case MSG_TYPE_ROBOT_QUESTION_RECOMMEND:
                holder = new RobotQRMessageHolder(context, convertView);
                break;
            case MSG_TYPE_RETRACTED_MSG:
                holder = new RetractedMessageHolder(context, convertView);
                break;
            case MSG_TYPE_FILE_L:
            case MSG_TYPE_FILE_R: {
                holder = new FileMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_FILE_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_VIDEO_L:
            case MSG_TYPE_VIDEO_R: {
                holder = new VideoMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_VIDEO_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_LOCATION_R: {
                holder = new LocationMessageHolder(context, convertView);
                holder.setRight(true);
                break;
            }
            case MSG_TYPE_NOTICE:
                holder = new NoticeMessageHolder(context, convertView);
                break;
            case MSG_TYPE_CARD_L:
            case MSG_TYPE_CARD_R: {
                holder = new CardMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_CARD_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_ROBOT_ORDERCARD_L:
            case MSG_TYPE_ROBOT_ORDERCARD_R: {
                holder = new OrderCardMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_ROBOT_ORDERCARD_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_FRAUD_PREVENTION: {
                holder = new SystemMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_MUITI_LEAVE_MSG_R: {
                holder = new SobotMuitiLeavemsgMessageHolder(context, convertView);
                holder.setRight(true);
                break;
            }
            case MSG_TYPE_MINIPROGRAM_CARD_L: {
                holder = new MiniProgramMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_HOT_ISSUE: {
                holder = new HotIssueMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_ARTICLE_CARD_L: {
                holder = new ArticleMessageHolder(context, convertView);
                break;
            }
            case MSG_TYPE_CUSTOMER_CARD_R:
            case MSG_TYPE_CUSTOMER_CARD_L: {
                holder = new CustomCardMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_CUSTOMER_CARD_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_AI_CARD_R:
                holder = new AiCardRightMessageHolder(context, convertView);
                holder.setRight(true);
                break;
            case MSG_TYPE_AI_CARD_L: {
                holder = new AiCardMessageHolder(context, convertView);
                holder.setRight(false);
                break;
            }
            case MSG_TYPE_APPOINT_R:
            case MSG_TYPE_APPOINT_L: {
                holder = new AppointTextMessageHolder(context, convertView);
                if (itemType == MSG_TYPE_APPOINT_L) {
                    holder.setRight(false);
                } else {
                    holder.setRight(true);
                }
                break;
            }
            case MSG_TYPE_CAI:
                holder = new CaiReasonMessageHolder(context, convertView);
                break;
            case MSG_TYPE_AI_ROBOT_CAI:
                holder = new AiRobotCaiReasonMessageHolder(context, convertView);
                break;
            case MSG_TYPE_CHANGE_LANGUAE:
                holder = new ChangeLanguaeMessageHolder(context, convertView);
                break;
            case MSG_TYPE_SENSITIVE_AUTHORIZE:
                holder = new SensitiveAuthorizeHolder(context, convertView);
                break;
            case MSG_TYPE_ROBOT_AIAGENT_BUTTON:
                holder = new RobotAiagentButtonMessageHolder(context, convertView);
                break;
            default: {
                holder = new TextMessageHolder(context, convertView);
                break;
            }
        }
        convertView.setTag(holder);
        return holder;
    }

    public int getIdByName(Context context, String className,
                           String resName) {
        context = context.getApplicationContext();
        String packageName = context.getPackageName();
        int indentify = context.getResources().getIdentifier(resName,
                className, packageName);
        return indentify;
    }

    public ZhiChiMessageBase getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    /**
     * 统一的时间提醒
     *
     * @param baseHolder
     * @param position
     */
    public void handerRemindTiem(final MsgHolderBase baseHolder, final int position) {
        boolean isHide = SharedPreferencesUtil.getBooleanData(context, ZhiChiConstant.SOBOT_HIDE_TIMEMSG, false);
        if (isHide) {
            return;
        }
        ZhiChiMessageBase message = list.get(position);

        //时间提醒
        if (baseHolder.reminde_time_Text == null) {
            return;
        }
        VersionUtils.setBackground(null, baseHolder.reminde_time_Text);
        baseHolder.reminde_time_Text.setTextColor(context.getResources()
                .getColor(R.color.sobot_color_remind_text));
        String time = "";

        if (position == 0) {
            ZhiChiReplyAnswer answer = message.getAnswer();
            if (answer != null && answer.getRemindType() == ZhiChiConstant.sobot_remind_type_nomore) {
                baseHolder.reminde_time_Text.setVisibility(View.GONE);
                baseHolder.reminde_time_Text.setPadding(0, 0, 0, 0);
            } else {
                time = getTimeStr(message, position);
                baseHolder.reminde_time_Text.setText(time);
                baseHolder.reminde_time_Text.setVisibility(View.VISIBLE);
                baseHolder.reminde_time_Text.setPadding(0, 0, 0, SobotDensityUtil.dp2px(context, 20));
            }
        } else {
            if (message.getCid() != null && !message.getCid().equals(list.get(position - 1).getCid())) {
                time = getTimeStr(message, position);
                baseHolder.reminde_time_Text.setVisibility(View.VISIBLE);
                baseHolder.reminde_time_Text.setPadding(0, 0, 0, SobotDensityUtil.dp2px(context, 20));
                baseHolder.reminde_time_Text.setText(time);
            } else {
                baseHolder.reminde_time_Text.setVisibility(View.GONE);
            }
        }
    }

    private String getTimeStr(ZhiChiMessageBase tempModel, int position) {
        String stringData = SharedPreferencesUtil.getStringData(context, "lastCid", "");
        tempModel.setTs(TextUtils.isEmpty(tempModel.getTs()) ? (DateUtil.getCurrentDateTime()) : tempModel.getTs());
        String time = "";
        String dataTime = DateUtil.stringToFormatString(tempModel.getTs() + "", "yyyy-MM-dd", ZCSobotApi.getSwitchMarkStatus(MarkConfig.AUTO_MATCH_TIMEZONE));
        String nowTime = DateUtil.getCurrentDate();
        if (tempModel.getCid() != null && tempModel.getCid().equals(stringData) && nowTime.equals(dataTime)) {
            time = DateUtil.formatDateTime(tempModel.getTs(), true, "", ZCSobotApi.getSwitchMarkStatus(MarkConfig.AUTO_MATCH_TIMEZONE));
        } else {
            time = DateUtil.stringToFormatString(list.get(position).getTs() +
                    "", "MM-dd HH:mm", ZCSobotApi.getSwitchMarkStatus(MarkConfig.AUTO_MATCH_TIMEZONE));
        }
        return time;
    }

    /**
     * 删除商品详情
     */
    public void removeConsulting() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAction() != null) {
                if (list.get(i).getAction().equals(ZhiChiConstant.action_consultingContent_info)) {
                    list.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public ZhiChiMessageBase getMsgInfoByMsgId(String msgId) {
        for (int i = list.size() - 1; i >= 0; i--) {
            ZhiChiMessageBase msgInfo = list.get(i);
            if (msgInfo == null) {
                continue;
            }
            if (msgInfo.getMsgId() != null && msgInfo.getMsgId().equals(msgId)) {
                return msgInfo;
            }
        }
        return null;
    }

    public List<ZhiChiMessageBase> getDatas() {
        if (list != null) {
            return list;
        } else {
            list = new ArrayList<>();
            return list;
        }
    }

    public void removeKeyWordTranferItem() {
        try {
            List<ZhiChiMessageBase> listData = getDatas();
            for (int i = listData.size() - 1; i >= 0; i--) {
                if (ZhiChiConstant.message_sender_type_robot_keyword_msg == listData.get(i).getSenderType()) {
                    listData.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.i("error : removeKeyWordTranferItem()");
        }
    }

    public void removeSemanticsKeyWordTranferItem() {
        try {
            List<ZhiChiMessageBase> listData = getDatas();
            for (int i = listData.size() - 1; i >= 0; i--) {
                if (ZhiChiConstant.message_sender_type_robot_semantics_keyword_msg == listData.get(i).getSenderType()) {
                    listData.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.i("error : removeSemanticsKeyWordTranferItem()");
        }
    }

    /**
     * 评价成功后 删除评价
     */
    public void removeEvaluateData() {
        for (int i = list.size() - 1; i >= 0; i--) {
            ZhiChiMessageBase msgInfo = list.get(i);
            if (ZhiChiConstant.message_sender_type_custom_evaluate == msgInfo.getSenderType()) {
                SobotEvaluateModel sobotEvaluateModel = msgInfo.getSobotEvaluateModel();
                if (sobotEvaluateModel != null) {
                    list.remove(msgInfo);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public interface SobotMsgCallBack {
        void sendConsultingContent();

        void doEvaluate(final boolean evaluateFlag, final ZhiChiMessageBase message);

        void sendMessageToRobot(ZhiChiMessageBase base, int type, int questionFlag, String docId);

        void sendMessageToRobot(ZhiChiMessageBase base, int type, int questionFlag, String docId, String multiRoundMsg);

        void doClickTransferBtn(ZhiChiMessageBase base);

        void hidePanelAndKeyboard();

        void doRevaluate(final boolean revaluateFlag, final ZhiChiMessageBase message);

        void clickAudioItem(ZhiChiMessageBase message, ImageView voiceIV, boolean isRight);

        void sendMessage(String content);

        void removeMessageByMsgId(String msgid);

        void addMessage(ZhiChiMessageBase message);

        void clickIssueItem(FaqDocRespVo faq, String tag);

        void mulitDiaToLeaveMsg(String leaveTemplateId, String tipMsgId);

        //机器人模式下，上传视频、文件成功后，发送url 给机器人，只显示答案
        void sendFileToRobot(String msgId, String msgType, String fileUrl);

        void clickCardMenu(SobotChatCustomMenu menu);

        void sendCardMsg(SobotChatCustomMenu menu, SobotChatCustomCard card);

        void checkUnReadMsg();

        void unReadMsgIndex(int unReadMsgIndex);

        void submitCai(String msgId, SobotRealuateInfo realuateInfo);

        void submitAiRobotCai(ZhiChiMessageBase message, SobotAiRobotRealuateInfo realuateInfo);

        void chooseLangaue(SobotlanguaeModel sobotlanguaeModel, ZhiChiMessageBase messageBase);

        void chooseByAllLangaue(ArrayList<SobotlanguaeModel> sobotlanguaeModelList, ZhiChiMessageBase messageBase);
    }
}