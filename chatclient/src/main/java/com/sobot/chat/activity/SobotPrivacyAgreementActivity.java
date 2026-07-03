// SobotPrivacyAgreementActivity.java
package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.WebViewSecurityUtil;

public class SobotPrivacyAgreementActivity extends SobotChatBaseActivity {

    private WebView mWebView;
    private TextView tvAgree;
    private String policyContent;
    private String policyName;
    boolean isDarkMode;// 判断是否为深色模式

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_activity_privacy_agreement;
    }

    @Override
    protected void initBundleData(Bundle savedInstanceState) {
        if (getIntent() != null) {
            policyContent = getIntent().getStringExtra("policyContent");
            policyName = getIntent().getStringExtra("policyName");
        }
    }

    @Override
    protected void initView() {
// 获取当前系统的UI模式配置
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        isDarkMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        mWebView = findViewById(R.id.sobot_mWebView);
        tvAgree = findViewById(R.id.sobot_btn_agree);

        if (policyName != null) {
            setTitle(policyName);
        }
        tvAgree.setText(R.string.sobot_agree);
        tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("policyAgree", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        showLeftMenu(true);
        initWebView();
        displayInNotch(mWebView);
        // Why: JS 已关闭，暗色模式改为内联 CSS（兼容 API 29 以下设备，API 29+ 由 setForceDark 兜底）
        String darkBodyCss = isDarkMode ? "body{background-color:#121212;color:#ffffff;}" : "";
        //修改图片高度为自适应宽度
        String mUrl = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">\n" +
                "        <title></title>\n" +
                "        <style>\n" +
                darkBodyCss +
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
                "    <body>" + WebViewSecurityUtil.sanitizeHtml(policyContent) + "  </body>\n" +
                "</html>";
        //显示文本内容
        mWebView.loadDataWithBaseURL("about:blank", mUrl.replace("<p>", "").replace("</p>", "<br/>").replace("<P>", "").replace("</P>", "<br/>"), "text/html", "utf-8", null);

    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotPrivacyAgreementActivity";
    }

    @SuppressLint("NewApi")
    private void initWebView() {
//        if(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mWebView.getSettings().setForceDark(WebSettings.FORCE_DARK_ON);
        }

//        }
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
                    LogUtils.i("PrivacyAgreement download refuse non-https");
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
        setTitle(policyName);
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(16);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        // 安全：本页仅渲染服务端隐私协议 HTML，无 JS 业务需求 → 关闭以阻断 sanitizer 绕过型 XSS
        mWebView.getSettings().setJavaScriptEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        // localStorage 同步关闭
        mWebView.getSettings().setDomStorageEnabled(false);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
//        mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

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

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.isEmpty(url)) {
                    return true;
                }
                String lower = url.toLowerCase();
                // CWE-319: https-only，与 WebViewActivity 一致，拒 http 明文导航
                if (lower.startsWith("https://")) {
                    return false;
                }
                if (lower.startsWith("http://")) {
                    LogUtils.i("PrivacyAgreement block http(non-tls) navigation");
                    return true;
                }
                if (lower.startsWith("tel:") || lower.startsWith("mailto:")) {
                    try {
                        Intent dispatchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        dispatchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dispatchIntent);
                    } catch (Exception e) {
                        LogUtils.e("PrivacyAgreement dispatch intent failed", e);
                    }
                    return true;
                }
                LogUtils.i("PrivacyAgreement block non-whitelist scheme");
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 暗色模式 CSS 已内联到 HTML wrapper，无需再注入 JS
            }
        });
    }

    @Override
    protected void initData() {
        // 初始化数据
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

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
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
}
