package com.sobot.chat.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.LoadingView.SobotLoadingView;

/**
 * 通用提示操作弹窗
 */

public class SobotTipDialog extends Dialog {
    private TextView sobot_btn_i_know;
    private TextView tv_tip_content;
    private LinearLayout coustom_pop_layout;
    private int screenHeight;

    private MyClickListener myClickListener;
    private String tipContent,btnContent;
    private Context mContext;
    private int linkTextColor;

    /**
     * @param context
     * @param tipContent       提示内容
     */
    public SobotTipDialog(Context context,  String tipContent) {
        super(context, R.style.sobot_noAnimDialogStyle);
        this.mContext = context;
        this.myClickListener = myClickListener;
        this.tipContent = tipContent;
        this.btnContent = btnContent;
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
        linkTextColor = R.color.sobot_color_link;
        if (mContext.getResources().getColor(R.color.sobot_color_link) == mContext.getResources().getColor(R.color.sobot_common_blue)) {
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null && initMode.getVisitorScheme() != null) {
                //服务端返回的气泡中超链接背景颜色
                if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                    linkTextColor = Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sobot_layout_tip_dialog);
        initView();
    }
    public void setmMessage(String mMessage) {
        this.tipContent = mMessage;
        if(StringUtils.isNoEmpty(mMessage)) {
            tv_tip_content.setVisibility(SobotLoadingView.VISIBLE);
            HtmlTools.getInstance(getContext()).setRichText(tv_tip_content,tipContent , linkTextColor);
        }else{
            tv_tip_content.setVisibility(SobotLoadingView.GONE);
        }
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
        sobot_btn_i_know = findViewById(R.id.sobot_btn_i_know);
        tv_tip_content = findViewById(R.id.tv_tip_content);

        if (StringUtils.isNoEmpty(tipContent)) {
            HtmlTools.getInstance(getContext()).setRichText(tv_tip_content,tipContent , linkTextColor);
            tv_tip_content.setVisibility(View.VISIBLE);
        } else {
            tv_tip_content.setVisibility(View.GONE);
        }
        sobot_btn_i_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (myClickListener != null) {
                    myClickListener.clickCancle();
                }
            }
        });
        updateUIByThemeColor();
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
    public void updateUIByThemeColor() {
        if (ThemeUtils.isChangedThemeColor(getContext())) {
            int color = ThemeUtils.getThemeColor(getContext());
            Drawable bg = getContext().getResources().getDrawable(R.drawable.sobot_bg_theme_color_20dp);
            if (bg != null) {
                sobot_btn_i_know.setBackground(ThemeUtils.applyColorToDrawable(bg, color));
            }
            sobot_btn_i_know.setTextColor(ThemeUtils.getThemeTextAndIconColor(getContext()));
        }
    }
}
