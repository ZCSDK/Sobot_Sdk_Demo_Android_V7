package com.sobot.chat.activity;

import static com.sobot.chat.SobotUIConfig.sobot_webview_title_display;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.WebViewSecurityUtil;
import com.sobot.chat.widget.toast.ToastUtil;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends SobotChatBaseActivity implements View.OnClickListener {

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private LinearLayout sobot_rl_net_error;

    private TextView btnReconnect;
    private ImageView ivIconNonet;

    private String mUrl = "";
    private LinearLayout sobot_webview_toolsbar;
    private ImageView sobot_webview_goback;
    private ImageView sobot_webview_forward;
    private ImageView sobot_webview_reload;
    private ImageView sobot_webview_copy;

    //根据冲入的url判断是否url   true:是；false:不是
    private boolean isUrlOrText = true;

    //千人千面UI
    private boolean isChangeThemeColor = false;
    private int themeColor = 0;
    private boolean canGoForward = false;
    private boolean canGoBack = false;

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_webview;
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra("url"))) {
                mUrl = getIntent().getStringExtra("url");
                isUrlOrText = StringUtils.isURL(mUrl);
            }
        } else {
            mUrl = savedInstanceState.getString("url");
            isUrlOrText = StringUtils.isURL(mUrl);
        }
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "WebViewActivity";
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
                LogUtils.i("键盘高度===========" + keyboardHeight);
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
                LogUtils.e("WebViewActivity keyboard layout adjust failed", e);
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
            LogUtils.e("adjustWebViewForKeyboard failed", e);
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
            LogUtils.e("resetWebViewLayout failed", e);
        }
    }

    @Override
    protected void initView() {
        setTitle("");
        showLeftMenu(true);
        isChangeThemeColor = ThemeUtils.isChangedThemeColor(this);
        mWebView = findViewById(R.id.sobot_mWebView);
        mProgressBar = findViewById(R.id.sobot_loadProgress);
        sobot_rl_net_error = findViewById(R.id.sobot_rl_net_error);
        sobot_webview_toolsbar = findViewById(R.id.sobot_webview_toolsbar);
        btnReconnect = findViewById(R.id.sobot_btn_reconnect);
        btnReconnect.setOnClickListener(this);
        ivIconNonet = findViewById(R.id.sobot_icon_nonet);
        sobot_webview_goback = findViewById(R.id.sobot_webview_goback);
        sobot_webview_forward = findViewById(R.id.sobot_webview_forward);
        sobot_webview_reload = findViewById(R.id.sobot_webview_reload);
        sobot_webview_copy = findViewById(R.id.sobot_webview_copy);
        sobot_webview_goback.setOnClickListener(this);
        sobot_webview_forward.setOnClickListener(this);
        sobot_webview_reload.setOnClickListener(this);
        sobot_webview_copy.setOnClickListener(this);
        try {
            ivIconNonet.setImageDrawable(ThemeUtils.applyColorToDrawable(ivIconNonet.getDrawable(), ThemeUtils.getThemeColor(getSobotBaseActivity())));
            btnReconnect.setTextColor(ThemeUtils.getThemeTextAndIconColor(getSobotBaseActivity()));
            Drawable bg = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_button_style, null);
            if (bg != null) {
                Drawable btnReconnectBg = ThemeUtils.applyColorToDrawable(bg, ThemeUtils.getThemeColor(getSobotBaseActivity()));
                btnReconnect.setBackground(btnReconnectBg);
            }
        } catch (Exception e) {
            LogUtils.e("WebViewActivity initView theme apply failed", e);
        }

        if (isChangeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
            Drawable reload = getResources().getDrawable(R.drawable.sobot_webview_btn_reload_selector);
            Drawable copy = getResources().getDrawable(R.drawable.sobot_webview_btn_copy_selector);
            sobot_webview_reload.setImageDrawable(ThemeUtils.applyColorToDrawable(reload, themeColor));
            sobot_webview_copy.setImageDrawable(ThemeUtils.applyColorToDrawable(copy, themeColor));
        }
        sobot_webview_goback.setEnabled(false);
        sobot_webview_forward.setEnabled(false);
        displayInNotch(mWebView);

        resetViewDisplay();
        initWebView();
        loadUrl();
    }

    private void loadUrl() {
        if (isUrlOrText) {
            //安全：仅允许 https，拒绝 http 明文，避免 MITM 注入恶意 JS（CWE-319）
            if (TextUtils.isEmpty(mUrl) || !mUrl.startsWith("https://")) {
                LogUtils.i("WebViewActivity refuse non-https url");
                finish();
                return;
            }
            mWebView.loadUrl(mUrl);
            sobot_webview_copy.setVisibility(View.VISIBLE);
        } else {
            //修改图片高度为自适应宽度
            mUrl = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "    <head>\n" +
                    "        <meta charset=\"utf-8\">\n" +
                    "        <title></title>\n" +
                    "        <style>\n" +
                    "            img{\n" +
                    "                width: auto;\n" +
                    "                height:auto;\n" +
                    "                max-height: 100%;\n" +
                    "                max-width: 100%;\n" +
                    "            }\n" +
                    "            video{\n" +
                    "                width: auto;\n" +
                    "                height:auto;\n" +
                    "                max-height: 100%;\n" +
                    "                max-width: 100%;\n" +
                    "            }" +
                    "        </style>\n" +
                    "    </head>\n" +
                    "    <body>" + WebViewSecurityUtil.sanitizeHtml(mUrl) + "  </body>\n" +
                    "</html>";
            //显示文本内容
            mWebView.loadDataWithBaseURL("about:blank", mUrl.replace("<p>", "").replace("</p>", "<br/>").replace("<P>", "").replace("</P>", "<br/>"), "text/html", "utf-8", null);
        }
        LogUtils.i("webViewActivity loadUrl, isUrl=" + isUrlOrText + ", len=" + (mUrl == null ? 0 : mUrl.length()));
    }


    @Override
    protected void initData() {

    }

    @Override
    protected void onLeftMenuClick(View view) {
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view == btnReconnect) {
            if (!TextUtils.isEmpty(mUrl)) {
                resetViewDisplay();
            }
        } else if (view == sobot_webview_forward) {
            mWebView.goForward();
        } else if (view == sobot_webview_goback) {
            mWebView.goBack();
        } else if (view == sobot_webview_reload) {
            mWebView.reload();
        } else if (view == sobot_webview_copy) {
            copyUrl(mUrl);
        }
    }

    private void copyUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        ClipboardManager cmb = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cmb == null) {
            return;
        }
        ClipData clipData = ClipData.newPlainText("url", url);
        // 安全：API 33+ 标记剪贴板内容为敏感，系统通知/输入法/无障碍服务不显示明文预览（CWE-200）。
        // 顺便替换已弃用的 ClipboardManager.setText(CharSequence)。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PersistableBundle extras = new PersistableBundle();
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true);
            clipData.getDescription().setExtras(extras);
        }
        cmb.setPrimaryClip(clipData);
        ToastUtil.showToast(getApplicationContext(), CommonUtils.getResString(WebViewActivity.this, "sobot_ctrl_v_success"));
    }

    /**
     * 根据有无网络显示不同的View
     */
    private void resetViewDisplay() {
        if (CommonUtils.isNetWorkConnected(getApplicationContext())) {
            mWebView.setVisibility(View.VISIBLE);
            loadUrl();
            sobot_webview_toolsbar.setVisibility(View.VISIBLE);
            sobot_rl_net_error.setVisibility(View.GONE);
        } else {
            mWebView.setVisibility(View.GONE);
            sobot_webview_toolsbar.setVisibility(View.GONE);
            sobot_rl_net_error.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NewApi")
    private void initWebView() {
        try {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        } catch (Exception e) {
            LogUtils.e("uncaught", e);
        }
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (!WebViewSecurityUtil.isHttpOrHttps(url)) {
                    LogUtils.i("WebView download refuse non-http(s)");
                    return;
                }
                // 安全：隐式 ACTION_VIEW 无法保证有 Activity 处理（如平板裁剪版无浏览器），
                // 缺 try-catch 会抛 ActivityNotFoundException 杀宿主进程。
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    LogUtils.e("WebView download dispatch failed", e);
                }
            }
        });
        // 注册键盘监听器
        if (keyboardLayoutListener != null) {
            mWebView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        }
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(16);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        // 安全：仅外部 http(s) URL 才启用 JS；text 分支仅渲染服务端 HTML，关闭 JS 阻断 sanitizer 绕过型 XSS
        mWebView.getSettings().setJavaScriptEnabled(isUrlOrText);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // localStorage 跟随 JS 开关，text 分支不需要
        mWebView.getSettings().setDomStorageEnabled(isUrlOrText);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
