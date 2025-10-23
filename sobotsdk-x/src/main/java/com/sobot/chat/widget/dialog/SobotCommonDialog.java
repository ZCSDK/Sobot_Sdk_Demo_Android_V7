package com.sobot.chat.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;

/**
 * 通用提示操作弹窗
 */

public class SobotCommonDialog extends Dialog {
    private LinearLayout ll_btn_h, ll_btn_v;
    private Button sobot_btn_cancle_h, sobot_btn_ok_h, sobot_btn_cancle_v, sobot_btn_ok_v;
    private TextView tv_title, tv_tip_content;
    private LinearLayout coustom_pop_layout;
    private int screenHeight;

    private MyClickListener myClickListener;
    private String title, tipContent, okBtnContent, cancleBtnContent;

    /**
     * @param context
     * @param tipContent       标题
     * @param tipContent       提示内容
     * @param okBtnContent     确认按钮文案 =右侧按钮
     * @param cancleBtnContent 取消按钮文案 =左侧文案
     * @param myClickListener  按钮点击回调事件
     */
    public SobotCommonDialog(Context context, String title, String tipContent, String okBtnContent, String cancleBtnContent, MyClickListener myClickListener) {
        super(context, R.style.sobot_noAnimDialogStyle);
        this.myClickListener = myClickListener;
        this.title = title;
        this.tipContent = tipContent;
        this.okBtnContent = okBtnContent;
        this.cancleBtnContent = cancleBtnContent;
        int[] screen = ScreenUtils
                .getScreenWH(getContext());
        if (screen.length < 1) {
            screenHeight = screen[1];
        }

        // 修改Dialog(Window)的弹出位置
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            //横屏设置dialog全屏
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            setParams(context, layoutParams);
            window.setAttributes(layoutParams);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sobot_layout_common_dialog);
        initView();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!(event.getX() >= -10 && event.getY() >= -10)
                    || event.getY() <= (screenHeight - coustom_pop_layout.getHeight() - 20)) {//如果点击位置在当前View外部则销毁当前视图,其中10与20为微调距离
                dismiss();
            }
        }
        return true;
    }

    private void initView() {
        coustom_pop_layout = findViewById(R.id.pop_layout);
        ll_btn_h = findViewById(R.id.ll_btn_h);
        tv_title = findViewById(R.id.tv_title);
        sobot_btn_cancle_h = findViewById(R.id.sobot_btn_cancle_h);
        sobot_btn_ok_h = findViewById(R.id.sobot_btn_ok_h);
        ll_btn_v = findViewById(R.id.ll_btn_v);
        sobot_btn_cancle_v = findViewById(R.id.sobot_btn_cancle_v);
        sobot_btn_ok_v = findViewById(R.id.sobot_btn_ok_v);
        tv_tip_content = findViewById(R.id.tv_tip_content);

        if (StringUtils.isNoEmpty(cancleBtnContent)) {
            sobot_btn_cancle_h.setText(cancleBtnContent);
            sobot_btn_cancle_v.setText(cancleBtnContent);
        }
        if (StringUtils.isNoEmpty(okBtnContent)) {
            sobot_btn_ok_v.setText(okBtnContent);
            sobot_btn_ok_h.setText(okBtnContent);
        }
        if (StringUtils.isNoEmpty(title)) {
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText(title);
        } else {
            tv_title.setVisibility(View.GONE);
        }
        if (StringUtils.isNoEmpty(tipContent)) {
            tv_tip_content.setText(tipContent);
            tv_tip_content.setVisibility(View.VISIBLE);
        } else {
            tv_tip_content.setVisibility(View.GONE);
        }
        if ((StringUtils.calculateTextLines(17, sobot_btn_ok_h.getText().toString(), ScreenUtils.dip2px(getContext(), (float) (270 - 8) / 2), getContext()) > 1) || (StringUtils.calculateTextLines(16, sobot_btn_cancle_h.getText().toString(), ScreenUtils.dip2px(getContext(), (float) (270 - 8) / 2), getContext()) > 1)) {
            ll_btn_h.setVisibility(View.GONE);
            ll_btn_v.setVisibility(View.VISIBLE);
        } else {
            ll_btn_h.setVisibility(View.VISIBLE);
            ll_btn_v.setVisibility(View.GONE);
        }
        sobot_btn_cancle_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myClickListener != null) {
                    myClickListener.clickCancle();
                }
            }
        });
        sobot_btn_ok_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myClickListener != null) {
                    myClickListener.clickOk();
                }
            }
        });
        sobot_btn_cancle_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myClickListener != null) {
                    myClickListener.clickCancle();
                }
            }
        });
        sobot_btn_ok_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myClickListener != null) {
                    myClickListener.clickOk();
                }
            }
        });
//        if(ThemeUtils.isChangedThemeColor(mContext)){
//            int themeColor = ThemeUtils.getThemeColor(mContext);
//            sobot_btn_cancle_h.setTextColor(themeColor);
//            sobot_btn_ok_h.setTextColor(themeColor);
//        }
    }

    private void setParams(Context context, WindowManager.LayoutParams lay) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        Rect rect = new Rect();
        View view = getWindow().getDecorView();
        view.getWindowVisibleDisplayFrame(rect);
        lay.width = dm.widthPixels;
    }

    //自定义点击事件
    public interface MyClickListener {
        void clickOk();

        void clickCancle();
    }
}
