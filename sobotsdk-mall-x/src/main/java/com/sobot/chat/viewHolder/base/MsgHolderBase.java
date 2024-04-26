package com.sobot.chat.viewHolder.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.Suggestions;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.ReSendDialog;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * view基类
 */
public abstract class MsgHolderBase {

    public Context mContext;
    //左侧右侧气泡 标识 默认false左侧
    public boolean isRight = false;

    //气泡父控件
    public View mItemView;
    // 用户姓名
    public TextView name;
    // 头像
    public SobotRCImageView imgHead;
    //时间提醒
    public TextView reminde_time_Text;

    //左侧 转人工、顶踩控件
    public LinearLayout sobot_chat_more_action;//包含以下所有控件
    private LinearLayout sobot_ll_transferBtn;//转人工按钮
    public TextView sobot_tv_transferBtn;//机器人转人工按钮
    public ImageView sobot_likeBtn_tv;//机器人评价 顶 的按钮
    public ImageView sobot_dislikeBtn_tv;//机器人评价 踩 的按钮
    public RelativeLayout rightEmptyRL;//左侧消息右边的空白区域
    //底部顶踩显示
    protected LinearLayout sobot_ll_bottom_likeBtn;
    protected LinearLayout sobot_ll_bottom_dislikeBtn;
    protected TextView sobot_tv_bottom_likeBtn;//气泡下边 机器人评价 顶 的按钮
    protected TextView sobot_tv_bottom_dislikeBtn;//气泡下边 机器人评价 踩 的按钮
    public TextView stripe;//关联问题提示语
    public LinearLayout answersList;//关联问题


    //右侧 发送消息状态控件 发送中（菊花转）、发送失败（红色叹号，点击重新发送）
    public ImageView msgStatus;// 消息发送的状态
    public TextView msgReadStatus;// 消息已读未读状态
    public LinearLayout ll_status;// 状态布局
    public ProgressBar msgProgressBar; // 重新发送的进度条的信信息；

    //  气泡内容显示区  
    public View sobot_msg_content_ll;

    //消息体
    public ZhiChiMessageBase message;
    public ZhiChiInitModeBase initMode;
    public Information information;
    //回调事件
    public SobotMsgAdapter.SobotMsgCallBack msgCallBack;

    public RelativeLayout sobot_rl_hollow_container;//文件类型的气泡
    public LinearLayout sobot_ll_hollow_container;//文件类型的气泡
    public int sobot_chat_file_bgColor;//文件类型的气泡默认颜色

    public int msgMaxWidth;//气泡里边的内容最大宽度

    //接口返回是否显示头像
    private boolean isShowFace = true;
    private boolean isShowNickName = true;

    //气泡里边卡片宽度，默认240
    public int msgCardWidth = 240;

    public MsgHolderBase(Context context, View convertView) {
        mItemView = convertView;
        mContext = context;
        initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_initModel);
        information = (Information) SharedPreferencesUtil.getObject(context,
                ZhiChiConstant.sobot_last_current_info);
        reminde_time_Text = (TextView) convertView.findViewById(R.id.sobot_reminde_time_Text);
        imgHead = (SobotRCImageView) convertView.findViewById(R.id.sobot_msg_face_iv);
        name = (TextView) convertView.findViewById(R.id.sobot_msg_nike_name_tv);

        sobot_chat_more_action = (LinearLayout) convertView.findViewById(R.id.sobot_chat_more_action);
        sobot_ll_transferBtn = (LinearLayout) convertView.findViewById(R.id.sobot_ll_transferBtn);
        sobot_tv_transferBtn = convertView.findViewById(R.id.sobot_tv_transferBtn);
        if (!isRight() && sobot_tv_transferBtn != null) {
            sobot_tv_transferBtn.setText(R.string.sobot_transfer_to_customer_service);
        }
        rightEmptyRL = (RelativeLayout) convertView.findViewById(R.id.sobot_left_msg_right_empty_rl);
        sobot_likeBtn_tv = convertView.findViewById(R.id.sobot_likeBtn_tv);
        sobot_dislikeBtn_tv = convertView.findViewById(R.id.sobot_dislikeBtn_tv);

        sobot_ll_bottom_likeBtn = convertView.findViewById(R.id.sobot_ll_bottom_likeBtn);
        sobot_ll_bottom_dislikeBtn = convertView.findViewById(R.id.sobot_ll_bottom_dislikeBtn);
        sobot_tv_bottom_likeBtn = convertView.findViewById(R.id.sobot_tv_bottom_likeBtn);
        sobot_tv_bottom_dislikeBtn = convertView.findViewById(R.id.sobot_tv_bottom_dislikeBtn);

