package com.sobot.chat.viewHolder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SatisfactionSetBase;
import com.sobot.chat.api.model.SobotRealuateConfigInfo;
import com.sobot.chat.api.model.SobotRealuateInfo;
import com.sobot.chat.api.model.SobotRealuateTagInfoList;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.notchlib.utils.ScreenUtil;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotAntoLineEquidistanceLayout;
import com.sobot.chat.widget.SobotAntoLineLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 点踩问题消息
 */
public class CaiMessageHolder extends MsgHolderBase implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    private LinearLayout sobot_hide_layout;

    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//标签 自动换行
    private ScrollView scroll_v;
    private EditText ed_describe;
    private TextView sobot_submit;//提交
    private TextView msg;//提交
    private View v_top;

    private FrameLayout sobot_cai_action;

    public ZhiChiMessageBase message;
    private SobotRealuateInfo sobotRealuateInfo;
    private SobotRealuateConfigInfo realuateConfigInfo;//点踩配置信息
    private int themeColor;
    private boolean changeThemeColor;

    @SuppressLint("ClickableViewAccessibility")
    public CaiMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_hide_layout = (LinearLayout) convertView.findViewById(R.id.sobot_hide_layout);
        msg = convertView.findViewById(R.id.sobot_msg);
        v_top = convertView.findViewById(R.id.v_top);
        sobot_cai_action = convertView.findViewById(R.id.sobot_cai_action);
        sobot_evaluate_lable_autoline = convertView.findViewById(R.id.sobot_evaluate_lable_autoline);
        sobot_submit = convertView.findViewById(R.id.sobot_submit);
        ed_describe = convertView.findViewById(R.id.ed_describe);
        scroll_v = convertView.findViewById(R.id.scroll_v);
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(context);
        }
        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
            String themeColor[] = initMode.getVisitorScheme().getRebotTheme().split(",");
            if (themeColor.length > 1) {
                if (mContext.getResources().getColor(R.color.sobot_gradient_start) != Color.parseColor(themeColor[0]) || mContext.getResources().getColor(R.color.sobot_gradient_end) != Color.parseColor(themeColor[1])) {
                    int[] colors = new int[themeColor.length];
                    for (int i = 0; i < themeColor.length; i++) {
                        colors[i] = Color.parseColor(themeColor[i]);
                    }
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                    if (v_top != null) {
                        v_top.setBackground(aDrawable);
                    }
                } else {
                    int[] colors = new int[]{mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_start), mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_end)};
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                    if (v_top != null) {
                        v_top.setBackground(aDrawable);
                    }
                }
            }
        }
        resetMaxWidth();
        ed_describe.setOnTouchListener(this);
    }


    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        this.message = message;
        this.sobotRealuateInfo = message.getRealuateInfo();

        if (message.getSubmitStatus() == 2) {
            //已提交的消息，只显示第一条
            msg.setVisibility(View.VISIBLE);
            msg.setText(TextUtils.isEmpty(message.getMsg()) ? "" : message.getMsg());
            sobot_msg_content_ll.setVisibility(View.VISIBLE);
            sobot_cai_action.setVisibility(View.GONE);
        } else if (sobotRealuateInfo != null) {
            this.realuateConfigInfo = sobotRealuateInfo.getChatRealuateConfigInfo();
            showData();
        }
    }

    private void showData() {
        if (!TextUtils.isEmpty(realuateConfigInfo.getRealuateAfterWord())) {
            //显示第一条消息
            msg.setVisibility(View.VISIBLE);
            msg.setText(realuateConfigInfo.getRealuateAfterWord());
            sobot_msg_content_ll.setVisibility(View.VISIBLE);
        } else {
            sobot_msg_content_ll.setVisibility(View.GONE);
        }
        if (sobotRealuateInfo.getSubmitStatus() == 2) {
            sobot_cai_action.setVisibility(View.GONE);
        } else {
            sobot_cai_action.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(sobotRealuateInfo.getRealuateDetail())) {
            ed_describe.setText(sobotRealuateInfo.getRealuateDetail());
        } else {
            ed_describe.setText("");
        }
        //标签
        if (realuateConfigInfo.getChatRealuateTagInfoList() != null) {
            createChildLableView(realuateConfigInfo.getChatRealuateTagInfoList());
        }
        //输入框
        if (!TextUtils.isEmpty(realuateConfigInfo.getRealuateEvaluateWord())) {
            ed_describe.setHint(realuateConfigInfo.getRealuateEvaluateWord());
        }
        //提交按钮
        if (changeThemeColor) {
            sobot_submit.setTextColor(themeColor);
        }
        if (TextUtils.isEmpty(sobotRealuateInfo.getRealuateDetail()) && sobotRealuateInfo.getRealuateTag() == null) {
            sobot_submit.setAlpha(0.5f);
        }
        sobot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(ed_describe.getText().toString()) && sobotRealuateInfo.getRealuateTag() == null) {
                    //不可提交
                } else {
                    sobotRealuateInfo.setRealuateDetail(ed_describe.getText().toString());
                    message.setRealuateInfo(sobotRealuateInfo);
                    sobotRealuateInfo.setMsgId(message.getMsgId());
                    sobotRealuateInfo.setCid(message.getCid());
                    msgCallBack.submitCai(message.getMsgId(), sobotRealuateInfo);
                }
            }
        });
        ed_describe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(ed_describe.getText().toString()) && sobotRealuateInfo.getRealuateTag() == null) {
                    sobot_submit.setAlpha(0.5f);
                } else {
                    if (sobotRealuateInfo != null) {
                        sobotRealuateInfo.setRealuateDetail(ed_describe.getText().toString());
                    }
                    sobot_submit.setAlpha(1f);
                }
            }
        });
    }

    //隐藏所有自动换行的标签
    private void createChildLableView(final List<SobotRealuateTagInfoList> list) {
        sobot_evaluate_lable_autoline.removeAllViews();
//        menuLin.removeAllViews();
        for (int i = 0; i < list.size(); i++) {
            final SobotRealuateTagInfoList temp = list.get(i);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.sobot_layout_cai_item, null);
            final CheckBox checkBox = view.findViewById(R.id.sobot_evaluate_cb_lable);
            checkBox.setText(list.get(i).getRealuateTag());
            if (changeThemeColor) {
                checkBox.setTextColor(themeColor);
            }
            checkBox.setOnCheckedChangeListener(this);
            if (sobotRealuateInfo.getRealuateTag() != null && sobotRealuateInfo.getRealuateTag().getId().equals(list.get(i).getId())) {
                checkBox.setChecked(true);
                sobot_submit.setAlpha(1f);
            }
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox.isChecked()) {
                        sobotRealuateInfo.setRealuateTag(temp);
                        sobot_submit.setAlpha(1f);
                    } else {
                        sobotRealuateInfo.setRealuateTag(null);
                        if (!TextUtils.isEmpty(ed_describe.getText().toString())) {
                            sobot_submit.setAlpha(1f);
                        } else {
                            sobot_submit.setAlpha(0.5f);
                        }
                    }
                    createChildLableView(realuateConfigInfo.getChatRealuateTagInfoList());

                }
            });
            sobot_evaluate_lable_autoline.addView(view);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (changeThemeColor) {
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_bg_cai_tag);
            if (b) {
                compoundButton.setBackground(ThemeUtils.applyColorToDrawable(drawable, themeColor));
                compoundButton.setTextColor(themeColor);
            } else {
                compoundButton.setBackground(drawable);
                compoundButton.setTextColor(mContext.getResources().getColor(R.color.sobot_common_gray1));
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == ed_describe) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
            }
        }
        return false;
    }
}