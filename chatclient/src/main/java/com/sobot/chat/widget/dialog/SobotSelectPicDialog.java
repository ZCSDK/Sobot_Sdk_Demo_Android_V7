package com.sobot.chat.widget.dialog;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.utils.ScreenUtils;

/**
 * Created by jinxl on 2017/4/10.
 */

public class SobotSelectPicDialog extends Dialog {

    private View view_take_photo_split;//拍照分割线
    private TextView btn_take_photo, btn_pick_photo, btn_pick_vedio, btn_cancel;
    private LinearLayout coustom_pop_layout;
    private View.OnClickListener itemsOnClick;
    private Context context;
    private final int screenHeight;

    public SobotSelectPicDialog(Activity context, View.OnClickListener itemsOnClick) {
        super(context, R.style.sobot_clearHistoryDialogStyle);
        this.context = context;
        this.itemsOnClick = itemsOnClick;
        screenHeight = ScreenUtils.getScreenHeight(context);
        // 修改Dialog(Window)的弹出位置
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            //横屏设置dialog全屏
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                setParams(context, layoutParams);
            }
            window.setAttributes(layoutParams);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sobot_take_pic_pop);
        initView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!(event.getX() >= -10 && event.getY() >= -10)
                    || event.getY() <= (screenHeight - coustom_pop_layout.getHeight() - 20)) {//如果点击位置在当前View外部则销毁当前视图,其中10与20为微调距离
                dismiss();
            }
        }
        return true;
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

    private void initView() {
        view_take_photo_split= findViewById(R.id.btn_take_photo);
        btn_take_photo = findViewById(R.id.btn_take_photo);
        btn_take_photo.setText(R.string.sobot_attach_take_pic);
        try {
            Information information=ZCSobotApi.getCurrentInfoSetting(getContext());
            if (information!=null&&information.isHideTicketCameraBtn()){
                btn_take_photo.setVisibility(View.GONE);
                if (view_take_photo_split!=null){
                    view_take_photo_split.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
        }
        btn_pick_photo =findViewById(R.id.btn_pick_photo);
        btn_pick_photo.setText(R.string.sobot_choice_form_picture);
        btn_pick_vedio =findViewById(R.id.btn_pick_vedio);
        btn_pick_vedio.setText(R.string.sobot_choice_form_vedio);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setText(R.string.sobot_btn_cancle);
        coustom_pop_layout = (LinearLayout) findViewById(R.id.pop_layout);


        btn_take_photo.setOnClickListener(itemsOnClick);
        btn_pick_photo.setOnClickListener(itemsOnClick);
        btn_pick_vedio.setOnClickListener(itemsOnClick);
        btn_cancel.setOnClickListener(itemsOnClick);
    }
}
