
package com.sobot.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.robottemplate.RobotTemplate1ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//机器人 消息多轮模板1 PagerAdapter
public class SobotRobotTemplate1PageAdater extends SobotBaseTemplateAdapter {

    public SobotRobotTemplate1PageAdater(Context context, int msgMaxWidth, RobotTemplate1ViewPager pvTemplateFirst, List<Map<String, String>> interfaceRetList, ZhiChiMessageBase messageBase, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        super(context);
        if (context != null && interfaceRetList != null && !interfaceRetList.isEmpty() && messageBase != null && pvTemplateFirst != null) {
            ArrayList<View> tempArr = new ArrayList<>();
            for (int i = 0; i < interfaceRetList.size(); i++) {
                Map<String, String> interfaceRet = interfaceRetList.get(i);
                View convertView = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_template1_item_l, pvTemplateFirst, false);
                convertView.getLayoutParams().width = msgMaxWidth;
                SobotProgressImageView sobotThumbnail = (SobotProgressImageView) convertView.findViewById(R.id.sobot_template1_item_thumbnail);
                TextView sobotTitle = (TextView) convertView.findViewById(R.id.sobot_template1_item_title);
                TextView sobotSummary = (TextView) convertView.findViewById(R.id.sobot_template1_item_summary);
                TextView sobotLable = (TextView) convertView.findViewById(R.id.sobot_template1_item_lable);
                TextView sobotOtherLable = (TextView) convertView.findViewById(R.id.sobot_template1_item_other_flag);

                if (!TextUtils.isEmpty(interfaceRet.get("thumbnail"))) {
                    sobotThumbnail.setVisibility(View.VISIBLE);
                    sobotSummary.setEllipsize(TextUtils.TruncateAt.END);
                    sobotThumbnail.setImageUrl(interfaceRet.get("thumbnail"));
                } else {
                    sobotThumbnail.setVisibility(View.GONE);
                }

                sobotTitle.setText(interfaceRet.get("title"));
                sobotSummary.setText(interfaceRet.get("summary"));
                sobotLable.setText(interfaceRet.get("label"));
                sobotOtherLable.setText(interfaceRet.get("tag"));

                if (!TextUtils.isEmpty(interfaceRet.get("label"))) {
                    sobotLable.setVisibility(View.VISIBLE);
                    sobotLable.setTextColor(ThemeUtils.getThemeColor(context));
                } else {
                    sobotLable.setVisibility(View.GONE);
                }
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String lastCid = SharedPreferencesUtil.getStringData(context, "lastCid", "");
                        //当前cid相同才能重复点;ClickFlag 是否允许多次点击 0:只点击一次 1:允许重复点击
                        //ClickFlag=0 时  ClickCount=0可点击，大于0 不可点击
                        if (messageBase.getSugguestionsFontColor() == 0) {
                            if (!TextUtils.isEmpty(messageBase.getCid()) && lastCid.equals(messageBase.getCid())) {
                                if (messageBase.getAnswer().getMultiDiaRespInfo().getClickFlag() == 0 && messageBase.getClickCount() > 0) {
                                    return;
                                }
                                messageBase.addClickCount();
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }

                        SobotMultiDiaRespInfo mMultiDiaRespInfo = messageBase.getAnswer().getMultiDiaRespInfo();

                        if (mMultiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(interfaceRet.get("anchor"))) {
                            if (SobotOption.newHyperlinkListener != null) {
                                //如果返回true,拦截;false 不拦截
                                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(context, interfaceRet.get("anchor"));
                                if (isIntercept) {
                                    return;
                                }
                            }
                            Intent intent = new Intent(context, WebViewActivity.class);
                            intent.putExtra("url", interfaceRet.get("anchor"));
                            context.startActivity(intent);
                        } else {
                            ChatUtils.sendMultiRoundQuestions(context, mMultiDiaRespInfo, interfaceRet, msgCallBack);
                        }
                    }
                });
                tempArr.add(convertView);
            }

            // 分组处理
            List<List<View>> groups = new ArrayList<>();
            int groupSize = 3;
            for (int startIndex = 0; startIndex < tempArr.size(); startIndex += groupSize) {
                int endIndex = Math.min(startIndex + groupSize, tempArr.size());
                List<View> group = tempArr.subList(startIndex, endIndex);
                groups.add(group);
            }

            for (int j = 0; j < groups.size(); j++) {
                LinearLayout pagell = new LinearLayout(context);
                pagell.setOrientation(LinearLayout.VERTICAL);
                pagell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                List<View> textViewList = groups.get(j);
                for (int m = 0; m < textViewList.size(); m++) {
                    pagell.addView(textViewList.get(m));
                }
                mViewList.add(pagell);
            }
        }
    }
}