package com.sobot.chat.activity.halfdialog;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPhotoActivity;
import com.sobot.chat.activity.SobotVideoActivity;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.adapter.SobotUploadFileAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.api.model.SobotUserTicketInfo;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.FastClickUtils;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MD5Util;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.HorizontalItemSpacingDecoration;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.dialog.SobotDeleteWorkOrderDialog;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.dialog.SobotSelectPicDialog;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SobotReplyActivity extends SobotDialogBaseActivity implements View.OnClickListener {


    private EditText sobotReplyEdit;
    private RecyclerView sobotReplyMsgPic;
    private ImageView sobot_btn_file;//上传按钮
    private TextView sobotBtnSubmit;

    private ArrayList<SobotFileModel> pic_list = new ArrayList<>();
    private SobotUploadFileAdapter adapter;
    private SobotSelectPicDialog menuWindow;


    /**
     * 删除图片弹窗
     */
    protected SobotDeleteWorkOrderDialog seleteMenuWindow;

    protected File cameraFile;
    private String mUid = "";
    private String mCompanyId = "";
    private SobotUserTicketInfo mTicketInfo;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_dialog_reply;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotReplyActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        sobot_btn_file = findViewById(R.id.sobot_btn_file);
        sobotReplyEdit = (EditText) findViewById(R.id.sobot_reply_edit);
        sobotReplyMsgPic = findViewById(R.id.sobot_reply_msg_pic);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        // 设置RecyclerView的LayoutManager
        sobotReplyMsgPic.setLayoutManager(layoutManager);
        sobotReplyMsgPic.addItemDecoration(new HorizontalItemSpacingDecoration(ScreenUtils.dip2px(this, 8)));
        sobotBtnSubmit = findViewById(R.id.sobot_btn_submit);
        sobotBtnSubmit.setText(R.string.sobot_btn_submit_text);
        if (ThemeUtils.isChangedThemeColor(getSobotBaseContext())) {
            Drawable bg = getResources().getDrawable(R.drawable.sobot_btn_bg_16);
            if (bg != null) {
                sobotBtnSubmit.setBackground(ThemeUtils.applyColorToDrawable(bg, ThemeUtils.getThemeColor(getSobotBaseContext())));
            }
        }

        List<SobotFileModel> picTempList = (List<SobotFileModel>) getIntent().getSerializableExtra("picTempList");
        String replyTempContent = getIntent().getStringExtra("replyTempContent");
        if (!StringUtils.isEmpty(replyTempContent)) {
            sobotReplyEdit.setText(replyTempContent);
        }

        if (picTempList != null && !picTempList.isEmpty()) {
            pic_list.addAll(picTempList);
        }

        sobotBtnSubmit.setOnClickListener(this);
        sobot_btn_file.setOnClickListener(this);
        adapter = new SobotUploadFileAdapter(SobotReplyActivity.this, pic_list, true,true, new SobotUploadFileAdapter.Listener() {
            @Override
            public void downFileLister(SobotFileModel model) {

            }

            @Override
            public void previewMp4(SobotFileModel fileModel) {
                hideKeyboard();
                File file = new File(fileModel.getFileUrl());
                SobotCacheFile cacheFile = new SobotCacheFile();
                cacheFile.setFileName(file.getName());
                cacheFile.setUrl(fileModel.getFileUrl());
                cacheFile.setFilePath(fileModel.getFileUrl());
                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(fileModel.getFileUrl())));
                cacheFile.setMsgId("" + System.currentTimeMillis());
                Intent intent = SobotVideoActivity.newIntent(SobotReplyActivity.this, cacheFile);
                SobotReplyActivity.this.startActivity(intent);
            }

            @Override
            public void deleteFile(final SobotFileModel fileModel) {
                hideKeyboard();
                String popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_picture);
                if (fileModel != null) {
                    if (!TextUtils.isEmpty(fileModel.getFileUrl()) && MediaFileUtils.isVideoFileType(fileModel.getFileUrl())) {
                        popMsg = getContext().getResources().getString(R.string.sobot_do_you_delete_video);
                    }
                }
                if (seleteMenuWindow != null) {
                    seleteMenuWindow.dismiss();
                    seleteMenuWindow = null;
                }
                if (seleteMenuWindow == null) {
                    seleteMenuWindow = new SobotDeleteWorkOrderDialog(SobotReplyActivity.this, popMsg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seleteMenuWindow.dismiss();
                            if (v.getId() == R.id.btn_pick_photo) {
                                Log.e("onClick: ", seleteMenuWindow.getPosition() + "");
                                pic_list.remove(fileModel);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                seleteMenuWindow.show();
            }

            @Override
            public void previewPic(String fileUrl, String fileName) {
                hideKeyboard();
                if (SobotOption.imagePreviewListener != null) {
                    //如果返回true,拦截;false 不拦截
                    boolean isIntercept = SobotOption.imagePreviewListener.onPreviewImage(getSobotBaseContext(), fileUrl);
                    if (isIntercept) {
                        return;
                    }
                }
                Intent intent = new Intent(SobotReplyActivity.this, SobotPhotoActivity.class);
                intent.putExtra("imageUrL", fileUrl);
                startActivity(intent);
            }
        });
        sobotReplyMsgPic.setAdapter(adapter);
        mUid = getIntent().getStringExtra("uid");
        mCompanyId = getIntent().getStringExtra("companyId");
        mTicketInfo = (SobotUserTicketInfo) getIntent().getSerializableExtra("ticketInfo");
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(SobotReplyActivity.this, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dip2px(SobotReplyActivity.this, 104));
                            lp.setMargins((rect.right > 110 ? 110 : rect.right) + ScreenUtils.dip2px(SobotReplyActivity.this, 20), (rect.right > 110 ? 110 : rect.right) + ScreenUtils.dip2px(SobotReplyActivity.this, 20), ScreenUtils.dip2px(SobotReplyActivity.this, 20), ScreenUtils.dip2px(SobotReplyActivity.this, 20));
                            sobotReplyEdit.setLayoutParams(lp);
                        }
                    }
                }
            });

        }
        displayInNotch(sobotReplyMsgPic);
    }

    @Override
    protected void initData() {

    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {/*点击外部隐藏键盘*/
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getY() <= 0) {
                Intent intent = new Intent();
                intent.putExtra("replyTempContent", sobotReplyEdit.getText().toString());
                intent.putExtra("picTempList", (Serializable) pic_list);
                intent.putExtra("isTemp", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
        return true;
    }

    /*是否在外部*/
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        hideKeyboard();
        if (v == sobot_btn_file) {
            if (pic_list.size() >= 15) {
                //图片上限15张
                ToastUtil.showToast(this, getResources().getString(R.string.sobot_ticket_update_file_max_hite));
            } else {
                menuWindow = new SobotSelectPicDialog(SobotReplyActivity.this, itemsOnClick);
                menuWindow.show();
            }
        }
        if (v == sobotBtnSubmit) {//提交
            hideKeyboard();
            if (StringUtils.isEmpty(sobotReplyEdit.getText().toString().trim())) {
                ToastUtil.showToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_please_input_reply_no_empty));
                return;
            }
            if (FastClickUtils.isCanClick()) {
                SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                sobotBtnSubmit.setAlpha(0.5f);
                sobotBtnSubmit.setEnabled(false);
                sobotBtnSubmit.setClickable(false);
                zhiChiApi.replyTicketContent(this, mUid, mTicketInfo.getTicketId(), sobotReplyEdit.getText().toString(), getFileStr(), mCompanyId, new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        sobotBtnSubmit.setAlpha(1f);
                        sobotBtnSubmit.setEnabled(true);
                        sobotBtnSubmit.setClickable(true);
                        LogUtils.e(s);
                        ToastUtil.showCustomToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_leavemsg_success_tip), R.drawable.sobot_icon_success);
                        try {
                            Thread.sleep(500);//睡眠一秒  延迟拉取数据
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pic_list.clear();
                        Intent intent = new Intent();
                        intent.putExtra("replyTempContent", "");
                        intent.putExtra("picTempList", (Serializable) pic_list);
                        intent.putExtra("isTemp", false);
                        setResult(Activity.RESULT_OK, intent);
                        SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        sobotBtnSubmit.setAlpha(1f);
                        sobotBtnSubmit.setEnabled(true);
                        sobotBtnSubmit.setClickable(true);
                        ToastUtil.showCustomToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_leavemsg_error_tip));
                        e.printStackTrace();
                        SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    }
                });
            }
        }
    }

    // 为弹出窗口popupwindow实现监听类
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        public void onClick(View v) {
            menuWindow.dismiss();
            if (v.getId() == R.id.btn_take_photo) {
                LogUtils.i("拍照");
                selectPicFromCamera();

            }
            if (v.getId() == R.id.btn_pick_photo) {
                LogUtils.i("选择照片");
                permissionListener = new PermissionListenerImpl() {
                    @Override
                    public void onPermissionSuccessListener() {
                        ChatUtils.openSelectPic(SobotReplyActivity.this);
                    }
                };
                if (!isHasPermission(1, 0)) {
                    return;
                }
                ChatUtils.openSelectPic(SobotReplyActivity.this);
            }
            if (v.getId() == R.id.btn_pick_vedio) {
                LogUtils.i("选择视频");
                permissionListener = new PermissionListenerImpl() {
                    @Override
                    public void onPermissionSuccessListener() {
                        ChatUtils.openSelectVedio(SobotReplyActivity.this, null);
                    }
                };
                if (!isHasPermission(1, 1)) {
                    return;
                }
                ChatUtils.openSelectVedio(SobotReplyActivity.this, null);
            }

        }
    };

    public void addPicView(SobotFileModel item) {
        if (sobotReplyMsgPic.getVisibility() == View.GONE) {
            sobotReplyMsgPic.setVisibility(View.VISIBLE);
        }
        pic_list.add(item);
        adapter.notifyDataSetChanged();
        sobotReplyMsgPic.scrollToPosition(adapter.getItemCount() - 1);
    }


    public String getFileStr() {
        String tmpStr = "";
        for (int i = 0; i < pic_list.size(); i++) {
            tmpStr += pic_list.get(i).getFileUrl() + ";";
        }
        return tmpStr;
    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(SobotReplyActivity.this);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage == null) {
                        selectedImage = ImageUtils.getUri(data, SobotReplyActivity.this);
                    }
                    String path = ImageUtils.getPath(this, selectedImage);
                    if (MediaFileUtils.isVideoFileType(path)) {
                        try {
                            File selectedFile = new File(path);
                            if (selectedFile.exists()) {
                                if (selectedFile.length() > 50 * 1024 * 1024) {
                                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_file_upload_failed));
                                    return;
                                }
                            }
                            SobotDialogUtils.startProgressDialog(this);
