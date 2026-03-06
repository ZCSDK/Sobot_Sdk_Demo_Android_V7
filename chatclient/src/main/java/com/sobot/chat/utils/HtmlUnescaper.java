package com.sobot.chat.utils;

import java.util.HashMap;
import java.util.Map;

//特殊字符还原
public class HtmlUnescaper {
    // 定义 HTML 实体映射表
    private static final Map<String, String> HTML_ENTITIES = new HashMap<>();

    static {
        // 基础字符实体
        HTML_ENTITIES.put("&amp;", "&");       // 和号 避免出现&amp;lt;

        // 特殊处理：避免URL参数被误解析
        HTML_ENTITIES.put("&mid=", "&#38;mid=");     // 雷霆超链接url里把&mid当参数用了
        HTML_ENTITIES.put("&times=", "&#38;times=");
        HTML_ENTITIES.put("&para=", "&#38;para=");
        HTML_ENTITIES.put("&image=", "&#38;image=");
        HTML_ENTITIES.put("&and=", "&#38;and=");
        HTML_ENTITIES.put("&beta=", "&#38;beta=");
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

        // 预分配容量以提高性能
        StringBuilder result = new StringBuilder(htmlString.length());
        int i = 0;

        while (i < htmlString.length()) {
            char c = htmlString.charAt(i);
            if (c == '&') {
                // 查找可能的实体结束位置
                int endIndex = htmlString.indexOf(';', i);
                if (endIndex != -1 && (endIndex - i) <= 10) { // 限制实体长度防止异常
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
