package com.sobot.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.ArticleModel;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotLink;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
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
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 引用详情
 */
public class SobotQuoteDetailActivity extends SobotChatBaseActivity {

    private LinearLayout sobot_rich_ll;//内容显示
    private LinearLayout ll_outer_most;//acivity整个view
    private ZhiChiAppointMessage appointMessage;
    private int msgMaxWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appointMessage = (ZhiChiAppointMessage) getIntent().getSerializableExtra("AppointMessage");
        msgMaxWidth = ScreenUtils.getScreenWidth(this) - (ScreenUtils.dip2px(this, 30));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_quote_detail;
    }

    @Override
    protected void initView() {
        sobot_rich_ll = findViewById(R.id.sobot_rich_ll);
        ll_outer_most = findViewById(R.id.ll_outer_most);
        ll_outer_most.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        if (appointMessage != null) {
            //0文本,1图片,2音频,3视频,4文件,5对象,当msgType=5 时，根据content里边的 type 判断具体的时哪种消息 0-富文本 1-多伦会话 2-位置 3-小卡片 4-订单卡片 6-小程序 17-文章 21-自定义卡片
            int msgType = appointMessage.getMsgType();
            LinearLayout.LayoutParams wlayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (msgType == 0) {
                //文本
                String text = appointMessage.getContent();
                TextView textView = new TextView(this);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.sobot_text_font_18));
                textView.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                textView.setLayoutParams(wlayoutParams);
                //设置行间距
                textView.setLineSpacing(0, 1.1f);
                if (!TextUtils.isEmpty(text)) {
                    HtmlTools.getInstance(getSobotBaseActivity()).setRichText(textView, text, getLinkTextColor());
                    sobot_rich_ll.addView(textView);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            } else if (msgType == 1) {
                //图片
                String url = appointMessage.getContent();
                LinearLayout.LayoutParams mlayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setMaxWidth(msgMaxWidth);
                imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(mlayoutParams);
                SobotBitmapUtil.display(this, url, imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                sobot_rich_ll.addView(imageView);
            } else if (msgType == 2 || msgType == 4) {
                //音频、文件
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("url") && !TextUtils.isEmpty(contentJsonObject.optString("url"))) {
                        if (contentJsonObject.has("voiceType") && contentJsonObject.optInt("voiceType") == 1) {
                            //音频，知识库返回的
                            SobotCacheFile cacheFile = new SobotCacheFile();
                            cacheFile.setUrl(contentJsonObject.optString("url"));
                            cacheFile.setFileName(contentJsonObject.optString("fileName"));
                            cacheFile.setFileType(17);
                            cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                            //播放音频
                            View view = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_file_l, null);
                            TextView sobot_file_name = (TextView) view.findViewById(R.id.sobot_file_name);
                            TextView sobot_file_size = (TextView) view.findViewById(R.id.sobot_file_size);
                            sobot_file_name.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                            sobot_file_size.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                            SobotSectorProgressView sobot_progress = (SobotSectorProgressView) view.findViewById(R.id.sobot_progress);
                            sobot_file_name.setText(contentJsonObject.optString("fileName"));
                            sobot_file_size.setText(TextUtils.isEmpty(contentJsonObject.optString("fileSize")) ? "" : contentJsonObject.optString("fileSize"));
                            SobotBitmapUtil.display(this, ChatUtils.getFileIcon(this, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(contentJsonObject.optString("url")))), sobot_progress);
                            sobot_rich_ll.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 打开详情页面
                                    Intent intent = new Intent(SobotQuoteDetailActivity.this, SobotFileDetailActivity.class);
                                    SobotCacheFile cacheFile = new SobotCacheFile();
                                    cacheFile.setFileName(cacheFile.getFileName());
                                    cacheFile.setUrl(cacheFile.getUrl());
                                    cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(cacheFile.getUrl())));
                                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            final SobotCacheFile cacheFile = new SobotCacheFile();
                            cacheFile.setUrl(contentJsonObject.optString("url"));
                            cacheFile.setFileName(contentJsonObject.optString("fileName"));
                            cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(cacheFile.getUrl())));
                            cacheFile.setMsgId(cacheFile.getUrl());
                            cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                            View view = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_file_l, null);
                            TextView sobot_file_name = (TextView) view.findViewById(R.id.sobot_file_name);
                            TextView sobot_file_size = (TextView) view.findViewById(R.id.sobot_file_size);
                            sobot_file_name.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                            sobot_file_size.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                            SobotSectorProgressView sobot_progress = (SobotSectorProgressView) view.findViewById(R.id.sobot_progress);
                            sobot_file_name.setText(contentJsonObject.optString("fileName"));
                            sobot_file_size.setText(TextUtils.isEmpty(contentJsonObject.optString("fileSize")) ? "" : contentJsonObject.optString("fileSize"));
                            SobotBitmapUtil.display(this, ChatUtils.getFileIcon(this, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(contentJsonObject.optString("url")))), sobot_progress);
                            sobot_rich_ll.addView(view);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 打开详情页面
                                    Intent intent = new Intent(SobotQuoteDetailActivity.this, SobotFileDetailActivity.class);
                                    intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msgType == 3) {
                //视频
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("url") && !TextUtils.isEmpty(contentJsonObject.optString("url"))) {
                        SobotCacheFile cacheFile = new SobotCacheFile();
                        cacheFile.setUrl(contentJsonObject.optString("url"));
                        cacheFile.setFileName(contentJsonObject.optString("fileName"));
                        cacheFile.setFileType(GsonUtil.changeFileType(contentJsonObject.optInt("type")));
                        cacheFile.setFileSize(contentJsonObject.optString("fileSize"));
                        cacheFile.setSnapshot(contentJsonObject.optString("snapshot"));

                        View videoView = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_item_rich_vedio_view, null);
                        ImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
