package com.sobot.chat.adapter;

import static com.sobot.chat.utils.DateUtil.DATE_TIME_FORMAT;

import android.app.Activity;
import android.graphics.Rect;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.DateUtil;

import java.util.List;

/**
 * 留言记录适配器
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotTicketInfoAdapter extends RecyclerView.Adapter {

    private Activity activity;
    private List<SobotTicketStatus> statusList;
    private List<SobotUserTicketInfo> list;
    private SobotItemListener listener;


    public SobotTicketInfoAdapter(Activity activity, List<SobotUserTicketInfo> list,SobotItemListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.list = list;
    }

    public void setStatusList(List<SobotTicketStatus> statusList) {
        this.statusList = statusList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(activity).inflate(R.layout.sobot_ticket_info_item, viewGroup, false);
        RecyclerView.ViewHolder vh = new TicketInfoViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        TicketInfoViewHolder vh = (TicketInfoViewHolder) viewHolder;
        final SobotUserTicketInfo data = list.get(i);
        if (data != null && !TextUtils.isEmpty(data.getContent())) {
            String tempStr = data.getContent().replaceAll("<br/>", "").replace("<p></p>", "")
                    .replaceAll("<p>", "").replaceAll("</p>", "").replaceAll("\n", "");
            if(tempStr.contains("<img")) {
                tempStr = tempStr.replaceAll("<img[^>]*>", " [" + activity.getResources().getString(R.string.sobot_upload) + "] ");
            }
            vh.tv_content.setText(TextUtils.isEmpty(data.getContent()) ? "" : Html.fromHtml(tempStr));
        }
        SobotTicketStatus status = getStatus(data.getTicketStatus());
        if (status != null) {
            vh.tv_ticket_status.setText(status.getCustomerStatusName());
            if (status.getStatusType() == 1 || status.getStatusType() == 2 || status.getStatusType() == 4) {
                //处理中
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_deal_text));
                vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_deal);
            } else if (status.getStatusType() == 3) {
                //带您回复
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_reply_text));
                vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_reply);
            } else if (status.getStatusType() == 5 || status.getStatusType() == 6) {
                //已解决
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_resolved_text));
                vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_resolved);
            }
        }
        vh.sobot_tv_new.setVisibility(data.isNewFlag() ? View.VISIBLE : View.GONE);
        vh.tv_time.setText(DateUtil.stringToFormatString(data.getTimeStr(), DATE_TIME_FORMAT, ZCSobotApi.getSwitchMarkStatus(MarkConfig.AUTO_MATCH_TIMEZONE)));

        displayInNotch(vh.tv_time);
        displayInNotch(vh.tv_content);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.setNewFlag(false);
                notifyDataSetChanged();
                if(listener!=null){
                    listener.onItemClick(data);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TicketInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_ticket_status;
        private TextView tv_content;
        private TextView tv_time;
        private ImageView sobot_tv_new;

        TicketInfoViewHolder(View view) {
            super(view);
            tv_ticket_status = view.findViewById(R.id.sobot_tv_ticket_status);
            tv_content = view.findViewById(R.id.sobot_tv_content);
            tv_time = view.findViewById(R.id.sobot_tv_time);
            sobot_tv_new = view.findViewById(R.id.sobot_tv_new);
        }
    }

    public SobotTicketStatus getStatus(String code) {
        if (statusList != null && statusList.size() > 0) {
            for (int i = 0; i < statusList.size(); i++) {
                if (code.equals(statusList.get(i).getStatusCode())) {
                    return statusList.get(i);
                }
            }
        }
        return null;
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(activity);
            // 设置Activity全屏
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(activity, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            view.setPadding((rect.right > 110 ? 110 : rect.right), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                        }
                    }
                }
            });

        }
    }
    public interface  SobotItemListener {
        void onItemClick(SobotUserTicketInfo model);
    }
}