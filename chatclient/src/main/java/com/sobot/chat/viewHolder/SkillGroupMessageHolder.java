package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.adapter.SobotSkillAdapter;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.horizontalgridpage.SobotRecyclerCallBack;
import com.sobot.chat.widget.recycler.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * 转人工--技能组
 */
public class SkillGroupMessageHolder extends MsgHolderBase {
    private RecyclerView sobot_rcy_skill;

    private TextView sobot_tv_title;
    private SobotSkillAdapter sobotSkillAdapter;
    private List<ZhiChiGroupBase> list_skill;
    private int msgFlag = 0;
    private Context mContext;

    public SkillGroupMessageHolder(Context context, View convertView) {
        super(context, convertView);
        mContext = context;
        list_skill = new ArrayList<ZhiChiGroupBase>();
        sobot_tv_title = (TextView) convertView.findViewById(R.id.sobot_tv_title);
        sobot_rcy_skill = (RecyclerView) convertView.findViewById(R.id.rv_list);

    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        if (message.getSkillGroups() != null && !message.getSkillGroups().isEmpty()) {
            list_skill.clear();
            list_skill.addAll(message.getSkillGroups());
            // 横屏 / Pad 走宽屏布局（由 sobot_list_span_count 资源限定符决定，>1 即宽屏）
            int spanFromRes = mContext.getResources().getInteger(R.integer.sobot_list_span_count);
            boolean isWideLayout = spanFromRes > 1;
            int size = list_skill.size();
            int groupStyle = list_skill.get(0).getGroupStyle();
            int spanCount = 1;
            int spacingDp = 0;
            if (groupStyle == 1) {
                //图文样式
                if (isWideLayout) {
                    // 横屏 / Pad：≤4 时按数量平分，>4 时固定 4 列
                    spanCount = Math.min(size, 4);
                } else {
                    // 竖屏：2 / 4 → 2 列，其余 3 列
                    spanCount = (size == 2 || size == 4) ? 2 : 3;
                }
            } else if (groupStyle == 2) {
                //图文加描述：宽屏 2 列、竖屏单列，间距 8dp
                spanCount = spanFromRes;
                spacingDp = 8;
            } else {
                //其他模式：宽屏 2 列、竖屏单列，间距 10dp
                spanCount = spanFromRes;
                spacingDp = 10;
            }
            sobot_rcy_skill.setLayoutManager(new GridLayoutManager(mContext, spanCount));
            // ViewHolder 复用时 bindData 会重复触发，先清空避免 ItemDecoration 累加
            while (sobot_rcy_skill.getItemDecorationCount() > 0) {
                sobot_rcy_skill.removeItemDecorationAt(0);
            }
            int spacingPx = ScreenUtils.dip2px(mContext, spacingDp);
            sobot_rcy_skill.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingPx, false));
            sobotSkillAdapter = new SobotSkillAdapter(context, list_skill, msgFlag, new SobotRecyclerCallBack() {
                @Override
                public void onItemClickListener(View view, int position) {
                    if (list_skill != null && !list_skill.isEmpty()) {
                        Intent intent = new Intent();
                        intent.setAction(ZhiChiConstant.ACTION_SKILL_GRROUP);
                        // 安全：限定本进程，避免技能组对象泄漏给其他应用 (CWE-927)
                        intent.setPackage(context.getPackageName());
                        if (getInitModel() != null && getInitModel().getAssignmentMode() == 1) {
                            //异步接待 进行转人工
                            if (!TextUtils.isEmpty(list_skill.get(position).getGroupName())) {
                                //发送广播，通知转人工
                                intent.putExtra("group", list_skill.get(position));
                                context.sendBroadcast(intent);
                            }
                        } else {
                            if ("true".equals(list_skill.get(position).isOnline())) {
                                if (!TextUtils.isEmpty(list_skill.get(position).getGroupName())) {
                                    //发送广播，通知转人工
                                    intent.putExtra("group", list_skill.get(position));
                                    context.sendBroadcast(intent);
                                }
                            } else {
                                if (msgFlag == ZhiChiConstant.sobot_msg_flag_open) {
                                    intent.putExtra("toLeaveMsg", true);
                                    intent.putExtra("group", list_skill.get(position));
                                    context.sendBroadcast(intent);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onItemLongClickListener(View view, int position) {

                }
            });
            sobot_rcy_skill.setAdapter(sobotSkillAdapter);
            sobotSkillAdapter.setList(list_skill);
            sobotSkillAdapter.setMsgFlag(msgFlag);
            if (TextUtils.isEmpty(list_skill.get(0).getGroupGuideDoc())) {
                sobot_tv_title.setText(R.string.sobot_switch_robot_title_2);
            } else {
                sobot_tv_title.setText(list_skill.get(0).getGroupGuideDoc());
            }
        } else {
            sobot_tv_title.setText(R.string.sobot_switch_robot_title_2);
        }
    }

    public ZhiChiInitModeBase getInitModel() {
        ZhiChiInitModeBase initModel = null;
        if (mContext != null) {
            initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(mContext,
                    ZhiChiConstant.sobot_last_current_initModel);
        }
        return initModel;
    }
}
