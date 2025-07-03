package com.sobot.chat.adapter;

import static com.sobot.chat.utils.DateUtil.DATE_TIME_FORMAT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.StUserDealTicketReplyInfo;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.DateUtil;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotFiveStarsSmallLayout;
import com.sobot.chat.widget.StExpandableTextView;
import com.sobot.chat.widget.attachment.FileTypeConfig;

import java.util.List;

/**
 * 留言记录适配器
 *
 * @author Created by jinxl on 2019/3/7.
 */
public class SobotTicketDetailAdapter extends RecyclerView.Adapter {

    private Activity mActivity;
    private List<SobotTicketStatus> statusList;
    private List<Object> list;

    //详情头
    public static final int MSG_TYPE_HEAD = 0;
    //受理中
    public static final int MSG_TYPE_ITEM = 1;
    //评价尾
    public static final int MSG_TYPE_EVALUATE = 2;
    public static final int MSG_TYPE_NO_DATA = 3;

    SobotUploadFileAdapter.Listener listener = new SobotUploadFileAdapter.Listener() {
        @Override
        public void downFileLister(SobotFileModel fileModel) {
            // 打开详情页面
            Intent intent = new Intent(mActivity, SobotFileDetailActivity.class);
            SobotCacheFile cacheFile = new SobotCacheFile();
            cacheFile.setFileName(fileModel.getFileName());
            cacheFile.setUrl(fileModel.getFileUrl());
            cacheFile.setFileType(FileTypeConfig.getFileType(fileModel.getFileType()));
            cacheFile.setMsgId(fileModel.getFileId());
            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);

        }

        @Override
        public void previewMp4(SobotFileModel fileModel) {
            SobotCacheFile cacheFile = new SobotCacheFile();
            String name = MD5Util.encode(fileModel.getFileUrl());
            int dotIndex = fileModel.getFileUrl().lastIndexOf('.');
            if (dotIndex == -1) {
                name = name + ".mp4";
            } else {
                name = name + fileModel.getFileUrl().substring(dotIndex + 1);
            }
            cacheFile.setFileName(name);
            cacheFile.setUrl(fileModel.getFileUrl());
            cacheFile.setFileType(FileTypeConfig.getFileType(fileModel.getFileType()));
            cacheFile.setMsgId(fileModel.getFileId());
            Intent intent = SobotVideoActivity.newIntent(mActivity, cacheFile);
            mActivity.startActivity(intent);

        }

        @Override
        public void deleteFile(SobotFileModel fileModel) {

        }

        @Override
        public void previewPic(String fileUrl, String fileName) {
            if (SobotOption.imagePreviewListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(mActivity, fileUrl);
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(mActivity, SobotPhotoActivity.class);
            intent.putExtra("imageUrL", fileUrl);
            mActivity.startActivity(intent);
        }
    };

    public SobotTicketDetailAdapter(Activity activity, List list) {
        this.mActivity = activity;
        this.list = list;
    }

    public void setStatusList(List<SobotTicketStatus> statusList) {
        this.statusList = statusList;
    }

