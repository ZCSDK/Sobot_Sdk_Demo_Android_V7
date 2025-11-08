package com.sobot.chat.viewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.MiniProgramModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 小程序卡片
 */
public class MiniProgramMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private View sobot_rl_mini;
    private ImageView tv_mimi_logo;
    private TextView tv_mimi_des;
    private TextView tv_mimi_title;
    private ImageView tv_mimi_thumbUrl;
    private MiniProgramModel miniProgramModel;

    public MiniProgramMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_rl_mini = convertView.findViewById(R.id.sobot_rl_mini);
        tv_mimi_logo = convertView.findViewById(R.id.tv_mimi_logo);
        tv_mimi_des = convertView.findViewById(R.id.tv_mimi_des);
        tv_mimi_title = convertView.findViewById(R.id.tv_mimi_title);
        tv_mimi_thumbUrl =  convertView.findViewById(R.id.tv_mimi_thumbUrl);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        miniProgramModel = message.getMiniProgramModel();
        if (miniProgramModel != null) {
            if (!TextUtils.isEmpty(miniProgramModel.getLogo())) {
                SobotBitmapUtil.display(mContext, miniProgramModel.getLogo(), tv_mimi_logo);
                tv_mimi_logo.setVisibility(View.VISIBLE);
            } else {
                tv_mimi_logo.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(miniProgramModel.getDescribe())) {
                tv_mimi_des.setText(miniProgramModel.getDescribe());
                tv_mimi_des.setVisibility(View.VISIBLE);
            } else {
                tv_mimi_des.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(miniProgramModel.getTitle())) {
                tv_mimi_title.setText(miniProgramModel.getTitle());
                tv_mimi_title.setVisibility(View.VISIBLE);
            } else {
                tv_mimi_title.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(miniProgramModel.getThumbUrl())) {
                SobotBitmapUtil.display(mContext, miniProgramModel.getThumbUrl(), tv_mimi_thumbUrl);
                tv_mimi_thumbUrl.setVisibility(View.VISIBLE);
            } else {
                tv_mimi_thumbUrl.setVisibility(View.GONE);
            }
        }
        resetMaxWidth();
        sobot_rl_mini.setOnClickListener(this);
        if (!isRight()) {
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding((int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing));
                }
            } else {
                hideAnswers();
                if (sobot_msg_content_ll != null) {
                    //图片、视频、文件、小程序根据关联问题数量动态判断气泡内间距
                    sobot_msg_content_ll.setPadding((int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing), (int) mContext.getResources().getDimension(R.dimen.sobot_msg_frame_msgtype_spacing));
                }
            }
        }
        setLongClickListener(sobot_rl_mini);
        refreshReadStatus();
    }


    @Override
    public void onClick(View v) {
        if (v == sobot_rl_mini && miniProgramModel != null) {
            if (SobotOption.miniProgramClickListener != null) {
                SobotOption.miniProgramClickListener.onClick(mContext, miniProgramModel);
            } else {
                ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_mini_program_only_open_by_weixin));
            }
        }
    }
}
