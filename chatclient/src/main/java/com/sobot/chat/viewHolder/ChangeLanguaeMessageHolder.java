package com.sobot.chat.viewHolder;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotlanguaeModel;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.viewHolder.base.MsgHolderBase;

import java.util.ArrayList;

/**
 * 切换语言消息
 */
public class ChangeLanguaeMessageHolder extends MsgHolderBase {
    private LinearLayout sobot_languaeList;//语言列表
    private TextView tv_more;//更多语言按钮
    private View view_split;//更多语言按钮 分割线
    private TextView tvStripe;


    public ChangeLanguaeMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_languaeList = (LinearLayout) convertView.findViewById(R.id.sobot_languaeList);
        tv_more = convertView.findViewById(R.id.tv_more);
        view_split = convertView.findViewById(R.id.view_split);
        tvStripe = convertView.findViewById(R.id.sobot_stripe);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        tvStripe.setMaxWidth(msgMaxWidth);
        resetMaxWidth();
        if (message.getLanguaeModels() != null && !message.getLanguaeModels().isEmpty()) {
            ArrayList<SobotlanguaeModel> languaeModels = message.getLanguaeModels();
            sobot_languaeList.removeAllViews();
            if (languaeModels.size() > 6) {
                view_split.setVisibility(View.VISIBLE);
                tv_more.setVisibility(View.VISIBLE);
                tv_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FastClickUtils.isCanClick(2000)) {
                            if (msgCallBack != null) {
                                msgCallBack.chooseByAllLangaue(languaeModels, message);
                            }
                        }
                    }
                });
            } else {
                view_split.setVisibility(View.GONE);
                tv_more.setVisibility(View.GONE);
            }
            tv_more.setTextColor(ThemeUtils.getThemeColor(mContext));
            for (int i = 0; i < languaeModels.size(); i++) {
                if (i < 6) {
                    final SobotlanguaeModel model = languaeModels.get(i);
                    TextView lanTV = new TextView(context);
                    lanTV.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    lanTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                    lanTV.setTextColor(ThemeUtils.getThemeColor(mContext));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (i != 0) {
                        lp.topMargin = ScreenUtils.dip2px(context, 12);
                    } else {
                        lp.topMargin = ScreenUtils.dip2px(context, 10);
                    }
                    lanTV.setLayoutParams(lp);
                    lanTV.setPadding(ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 9), ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 9));
                    String tempStr = model.getName();
                    lanTV.setGravity(Gravity.CENTER);
                    lanTV.setBackgroundResource(R.drawable.sobot_oval_white_bg);
                    lanTV.setText(tempStr);
                    lanTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (FastClickUtils.isCanClick()) {
                                if (msgCallBack != null) {
                                    msgCallBack.chooseLangaue(model, message);
                                }
                            }
                        }
                    });
                    sobot_languaeList.addView(lanTV);
                }
            }
        }
    }
}