//        mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

        //安全：禁止 HTTPS 页面加载 HTTP 资源，避免中间人攻击注入脚本
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        //Android 4.4 以下的系统中存在一共三个有远程代码执行漏洞的隐藏接口
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        // 安全：关闭 WebSQL，防止恶意页面通过数据库存大量数据
        mWebView.getSettings().setDatabaseEnabled(false);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                String lower = url.toLowerCase();
                //安全：仅放行 https，http 明文一律拦截（CWE-319）
                if (lower.startsWith("https://")) {
                    return false;
                }
                if (lower.startsWith("http://")) {
                    LogUtils.i("WebView block http(non-tls) navigation");
                    return true;
                }
                //tel / mailto 交给系统 ACTION_VIEW 处理
                if (lower.startsWith("tel:") || lower.startsWith("mailto:")) {
                    try {
                        Intent dispatchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        dispatchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dispatchIntent);
                    } catch (Exception e) {
                        LogUtils.e("WebView dispatch intent failed", e);
                    }
                    return true;
                }
                //其余 scheme（file/intent/javascript/data/...）一律拦截
                LogUtils.i("WebView block non-whitelist scheme");
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                canGoBack = mWebView.canGoBack();
                canGoForward = mWebView.canGoForward();
                sobot_webview_goback.setEnabled(canGoBack);
                sobot_webview_forward.setEnabled(canGoForward);
                refreshBtn();
                if (isUrlOrText && !mUrl.replace("http://", "").replace("https://", "").equals(view.getTitle()) && sobot_webview_title_display) {
                    setTitle(view.getTitle());
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                LogUtils.i("网页--title---：" + title);
                if (isUrlOrText && !mUrl.replace("http://", "").replace("https://", "").equals(title) && sobot_webview_title_display) {
                    setTitle(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 0 && newProgress < 100) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                } else if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
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

    @Override
    protected void onResume() {
        super.onResume();
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

    private void refreshBtn() {
        LogUtils.d("===========canGoBack=" + canGoBack + "=========canGoForward=" + canGoForward);
        if (isChangeThemeColor) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Drawable forward = getResources().getDrawable(R.drawable.sobot_webview_toolsbar_forward_disable);
                    Drawable back = getResources().getDrawable(R.drawable.sobot_webview_toolsbar_back_disable);
                    if (canGoBack) {
                        sobot_webview_goback.setImageDrawable(ThemeUtils.applyColorToDrawable(back, themeColor));
                    } else {
                        sobot_webview_goback.setImageDrawable(ThemeUtils.applyColorToDrawable(back, "#c2c4c4"));
                    }
                    if (canGoForward) {
                        sobot_webview_forward.setImageDrawable(ThemeUtils.applyColorToDrawable(forward, themeColor));
                    } else {
                        sobot_webview_forward.setImageDrawable(ThemeUtils.applyColorToDrawable(forward, "#c2c4c4"));
                    }
                }
            });

        }
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
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        //被摧毁前缓存一些数据
        outState.putString("url", mUrl);
        super.onSaveInstanceState(outState);
    }

    private static final int REQUEST_CODE_ALBUM = 0x0111;

    private ValueCallback<Uri[]> uploadMessageAboveL;

    public Context getContext() {
        return WebViewActivity.this;
    }

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
            openCapture();
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
            startActivityForResult(intent, REQUEST_CODE_ALBUM);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALBUM || requestCode == ZCSobotConstant.REQUEST_CODE_OPENCAMERA) {
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
            if (requestCode == REQUEST_CODE_ALBUM) {
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
}