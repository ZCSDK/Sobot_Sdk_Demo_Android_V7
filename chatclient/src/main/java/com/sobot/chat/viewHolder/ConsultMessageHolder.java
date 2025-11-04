package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.image.SobotProgressImageView;

/**
 * 商品咨询项目
 */
public class ConsultMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView tv_title;//   商品标题页title    商品描述  商品图片   发送按钮  商品标签
    private SobotProgressImageView iv_pic;
    private TextView btn_sendBtn;
    private View sobot_container;
    private TextView tv_lable;
    private TextView tv_des;
    private ZhiChiMessageBase mData;

    public ConsultMessageHolder(Context context, View convertView) {
        super(context, convertView);
        btn_sendBtn = convertView.findViewById(R.id.sobot_goods_sendBtn);
        try {
            Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_btn_bg_16, null);
            if (drawable != null) {
                btn_sendBtn.setBackground(ThemeUtils.applyColorToDrawable(drawable, ThemeUtils.getThemeColor(mContext)));
            }
        } catch (Resources.NotFoundException e) {
        }
        if (CommonUtils.checkSDKIsZh(mContext)) {
            btn_sendBtn.setText(R.string.sobot_send_cus_service);
        } else {
            btn_sendBtn.setText(R.string.sobot_button_send);
        }
        btn_sendBtn.setTextColor(ThemeUtils.getThemeTextAndIconColor(context));
        sobot_container = convertView.findViewById(R.id.sobot_container);
        iv_pic = (SobotProgressImageView) convertView.findViewById(R.id.sobot_goods_pic);
        tv_title = (TextView) convertView.findViewById(R.id.sobot_goods_title);
        tv_lable = (TextView) convertView.findViewById(R.id.sobot_goods_label);
        tv_des = (TextView) convertView.findViewById(R.id.sobot_goods_des);
        sobot_container.setOnClickListener(this);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        mData = message;
        String title = message.getContent();
        String picurl = message.getPicurl();
        final String url = message.getUrl();
        String lable = message.getAname();
        String describe = message.getReceiverFace();
        if (!TextUtils.isEmpty(picurl)) {
            iv_pic.setVisibility(View.VISIBLE);
            iv_pic.setImageUrl(CommonUtils.encode(picurl));
        } else {
            iv_pic.setVisibility(View.GONE);
        }

        tv_des.setText(describe);
        tv_title.setText(title);

        if (!TextUtils.isEmpty(lable)) {
            tv_lable.setVisibility(View.VISIBLE);
            tv_lable.setText(lable);
        } else {
            if (!TextUtils.isEmpty(picurl)) {
                tv_lable.setVisibility(View.INVISIBLE);
            } else {
                tv_lable.setVisibility(View.GONE);
            }
        }
        btn_sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i("发送连接---->" + url);
                if (msgCallBack != null) {
                    msgCallBack.sendConsultingContent();
                }
            }
        });
        refreshReadStatus();
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_container && mData != null && !TextUtils.isEmpty(mData.getUrl())) {
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(mData.getUrl());
                return;
            }
            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, mData.getUrl());
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mContext, WebViewActivity.class);
            intent.putExtra("url", mData.getUrl());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
}
