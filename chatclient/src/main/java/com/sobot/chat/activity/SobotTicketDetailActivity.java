package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.activity.halfdialog.SobotTicketEvaluateActivity;
import com.sobot.chat.adapter.SobotTicketDetailAdapter;
import com.sobot.chat.adapter.SobotUploadFileAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.api.model.StTicketDetailInfo;
import com.sobot.chat.api.model.StUserDealTicketReplyInfo;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.HorizontalItemSpacingDecoration;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotCusFieldImagePreviewDialog;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 留言详情页面
 * <p>
 * 展示单条留言工单的详细信息，包括工单内容、回复列表等。
 * 底部操作栏根据工单状态动态显示：
 * - 未完成工单：显示"回复"按钮
 * - 已完成且可评价工单：显示"回复"和"评价"按钮（根据按钮文字长度自动切换水平/垂直布局）
 * - 已完成且不可回复工单：仅显示"评价"按钮
 * <p>
 * 支持功能：
 * 1. 查看留言工单详情及回复列表
 * 2. 回复工单（详情页底部 inline 输入栏，支持文本 + 附件上传，替代原 SobotReplyActivity 弹窗）
 * 3. 评价工单（跳转 {@link SobotTicketEvaluateActivity}）
 * 4. 返回时弹出评价弹窗（通过 {@link Information#isShowLeaveDetailBackEvaluate()} 控制，每个工单仅弹一次）
 */
public class SobotTicketDetailActivity extends SobotChatBaseActivity implements View.OnClickListener {

    /** 附件上传张数上限 */
    private static final int MAX_FILE_COUNT = 15;

    private String mUid = "";           // 用户ID
    private String mCompanyId = "";     // 企业ID
    private String mTicketId = "";      // 工单ID
    private boolean refresh;            // 回复或评价后需要重新刷新标记，用于返回列表页时通知刷新
    private Information information;     // 用户配置信息，包含留言相关开关设置

    private List<Object> mList = new ArrayList<>();  // 详情数据列表，第一项为工单信息，后续为回复列表
    private SobotTicketDetailAdapter mAdapter;       // 详情列表适配器
    private RecyclerView recyclerView;               // 详情列表
    private LinearLayout sobot_ticket_bottom_ll;            // 底部操作栏：仅显示回复按钮（未完成工单）
    private LinearLayout sobot_ticket_success_bottom_ll;    // 底部操作栏：工单完成后的评价+回复区域
    private LinearLayout v_bottom_btns, sobot_evaluate_v, sobot_reply_v;  // 垂直布局的评价和回复按钮（按钮文字过长时使用）
    private LinearLayout h_bottom_btns, sobot_evaluate_h, sobot_reply_h;  // 水平布局的评价和回复按钮（默认布局）
    private TextView sobot_evaluate_tv;  // 评价按钮文字，用于判断文字行数以切换水平/垂直布局

    // 横屏右侧面板 4 个圆形图标按钮（Figma node 658:22370 / 745:57673），仅 layout-w600dp 引用，
    // 竖屏 / Pad / 折叠屏内屏 findViewById 返回 null，所有访问都包在 R.integer.sobot_list_span_count > 1 分支内
    private LinearLayout ll_side_action;    // 整块右侧面板，回复弹窗弹起时整体 GONE
    private ImageView iv_side_scroll_up;    // ↑ 滚动到列表顶
    private ImageView iv_side_evaluate;     // 👍 评价
    private ImageView iv_side_reply;        // 💬 回复（主题色填充）
    private ImageView iv_side_scroll_down;  // ↓ 滚动到列表底

    private SobotUserTicketEvaluate mEvaluate;  // 当前工单的评价配置信息
    private StTicketDetailInfo mTicketInfo;      // 当前工单详情数据
    private List<SobotTicketStatus> statusList;                   // 工单状态列表，用于在详情中显示状态标签

    // ===== inline 回复输入栏（从 SobotReplyActivity 迁入，替代原弹窗方案）=====
    // 布局已通过 include 嵌入三档详情布局，id=sobot_reply_container，默认 gone
    private LinearLayout sobot_reply_container;     // include 根容器，控制 inline 栏显隐
    private EditText sobot_reply_edit;               // 回复输入框
    private ImageView sobot_btn_file;                // 附件按钮
    private TextView sobot_btn_submit;               // 发送按钮
    private TextView sobot_btn_cancel;               // 取消按钮（收起 inline 栏，不提交）
    private RecyclerView sobot_reply_msg_pic;        // 附件横向 chip 列表
    private ArrayList<SobotFileModel> mPicList = new ArrayList<>();  // 附件列表数据
    private SobotUploadFileAdapter mUploadAdapter;                   // 附件适配器
    private SobotSelectPicDialog mSelectPicDialog;                   // 选图弹窗（相册 / 拍照 / 视频）
    private SobotDeleteWorkOrderDialog mDeleteDialog;                // 删除附件确认弹窗
    private int mCurrentBottomType = 0;              // 当前底部栏类型，inline 栏收起后按此恢复 showBottom
    // 键盘避让：configChanges 含 keyboardHidden 时 adjustResize 不触发窗口 resize，
    // 用 ViewTreeObserver 手动测量键盘高度给根布局加 paddingBottom，确保 inline 栏被推到键盘上方
    private ViewTreeObserver.OnGlobalLayoutListener mKeyboardListener;
    private View mDecorView;
    // ===== 走查（新）：底部导航栏避让 + 输入框与键盘 10dp 间距 =====
    // 横屏 FLAG_FULLSCREEN 下窗口延伸进三键/手势导航栏区域，API<35 或 targetSdk<35 时
    // 基类的 Android15 避让不生效，需本类捕获 navigationBars inset 手动避让（否则回复栏压在底部按键区上）
    private int mNavInsetBottom = 0;                   // 底部导航栏高度（px），仅 needSelfNavBarAvoidance() 场景非 0
    private boolean mKeyboardShowing = false;          // 键盘是否弹起（onGlobalLayout 维护；insets 重派发时防止覆盖键盘避让值）
    private int mReplyContainerPadBottomXml = 0;       // 容器 XML 初始 paddingBottom（initReplyContainer 缓存，收起/销毁时精确恢复）
    private static final int REPLY_BOTTOM_GAP_DP = 10; // 回复栏与键盘 / 底部按键区之间的走查间距
    /**
     * 唤起 inline 回复栏前列表是否已停在底部：
     * - true  → 键盘弹起时列表整体上移、继续锚定最后一条（"留言底部展示完"场景）
     * - false → 键盘弹起时保持当前可视锚点不跳底，列表仍可自由滚到顶/底（"未展示完"场景）
     * 默认 true：非回复流程的高度压缩（如有）维持旧的贴底行为
     */
    private boolean mReplyAnchorAtBottom = true;
    /**
     * "未展示完"场景的可视锚点：首个完全可见 item 的 position 与其 top 相对 RecyclerView paddingTop 的偏移。
     * mReplyAnchorAtBottom=false 时有效，配合 LinearLayoutManager.scrollToPositionWithOffset 恢复
     */
    private int mReplyAnchorPos = RecyclerView.NO_POSITION;
    private int mReplyAnchorOffset = 0;

    /**
     * 创建启动留言详情页的 Intent
     *
     * @param context   上下文
     * @param companyId 企业ID
     * @param uid       用户ID
     * @param ticketId  工单ID
     * @return 配置好参数的 Intent
     */
    public static Intent newIntent(Context context, String companyId, String uid, String ticketId) {
        Intent intent = new Intent(context, SobotTicketDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(ChatUtils.INTENT_KEY_UID, uid);
        bundle.putString(ChatUtils.INTENT_KEY_COMPANYID, companyId);
        bundle.putSerializable(ChatUtils.INTENT_KEY_TICKET_ID, ticketId);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_ticket_detail;
    }

    /**
     * 从 Intent 中解析传递的参数：用户ID、企业ID、工单ID、工单状态列表
     */
    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            mUid = getIntent().getStringExtra(ChatUtils.INTENT_KEY_UID);
            mCompanyId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_COMPANYID);
            mTicketId = getIntent().getStringExtra(ChatUtils.INTENT_KEY_TICKET_ID);
            statusList = ChatUtils.getStatusList();

        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketDetailActivity";
    }

    /**
     * 初始化视图控件
     * - 设置标题栏返回按钮（返回时判断是否需要弹出评价弹窗）
     * - 初始化详情列表 RecyclerView
     * - 初始化底部操作栏（回复、评价按钮的水平/垂直两种布局）
     */
    @Override
    protected void initView() {
        showLeftMenu(true);
        // 返回按钮点击：已完成工单首次返回时弹出评价弹窗
        getLeftMenu().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List ticketIds = (List) SharedPreferencesUtil.getObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds");
                //已完成留言详情界面：返回时是否弹出服务评价窗口(只会第一次返回弹，下次返回不会再弹)
                if (information != null && information.isShowLeaveDetailBackEvaluate() && sobot_ticket_success_bottom_ll.getVisibility() == View.VISIBLE) {
                    if (ticketIds != null && ticketIds.contains(mTicketId)) {
                        finish();
                    } else {
                        if (ticketIds == null) {
                            ticketIds = new ArrayList();
                        }
                        ticketIds.add(mTicketId);
                        SharedPreferencesUtil.saveObject(SobotTicketDetailActivity.this, "showBackEvaluateTicketIds", ticketIds);
                        Intent intent = new Intent(SobotTicketDetailActivity.this, SobotTicketEvaluateActivity.class);
                        intent.putExtra("sobotUserTicketEvaluate", mEvaluate);
                        startActivityForResult(intent, ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE);
                    }
                } else {
                    finish();
                }
            }
        });
        setTitle(R.string.sobot_message_details);
        recyclerView = findViewById(R.id.sobot_listview);
        displayInNotch(recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // stackFromEnd 不再固定为 true（其副作用：内容不满一屏时列表贴底显示，
        // 导致"只有一条工单内容"时不居顶而在底部）。
        // 改为动态策略：内容不满一屏 → false 贴顶；内容超一屏 → true 贴底锚定，
        // 由 applyListBottomAlignment() 在数据刷新后评估（见该方法注释）。
        // 设置RecyclerView的LayoutManager
        recyclerView.setLayoutManager(layoutManager);
        // 走查 #4：监听 RecyclerView 高度变化（键盘弹起 / inline 回复栏显隐压缩高度），重新锚定。
        // 锚点策略（高度被压缩 bottom < oldBottom，即键盘/输入栏弹起时）：
        // - 唤起前已在底部（"留言底部展示完"）→ scrollToPosition(末条) + stackFromEnd，整体上移继续贴底；
        // - 唤起前不在底部（"未展示完"）→ scrollToPositionWithOffset(唤起前首个可见 item + 偏移)，
        //   保持当前可视内容不动、不强制跳底，列表仍可自由滚动到最顶/最底。
        // 高度恢复（bottom > oldBottom，键盘收起）时按同一锚点对称还原，避免收起后位置漂移。
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mAdapter == null || mAdapter.getItemCount() <= 0) {
                    return;
                }
                if (bottom < oldBottom) {
                    // 高度被压缩：键盘/输入栏弹起
                    if (mReplyAnchorAtBottom) {
                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                        if (lm instanceof LinearLayoutManager) {
                            // 弹起时内容可能从"不满屏"变为"超屏"，置 stackFromEnd=true 保证 scrollToPosition
                            // 精确贴底（解决 scrollToPosition 只保证可见不贴底的锚点漂移）
                            ((LinearLayoutManager) lm).setStackFromEnd(true);
                        }
                        recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    } else if (mReplyAnchorPos != RecyclerView.NO_POSITION) {
                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                        if (lm instanceof LinearLayoutManager) {
                            ((LinearLayoutManager) lm).scrollToPositionWithOffset(mReplyAnchorPos, mReplyAnchorOffset);
                        }
                    }
                } else if (bottom > oldBottom) {
                    // 高度恢复：键盘/输入栏收起
                    if (!mReplyAnchorAtBottom && mReplyAnchorPos != RecyclerView.NO_POSITION) {
                        // 还原"未展示完"场景的原可视锚点
                        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                        if (lm instanceof LinearLayoutManager) {
                            ((LinearLayoutManager) lm).scrollToPositionWithOffset(mReplyAnchorPos, mReplyAnchorOffset);
                        }
                    } else if (mReplyAnchorAtBottom) {
                        // 已在底部场景：重新按"满屏与否"评估对齐方式——
                        // 收起后内容若不满一屏应回到贴顶（如仅一条工单内容），避免残留 stackFromEnd=true 贴底
                        applyListBottomAlignment();
                    }
                }
            }
        });

        sobot_ticket_success_bottom_ll = findViewById(R.id.sobot_ticket_success_bottom_ll);
        sobot_ticket_bottom_ll = findViewById(R.id.sobot_ticket_bottom_ll);
        sobot_ticket_bottom_ll.setOnClickListener(this);

        h_bottom_btns = findViewById(R.id.h_bottom_btns);
        sobot_reply_h = findViewById(R.id.sobot_reply_h);
        sobot_evaluate_h = findViewById(R.id.sobot_evaluate_h);

        sobot_evaluate_tv = findViewById(R.id.sobot_evaluate_tv);
        sobot_reply_h.setOnClickListener(this);
        sobot_evaluate_h.setOnClickListener(this);

        v_bottom_btns = findViewById(R.id.v_bottom_btns);
        sobot_evaluate_v = findViewById(R.id.sobot_evaluate_v);
        sobot_reply_v = findViewById(R.id.sobot_reply_v);
        sobot_reply_v.setOnClickListener(this);
        sobot_evaluate_v.setOnClickListener(this);

        mAdapter = new SobotTicketDetailAdapter(SobotTicketDetailActivity.this, mList);
        recyclerView.setAdapter(mAdapter);

        // 横屏：force 设置 RecyclerView 底部留白 + clipToPadding=false
        // —— 走查 #7 未通过"消息记录底部需要多余的间距像素"。
        // XML 里已设 paddingBottom=32dp + clipToPadding=false，但 Presenter 数据加载完成 /
        // 调用 notifyDataSetChanged 后，某些 itemDecoration 或布局流程会覆盖这两个属性，
        // 这里在 setAdapter 之后再写一次，确保最终生效（竖屏 R.integer.span_count<=1 不执行，不影响竖屏布局）。
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
            // 32dp 底部留白：不引用 R.dimen.sobot_32dp（资源里未定义），直接 dip2px 转像素值，避免资源缺失编译报错
            int padBottom = ScreenUtils.dip2px(SobotTicketDetailActivity.this, 32);
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    padBottom);
            recyclerView.setClipToPadding(false);
            recyclerView.setClipChildren(false);
        }

        // 横屏右侧面板 4 个圆形图标按钮（layout-w600dp 才有，竖屏返回 null）
        ll_side_action = findViewById(R.id.ll_side_action);
        iv_side_scroll_up = findViewById(R.id.iv_side_scroll_up);
        iv_side_evaluate = findViewById(R.id.iv_side_evaluate);
        iv_side_reply = findViewById(R.id.iv_side_reply);
        iv_side_scroll_down = findViewById(R.id.iv_side_scroll_down);
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1 && iv_side_reply != null) {
            // 主题色染色回复按钮背景
            Drawable replyBg = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_side_btn_primary, null);
            if (replyBg != null) {
                iv_side_reply.setBackground(ThemeUtils.applyColorWithMultiplyMode(replyBg, ThemeUtils.getThemeColor(this)));
            }
            // 回复图标固定白色（图标在主题色背景上需反白可见，走查 Fix1）
            Drawable replyIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_ic_side_reply, null);
            if (replyIcon != null) {
                iv_side_reply.setImageDrawable(ThemeUtils.applyColorToDrawable(replyIcon, Color.WHITE));
            }
            // 阴影兜底：按钮背景是 selector 套 layer-list 的多层 drawable，
            // 部分 API 上这类背景无法向默认 ViewOutlineProvider.BACKGROUND 提供有效 outline，
            // 导致 XML 配置的 elevation 阴影整体不渲染（"右侧按钮没有阴影"的根因）。
            // 显式设置 oval outline provider，保证 4dp/6dp elevation 的圆形阴影在所有 API 21+ 设备可见。
            ViewOutlineProvider ovalProvider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            };
            iv_side_scroll_up.setOutlineProvider(ovalProvider);
            iv_side_evaluate.setOutlineProvider(ovalProvider);
            iv_side_reply.setOutlineProvider(ovalProvider);
            iv_side_scroll_down.setOutlineProvider(ovalProvider);
            iv_side_scroll_up.setOnClickListener(this);
            iv_side_evaluate.setOnClickListener(this);
            iv_side_reply.setOnClickListener(this);
            iv_side_scroll_down.setOnClickListener(this);
        }

        // 初始化 inline 回复输入栏（替代原 SobotReplyActivity 弹窗）
        initReplyContainer();
    }

    /**
     * 初始化 inline 回复输入栏控件（从 SobotReplyActivity 迁入）。
     * 绑定 include 布局中的 EditText / 附件按钮 / 发送 / 取消 / 附件 RecyclerView，
     * 设置附件适配器、点击监听、发送按钮主题色。
     */
    private void initReplyContainer() {
        sobot_reply_container = findViewById(R.id.sobot_reply_container);
        sobot_reply_edit = findViewById(R.id.sobot_reply_edit);
        sobot_btn_file = findViewById(R.id.sobot_btn_file);
        sobot_btn_submit = findViewById(R.id.sobot_btn_submit);
        sobot_btn_cancel = findViewById(R.id.sobot_btn_cancel);
        sobot_reply_msg_pic = findViewById(R.id.sobot_reply_msg_pic);

        // 附件横向列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        sobot_reply_msg_pic.setLayoutManager(layoutManager);
        sobot_reply_msg_pic.addItemDecoration(new HorizontalItemSpacingDecoration(ScreenUtils.dip2px(this, 8), ChatUtils.isRtl(this)));

        // 发送按钮主题色背景（换肤时染色），文字始终白色（Fix4）
        boolean isWide = getResources().getInteger(R.integer.sobot_list_span_count) > 1;
        int sendBgRes = isWide ? R.drawable.sobot_bg_reply_send_pill : R.drawable.sobot_bg_theme_color_16dp;
        Drawable sendBg = ResourcesCompat.getDrawable(getResources(), sendBgRes, null);
        if (sendBg != null) {
            if (ThemeUtils.isChangedThemeColor(this)) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(sendBg, ThemeUtils.getThemeColor(this)));
            }
        }
        sobot_btn_submit.setTextColor(getResources().getColor(R.color.sobot_color_white));

        // 附件适配器：支持删除 / 预览图片 / 预览视频 / 预览文件
        mUploadAdapter = new SobotUploadFileAdapter(this, mPicList, true, true, new SobotUploadFileAdapter.Listener() {
            @Override
            public void downFileLister(SobotFileModel model) {
                // 文件类附件（xlsx/doc/pdf/txt/zip…）：跳文件预览页（下载后打开）。
                // 之前是空实现——点击非图片非视频附件完全无反应（走查补齐）。
                // fileType 换算对齐 SobotTicketDetailAdapter.downFileLister：SobotFileModel.fileType
                // 是 String 后缀（"xlsx"），SobotCacheFile.fileType 是 FileTypeConfig 体系 int
                hideKeyboard();
                if (model == null || TextUtils.isEmpty(model.getFileUrl())) {
                    return;
                }
                SobotCacheFile cacheFile = new SobotCacheFile();
                cacheFile.setFileName(model.getFileName());
                cacheFile.setUrl(model.getFileUrl());
                cacheFile.setFilePath(!TextUtils.isEmpty(model.getFileLocalPath()) ? model.getFileLocalPath() : model.getFileUrl());
                cacheFile.setFileType(FileTypeConfig.getFileType(model.getFileType()));
                cacheFile.setMsgId(model.getFileId());
                Intent intent = new Intent(SobotTicketDetailActivity.this, SobotFileDetailActivity.class);
                intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
                startActivity(SobotVideoActivity.newIntent(SobotTicketDetailActivity.this, cacheFile));
            }

            @Override
            public void deleteFile(final SobotFileModel fileModel) {
                hideKeyboard();
                String popMsg = getString(R.string.sobot_do_you_delete_picture);
                if (fileModel != null && !TextUtils.isEmpty(fileModel.getFileUrl()) && MediaFileUtils.isVideoFileType(fileModel.getFileUrl())) {
                    popMsg = getString(R.string.sobot_do_you_delete_video);
                }
                if (mDeleteDialog != null) {
                    mDeleteDialog.dismiss();
                    mDeleteDialog = null;
                }
                mDeleteDialog = new SobotDeleteWorkOrderDialog(SobotTicketDetailActivity.this, popMsg, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDeleteDialog.dismiss();
                        if (v.getId() == R.id.btn_pick_photo) {
                            mPicList.remove(fileModel);
                            mUploadAdapter.notifyDataSetChanged();
                        }
                    }
                });
                mDeleteDialog.show();
            }

            @Override
            public void previewPic(String fileUrl, String fileName) {
                hideKeyboard();
                if (SobotOption.imagePreviewListener != null) {
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(SobotTicketDetailActivity.this, fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                // 收集 mPicList 中所有图片类型附件，多图用 PreviewDialog 支持左右切换
                List<String> imageUrls = new ArrayList<>();
                int startIndex = 0;
                for (SobotFileModel f : mPicList) {
                    int t = FileTypeConfig.getFileType(f.getFileType());
                    if (t == FileTypeConfig.MSGTYPE_FILE_PIC) {
                        if (TextUtils.equals(f.getFileUrl(), fileUrl)) {
                            startIndex = imageUrls.size();
                        }
                        imageUrls.add(f.getFileUrl());
                    }
                }
                if (imageUrls.size() > 1) {
                    new SobotCusFieldImagePreviewDialog(SobotTicketDetailActivity.this, imageUrls, startIndex, true).show();
                } else {
                    Intent intent = new Intent(SobotTicketDetailActivity.this, SobotPhotoActivity.class);
                    intent.putExtra("imageUrL", fileUrl);
                    startActivity(intent);
                }
            }
        });
        sobot_reply_msg_pic.setAdapter(mUploadAdapter);

        // 按钮点击监听
        sobot_btn_file.setOnClickListener(this);
        sobot_btn_submit.setOnClickListener(this);
        sobot_btn_cancel.setOnClickListener(this);

        // 走查（新）：缓存容器 XML 初始 paddingBottom（此时键盘/insets 逻辑尚未改写），
        // 键盘收起、inline 栏隐藏、onDestroy 时用它精确还原，替代原先按 dimen 恢复导致的与 XML 脱节
        mReplyContainerPadBottomXml = sobot_reply_container.getPaddingBottom();
        // 注册底部导航栏 inset 监听（仅横屏全屏且基类 Android15 避让未生效的场景，见方法注释）
        initNavBarInsetListener();
    }

    /**
     * 注册底部导航栏 inset 监听（走查：回复栏避让手机底部按键区）。
     * 仅横屏全屏（LANDSCAPE_SCREEN 开启 FLAG_FULLSCREEN，窗口延伸进导航栏区域）且基类
     * Android15 避让未生效时启用；竖屏由系统自动 resize 避让、Android15 由基类给
     * view_root 加 bottomInset，这两种情况再叠加 navInset 会造成双重避让（底部多出一条空隙）。
     * 导航栏显隐 / 旋转导致 inset 重派发时，若 inline 栏可见且键盘未弹起，实时重算避让。
     */
    private void initNavBarInsetListener() {
        if (!needSelfNavBarAvoidance()) {
            return;
        }
        View rootView = findViewById(R.id.view_root);
        if (rootView == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(rootView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                mNavInsetBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                if (sobot_reply_container != null
                        && sobot_reply_container.getVisibility() == View.VISIBLE
                        && !mKeyboardShowing) {
                    applyReplyBottomPadding(0);
                }
                // 右侧 4 按钮面板同避让底部按键区：面板 gravity=center 垂直居中，短屏（h≈360dp）
                // 且 4 键全显时按钮组底部余量仅 ~34dp，会被 48dp 三键导航栏压住；
                // 给面板加 paddingBottom 后居中区整体上移，任何屏高均不侵入按键区
                // （竖屏 / Pad 布局无 ll_side_action，null 直接跳过；面板随回复流程 GONE 时 padding 保留无副作用）
                if (ll_side_action != null && ll_side_action.getPaddingBottom() != mNavInsetBottom) {
                    ll_side_action.setPadding(
                            ll_side_action.getPaddingLeft(),
                            ll_side_action.getPaddingTop(),
                            ll_side_action.getPaddingRight(),
                            mNavInsetBottom);
                }
                return insets;
            }
        });
    }

    /**
     * 是否需要本类自行避让底部导航栏：
     * - Android15（API35+ 且 targetSdk35+）：基类已给 view_root 加 bottomInset → false
     * - 横屏（LANDSCAPE_SCREEN 开启 FLAG_FULLSCREEN）：窗口延伸进导航栏区域 → true
     * - 竖屏：窗口由系统 resize 到导航栏之上 → false
     */
    private boolean needSelfNavBarAvoidance() {
        if (Build.VERSION.SDK_INT >= 35
                && CommonUtils.getTargetSdkVersion(this) >= 35) {
            return false;
        }
        return ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN);
    }

    /**
     * 统一计算并应用 inline 回复栏 paddingBottom（走查：输入框与键盘/底部按键区间距 10dp）。
     * - keyboardHeightPx > 0（键盘弹起）：pad = 键盘高 + 10dp（keyboardHeight 已含被键盘
     *   覆盖的导航栏高度，不重复叠加 mNavInsetBottom）
     * - keyboardHeightPx == 0（键盘收起/未弹起）：pad = 导航栏高 + 10dp（避让三键/手势条）
     * 改这里会影响：showReplyContainer / startKeyboardAvoidance / initNavBarInsetListener 三处调用，
     * 恢复 XML 原值统一走 stopKeyboardAvoidance。
     */
    private void applyReplyBottomPadding(int keyboardHeightPx) {
        if (sobot_reply_container == null) {
            return;
        }
        int gapPx = ScreenUtils.dip2px(this, REPLY_BOTTOM_GAP_DP);
        int padBottom = (keyboardHeightPx > 0 ? keyboardHeightPx : mNavInsetBottom) + gapPx;
        if (sobot_reply_container.getPaddingBottom() != padBottom) {
            sobot_reply_container.setPadding(
                    sobot_reply_container.getPaddingLeft(),
                    sobot_reply_container.getPaddingTop(),
                    sobot_reply_container.getPaddingRight(),
                    padBottom);
        }
    }

    /**
     * 初始化数据
     * 1. 从 SharedPreferences 获取用户配置信息
     * 2. 隐藏所有底部操作栏（等待数据加载后根据工单状态显示）
     * 3. 如果工单状态列表为空，先请求状态列表再请求详情；否则直接请求详情
     */
    @Override
    protected void initData() {
        information = (Information) SharedPreferencesUtil.getObject(SobotTicketDetailActivity.this, "sobot_last_current_info");

        h_bottom_btns.setVisibility(View.GONE);
        v_bottom_btns.setVisibility(View.GONE);

        sobot_ticket_bottom_ll.setVisibility(View.GONE);
        sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
        if (statusList == null || statusList.size() == 0) {
            String companyId = SharedPreferencesUtil.getStringData(this,
                    ZhiChiConstant.SOBOT_CONFIG_COMPANYID, "");
            String languageCode = SharedPreferencesUtil.getStringData(this, ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
            zhiChiApi.getTicketStatus(this, companyId, languageCode, new StringResultCallBack<List<SobotTicketStatus>>() {
                @Override
                public void onSuccess(List<SobotTicketStatus> sobotTicketStatuses) {
                    ChatUtils.setStatusList(sobotTicketStatuses);
                    if (statusList == null) {
                        statusList = new ArrayList<>();
                    } else {
                        statusList.clear();
                    }
                    statusList.addAll(sobotTicketStatuses);
                    mAdapter.setStatusList(statusList);
                    requestDate();
                }

                @Override
                public void onFailure(Exception e, String s) {
                    requestDate();
                }
            });
        } else {
            mAdapter.setStatusList(statusList);
            requestDate();
        }

    }

    /**
     * 请求留言工单详情数据
     * 成功后：标记工单回复已读，刷新列表数据，根据工单状态显示底部操作栏，滚动到底部
     */
    public void requestDate() {
        SobotDialogUtils.startProgressDialog(this);
        zhiChiApi.getUserTicketDetail(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, new StringResultCallBack<StTicketDetailInfo>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(StTicketDetailInfo datas) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseContext());
                //工单回复标记已读
                zhiChiApi.updateUserTicketReplyInfo(SobotTicketDetailActivity.this, mCompanyId, information.getPartnerid(), mTicketId);
                //留言详情
                if (datas != null) {
                    mTicketInfo = datas;
                    mEvaluate = datas.getCusNewSatisfactionVO();
                    mList.clear();
                    mList.add(datas);
                    if (datas.getReplyList() != null && !datas.getReplyList().isEmpty()) {
                        mList.addAll(datas.getReplyList());
//                    } else {
//                        mList.add(true);
                    }

                    int type = 0;//0:只显示回复, 1、评价和回复都显示，2 只显示评价 ,3、评价和回复都不显示
                    // 是否显示已评价
                    if (mTicketInfo.getIsShowSatisfactionButton() == 1 && mTicketInfo.getIsEvaluated() == 0 && datas.getCusNewSatisfactionVO() != null) {
                        //显示评价
                        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY)) {
                            type = 1;//显示评价
                        } else {
                            type = 2;//显示评价不显示回复
                        }
                    }else {
                        //查询工单状态类型
                        SobotTicketStatus ticketStatus = getStatus(mTicketInfo.getTicketStatus());
                        //不显示评价
                        if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY) && ticketStatus.getCustomerStatusCode() == 3 ){
                            //隐藏回复
                            type = 3;
                        }
                    }
                    showBottom(type);
