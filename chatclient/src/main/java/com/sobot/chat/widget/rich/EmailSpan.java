package com.sobot.chat.widget.rich;

import android.app.Activity;
import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.core.app.ShareCompat;

import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SobotOption;

public class EmailSpan extends ClickableSpan {

    private String email;
    private int color;
    private Context context;

    public EmailSpan(Context context, String email, int color) {
        this.email = email;
        this.context = context;
        try {
            this.color = context.getResources().getColor(color);
        } catch (Exception e) {
            this.color = color;
        }
    }

    @Override
    public void onClick(View widget) {
        if (SobotOption.dispatchEmailClick(context, email)) {
            return;
        }
        try {
            ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder
                    .from((Activity) widget.getContext());
            builder.setType("message/rfc822");
            builder.addEmailTo(email);
            builder.setSubject("");
            builder.setChooserTitle("");
            builder.startChooser();
        } catch (Exception e) {
            LogUtils.e("uncaught", e);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
        ds.setUnderlineText(false); // 去掉下划线
    }
}