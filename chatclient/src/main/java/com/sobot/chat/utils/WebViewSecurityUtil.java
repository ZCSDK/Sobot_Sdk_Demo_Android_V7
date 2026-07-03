package com.sobot.chat.utils;

import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;

/**
 * WebView 与富文本安全工具
 * 集中处理 HTML 清洗与 URL scheme 校验，避免多个 WebView 入口出现差异
 */
public final class WebViewSecurityUtil {

    // CWE-1333: 入口长度上限，防止贪婪量词回溯造成 ReDoS（主线程冻结）
    private static final int MAX_HTML_LENGTH = 32 * 1024;

    private WebViewSecurityUtil() {
    }

    /**
     * 清洗待渲染的 HTML 片段（用于 loadDataWithBaseURL 或 Html.fromHtml 输入）
     * 处理：长度截断 -> 反解码 HTML 实体 -> 剥脚本/样式/注释/危险标签/危险属性/危险协议
     * 注：正则黑名单非终极方案；调用方必须配合 WebView setJavaScriptEnabled(false) +
     * 本类提供的 {@link #sanitizeSpanned(android.text.Spanned)} 过滤 URLSpan 危险协议，
     * 或直接使用一站式入口 {@link #safeFromHtml(String)}，形成深度防御。
     */
    public static String sanitizeHtml(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        // 0. 长度上限：超出截断而不抛异常，避免 ReDoS（CWE-1333）
        String raw = input.length() > MAX_HTML_LENGTH ? input.substring(0, MAX_HTML_LENGTH) : input;
        // 1. 先反解码 HTML 实体，避免 &lt;script&gt; 绕过纯字符匹配
        String s = HtmlUnescaper.unescapeHtml4(raw);
        // 2. 去掉 script / style 包裹内容（允许未闭合到 EOF）
        s = s.replaceAll("(?is)<\\s*script\\b[^>]*>[\\s\\S]*?(?:<\\s*/\\s*script\\s*>|$)", "");
        s = s.replaceAll("(?is)<\\s*style\\b[^>]*>[\\s\\S]*?(?:<\\s*/\\s*style\\s*>|$)", "");
        // 3. 去掉 HTML 注释（含 <![CDATA[ 与 IE 条件注释）
        s = s.replaceAll("(?is)<!--.*?-->", "");
        s = s.replaceAll("(?is)<!\\[CDATA\\[.*?\\]\\]>", "");
        // 4. 去掉危险标签（含开闭与自闭）；增加 base / xmp / template / xml
        s = s.replaceAll("(?is)<\\s*(iframe|object|embed|link|meta|svg|math|form|base|xmp|template|xml|frame|frameset|applet|audio|video|source|track)\\b[^>]*>[\\s\\S]*?<\\s*/\\s*\\1\\s*>", "");
        s = s.replaceAll("(?is)<\\s*(iframe|object|embed|link|meta|svg|math|form|base|xmp|template|xml|frame|frameset|applet|audio|video|source|track)\\b[^>]*/?>", "");
        // 5. 去掉 on* 事件属性（含 / 分隔属性场景：<img/onerror=...>）
        s = s.replaceAll("(?is)[\\s/]on[a-z]+\\s*=\\s*\"[^\"]*\"", "");
        s = s.replaceAll("(?is)[\\s/]on[a-z]+\\s*=\\s*'[^']*'", "");
        s = s.replaceAll("(?is)[\\s/]on[a-z]+\\s*=\\s*[^\\s>]+", "");
        // 6. 去掉危险属性：style / formaction / srcset / poster / background / action / xlink:href
        s = s.replaceAll("(?is)[\\s/](style|formaction|srcset|poster|background|action|ping|usemap|xlink:href)\\s*=\\s*\"[^\"]*\"", "");
        s = s.replaceAll("(?is)[\\s/](style|formaction|srcset|poster|background|action|ping|usemap|xlink:href)\\s*=\\s*'[^']*'", "");
        s = s.replaceAll("(?is)[\\s/](style|formaction|srcset|poster|background|action|ping|usemap|xlink:href)\\s*=\\s*[^\\s>]+", "");
        // 7. 去掉危险协议：href/src 三种引号形态（双引号、单引号、无引号）
        s = s.replaceAll("(?is)href\\s*=\\s*\"\\s*(javascript|data|vbscript|file)\\s*:[^\"]*\"", "href=\"#\"");
        s = s.replaceAll("(?is)href\\s*=\\s*'\\s*(javascript|data|vbscript|file)\\s*:[^']*'", "href='#'");
        s = s.replaceAll("(?is)href\\s*=\\s*(javascript|data|vbscript|file)\\s*:[^\\s>]*", "href=\"#\"");
        s = s.replaceAll("(?is)src\\s*=\\s*\"\\s*(javascript|data|vbscript|file)\\s*:[^\"]*\"", "src=\"\"");
        s = s.replaceAll("(?is)src\\s*=\\s*'\\s*(javascript|data|vbscript|file)\\s*:[^']*'", "src=''");
        s = s.replaceAll("(?is)src\\s*=\\s*(javascript|data|vbscript|file)\\s*:[^\\s>]*", "src=\"\"");
        return s;
    }

    /**
     * 一站式安全富文本：sanitizeHtml → Html.fromHtml → sanitizeSpanned。
     * 所有渲染 TextView setText(Html.fromHtml(...)) 场景统一使用本方法，
     * 避免调用方漏调 sanitizeSpanned 导致深度防御链断裂。
     */
    public static Spanned safeFromHtml(String htmlContent) {
        if (TextUtils.isEmpty(htmlContent)) {
            return new SpannableString("");
        }
        String safe = sanitizeHtml(htmlContent);
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(safe, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(safe);
        }
        return sanitizeSpanned(spanned);
    }

    /**
     * 扫描 Html.fromHtml 产物中的 URLSpan，剥离危险协议（javascript:/data:/vbscript:/file:）。
     * 闭合"sanitizeHtml 正则黑名单 + setJavaScriptEnabled(false) + URLSpan 过滤"的深度防御链。
     * 适用于 TextView 渲染场景：即便 sanitizer 被绕过，URLSpan 携带危险协议在点击时也走不到 Intent。
     * CWE-79
     */
    public static Spanned sanitizeSpanned(Spanned spanned) {
        if (spanned == null) {
            return new SpannableString("");
        }
        Spannable sp;
        if (spanned instanceof Spannable) {
            sp = (Spannable) spanned;
        } else {
            sp = new SpannableString(spanned);
        }
        URLSpan[] urls = sp.getSpans(0, sp.length(), URLSpan.class);
        if (urls == null || urls.length == 0) {
            return sp;
        }
        for (URLSpan url : urls) {
            String href = url.getURL();
            if (isDangerousScheme(href)) {
                sp.removeSpan(url);
            }
        }
        return sp;
    }

    private static boolean isDangerousScheme(String href) {
        if (TextUtils.isEmpty(href)) {
            return false;
        }
        String s = href.trim().toLowerCase();
        return s.startsWith("javascript:")
                || s.startsWith("data:")
                || s.startsWith("vbscript:")
                || s.startsWith("file:");
    }

    /**
     * 判断 URL 是否为 http(s)
     */
    public static boolean isHttpOrHttps(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
