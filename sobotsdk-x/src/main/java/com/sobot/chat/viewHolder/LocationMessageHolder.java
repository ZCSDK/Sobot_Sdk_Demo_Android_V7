package com.sobot.chat.viewHolder;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.api.model.SobotLocationModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StMapOpenHelper;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 位置消息
 */
public class LocationMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView st_localName;
    private TextView st_localLabel;
    private SobotRCImageView st_snapshot;
    private ZhiChiMessageBase mMessage;
    private SobotLocationModel mLocationData;

    private int sobot_bg_default_map;
    // 延迟显示 发送中（旋转菊花）效果
    private Runnable loadingRunnable;
    private final Handler handler = new Handler();

    public LocationMessageHolder(Context context, View convertView) {
        super(context, convertView);
        st_localName = (TextView) convertView.findViewById(R.id.st_localName);
        st_localLabel = (TextView) convertView.findViewById(R.id.st_localLabel);
        st_snapshot = (SobotRCImageView) convertView.findViewById(R.id.st_snapshot);
        sobot_msg_content_ll.setOnClickListener(this);
        sobot_bg_default_map = R.drawable.sobot_bg_default_map;
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mMessage = message;
        if (message.getAnswer() != null && message.getAnswer().getLocationData() != null) {
            mLocationData = message.getAnswer().getLocationData();
            st_localName.setText(mLocationData.getLocalName());
            st_localLabel.setText(mLocationData.getLocalLabel());
            SobotBitmapUtil.display(context, mLocationData.getSnapshot(), st_snapshot, sobot_bg_default_map, sobot_bg_default_map);
            if (isRight) {
                refreshUi();
            }
        }
        setLongClickListener(sobot_msg_content_ll);
        refreshReadStatus();
    }

    private void refreshUi() {
        try {
            if (mMessage == null) {
                return;
            }
            if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_SUCCESS) {// 成功的状态
                msgStatus.setVisibility(View.GONE);
                msgProgressBar.setVisibility(View.GONE);
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
            } else if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_ERROR) {
                msgStatus.setVisibility(View.VISIBLE);
                msgProgressBar.setVisibility(View.GONE);
                msgProgressBar.setClickable(true);
                msgStatus.setOnClickListener(this);
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
            } else if (mMessage.getSendSuccessState() == ZhiChiConstant.MSG_SEND_STATUS_LOADING) {
                // 当状态变为成功或失败时，移除延迟任务
                if (handler != null && loadingRunnable != null) {
                    handler.removeCallbacks(loadingRunnable);
                }
                loadingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        msgStatus.setVisibility(View.GONE);
                        msgProgressBar.setVisibility(View.VISIBLE);
                    }
                };
                handler.postDelayed(loadingRunnable, ZCSobotConstant.LOADING_TIME);
            }
        } catch (Exception e) {
            // 当状态变为成功或失败时，移除延迟任务
            if (handler != null && loadingRunnable != null) {
                handler.removeCallbacks(loadingRunnable);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == msgStatus) {
            showReSendDialog(mContext, msgStatus, new ReSendListener() {

                @Override
                public void onReSend() {
                    if (msgCallBack != null && mMessage != null && mMessage.getAnswer() != null) {
                        msgCallBack.sendMessageToRobot(mMessage, 5, 0, null);
                    }
                }
            });
        }

        if (v == sobot_msg_content_ll) {
            if (mLocationData != null) {
                if (SobotOption.mapCardListener != null) {
                    //如果返回true,拦截;false 不拦截
                    boolean isIntercept = SobotOption.mapCardListener.onClickMapCradMsg(mContext, mLocationData);
                    if (isIntercept) {
                        return;
                    }
                }
                StMapOpenHelper.openMap(mContext, mLocationData);
            }
        }
    }
}
