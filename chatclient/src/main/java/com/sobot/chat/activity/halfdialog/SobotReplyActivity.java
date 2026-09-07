package com.sobot.chat.activity.halfdialog;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Rect;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotUploadFileAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.HorizontalItemSpacingDecoration;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class SobotReplyActivity extends SobotDialogBaseActivity implements View.OnClickListener {


    /** 附件上传张数上限 */
    private static final int MAX_FILE_COUNT = 15;
    private EditText sobotReplyEdit;
    private RecyclerView sobotReplyMsgPic;
    private ImageView sobot_btn_file;//上传按钮
    private TextView sobotBtnSubmit;

    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();
    private SobotUploadFileAdapter adapter;
    private SobotSelectPicDialog menuWindow;
    private LinearLayout sobot_container, ll_sobot_bottom;


    /**
     * 删除图片弹窗
     */
    protected SobotDeleteWorkOrderDialog seleteMenuWindow;

    protected File cameraFile;
    private String mUid = "";
    private String mCompanyId = "";
    private String mTicketId = "";
    private boolean isSave;

    /**
     * 键盘弹出是否已触发（去重标记）
     * —— 防止 onWindowFocusChanged 多次变化（弹出/关闭输入法面板自身也会触发 focus 变化）重复调用 showSoftInput
     */
    private boolean mKeyboardTriggered = false;

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        // 关键：SobotDialogBaseActivity.onCreate 会按 MarkConfig.LANDSCAPE_SCREEN 强制锁方向。
        // 当详情页处于宽屏（w>=600dp）时，Reply Activity 应跟随调用方方向而非被锁回竖屏，
        // 否则 layout 走 layout/（多行 96dp + 内容被挤压），与 layout-w600dp/ 紧凑横屏布局不一致
        if (getResources().getConfiguration().screenWidthDp >= 600) {
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 回复弹窗显示期间：统一隐藏状态栏（横屏 + 竖屏都隐藏），
        // 避免状态栏时间/信号图标覆盖在详情页标题栏上，造成视觉重叠。
        // ⚠️ 之前担心 FLAG_FULLSCREEN 影响 adjustResize——实际上我们用
        //    ViewTreeObserver.OnGlobalLayoutListener 手动测量键盘高度并给
        //    sobot_container 设置 paddingBottom，不依赖系统 adjustResize。
        //    所以可以放心加 FLAG_FULLSCREEN 隐藏状态栏。
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } catch (Exception e) {
            LogUtils.e("SobotReply set fullscreen failed", e);
        }

        // 横屏（w>=600dp）改造：
        //   - 锁方向到 SENSOR_LANDSCAPE，避免系统选择器临时竖屏后回不来
        //   - 窗口改为 MATCH_PARENT 全屏（透明背景下视觉无感），让 ViewTreeObserver
        //     键盘避让计算有完整的屏幕高度
        //   - 外层 layout-w600dp gravity=bottom 把 60dp 回复栏定位到屏幕底部
        //   - 键盘弹起 → ViewTreeObserver 测量键盘高度 → sobot_container 加
        //     paddingBottom = 键盘高度 → 回复栏自动贴在键盘正上方
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
            // 显式锁方向到 SENSOR_LANDSCAPE：绕过自己的 override（用 super 直接调），
            // 这样后续系统图片选择器（强制竖屏）返回后，Reply 会自己强制回到横屏，
            // 不会出现"选完图片 UI 变成竖屏"的现象
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

            Window window = getWindow();
            if (window != null) {
                WindowManager.LayoutParams lp = window.getAttributes();
                // 关键：必须用 MATCH_PARENT 高度（之前的 WRAP_CONTENT 让窗口只有 60dp，
                // 根本没空间让键盘避让 paddingBottom 生效，必然遮挡）
                // 窗口全屏 + windowBackground=transparent 视觉透明 → Detail 仍可见，
                // 外层 LinearLayout 的 gravity=bottom 把内层 60dp 回复栏定位到屏幕底部
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                lp.gravity = Gravity.NO_GRAVITY;
                window.setAttributes(lp);
                // 浮动 Dialog 主题（windowIsFloating=true）下 adjustResize 其实不生效，
                // 真正的避让靠下面 ViewTreeObserver 给 sobot_container 加 paddingBottom。
                // 这里用 STATE_UNSPECIFIED（禁止系统自动弹键盘），由我们在 onWindowFocusChanged
                // 延迟 300ms 后手动精确控制（避免系统自动弹与手动弹冲突 → "弹起又消失"）
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
            }
        }
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_dialog_reply;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotReplyActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        isSave = true;
        sobot_container = findViewById(R.id.sobot_container);
        ll_sobot_bottom = findViewById(R.id.ll_sobot_bottom);
        ll_sobot_bottom.setOnClickListener(this);

        boolean isWide = getResources().getInteger(R.integer.sobot_list_span_count) > 1;

        // 横屏：用 ViewTreeObserver 监听全局布局变化，通过 getWindowVisibleDisplayFrame 实时测量键盘高度，
        // 然后给外层容器 setPadding(保留原有 left/top/right padding + 调整 bottom)。
        // 保留原有 top padding 是关键：根布局 fitsSystemWindows=true 会自动加 paddingTop=状态栏高度，
        // 若强制清零会让内容顶到状态栏下面，遮挡状态栏图标（走查 #6）。
        // 外层 gravity=bottom 会把回复栏推到键盘正上方；此方案不依赖 ADJUST_RESIZE / WindowInsets，
        // Android 各版本通用稳定。
        if (isWide) {
            final View decorView = getWindow().getDecorView();
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    decorView.getWindowVisibleDisplayFrame(r);
                    int rootHeight = decorView.getRootView().getHeight();
                    int keyboardHeight = rootHeight - r.bottom;
                    // 读取现有 padding（left/top/right 由 fitsSystemWindows 注入，
                    // 比如状态栏高度、导航栏高度，绝对不能清零）
                    int curLeft = sobot_container.getPaddingLeft();
                    int curTop = sobot_container.getPaddingTop();
                    int curRight = sobot_container.getPaddingRight();
                    // 阈值过滤导航栏/状态栏（一般 < 屏高 15%），只在键盘弹起时才加 bottom padding
                    if (keyboardHeight > rootHeight * 0.15) {
                        if (sobot_container.getPaddingBottom() != keyboardHeight) {
                            sobot_container.setPadding(curLeft, curTop, curRight, keyboardHeight);
                        }
                    } else {
                        if (sobot_container.getPaddingBottom() != 0) {
                            sobot_container.setPadding(curLeft, curTop, curRight, 0);
                        }
                    }
                }
            });
        }

        sobot_btn_file = findViewById(R.id.sobot_btn_file);
        sobotReplyEdit = (EditText) findViewById(R.id.sobot_reply_edit);
        sobotReplyMsgPic = findViewById(R.id.sobot_reply_msg_pic);
        // 横屏：跳过焦点变色监听 — setBackground() 会触发 requestLayout() 重测量，
        // 导致 EditText 实际高度膨胀，破坏紧凑 40dp 单行外观
        if (!isWide) {
            sobotReplyEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Drawable bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_dialog_bg_line_4, null);
                        sobotReplyEdit.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(getContext())));
                    } else {
                        Drawable bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_line2_4, null);
                        sobotReplyEdit.setBackground(bgDrawable);
                    }
                }
            });
        }

        // 【关键】先设置 IME flag，再处理焦点/键盘：
        // 如果先 requestFocus 再改 IME flag，某些 ROM 会把已经建立的 InputConnection 断开，
        // 导致后面 showSoftInput 失败。正确顺序：setImeOptions → focusable → requestFocus
        // （真正弹键盘在 onWindowFocusChanged 里，initView 太早 View 还没 attach 到 Window）
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            sobotReplyEdit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI
                    | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        }

        // 确保 EditText 可聚焦（Android 12+ 某些主题默认 focusable=false）
        sobotReplyEdit.setFocusable(true);
        sobotReplyEdit.setFocusableInTouchMode(true);
        // initView 阶段可以先发一次 requestFocus（占位，让 View 准备好）
        // 真正弹键盘在 onWindowFocusChanged(hasFocus=true) 里 —— 那是拿到 InputConnection 的最早可靠时机
        sobotReplyEdit.requestFocus();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        // 设置RecyclerView的LayoutManager
        sobotReplyMsgPic.setLayoutManager(layoutManager);
        sobotReplyMsgPic.addItemDecoration(new HorizontalItemSpacingDecoration(ScreenUtils.dip2px(this, 8), ChatUtils.isRtl(getSobotBaseActivity())));
        sobotBtnSubmit = findViewById(R.id.sobot_btn_submit);
        sobotBtnSubmit.setText(R.string.sobot_btn_submit_text);
        if (ThemeUtils.isChangedThemeColor(getSobotBaseContext())) {
            // 横屏走胶囊背景（圆角 18dp），竖屏沿用原 16dp 圆角背景
            int sendBgRes = isWide ? R.drawable.sobot_bg_reply_send_pill : R.drawable.sobot_bg_theme_color_16dp;
            Drawable bg = getResources().getDrawable(sendBgRes);
            if (bg != null) {
                sobotBtnSubmit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, ThemeUtils.getThemeColor(getSobotBaseContext())));
            }
            sobotBtnSubmit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        }

        ArrayList<SobotFileModel> picTempList = (ArrayList<SobotFileModel>) getIntent().getSerializableExtra("picTempList");
        String replyTempContent = getIntent().getStringExtra("replyTempContent");
        if (!StringUtils.isEmpty(replyTempContent)) {
            sobotReplyEdit.setText(replyTempContent);
        }

        if (picTempList != null && !picTempList.isEmpty()) {
            pic_list.addAll(picTempList);
        }

        sobotBtnSubmit.setOnClickListener(this);
        sobot_btn_file.setOnClickListener(this);
        // 新增：取消按钮点击 → finish（与系统返回等价）
        final TextView sobotBtnCancel = findViewById(R.id.sobot_btn_cancel);
        if (sobotBtnCancel != null) {
            sobotBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        // 诊断：post 等首次布局完成后打印关键尺寸，确认实际生效的 XML
        // 宽屏（横屏 / 折叠屏内屏 / 平板）：完整改用 layout-w600dp/sobot_layout_dialog_reply.xml
        // 单行紧凑布局（📎 + 圆角输入 + 发送 + ✖），无需 Java 端再锁定高度 / 加边框
        adapter = new SobotUploadFileAdapter(SobotReplyActivity.this, pic_list, true, true, new SobotUploadFileAdapter.Listener() {
            @Override
            public void downFileLister(SobotFileModel model) {

            }

            @Override
            public void previewMp4(SobotFileModel fileModel) {
                hideKeyboard();
                File file = new File(fileModel.getFileUrl());
                SobotCacheFile cacheFile = new SobotCacheFile();
                cacheFile.setFileName(file.getName());
                cacheFile.setUrl(fileModel.getFileUrl());
                cacheFile.setFilePath(fileModel.getFileUrl());
                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(fileModel.getFileUrl())));
                cacheFile.setMsgId("" + System.currentTimeMillis());
                Intent intent = SobotVideoActivity.newIntent(SobotReplyActivity.this, cacheFile);
                SobotReplyActivity.this.startActivity(intent);
            }

            @Override
            public void deleteFile(final SobotFileModel fileModel) {
                hideKeyboard();
                String popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_picture);
                if (fileModel != null) {
                    if (!TextUtils.isEmpty(fileModel.getFileUrl()) && MediaFileUtils.isVideoFileType(fileModel.getFileUrl())) {
                        popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_video);
                    }
                }
                if (seleteMenuWindow != null) {
                    seleteMenuWindow.dismiss();
                    seleteMenuWindow = null;
                }
                if (seleteMenuWindow == null) {
                    seleteMenuWindow = new SobotDeleteWorkOrderDialog(SobotReplyActivity.this, popMsg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seleteMenuWindow.dismiss();
                            if (v.getId() == R.id.btn_pick_photo) {
                                LogUtils.e(seleteMenuWindow.getPosition() + "");
                                pic_list.remove(fileModel);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                seleteMenuWindow.show();
            }

            @Override
            public void previewPic(String fileUrl, String fileName) {
                hideKeyboard();
                if (SobotOption.imagePreviewListener != null) {
                    //如果返回true,拦截;false 不拦截
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(getSobotBaseContext(), fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                // 收集 pic_list 中所有图片类型附件，调用支持左右切换的 PreviewDialog
                java.util.List<String> imageUrls = new java.util.ArrayList<>();
                int startIndex = 0;
                for (com.sobot.chat.api.model.SobotFileModel f : pic_list) {
                    int t = com.sobot.chat.widget.attachment.FileTypeConfig.getFileType(f.getFileType());
                    if (t == com.sobot.chat.widget.attachment.FileTypeConfig.MSGTYPE_FILE_PIC) {
                        if (android.text.TextUtils.equals(f.getFileUrl(), fileUrl)) {
                            startIndex = imageUrls.size();
                        }
                        imageUrls.add(f.getFileUrl());
                    }
                }
                if (imageUrls.size() > 1) {
                    new com.sobot.chat.widget.dialog.SobotCusFieldImagePreviewDialog(
                            SobotReplyActivity.this, imageUrls, startIndex, true).show();
                } else {
                    // 单张：保持原 SobotPhotoActivity 跳转（含长按下载等额外功能）
                    Intent intent = new Intent(SobotReplyActivity.this, SobotPhotoActivity.class);
                    intent.putExtra("imageUrL", fileUrl);
                    startActivity(intent);
                }
            }
        });
        sobotReplyMsgPic.setAdapter(adapter);
        if (pic_list.size() > 0) {
            sobotReplyMsgPic.setVisibility(View.VISIBLE);
        }
        mUid = getIntent().getStringExtra(ChatUtils.INTENT_KEY_UID);
        mCompanyId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_COMPANYID);
        mTicketId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_TICKET_ID);
        // 横屏宽屏（layout-w600dp）已通过单行 40dp 紧凑布局自行处理刘海避让，
        // 不能再替换 EditText 的 LayoutParams（旧逻辑会硬塞 height=104dp + MATCH_PARENT 宽度，
        // 把输入框撑成 104dp 高、且 weight=1 失效后挤掉发送/✕ 按钮）
        if (!isWide && ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(SobotReplyActivity.this, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(SobotReplyActivity.this, 104));
                            lp.setMargins((rect.right > 110 ? 110 : rect.right) + ScreenUtils.dip2px(SobotReplyActivity.this, 20), (rect.right > 110 ? 110 : rect.right) + ScreenUtils.dip2px(SobotReplyActivity.this, 20), ScreenUtils.dip2px(SobotReplyActivity.this, 20), ScreenUtils.dip2px(SobotReplyActivity.this, 20));
                            sobotReplyEdit.setLayoutParams(lp);
                        }
                    }
                }
            });

        }
        // 挖孔避让由基类统一处理（对白色背景层整体内缩），不再对附件列表单独避让（避免叠加错位）
    }

    @Override
    protected void initData() {

    }

    @Override
    public void finish() {
        if (isSave) {
            Intent intent = new Intent();
            intent.putExtra("replyTempContent", sobotReplyEdit.getText().toString());
            intent.putExtra("picTempList", (Serializable) pic_list);
            intent.putExtra("isTemp", true);
            setResult(Activity.RESULT_OK, intent);
        }
        super.finish();
    }

    @Override
    public void onClick(View v) {

        if (v == sobot_btn_file) {
            if (pic_list.size() >= MAX_FILE_COUNT) {
                //附件上限，正常情况下按钮已隐藏，此处仅兜底
                ToastUtil.showToast(this, String.format(getResources().getString(R.string.sobot_ticket_update_file_max_hite), MAX_FILE_COUNT));
            } else {
                menuWindow = new SobotSelectPicDialog(SobotReplyActivity.this, itemsOnClick);
                menuWindow.show();
            }
//            sobotReplyEdit.clearFocus();
//            sobot_btn_file.requestFocus();
//            hideKeyboard();
        } else if (v == ll_sobot_bottom) {//底部点击不能关闭页面
            sobotReplyEdit.clearFocus();
            sobot_btn_file.requestFocus();
            hideKeyboard();
        } else if (v == sobotBtnSubmit) {//提交
            sobotReplyEdit.clearFocus();
            sobotBtnSubmit.requestFocus();
            hideKeyboard();
            if (StringUtils.isEmpty(sobotReplyEdit.getText().toString().trim())) {
                ToastUtil.showToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_please_input_reply_no_empty));
                return;
            }
            if (FastClickUtils.isCanClick()) {
                SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                sobotBtnSubmit.setAlpha(0.5f);
                sobotBtnSubmit.setEnabled(false);
                sobotBtnSubmit.setClickable(false);
                zhiChiApi.replyTicketContent(this, mUid, mTicketId, sobotReplyEdit.getText().toString(), getFileStr(), mCompanyId, new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        sobotBtnSubmit.setAlpha(1f);
                        sobotBtnSubmit.setEnabled(true);
                        sobotBtnSubmit.setClickable(true);
                        LogUtils.e(s);
                        ToastUtil.showCustomToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_leavemsg_success_tip), R.drawable.sobot_icon_success);
                        try {
                            Thread.sleep(500);//睡眠一秒  延迟拉取数据
                        } catch (InterruptedException e) {
                            LogUtils.e("uncaught", e);
                        }
                        pic_list.clear();
                        Intent intent = new Intent();
                        intent.putExtra("replyTempContent", "");
                        intent.putExtra("picTempList", (Serializable) pic_list);
                        intent.putExtra("isTemp", false);
                        setResult(Activity.RESULT_OK, intent);
                        SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                        isSave = false;//删除临时数据
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        sobotBtnSubmit.setAlpha(1f);
                        sobotBtnSubmit.setEnabled(true);
                        sobotBtnSubmit.setClickable(true);
                        ToastUtil.showCustomToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_leavemsg_error_tip));
                        LogUtils.e("uncaught", e);
                        SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    }
                });
            }
        }
    }

    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == R.id.btn_take_photo) {
                LogUtils.i("拍照");
                selectPicFromCamera();

            }
            if (v.getId() == R.id.btn_pick_photo) {
                LogUtils.i("选择照片");
                selectPicFromLocal();
            }
            if (v.getId() == R.id.btn_pick_vedio) {
                LogUtils.i("选择视频");
                selectVedioFromLocal();
            }
        }
    };

    public void addPicView(SobotFileModel item) {
        if (sobotReplyMsgPic.getVisibility() == View.GONE) {
            sobotReplyMsgPic.setVisibility(View.VISIBLE);
        }
        pic_list.add(item);
        adapter.notifyDataSetChanged();
        sobotReplyMsgPic.scrollToPosition(adapter.getItemCount() - 1);
    }


    public String getFileStr() {
        String tmpStr = "";
        for (int i = 0; i < pic_list.size(); i++) {
            tmpStr += pic_list.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(SobotReplyActivity.this);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, SobotReplyActivity.this);
                    }
                    // 异步解析路径：大视频 content:// 同步 getPath 会拷贝到 cache 耗时 1~3s 阻塞主线程
                    SobotDialogUtils.startProgressDialog(this);
                    final Uri finalSelectedImage = selectedImage;
                    ImageUtils.getPathAsync(this, selectedImage, new ImageUtils.OnPathCallback() {
                        @Override
                        public void onResult(String path) {
                            if (StringUtils.isEmpty(path)) {
                                SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                                showHint(getContext().getResources().getString(R.string.sobot_did_not_get_picture_path));
                                return;
                            }
                            if (MediaFileUtils.isVideoFileType(path)) {
                                try {
                                    File selectedFile = new File(path);
                                    if (selectedFile.exists()) {
                                        if (selectedFile.length() > 50 * 1024 * 1024) {
                                            SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                                            ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_file_upload_failed));
                                            return;
                                        }
                                    }
                                    String fName = MD5Util.encode(path);
                                    String filePath;
                                    try {
                                        filePath = FileUtil.saveImageFile(SobotReplyActivity.this, finalSelectedImage, fName + FileUtil.getFileEndWith(path), path);
                                    } catch (Exception e) {
                                        LogUtils.e("uncaught", e);
                                        SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                                        ToastUtil.showToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_pic_type_error));
                                        return;
                                    }
                                    sendFileListener.onSuccess(filePath);
                                } catch (Exception e) {
                                    LogUtils.e("uncaught", e);
                                }
                            } else {
                                ChatUtils.sendPicByUriPost(SobotReplyActivity.this, finalSelectedImage, sendFileListener, false);
                            }
                        }
                    });
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(this);
                    ChatUtils.sendPicByFilePath(this, cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_pic_select_again));
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                        ChatUtils.sendPicByFilePath(SobotReplyActivity.this, tmpPic.getAbsolutePath(), sendFileListener, true);
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                }
            }
        }

    }

    public void showHint(String content) {
        ToastUtil.showToast(getApplicationContext(), content);
    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotReplyActivity.this, mCompanyId, mUid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {

                    SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    if (zhiChiMessage.getData() != null) {
                        SobotFileModel item = new SobotFileModel();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                        item.setFileName(fileName);
                        item.setFileType(fileType);
                        addPicView(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    showHint(TextUtils.isEmpty(des) ? getResources().getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
        }
    };


    /**
     * Window 首次真正获得焦点时才触发键盘弹出 —— 这是浮动 Dialog 主题（windowIsFloating=true）
     * 下唯一可靠的时机：onCreate / onStart / onResume 阶段 View 还没 attach 到 Window，
     * InputConnection 未建立，showSoftInput() 100% 返回 false。
     *
     * 【关键】延迟 300ms 再真正执行：
     *   - 等待 Activity 启动动画完全结束（启动动画过程中 Window 焦点会短暂切换）
     *   - 等待 DecorView 真正 attach 到 WindowManager
     *   - 避免"键盘刚弹起来 → Activity 焦点被 IME 窗口抢走 → 系统又收起键盘"的闪一下问题
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !mKeyboardTriggered) {
            mKeyboardTriggered = true;
            if (sobotReplyEdit != null) {
                sobotReplyEdit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        triggerShowKeyboard();
                    }
                }, 300);
            } else {
                triggerShowKeyboard();
            }
        }
    }

    /**
     * 软键盘弹出 —— 简化版稳定触发（避免 showSoftInput + toggleSoftInput 互相冲突导致弹了又收）
     *
     * 修复之前"弹起又消失"的根因：
     *  ① 旧代码同时调用 showSoftInput(SHOW_FORCED) 和 toggleSoftInput(SHOW_FORCED)，
     *    两个调用在 IMM 队列里互相覆盖，某些 ROM 会判定"先弹再切一次状态"→ 直接收键盘；
     *  ② 旧代码立即触发，Activity 启动动画还没结束，Window 焦点在 Activity / Launcher / IME
     *    之间短暂切换 → 系统拿到 focus=false 后主动收键盘。
     *
     * 现在做法：
     *  - 只调 showSoftInput(SHOW_FORCED)（不用 toggleSoftInput，避免状态切换冲突）
     *  - 前面已经延迟 300ms 让焦点稳定，再在 450ms 后二次兜底（确保 InputConnection 已建立）
     *  - 每次调用前都检查 isFinishing()，避免泄露
     */
    private void triggerShowKeyboard() {
        if (sobotReplyEdit == null || isFinishing()) {
            return;
        }
        try {
            // 0. 前置：确保 EditText 可聚焦并真有焦点（如果还没拿到，再要一次）
            sobotReplyEdit.setFocusable(true);
            sobotReplyEdit.setFocusableInTouchMode(true);
            if (!sobotReplyEdit.hasFocus()) {
                sobotReplyEdit.requestFocus();
            }
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }
            // 仅用 showSoftInput(SHOW_FORCED)：不用 toggleSoftInput 避免状态切换冲突
            //（国产 ROM 对浮动 Dialog 窗口下 toggleSoftInput 有时会判定为"已显示，所以切到隐藏"）
            imm.showSoftInput(sobotReplyEdit, InputMethodManager.SHOW_FORCED);
            // 450ms 二次兜底：国产 ROM / 低版本 InputConnection 建立延迟，
            // 首次 showSoftInput 会失败，等 450ms 再试一次基本 100% 成功
            sobotReplyEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) {
                        return;
                    }
                    try {
                        if (sobotReplyEdit != null && sobotReplyEdit.hasFocus()) {
                            imm.showSoftInput(sobotReplyEdit, InputMethodManager.SHOW_FORCED);
                        } else if (sobotReplyEdit != null) {
                            sobotReplyEdit.requestFocus();
                            imm.showSoftInput(sobotReplyEdit, InputMethodManager.SHOW_FORCED);
                        }
                    } catch (Exception e) {
                        LogUtils.e("SobotReply retry keyboard failed", e);
                    }
                }
            }, 450);
        } catch (Exception e) {
            LogUtils.e("SobotReply trigger keyboard failed", e);
        }
    }


}