//                            ChatUtils.sendPicByFilePath(this,path,sendFileListener,false);
                            String fName = MD5Util.encode(path);
                            String filePath = null;
                            try {
                                filePath = FileUtil.saveImageFile(this, selectedImage, fName + FileUtil.getFileEndWith(path), path);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToast(getApplicationContext(), getContext().getResources().getString(R.string.sobot_pic_type_error));
                                return;
                            }
                            sendFileListener.onSuccess(filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        SobotDialogUtils.startProgressDialog(this);
                        ChatUtils.sendPicByUriPost(this, selectedImage, sendFileListener, false);
                    }
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_did_not_get_picture_path));
                }
            } else if (requestCode == ZhiChiConstant.REQUEST_CODE_makePictureFromCamera) {
                if (cameraFile != null && cameraFile.exists()) {
                    SobotDialogUtils.startProgressDialog(this);
                    ChatUtils.sendPicByFilePath(this, cameraFile.getAbsolutePath(), sendFileListener, true);
                } else {
                    showHint(getContext().getResources().getString(R.string.sobot_pic_select_again));
                }
            }
        } else if (resultCode == SobotCameraActivity.RESULT_CODE) {
            if (requestCode == ChatUtils.REQUEST_CODE_CAMERA) {
                int actionType = SobotCameraActivity.getActionType(data);
                if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                    File videoFile = new File(SobotCameraActivity.getSelectedVideo(data));
                    if (videoFile.exists()) {
                        cameraFile = videoFile;
                        SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                        sendFileListener.onSuccess(videoFile.getAbsolutePath());
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                } else {
                    File tmpPic = new File(SobotCameraActivity.getSelectedImage(data));
                    if (tmpPic.exists()) {
                        cameraFile = tmpPic;
                        SobotDialogUtils.startProgressDialog(SobotReplyActivity.this);
                        ChatUtils.sendPicByFilePath(SobotReplyActivity.this, tmpPic.getAbsolutePath(), sendFileListener, true);
                    } else {
                        showHint(getResources().getString(R.string.sobot_pic_select_again));
                    }
                }
            }
        }

    }

    public void showHint(String content) {
        ToastUtil.showToast(getApplicationContext(), content);
    }

    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(SobotReplyActivity.this, mCompanyId, mUid, filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {

                    SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    if (zhiChiMessage.getData() != null) {
                        SobotFileModel item = new SobotFileModel();
                        item.setFileUrl(zhiChiMessage.getData().getUrl());
                        item.setFileLocalPath(filePath);
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
                        item.setFileName(fileName);
                        item.setFileType(fileType);
                        addPicView(item);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
                    showHint(TextUtils.isEmpty(des) ? getResources().getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            SobotDialogUtils.stopProgressDialog(SobotReplyActivity.this);
        }
    };


}