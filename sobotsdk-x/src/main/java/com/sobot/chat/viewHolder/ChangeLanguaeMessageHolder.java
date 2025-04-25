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


    public ChangeLanguaeMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_languaeList = (LinearLayout) convertView.findViewById(R.id.sobot_languaeList);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getLanguaeModels() != null && message.getLanguaeModels().size() > 0) {
            final ArrayList<SobotlanguaeModel> languaeModels = message.getLanguaeModels();
            sobot_languaeList.removeAllViews();
            for (int i = 0; i < languaeModels.size(); i++) {
                if (i > 5) {
                    TextView lanTV = new TextView(context);
                    lanTV.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 268), ViewGroup.LayoutParams.WRAP_CONTENT));
                    lanTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                    lanTV.setTextColor(ThemeUtils.getThemeColor(mContext));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.topMargin = ScreenUtils.dip2px(context, 12);
                    lanTV.setLayoutParams(lp);
                    lanTV.setLineSpacing(context.getResources().getDimension(R.dimen.sobot_text_line_spacing_extra), 1);
                    lanTV.setPadding(ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 7), ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 7));
                    lanTV.setGravity(Gravity.CENTER);
                    lanTV.setBackgroundResource(R.drawable.sobot_oval_white_bg);
                    lanTV.setText(context.getResources().getString(R.string.sobot_more_language));
                    lanTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (FastClickUtils.isCanClick(1500)) {
                                if (msgCallBack != null) {
                                    msgCallBack.chooseByAllLangaue(languaeModels, message);
                                }
                            }
                        }
                    });
                    sobot_languaeList.addView(lanTV);
                    break;
                } else {
                    final SobotlanguaeModel model = languaeModels.get(i);
                    TextView lanTV = new TextView(context);
                    lanTV.setLayoutParams(new LinearLayout.LayoutParams(ScreenUtils.dip2px(context, 268), ViewGroup.LayoutParams.WRAP_CONTENT));
                    lanTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                    lanTV.setTextColor(ThemeUtils.getThemeColor(mContext));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (i != 0) {
                        lp.topMargin = ScreenUtils.dip2px(context, 12);
                    } else {
                        lp.topMargin = ScreenUtils.dip2px(context, 10);
                    }
                    lanTV.setLayoutParams(lp);
                    lanTV.setPadding(ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 7), ScreenUtils.dip2px(context, 16), ScreenUtils.dip2px(context, 7));
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