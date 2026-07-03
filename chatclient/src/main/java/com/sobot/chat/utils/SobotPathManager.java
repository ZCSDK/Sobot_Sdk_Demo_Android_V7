package com.sobot.chat.utils;

import android.content.Context;
import android.os.Environment;

import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.application.MyApplication;

import java.io.File;
import java.security.MessageDigest;

/**
 * @author Created by jinxl on 2018/12/3.
 */
public class SobotPathManager {
    private Context mContext;

    private static String mRootPath;

    private static final String ROOT_DIR = "download";
    private static final String VIDEO_DIR = "video";
    private static final String VOICE_DIR = "voice";
    private static final String PIC_DIR = "pic";
    private static final String CACHE_DIR = "cache";

    private SobotPathManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        } else {
            mContext = MyApplication.getInstance().getLastActivity();
        }
    }

    private static SobotPathManager instance;

    public static SobotPathManager getInstance() {
        if (instance == null) {
            synchronized (SobotPathManager.class) {
                if (instance == null) {
                    instance = new SobotPathManager(SobotApp.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public String getRootDir() {
        if (mRootPath == null) {
            String packageName = mContext != null ? mContext.getPackageName() : "";
            // 改用应用专属外部存储目录（Android 10+ 兼容）
            File externalFilesDir = mContext.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                mRootPath = externalFilesDir.getPath() + File.separator + ROOT_DIR + File.separator + encode(packageName + "cache_sobot");
            } else {
                // 回退到内部存储
                mRootPath = mContext.getFilesDir().getPath() + File.separator + ROOT_DIR + File.separator + encode(packageName + "cache_sobot");
            }
        }
        LogUtils.d("SobotPathManager getRootDir() = " + mRootPath);
        return mRootPath;
    }

    //sdcard/download/xxxx/video
    public String getVideoDir() {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (externalFilesDir != null) {
            return externalFilesDir.getPath() + File.separator;
        }
        // 回退到内部存储
        return mContext.getFilesDir().getPath() + File.separator + "video" + File.separator;
    }

    //sdcard/download/xxxx/voice
    public String getVoiceDir() {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (externalFilesDir != null) {
            return externalFilesDir.getPath() + File.separator;
        }
        // 回退到内部存储
        return mContext.getFilesDir().getPath() + File.separator + "voice" + File.separator;
    }

    //sdcard/download/xxxx/pic
    public String getPicDir() {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir != null) {
            return externalFilesDir.getPath() + File.separator;
        }
        // 回退到内部存储
        return mContext.getFilesDir().getPath() + File.separator + "pictures" + File.separator;
    }

    //sdcard/download/xxxx/cache
    public String getCacheDir() {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (externalFilesDir != null) {
            return externalFilesDir.getPath() + File.separator + CACHE_DIR + File.separator;
        }
        // 回退到内部存储
        return mContext.getFilesDir().getPath() + File.separator + "download" + File.separator + CACHE_DIR + File.separator;
    }

    private String encode(String str) {
        StringBuilder sb = new StringBuilder();

        try {
            // Why: 用于生成包名目录哈希（非密码学场景），SHA-256 替换 MD5 满足合规要求，截取 16 字节保持目录名长度兼容
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] digest = instance.digest(str.getBytes());
            int limit = Math.min(digest.length, 16);
            for (int i = 0; i < limit; i++) {
                int num = digest[i] & 0xff;
                String hex = Integer.toHexString(num);
                if (hex.length() < 2) {
                    sb.append("0");
                }
                sb.append(hex);
            }

        } catch (Exception e) {
            LogUtils.e("SobotPathManager encode error", e);
        }

        return sb.toString();
    }
}
