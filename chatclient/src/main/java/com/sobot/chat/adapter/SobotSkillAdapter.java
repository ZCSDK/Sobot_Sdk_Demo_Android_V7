package com.sobot.chat.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.horizontalgridpage.SobotRecyclerCallBack;
import com.sobot.chat.widget.image.SobotRCImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.List;

public class SobotSkillAdapter extends RecyclerView.Adapter<SobotSkillAdapter.ViewHolder> {
    private int msgFlag;//留言开关
    private Context mContext;
    private List<ZhiChiGroupBase> list;
    private SobotRecyclerCallBack callBack;
    private static int themeColor;
    private static final int STYLE_TEXT = 0;//默认
    private static final int STYLE_PIC_TEXT = 1;//图文
    private static final int STYLE_PIC_TEXT_DES = 2;//图文——描述

    public SobotSkillAdapter(Context context, List<ZhiChiGroupBase> list, int msgFlag, SobotRecyclerCallBack callBack) {
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
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill_pic_text, parent, false);
        } else if (viewType == STYLE_PIC_TEXT_DES) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill_pic_text_des, parent, false);
        } else {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.sobot_list_item_skill_text, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(mContext, itemView);
        setListener(viewHolder);//设置监听
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SobotSkillAdapter.ViewHolder viewHolder, int position) {
        if (getInitModel() != null && getInitModel().getAssignmentMode() == 1) {
            //异步接待 屏蔽留言
            msgFlag = ZhiChiConstant.sobot_msg_flag_close;
        }
        viewHolder.itemView.setTag(position);
        ZhiChiGroupBase zhiChiSkillIModel = (ZhiChiGroupBase) list.get(position);
        if (zhiChiSkillIModel != null) {
            if (zhiChiSkillIModel.getGroupStyle() == STYLE_PIC_TEXT) {
                //图文样式
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                if (!TextUtils.isEmpty(zhiChiSkillIModel.getGroupPic())) {
                    SobotBitmapUtil.display(mContext, CommonUtils.encode(zhiChiSkillIModel.getGroupPic())
                            , viewHolder.sobot_iv_group_img);
                }
            } else if (zhiChiSkillIModel.getGroupStyle() == STYLE_PIC_TEXT_DES) {
                //图文+描述样式
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                viewHolder.sobot_tv_group_desc.setText(zhiChiSkillIModel.getDescription());
                if (!TextUtils.isEmpty(zhiChiSkillIModel.getGroupPic())) {
                    SobotBitmapUtil.display(mContext, CommonUtils.encode(zhiChiSkillIModel.getGroupPic())
                            , viewHolder.sobot_iv_group_img);
                }
            } else {
                //纯文本
                if (viewHolder.iv_select != null) {
                    if (ChatUtils.isRtl(mContext)) {
                        viewHolder.iv_select.setImageResource(R.drawable.sobot_icon_right_arrow_rtl);
                    } else {
                        viewHolder.iv_select.setImageResource(R.drawable.sobot_icon_right_arrow);
                    }
                }
                viewHolder.sobot_tv_group_name.setText(zhiChiSkillIModel.getGroupName());
                if ("true".equals(zhiChiSkillIModel.isOnline())) {
                    viewHolder.sobot_tv_group_desc.setVisibility(View.GONE);
                    viewHolder.sobot_tv_group_name.setTextSize(14);
                } else {
                    String content;
                    if (msgFlag == ZhiChiConstant.sobot_msg_flag_open) {
                        content = mContext.getString(R.string.sobot_skill_no_service);
                    } else {
                        content = mContext.getString(R.string.sobot_no_access);
                    }
                    viewHolder.sobot_tv_group_desc.setText(content);
                    viewHolder.sobot_tv_group_desc.setVisibility(View.VISIBLE);
                }
                viewHolder.sobot_ll_content.setTag(position);
            }
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private SobotRCImageView sobot_iv_group_img;
        private TextView sobot_tv_group_name;
        private TextView sobot_tv_group_desc;
        private LinearLayout sobot_ll_content;
        private ImageView iv_select;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            this.sobot_iv_group_img = itemView.findViewById(R.id.sobot_iv_group_img);
            this.sobot_tv_group_name = itemView.findViewById(R.id.sobot_tv_group_name);
            this.sobot_tv_group_desc = itemView.findViewById(R.id.sobot_tv_group_desc);
            this.sobot_ll_content = itemView.findViewById(R.id.sobot_ll_content);
            this.iv_select = itemView.findViewById(R.id.iv_select);
        }
    }


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

    public ZhiChiInitModeBase getInitModel() {
        ZhiChiInitModeBase initModel = null;
        if (mContext != null) {
            initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(mContext,
                    ZhiChiConstant.sobot_last_current_initModel);
        }
        return initModel;
    }
}