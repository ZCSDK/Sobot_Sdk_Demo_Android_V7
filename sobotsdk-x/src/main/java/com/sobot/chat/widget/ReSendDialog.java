package com.sobot.chat.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sobot.chat.R;

/**
 * 自定义退出对话框
 */
public class ReSendDialog extends Dialog {

	private Context content;
	public Button button;
	public Button button2;
	private TextView sobot_message;
	public OnItemClick mOnItemClick = null;
	public ReSendDialog(Context context) {
		super(context, R.style.sobot_noAnimDialogStyle);
		this.content = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sobot_resend_message_dialog);
		sobot_message= (TextView) findViewById(R.id.sobot_message);
		sobot_message.setText(R.string.sobot_resendmsg);
		button = (Button) findViewById(R.id.sobot_negativeButton);
		button.setText(R.string.sobot_button_send);
		button2 = (Button) findViewById(R.id.sobot_positiveButton);
		button2.setText(R.string.sobot_btn_cancle);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnItemClick.OnClick(0);
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnItemClick.OnClick(1);
			}
		});
	}

	public void setOnClickListener(OnItemClick onItemClick) {
		mOnItemClick = onItemClick;
	}

	public interface OnItemClick{
		void OnClick(int type);
	}
}