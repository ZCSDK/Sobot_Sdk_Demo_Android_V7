package com.sobot.chat.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.base.SobotBaseHelpCenterActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.StDocModel;
import com.sobot.chat.api.model.StHelpDocModel;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.WebViewSecurityUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

/**
 * 帮助中心问题详情
 */
public class SobotProblemDetailActivity extends SobotBaseHelpCenterActivity implements View.OnClickListener {
    public static final String EXTRA_KEY_DOC = "extra_key_doc";

    private StDocModel mDoc;
    private WebView mWebView;
    private LinearLayout ll_bottom, ll_bottom_h, ll_bottom_v;
    private LinearLayout ll_sobot_layout_online_service, ll_sobot_layout_online_service_v;
    private LinearLayout ll_sobot_layout_online_tel, ll_sobot_layout_online_tel_v;
    private TextView tv_sobot_layout_online_tel, tv_sobot_layout_online_tel_v, tv_open_chat_v, tv_open_chat;
    private View view_split_online_tel;

    private TextView mProblemTitle;
    private String tel;
    private HelpConfigModel configModel;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_problem_detail;
    }

    public static Intent newIntent(Context context, Information information, StDocModel data, HelpConfigModel configModel) {
        Intent intent = new Intent(context, SobotProblemDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ZhiChiConstant.SOBOT_BUNDLE_INFO, information);
        intent.putExtra(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, bundle);
        intent.putExtra(EXTRA_KEY_DOC, data);
        intent.putExtra("configModel", configModel);
        return intent;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotProblemDetailActivity";
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        super.initBundleData(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mDoc = (StDocModel) intent.getSerializableExtra(EXTRA_KEY_DOC);
            configModel = (HelpConfigModel) intent.getSerializableExtra("configModel");
        }
    }

    // 键盘真正显示，避免多次走回调
    private boolean isKeyboardShown = false;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            try {
                Rect r = new Rect();
                mWebView.getWindowVisibleDisplayFrame(r);
                int screenHeight = mWebView.getRootView().getHeight();
                int keyboardHeight = screenHeight - r.bottom;
                if (keyboardHeight < 0) {
                    keyboardHeight = 0;
                }
                LogUtils.d("键盘高度===========" + keyboardHeight);
                boolean isKeyboardCurrentlyVisible = keyboardHeight > screenHeight * 0.15;
                // 只有状态真正改变时才处理
                if (isKeyboardCurrentlyVisible && !isKeyboardShown) {
                    // 键盘刚显示
                    isKeyboardShown = true;
                    adjustWebViewForKeyboard(keyboardHeight);
                } else if (!isKeyboardCurrentlyVisible && isKeyboardShown) {
                    // 键盘刚隐藏
                    isKeyboardShown = false;
                    resetWebViewLayout();
                }
            } catch (Exception e) {
            }
        }
    };

    //webview高度 - 键盘高度
    private void adjustWebViewForKeyboard(int keyboardHeight) {
        try {
            //LinearLayout.LayoutParams 需要自己判断具体的类型
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mWebView.getLayoutParams();
            params.height = mWebView.getHeight() - keyboardHeight;
            mWebView.setLayoutParams(params);
        } catch (Exception e) {
        }
    }

    //webview高度 还原
    private void resetWebViewLayout() {
        try {
            //LinearLayout.LayoutParams 需要自己判断具体的类型
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mWebView.getLayoutParams();
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            mWebView.setLayoutParams(params);
        } catch (Exception e) {
        }
    }

    @Override
    protected void initView() {
        changeAppLanguage();
        showLeftMenu(true);
        setTitle(R.string.sobot_problem_detail_title);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_bottom_h = findViewById(R.id.ll_bottom_h);
        ll_bottom_v = findViewById(R.id.ll_bottom_v);
        tv_open_chat_v = findViewById(R.id.tv_open_chat_v);
        tv_open_chat = findViewById(R.id.tv_open_chat);
        ll_sobot_layout_online_service = findViewById(R.id.ll_sobot_layout_online_service);
        ll_sobot_layout_online_service_v = findViewById(R.id.ll_sobot_layout_online_service_v);
        ll_sobot_layout_online_tel = findViewById(R.id.ll_sobot_layout_online_tel);
        ll_sobot_layout_online_tel_v = findViewById(R.id.ll_sobot_layout_online_tel_v);
        tv_sobot_layout_online_tel = findViewById(R.id.tv_sobot_layout_online_tel);
        tv_sobot_layout_online_tel_v = findViewById(R.id.tv_sobot_layout_online_tel_v);
        view_split_online_tel = findViewById(R.id.view_split_online_tel);
        mProblemTitle = findViewById(R.id.sobot_text_problem_title);
        mWebView = (WebView) findViewById(R.id.sobot_webView);
        ll_sobot_layout_online_service.setOnClickListener(this);
        ll_sobot_layout_online_tel.setOnClickListener(this);
        ll_sobot_layout_online_service_v.setOnClickListener(this);
        ll_sobot_layout_online_tel_v.setOnClickListener(this);
        initWebView();
        displayInNotch(mWebView);
        displayInNotch(ll_bottom);
        displayInNotch(mProblemTitle);
        configModel = (HelpConfigModel) getIntent().getSerializableExtra("configModel");
        if (configModel != null) {
            setToolBarDefBg();
            setBottomBtnUi(configModel);
        }
    }

    @Override
    protected void initData() {
        ZhiChiApi api = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        api.getHelpDocByDocId(SobotProblemDetailActivity.this, mInfo.getApp_key(), mDoc.getDocId(), new StringResultCallBack<StHelpDocModel>() {

            @Override
            public void onSuccess(StHelpDocModel data) {
                mProblemTitle.setText(data.getQuestionTitle());
                String answerDesc = data.getAnswerDesc();
                if (!TextUtils.isEmpty(answerDesc)) {
                    int zinyanColor = getResources().getColor(R.color.sobot_color_wenzi_black);
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("#");
                    stringBuffer.append(Integer.toHexString(Color.red(zinyanColor)));
                    stringBuffer.append(Integer.toHexString(Color.green(zinyanColor)));
                    stringBuffer.append(Integer.toHexString(Color.blue(zinyanColor)));
                    //修改图片高度为自适应宽度

                    // 检查内容是否包含阿拉伯语字符
                    boolean isArabicContent = containsArabicCharacters(answerDesc);

                    // 构建HTML内容
                    StringBuilder htmlBuilder = new StringBuilder();
                    htmlBuilder.append("<!DOCTYPE html>\n");
                    htmlBuilder.append("<html");
                    if (isArabicContent) {
                        htmlBuilder.append(" dir=\"rtl\"");
                    }
                    htmlBuilder.append(">\n");
                    htmlBuilder.append("    <head>\n");
                    htmlBuilder.append("        <meta charset=\"utf-8\">\n");
                    htmlBuilder.append("        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">\n");
                    htmlBuilder.append("        <title></title>\n");
                    htmlBuilder.append("        <style>\n body{color:").append(stringBuffer != null ? stringBuffer.toString() : "")
                            .append(";}\n");
                    htmlBuilder.append("            img{\n");
                    htmlBuilder.append("                width: auto;\n");
                    htmlBuilder.append("                height:auto;\n");
                    htmlBuilder.append("                max-height: 100%;\n");
                    htmlBuilder.append("                max-width: 100%;\n");
                    htmlBuilder.append("            }\n");
                    htmlBuilder.append("            video{\n");
                    htmlBuilder.append("                width: auto;\n");
                    htmlBuilder.append("                height:auto;\n");
                    htmlBuilder.append("                max-height: 100%;\n");
                    htmlBuilder.append("                max-width: 100%;\n");
                    htmlBuilder.append("            }\n");
                    if (isArabicContent) {
                        htmlBuilder.append("            body {\n");
                        htmlBuilder.append("                text-align: right;\n");
                        htmlBuilder.append("                direction: rtl;\n");
                        htmlBuilder.append("                word-wrap: break-word;\n");
                        htmlBuilder.append("                word-break: break-all;\n");
                        htmlBuilder.append("                overflow-wrap: break-word;\n");
                        htmlBuilder.append("            }\n");
                    } else {
                        htmlBuilder.append("            body {\n");
                        htmlBuilder.append("                word-wrap: break-word;\n");
                        htmlBuilder.append("                word-break: break-word;\n");
                        htmlBuilder.append("                overflow-wrap: break-word;\n");
                        htmlBuilder.append("            }\n");
                    }
                    htmlBuilder.append("        </style>\n");
                    htmlBuilder.append("    </head>\n");
                    htmlBuilder.append("    <body>");
                    htmlBuilder.append(WebViewSecurityUtil.sanitizeHtml(answerDesc).replace("<p>", "").replace("</p>", "<br/>").replace("<P>", "").replace("</P>", "<br/>"));
                    htmlBuilder.append("  </body>\n");
                    htmlBuilder.append("</html>");

                    //显示文本内容
                    mWebView.loadDataWithBaseURL("about:blank", htmlBuilder.toString(), "text/html", "utf-8", null);
                }
            }


            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
    }

    /**
     * 检查字符串是否包含阿拉伯语字符
     *
     * @param text 要检查的文本
     * @return 如果包含阿拉伯语字符返回true，否则返回false
     */
    private boolean containsArabicCharacters(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        // 阿拉伯语Unicode范围: \u0600-\u06FF, \u0750-\u077F, \u08A0-\u08FF, \uFB50-\uFDFF, \uFE70-\uFEFF
        return text.matches(".*[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF].*");
    }

    //设置导航条颜色
    private void setBottomBtnUi(HelpConfigModel configModel) {
        this.configModel = configModel;
        if (configModel != null) {
            if (mInfo != null && StringUtils.isNoEmpty(mInfo.getHelpCenterTelTitle()) && StringUtils.isNoEmpty(mInfo.getHelpCenterTel())) {
                tel = mInfo.getHelpCenterTel();
                tv_sobot_layout_online_tel.setText(mInfo.getHelpCenterTelTitle());
                ll_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                tv_sobot_layout_online_tel_v.setText(mInfo.getHelpCenterTelTitle());
                view_split_online_tel.setVisibility(View.VISIBLE);
                if (StringUtils.calculateTextLines(14, configModel.getHotlineName(), ScreenUtils.getScreenWidth(getSobotBaseActivity()) / 2 - ScreenUtils.dip2px(getSobotBaseActivity(), 16 + 4 + 20 + 14 + 8), getSobotBaseActivity()) < 2) {
                    ll_bottom_h.setVisibility(View.VISIBLE);
                    ll_bottom_v.setVisibility(View.GONE);
                } else {
                    ll_bottom_h.setVisibility(View.GONE);
                    ll_bottom_v.setVisibility(View.VISIBLE);
                }
            } else {
                if (!TextUtils.isEmpty(configModel.getHotlineName()) && !TextUtils.isEmpty(configModel.getHotlineTel())) {
                    tel = configModel.getHotlineTel();
                    tv_sobot_layout_online_tel.setText(configModel.getHotlineName());
                    tv_sobot_layout_online_tel_v.setText(configModel.getHotlineName());
                    ll_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                    view_split_online_tel.setVisibility(View.VISIBLE);
                    if (StringUtils.calculateTextLines(14, configModel.getHotlineName(), ScreenUtils.getScreenWidth(getSobotBaseActivity()) / 2 - ScreenUtils.dip2px(getSobotBaseActivity(), 16 + 4 + 20 + 14 + 8), getSobotBaseActivity()) < 2) {
                        ll_bottom_h.setVisibility(View.VISIBLE);
                        ll_bottom_v.setVisibility(View.GONE);
                    } else {
                        ll_bottom_h.setVisibility(View.GONE);
                        ll_bottom_v.setVisibility(View.VISIBLE);
                    }
                } else {
                    ll_sobot_layout_online_tel.setVisibility(View.GONE);
                    view_split_online_tel.setVisibility(View.GONE);
                }
            }
        }
    }


    private void initWebView() {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
            } catch (Exception e) {
                LogUtils.e("uncaught", e);
            }
        }
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // CWE-319: 仅允许 https，与 WebViewActivity 对齐，拒 http 明文派发
                if (TextUtils.isEmpty(url) || !url.toLowerCase().startsWith("https://")) {
                    LogUtils.i("ProblemDetail download refuse non-https");
                    return;
                }
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri content = Uri.parse(url);
                intent.setData(content);
                startActivity(intent);
            }
        });
        // 注册键盘监听器
        if (keyboardLayoutListener != null) {
            mWebView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        }
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(14);
        mWebView.getSettings().setTextZoom(100);
        // 安全：本页仅渲染服务端帮助文档 HTML，无 JS 业务需求 → 关闭以阻断 sanitizer 绕过型 XSS
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setBackgroundColor(0);

        // localStorage 同步关闭
        mWebView.getSettings().setDomStorageEnabled(false);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
        // mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

        //安全：禁止 HTTPS 页面加载 HTTP 资源
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        //Android 4.4 以下的系统中存在一共三个有远程代码执行漏洞的隐藏接口
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        // 安全：关闭 WebSQL
        mWebView.getSettings().setDatabaseEnabled(false);

        //把html中的内容放大webview等宽的一列中
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 图片自适应已由 HTML wrapper 的内联 CSS 兜底，无需再注入 JS
            }

            @Override
            // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (SobotOption.dispatchUrlClick(getSobotBaseActivity(), url)) {
                    return true;
                }
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                String lower = url.toLowerCase();
                // CWE-319: https-only，与 WebViewActivity 一致，拒 http 明文导航
                if (lower.startsWith("https://")) {
                    return false;
                }
                if (lower.startsWith("http://")) {
                    LogUtils.i("ProblemDetail block http(non-tls) navigation");
                    return true;
                }
                if (lower.startsWith("tel:") || lower.startsWith("mailto:")) {
                    try {
                        Intent dispatchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        dispatchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dispatchIntent);
                    } catch (Exception e) {
                        LogUtils.e("ProblemDetail dispatch intent failed", e);
                    }
                    return true;
                }
                LogUtils.i("ProblemDetail block non-whitelist scheme");
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
//                if (newProgress > 0 && newProgress < 100) {
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    mProgressBar.setProgress(newProgress);
//                } else if (newProgress == 100) {
//                    mProgressBar.setVisibility(View.GONE);
//                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                // 1. 检查是否包含 capture="camera"
                boolean isCaptureCamera = false;
                try {
                    Intent intent = fileChooserParams.createIntent();
                    if (intent != null && intent.hasExtra("capture")) {
                        String capture = intent.getStringExtra("capture");
                        if ("camera".equals(capture)) {
                            isCaptureCamera = true;
                        }
                    }
                } catch (Exception e) {
                    LogUtils.e("Error in onShowFileChooser", e);
                }

                // 2. 获取 accept 类型数组
                String[] acceptTypes = fileChooserParams.getAcceptTypes();
                if (isCaptureCamera || (acceptTypes != null && acceptTypes.length > 0 && "image/*".equals(acceptTypes[0]))) {
                    // 是拍照行为
                    openCapture(); // 打开相机
                } else {
                    // 是文件上传行为，转换 acceptTypes 到 Intent.setType()
                    chooseFile(acceptTypes); // 传入 acceptTypes 进行 setType 设置
                }
                return true;
            }

        });
    }


    private ValueCallback<Uri[]> uploadMessageAboveL;

    /**
     * 打开相机拍照
     */
    private void openCapture() {
        cameraFile = null;
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                if (isCameraCanUse()) {
                    cameraFile = openCamera(getSobotBaseActivity());
                }
            }

            @Override
            public void onPermissionErrorListener(Activity activity, String title) {
                super.onPermissionErrorListener(activity, title);
                // 权限拒绝，通知 WebView 取消上传
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
            }
        };
        if (!isHasPermission(3, 3)) {
            return;
        }
        if (isCameraCanUse()) {
            cameraFile = openCamera(getSobotBaseActivity());
        }
    }

    /**
     * 打开文件系统选取文件上传
     */
    private void chooseFile(String[] acceptTypes) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_ALBUM);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ZhiChiConstant.REQUEST_CODE_ALBUM || requestCode == ZCSobotConstant.REQUEST_CODE_OPENCAMERA) {
            if (uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                // 一定要返回null,否则<input file> 就是没有反应
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
                return;
            }

            Uri imageUri = null;
            if (requestCode == ZhiChiConstant.REQUEST_CODE_ALBUM) {
                if (data != null) {
                    imageUri = data.getData();
                }
            } else if (requestCode == ZCSobotConstant.REQUEST_CODE_OPENCAMERA) {
                if (cameraFile != null && cameraFile.exists()) {
                    imageUri = ChatUtils.getUri(getSobotBaseActivity(), cameraFile);
                }
            }

            // 添加空值检查
            if (imageUri != null) {
                // 上传文件
                uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
            } else {
                // 如果imageUri为null，也返回null
                uploadMessageAboveL.onReceiveValue(null);
            }
            uploadMessageAboveL = null;
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.sobot_problem_detail_title);
        tv_open_chat_v.setText(R.string.sobot_help_center_online_service);
        tv_open_chat.setText(R.string.sobot_help_center_online_service);
        if (mInfo != null && StringUtils.isNoEmpty(mInfo.getHelpCenterTelTitle()) && StringUtils.isNoEmpty(mInfo.getHelpCenterTel())) {
            tv_sobot_layout_online_tel.setText(mInfo.getHelpCenterTelTitle());
            tv_sobot_layout_online_tel_v.setText(mInfo.getHelpCenterTelTitle());
        }
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            if (keyboardLayoutListener != null) {
                mWebView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
                keyboardLayoutListener = null;
            }
            mWebView.removeAllViews();
            final ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mWebView);
            }
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if (v == ll_sobot_layout_online_service || v == ll_sobot_layout_online_service_v) {
            if (SobotOption.openChatListener != null) {
                boolean isIntercept = SobotOption.openChatListener.onOpenChatClick(getSobotBaseActivity(), mInfo);
                if (isIntercept) {
                    return;
                }
            }
            ZCSobotApi.openZCChat(getSobotBaseActivity(), mInfo);
        }
        if (v == ll_sobot_layout_online_tel || v == ll_sobot_layout_online_tel_v) {
            if (tel != null && !TextUtils.isEmpty(tel)) {
                if (SobotOption.functionClickListener != null) {
                    SobotOption.functionClickListener.onClickFunction(getSobotBaseActivity(), SobotFunctionType.ZC_PhoneCustomerService);
                }
                if (SobotOption.dispatchPhoneClick(getSobotBaseActivity(), "tel:" + tel)) {
                    return;
                }
                ChatUtils.callUp(tel, getSobotBaseActivity());
            }
        }
    }
}