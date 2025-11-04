package com.sobot.chat.activity;

import static com.sobot.chat.SobotUIConfig.sobot_webview_title_display;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
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
                // 计算键盘高度，考虑工具栏
                int keypadHeight = screenHeight - r.bottom;
                //自定义导航栏高度
                View toolBar = getToolBar();
                if (toolBar != null && toolBar.getVisibility() == View.VISIBLE) {
                    keypadHeight -= toolBar.getHeight();
                }
                if (keypadHeight < 0) {
                    keypadHeight = 0;
                }
                LogUtils.i("键盘高度===========" + keypadHeight);
                boolean currentlyKeyboardShown = keypadHeight > screenHeight * 0.15;
                // 只有状态真正改变时才处理
                if (currentlyKeyboardShown && !isKeyboardShown) {
                    // 键盘刚显示
                    isKeyboardShown = true;
                    adjustWebViewForKeyboard(keypadHeight);
                } else if (!currentlyKeyboardShown && isKeyboardShown) {
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
            //加载url
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
                    "    <body>" + mUrl + "  </body>\n" +
                    "</html>";
            //显示文本内容
            mWebView.loadDataWithBaseURL("about:blank", mUrl.replace("<p>", "").replace("</p>", "<br/>").replace("<P>", "").replace("</P>", "<br/>"), "text/html", "utf-8", null);
        }
        LogUtils.i("webViewActivity---" + mUrl);
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
        LogUtils.i("API是大于11");
        android.content.ClipboardManager cmb = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(url);
        cmb.getText();
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
            //ignor
        }
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //检测到下载文件就打开系统浏览器
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri content = Uri.parse(url);
                intent.setData(content);
                startActivity(intent);
            }
        });
        // 注册键盘监听器
        mWebView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(16);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置可以使用localStorage
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
//        mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

        //关于webview的http和https的混合请求的，从Android5.0开始，WebView默认不支持同时加载Https和Http混合模式。
        // 在API>=21的版本上面默认是关闭的，在21以下就是默认开启的，直接导致了在高版本上面http请求不能正确跳转。
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        //Android 4.4 以下的系统中存在一共三个有远程代码执行漏洞的隐藏接口
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.removeJavascriptInterface("accessibility");
        mWebView.removeJavascriptInterface("accessibilityTraversal");

        // 应用可以有数据库
        mWebView.getSettings().setDatabaseEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //注释的地方是打开其它应用，比如qq
                /*if (url.startsWith("http") || url.startsWith("https")) {
                    return false;
                } else {
                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(in);
                    return true;
                }*/
                return false;
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
            mWebView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (acceptTypes != null && acceptTypes.length > 0) {
            if (acceptTypes.length == 1) {
                if (StringUtils.isEmpty(acceptTypes[0])) {
                    intent.setType("*/*"); // 默认所有类型
                } else {
                    intent.setType(acceptTypes[0]); // 单一类型，如 image/*, application/pdf
                }
            } else {
                intent.setType("*/*"); // 多种类型，使用通配符并配合 EXTRA_MIME_TYPES
                intent.putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes);
            }
        } else {
            intent.setType("*/*"); // 默认所有类型
        }
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALBUM || requestCode == ZCSobotConstant.REQUEST_CODE_OPENCAMERA) {
            if (uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                //一定要返回null,否则<input file> 就是没有反应
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;
                }
            }
            if (resultCode == RESULT_OK) {
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
                if (imageUri != null) {
                    //上传文件
                    if (uploadMessageAboveL != null) {
                        uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                        uploadMessageAboveL = null;
                    }
                }
            }
        }
    }
}