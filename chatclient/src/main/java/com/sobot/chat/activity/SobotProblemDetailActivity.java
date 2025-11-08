package com.sobot.chat.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
    private TextView tv_sobot_layout_online_tel, tv_sobot_layout_online_tel_v;
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

    @Override
    protected void initView() {
        changeAppLanguage();
        showLeftMenu(true);
        setTitle(R.string.sobot_problem_detail_title);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_bottom_h = findViewById(R.id.ll_bottom_h);
        ll_bottom_v = findViewById(R.id.ll_bottom_v);
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
                    answerDesc = "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "    <head>\n" +
                            "        <meta charset=\"utf-8\">\n" +
                            "        <title></title>\n" +
                            "        <style>\n body{color:" + (stringBuffer != null ? stringBuffer.toString() : "") +
                            ";}\n" +
                            "            img{\n" +
                            "                width: auto;\n" +
                            "                height:auto;\n" +
                            "                max-height: 100%;\n" +
                            "                max-width: 100%;\n" +
                            "            }" +
                            "            video{\n" +
                            "                width: auto;\n" +
                            "                height:auto;\n" +
                            "                max-height: 100%;\n" +
                            "                max-width: 100%;\n" +
                            "            }" +
                            "        </style>\n" +
                            "    </head>\n" +
                            "    <body>" + answerDesc + "  </body>\n" +
                            "</html>";
                    //显示文本内容
                    String html = answerDesc.replace("<p>", "").replace("</p>", "<br/>").replace("<P>", "").replace("</P>", "<br/>");
                    mWebView.loadDataWithBaseURL("about:blank", html, "text/html", "utf-8", null);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                ToastUtil.showToast(getApplicationContext(), des);
            }
        });
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
        mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        mWebView.getSettings().setDefaultFontSize(14);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setBackgroundColor(0);

        // 设置可以使用localStorage
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBlockNetworkImage(false);
        mWebView.getSettings().setSavePassword(false);
        // mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString() + " sobot");

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

        //把html中的内容放大webview等宽的一列中
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                imgReset();
            }

            @Override
            // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (SobotOption.hyperlinkListener != null) {
                    SobotOption.hyperlinkListener.onUrlClick(url);
                    return true;
                }
                if (SobotOption.newHyperlinkListener != null) {
                    //如果返回true,拦截;false 不拦截
                    boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotBaseActivity(), url);
                    if (isIntercept) {
                        return true;
                    }
                }
                return false;
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

    private static final int REQUEST_CODE_ALBUM = 0x0111;
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

    /**
     * 对图片进行重置大小，宽度就是手机屏幕宽度，高度根据宽度比便自动缩放
     **/
    private void imgReset() {
        mWebView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName('img'); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "var img = objs[i];   " +
                "    img.style.maxWidth = '100%'; img.style.height = 'auto';  " +
                "}" +
                "})()");
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
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
                if (SobotOption.newHyperlinkListener != null) {
                    boolean isIntercept = SobotOption.newHyperlinkListener.onPhoneClick(getSobotBaseActivity(), "tel:" + tel);
                    if (isIntercept) {
                        return;
                    }
                }
                ChatUtils.callUp(tel, getSobotBaseActivity());
            }
        }
    }
}