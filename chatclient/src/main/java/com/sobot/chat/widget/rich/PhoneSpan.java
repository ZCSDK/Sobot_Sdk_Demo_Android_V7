package com.sobot.chat.widget.rich;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.SobotOption;

public class PhoneSpan extends ClickableSpan {

    private String phone;
    private int color;
    private Context context;

    public PhoneSpan(Context context, String phone, int color) {
        this.phone = phone;
        if (isColorResource(color)) {
            this.color = context.getResources().getColor(color);
        } else {
            this.color = color;
        }
        this.context = context;
    }

    /**
     * 区分 Android 资源 ID 与服务端下发的 ARGB 色值，避免把颜色值当资源查询。
     */
    private static boolean isColorResource(int color) {
        int packageId = color >>> 24;
        return packageId == 0x01 || packageId == 0x7f;
    }

    @Override
    public void onClick(View widget) {
        if (SobotOption.dispatchPhoneClick(context, "tel:" + phone)) {
            return;
        }
        ChatUtils.callUp(phone, context);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
        ds.setUnderlineText(false); // 去掉下划线
    }
}