//                        if (!TextUtils.isEmpty(richListModel.getVideoImgUrl())) {
//                            SobotBitmapUtil.display(this, richListModel.getVideoImgUrl(), sobot_video_first_image, R.drawable.sobot_rich_item_vedoi_default, R.drawable.sobot_rich_item_vedoi_default);
//                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                        videoView.setLayoutParams(layoutParams);
                        sobot_rich_ll.addView(videoView);
                        videoView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                cacheFile.setFileName(cacheFile.getFileName());
                                cacheFile.setUrl(cacheFile.getUrl());
                                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(cacheFile.getUrl())));
                                Intent intent = SobotVideoActivity.newIntent(SobotQuoteDetailActivity.this, cacheFile);
                                startActivity(intent);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msgType == 5) {
                //对象，// 当msgType=5 时，根据content里边的 type 判断具体的时哪种消息0-富文本 1-多伦会话 2-位置 3-小卡片 4-订单卡片 6-小程序 17-文章 21-自定义卡片
                try {
                    JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                    if (contentJsonObject.has("type") && !TextUtils.isEmpty(contentJsonObject.optString("type"))) {
                        if ("0".equals(contentJsonObject.optString("type"))) {
                            //富文本类型
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                JSONObject msgJsonObject = new JSONObject(contentJsonObject.optString("msg"));
                                if (msgJsonObject.has("richList") && !GsonUtil.isEmpty(msgJsonObject.optString("richList"))) {
                                    JSONArray data = msgJsonObject.getJSONArray("richList");
                                    if (data != null) {
                                        List<ChatMessageRichListModel> list = new ArrayList<>();
                                        for (int i = 0; i < data.length(); i++) {
                                            ChatMessageRichListModel base = new ChatMessageRichListModel();
                                            JSONObject obj = data.getJSONObject(i);
                                            if (obj != null) {
                                                if (obj.has("type")) {
                                                    base.setType(obj.optInt("type"));
                                                }
                                                if (obj.has("name")) {
                                                    base.setName(StringUtils.checkStringIsNull(obj.optString("name")));
                                                }
                                                if (obj.has("msg")) {
                                                    base.setMsg(StringUtils.checkStringIsNull(obj.optString("msg")));
                                                }
                                                if (obj.has("showType")) {
                                                    base.setShowType(obj.optInt("showType"));
                                                }
                                                if (obj.has("fileSize")) {
                                                    base.setFileSize(StringUtils.checkStringIsNull(obj.optString("fileSize")));
                                                }
                                                if (obj.has("videoImgUrl")) {
                                                    base.setFileSize(StringUtils.checkStringIsNull(obj.optString("videoImgUrl")));
                                                }
                                            }
                                            list.add(base);
                                        }
                                        //显示多文本
                                        for (int i = 0; i < list.size(); i++) {
                                            final ChatMessageRichListModel richListModel = list.get(i);
                                            if (richListModel != null) {
                                                //如果最后一个是空行，直接过滤掉不显示
                                                if (TextUtils.isEmpty(richListModel.getMsg()) && i == (list.size() - 1)) {
                                                    continue;
                                                }
                                                // 0：文本，1：图片，2：音频，3：视频，4：文件
                                                if (richListModel.getType() == 0) {
                                                    TextView textView = new TextView(this);
                                                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.sobot_text_font_14));
                                                    textView.setLayoutParams(wlayoutParams);
                                                    textView.setMaxWidth(msgMaxWidth);
                                                    textView.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                    //设置行间距
                                                    textView.setLineSpacing(0, 1.1f);
                                                    if (!TextUtils.isEmpty(richListModel.getName()) && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                                        try {
                                                            textView.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_rlink));
                                                        } catch (Exception e) {
                                                        }
                                                        textView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (SobotOption.newHyperlinkListener != null) {
                                                                    //如果返回true,拦截;false 不拦截
                                                                    boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(SobotQuoteDetailActivity.this, richListModel.getMsg());
                                                                    if (isIntercept) {
                                                                        return;
                                                                    }
                                                                }
                                                                Intent intent = new Intent(SobotQuoteDetailActivity.this, WebViewActivity.class);
                                                                intent.putExtra("url", richListModel.getMsg());
                                                                startActivity(intent);
                                                            }
                                                        });
                                                        textView.setText(richListModel.getName());
                                                        sobot_rich_ll.addView(textView);
                                                        if (richListModel.getShowType() == 1) {
                                                            //超链接，并且是卡片形式才显示卡片
                                                            final View view = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_link_card, null);
                                                            TextView tv_title = view.findViewById(R.id.tv_title);
                                                            tv_title.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                            tv_title.setText(R.string.sobot_parsing);
                                                            if (richListModel.getSobotLink() != null) {
                                                                tv_title = view.findViewById(R.id.tv_title);
                                                                TextView tv_des = view.findViewById(R.id.tv_des);
                                                                tv_des.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                                ImageView image_link = view.findViewById(R.id.image_link);
                                                                if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle())) {
                                                                    tv_title.setVisibility(View.GONE);
                                                                } else {
                                                                    tv_title.setText(richListModel.getSobotLink().getTitle());
                                                                    tv_title.setVisibility(View.VISIBLE);
                                                                }
                                                                tv_des.setText(TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) ? richListModel.getMsg() : richListModel.getSobotLink().getDesc());
                                                                SobotBitmapUtil.display(this, richListModel.getSobotLink().getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                                                if (TextUtils.isEmpty(richListModel.getSobotLink().getTitle()) && TextUtils.isEmpty(richListModel.getSobotLink().getDesc()) && TextUtils.isEmpty(richListModel.getSobotLink().getImgUrl())) {
                                                                    view.setVisibility(View.GONE);
                                                                }
                                                            } else {
                                                                SobotMsgManager.getInstance(this).getZhiChiApi().getHtmlAnalysis(this, richListModel.getMsg(), new StringResultCallBack<SobotLink>() {
                                                                    @Override
                                                                    public void onSuccess(SobotLink link) {
                                                                        if (link != null) {
                                                                            richListModel.setSobotLink(link);
                                                                            TextView tv_title = view.findViewById(R.id.tv_title);
                                                                            TextView tv_des = view.findViewById(R.id.tv_des);
                                                                            tv_title.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                                            tv_des.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                                            ImageView image_link = view.findViewById(R.id.image_link);
                                                                            if (TextUtils.isEmpty(link.getTitle())) {
                                                                                tv_title.setVisibility(View.VISIBLE);
                                                                                tv_title.setText(richListModel.getName());
                                                                            } else {
                                                                                tv_title.setText(link.getTitle());
                                                                                tv_title.setVisibility(View.VISIBLE);
                                                                            }
                                                                            tv_des.setText(TextUtils.isEmpty(link.getDesc()) ? richListModel.getMsg() : link.getDesc());
                                                                            SobotBitmapUtil.display(SobotQuoteDetailActivity.this, link.getImgUrl(), image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Exception e, String s) {
                                                                        if (view != null) {
                                                                            TextView tv_title = view.findViewById(R.id.tv_title);
                                                                            tv_title.setText(richListModel.getMsg());
                                                                            ImageView image_link = view.findViewById(R.id.image_link);
                                                                            SobotBitmapUtil.display(SobotQuoteDetailActivity.this, "", image_link, R.drawable.sobot_link_image, R.drawable.sobot_link_image);
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
                                                                        boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(SobotQuoteDetailActivity.this, richListModel.getMsg());
                                                                        if (isIntercept) {
                                                                            return;
                                                                        }
                                                                    }
                                                                    Intent intent = new Intent(SobotQuoteDetailActivity.this, WebViewActivity.class);
                                                                    intent.putExtra("url", richListModel.getMsg());
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        textView.setTextColor(ContextCompat.getColor(this, R.color.sobot_left_msg_text_color));
                                                        if (!TextUtils.isEmpty(richListModel.getMsg()) && i == (list.size() - 1)) {
                                                            //如果是richlist的最后一个，把这个的尾部的<br/>都去掉
                                                            String content = richListModel.getMsg().trim();
                                                            while (content.length() > 5 && "<br/>".equals(content.substring(content.length() - 5, content.length()))) {
                                                                content = content.substring(0, content.length() - 5);
                                                            }
                                                            HtmlTools.getInstance(this).setRichTextViewText(textView, content, getLinkTextColor());
                                                        } else {
                                                            HtmlTools.getInstance(this).setRichTextViewText(textView, richListModel.getMsg(), getLinkTextColor());
                                                        }
                                                        sobot_rich_ll.addView(textView);
                                                        textView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                } else if (richListModel.getType() == 1 && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                                    LinearLayout.LayoutParams mlayoutParams;
                                                    try {
                                                        int pictureWidth = this.getResources().getDimensionPixelSize(R.dimen.sobot_rich_msg_picture_width_dp);
                                                        int pictureHeight = this.getResources().getDimensionPixelSize(R.dimen.sobot_rich_msg_picture_height_dp);
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
                                                                ScreenUtils.dip2px(this, 200));
                                                    }
                                                    if (i != 0) {
                                                        mlayoutParams.setMargins(0, ScreenUtils.dip2px(this, 10), 0, ScreenUtils.dip2px(this, 6));
                                                    }
                                                    ImageView imageView = new ImageView(this);
                                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                                    imageView.setLayoutParams(mlayoutParams);
                                                    SobotBitmapUtil.display(this, richListModel.getMsg(), imageView);
                                                    imageView.setOnClickListener(new MsgHolderBase.ImageClickLisenter(this, richListModel.getMsg(), false));
                                                    sobot_rich_ll.addView(imageView);
                                                } else if (richListModel.getType() == 3 && HtmlTools.isHasPatterns(richListModel.getMsg())) {
                                                    View videoView = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_item_rich_vedio_view, null);
                                                    ImageView sobot_video_first_image = videoView.findViewById(R.id.sobot_video_first_image);
                                                    if (!TextUtils.isEmpty(richListModel.getVideoImgUrl())) {
                                                        SobotBitmapUtil.display(this, richListModel.getVideoImgUrl(), sobot_video_first_image, R.drawable.sobot_rich_item_vedoi_default, R.drawable.sobot_rich_item_vedoi_default);
                                                    }
                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(msgMaxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    if (i != 0) {
                                                        layoutParams.setMargins(0, ScreenUtils.dip2px(this, 10), 0, 0);
                                                    }
                                                    videoView.setLayoutParams(layoutParams);
                                                    sobot_rich_ll.addView(videoView);
                                                    videoView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            SobotCacheFile cacheFile = new SobotCacheFile();
                                                            String name = MD5Util.encode(richListModel.getMsg());
                                                            int dotIndex = richListModel.getMsg().lastIndexOf('.');
                                                            if (dotIndex == -1) {
                                                                name = name +".mp4";
                                                            }else {
                                                                name = name + richListModel.getMsg().substring(dotIndex + 1);
                                                            }
                                                            cacheFile.setFileName(name);
                                                            cacheFile.setUrl(richListModel.getMsg());
                                                            cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                                            cacheFile.setMsgId(appointMessage.getMsgId() + richListModel.getMsg());
                                                            Intent intent = SobotVideoActivity.newIntent(SobotQuoteDetailActivity.this, cacheFile);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                } else if ((richListModel.getType() == 4 || richListModel.getType() == 2)) {
                                                    View view = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_file_l, null);
                                                    TextView sobot_file_name = (TextView) view.findViewById(R.id.sobot_file_name);
                                                    TextView sobot_file_size = (TextView) view.findViewById(R.id.sobot_file_size);
                                                    sobot_file_name.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                    sobot_file_size.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                                    SobotSectorProgressView sobot_progress = (SobotSectorProgressView) view.findViewById(R.id.sobot_progress);
                                                    sobot_file_name.setText(richListModel.getName());
                                                    sobot_file_size.setText(TextUtils.isEmpty(richListModel.getFileSize()) ? "" : richListModel.getFileSize());
                                                    SobotBitmapUtil.display(this, ChatUtils.getFileIcon(this, FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg()))), sobot_progress);
                                                    sobot_rich_ll.addView(view);
                                                    view.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if (richListModel.getType() == 2) {
                                                                Intent intent = new Intent(SobotQuoteDetailActivity.this, WebViewActivity.class);
                                                                intent.putExtra("url", richListModel.getMsg());
                                                                startActivity(intent);
                                                            } else {
                                                                // 打开详情页面
                                                                Intent intent = new Intent(SobotQuoteDetailActivity.this, SobotFileDetailActivity.class);
                                                                SobotCacheFile cacheFile = new SobotCacheFile();
                                                                cacheFile.setFileName(richListModel.getName());
                                                                cacheFile.setUrl(richListModel.getMsg());
                                                                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(richListModel.getMsg())));
                                                                cacheFile.setMsgId(appointMessage.getMsgId() + richListModel.getMsg());
                                                                intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                        sobot_rich_ll.setVisibility(View.VISIBLE);
                                    }

                                }
                            }
                        } else if ("1".equals(contentJsonObject.optString("type"))) {
                            //多伦会话类型 只有历史记录有，机器人实时接口返回没有message
                        } else if ("3".equals(contentJsonObject.optString("type"))) {
                            //商品卡片

                        } else if ("4".equals(contentJsonObject.optString("type"))) {
                            //订单卡片

                        } else if ("6".equals(contentJsonObject.optString("type"))) {
                            //小程序卡片

                        } else if ("17".equals(contentJsonObject.optString("type"))) {
                            //文章卡片
                            if (contentJsonObject.has("msg") && !TextUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                try {
                                    JSONObject miniJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                    final ArticleModel model = new ArticleModel();
                                    if (miniJsonObj.has("content")) {
                                        model.setContent(StringUtils.checkStringIsNull(miniJsonObj.optString("content")));
                                    }
                                    if (miniJsonObj.has("articleBody")) {
                                        model.setArticleBody(StringUtils.checkStringIsNull(miniJsonObj.optString("articleBody")));
                                    }
                                    if (miniJsonObj.has("desc")) {
                                        model.setDesc(StringUtils.checkStringIsNull(miniJsonObj.optString("desc")));
                                    }
                                    if (miniJsonObj.has("richMoreUrl")) {
                                        model.setRichMoreUrl(StringUtils.checkStringIsNull(miniJsonObj.optString("richMoreUrl")));
                                    }
                                    if (miniJsonObj.has("snapshot")) {
                                        model.setSnapshot(StringUtils.checkStringIsNull(miniJsonObj.optString("snapshot")));
                                    }
                                    if (miniJsonObj.has("title")) {
                                        model.setTitle(StringUtils.checkStringIsNull(miniJsonObj.optString("title")));
                                    }
                                    View view = LayoutInflater.from(this).inflate(R.layout.sobot_chat_msg_item_article_card_common, null);
                                    ImageView iv_snapshot = (ImageView) view.findViewById(R.id.iv_snapshot);
                                    TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                                    TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
                                    tv_title.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                    tv_desc.setTextColor(ContextCompat.getColor(getSobotBaseActivity(), R.color.sobot_color_text_first));
                                    if (model != null) {
                                        if (!TextUtils.isEmpty(model.getSnapshot())) {
                                            SobotBitmapUtil.display(getSobotBaseContext(), model.getSnapshot(), iv_snapshot);
                                            iv_snapshot.setVisibility(View.VISIBLE);
                                        } else {
                                            iv_snapshot.setVisibility(View.GONE);
                                        }
                                        if (!TextUtils.isEmpty(model.getTitle())) {
                                            tv_title.setText(model.getTitle());
                                            tv_title.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_title.setVisibility(View.GONE);
                                        }
                                        if (!TextUtils.isEmpty(model.getDesc())) {
                                            tv_desc.setText(model.getDesc());
                                            tv_desc.setVisibility(View.VISIBLE);
                                        } else {
                                            tv_desc.setVisibility(View.GONE);
                                        }
                                    }
                                    sobot_rich_ll.addView(view);
                                    view.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (model != null && !TextUtils.isEmpty(model.getRichMoreUrl())) {
                                                if (SobotOption.newHyperlinkListener != null) {
                                                    //如果返回true,拦截;false 不拦截
                                                    boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotBaseContext(), model.getRichMoreUrl());
                                                    if (isIntercept) {
                                                        return;
                                                    }
                                                }
                                                Intent intent = new Intent(getSobotBaseContext(), WebViewActivity.class);
                                                intent.putExtra("url", model.getRichMoreUrl());
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        } else if ("21".equals(contentJsonObject.optString("type"))) {
                            //自定义卡片类型

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected int getLinkTextColor() {
        return R.color.sobot_color_rlink;

        /*if (isRight()) {
            if (mContext.getResources().getColor(R.color.sobot_color_rlink) == mContext.getResources().getColor(R.color.sobot_common_blue)) {
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //服务端返回的气泡中超链接背景颜色
                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                        return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                    }
                }
                return R.color.sobot_color_rlink;
            } else {
                return R.color.sobot_color_rlink;
            }
        } else {
            if (mContext.getResources().getColor(R.color.sobot_color_link) == mContext.getResources().getColor(R.color.sobot_common_blue)) {
                if (initMode != null && initMode.getVisitorScheme() != null) {
                    //服务端返回的气泡中超链接背景颜色
                    if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                        return Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                    }
                }
                return R.color.sobot_color_link;
            } else {
                return R.color.sobot_color_link;
            }
        }*/
    }
}