package com.sobot.chat.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.listener.SobotCusFieldListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.WebViewSecurityUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * 带标题的附件上传控件，支持最多 50 个文件（图片或文件），每项可预览/删除。
 */
public class SobotUploadView extends LinearLayout {

    /**
     * 单个附件字段最多可上传文件数（默认 50，可通过 {@link #setMaxCount(int)} 调整）。
     * 从 static final 改为实例字段，便于不同业务场景定制（如留言新建表单上限 15）。
     */
    private int maxUploadCount = 50;

    private TextView tvTitle;
    private TextView tvError;
    private TextView sobot_btn_file;
    private TextView sobot_file_hite;
    /**
     * 虚线边框上传区域容器（包含上传按钮和提示文字）
     */
    private LinearLayout sobot_upload_area;
    /**
     * 横向滚动容器，有文件时显示
     */
    private HorizontalScrollView sobot_file_list_scroll;
    /**
     * 文件列表横向容器，每个子 View 对应一个已上传文件
     */
    private LinearLayout sobot_layout_file_list;

    private String titleText;
    private Context mContext;
    private SobotCusFieldListener cusCallBack;
    private SobotUploadView selfView;
    private SobotCusFieldConfig cusFieldConfig;
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
        View root = View.inflate(getContext(), R.layout.sobot_item_upload_view, null);
        tvTitle = root.findViewById(R.id.sobot_title_lable);
        tvError = root.findViewById(R.id.sobot_file_error);
        tvError.setVisibility(View.GONE);
        sobot_file_hite = root.findViewById(R.id.sobot_file_hite);
        sobot_btn_file = root.findViewById(R.id.sobot_btn_file);
        sobot_upload_area = root.findViewById(R.id.sobot_upload_area);
        sobot_file_list_scroll = root.findViewById(R.id.sobot_file_list_scroll);
        sobot_layout_file_list = root.findViewById(R.id.sobot_layout_file_list);

        sobot_btn_file.setOnClickListener(v -> {
            if (cusCallBack != null) {
                cusCallBack.onClickUpload(selfView, cusFieldConfig);
            }
        });

        if (StringUtils.isNoEmpty(titleText)) {
            tvTitle.setText(titleText);
        }

        LayoutParams lp = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        root.setLayoutParams(lp);
        addView(root);
    }

    public void setCusFieldConfig(SobotCusFieldConfig cusFieldConfig) {
        this.cusFieldConfig = cusFieldConfig;
    }

    /**
     * 上传成功后调用：向列表追加一个文件行，并更新上传按钮可见性。
     */
    public void addPicView(SobotCacheFile cacheFile) {
        if (cacheFile == null) return;

        View item = View.inflate(mContext, R.layout.sobot_item_field_file_item, null);
        SobotSectorProgressView thumbnail = item.findViewById(R.id.sobot_file_thumbnail);
        TextView tvName = item.findViewById(R.id.sobot_file_name);
        ImageView ivDelete = item.findViewById(R.id.sobot_file_delete);

        if (cacheFile.getFileType() == ZhiChiConstant.MSGTYPE_FILE_PIC) {
            SobotBitmapUtil.display(mContext, cacheFile.getFilePath(), thumbnail,
                    R.drawable.sobot_image_loading_bg, R.drawable.sobot_image_loading_bg);
        } else {
            SobotBitmapUtil.display(mContext,
                    ChatUtils.getFileIcon(mContext, cacheFile.getFileType()), thumbnail);
        }
        tvName.setText(cacheFile.getFileName());

        // 每项点击 → 预览
        item.setOnClickListener(v -> {
            if (cusCallBack != null) {
                cusCallBack.onClickPreview(selfView, cusFieldConfig, cacheFile);
            }
        });
        // 删除按钮
        ivDelete.setOnClickListener(v -> {
            if (cusFieldConfig != null) {
                cusFieldConfig.removeCacheFile(cacheFile);
            }
            sobot_layout_file_list.removeView(item);
            updateUploadButtonVisibility();
            if (cusCallBack != null) {
                cusCallBack.onClickDelete(selfView, cusFieldConfig, cacheFile);
            }
        });

        sobot_layout_file_list.addView(item);
        // 确保 item 间距生效（覆盖 inflate 时 xml 中的 margin 可能丢失的问题）
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
        int marginPx = (int) (8 * getResources().getDisplayMetrics().density + 0.5f);
        lp.setMarginEnd(marginPx);
        item.setLayoutParams(lp);
        updateUploadButtonVisibility();
        hideError();
        // 滚动到最新上传的文件
        sobot_file_list_scroll.post(() -> sobot_file_list_scroll.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
    }

    /**
     * 根据当前已上传数量控制上传区域和文件列表的显示/隐藏
     */
    private void updateUploadButtonVisibility() {
        int count = sobot_layout_file_list.getChildCount();
        sobot_upload_area.setVisibility(count >= maxUploadCount ? View.GONE : View.VISIBLE);
        sobot_file_list_scroll.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * 调整数量上限。需在 {@link #setTipText(int)} / {@link #addPicView(SobotCacheFile)} 之前调用。
     * 默认 50（自定义字段场景），ticket 新建场景调 15。
     */
    public void setMaxCount(int count) {
        if (count > 0) {
            this.maxUploadCount = count;
        }
    }

    /**
     * 当前已上传文件数
     */
    public int getUploadedCount() {
        return sobot_layout_file_list == null ? 0 : sobot_layout_file_list.getChildCount();
    }

    /**
     * 设置标题
     */
    public void setTitle(String title, boolean isMust) {
        String mustFill = "<font color='#F5222D'> *</font>";
        if (isMust) {
            titleText = title + mustFill;
        } else {
            titleText = title;
        }
        if (tvTitle == null) return;
        tvTitle.setText(WebViewSecurityUtil.safeFromHtml(titleText));
    }

    /**
     * 设置标题（含说明图标）
     */
    public void setTitle(String title, boolean isMust, String finalExplain) {
        String endStr = "";
        if (StringUtils.isNoEmpty(finalExplain)) {
            endStr = "<span style='font-size:18px;'> ⓘ</span>";
            tvTitle.setOnClickListener(v -> SobotDialogUtils.startTipDialog(mContext, finalExplain));
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
        tvTitle.setText(WebViewSecurityUtil.safeFromHtml(titleText));
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public void showError(String error) {
        if (!TextUtils.isEmpty(error)) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(error);
        }
    }

    public void hideError() {
        tvError.setVisibility(GONE);
    }

    /**
     * 设置提示文字：最多上传 50 个，大小不超过 Xm
     */
    public void setTipText(int maxStorage) {
        String hideTxt = getResources().getString(R.string.sobot_ticket_update_file_hite);
        sobot_file_hite.setText(String.format(hideTxt, maxUploadCount, maxStorage + "M"));
    }

    public String getValue() {
        return "";
    }

    public SobotCusFieldConfig getCusFieldConfig() {
        return cusFieldConfig;
    }

    public void setCusCallBack(SobotCusFieldListener callBack, SobotUploadView view) {
        this.cusCallBack = callBack;
        this.selfView = view;
    }
}
