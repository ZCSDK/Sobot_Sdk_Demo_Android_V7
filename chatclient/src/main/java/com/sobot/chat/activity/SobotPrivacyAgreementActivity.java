// SobotPrivacyAgreementActivity.java
package com.sobot.chat.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotChatBaseActivity;

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
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        showLeftMenu(true);
        initWebView();
        displayInNotch(mWebView);
        //修改图片高度为自适应宽度
       String mUrl = "<!DOCTYPE html>\n" +
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
                "    <body>" + policyContent + "  </body>\n" +
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
                //ignor
            }
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
        setTitle(policyName);
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
                // 注入深色模式 CSS
                if(isDarkMode) {
                    String javascript = "javascript:(function() { " +
                            "document.documentElement.style.setProperty('--background-color', '#121212'); " +
                            "document.documentElement.style.setProperty('--text-color', '#ffffff'); " +
                            "document.body.style.backgroundColor = '#121212'; " +
                            "document.body.style.color = '#ffffff'; " +
                            "})()";

                    mWebView.loadUrl(javascript);
                }

//                canGoBack = mWebView.canGoBack();
//                canGoForward = mWebView.canGoForward();
//                sobot_webview_goback.setEnabled(canGoBack);
//                sobot_webview_forward.setEnabled(canGoForward);
//                refreshBtn();
//                if (isUrlOrText && !mUrl.replace("http://", "").replace("https://", "").equals(view.getTitle()) && sobot_webview_title_display) {
//                    setTitle(view.getTitle());
//                }
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