        stripe = convertView
                .findViewById(R.id.sobot_stripe);
        answersList = (LinearLayout) convertView
                .findViewById(R.id.sobot_answersList);

        msgProgressBar = (ProgressBar) convertView.findViewById(R.id.sobot_msgProgressBar);// 重新发送的进度条信息
        // 消息的状态
        msgStatus = (ImageView) convertView.findViewById(R.id.sobot_msgStatus);
        msgReadStatus = convertView.findViewById(R.id.sobot_msg_read_status);
        ll_status = convertView.findViewById(R.id.ll_status);

        sobot_msg_content_ll = convertView.findViewById(R.id.sobot_msg_content_ll);

        sobot_rl_hollow_container = (RelativeLayout) convertView.findViewById(R.id.sobot_rl_hollow_container);

        sobot_chat_file_bgColor = R.color.sobot_chat_file_bgColor;
        applyCustomHeadUI();
        if (initMode != null && initMode.getVisitorScheme() != null) {
            if (imgHead != null) {
                if (initMode.getVisitorScheme().getShowFace() == 1) {
                    isShowFace = true;
                    //显示头像
                    imgHead.setVisibility(View.VISIBLE);
                } else {
                    isShowFace = false;
                    //隐藏头像
                    imgHead.setVisibility(View.GONE);
                }
            }
            if (name != null) {
                if (initMode.getVisitorScheme().getShowStaffNick() == 1) {
                    isShowNickName = true;
                    //显示昵称
                    name.setVisibility(View.VISIBLE);
                } else {
                    isShowNickName = false;
                    //隐藏昵称
                    name.setVisibility(View.GONE);
                }
            }
        } else {
            if (imgHead != null) {
                isShowFace = true;
                //显示头像
                imgHead.setVisibility(View.VISIBLE);
            }
            if (name != null) {
                isShowNickName = true;
                //显示昵称
                name.setVisibility(View.VISIBLE);
            }
        }
        if (isRight()) {
            if (information != null && information.isShowRightMsgFace()) {
                //带有客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) * 60 / 100;
                msgCardWidth = 240 - 30;
            } else {
                //不带客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) * 70 / 100;
                msgCardWidth = 240;
            }
        } else {
            if (isShowFace) {
                //带有客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) * 60 / 100;
                msgCardWidth = 240 - 30;
            } else {
                //不带客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth((Activity) mContext) * 70 / 100;
                msgCardWidth = 240;
            }
        }
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
                msgReadStatus.setVisibility(View.VISIBLE);
//            0-未标记，1-未读，2-已读
                if (message.getReadStatus() == 1) {
                    msgReadStatus.setText(R.string.sobot_msg_no_read);
                    msgReadStatus.setTextColor(ThemeUtils.getThemeColor(mContext));
                } else if (message.getReadStatus() == 2) {
                    msgReadStatus.setText(R.string.sobot_msg_read);
                    msgReadStatus.setTextColor(mContext.getResources().getColor(R.color.sobot_common_gray3));
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
            name.setMaxWidth(msgMaxWidth + ScreenUtils.dip2px(mContext, 36));
            if (isRight()) {
                if (information != null && information.isShowRightMsgNickName()) {
                    name.setVisibility(View.VISIBLE);
                    if (message != null && name != null) {
                        if (!TextUtils.isEmpty(message.getSenderName())) {
                            name.setText(message.getSenderName());
                        }
                    }
                } else {
                    name.setVisibility(View.GONE);
                }
                if (information != null && information.isShowRightMsgFace()) {
                    imgHead.setVisibility(View.VISIBLE);
                    if (message != null && imgHead != null) {
                        SobotBitmapUtil.display(mContext, CommonUtils.encode(message.getSenderFace()),
                                imgHead, R.drawable.sobot_default_pic, R.drawable.sobot_default_pic);
                    }
                } else {
                    imgHead.setVisibility(View.GONE);
                }
                //是否是 文件、商品卡片、订单卡片、文件类型的气泡 线框气泡
                boolean isWireframeMsgType = itemType == SobotMsgAdapter.MSG_TYPE_FILE_R || itemType == SobotMsgAdapter.MSG_TYPE_CARD_R || itemType == SobotMsgAdapter.MSG_TYPE_ROBOT_ORDERCARD_R || itemType == SobotMsgAdapter.MSG_TYPE_LOCATION_R || itemType == SobotMsgAdapter.MSG_TYPE_MUITI_LEAVE_MSG_R || itemType == SobotMsgAdapter.MSG_TYPE_IMG_R || itemType == SobotMsgAdapter.MSG_TYPE_VIDEO_R || itemType == SobotMsgAdapter.MSG_TYPE_CUSTOMER_CARD_R;
                if (mContext.getResources().getColor(R.color.sobot_gradient_end) == mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_end)) {
                    if (initMode != null && initMode.getVisitorScheme() != null) {
                        //服务端返回的导航条背景颜色
                        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
                            String themeColor[] = initMode.getVisitorScheme().getRebotTheme().split(",");
                            if (themeColor.length > 1) {
                                if (mContext.getResources().getColor(R.color.sobot_gradient_start) != Color.parseColor(themeColor[0]) || mContext.getResources().getColor(R.color.sobot_gradient_end) != Color.parseColor(themeColor[1])) {
                                    int[] colors = new int[themeColor.length];
                                    if (isWireframeMsgType) {
                                        GradientDrawable gradientDrawable = new GradientDrawable();
                                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                                        gradientDrawable.setStroke(mContext.getResources().getDimensionPixelOffset(R.dimen.sobot_right_msg_line_width), Color.parseColor(themeColor[colors.length - 1]));//线的宽度 与 线的颜色
                                        gradientDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                                        if (sobot_msg_content_ll != null) {
                                            sobot_msg_content_ll.setBackground(gradientDrawable);
                                        }
                                    } else {
                                        for (int i = 0; i < themeColor.length; i++) {
                                            colors[i] = Color.parseColor(themeColor[i]);
                                        }
                                        GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                                        aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                                        if (sobot_msg_content_ll != null) {
                                            sobot_msg_content_ll.setBackground(aDrawable);
                                        }
                                    }
                                } else {
                                    setRightMsgDefaulBg(isWireframeMsgType);
                                }
                            }
                        }
                    } else {
                        setRightMsgDefaulBg(isWireframeMsgType);
                    }
                } else {
                    setRightMsgDefaulBg(isWireframeMsgType);
                }
            } else {
                if (message != null && name != null) {
                    if (StringUtils.isNoEmpty(message.getSenderName())) {
                        name.setText(message.getSenderName());
                    }
                    //后端返回的昵称需要显示
                    if (isShowNickName) {
                        name.setVisibility(View.VISIBLE);
                    } else {
                        name.setVisibility(View.GONE);
                    }
                    //相邻两条消息同一个人，1分钟内不显示
                    if (!message.isShowFaceAndNickname()) {
                        name.setVisibility(View.GONE);
                    }
                }
                if (message != null && imgHead != null) {
                    if (StringUtils.isNoEmpty(message.getSenderFace())) {
                        SobotBitmapUtil.display(mContext, CommonUtils.encode(message.getSenderFace()),
                                imgHead, R.drawable.sobot_def_admin, R.drawable.sobot_def_admin);
                    }
                    //后端返回的头像需要显示
                    if (isShowFace) {
                        imgHead.setVisibility(View.VISIBLE);
                    } else {
                        imgHead.setVisibility(View.GONE);
                    }
                    //相邻两条消息同一个人，1分钟内不显示
                    if (!message.isShowFaceAndNickname()) {
                        //占位
                        SobotBitmapUtil.display(mContext, "",
                                imgHead);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    //设置右侧气泡默认背景色（不使用文件、商品卡片、订单卡片、文件类型的气泡）
    private void setRightMsgDefaulBg(boolean isWireframeMsgType) {
        if (!isWireframeMsgType) {
            int[] colors = new int[]{mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_start), mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_end)};
            GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
            if (sobot_msg_content_ll != null) {
                sobot_msg_content_ll.setBackground(aDrawable);
            }
        } else {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setStroke(mContext.getResources().getDimensionPixelOffset(R.dimen.sobot_right_msg_line_width), mContext.getResources().getColor(R.color.sobot_chat_file_bgColor));//线的宽度 与 线的颜色
            gradientDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
            if (sobot_msg_content_ll != null) {
                sobot_msg_content_ll.setBackground(gradientDrawable);
            }
        }
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
     *
     * @param msgStatus
     * @param reSendListener
     */
    public static void showReSendDialog(Context context, final ImageView msgStatus, final ReSendListener reSendListener) {
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
        if (imgHead != null) {
//            imgHead.setCornerRadius(4);
            imgHead.setRoundAsCircle(true);
        }
    }

    public String processPrefix(final ZhiChiMessageBase message, int num) {
        if (message != null && message.getAnswer() != null && message.getAnswer().getMultiDiaRespInfo() != null
                && message.getAnswer().getMultiDiaRespInfo().getIcLists() != null) {
            return "•";
        }
        return num + ".";
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
                    msgCallBack.doClickTransferBtn(message);
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
        if (sobot_likeBtn_tv == null ||
                sobot_dislikeBtn_tv == null) {
            return;
        }
        if (isRight()) {
            return;
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
            sobot_likeBtn_tv.setVisibility(View.VISIBLE);
            sobot_dislikeBtn_tv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
            if (sobot_tv_bottom_likeBtn != null) {
                sobot_tv_bottom_likeBtn.setVisibility(View.GONE);
                sobot_tv_bottom_dislikeBtn.setVisibility(View.GONE);
                sobot_ll_bottom_likeBtn.setVisibility(View.GONE);
                sobot_ll_bottom_dislikeBtn.setVisibility(View.GONE);
            }
        } else {
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            if (sobot_tv_bottom_likeBtn != null) {
                sobot_tv_bottom_likeBtn.setVisibility(View.VISIBLE);
                sobot_tv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
                sobot_ll_bottom_likeBtn.setVisibility(View.VISIBLE);
                sobot_ll_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            }
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
        }

        sobot_likeBtn_tv.setEnabled(true);
        sobot_dislikeBtn_tv.setEnabled(true);
        sobot_likeBtn_tv.setSelected(false);
        sobot_dislikeBtn_tv.setSelected(false);
        sobot_likeBtn_tv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doRevaluate(true);
            }
        });
        sobot_dislikeBtn_tv.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                doRevaluate(false);
            }
        });
        if (sobot_tv_bottom_likeBtn != null) {
            sobot_tv_bottom_likeBtn.setEnabled(true);
            sobot_tv_bottom_dislikeBtn.setEnabled(true);
            sobot_tv_bottom_likeBtn.setSelected(false);
            sobot_tv_bottom_dislikeBtn.setSelected(false);
            sobot_tv_bottom_likeBtn.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(true);
                }
            });
            sobot_tv_bottom_dislikeBtn.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(false);
                }
            });
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

    /**
     * 隐藏 顶踩 按钮
     */
    public void hideRevaluateBtn() {
        hideContainer();
        sobot_likeBtn_tv.setVisibility(View.GONE);
        sobot_dislikeBtn_tv.setVisibility(View.GONE);
        rightEmptyRL.setVisibility(View.GONE);
        if (sobot_ll_bottom_likeBtn != null) {
            sobot_tv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_likeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 显示顶之后的view
     */
    public void showLikeWordView() {
        if (dingcaiIsShowRight()) {
            sobot_likeBtn_tv.setSelected(true);
            sobot_likeBtn_tv.setEnabled(false);
            sobot_dislikeBtn_tv.setEnabled(false);
            sobot_dislikeBtn_tv.setSelected(false);
            sobot_likeBtn_tv.setVisibility(View.VISIBLE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
            rightEmptyRL.setVisibility(View.VISIBLE);
        } else {
            sobot_tv_bottom_likeBtn.setSelected(true);
            sobot_tv_bottom_likeBtn.setEnabled(false);
            sobot_tv_bottom_dislikeBtn.setEnabled(false);
            sobot_tv_bottom_dislikeBtn.setSelected(false);
            sobot_tv_bottom_likeBtn.setVisibility(View.VISIBLE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_likeBtn.setVisibility(View.VISIBLE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
        }
    }

    /**
     * 显示踩之后的view
     */
    public void showDislikeWordView() {
        if (dingcaiIsShowRight()) {
            sobot_dislikeBtn_tv.setSelected(true);
            sobot_dislikeBtn_tv.setEnabled(false);
            sobot_likeBtn_tv.setEnabled(false);
            sobot_likeBtn_tv.setSelected(false);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
        } else {
            sobot_tv_bottom_dislikeBtn.setSelected(true);
            sobot_tv_bottom_dislikeBtn.setEnabled(false);
            sobot_tv_bottom_likeBtn.setEnabled(false);
            sobot_tv_bottom_likeBtn.setSelected(false);
            sobot_tv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            sobot_ll_bottom_likeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
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
                resetMaxWidth(stripe);
            }
            if (answersList != null) {
                if (message.getListSuggestions() != null && message.getListSuggestions().size() > 0) {
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
                        int currentItem = i + 1;
                        String tempStr = processPrefix(message, currentItem) + answerStringList[i];
                        answer.setText(tempStr);
                        answersList.addView(answer);
                    }
                }
                resetMaxWidth(answersList);
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
        if (sobot_msg_content_ll != null) {
            if (sobot_msg_content_ll.getParent() instanceof LinearLayout) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) sobot_msg_content_ll.getLayoutParams();
                layoutParams.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                sobot_msg_content_ll.setLayoutParams(layoutParams);
            } else if (sobot_msg_content_ll.getParent() instanceof RelativeLayout) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) sobot_msg_content_ll.getLayoutParams();
                layoutParams.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                sobot_msg_content_ll.setLayoutParams(layoutParams);
            } else if (sobot_msg_content_ll.getParent() instanceof FrameLayout) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) sobot_msg_content_ll.getLayoutParams();
                layoutParams.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                sobot_msg_content_ll.setLayoutParams(layoutParams);
            } else if (sobot_msg_content_ll.getParent() instanceof ViewGroup) {
                ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) sobot_msg_content_ll.getLayoutParams();
                layoutParams.width = msgMaxWidth + ScreenUtils.dip2px(mContext, 36);
                sobot_msg_content_ll.setLayoutParams(layoutParams);
            }
        }
    }

    //气泡里边控件最大宽度
    public void resetMaxWidth(View view) {
        if (view != null) {
            if (view.getParent() instanceof LinearLayout) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                layoutParams.width = msgMaxWidth;
                view.setLayoutParams(layoutParams);
            } else if (view.getParent() instanceof RelativeLayout) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.width = msgMaxWidth;
                view.setLayoutParams(layoutParams);
            } else if (view.getParent() instanceof FrameLayout) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                layoutParams.width = msgMaxWidth;
                view.setLayoutParams(layoutParams);
            } else if (view.getParent() instanceof ViewGroup) {
                ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) view.getLayoutParams();
                layoutParams.width = msgMaxWidth;
                view.setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * 显示复制和引用的提示
     *
     * @param context
     * @param v
     * @param str
     * @param x
     * @param y
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
            mPopView.findViewById(R.id.sobot_tv_appoint_txt).setVisibility(View.GONE);
            mPopView.findViewById(R.id.view_split).setVisibility(View.GONE);
        } else {
            if (answersList != null && ((message.getListSuggestions() != null && message.getListSuggestions().size() > 0) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
                //只要带有关联问题都不能引用
                mPopView.findViewById(R.id.sobot_tv_appoint_txt).setVisibility(View.GONE);
                mPopView.findViewById(R.id.view_split).setVisibility(View.GONE);
            } else {
                mPopView.findViewById(R.id.sobot_tv_appoint_txt).setVisibility(View.VISIBLE);
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
        mPopView.findViewById(R.id.sobot_tv_copy_txt).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= 11) {
                            LogUtils.i("API是大于11");
                            android.content.ClipboardManager cmb = (android.content.ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cmb != null) {
                                cmb.setText(str);
                            }
                        } else {
                            LogUtils.i("API是小于11");
                            android.text.ClipboardManager cmb = (android.text.ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cmb != null) {
                                cmb.setText(str);
                            }
                        }
                        ToastUtil.showCustomToast(context, context.getResources().getString(R.string.sobot_ctrl_v_success), R.drawable.sobot_iv_login_right);
                        mPopWindow.dismiss();
                    }
                });
        mPopView.findViewById(R.id.sobot_tv_appoint_txt).setOnClickListener(
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
        if (answersList != null && ((message.getListSuggestions() != null && message.getListSuggestions().size() > 0) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
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
     *
     * @param context
     * @param v
     * @param x
     * @param y
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
        if (answersList != null && ((message.getListSuggestions() != null && message.getListSuggestions().size() > 0) || (message.getSugguestions() != null && message.getSugguestions().length > 0))) {
            //只要带有关联问题都不能引用
            return;
        }
        /** pop view */
        View mPopView = LayoutInflater.from(context).inflate(R.layout.sobot_pop_chat_room_long_press, null);
        mPopView.findViewById(R.id.sobot_tv_copy_txt).setVisibility(View.GONE);
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
        mPopView.findViewById(R.id.sobot_tv_appoint_txt).setOnClickListener(
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
        } else if (ZhiChiConstant.message_sender_type_customer_sendImage == message.getSenderType()) {
            appointMessage.setAppointType(1);
        } else if (ZhiChiConstant.message_sender_type_send_voice == message.getSenderType()) {
            appointMessage.setAppointType(1);
        } else {
            appointMessage.setAppointType(1);
        }

        JSONObject messageJsonObject = null;
        try {
            messageJsonObject = new JSONObject(message.getMessage());
            if (messageJsonObject != null && messageJsonObject.has("msgType") && !TextUtils.isEmpty(messageJsonObject.optString("msgType"))) {
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
        if (initMode != null && initMode.getRealuateStyle() == 1) {
            return false;
        }
        return true;
    }
}