    public int getIdByName(Context context, String className,
                           String resName) {
        context = context.getApplicationContext();
        String packageName = context.getPackageName();
        int indentify = context.getResources().getIdentifier(resName,
                className, packageName);
        return indentify;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case MSG_TYPE_HEAD: {
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_head_item, null);
                holder = new HeadViewHolder(convertView);
                break;
            }
            case MSG_TYPE_ITEM: {
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_item, null);
                holder = new DetailViewHolder(convertView);
                break;
            }
            case MSG_TYPE_EVALUATE: {
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_foot_item, null);
                holder = new EvaluateViewHolder(convertView);
                break;
            }
            case MSG_TYPE_NO_DATA: {
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_no_data_item,  viewGroup, false);
                holder = new NoDataViewHolder(convertView);
                break;
            }
            default:
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_item, null);
                holder = new DetailViewHolder(convertView);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == MSG_TYPE_NO_DATA) {
            NoDataViewHolder vh = (NoDataViewHolder) viewHolder;
            displayInNotch(mActivity, vh.sobot_ll_root, 0);
        }else if (getItemViewType(position) == MSG_TYPE_HEAD) {
            HeadViewHolder vh = (HeadViewHolder) viewHolder;
            displayInNotch(mActivity, vh.tv_time, 0);
            displayInNotch(mActivity, vh.tv_exp, 0);
            Drawable drawable = mActivity.getResources().getDrawable(R.drawable.sobot_chat_msg_bg_right);
            vh.textView.setTextColor(ThemeUtils.getThemeColor(mActivity));
            vh.v_top.setBackground(ThemeUtils.applyColorToDrawable(drawable, ThemeUtils.getThemeColor(mActivity)));
            if (list.get(position) instanceof SobotUserTicketInfo) {
                final SobotUserTicketInfo data = (SobotUserTicketInfo) list.get(position);
                if (data != null && !TextUtils.isEmpty(data.getContent())) {
                    String tempStr = data.getContent().replaceAll("<br/>", "").replace("<p></p>", "")
                            .replaceAll("<p>", "").replaceAll("</p>", "<br/>").replaceAll("\n", "<br/>");
                    if(tempStr.contains("<img")) {
                        tempStr = tempStr.replaceAll("<img[^>]*>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] ");
                    }
                    vh.tv_exp.setText(TextUtils.isEmpty(data.getContent()) ? "" : Html.fromHtml(tempStr));
                }

                SobotTicketStatus status = getStatus(data.getTicketStatus());
                if (status != null) {
                    vh.tv_ticket_status.setText(status.getCustomerStatusName());
                    if (status.getCustomerStatusCode() == 1) {
                        //处理中
                        vh.tv_ticket_status.setTextColor(mActivity.getResources().getColor(R.color.sobot_ticket_deal_text));
                        vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_deal);
                    } else if (status.getCustomerStatusCode() == 2) {
                        //带您回复
                        vh.tv_ticket_status.setTextColor(mActivity.getResources().getColor(R.color.sobot_ticket_reply_text));
                        vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_reply);
                    } else if (status.getCustomerStatusCode() == 3) {
                        //已解决
                        vh.tv_ticket_status.setTextColor(mActivity.getResources().getColor(R.color.sobot_ticket_resolved_text));
                        vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_resolved);
                    } else {
                        //兜底
                        vh.tv_ticket_status.setTextColor(mActivity.getResources().getColor(R.color.sobot_ticket_deal_text));
                        vh.tv_ticket_status.setBackgroundResource(R.drawable.sobot_ticket_detail_status_deal);
                    }
                }
                vh.tv_time.setText(DateUtil.stringToFormatString(data.getTimeStr(), DATE_TIME_FORMAT, ZCSobotApi.getSwitchMarkStatus(MarkConfig.AUTO_MATCH_TIMEZONE)));
                vh.tv_exp.setHaveFile(data.getFileList() != null && data.getFileList().size() > 0);
                vh.recyclerView.setAdapter(new SobotUploadFileAdapter(mActivity, data.getFileList(),false,listener));
            }
        } else if (getItemViewType(position) == MSG_TYPE_EVALUATE) {
            EvaluateViewHolder vh = (EvaluateViewHolder) viewHolder;
            displayInNotch(mActivity, vh.sobot_my_evaluate_ll, 0);
            final SobotUserTicketEvaluate mEvaluate = (SobotUserTicketEvaluate) list.get(position);
            if (mEvaluate.isOpen()) {
                if (mEvaluate.isEvalution()) {
                    //已评价
                    vh.sobot_ll_score.setVisibility(View.VISIBLE);
                    vh.sobot_ll_remark.setVisibility(View.VISIBLE);
                    vh.sobot_my_evaluate_tv.setVisibility(View.VISIBLE);
                    vh.sobot_my_evaluate_ll.setVisibility(View.VISIBLE);
                    vh.sobot_ll_ratingBar.init(mEvaluate.getScore());
                    if (mEvaluate.getTxtFlag() == 1) {
                        vh.sobot_ll_remark.setVisibility(View.VISIBLE);
                        if (TextUtils.isEmpty(mEvaluate.getRemark())) {
                            vh.sobot_tv_remark.setText("--");
                        } else {
                            vh.sobot_tv_remark.setText(mEvaluate.getRemark());
                        }
                    } else {
                        vh.sobot_ll_remark.setVisibility(View.GONE);
                    }
                    if (mEvaluate.getIsTagFlag() == 1) {
                        vh.sobot_ll_lab.setVisibility(View.VISIBLE);
                        if (TextUtils.isEmpty(mEvaluate.getTag())) {
                            vh.sobot_tv_lab.setText("--");
                        } else {
                            vh.sobot_tv_lab.setText(mEvaluate.getTag().replace(",", "；"));
                        }
                    } else {
                        boolean showTag = false;
                        if (mEvaluate.getScoreInfo() != null && mEvaluate.getScoreInfo().size() > 0) {
                            for (int j = 0; j < mEvaluate.getScoreInfo().size(); j++) {
                                if (mEvaluate.getScoreInfo().get(j).getTags() != null && mEvaluate.getScoreInfo().get(j).getTags().size() > 0) {
                                    showTag = true;
                                }
                            }
                        }
                        if (showTag) {
                            vh.sobot_ll_lab.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(mEvaluate.getTag())) {
                                vh.sobot_tv_lab.setText("--");
                            } else {
                                vh.sobot_tv_lab.setText(mEvaluate.getTag().replace(",", "；"));
                            }
                        } else {
                            vh.sobot_ll_lab.setVisibility(View.GONE);
                        }
                    }
                    if (mEvaluate.getIsQuestionFlag() == 1) {
                        vh.sobot_ll_isSolve.setVisibility(View.VISIBLE);
                        if (mEvaluate.getDefaultQuestionFlagValue() == 0) {
                            vh.sobot_tv_isSolve.setText(R.string.sobot_evaluate_no);
                        } else if (mEvaluate.getDefaultQuestionFlagValue() == 1) {
                            vh.sobot_tv_isSolve.setText(R.string.sobot_evaluate_yes);
                        } else {
                            vh.sobot_tv_isSolve.setText("--");
                        }
                    } else {
                        vh.sobot_ll_isSolve.setVisibility(View.GONE);
                    }
                } else {
                    vh.sobot_my_evaluate_tv.setVisibility(View.GONE);
                    vh.sobot_my_evaluate_ll.setVisibility(View.GONE);
                    vh.sobot_ll_score.setVisibility(View.GONE);
                    vh.sobot_ll_remark.setVisibility(View.GONE);
                }
            } else {
                vh.sobot_my_evaluate_tv.setVisibility(View.GONE);
                vh.sobot_my_evaluate_ll.setVisibility(View.GONE);
                vh.sobot_ll_score.setVisibility(View.GONE);
                vh.sobot_ll_remark.setVisibility(View.GONE);
            }

        } else {
            DetailViewHolder vh = (DetailViewHolder) viewHolder;
//            displayInNotch(mActivity, vh.sobot_ll_root, ScreenUtils.dip2px(mActivity, 20));
            int color;
            if (position == 1) {
                vh.sobot_top_line_view.setBackgroundColor(Color.parseColor("#00000000"));
                vh.sobot_tv_icon2.setBackgroundResource(R.drawable.sobot_icon_point_old);
            } else {
                vh.sobot_tv_icon2.setBackgroundResource(R.drawable.sobot_icon_point_old);
                vh.sobot_top_line_view.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.sobot_ticket_deal_line_grey));
            }


            if (list.get(position) instanceof StUserDealTicketReplyInfo) {
                final StUserDealTicketReplyInfo reply = (StUserDealTicketReplyInfo) list.get(position);
                if (reply.getStartType() == 0) {
                    //客服
                    vh.sobot_tv_status.setVisibility(View.VISIBLE);
                    vh.sobot_tv_status.setText(R.string.sobot_ticket_service_reply);
                    if (TextUtils.isEmpty(reply.getReplyContent())) {
                        vh.sobot_tv_content_ll.setBackground(null);
                        vh.sobot_tv_content_detail.setVisibility(View.GONE);
                        vh.sobot_tv_content_detail.setOnClickListener(null);
                        vh.sobot_tv_content_detail_split.setVisibility(View.GONE);
                        vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                    } else {
                        //如果回复里包含图片（img标签），如果有，显示查看详情，并且跳转到WebViewActivity展示
                        if (StringUtils.getImgSrc(reply.getReplyContent()).size() > 0) {
                            vh.sobot_tv_content_ll.setBackgroundResource(R.drawable.sobot_round_ticket);
                            vh.sobot_tv_content_detail.setVisibility(View.VISIBLE);
                            vh.sobot_tv_content_detail_split.setVisibility(View.VISIBLE);
                            vh.sobot_tv_content.setPadding(ScreenUtils.dip2px(mActivity, 15), ScreenUtils.dip2px(mActivity, 10), ScreenUtils.dip2px(mActivity, 15), ScreenUtils.dip2px(mActivity, 10));
                            vh.sobot_tv_content_detail.setPadding(ScreenUtils.dip2px(mActivity, 15), ScreenUtils.dip2px(mActivity, 11), ScreenUtils.dip2px(mActivity, 15), ScreenUtils.dip2px(mActivity, 11));
                            vh.sobot_tv_content_detail.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mActivity, WebViewActivity.class);
                                    intent.putExtra("url", reply.getReplyContent());
                                    mActivity.startActivity(intent);
                                }
                            });
                        } else {
                            vh.sobot_tv_content_ll.setBackgroundDrawable(null);
                            vh.sobot_tv_content_detail.setVisibility(View.GONE);
                            vh.sobot_tv_content_detail.setOnClickListener(null);
                            vh.sobot_tv_content_detail_split.setVisibility(View.GONE);
                            vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                        }
                        HtmlTools.getInstance(mActivity).setRichText(vh.sobot_tv_content, reply.getReplyContent().replaceAll("<br/>", "").replaceAll("\n", "<br/>").replaceAll("<img.*?/>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] "), getLinkTextColor());
                    }


                } else {
                    //客户
                    vh.sobot_tv_content_ll.setBackgroundDrawable(null);
                    vh.sobot_tv_content_detail.setVisibility(View.GONE);
                    vh.sobot_tv_content_detail.setOnClickListener(null);
                    vh.sobot_tv_content_detail_split.setVisibility(View.GONE);
                    vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                    vh.sobot_tv_status.setVisibility(View.VISIBLE);
                    vh.sobot_tv_status.setText(R.string.sobot_ticket_me_reply);
                    vh.sobot_tv_content.setText(TextUtils.isEmpty(reply.getReplyContent()) ? mActivity.getResources().getString(R.string.sobot_nothing) : Html.fromHtml(reply.getReplyContent().replaceAll("<img.*?/>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] ")));
                }
                vh.sobot_tv_time.setText(DateUtil.toDate(reply.getReplyTime(), DateUtil.DATE_FORMAT));
                vh.recyclerView.setAdapter(new SobotUploadFileAdapter(mActivity, reply.getFileList(), false, listener));


