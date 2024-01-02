package com.sobot.chat.activity.base;

import static com.sobot.chat.fragment.SobotBaseFragment.REQUEST_CODE_CAMERA;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.PermissionListener;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.dialog.SobotPermissionTipDialog;
import com.sobot.chat.widget.statusbar.StatusBarUtil;

import java.io.File;
import java.util.Locale;

public abstract class SobotBaseActivity extends FragmentActivity {

    public ZhiChiApi zhiChiApi;

    protected File cameraFile;

    //权限回调
    public PermissionListener permissionListener;
    private int initMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //修改国际化语言
        changeAppLanguage();
        super.onCreate(savedInstanceState);
        initMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);//竖屏
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//横屏

            }
        }
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(this);
            // 设置Activity全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(getContentViewResId());
        try {
            String host = SharedPreferencesUtil.getStringData(getSobotBaseContext(), ZhiChiConstant.SOBOT_SAVE_HOST_AFTER_INITSDK, SobotBaseUrl.getApi_Host());
            if (!host.equals(SobotBaseUrl.getApi_Host())) {
                SobotBaseUrl.setApi_Host(host);
            }
        } catch (Exception e) {
        }
        setUpToolBar();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        zhiChiApi = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        MyApplication.getInstance().addActivity(this);
        View toolBar = findViewById(R.id.sobot_layout_titlebar);
        if (toolBar != null) {
            setUpToolBarLeftMenu();

            setUpToolBarRightMenu();
        }
        try {
            initBundleData(savedInstanceState);
            initView();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //左上角返回按钮水滴屏适配
        if (getLeftMenu() != null) {
            displayInNotch(getLeftMenu());
        }
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(this, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 44;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 44;
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 44;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 44;
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding((rect.right > 110 ? 110 : rect.right) + view.getPaddingLeft(), view.getPaddingTop(), (rect.right > 110 ? 110 : rect.right) + view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    public void changeAppLanguage() {
        Locale language = (Locale) SharedPreferencesUtil.getObject(SobotBaseActivity.this, ZhiChiConstant.SOBOT_LANGUAGE);
        try {
            // 本地语言设置
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            if (language != null) {
                conf.locale = language;
            }
            res.updateConfiguration(conf, dm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void setUpToolBarRightMenu() {
        if (getRightMenu() != null) {
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getRightMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRightMenuClick(v);
                }
            });
        }
    }

    protected void setUpToolBarLeftMenu() {
        if (getLeftMenu() != null) {
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getLeftMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLeftMenuClick(v);
                }
            });
        }
    }


    protected void setUpToolBar() {
        View toolBar = getToolBar();
        if (toolBar == null) {
            return;
        }

        int robot_current_themeImg = SharedPreferencesUtil.getIntData(this, "robot_current_themeImg", 0);
        if (robot_current_themeImg != 0) {
            toolBar.setBackgroundResource(robot_current_themeImg);
        }
        updateToolBarBg();
    }

    protected View getToolBar() {
        return findViewById(R.id.sobot_layout_titlebar);
    }

    protected TextView getLeftMenu() {
        return findViewById(R.id.sobot_tv_left);
    }

    protected TextView getRightMenu() {
        return findViewById(R.id.sobot_tv_right);
    }

    protected TextView getTitleView() {
        return findViewById(R.id.sobot_text_title);
    }


    /**
     * @param resourceId
     * @param textId
     * @param isShow
     */
    protected void showRightMenu(int resourceId, String textId, boolean isShow) {
        View tmpMenu = getRightMenu();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView rightMenu = (TextView) tmpMenu;
        if (!TextUtils.isEmpty(textId)) {
            rightMenu.setText(textId);
        } else {
            rightMenu.setText("");
        }

        if (resourceId != 0) {
            Drawable img = getResources().getDrawable(resourceId);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            rightMenu.setCompoundDrawables(null, null, img, null);
        } else {
            rightMenu.setCompoundDrawables(null, null, null, null);
        }

        if (isShow) {
            rightMenu.setVisibility(View.VISIBLE);
        } else {
            rightMenu.setVisibility(View.GONE);
        }
    }

    /**
     * @param resourceId
     * @param textId
     * @param isShow
     */
    protected void showLeftMenu(int resourceId, String textId, boolean isShow) {
        View tmpMenu = getLeftMenu();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView leftMenu = (TextView) tmpMenu;
        if (!TextUtils.isEmpty(textId)) {
            leftMenu.setText(textId);
        } else {
            leftMenu.setText("");
        }

        if (resourceId != 0) {
            Drawable img = getResources().getDrawable(resourceId);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            leftMenu.setCompoundDrawables(img, null, null, null);
        } else {
            leftMenu.setCompoundDrawables(null, null, null, null);
        }

        if (isShow) {
            leftMenu.setVisibility(View.VISIBLE);
        } else {
            leftMenu.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(SobotBaseActivity.this);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }

    /**
     * 导航栏左边点击事件
     *
     * @param view
     */
    protected void onLeftMenuClick(View view) {
        onBackPressed();
    }

    /**
     * 导航栏右边点击事件
     *
     * @param view
     */
    protected void onRightMenuClick(View view) {

    }

    public void setTitle(CharSequence title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setText(title);
    }

    public void setTitle(int title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    //返回布局id
    protected abstract int getContentViewResId();

    protected void initBundleData(Bundle savedInstanceState) {
    }

    protected abstract void initView();

    protected abstract void initData();


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE:
                try {
                    for (int i = 0; i < grantResults.length; i++) {
                        //判断权限的结果，如果有被拒绝，就return
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            String permissionTitle = getResources().getString(R.string.sobot_no_permission_text);
                            if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_write_external_storage_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getResources().getString(R.string.sobot_memory_card) + " , " + getResources().getString(R.string.sobot_memory_card_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(this, permissionTitle);
                                    }
                                }
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_record_audio_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getResources().getString(R.string.sobot_microphone) + " , " + getString(R.string.sobot_microphone_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(this, permissionTitle);
                                    }
                                }
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_camera_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getString(R.string.sobot_camera) + " , " + getString(R.string.sobot_camera_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(this, permissionTitle);
                                    }
                                }
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_write_external_storage_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getString(R.string.sobot_memory_card) + " , " + getString(R.string.sobot_memory_card_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(getSobotBaseActivity(), permissionTitle);
                                    }
                                }
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_VIDEO)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_write_external_storage_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_VIDEO) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getString(R.string.sobot_memory_card) + " , " + getString(R.string.sobot_memory_card_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(getSobotBaseActivity(), permissionTitle);
                                    }
                                }
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_AUDIO)) {
                                permissionTitle = getResources().getString(R.string.sobot_no_write_external_storage_permission);
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_AUDIO) && !ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
                                    ToastUtil.showCustomLongToast(this, CommonUtils.getAppName(this) + getResources().getString(R.string.sobot_want_use_your) + getString(R.string.sobot_memory_card) + " , " + getString(R.string.sobot_memory_card_yongtu));
                                } else {
                                    //调用权限失败
                                    if (permissionListener != null) {
                                        permissionListener.onPermissionErrorListener(getSobotBaseActivity(), permissionTitle);
                                    }
                                }
                            }
                            return;
                        }
                    }
                    if (permissionListener != null) {
                        permissionListener.onPermissionSuccessListener();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 检查存储权限
     *
     * @param checkType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkStoragePermission(int checkType) {
        //如果是升级Android13之前就已经具有读写SDK的权限，那么升级到13之后，自己具有上述三个权限。
        //如果是升级Android13之后新装的应用，并且targetSDK小于33，则申请READ_EXTERNAL_STORAGE权限时，会自动转化为对上述三个权限的申请，权限申请框只一个
        //如果是升级Android13之后新装的应用，并且targetSDK大于等于33，则申请READ_EXTERNAL_STORAGE权限时会自动拒绝（同理WRITE_EXTERNAL_STORAGE也是一样）。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkType == 0) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请图片权限
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                    return false;
                }
            } else if (checkType == 1) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请视频权限
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                    return false;
                }
            } else if (checkType == 2) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请音频权限
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                    return false;
                }
            } else {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请：图片权限 视频权限 音频权限
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                    return false;
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请READ_EXTERNAL_STORAGE权限
                this.requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有存储卡权限
     *
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true, 已经获取权限;false,没有权限
     */
    protected boolean isHasStoragePermission(int checkPermissionType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissionType == 0) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } else if (checkPermissionType == 1) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } else if (checkPermissionType == 2) {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            } else {
                if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_MEDIA_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } else if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有录音权限
     *
     * @return true, 已经获取权限;false,没有权限
     */
    protected boolean isHasAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}
                        , ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有相机权限
     *
     * @return true, 已经获取权限;false,没有权限
     */
    protected boolean isHasCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * 通过照相上传图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            ToastUtil.showCustomToast(getSobotBaseActivity().getApplicationContext(), getString(R.string.sobot_sdcard_does_not_exist),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                //如果有拍照所需的权限，跳转到拍照界面
                startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), REQUEST_CODE_CAMERA);
            }
        };

        if (checkIsShowPermissionPop(getString(R.string.sobot_camera), getString(R.string.sobot_camera_yongtu), 3, 3)) {
            return;
        }

        if (!checkCameraPermission()) {
            return;
        }

        // 打开拍摄页面
        startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), REQUEST_CODE_CAMERA);
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectPic(getSobotBaseActivity());
            }
        };
        if (checkIsShowPermissionPop(getString(R.string.sobot_memory_card), getString(R.string.sobot_memory_card_yongtu), 1, 0)) {
            return;
        }
        if (!checkStoragePermission(0)) {
            return;
        }
        ChatUtils.openSelectPic(getSobotBaseActivity());
    }

    /**
     * 判断是否有存储卡权限
     *
     * @param type                1 存储卡;2 麦克风;3 相机;
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true, 已经获取权限;false,没有权限
     */
    protected boolean isHasPermission(int type, int checkPermissionType) {
        if (type == 1) {
            return isHasStoragePermission(checkPermissionType);
        } else if (type == 2) {
            return isHasAudioPermission();
        } else if (type == 3) {
            return isHasCameraPermission();
        }
        return true;
    }

    /**
     * 检测是否需要弹出权限用途提示框pop,如果弹出，则拦截接下来的处理逻辑，自己处理
     *
     * @param title
     * @param content
     * @param type
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return
     */
    public boolean checkIsShowPermissionPop(String title, String content, final int type, final int checkPermissionType) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.SHOW_PERMISSION_TIPS_POP)) {
            if (!isHasPermission(type, checkPermissionType)) {
                SobotPermissionTipDialog dialog = new SobotPermissionTipDialog(getSobotBaseActivity(), title, content, new SobotPermissionTipDialog.ClickViewListener() {
                    @Override
                    public void clickRightView(Context context, SobotPermissionTipDialog dialog) {
                        dialog.dismiss();
                        if (type == 1) {
                            if (!checkStoragePermission(checkPermissionType)) {
                                return;
                            }
                        } else if (type == 2) {
                            if (!checkAudioPermission()) {
                                return;
                            }
                        } else if (type == 3) {
                            if (!checkCameraPermission()) {
                                return;
                            }
                        }
                    }

                    @Override
                    public void clickLeftView(Context context, SobotPermissionTipDialog dialog) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        }
        return false;
    }

    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectVedio(getSobotBaseActivity());
            }
        };
        if (checkIsShowPermissionPop(getString(R.string.sobot_memory_card), getString(R.string.sobot_memory_card_yongtu), 1, 1)) {
            return;
        }
        if (!checkStoragePermission(1)) {
            return;
        }
        ChatUtils.openSelectVedio(getSobotBaseActivity());
    }


    public SobotBaseActivity getSobotBaseActivity() {
        return this;
    }

    public Context getSobotBaseContext() {
        return this;
    }

    public static boolean isCameraCanUse() {

        boolean canUse = false;
        Camera mCamera = null;

        try {
            mCamera = Camera.open(0);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            canUse = false;
        }

        if (mCamera != null) {
            mCamera.release();
            canUse = true;
        }

        return canUse;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (initMode != currentNightMode) {
            initMode = currentNightMode;
            recreate();
        }
    }

    /**
     * 是否是全屏
     *
     * @return
     */
    protected boolean isFullScreen() {
        return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /**
     * 导航栏渐变逻辑
     * 先判断客户开发是否设置，如果设置了 直接使用；如果没有修改（和系统默认一样），就就绪判断后端接口返回的颜色；
     * 如果接口返回的也和系统一样，就不处理（默认渐变色）；如果不一样，直接按照接口的设置渐变色
     */
    private void updateToolBarBg() {
        try {
            ZhiChiInitModeBase initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseActivity(),
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initModel == null) {
                setToolBarDefBg();
                return;
            }
            if (getToolBar() == null) {
                return;
            }
            if (initModel != null && initModel.getVisitorScheme() != null) {
                //导航条显示1 开启 0 关闭
                if (initModel.getVisitorScheme().getTopBarFlag() == 1) {
                    getToolBar().setVisibility(View.VISIBLE);
                } else {
                    getToolBar().setVisibility(View.GONE);
                }
            }

            if (getResources().getColor(R.color.sobot_gradient_start) == getResources().getColor(R.color.sobot_color_title_bar_left_bg) && getResources().getColor(R.color.sobot_gradient_end) == getResources().getColor(R.color.sobot_color_title_bar_bg)) {
                if (initModel != null && initModel.getVisitorScheme() != null) {
                    //服务端返回的导航条背景颜色
                    if (!TextUtils.isEmpty(initModel.getVisitorScheme().getTopBarColor())) {
                        String topBarColor[] = initModel.getVisitorScheme().getTopBarColor().split(",");
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
                            } else {
                                setToolBarDefBg();
                            }
                        }
                    }
                } else {
                    setToolBarDefBg();
                }
            } else {
                setToolBarDefBg();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 设置默认导航栏渐变色
     */
    private void setToolBarDefBg() {
        try {
            int[] colors = new int[]{getResources().getColor(R.color.sobot_color_title_bar_left_bg), getResources().getColor(R.color.sobot_color_title_bar_bg)};
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColors(colors); //添加颜色组
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
            gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
            getToolBar().setBackground(gradientDrawable);
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            } else {
                GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                StatusBarUtil.setColor(getSobotBaseActivity(), aDrawable);
            }
        } catch (Exception e) {
        }
    }
}