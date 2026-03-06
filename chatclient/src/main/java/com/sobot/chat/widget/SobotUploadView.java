package com.sobot.chat.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.listener.SobotCusFieldListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 带标题，上传附件 错误提示
 */
public class SobotUploadView extends LinearLayout implements View.OnClickListener {

    //标题
    private TextView tvTitle;
    //错误提示
    private TextView tvError;
    //上传
    private TextView sobot_btn_file, sobot_file_hite;

    //显示已上传的文件
    private LinearLayout sobot_layout_attachment_frame;
    private SobotSectorProgressView sobot_file_thumbnail;
    private TextView sobot_file_name;
    private ImageView sobot_file_delete;
    private String titleText;//标题
    private Context mContext;
    private SobotCusFieldListener cusCallBack;//回调方法
    private SobotUploadView view; //自己的 view
    private SobotCusFieldConfig cusFieldConfig;//字段配置
    private String valueId;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }


    public SobotUploadView(Context context) {
        this(context, null);
    }

    public SobotUploadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SobotUploadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.sobot_item_upload_view, null);
        tvTitle = view.findViewById(R.id.sobot_title_lable);
        tvError = view.findViewById(R.id.sobot_file_error);
        tvError.setVisibility(View.GONE);
        sobot_file_hite = view.findViewById(R.id.sobot_file_hite);
        sobot_btn_file = view.findViewById(R.id.sobot_btn_file);

        sobot_layout_attachment_frame = view.findViewById(R.id.sobot_layout_attachment_frame);
        sobot_layout_attachment_frame.setVisibility(View.GONE);
        sobot_file_thumbnail = view.findViewById(R.id.sobot_file_thumbnail);
        sobot_file_name = view.findViewById(R.id.sobot_file_name);
        sobot_file_delete = view.findViewById(R.id.sobot_file_delete);
        sobot_file_delete.setOnClickListener(this);
        sobot_layout_attachment_frame.setOnClickListener(this);
        sobot_btn_file.setOnClickListener(this);
        if (StringUtils.isNoEmpty(titleText)) {
            tvTitle.setText(titleText);
        }

        //点击选项，后背景颜色修改
        LayoutParams lpUpdateText = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        view.setLayoutParams(lpUpdateText);
        addView(view);
    }

    public void setCusFieldConfig(SobotCusFieldConfig cusFieldConfig) {
        this.cusFieldConfig = cusFieldConfig;
    }

    public void addPicView(SobotCusFieldConfig cusFieldConfig) {
        this.cusFieldConfig = cusFieldConfig;
        if (cusFieldConfig != null && cusFieldConfig.getCacheFile() != null) {
            sobot_layout_attachment_frame.setVisibility(View.VISIBLE);
            if (cusFieldConfig.getCacheFile().getFileType() == ZhiChiConstant.MSGTYPE_FILE_PIC) {
                SobotBitmapUtil.display(mContext, cusFieldConfig.getCacheFile().getFilePath(), sobot_file_thumbnail);
            } else {
                SobotBitmapUtil.display(mContext, ChatUtils.getFileIcon(mContext, cusFieldConfig.getCacheFile().getFileType()), sobot_file_thumbnail);
            }
            sobot_file_name.setText(cusFieldConfig.getCacheFile().getFileName());
            sobot_file_delete.setVisibility(VISIBLE);
            hideError();
        }
    }


    /**
     * 设置标题
     *
     * @param title
     * @param isMust
     */
    public void setTitle(String title, boolean isMust) {
        String mustFill = "<font color='#F5222D'> *</font>";
        if (isMust) {
            titleText = title + mustFill;
        } else {
            titleText = title;
        }
        if (tvTitle == null) return;
        tvTitle.setText(Html.fromHtml(titleText));
    }

    /**
     * 设置标题
     *
     * @param title
     * @param isMust
     */
    public void setTitle(String title, boolean isMust, String finalExplain) {
        String endStr = "";
        if (StringUtils.isNoEmpty(finalExplain)) {
            endStr = "<span style='font-size:18px;'> ⓘ</span>";
            tvTitle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    弹框显示，finalExplain
                    SobotDialogUtils.startTipDialog(mContext, finalExplain);
                }
            });
        }
        String mustFill = "<font color='#F5222D'> *</font>";
        if (isMust) {
            endStr = endStr + mustFill;
        }
        if (ChatUtils.isRtl(mContext)) {
            endStr = "\u200F" + endStr;
            titleText = "\u200F" + title + endStr;
        } else {
            endStr = "\u200E" + endStr;
            titleText = "\u200E" + title + endStr;
        }

        if (tvTitle == null) return;
        tvTitle.setText(Html.fromHtml(titleText));
    }

    public TextView getTvTitle() {
        return tvTitle;
    }


    /**
     * 设置错误提示
     *
     * @param error 错误
     */
    public void showError(String error) {
        if (!TextUtils.isEmpty(error)) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(error);
        }
    }

    // 隐藏错误
    public void hideError() {
        tvError.setVisibility(GONE);
    }

    public void setTipText(int maxStoryge) {
        String hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        sobot_file_hite.setText(String.format(hideTxt, "1", maxStoryge + "M"));
    }

    //设置右边图标
    public String getValue() {
        String v = "";
        return v;
    }

    public SobotCusFieldConfig getCusFieldConfig() {
        return cusFieldConfig;
    }

    /**
     * 设置回调
     *
     * @param callBack
     */
    public void setCusCallBack(SobotCusFieldListener callBack, SobotUploadView view) {
        this.cusCallBack = callBack;
        this.view = view;
    }

    @Override
    public void onClick(View v) {
        if (v == sobot_file_delete) {
            //删除
            if (cusFieldConfig != null) {
                cusFieldConfig.setCacheFile(null);
            }
            sobot_layout_attachment_frame.setVisibility(View.GONE);
//            sobot_file_thumbnail
            sobot_file_name.setText("");
            sobot_file_delete.setVisibility(GONE);
            if (cusCallBack != null) {
                cusCallBack.onClickDelete(view, cusFieldConfig);
            }
        } else if (v == sobot_layout_attachment_frame) {
            //预览
            if (cusCallBack != null) {
                cusCallBack.onClickPreview(view, cusFieldConfig);
            }
        } else if (v == sobot_btn_file) {
            //上传对话框
            if (cusCallBack != null) {
                cusCallBack.onClickUpload(view, cusFieldConfig);
            }
        }
    }
}
