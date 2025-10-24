package com.sobot.chat.widget.toast;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sobot.chat.R;

/**
 * 自定义时长的Toast
 */
public class CustomToast {

    public static Toast makeText(Context context, CharSequence text,
                                 int duration) {
        if (context == null) {
            return null;
        }
        Toast toast = new Toast(context.getApplicationContext());
        View view = View.inflate(context, R.layout.sobot_custom_toast_layout_2, null);
        TextView tv = (TextView) view.findViewById(R.id.sobot_tv_content);
        tv.setText(text);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        return toast;
    }

    public static Toast makeText(Context context, CharSequence text,
                                 int duration, int resId) {
        if (context == null) {
            return null;
        }
        Toast toast = new Toast(context.getApplicationContext());
        View view = View.inflate(context, R.layout.sobot_custom_toast_layout, null);
        TextView tv = (TextView) view.findViewById(R.id.sobot_tv_content);
        tv.setText(text);
        ImageView tv_content = (ImageView) view.findViewById(R.id.sobot_iv_content);
        tv_content.setImageResource(resId);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        return toast;
    }
}