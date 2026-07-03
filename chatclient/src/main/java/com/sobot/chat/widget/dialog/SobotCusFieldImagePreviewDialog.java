package com.sobot.chat.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.subscaleview.ImageSource;
import com.sobot.chat.widget.subscaleview.SobotScaleImageView;
import com.sobot.pictureframe.SobotBitmapUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 附件图片预览弹窗，支持左右滑动切换。
 * 数据源两种构造：
 *   - {@link #SobotCusFieldImagePreviewDialog(Activity, List, int)}（自定义字段场景）：本地 cache 文件列表 + 下载到文件详情页
 *   - {@link #SobotCusFieldImagePreviewDialog(Activity, List, int, boolean)}（回复 / 详情场景）：URL 或本地路径混合列表，下载按钮隐藏
 */
public class SobotCusFieldImagePreviewDialog extends Dialog {

    private final Activity mActivity;
    /**
     * 仅图片类型的文件列表（自定义字段场景使用）
     */
    private final List<SobotCacheFile> mImageList;
    /**
     * 图片 URL / 本地路径列表（回复 / 详情场景使用）
     */
    private final List<String> mImageUrlList;
    private final int mStartIndex;

    private TextView tvIndicator;
    private TextView tvDownload;
    private ViewPager viewPager;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    public SobotCusFieldImagePreviewDialog(@NonNull Activity activity,
                                           @NonNull List<SobotCacheFile> imageList,
                                           int startIndex) {
        super(activity);
        this.mActivity = activity;
        this.mImageList = imageList;
        this.mImageUrlList = null;
        this.mStartIndex = startIndex;
    }

    /**
     * URL / 本地路径混合源构造（回复 / 详情场景）。
     * 第 4 个参数 useUrlList 仅作为重载签名区分；为 true 表示按 mImageUrlList 渲染。
     */
    public SobotCusFieldImagePreviewDialog(@NonNull Activity activity,
                                           @NonNull List<String> imageUrls,
                                           int startIndex,
                                           boolean useUrlList) {
        super(activity);
        this.mActivity = activity;
        this.mImageList = null;
        this.mImageUrlList = new ArrayList<>(imageUrls);
        this.mStartIndex = startIndex;
    }

    private int getDataCount() {
        return mImageUrlList != null ? mImageUrlList.size() : mImageList.size();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 全屏覆盖，包含状态栏和导航栏
//        Window window = getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
//                    WindowManager.LayoutParams.MATCH_PARENT);
//            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            window.getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            window.setDimAmount(0f);
//        }

        setContentView(R.layout.sobot_dialog_image_preview);
        setCanceledOnTouchOutside(false);

        tvIndicator = findViewById(R.id.sobot_preview_indicator);
        tvDownload = findViewById(R.id.sobot_preview_btn_download);
        viewPager = findViewById(R.id.sobot_preview_viewpager);

        viewPager.setAdapter(new ImagePagerAdapter());
        viewPager.setCurrentItem(mStartIndex, false);
        updateIndicator(mStartIndex);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
                updateDownloadButton(position);
            }
        });

        updateDownloadButton(mStartIndex);
    }

    private void updateIndicator(int position) {
        tvIndicator.setText((position + 1) + "/" + getDataCount());
    }

    private void updateDownloadButton(int position) {
        // URL 数据源（回复 / 详情场景）：隐藏"下载"按钮，避免误调用 SobotFileDetailActivity
        if (mImageUrlList != null) {
            if (tvDownload != null) {
                tvDownload.setVisibility(View.GONE);
            }
            return;
        }
        SobotCacheFile file = mImageList.get(position);
        tvDownload.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, SobotFileDetailActivity.class);
            file.setMsgId("" + System.currentTimeMillis());
            intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
        });
    }

    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return getDataCount();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            SobotScaleImageView imageView = new SobotScaleImageView(mActivity);
            imageView.setMinimumDpi(50);
            imageView.setMinimumTileDpi(240);
            imageView.setDoubleTapZoomStyle(SobotScaleImageView.ZOOM_FOCUS_FIXED);
            imageView.setDoubleTapZoomScale(2F);
            imageView.setPanLimit(SobotScaleImageView.PAN_LIMIT_INSIDE);
            imageView.setPanEnabled(true);
            imageView.setZoomEnabled(true);
            imageView.setQuickScaleEnabled(true);
            imageView.setOnClickListener(v -> dismiss());

            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            String filePath = mImageUrlList != null
                    ? mImageUrlList.get(position)
                    : mImageList.get(position).getFilePath();
            loadImageAsync(filePath, imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    /**
     * 在后台线程加载 bitmap，切回主线程后设置到 SobotScaleImageView。
     * 支持两种来源：
     *   - 本地路径：直接 decode 显示
     *   - http(s):// 网络 URL：MD5 命名缓存到 app cache，再 decode 显示
     */
    private void loadImageAsync(String pathOrUrl, SobotScaleImageView imageView) {
        if (TextUtils.isEmpty(pathOrUrl)) {
            return;
        }
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            loadRemoteImage(pathOrUrl, imageView);
        } else {
            loadLocalImage(pathOrUrl, imageView);
        }
    }

    private void loadLocalImage(String filePath, SobotScaleImageView imageView) {
        new Thread(() -> {
            try {
                if (!new File(filePath).exists()) return;
                Bitmap bitmap = SobotBitmapUtil.compress(filePath, mActivity.getApplicationContext(), true);
                if (bitmap == null) return;
                int degree = ImageUtils.readPictureDegree(filePath);
                if (degree > 0) {
                    bitmap = ImageUtils.rotateBitmap(bitmap, degree);
                }
                final Bitmap finalBitmap = bitmap;
                mMainHandler.post(() -> {
                    if (!mActivity.isDestroyed() && isShowing()) {
                        imageView.setImage(ImageSource.bitmap(finalBitmap));
                    }
                });
            } catch (Exception e) {
                LogUtils.e("SobotCusFieldImagePreviewDialog load local image error", e);
            }
        }).start();
    }

    /**
     * 下载远程图到 app cache 目录，复用 SobotPhotoActivity 的 MD5 + HttpUtils 方案。
     */
    private void loadRemoteImage(String url, SobotScaleImageView imageView) {
        try {
            String cleanUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
            File cacheDir = mActivity.getApplicationContext().getExternalFilesDir("images");
            if (cacheDir == null) {
                cacheDir = mActivity.getApplicationContext().getFilesDir();
            }
            final File savePath = new File(cacheDir, MD5Util.encode(cleanUrl));
            if (savePath.exists()) {
                loadLocalImage(savePath.getAbsolutePath(), imageView);
                return;
            }
            HttpUtils.getInstance().download(cleanUrl, savePath, null, new HttpUtils.FileCallBack() {
                @Override
                public void onResponse(File file) {
                    loadLocalImage(file.getAbsolutePath(), imageView);
                }

                @Override
                public void onError(Exception e, String msg, int responseCode) {
                    LogUtils.w("SobotCusFieldImagePreviewDialog download failed: " + msg, e);
                }

                @Override
                public void inProgress(int progress) {
                    // 预览态不展示进度
                }
            });
        } catch (Exception e) {
            LogUtils.e("SobotCusFieldImagePreviewDialog load remote image error", e);
        }
    }
}
