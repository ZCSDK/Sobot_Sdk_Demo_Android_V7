package com.sobot.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.StVideoView;
import com.sobot.chat.camera.listener.StVideoListener;
import com.sobot.chat.utils.LogUtils;
import com.sobot.pictureframe.SobotBitmapUtil;

/**
 * @author Created by jinxl on 2018/12/3.
 */
public class SobotVideoActivity extends FragmentActivity implements View.OnClickListener {
    private static final String EXTRA_VIDEO_FILE_DATA = "EXTRA_VIDEO_FILE_DATA";
    public static final int ACTION_TYPE_VIDEO = 1;

    private StVideoView mVideoView;
    private TextView st_tv_play;
    private ImageView st_iv_pic;
    private ProgressBar progressBar;

    private SobotCacheFile mCacheFile;

    /**
     * @param context 应用程序上下文
     * @return
     */
    public static Intent newIntent(Context context, SobotCacheFile cacheFile) {
        if (cacheFile == null) {
            return null;
        }
        Intent intent = new Intent(context, SobotVideoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_VIDEO_FILE_DATA, cacheFile);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.sobot_activity_video);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //避免刘海屏遮挡
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(layoutParams);
        }
        MyApplication.getInstance().addActivity(this);
        mVideoView = (StVideoView) findViewById(R.id.sobot_videoview);
        st_tv_play = (TextView) findViewById(R.id.st_tv_play);
        st_iv_pic = (ImageView) findViewById(R.id.st_iv_pic);
        progressBar = (ProgressBar) findViewById(R.id.sobot_msgProgressBar);
        st_tv_play.setOnClickListener(this);
        initData();
        mVideoView.setVideoLisenter(new StVideoListener() {

            @Override
            public void onStart() {
                st_tv_play.setVisibility(View.GONE);
            }

            @Override
            public void onPrepared() {
                showFinishUi();
            }

            @Override
            public void onEnd() {
                LogUtils.i("progress---onEnd");
                st_tv_play.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                //错误监听
                showErrorUi();
            }

            @Override
            public void onCancel() {
                finish();
            }

        });
    }

    private void initData() {
        try {
            Intent intent = getIntent();
            mCacheFile = (SobotCacheFile) intent.getSerializableExtra(EXTRA_VIDEO_FILE_DATA);
            if (mCacheFile == null) {
                return;
            }
            showLoadingUi();
            if (!TextUtils.isEmpty(mCacheFile.getFilePath())) {
                //设置视频保存路径
                mVideoView.setVideoPath(mCacheFile.getFilePath());
                mVideoView.playVideo();
            } else if (!TextUtils.isEmpty(mCacheFile.getUrl())) {
                //设置视频保存路径
                mVideoView.setVideoPath(mCacheFile.getUrl());
                mVideoView.playVideo();
            }

        } catch (Exception e) {
            //ignore
            e.printStackTrace();
        }
    }


    private void showLoadingUi() {
        st_tv_play.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        st_iv_pic.setVisibility(View.VISIBLE);
        SobotBitmapUtil.display(this, mCacheFile.getSnapshot(), st_iv_pic, 0, 0);
    }

    private void showErrorUi() {
        st_tv_play.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        st_iv_pic.setVisibility(View.VISIBLE);
        SobotBitmapUtil.display(this, mCacheFile.getSnapshot(), st_iv_pic, 0, 0);
    }

    private void showFinishUi() {
        st_tv_play.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        st_iv_pic.setVisibility(View.GONE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else if (Build.VERSION.SDK_INT >= 16) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onPause() {
        mVideoView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v == st_tv_play) {
            st_tv_play.setSelected(!st_tv_play.isSelected());
            mVideoView.switchVideoPlay(st_tv_play.isSelected());
        }
    }
}
