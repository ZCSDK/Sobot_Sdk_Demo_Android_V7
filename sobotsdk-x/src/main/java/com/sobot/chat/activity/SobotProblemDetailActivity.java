package com.sobot.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.base.SobotBaseHelpCenterActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.StDocModel;
import com.sobot.chat.api.model.StHelpDocModel;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.statusbar.StatusBarUtil;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.utils.SobotStringUtils;

/**
 * 帮助中心问题详情
 */
public class SobotProblemDetailActivity extends SobotBaseHelpCenterActivity implements View.OnClickListener {
    public static final String EXTRA_KEY_DOC = "extra_key_doc";

    private StDocModel mDoc;
    private WebView mWebView;
    private LinearLayout ll_bottom, ll_bottom_h, ll_bottom_v;
    private TextView tv_sobot_layout_online_service, tv_sobot_layout_online_service_v;
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
        showLeftMenu(  true);
        setTitle(R.string.sobot_problem_detail_title);
        ll_bottom = findViewById(R.id.ll_bottom);
        ll_bottom_h = findViewById(R.id.ll_bottom_h);
        ll_bottom_v = findViewById(R.id.ll_bottom_v);
        tv_sobot_layout_online_service = findViewById(R.id.tv_sobot_layout_online_service);
        tv_sobot_layout_online_service_v = findViewById(R.id.tv_sobot_layout_online_service_v);
        tv_sobot_layout_online_tel = findViewById(R.id.tv_sobot_layout_online_tel);
        tv_sobot_layout_online_tel_v = findViewById(R.id.tv_sobot_layout_online_tel_v);
        view_split_online_tel = findViewById(R.id.view_split_online_tel);
        mProblemTitle = findViewById(R.id.sobot_text_problem_title);
        mWebView = (WebView) findViewById(R.id.sobot_webView);
        tv_sobot_layout_online_service.setText(R.string.sobot_help_center_online_service);
        tv_sobot_layout_online_service.setOnClickListener(this);
        tv_sobot_layout_online_tel.setOnClickListener(this);
        tv_sobot_layout_online_service_v.setOnClickListener(this);
        tv_sobot_layout_online_tel_v.setOnClickListener(this);
        initWebView();
        displayInNotch(mWebView);
        displayInNotch(ll_bottom);
        displayInNotch(mProblemTitle);
        configModel = (HelpConfigModel) getIntent().getSerializableExtra("configModel");
        if (configModel != null) {
            setToobar(configModel);
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
    private void setToobar(HelpConfigModel configModel) {
        this.configModel = configModel;
        if (configModel != null) {
            SharedPreferencesUtil.saveObject(getSobotBaseActivity(), "SobotHelpConfigModel", configModel);
            if (mInfo != null && SobotStringUtils.isNoEmpty(mInfo.getHelpCenterTelTitle()) && SobotStringUtils.isNoEmpty(mInfo.getHelpCenterTel())) {
                tel = mInfo.getHelpCenterTel();
                tv_sobot_layout_online_tel.setText(mInfo.getHelpCenterTelTitle());
                tv_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                tv_sobot_layout_online_tel_v.setText(mInfo.getHelpCenterTelTitle());
                view_split_online_tel.setVisibility(View.VISIBLE);
                tv_sobot_layout_online_tel.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int lineCount = tv_sobot_layout_online_tel.getLineCount();
                        if (lineCount > 1) {
                            ll_bottom_h.setVisibility(View.GONE);
                            ll_bottom_v.setVisibility(View.VISIBLE);
                        } else {
                            ll_bottom_h.setVisibility(View.VISIBLE);
                            ll_bottom_v.setVisibility(View.GONE);
                        }
                    }
                }, 100);
            } else {
                if (!TextUtils.isEmpty(configModel.getHotlineName()) && !TextUtils.isEmpty(configModel.getHotlineTel())) {
                    tel = configModel.getHotlineTel();
                    tv_sobot_layout_online_tel.setText(configModel.getHotlineName());
                    tv_sobot_layout_online_tel_v.setText(configModel.getHotlineName());
                    tv_sobot_layout_online_tel.setVisibility(View.VISIBLE);
                    view_split_online_tel.setVisibility(View.VISIBLE);
                    tv_sobot_layout_online_tel.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int lineCount = tv_sobot_layout_online_tel.getLineCount();
                            if (lineCount > 1) {
                                ll_bottom_h.setVisibility(View.GONE);
                                ll_bottom_v.setVisibility(View.VISIBLE);
                            } else {
                                ll_bottom_h.setVisibility(View.VISIBLE);
                                ll_bottom_v.setVisibility(View.GONE);
                            }
                        }
                    }, 100);
                } else {
                    tv_sobot_layout_online_tel.setVisibility(View.GONE);
                    view_split_online_tel.setVisibility(View.GONE);
                }
            }
            //服务端返回的导航条背景颜色
            if (!TextUtils.isEmpty(configModel.getTopBarColor())) {
                String topBarColor[] = configModel.getTopBarColor().split(",");
                if (topBarColor.length > 1) {
                    if (getResources().getColor(R.color.sobot_gradient_start) != Color.parseColor(topBarColor[0]) || getResources().getColor(R.color.sobot_gradient_end) != Color.parseColor(topBarColor[1])) {
                        int[] colors = new int[topBarColor.length];
                        for (int i = 0; i < topBarColor.length; i++) {
                            colors[i] = Color.parseColor(topBarColor[i]);
                        }
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColors(colors); //添加颜色组
                        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                        getToolBar().setBackground(gradientDrawable);
                        GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
                        } else {
                            StatusBarUtil.setColor(getSobotBaseActivity(), aDrawable);
                        }
                    }
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
                chooseAlbumPic();
                return true;
            }

        });
    }

    private static final int REQUEST_CODE_ALBUM = 0x0111;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    /**
     * 选择相册照片
     */
    private void chooseAlbumPic() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("video/*;image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), REQUEST_CODE_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ALBUM) {
            if (uploadMessage == null && uploadMessageAboveL == null) {
                return;
            }
            if (resultCode != RESULT_OK) {
                //一定要返回null,否则<input file> 就是没有反应
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(null);
                    uploadMessageAboveL = null;

                }
            }

            if (resultCode == RESULT_OK) {
                Uri imageUri = null;
                switch (requestCode) {
                    case REQUEST_CODE_ALBUM:

                        if (data != null) {
                            imageUri = data.getData();
                        }
                        break;
                }

                //上传文件
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(imageUri);
                    uploadMessage = null;
                }
                if (uploadMessageAboveL != null) {
                    uploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                    uploadMessageAboveL = null;
                }
            }
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

        if (v == tv_sobot_layout_online_service || v == tv_sobot_layout_online_service_v) {
            if (SobotOption.openChatListener != null) {
                boolean isIntercept = SobotOption.openChatListener.onOpenChatClick(getSobotBaseActivity(), mInfo);
                if (isIntercept) {
                    return;
                }
            }
            ZCSobotApi.openZCChat(getApplicationContext(), mInfo);
        }
        if (v == tv_sobot_layout_online_tel || v == tv_sobot_layout_online_tel_v) {
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