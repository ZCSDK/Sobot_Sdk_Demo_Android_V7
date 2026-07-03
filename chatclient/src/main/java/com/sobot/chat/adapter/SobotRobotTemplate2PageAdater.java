package com.sobot.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;
import com.sobot.chat.widget.robottemplate.RobotTemplateViewPager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//机器人 消息多轮模板2 PagerAdapter
public class SobotRobotTemplate2PageAdater extends SobotBaseTemplateAdapter {

    public SobotRobotTemplate2PageAdater(Context context, int msgMaxWidth, RobotTemplateViewPager pvTemplateSecond, String type, ArrayList<SobotLablesViewModel> label, ZhiChiMessageBase messageBase, final SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        super(context);
        if (context != null && label != null && !label.isEmpty() && messageBase != null && pvTemplateSecond != null) {
            ArrayList<View> tempArr = new ArrayList<>();
            for (int i = 0; i < label.size(); i++) {
                View llRoot = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_template2_item_l, pvTemplateSecond, false);
                llRoot.getLayoutParams().width = msgMaxWidth;
                TextView textView = llRoot.findViewById(R.id.sobot_template_item_title);
                if ("1".equals(type)) {
                    textView.setText((i + 1) + "、 " + label.get(i).getTitle());
                    textView.setBackground(null);
                    textView.setGravity(Gravity.LEFT);
                    textView.setPadding(0, 0, 0, 0);
                } else {
                    textView.setText(label.get(i).getTitle());
                }

                if (messageBase.getSugguestionsFontColor() == 0) {
                    try {
                        if (context.getResources().getColor(R.color.sobot_color) == context.getResources().getColor(R.color.sobot_common_green)) {
                            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                                    ZhiChiConstant.sobot_last_current_initModel);
                            if (initMode != null && initMode.getVisitorScheme() != null) {
                                if ("1".equals(type)) {
                                    //服务端返回的可点击链接颜色
                                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                                        textView.setTextColor(Color.parseColor(initMode.getVisitorScheme().getMsgClickColor()));
                                    }
                                } else {
                                    //服务端返回的可主题颜色
                                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
                                        textView.setTextColor(Color.parseColor(initMode.getVisitorScheme().getRebotTheme()));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        textView.setTextColor(ThemeUtils.getThemeColor(context));
                    }
                } else {
                    textView.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_first));
                }
                final int finalI = i;
                llRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (messageBase == null || messageBase.getAnswer() == null) {
                            return;
                        }
                        String lastCid = SharedPreferencesUtil.getStringData(context, "lastCid", "");
                        //当前cid相同相同才能重复点;ClickFlag 是否允许多次点击 0:只点击一次 1:允许重复点击
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
                        SobotMultiDiaRespInfo multiDiaRespInfo = messageBase.getAnswer().getMultiDiaRespInfo();
                        SobotLablesViewModel lablesViewModel = label.get(finalI);
                        if (multiDiaRespInfo != null && multiDiaRespInfo.getEndFlag() && !TextUtils.isEmpty(lablesViewModel.getAnchor())) {
                            if (SobotOption.dispatchUrlClick(context, lablesViewModel.getAnchor())) {
                                return;
                            }
                            Intent intent = new Intent(context, WebViewActivity.class);
                            intent.putExtra("url", lablesViewModel.getAnchor());
                            context.startActivity(intent);
                        } else {
                            sendMultiRoundQuestions(lablesViewModel, multiDiaRespInfo, finalI, messageBase, msgCallBack);
                        }
                    }
                });
                tempArr.add(llRoot);
            }
            List<List<View>> groups = new ArrayList<>(); // 存放分好组的结果
            int groupSize = 6; // 设置每组的大小
            for (int startIndex = 0; startIndex < tempArr.size(); startIndex += groupSize) {
                int endIndex = Math.min(startIndex + groupSize, tempArr.size()); // 计算当前组的结尾索引
                List<View> group = tempArr.subList(startIndex, endIndex); // 获取当前组的子列表
                groups.add(group); // 将当前组添加到结果列表中
            }
            int columns = context.getResources().getInteger(R.integer.sobot_robot_template_columns);
            if (columns <= 0) {
                columns = 1;
            }
            for (int j = 0; j < groups.size(); j++) {
                LinearLayout pagell = new LinearLayout(context);
                pagell.setOrientation(LinearLayout.VERTICAL);
                pagell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                List<View> textViewList = groups.get(j);
                if (columns == 1) {
                    for (int m = 0; m < textViewList.size(); m++) {
                        pagell.addView(textViewList.get(m));
                    }
                } else {
                    // 多列模式：每 columns 个 item 包一行；item 用 weight 等分行宽、高度 WRAP_CONTENT
                    // 行的高度跟自身内容自然撑开，避免与 ViewPager 强制 EXACTLY 高度的父链交互产生异常拉伸；
                    // 同行两列等高（以高者为准）通过 post() 在首次 layout 后做对齐
                    for (int m = 0; m < textViewList.size(); m += columns) {
                        final LinearLayout row = new LinearLayout(context);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        final List<View> rowItems = new ArrayList<>();
                        for (int k = 0; k < columns; k++) {
                            int idx = m + k;
                            if (idx < textViewList.size()) {
                                View item = textViewList.get(idx);
                                item.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                                row.addView(item);
                                rowItems.add(item);
                            } else {
                                // 末尾不足 columns 个用空白占位保持左对齐
                                View placeholder = new View(context);
                                placeholder.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                                row.addView(placeholder);
                            }
                        }
                        // 首次 layout 后对齐本行 item 高度（以高者为准），同步把白底 TextView 撑满
                        row.post(new Runnable() {
                            @Override
                            public void run() {
                                int maxH = 0;
                                for (View item : rowItems) {
                                    if (item.getHeight() > maxH) {
                                        maxH = item.getHeight();
                                    }
                                }
                                boolean changed = false;
                                for (View item : rowItems) {
                                    if (item.getHeight() < maxH) {
                                        ViewGroup.LayoutParams lp = item.getLayoutParams();
                                        lp.height = maxH;
                                        item.setLayoutParams(lp);
                                        View titleView = item.findViewById(R.id.sobot_template_item_title);
                                        if (titleView != null) {
                                            ViewGroup.LayoutParams tlp = titleView.getLayoutParams();
                                            tlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                            titleView.setLayoutParams(tlp);
                                        }
                                        changed = true;
                                    }
                                }
                                if (changed) {
                                    row.requestLayout();
                                }
                            }
                        });
                        pagell.addView(row);
                    }
                }
                mViewList.add(pagell);
            }
        }

    }

    private void sendMultiRoundQuestions(SobotLablesViewModel data, SobotMultiDiaRespInfo multiDiaRespInfo, int clickPosition, ZhiChiMessageBase messageBase, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        if (multiDiaRespInfo == null) {
            return;
        }
        String labelText = data.getTitle();
        String[] outputParam = multiDiaRespInfo.getOutPutParamList();
        if (msgCallBack != null && messageBase != null) {
            ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
            Map<String, String> map = new HashMap<>();
            map.put("level", multiDiaRespInfo.getLevel() + "");
            map.put("conversationId", multiDiaRespInfo.getConversationId());
            if (outputParam != null) {
                if (outputParam.length == 1) {
                    map.put(outputParam[0], data.getTitle());
                } else {
                    if (multiDiaRespInfo.getInterfaceRetList() != null && multiDiaRespInfo.getInterfaceRetList().size() > 0) {
                        for (String anOutputParam : outputParam) {
                            map.put(anOutputParam, multiDiaRespInfo.getInterfaceRetList().get(clickPosition).get(anOutputParam));
                        }
                    }
                }
            }
            msgObj.setContent(GsonUtil.map2Str(map));
            msgObj.setId(System.currentTimeMillis() + "");
            msgCallBack.sendMessageToRobot(msgObj, 4, 2, labelText, labelText);
        }
    }
}
