package com.sobot.chat.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MD5Util {

    /**
     * 计算字符串摘要，结果为 32 位小写 hex
     * 历史方法名延续 MD5Util，内部已升级为 SHA-256 后截取前 16 字节
     * Why: 仅用于临时文件名生成（非密码学场景），SHA-256 替代 MD5 满足合规
     */
    public static String encode(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] digest = instance.digest(str.getBytes(StandardCharsets.UTF_8));
            int limit = Math.min(digest.length, 16); // 截取 128 位以保持文件名长度兼容
            for (int i = 0; i < limit; i++) {
                int num = digest[i] & 0xff;
                String hex = Integer.toHexString(num);
                if (hex.length() < 2) {
                    sb.append("0");
                }
                sb.append(hex);
            }
        } catch (Exception e) {
            LogUtils.e("MD5Util encode error", e);
        }
        return sb.toString();
    }
}
