package com.sobot.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.horizontalgridpage.SobotRecyclerCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.List;

public class SobotSikllAdapter extends RecyclerView.Adapter<SobotSikllAdapter.ViewHolder> {
    private int msgFlag;//留言开关
    private Context mContext;
    private List<ZhiChiGroupBase> list;
    private SobotRecyclerCallBack callBack;
    private static int themeColor;

    public SobotSikllAdapter(Context context, List<ZhiChiGroupBase> list, int msgFlag, SobotRecyclerCallBack callBack) {
        mContext = context;
        this.msgFlag = msgFlag;
        this.list = list;
        this.callBack = callBack;
        themeColor = ThemeUtils.getThemeColor(mContext);
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    /**
     * 设置监听
     *
     * @param holder 监听对象
     */
    public void setListener(RecyclerView.ViewHolder holder) {
        // 设置监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onItemClickListener(v, (Integer) v.getTag());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                callBack.onItemLongClickListener(v, (Integer) v.getTag());
                return true;
            }
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == STYLE_PIC_TEXT) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill_second_style, parent, false);
        } else if (viewType == STYLE_PIC_TEXT_DES) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill_third_style, parent, false);
        } else {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(mContext, itemView);
        setListener(viewHolder);//设置监听
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);
        ZhiChiGroupBase zhiChiSkillIModel = (ZhiChiGroupBase) list.get(position);
        if (zhiChiSkillIModel != null) {
            if (zhiChiSkillIModel.getGroupStyle() == 1) {
                //图文样式
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                if (!TextUtils.isEmpty(zhiChiSkillIModel.getGroupPic())) {
                    SobotBitmapUtil.display(mContext, zhiChiSkillIModel.getGroupPic(), viewHolder.sobot_iv_group_img);
                }
            } else if (zhiChiSkillIModel.getGroupStyle() == 2) {
                //图文+描述样式
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                viewHolder.sobot_tv_group_desc.setText(zhiChiSkillIModel.getDescription());
                if (!TextUtils.isEmpty(zhiChiSkillIModel.getGroupPic())) {
                    SobotBitmapUtil.display(mContext, zhiChiSkillIModel.getGroupPic(), viewHolder.sobot_iv_group_img);
                }
            } else {
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                if ("true".equals(zhiChiSkillIModel.isOnline())) {
                    viewHolder.sobot_tv_group_desc.setVisibility(View.GONE);
                    viewHolder.sobot_tv_group_name.setTextSize(14);
                } else {
                    String content;
                    viewHolder.sobot_tv_group_name.setTextSize(12);
                    viewHolder.sobot_tv_group_name.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_common_gray2));
                    if (msgFlag == ZhiChiConstant.sobot_msg_flag_open) {
                        content = mContext.getString(R.string.sobot_no_access) + "，" + mContext.getString(R.string.sobot_can) + " <font color='#0DAEAF'>" + mContext.getString(R.string.sobot_str_bottom_message) + "</a>";
                    } else {
                        content = mContext.getString(R.string.sobot_no_access);
                    }
                    viewHolder.sobot_tv_group_desc.setText(Html.fromHtml(content));
                    viewHolder.sobot_tv_group_desc.setVisibility(View.VISIBLE);
                }
                if(ThemeUtils.isChangedThemeColor(mContext)) {
                    viewHolder.sobot_tv_group_name.setTextColor(themeColor);
                    viewHolder.sobot_ll_content.setTag(position);
                    viewHolder.sobot_ll_content.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_dialog_button_selector);
                            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                view.setBackground(ThemeUtils.applyColorToDrawable(drawable, themeColor));
                                viewHolder.sobot_tv_group_name.setTextColor(mContext.getResources().getColor(R.color.sobot_common_white));
                            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() == motionEvent.ACTION_OUTSIDE || motionEvent.getAction() == motionEvent.ACTION_POINTER_DOWN || motionEvent.getAction() == motionEvent.ACTION_POINTER_UP) {
                                view.setBackground(drawable);
                                viewHolder.sobot_tv_group_name.setTextColor(themeColor);
                                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                    //点击事件
                                    if (callBack != null) {
                                        callBack.onItemClickListener(view, (Integer) view.getTag());
                                    }
                                }
                            }
                            return true;
                        }
                    });
                }
            }
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView sobot_iv_group_img;
        private TextView sobot_tv_group_name;
        private TextView sobot_tv_group_desc;
        private LinearLayout sobot_ll_content;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            this.sobot_iv_group_img = itemView.findViewById(R.id.sobot_iv_group_img);
            this.sobot_tv_group_name = itemView.findViewById(R.id.sobot_tv_group_name);
            this.sobot_tv_group_desc = itemView.findViewById(R.id.sobot_tv_group_desc);
            this.sobot_ll_content = itemView.findViewById(R.id.sobot_ll_content);
        }
    }

    private static final int STYLE_DEF = 0;//默认
    private static final int STYLE_PIC_TEXT = 1;//图文
    private static final int STYLE_PIC_TEXT_DES = 2;//图文——描述

    @Override
    public int getItemViewType(int position) {
        ZhiChiGroupBase groupBase = list.get(position);
        if (groupBase != null) {
            return groupBase.getGroupStyle();
        }
        return 0;
    }

    public List<ZhiChiGroupBase> getList() {
        return list;
    }

    public void setList(List<ZhiChiGroupBase> list) {
        this.list = list;
    }

    public int getMsgFlag() {
        return msgFlag;
    }

    public void setMsgFlag(int msgFlag) {
        this.msgFlag = msgFlag;
    }
}