//            } else if(item instanceof StUserDealTicketInfo){
//                sobot_tv_status.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position>=list.size()){
            return MSG_TYPE_NO_DATA;
        }
        Object data = list.get(position);
        if (data instanceof SobotUserTicketInfo) {
            return MSG_TYPE_HEAD;
        } else if (data instanceof StUserDealTicketReplyInfo) {
            return MSG_TYPE_ITEM;
        } else if (data instanceof SobotUserTicketEvaluate) {
            return MSG_TYPE_EVALUATE;
        } else if (data instanceof Boolean) {
            return MSG_TYPE_NO_DATA;
        }
        return MSG_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class HeadViewHolder extends RecyclerView.ViewHolder {
        private StExpandableTextView tv_exp;
        private TextView textView;

        private TextView tv_time;
        private TextView tv_ticket_status;
        private RecyclerView recyclerView;
        private View v_top;

        HeadViewHolder(View view) {
            super(view);
            tv_exp = (StExpandableTextView) view.findViewById(R.id.sobot_content_fl);
            v_top = view.findViewById(R.id.v_top);
            textView = tv_exp.getTextBtn();
            tv_exp.setOnExpandStateChangeListener(new StExpandableTextView.OnExpandStateChangeListener() {
                @Override
                public void onExpandStateChanged(TextView text, boolean isExpanded) {
                    if (isExpanded) {//展开
                        textView.setText(R.string.sobot_notice_collapse);
                    } else {
                        textView.setText(R.string.sobot_notice_expand_all);
                    }
                }
            });
            textView.setText(R.string.sobot_notice_expand_all);
            tv_time = (TextView) view.findViewById(R.id.sobot_tv_time);
            ViewGroup otherGroup = tv_exp.getmOtherView();
            if (otherGroup != null) {
                recyclerView = (RecyclerView) otherGroup.findViewById(R.id.sobot_attachment_file_layout);
                LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
                // 设置RecyclerView的LayoutManager
                recyclerView.setLayoutManager(layoutManager);
            }
            tv_ticket_status = (TextView) view.findViewById(R.id.sobot_tv_ticket_status);

        }
    }


    class DetailViewHolder extends RecyclerView.ViewHolder {
        private TextView sobot_tv_time;
        private TextView sobot_tv_icon2;
        private TextView sobot_tv_status;
        private TextView sobot_tv_content;
        private TextView sobot_tv_content_detail;
        private View sobot_tv_content_detail_split;
        private LinearLayout sobot_ll_container;
        private LinearLayout sobot_tv_content_ll;
        private View sobot_top_line_view;
        //        private LinearLayout sobot_ll_root;
        private RecyclerView recyclerView;

        DetailViewHolder(View view) {
            super(view);
//            sobot_ll_root = (LinearLayout) view.findViewById(R.id.sobot_ll_root);
            sobot_tv_icon2 = (TextView) view.findViewById(R.id.sobot_tv_icon2);
            sobot_tv_status = (TextView) view.findViewById(R.id.sobot_tv_status);
            sobot_tv_time = (TextView) view.findViewById(R.id.sobot_tv_time);
            sobot_tv_content_ll = (LinearLayout) view.findViewById(R.id.sobot_tv_content_ll);
            sobot_tv_content = (TextView) view.findViewById(R.id.sobot_tv_content);
            sobot_tv_content_detail_split = view.findViewById(R.id.sobot_tv_content_detail_split);
            sobot_tv_content_detail = (TextView) view.findViewById(R.id.sobot_tv_content_detail);
            sobot_tv_content_detail.setText(R.string.sobot_see_detail);
            sobot_ll_container = (LinearLayout) view.findViewById(R.id.sobot_ll_container);
            sobot_top_line_view = view.findViewById(R.id.sobot_top_line_view);
            recyclerView = (RecyclerView) view.findViewById(R.id.sobot_attachment_file_layout);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            // 设置RecyclerView的LayoutManager
            recyclerView.setLayoutManager(layoutManager);
        }
    }


    class NoDataViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sobot_ll_root;
        NoDataViewHolder(View view){
            super(view);
            sobot_ll_root = view.findViewById(R.id.sobot_ll_root);

        }
    }
    class EvaluateViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sobot_ll_score;
        private TextView sobot_tv_remark;
        private LinearLayout sobot_ll_remark;
        private SobotFiveStarsSmallLayout sobot_ll_ratingBar;
        private TextView sobot_my_evaluate_tv;
        private TextView sobot_tv_my_evaluate_score;
        private TextView sobot_tv_my_evaluate_remark;

        private LinearLayout sobot_my_evaluate_ll;
        private LinearLayout sobot_ll_lab;
        private LinearLayout sobot_ll_isSolve;
        private TextView sobot_tv_isSolve, sobot_tv_lab;

        EvaluateViewHolder(View view) {
            super(view);
            sobot_ll_score = (LinearLayout) view.findViewById(R.id.sobot_ll_score);
            sobot_ll_lab = (LinearLayout) view.findViewById(R.id.sobot_ll_lab);
            sobot_ll_isSolve = (LinearLayout) view.findViewById(R.id.sobot_ll_isSolve);
            sobot_tv_isSolve = (TextView) view.findViewById(R.id.sobot_tv_isSolve);
            sobot_tv_lab = (TextView) view.findViewById(R.id.sobot_tv_lab);
            sobot_tv_remark = (TextView) view.findViewById(R.id.sobot_tv_remark);
            sobot_ll_remark = (LinearLayout) view.findViewById(R.id.sobot_ll_remark);
            sobot_ll_ratingBar = view.findViewById(R.id.sobot_ratingBar);
            sobot_my_evaluate_tv = (TextView) view.findViewById(R.id.sobot_my_evaluate_tv);
            sobot_my_evaluate_tv.setText(R.string.sobot_my_service_comment);
            sobot_tv_my_evaluate_score = (TextView) view.findViewById(R.id.sobot_tv_my_evaluate_score);
            sobot_tv_my_evaluate_score.setText(mActivity.getResources().getString(R.string.sobot_rating_score) + "：");
            sobot_tv_my_evaluate_remark = (TextView) view.findViewById(R.id.sobot_tv_my_evaluate_remark);
            sobot_tv_my_evaluate_remark.setText(mActivity.getResources().getString(R.string.sobot_rating_dec) + "：");
            sobot_my_evaluate_ll = (LinearLayout) view.findViewById(R.id.sobot_my_evaluate_ll);
        }
    }

    //左右两边气泡内链接文字的字体颜色
    protected int getLinkTextColor() {
        return R.color.sobot_color_link;
    }

    public void displayInNotch(Activity mActivity, final View view, final int addPaddingLeft) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(mActivity);
            // 设置Activity全屏
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(mActivity, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            view.setPadding((rect.right > 110 ? 110 : rect.right) + addPaddingLeft, view.getPaddingTop(), (rect.right > 110 ? 110 : rect.right) + view.getPaddingRight(), view.getPaddingBottom());
                        }
                    }
                }
            });

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

}