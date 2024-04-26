package com.sobot.chat.viewHolder;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.sobot.chat.listener.NoDoubleClickListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.HtmlTools;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotSectorProgressView;
import com.sobot.chat.widget.attachment.FileTypeConfig;
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
        sobot_ll_switch.setOnClickListener(this);

//        sobot_ll_bottom_likeBtn = convertView.findViewById(R.id.sobot_ll_bottom_likeBtn);
//        sobot_ll_bottom_dislikeBtn = convertView.findViewById(R.id.sobot_ll_bottom_dislikeBtn);
//        sobot_tv_bottom_likeBtn = convertView.findViewById(R.id.sobot_tv_bottom_likeBtn);
//        sobot_tv_bottom_dislikeBtn = convertView.findViewById(R.id.sobot_tv_bottom_dislikeBtn);
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
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
    //顶踩 显示 点击 逻辑
    public void refreshItem() {
        if (message == null) {
            return;
        }
        //找不到顶和踩就返回
        if (sobot_likeBtn_tv == null ||
                sobot_dislikeBtn_tv == null) {
            return;
        }
        if (isRight()) {
            return;
        }
        //顶 踩的状态 0 不显示顶踩按钮  1显示顶踩 按钮  2 显示顶之后的view  3显示踩之后view
        switch (message.getRevaluateState()) {
            case 1:
                showRevaluateBtn();
                break;
            case 2:
                showLikeWordView();
                break;
            case 3:
                showDislikeWordView();
                break;
            default:
                hideRevaluateBtn();
                break;
        }
    }
    /**
     * 隐藏 顶踩 按钮
     */
    public void hideRevaluateBtn() {
        hideContainer();
        sobot_likeBtn_tv.setVisibility(View.GONE);
        sobot_dislikeBtn_tv.setVisibility(View.GONE);
        rightEmptyRL.setVisibility(View.GONE);
        if (sobot_ll_bottom_likeBtn != null) {
            sobot_tv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_likeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 显示 顶踩 按钮
     */
    public void showRevaluateBtn() {
        if(dingcaiIsShowRight()) {
            sobot_likeBtn_tv.setVisibility(View.VISIBLE);
            sobot_dislikeBtn_tv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
            sobot_likeBtn_tv.setEnabled(true);
            sobot_dislikeBtn_tv.setEnabled(true);
            sobot_likeBtn_tv.setSelected(false);
            sobot_dislikeBtn_tv.setSelected(false);
            sobot_likeBtn_tv.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(true);
                }
            });
            sobot_dislikeBtn_tv.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    doRevaluate(false);
                }
            });
            //有顶和踩时显示信息显示两行 68-12-12=44 总高度减去上下内间距
            msg.setMinHeight(ScreenUtils.dip2px(mContext, 44));
            //有顶和踩时,拆分后的富文本消息如果只有一个并且是文本类型设置最小高度 68-12-12=44 总高度减去上下内间距
            if (sobot_rich_ll != null && sobot_rich_ll.getChildCount() == 1) {
                for (int i = 0; i < sobot_rich_ll.getChildCount(); i++) {
                    View view = sobot_rich_ll.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setMinHeight(ScreenUtils.dip2px(mContext, 44));
                    }
                }
            }
        }else{
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
            if(sobot_tv_bottom_likeBtn!=null) {
                sobot_tv_bottom_likeBtn.setVisibility(View.VISIBLE);
                sobot_tv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
                sobot_ll_bottom_likeBtn.setVisibility(View.VISIBLE);
                sobot_ll_bottom_dislikeBtn.setVisibility(View.VISIBLE);
                sobot_tv_bottom_likeBtn.setEnabled(true);
                sobot_tv_bottom_dislikeBtn.setEnabled(true);
                sobot_tv_bottom_likeBtn.setSelected(false);
                sobot_tv_bottom_dislikeBtn.setSelected(false);
                sobot_tv_bottom_likeBtn.setOnClickListener(new NoDoubleClickListener() {
                    @Override
                    public void onNoDoubleClick(View v) {
                        doRevaluate(true);
                    }
                });
                sobot_tv_bottom_dislikeBtn.setOnClickListener(new NoDoubleClickListener() {
                    @Override
                    public void onNoDoubleClick(View v) {
                        doRevaluate(false);
                    }
                });
            }
        }
    }
    /**
     * 顶踩 操作
     *
     * @param revaluateFlag true 顶  false 踩
     */
    private void doRevaluate(boolean revaluateFlag) {
        if (msgCallBack != null) {
            msgCallBack.doRevaluate(revaluateFlag, message);
        }
    }
    /**
     * 显示顶之后的view
     */
    public void showLikeWordView() {
        if(dingcaiIsShowRight()) {
            sobot_likeBtn_tv.setSelected(true);
            sobot_likeBtn_tv.setEnabled(false);
            sobot_dislikeBtn_tv.setEnabled(false);
            sobot_dislikeBtn_tv.setSelected(false);
            sobot_likeBtn_tv.setVisibility(View.VISIBLE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
            rightEmptyRL.setVisibility(View.VISIBLE);
            //有顶或者踩时显示信息显示一行 22
            msg.setMinHeight(ScreenUtils.dip2px(mContext, 22));
            //有顶或者踩时,拆分后的富文本消息如果只有一个并且是文本类型设置最小高度 22
            if (sobot_rich_ll != null && sobot_rich_ll.getChildCount() == 1) {
                for (int i = 0; i < sobot_rich_ll.getChildCount(); i++) {
                    View view = sobot_rich_ll.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setMinHeight(ScreenUtils.dip2px(mContext, 22));
                    }
                }
            }
        }else{
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_tv_bottom_likeBtn.setSelected(true);
            sobot_tv_bottom_likeBtn.setEnabled(false);
            sobot_tv_bottom_dislikeBtn.setEnabled(false);
            sobot_tv_bottom_dislikeBtn.setSelected(false);
            sobot_tv_bottom_likeBtn.setVisibility(View.VISIBLE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_ll_bottom_likeBtn.setVisibility(View.VISIBLE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.GONE);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
        }
    }

    /**
     * 显示踩之后的view
     */
    public void showDislikeWordView() {
        if(dingcaiIsShowRight()) {
            sobot_dislikeBtn_tv.setSelected(true);
            sobot_dislikeBtn_tv.setEnabled(false);
            sobot_likeBtn_tv.setEnabled(false);
            sobot_likeBtn_tv.setSelected(false);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.VISIBLE);
            rightEmptyRL.setVisibility(View.VISIBLE);
            //有顶或者踩时显示信息显示一行 22
            msg.setMinHeight(ScreenUtils.dip2px(mContext, 22));
            //有顶或者踩时,拆分后的富文本消息如果只有一个并且是文本类型设置最小高度 22
            if (sobot_rich_ll != null && sobot_rich_ll.getChildCount() == 1) {
                for (int i = 0; i < sobot_rich_ll.getChildCount(); i++) {
                    View view = sobot_rich_ll.getChildAt(i);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setMinHeight(ScreenUtils.dip2px(mContext, 22));
                    }
                }
            }
        } else {
            sobot_tv_bottom_dislikeBtn.setSelected(true);
            sobot_tv_bottom_dislikeBtn.setEnabled(false);
            sobot_tv_bottom_likeBtn.setEnabled(false);
            sobot_tv_bottom_likeBtn.setSelected(false);
            sobot_tv_bottom_likeBtn.setVisibility(View.GONE);
            sobot_tv_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            sobot_ll_bottom_likeBtn.setVisibility(View.GONE);
            sobot_chat_more_action.setVisibility(View.VISIBLE);
            sobot_ll_bottom_dislikeBtn.setVisibility(View.VISIBLE);
            sobot_likeBtn_tv.setVisibility(View.GONE);
            sobot_dislikeBtn_tv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_ll_switch) {
            // 换一组
            if (message != null && message.getListSuggestions() != null
                    && message.getListSuggestions().size() > 0) {
                LogUtils.i(message.getCurrentPageNum() + "==================");
                int pageNum = message.getCurrentPageNum() + 1;
                int total = message.getListSuggestions().size();
                int pre = message.getGuideGroupNum();
                if (pre == 0) {
                    pre = 5;
                }
                int maxNum = (total % pre == 0) ? (total / pre) : (total / pre + 1);
                LogUtils.i(maxNum + "=========maxNum=========");
                pageNum = (pageNum >= maxNum) ? 0 : pageNum;
                message.setCurrentPageNum(pageNum);

                LogUtils.i(message.getCurrentPageNum() + "==================");
                resetAnswersList();
            }


        }
    }

    // 查看阅读全文的监听
    public static class ReadAllTextLisenter implements View.OnClickListener {
        private String mUrlContent;
        private Context context;

        public ReadAllTextLisenter(Context context, String urlContent) {
            super();
            this.mUrlContent = urlContent;
            this.context = context;
        }

        @Override
        public void onClick(View arg0) {

            if (!mUrlContent.startsWith("http://")
                    && !mUrlContent.startsWith("https://")) {
                mUrlContent = "http://" + mUrlContent;
            }
            // 内部浏览器
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("url", mUrlContent);
            context.startActivity(intent);
        }
    }


    private void setupMsgContent(final Context context, final ZhiChiMessageBase message) {
        if (message.getAnswer() != null && message.getAnswer().getRichList() != null && message.getAnswer().getRichList().size() > 0) {
            LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
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
                                if (tempRichList.size() > 0) {
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
                    if (tempRichList != null && tempRichList.size() > 0) {
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
                        textView.setTextSize(14);
                        textView.setLayoutParams(wlayoutParams);
                        textView.setMaxWidth(msgMaxWidth);
                        //设置行间距
                        textView.setLineSpacing(0, 1.1f);
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
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, msgCardWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
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
                                                SobotBitmapUtil.display(mContext, link.getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e, String s) {
                                            if (view != null) {
                                                view.setVisibility(View.GONE);
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
                                HtmlTools.getInstance(mContext).setRichTextViewText(textView, content, getLinkTextColor());
                            } else {
                                HtmlTools.getInstance(mContext).setRichTextViewText(textView, richListModel.getMsg(), getLinkTextColor());
                            }
                            sobot_rich_ll.addView(textView);
                        }
                    } else if (richListModel.getType() == 1 && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                        LinearLayout.LayoutParams mlayoutParams;
                        try {
                            int pictureWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sobot_rich_msg_picture_width_dp);
                            int pictureHeight = mContext.getResources().getDimensionPixelSize(R.dimen.sobot_rich_msg_picture_height_dp);
                            if (pictureWidth == 0) {
                                //如果设置的宽度等于0，默认图片的最大宽度是气泡的最大宽度
                                pictureWidth = msgMaxWidth;
                            }
                            if (pictureWidth > msgMaxWidth) {
                                //如果设置的宽度大于气泡的最大宽度，等比例缩放设置的高度
                                float picbili = (float) pictureWidth / msgMaxWidth;
                                pictureWidth = msgMaxWidth;
                                pictureHeight = (int) (pictureHeight / picbili);
                            }
                            mlayoutParams = new LinearLayout.LayoutParams(pictureWidth, pictureHeight);
                        } catch (Exception e) {
                            e.printStackTrace();
                            mlayoutParams = new LinearLayout.LayoutParams(msgMaxWidth,
                                    ScreenUtils.dip2px(context, 200));
                        }
                        if (i != 0) {
                            mlayoutParams.setMargins(0, ScreenUtils.dip2px(context, 10), 0, ScreenUtils.dip2px(context, 6));
                        }
                        ImageView imageView = new ImageView(mContext);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        imageView.setLayoutParams(mlayoutParams);
                        SobotBitmapUtil.display(mContext, richListModel.getMsg(), imageView);
                        imageView.setOnClickListener(new ImageClickLisenter(context, richListModel.getMsg(), isRight));
                        sobot_rich_ll.addView(imageView);
                        setLongClickListener(imageView);
                    } else if (richListModel.getType() == 3 && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                        View videoView = LayoutInflater.from(mContext).inflate(R.layout.sobot_chat_msg_item_rich_vedio_view, null);
                        ImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
                        if (!TextUtils.isEmpty(richListModel.getVideoImgUrl())) {
                            SobotBitmapUtil.display(mContext, richListModel.getVideoImgUrl(), sobot_video_first_image, R.drawable.sobot_rich_item_vedoi_default, R.drawable.sobot_rich_item_vedoi_default);
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (i != 0) {
                            layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), 0, 0);
                        }
                        videoView.setLayoutParams(layoutParams);
                        sobot_rich_ll.addView(videoView);
                        videoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                cacheFile.setFileName(richListModel.getName());
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
                        sobot_file_size.setText(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                        SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg()))), sobot_progress);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ScreenUtils.dip2px(mContext, msgCardWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (i != 0) {
                             layoutParams.setMargins(0, ScreenUtils.dip2px(mContext, 10), ScreenUtils.dip2px(mContext, 6), 0);
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

}