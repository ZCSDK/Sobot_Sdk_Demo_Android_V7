package com.sobot.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SatisfactionSet;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.api.model.SobotEvaluateModel;
import com.sobot.chat.api.model.SobotLocationModel;
import com.sobot.chat.api.model.SobotMsgCenterModel;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.SobotQuestionRecommend;
import com.sobot.chat.api.model.SobotTicketStatus;
import com.sobot.chat.api.model.SobotTypeModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiMessageLocationModel;
import com.sobot.chat.api.model.ZhiChiMessageMsgModel;
import com.sobot.chat.api.model.ZhiChiMessageObjectModel;
import com.sobot.chat.api.model.ZhiChiPushMessage;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.core.channel.Const;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.gson.SobotGsonUtil;
import com.sobot.chat.notchlib.utils.RomUtils;
import com.sobot.chat.server.SobotSessionServer;
import com.sobot.chat.widget.dialog.SobotDialogUtils;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.pictureframe.SobotBitmapUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {

    public static final int REQUEST_CODE_CAMERA = 108;
    public static final String INTENT_KEY_UID = "intent_key_uid";
    public static final String INTENT_KEY_COMPANYID = "intent_key_companyid";
    public static final String INTENT_KEY_TICKET_ID = "intent_key_ticket_id";
    public static final String SOBOT_ACTION_CLOSE_TIKET = "sobot_action_close_tiket";
    private static List<SobotTicketStatus> statusList;//工单状态集合
    private static List<SobotTypeModel> typeList;//工单分类集合

    public static void setStatusList(List<SobotTicketStatus> statusList) {
        ChatUtils.statusList = statusList;
    }

    public static List<SobotTicketStatus> getStatusList() {
        return statusList;
    }

    public static List<SobotTypeModel> getTypeList() {
        return typeList;
    }

    public static void setTypeList(List<SobotTypeModel> typeList) {
        ChatUtils.typeList = typeList;
    }

    /**
     * activity打开选择图片界面
     *
     * @param act
     */
    public static void openSelectPic(Activity act) {
        openSelectPic(act, null);
    }

    /**
     * Fragment打开选择图片界面
     *
     * @param act
     */
    public static void openSelectPic(Activity act, Fragment childFragment) {
        if (act == null) {
            return;
        }
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        try {
            if (childFragment != null) {
                childFragment.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
            } else {
                act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
            }
        } catch (Exception e) {
            e.printStackTrace();
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            try {
                if (childFragment != null) {
                    childFragment.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
                } else {
                    act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                ToastUtil.showToast(act.getApplicationContext(), ResourceUtils.getResString(act, "sobot_not_open_album"));
            }
        }
    }

    /**
     * 打开选择视频界面
     *
     * @param act
     */
    public static void openSelectVedio(Activity act) {
        if (act == null) {
            return;
        }
        Intent intent;
        if (Build.VERSION.SDK_INT < 27 || RomUtils.isOppo() || RomUtils.isOnePlus() || RomUtils.isVivo()) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        }
        try {
            act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
        } catch (Exception e) {
            e.printStackTrace();
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            try {
                act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
            } catch (Exception exception) {
                exception.printStackTrace();
                ToastUtil.showToast(act.getApplicationContext(), ResourceUtils.getResString(act, "sobot_not_open_album"));
            }
        }
    }

    /**
     * Fragment打开选择视频界面
     *
     * @param act
     */
    public static void openSelectVedio(Activity act, Fragment childFragment) {
        if (act == null) {
            return;
        }
        Intent intent;
        if (Build.VERSION.SDK_INT < 27 || RomUtils.isOppo() || RomUtils.isOnePlus() || RomUtils.isVivo()) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        }
        try {
            if (childFragment != null) {
                childFragment.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
            } else {
                act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
            }
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            try {
                if (childFragment != null) {
                    childFragment.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
                } else {
                    act.startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                ToastUtil.showToast(act.getApplicationContext(), ResourceUtils.getResString(act, "sobot_not_open_album"));
            }
        }
    }

    public static void sendPicByUri(Context context, int readFlag, Handler handler,
                                    Uri selectedImage, ZhiChiInitModeBase initModel,
                                    final SobotMsgAdapter messageAdapter, boolean isCamera, int currentModel, Information info) {
        if (initModel == null) {
            return;
        }
        String picturePath = ImageUtils.getPath(context, selectedImage);
        LogUtils.i("picturePath:" + picturePath);
        if (!TextUtils.isEmpty(picturePath)) {
            File tmpFile = new File(picturePath);
            if (tmpFile.exists() && tmpFile.isFile()) {
                sendPicLimitBySize(readFlag, picturePath, initModel.getCid(),
                        initModel.getPartnerid(), handler, context, messageAdapter, isCamera, currentModel, initModel, info);
            }
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                ToastUtil.showToast(context.getApplicationContext(), ResourceUtils.getResString(context, "sobot_not_find_pic"));
                return;
            }
            sendPicLimitBySize(readFlag, file.getAbsolutePath(),
                    initModel.getCid(), initModel.getPartnerid(), handler, context, messageAdapter, isCamera, currentModel, initModel, info);
        }
    }

    public static void sendPicLimitBySize(int readFlag, String filePath, String cid, String uid,
                                          Handler handler, Context context,
                                          final SobotMsgAdapter messageAdapter, boolean isCamera, int currentModel, ZhiChiInitModeBase initModel, Information info) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Bitmap bitmap = SobotBitmapUtil.compress(filePath, context, isCamera);
            if (bitmap != null) {
                //判断图片是否有旋转，有的话旋转后在发送（手机出现选择图库相片发送后和原生的图片方向不一致）
                try {
                    int degree = ImageUtils.readPictureDegree(filePath);
                    if (degree > 0) {
                        bitmap = ImageUtils.rotateBitmap(bitmap, degree);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!(filePath.endsWith(".gif") || filePath.endsWith(".GIF"))) {
                    String picDir = SobotPathManager.getInstance().getPicDir();
                    IOUtils.createFolder(picDir);
                    String fName = MD5Util.encode(filePath);
                    filePath = picDir + fName + "_tmp.jpg";
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(filePath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        String fName = MD5Util.encode(filePath);
                        Uri uri = ImageUtils.getImageContentUri(context, filePath);
                        filePath = FileUtil.saveImageFile(context, uri, fName + FileUtil.getFileEndWith(filePath), filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                long size = CommonUtils.getFileSize(filePath);
                if (size < (20 * 1024 * 1024)) {
                    String id = cid + System.currentTimeMillis() + "";
                    sendImageMessageToHandler(readFlag, filePath, handler, id, info);
                    sendPicture(context, readFlag, cid, uid, filePath, handler, id,
                            messageAdapter, currentModel, initModel);
                } else {
                    ToastUtil.showToast(context.getApplicationContext(), ResourceUtils.getResString(context, "sobot_file_lt_8M"));
                }
            } else {
                ToastUtil.showToast(context.getApplicationContext(), ResourceUtils.getResString(context, "sobot_pic_type_error"));
            }
        } else {
            if (!TextUtils.isEmpty(filePath)) {
                long size = CommonUtils.getFileSize(filePath);
                if (size < (20 * 1024 * 1024)) {
                    String id = cid + System.currentTimeMillis() + "";
                    sendImageMessageToHandler(readFlag, filePath, handler, id, info);
                    sendPicture(context, readFlag, cid, uid, filePath, handler, id,
                            messageAdapter, currentModel, initModel);
                } else {
                    ToastUtil.showToast(context.getApplicationContext(), ResourceUtils.getResString(context, "sobot_file_lt_8M"));
                }
            } else {
                ToastUtil.showToast(context.getApplicationContext(), ResourceUtils.getResString(context, "sobot_pic_type_error"));
            }
        }
    }


    // 图片通知
    public static void sendImageMessageToHandler(int readFlag, String imageUrl,
                                                 Handler handler, String id, Information information) {
        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(imageUrl);
        reply.setMsgType(ZhiChiConstant.message_type_pic);
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setId(id);
        zhichiMessage.setMsgId(id);
        if (readFlag == 1) {
            zhichiMessage.setReadStatus(1);
        }
        zhichiMessage.setT(System.currentTimeMillis() + "");
        zhichiMessage.setSendSuccessState(ZhiChiConstant.MSG_SEND_STATUS_LOADING);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (information != null) {
            zhichiMessage.setSenderName(information.getUser_nick());
            zhichiMessage.setSenderFace(information.getFace());
        }
        Message message = new Message();
        message.what = ZhiChiConstant.hander_send_msg;
        message.obj = zhichiMessage;
        handler.sendMessage(message);
    }

    public static void sendPicture(final Context context, final int readFlag, String cid, String uid,
                                   final String filePath, final Handler handler, final String msgId, final SobotMsgAdapter messageAdapter, int currentModel, final ZhiChiInitModeBase initModel) {
        SobotMsgManager.getInstance(context).getZhiChiApi().sendFile(readFlag, msgId, cid, uid, filePath, "", currentModel, new ResultCallBack<ZhiChiMessage>() {
            @Override
            public void onSuccess(ZhiChiMessage zhiChiMessage) {
                if (ZhiChiConstant.result_success_code == Integer
                        .parseInt(zhiChiMessage.getCode())) {
                    if (1 == Integer
                            .parseInt(zhiChiMessage.getData().getStatus())) {
                        if (msgId != null) {
                            if (initModel != null && initModel.getMsgAppointFlag() == 1) {
                                //引用开启
                                ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
                                messageMsgModel.setMsgType(ZhiChiConstant.message_type_pic);
                                messageMsgModel.setContent(zhiChiMessage.getData().getUrl());
                                zhiChiMessage.getData().setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
                            }
                            //设置已读未读状态
                            zhiChiMessage.getData().setReadStatus(readFlag);
                            Message message = handler.obtainMessage();
                            message.what = ZhiChiConstant.hander_sendPicStatus_success;
                            zhiChiMessage.getData().setMsgId(msgId);
                            message.obj = zhiChiMessage;
                            handler.sendMessage(message);
                        }
                    } else {
                        if (msgId != null) {
                            Message message = handler.obtainMessage();
                            message.what = ZhiChiConstant.hander_sendPicStatus_fail;
                            message.obj = msgId;
                            handler.sendMessage(message);
                        }
                        if (!TextUtils.isEmpty(zhiChiMessage.getMsg())) {
                            ToastUtil.showToast(context, zhiChiMessage.getMsg());
                        }
                    }
                } else {
                    if (msgId != null) {
                        Message message = handler.obtainMessage();
                        message.what = ZhiChiConstant.hander_sendPicStatus_fail;
                        message.obj = msgId;
                        handler.sendMessage(message);
                    }
                    if (!TextUtils.isEmpty(zhiChiMessage.getMsg())) {
                        ToastUtil.showToast(context, zhiChiMessage.getMsg());
                    }
                }
            }

            @Override
            public void onLoading(long total, long current,
                                  boolean isUploading) {
                LogUtils.i("发送图片 进度:" + current);
                if (msgId != null) {
                    int position = messageAdapter.getMsgInfoPosition(msgId);
                    LogUtils.i("发送图片 position:" + position);
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                LogUtils.i("发送图片error:" + des + "exception:" + e);
                if (msgId != null) {
                    Message message = handler.obtainMessage();
                    message.what = ZhiChiConstant.hander_sendPicStatus_fail;
                    message.obj = msgId;
                    handler.sendMessage(message);
                }
            }
        });
    }

    public static String getMessageContentByOutLineType(Context context, ZhiChiInitModeBase
            initModel, int type) {
        Resources resources = context.getResources();
        if (1 == type || 2 == type) {// 1:是客服下线导致的用户离线，2:是客服主动把用户离线了
            return initModel.isServiceEndPushFlag() && !TextUtils.isEmpty(initModel.getServiceEndPushMsg()) ? initModel.getServiceEndPushMsg() : ResourceUtils.getResString(context, "sobot_outline_closed");//ResourceUtils.getResString(context,"sobot_outline_leverByManager");
        } else if (3 == type) { // 被加入黑名单
            return ResourceUtils.getResString(context, "sobot_outline_leverByManager");
        } else if (4 == type) { // 超时下线
            return initModel != null ? initModel.getUserOutWord() : ResourceUtils.getResString(context, "sobot_outline_leverByManager");
        } else if (5 == type) {
            return ResourceUtils.getResString(context, "sobot_outline_leverByManager");
        } else if (6 == type) {
            return ResourceUtils.getResString(context, "sobot_outline_openNewWindows");
        } else if (99 == type) {
            return ResourceUtils.getResString(context, "sobot_outline_leavemsg");
        } else if (9 == type) {
            return ResourceUtils.getResString(context, "sobot_line_up_close_chat");
        }
        return ResourceUtils.getResString(context, "sobot_outline_leverByManager");
    }

    public static ZhiChiMessageBase getUnreadMode(Context context) {
        ZhiChiMessageBase msgBase = new ZhiChiMessageBase();
        msgBase.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsg(ResourceUtils.getResString(context, "sobot_no_read"));
        answer.setRemindType(ZhiChiConstant.sobot_remind_type_below_unread);
        msgBase.setAnswer(answer);
        return msgBase;
    }

    /**
     * 获取客服邀请的mode
     *
     * @param pushMessage 推送的信息
     * @return
     */
    public static ZhiChiMessageBase getCustomEvaluateMode(Context context, ZhiChiPushMessage pushMessage, SatisfactionSet satisfactionSet) {
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setT(System.currentTimeMillis() + "");
        base.setSenderName(TextUtils.isEmpty(pushMessage.getAname()) ? ResourceUtils.getResString(context, "sobot_cus_service") : pushMessage.getAname());
        SobotEvaluateModel sobotEvaluateModel = new SobotEvaluateModel();
        sobotEvaluateModel.setIsQuestionFlag(pushMessage.getIsQuestionFlag());
        base.setSobotEvaluateModel(sobotEvaluateModel);
        base.setSatisfactionSet(satisfactionSet);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        base.setSenderType(ZhiChiConstant.message_sender_type_custom_evaluate);
        base.setAction(ZhiChiConstant.action_custom_evaluate);
        base.setAnswer(reply);
        return base;
    }

    public static ZhiChiMessageBase getUploadFileModel(Context context, int readFlag, String tmpMsgId, File selectedFile, Information information) {
        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        SobotCacheFile cacheFile = new SobotCacheFile();
        cacheFile.setMsgId(tmpMsgId);
        cacheFile.setFilePath(selectedFile.getAbsolutePath());
        cacheFile.setFileName(selectedFile.getName());
        cacheFile.setFileType(ChatUtils.getFileType(selectedFile));
        cacheFile.setFileSize(Formatter.formatFileSize(context, selectedFile.length()));
        reply.setCacheFile(cacheFile);
        zhichiMessage.setMsgId(tmpMsgId);
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setId(tmpMsgId);
        if (readFlag == 1) {
            zhichiMessage.setReadStatus(1);
        }
        zhichiMessage.setT(System.currentTimeMillis() + "");
        reply.setMsgType(ZhiChiConstant.message_type_file);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (information != null) {
            zhichiMessage.setSenderName(information.getUser_nick());
            zhichiMessage.setSenderFace(information.getFace());
        }
        return zhichiMessage;
    }

    public static ZhiChiMessageBase getLocationModel(int readFlag, String tmpMsgId, SobotLocationModel data, Information information, ZhiChiInitModeBase initModel) {
        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setLocationData(data);
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setId(tmpMsgId);
        zhichiMessage.setMsgId(tmpMsgId);
        if (readFlag == 1) {
            zhichiMessage.setReadStatus(1);
        }
        zhichiMessage.setT(System.currentTimeMillis() + "");
        reply.setMsgType(ZhiChiConstant.message_type_location);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (information != null) {
            zhichiMessage.setSenderName(information.getUser_nick());
            zhichiMessage.setSenderFace(information.getFace());
        }
        if (initModel != null && initModel.getMsgAppointFlag() == 1) {
            //引用开启
            ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
            messageMsgModel.setMsgType(5);
            ZhiChiMessageObjectModel objectModel = new ZhiChiMessageObjectModel();
            objectModel.setType(2);
            ZhiChiMessageLocationModel locationModel = new ZhiChiMessageLocationModel();
            locationModel.setDesc(data.getLocalLabel());
            locationModel.setLat(data.getLat());
            locationModel.setLng(data.getLng());
            locationModel.setTitle(data.getLocalName());
            locationModel.setPicUrl(data.getSnapshot());
            objectModel.setMsg(locationModel);
            messageMsgModel.setContent(objectModel);
            zhichiMessage.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
        }
        return zhichiMessage;
    }

    public static HashMap<String, Object> getSendAiCardParameter(String btnText, SobotChatCustomGoods goods, SobotChatCustomCard card) {
        HashMap<String, Object> parame = new HashMap<>();
        try {
            JSONArray questionArray = new JSONArray();
            card.setCardStyle(0);//代表时客户发的
            if (goods != null && goods.getParamInfos() != null) {
                for (int i = 0; i < goods.getParamInfos().size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("nodeId", card.getNodeId());
                    object.put("processId", card.getProcessId());
                    object.put("variableId", goods.getParamInfos().get(i).getVariableId());
                    object.put("variableValue", goods.getParamInfos().get(i).getParamValue());
                    object.put("customCardButtonName", btnText);
                    questionArray.put(object);
                }
                List<SobotChatCustomGoods> list = new ArrayList<>();
                list.add(goods);
                card.setCustomCards(list);
            } else {
                JSONObject object = new JSONObject();
                object.put("nodeId", card.getNodeId());
                object.put("processId", card.getProcessId());
                object.put("variableId", "");
                object.put("variableValue", "");
                object.put("customCardButtonName", btnText);
                questionArray.put(object);
            }
            String cardOriginalInfo = card.getOriginalInfo();
            if (goods != null && StringUtils.isNoEmpty(cardOriginalInfo)) {
                JSONObject object = new JSONObject(cardOriginalInfo);
                JSONArray array = object.getJSONArray("customCards");
                array.put(new JSONObject(goods.getOriginalString()));
                object.remove("customCards");
                object.put("customCards", array);
                cardOriginalInfo = SobotJsonUtils.object2Json(object);
            }
            parame.put("question", SobotJsonUtils.object2Json(questionArray));//String 点击卡片的节点
            parame.put("showQuestion", cardOriginalInfo);//String 点击卡片的所有属性
            parame.put("inputTypeEnum", "PROCESS_CARD_CLICK");
            if (StringUtils.isNoEmpty(card.getInterfaceInfo())) {
                parame.put("interfaceInfo", SobotGsonUtil.jsonToMaps(card.getInterfaceInfo()));//全量的对象
            } else {
                parame.put("interfaceInfo", "");//全量的对象
            }
            parame.put("objMsgType", "21");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parame;
    }

    public static ZhiChiMessageBase getCustomerCard(int readFlag, String msgId, SobotChatCustomCard card, Information information, ZhiChiInitModeBase initModel) {
        ZhiChiMessageBase messageBase = new ZhiChiMessageBase();
        messageBase.setMsgId(msgId);
        messageBase.setId(msgId);
        if (information != null) {
            messageBase.setSenderFace(information.getFace());
            messageBase.setSenderName(information.getUser_name());
        }
        if (initModel != null && initModel.getMsgAppointFlag() == 1) {
            //引用开启
            ZhiChiMessageMsgModel messageMsgModel = new ZhiChiMessageMsgModel();
            messageMsgModel.setMsgType(5);
            ZhiChiMessageObjectModel objectModel = new ZhiChiMessageObjectModel();
            objectModel.setType(21);
            objectModel.setMsg(card);
            messageMsgModel.setContent(objectModel);
            messageBase.setMessage(SobotGsonUtil.beanToJson(messageMsgModel));
        }
        if (readFlag == 1) {
            messageBase.setReadStatus(1);
        }
        messageBase.setSenderType(ZhiChiConstant.message_sender_type_customer);
        messageBase.setCustomCard(card);
        messageBase.setT(System.currentTimeMillis() + "");
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsgType(ZhiChiConstant.message_type_card_msg);
        messageBase.setAnswer(reply);
        return messageBase;
    }

    public static ZhiChiMessageBase getMuitidiaLeaveMsgModel(String tmpMsgId, String data, Information information) {
        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(data);
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setId(tmpMsgId);
        zhichiMessage.setT(System.currentTimeMillis() + "");
        reply.setMsgType(ZhiChiConstant.message_type_muiti_leave_msg);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (information != null) {
            zhichiMessage.setSenderName(information.getUser_nick());
            zhichiMessage.setSenderFace(information.getFace());
        }
        return zhichiMessage;
    }

    public static ZhiChiMessageBase getUploadVideoModel(Context context, int readFlag, String tmpMsgId, File selectedFile, String snapshot, Information information) {
        ZhiChiMessageBase zhichiMessage = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        SobotCacheFile cacheFile = new SobotCacheFile();
        cacheFile.setMsgId(tmpMsgId);
        cacheFile.setFilePath(selectedFile.getAbsolutePath());
        cacheFile.setFileName(selectedFile.getName());
        cacheFile.setSnapshot(snapshot);
        cacheFile.setFileType(ChatUtils.getFileType(selectedFile));
        cacheFile.setFileSize(Formatter.formatFileSize(context, selectedFile.length()));
        reply.setCacheFile(cacheFile);
        zhichiMessage.setAnswer(reply);
        zhichiMessage.setId(tmpMsgId);
        zhichiMessage.setMsgId(tmpMsgId);
        if (readFlag == 1) {
            zhichiMessage.setReadStatus(1);
        }
        zhichiMessage.setT(System.currentTimeMillis() + "");
        reply.setMsgType(ZhiChiConstant.message_type_video);
        zhichiMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (information != null) {
            zhichiMessage.setSenderName(information.getUser_nick());
            zhichiMessage.setSenderFace(information.getFace());
        }
        return zhichiMessage;
    }


    /**
     * 机器人自动转人工提示语
     *
     * @return
     */
    public static ZhiChiMessageBase getRobotTransferTip(ZhiChiInitModeBase initModel) {
        ZhiChiMessageBase robot = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(initModel.getTransferManualPromptWord());
        reply.setMsgType(ZhiChiConstant.message_type_text);
        robot.setT(System.currentTimeMillis() + "");
        robot.setAnswer(reply);
        robot.setSenderFace(initModel.getRobotLogo());
        robot.setSender(initModel.getRobotName());
        robot.setSenderType(ZhiChiConstant.message_sender_type_robot);
        robot.setSenderName(initModel.getRobotName());
        return robot;
    }

    /**
     * 获取非置顶通告model
     *
     * @param context
     * @param initModel
     * @return
     */
    public static ZhiChiMessageBase getNoticeModel(Context context, ZhiChiInitModeBase initModel) {
        ZhiChiMessageBase robot = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        String announceMsg = initModel.getAnnounceMsg();
        reply.setMsg(announceMsg);
        reply.setMsgType(ZhiChiConstant.message_type_text);
        robot.setT(System.currentTimeMillis() + "");
        robot.setAnswer(reply);
        robot.setSenderType(ZhiChiConstant.message_sender_type_notice);
        return robot;
    }

    /**
     * 保存一些配置项
     *
     * @param context
     * @param info
     */
    public static void saveOptionSet(Context context, Information info) {
        if (TextUtils.isEmpty(info.getPartnerid())) {
            info.setEquipmentId(CommonUtils.getPartnerId(context));
        }
    }


    /**
     * 初始化时检查一下传入参数是否发生变化，是否需要重新进行初始化
     *
     * @param context
     * @param info
     * @return
     */
    public static boolean checkConfigChange(Context context, String appkey, final Information info) {
        String last_current_appkey = SharedPreferencesUtil.getOnlyStringData(context, ZhiChiConstant.sobot_last_current_appkey, "");
        if (!TextUtils.isEmpty(last_current_appkey) || !TextUtils.isEmpty(info.getApp_key())) {
            if (!last_current_appkey.equals(info.getApp_key())) {
                SharedPreferencesUtil.removeKey(context, ZhiChiConstant.sobot_last_login_group_id);
                LogUtils.i("appkey发生了变化，重新初始化..............");
                return true;
            }
        }
        String sobot_last_current_customer_code = SharedPreferencesUtil.getOnlyStringData
                (context, ZhiChiConstant.sobot_last_current_customer_code, "");
        String last_current_partnerId = SharedPreferencesUtil.getStringData
                (context, appkey + "_" + ZhiChiConstant.sobot_last_current_partnerId, "");
        String last_current_dreceptionistId = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.SOBOT_RECEPTIONISTID, "");
        String last_current_robot_code = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.SOBOT_ROBOT_CODE, "");
        String last_current_remark = SharedPreferencesUtil.getStringData
                (context, appkey + "_" + ZhiChiConstant.sobot_last_current_remark, "");
        String last_current_groupid = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_groupid, "");
        int last_current_service_mode = SharedPreferencesUtil.getIntData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_service_mode, -1);
        String last_current_customer_fields = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_customer_fields, "");
        String last_current_params = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_params, "");
        String sobot_last_current_isvip = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_isvip, "");
        String sobot_last_current_vip_level = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_vip_level, "");
        String sobot_last_current_user_label = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_user_label, "");
        String sobot_last_current_robot_alias = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_robot_alias, "");
        String sobot_last_current_languae = SharedPreferencesUtil.getStringData(
                context, appkey + "_" + ZhiChiConstant.sobot_last_current_languae, "");
        // appkey，技能组、用户id，客服id，对接机器人编号、接入模式，自定义字段，自定义固定KEY字段 ，userRemark,isvip,vip级别，用户标签，机器人别名,商户id,语言
        //判断上次uid是否跟此次传入的一样
        if (!last_current_partnerId.equals(info.getPartnerid() == null ? "" : info.getPartnerid())) {
            LogUtils.i("partnerid发生了变化，重新初始化..............");
            return true;
        } else if (!last_current_dreceptionistId.equals(info.getChoose_adminid() == null ? "" : info.getChoose_adminid())) {
            LogUtils.i("转入的指定客服发生了变化，重新初始化..............");
            return true;
        } else if (!last_current_robot_code.equals(info.getRobotCode() == null ? "" : info.getRobotCode())) {
            LogUtils.i("指定机器人发生变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_robot_alias.equals(info.getRobot_alias() == null ? "" : info.getRobot_alias())) {
            LogUtils.i("指定机器人别名发生变化，重新初始化..............");
            return true;
        } else if (!last_current_remark.equals(info.getRemark() == null ? "" : info.getRemark())) {
            LogUtils.i("备注发生变化，重新初始化..............");
            return true;
        } else if (!last_current_groupid.equals(info.getGroupid() == null ? "" : info.getGroupid())) {
            LogUtils.i("技能组发生变化，重新初始化..............");
            return true;
        } else if (last_current_service_mode != info.getService_mode()) {
            LogUtils.i("接入模式发生变化，重新初始化..............");
            return true;
        } else if (!last_current_customer_fields.equals(info.getCustomer_fields() == null ? "" : info.getCustomer_fields())) {
            LogUtils.i("自定义字段发生变化，重新初始化..............");
            return true;
        } else if (!last_current_params.equals(info.getParams() == null ? "" : info.getParams())) {
            LogUtils.i("自定义资料发生变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_isvip.equals(info.getIsVip() == null ? "" : info.getIsVip())) {
            LogUtils.i("是否vip发生变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_vip_level.equals(info.getVip_level() == null ? "" : info.getVip_level())) {
            LogUtils.i("vip级别发生变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_user_label.equals(info.getUser_label() == null ? "" : info.getUser_label())) {
            LogUtils.i("用户标签发生变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_customer_code.equals(info.getCustomer_code() == null ? "" : info.getCustomer_code())) {
            LogUtils.i("商户id customer_code发生了变化，重新初始化..............");
            return true;
        } else if (!sobot_last_current_languae.equals(info.getLocale() == null ? "" : info.getLocale())) {
            LogUtils.i("语言发生了变化，重新初始化..............");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据当前cid的位置获取cid
     *
     * @return
     */
    public static String getCurrentCid(ZhiChiInitModeBase initModel, List<String> cids, int currentCidPosition) {
        if (initModel != null) {
            String currentCid = initModel.getCid();
            if (currentCidPosition > 0) {
                if (currentCidPosition > cids.size() - 1) {
                    currentCid = "-1";
                } else {
                    currentCid = cids.get(currentCidPosition);
                }
            }
            return currentCid;
        } else {
            return "-1";
        }
    }


    /**
     * 获取被xx客服接入的提醒对象
     *
     * @param context
     * @param aname
     * @return
     */
    public static ZhiChiMessageBase getServiceAcceptTip(Context context, String tips, String aname, String face) {
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        base.setAction(ZhiChiConstant.action_remind_connt_success);
        base.setT(System.currentTimeMillis() + "");
        base.setSenderName(TextUtils.isEmpty(aname) ? "" : aname);
        base.setSenderFace(TextUtils.isEmpty(face) ? "" : face);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(tips.replace("#客服昵称#", aname).replace("#Customer nickname#", aname));
        reply.setRemindType(ZhiChiConstant.sobot_remind_type_accept_request);
        base.setAnswer(reply);
        return base;
    }

    /**
     * 获取富文本消息（人工欢迎语、待分配说辞）
     */
    public static ZhiChiMessageBase getRichListMsg(String msg, List<ChatMessageRichListModel> richListModels, String aname, String aface) {
        if (richListModels != null && !richListModels.isEmpty()) {
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setT(System.currentTimeMillis() + "");
            base.setSenderName(TextUtils.isEmpty(aname) ? "" : aname);
            base.setSenderFace(TextUtils.isEmpty(aface) ? "" : aface);
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            reply.setMsgType(ZhiChiConstant.message_type_emoji);
            base.setSenderType(ZhiChiConstant.message_sender_type_service);
            reply.setMsg(msg);
            reply.setRichList(richListModels);
            base.setAnswer(reply);
            return base;
        } else {
            return null;
        }
    }

    /**
     * 获取人工提示语的对象
     *
     * @param aname   客服名称
     * @param aface   客服头像
     * @param content 欢迎语内容
     * @return
     */
    public static ZhiChiMessageBase getServiceHelloTip(String aname, String aface, String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setAction(ZhiChiConstant.action_remind_connt_success);
        base.setT(System.currentTimeMillis() + "");
        base.setSenderName(TextUtils.isEmpty(aname) ? "" : aname);
        base.setSenderFace(TextUtils.isEmpty(aface) ? "" : aface);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsgType(ZhiChiConstant.message_type_text);
        base.setSenderType(ZhiChiConstant.message_sender_type_service);
        reply.setMsg(content);
        base.setAnswer(reply);
        return base;
    }

    /**
     * 获取大模型转人工提示语的对象
     *
     * @param aname   客服名称
     * @param aface   客服头像
     * @param content 提示语内容
     * @return
     */
    public static ZhiChiMessageBase getAIAgentTransferTip(String aname, String aface, String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setT(System.currentTimeMillis() + "");
        base.setSenderName(TextUtils.isEmpty(aname) ? "" : aname);
        base.setSenderFace(TextUtils.isEmpty(aface) ? "" : aface);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsgType(ZhiChiConstant.message_type_text);
        //归为机器人 不然和人工原因是同一种类型导致人工欢迎不显示头像昵称（1分钟内sendtype一样）
        base.setSenderType(ZhiChiConstant.message_sender_type_robot);
        reply.setMsg(content);
        base.setAnswer(reply);
        return base;
    }

    /**
     * 获取离线留言消息对象
     *
     * @param content
     * @return
     */
    public static ZhiChiMessageBase getLeaveMsgTip(String content) {
        ZhiChiMessageBase tmpMsg = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(content);
        reply.setMsgType(ZhiChiConstant.message_type_text);
        tmpMsg.setAnswer(reply);
        tmpMsg.setLeaveMsgFlag(true);
        tmpMsg.setSenderType(ZhiChiConstant.message_sender_type_customer);
        tmpMsg.setT(System.currentTimeMillis() + "");
        return tmpMsg;
    }

    /**
     * @return
     */
    public static ZhiChiMessageBase getInLineHint(String queueDoc) {
        ZhiChiMessageBase paiduizhichiMessageBase = new ZhiChiMessageBase();
        paiduizhichiMessageBase.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        paiduizhichiMessageBase.setAction(ZhiChiConstant.action_remind_info_paidui);
        paiduizhichiMessageBase.setT(System.currentTimeMillis() + "");
        ZhiChiReplyAnswer reply_paidui = new ZhiChiReplyAnswer();
        reply_paidui.setMsg(queueDoc);
        reply_paidui.setRemindType(ZhiChiConstant.sobot_remind_type_paidui_status);
        paiduizhichiMessageBase.setAnswer(reply_paidui);
        return paiduizhichiMessageBase;
    }

    /**
     * 判断是否评价完毕就释放会话
     *
     * @param context
     * @param isComment
     * @param current_client_model
     * @return
     */
    public static boolean isEvaluationCompletedExit(Context context, boolean isComment, int current_client_model) {

        boolean evaluationCompletedExit = SharedPreferencesUtil.getBooleanData
                (context, ZhiChiConstant.SOBOT_CHAT_EVALUATION_COMPLETED_EXIT, false);
        if (evaluationCompletedExit && isComment && current_client_model == ZhiChiConstant.client_model_customService) {
            return true;
        }
        return false;
    }

    /**
     * 退出登录
     *
     * @param context
     * @param reason  手动结束会话的原因，非必填
     */
    public static void userLogout(Context context, String reason) {

        SharedPreferencesUtil.saveBooleanData(context, ZhiChiConstant.SOBOT_IS_EXIT, true);
        try {
            //断开通道
            ZCSobotApi.closeIMConnection(context);
            context.stopService(new Intent(context, SobotSessionServer.class));
            String cid = SharedPreferencesUtil.getStringData(context, Const.SOBOT_CID, "");
            String uid = SharedPreferencesUtil.getStringData(context, Const.SOBOT_UID, "");
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_WSLINKBAK);
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_WSLINKDEFAULT);
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_UID);
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_CID);
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_PUID);
            SharedPreferencesUtil.removeKey(context, Const.SOBOT_APPKEY);

            if (!TextUtils.isEmpty(cid) && !TextUtils.isEmpty(uid)) {
                ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(context).getZhiChiApi();
                zhiChiApi.out(cid, uid, reason, new StringResultCallBack<CommonModel>() {
                    @Override
                    public void onSuccess(CommonModel result) {
                        LogUtils.i("下线成功");
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断机器人引导转人工是否勾选
     *
     * @param manualType 机器人引导转人工 勾选为1，默认为0 固定位置，比如1,1,1,1,1=直接回答勾选，理解回答勾选，引导回答勾选，未知回答勾选,特殊消息转人工配置开关
     * @param msg        answerType (1,9,11,12,14)都为直接回答类型（2.8.3之后逻辑，都判断是否显示转人工按钮）
     * @return true表示勾选上了
     */
    public static boolean checkManualType(String manualType, ZhiChiMessageBase msg) {
        if (TextUtils.isEmpty(manualType) || TextUtils.isEmpty(msg.getAnswerType())) {
            return false;
        }
        try {
            Integer type = Integer.valueOf(msg.getAnswerType());
            String[] mulArr = manualType.split(",");
            if (msg.getSpecialMsgFlag() == 1) {
                if (mulArr.length >= 5 && "1".equals(mulArr[4])) {
                    return true;
                }
            } else if ((type == 1 && "1".equals(mulArr[0])) || (type == 9 && "1".equals(mulArr[0])) || (type == 11 && "1".equals(mulArr[0])) || (type == 12 && "1".equals(mulArr[0])) || (type == 14 && "1".equals(mulArr[0])) || (type == 2 && "1".equals(mulArr[1]))
                    || (type == 4 && "1".equals(mulArr[2])) || (type == 3 && "1".equals(mulArr[3]))) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void sendPicByFilePath(Context context, String filePath, SobotSendFileListener listener, boolean isCamera) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Bitmap bitmap = SobotBitmapUtil.compress(filePath, context, isCamera);
            if (bitmap != null) {
                //判断图片是否有旋转，有的话旋转后在发送（手机出现选择图库相片发送后和原生的图片方向不一致）
                try {
                    int degree = ImageUtils.readPictureDegree(filePath);
                    if (degree > 0) {
                        bitmap = ImageUtils.rotateBitmap(bitmap, degree);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!(filePath.endsWith(".gif") || filePath.endsWith(".GIF"))) {
                    String picDir = SobotPathManager.getInstance().getPicDir();
                    IOUtils.createFolder(picDir);
                    String fName = MD5Util.encode(filePath);
                    filePath = picDir + fName + "_tmp.jpg";
                    FileOutputStream fos;
                    try {
                        fos = new FileOutputStream(filePath);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_pic_type_error"));
                        listener.onError();
                        return;
                    }
                } else {
                    try {
                        String fName = MD5Util.encode(filePath);
                        Uri uri = ImageUtils.getImageContentUri(context, filePath);
                        filePath = FileUtil.saveImageFile(context, uri, fName + FileUtil.getFileEndWith(filePath), filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_pic_type_error"));
                        listener.onError();
                        return;
                    }
                }
                long size = CommonUtils.getFileSize(filePath);
                if (size < (50 * 1024 * 1024)) {
                    listener.onSuccess(filePath);
                } else {
                    ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_file_upload_failed"));
                    listener.onError();
                }
            } else {
                ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_pic_type_error"));
                listener.onError();
            }
        } else {
            if (!TextUtils.isEmpty(filePath)) {
                long size = CommonUtils.getFileSize(filePath);
                if (size < (50 * 1024 * 1024)) {
                    listener.onSuccess(filePath);
                } else {
                    ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_file_upload_failed"));
                    listener.onError();
                }
            } else {
                ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_pic_type_error"));
                listener.onError();
            }
        }
    }

    public static void sendPicByUriPost(Context context, Uri selectedImage, SobotSendFileListener listener, boolean isCamera) {
        String picturePath = ImageUtils.getPath(context, selectedImage);
        if (!TextUtils.isEmpty(picturePath)) {
            sendPicByFilePath(context, picturePath, listener, isCamera);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                SobotDialogUtils.stopProgressDialog(context);
                ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_not_find_pic"));
                return;
            }
            sendPicByFilePath(context, picturePath, listener, isCamera);
        }
    }

    public static void sendPicByFilePost(Context context, File selectedFile, SobotSendFileListener listener) {
        if (!selectedFile.exists()) {
            SobotDialogUtils.stopProgressDialog(context);
            ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_not_find_pic"));
            return;
        }
        if (!TextUtils.isEmpty(selectedFile.getPath())) {
            long size = CommonUtils.getFileSize(selectedFile.getPath());
            if (size < (50 * 1024 * 1024)) {
                listener.onSuccess(selectedFile.getPath());
            } else {
                ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_file_upload_failed"));
                listener.onError();
            }
        } else {
            ToastUtil.showToast(context, ResourceUtils.getResString(context, "sobot_pic_type_error"));
            listener.onError();
        }
    }

    public interface SobotSendFileListener {
        void onSuccess(String filePath);

        void onError();
    }

    /**
     * 检查是否开启   是否已解决配置
     *
     * @return
     */
    public static boolean isQuestionFlag(SobotEvaluateModel evaluateModel) {
        if (evaluateModel != null) {
            return (evaluateModel.getIsQuestionFlag() == 1);
        }
        return false;
    }

    /**
     * 保存消息列表
     *
     * @param context
     * @param info
     * @param appkey
     * @param initModel
     * @param messageList
     */
    public static void saveLastMsgInfo(Context context, Information info, String appkey, ZhiChiInitModeBase initModel, List<ZhiChiMessageBase> messageList) {
        try {
            SobotCache sobotCache = SobotCache.get(context);
            SobotMsgCenterModel sobotMsgCenterModel = new SobotMsgCenterModel();
            sobotMsgCenterModel.setInfo(info);
            sobotMsgCenterModel.setFace(initModel.getCompanyLogo());
            sobotMsgCenterModel.setName(initModel.getCompanyName());
            sobotMsgCenterModel.setAppkey(appkey);
            sobotMsgCenterModel.setUnreadCount(0);

            if (messageList != null) {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    ZhiChiMessageBase tempMsg = messageList.get(i);
                    if (ZhiChiConstant.message_sender_type_consult_info == tempMsg.getSenderType()) {
                        continue;
                    }
                    sobotMsgCenterModel.setSenderName(tempMsg.getSenderName());
                    if (TextUtils.isEmpty(tempMsg.getSenderFace())) {
                        //设置用户默认投下头像
                        sobotMsgCenterModel.setSenderFace("https://img.sobot.com/console/common/face/user.png");
                    } else {
                        sobotMsgCenterModel.setSenderFace("");
                    }
                    String lastMsg = "";
                    if (tempMsg.getAnswer() != null) {
                        if (ZhiChiConstant.message_type_pic == tempMsg.getAnswer().getMsgType()) {
                            lastMsg = ResourceUtils.getResString(context, "sobot_upload");
                        } else if (ZhiChiConstant.message_type_card == tempMsg.getAnswer().getMsgType()) {
                            lastMsg = ResourceUtils.getResString(context, "sobot_chat_type_goods");
                        } else if (ZhiChiConstant.message_type_ordercard == tempMsg.getAnswer().getMsgType()) {
                            lastMsg = ResourceUtils.getResString(context, "sobot_chat_type_card");
                        } else if (ZhiChiConstant.message_type_video == tempMsg.getAnswer().getMsgType()) {
                            lastMsg = ResourceUtils.getResString(context, "sobot_upload_video");
                        } else if (ZhiChiConstant.message_type_file == tempMsg.getAnswer().getMsgType()) {
                            lastMsg = ResourceUtils.getResString(context, "sobot_choose_file");
                        } else {
                            lastMsg = ResourceUtils.getResString(context, "sobot_chat_type_other_msg");
                        }
                    }
                    sobotMsgCenterModel.setLastMsg(lastMsg);
                    Locale locale = (Locale) SharedPreferencesUtil.getObject(context, ZhiChiConstant.SOBOT_LANGUAGE);
                    String formatString = DateUtil.getDateTimePatternByLanguage(locale, true);
                    sobotMsgCenterModel.setLastDate(DateUtil.longStrToDateStr(!TextUtils.isEmpty(tempMsg.getT()) ? tempMsg.getT() : System.currentTimeMillis() + "", formatString, locale));
                    sobotMsgCenterModel.setLastDateTime(!TextUtils.isEmpty(tempMsg.getT()) ? tempMsg.getT() : System.currentTimeMillis() + "");
                    break;
                }
                sobotCache.put(SobotMsgManager.getMsgCenterDataKey(appkey, info.getPartnerid()), sobotMsgCenterModel);

                ArrayList<String> msgDatas = (ArrayList<String>) sobotCache.getAsObject(SobotMsgManager.getMsgCenterListKey(info.getPartnerid()));
                if (msgDatas == null) {
                    msgDatas = new ArrayList<String>();
                }
                if (!msgDatas.contains(appkey)) {
                    msgDatas.add(appkey);
                    sobotCache.put(SobotMsgManager.getMsgCenterListKey(info.getPartnerid()), msgDatas);
                }
                SharedPreferencesUtil.removeKey(context, ZhiChiConstant.SOBOT_CURRENT_IM_APPID);
                Intent lastMsgIntent = new Intent(ZhiChiConstant.SOBOT_ACTION_UPDATE_LAST_MSG);
                lastMsgIntent.putExtra("lastMsg", sobotMsgCenterModel);
                LocalBroadcastManager.getInstance(context).sendBroadcast(lastMsgIntent);

                SharedPreferencesUtil.saveStringData(context, ZhiChiConstant.SOBOT_LAST_MSG_CONTENT, sobotMsgCenterModel.getLastMsg());
            }
        } catch (Exception e) {
            //
        }
    }

    public static void sendMultiRoundQuestions(Context context, SobotMultiDiaRespInfo multiDiaRespInfo, Map<String, String> interfaceRet, SobotMsgAdapter.SobotMsgCallBack msgCallBack) {
        if (context != null && multiDiaRespInfo != null && interfaceRet != null) {
            ZhiChiMessageBase msgObj = new ZhiChiMessageBase();
            String content = "{\"interfaceRetList\":[" + GsonUtil.map2Json(interfaceRet) + "]," + "\"template\":" + multiDiaRespInfo.getTemplate() + "}";
            msgObj.setT(System.currentTimeMillis() + "");
            msgObj.setContent(formatQuestionStr(multiDiaRespInfo.getOutPutParamList(), interfaceRet, multiDiaRespInfo));
            msgObj.setId(System.currentTimeMillis() + "");
            if (msgCallBack != null) {
                msgCallBack.sendMessageToRobot(msgObj, 4, 2, content, interfaceRet.get("title"));
            }
        }
    }

    private static String formatQuestionStr(String[] outPutParam, Map<String, String> interfaceRet, SobotMultiDiaRespInfo multiDiaRespInfo) {
        if (multiDiaRespInfo != null && interfaceRet != null && interfaceRet.size() > 0) {
            Map map = new HashMap<>();
            map.put("level", multiDiaRespInfo.getLevel());
            map.put("conversationId", multiDiaRespInfo.getConversationId());
            if (outPutParam != null && outPutParam.length > 0) {
                for (int i = 0; i < outPutParam.length; i++) {
                    map.put(outPutParam[i], interfaceRet.get(outPutParam[i]));
                }
            }
            return GsonUtil.map2JsonByObjectMap(map);
        }
        return "";
    }

    public static String getMultiMsgTitle(SobotMultiDiaRespInfo multiDiaRespInfo) {
        if (multiDiaRespInfo == null) {
            return "";
        }
        if ("000000".equals(multiDiaRespInfo.getRetCode())) {
            if (multiDiaRespInfo.getEndFlag()) {
                return !TextUtils.isEmpty(multiDiaRespInfo.getAnswerStrip()) ? multiDiaRespInfo.getAnswerStrip() : multiDiaRespInfo.getAnswer();
            } else {
                return multiDiaRespInfo.getRemindQuestion();
            }
        }
        return multiDiaRespInfo.getRetErrorMsg();
    }

    /**
     * 获取热点问题的类型
     *
     * @param initModel 初始化参数
     * @return
     */
    public static ZhiChiMessageBase getQuestionRecommendData(final ZhiChiInitModeBase initModel, final SobotQuestionRecommend data) {
        ZhiChiMessageBase robot = new ZhiChiMessageBase();
        robot.setSenderType(ZhiChiConstant.message_sender_type_questionRecommend);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        robot.setT(System.currentTimeMillis() + "");
        reply.setQuestionRecommend(data);
        robot.setAnswer(reply);
        robot.setSenderFace(initModel.getRobotLogo());
        robot.setSender(initModel.getRobotName());
        robot.setSenderName(initModel.getRobotName());
        return robot;
    }

    public static ZhiChiMessageBase getTipByText(String content) {
        ZhiChiMessageBase data = new ZhiChiMessageBase();
        data.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        data.setT(System.currentTimeMillis() + "");
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(content);
        reply.setRemindType(ZhiChiConstant.sobot_remind_type_tip);
        data.setAnswer(reply);
        return data;
    }

    public static void msgLogicalProcess(Context context, ZhiChiInitModeBase initModel, SobotMsgAdapter messageAdapter, ZhiChiPushMessage pushMessage) {
        if (initModel != null && ChatUtils.isNeedWarning(context, pushMessage.getContent(), initModel.getAccountStatus())) {
            messageAdapter.justAddData(ChatUtils.getTipByText(ResourceUtils.getResString(context, "sobot_money_trading_tip")));
        }
    }

    private static boolean isNeedWarning(Context context, String content, int accountStatus) {
        return !TextUtils.isEmpty(content) && (accountStatus == ZhiChiConstant.SOBOT_ACCOUNTSTATUS_FREE_EDITION
                || accountStatus == ZhiChiConstant.SOBOT_ACCOUNTSTATUS_TRIAL_EDITION)
                && content.contains(ResourceUtils.getResString(context, "sobot_ver_code"));
    }

    //是否是免费版（待激活（-1）、免费版（0））
    public static boolean isFreeAccount(int accountStatus) {
        return accountStatus == ZhiChiConstant.SOBOT_ACCOUNTSTATUS_FREE_EDITION
                || accountStatus == ZhiChiConstant.SOBOT_ACCOUNTSTATUS_INACTIVATED;
    }

    /**
     * 获取引导问题中的问题选项view
     */
    public static TextView initAnswerItemTextView(Context context, boolean isHistoryMsg) {
        if (context == null) {
            context = MyApplication.getInstance().getApplicationContext();
        }
        TextView answer = new TextView(context);
        answer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        answer.setTextSize(14);
        answer.setIncludeFontPadding(false);
        answer.setLineSpacing(context.getResources().getDimension(R.dimen.sobot_text_line_spacing_extra), 1);
        answer.setPadding(0, ScreenUtils.dip2px(context, 8), 0, 0);
        // 设置字体的颜色的样式
        int colorId = 0;
        if (isHistoryMsg) {
            answer.setTextColor(context.getResources().getColor(R.color.sobot_color_suggestion_history));
        } else {
            try {
                if (context.getResources().getColor(R.color.sobot_color_link) == context.getResources().getColor(R.color.sobot_common_blue)) {
                    ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                            ZhiChiConstant.sobot_last_current_initModel);
                    if (initMode != null && initMode.getVisitorScheme() != null) {
                        //服务端返回的可点击链接颜色
                        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getMsgClickColor())) {
                            colorId = Color.parseColor(initMode.getVisitorScheme().getMsgClickColor());
                            answer.setTextColor(colorId);
                        }
                    }
                } else {
                    answer.setTextColor(context.getResources().getColor(R.color.sobot_color_link));
                }
            } catch (Exception e) {
                answer.setTextColor(context.getResources().getColor(R.color.sobot_color_suggestion_history));
            }
        }
        return answer;
    }

    /**
     * 根据文件类型获取文件icon
     *
     * @param context
     * @param fileType
     * @return
     */
    public static int getFileIcon(Context context, int fileType) {
        int tmpResId;
        if (context == null) {
            return 0;
        }
        switch (fileType) {
            case ZhiChiConstant.MSGTYPE_FILE_DOC:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_doc");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_PPT:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_ppt");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_XLS:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_xls");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_PDF:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_pdf");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_MP3:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_mp3");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_MP4:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_mp4");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_RAR:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_rar");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_TXT:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_txt");
                break;
            case ZhiChiConstant.MSGTYPE_FILE_OTHER:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_unknow");
                break;
            default:
                tmpResId = ResourceUtils.getIdByName(context, "drawable", "sobot_icon_file_unknow");
                break;
        }
        return tmpResId;
    }

    /**
     * 根据文件名获取文件类型
     *
     * @param file
     * @return
     */
    public static int getFileType(File file) {
        int tmpFileType = ZhiChiConstant.MSGTYPE_FILE_OTHER;
        if (file == null) {
            return tmpFileType;
        }
        try {
            String name = file.getName().toLowerCase();
            if (name.endsWith("doc") || name.endsWith("docx")) {
                return ZhiChiConstant.MSGTYPE_FILE_DOC;
            } else if (name.endsWith("ppt") || name.endsWith("pptx")) {
                return ZhiChiConstant.MSGTYPE_FILE_PPT;
            } else if (name.endsWith("xls") || name.endsWith("xlsx")) {
                return ZhiChiConstant.MSGTYPE_FILE_XLS;
            } else if (name.endsWith("pdf")) {
                return ZhiChiConstant.MSGTYPE_FILE_PDF;
            } else if (name.endsWith("mp3")) {
                return ZhiChiConstant.MSGTYPE_FILE_MP3;
            } else if (name.endsWith("mp4")) {
                return ZhiChiConstant.MSGTYPE_FILE_MP4;
            } else if (name.endsWith("rar") || name.endsWith("zip")) {
                return ZhiChiConstant.MSGTYPE_FILE_RAR;
            } else if (name.endsWith("txt")) {
                return ZhiChiConstant.MSGTYPE_FILE_TXT;
            }
        } catch (Exception e) {
            //ignor
        }
        return tmpFileType;
    }

    public static void callUp(String phone, Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("tel:" + phone));// mobile为你要拨打的电话号码，模拟器中为模拟器编号也可
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.showCustomToast(context, context.getResources().getString(R.string.sobot_no_support_call));
            e.printStackTrace();
        }
    }

    //对象深拷贝
    public static <T extends Serializable> T clone(T obj) {
        T cloneObj = null;
        try {
            //写入字节流
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(out);
            obs.writeObject(obj);
            obs.close();

            //分配内存，写入原始对象，生成新对象
            ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream os = new ObjectInputStream(is);
            cloneObj = (T) os.readObject();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloneObj;
    }


    public static int getLinkTextColor() {
        return R.color.sobot_color_rlink;
    }

    public static String getCurrentLanguage() {
        String languaeCode = "";
        try {
            Locale locale;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                locale = LocaleList.getDefault().get(0);
            } else {
                locale = Locale.getDefault();
            }
            if (StringUtils.isNoEmpty(locale.toLanguageTag().toString())) {
                //繁体 特殊处理
                if (locale.toLanguageTag().contains("zh-Hant") || "zh-TW".equals(locale.toLanguageTag()) || "zh-HK".equals(locale.toLanguageTag()) || "zh-MO".equals(locale.toLanguageTag())) {
                    languaeCode = "zh-Hant";
                } else {
                    languaeCode = locale.getLanguage();
                }
            } else {
                languaeCode = locale.getLanguage();
            }
        } catch (Exception e) {

        }
        return languaeCode; // 返回的是语言代码，例如 "en" 表示英语
    }

    //------------------解析markdown --------
    //思路：
    // 1.先判断是否图片，有图片，先提取图片成<img>,再通过<img>分割成字符串数组
    // 2.没有图片就直接转markdown成html
    // 3、最终aiagent 消息转成富文本消息（richlist展示）

    //markdown 里边是否有图片 true ：有，fasel:没有
    public static boolean isHasPictureInMarkdown(String markdownText) {
        if (StringUtils.isEmpty(markdownText)) {
            return false;
        }
        if (markdownText.contains("<img src")) {
            return true;
        }
        String regex = "!\\[(.+?)\\]\\((.+?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdownText);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

    //markdown 里边有图片 ,转成<img>后再切成数组
    public static String[] parseMarkdownToArr(String markdownText) {
        String[] strArr = null;
        if (StringUtils.isEmpty(markdownText)) {
            return strArr;
        }

        // 处理图片
        markdownText = convertMarkdownPicToHtml(markdownText);
        if (markdownText.contains("<img src")) {
            markdownText = extractAndReplaceImages(markdownText);
        }
        String newContent = convertMarkdownPicToHtml(markdownText);

        // 提取表格数据
        List<String> result = new ArrayList<>();
        String[] parts = newContent.split("<img>");

        for (String part : parts) {
            // 检查是否包含表格
            if (isMarkdownTable(part)) {
                // 将表格作为一个独立元素处理
                List<String> stringList = extractMultipleMarkdownTables(part);
                if (stringList != null) {
                    result.addAll(stringList);
                } else {
                    result.add(part);
                }
            } else {
                result.add(part);
            }
        }
        return result.toArray(new String[0]);
    }
// 在 ChatUtils 类中添加以下方法

    /**
     * 提取 Markdown 中的多组表格数据，并保留非表格内容
     *
     * @param markdownText 包含多组表格和非表格内容的 Markdown 文本
     * @return 包含HTML格式表格和其他内容的字符串列表
     */
    public static List<String> extractMultipleMarkdownTables(String markdownText) {
        try {
            List<String> result = new ArrayList<>();
            if (StringUtils.isEmpty(markdownText)) {
                return result;
            }

            String[] lines = markdownText.split("\n");
            List<String> currentTableLines = new ArrayList<>();
            List<String> nonTableContent = new ArrayList<>();
            boolean inTable = false;
            boolean tableStarted = false;

            for (String line : lines) {
                // 检查是否是表格行(包含至少两个|符号)
                if (line.contains("|") && line.length() - line.replace("|", "").length() >= 2) {
                    // 如果是非表格内容积累阶段，先处理掉这些内容
                    if (!nonTableContent.isEmpty()) {
                        result.add(String.join("\n", nonTableContent));
                        nonTableContent.clear();
                    }

                    // 如果是分隔行(| --- | --- |)
                    if (line.contains("---")) {
                        tableStarted = true;
                        currentTableLines.add(line);
                    } else {
                        // 表格内容行
                        currentTableLines.add(line);
                        inTable = true;
                    }
                } else {
                    // 非表格行
                    if (inTable && tableStarted) {
                        // 表格正在进行中，继续收集非表格行，可能是表格结束
                        nonTableContent.add(line);
                    } else if (inTable) {
                        // 在表格定义中但还没遇到分隔行，这可能不是有效表格，回退到非表格内容
                        nonTableContent.addAll(currentTableLines);
                        nonTableContent.add(line);
                        currentTableLines.clear();
                        inTable = false;
                    } else {
                        // 纯非表格内容
                        nonTableContent.add(line);
                    }

                    // 检查是否是表格结束点（非表格内容为空行或者其他明显非表格内容）
                    if (inTable && tableStarted && !currentTableLines.isEmpty()) {
                        // 检查下一个非表格行是否确实是表格结束信号
                        if (!line.trim().isEmpty() &&
                                !(line.contains("|") && line.length() - line.replace("|", "").length() >= 2)) {
                            // 结束当前表格
                            String htmlTable = convertMarkdownTableToHtml(new ArrayList<>(currentTableLines));
                            if (!StringUtils.isEmpty(htmlTable)) {
                                result.add(htmlTable);
                            }
                            // 添加当前非表格行
                            if (!line.trim().isEmpty()) {
                                result.add(line);
                            }
                            currentTableLines.clear();
                            inTable = false;
                            tableStarted = false;
                        }
                    }
                }
            }

            // 处理剩余的非表格内容
            if (!nonTableContent.isEmpty()) {
                String content = String.join("\n", nonTableContent);
                if (!content.trim().isEmpty()) {
                    result.add(content);
                }
            }

            // 处理最后一个表格（如果文本以表格结束）
            if (inTable && tableStarted && !currentTableLines.isEmpty()) {
                String htmlTable = convertMarkdownTableToHtml(new ArrayList<>(currentTableLines));
                if (!StringUtils.isEmpty(htmlTable)) {
                    result.add(htmlTable);
                }
            }
            return result;
        } catch (Exception e) {
        }
        return null;
    }


    /**
     * 将 Markdown 表格行转换为 HTML 表格
     *
     * @param tableLines 表格行数据
     * @return HTML 格式的表格
     */
    private static String convertMarkdownTableToHtml(List<String> tableLines) {
        if (tableLines == null || tableLines.size() < 2) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellspacing='0' cellpadding='5'>");

        boolean isHeaderProcessed = false;

        for (int i = 0; i < tableLines.size(); i++) {
            String line = tableLines.get(i).trim();
            if (line.startsWith("|")) {
                line = line.substring(1);
            }
            if (line.endsWith("|")) {
                line = line.substring(0, line.length() - 1);
            }

            String[] cells = line.split("\\|");

            // 处理表头
            if (!isHeaderProcessed) {
                html.append("<thead><tr>");
                for (String cell : cells) {
                    html.append("<th>").append(cell.trim()).append("</th>");
                }
                html.append("</tr></thead><tbody>");
                isHeaderProcessed = true;
                continue;
            }

            // 处理分隔行(| --- | --- |)
            if (line.contains("---")) {
                continue;
            }

            // 处理数据行
            html.append("<tr>");
            for (String cell : cells) {
                html.append("<td>").append(cell.trim()).append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        return html.toString();
    }

    /**
     * 检查文本是否包含 Markdown 表格
     *
     * @param text 待检查的文本
     * @return true 表示包含表格，false 表示不包含
     */
    public static boolean isMarkdownTable(String text) {
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        String[] lines = text.split("\n");
        int tableLineCount = 0;

        for (String line : lines) {
            line = line.trim();
            // 检查是否是表格行(至少包含两个|符号，且不是分隔行)
            if (line.contains("|") && line.length() - line.replace("|", "").length() >= 2) {
                // 检查是否是表头分隔行(| --- | --- |)
                if (line.contains("|") && line.contains("---")) {
                    continue;
                }
                tableLineCount++;
            }
        }

        // 至少需要有表头和一行数据
        return tableLineCount >= 2;
    }


    //解析MarkDown数据 转成Html
    public static String parseMarkdownData(String tempContent) {
        if (StringUtils.isEmpty(tempContent)) {
            return "";
        }
        String[] arrStr = tempContent.replace("1. **", "**1.")
                .replace("2. **", "**2.")
                .replace("3. **", "**3.")
                .replace("4. **", "**4.")
                .replace("5. **", "**5.")
                .replace("6. **", "**6.")
                .replace("7. **", "**7.")
                .replace("8. **", "**8.")
                .replace("9. **", "**9.")
                .replace("10. **", "**10.")
                .replace("11. **", "**11.")
                .replace("12. **", "**12.")
                .replace("13. **", "**13.")
                .replace("14. **", "**14.")
                .replace("15. **", "**15.")
                .replace("16. **", "**16.")
                .replace("17. **", "**17.")
                .replace("18. **", "**18.")
                .replace("19. **", "**19.")
                .replace("20. **", "**20.")
                .replace("\n", "<br/>").replace("<br>", "<br/>").split("<br/>");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arrStr.length; i++) {
            String tempStr = arrStr[i];
            if (StringUtils.isEmpty(arrStr[i])) {
                tempStr = "";
            }
            if (arrStr[i].startsWith("######")) {
                //标题6级
                tempStr = "<span style=\"font-size:12px; font-weight: bold;\">" + tempStr.replace("######", "") + "</span>" + "\n";
            }
            if (tempStr.startsWith("#####")) {
                //标题5级
                tempStr = "<span style=\"font-size:14px; font-weight: bold;\">" + tempStr.replace("#####", "") + "</span>";
            }
            if (tempStr.startsWith("####")) {
                //标题4级
                tempStr = "<span style=\"font-size:16px; font-weight: bold;\">" + tempStr.replace("####", "") + "</span>";
            }
            if (tempStr.startsWith("###")) {
                //标题3级
                tempStr = "<span style=\"font-size:18px; font-weight: bold;\">" + tempStr.replace("###", "") + "</span>";
            }
            if (tempStr.startsWith("##")) {
                //标题2级
                tempStr = "<span style=\"font-size:20px; font-weight: bold;\">" + tempStr.replace("##", "") + "</span>";
            }
            if (tempStr.startsWith("#")) {
                //标题1级
                tempStr = "<span style=\"font-size:22px; font-weight: bold;\">" + tempStr.replace("#", "") + "</span>";
            }
            if (tempStr.contains("* ") || tempStr.contains("+ ") || tempStr.contains("- ")) {
                //无序列表
                tempStr = tempStr.replaceFirst("\\* ", "<span style=\"font-size:10px;\">● </span>").replaceFirst("\\+ ", "<span style=\"font-size:10px;\">● </span>").replaceFirst("\\- ", "<span style=\"font-size:10px;\">● </span>");
            }
            if (searchMarkdown(tempStr, "***") > 1) {
                //粗斜体
                tempStr = replaceMarkdownToHtmlByUserHtml(tempStr, "***", "\\*\\*\\*", "<strong><i>", "</i></strong>");
            }
            if (searchMarkdown(tempStr, "**") > 1) {
                //粗体1
                tempStr = replaceMarkdownToHtml(tempStr, "**", "\\*\\*", "strong");
            }
            if (searchMarkdown(tempStr, "__") > 1) {
                //粗体2
                tempStr = replaceMarkdownToHtml(tempStr, "__", "\\_\\_", "strong");
            }
            if (searchMarkdown(tempStr, "*") > 1) {
                //斜体1
                tempStr = replaceMarkdownToHtml(tempStr, "*", "\\*", "i");
            }
            if (searchMarkdown(tempStr, "_") > 1) {
                //斜体2
                tempStr = replaceMarkdownToHtml(tempStr, "_", "\\_", "i");
            }
            if (searchMarkdown(tempStr, "~~") > 1) {
                //删除线
                tempStr = replaceMarkdownToHtml(tempStr, "~~", "\\~\\~", "strike");
            }
            if (searchMarkdown(tempStr, "](") > 0) {
                //超链接
                tempStr = convertMarkdownLinkToHtml(tempStr);
            }
            if (arrStr != null && arrStr.length > 1 && i != (arrStr.length - 1)) {
                //不是最后，加换行
                result.append(tempStr).append("<br/>");
            } else {
                result.append(tempStr);
            }
        }
        return result.toString();
    }

    /**
     * @param str              原始数据
     * @param searchStr        检测字符
     * @param strReg           替换正则
     * @param firstReplaceHtml 第一个替换内容
     * @param firstReplaceHtml 第二个内容
     * @return 替换后的字符串
     */
    public static String replaceMarkdownToHtmlByUserHtml(String str, String searchStr, String strReg, String firstReplaceHtml, String secondReplaceHtml) {
        while (searchMarkdown(str, searchStr) > 1) {
            str = str.replaceFirst(strReg, firstReplaceHtml);
            str = str.replaceFirst(strReg, secondReplaceHtml);
        }
        return str;
    }

    /**
     * @param str             原始数据
     * @param searchStr       检测字符
     * @param strReg          替换正则
     * @param replaceHtmlName 替换内容
     * @return 替换后的字符串
     */
    public static String replaceMarkdownToHtml(String str, String searchStr, String strReg, String replaceHtmlName) {
        while (searchMarkdown(str, searchStr) > 1) {
            str = str.replaceFirst(strReg, "<" + replaceHtmlName + ">");
            str = str.replaceFirst(strReg, "</" + replaceHtmlName + ">");
        }
        return str;
    }

    //处理图片
    public static String convertMarkdownPicToHtml(String markdownText) {
        if (StringUtils.isEmpty(markdownText)) {
            return "";
        }
        // 正则表达式匹配Markdown格式的超链接
        String regex = "!\\[(.+?)\\]\\((.+?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdownText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 获取图片的文本和URL
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            // 使用StringBuilder构建新的<img>标签
            StringBuilder replacement = new StringBuilder();
            replacement.append("<img>").append(linkUrl).append("<img>");
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    //处理超链接
    public static String convertMarkdownLinkToHtml(String markdownText) {
        // 正则表达式匹配Markdown格式的超链接
        String regex = "\\[(.+?)\\]\\((.+?)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdownText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            // 获取超链接的文本和URL
            String linkText = matcher.group(1);
            String linkUrl = matcher.group(2);
            // 使用StringBuilder构建新的<a>标签
            StringBuilder replacement = new StringBuilder("<a href=\"");
            replacement.append(linkUrl).append("\">").append(linkText).append("</a>");
            // 将Markdown格式的超链接替换为HTML的<a>标签
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return sb.toString();
    }


    //查找字符串里与指定字符串相同的个数
    public static int searchMarkdown(String str, String searchStr) {//查找字符串里与指定字符串相同的个数
        int n = 0;//计数器
        while (str.indexOf(searchStr) != -1) {
            int i = str.indexOf(searchStr);
            n++;
            str = str.substring(i + 1);
        }
        return n;
    }

    /**
     * 设置两个文本控件宽度是否一致
     *
     * @param textView1
     * @param textView2
     * @param isWidthConsistent true 宽度一致，false 宽度正常
     */
    public static void doTwoViewWidthConsistent(TextView textView1, String text1, TextView textView2, String text2, boolean isWidthConsistent) {
        if (textView1 == null || textView2 == null || StringUtils.isEmpty(text1) || StringUtils.isEmpty(text1)) {
            return;
        }
        // 创建Paint对象
        Paint paint = new Paint();
        // 设置Paint的文字大小与TextView相同
        paint.setTextSize(textView1.getTextSize());
        // 测量两段文字的宽度
        float width1 = paint.measureText(text1);
        float width2 = paint.measureText(text2);
        // 设置最大宽度为两段文字中的最大值
        float maxWidth = Math.max(width1, width2);
        if (isWidthConsistent) {
            // 设置TextView的宽度为最大宽度
            textView1.setWidth((int) maxWidth);
            textView2.setWidth((int) maxWidth);
        } else {
            textView1.setWidth((int) width1);
            textView2.setWidth((int) width2);
        }
        // 设置文字
        textView1.setText(text1);
        textView2.setText(text2);
    }

    //通过设置为 false 来禁用 Android 15 (API 35) 新增的语言区域感知行高功能 (不然android15 光标变高了)
    public static void useLocalePreferredLineHeightForMinimum(EditText editText) {
        if (editText != null && Build.VERSION.SDK_INT >= 35) {
            editText.setLocalePreferredLineHeightForMinimumUsed(false);
        }
    }

    //判断在线客服座席是否是默认头像
    public static boolean isDefaultFace(String adminFace) {
        return StringUtils.isNoEmpty(adminFace) && (adminFace.contains("static-resource/account/image/admin.png") || adminFace.contains("console/common/face/admin.png") || adminFace.contains("static-resource/main/image/admin.png"));
    }


    public static Uri getUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".sobot_fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    //判断消息是否是阴影卡片类型
    public static boolean isMsgCard(int itemType) {
        return (itemType == SobotMsgAdapter.MSG_TYPE_ARTICLE_CARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_MINIPROGRAM_CARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_FILE_L
                || itemType == SobotMsgAdapter.MSG_TYPE_FILE_R
                || itemType == SobotMsgAdapter.MSG_TYPE_ROBOT_ORDERCARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_ROBOT_ORDERCARD_R
                || itemType == SobotMsgAdapter.MSG_TYPE_AI_CARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_AI_CARD_R
                || itemType == SobotMsgAdapter.MSG_TYPE_CUSTOMER_CARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_CUSTOMER_CARD_R
                || itemType == SobotMsgAdapter.MSG_TYPE_CARD_L
                || itemType == SobotMsgAdapter.MSG_TYPE_CARD_R
                || itemType == SobotMsgAdapter.MSG_TYPE_CONSULT
                || itemType == SobotMsgAdapter.MSG_TYPE_LOCATION_R);
    }

    public static void main(String[] args) {
        // 测试数据
        String data1 = "dsfdsj士大夫精神发<img src=\\\"https://img-hk.sobot.com/5fb7495819ee4b7dbba8ec41ae3af49e/common/cca16bb0f95578bbefc71f85c374be1e_1762398835585.jpeg\\\"/> jsaldjfksjd ;f";
        String data2 = "撒旦范德萨分<img src=https://img.zhichidata.com/12f6404c8f0a4dbd93327a559bc3a424/aigc/2c9e5cbf9339415d8a6b628d642ba4c8/image5.png>";

        System.out.println("原始数据1: " + data1);
        System.out.println("处理结果1: " + extractAndReplaceImages(data1));
        System.out.println();
        System.out.println("原始数据2: " + data2);
        System.out.println("处理结果2: " + extractAndReplaceImages(data2));
    }

    /**
     * 从文本中提取并替换图片地址
     *
     * @param text 输入文本
     * @return 处理后的文本
     */
    public static String extractAndReplaceImages(String text) {
        // 步骤1：正则匹配<img />标签
        try {
            String imgTagRegex = "<img\\s+[^>]*src\\s*=\\s*[\"']?([^\"'\\s>]+)[\"']?[^>]*>";
            Pattern imgTagPattern = Pattern.compile(imgTagRegex, Pattern.CASE_INSENSITIVE);
            Matcher imgTagMatcher = imgTagPattern.matcher(text);
            StringBuffer result = new StringBuffer();
            while (imgTagMatcher.find()) {
                String imgTag = imgTagMatcher.group(0); // 完整的<img>标签
                // 步骤2：从src值中提取图片URL
                String imageUrl = extractImageUrl(imgTag);
                if (imageUrl != null) {
                    // 步骤3：替换为新的<img>标签
                    String replacement = "<img>" + imageUrl + "<img>";
                    imgTagMatcher.appendReplacement(result, replacement);
                } else {
                    // 如果没有找到有效的图片URL，保留原标签
                    imgTagMatcher.appendReplacement(result, imgTag);
                }
            }
            imgTagMatcher.appendTail(result);
            return result.toString();
        } catch (Exception e) {
        }
        return text;
    }

    /**
     * 从src属性值中提取图片URL
     *
     * @param srcContent src完整数据
     * @return 提取的图片URL，如果没有找到返回null
     */
    public static String extractImageUrl(String srcContent) {
        // 正则匹配图片URL
        try {
            String imageUrlRegex = "https?://[^\\\"']*\\.(png|jpg|jpeg|gif|bmp|webp|svg|ico|tiff|tif|heic|heif|raw|psd|ai)";
            Pattern imageUrlPattern = Pattern.compile(imageUrlRegex, Pattern.CASE_INSENSITIVE);
            Matcher imageUrlMatcher = imageUrlPattern.matcher(srcContent);
            if (imageUrlMatcher.find()) {
                return imageUrlMatcher.group(0);
            }
        } catch (Exception e) {
        }
        return srcContent;
    }

    /**
     * 判断当前是否是阿拉伯语
     * 注意：如果用户禁止了镜像，也返回不是阿语
     *
     * @param context
     * @return
     */
    public static final boolean isRtl(Context context) {
        if (context == null) {
            return false;
        }
        if (!isSupportsRtl(context)) {
            LogUtils.d("用户主项目不支持布局镜像");
            return false;
        }
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.IS_CLOSE_SYSTEMRTL)) {
            LogUtils.d("用户调用SDK方法禁止了布局镜像");
            return false;
        }
        //是否使用国际化语言
        try {
            Locale language = (Locale) SharedPreferencesUtil.getObject(context, ZhiChiConstant.SOBOT_LANGUAGE);
            if (language != null) {
                String languageCode = language.getLanguage();
                //希伯来语(he), 波斯语(fa)
                if ("ar".equals(languageCode) || "he".equals(languageCode) || "fa".equals(languageCode)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // 获取 Application 清单文件 属性值 supportsRtl，是否支持镜像RTL
    public static boolean isSupportsRtl(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            // 检查 ApplicationInfo 的 flags 是否包含 FLAG_SUPPORTS_RTL
            return (appInfo.flags & ApplicationInfo.FLAG_SUPPORTS_RTL) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            // 默认情况下，如果未明确设置或发生错误，可以假设不支持或根据API级别处理
            return false;
        }
    }
}