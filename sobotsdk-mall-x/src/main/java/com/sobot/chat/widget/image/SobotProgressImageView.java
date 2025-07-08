package com.sobot.chat.widget.image;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.imageloader.SobotImageLoader;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.pictureframe.SobotBitmapUtil;
import com.sobot.utils.SobotStringUtils;

/**
 * 带有预加载效果的图片控件
 */
public class SobotProgressImageView extends SobotRCRelativeLayout {

    private SobotRCRelativeLayout rcRoot;//根布局
    private ProgressBar progressBar;
    private ImageView mImageView;
    //加载失败图片控件
    private ImageView imageview_error;
    //图片最大高
    private int mMaxHeight;
    //图片最大宽
    private int mMaxWidth;
    //图片最小高
    private int mMinHeight;
    //图片最小宽
    private int mMinWidth;
    //图片宽高
    private int mImageHeight, mImageWidth;
    //布局变化时是否开启系统动画，默认不开启
    private boolean layoutChangeAnimate = false;
    //是否显示加载中控件 默认 不显示
    private boolean isShowProgressbar = false;
    private int bgColot;//预加载背景色


    public SobotProgressImageView(Context context) {
        super(context);
    }

    public SobotProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //加载视图的布局
        LayoutInflater.from(context).inflate(R.layout.sobot_layout_progress_imageview, this, true);