//                } else {
//                    mList.add(true);
                }
                mAdapter.notifyDataSetChanged();
                // 数据刷新后按"满屏与否"设置列表对齐方式：
                // 内容不满一屏（如仅一条工单内容）→ stackFromEnd=false，内容贴顶显示；
                // 内容超一屏 → stackFromEnd=true，贴底锚定（配合下方 scrollToPosition 精确停在最后一条）
                applyListBottomAlignment();
                // 数据更新后滚动到底部
                // —— 走查 #9：回复提交 / 评价完成后网络返回刷新列表，必须锚定到最后一条消息末尾，
                //    避免新插入的 reply cell 被底部按钮 / 右侧横屏面板遮挡（用户看不见新回复，
                //    还以为没发送成功）。
                // 策略：立即做一次非平滑 scrollToPosition，再 postDelayed 200ms 做一次 smoothScroll
                // 兜底（等 notifyDataSetChanged 之后真正的 layout / itemAnimator 跑完全流程，
                // 防止首次 scroll 只滚到"可见"而非"贴底"）
                int newItemCount = mAdapter.getItemCount();
                if (newItemCount > 0) {
                    recyclerView.scrollToPosition(newItemCount - 1);
                    final int finalCount = newItemCount;
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mAdapter != null && recyclerView != null
                                    && mAdapter.getItemCount() >= finalCount) {
                                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    }, 200);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                SobotDialogUtils.stopProgressDialog(getSobotBaseContext());
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
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
    /**
     * 底部显示的类型
     *
     * @param type 0:只显示回复, 1、评价和回复都显示，2 只显示评价,3、评价和回复都不显示
     */
    private void showBottom(int type) {
        mCurrentBottomType = type;  // 记录当前底部栏类型，inline 回复栏收起后按此恢复
        // 横屏：使用右侧 4 图标面板（Figma node 658-22370 / 745-57673）
        // type=0 显示 滚顶+回复+滚底；type=1 全显；type=2 显示 滚顶+评价+滚底；type=3 仅 滚顶+滚底
        if (getResources().getInteger(R.integer.sobot_list_span_count) > 1 && iv_side_reply != null) {
            iv_side_scroll_up.setVisibility(View.VISIBLE);
            iv_side_scroll_down.setVisibility(View.VISIBLE);
            // 评价：type=1 / type=2 且有评价对象时显示；type=0 / type=3 隐藏
            iv_side_evaluate.setVisibility((type == 1 || type == 2) && mEvaluate != null ? View.VISIBLE : View.GONE);
            // 回复：type=0 / type=1 显示；type=2 / type=3 隐藏
            iv_side_reply.setVisibility(type == 2 || type == 3 ? View.GONE : View.VISIBLE);
            // 横屏走查 #7：右侧面板显隐后（含 evaluate_tv.post 异步切换），延迟重新锚定到底部，
            // 覆盖底部布局变化导致 RecyclerView 可用高度改变后的锚点漂移
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null && mAdapter.getItemCount() > 0) {
                        recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }
            });
            return;
        }
        if (type == 0) {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.VISIBLE);
        } else if (type == 1) {
            sobot_ticket_success_bottom_ll.setVisibility(View.VISIBLE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
            if (getResources().getInteger(R.integer.sobot_list_span_count) > 1) {
                // 宽屏右侧面板较窄：固定走垂直布局，跳过 lineCount 自动切换
                v_bottom_btns.setVisibility(View.VISIBLE);
                h_bottom_btns.setVisibility(View.GONE);
            } else {
                // 竖屏：按 lineCount 自动切换水平/垂直
                v_bottom_btns.setVisibility(View.GONE);
                h_bottom_btns.setVisibility(View.VISIBLE);
                sobot_evaluate_tv.post(new Runnable() {
                    // 在视图布局完成后执行的代码
                    @Override
                    public void run() {
                        Layout layout = sobot_evaluate_tv.getLayout();
                        if (layout != null) {
                            int lineCount = layout.getLineCount();
                            LogUtils.d("=======1====lineCount==" + lineCount);
                            if (lineCount > 1) {
                                v_bottom_btns.setVisibility(View.VISIBLE);
                                h_bottom_btns.setVisibility(View.GONE);
                            } else {
                                v_bottom_btns.setVisibility(View.GONE);
                                h_bottom_btns.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }

        } else if (type == 2) {
            //只显示评价：h / v 两套布局各持有独立的评价 / 回复按钮，回复按钮需分别隐藏，
            //同时恢复评价按钮显隐（覆盖此前 type=1 → type=2 切换后残留的状态）
            sobot_ticket_success_bottom_ll.setVisibility(View.VISIBLE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
            sobot_evaluate_h.setVisibility(View.VISIBLE);
            sobot_reply_h.setVisibility(View.GONE);
            h_bottom_btns.setVisibility(View.VISIBLE);
            v_bottom_btns.setVisibility(View.GONE);
            sobot_evaluate_v.setVisibility(View.VISIBLE);
            sobot_reply_v.setVisibility(View.GONE);
            // 评价文案过长时自动切换垂直布局（与 type=1 同规则）
            sobot_evaluate_tv.post(new Runnable() {
                // 在视图布局完成后执行的代码
                @Override
                public void run() {
                    Layout layout = sobot_evaluate_tv.getLayout();
                    if (layout != null) {
                        int lineCount = layout.getLineCount();
                        LogUtils.d("=======1====lineCount==" + lineCount);
                        if (lineCount > 1) {
                            v_bottom_btns.setVisibility(View.VISIBLE);
                            h_bottom_btns.setVisibility(View.GONE);
                        } else {
                            v_bottom_btns.setVisibility(View.GONE);
                            h_bottom_btns.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

        } else if (type == 3) {
            //评价和回复都不显示：两个底部操作栏整体隐藏
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);

        } else {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
        }
    }

    /**
     * 横屏：切换右侧 4 图标面板整体可见性。
     * 用于回复弹窗弹起 / 关闭时联动隐藏 / 恢复（避免与回复栏并排干扰）。
     * 竖屏（iv_side_reply == null）直接 no-op。
     *
     * @param visible true 恢复显示（按当前 type 由 showBottom 决定具体哪几个），false 全隐藏
     */
    private void setSidePanelVisibility(boolean visible) {
        if (getResources().getInteger(R.integer.sobot_list_span_count) <= 1 || ll_side_action == null) {
            return;
        }
        // 整块右侧面板 GONE / VISIBLE：GONE 时左侧 RecyclerView（weight=1）自动扩展为全宽
        ll_side_action.setVisibility(visible ? View.VISIBLE : View.GONE);
        // Fix4: 分割线随侧边面板一起隐藏/恢复，避免列表全宽时右侧残留 1dp 竖线
        View divider = findViewById(R.id.v_side_divider);
        if (divider != null) {
            divider.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 提交工单满意度评价
     *
     * @param score               评分
     * @param remark              评价内容
     * @param labelTag            评价标签
     * @param defaultQuestionFlag 默认问题标记
     */
    public void submitEvaluate(final int score, final String remark, final String labelTag, final int defaultQuestionFlag) {
        zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, score, remark, labelTag, defaultQuestionFlag, new StringResultCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                ToastUtil.showCustomToast(SobotTicketDetailActivity.this, getResources().getString(R.string.sobot_leavemsg_success_tip), R.drawable.sobot_icon_success);
                // 乐观更新：提交接口已确认成功（retCode=000000 并回显评价内容），本地立即
                // 更新底部按钮与评价记录；不再延迟重拉详情——服务端详情查询存在写读延迟，
                // 延迟刷新拿到的旧数据（isEvaluated=0 / 无评价记录）会把界面打回原样，
                // 表现为"评价按钮不消失、无评价数据"。重进页面时 onCreate 的
                // requestDate() 自然拉到落库完成后的最新数据
                applyLocalEvaluateResult(score, remark, labelTag, defaultQuestionFlag);
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    /**
     * 评价提交成功后的本地乐观更新（不依赖详情接口刷新）。
     * 背景：提交接口已返回成功（retCode=000000 并回显评价内容），但详情查询接口存在
     * 写读延迟，重拉详情拿到的 isEvaluated/replyList 仍是旧值，导致界面"评价按钮不消失、
     * 无评价记录"。此处直接用本次提交参数本地更新 UI：
     * - isEvaluated 置 1，按"已评价"重算底部按钮（评价入口消失，规则与 requestDate 一致）
     * - 构造 itemType=2 的评价记录（StUserDealTicketReplyInfo）追加列表、滚动到底
     * 已知边界：后续任何 requestDate()（回复成功 / 重进页面）会用服务端数据整体重建列表，
     * 若彼时服务端仍未同步评价记录，本地乐观数据会被覆盖——联动看 submitReply onSuccess
     * 与 requestDate。
     */
    private void applyLocalEvaluateResult(int score, String remark, String labelTag, int defaultQuestionFlag) {
        if (mTicketInfo != null) {
            // 置已评价后按 requestDate 同规则重算 type：不可回复且工单已解决 → 3（底部隐藏），
            // 否则 0（仅显示回复）
            mTicketInfo.setIsEvaluated(1);
            int type;
            SobotTicketStatus ticketStatus = getStatus(mTicketInfo.getTicketStatus());
            if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LEAVE_COMPLETE_CAN_REPLY)
                    && ticketStatus != null && ticketStatus.getCustomerStatusCode() == 3) {
                type = 3;
            } else {
                type = 0;
            }
            showBottom(type);
        }
        // 本地构造评价记录：itemType=2 → MSG_TYPE_EVALUATE，startType=1（我）
        StUserDealTicketReplyInfo evaluateItem = new StUserDealTicketReplyInfo();
        evaluateItem.setItemType(2);
        evaluateItem.setStartType(1);
        evaluateItem.setScore(score);
        evaluateItem.setRemark(remark);
        evaluateItem.setReplyTime(System.currentTimeMillis());
        // 标签：提交时为逗号分隔字符串，拆回列表供评价 item 渲染
        if (!StringUtils.isEmpty(labelTag)) {
            evaluateItem.setTags(Arrays.asList(labelTag.split(",")));
        }
        // defaultQuestionFlag 2=未选择 → 转 -1，让评价 item 隐藏"是否已解决"行
        //（与 Adapter 的 questionFlag>=0 才显示的规则对齐）
        evaluateItem.setQuestionFlag(defaultQuestionFlag == 2 ? -1 : defaultQuestionFlag);
        mList.add(evaluateItem);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        scrollDetailListToBottomSafe();
    }

    /**
     * 点击事件处理
     * - 回复按钮（水平 / 垂直布局 / 底部回复栏 / 横屏右侧面板）：显示 inline 回复输入栏
     * - 评价按钮（水平 / 垂直布局 / 横屏右侧面板）：跳转评价页面
     * - inline 栏附件按钮：弹出选图对话框（相册 / 拍照 / 视频）
     * - inline 栏发送按钮：提交回复
     * - inline 栏取消按钮：收起 inline 栏，恢复底部操作栏
     * - 横屏右侧面板滚动按钮：滚动列表到顶 / 底
     */
    @Override
    public void onClick(View v) {
        if (v == sobot_reply_h || v == sobot_reply_v || v == sobot_ticket_bottom_ll || v == iv_side_reply) {
            // 回复入口：显示 inline 输入栏（替代原跳转 SobotReplyActivity）
            showReplyContainer();
        } else if (v == sobot_evaluate_h || v == sobot_evaluate_v || v == iv_side_evaluate) {
            //评价（含横屏右侧面板评价图标）
            if (mEvaluate != null) {
                Intent intent = new Intent(SobotTicketDetailActivity.this, SobotTicketEvaluateActivity.class);
                intent.putExtra("sobotUserTicketEvaluate", mEvaluate);
                startActivityForResult(intent, ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE);
            }
        } else if (v == sobot_btn_file) {
            // inline 栏附件按钮：弹出选图对话框
            if (mPicList.size() >= MAX_FILE_COUNT) {
                ToastUtil.showToast(this, String.format(getString(R.string.sobot_ticket_update_file_max_hite), MAX_FILE_COUNT));
            } else {
                mSelectPicDialog = new SobotSelectPicDialog(this, itemsOnClick);
                mSelectPicDialog.show();
            }
        } else if (v == sobot_btn_submit) {
            // inline 栏发送按钮：提交回复
            submitReply();
        } else if (v == sobot_btn_cancel) {
            // inline 栏取消按钮：收起输入栏，恢复底部操作栏
            hideReplyContainer();
        } else if (v == iv_side_scroll_up) {
            //横屏右侧面板：滚动列表到顶
            if (recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
                recyclerView.smoothScrollToPosition(0);
            }
        } else if (v == iv_side_scroll_down) {
            //横屏右侧面板：滚动列表到底
            if (recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
                recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }


    /**
     * 物理返回键处理：如果有回复或评价操作，设置 RESULT_OK 通知列表页刷新
     */
    @Override
    public void onBackPressed() {
        if (mTicketInfo != null && refresh) {
            setResult(Activity.RESULT_OK);
        }
        super.onBackPressed();
    }

    /**
     * 处理子页面 / 系统选择器返回结果
     * - REQUEST_CODE_picture：相册选图返回，异步解析路径后上传
     * - REQUEST_CODE_makePictureFromCamera：拍照返回，上传
     * - SobotCameraActivity.RESULT_CODE：自定义相机返回（拍照 / 视频）
     * - EXTRA_TICKET_EVALUATE_REQUEST_CODE：评价页面返回，提交评价并刷新
     * - EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE：返回时弹出的评价页面返回，提交评价后关闭当前页面
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) {
                // 相册选图返回：异步解析路径后上传
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, this);
                    }
                    SobotDialogUtils.startProgressDialog(this);
                    final Uri finalSelectedImage = selectedImage;
                    ImageUtils.getPathAsync(this, selectedImage, new ImageUtils.OnPathCallback() {
                        @Override
                        public void onResult(String path) {
                            if (StringUtils.isEmpty(path)) {
                                SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                                showHint(getString(R.string.sobot_did_not_get_picture_path));
                                return;
                            }
                            if (MediaFileUtils.isVideoFileType(path)) {
                                try {
                                    File selectedFile = new File(path);
                                    if (selectedFile.exists() && selectedFile.length() > 50 * 1024 * 1024) {
                                        SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                                        ToastUtil.showToast(getApplicationContext(), getString(R.string.sobot_file_upload_failed));
                                        return;
                                    }
                                    String fName = MD5Util.encode(path);
                                    String filePath;
                                    try {
                                        filePath = FileUtil.saveImageFile(SobotTicketDetailActivity.this, finalSelectedImage, fName + FileUtil.getFileEndWith(path), path);
                                    } catch (Exception e) {
                                        LogUtils.e("uncaught", e);
                                        SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                                        ToastUtil.showToast(getApplicationContext(), getString(R.string.sobot_pic_type_error));
                                        return;
                                    }
                                    sendFileListener.onSuccess(filePath);
                                } catch (Exception e) {
                                    LogUtils.e("uncaught", e);
                                }
                            } else {
                                ChatUtils.sendPicByUriPost(SobotTicketDetailActivity.this, finalSelectedImage, sendFileListener, false);
                            }
                        }
                    });
                } else {
                    showHint(getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                // 拍照返回
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(this);
                    ChatUtils.sendPicByFilePath(this, cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getString(R.string.sobot_pic_select_again));
                }
            } else if (requestCode == ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_CODE) {
                //提交评价
                submitEvaluate(data.getIntExtra("score", 0), data.getStringExtra("content"), data.getStringExtra("labelTag"), data.getIntExtra("defaultQuestionFlag", -1));
            } else if (requestCode == ZCSobotConstant.EXTRA_TICKET_EVALUATE_REQUEST_FINISH_CODE && null != data) {
                final int score = data.getIntExtra("score", 0);
                final String remark = data.getStringExtra("content");
                final String labelTag = data.getStringExtra("labelTag");
                final int defaultQuestionFlag = data.getIntExtra("defaultQuestionFlag", -1);
                zhiChiApi.addTicketSatisfactionScoreInfo(SobotTicketDetailActivity.this, mUid, mCompanyId, mTicketId, score, remark, labelTag, defaultQuestionFlag, new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        ToastUtil.showCustomToastWithListenr(SobotTicketDetailActivity.this, getResources().getString(R.string.sobot_leavemsg_success_tip), 1000, new ToastUtil.OnAfterShowListener() {
                            @Override
                            public void doAfter() {
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        ToastUtil.showToast(getApplicationContext(), des);
                    }
                });
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            // 自定义相机返回（拍照 / 视频）
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(this);
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(this);
                        ChatUtils.sendPicByFilePath(this, tmpPic.getAbsolutePath(), sendFileListener, true);
                    } else {
                        showHint(getString(R.string.sobot_pic_select_again));
                    }
                }
            }
        }
    }

    /**
     * 按内容是否占满一屏动态设置列表对齐方式（post 到下一帧，等 notifyDataSetChanged 的 layout 完成）：
     * - 全部 item 完全可见（内容不满一屏）→ stackFromEnd=false，内容从顶部开始显示
     *   （否则 stackFromEnd=true 会让少量内容贴底，"仅一条工单内容居底"的问题）
     * - 内容超一屏 → stackFromEnd=true，贴底锚定，保证 scrollToPosition/stackFromEnd 组合
     *   在键盘弹起、回复刷新时精确停在最后一条消息末尾（不锚点漂移）
     * 全模式生效（竖屏/横屏/Pad）。
     */
    private void applyListBottomAlignment() {
        if (recyclerView == null) {
            return;
        }
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || recyclerView == null || mAdapter == null) {
                    return;
                }
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
                if (!(lm instanceof LinearLayoutManager) || mAdapter.getItemCount() <= 0) {
                    return;
                }
                LinearLayoutManager linear = (LinearLayoutManager) lm;
                // 首个与最后一个 item 都完全可见 → 内容不满一屏
                boolean allVisible = linear.findFirstCompletelyVisibleItemPosition() == 0
                        && linear.findLastCompletelyVisibleItemPosition() >= mAdapter.getItemCount() - 1;
                boolean wantStackFromEnd = !allVisible;
                if (linear.getStackFromEnd() != wantStackFromEnd) {
                    linear.setStackFromEnd(wantStackFromEnd);
                }
            }
        });
    }

    /**
     * 把详情 RecyclerView 锚定到最后一条消息末尾。
     * 用于"唤起 inline 回复栏前 / 回复提交刷新列表后 / 底部栏显隐切换时"
     * 确保最后一条消息完整可见，不会与底部回复按钮 / inline 输入栏 / 横屏右侧面板重合。
     *
     * @param smooth true 用 smoothScroll（动画流畅）；false 用 scrollToPosition（立即跳转）
     */
    private void scrollDetailListToBottom(boolean smooth) {
        if (recyclerView == null || mAdapter == null) {
            return;
        }
        final int count = mAdapter.getItemCount();
        if (count <= 0) {
            return;
        }
        if (smooth) {
            recyclerView.smoothScrollToPosition(count - 1);
        } else {
            recyclerView.scrollToPosition(count - 1);
        }
        // 二次兜底：postDelayed 260ms 再检查一次——
        // RecyclerView 在 layout 动画、IME 高度变化、右侧面板显隐切换时，
        // 第一次 scroll 可能因为新的 layout 请求被冲掉，导致"看起来在底部但还差一点"，
        // 多做一次确保最终稳定停留在末尾（Excel 走查 #9 的场景）。
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recyclerView == null || mAdapter == null) {
                    return;
                }
                int curCount = mAdapter.getItemCount();
                if (curCount <= 0) {
                    return;
                }
                recyclerView.smoothScrollToPosition(curCount - 1);
            }
        }, 260);
    }

    // ===== inline 回复输入栏显隐控制（替代原 SobotReplyActivity 弹窗）=====

    /**
     * 显示 inline 回复输入栏：
     * - 隐藏底部操作栏（sobot_ticket_bottom_ll / sobot_ticket_success_bottom_ll）
     * - 横屏额外隐藏右侧面板（setSidePanelVisibility(false)）
     * - 显示 inline 容器（sobot_reply_container VISIBLE）
     * - EditText requestFocus + 弹键盘
     * - 列表贴底，避免最后一条消息被输入栏遮挡
     */
    private void showReplyContainer() {
        // 锚点捕获（必须在任何显隐/高度变化之前）：
        // - 已在底部 → mReplyAnchorAtBottom=true，后续高度压缩整体上移贴底（"底部展示完"场景）
        // - 不在底部 → 记录首个可见 item position + 偏移，键盘弹起时保持该锚点不跳底（"未展示完"场景）
        captureReplyAnchor();
        // 隐藏底部操作栏
        if (sobot_ticket_bottom_ll != null) {
            sobot_ticket_bottom_ll.setVisibility(View.GONE);
        }
        if (sobot_ticket_success_bottom_ll != null) {
            sobot_ticket_success_bottom_ll.setVisibility(View.GONE);
        }
        // 横屏：隐藏右侧 4 图标面板 + 分割线，避免与底部输入栏并排干扰
        setSidePanelVisibility(false);
        // 显示 inline 回复输入栏（Fix3: VISIBLE 后根 LinearLayout 重新分配高度，
        // content area weight=1 自动收缩，RecyclerView 被推到 inline 栏上方）
        if (sobot_reply_container != null) {
            sobot_reply_container.setVisibility(View.VISIBLE);
            sobot_reply_container.requestLayout();
        }
        // 走查（新）：键盘尚未弹起的瞬间先避让底部导航栏（navInset + 10dp gap），
        // 防止回复栏短暂压在三键/手势条上；键盘弹起后 onGlobalLayout 覆盖为 键盘高 + 10dp
        mKeyboardShowing = false;
        applyReplyBottomPadding(0);
        // 注册键盘高度监听：configChanges 含 keyboardHidden 导致 adjustResize 不触发窗口 resize，
        // 必须手动测量键盘高度并给根布局加 paddingBottom，否则键盘盖住 inline 输入栏
        startKeyboardAvoidance();
        // EditText 获取焦点 + 弹出键盘
        if (sobot_reply_edit != null) {
            sobot_reply_edit.setFocusable(true);
            sobot_reply_edit.setFocusableInTouchMode(true);
            sobot_reply_edit.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(sobot_reply_edit, InputMethodManager.SHOW_FORCED);
            }
        }
        // Fix3+Fix5: 多次延迟滚到底，覆盖 inline 栏布局完成 + 键盘弹起两个阶段。
        // 仅"唤起前已在底部"时贴底；"未展示完"场景由 onLayoutChange 的锚点还原保持位置，
        // 不做强制贴底（否则会覆盖 scrollToPositionWithOffset 的锚定结果）
        if (mReplyAnchorAtBottom) {
            scrollDetailListToBottom(true);
            recyclerView.postDelayed(this::scrollDetailListToBottomSafe, 100);
            recyclerView.postDelayed(this::scrollDetailListToBottomSafe, 300);
            recyclerView.postDelayed(this::scrollDetailListToBottomSafe, 500);
        }
    }

    /**
     * 捕获唤起 inline 回复栏前的列表锚点状态，供 onLayoutChange 高度变化时分流锚定：
     * - 在底部（canScrollVertically(1)==false）→ mReplyAnchorAtBottom=true
     * - 不在底部 → 记录 LinearLayoutManager 首个可见 item 的 position 与 top 偏移
     */
    private void captureReplyAnchor() {
        if (recyclerView == null) {
            mReplyAnchorAtBottom = true;
            mReplyAnchorPos = RecyclerView.NO_POSITION;
            return;
        }
        mReplyAnchorAtBottom = !recyclerView.canScrollVertically(1);
        if (mReplyAnchorAtBottom) {
            mReplyAnchorPos = RecyclerView.NO_POSITION;
            return;
        }
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            LinearLayoutManager linear = (LinearLayoutManager) lm;
            mReplyAnchorPos = linear.findFirstVisibleItemPosition();
            View anchorChild = mReplyAnchorPos != RecyclerView.NO_POSITION
                    ? linear.findViewByPosition(mReplyAnchorPos) : null;
            mReplyAnchorOffset = anchorChild != null
                    ? anchorChild.getTop() - recyclerView.getPaddingTop() : 0;
        } else {
            mReplyAnchorAtBottom = true;
            mReplyAnchorPos = RecyclerView.NO_POSITION;
        }
    }

    /**
     * 注册键盘高度监听，手动测量键盘高度并给根布局加 paddingBottom。
     * 解决 configChanges=keyboardHidden 下 adjustResize 不生效的问题。
     * inline 栏 VISIBLE 时调 start，收起时调 stop。
     */
    private void startKeyboardAvoidance() {
        mDecorView = getWindow().getDecorView();
        if (mDecorView == null) return;
        // 先移除旧监听，避免重复注册
        stopKeyboardAvoidance();
        mKeyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mDecorView == null || sobot_reply_container == null) return;
                android.graphics.Rect r = new android.graphics.Rect();
                mDecorView.getWindowVisibleDisplayFrame(r);
                int rootHeight = mDecorView.getRootView().getHeight();
                int keyboardHeight = rootHeight - r.bottom;
                // 阈值过滤导航栏/状态栏（一般 < 屏高 15%），只在键盘弹起时才加 bottom padding
                if (keyboardHeight > rootHeight * 0.15) {
                    // 键盘弹起：pad = 键盘高 + 10dp gap（走查：输入框与键盘间距 10dp；
                    // keyboardHeight 已含被键盘覆盖的导航栏高度，不叠加 mNavInsetBottom）
                    mKeyboardShowing = true;
                    applyReplyBottomPadding(keyboardHeight);
                } else {
                    // 键盘收起：pad = 导航栏高 + 10dp gap（走查：避让手机底部按键区）
                    mKeyboardShowing = false;
                    applyReplyBottomPadding(0);
                }
            }
        };
        mDecorView.getViewTreeObserver().addOnGlobalLayoutListener(mKeyboardListener);
    }

    /**
     * 移除键盘高度监听，恢复 inline 栏原始 padding
     */
    private void stopKeyboardAvoidance() {
        if (mDecorView != null && mKeyboardListener != null) {
            mDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(mKeyboardListener);
            mKeyboardListener = null;
        }
        mKeyboardShowing = false;
        if (sobot_reply_container != null) {
            // 恢复 initReplyContainer 缓存的 XML 初始值（inline 栏即将 GONE，下次 VISIBLE 重新计算避让）
            sobot_reply_container.setPadding(
                    sobot_reply_container.getPaddingLeft(),
                    sobot_reply_container.getPaddingTop(),
                    sobot_reply_container.getPaddingRight(),
                    mReplyContainerPadBottomXml);
        }
    }

    /**
     * 安全滚到底（空安全包装，供 postDelayed 回调使用）
     */
    private void scrollDetailListToBottomSafe() {
        if (!isFinishing() && recyclerView != null && mAdapter != null && mAdapter.getItemCount() > 0) {
            recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    /**
     * 收起 inline 回复输入栏（取消回复）：
     * - 隐藏 inline 容器
     * - 隐藏键盘
     * - 恢复底部操作栏（按 mCurrentBottomType 调 showBottom）
     * - 横屏恢复右侧面板 setSidePanelVisibility(true)
     */
    private void hideReplyContainer() {
        // 移除键盘监听 + 恢复 padding
        stopKeyboardAvoidance();
        // 必须在容器 GONE 之前收键盘：容器 GONE 后 EditText 失焦，getCurrentFocus 拿到的
        // token 无效会导致 hideSoftInputFromWindow 静默失败（点 x 关闭后键盘残留的根因）。
        // 用 decorView 的 windowToken 强制隐藏，兼容所有 ROM（与 submitReply 同款做法）
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        // 兜底
        hideKeyboard();
        if (sobot_reply_container != null) {
            sobot_reply_container.setVisibility(View.GONE);
        }
        // 恢复底部操作栏（showBottom 内部按横竖屏分流处理）
        showBottom(mCurrentBottomType);
        // 横屏：恢复右侧面板
        setSidePanelVisibility(true);
        // 重置锚点状态：回复流程结束，恢复默认"贴底"语义，下次唤起重新捕获
        resetReplyAnchor();
    }

    /**
     * 重置回复锚点状态为默认值（贴底语义），在下一次 showReplyContainer 时重新捕获
     */
    private void resetReplyAnchor() {
        mReplyAnchorAtBottom = true;
        mReplyAnchorPos = RecyclerView.NO_POSITION;
        mReplyAnchorOffset = 0;
    }

    /**
     * 提交回复：
     * - 校验输入内容非空
     * - 调用 replyTicketContent 接口
     * - 成功后清空 EditText / 附件、收起 inline 栏、恢复底部栏、requestDate() 刷新
     * - 失败后恢复发送按钮可点击状态
     */
    private void submitReply() {
        // 先移除键盘监听（防止 onGlobalLayout 回调干扰后续 hideKeyboard）
        stopKeyboardAvoidance();
        // 点击发送立即收回键盘：用 decorView 的 windowToken 最可靠
        // （clearFocus 后 getCurrentFocus 可能已不是 EditText，token 不对 hideSoftInput 会失败）
        if (sobot_reply_edit != null) {
            sobot_reply_edit.clearFocus();
        }
        sobot_btn_submit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // 用 decorView 的 token 强制隐藏，兼容所有 ROM
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        // 兜底
        hideKeyboard();
        if (sobot_reply_edit == null || StringUtils.isEmpty(sobot_reply_edit.getText().toString().trim())) {
            ToastUtil.showToast(getApplicationContext(), getString(R.string.sobot_please_input_reply_no_empty));
            return;
        }
        if (FastClickUtils.isCanClick()) {
            SobotDialogUtils.startProgressDialog(this);
            sobot_btn_submit.setAlpha(0.5f);
            sobot_btn_submit.setEnabled(false);
            sobot_btn_submit.setClickable(false);
            zhiChiApi.replyTicketContent(this, mUid, mTicketId, sobot_reply_edit.getText().toString(), getFileStr(), mCompanyId, new StringResultCallBack<String>() {
                @Override
                public void onSuccess(String s) {
                    sobot_btn_submit.setAlpha(1f);
                    sobot_btn_submit.setEnabled(true);
                    sobot_btn_submit.setClickable(true);
                    LogUtils.e(s);
                    ToastUtil.showCustomToast(getApplicationContext(), getString(R.string.sobot_leavemsg_success_tip), R.drawable.sobot_icon_success);
                    // 清空输入框和附件
                    sobot_reply_edit.setText("");
                    mPicList.clear();
                    mUploadAdapter.notifyDataSetChanged();
                    sobot_reply_msg_pic.setVisibility(View.GONE);
                    // 收起 inline 栏，恢复底部操作栏
                    // 移除键盘监听 + 恢复 padding（必须在 hideKeyboard 前调）
                    stopKeyboardAvoidance();
                    // 隐藏键盘（回复成功后收回）
                    hideKeyboard();
                    if (sobot_reply_container != null) {
                        sobot_reply_container.setVisibility(View.GONE);
                    }
                    showBottom(mCurrentBottomType);
                    setSidePanelVisibility(true);
                    // 重置锚点：提交成功后 requestDate 会刷新并贴底展示新消息，
                    // 恢复默认"贴底"语义，避免高度恢复时误用旧锚点回跳
                    resetReplyAnchor();
                    try {
                        Thread.sleep(500);  // 延迟拉取数据，等待服务端写入完成
                    } catch (InterruptedException e) {
                        LogUtils.e("uncaught", e);
                    }
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                    refresh = true;
                    requestDate();
                }

                @Override
                public void onFailure(Exception e, String des) {
                    sobot_btn_submit.setAlpha(1f);
                    sobot_btn_submit.setEnabled(true);
                    sobot_btn_submit.setClickable(true);
                    ToastUtil.showCustomToast(getApplicationContext(), getString(R.string.sobot_leavemsg_error_tip));
                    LogUtils.e("uncaught", e);
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                }
            });
        }
    }

    /**
     * 添加附件到 pic_list 并刷新附件 RecyclerView（从 SobotReplyActivity 迁入）
     */
    public void addPicView(SobotFileModel item) {
        if (sobot_reply_msg_pic.getVisibility() == View.GONE) {
            sobot_reply_msg_pic.setVisibility(View.VISIBLE);
        }
        mPicList.add(item);
        mUploadAdapter.notifyDataSetChanged();
        sobot_reply_msg_pic.scrollToPosition(mUploadAdapter.getItemCount() - 1);
    }

    /**
     * 将 pic_list 中所有附件 URL 拼接成分号分隔的字符串（用于 replyTicketContent 接口入参）
     */
    public String getFileStr() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mPicList.size(); i++) {
            sb.append(mPicList.get(i).getFileUrl()).append(";");
        }
        return sb.toString();
    }

    /**
     * Toast 提示快捷方法（从 SobotReplyActivity 迁入）
     */
    public void showHint(String content) {
        ToastUtil.showToast(getApplicationContext(), content);
    }

    /**
     * 选图弹窗（SobotSelectPicDialog）按钮点击监听：拍照 / 相册 / 视频
     */
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSelectPicDialog != null) {
                mSelectPicDialog.dismiss();
            }
            if (v.getId() == R.id.btn_take_photo) {
                LogUtils.i("拍照");
                selectPicFromCamera();
            } else if (v.getId() == R.id.btn_pick_photo) {
                LogUtils.i("选择照片");
                selectPicFromLocal();
            } else if (v.getId() == R.id.btn_pick_vedio) {
                LogUtils.i("选择视频");
                selectVedioFromLocal();
            }
        }
    };

    /**
     * 附件上传回调：路径解析成功后调 fileUploadForPostMsg 上传到服务端，
     * 上传成功后将返回的 URL 封装为 SobotFileModel 加入 pic_list（从 SobotReplyActivity 迁入）
     */
    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotTicketDetailActivity.this, mCompanyId, mUid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
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
                    SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
                    showHint(TextUtils.isEmpty(des) ? getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(SobotTicketDetailActivity.this);
        }
    };

    @Override
    protected void onDestroy() {
        // 移除键盘监听，防止内存泄漏
        stopKeyboardAvoidance();
        super.onDestroy();
    }
}