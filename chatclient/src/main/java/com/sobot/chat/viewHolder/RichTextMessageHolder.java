package com.sobot.chat.viewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 富文本消息
 */
public class RichTextMessageHolder extends MsgHolderBase implements View.OnClickListener {
    private TextView msg; // 聊天的消息内容
    private LinearLayout sobot_rich_ll;//拆分的富文本消息
    private TextView sobot_msgStripe; // 多轮会话中配置的引导语


    private LinearLayout sobot_ll_switch;//换一组按钮
    private TextView sobot_tv_switch;
    private View sobot_view_split;//换一组和查看详情分割线

    private ProgressBar progressbar_loading;


    public RichTextMessageHolder(Context context, View convertView) {
        super(context, convertView);
        msg = (TextView) convertView.findViewById(R.id.sobot_msg);
        sobot_rich_ll = (LinearLayout) convertView.findViewById(R.id.sobot_rich_ll);
        sobot_msgStripe = (TextView) convertView.findViewById(R.id.sobot_msgStripe);
        sobot_ll_switch = (LinearLayout) convertView.findViewById(R.id.sobot_ll_switch);
        sobot_tv_switch = (TextView) convertView.findViewById(R.id.sobot_tv_switch);
        sobot_tv_switch.setText(R.string.sobot_switch);
        sobot_view_split = convertView.findViewById(R.id.sobot_view_split);
        answersList = (LinearLayout) convertView
                .findViewById(R.id.sobot_answersList);
        progressbar_loading = (ProgressBar) convertView
                .findViewById(R.id.progressbar_loading);
        sobot_ll_switch.setOnClickListener(this);
    }

