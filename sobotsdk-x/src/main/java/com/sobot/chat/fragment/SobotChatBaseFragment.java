package com.sobot.chat.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotSelectPicAndVideoActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.PermissionListener;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;

import java.io.File;
import java.util.Arrays;

/**
 * @author Created by jinxl on 2018/2/1.
 */
public abstract class SobotChatBaseFragment extends Fragment {

    public ZhiChiApi zhiChiApi;
    protected File cameraFile;

    private Activity activity;
    //权限回调
    public PermissionListener permissionListener;
    private View overlay;//权限用途提示蒙层
    private ViewGroup viewGroup;//根view content

    public SobotChatBaseFragment() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (activity == null) {
            activity = (Activity) context;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zhiChiApi = SobotMsgManager.getInstance(getContext().getApplicationContext()).getZhiChiApi();
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(getActivity());
            // 设置Activity全屏
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(getActivity(), new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 110 ? 110 : rect.right) + 14;
                                layoutParams.leftMargin = (rect.right > 110 ? 110 : rect.right) + 14;
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


    @Override
    public void onDestroyView() {
        HttpUtils.getInstance().cancelTag(SobotChatBaseFragment.this);
        HttpUtils.getInstance().cancelTag(ZhiChiConstant.SOBOT_GLOBAL_REQUEST_CANCEL_TAG);
        super.onDestroyView();
    }


    public SobotChatBaseFragment getSobotBaseFragment() {
        return this;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE:
                try {
                    //android 14 api 34 以上 部分权限直接打开“选择图片和视频界面”
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && grantResults.length > 1) {
                        boolean isAllGranted = true;
                        for (int i = 0; i < grantResults.length; i++) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                isAllGranted = false;
                            }
                        }
                        if (!isAllGranted) {
                            //如果android 14 有部分权限，直接启动“选择图片和视频界面”
                            for (int i = 0; i < grantResults.length; i++) {
                                if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    int selectType;
                                    if (Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_IMAGES) && Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                                        selectType = 3;//部分视频和图片
                                    } else if (Arrays.asList(permissions).contains(Manifest.permission.READ_MEDIA_VIDEO)) {
                                        selectType = 2;//部分视频
                                    } else {
                                        selectType = 1;//部分图片
                                    }
                                    openSelectPic(selectType);
                                    return;
                                }
                            }
                        }
                    }

                    for (int i = 0; i < grantResults.length; i++) {
                        //判断权限的结果，如果有被拒绝，就return
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_IMAGES)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.READ_MEDIA_VIDEO)) {
                                showPerssionSettingUi();
                                return;
                            }
                            if (permissions[i] != null && !permissions[i].equals(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                                //不处理 READ_MEDIA_VISUAL_USER_SELECTED权限，如果是Android13 全部允许权限时，这个权限返回的还是-1
                                return;
                            }
                        }
                    }
                    if (permissionListener != null) {
                        permissionListener.onPermissionSuccessListener();
                    }
                    hidePerssionUi();
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 检测是否没有对应的权限，没有权限显示提示蒙层
     *
     * @param type                1：文件 2：麦克风 3：相机
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true :有权限 false:没有权限
     */
    public boolean isHasPermission(int type, int checkPermissionType) {
        boolean isHasPermission = false;
        if (type == 1) {
            int result = checkStoragePermission(checkPermissionType);
            if (result == 0) {
                isHasPermission = true;
            } else if (result == 2) {
                //部分权限
                isHasPermission = false;
                if (checkPermissionType == 3) {
                    //1:部分图片 2:部分视频 3:部分视频和图片
                    openSelectPic(3);
                } else if (checkPermissionType == 1) {
                    openSelectPic(2);
                } else {
                    openSelectPic(1);
                }
            } else {
                isHasPermission = false;
                if (checkPermissionType == 3) {
                    showPerssionUi(1);
                } else {
                    showPerssionUi(0);
                }
                //申请权限
                requestStoragePermission(checkPermissionType);
            }
        } else if (type == 2) {
            isHasPermission = checkAudioPermission();
            if (!isHasPermission) {
                showPerssionUi(2);
            }
        } else if (type == 3) {
            isHasPermission = checkCameraPermission();
            if (!isHasPermission) {
                showPerssionUi(3);
            }
        }
        return isHasPermission;
    }

    /**
     * 显示权限蒙层
     *
     * @param type 0：照片和视频 1：文件 2：麦克风 3：相机
     */
    public void showPerssionUi(int type) {
        overlay = LayoutInflater.from(getSobotActivity()).inflate(R.layout.sobot_layout_overlay, null);
        if (overlay != null) {
            final LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            final LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            Button btn_left = overlay.findViewById(R.id.btn_left);
            Button btn_right = overlay.findViewById(R.id.btn_right);
            TextView tv_setting_title = overlay.findViewById(R.id.tv_setting_title);
            TextView tv_setting_content = overlay.findViewById(R.id.tv_setting_content);
            if (type == 0) {
                tv_content.setText("\"" + CommonUtils.getAppName(getContext()) + "\" " + getResources().getString(R.string.sobot_album_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_album));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_album));
            } else if (type == 1) {
                tv_content.setText("\"" + CommonUtils.getAppName(getContext()) + "\" " + getResources().getString(R.string.sobot_storage_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_storage));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_storage));
            } else if (type == 2) {
                tv_content.setText("\"" + CommonUtils.getAppName(getContext()) + "\" " + getResources().getString(R.string.sobot_microphone_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_microphone));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_microphone));
            } else if (type == 3) {
                tv_content.setText("\"" + CommonUtils.getAppName(getContext()) + "\" " + getResources().getString(R.string.sobot_camera_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_camera));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_camera));
            }
            viewGroup = getSobotActivity().findViewById(android.R.id.content);
            viewGroup.addView(overlay);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ll_setting.getVisibility() == View.GONE) {
                        ll_info.setVisibility(View.VISIBLE);
                    }
                }
            }, 300);//延迟0.5s 是避免多次拒绝后ll_info 隐藏会出现闪一下的问题
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hidePerssionUi();
                }
            });
            btn_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hidePerssionUi();
                }
            });
            btn_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hidePerssionUi();
                    Uri packageURI = Uri.parse("package:" + getSobotActivity().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    getSobotActivity().startActivity(intent);
                }
            });
        }
    }

    //拒绝权限后显示 去设置UI
    public void showPerssionSettingUi() {
        String permissionTitle = "";
        if (overlay != null) {
            LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            ll_info.setVisibility(View.GONE);
            ll_setting.setVisibility(View.VISIBLE);
            if (tv_content != null && StringUtils.isNoEmpty(tv_content.getText().toString())) {
                permissionTitle = tv_content.getText().toString();
            }
        }
        if (permissionListener != null) {
            permissionListener.onPermissionErrorListener(getSobotActivity(), permissionTitle);
        }
    }

    //移除权限提示蒙层
    public void hidePerssionUi() {
        if (overlay != null) {
            if (viewGroup == null) {
                viewGroup = getSobotActivity().findViewById(android.R.id.content);
            }
            viewGroup.removeView(overlay);
        }
    }

    /**
     * 申请存储权限
     *
     * @param checkType 0：图片权限 1：视频权限，3，所有细分的权限， android 13 使用
     */
    public void requestStoragePermission(int checkType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (checkType == 0) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                } else if (checkType == 1) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                }
            } else {
                if (checkType == 0) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                } else if (checkType == 1) {
                    this.requestPermissions(new String[]{Manifest.permission.READ_MEDIA_VIDEO}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
                }
            }
        } else {
            //申请READ_EXTERNAL_STORAGE权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    /**
     * 检查存储权限
     *
     * @param checkType 0：图片权限 1：视频权限，3，所有细分的权限， android 13 使用
     * @return int  0：有权限，1：没有权限，2:有部分权限
     */
    public int checkStoragePermission(int checkType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //android 13 api 33 以上
            if (checkType == 0 || checkType == 1) {
                //检测是否有图片权限
                if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        == PackageManager.PERMISSION_GRANTED) {
                    //有权限
                    return 0;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED) {
                    //android 14 有部分权限
                    return 2;
                } else {
                    //没有权限
                    return 1;
                }
            } else {
                //检测是否有图片权限
                if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_VIDEO)
                        == PackageManager.PERMISSION_GRANTED ) {
                    //有权限
                    return 0;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                        == PackageManager.PERMISSION_GRANTED) {
                    //android 14 有部分权限
                    return 2;
                } else {
                    //没有权限
                    return 1;
                }
            }
        } else {
            // android 13 api33 以前
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return 0; //有权限
            } else {
                return 1; //没有权限
            }
        }
    }

    /**
     * android 14 部分权限情况下，回显照片或者视频
     *
     * @param selectType 1:部分图片 2:部分视频 3:部分视频和图片
     */
    private void openSelectPic(int selectType) {
        //隐藏权限提示蒙层
        hidePerssionUi();
        Intent intent = new Intent(getSobotActivity(), SobotSelectPicAndVideoActivity.class);
        intent.putExtra("selectType", selectType);
        startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
    }

    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
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
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}
                        , ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_CODE);
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
            ToastUtil.showCustomToast(getSobotActivity().getApplicationContext(), getString(R.string.sobot_sdcard_does_not_exist),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                //如果有拍照所需的权限，跳转到拍照界面
                startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseFragment().getContext()), ChatUtils.REQUEST_CODE_CAMERA);
            }
        };

        if (!isHasPermission(3, 3)) {
            return;
        }
        // 打开拍摄页面
        startActivityForResult(SobotCameraActivity.newIntent(getContext()), ChatUtils.REQUEST_CODE_CAMERA);
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectPic(getSobotActivity(), getSobotBaseFragment());
            }
        };
        if (!isHasPermission(1, 0)) {
            return;
        }
        ChatUtils.openSelectPic(getSobotActivity(), SobotChatBaseFragment.this);
    }


    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                ChatUtils.openSelectVedio(getSobotActivity(), getSobotBaseFragment());
            }
        };
        if (!isHasPermission(1, 1)) {
            return;
        }
        ChatUtils.openSelectVedio(getSobotActivity(), SobotChatBaseFragment.this);
    }

    /**
     * 返回activity
     *
     * @return
     */
    public Activity getSobotActivity() {
        Activity activity = getActivity();
        if (activity == null) {
            return this.activity;
        }
        return activity;
    }

}