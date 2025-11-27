package com.sobot.chat.utils;

import java.util.HashMap;
import java.util.Map;

//特殊字符还原
public class HtmlUnescaper {
    // 定义 HTML 实体映射表
    private static final Map<String, String> HTML_ENTITIES = new HashMap<>();

    static {
        // 1. 基础字符（必须转义）
        HTML_ENTITIES.put("&lt;", "<");        // 小于号
        HTML_ENTITIES.put("&gt;", ">");        // 大于号
        HTML_ENTITIES.put("&amp;", "&");       // 和号
        HTML_ENTITIES.put("&quot;", "\"");     // 双引号
        HTML_ENTITIES.put("&apos;", "'");      // 单引号（XHTML 必需）

        // 2. 常用符号与标点
        HTML_ENTITIES.put("&nbsp;", " ");       // 不换行空格
        HTML_ENTITIES.put("&ndash;", "–");      // 短破折号
        HTML_ENTITIES.put("&mdash;", "—");      // 长破折号
        HTML_ENTITIES.put("&middot;", "·");     // 中点
        HTML_ENTITIES.put("&hellip;", "…");     // 省略号

        // 3. 货币符号
        HTML_ENTITIES.put("&copy;", "©");       // 版权符号
        HTML_ENTITIES.put("&reg;", "®");        // 注册商标
        HTML_ENTITIES.put("&trade;", "™");      // 商标符号
        HTML_ENTITIES.put("&cent;", "¢");       // 美分
        HTML_ENTITIES.put("&pound;", "£");      // 英镑
        HTML_ENTITIES.put("&euro;", "€");       // 欧元
        HTML_ENTITIES.put("&yen;", "¥");        // 日元

        // 4. 数学与逻辑符号
        HTML_ENTITIES.put("&times;", "×");      // 乘号
        HTML_ENTITIES.put("&divide;", "÷");     // 除号
        HTML_ENTITIES.put("&plusmn;", "±");     // 正负号
        HTML_ENTITIES.put("&ne;", "≠");         // 不等于
        HTML_ENTITIES.put("&le;", "≤");         // 小于等于
        HTML_ENTITIES.put("&ge;", "≥");         // 大于等于
        HTML_ENTITIES.put("&sum;", "∑");        // 求和
        HTML_ENTITIES.put("&prod;", "∏");       // 乘积

        // 5. 箭头与方向符号
        HTML_ENTITIES.put("&larr;", "←");       // 左箭头
        HTML_ENTITIES.put("&rarr;", "→");       // 右箭头
        HTML_ENTITIES.put("&uarr;", "↑");       // 上箭头
        HTML_ENTITIES.put("&darr;", "↓");       // 下箭头
        HTML_ENTITIES.put("&harr;", "↔");       // 双向箭头

        // 6. 引号与破折号（双引号、单引号）
        HTML_ENTITIES.put("&ldquo;", "“");      // 左双引号
        HTML_ENTITIES.put("&rdquo;", "”");      // 右双引号
        HTML_ENTITIES.put("&lsquo;", "‘");      // 左单引号
        HTML_ENTITIES.put("&rsquo;", "’");      // 右单引号

        // 7. 法语/西欧语言特殊字符
        HTML_ENTITIES.put("&agrave;", "à");     // à
        HTML_ENTITIES.put("&acirc;", "â");     // â
        HTML_ENTITIES.put("&auml;", "ä");     // ä
        HTML_ENTITIES.put("&atilde;", "ã");    // ã
        HTML_ENTITIES.put("&aring;", "å");    // å
        HTML_ENTITIES.put("&aelig;", "æ");     // æ
        HTML_ENTITIES.put("&egrave;", "è");    // è
        HTML_ENTITIES.put("&ecirc;", "ê");    // ê
        HTML_ENTITIES.put("&euml;", "ë");     // ë
        HTML_ENTITIES.put("&igrave;", "ì");    // ì
        HTML_ENTITIES.put("&icirc;", "î");    // î
        HTML_ENTITIES.put("&iuml;", "ï");     // ï
        HTML_ENTITIES.put("&ograve;", "ò");    // ò
        HTML_ENTITIES.put("&ocirc;", "ô");    // ô
        HTML_ENTITIES.put("&ouml;", "ö");     // ö
        HTML_ENTITIES.put("&otilde;", "õ");   // õ
        HTML_ENTITIES.put("&oslash;", "ø");   // Ø
        HTML_ENTITIES.put("&ugrave;", "ù");   // ù
        HTML_ENTITIES.put("&ucirc;", "û");    // û
        HTML_ENTITIES.put("&uuml;", "ü");    // ü
        HTML_ENTITIES.put("&yacute;", "ý");   // ý
        HTML_ENTITIES.put("&yuml;", "ÿ");    // ÿ
        HTML_ENTITIES.put("&szlig;", "ß");   // ß（德语）

        // 8. Unicode 数字实体
        HTML_ENTITIES.put("&#x20;", " ");      // 空格 (U+0020)
        HTML_ENTITIES.put("&#x0A;", "\n");     // 换行符 (U+000A)
        HTML_ENTITIES.put("&#x0D;", "\r");     // 回车符 (U+000D)
        HTML_ENTITIES.put("&#x09;", "\t");     // 制表符 (U+0009)
        HTML_ENTITIES.put("&#x27;", "'");      // 单引号 (U+0027)
        HTML_ENTITIES.put("&#39;", "'");       // 单引号（十进制）


        // 9. 兼容性补充（已存在但未分类）
        HTML_ENTITIES.put("&Agrave;", "À");
        HTML_ENTITIES.put("&Aacute;", "Á");
        HTML_ENTITIES.put("&Acirc;", "Â");
        HTML_ENTITIES.put("&Atilde;", "Ã");
        HTML_ENTITIES.put("&Auml;", "Ä");
        HTML_ENTITIES.put("&Aring;", "Å");
        HTML_ENTITIES.put("&AElig;", "Æ");
        HTML_ENTITIES.put("&Ccedil;", "Ç");
        HTML_ENTITIES.put("&Egrave;", "È");
        HTML_ENTITIES.put("&Eacute;", "É");
        HTML_ENTITIES.put("&Ecirc;", "Ê");
        HTML_ENTITIES.put("&Euml;", "Ë");
        HTML_ENTITIES.put("&Igrave;", "Ì");
        HTML_ENTITIES.put("&Iacute;", "Í");
        HTML_ENTITIES.put("&Icirc;", "Î");
        HTML_ENTITIES.put("&Iuml;", "Ï");
        HTML_ENTITIES.put("&Ntilde;", "Ñ");
        HTML_ENTITIES.put("&Ograve;", "Ò");
        HTML_ENTITIES.put("&Oacute;", "Ó");
        HTML_ENTITIES.put("&Ocirc;", "Ô");
        HTML_ENTITIES.put("&Otilde;", "Õ");
        HTML_ENTITIES.put("&Ouml;", "Ö");
    }


    /**
     * 将 HTML 实体转换为对应字符
     *
     * @param htmlString 包含 HTML 实体的字符串
     * @return 转换后的字符串
     */
    public static String unescapeHtml4(String htmlString) {
        if (htmlString == null || htmlString.isEmpty()) {
            return htmlString;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < htmlString.length()) {
            char c = htmlString.charAt(i);
            if (c == '&') {
                int endIndex = htmlString.indexOf(';', i);
                if (endIndex != -1) {
                    String entity = htmlString.substring(i, endIndex + 1);
                    String replacement = HTML_ENTITIES.get(entity);
                    if (replacement != null) {
                        result.append(replacement);
                        i = endIndex + 1;
                        continue;
                    }
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }
}

