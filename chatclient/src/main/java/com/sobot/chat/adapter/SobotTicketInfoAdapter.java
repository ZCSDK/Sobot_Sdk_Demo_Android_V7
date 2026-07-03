package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.PersistableBundle;
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
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.WebViewSecurityUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.toast.ToastUtil;

import java.util.List;
import java.util.Locale;

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


    public SobotTicketInfoAdapter(Activity activity, List<SobotUserTicketInfo> list, SobotItemListener listener) {
        this.activity = activity;
        this.listener = listener;
        this.list = list;
        statusList = ChatUtils.getStatusList();
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
        if (data != null && !TextUtils.isEmpty(data.getTicketTitle())) {
            vh.tv_title.setText(data.getTicketTitle());
        }
        if (data != null && !TextUtils.isEmpty(data.getTicketCode())) {
            vh.tv_ticket_code.setText("#" + data.getTicketCode());
            vh.tv_ticket_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyTicket(data);
                }
            });
        }
        if (data != null && !TextUtils.isEmpty(data.getContent())) {
            String tempStr = data.getContent().replaceAll("<br/>", "").replace("<p></p>", "")
                    .replaceAll("<p>", "").replaceAll("</p>", "").replaceAll("\n", "");
            if (tempStr.contains("<img")) {
                tempStr = tempStr.replaceAll("<img[^>]*>", " [" + activity.getResources().getString(R.string.sobot_upload) + "] ");
            }
            vh.tv_content.setText(TextUtils.isEmpty(data.getContent()) ? "" : WebViewSecurityUtil.safeFromHtml(tempStr));
        }
        SobotTicketStatus status = getStatus(data.getTicketStatus());
        if (status != null) {
            vh.tv_ticket_status.setText(status.getCustomerStatusName());
            if (status.getStatusType() == 1 || status.getStatusType() == 2 || status.getStatusType() == 4) {
                //处理中
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_deal_text));
            } else if (status.getStatusType() == 3) {
                //带您回复
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_reply_text));
            } else if (status.getStatusType() == 5 || status.getStatusType() == 6) {
                //已解决
                vh.tv_ticket_status.setTextColor(activity.getResources().getColor(R.color.sobot_ticket_resolved_text));
            }
        } else {
            LogUtils.d((statusList == null) + "==" + data.getTicketStatus());
            vh.tv_ticket_status.setText("");
        }
        vh.sobot_tv_new.setVisibility(data.isNewFlag() ? View.VISIBLE : View.GONE);
        Locale locale = (Locale) SharedPreferencesUtil.getObject(activity, ZhiChiConstant.SOBOT_LANGUAGE);
        String formatString = DateUtil.getDateTimePatternByLanguage(locale, true);
        vh.tv_time.setText(DateUtil.longStrToDateStr(data.getTime(), formatString, locale));
        if (i > 3 && i == list.size() - 1) {
            vh.v_end.setVisibility(View.VISIBLE);
        } else {
            vh.v_end.setVisibility(View.GONE);
        }


        displayInNotch(vh.tv_time);
        displayInNotch(vh.tv_content);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.setNewFlag(false);
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onItemClick(data);
                }
            }
        });
    }

    //复制工单编号
    private void copyTicket(SobotUserTicketInfo data) {
        if (activity != null && data != null && !TextUtils.isEmpty(data.getTicketCode())) {
            ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cmb != null) {
                ClipData clipData = ClipData.newPlainText("ticket code", data.getTicketCode());
                // 安全：API 33+ 标记剪贴板内容为敏感，系统通知/输入法/无障碍服务不显示明文预览（CWE-200）。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PersistableBundle extras = new PersistableBundle();
                    extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
                    clipData.getDescription().setExtras(extras);
                }
                cmb.setPrimaryClip(clipData);
            }
            ToastUtil.showCustomToast(activity, activity.getResources().getString(R.string.sobot_ctrl_ticket_success));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TicketInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_ticket_status;
        private TextView tv_ticket_code;
        private TextView tv_content, tv_title;
        private TextView tv_time;
        private ImageView sobot_tv_new;
        private View v_end;

        TicketInfoViewHolder(View view) {
            super(view);
            tv_ticket_status = view.findViewById(R.id.sobot_tv_ticket_status);
            tv_content = view.findViewById(R.id.sobot_tv_content);
            tv_ticket_code = view.findViewById(R.id.tv_ticket_code);
            tv_title = view.findViewById(R.id.sobot_tv_title);
            tv_time = view.findViewById(R.id.sobot_tv_time);
            sobot_tv_new = view.findViewById(R.id.sobot_tv_new);
            v_end = view.findViewById(R.id.v_end);
        }
    }

    public SobotTicketStatus getStatus(int code) {
        if (statusList != null && statusList.size() > 0) {
            for (int i = 0; i < statusList.size(); i++) {
                if (code == statusList.get(i).getStatusCode()) {
                    return statusList.get(i);
                }
            }
        }
        return null;
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 横屏卡片样式（layout-w600dp，sobot_list_span_count=2）下，每个 item 是固定宽度卡片，
            // 不撑满屏幕宽度，刘海区域由 RecyclerView padding 统一避让，无需再对 TextView 单独加 paddingLeft；
            // 否则会让 tv_content / tv_time 的内容被强制右推，视觉上"内容前面留白 + 文字居右"
            if (activity != null
                    && activity.getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
                return;
            }
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

    public interface SobotItemListener {
        void onItemClick(SobotUserTicketInfo model);
    }
}