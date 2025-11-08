package com.sobot.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.lablesview.SobotLablesViewModel;

import java.util.ArrayList;
import java.util.List;

//机器人 大模型机器人 按钮消息 PagerAdapter
public class SobotRobotAiAgentButtonPageAdater extends PagerAdapter {
    private ArrayList<View> mViewList = new ArrayList<>();

    public SobotRobotAiAgentButtonPageAdater(final Context context, final ArrayList<SobotLablesViewModel> label, final ZhiChiMessageBase messageBase, final SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        if (context != null && label != null && !label.isEmpty() && messageBase != null) {
            ArrayList<View> tempArr = new ArrayList<>();
            for (int i = 0; i < label.size(); i++) {
                View llRoot = LayoutInflater.from(context).inflate(R.layout.sobot_chat_msg_item_template2_item_l, null);
                TextView textView = llRoot.findViewById(R.id.sobot_template_item_title);
                textView.setText(label.get(i).getTitle());
                textView.setTextColor(ThemeUtils.getThemeColor(context));
//                if (messageBase.getSugguestionsFontColor() == 0) {
//                    try {
//                        if (context.getResources().getColor(R.color.sobot_color) == context.getResources().getColor(R.color.sobot_common_green)) {
//                            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
//                                    ZhiChiConstant.sobot_last_current_initModel);
//                            if (initMode != null && initMode.getVisitorScheme() != null) {
//                                //服务端返回的可点击链接颜色
//                                if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
//                                    textView.setTextColor(Color.parseColor(initMode.getVisitorScheme().getMsgClickColor()));
//                                }
//                            }
//                        }
//                    } catch (Exception e) {
//                        textView.setTextColor(ThemeUtils.getThemeColor(context));
//                    }
//                } else {
//                    textView.setTextColor(ContextCompat.getColor(context, R.color.sobot_color_text_first));
//                }
                final int finalI = i;
                llRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (msgCallBack != null) {
                            ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
                            msgObj.setNodeId(messageBase.getNodeId());
                            msgObj.setProcessId(messageBase.getProcessId());
                            msgObj.setVariableId(messageBase.getVariableId());
                            msgObj.setContent(StringUtils.checkStringIsNull(label.get(finalI).getTitle()));
                            msgCallBack.sendMessageToRobot(msgObj, 6, 1, "");
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

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    // 添加界面，一般会添加当前页和左右两边的页面
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    // 去除页面
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }
}
