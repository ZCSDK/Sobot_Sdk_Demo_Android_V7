package com.sobot.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.sobot.chat.api.model.StTicketDetailInfo;
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
import com.sobot.chat.widget.SobotGridSpacingItemDecoration;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotProgressImageView;

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
            // 打开文件详情页面
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
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_evaluate_item, null);
                holder = new EvaluateViewHolder(convertView);
                break;
            }
            case MSG_TYPE_NO_DATA: {
                View convertView = LayoutInflater.from(mActivity).inflate(R.layout.sobot_ticket_detail_no_data_item, viewGroup, false);
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
        } else if (getItemViewType(position) == MSG_TYPE_HEAD) {
            HeadViewHolder vh = (HeadViewHolder) viewHolder;
            displayInNotch(mActivity, vh.tv_time, 0);
            if (list.get(position) instanceof StTicketDetailInfo) {
                final StTicketDetailInfo data = (StTicketDetailInfo) list.get(position);
                if (data != null && !TextUtils.isEmpty(data.getTicketTitle())) {
                    vh.tv_ticket_title.setText(data.getTicketTitle());
                }
                if (data != null && !TextUtils.isEmpty(data.getTicketContent())) {
                    String tempStr = data.getTicketContent().replaceAll("<br/>", "").replace("<p></p>", "")
                            .replaceAll("<p>", "").replaceAll("</p>", "<br/>").replaceAll("\n", "<br/>");
                    if (tempStr.contains("<img")) {
                        tempStr = tempStr.replaceAll("<img[^>]*>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] ");
                    }
                    vh.tv_ticket_content.setText(TextUtils.isEmpty(data.getTicketContent()) ? "" : Html.fromHtml(tempStr));
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
                vh.tv_time.setText(DateUtil.getTimeStr(mActivity, data.getTicketCreateTime()));

                if (null != data.getFileList() && !data.getFileList().isEmpty()) {
                    vh.recyclerView.setVisibility(View.VISIBLE);
                    vh.recyclerView.setAdapter(new SobotUploadFileAdapter(mActivity, data.getFileList(), false, listener));
                } else {
                    vh.recyclerView.setVisibility(View.GONE);
                }
            }
        } else if (getItemViewType(position) == MSG_TYPE_EVALUATE) {
            EvaluateViewHolder vh = (EvaluateViewHolder) viewHolder;
            final StUserDealTicketReplyInfo mEvaluate = (StUserDealTicketReplyInfo) list.get(position);
            vh.sobot_tv_name.setText(R.string.sobot_str_my);
            vh.iv_head.setImageLocal(R.drawable.sobot_tiket_item_me);
            vh.sobot_tv_time.setText(DateUtil.getTimeStr(mActivity, mEvaluate.getReplyTime()));

            //已评价
            // 创建加粗样式（Typeface.BOLD 表示加粗，Typeface.NORMAL 表示正常）
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

            if (mEvaluate.getQuestionFlag() >= 0) {
                vh.sobot_tv_isSolve.setVisibility(View.VISIBLE);
                String solve = mActivity.getResources().getText(R.string.sobot_evaluate_issolve).toString();
                int solveLen = solve.length();
                if (mEvaluate.getQuestionFlag() == 0) {
                    solve +=mActivity.getResources().getText(R.string.sobot_evaluate_no).toString();
                } else if (mEvaluate.getQuestionFlag() == 1) {
                    solve +=mActivity.getResources().getText(R.string.sobot_evaluate_yes).toString();
                }
                SpannableString spannableString = new SpannableString(solve);
                spannableString.setSpan(boldSpan, 0, solveLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                vh.sobot_tv_isSolve.setText(spannableString);
            } else {
                vh.sobot_tv_isSolve.setVisibility(View.GONE);
            }
            vh.sobot_ll_ratingBar.init(mEvaluate.getScore());

            if (StringUtils.isNoEmpty(mEvaluate.getRemark())) {
               String remark = mActivity.getResources().getString(R.string.sobot_rating_dec) + "：";
                int remarkLen = remark.length();
                remark +=mEvaluate.getRemark();
                vh.sobot_tv_remark.setVisibility(View.VISIBLE);
                SpannableString spannableString = new SpannableString(remark);
                spannableString.setSpan(boldSpan, 0, remarkLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                vh.sobot_tv_remark.setText(spannableString);
            } else {
                vh.sobot_tv_remark.setVisibility(View.GONE);
            }

            if (null != mEvaluate.getTags() && !mEvaluate.getTags().isEmpty()) {
                vh.sobot_tv_lab.setVisibility(View.VISIBLE);
                String leb = mActivity.getResources().getString(R.string.sobot_evaluate_lab);
                int lebLen = leb.length();
                StringBuilder stringBuilder = new StringBuilder(leb);
                for (int i = 0; i < mEvaluate.getTags().size(); i++) {
                    stringBuilder.append(mEvaluate.getTags().get(i));
                    if (i < mEvaluate.getTags().size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                SpannableString spannableString = new SpannableString(stringBuilder.toString());
                spannableString.setSpan(boldSpan, 0, lebLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                vh.sobot_tv_lab.setText(spannableString);
            } else {
                vh.sobot_tv_lab.setVisibility(View.GONE);
            }
        } else {
            DetailViewHolder vh = (DetailViewHolder) viewHolder;

            if (list.get(position) instanceof StUserDealTicketReplyInfo) {
                final StUserDealTicketReplyInfo reply = (StUserDealTicketReplyInfo) list.get(position);
                if (reply.getStartType() == 1) {
                    //我
                    vh.sobot_tv_name.setText(R.string.sobot_str_my);
                    vh.iv_head.setImageLocal(R.drawable.sobot_tiket_item_me);
                    vh.sobot_tv_content_detail.setVisibility(View.GONE);
                    vh.sobot_tv_content_detail.setOnClickListener(null);
                    vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                    vh.sobot_tv_content.setText(TextUtils.isEmpty(reply.getReplyContent()) ? mActivity.getResources().getString(R.string.sobot_nothing) : Html.fromHtml(reply.getReplyContent().replaceAll("<img.*?/>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] ")));
                } else {
                    if (StringUtils.isNoEmpty(reply.getUpdateUserName())) {
                        //客服
                        vh.sobot_tv_name.setText(reply.getUpdateUserName());
                        //默认头像
                        int bgColor = ThemeUtils.getThemeColor(mActivity);
                        Drawable afaceDrawable = ThemeUtils.createTextImageDrawable(mActivity, reply.getUpdateUserName(), (int) mActivity.getResources().getDimension(R.dimen.sobot_tiket_head_w), (int) mActivity.getResources().getDimension(R.dimen.sobot_tiket_head_w), bgColor, ThemeUtils.getThemeTextAndIconColor(mActivity));
                        if (afaceDrawable != null) {
                            vh.iv_head.setImageDrawable(afaceDrawable);
                            vh.iv_head.setVisibility(View.VISIBLE);
                            vh.iv_head.setRoundAsCircle(true);
                        }
                    }
                    if (TextUtils.isEmpty(reply.getReplyContent())) {
                        vh.sobot_tv_content_detail.setVisibility(View.GONE);
                        vh.sobot_tv_content_detail.setOnClickListener(null);
                        vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                    } else {
                        //如果回复里包含图片（img标签），如果有，显示查看详情，并且跳转到WebViewActivity展示
                        if (StringUtils.getImgSrc(reply.getReplyContent()).size() > 0) {
                            vh.sobot_tv_content_detail.setVisibility(View.VISIBLE);
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
                            vh.sobot_tv_content_detail.setVisibility(View.GONE);
                            vh.sobot_tv_content_detail.setOnClickListener(null);
                            vh.sobot_tv_content.setPadding(0, 0, 0, 0);
                        }
                        HtmlTools.getInstance(mActivity).setRichText(vh.sobot_tv_content, reply.getReplyContent().replaceAll("<br/>", "").replaceAll("\n", "<br/>").replaceAll("<img.*?/>", " [" + mActivity.getResources().getString(R.string.sobot_upload) + "] "), getLinkTextColor());
                    }
                }

                vh.sobot_tv_time.setText(DateUtil.getTimeStr(mActivity, reply.getReplyTime()));
                if (null != reply.getFileList() && !reply.getFileList().isEmpty()) {
                    vh.recyclerView.setVisibility(View.VISIBLE);
                    vh.recyclerView.setAdapter(new SobotUploadFileAdapter(mActivity, reply.getFileList(), false, listener));
                } else {
                    vh.recyclerView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= list.size()) {
            return MSG_TYPE_NO_DATA;
        }
        Object data = list.get(position);
        if (data instanceof StTicketDetailInfo) {
            return MSG_TYPE_HEAD;
        } else if (data instanceof StUserDealTicketReplyInfo) {
            if (((StUserDealTicketReplyInfo) data).getItemType() == 2) {
                return MSG_TYPE_EVALUATE;
            } else {
                return MSG_TYPE_ITEM;
            }
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

        private TextView tv_time;
        private TextView tv_ticket_status;
        private TextView tv_ticket_title;
        private TextView tv_ticket_content;
        private RecyclerView recyclerView;

        HeadViewHolder(View view) {
            super(view);
            tv_time = (TextView) view.findViewById(R.id.sobot_tv_time);
            recyclerView = (RecyclerView) view.findViewById(R.id.sobot_attachment_file_layout);
            tv_ticket_status = (TextView) view.findViewById(R.id.sobot_tv_ticket_status);
            tv_ticket_title = (TextView) view.findViewById(R.id.tv_title);
            tv_ticket_content = (TextView) view.findViewById(R.id.tv_context);
            GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 2); // 创建GridLayoutManager，参数为列数
            // 设置RecyclerView的LayoutManager
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SobotGridSpacingItemDecoration(2, ScreenUtils.dip2px(mActivity, 8), false));


        }
    }


    class DetailViewHolder extends RecyclerView.ViewHolder {
        private TextView sobot_tv_time;
        private TextView sobot_tv_name;
        private TextView sobot_tv_content;
        private TextView sobot_tv_content_detail;
        private SobotProgressImageView iv_head;
        private RecyclerView recyclerView;

        DetailViewHolder(View view) {
            super(view);
            iv_head = view.findViewById(R.id.iv_head);
            sobot_tv_name = (TextView) view.findViewById(R.id.sobot_tv_name);
            sobot_tv_time = (TextView) view.findViewById(R.id.sobot_tv_time);
            sobot_tv_content = (TextView) view.findViewById(R.id.sobot_tv_content);
            sobot_tv_content_detail = (TextView) view.findViewById(R.id.sobot_tv_content_detail);
            sobot_tv_content_detail.setText(R.string.sobot_see_detail);
            recyclerView = (RecyclerView) view.findViewById(R.id.sobot_attachment_file_layout);
            GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 2); // 创建GridLayoutManager，参数为列数
            // 设置RecyclerView的LayoutManager
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SobotGridSpacingItemDecoration(2, ScreenUtils.dip2px(mActivity, 8), false));
        }
    }


    class NoDataViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout sobot_ll_root;

        NoDataViewHolder(View view) {
            super(view);
            sobot_ll_root = view.findViewById(R.id.sobot_ll_root);

        }
    }

    class EvaluateViewHolder extends RecyclerView.ViewHolder {
        private TextView sobot_tv_remark;
        private SobotFiveStarsSmallLayout sobot_ll_ratingBar;
        private TextView sobot_tv_my_evaluate_score;

        private TextView sobot_tv_isSolve, sobot_tv_lab;
        private TextView sobot_tv_time;
        private TextView sobot_tv_name;
        private SobotProgressImageView iv_head;

        EvaluateViewHolder(View view) {
            super(view);
            iv_head = view.findViewById(R.id.iv_head);
            sobot_tv_name = (TextView) view.findViewById(R.id.sobot_tv_name);
            sobot_tv_time = (TextView) view.findViewById(R.id.sobot_tv_time);

            sobot_tv_isSolve = (TextView) view.findViewById(R.id.sobot_tv_isSolve);
            sobot_tv_lab = (TextView) view.findViewById(R.id.sobot_tv_lab);
            sobot_tv_remark = (TextView) view.findViewById(R.id.sobot_tv_remark);

            sobot_ll_ratingBar = view.findViewById(R.id.sobot_ratingBar);
            sobot_tv_my_evaluate_score = (TextView) view.findViewById(R.id.sobot_tv_my_evaluate_score);
            sobot_tv_my_evaluate_score.setText(mActivity.getResources().getString(R.string.sobot_rating_score) + "：");
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

}