        //加载自定义的属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.sobot_progress_imageview);
        mMaxHeight = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_max_height, 0);
        mMaxWidth = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_max_width, 0);
        mMinHeight = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_min_height, 0);
        mMinWidth = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_min_width, 0);
        mImageHeight = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_height, 0);
        mImageWidth = ta.getDimensionPixelSize(R.styleable.sobot_progress_imageview_image_width, 0);
        layoutChangeAnimate = ta.getBoolean(R.styleable.sobot_progress_imageview_image_layout_change_animate, false);
        isShowProgressbar = ta.getBoolean(R.styleable.sobot_progress_imageview_image_is_show_progressbar, false);
        bgColot = ta.getColor(R.styleable.sobot_progress_imageview_image_bg, getResources().getColor(R.color.sobot_color_progress_image_bg));
        //回收资源，这一句必须调用
        ta.recycle();
    }

    /**
     * 此方法会在所有的控件都从xml文件中加载完成后调用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取子控件
        rcRoot = findViewById(R.id.rc_root);
        rcRoot.setBackgroundColor(bgColot);
        if (layoutChangeAnimate) {
            LayoutTransition transition = new LayoutTransition();
            rcRoot.setLayoutTransition(transition);
        }
        mImageView = findViewById(R.id.imageview);
        imageview_error = findViewById(R.id.imageview_error);
        progressBar = findViewById(R.id.sobot_msgProgressBar);
        if (isShowProgressbar) {
            progressBar.setVisibility(VISIBLE);
        } else {
            progressBar.setVisibility(GONE);
        }
        if (mImageWidth > 0 && mImageHeight > 0) {
            rcRoot.setLayoutParams(new LayoutParams(mImageWidth, mImageHeight));
            mImageView.setLayoutParams(new LayoutParams(mImageWidth, mImageHeight));
        } else if (mImageWidth > 0) {
            rcRoot.setLayoutParams(new LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            mImageView.setLayoutParams(new LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else if (mImageHeight > 0) {
            rcRoot.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mImageHeight));
            mImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mImageHeight));
        }
        if (mMaxHeight > 0) {
            mImageView.setMaxHeight(mMaxHeight);
        }
        if (mMaxWidth > 0) {
            mImageView.setMaxWidth(mMaxWidth);
        }
        if (mMinHeight > 0) {
            mImageView.setMinimumHeight(mMinHeight);
        }
        if (mMinWidth > 0) {
            mImageView.setMinimumWidth(mMinWidth);
        }
    }

    /**
     * 设置图片 同时设置图片展示样式
     */
    public void setImageUrlWithScaleType(String imageUrl, ImageView.ScaleType scaleType) {
        if (scaleType != null) {
            setScaleType(scaleType);
        }
        setImageUrl(imageUrl);
    }

    /**
     * 设置图片
     */
    public void setImageUrl(String imageUrl) {
        if (mImageView != null) {
            if (SobotStringUtils.isNoEmpty(imageUrl)) {
                int preloadWidth = 0;
                int preloadHeight = 0;
                if (mImageWidth == 0 && mImageHeight == 0) {
                    preloadWidth = ScreenUtils.dip2px(getContext(), 360);
                    preloadHeight = ScreenUtils.dip2px(getContext(), 360);
                }else {
                    preloadWidth = ScreenUtils.dip2px(getContext(), mImageWidth);
                    preloadHeight = ScreenUtils.dip2px(getContext(), mImageHeight);
                }
                SobotBitmapUtil.display(getContext(), imageUrl, mImageView, 0, 0, preloadWidth, preloadHeight, new SobotImageLoader.SobotDisplayImageListener() {
                    @Override
                    public void onSuccess(View view, String path) {
                        progressBar.setVisibility(GONE);
                        imageview_error.setVisibility(GONE);
                        rcRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sobot_color_transparent));
                        if (mImageWidth == 0 && mImageHeight == 0) {
                            rcRoot.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            mImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        } else if (mImageHeight == 0) {
                            rcRoot.setLayoutParams(new LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                            mImageView.setLayoutParams(new LayoutParams(mImageWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                        } else if (mImageWidth == 0) {
                            rcRoot.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mImageHeight));
                            mImageView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mImageHeight));
                        }
                    }

                    @Override
                    public void onFail(View view, String path) {
                        progressBar.setVisibility(GONE);
                        imageview_error.setVisibility(VISIBLE);
                        rcRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sobot_color_progress_image_bg));
                    }
                });
            } else {
                progressBar.setVisibility(GONE);
                imageview_error.setVisibility(GONE);
            }
        }
    }

    /**
     * 设置本地图片
     */
    public void setImageLocal(int imageResouseId) {
        if (mImageView != null) {
            progressBar.setVisibility(GONE);
            imageview_error.setVisibility(GONE);
            rcRoot.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rcRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sobot_color_transparent));
            SobotBitmapUtil.display(getContext(), imageResouseId, mImageView);
        }
    }
    /**
     * 设置本地图片
     */
    public void setImageLocal(int imageResouseId, ImageView.ScaleType scaleType) {
        if (scaleType != null) {
            setScaleType(scaleType);
        }
        if (mImageView != null) {
            progressBar.setVisibility(GONE);
            imageview_error.setVisibility(GONE);
            rcRoot.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rcRoot.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.sobot_color_transparent));
            SobotBitmapUtil.display(getContext(), imageResouseId, mImageView);
        }
    }

    public ImageView getImageView() {
        return mImageView;
    }

    /**
     * 设置图片加载样式
     *
     * @param scaleType
     */
    public void setScaleType(ImageView.ScaleType scaleType) {
        if (mImageView != null && scaleType != null) {
            mImageView.setScaleType(scaleType);
        }
    }


    public void setImageWidthAndHeight(int imageWidth,int imageHeight) {
        this.mImageWidth = imageWidth;
        this.mImageHeight = imageHeight;
        mImageView.setLayoutParams(new LayoutParams(imageWidth,imageHeight));
    }



    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int mMaxHeight) {
        this.mMaxHeight = mMaxHeight;
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setMaxWidth(int mMaxWidth) {
        this.mMaxWidth = mMaxWidth;
    }

    public int getMinHeight() {
        return mMinHeight;
    }

    public void setMinHeight(int mMinHeight) {
        this.mMinHeight = mMinHeight;
    }


    public void setMinWidth(int mMinWidth) {
        this.mMinWidth = mMinWidth;
    }

    /**
     * 是否显示加载进度控件
     *
     * @param isShow
     */
    public void isShowProgressBar(boolean isShow) {
        if (isShow) {
            if (progressBar != null) {
                progressBar.setVisibility(VISIBLE);
            }
        } else {
            if (progressBar != null) {
                progressBar.setVisibility(GONE);
            }
        }
    }
}