    @Override
    public void bindData(Context context, ZhiChiMessageBase message) {
        this.mContext = context;
        this.message = message;
        if (message != null && StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant()) && progressbar_loading != null) {
            //如果是aiagent 答案
            if (message.getAnswer() != null && message.getAnswer().getRichList() != null && !message.getAnswer().getRichList().isEmpty() && (StringUtils.isNoEmpty(message.getAnswer().getRichList().get(0).getMsg()))) {
                progressbar_loading.setVisibility(View.GONE);
            } else {
                progressbar_loading.setVisibility(View.VISIBLE);
                if (sobot_msg_content_ll != null && mContext != null) {
                    //四个角弧度一样
                    Drawable bg = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_chat_msg_bg_left_second, null);
                    if (bg != null) {
                        sobot_msg_content_ll.setBackground(bg);
                    }
                }
            }
        } else {
            if (progressbar_loading != null) {
                progressbar_loading.setVisibility(View.GONE);
            }
        }
        // 更具消息类型进行对布局的优化
        if (message.getAnswer() != null) {
            setupMsgContent(context, message);
            sobot_msgStripe.setVisibility(View.GONE);
        }

        if (message.isGuideGroupFlag()//有分组
                && message.getListSuggestions() != null//有分组问题列表
                && message.getGuideGroupNum() > -1//分组不是全部
                && message.getListSuggestions().size() > 0//问题数量大于0
                && message.getGuideGroupNum() < message.getListSuggestions().size()//分组数量小于问题数量
        ) {
            sobot_ll_switch.setVisibility(View.VISIBLE);
            sobot_view_split.setVisibility(View.VISIBLE);
        } else {
            sobot_ll_switch.setVisibility(View.GONE);
            sobot_view_split.setVisibility(View.GONE);
        }

        if (!isRight) {
            msg.setMinHeight(0);
            refreshItem();//左侧消息刷新顶和踩布局
            checkShowTransferBtn();//检查转人工逻辑
            //关联问题显示逻辑
            if (message != null && message.getSugguestions() != null && message.getSugguestions().length > 0) {
                resetAnswersList();
            } else {
                hideAnswers();
            }
            msg.setMaxWidth(msgMaxWidth);
//            sobot_rich_ll.setLayoutParams(new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        setLongClickListener(msg);
        setLongClickListener(sobot_msg_content_ll);
        refreshReadStatus();
    }

    /**
     * 显示 顶踩 按钮
     */
    public void showRevaluateBtn() {
        super.showRevaluateBtn();
        if (dingcaiIsShowRight()) {
            //有顶和踩时显示信息显示两行 64-12-12=40 总高度减去上下内间距
            msg.setMinHeight(ScreenUtils.dip2px(mContext, 44));
            //有顶和踩时,拆分后的富文本消息如果只有一个并且是文本类型设置最小高度 64-12-12=40 总高度减去上下内间距
            if (sobot_rich_ll != null && sobot_rich_ll.getChildCount() == 1) {
                for (int i = 0; i < sobot_rich_ll.getChildCount(); i++) {
                    View view = sobot_rich_ll.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setMinHeight(ScreenUtils.dip2px(mContext, 44));
                    }
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v == sobot_ll_switch) {
            // 换一组
            if (message != null && message.getListSuggestions() != null
                    && message.getListSuggestions().size() > 0) {
                int pageNum = message.getCurrentPageNum() + 1;
                int total = message.getListSuggestions().size();
                int pre = message.getGuideGroupNum();
                if (pre == 0) {
                    pre = 5;
                }
                int maxNum = (total % pre == 0) ? (total / pre) : (total / pre + 1);
                pageNum = (pageNum >= maxNum) ? 0 : pageNum;
                message.setCurrentPageNum(pageNum);
                resetAnswersList();
            }


        }
    }


    private void setupMsgContent(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && message.getAnswer().getRichList() != null && message.getAnswer().getRichList().size() > 0) {
            sobot_rich_ll.removeAllViews();
            try {
                if (message.getAnswer().getRichList().size() > 1) {
                    //richList 数量大于1个，如果里边有不是卡片的超链接，超链接的上个又是文本的情况，需要单独处理（合并到上个文本后边）
                    List<ChatMessageRichListModel> tempRichList = new ArrayList<>();
                    for (int i = 0; i < message.getAnswer().getRichList().size(); i++) {
                        //处理后的临时richList,替换旧的richList
                        ChatMessageRichListModel richListModel = message.getAnswer().getRichList().get(i);
                        if (richListModel != null) {
                            //如果当前是文本,文本又不是卡片，需要处理
                            if (richListModel.getType() == 0 && richListModel.getShowType() != 1) {
                                ChatMessageRichListModel model = new ChatMessageRichListModel();
                                model.setType(0);
                                if (!tempRichList.isEmpty()) {
                                    //如果上一个是文本,需要合并当前文本到上个文本后边
                                    ChatMessageRichListModel tempRichListModel = tempRichList.get(tempRichList.size() - 1);
                                    if (tempRichListModel != null && tempRichListModel.getType() == 0) {
                                        if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                            model.setMsg(tempRichListModel.getMsg() + "<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                        } else {
                                            model.setMsg(tempRichListModel.getMsg() + richListModel.getMsg());
                                        }
                                        tempRichList.remove(tempRichList.size() - 1);
                                        tempRichList.add(model);
                                    } else {
                                        tempRichList.add(richListModel);
                                    }
                                } else {
                                    if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                        //当前是超链接，同时又不是卡片
                                        model.setMsg("<a href=\"" + richListModel.getMsg() + "\">" + richListModel.getName() + "</a>");
                                    } else {
                                        model.setMsg(richListModel.getMsg());
                                    }
                                    tempRichList.add(model);
                                }
                            } else {
                                tempRichList.add(richListModel);
                            }
                        }
                    }
                    if (!tempRichList.isEmpty()) {
                        message.getAnswer().setRichList(tempRichList);
                    }
                }
            } catch (Exception e) {
            }
            for (int i = 0; i < message.getAnswer().getRichList().size(); i++) {
                final ChatMessageRichListModel richListModel = message.getAnswer().getRichList().get(i);
                if (richListModel != null) {
                    //如果最后一个是空行，直接过滤掉不显示
                    if (TextUtils.isEmpty(richListModel.getMsg()) && i == (message.getAnswer().getRichList().size() - 1)) {
                        continue;
                    }
                    // 0：文本，1：图片，2：音频，3：视频，4：文件
                    if (richListModel.getType() == 0) {
                        TextView textView = new TextView(mContext);
                        textView.setIncludeFontPadding(false);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                        textView.setLineSpacing(mContext.getResources().getDimension(R.dimen.sobot_text_line_spacing_extra), 1);
                        if (i != 0) {
                            LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            wlayoutParams.setMargins(0, ScreenUtils.dip2px(context, 10), 0, 0);
                            textView.setLayoutParams(wlayoutParams);
                        } else {
                            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                        }
                        textView.setMaxWidth(msgMaxWidth);
                        setLongClickListener(textView);
                        if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                            try {
                                textView.setTextColor(getLinkTextColor());
                            } catch (Exception e) {
                            }
//                            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
//                            textView.getPaint().setAntiAlias(true);//抗锯齿
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (SobotOption.newHyperlinkListener != null) {
                                        //如果返回true,拦截;false 不拦截
                                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, richListModel.getMsg());
                                        if (isIntercept) {
                                            return;
                                        }
                                    }
                                    Intent intent = new Intent(context, WebViewActivity.class);
                                    intent.putExtra("url", richListModel.getMsg());
                                    context.startActivity(intent);
                                }
                            });
                            textView.setText(richListModel.getName());
                            sobot_rich_ll.addView(textView);
                            if (richListModel.getShowType() == 1) {
                                //超链接，并且是卡片形式才显示卡片
                                final View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_link_card, null);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgCardWidth - 12, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, ScreenUtils.dip2px(mContext, 4));
                                view.setLayoutParams(layoutParams);
                                TextView tv_title = view.findViewById(R.id.tv_title);
                                tv_title.setText(R.string.sobot_parsing);
                                if (richListModel.getSobotLink() != null) {
                                    tv_title = view.findViewById(R.id.tv_title);
                                    TextView tv_des = view.findViewById(R.id.tv_des);
                                    ImageView image_link = view.findViewById(R.id.image_link);
                                    if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle())) {
                                        tv_title.setVisibility(View.GONE);
                                    } else {
                                        tv_title.setText(richListModel.getSobotLink().getTitle());
                                        tv_title.setVisibility(View.VISIBLE);
                                    }
                                    tv_des.setText(TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) ? richListModel.getMsg() : richListModel.getSobotLink().getDesc());
                                    SobotBitmapUtil.display(mContext, richListModel.getSobotLink().getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                    if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle()) && TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) && TextUtils.isEmpty(richListModel.getSobotLink().getImgUrl())) {
                                        view.setVisibility(View.GONE);
                                    }
                                } else {
                                    SobotMsgManager.getInstance(mContext).getZhiChiApi().getHtmlAnalysis(context, richListModel.getMsg(), new StringResultCallBack<SobotLink>() {
                                        @Override
                                        public void onSuccess(SobotLink link) {
                                            if (link != null) {
                                                richListModel.setSobotLink(link);
                                                TextView tv_title = view.findViewById(R.id.tv_title);
                                                TextView tv_des = view.findViewById(R.id.tv_des);
                                                ImageView image_link = view.findViewById(R.id.image_link);
                                                if (TextUtils.isEmpty(link.getTitle())) {
                                                    tv_title.setVisibility(View.VISIBLE);
                                                    tv_title.setText(richListModel.getName());
                                                } else {
                                                    tv_title.setText(link.getTitle());
                                                    tv_title.setVisibility(View.VISIBLE);
                                                }
                                                tv_des.setText(TextUtils.isEmpty(link.getDesc()) ? richListModel.getMsg() : link.getDesc());
                                                if (mContext != null && !isActivityDestroyed(mContext)) {
                                                    SobotBitmapUtil.display(mContext, link.getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e, String s) {
                                            if (view != null) {
                                                TextView tv_title = view.findViewById(R.id.tv_title);
                                                tv_title.setText(richListModel.getMsg());
                                                ImageView image_link = view.findViewById(R.id.image_link);
                                                SobotBitmapUtil.display(mContext, "", image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                            }
                                        }
                                    });
                                }
                                sobot_rich_ll.addView(view);
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (SobotOption.newHyperlinkListener != null) {
                                            //如果返回true,拦截;false 不拦截
                                            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(mContext, richListModel.getMsg());
                                            if (isIntercept) {
                                                return;
                                            }
                                        }
                                        Intent intent = new Intent(context, WebViewActivity.class);
                                        intent.putExtra("url", richListModel.getMsg());
                                        context.startActivity(intent);
                                    }
                                });
                            }
                        } else {
                            textView.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_left_msg_text_color));
                            if (!TextUtils.isEmpty(richListModel.getMsg()) && i == (message.getAnswer().getRichList().size() - 1)) {
                                //如果是richlist的最后一个，把这个的尾部的<br/>都去掉
                                String content = richListModel.getMsg().trim();
                                while (content.length() > 5 && "<br/>".equals(content.substring(content.length() - 5, content.length()))) {
                                    content = content.substring(0, content.length() - 5);
                                }
                                HtmlTools.getInstance(mContext).setRichText(textView, content, getLinkTextColor());
                            } else {
                                HtmlTools.getInstance(mContext).setRichText(textView, richListModel.getMsg(), getLinkTextColor());
                            }
                            sobot_rich_ll.addView(textView);
                        }
                    } else if (richListModel.getType() == 1) {
                        int imgHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
                        if (StringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant()) && !message.isAiAgentReceiveMsgEnd()) {
                            //图片如果是大模型消息里，同时消息还在接收，图片高度固定
                            imgHeight = 500;
                        }
                        View imageView = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_rich_image_view, sobot_rich_ll, false);
                        SobotProgressImageView image = imageView.findViewById(R.id.sobot_iv_picture);
                        if (!TextUtils.isEmpty(richListModel.getMsg())) {
                            image.setImageUrlWithScaleType(richListModel.getMsg(), ImageView.ScaleType.FIT_START);
                            image.setImageWidthAndHeight(msgMaxWidth, imgHeight);
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, imgHeight);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        image.setOnClickListener(new ImageClickLisenter(context, richListModel.getMsg(), isRight));
                        sobot_rich_ll.addView(imageView, layoutParams);
                        setLongClickListener(imageView);
                    } else if (richListModel.getType() == 3) {
                        View videoView = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_rich_vedio_view, sobot_rich_ll, false);
                        SobotProgressImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
                        if (!TextUtils.isEmpty(richListModel.getVideoImgUrl())) {
                            sobot_video_first_image.setImageUrl(richListModel.getVideoImgUrl());
                            sobot_video_first_image.setImageWidthAndHeight(msgMaxWidth, msgMaxWidth * 146 / 246);
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, msgMaxWidth * 146 / 246);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        sobot_rich_ll.addView(videoView, layoutParams);
                        videoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                String name = MD5Util.encode(richListModel.getMsg());
                                int dotIndex = richListModel.getMsg().lastIndexOf('.');
                                if (dotIndex == -1) {
                                    name = name + ".mp4";
                                } else {
                                    name = name + richListModel.getMsg().substring(dotIndex + 1);
                                }
                                cacheFile.setFileName(name);
                                cacheFile.setUrl(richListModel.getMsg());
                                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                cacheFile.setMsgId(message.getMsgId());
                                Intent intent = SobotVideoActivity.newIntent(mContext, cacheFile);
                                mContext.startActivity(intent);
                            }
                        });
                        setLongClickListener(videoView);
                    } else if ((richListModel.getType() == 4 || richListModel.getType() == 2)) {
                        View view = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_file_l, null);
                        TextView sobot_file_name = (TextView) view.findViewById(R.id.sobot_file_name);
                        TextView sobot_file_size = (TextView) view.findViewById(R.id.sobot_file_size);
                        SobotSectorProgressView sobot_progress = (SobotSectorProgressView) view.findViewById(R.id.sobot_progress);
                        sobot_file_name.setText(richListModel.getName());
                        sobot_file_name.setMaxWidth(msgMaxWidth-ScreenUtils.dip2px(mContext, 42));
                        sobot_file_size.setText(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                        SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg()))), sobot_progress);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgCardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        view.setLayoutParams(layoutParams);
                        sobot_rich_ll.addView(view);
                        setLongClickListener(view);
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (richListModel.getType() == 2) {
                                    Intent intent = new Intent(context, WebViewActivity.class);
                                    intent.putExtra("url", richListModel.getMsg());
                                    context.startActivity(intent);
                                } else {
                                    // 打开详情页面
                                    Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                                    SobotCacheFile cacheFile = new SobotCacheFile();
                                    cacheFile.setFileName(richListModel.getName());
                                    cacheFile.setFileSize(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                                    cacheFile.setUrl(richListModel.getMsg());
                                    cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                    cacheFile.setMsgId(message.getMsgId() + richListModel.getMsg());
                                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            }
                        });
                    } else if ((richListModel.getType() == 5)) {
                        try {
                            //md 表格
                            WebView webview = new WebView(mContext);
                            int color = ContextCompat.getColor(mContext, R.color.sobot_color_bg_second);
                            String bgColorString = String.format("#%06X", (0xFFFFFF & color));
                            String htmlContent = "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "<meta charset='utf-8'>" +
                                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                    "<style>" +
                                    "body { background: " + bgColorString + "; margin: 0; padding: 0px; overflow-x: scroll; }" +  // 添加 overflow-x: scroll
                                    "table { background: #f0f0f0; border: 1px solid #e6e6e6; " +
                                    "border-radius: 12px; border-collapse: separate; border-spacing: 0; " +
                                    "display: table; table-layout: auto; " +
                                    "margin: 0; overflow-x: scroll; overflow-y: hidden; }" +
                                    "th, td { min-width: 80px; max-width: 300px; padding: 10px 10px; font-size: 14px; " +
                                    "color: #161616; line-height: 22px; word-break: break-word; " +
                                    "border-width: 0 0 1px 1px; border-style: solid; border-color: #e6e6e6; " +
                                    "text-align: left; }" +
                                    "th { font-weight: 600; background-color: #f0f0f0; }" +
                                    "td { background-color: #fff; }" +
                                    "th:first-child, td:first-child { border-left-width: 0; }" +
                                    "tr:last-child td { border-bottom-width: 0; }" +
                                    "th:first-child { border-top-left-radius: 12px; }" +
                                    "th:last-child { border-top-right-radius: 12px; }" +
                                    "tr:last-child td:first-child { border-bottom-left-radius: 12px; }" +
                                    "tr:last-child td:last-child { border-bottom-right-radius: 12px; }" +
                                    "</style>" +
                                    "</head>" +
                                    "<body>" +
                                    richListModel.getMsg() +
                                    "</body>" +
                                    "</html>";
                            webview.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            if (i != 0) {
                                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                            }
                            webview.setLayoutParams(layoutParams);
                            sobot_rich_ll.addView(webview);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            sobot_rich_ll.setVisibility(View.VISIBLE);
            msg.setVisibility(View.GONE);
        } else {
            sobot_rich_ll.setVisibility(View.GONE);
            if (message.getAnswer() != null && !TextUtils.isEmpty(message.getAnswer().getMsg())) {
                msg.setVisibility(View.VISIBLE);
                String robotAnswer = "";
                if ("9".equals(message.getAnswer().getMsgType())) {
                    if (message.getAnswer().getMultiDiaRespInfo() != null) {
                        robotAnswer = message.getAnswer().getMultiDiaRespInfo().getAnswer();
                    }

                } else {
                    robotAnswer = message.getAnswer().getMsg();
                }
                HtmlTools.getInstance(context).setRichText(msg, robotAnswer, getLinkTextColor());
            } else {
                msg.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 判断context 所属的activity是否销毁了
     *
     * @param context
     * @return
     */
    public boolean isActivityDestroyed(Context context) {
        if (context == null) {
            return true;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed();
            } else {
                // 对于低于API 17的版本，可以通过检查Activity的生命周期状态
                return activity.isFinishing() || activity.isDestroyed();
            }
        }

        // 如果context不是Activity实例，则认为它没有被销毁
        return false;
    }
}