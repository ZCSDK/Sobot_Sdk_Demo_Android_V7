package com.sobot.chat.widget.dialog;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.utils.ZhiChiConstant;

/**
 * 右上角清空历史记录弹窗
 */
public class SobotClearHistoryActivity extends SobotDialogBaseActivity implements  View.OnClickListener {
    private Button sobot_btn_take_photo, sobot_btn_cancel;
    private LinearLayout sobot_pop_layout;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_clear_history_dialog;
    }

    @Override
    protected void initView() {
        sobot_btn_take_photo = (Button) findViewById(R.id.sobot_btn_take_photo);
        sobot_btn_cancel = (Button) findViewById(R.id.sobot_btn_cancel);
        sobot_btn_cancel.setText(R.string.sobot_btn_cancle);
        sobot_pop_layout = (LinearLayout) findViewById(R.id.sobot_pop_layout);
        sobot_btn_take_photo.setText(R.string.sobot_clear_history_message);
        sobot_btn_take_photo.setTextColor(getContext().getResources()
                .getColor(R.color.sobot_text_delete_hismsg_color));
        sobot_btn_take_photo.setOnClickListener(this);
        sobot_btn_cancel.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }


    @Override
    public void onClick(View v) {
        if (v == sobot_btn_cancel) {
            finish();
        }else if(v == sobot_btn_take_photo){
            setResult(ZhiChiConstant.REQUEST_COCE_TO_CLEAR_HISTORY);
            finish();
        }
    }


}