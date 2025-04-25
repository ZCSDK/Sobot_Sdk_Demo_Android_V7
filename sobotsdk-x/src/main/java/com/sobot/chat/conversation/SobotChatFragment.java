package com.sobot.chat.conversation;


import static com.sobot.chat.api.enumtype.SobotAutoSendMsgMode.ZCMessageTypeFile;
import static com.sobot.chat.api.enumtype.SobotAutoSendMsgMode.ZCMessageTypePhoto;
import static com.sobot.chat.api.enumtype.SobotAutoSendMsgMode.ZCMessageTypeText;
import static com.sobot.chat.api.enumtype.SobotAutoSendMsgMode.ZCMessageTypeVideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.SobotUIConfig;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.activity.SobotPostLeaveMsgActivity;
import com.sobot.chat.activity.SobotPostMsgActivity;
import com.sobot.chat.activity.SobotTicketListActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.activity.halfdialog.SobotAIEvaluateActivity;
import com.sobot.chat.activity.halfdialog.SobotChooseLanguaeActivity;
import com.sobot.chat.activity.halfdialog.SobotEvaluateActivity;
import com.sobot.chat.activity.halfdialog.SobotFormInfoActivity;
import com.sobot.chat.activity.halfdialog.SobotRobotListActivity;
import com.sobot.chat.activity.halfdialog.SobotSkillGroupActivity;
import com.sobot.chat.adapter.SobotMsgAdapter;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.apiUtils.GsonUtil;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;
import com.sobot.chat.api.apiUtils.SobotVerControl;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.enumtype.CustomerState;
import com.sobot.chat.api.enumtype.SobotAutoSendMsgMode;
import com.sobot.chat.api.enumtype.SobotChatStatusMode;
import com.sobot.chat.api.model.BaseCode;
import com.sobot.chat.api.model.BaseListCodeV6;
import com.sobot.chat.api.model.ChatMessageRichListModel;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.CommonModelBase;
import com.sobot.chat.api.model.ConsultingContent;
import com.sobot.chat.api.model.FaqDocRespVo;
import com.sobot.chat.api.model.FormInfoModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.OrderCardContentModel;
import com.sobot.chat.api.model.QuickMenuItemModel;
import com.sobot.chat.api.model.QuickMenuModel;
import com.sobot.chat.api.model.SatisfactionSet;
import com.sobot.chat.api.model.SobotAiRobotAnswerCommontParams;
import com.sobot.chat.api.model.SobotAiRobotRealuateConfigInfo;
import com.sobot.chat.api.model.SobotAiRobotRealuateInfo;
import com.sobot.chat.api.model.SobotAiRobotRealuateTag;
import com.sobot.chat.api.model.SobotCommentParam;
import com.sobot.chat.api.model.SobotConnCusParam;
import com.sobot.chat.api.model.SobotEvaluateModel;
import com.sobot.chat.api.model.SobotKeyWordTransfer;
import com.sobot.chat.api.model.SobotLocationModel;
import com.sobot.chat.api.model.SobotMultiDiaRespInfo;
import com.sobot.chat.api.model.SobotRealuateConfigInfo;
import com.sobot.chat.api.model.SobotRealuateInfo;
import com.sobot.chat.api.model.SobotRobot;
import com.sobot.chat.api.model.SobotSemanticsKeyWordTransfer;
import com.sobot.chat.api.model.SobotTransferOperatorParam;
import com.sobot.chat.api.model.SobotlanguaeModel;
import com.sobot.chat.api.model.SobotlanguaeResultModel;
import com.sobot.chat.api.model.ZhiChiAppointMessage;
import com.sobot.chat.api.model.ZhiChiCidsModel;
import com.sobot.chat.api.model.ZhiChiGroup;
import com.sobot.chat.api.model.ZhiChiGroupBase;
import com.sobot.chat.api.model.ZhiChiHistoryMessage;
import com.sobot.chat.api.model.ZhiChiHistoryMessageBase;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessage;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.api.model.ZhiChiPushMessage;
import com.sobot.chat.api.model.ZhiChiReplyAnswer;
import com.sobot.chat.api.model.customcard.SobotChatCustomCard;
import com.sobot.chat.api.model.customcard.SobotChatCustomGoods;
import com.sobot.chat.api.model.customcard.SobotChatCustomMenu;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.Const;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.handler.SobotMsgHandler;
import com.sobot.chat.listener.OnMultiClickListener;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.listener.SobotFunctionType;
import com.sobot.chat.presenter.StPostMsgPresenter;
import com.sobot.chat.server.SobotSessionServer;
import com.sobot.chat.utils.AnimationUtil;
import com.sobot.chat.utils.AudioTools;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.CustomToast;
import com.sobot.chat.utils.ExtAudioRecorder;
import com.sobot.chat.utils.ImageUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.MediaFileUtils;
import com.sobot.chat.utils.ResourceUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.SobotOption;
import com.sobot.chat.utils.SobotPathManager;
import com.sobot.chat.utils.SobotSerializableMap;
import com.sobot.chat.utils.StServiceUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.TimeTools;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConfig;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.ArticleMessageHolder;
import com.sobot.chat.viewHolder.CusEvaluateMessageHolder;
import com.sobot.chat.viewHolder.FileMessageHolder;
import com.sobot.chat.viewHolder.ImageMessageHolder;
import com.sobot.chat.viewHolder.MiniProgramMessageHolder;
import com.sobot.chat.viewHolder.RichTextMessageHolder;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder1;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder2;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder3;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder4;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder5;
import com.sobot.chat.viewHolder.RobotTemplateMessageHolder6;
import com.sobot.chat.viewHolder.TextMessageHolder;
import com.sobot.chat.viewHolder.VideoMessageHolder;
import com.sobot.chat.viewHolder.VoiceMessageHolder;
import com.sobot.chat.voice.AudioPlayCallBack;
import com.sobot.chat.voice.AudioPlayPresenter;
import com.sobot.chat.widget.ContainsEmojiEditText;
import com.sobot.chat.widget.dialog.SobotClearHistoryActivity;
import com.sobot.chat.widget.dialog.SobotCommonDialog;
import com.sobot.chat.widget.emoji.DisplayEmojiRules;
import com.sobot.chat.widget.emoji.EmojiconNew;
import com.sobot.chat.widget.emoji.InputHelper;
import com.sobot.chat.widget.image.SobotProgressImageView;
import com.sobot.chat.widget.kpswitch.CustomeChattingPanel;
import com.sobot.chat.widget.kpswitch.util.KPSwitchConflictUtil;
import com.sobot.chat.widget.kpswitch.util.KeyboardUtil;
import com.sobot.chat.widget.kpswitch.util.ViewUtil;
import com.sobot.chat.widget.kpswitch.view.ChattingPanelEmoticonView;
import com.sobot.chat.widget.kpswitch.view.ChattingPanelMoreMenuView;
import com.sobot.chat.widget.kpswitch.view.CustomeViewFactory;
import com.sobot.chat.widget.kpswitch.widget.KPSwitchPanelLinearLayout;
import com.sobot.chat.widget.refresh.SobotScrollLinearLayoutManager;
import com.sobot.chat.widget.statusbar.StatusBarUtil;
import com.sobot.gson.JsonSyntaxException;
import com.sobot.gson.SobotGsonUtil;
import com.sobot.gson.reflect.TypeToken;
import com.sobot.network.http.callback.SobotResultCallBack;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.network.http.upload.SobotUpload;
import com.sobot.pictureframe.SobotBitmapUtil;
import com.sobot.utils.SobotDensityUtil;
import com.sobot.utils.SobotLogUtils;
import com.sobot.utils.SobotSharedPreferencesUtil;
import com.sobot.utils.SobotStringUtils;
import com.sobot.widget.refresh.layout.SobotRefreshLayout;
import com.sobot.widget.refresh.layout.api.RefreshLayout;
import com.sobot.widget.refresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author Created by jinxl on 2018/2/1.
 */
public class SobotChatFragment extends SobotChatBaseFragment implements View.OnClickListener
        , SobotMsgAdapter.SobotMsgCallBack,
        ContainsEmojiEditText.SobotAutoCompleteListener
        , ChattingPanelEmoticonView.SobotEmoticonClickListener
        , ChattingPanelMoreMenuView.SobotPlusClickListener, ChattingPanelMoreMenuView.SobotPlusCountListener {

    //---------------UI控件 START---------------
    public LinearLayout sobot_header_center_ll;//导航栏中间部分
    public LinearLayout sobot_titlebar_text_ll;//导航栏中间文字部分
    public TextView mTitleTextView;//导航栏昵称
    public TextView mCompanyTextView;//导航栏公司名称
    public SobotProgressImageView mAvatarIV;//导航栏头像
    public TextView sobot_title_conn_status;
    public LinearLayout sobot_container_conn_status;
    public ImageView iv_right_second;
    public ImageView iv_right_third;
    private ImageView iv_close;//右上角 关闭
    private ImageView sobot_iv_right;//右上角 三个点
    private ImageView sobot_iv_left;//左上角返回
    public ProgressBar sobot_conn_loading;
    public RelativeLayout net_status_remide;
    public TextView sobot_net_not_connect;
    public RelativeLayout relative;
    public ImageView iv_not_read;
    private TextView sobot_tv_satisfaction, notReadInfo, sobot_tv_message,
            sobot_txt_restart_talk;
    private LinearLayout ll_satisfaction, ll_message,
            ll_restart_talk;
    private TextView textReConnect;
    private ProgressBar loading_anim_view;
    private TextView txt_loading;
    private ImageView icon_nonet;
    private Button btn_reconnect;
    private RelativeLayout chat_main; // 聊天主窗口
    private LinearLayout sobot_bar_bottom; // 底部区域
    private FrameLayout welcome; // 欢迎窗口;
    private RecyclerView messageRV;//消息列表
    private SobotRefreshLayout srv_message;//下拉刷线控件
    private ContainsEmojiEditText et_sendmessage;// 当前用户输入的信息
    private Button btn_send_pic; // 发送图片
    //    private ImageButton btn_set_mode_rengong; // 转人工button
    private TextView send_voice_robot_hint;
    private CheckBox cb_plus_view; // 底部加号
    private int mRobotOperatorCount;//机器人聊天模式下加号面板菜单功能的数量
    private int mOperatorCount;// 人工聊天模式下加号面板菜单功能的数量
    private int mRobotPanleHeiht;//机器人聊天模式下加号面板菜单高度
    private int mArtificialPanleHeiht;// 人工聊天模式下加号面板菜单功能的高度
    private CheckBox cb_emoticon_view; // 表情面板
    private TextView voice_time_long;/*显示语音时长*/
    private LinearLayout ll_sound_recording;// 录音80%透明弹窗布局
    private FrameLayout fl_sound_recording_animation;//录音 动画布局
    private TextView tv_recording_hint;// 录音上滑的显示文本；
    private TextView tv_recording_countdown_hint;//录音倒计时提示
    private ImageView iv_sound_recording_cancle;// 录音 取消 图片动画
    private ImageView iv_sound_recording_in_progress; // 录音中 图片动画
    private AnimationDrawable animationDrawable;/* 语音的动画 */
    private AnimationDrawable cancleAnimationDrawable;/* 语音的动画 */
    private ImageButton btn_model_edit; // 编辑模式
    private ImageButton btn_model_voice;// 语音模式
    private TextView txt_speak_content; // 发送语音的文字
    private KPSwitchPanelLinearLayout mPanelLayout; // 聊天下面的面板
    private LinearLayout btn_press_to_speak; // 说话view ;
    private LinearLayout edittext_layout; // 输入框view;
    private RelativeLayout sobot_ll_restart_talk; // 开始新会话布局ID
    private ImageView image_reLoading;
    private LinearLayout sobot_ll_bottom;//聊天界面底部布局

    //通告
    private RelativeLayout sobot_announcement; // 通告view ;
    private TextView sobot_announcement_right_icon;
    private TextView sobot_announcement_title;
    //机器人切换按钮
    private LinearLayout ll_switch_robot;
    private TextView tv_switch_robot;
    private ImageView iv_switch_robot;

    private boolean isAddedMenu = false;//是否已经加过
    private HorizontalScrollView sobot_custom_menu;//横向滚动布局
    private LinearLayout sobot_custom_menu_linearlayout;
    //---------------UI控件 END---------------


    //-----------
    // 消息列表展示
    private List<ZhiChiMessageBase> messageList = new ArrayList<ZhiChiMessageBase>();


    //--------

    private int showTimeVisiableCustomBtn = 0;/*用户设置几次显示转人工按钮*/
    private List<ZhiChiGroupBase> list_group;

    protected int type = -1;//当前模式的类型
    private static String preCurrentCid = null;//保存上一次会话cid；
    private static int statusFlag = 0; // 保存当前转人工成功的状态
    private boolean isSessionOver = true;//表示此会话是否结束null

    private boolean isComment = false;/* 判断用户是否评价过 */
    private boolean isShowQueueTip = true;//是否显示 排队提醒 用以过滤关键字转人工时出现的提醒
    private int queueNum = 0;//排队的人数
    private int queueTimes = 0;//收到排队顺序变化提醒的次数

    //录音相关
    protected Timer voiceTimer;
    protected TimerTask voiceTimerTask;
    protected int voiceTimerLong = 0;
    protected String voiceTimeLongStr = "00";// 时间的定时的任务
    private int minRecordTime = 60;// 允许录音时间
    private int recordDownTime = minRecordTime - 10;// 允许录音时间 倒计时
    boolean isCutVoice;
    private String voiceMsgId = "";//  语音消息的Id
    private int currentVoiceLong = 0;

    AudioPlayPresenter mAudioPlayPresenter = null;
    AudioPlayCallBack mAudioPlayCallBack = null;
    private String mFileName = null;
    private ExtAudioRecorder extAudioRecorder;

    //以下参数为历史记录需要的接口
    private List<String> cids = new ArrayList<>();//cid的列表
    private int currentCidPosition = 0;//当前查询聊天记录所用的cid位置
    //表示查询cid的接口 当前调用状态 0、未调用 1、调用中 2、调用成功  3、调用失败
    private int queryCidsStatus = ZhiChiConstant.QUERY_CIDS_STATUS_INITIAL;
    private boolean isInGethistory = false;//表示是否正在查询历史记录
    private boolean isConnCustomerService = false;//控制同一时间 只能调一次转人工接口
    private boolean isNoMoreHistoryMsg = false;


    //键盘相关
    public int currentPanelId = 0;//切换聊天面板时 当前点击的按钮id 为了能切换到对应的view上
    private int mBottomViewtype = 0;//记录键盘的状态

    //---------
    //键盘监听
    private ViewTreeObserver.OnGlobalLayoutListener mKPSwitchListener;

    private MyMessageReceiver receiver;
    //本地广播数据类型实例。
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;

    //留言处理
    private StPostMsgPresenter mPostMsgPresenter;

    //2.9.2添加 初始化时如果有离线消息直接转对应的客服
    private int offlineMsgConnectFlag;
    private String offlineMsgAdminId;
    //命中后端关键词转人工，机器人接口返回的
    ZhiChiMessageBase keyWordMessageBase;

    String tempMsgContent;//2.9.3 仅人工/人工优先模拟人工模式，临时保存客户真正转人工前第一次发送的消息，转人工成功后自动发送发送，发送完清除

    //初始化接口是否已经结束，防止多次调用
    private boolean isAppInitEnd = true;

    //底部菜单的行数
    private int bottomMenuLines = 1;

    private int mUnreadNum = 0;//未读消息数
    private int lastVisibleItem = 0;//锁屏前的显示的位置
    private int firstVisiableItemTmp = 0;//显示列表中的第一个item位置
    private int visibleItemCountTmp = 0;//显示列表中item的个数
    //“未读消息的位置”
    private int unReadMsgIndex;
    //新消息提示
    private boolean showNewMsg = false;//是否展示新消息提醒
    private int newMsgNum = 0;//新消息个数
    //多消息显示不全，提醒未读个数
    private int msgAnswersNum = 0;//多消息个数
    //可以获取RecyclerView 获取屏幕上消息的消息的索引，显示的数量
    private SobotScrollLinearLayoutManager rvScrollLayoutManager;
    public LinearLayout ll_notReadInfo;//顶部未读消息 用于暂时离开后客服发送的未读消息
    //底部新消息UI
    private LinearLayout ll_newmsg;
    private TextView tv_newmsg;
    private ImageView iv_newmsg;
    private Map<Integer, QuickMenuModel> allQuickMenuModel;

    public static SobotChatFragment newInstance(Bundle info) {
        Bundle arguments = new Bundle();
        arguments.putBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION, info);
        SobotChatFragment fragment = new SobotChatFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.i("onCreate");
        unReadMsgIds = new HashMap<>();
        allQuickMenuModel = new HashMap<>();
        try {
            String host = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_SAVE_HOST_AFTER_INITSDK, SobotBaseUrl.getApi_Host());
            if (!host.equals(SobotBaseUrl.getApi_Host())) {
                SobotBaseUrl.setApi_Host(host);
            }
        } catch (Exception e) {
        }
        if (getArguments() != null) {
            Bundle informationBundle = getArguments().getBundle(ZhiChiConstant.SOBOT_BUNDLE_INFORMATION);
            if (informationBundle != null) {
                Serializable sobot_info = informationBundle.getSerializable(ZhiChiConstant.SOBOT_BUNDLE_INFO);
                if (sobot_info != null && sobot_info instanceof Information) {
                    info = (Information) sobot_info;
                    boolean isUseLanguage = SharedPreferencesUtil.getBooleanData(getSobotActivity(), ZhiChiConstant.SOBOT_USE_LANGUAGE, false);
                    if (isUseLanguage) {
                        //客户指定语言了
                        String settingLanguage = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_USER_SETTTINNG_LANGUAGE, "");
                        if (StringUtils.isNoEmpty(settingLanguage)) {
                            info.setLocale(settingLanguage);
                        }
                    }
                    //手机系统语言
                    String sysLanguae = ChatUtils.getCurrentLanguage();
                    if (StringUtils.isNoEmpty(sysLanguae)) {
                        info.setSystemLanguage(sysLanguae);
                    }
                    SharedPreferencesUtil.saveObject(getSobotActivity(),
                            ZhiChiConstant.sobot_last_current_info, info);
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sobot_chat_fragment, container, false);
        initView(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (info == null) {
            LogUtils.e("初始化参数不能为空");
            finish();
            return;
        }

        String platformUnionCode = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(platformUnionCode)) {
            if (TextUtils.isEmpty(info.getApp_key()) && TextUtils.isEmpty(info.getCustomer_code())) {
                LogUtils.i("appkey或者customCode必须设置一项");
                finish();
                return;
            }
        } else {
            if (TextUtils.isEmpty(info.getApp_key())) {
                LogUtils.e("您的AppKey为空");
                finish();
                return;
            }
        }

        SharedPreferencesUtil.saveStringData(mAppContext, ZhiChiConstant.SOBOT_CURRENT_IM_APPID, info.getApp_key());

        //保存自定义配置
        ChatUtils.saveOptionSet(mAppContext, info);

        initData();
        //设置ai消息回调
        setMsgHandler(new SobotMsgHandler() {
            @Override
            public void showMsg(ZhiChiMessageBase aiMsg) {
                Message message = handler.obtainMessage();
                message.what = ZhiChiConstant.hander_ai_robot_message;
                message.obj = aiMsg;
                boolean b = handler.sendMessage(message);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (iv_close != null) {
            if (info.isShowCloseBtn() && current_client_model == ZhiChiConstant.client_model_customService) {
                //显示右上角的关闭按钮
                iv_close.setVisibility(View.VISIBLE);
            } else {
                iv_close.setVisibility(View.GONE);
            }
        }
        //获取未读数
        if (lastVisibleItem > 0) {
            unReadMsgIndex = lastVisibleItem;
            loadUnreadNum();
            lastVisibleItem = 0;
        }

        SharedPreferencesUtil.saveStringData(mAppContext, ZhiChiConstant.SOBOT_CURRENT_IM_APPID, info.getApp_key());
        Intent intent = new Intent(mAppContext, SobotSessionServer.class);
        intent.putExtra(ZhiChiConstant.SOBOT_CURRENT_IM_PARTNERID, info.getPartnerid());
        StServiceUtils.safeStartService(mAppContext, intent);
        SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key()).clearCache();
        //人工状态，检查连接
        if (customerState == CustomerState.Online || customerState == CustomerState.Queuing) {
            //获取tcp服务被杀死的时间，如果是0，不进行初始化，直接检查通道就行
            long lastHideTime = SharedPreferencesUtil.getLongData(mAppContext, ZhiChiConstant.SOBOT_HIDE_CHATPAGE_TIME, System.currentTimeMillis());
            if (lastHideTime != 0 && !CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
                //LogUtils.i((System.currentTimeMillis() + "-------------" + lastHideTime + "==========" + (System.currentTimeMillis() - lastHideTime)));
                // LogUtils.i("----人工状态 SobotTCPServer 被杀死了");
                if ((System.currentTimeMillis() - lastHideTime) > 30 * 60 * 1000) {
                    //   LogUtils.i("----由于SobotTCPServer 被杀死了超过30分钟，需要重新初始化---------");
                    initSdk(true, 0);
                } else {
                    zhiChiApi.reconnectChannel();
                }
            } else {
                zhiChiApi.reconnectChannel();
            }
            String puid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_PUID, "");
            if (StringUtils.isNoEmpty(puid)) {
                //互挤判断
                zhiChiApi.userStatus(getSobotActivity(), info.getApp_key(), initModel.getPartnerid(), puid, new SobotResultCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        if (o instanceof Boolean) {
                            boolean isEffectiveUserStatus = (boolean) o;
                            //false 代表不对，需要做离线处理（204 打开了新窗口）；true代表正常聊天，不需要处理
                            if (!isEffectiveUserStatus) {
                                customerServiceOffline(initModel, 6);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String s) {

                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        if (initModel != null) {
            if (!isSessionOver) {
                //保存会话信息
                saveCache();
            } else {
                //清除会话信息
                clearCache();
            }
            //保存消息列表
            ChatUtils.saveLastMsgInfo(getSobotActivity(), info, info.getApp_key(), initModel, messageList);
        }

        //记录显示列表的位置
        lastVisibleItem = firstVisiableItemTmp + visibleItemCountTmp;

        stopInputListener();
        if (AudioTools.getInstance().isPlaying()) {
            //停止播放
            AudioTools.getInstance().stop();
            messageRV.post(new Runnable() {

                @Override
                public void run() {
                    if (info == null) {
                        return;
                    }
                    for (int i = 0, count = messageRV.getChildCount(); i < count; i++) {
                        View child = messageRV.getChildAt(i);
                        if (child == null || child.getTag() == null || !(child.getTag() instanceof VoiceMessageHolder)) {
                            continue;
                        }
                        VoiceMessageHolder holder = (VoiceMessageHolder) child.getTag();
                        if (holder != null) {
                            holder.stopAnim();
                            holder.checkBackground();
                        }
                    }
                }
            });
        }
        ////放弃音频焦点
        abandonAudioFocus();
        // 取消注册传感器
        if (_sensorManager != null) {
            _sensorManager.unregisterListener(this);
            _sensorManager = null;
        }
        if (mProximiny != null)
            mProximiny = null;
        super.onPause();
        // 用于记录当前的键盘状态，在从后台回到当前页面的时候，键盘状态能够正确的恢复并且不会导致布局冲突。
//        mPanelLayout.recordKeyboardStatus(getSobotActivity().getWindow());
    }

    @Override
    public void onDestroyView() {
        if (!isAboveZero) {
            SharedPreferencesUtil.saveLongData(getSobotActivity(), ZhiChiConstant.SOBOT_FINISH_CURTIME, System.currentTimeMillis());
        }
        hideReLoading();
        // 停止用户的定时任务
        stopUserInfoTimeTask();
        // 停止客服的定时任务
        stopCustomTimeTask();
        stopVoice();
        AudioTools.destory();
        SobotUpload.getInstance().unRegister();
        mPostMsgPresenter.destory();
        if (SobotOption.sobotViewListener != null) {
            SobotOption.sobotViewListener.onChatActClose(customerState);
        }
        super.onDestroyView();
    }

    private void initView(View rootView) {
        if (rootView == null) {
            return;
        }

        //新消息提醒UI
        ll_newmsg = rootView.findViewById(R.id.ll_newmsg);
        tv_newmsg = rootView.findViewById(R.id.tv_newmsg);
        iv_newmsg = rootView.findViewById(R.id.iv_newmsg);
        ll_newmsg.setOnClickListener(this);

        //loading 层
        relative = (RelativeLayout) rootView.findViewById(R.id.sobot_layout_titlebar);
        sobot_header_center_ll = (LinearLayout) rootView.findViewById(R.id.sobot_header_center_ll);
        mTitleTextView = (TextView) rootView.findViewById(R.id.sobot_text_title);
        sobot_titlebar_text_ll = (LinearLayout) rootView.findViewById(R.id.sobot_titlebar_text_ll);
        mCompanyTextView = (TextView) rootView.findViewById(R.id.sobot_titlebar_company_name);
        mAvatarIV = rootView.findViewById(R.id.sobot_avatar_iv);
        sobot_title_conn_status = (TextView) rootView.findViewById(R.id.sobot_title_conn_status);
        sobot_container_conn_status = (LinearLayout) rootView.findViewById(R.id.sobot_container_conn_status);
        iv_close = rootView.findViewById(R.id.iv_close);
        iv_right_second = rootView.findViewById(R.id.iv_right_second);
        iv_right_third = rootView.findViewById(R.id.iv_right_third);
        sobot_conn_loading = (ProgressBar) rootView.findViewById(R.id.sobot_conn_loading);
        net_status_remide = (RelativeLayout) rootView.findViewById(R.id.sobot_net_status_remide);
        sobot_net_not_connect = (TextView) rootView.findViewById(R.id.sobot_net_not_connect);
        sobot_net_not_connect.setText(R.string.sobot_network_unavailable);

        relative.setVisibility(View.GONE);
        notReadInfo = (TextView) rootView.findViewById(R.id.notReadInfo);
        iv_not_read = rootView.findViewById(R.id.iv_not_read);
        ll_notReadInfo = rootView.findViewById(R.id.ll_notReadInfo);
        chat_main = (RelativeLayout) rootView.findViewById(R.id.sobot_chat_main);
        sobot_bar_bottom = rootView.findViewById(R.id.sobot_bar_bottom);
        sobot_iv_right = rootView.findViewById(R.id.sobot_iv_right);
        welcome = (FrameLayout) rootView.findViewById(R.id.sobot_welcome);
        txt_loading = (TextView) rootView.findViewById(R.id.sobot_txt_loading);
        textReConnect = (TextView) rootView.findViewById(R.id.sobot_textReConnect);
        textReConnect.setText(R.string.sobot_try_again);
        loading_anim_view = (ProgressBar) rootView.findViewById(R.id.sobot_image_view);
        image_reLoading = (ImageView) rootView.findViewById(R.id.sobot_image_reloading);
        icon_nonet = (ImageView) rootView.findViewById(R.id.sobot_icon_nonet);
        btn_reconnect = (Button) rootView.findViewById(R.id.sobot_btn_reconnect);
        btn_reconnect.setText(R.string.sobot_reunicon);
        btn_reconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                textReConnect.setVisibility(View.GONE);
                icon_nonet.setVisibility(View.GONE);
                btn_reconnect.setVisibility(View.GONE);
                loading_anim_view.setVisibility(View.VISIBLE);
                txt_loading.setVisibility(View.VISIBLE);
                //重新加载，相当于第一次进入
                customerInit(1);
            }
        });

        messageRV = rootView.findViewById(R.id.sobot_rv_message);
        srv_message = rootView.findViewById(R.id.sobot_srv_message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            messageRV.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        displayInNotch(messageRV);
        et_sendmessage = (ContainsEmojiEditText) rootView.findViewById(R.id.sobot_et_sendmessage);
        et_sendmessage.setVisibility(View.VISIBLE);
        displayInNotch(et_sendmessage);
        btn_send_pic = rootView.findViewById(R.id.sobot_btn_send_view);
        send_voice_robot_hint = (TextView) rootView.findViewById(R.id.send_voice_robot_hint);
        send_voice_robot_hint.setText(R.string.sobot_robot_voice_hint);
        send_voice_robot_hint.setVisibility(View.GONE);
        cb_plus_view = rootView.findViewById(R.id.sobot_btn_upload_view);
        cb_emoticon_view = rootView.findViewById(R.id.sobot_btn_emoticon_view);
        btn_model_edit = (ImageButton) rootView.findViewById(R.id.sobot_btn_model_edit);
        btn_model_voice = (ImageButton) rootView.findViewById(R.id.sobot_btn_model_voice);
        mPanelLayout = rootView.findViewById(R.id.sobot_panel_bottom);
        btn_press_to_speak = (LinearLayout) rootView.findViewById(R.id.sobot_btn_press_to_speak);
        edittext_layout = (LinearLayout) rootView.findViewById(R.id.sobot_edittext_layout);
        tv_recording_hint = (TextView) rootView.findViewById(R.id.tv_recording_hint);
        tv_recording_countdown_hint = (TextView) rootView.findViewById(R.id.tv_recording_countdown_hint);
        ll_sound_recording = (LinearLayout) rootView.findViewById(R.id.ll_sound_recording);

        // 开始语音的布局的信息
        fl_sound_recording_animation = rootView.findViewById(R.id.fl_sound_recording_animation);
        // 停止语音
        iv_sound_recording_cancle = (ImageView) rootView.findViewById(R.id.iv_sound_recording_cancle);
        // 动画的效果
        iv_sound_recording_in_progress = (ImageView) rootView.findViewById(R.id.iv_sound_recording_in_progress);
        // 时长的界面
        voice_time_long = (TextView) rootView.findViewById(R.id.sobot_voiceTimeLong);
        txt_speak_content = (TextView) rootView.findViewById(R.id.sobot_txt_speak_content);
        txt_speak_content.setText(R.string.sobot_press_say);

        sobot_ll_restart_talk = (RelativeLayout) rootView.findViewById(R.id.sobot_ll_restart_talk);
        sobot_txt_restart_talk = (TextView) rootView.findViewById(R.id.sobot_txt_restart_talk);
        sobot_txt_restart_talk.setText(R.string.sobot_restart_talk);
        sobot_tv_message = (TextView) rootView.findViewById(R.id.sobot_tv_message);
        sobot_tv_message.setText(R.string.sobot_str_bottom_message);
        sobot_tv_satisfaction = (TextView) rootView.findViewById(R.id.sobot_tv_satisfaction);
        sobot_tv_satisfaction.setText(R.string.sobot_str_bottom_satisfaction);
        ll_satisfaction = rootView.findViewById(R.id.ll_satisfaction);
        ll_message = rootView.findViewById(R.id.ll_message);
        ll_restart_talk = rootView.findViewById(R.id.ll_restart_talk);
        sobot_ll_bottom = (LinearLayout) rootView.findViewById(R.id.sobot_ll_bottom);
        ll_appoint = rootView.findViewById(R.id.ll_appoint);
        tv_appoint_temp_content = rootView.findViewById(R.id.tv_appoint_temp_content);
        iv_appoint_clear = rootView.findViewById(R.id.iv_appoint_clear);
        iv_appoint_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除引用缓存
                clearAppointUI();
            }
        });
        ll_switch_robot = (LinearLayout) rootView.findViewById(R.id.ll_switch_robot);
        tv_switch_robot = (TextView) rootView.findViewById(R.id.tv_switch_robot);
        iv_switch_robot = rootView.findViewById(R.id.iv_switch_robot);
        tv_switch_robot.setText(R.string.sobot_switch_business);

        sobot_announcement = (RelativeLayout) rootView.findViewById(R.id.sobot_announcement);
        sobot_announcement_right_icon = (TextView) rootView.findViewById(R.id.sobot_announcement_right_icon);
        sobot_announcement_title = (TextView) rootView.findViewById(R.id.sobot_announcement_title);
        sobot_announcement_title.setSelected(true);

        sobot_custom_menu = (HorizontalScrollView) rootView.findViewById(R.id.sobot_custom_menu);
        sobot_custom_menu.setVisibility(View.GONE);

        sobot_custom_menu_linearlayout = (LinearLayout) rootView.findViewById(R.id.sobot_custom_menu_linearlayout);
        //根据主题更新控件颜色
        updateUIByThemeColor();
        mPostMsgPresenter = StPostMsgPresenter.newInstance(SobotChatFragment.this, getSobotActivity());
    }

    /* 处理消息 */
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {

        @SuppressWarnings("unchecked")
        public void handleMessage(final android.os.Message msg) {
            if (!isActive()) {
                return;
            }
            switch (msg.what) {
                case ZhiChiConstant.hander_show_unread_tip:
                    updateFloatUnreadIcon();
                    break;
                case ZhiChiConstant.hander_show_newmsg_tip:
                    tv_newmsg.setTextColor(ThemeUtils.getThemeColor(getSobotActivity()));
                    int color = ThemeUtils.getThemeColor(getSobotActivity());
                    iv_newmsg.setImageDrawable(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_not_read_newmsg), color));
                    tv_newmsg.setText(newMsgNum + " " + getResources().getString(R.string.sobot_new_msg));
                    ll_newmsg.setVisibility(View.VISIBLE);
                    break;
                case ZhiChiConstant.hander_hide_newmsg_tip:
                    newMsgNum = 0;
                    tv_newmsg.setTextColor(ThemeUtils.getThemeColor(getSobotActivity()));
                    tv_newmsg.setText(newMsgNum + " " + getResources().getString(R.string.sobot_new_msg));
                    ll_newmsg.setVisibility(View.GONE);
                    break;
                case ZhiChiConstant.hander_send_msg:
                    //发送消息更新UI
                    updateUiMessage(messageAdapter, msg);
                    gotoLastItem();
                    break;
                case ZhiChiConstant.hander_add_message_no_goto_last:
                    //添加消息更新UI
                    updateUiMessage(messageAdapter, msg);
                    gotoLastItem();
                    break;
                case ZhiChiConstant.hander_update_msg_status:
                    //消息发送状态更新
                    updateMessageStatus(messageAdapter, msg);
                    break;
                case ZhiChiConstant.update_send_data:
                    ZhiChiMessageBase myMessage = (ZhiChiMessageBase) msg.obj;
                    messageAdapter.updateDataById(myMessage.getId(), myMessage);
                    gotoLastItem();
                    break;
                case ZhiChiConstant.hander_ai_robot_message_start:
                    //大模型机器人 开始 显示3个点动画
                    String aiMsgId = (String) msg.obj;
                    if (SobotStringUtils.isNoEmpty(aiMsgId)) {
                        ZhiChiMessageBase startBaseMsg = new ZhiChiMessageBase();
                        startBaseMsg.setT(System.currentTimeMillis() + "");
                        startBaseMsg.setMsgId(aiMsgId);
                        startBaseMsg.setId(aiMsgId);
                        startBaseMsg.setServant("aiagent");//aiagent 答案
                        startBaseMsg.setSenderName(initModel.getRobotName());
                        startBaseMsg.setSender(initModel.getRobotName());
                        startBaseMsg.setSenderFace(initModel.getRobotLogo());
                        startBaseMsg.setSenderType(ZhiChiConstant.message_sender_type_robot);
                        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
                        answer.setMsgType(ZhiChiConstant.message_type_emoji);
                        startBaseMsg.setAnswer(answer);
                        messageAdapter.addData(startBaseMsg);
                    }
                    break;
                case ZhiChiConstant.hander_ai_robot_message_fail:
                    //大模型机器人 连接失败结束 三个点动画
                    String tempAiMsgId = (String) msg.obj;
                    if (SobotStringUtils.isNoEmpty(tempAiMsgId)) {
                        messageAdapter.removeByMsgId(tempAiMsgId);
                    }
                    break;
                case ZhiChiConstant.hander_ai_robot_message:
                    //处理aiagent消息
                    ZhiChiMessageBase aiMsg = (ZhiChiMessageBase) msg.obj;
                    if (aiMsg == null) {
                        LogUtils.d("===收到消息1===msg.obj为空");
                        return;
                    }
                    if (SobotStringUtils.isNoEmpty(aiMsg.getAiAgentCid()) && initModel != null && !aiMsg.getAiAgentCid().equals(initModel.getAiAgentCid())) {
                        //大模型机器人返回的消息里边aiagentcid 如果和初始化返回的不一样，就覆盖初始化返回的，用户能一直和大模型机器人聊天
                        initModel.setAiAgentCid(aiMsg.getAiAgentCid());
                        SharedPreferencesUtil.saveObject(mAppContext,
                                ZhiChiConstant.sobot_last_current_initModel, initModel);
                    }
                    LogUtils.d("===收到消息1===" + aiMsg.getRobotAnswerMessageType() + "==type=" + type);
                    String loadingId = aiMsg.getId();
                    if (SobotStringUtils.checkStringIsNull(loadingId).contains("aiagent") && messageAdapter.getMsgInfoPosition(loadingId) >= 0) {
                        //删除三个点动画消息，同时再删除的位置添加新的消息
                        aiMsg.setId(aiMsg.getMsgId());
                        if (aiMsg.getVariableValueEnums() != null && aiMsg.getVariableValueEnums().length > 0) {
                            //大模型按钮消息
                            aiMsg.setSenderType(ZhiChiConstant.message_sender_type_aiagent_button);
                        }
                        messageAdapter.removeAndAddAIMsgDataByMsgId(SobotStringUtils.checkStringIsNull(loadingId), aiMsg);
                        gotoLastItemWithOffset();
                    } else {
                        if (aiMsg.getVariableValueEnums() != null && aiMsg.getVariableValueEnums().length > 0) {
                            //大模型按钮消息
                            aiMsg.setSenderType(ZhiChiConstant.message_sender_type_aiagent_button);
                        }
                        //消息
                        if ("2".equals(aiMsg.getSendStatus())) {
                            //发送成功
                            aiMsg.setSendSuccessState(1);
                            if (aiRobotRealuateConfigInfo != null && aiRobotRealuateConfigInfo.getRealuateFlag() == 1) {
                                //大模型顶踩开启了
                                aiMsg.setRevaluateState(1);
                            }
                            messageAdapter.updateAIDataById(aiMsg.getMsgId(), aiMsg, true);
                            //结束了才走转人工逻辑
                            if (aiMsg.getTransferType() > 0) {
                                //转人工 MESSAGE 是拼接消息，不转人工
                                //判断是否显示转人工提示语
                                showTransferPrompt();
                                transfer2Custom(0, null, null, null, null, true, aiMsg.getTransferType(), null, null, "0", aiMsg.getMsgId(), null);
                            }
                            gotoLastItemWithOffset();
                        } else {
                            //更新消息
                            messageAdapter.updateAIDataById(aiMsg.getMsgId(), aiMsg, false);
                            if (SobotStringUtils.isNoEmpty(aiMsg.getT())) {
                                long t = Long.parseLong(aiMsg.getT());
                                if ((t - goToLastTime) > 800) {
                                    goToLastTime = t;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            gotoLastItemWithOffset();
                                        }
                                    }, 50);
                                }
                            }
                        }
                    }
                    break;
                case ZhiChiConstant.hander_robot_message:
                    ZhiChiMessageBase zhiChiMessageBasebase = (ZhiChiMessageBase) msg.obj;
                    zhiChiMessageBasebase.setT(Calendar.getInstance().getTime().getTime() + "");
                    if (type == ZhiChiConstant.type_robot_first || type == ZhiChiConstant.type_custom_first) {
                        //智能客服模式下，特定问题类型的机器人回答语下显示“转人工”按钮。
                        if (initModel != null && ChatUtils.checkManualType(initModel.getManualType(),
                                zhiChiMessageBasebase) && current_client_model != ZhiChiConstant.client_model_customService) {
                            //如果此项在工作台上勾选 那就显示转人工按钮
                            if (!StringUtils.isEmpty(zhiChiMessageBasebase.getFromQuickMenuType()) && zhiChiMessageBasebase.getFromQuickMenuType().equals("1")) {
                                //快捷菜单--发送内部知识库，不显示顶踩、转人工
                                zhiChiMessageBasebase.setRevaluateState(0);
                                zhiChiMessageBasebase.setShowTransferBtn(false);
                            } else {
                                zhiChiMessageBasebase.setShowTransferBtn(true);
                            }
                        }
                    }
                    //是否多消息
                    boolean isManyAnswers = false;
                    int msgAnswersStartIndex = 0;
                    if (zhiChiMessageBasebase.getAnswers() != null && zhiChiMessageBasebase.getAnswers().size() > 1) {
                        //多消息
                        isManyAnswers = true;
                        msgAnswersNum = zhiChiMessageBasebase.getAnswers().size();
                        msgAnswersStartIndex = messageAdapter.getItemCount();
                        LogUtils.d("=====msgAnswersNum=" + msgAnswersNum + ",msgAnswersStartIndex=" + msgAnswersStartIndex);
                    }
                    // 1 直接回答，2 理解回答，3 不能回答, 4引导回答，5、本地寒暄，6互联网寒暄，
                    // 7 私有寒暄（包括第三方天气、快递接口）,8百科, 9 向导回答,10 业务接口
                    //后台app 客服设置 评价机器人推送开关 打开后，类型是1，2, 9，11，12，14显示 152 开头的多伦回话
                    //1525 多伦工单节点不显示顶踩 3.1.1新增
                    if (ZhiChiConstant.type_answer_direct.equals(zhiChiMessageBasebase.getAnswerType())
                            || ZhiChiConstant.type_answer_wizard.equals(zhiChiMessageBasebase.getAnswerType())
                            || "1".equals(zhiChiMessageBasebase.getAnswerType())
                            || "2".equals(zhiChiMessageBasebase.getAnswerType())
                            || "11".equals(zhiChiMessageBasebase.getAnswerType())
                            || "12".equals(zhiChiMessageBasebase.getAnswerType())
                            || "14".equals(zhiChiMessageBasebase.getAnswerType()) || (!TextUtils.isEmpty(zhiChiMessageBasebase.getAnswerType()) && zhiChiMessageBasebase.getAnswerType().startsWith("152"))) {
                        if (initModel != null && initModel.isRealuateFlag() && current_client_model != ZhiChiConstant.client_model_customService) {
                            //顶踩开关打开 显示顶踩按钮
                            zhiChiMessageBasebase.setRevaluateState(1);
                            if ((ZhiChiConstant.message_sender_type_robot_guide == zhiChiMessageBasebase.getSenderType()) || (!TextUtils.isEmpty(zhiChiMessageBasebase.getAnswerType()) && "1525".equals(zhiChiMessageBasebase.getAnswerType()))) {
                                //如果是引导问题，不能显示顶踩
                                zhiChiMessageBasebase.setRevaluateState(0);
                                //如果是引导问题，不能显示转人工
                                zhiChiMessageBasebase.setShowTransferBtn(false);
                            }
                            if (!StringUtils.isEmpty(zhiChiMessageBasebase.getFromQuickMenuType()) && zhiChiMessageBasebase.getFromQuickMenuType().equals("1")) {
                                //快捷菜单--发送内部知识库，不显示顶踩、转人工
                                zhiChiMessageBasebase.setRevaluateState(0);
                                zhiChiMessageBasebase.setShowTransferBtn(false);
                            }
                        } else {
                            //顶踩开关打开 隐藏顶踩按钮
                            zhiChiMessageBasebase.setRevaluateState(0);
                        }
                    }

                    if (zhiChiMessageBasebase.getAnswer() != null && zhiChiMessageBasebase.getAnswer().getMultiDiaRespInfo() != null
                            && zhiChiMessageBasebase.getAnswer().getMultiDiaRespInfo().getEndFlag()) {
                        // 多轮会话结束时禁用所有多轮会话可点击选项
                        restMultiMsg();
                        SobotMultiDiaRespInfo multiDiaRespInfo = zhiChiMessageBasebase.getAnswer().getMultiDiaRespInfo();
                        if (multiDiaRespInfo.getEndFlag() && "1525".equals(zhiChiMessageBasebase.getAnswerType()) && !TextUtils.isEmpty(multiDiaRespInfo.getLeaveTemplateId())) {
                            mulitDiaToLeaveMsg(multiDiaRespInfo.getLeaveTemplateId(), "");
                        }
                    }
                    SobotKeyWordTransfer keyWordTransfer = zhiChiMessageBasebase.getSobotKeyWordTransfer();
                    SobotSemanticsKeyWordTransfer semanticsKeyWordTransfer = zhiChiMessageBasebase.getSemanticsKeyWordTransfer();
                    if (keyWordTransfer != null) {
                        //关键词转人工
                        if (type != ZhiChiConstant.type_robot_only) {
                            if (1 == keyWordTransfer.getTransferFlag()) {
//                                transferFlag=1或3：
//                                queueFlag=1:展示提示语，不展示机器人回复，触发转人工逻辑
//                                        queueFlag=0:
//                                onlineFlag:1 表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
//                                onlineFlag:2 表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
//                                onlineFlag:3 表示无客服在线 （不执行转人工，展示机器人回复）
                                if (keyWordTransfer.isQueueFlag()) {
                                    //展示提示语，不展示机器人回复，触发转人工逻辑
                                    addKeyWordTipMsg(keyWordTransfer);
                                    transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else {
                                    if (keyWordTransfer.getOnlineFlag() == 1) {
                                        //表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
                                        addKeyWordTipMsg(keyWordTransfer);
                                        transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                    } else if (keyWordTransfer.getOnlineFlag() == 2) {
                                        //表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
                                        transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                    } else if (keyWordTransfer.getOnlineFlag() == 3) {
                                        //表示无客服在线 （不执行转人工，展示机器人回复）
                                        messageAdapter.justAddData(zhiChiMessageBasebase);
                                    }
                                }
                            } else if (2 == keyWordTransfer.getTransferFlag()) {
                                //不展示机器人回复，展示选择技能组文案
                                //转给多个技能组（一个消息cell），用户可以选择
                                ZhiChiMessageBase keyWordBase = new ZhiChiMessageBase();
                                keyWordBase.setSenderFace(zhiChiMessageBasebase.getSenderFace());
                                keyWordBase.setSenderType(ZhiChiConstant.message_sender_type_robot_keyword_msg);
                                keyWordBase.setSenderName(zhiChiMessageBasebase.getSenderName());
                                keyWordBase.setSobotKeyWordTransfer(keyWordTransfer);
                                messageAdapter.justAddData(keyWordBase);
                            } else if (3 == keyWordTransfer.getTransferFlag()) {
                                if (keyWordTransfer.isQueueFlag()) {
                                    //展示提示语，不展示机器人回复，触发转人工逻辑
                                    addKeyWordTipMsg(keyWordTransfer);
                                    //默认，按正常转人工的逻辑走
                                    transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else {
                                    if (keyWordTransfer.getOnlineFlag() == 1) {
                                        //表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
                                        addKeyWordTipMsg(keyWordTransfer);
                                        //默认，按正常转人工的逻辑走
                                        transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                    } else if (keyWordTransfer.getOnlineFlag() == 2) {
                                        //表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
                                        //默认，按正常转人工的逻辑走
                                        transfer2Custom(keyWordTransfer.getGroupId(), keyWordTransfer.getKeyword(), keyWordTransfer.getKeywordId(), keyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                    } else if (keyWordTransfer.getOnlineFlag() == 3) {
                                        //表示无客服在线 （不执行转人工，展示机器人回复）
                                        messageAdapter.justAddData(zhiChiMessageBasebase);
                                        gotoLastItem();
                                    }
                                }
                            }
                        } else {
                            //展示机器人回复
                            messageAdapter.justAddData(zhiChiMessageBasebase);
                        }
                    } else if (semanticsKeyWordTransfer != null) {
                        //语义转人工
                        if (type != ZhiChiConstant.type_robot_only) {
                            SobotConnCusParam param = new SobotConnCusParam();
                            param.setSemanticsKeyWordId(StringUtils.checkStringIsNull(semanticsKeyWordTransfer.getSemanticsKeyWordId()));
                            param.setSemanticsKeyWordName(StringUtils.checkStringIsNull(semanticsKeyWordTransfer.getSemanticsKeyWordName()));
                            param.setSemanticsKeyWordQuestion(StringUtils.checkStringIsNull(semanticsKeyWordTransfer.getSemanticsKeyWordQuestion()));
                            param.setSemanticsKeyWordQuestionId(StringUtils.checkStringIsNull(semanticsKeyWordTransfer.getSemanticsKeyWordQuestionId()));
                            if (1 == semanticsKeyWordTransfer.getTransferFlag()) {
//                                transferFlag=1或3：
//                                queueFlag=1:展示提示语，不展示机器人回复，触发转人工逻辑
//                                        queueFlag=0:
//                                onlineFlag:1 表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
//                                onlineFlag:2 表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
//                                onlineFlag:3 表示无客服在线 （不执行转人工，展示机器人回复）
                                if (semanticsKeyWordTransfer.isQueueFlag()) {
                                    //展示提示语，不展示机器人回复，触发转人工逻辑
                                    addSemanticsKeyWordKeyMsg(semanticsKeyWordTransfer);
                                    transfer2Custom(semanticsKeyWordTransfer.getGroupId(), semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                } else {
                                    if (semanticsKeyWordTransfer.getOnlineFlag() == 1) {
                                        //表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
                                        addSemanticsKeyWordKeyMsg(semanticsKeyWordTransfer);
                                        transfer2Custom(semanticsKeyWordTransfer.getGroupId(), semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                    } else if (semanticsKeyWordTransfer.getOnlineFlag() == 2) {
                                        //表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
                                        transfer2Custom(semanticsKeyWordTransfer.getGroupId(), semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                    } else if (semanticsKeyWordTransfer.getOnlineFlag() == 3) {
                                        //表示无客服在线 （不执行转人工，展示机器人回复）
                                        messageAdapter.justAddData(zhiChiMessageBasebase);
                                    }
                                }
                            } else if (2 == semanticsKeyWordTransfer.getTransferFlag()) {
                                //不展示机器人回复，展示选择技能组文案
                                //转给多个技能组（一个消息cell），用户可以选择
                                ZhiChiMessageBase keyWordBase = new ZhiChiMessageBase();
                                keyWordBase.setSenderFace(zhiChiMessageBasebase.getSenderFace());
                                keyWordBase.setSenderType(ZhiChiConstant.message_sender_type_robot_semantics_keyword_msg);
                                keyWordBase.setSenderName(zhiChiMessageBasebase.getSenderName());
                                keyWordBase.setSemanticsKeyWordTransfer(semanticsKeyWordTransfer);
                                messageAdapter.justAddData(keyWordBase);
                            } else if (3 == semanticsKeyWordTransfer.getTransferFlag()) {
                                if (semanticsKeyWordTransfer.isQueueFlag()) {
                                    //展示提示语，不展示机器人回复，触发转人工逻辑
                                    addSemanticsKeyWordKeyMsg(semanticsKeyWordTransfer);
                                    //默认，按正常转人工的逻辑走
                                    transfer2Custom("", semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                } else {
                                    if (semanticsKeyWordTransfer.getOnlineFlag() == 1) {
                                        //表示有客服在线可接入（展示提示语，不展示机器人回复，触发转人工逻辑）
                                        addSemanticsKeyWordKeyMsg(semanticsKeyWordTransfer);
                                        //默认，按正常转人工的逻辑走
                                        transfer2Custom("", semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                    } else if (semanticsKeyWordTransfer.getOnlineFlag() == 2) {
                                        //表示需要弹出分组接待（不展示提示语，不展示机器人回复，触发转人工逻辑）
                                        //默认，按正常转人工的逻辑走
                                        transfer2Custom("", semanticsKeyWordTransfer.isQueueFlag(), zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId(), param);
                                    } else if (semanticsKeyWordTransfer.getOnlineFlag() == 3) {
                                        //表示无客服在线 （不执行转人工，展示机器人回复）
                                        messageAdapter.justAddData(zhiChiMessageBasebase);
                                    }
                                }
                            }
                        } else {
                            //展示机器人回复
                            messageAdapter.justAddData(zhiChiMessageBasebase);
                        }
                    } else {
                        if (zhiChiMessageBasebase.getAnswer() != null && StringUtils.isNoEmpty(zhiChiMessageBasebase.getAnswerType()) && "1526".equals(zhiChiMessageBasebase.getAnswerType())) {
                            //多轮 1526 转人工节点
                            SobotMultiDiaRespInfo multiDiaRespInfo = zhiChiMessageBasebase.getAnswer().getMultiDiaRespInfo();
                            String msgStr = StringUtils.stripHtml(ChatUtils.getMultiMsgTitle(multiDiaRespInfo));
                            ZhiChiMessageBase messageBase = ChatUtils.getTipByText(StringUtils.checkStringIsNull(msgStr));
                            messageAdapter.justAddData(messageBase);
                            if (StringUtils.isNoEmpty(zhiChiMessageBasebase.getNodeTransferFlag())) {
                                //1526类型 写死transferType=11（转人工类型）,  activeTransfer=0(机器人人触发)
                                if ("1".equals(zhiChiMessageBasebase.getNodeTransferFlag())) {
                                    //指定客服转 需要同时设置强转
                                    transfer2Custom(ZhiChiConstant.SOBOT_TYEP_TRANSFER_CUSTOM_DUOLUN1526, StringUtils.checkStringIsNull(zhiChiMessageBasebase.getTransferTargetId()), null, null, null, true, 11, zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else if ("2".equals(zhiChiMessageBasebase.getNodeTransferFlag())) {
                                    //指定技能组转
                                    transfer2Custom(0, null, StringUtils.checkStringIsNull(zhiChiMessageBasebase.getTransferTargetId()), null, null, true, 11, zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else {
                                    //默认转
                                    transfer2Custom(0, null, null, null, null, true, 11, zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                }
                            }
                        } else {
                            messageAdapter.justAddData(zhiChiMessageBasebase);
                        }
                        if (type != ZhiChiConstant.type_robot_only) {
                            //仅机器人不触发转人工
                            if (zhiChiMessageBasebase.getTransferType() == 1
                                    || zhiChiMessageBasebase.getTransferType() == 2
                                    || zhiChiMessageBasebase.getTransferType() == 3
                                    || zhiChiMessageBasebase.getTransferType() == 5) {
                                //1重复提问、2情绪负向、3关键字转人工、 5自动转人工  转人工
                                if (zhiChiMessageBasebase.getTransferType() == 5) {
//                                    6. 理解回答转人工 0/1
//                                    7. 引导回答转人工 0/1
//                                    8. 未知回答转人工0/1
//                                    9. 点踩转人工 1
                                    int transferType = 5;
                                    if ("1".equals(zhiChiMessageBasebase.getAnswerType())) {
                                        transferType = 6;
                                    } else if ("2".equals(zhiChiMessageBasebase.getAnswerType())) {
                                        transferType = 7;
                                    } else if ("4".equals(zhiChiMessageBasebase.getAnswerType())) {
                                        transferType = 8;
                                    } else if ("3".equals(zhiChiMessageBasebase.getAnswerType())) {
                                        transferType = 9;
                                    }
                                    //判断是否显示转人工提示语
                                    showTransferPrompt();
                                    transfer2Custom(0, null, null, null, null, true, transferType, zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else if (zhiChiMessageBasebase.getTransferType() == 3) {
                                    //关键字转人工
                                    transfer2Custom(0, null, null, zhiChiMessageBasebase.getSobotKeyWordTransfer().getKeyword(), zhiChiMessageBasebase.getSobotKeyWordTransfer().getKeywordId(), true, zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                } else {
                                    showTransferPrompt();
                                    transfer2Custom(0, null, null, null, null, true, zhiChiMessageBasebase.getTransferType(), zhiChiMessageBasebase.getDocId(), zhiChiMessageBasebase.getOriginQuestion(), "0", zhiChiMessageBasebase.getMsgId(), zhiChiMessageBasebase.getRuleId());
                                }
                            }
                        }
                    }
                    if (SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key()).getInitModel() != null) {
                        //机器人接口比较慢的情况下 用户销毁了view 依旧需要保存好机器人回答
                        SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key()).addMessage(zhiChiMessageBasebase);
                    }
                    // 智能转人工：机器人优先时，如果未知问题或者向导问题则显示转人工
                    if (type == ZhiChiConstant.type_robot_first && (ZhiChiConstant.type_answer_unknown.equals(zhiChiMessageBasebase
                            .getAnswerType()) || ZhiChiConstant.type_answer_guide.equals(zhiChiMessageBasebase
                            .getAnswerType()))) {
                        showTransferCustomer();
                    }
                    if (isManyAnswers) {
                        if (rvScrollLayoutManager != null && messageRV != null && messageAdapter != null) {
                            if (zhiChiMessageBasebase.getAnswers() != null) {
                                gotoIndexItem(messageAdapter.getItemCount() - zhiChiMessageBasebase.getAnswers().size() - 1);
                            }
                        } else {
                            gotoLastItem();
                        }
                    } else {
                        gotoIndexItem(messageAdapter.getItemCount() - 2);
                    }
                    break;
                // 修改语音的发送状态
                case ZhiChiConstant.message_type_update_voice:
                    updateVoiceStatusMessage(messageAdapter, msg);
                    gotoLastItem();
                    break;
                case ZhiChiConstant.message_type_cancel_voice://取消未发送的语音
                    cancelUiVoiceMessage(messageAdapter, msg);
                    break;
                case ZhiChiConstant.hander_sendPicStatus_success:
                    isAboveZero = true;
                    setTimeTaskMethod(handler);
                    ZhiChiMessage zhiChiMessage = (ZhiChiMessage) msg.obj;
                    if (zhiChiMessage != null && zhiChiMessage.getData() != null) {
                        if (current_client_model == ZhiChiConstant.client_model_robot) {
                            //如果当前模式是机器人模式，就把上传的图片的url 发给机器人，只显示问答的结果
                            sendHttpRobotMessage("1", zhiChiMessage.getData().getMsgId(), zhiChiMessage.getData().getUrl(), initModel.getPartnerid(),
                                    initModel.getCid(), "", handler, 1, "", info.getLocale(), "", null);
                        }
                        messageAdapter.updateMessageByMsgId(zhiChiMessage.getData().getMsgId(), zhiChiMessage.getData());
                        updateUiMessageStatus(messageAdapter, zhiChiMessage.getData().getMsgId(), ZhiChiConstant.MSG_SEND_STATUS_SUCCESS, 0);
                    }
                    break;
                case ZhiChiConstant.hander_sendPicStatus_fail:
                    String resultId = (String) msg.obj;
                    updateUiMessageStatus(messageAdapter, resultId, ZhiChiConstant.MSG_SEND_STATUS_ERROR, 0);
                    break;
                case ZhiChiConstant.hander_sendPicIsLoading:
                    String loadId = (String) msg.obj;
                    int uploadProgress = msg.arg1;
                    updateUiMessageStatus(messageAdapter, loadId, ZhiChiConstant.MSG_SEND_STATUS_LOADING, uploadProgress);
                    break;
                case ZhiChiConstant.hander_timeTask_custom_isBusying: // 客服的定时任务
                    // --客服忙碌
                    updateUiMessage(messageAdapter, msg);
                    LogUtils.i("客服的定时任务:" + noReplyTimeCustoms);
                    stopCustomTimeTask();
                    break;
                case ZhiChiConstant.hander_timeTask_userInfo:// 客户的定时任务
                    updateUiMessage(messageAdapter, msg);
                    stopUserInfoTimeTask();
                    LogUtils.i("客户的定时任务的时间  停止定时任务：" + noReplyTimeUserInfo);
                    break;
                case ZhiChiConstant.voiceIsRecoding:
                    // 录音的时间超过一分钟的时间切断进行发送语音
                    if (voiceTimerLong >= minRecordTime * 1000) {
                        isCutVoice = true;
                        voiceCuttingMethod();
                        voiceTimerLong = 0;
//                        recording_hint.setText(R.string.sobot_voiceTooLong);
                        tv_recording_countdown_hint.setVisibility(View.INVISIBLE);
                        iv_sound_recording_in_progress.setVisibility(View.GONE);
                        iv_sound_recording_cancle.setVisibility(View.GONE);
                        closeVoiceWindows(2);
                        btn_press_to_speak.setPressed(false);
                        currentVoiceLong = 0;
                    } else {
                        final int time = Integer.parseInt(msg.obj.toString());
//					LogUtils.i("录音定时任务的时长：" + time);
                        currentVoiceLong = time;
                        if (time < recordDownTime * 1000) {
                            tv_recording_countdown_hint.setVisibility(View.INVISIBLE);
                            if (time % 1000 == 0) {
                                voiceTimeLongStr = TimeTools.instance.calculatTime(time);
                                voice_time_long.setText(voiceTimeLongStr.substring(3) + "''");
                            }
                        } else if (time < minRecordTime * 1000) {
                            if (time % 1000 == 0) {
                                voiceTimeLongStr = TimeTools.instance.calculatTime(time);
                                voice_time_long.setText(voiceTimeLongStr.substring(3) + "''");
                                tv_recording_countdown_hint.setVisibility(View.VISIBLE);
                                tv_recording_countdown_hint.setText(getResources().getString(R.string.sobot_count_down) + " " + (minRecordTime * 1000 - time) / 1000 + "''");
                            }
                        } else {
                            tv_recording_countdown_hint.setVisibility(View.INVISIBLE);
//                            voice_time_long.setText(R.string.sobot_voiceTooLong);
                        }
                    }
                    break;
                case ZhiChiConstant.hander_close_voice_view:
                    int longOrShort = msg.arg1;
                    txt_speak_content.setText(R.string.sobot_press_say);
                    currentVoiceLong = 0;
                    ll_sound_recording.setVisibility(View.GONE);

                    if (longOrShort == 0) {
                        for (int i = messageList.size() - 1; i > 0; i--) {
                            if (messageList.get(i).getSenderType() == 8) {
                                messageList.remove(i);
                                break;
                            }
                        }
                    }
                    break;
                case ZhiChiConstant.hander_btn_emotion_view:
                    mPanelLayout.getLayoutParams().height = ScreenUtils.dip2px(getSobotActivity(), 250);
                    break;
                case ZhiChiConstant.hander_btn_upload_view:
                    mPanelLayout.getLayoutParams().height = ScreenUtils.dip2px(getSobotActivity(), 200);
                    break;
                case ZhiChiConstant.hander_comment_finish:
                    CustomToast.makeText(getSobotActivity(),
                            ResourceUtils.getResString(getSobotActivity(), "sobot_thank_dialog_hint"), 1000,
                            ResourceUtils.getDrawableId(getSobotActivity(), "sobot_icon_success")).show();
                    boolean isFinish = (boolean) msg.obj;
                    LogUtils.d("========isFinish===" + isFinish);
                    LogUtils.d("======getSobotActivity().isFinishing()=====" + getSobotActivity().isFinishing());
                    if (isFinish && !getSobotActivity().isFinishing()) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getSobotActivity().finish();
                            }
                        }, 500);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //关键词转人工 显示后台设置的提示语 2.9.9添加
    private void addKeyWordTipMsg(SobotKeyWordTransfer keyWordTransfer) {
        if (!TextUtils.isEmpty(keyWordTransfer.getTransferTips())) {
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setT(Calendar.getInstance().getTime().getTime() + "");
            base.setId(getMsgId());
            base.setMsgId(getMsgId());
            base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            reply.setRemindType(ZhiChiConstant.sobot_remind_type_simple_tip);
            reply.setMsg(keyWordTransfer.getTransferTips());
            base.setAnswer(reply);
            messageAdapter.justAddData(base);
        }
    }

    //语义转人工 显示后台设置的提示语 4.1.8添加
    private void addSemanticsKeyWordKeyMsg(SobotSemanticsKeyWordTransfer keyWordTransfer) {
        if (!TextUtils.isEmpty(keyWordTransfer.getTransferTips())) {
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setT(Calendar.getInstance().getTime().getTime() + "");
            base.setId(getMsgId());
            base.setMsgId(getMsgId());
            base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            reply.setRemindType(ZhiChiConstant.sobot_remind_type_simple_tip);
            reply.setMsg(keyWordTransfer.getTransferTips());
            base.setAnswer(reply);
            messageAdapter.justAddData(base);
        }
    }

    protected void initData() {
        if (StatusBarUtil.SOBOT_STATUS_HIGHT == 0) {
            try {
                View decorView = getSobotActivity().getWindow().getDecorView();
                if (decorView != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                            int statusBarHeight = insets.getSystemWindowInsetTop();
                            SobotLogUtils.d("状态栏高度: " + statusBarHeight);
                            StatusBarUtil.SOBOT_STATUS_HIGHT = statusBarHeight;
                            SobotSharedPreferencesUtil.getInstance(getSobotActivity()).put("SobotStatusBarHeight", statusBarHeight);
                            updateToolBarBg();
                            return insets;
                        }
                    });
                } else {
                    updateToolBarBg();
                }
            } catch (Exception e) {
                updateToolBarBg();
            }
        } else {
            updateToolBarBg();
        }
        setToolBar();
        initBrocastReceiver();
        initListener();
        initMessageRecyclerView();
        loadUnreadNum();
        //如果进入页面没咨询过，返回时记录当前时间，下次再进入时计算:    当前时间 - 上次页面关闭时间 =时间差
        //比较时间差和用户超时时间， 如果大于用户超时时间，就重新调用初始化接口 ，使用新的cid ,
        //避免长时间不咨询再次进来，会话的创建时间还是很早之前的，保证cid的准确性
        boolean isReCon = false;
        ZhiChiConfig config = SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key());
        if (config != null && config.getInitModel() != null && !config.isAboveZero) {
            long pre_finish_time = SharedPreferencesUtil.getLongData(getSobotActivity(), ZhiChiConstant.SOBOT_FINISH_CURTIME, System.currentTimeMillis());
            long cur_tiem_cha = System.currentTimeMillis() - pre_finish_time;
            if (!TextUtils.isEmpty(config.getInitModel().getUserOutTime()) && pre_finish_time > 0) {
                long userOutTime = Long.parseLong(config.getInitModel().getUserOutTime()) * 60 * 1000;
                isReCon = (cur_tiem_cha - userOutTime) > 0 ? true : false;
                LogUtils.i("进入当前界面减去上次界面关闭的时间差：" + cur_tiem_cha + " ms");
                LogUtils.i("用户超时时间：" + userOutTime + " ms");
                LogUtils.i("是否需要重新初始化：" + isReCon);
            }
        }
        initSdk(isReCon, 1);
        //关闭SobotSessionServer里的定时器
        Intent intent = new Intent();
        intent.setAction(ZhiChiConstants.SOBOT_TIMER_BROCAST);
        intent.putExtra("isStartTimer", false);
        localBroadcastManager.sendBroadcast(intent);

    }


    /**
     * 导航栏渐变逻辑
     * 先判断客户开发是否设置，如果设置了 直接使用；如果没有修改（和系统默认一样），就就绪判断后端接口返回的颜色；
     * 如果接口返回的也和系统一样，就不处理（默认渐变色）；如果不一样，直接按照接口的设置渐变色
     */
    private void updateToolBarBg() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (initModel == null) {
                        setLoadingToolBarDefBg();
                        return;
                    }
                    if (relative == null) {
                        return;
                    }
                    if (initModel.getVisitorScheme() != null) {
                        //导航条显示1 开启 0 关闭
                        if (initModel.getVisitorScheme().getTopBarFlag() == 1) {
                            relative.setVisibility(View.VISIBLE);
                        } else {
                            relative.setVisibility(View.GONE);
                        }
                    }

                    if (initModel != null && initModel.getVisitorScheme() != null) {
                        //服务端返回的导航条背景颜色
                        if (!TextUtils.isEmpty(initModel.getVisitorScheme().getTopBarColor())) {
                            String topBarColor[] = initModel.getVisitorScheme().getTopBarColor().split(",");
                            if (topBarColor.length > 1) {
                                int[] colors = new int[topBarColor.length];
                                for (int i = 0; i < topBarColor.length; i++) {
                                    colors[i] = Color.parseColor(topBarColor[i]);
                                }
                                GradientDrawable gradientDrawable = new GradientDrawable();
                                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                                gradientDrawable.setColors(colors); //添加颜色组
                                gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                                gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                                relative.setBackground(gradientDrawable);
                                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
                                } else {
                                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                                    StatusBarUtil.setColor(getSobotActivity(), aDrawable);
                                }
                            }
                        }
                    } else {
                        setToolBarDefBg();
                    }
                }

            }, 0);
        } catch (Exception e) {
        }
    }

    /**
     * 设置加载中导航栏渐变色
     */
    private void setLoadingToolBarDefBg() {
        try {
            int[] colors = new int[]{getResources().getColor(R.color.sobot_color_chat_bg), getResources().getColor(R.color.sobot_color_chat_bg)};
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColors(colors); //添加颜色组
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
            gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
            relative.setBackground(gradientDrawable);
            GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            } else {
                StatusBarUtil.setColor(getSobotActivity(), aDrawable);
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
            relative.setBackground(gradientDrawable);
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            } else {
                GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                StatusBarUtil.setColor(getSobotActivity(), aDrawable);
            }
        } catch (Exception e) {
        }
    }

    private void setToolBar() {
        if (getView() == null) {
            return;
        }
        View rootView = getView();
        View toolBar = rootView.findViewById(R.id.sobot_layout_titlebar);
        sobot_iv_left = rootView.findViewById(R.id.sobot_iv_left);
//        sobot_tv_close.setText(R.string.sobot_colse);//4.0.3去掉文字，只显示图片
        if (toolBar != null) {
            if (sobot_iv_left != null) {
                //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
                displayInNotch(sobot_iv_left);
                sobot_iv_left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onLeftMenuClick();
                    }
                });
            }

            if (sobot_iv_right != null) {
                sobot_iv_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onRightMenuClick(v);
                    }
                });
            }
            if (iv_close != null && info.isShowCloseBtn() && current_client_model == ZhiChiConstant.client_model_customService) {
                //设置导航栏关闭按钮
                iv_close.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initBrocastReceiver() {
        if (receiver == null) {
            receiver = new MyMessageReceiver();
        }
        // 创建过滤器，并指定action，使之用于接收同action的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 检测网络的状态
        // 注册广播接收器
        getSobotActivity().registerReceiver(receiver, filter);

        if (localReceiver == null) {
            localReceiver = new LocalReceiver();
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(mAppContext);
        // 创建过滤器，并指定action，使之用于接收同action的广播
        IntentFilter localFilter = new IntentFilter();
        localFilter.addAction(ZhiChiConstants.receiveMessageBrocast);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_LOCATION);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_TEXT);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_OBJECT);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_CARD);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_ORDER_CARD);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_CUSTOM_CARD);
        localFilter.addAction(ZhiChiConstant.SOBOT_BROCAST_ACTION_TRASNFER_TO_OPERATOR);
        localFilter.addAction(ZhiChiConstants.chat_remind_post_msg);
        localFilter.addAction(ZhiChiConstants.chat_remind_ticket_list);
        localFilter.addAction(ZhiChiConstants.sobot_click_cancle);
        localFilter.addAction(ZhiChiConstants.dcrc_comment_state);/* 人工客服评论成功 */
        localFilter.addAction(ZhiChiConstants.sobot_close_now);/* 立即结束 */
        localFilter.addAction(ZhiChiConstants.sobot_close_now_clear_cache);// 立即结束不留缓存
        localFilter.addAction(ZhiChiConstants.SOBOT_CHANNEL_STATUS_CHANGE);/* 接收通道状态变化 */
        localFilter.addAction(ZhiChiConstants.SOBOT_BROCAST_KEYWORD_CLICK);/* 机器人转人工关键字  用户选择  技能组  转人工 */
        localFilter.addAction(ZhiChiConstants.SOBOT_BROCAST_REMOVE_FILE_TASK);//取消文件上传
        localFilter.addAction(ZhiChiConstants.chat_remind_to_customer);//转人工
        localFilter.addAction(ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_TO_CHATLIST);//多伦工单节点留言弹窗留言提交后回显到聊天列表
        localFilter.addAction(ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_RE_COMMIT);//多伦工单节点提醒点击后重复弹窗
        localFilter.addAction(ZhiChiConstants.SOBOT_POST_MSG_TMP_BROCAST);/*横屏选取完留言模版后跳转到留言界面*/
        localFilter.addAction(ZhiChiConstants.SOBOT_POST_MSG_APPOINT_BROCAST);/*长按引用消息*/
        localFilter.addAction(ZhiChiConstants.SOBOT_BROCAST_SEMANTICS_KEYWORD_CLICK);/* 机器人语义转人工  用户选择  技能组  转人工 */
        localFilter.addAction(ZhiChiConstants.SOBOT_SEND_AI_CARD_MSG);/* 发送大模型卡片 */

        // 注册广播接收器
        localBroadcastManager.registerReceiver(localReceiver, localFilter);
    }

    private void initListener() {
        //监听聊天的面板
        mKPSwitchListener = KeyboardUtil.attach(getSobotActivity(), mPanelLayout,
                new KeyboardUtil.OnKeyboardShowingListener() {
                    @Override
                    public void onKeyboardShowing(boolean isShowing) {
//                        resetEmoticonBtn();
                        if (isShowing) {
                            gotoLastItem();
                        } else {
                            if (currentPanelId == 0) {
                                KPSwitchConflictUtil.hidePanelAndKeyboard(mPanelLayout);
                            }
                        }
                    }
                });
        KPSwitchConflictUtil.attach(mPanelLayout, cb_plus_view, et_sendmessage);

        ll_notReadInfo.setOnClickListener(this);
        btn_send_pic.setOnClickListener(this);
        btn_model_edit.setOnClickListener(this);
        btn_model_voice.setOnClickListener(this);
        ll_switch_robot.setOnClickListener(this);
        iv_close.setOnClickListener(this);
        iv_right_second.setOnClickListener(this);
        iv_right_third.setOnClickListener(this);
        RecyclerView.ItemAnimator animator = messageRV.getItemAnimator();
        if (animator != null) {
            //添加删除item 动画时长都是0
            animator.setRemoveDuration(0);
            animator.setAddDuration(0);
        }
        messageRV.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (voiceTimerLong > 0) {
                    //正在录音 拦截 禁止滑动
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        messageRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // RecyclerView停止滑动，可以在此处做一些操作
                    remarkReadStatus();
//                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
//                    // RecyclerView正在被拖拽滑动，可以在此处做一些操作
//                } else {
//                    // RecyclerView正在自动滑动到某个位置，可以在此处做一些操作
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (rvScrollLayoutManager != null) {
                    // 获取当前可见的第一个item
                    int firstVisiableItem = rvScrollLayoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = rvScrollLayoutManager.findLastCompletelyVisibleItemPosition();
                    // 获取当前可见的item数量
                    int visibleItemCount = lastVisibleItemPosition - firstVisiableItem;
                    int totalItemCount = rvScrollLayoutManager.getItemCount();
                    if (msgAnswersNum > 0) {
                        if ((firstVisiableItem + visibleItemCount < totalItemCount)) {
                            //显示未读条数
                            showNewMsg = true;
                            msgAnswersNum = 0;
                            newMsgNum = totalItemCount - (firstVisiableItem + visibleItemCount - 1);
                        }
                    }
                    // 判断是否滑动到底部
                    if ((totalItemCount > 0) && (lastVisibleItemPosition >= totalItemCount - 1)) {
                        showNewMsg = false;//如果在底部，收到消息之间显示
                    } else {
                        showNewMsg = true;//未滑动到底部，收到消息显示新消息
                    }
                    //更新新消息
                    if (newMsgNum > 0) {
                        int showItem = totalItemCount - lastVisibleItemPosition - 1;
                        if (showItem < newMsgNum) {
                            newMsgNum = showItem;
                            if (newMsgNum == 0) {
                                //隐藏新消息
                                handler.sendEmptyMessage(ZhiChiConstant.hander_hide_newmsg_tip);
                            } else {
                                //显示新消息
                                handler.sendEmptyMessage(ZhiChiConstant.hander_show_newmsg_tip);
                            }

                        }
                    }
//                    LogUtils.d( "===============firstVisiableItem =" + firstVisiableItem + ",visibleItemCount=" + visibleItemCount + ",totalItemCount=" + totalItemCount + ",msgAnswersNum=" + msgAnswersNum);
                    if (unReadMsgIndex > 0 && mUnreadNum > 0) {
                        if (unReadMsgIndex <= firstVisiableItem) {
                            //显示未读提示
                            handler.sendEmptyMessage(ZhiChiConstant.hander_show_unread_tip);
                        }
                        if (messageList.size() > 0 && messageList.size() > firstVisiableItem) {
                            if (messageList.get(firstVisiableItem) != null && messageList.get(firstVisiableItem).getAnswer() != null
                                    && ZhiChiConstant.sobot_remind_type_below_unread == messageList.get(firstVisiableItem).getAnswer().getRemindType()) {
                                hideNotReadLayout();
                            }
                        }
                    } else if (ll_notReadInfo.getVisibility() == View.VISIBLE) {
                        ll_notReadInfo.setVisibility(View.GONE);
                    }
                }

            }
        });
        et_sendmessage.setSobotAutoCompleteListener(this);
        et_sendmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPanelId = 0;
                doEmoticonBtn2Blur();
                cb_plus_view.setChecked(false);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    et_sendmessage.dismissPop();
                }
            }
        });
        et_sendmessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean isFocused) {
                if (isFocused) {
                    int length = et_sendmessage.getText().toString().trim().length();
                    if (length != 0) {
                        btn_send_pic.setVisibility(View.VISIBLE);
                        cb_plus_view.setVisibility(View.GONE);
                    }
                }
            }
        });

        et_sendmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                resetBtnUploadAndSend();
                int lineCount = et_sendmessage.getLineCount();
                if (lineCount > 1) {
                    et_sendmessage.setPadding(0, 0, 0, ScreenUtils.dip2px(getSobotActivity(), 6));
                } else {
                    et_sendmessage.setPadding(0, 0, 0, ScreenUtils.dip2px(getSobotActivity(), 12));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        cb_emoticon_view.setOnClickListener(this);
        cb_emoticon_view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //隐藏键盘
                    try {
                        KeyboardUtil.hideKeyboard(getSobotActivity().getCurrentFocus());
                        // 切换表情面板
                        KPSwitchConflictUtil.showPanel(mPanelLayout);
                        int validPanelHeight = KeyboardUtil.getValidPanelHeight(getSobotActivity());
                        ViewUtil.refreshHeight(mPanelLayout, validPanelHeight);
                        setPanelView(mPanelLayout, cb_emoticon_view.getId());
                        currentPanelId = cb_emoticon_view.getId();
                    } catch (Exception e) {
                    }
                } else {
                    if (currentPanelId == cb_emoticon_view.getId()) {
                        //键盘
                        KPSwitchConflictUtil.showKeyboard(mPanelLayout, et_sendmessage);
                    }
                }
                //切换表情按钮的状态
                gotoLastItem();
            }
        });
        cb_plus_view.setOnClickListener(this);

        btn_press_to_speak.setOnTouchListener(new PressToSpeakListen());
        messageRV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    hidePanelAndKeyboard(mPanelLayout);
                }
                return false;
            }
        });

        //会话结束 底部 开始新会话
        ll_restart_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                initSdk(true, 0);
            }
        });
        //会话结束 底部 留言
        ll_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startToPostMsgActivty(false);
            }
        });
        //会话结束 底部 评价
        ll_satisfaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                submitEvaluation(true, 5, -2, "");
            }
        });
    }

    //初始化消息列表控件
    private void initMessageRecyclerView() {
        rvScrollLayoutManager = new SobotScrollLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        messageRV.setLayoutManager(rvScrollLayoutManager);
        srv_message.setEnableRefresh(true);// 能下拉
        srv_message.setEnableLoadMore(false);//不能加载更多
        srv_message.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getHistoryMessage(false);
            }
        });
        messageAdapter = new SobotMsgAdapter(getSobotActivity(), messageList, this);
        messageRV.setAdapter(messageAdapter);

    }

    private void remarkReadStatus() {
        //开关是否开启
        if (unReadMsgIds != null && unReadMsgIds.size() > 0) {
            //如果未读集合大于1，请求已读接口
            final JSONArray array = new JSONArray();
            for (String id : unReadMsgIds.keySet()) {
                ZhiChiMessageBase messageBase = unReadMsgIds.get(id);
                if (messageBase != null && messageBase.getReadStatus() != 2 && !TextUtils.isEmpty(messageBase.getCid())) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("cid", messageBase.getCid());
                        object.put("msgId", messageBase.getMsgId());
                    } catch (Exception e) {
//                                e.printStackTrace();
                    }
                    array.put(object);
                }

            }
            if (array.length() > 0) {
                final Set<String> keys = unReadMsgIds.keySet();
                zhiChiApi.realMarkReadToAdmin(initModel.getPartnerid(), array, new StringResultCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        //清空未读
                        for (String key : keys) {
                            ZhiChiMessageBase messageBase = unReadMsgIds.get(key);
                            if (messageBase != null) {
                                messageBase.setReadStatus(2);
                                unReadMsgIds.put(key, messageBase);
                            } else {
                                unReadMsgIds.remove(key);
                            }
                        }
                        try {
                            messageAdapter.notifyDataSetChanged();
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onFailure(Exception e, String s) {

                    }
                });
            }
        }
    }

    /**
     * 按住说话动画开始
     */
    private void startMicAnimate() {
        iv_sound_recording_in_progress.setBackgroundResource(R.drawable.sobot_voice_animation);
        animationDrawable = (AnimationDrawable) iv_sound_recording_in_progress.getBackground();
        iv_sound_recording_in_progress.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable.start();
            }
        });

        iv_sound_recording_cancle.setBackgroundResource(R.drawable.sobot_voice_animation_cancle);
        cancleAnimationDrawable = (AnimationDrawable) iv_sound_recording_cancle.getBackground();
        iv_sound_recording_cancle.post(new Runnable() {
            @Override
            public void run() {
                cancleAnimationDrawable.start();
            }
        });
        tv_recording_hint.setText(R.string.sobot_move_up_to_cancel);
    }

    public void closeVoiceWindows(int toLongOrShort) {
        Message message = handler.obtainMessage();
        message.what = ZhiChiConstant.hander_close_voice_view;
        message.arg1 = toLongOrShort;
        handler.sendMessageDelayed(message, 500);
    }

    // 当时间超过1秒的时候自动发送
    public void voiceCuttingMethod() {
        stopVoice();
        sendVoiceMap(1, voiceMsgId);
        voice_time_long.setText("59" + "''");
    }

    /**
     * 开始录音
     */
    private void startVoice() {
        try {
            stopVoice();
            mFileName = SobotPathManager.getInstance().getVoiceDir() + UUID.randomUUID().toString() + ".wav";
            String state = android.os.Environment.getExternalStorageState();
            if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                LogUtils.i("sd卡被卸载了");
            }
            File directory = new File(mFileName).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                LogUtils.i("文件夹创建失败");
            }
            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
            extAudioRecorder.setOutputFile(mFileName);
            extAudioRecorder.prepare();
            extAudioRecorder.start(new ExtAudioRecorder.AudioRecorderListener() {
                @Override
                public void onHasPermission() {
                    startMicAnimate();
                    startVoiceTimeTask(handler);
                    sendVoiceMap(0, voiceMsgId);
                }

                @Override
                public void onNoPermission() {
                    ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_no_record_audio_permission));
                }
            });
        } catch (Exception e) {
            LogUtils.i("prepare() failed");
        }
    }

    /* 停止录音 */
    private void stopVoice() {
        /* 布局的变化 */
        try {
            if (extAudioRecorder != null) {
                stopVoiceTimeTask();
                extAudioRecorder.stop();
                extAudioRecorder.release();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 录音的时间控制
     */
    public void startVoiceTimeTask(final Handler handler) {
        voiceTimerLong = 0;
        stopVoiceTimeTask();
        voiceTimer = new Timer();
        voiceTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                sendVoiceTimeTask(handler);
            }
        };
        // 500ms进行定时任务
        voiceTimer.schedule(voiceTimerTask, 0, 500);

    }

    /**
     * 发送声音的定时的任务
     *
     * @param handler
     */
    public void sendVoiceTimeTask(Handler handler) {
        Message message = handler.obtainMessage();
        message.what = ZhiChiConstant.voiceIsRecoding;
        voiceTimerLong = voiceTimerLong + 500;
        message.obj = voiceTimerLong;
        handler.sendMessage(message);
    }

    public void stopVoiceTimeTask() {
        if (voiceTimer != null) {
            voiceTimer.cancel();
            voiceTimer = null;
        }
        if (voiceTimerTask != null) {
            voiceTimerTask.cancel();
            voiceTimerTask = null;
        }
        voiceTimerLong = 0;
    }

    /**
     * 发送语音的方式
     *
     * @param type       0：正在录制语音。  1：发送语音。2：取消正在录制的语音显示
     * @param voiceMsgId 语音消息ID
     */
    private void sendVoiceMap(int type, String voiceMsgId) {
        // 发送语音的界面
        if (type == 0) {
//            sendVoiceMessageToHandler(voiceMsgId, mFileName, voiceTimeLongStr, ZhiChiConstant.MSG_SEND_STATUS_ANIM, SEND_VOICE, handler);
        } else if (type == 2) {
//            sendVoiceMessageToHandler(voiceMsgId, mFileName, voiceTimeLongStr, ZhiChiConstant.MSG_SEND_STATUS_ERROR, CANCEL_VOICE, handler);
        } else {
            sendVoiceMessageToHandler(voiceMsgId, mFileName, voiceTimeLongStr, ZhiChiConstant.MSG_SEND_STATUS_ANIM, SEND_VOICE, handler);
            sendVoiceMessageToHandler(voiceMsgId, mFileName, voiceTimeLongStr, ZhiChiConstant.MSG_SEND_STATUS_LOADING, UPDATE_VOICE, handler);
            // 发送http 返回发送成功的按钮
            sendVoice(voiceMsgId, voiceTimeLongStr, initModel.getCid(), initModel.getPartnerid(), mFileName, handler);
        }
        gotoLastItem();
    }

    /**
     * 获取未读消息
     */
    private void loadUnreadNum() {
        mUnreadNum = SobotMsgManager.getInstance(mAppContext).getUnreadCount(info.getApp_key(), true, info.getPartnerid());
    }

    /**
     * 初始化sdk
     *
     * @param isReConnect 是否是重新接入
     **/
    private void initSdk(boolean isReConnect, int isFirstEntry) {
        if (isReConnect) {
            current_client_model = ZhiChiConstant.client_model_robot;
            showTimeVisiableCustomBtn = 0;
            messageList.clear();
            messageAdapter.notifyDataSetChanged();
            cids.clear();
            currentCidPosition = 0;
            queryCidsStatus = ZhiChiConstant.QUERY_CIDS_STATUS_INITIAL;
            isNoMoreHistoryMsg = false;
            isAboveZero = false;
            isComment = false;// 重新开始会话时 重置为 没有评价过
            customerState = CustomerState.Offline;
            remindRobotMessageTimes = 0;
            queueTimes = 0;
            isSessionOver = false;
            isHasRequestQueryFrom = false;

            ll_restart_talk.setVisibility(View.GONE);
            ll_message.setVisibility(View.GONE);
            ll_satisfaction.setVisibility(View.GONE);
            image_reLoading.setVisibility(View.VISIBLE);
            AnimationUtil.rotate(image_reLoading);

            srv_message.setEnableRefresh(true);// 设置下拉刷新列表
            srv_message.setEnableLoadMore(false);// 设置下拉刷新列表

            String last_current_dreceptionistId = SharedPreferencesUtil.getStringData(
                    mAppContext, info.getApp_key() + "_" + ZhiChiConstant.SOBOT_RECEPTIONISTID, "");
            info.setChoose_adminid(last_current_dreceptionistId);
            resetUser(isFirstEntry);
        } else {
            //检查配置项是否发生变化
            if (ChatUtils.checkConfigChange(mAppContext, info.getApp_key(), info)) {
                resetUser(isFirstEntry);
            } else {
                //用户指定语言
                boolean isUseLanguage = SharedPreferencesUtil.getBooleanData(getSobotActivity(), ZhiChiConstant.SOBOT_USE_LANGUAGE, false);
                if (isUseLanguage) {
                    changeAppLanguage();
                }
                doKeepsessionInit(isFirstEntry);
            }
        }
        SharedPreferencesUtil.saveBooleanData(mAppContext,
                "refrashSatisfactionConfig", true);
    }

    /**
     * 重置用户
     */
    private void resetUser(int isFirstEntry) {
        String platformID = SharedPreferencesUtil.getStringData(mAppContext, ZhiChiConstant.SOBOT_PLATFORM_UNIONCODE, "");
        //电商标示为fasle 或者 platformUnionCode 都认为是普通版，重置用户是都要结束会话
        if (!SobotVerControl.isPlatformVer || TextUtils.isEmpty(platformID)) {
            zhiChiApi.disconnChannel();
        }
        clearCache();
        SharedPreferencesUtil.saveStringData(mAppContext,
                info.getApp_key() + "_" + ZhiChiConstant.sobot_last_login_group_id, TextUtils.isEmpty(info.getGroupid()) ? "" : info.getGroupid());
        //重新初始化时，用户如果传语言了，界面先使用指定的语言
        if (!TextUtils.isEmpty(info.getLocale())) {
            if ("zh-Hans".equals(info.getLocale())) {
                //转成中文zh
                info.setLocale("zh");
            }
            changeAppLanguage(info.getLocale());
        }
        customerInit(isFirstEntry);
    }

    /**
     * 调用初始化接口
     */
    private void customerInit(int isFirstEntry) {
        robotWelcomeMsgId = "";//清空机器人欢迎语的msgid，初始化后再次获取
        LogUtils.i("customerInit初始化接口");
        if (!isAppInitEnd) {
            LogUtils.i("初始化接口appinit 接口还没结束，结束前不能重复调用");
            return;
        }
        isAppInitEnd = false;
        if (info != null) {
            info.setIsFirstEntry(isFirstEntry);
        }

        //如果ZCSobotApi的设置了自定应答语，info里没有设置，会把ZCSobotApi的赋值给info
        String robot_Hello_Word = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_ROBOT_HELLO_WORD, "");
        String user_Out_Word = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_USER_OUT_WORD, "");
        String user_Tip_Word = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_USER_TIP_WORD, "");
        String admin_Hello_Word = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_ADMIN_HELLO_WORD, "");
        String admin_Offline_Title = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_ADMIN_OFFLINE_TITLE, "");
        String admin_Tip_Word = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.SOBOT_ADMIN_TIP_WORD, "");

        if (info != null) {
            if (TextUtils.isEmpty(info.getRobot_hello_word()) && !TextUtils.isEmpty(robot_Hello_Word)) {
                info.setRobot_Hello_Word(robot_Hello_Word);
            }
            if (TextUtils.isEmpty(info.getUser_out_word()) && !TextUtils.isEmpty(user_Out_Word)) {
                info.setUser_Out_Word(user_Out_Word);
            }
            if (TextUtils.isEmpty(info.getUser_tip_word()) && !TextUtils.isEmpty(user_Tip_Word)) {
                info.setUser_Tip_Word(user_Tip_Word);
            }
            if (TextUtils.isEmpty(info.getAdmin_hello_word()) && !TextUtils.isEmpty(admin_Hello_Word)) {
                info.setAdmin_Hello_Word(admin_Hello_Word);
            }
            if (TextUtils.isEmpty(info.getAdmin_offline_title()) && !TextUtils.isEmpty(admin_Offline_Title)) {
                info.setAdmin_Offline_Title(admin_Offline_Title);
            }
            if (TextUtils.isEmpty(info.getAdmin_tip_word()) && !TextUtils.isEmpty(admin_Tip_Word)) {
                info.setAdmin_Tip_Word(admin_Tip_Word);
            }
        }
        //隐藏新消息和未读消息布局
        hideNewmsgLayout();
        hideNotReadLayout();
        //隐藏切换机器人布局
        ll_switch_robot.setVisibility(View.GONE);
        zhiChiApi.sobotInit(SobotChatFragment.this, info, new StringResultCallBack<ZhiChiInitModeBase>() {
            @Override
            public void onSuccess(ZhiChiInitModeBase result) {
                initModel = result;
                if (initModel != null) {
                    SharedPreferencesUtil.saveObject(mAppContext,
                            ZhiChiConstant.sobot_last_current_info, info);
                    SharedPreferencesUtil.saveObject(mAppContext,
                            ZhiChiConstant.sobot_last_current_initModel, initModel);
                    if (!TextUtils.isEmpty(initModel.getLanguage())) {
                        //这个时服务端返回的语言
                        changeAppLanguage(initModel.getLanguage());
                    } else {
                        //这个时服务端返回的接待方案里边的兜底语言
                        changeAppLanguage(initModel.getLan());
                    }
                    //只有创建新会话才会切换语言
                    if (initModel.getChooseLanType() == 2 && initModel.getUstatus() == ZhiChiConstant.ustatus_offline) {
                        //切换语言禁止下拉
                        srv_message.setEnableRefresh(false);
                        if (welcome != null) {
                            welcome.setVisibility(View.GONE);
                        }
                        if (chat_main != null) {
                            chat_main.setVisibility(View.VISIBLE);
                        }
                        if (sobot_bar_bottom != null) {
                            sobot_bar_bottom.setVisibility(View.GONE);
                        }
                        if (sobot_iv_right != null) {
                            sobot_iv_right.setVisibility(View.GONE);
                        }
                        isAppInitEnd = true;
                        if (!isActive()) {
                            return;
                        }
                        if (info.getService_mode() > 0) {
                            initModel.setType(info.getService_mode() + "");
                        }
                        type = Integer.parseInt(initModel.getType());
                        //更加主题色，更新UI
                        updateToolBarBg();
                        String tempLanguage = "";
                        if (StringUtils.isNoEmpty(initModel.getLanguage())) {
                            tempLanguage = initModel.getLanguage();
                        } else {
                            tempLanguage = initModel.getLan();
                        }
                        if (type == 1) {
                            //仅机器人
                            showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
                        } else if (type == 3 || type == 4) {
                            //机器人优先
                            showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
                        } else if (type == 2) {
                            //仅人工
                            showLogicTitle("", "", false);
                        }
                        //让用户切换语言
                        zhiChiApi.languageList(getSobotActivity(), initModel.getPartnerid(), tempLanguage, new SobotResultCallBack<ArrayList<SobotlanguaeModel>>() {
                            @Override
                            public void onSuccess(ArrayList<SobotlanguaeModel> sobotlanguaeModels) {
                                if (sobotlanguaeModels != null && sobotlanguaeModels.size() > 0) {
                                    ZhiChiMessageBase messageBase = new ZhiChiMessageBase();
                                    messageBase.setId(getMsgId());
                                    messageBase.setMsgId(getMsgId());
                                    messageBase.setT(Calendar.getInstance().getTime().getTime() + "");
                                    messageBase.setLanguaeModels(sobotlanguaeModels);
                                    messageBase.setSenderType(ZhiChiConstant.message_sender_type_change_languae);
                                    ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                                    messageBase.setAnswer(reply);
                                    messageBase.setShowFaceAndNickname(false);
                                    messageAdapter.justAddData(messageBase);
                                    messageAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Exception e, String s) {
                                onInitResult(initModel);
                            }
                        });
                    } else {
                        onInitResult(initModel);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                //必须放到initSdk后边才能设置导航栏颜色（会话没结束时返回再进来initModel才有值）
                updateToolBarBg();
                isAppInitEnd = true;
                SharedPreferencesUtil.saveObject(mAppContext,
                        ZhiChiConstant.sobot_last_current_info, info);
                if (!isActive()) {
                    return;
                }
//                if (e instanceof IllegalArgumentException && !TextUtils.isEmpty(des)) {
//                    ToastUtil.showToast(mAppContext, des);
//                    finish();
//                } else {
//
//                }
                showInitError();
                isSessionOver = true;
            }
        });
    }

    //初始化成功后执行逻辑
    private void onInitResult(final ZhiChiInitModeBase result) {
        //清空快捷菜单缓存
        if (allQuickMenuModel != null) {
            allQuickMenuModel.clear();
        } else {
            allQuickMenuModel = new HashMap<>();
        }
        applyUIConfig();
        if (initModel == null) {
            return;
        }
        //切换语言禁止下拉
        srv_message.setEnableRefresh(true);
        isAppInitEnd = true;
        if (!isActive()) {
            return;
        }
        if (chat_main != null) {
            chat_main.setVisibility(View.VISIBLE);
        }
        if (sobot_bar_bottom != null) {
            sobot_bar_bottom.setVisibility(View.VISIBLE);
        }
        if (sobot_iv_right != null && SobotUIConfig.sobot_title_right_menu1_display) {
            sobot_iv_right.setVisibility(View.VISIBLE);
        }
        //切换语言后重新赋值
        sobot_net_not_connect.setText(R.string.sobot_network_unavailable);
        textReConnect.setText(R.string.sobot_try_again);
        btn_reconnect.setText(R.string.sobot_reunicon);
        send_voice_robot_hint.setText(R.string.sobot_robot_voice_hint);
        txt_speak_content.setText(R.string.sobot_press_say);
        sobot_txt_restart_talk.setText(R.string.sobot_restart_talk);
        sobot_tv_message.setText(R.string.sobot_str_bottom_message);
        sobot_tv_satisfaction.setText(R.string.sobot_str_bottom_satisfaction);
        tv_switch_robot.setText(R.string.sobot_switch_business);

        SharedPreferencesUtil.saveStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, "");
        SharedPreferencesUtil.saveLongData(getSobotActivity(), ZhiChiConstant.SOBOT_FINISH_CURTIME, 0);
        //分词联想开关
        SharedPreferencesUtil.saveBooleanData(getSobotActivity(), ZhiChiConstant.SOBOT_CONFIG_SUPPORT, initModel.getRobotGuessFlag() == 1 ? true : false);
        //更加主题色，更新UI
        updateToolBarBg();
        processPlatformAppId();
        getAnnouncement();
        if (info.getService_mode() > 0) {
            initModel.setType(info.getService_mode() + "");
        }
        type = Integer.parseInt(initModel.getType());
        SharedPreferencesUtil.saveIntData(mAppContext,
                info.getApp_key() + "_" + ZhiChiConstant.initType, type);
        // 通告不置顶
        if (initModel.getAnnounceMsgFlag() && !initModel.isAnnounceTopFlag() && !TextUtils.isEmpty(initModel.getAnnounceMsg())) {
            ZhiChiMessageBase noticeModel = ChatUtils.getNoticeModel(getSobotActivity(), initModel);
            noticeModel.setMsgId(getMsgId());
            robotWelcomeMsgId = noticeModel.getMsgId();
            messageAdapter.justAddData(noticeModel);
            messageAdapter.notifyDataSetChanged();
        }
        //初始化查询cid
        queryCids();
        SharedPreferencesUtil.saveBooleanData(mAppContext,
                "refrashSatisfactionConfig", true);
        //查询快捷菜单
        requestAllQuickMenu(quick_menu_robot);

        //底部菜单个数
        if (result.getVisitorScheme() != null && result.getVisitorScheme().getAppExtModelList() != null) {
            if (result.getVisitorScheme().getAppExtModelList().size() > 4) {
                bottomMenuLines = 2;
            }
        }
        //查询评价配置
        requestEvaluateConfig(false, null);
        if (initModel.isAiAgent()) {
            //获取大模型顶踩配置信息
            getAiRobotRealuateConfigInfo(false, "", "", "");
        }
        //设置初始layout,无论什么模式都是从机器人的UI变过去的
        showRobotLayout();
        if (!TextUtils.isEmpty(initModel.getPartnerid())) {
            SharedPreferencesUtil.saveStringData(mAppContext, Const.SOBOT_UID, initModel.getPartnerid());
        }
        if (!TextUtils.isEmpty(initModel.getCid())) {
            SharedPreferencesUtil.saveStringData(mAppContext, Const.SOBOT_CID, initModel.getCid());
        }
        SharedPreferencesUtil.saveIntData(mAppContext,
                ZhiChiConstant.sobot_msg_flag, initModel.getMsgFlag());
        SharedPreferencesUtil.saveBooleanData(mAppContext,
                ZhiChiConstant.sobot_leave_msg_flag, initModel.isMsgToTicketFlag());
        SharedPreferencesUtil.saveStringData(mAppContext,
                "lastCid", initModel.getCid());
        SharedPreferencesUtil.saveStringData(mAppContext,
                info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_partnerId, info.getPartnerid());
        SharedPreferencesUtil.saveOnlyStringData(mAppContext,
                ZhiChiConstant.sobot_last_current_appkey, info.getApp_key());
        SharedPreferencesUtil.saveObject(mAppContext,
                ZhiChiConstant.sobot_last_current_info, info);
        SharedPreferencesUtil.saveObject(mAppContext,
                ZhiChiConstant.sobot_last_current_initModel, initModel);
        SharedPreferencesUtil.saveOnlyStringData(mAppContext, ZhiChiConstant.sobot_last_current_customer_code, info.getCustomer_code());

        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.SOBOT_RECEPTIONISTID, TextUtils.isEmpty(info.getChoose_adminid()) ? "" : info.getChoose_adminid());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.SOBOT_ROBOT_CODE, TextUtils.isEmpty(info.getRobotCode()) ? "" : info.getRobotCode());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_remark, TextUtils.isEmpty(info.getRemark()) ? "" : info.getRemark());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_groupid, TextUtils.isEmpty(info.getGroupid()) ? "" : info.getGroupid());
        SharedPreferencesUtil.saveIntData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_service_mode, info.getService_mode());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_customer_fields, TextUtils.isEmpty(info.getCustomer_fields()) ? "" : info.getCustomer_fields());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_params, TextUtils.isEmpty(info.getParams()) ? "" : info.getParams());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_isvip, TextUtils.isEmpty(info.getIsVip()) ? "" : info.getIsVip());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_vip_level, TextUtils.isEmpty(info.getVip_level()) ? "" : info.getVip_level());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_user_label, TextUtils.isEmpty(info.getUser_label()) ? "" : info.getUser_label());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_robot_alias, TextUtils.isEmpty(info.getRobot_alias()) ? "" : info.getRobot_alias());
        SharedPreferencesUtil.saveStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_current_languae, TextUtils.isEmpty(info.getLocale()) ? "" : info.getLocale());

        //根据主题更新控件颜色
        updateUIByThemeColor();
        //如果有离线直接转人工功能开启，判断离线客服id有值，直接转人工
        if (initModel.getOfflineMsgConnectFlag() == 1 && !TextUtils.isEmpty(initModel.getOfflineMsgAdminId())
                && !"null".equals(initModel.getOfflineMsgAdminId())) {
            offlineMsgConnectFlag = initModel.getOfflineMsgConnectFlag();
            offlineMsgAdminId = initModel.getOfflineMsgAdminId();
            connectCustomerService(null, false);
            return;
        }
        //如果开启了客服主动关闭会话后直接分配客服功能
        if (initModel.getUserRemoveConnectFlag() == 1 && !TextUtils.isEmpty(initModel.getUserRemovedAdminId())) {
            offlineMsgConnectFlag = initModel.getUserRemoveConnectFlag();
            offlineMsgAdminId = initModel.getUserRemovedAdminId();
            connectCustomerService(null, false);
            return;
        }
        if (type == ZhiChiConstant.type_robot_only) {
            remindRobotMessage(handler, initModel, info);
            showSwitchRobotBtn();
            if (SobotOption.sobotChatStatusListener != null) {
                //修改聊天状态为机器人状态
                SobotOption.sobotChatStatusListener.onChatStatusListener(SobotChatStatusMode.ZCServerConnectRobot);
            }
        } else if (type == ZhiChiConstant.type_robot_first) {
            //机器人优先
            if (initModel.getUstatus() == ZhiChiConstant.ustatus_online || initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                //需要判断  是否需要保持会话
                if (initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                    //机器人会话 欢迎语、常见问题
                    remindRobotMessage(handler, initModel, info);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        connectCustomerService(null);
                    }
                }, 700);
            } else {
                //仅机器人或者机器人优先，不需要保持会话
                remindRobotMessage(handler, initModel, info);
                showSwitchRobotBtn();
                if (SobotOption.sobotChatStatusListener != null) {
                    //修改聊天状态为机器人状态
                    SobotOption.sobotChatStatusListener.onChatStatusListener(SobotChatStatusMode.ZCServerConnectRobot);
                }
            }
        } else {
            if (type == ZhiChiConstant.type_custom_only) {
                //仅人工客服
                if (isUserBlack()) {
                    showLeaveMsg();
                } else {
                    if (initModel.getUstatus() == ZhiChiConstant.ustatus_online || initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                        connectCustomerService(null);
                    } else {
                        if (initModel.getInvalidSessionFlag() == 1) {
                            //设置底部键盘
                            setBottomView(ZhiChiConstant.bottomViewtype_onlyrobot);
//                                    btn_set_mode_rengong.setVisibility(View.GONE);
                            btn_model_edit.setVisibility(View.GONE);
                            btn_model_voice.setVisibility(View.GONE);
                            cb_emoticon_view.setVisibility(View.VISIBLE);
                            setTitle("", false);
                        } else {
                            transfer2Custom(null, null, null, true, "0", "", "");
                        }
                    }
                }
            } else if (type == ZhiChiConstant.type_custom_first) {
                if (initModel.getUstatus() == ZhiChiConstant.ustatus_online || initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                    //需要判断  是否需要保持会话
                    if (initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                        //机器人会话 欢迎语、常见问题
                        remindRobotMessage(handler, initModel, info);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectCustomerService(null);
                        }
                    }, 700);
                } else {
                    showSwitchRobotBtn();
                    if (initModel.getInvalidSessionFlag() == 1) {
                        //机器人会话 欢迎语、常见问题
                        remindRobotMessage(handler, initModel, info);
                        //人工优先模式，开启延迟转人工后，只要自动发送消息对象不为空并且不是默认的，就触发转人工
                        if (info.getAutoSendMsgMode() != null && info.getAutoSendMsgMode() != SobotAutoSendMsgMode.Default) {
                            doClickTransferBtn();
                        }
                    } else {
                        //客服优先
                        transfer2Custom(null, null, null, true, "0", "", "");
                    }
                }
            }
        }
        isSessionOver = false;
        //检查右上角的关闭按钮是否应该显示
        if (iv_close != null) {
            if (info.isShowCloseBtn() && current_client_model == ZhiChiConstant.client_model_customService) {
                //显示右上角的关闭按钮
                iv_close.setVisibility(View.VISIBLE);
            } else {
                iv_close.setVisibility(View.GONE);
            }
        }
        //设置一次面板，可以回显机器人、人工模式下加号菜单的数量
        setPanelView(mPanelLayout, cb_plus_view.getId());
        resetBtnUploadAndSend();
        if (initModel.getRealuateInfoFlag() == 1) {
            //是否需要获取点踩配置
            requestRealuateConfig(false, "", "");
        }
    }

    /**
     * 请求快捷菜单,初始化成功请求一次
     */
    private void requestAllQuickMenu(final int opportunity) {
        if (allQuickMenuModel != null && allQuickMenuModel.size() > 0) {
            showQuickMenu(opportunity);
        } else {
            zhiChiApi.getAllMenu(getSobotActivity(), initModel.getPartnerid(), initModel.getCid(), new SobotResultCallBack<List<QuickMenuModel>>() {
                @Override
                public void onSuccess(List<QuickMenuModel> quickMenuItemModels) {
                    if (quickMenuItemModels != null && quickMenuItemModels.size() > 0) {
                        for (int i = 0; i < quickMenuItemModels.size(); i++) {
                            if (quickMenuItemModels.get(i) != null) {
                                allQuickMenuModel.put(quickMenuItemModels.get(i).getOpportunity(), quickMenuItemModels.get(i));
                            }
                        }
                        showQuickMenu(opportunity);
                    }
                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }

    /**
     * 菜单方案触发次数递增
     *
     * @param menuPlanId 菜单方案触发次数递增
     */
    private void menuPlanTriggerCount(String menuPlanId) {
        zhiChiApi.menuPlanTriggerCount(getSobotActivity(), initModel.getCompanyId(), menuPlanId, initModel.getCid(), new SobotResultCallBack() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(Exception e, String s) {

            }
        });

    }

    private void hideQuickMenu() {
        if (!isAddedMenu) {
            sobot_custom_menu_linearlayout.removeAllViews();
            sobot_custom_menu.setVisibility(View.GONE);
        } else {
            sobot_custom_menu_linearlayout.removeViews(1, sobot_custom_menu_linearlayout.getChildCount() - 1);
        }
    }

    /**
     * 特殊处理电商版传CustomerCode的情况
     */
    private void processPlatformAppId() {
        if (SobotVerControl.isPlatformVer && !TextUtils.isEmpty(info.getCustomer_code())) {
            if (!TextUtils.isEmpty(initModel.getAppId())) {
                info.setApp_key(initModel.getAppId());
            }

            SharedPreferencesUtil.saveStringData(mAppContext, ZhiChiConstant.SOBOT_CURRENT_IM_APPID, info.getApp_key());
        }
    }

    /**
     * 会话保持初始化的逻辑
     */
    private void doKeepsessionInit(int isFirstEntry) {
        List<ZhiChiMessageBase> tmpList = SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key()).getMessageList();
        if (tmpList != null && SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key()).getInitModel() != null) {
            //有数据
            int lastType = SharedPreferencesUtil.getIntData(mAppContext,
                    info.getApp_key() + "_" + ZhiChiConstant.initType, -1);
            if (info.getService_mode() < 0 || lastType == info.getService_mode()) {
                if (!TextUtils.isEmpty(info.getGroupid())) {
                    //判断是否是上次的技能组
                    String lastUseGroupId = SharedPreferencesUtil.getStringData(mAppContext, info.getApp_key() + "_" + ZhiChiConstant.sobot_last_login_group_id, "");
                    if (lastUseGroupId.equals(info.getGroupid())) {
                        keepSession(tmpList);
                    } else {
                        resetUser(isFirstEntry);
                    }
                } else {
                    keepSession(tmpList);
                }
            } else {
                resetUser(isFirstEntry);
            }
        } else {
            resetUser(isFirstEntry);
        }
    }

    /**
     * 用户操作离线后下线的逻辑 只下线，不修改界面ui
     *
     * @param initModel
     */
    public void userOffline(ZhiChiInitModeBase initModel) {
        if (initModel == null) {
            return;
        }
        queueNum = 0;
        stopInputListener();
        stopUserInfoTimeTask();
        stopCustomTimeTask();
        customerState = CustomerState.Offline;

        //清除引用
        clearAppointUI();

        isSessionOver = true;
        // 发送用户离线的广播
        CommonUtils.sendLocalBroadcast(mAppContext, new Intent(Const.SOBOT_CHAT_USER_OUTLINE));
        stopPolling();
    }

    /**
     * 显示下线的逻辑
     *
     * @param initModel
     * @param outLineType 下线的类型
     */
    public void customerServiceOffline(ZhiChiInitModeBase initModel, int outLineType) {
        if (initModel == null) {
            return;
        }
        queueNum = 0;
        stopInputListener();
        stopUserInfoTimeTask();
        stopCustomTimeTask();
        customerState = CustomerState.Offline;

        // 设置提醒
        showOutlineTip(initModel, outLineType);
        //更改底部键盘
        setBottomView(ZhiChiConstant.bottomViewtype_outline);
        //清除引用
        clearAppointUI();
        hideRobotVoiceHint();
        //隐藏底部标签控件
        sobot_custom_menu.setVisibility(View.GONE);
        mBottomViewtype = ZhiChiConstant.bottomViewtype_outline;

        if (Integer.parseInt(initModel.getType()) == ZhiChiConstant.type_custom_only) {
            if (1 == outLineType) {
                //如果在排队中 客服离开，那么提示无客服
//                showLogicTitle(getResources().getString(R.string.sobot_no_access), null, false);
                showLogicTitle("", null, false);
            }
            if (9 == outLineType) {
                //排队自动断开 不显示头部标题
                if (mTitleTextView != null) {
                    mTitleTextView.setVisibility(View.GONE);
                    mCompanyTextView.setVisibility(View.GONE);
                }
            }
        }

        if (6 == outLineType) {
            LogUtils.i("打开新窗口");
        }
        isSessionOver = true;
        // 发送用户离线的广播
        CommonUtils.sendLocalBroadcast(mAppContext, new Intent(Const.SOBOT_CHAT_USER_OUTLINE));
        stopPolling();
    }

    /**
     * 发出离线提醒
     *
     * @param initModel
     * @param outLineType 下线类型
     */
    private void showOutlineTip(ZhiChiInitModeBase initModel, int outLineType) {
        if (SobotOption.sobotChatStatusListener != null) {
            //修改聊天状态为离线状态
            SobotOption.sobotChatStatusListener.onChatStatusListener(SobotChatStatusMode.ZCServerConnectOffline);
        }
        String offlineMsg = ChatUtils.getMessageContentByOutLineType(getSobotActivity(), initModel, outLineType);
        if (!TextUtils.isEmpty(offlineMsg)) {
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setT(Calendar.getInstance().getTime().getTime() + "");
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
            reply.setRemindType(ZhiChiConstant.sobot_remind_type_outline);
            base.setAnswer(reply);
            if (1 == outLineType) {
                offlineMsg = offlineMsg.replace("#" + getResources().getString(R.string.sobot_cus_service) + "#", currentUserName).replace("#客服#", currentUserName).replace("#agent#", currentUserName);
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else if (2 == outLineType) {
                offlineMsg = offlineMsg.replace("#" + getResources().getString(R.string.sobot_cus_service) + "#", currentUserName).replace("#客服#", currentUserName).replace("#agent#", currentUserName);
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else if (3 == outLineType) {
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
                if (initModel != null) {
                    initModel.setIsblack("1");
                }
            } else if (5 == outLineType) {
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else if (4 == outLineType) {
                base.setAction(ZhiChiConstant.action_remind_past_time);
            } else if (6 == outLineType) {
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else if (99 == outLineType) {
                //留言转离线消息 成功后结束会话，添加提示语
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else if (9 == outLineType) {
                //排队断开
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            } else {
                //只要是204消息，最后肯定会结束会话
                base.setAction(ZhiChiConstant.sobot_outline_leverByManager);
            }
            reply.setMsg(offlineMsg);
            // 提示会话结束
            updateUiMessage(messageAdapter, base);
        }
    }

    /**
     * 显示排队提醒
     */
    private void showInLineHint(String queueDoc) {
        // 更新界面的操作
        if (!TextUtils.isEmpty(queueDoc)) {
            updateUiMessage(messageAdapter, ChatUtils.getInLineHint(queueDoc));
            gotoLastItem();
        }
    }

    //保持会话
    private void keepSession(List<ZhiChiMessageBase> tmpList) {
        onInitResult(initModel);
        ZhiChiConfig config = SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key());
        initModel = config.getInitModel();
        messageAdapter.addData(tmpList);
        messageAdapter.notifyDataSetChanged();
        current_client_model = config.current_client_model;
        type = Integer.parseInt(initModel.getType());
        if (unReadMsgIds == null) {
            unReadMsgIds = new HashMap<>();
        }
        String currentCid = initModel.getCid();
        if (preCurrentCid == null) {
            statusFlag = 0;
        } else if (!currentCid.equals(preCurrentCid)) {
            statusFlag = 0;
        }
        SharedPreferencesUtil.saveIntData(mAppContext,
                info.getApp_key() + "_" + ZhiChiConstant.initType, type);
        LogUtils.i("sobot----type---->" + type);
        showLogicTitle(config.activityTitle, config.adminFace, false);
        //根据主题更新控件颜色
        updateUIByThemeColor();
        showSwitchRobotBtn();
        customerState = config.customerState;
        mSatisfactionSet = config.mSatisfactionSet;
        remindRobotMessageTimes = config.remindRobotMessageTimes;
        isComment = config.isComment;
        isAboveZero = config.isAboveZero;
        currentUserName = config.currentUserName;
        isNoMoreHistoryMsg = config.isNoMoreHistoryMsg;
        currentCidPosition = config.currentCidPosition;
        queryCidsStatus = config.queryCidsStatus;
        isShowQueueTip = config.isShowQueueTip;
        mRobotOperatorCount = config.mRobotOperatorCount;
        mOperatorCount = config.mOperatorCount;
        mRobotPanleHeiht = config.mRobotPanleHeiht;
        mArtificialPanleHeiht = config.mArtificialPanleHeiht;
        isOpenUnread = config.isOpenUnread;
        if (config.cids != null) {
            cids.addAll(config.cids);
        }
        showTimeVisiableCustomBtn = config.showTimeVisiableCustomBtn;
        queueNum = config.queueNum;
        if (isNoMoreHistoryMsg) {
            srv_message.setEnableRefresh(false);// 设置下拉刷新列表
        }
        setAdminFace(config.adminFace);
        setAdminName(config.adminName);
        mBottomViewtype = config.bottomViewtype;
        setBottomView(config.bottomViewtype);
        isChatLock = config.isChatLock;
        if (type == ZhiChiConstant.type_custom_only && statusFlag == 0) {
            //仅人工客服
            preCurrentCid = currentCid;
            if (isUserBlack()) {
                showLeaveMsg();
            } else {
                if (initModel.getInvalidSessionFlag() == 1) {
                    //设置底部键盘
                    setBottomView(ZhiChiConstant.bottomViewtype_onlyrobot);
//                    btn_set_mode_rengong.setVisibility(View.GONE);
                    btn_model_edit.setVisibility(View.GONE);
                    btn_model_voice.setVisibility(View.GONE);
                    cb_emoticon_view.setVisibility(View.VISIBLE);
                    tempMsgContent = config.tempMsgContent;
                    setTitle("", false);
                } else {
                    transfer2Custom(null, null, null, true, "1", "", "");
                }
            }
        }
        if (type == ZhiChiConstant.type_custom_first && statusFlag == 0) {
            //人工优先
            tempMsgContent = config.tempMsgContent;
        }
        LogUtils.i("sobot----isChatLock--->" + "userInfoTimeTask " + config.userInfoTimeTask + "=====customTimeTask====" + config.customTimeTask + isChatLock);
        paseReplyTimeCustoms = config.paseReplyTimeCustoms;
        paseReplyTimeUserInfo = config.paseReplyTimeUserInfo;
        if (config.userInfoTimeTask && isChatLock != 1) {
            stopUserInfoTimeTask();
            startUserInfoTimeTask(handler);
            noReplyTimeUserInfo = config.paseReplyTimeUserInfo;
        }
        if (config.customTimeTask && isChatLock != 1) {
            stopCustomTimeTask();
            startCustomTimeTask(handler);
            noReplyTimeCustoms = config.paseReplyTimeCustoms;
        }
        if (info.getAutoSendMsgMode().geIsEveryTimeAutoSend()) {
            //每次都发
            config.isProcessAutoSendMsg = true;
        }
        if (config.isProcessAutoSendMsg) {
            //自动发一条信息
            if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeText) {
                //自动发送文本消息
                processAutoSendMsg(info);
            } else {
                //只有人工在线的模式下才会自动发送消息 (图片、文件、视频)
                if (info.getAutoSendMsgMode() != null && info.getAutoSendMsgMode() != SobotAutoSendMsgMode.Default && current_client_model == ZhiChiConstant.client_model_customService && !TextUtils.isEmpty(info.getAutoSendMsgMode().getContent())) {
                    if (info.getAutoSendMsgMode() == SobotAutoSendMsgMode.SendToOperator && customerState == CustomerState.Online) {
                        //发送内容
                        String content = info.getAutoSendMsgMode().getContent();
                        if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeFile) {
                            //发送文件
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadFile(sendFile, handler, messageAdapter, false);
                            }
                        } else if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeVideo) {
                            //发送视频
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadVideo(sendFile, null, messageAdapter);
                            }
                        } else if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypePhoto) {
                            //发送图片
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadFile(sendFile, handler, messageAdapter, false);
                            }
                        }
                    }
                }
            }
            config.isProcessAutoSendMsg = false;
        }
        //设置自动补全参数
        et_sendmessage.setRequestParams(initModel.getPartnerid(), initModel.getRobotid() + "");
        if (customerState == CustomerState.Online && current_client_model == ZhiChiConstant.client_model_customService) {
            createConsultingContent(1);
            createOrderCardContent(1);
            //人工模式关闭自动补全功能
            et_sendmessage.setAutoCompleteEnable(false);
        } else {
            //其他状态下开启自动补全
            et_sendmessage.setAutoCompleteEnable(true);
        }
        getAnnouncement();
        //查询快捷菜单
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            requestAllQuickMenu(quick_menu_service);
        } else {
            requestAllQuickMenu(quick_menu_robot);
        }
        SharedPreferencesUtil.saveBooleanData(mAppContext,
                "refrashSatisfactionConfig", true);
        config.clearMessageList();
        config.clearInitModel();
        isSessionOver = false;
        for (int i = messageList.size() - 1; i > 0; i--) {
            if (messageList.get(i).getSenderType() == ZhiChiConstant.message_sender_type_remide_info
                    && messageList.get(i).getAnswer() != null
                    && ZhiChiConstant.sobot_remind_type_simple_tip == messageList.get(i).getAnswer().getRemindType()) {
                messageList.remove(i);
                messageAdapter.notifyDataSetChanged();
                break;
            }
        }
        processNewTicketMsg(handler);
        inPolling = config.inPolling;
        //如果当前是人工模式，又在轮询，就启动轮询方法
        if (current_client_model == ZhiChiConstant.client_model_customService && inPolling && !CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
            startPolling();
        }
        checkUnReadMsg();
        if (initModel != null && initModel.isAiAgent()) {
            //获取大模型顶踩配置信息
            getAiRobotRealuateConfigInfo(false, "", "", "");
        }
        //设置一次面板，可以回显机器人、人工模式下加号菜单的数量
        setPanelView(mPanelLayout, cb_plus_view.getId());
        resetBtnUploadAndSend();
        gotoLastItem();
    }

    /**
     * 机器人智能转人工时，判断是否应该显示转人工按钮
     */
    private void showTransferCustomer() {
        showTimeVisiableCustomBtn++;
        setMenuFrist(ZhiChiConstant.bottomViewtype_robot);
    }


    /**
     * 获取客户传入的技能组id 直接转人工
     */
    private void transfer2CustomBySkillId(SobotConnCusParam cusParam, int transferType) {
        if (cusParam == null) {
            SobotConnCusParam param = new SobotConnCusParam();
            param.setGroupId(info.getGroupid());
            param.setGroupName(info.getGroup_name());
            param.setChooseAdminId(info.getChoose_adminid());
            param.setTransferType(transferType);
            requestQueryFrom(param, info.isCloseInquiryForm());
        } else {
            requestQueryFrom(cusParam, info.isCloseInquiryForm());
        }
    }

    /**
     * 显示表情按钮   如果没有表情资源则不会显示此按钮
     */
    private void showEmotionBtn() {
        Map<String, String> mapAll = DisplayEmojiRules.getMapAll(mAppContext);
        if (mapAll.size() > 0) {
            cb_emoticon_view.setVisibility(View.VISIBLE);
        } else {
            cb_emoticon_view.setVisibility(View.VISIBLE);
        }
    }

    private void transfer2Custom(String tempGroupId, boolean isShowTips, int transferType, String anwerMsgId, String ruleld, SobotConnCusParam param) {
        transfer2Custom(0, null, tempGroupId, "", "", isShowTips, transferType, "", "", "0", anwerMsgId, ruleld, param);
    }

    private void transfer2Custom(String tempGroupId, String keyword, String keywordId, boolean isShowTips, int transferType, String anwerMsgId, String ruleld) {
        transfer2Custom(0, null, tempGroupId, keyword, keywordId, isShowTips, transferType, "", "", "0", anwerMsgId, ruleld, null);
    }

    private void transfer2Custom(String tempGroupId, String keyword, String keywordId, boolean isShowTips, String activeTransfer, String anwerMsgId, String ruleld) {
        transfer2Custom(0, null, tempGroupId, keyword, keywordId, isShowTips, 0, "", "", activeTransfer, anwerMsgId, ruleld, null);
    }

    private void transfer2Custom(int eventType, String tempChooseAdminId, String tempGroupId, String keyword, String keywordId,
                                 boolean isShowTips, int transferType, String docId, String unknownQuestion, String
                                         activeTransfer, String answerMsgId, String ruleId) {
        transfer2Custom(eventType, tempChooseAdminId, tempGroupId, keyword, keywordId, isShowTips, transferType, docId, unknownQuestion, activeTransfer, answerMsgId, ruleId, null);
    }

    /**
     * 转人工按钮的逻辑封装
     * 如果用户传入了skillId 那么就用这个id直接转人工
     * 如果没有传  那么就检查技能组开关是否打开
     *
     * @param eventType         特殊业务可以根据对应类型处理
     * @param tempChooseAdminId 客服id
     * @param tempGroupId       技能组id
     * @param keyword           触发转人工的关键词
     * @param keywordId         触发转人工的关键词id
     * @param isShowTips        是否显示提示
     * @param transferType      转人工类型 重复提问、情绪负向转人工 传入后台做统计用
     *                          0普通 1重复提问 2情绪负向 转人工 3-关键词转人工 4-多伦会话转人工
     *                          5:机器人自动转人工(拆分 6-9 activeTransfer此时为1 根据answerType转换6-9)
     *                          6直接转人工，7理解转人工，8引导转人工，9未知转人工 10，点踩转人工
     * @param docId             词条触发转人工的词条id 指得是之前的transferType=5，现在的（6-9）的时候的词条id
     * @param unknownQuestion   未知问题触发转人工的客户问的未知问题
     * @param activeTransfer    转人工方式  0：机器人触发转人工 1：客户主动转人工
     * @param answerMsgId       消息id（直接回答的转人工按钮，对应的消息id）
     * @param ruleId            一问多答时的规则id，没有传入“”
     */
    private void transfer2Custom(int eventType, String tempChooseAdminId, String tempGroupId, String keyword, String keywordId,
                                 boolean isShowTips, int transferType, String docId, String unknownQuestion, String
                                         activeTransfer, String answerMsgId, String ruleId, SobotConnCusParam param) {
        if (param == null) {
            param = new SobotConnCusParam();
        }
        param.setEventType(eventType);
        if (info != null) {
            param.setGroupId(StringUtils.checkStringIsNull(info.getGroupid()));
            param.setGroupName(StringUtils.checkStringIsNull(info.getGroup_name()));
            param.setChooseAdminId(StringUtils.checkStringIsNull(info.getChoose_adminid()));
        }
        param.setKeyword(keyword);
        param.setKeywordId(keywordId);
        param.setDocId(docId);
        param.setUnknownQuestion(unknownQuestion);
        param.setTransferType(transferType);
        param.setActiveTransfer(activeTransfer);
        param.setAnswerMsgId(answerMsgId);
        param.setRuleId(ruleId);
        //如果有业务（关键词转人工、多轮1526 转人工）技能组id 和客服id ，就覆盖进线info里边的技能组id 和客服id
        if (StringUtils.isNoEmpty(tempChooseAdminId)) {
            param.setChooseAdminId(tempChooseAdminId);
        }
        if (StringUtils.isNoEmpty(tempGroupId)) {
            param.setGroupId(tempGroupId);
            param.setGroupName("");
        }
        SobotTransferOperatorParam tparam = null;
        if (SobotOption.transferOperatorInterceptor != null) {
            // 拦截转人工
            tparam = new SobotTransferOperatorParam();
            tparam.setGroupId(tempGroupId);
            tparam.setKeyword(keyword);
            tparam.setKeywordId(keywordId);
            tparam.setShowTips(isShowTips);
            tparam.setTransferType(transferType);
            tparam.setConsultingContent(info.getConsultingContent());
            SobotOption.transferOperatorInterceptor.onTransferStart(getSobotActivity(), tparam);
        }
        if (!isHasRequestQueryFrom && SobotStringUtils.isNoEmpty(initModel.getInquiryPlanId())) {
            requesetFormInfo(param, tparam);
        } else {
            doTransfer2Custom(param, tparam, false);
        }
    }

    public void doTransfer2Custom(SobotConnCusParam param, SobotTransferOperatorParam tparam, boolean isNewFrom) {
        boolean closeForm = false;
        if (!isNewFrom) {
            closeForm = info.isCloseInquiryForm();
        }
        if (SobotOption.transferOperatorInterceptor != null) {
            // 拦截转人工
            SobotOption.transferOperatorInterceptor.onTransferStart(getSobotActivity(), tparam);
        } else if (StringUtils.isNoEmpty(param.getGroupId())
                || StringUtils.isNoEmpty(param.getChooseAdminId())
                || StringUtils.isNoEmpty(info.getTransferAction())
                || initModel.isSmartRouteInfoFlag()) {

            //指定了客服id、指定了技能组、配置了转人工溢出、开启智能路由 ，只要有一个条件满足就直接先走询前表单然后转人工
            requestQueryFrom(param, closeForm);
        } else {
            if (initModel.getGroupflag().equals(ZhiChiConstant.groupflag_on)) {
                //如果技能组开启，同时没有开启智能路由，那么拉取技能组数据
                getGroupInfo(param);
            } else {
                //直接转人工
                requestQueryFrom(param, closeForm);
            }
        }
    }

    /**
     * 获取技能组
     *
     * @param param 转人工参数
     */
    private void getGroupInfo(final SobotConnCusParam param) {
        zhiChiApi.getGroupList(SobotChatFragment.this, info.getApp_key(), initModel.getPartnerid(), new StringResultCallBack<ZhiChiGroup>() {
            @Override
            public void onSuccess(ZhiChiGroup zhiChiGroup) {
                if (!isActive()) {
                    return;
                }
                boolean hasOnlineCustom = false;
                if (ZhiChiConstant.groupList_ustatus_time_out.equals(zhiChiGroup.getUstatus())) {
                    customerServiceOffline(initModel, 4);
                } else {
                    list_group = zhiChiGroup.getData();
                    if (list_group != null && list_group.size() > 0) {
                        for (int i = 0; i < list_group.size(); i++) {
                            if ("true".equals(list_group.get(i).isOnline())) {
                                hasOnlineCustom = true;
                                break;
                            }
                        }
                        if (hasOnlineCustom) {
                            if (initModel.getUstatus() == ZhiChiConstant.ustatus_online || initModel.getUstatus() == ZhiChiConstant.ustatus_queue) {
                                // 会话保持直接转人工
                                connectCustomerService(null);
                            } else {
                                //只要有客服在线，就先弹技能组选择，技能组有客服在线，显技能组名字，点击后，查讯前表单；
                                // 无客服，开启留言，点开后留言;无客服在线，又未开启留言，灰色，不可点击
                                if (!TextUtils.isEmpty(info.getGroupid())) {
                                    //指定技能组
                                    transfer2CustomBySkillId(param, param != null ? param.getTransferType() : 0);
                                } else {
                                    Intent intent = new Intent(mAppContext, SobotSkillGroupActivity.class);
                                    intent.putExtra("grouplist", (Serializable) list_group);
                                    intent.putExtra("uid", initModel.getPartnerid());
                                    intent.putExtra("type", type);
                                    intent.putExtra("appkey", info.getApp_key());
                                    intent.putExtra("companyId", initModel.getCompanyId());
                                    intent.putExtra("msgTmp", initModel.getMsgTmp());
                                    intent.putExtra("msgTxt", initModel.getMsgTxt());
                                    intent.putExtra("msgFlag", initModel.getMsgFlag());
                                    intent.putExtra("transferType", param != null ? param.getTransferType() : 0);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM, param);
                                    intent.putExtras(bundle);
                                    startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_GRROUP);
                                }
                            }
                        } else {
                            if (messageAdapter != null && keyWordMessageBase != null) {
                                messageAdapter.justAddData(keyWordMessageBase);
                                messageAdapter.notifyDataSetChanged();
                                keyWordMessageBase = null;
                            } else {
                                //技能组没有客服在线
                                connCustomerServiceFail(true);
                            }

                            //延迟转人工如果没有转成功（技能组没有客服在线），需要把该消息当留言转离线消息处理下
                            if (!TextUtils.isEmpty(tempMsgContent)) {
                                String skillGroupId = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, "");
                                zhiChiApi.leaveMsg(SobotChatFragment.this, initModel.getPartnerid(), skillGroupId, tempMsgContent, tmpMsgType + "", new StringResultCallBack<BaseCode>() {
                                    @Override
                                    public void onSuccess(BaseCode baseCode) {

                                    }

                                    @Override
                                    public void onFailure(Exception e, String s) {

                                    }
                                });
                            }
                        }
                    } else {
                        //没有设置技能组
                        requestQueryFrom(param, info.isCloseInquiryForm());
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (!isActive()) {
                    return;
                }
                ToastUtil.showToast(mAppContext, des);
            }
        });
    }

    /**
     * 转人工失败
     */
    private void connCustomerServiceFail(boolean isShowTips) {
        if (type == 2) {
            showLeaveMsg();
        } else {
            if (Integer.parseInt(initModel.getType()) == ZhiChiConstant.type_custom_only) {
                showLogicTitle(initModel.getRobotName(), "", false);
            } else {
                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);

            }
            showSwitchRobotBtn();
            if (isShowTips) {
                showCustomerOfflineTip();
            }
            if (type == ZhiChiConstant.type_custom_first && current_client_model ==
                    ZhiChiConstant.client_model_robot) {
                remindRobotMessage(handler, initModel, info);
            }
        }
        gotoLastItem();
    }

    /**
     * 转人工 用户是黑名单
     */
    private void connCustomerServiceBlack(boolean isShowTips) {
        showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
        showSwitchRobotBtn();
        if (isShowTips) {
            showCustomerUanbleTip();
        }
        if (type == ZhiChiConstant.type_custom_first) {
            remindRobotMessage(handler, initModel, info);
        }
    }

    /**
     * 显示机器人的布局
     */
    private void showRobotLayout() {
        if (initModel != null) {
            if (type == 1) {
                //仅机器人
                setBottomView(ZhiChiConstant.bottomViewtype_onlyrobot);
                mBottomViewtype = ZhiChiConstant.bottomViewtype_onlyrobot;
                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
            } else if (type == 3 || type == 4) {
                //机器人优先
                setBottomView(ZhiChiConstant.bottomViewtype_robot);
                mBottomViewtype = ZhiChiConstant.bottomViewtype_robot;
                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
            } else if (type == 2) {
                //仅人工
                setBottomView(ZhiChiConstant.bottomViewtype_customer);
                mBottomViewtype = ZhiChiConstant.bottomViewtype_customer;
                showLogicTitle("", "", false);
            }
            //仅人工不需要设置机器人布局
            if (type != ZhiChiConstant.type_custom_only) {
                //除了仅人工模式，打开机器人自动补全功能
                et_sendmessage.setRequestParams(initModel.getPartnerid(), initModel.getRobotid() + "");
                et_sendmessage.setAutoCompleteEnable(true);
            }
        }
    }

    /**
     * 转人工方法
     *
     * @param param 转人工参数对象
     */
    protected void connectCustomerService(SobotConnCusParam param, final boolean isShowTips) {
        if (isConnCustomerService) {
            return;
        }
        isConnCustomerService = true;
        boolean currentFlag = (customerState == CustomerState.Queuing || customerState == CustomerState.Online);

        if (param == null) {
            param = new SobotConnCusParam();
        }
        param.setTran_flag(info.getTranReceptionistFlag());
        param.setPartnerid(initModel.getPartnerid());
        param.setCid(initModel.getCid());
        param.setCurrentFlag(currentFlag);
        param.setTransferAction(info.getTransferAction());
        param.setIs_Queue_First(info.is_queue_first());
        param.setSummary_params(info.getSummary_params());
        param.setOfflineMsgAdminId(offlineMsgAdminId);
        param.setOfflineMsgConnectFlag(offlineMsgConnectFlag);
        SharedPreferencesUtil.saveStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, param.getGroupId());

        if (param.getEventType() == ZhiChiConstant.SOBOT_TYEP_TRANSFER_CUSTOM_DUOLUN1526) {
            //多轮转人工节点 1526 如果指定客服需要设置强转
            param.setTran_flag(1);
        }

        final String keyword = param.getKeyword();
        final String keywordId = param.getKeywordId();
        final String docId = param.getDocId();
        final String unknownQuestion = param.getUnknownQuestion();
        final String answerMsgId = param.getAnswerMsgId();
        final String ruleId = param.getRuleId();

        final String activeTransfer = param.getActiveTransfer();
        final int transferType = param.getTransferType();
        zhiChiApi.connnect(SobotChatFragment.this, param,
                new StringResultCallBack<ZhiChiMessageBase>() {
                    @Override
                    public void onSuccess(ZhiChiMessageBase zhichiMessageBase) {
                        //转人工接口执行完后，先断开通道和停止界面上的轮询,防止之前的轮询用的是上个会话的puid,导致拿不到新会话的消息
                        zhiChiApi.disconnChannel();
                        stopPolling();
                        LogUtils.i("connectCustomerService:zhichiMessageBase= " + zhichiMessageBase);
                        isConnCustomerService = false;
                        offlineMsgAdminId = "";
                        offlineMsgConnectFlag = 0;
                        if (!isActive()) {
                            return;
                        }

                        if (!TextUtils.isEmpty(zhichiMessageBase.getServiceEndPushMsg())) {
                            initModel.setServiceEndPushMsg(zhichiMessageBase.getServiceEndPushMsg());
                        }

                        int status = Integer.parseInt(zhichiMessageBase.getStatus());
                        statusFlag = status;
                        preCurrentCid = initModel.getCid();
                        setAdminFace(zhichiMessageBase.getAface());
                        setAdminName(zhichiMessageBase.getAname());
                        LogUtils.i("status---:" + status);
                        if (status != 0) {
                            if (status == ZhiChiConstant.transfer_robot_customServeive) {
                                //机器人超时下线转人工
                                customerServiceOffline(initModel, 4);
                            } else if (status == ZhiChiConstant.transfer_robot_custom_status) {
                                //如果设置指定客服的id。并且设置不是必须转入，服务器返回status=6.这个时候要设置receptionistId为null
                                //为null以后继续转人工逻辑。如果技能组开启就弹技能组，如果技能组没有开启，就直接转人工
                                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
                                info.setChoose_adminid(null);
                                //智能路由匹配失败重新转人工
                                initModel.setSmartRouteInfoFlag(false);
//                                    transfer2Custom(null, keyword, keywordId, isShowTips, docId, unknownQuestion, activeTransfer);
                                transfer2Custom(0, null, null, keyword, keywordId, isShowTips, transferType, docId, unknownQuestion, activeTransfer, answerMsgId, ruleId);
                            } else {
                                if (ZhiChiConstant.transfer_customeServeive_success == status) {
                                    connCustomerServiceSuccess(zhichiMessageBase);
                                } else if (ZhiChiConstant.transfer_customeServeive_fail == status) {
                                    if (messageAdapter != null && keyWordMessageBase != null) {
                                        messageAdapter.justAddData(keyWordMessageBase);
                                        messageAdapter.notifyDataSetChanged();
                                        keyWordMessageBase = null;
                                    } else {
                                        connCustomerServiceFail(isShowTips);
                                    }
                                } else if (ZhiChiConstant.transfer_customeServeive_isBalk == status) {
                                    if (messageAdapter != null && keyWordMessageBase != null) {
                                        messageAdapter.justAddData(keyWordMessageBase);
                                        messageAdapter.notifyDataSetChanged();
                                        keyWordMessageBase = null;
                                    } else {
                                        connCustomerServiceBlack(isShowTips);
                                    }
                                } else if (ZhiChiConstant.transfer_customeServeive_already == status) {
                                    connCustomerServiceSuccess(zhichiMessageBase);
                                } else if (ZhiChiConstant.transfer_robot_custom_max_status == status) {
                                    if (type == 2) {
                                        showLogicTitle(getResources().getString(R.string.sobot_wait_full), null, true);
                                        setBottomView(ZhiChiConstant.bottomViewtype_custom_only_msgclose);
                                        mBottomViewtype = ZhiChiConstant.bottomViewtype_custom_only_msgclose;
                                    }

                                    if (initModel.getMsgFlag() == ZhiChiConstant.sobot_msg_flag_open) {
                                        if (!TextUtils.isEmpty(zhichiMessageBase.getMsg())) {
                                            ToastUtil.showToast(mAppContext, zhichiMessageBase.getMsg());
                                        } else {
                                            ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_line_transfinite_def_hint));
                                        }
                                        startToPostMsgActivty(false);
                                    }
                                    showSwitchRobotBtn();
                                }
                            }
                            //延迟转人工如果没有转成功（排队除外），需要把该消息当留言转离线消息处理下
                            if (!TextUtils.isEmpty(tempMsgContent) && ZhiChiConstant.transfer_customeServeive_success != status) {
                                String skillGroupId = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, "");
                                zhiChiApi.leaveMsg(SobotChatFragment.this, initModel.getPartnerid(), skillGroupId, tempMsgContent, tmpMsgType + "", new StringResultCallBack<BaseCode>() {
                                    @Override
                                    public void onSuccess(BaseCode baseCode) {

                                    }

                                    @Override
                                    public void onFailure(Exception e, String s) {

                                    }
                                });
                            }
                        } else {
                            LogUtils.i("转人工--排队");
                            //开启通道
                            zhiChiApi.connChannel(zhichiMessageBase.getWslinkBak(),
                                    zhichiMessageBase.getWslinkDefault(), initModel.getPartnerid(), zhichiMessageBase.getPuid(), info.getApp_key(), zhichiMessageBase.getWayHttp());
                            customerState = CustomerState.Queuing;
                            isShowQueueTip = isShowTips;
                            if (!TextUtils.isEmpty(tempMsgContent)) {
                                //延迟转人工排队时需要添加这个接口
                                zhiChiApi.sendMsgWhenQueue(initModel.getReadFlag(), tempMsgContent, initModel.getPartnerid(), initModel.getCid(), "0", new StringResultCallBack<CommonModelBase>() {
                                    @Override
                                    public void onSuccess(CommonModelBase commonModelBase) {

                                    }

                                    @Override
                                    public void onFailure(Exception e, String s) {

                                    }
                                });
                            }
                            createCustomerQueue(zhichiMessageBase.getCount() + "", status, zhichiMessageBase.getQueueDoc(), isShowTips);
                            if (!CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
                                LogUtils.i2Local("转人工排队 开启轮询", "tcpserver 没运行，直接走fragment 界面的轮询");
                                SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().disconnChannel();
                                //SobotTCPServer不存在，直接走定时器轮询
                                pollingMsgForOne();
                                startPolling();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        LogUtils.i("connectCustomerService:e= " + e.toString() + des);
                        isConnCustomerService = false;
                        if (messageAdapter != null && keyWordMessageBase != null) {
                            messageAdapter.justAddData(keyWordMessageBase);
                            keyWordMessageBase = null;
                        }
                        if (!isActive()) {
                            return;
                        }
                        if (type == 2) {
                            setBottomView(ZhiChiConstant.bottomViewtype_custom_only_msgclose);
//                            showLogicTitle(getResources().getString(R.string.sobot_no_access), null, false);
                            showLogicTitle("", null, false);
                            isSessionOver = true;
                        }
                        ToastUtil.showToast(mAppContext, des);
                    }
                });
    }

    /**
     * 平滑滚动到末尾
     */
    private void gotoLastItem() {
//        String invoking = Log.getStackTraceString(new Throwable());
//        LogUtils.i("gotoLastItem==aaaaaaaaaaaaaaaaaaaaaaaaaaa==" + invoking);
        if (messageRV != null && messageAdapter != null && messageAdapter.getItemCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    messageRV.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }, 200);
        }
    }

    //上次滚动到底部的时间戳（大模型流式回答用到）
    private long goToLastTime = 0;

    /**
     * 滚动到末尾 偏量是2个屏幕高度
     * 大模型机器人用这个滚动
     */
    private void gotoLastItemWithOffset() {
        if (messageRV != null && messageAdapter != null && messageAdapter.getItemCount() > 0) {
            if (rvScrollLayoutManager != null) {
                rvScrollLayoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, -ScreenUtils.getScreenHeight(getSobotActivity()) * 2);
            } else {
                LinearLayoutManager layoutManager = (LinearLayoutManager) messageRV.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, -ScreenUtils.getScreenHeight(getSobotActivity()) * 2);
                } else {
                    messageRV.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        }
    }

    /**
     * 平滑滚动到指定位置
     *
     * @param index 指定位置
     */
    private void gotoIndexItem(int index) {
        if (index < 0) {
            index = 0;
        }
//        String invoking = Log.getStackTraceString(new Throwable());
//        LogUtils.i("gotoIndexItem==aaaaaaaaaaaaaaaaaaaaaaaaaaa==" + invoking);
        final int finalIndex = index;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (messageRV != null && messageAdapter != null && messageAdapter.getItemCount() > 0) {
                    if (finalIndex < messageAdapter.getItemCount()) {
                        messageRV.smoothScrollToPosition(finalIndex);
                    } else {
                        messageRV.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            }
        }, 200);
    }

    /**
     * 根据未读消息数更新右上角UI  “XX条未读消息”
     */
    private void updateFloatUnreadIcon() {
        ll_notReadInfo.setVisibility(View.VISIBLE);
        notReadInfo.setTextColor(ThemeUtils.getThemeColor(getSobotActivity()));
        int color = ThemeUtils.getThemeColor(getSobotActivity());
        iv_not_read.setImageDrawable(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_not_readinfo), color));
        if (mUnreadNum > 0) {
            notReadInfo.setText(mUnreadNum + " " + getResources().getString(R.string.sobot_new_msg));
        } else {
            ll_notReadInfo.setVisibility(View.GONE);
        }
    }

    /**
     * 转人工成功的方法
     */
    private void connCustomerServiceSuccess(ZhiChiMessageBase base) {
        if (base == null || initModel == null) {
            return;
        }
        bottomMenuLines = 2;
        initModel.setAdminHelloWord(!TextUtils.isEmpty(base.getAdminHelloWord()) ? base.getAdminHelloWord() : initModel.getAdminHelloWord());
        initModel.setAdminTipTime(!TextUtils.isEmpty(base.getServiceOutTime()) ? base.getServiceOutTime() : initModel.getAdminTipTime());
        initModel.setAdminTipWord(!TextUtils.isEmpty(base.getServiceOutDoc()) ? base.getServiceOutDoc() : initModel.getAdminTipWord());
        SharedPreferencesUtil.saveStringData(mAppContext, Const.SOBOT_PUID, base.getPuid());
        //开启通道
        zhiChiApi.connChannel(base.getWslinkBak(), base.getWslinkDefault(), initModel.getPartnerid(),
                base.getPuid(), info.getApp_key(), base.getWayHttp());
        createCustomerService(base.getAname(), base.getAface());
    }

    /**
     * 建立与客服的对话
     *
     * @param name 客服的名称
     * @param face 客服的头像
     */
    private void createCustomerService(String name, String face) {
        showEmotionBtn();
        //改变变量
        current_client_model = ZhiChiConstant.client_model_customService;
        if (initModel.isAiAgent()) {
            //修改大模型机器人 未操作的顶踩 和点踩后的原因卡片
            if (messageAdapter != null) {
                messageAdapter.removeOrUpdateAIRobotMsg();
            }
        }
        if (SobotOption.sobotChatStatusListener != null) {
            //修改聊天状态为客服状态
            SobotOption.sobotChatStatusListener.onChatStatusListener(SobotChatStatusMode.ZCServerConnectArtificial);
        }
        if (initModel.getReadFlag() == 1) {
            isOpenUnread = true;
        } else {
            isOpenUnread = false;
        }
        customerState = CustomerState.Online;
        isAboveZero = false;
        isComment = false;// 转人工时 重置为 未评价
        queueNum = 0;
        currentUserName = TextUtils.isEmpty(name) ? "" : name;
        //显示被xx客服接入
        if (initModel.getServicePromptFlag() == 1) {
            messageAdapter.addData(ChatUtils.getServiceAcceptTip(getSobotActivity(), initModel.getServicePromptWord(), name, face));
        }

        //转人工成功以后删除通过机器人关键字选择
        messageAdapter.removeKeyWordTranferItem();
        //转人工成功以后删除通过机器人语义关键字选择
        messageAdapter.removeSemanticsKeyWordTranferItem();

        if (initModel.isAdminHelloWordFlag()) {
            if (initModel.getAdminHelloWordCountRule() == 2) {
                //仅首次线上，isNew=1时有效
                if (initModel.getIsNew() == 1) {
                    String adminHelloWord = ZCSobotApi.getCurrentInfoSetting(getSobotActivity()) != null ? ZCSobotApi.getCurrentInfoSetting(mAppContext).getAdmin_hello_word() : "";
                    //显示人工欢迎语
                    if (!TextUtils.isEmpty(adminHelloWord)) {
                        messageAdapter.addData(ChatUtils.getServiceHelloTip(name, face, adminHelloWord));
                    } else {
                        messageAdapter.addData(ChatUtils.getServiceHelloTip(name, face, initModel.getAdminHelloWord()));
                    }
                }
            } else {
                if (!(initModel.getAdminHelloWordCountRule() == 1 && initModel.getUstatus() == ZhiChiConstant.ustatus_online)) {
                    //客户之前在线 并且 客服欢迎语规则只显示一次的开关打开 就不显示此次欢迎语
                    String adminHelloWord = ZCSobotApi.getCurrentInfoSetting(mAppContext) != null ? ZCSobotApi.getCurrentInfoSetting(mAppContext).getAdmin_hello_word() : "";
                    //显示人工欢迎语
                    if (!TextUtils.isEmpty(adminHelloWord)) {
                        messageAdapter.addData(ChatUtils.getServiceHelloTip(name, face, adminHelloWord));
                    } else {
                        messageAdapter.addData(ChatUtils.getServiceHelloTip(name, face, initModel.getAdminHelloWord()));
                    }
                }
            }
        }
        messageAdapter.notifyDataSetChanged();
        //显示关闭按钮
        //设置导航栏关闭按钮
        if (iv_close != null && info.isShowCloseBtn() && current_client_model == ZhiChiConstant.client_model_customService) {
            iv_close.setVisibility(View.VISIBLE);
        }
        //显示标题
        showLogicTitle(name, face, false);
        showSwitchRobotBtn();
        //创建咨询项目
        createConsultingContent(0);
        //创建订单卡片
        createOrderCardContent(0);
        //查询快捷菜单
        requestAllQuickMenu(quick_menu_service);
        //查询常见问题
        if (initModel.getSessionPhaseAndFaqIdRespVos() != null) {
            sobotHotIssue(handler, 3);
        }
        //展示自定义卡片
        if (info.getCustomCard() != null) {
            checkSendCardContent(handler);
        }
        gotoLastItem();
        //设置底部键盘
        setBottomView(ZhiChiConstant.bottomViewtype_customer);
        mBottomViewtype = ZhiChiConstant.bottomViewtype_customer;

        // 启动计时任务
        restartInputListener();
        stopUserInfoTimeTask();
        is_startCustomTimerTask = false;
        startUserInfoTimeTask(handler);
        hideItemTransferBtn();
        //关闭自动补全功能
        et_sendmessage.setAutoCompleteEnable(false);
        //自动发一条信息
        if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeText) {
            //自动发送文本消息
            processAutoSendMsg(info);
        } else {
            //只有人工在线的模式下才会自动发送消息
            if (info.getAutoSendMsgMode() != null && info.getAutoSendMsgMode() != SobotAutoSendMsgMode.Default && current_client_model == ZhiChiConstant.client_model_customService && !TextUtils.isEmpty(info.getAutoSendMsgMode().getContent())) {
                if (info.getAutoSendMsgMode() == SobotAutoSendMsgMode.SendToOperator && customerState == CustomerState.Online) {
                    //发送内容
                    String content = info.getAutoSendMsgMode().getContent();
                    if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeFile) {
                        //发送文件
                        File sendFile = new File(content);
                        if (sendFile.exists()) {
                            uploadFile(sendFile, handler, messageAdapter, false);
                        }
                    } else if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypeVideo) {
                        //发送视频
                        File sendFile = new File(content);
                        if (sendFile.exists()) {
                            uploadVideo(sendFile, null, messageAdapter);
                        }
                    } else if (info.getAutoSendMsgMode().getAuto_send_msgtype() == ZCMessageTypePhoto) {
                        //发送图片
                        File sendFile = new File(content);
                        if (sendFile.exists()) {
                            uploadFile(sendFile, handler, messageAdapter, false);
                        }
                    }
                }
            }
        }
        if (!isRemindTicketInfo) {
            processNewTicketMsg(handler);
        }
        if (!TextUtils.isEmpty(tempMsgContent)) {
            sendMsg(tempMsgContent);
            tempMsgContent = "";
        }
        if (!CommonUtils.isServiceWork(getSobotActivity(), "com.sobot.chat.core.channel.SobotTCPServer")) {
            LogUtils.i2Local("转人工成功后 开启轮询", "tcpserver 没运行，直接走fragment 界面的轮询");
            SobotMsgManager.getInstance(getSobotActivity()).getZhiChiApi().disconnChannel();
            //SobotTCPServer不存在，直接走定时器轮询
            pollingMsgForOne();
            startPolling();
        }
        if (cb_plus_view != null) {
            //设置一次面板，可以回人工模式下加号菜单的数量
            setPanelView(mPanelLayout, cb_plus_view.getId());
        }
    }

    /**
     * 隐藏条目中的转人工按钮
     */
    public void hideItemTransferBtn() {
        if (!isActive()) {
            return;
        }
        // 把机器人回答中的转人工按钮都隐藏掉
        messageRV.post(new Runnable() {

            @Override
            public void run() {

                for (int i = 0, count = messageRV.getChildCount(); i < count; i++) {
                    View child = messageRV.getChildAt(i);
                    if (child == null || child.getTag() == null || !(child.getTag() instanceof RichTextMessageHolder)) {
                        continue;
                    }
                    RichTextMessageHolder holder = (RichTextMessageHolder) child.getTag();
                    if (holder.message != null) {
                        holder.message.setShowTransferBtn(false);
                    }
                    holder.hideTransferBtn();
                }
            }
        });
    }

    /**
     * 显示客服不在线的提示
     */
    private void showCustomerOfflineTip() {
        if (initModel.isAdminNoneLineFlag()) {
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            String adminNoneLineTitle = ZCSobotApi.getCurrentInfoSetting(mAppContext) != null ? ZCSobotApi.getCurrentInfoSetting(mAppContext).getAdmin_offline_title() : "";
            if (!TextUtils.isEmpty(adminNoneLineTitle)) {
                reply.setMsg(adminNoneLineTitle);
            } else {
                if (TextUtils.isEmpty(initModel.getAdminNonelineTitle())) {
                    //如果提示语为空，直接返回，不然会显示错误数据
                    return;
                }
                reply.setMsg(initModel.getAdminNonelineTitle());
            }
            reply.setRemindType(ZhiChiConstant.sobot_remind_type_customer_offline);
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
            base.setAnswer(reply);
            base.setAction(ZhiChiConstant.action_remind_info_post_msg);
            updateUiMessage(messageAdapter, base);
        }
    }

    /**
     * 显示无法转接客服
     */
    private void showCustomerUanbleTip() {
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setMsg(getResources().getString(R.string.sobot_unable_transfer_to_customer_service));
        reply.setRemindType(ZhiChiConstant.sobot_remind_type_unable_to_customer);
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setSenderName(initModel.getRobotName());
        base.setSender(initModel.getRobotName());
        base.setSenderFace(initModel.getRobotLogo());
        base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        base.setAnswer(reply);
        base.setAction(ZhiChiConstant.action_remind_info_post_msg);
        updateUiMessage(messageAdapter, base);
    }

    /**
     * 机器人答案点踩 显示未解决问题，点击转人工客服
     */
    private void showCaiToCustomerTip() {
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setSenderName(initModel.getRobotName());
        base.setSender(initModel.getRobotName());
        base.setSenderFace(initModel.getRobotLogo());
        base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        base.setAction(ZhiChiConstant.action_remind_info_zhuanrengong);
        updateUiMessage(messageAdapter, base);
        gotoLastItem();
    }

    /**
     * 连接客服时，需要排队
     * 显示排队的处理逻辑
     *
     * @param num      当前排队的位置
     * @param status   当前转人工的返回状态，如果是7，就说明排队已经达到最大值，可以直接留言。
     * @param queueDoc 需要显示的排队提示语
     */
    private void createCustomerQueue(String num, int status, String queueDoc, boolean isShowTips) {
        if (customerState == CustomerState.Queuing && !TextUtils.isEmpty(num)
                && Integer.parseInt(num) > 0) {
            stopUserInfoTimeTask();
            stopCustomTimeTask();
            stopInputListener();

            queueNum = Integer.parseInt(num);
            //显示当前排队的位置
            if (status != ZhiChiConstant.transfer_robot_custom_max_status && isShowTips) {
                showInLineHint(queueDoc);
            }

            if (type == ZhiChiConstant.type_custom_only) {
                showLogicTitle("", null, false);
                setBottomView(ZhiChiConstant.bottomViewtype_onlycustomer_paidui);
                mBottomViewtype = ZhiChiConstant.bottomViewtype_onlycustomer_paidui;
            } else {
                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
                setBottomView(ZhiChiConstant.bottomViewtype_paidui);
                mBottomViewtype = ZhiChiConstant.bottomViewtype_paidui;
            }

            queueTimes = queueTimes + 1;
            if (type == ZhiChiConstant.type_custom_first) {
                if (queueTimes == 1) {
                    //如果当前为人工优先模式那么在第一次收到
                    remindRobotMessage(handler, initModel, info);
                }
            }
            showSwitchRobotBtn();
        }
    }

    /**
     * 初始化查询cid的列表
     */
    private void queryCids() {
        //如果initmodel 或者  querycid的接口调用中或者已经调用成功那么就不再重复查询
        if (initModel == null || queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_LOADING
                || queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_SUCCESS) {
            return;
        }
        long time = SharedPreferencesUtil.getLongData(mAppContext, ZhiChiConstant.SOBOT_SCOPE_TIME, 0);
        queryCidsStatus = ZhiChiConstant.QUERY_CIDS_STATUS_LOADING;
        // 初始化查询cid的列表
        zhiChiApi.queryCids(SobotChatFragment.this, initModel.getPartnerid(), time, new StringResultCallBack<ZhiChiCidsModel>() {

            @Override
            public void onSuccess(ZhiChiCidsModel data) {
                if (!isActive()) {
                    return;
                }
                queryCidsStatus = ZhiChiConstant.QUERY_CIDS_STATUS_SUCCESS;
                cids = data.getCids();
                if (cids != null) {
                    boolean hasRepeat = false;
                    for (int i = 0; i < cids.size(); i++) {
                        if (cids.get(i).equals(initModel.getCid())) {
                            hasRepeat = true;
                            break;
                        }
                    }
                    if (!hasRepeat) {
                        cids.add(initModel.getCid());
                    }
                    Collections.reverse(cids);
                }
                //拉取历史纪录
                getHistoryMessage(true);
            }

            @Override
            public void onFailure(Exception e, String des) {
                queryCidsStatus = ZhiChiConstant.QUERY_CIDS_STATUS_FAILURE;
            }
        });
    }

    private void showInitError() {
        showLogicTitle("", null, false);
        loading_anim_view.setVisibility(View.GONE);
        txt_loading.setVisibility(View.GONE);
        textReConnect.setVisibility(View.VISIBLE);
        icon_nonet.setVisibility(View.VISIBLE);
        btn_reconnect.setVisibility(View.VISIBLE);
        et_sendmessage.setVisibility(View.GONE);
        relative.setVisibility(View.GONE);
        welcome.setVisibility(View.VISIBLE);
    }

    /*
     * 发送咨询内容
     *
     */
    @Override
    public void sendConsultingContent() {
        sendCardMsg(info.getContent());
    }

    /**
     * @param base
     * @param type
     * @param questionFlag 0 是正常询问机器人
     *                     1 点击
     *                     2 是多轮会话
     * @param docId        没有就传Null
     */
    @Override
    public void sendMessageToRobot(ZhiChiMessageBase base, int type, int questionFlag, String
            docId) {
        sendMessageToRobot(base, type, questionFlag, docId, null);
    }

    /*发送0、机器人问答 1、文本  2、语音  3、图片 4、多轮会话 5、位置消息 6、点击大模型机器人按钮消息*/
    @Override
    public void sendMessageToRobot(ZhiChiMessageBase base, int type, int questionFlag, String
            docId, String sendContent) {
        if (base == null) {
            return;
        }
        base.setSenderName(info.getUser_nick());
        base.setSenderFace(info.getFace());
        if (type == 6) {
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> processInfoParams = new HashMap<>();
            processInfoParams.put("processId", SobotStringUtils.checkStringIsNull(base.getProcessId()));
            processInfoParams.put("nodeId", SobotStringUtils.checkStringIsNull(base.getNodeId()));
            processInfoParams.put("variableId", SobotStringUtils.checkStringIsNull(base.getVariableId()));
            processInfoParams.put("variableValue", SobotStringUtils.checkStringIsNull(base.getContent()));
            params.put("processInfo", processInfoParams);
            params.put("inputTypeEnum", "PROCESS_CLICK");
            base.setMsgId(getMsgId());
            base.setId(base.getMsgId());
            sendMsgToRobot(base, SEND_TEXT, questionFlag, docId, "", params);
        } else if (type == 5) {
            sendLocation(base.getId(), base.getAnswer().getLocationData(), handler, false);
        } else if (type == 4) {
            sendMsgToRobot(base, SEND_TEXT, questionFlag, docId, sendContent, null);
        } else if (type == 3) {
            /*图片消息*/
            // 根据图片的url 上传图片 更新上传图片的进度
            messageAdapter.updatePicStatusById(base.getId(), base.getSendSuccessState());
            messageAdapter.notifyDataSetChanged();
            ChatUtils.sendPicture(mAppContext, isOpenUnread && current_client_model == ZhiChiConstant.client_model_customService ? 1 : 0, initModel.getCid(), initModel.getPartnerid(),
                    base.getContent(), handler, base.getId(), messageAdapter, current_client_model, initModel);
        } else if (type == 2) {
            /*语音消息*/
            // 语音的重新上传
            sendVoiceMessageToHandler(base.getId(), base.getContent(), base.getAnswer()
                    .getDuration(), ZhiChiConstant.MSG_SEND_STATUS_LOADING, UPDATE_VOICE, handler);
            sendVoice(base.getId(), base.getAnswer().getDuration(), initModel.getCid(),
                    initModel.getPartnerid(), base.getContent(), handler);
        } else if (type == 1) {
            /*文本消息*/
            // 消息的转换
            sendMsgToRobot(base, UPDATE_TEXT, questionFlag, docId);
        } else if (type == 0) {
            /*机器人问答*/
            if (!isSessionOver) {
                // 消息的转换
                ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
                answer.setMsgType(ZhiChiConstant.message_type_text);
                answer.setMsg(base.getContent());
                base.setAnswer(answer);
                base.setSenderType(ZhiChiConstant.message_sender_type_customer);
                if (base.getId() == null || TextUtils.isEmpty(base.getId())) {
                    String msgId = getMsgId();
                    base.setId(msgId);
                    base.setMsgId(msgId);
                    updateUiMessage(messageAdapter, base);
                }
                sendMessageWithLogic(base.getId(), base.getContent(), initModel, handler, current_client_model, questionFlag, docId, null);
            } else {
                showOutlineTip(initModel, 1);
            }
        }
        gotoLastItem();
    }

    /**
     * 点击了转人工按钮
     */
    @Override
    public void doClickTransferBtn(ZhiChiMessageBase base) {
        //转人工按钮
        hidePanelAndKeyboard(mPanelLayout);
        doEmoticonBtn2Blur();
        if (base != null) {
            int temptransferType = base.getTransferType();
            if (temptransferType == 0 && !TextUtils.isEmpty(base.getAnswerType())) {
                if (Integer.parseInt(base.getAnswerType()) == 1) {
                    temptransferType = 6;
                } else if (Integer.parseInt(base.getAnswerType()) == 2) {
                    temptransferType = 7;
                } else if (Integer.parseInt(base.getAnswerType()) == 3) {
                    temptransferType = 9;
                } else if (Integer.parseInt(base.getAnswerType()) == 4) {
                    temptransferType = 8;
                }
            }
            if (base.getSpecialMsgFlag() == 1) {
                //特殊消息转人工 transferType=9
                temptransferType = 9;
            }
            transfer2Custom(0, null, null, null, null, true, temptransferType, base.getDocId(), base.getOriginQuestion(), "1", base.getMsgId(), base.getRuleId());
        } else {
            transfer2Custom(null, null, null, true, "1", "", "");
        }
    }

    public void doClickTransferBtn() {
        //转人工按钮
        hidePanelAndKeyboard(mPanelLayout);
        doEmoticonBtn2Blur();
        transfer2Custom(null, null, null, true, "1", "", "");
    }

    // 点击播放录音及动画
    @Override
    public void clickAudioItem(ZhiChiMessageBase message, final ImageView voiceIV, final boolean isRight) {
        initAudioManager();
        requestAudioFocus();
        if (mAudioPlayPresenter == null) {
            mAudioPlayPresenter = new AudioPlayPresenter(mAppContext);
        }
        if (mAudioPlayCallBack == null) {
            mAudioPlayCallBack = new AudioPlayCallBack() {
                @Override
                public void onPlayStart(ZhiChiMessageBase mCurrentMsg) {
                    if (mCurrentMsg != null && mCurrentMsg.getVoiceIV() != null) {
                        startAnim(mCurrentMsg, mCurrentMsg.getVoiceIV(), mCurrentMsg.isRight());
                    } else if (voiceIV != null) {
                        startAnim(mCurrentMsg, voiceIV, isRight);
                    } else {
                        showVoiceAnim(mCurrentMsg, true);
                    }
                }

                @Override
                public void onPlayEnd(ZhiChiMessageBase mCurrentMsg) {
                    if (mCurrentMsg != null && mCurrentMsg.getVoiceIV() != null) {
                        stopAnim(mCurrentMsg, mCurrentMsg.getVoiceIV());
                    } else if (voiceIV != null) {
                        stopAnim(mCurrentMsg, voiceIV);
                    } else {
                        showVoiceAnim(mCurrentMsg, false);
                        abandonAudioFocus();
                    }
                }
            };
        }
        mAudioPlayPresenter.clickAudio(message, mAudioPlayCallBack);
    }

    // 开始播放
    public void startAnim(ZhiChiMessageBase message, ImageView voiceIV, boolean isRight) {
        if (message != null)
            message.setVoideIsPlaying(true);

        Drawable playDrawable = voiceIV.getDrawable();
        if (playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).start();
        } else {
            resetAnim(voiceIV, isRight);
        }
    }

    private void resetAnim(ImageView voiceIV, boolean isRight) {
        voiceIV.setImageResource(isRight ? R.drawable.sobot_voice_appoint_right_icon : R.drawable.sobot_voice_appoint_left_icon);
        Drawable playDrawable = voiceIV.getDrawable();
        if (playDrawable != null
                && playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).start();
        }
    }

    // 关闭播放
    public void stopAnim(ZhiChiMessageBase message, ImageView voiceIV) {
        if (message != null)
            message.setVoideIsPlaying(false);
        Drawable playDrawable = voiceIV.getDrawable();
        if (playDrawable != null
                && playDrawable instanceof AnimationDrawable) {
            ((AnimationDrawable) playDrawable).stop();
            ((AnimationDrawable) playDrawable).selectDrawable(2);
        }
    }

    @Override
    public void sendMessage(String content) {
        sendMsg(content);
    }

    @Override
    public void removeMessageByMsgId(String msgid) {
        if (messageAdapter != null && !TextUtils.isEmpty(msgid)) {
            messageAdapter.removeByMsgId(msgid);
            messageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void addMessage(ZhiChiMessageBase message) {
        if (message != null) {
            messageAdapter.justAddData(message);
            messageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void mulitDiaToLeaveMsg(String leaveTemplateId, String tipMsgId) {
        if (mPostMsgPresenter != null) {
            hidePanelAndKeyboard();
            mPostMsgPresenter.obtainTmpConfigToMuItiPostMsg(initModel.getPartnerid(), leaveTemplateId, tipMsgId);
        }
    }

    @Override
    public void sendFileToRobot(String msgId, String msgType, String fileUrl) {
        setTimeTaskMethod(handler);
        if (!TextUtils.isEmpty(fileUrl)) {
            if (current_client_model == ZhiChiConstant.client_model_robot) {
                //如果当前模式是机器人模式，就把上传的视频的url 发给机器人，只显示问答的结果
                sendHttpRobotMessage(msgType, msgId, fileUrl, initModel.getPartnerid(),
                        initModel.getCid(), "", handler, 1, "", info.getLocale(), "", null);
            }
        }
    }

    public void showVoiceAnim(final ZhiChiMessageBase info, final boolean isShow) {
        if (!isActive()) {
            return;
        }
        messageRV.post(new Runnable() {

            @Override
            public void run() {
                if (info == null) {
                    return;
                }
                for (int i = 0, count = messageRV.getChildCount(); i < count; i++) {
                    View child = messageRV.getChildAt(i);
                    if (child == null || child.getTag() == null || !(child.getTag() instanceof VoiceMessageHolder)) {
                        continue;
                    }
                    VoiceMessageHolder holder = (VoiceMessageHolder) child.getTag();
                    holder.stopAnim();
                    if (holder.message == info) {
                        if (isShow) {
                            holder.startAnim();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void hidePanelAndKeyboard() {
        hidePanelAndKeyboard(mPanelLayout);
    }

    /**
     * 调用顶踩接口
     *
     * @param revaluateFlag true 顶  false 踩
     * @param message       顶踩用的 model
     */
    @Override
    public void doRevaluate(final boolean revaluateFlag, final ZhiChiMessageBase message) {
        if (isSessionOver) {
            showOutlineTip(initModel, 1);
            CustomToast.makeText(mAppContext, getResources().getString(R.string.sobot_ding_cai_sessionoff), 1500).show();
            return;
        }
        if (message == null) {
            LogUtils.i("消息为空，无法进行顶踩操作");
            return;
        }
        if (SobotStringUtils.isNoEmpty(message.getServant()) && "aiagent".equals(message.getServant())) {
            if (aiRobotRealuateConfigInfo == null) {
                getAiRobotRealuateConfigInfo(false, "", "", "");
                LogUtils.i("配置信息为空，无法进行顶踩操作");
                return;
            }
            //大模型顶踩操作
            SobotAiRobotAnswerCommontParams commontParams = new SobotAiRobotAnswerCommontParams();
            commontParams.setUid(initModel.getPartnerid());
            commontParams.setCid(message.getCid());
            //0:踩 1:赞
            commontParams.setStatus(revaluateFlag ? "1" : "0");
            commontParams.setAiAgentCid(message.getAiAgentCid());
            commontParams.setCompanyId(initModel.getCompanyId());
            commontParams.setSourceEnum("APP");
            commontParams.setRobotFlag(initModel.getReadFlag() + "");
            commontParams.setMsgId(message.getMsgId());
            commontParams.setRealuateEvaluateWord(aiRobotRealuateConfigInfo.getRealuateEvaluateWord());
            commontParams.setRealuateSubmitWord(aiRobotRealuateConfigInfo.getRealuateSubmitWord());
            commontParams.setRealuateButtonStyle(aiRobotRealuateConfigInfo.getRealuateButtonStyle());
            commontParams.setRealuateFlag(aiRobotRealuateConfigInfo.getRealuateFlag());
            commontParams.setRealuateStyle(aiRobotRealuateConfigInfo.getRealuateStyle());
            commontParams.setRealuateInfoFlag(0);
            zhiChiApi.aiAgentRobotAnswerComment(getSobotActivity(), commontParams, new StringResultCallBack<BaseListCodeV6>() {
                @Override
                public void onSuccess(BaseListCodeV6 data) {
                    if (!isActive()) {
                        return;
                    }
                    //改变顶踩按钮的布局
                    message.setRevaluateState(revaluateFlag ? 2 : 3);
                    refreshItemByCategory(TextMessageHolder.class);
                    refreshItemByCategory(ImageMessageHolder.class);
                    refreshItemByCategory(RichTextMessageHolder.class);
                    refreshItemByCategory(FileMessageHolder.class);
                    refreshItemByCategory(VideoMessageHolder.class);
                    refreshItemByCategory(MiniProgramMessageHolder.class);
                    refreshItemByCategory(ArticleMessageHolder.class);
                    if ((!TextUtils.isEmpty(message.getAnswerType()) && message.getAnswerType().startsWith("152"))) {
                        refreshItemByCategory(RobotTemplateMessageHolder1.class);
                        refreshItemByCategory(RobotTemplateMessageHolder2.class);
                        refreshItemByCategory(RobotTemplateMessageHolder3.class);
                        refreshItemByCategory(RobotTemplateMessageHolder4.class);
                        refreshItemByCategory(RobotTemplateMessageHolder5.class);
                        refreshItemByCategory(RobotTemplateMessageHolder6.class);
                    }
                    //仅机器人不显示
                    if (aiRobotRealuateConfigInfo != null && aiRobotRealuateConfigInfo.getRealuateTransferFlag() == 1 && current_client_model != ZhiChiConstant.client_model_customService && !revaluateFlag && type != ZhiChiConstant.type_robot_only) {
                        //点踩  并且不是人工状态 才显示转人工的系统提示语
                        showCaiToCustomerTip();
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_net_work_err));
                }
            });
            //判断点踩原因是否开启
            if (!revaluateFlag) {
                //显示点踩的原因
                if (aiRobotRealuateConfigInfo != null) {
                    if (aiRobotRealuateConfigInfo.getRealuateInfoFlag() == 1) {
                        //开启了
                        showAIRobotTipMsg(message.getMsgId(), message.getCid(), message.getAiAgentCid());
                    }
                } else {
                    getAiRobotRealuateConfigInfo(true, message.getMsgId(), message.getCid(), message.getAiAgentCid());
                }
            } else {
                CustomToast.makeText(mAppContext, revaluateFlag ? getResources().getString(R.string.sobot_ding_cai_like) : getResources().getString(R.string.sobot_ding_cai_dislike), 1500).show();
            }
        } else {
            zhiChiApi.rbAnswerComment(SobotChatFragment.this, message.getMsgId(), initModel.getPartnerid(), initModel.getCid(), initModel.getRobotid() + "",
                    message.getDocId(), message.getDocName(), revaluateFlag, message.getOriginQuestion(), message.getAnswerType(), message.getGptAnswerType(), message.getAnswer(), new StringResultCallBack<CommonModelBase>() {
                        @Override
                        public void onSuccess(CommonModelBase data) {
                            if (!isActive()) {
                                return;
                            }
                            //if (ZhiChiConstant.client_sendmsg_to_custom_fali.equals(data.getStatus())) {
                            //  customerServiceOffline(initModel, 1);
                            //} else if (ZhiChiConstant.client_sendmsg_to_custom_success.equals(data.getStatus())) {
                            //改变顶踩按钮的布局
                            message.setRevaluateState(revaluateFlag ? 2 : 3);
                            refreshItemByCategory(ImageMessageHolder.class);
                            refreshItemByCategory(RichTextMessageHolder.class);
                            refreshItemByCategory(FileMessageHolder.class);
                            refreshItemByCategory(VideoMessageHolder.class);
                            refreshItemByCategory(MiniProgramMessageHolder.class);
                            if ((!TextUtils.isEmpty(message.getAnswerType()) && message.getAnswerType().startsWith("152"))) {
                                refreshItemByCategory(RobotTemplateMessageHolder1.class);
                                refreshItemByCategory(RobotTemplateMessageHolder2.class);
                                refreshItemByCategory(RobotTemplateMessageHolder3.class);
                                refreshItemByCategory(RobotTemplateMessageHolder4.class);
                                refreshItemByCategory(RobotTemplateMessageHolder5.class);
                                refreshItemByCategory(RobotTemplateMessageHolder6.class);
                            }
                            //仅机器人不显示
                            if (initModel.getRealuateTransferFlag() == 1 && current_client_model != ZhiChiConstant.client_model_customService && !revaluateFlag && type != ZhiChiConstant.type_robot_only) {
                                //点踩  并且不是人工状态 才显示转人工的系统提示语
                                String content = getSobotActivity().getResources().getString(R.string.sobot_cant_solve_problem_new);
                                String click = getSobotActivity().getResources().getString(R.string.sobot_customer_service);
                                zhiChiApi.insertSysMsg(SobotChatFragment.this, initModel.getCid(), initModel.getPartnerid(), String.format(content, click), "点踩转人工提示", new StringResultCallBack<BaseCode>() {
                                    @Override
                                    public void onSuccess(BaseCode baseCode) {
                                        showCaiToCustomerTip();
                                    }

                                    @Override
                                    public void onFailure(Exception e, String des) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e, String des) {
                            ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_net_work_err));
                        }
                    });
            //判断点踩原因是否开启
            if (!revaluateFlag && initModel.getRealuateInfoFlag() == 1) {
                //显示点踩的原因
                if (mRealuateConfig != null) {
                    sendRealuateConfig(message.getMsgId(), message.getCid());
                } else {
                    requestRealuateConfig(true, message.getMsgId(), message.getCid());
                }
            } else {
                CustomToast.makeText(mAppContext, revaluateFlag ? getResources().getString(R.string.sobot_ding_cai_like) : getResources().getString(R.string.sobot_ding_cai_dislike), 1500).show();
            }
        }
    }

    /**
     * 大模型机器人点踩--显示点踩后评价引导语
     */
    private void showAIRobotTipMsg(String msgId, String cid, String aiAgentCid) {
        if (aiRobotRealuateConfigInfo != null) {
            SobotAiRobotRealuateInfo aiRealuateInfo = new SobotAiRobotRealuateInfo();
            aiRealuateInfo.setMsgId(msgId);
            aiRealuateInfo.setMsg(aiRobotRealuateConfigInfo.getRealuateAfterWord());
            aiRealuateInfo.setAiRobotRealuateConfigInfo(aiRobotRealuateConfigInfo);
            aiRealuateInfo.setCid(cid);
            aiRealuateInfo.setUid(initModel.getPartnerid());
            aiRealuateInfo.setType("insert");
            aiRealuateInfo.setSubmitStatus(1);
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            base.setMsgId(msgId);
            base.setId(msgId);
            base.setCid(cid);
            base.setAiAgentCid(aiAgentCid);
            base.setSenderName(initModel.getRobotName());
            base.setSender(initModel.getRobotName());
            base.setSenderFace(initModel.getRobotLogo());
            base.setServant("aiagent");
            base.setSenderType(ZhiChiConstant.message_sender_type_ai_tobot_cai_card);
            base.setAiRobotRealuateInfo(aiRealuateInfo);
            updateUiMessage(messageAdapter, base);
        }
    }

    /**
     * 客服邀请评价
     *
     * @param evaluateFlag true 直接提交  false 打开评价窗口
     * @param message      data
     */
    @Override
    public void doEvaluate(final boolean evaluateFlag, final ZhiChiMessageBase message) {
        if (initModel == null || message == null) {
            return;
        }
        SobotEvaluateModel sobotEvaluateModel = message.getSobotEvaluateModel();
        if (sobotEvaluateModel == null) {
            return;
        }
        if (evaluateFlag) {
            SobotCommentParam sobotCommentParam = new SobotCommentParam();
            sobotCommentParam.setType("1");
            sobotCommentParam.setScore(message.getSobotEvaluateModel().getScore() + "");
            sobotCommentParam.setScoreFlag(message.getSobotEvaluateModel().getScoreFlag());
            sobotCommentParam.setCommentType(0);
            sobotCommentParam.setProblem(sobotEvaluateModel.getProblem());
            sobotCommentParam.setIsresolve(sobotEvaluateModel.getIsResolved());
            sobotCommentParam.setScoreExplain(sobotEvaluateModel.getScoreExplain());
            sobotCommentParam.setScoreExplainLan(sobotEvaluateModel.getScoreExplainLan());
            sobotCommentParam.setTagsJson(sobotEvaluateModel.getTagsJson());

            zhiChiApi.comment(SobotChatFragment.this, initModel.getCid(), initModel.getPartnerid(), sobotCommentParam, new StringResultCallBack<CommonModel>() {
                @Override
                public void onSuccess(CommonModel commonModel) {
                    if (!isActive()) {
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setAction(ZhiChiConstants.dcrc_comment_state);
                    intent.putExtra("commentState", true);
                    intent.putExtra("commentType", 0);
                    intent.putExtra("score", message.getSobotEvaluateModel().getScore());
                    intent.putExtra("isResolved", message.getSobotEvaluateModel().getIsResolved());
                    CommonUtils.sendLocalBroadcast(mAppContext, intent);
                }

                @Override
                public void onFailure(Exception e, String des) {
                }
            });
        } else {
            submitEvaluation(false, sobotEvaluateModel.getScore(), sobotEvaluateModel.getIsResolved(), sobotEvaluateModel.getProblem());
        }
    }

    /**
     * 刷新所有指定类型viewHolder
     *
     * @param clz viewHolder.class
     */
    private <T> void refreshItemByCategory(final Class<T> clz) {
        if (!isActive()) {
            return;
        }
        messageRV.post(new Runnable() {

            @Override
            public void run() {
                for (int i = 0, count = messageRV.getChildCount(); i < count; i++) {
                    View child = messageRV.getChildAt(i);
                    if (child == null || child.getTag() == null) {
                        continue;
                    }
                    if (clz == RichTextMessageHolder.class && child.getTag() instanceof RichTextMessageHolder) {
                        RichTextMessageHolder holder = (RichTextMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == TextMessageHolder.class && child.getTag() instanceof TextMessageHolder) {
                        TextMessageHolder holder = (TextMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == ArticleMessageHolder.class && child.getTag() instanceof ArticleMessageHolder) {
                        ArticleMessageHolder holder = (ArticleMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == CusEvaluateMessageHolder.class && child.getTag() instanceof CusEvaluateMessageHolder) {
                        CusEvaluateMessageHolder holder = (CusEvaluateMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder1.class && child.getTag() instanceof RobotTemplateMessageHolder1) {
                        RobotTemplateMessageHolder1 holder = (RobotTemplateMessageHolder1) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder2.class && child.getTag() instanceof RobotTemplateMessageHolder2) {
                        RobotTemplateMessageHolder2 holder = (RobotTemplateMessageHolder2) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder3.class && child.getTag() instanceof RobotTemplateMessageHolder3) {
                        RobotTemplateMessageHolder3 holder = (RobotTemplateMessageHolder3) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder4.class && child.getTag() instanceof RobotTemplateMessageHolder4) {
                        RobotTemplateMessageHolder4 holder = (RobotTemplateMessageHolder4) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder5.class && child.getTag() instanceof RobotTemplateMessageHolder5) {
                        RobotTemplateMessageHolder5 holder = (RobotTemplateMessageHolder5) child.getTag();
                        holder.refreshItem();
                    } else if (clz == RobotTemplateMessageHolder6.class && child.getTag() instanceof RobotTemplateMessageHolder6) {
                        RobotTemplateMessageHolder6 holder = (RobotTemplateMessageHolder6) child.getTag();
                        holder.refreshItem();
                    } else if (clz == FileMessageHolder.class && child.getTag() instanceof FileMessageHolder) {
                        FileMessageHolder holder = (FileMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == VideoMessageHolder.class && child.getTag() instanceof VideoMessageHolder) {
                        VideoMessageHolder holder = (VideoMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == ImageMessageHolder.class && child.getTag() instanceof ImageMessageHolder) {
                        ImageMessageHolder holder = (ImageMessageHolder) child.getTag();
                        holder.refreshItem();
                    } else if (clz == MiniProgramMessageHolder.class && child.getTag() instanceof MiniProgramMessageHolder) {
                        MiniProgramMessageHolder holder = (MiniProgramMessageHolder) child.getTag();
                        holder.refreshItem();
                    }
                }
            }
        });
    }

    //置顶通告设置
    private void getAnnouncement() {
        if (!TextUtils.isEmpty(initModel.getAnnounceClickUrl())) {
            sobot_announcement_right_icon.setVisibility(View.VISIBLE);
        } else {
            sobot_announcement_right_icon.setVisibility(View.GONE);
        }

        if (initModel.getAnnounceMsgFlag() && initModel.isAnnounceTopFlag() && !TextUtils.isEmpty(initModel.getAnnounceMsg())) {
            sobot_announcement.setVisibility(View.VISIBLE);
            sobot_announcement_title.setText(initModel.getAnnounceMsg());
            sobot_announcement.setOnClickListener(new OnMultiClickListener() {
                @Override
                public void onMultiClick(View v) {
                    // 内部浏览器
                    if (!TextUtils.isEmpty(initModel.getAnnounceClickUrl())) {
                        if (SobotOption.hyperlinkListener != null) {
                            SobotOption.hyperlinkListener.onUrlClick(initModel.getAnnounceClickUrl());
                            return;
                        }
                        if (SobotOption.newHyperlinkListener != null) {
                            //如果返回true,拦截;false 不拦截
                            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotActivity(), initModel.getAnnounceClickUrl());
                            if (isIntercept) {
                                return;
                            }
                        }
                        Intent intent = new Intent(mAppContext, WebViewActivity.class);
                        intent.putExtra("url", initModel.getAnnounceClickUrl());
                        startActivity(intent);
                    }
                }
            });
        } else {
            sobot_announcement.setVisibility(View.GONE);
        }
    }

    /**
     * 设置底部键盘UI
     *
     * @param viewType
     */
    public void setBottomView(int viewType) {
        welcome.setVisibility(View.GONE);
        if (initModel != null && initModel.getVisitorScheme() != null) {
            //导航条显示1 开启 0 关闭
            if (initModel.getVisitorScheme().getTopBarFlag() == 1) {
                relative.setVisibility(View.VISIBLE);
            } else {
                relative.setVisibility(View.GONE);
            }
        } else {
            relative.setVisibility(View.VISIBLE);
        }
        chat_main.setVisibility(View.VISIBLE);
        et_sendmessage.setVisibility(View.VISIBLE);
        sobot_ll_restart_talk.setVisibility(View.GONE);
        sobot_ll_bottom.setVisibility(View.VISIBLE);

        if (isUserBlack()) {
            sobot_ll_restart_talk.setVisibility(View.GONE);
            sobot_ll_bottom.setVisibility(View.VISIBLE);
            btn_model_voice.setVisibility(View.GONE);
            cb_emoticon_view.setVisibility(View.VISIBLE);
        }
        if (info.isHideMenuSatisfaction()) {
            ll_satisfaction.setVisibility(View.GONE);
        } else {
            ll_satisfaction.setVisibility(View.VISIBLE);
        }
        ll_restart_talk.setVisibility(View.VISIBLE);
        ll_message.setVisibility(View.VISIBLE);

        String inputHintStr = "";
        LogUtils.i("setBottomView:" + viewType);
        switch (viewType) {
            case ZhiChiConstant.bottomViewtype_onlyrobot:
                // 仅机器人
                showVoiceBtn();
                if (image_reLoading.getVisibility() == View.VISIBLE) {
                    sobot_ll_bottom.setVisibility(View.VISIBLE);/* 底部聊天布局 */
                    edittext_layout.setVisibility(View.VISIBLE);/* 文本输入框布局 */
                    sobot_ll_restart_talk.setVisibility(View.GONE);

                    if (btn_press_to_speak.getVisibility() == View.VISIBLE) {
                        btn_press_to_speak.setVisibility(View.GONE);
                    }
//                    btn_set_mode_rengong.setClickable(false);
//                    btn_set_mode_rengong.setVisibility(View.GONE);
                }
                cb_emoticon_view.setVisibility(View.VISIBLE);
                if (getAddPlanMemuCount() > 0) {
                    cb_plus_view.setVisibility(View.VISIBLE);
                } else {
                    cb_plus_view.setVisibility(View.GONE);
                }
                btn_send_pic.setVisibility(View.GONE);
                //设置输入框提示语
                if (initModel.getVisitorScheme() != null && !StringUtils.isEmpty(initModel.getVisitorScheme().getRobotDoc())) {
                    inputHintStr = initModel.getVisitorScheme().getRobotDoc();
                }
                break;
            case ZhiChiConstant.bottomViewtype_robot:
                //机器人对话框
                showVoiceBtn();
                if (Build.VERSION.SDK_INT >= 11)
//                    btn_set_mode_rengong.setAlpha(1f);
                    if (image_reLoading.getVisibility() == View.VISIBLE) {
                        sobot_ll_bottom.setVisibility(View.VISIBLE);/* 底部聊天布局 */
                        edittext_layout.setVisibility(View.VISIBLE);/* 文本输入框布局 */
                        sobot_ll_restart_talk.setVisibility(View.GONE);

                        if (btn_press_to_speak.getVisibility() == View.VISIBLE) {
                            btn_press_to_speak.setVisibility(View.GONE);
                        }
//                    btn_set_mode_rengong.setClickable(true);
//                    btn_set_mode_rengong.setEnabled(true);
                    }
                if (getAddPlanMemuCount() > 0) {
                    cb_plus_view.setVisibility(View.VISIBLE);
                } else {
                    cb_plus_view.setVisibility(View.GONE);
                }
                cb_emoticon_view.setVisibility(View.VISIBLE);
                btn_send_pic.setVisibility(View.GONE);
                //设置输入框提示语
                if (initModel.getVisitorScheme() != null && !StringUtils.isEmpty(initModel.getVisitorScheme().getRobotDoc())) {
                    inputHintStr = initModel.getVisitorScheme().getRobotDoc();
                }
                break;
            case ZhiChiConstant.bottomViewtype_customer:
                //人工对话框
                hideRobotVoiceHint();
                btn_model_edit.setVisibility(View.GONE);
//                btn_set_mode_rengong.setVisibility(View.GONE);
                if (getAddPlanMemuCount() > 0) {
                    cb_plus_view.setVisibility(View.VISIBLE);
                } else {
                    cb_plus_view.setVisibility(View.GONE);
                }
                btn_send_pic.setVisibility(View.GONE);
                showEmotionBtn();
                showVoiceBtn();
                btn_model_voice.setEnabled(true);
                btn_model_voice.setClickable(true);
                cb_plus_view.setEnabled(true);
                cb_plus_view.setClickable(true);
                cb_emoticon_view.setClickable(true);
                cb_emoticon_view.setEnabled(true);
                if (Build.VERSION.SDK_INT >= 11) {
                    btn_model_voice.setAlpha(1f);
                    cb_plus_view.setAlpha(1f);
                }

                edittext_layout.setVisibility(View.VISIBLE);
                sobot_ll_bottom.setVisibility(View.VISIBLE);
                btn_press_to_speak.setVisibility(View.GONE);
                btn_press_to_speak.setClickable(true);
                btn_press_to_speak.setEnabled(true);
                txt_speak_content.setText(R.string.sobot_press_say);
                //设置输入框提示语
                if (initModel.getVisitorScheme() != null && !StringUtils.isEmpty(initModel.getVisitorScheme().getCustomDoc())) {
                    inputHintStr = initModel.getVisitorScheme().getCustomDoc();
                }
                break;
            case ZhiChiConstant.bottomViewtype_onlycustomer_paidui:
                //仅人工排队中
                onlyCustomPaidui();

                hidePanelAndKeyboard(mPanelLayout);
                if (rvScrollLayoutManager != null && rvScrollLayoutManager.findLastVisibleItemPosition() != messageAdapter.getItemCount()) {
                    gotoLastItem();
                }
                //设置输入框提示语
                if (initModel.getVisitorScheme() != null && !StringUtils.isEmpty(initModel.getVisitorScheme().getWaitDoc())) {
                    inputHintStr = initModel.getVisitorScheme().getWaitDoc();
                }
                break;
            case ZhiChiConstant.bottomViewtype_outline:
                //被提出
                hideReLoading();
                hidePanelAndKeyboard(mPanelLayout);/*隐藏键盘*/
                //清空输入框内的内容
                et_sendmessage.setText("");
                sobot_ll_bottom.setVisibility(View.GONE);
                sobot_ll_restart_talk.setVisibility(View.VISIBLE);
                if (info.isHideMenuSatisfaction()) {
                    ll_satisfaction.setVisibility(View.GONE);
                } else {
                    //会话结束 没有咨询过就不显示评价按钮
                    if (isAboveZero) {
                        ll_satisfaction.setVisibility(View.VISIBLE);
                    } else {
                        ll_satisfaction.setVisibility(View.GONE);
                    }
                }
                ll_restart_talk.setVisibility(View.VISIBLE);
                btn_model_edit.setVisibility(View.GONE);
                if (info.isHideMenuLeave()) {
                    ll_message.setVisibility(View.GONE);
                } else {
                    ll_message.setVisibility(initModel.getMsgFlag() == ZhiChiConstant.sobot_msg_flag_close ? View
                            .GONE : View.VISIBLE);
                }
                btn_model_voice.setVisibility(View.GONE);
                //是否开启点踩问答
                if (initModel.getRealuateInfoFlag() == 1) {
                    LogUtils.d("==========");
                    //过滤未提交的点踩问答
                    messageAdapter.removeCaiNoSubmitMsg();
                }
                gotoLastItem();
                break;
            case ZhiChiConstant.bottomViewtype_paidui:
                //智能模式下排队中
                if (btn_press_to_speak.getVisibility() == View.GONE) {
                    showVoiceBtn();
                }
//                btn_set_mode_rengong.setVisibility(View.VISIBLE);
                cb_emoticon_view.setVisibility(View.VISIBLE);
                if (image_reLoading.getVisibility() == View.VISIBLE) {
                    sobot_ll_bottom.setVisibility(View.VISIBLE);/* 底部聊天布局 */
                    edittext_layout.setVisibility(View.VISIBLE);/* 文本输入框布局 */
                    btn_model_voice.setVisibility(View.GONE);
                    sobot_ll_restart_talk.setVisibility(View.GONE);

                    if (btn_press_to_speak.getVisibility() == View.VISIBLE) {
                        btn_press_to_speak.setVisibility(View.GONE);
                    }
                }
                //设置输入框提示语
                if (initModel.getVisitorScheme() != null && !StringUtils.isEmpty(initModel.getVisitorScheme().getWaitDoc())) {
                    inputHintStr = initModel.getVisitorScheme().getWaitDoc();
                }
                break;
            case ZhiChiConstant.bottomViewtype_custom_only_msgclose:
                sobot_ll_restart_talk.setVisibility(View.VISIBLE);

                sobot_ll_bottom.setVisibility(View.GONE);
                if (image_reLoading.getVisibility() == View.VISIBLE) {
                    ll_restart_talk.setVisibility(View.VISIBLE);
                    sobot_txt_restart_talk.setClickable(true);
                    sobot_txt_restart_talk.setEnabled(true);
                }
                if (initModel.getMsgFlag() == ZhiChiConstant.sobot_msg_flag_close) {
                    //留言关闭
                    ll_satisfaction.setVisibility(View.GONE);
                    ll_message.setVisibility(View.GONE);
                } else {
                    ll_satisfaction.setVisibility(View.GONE);
                    ll_message.setVisibility(View.VISIBLE);
                }
                break;
        }
        //设置输入框提示语
        if (!StringUtils.isEmpty(inputHintStr)) {
            et_sendmessage.setHint(inputHintStr);
        }
        setMenuFrist(viewType);
        hideReLoading();
    }

    //仅人工时排队UI更新
    private void onlyCustomPaidui() {
        if (SobotOption.sobotChatStatusListener != null) {
            //仅人工排队状态
            SobotOption.sobotChatStatusListener.onChatStatusListener(SobotChatStatusMode.ZCServerConnectWaiting);
        }
        sobot_ll_bottom.setVisibility(View.VISIBLE);

//        btn_set_mode_rengong.setVisibility(View.GONE);
//        btn_set_mode_rengong.setClickable(false);

        if (getAddPlanMemuCount() > 0) {
            cb_plus_view.setVisibility(View.VISIBLE);
        } else {
            cb_plus_view.setVisibility(View.GONE);
        }
        btn_send_pic.setVisibility(View.GONE);
        cb_plus_view.setClickable(false);
        cb_plus_view.setEnabled(false);

        showEmotionBtn();
        cb_emoticon_view.setClickable(false);
        cb_emoticon_view.setEnabled(false);

        showVoiceBtn();
        btn_model_voice.setClickable(false);
        btn_model_voice.setEnabled(false);
        btn_model_voice.setVisibility(View.GONE);


        edittext_layout.setVisibility(View.GONE);
        cb_emoticon_view.setVisibility(View.GONE);
        btn_press_to_speak.setClickable(false);
        btn_press_to_speak.setEnabled(false);
        btn_press_to_speak.setVisibility(View.VISIBLE);
        txt_speak_content.setText(R.string.sobot_in_line);
        showLogicTitle("", null, false);
        if (sobot_ll_restart_talk.getVisibility() == View.VISIBLE) {
            sobot_ll_restart_talk.setVisibility(View.GONE);
        }
    }

    //3.0.3 type 0 转人工后创建商品卡片，1 人工状态每次返回再进入聊天页面是否再发送商品卡片
    private void createConsultingContent(int type) {
        ConsultingContent consultingContent = info.getConsultingContent();
        if (consultingContent != null && !TextUtils.isEmpty(consultingContent.getSobotGoodsTitle()) && !TextUtils.isEmpty(consultingContent.getSobotGoodsFromUrl())) {
            ZhiChiMessageBase zhichiMessageBase = new ZhiChiMessageBase();
            zhichiMessageBase.setSenderType(ZhiChiConstant.message_sender_type_consult_info);
            if (!TextUtils.isEmpty(consultingContent.getSobotGoodsImgUrl())) {
                zhichiMessageBase.setPicurl(consultingContent.getSobotGoodsImgUrl());
            }
            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
            zhichiMessageBase.setAnswer(reply);
            zhichiMessageBase.setContent(consultingContent.getSobotGoodsTitle());
            zhichiMessageBase.setUrl(consultingContent.getSobotGoodsFromUrl());
            zhichiMessageBase.setCid(initModel == null ? "" : initModel.getCid());
            zhichiMessageBase.setAname(consultingContent.getSobotGoodsLable());
            zhichiMessageBase.setReceiverFace(consultingContent.getSobotGoodsDescribe());

            zhichiMessageBase.setAction(ZhiChiConstant.action_consultingContent_info);
            updateUiMessage(messageAdapter, zhichiMessageBase);
            gotoLastItem();
            if (consultingContent.isAutoSend()) {
                if (type == 1) {
                    //人工状态下每次返回再进入聊天页面是否再发送商品卡片
                    if (consultingContent.isEveryTimeAutoSend()) {
                        sendConsultingContent();
                    }
                } else {
                    sendConsultingContent();
                }
            }
        } else {
            if (messageAdapter != null) {
                messageAdapter.removeConsulting();
            }
        }
    }

    //创建订单卡片，根据设置的isAutoSend，判断是否自动发送
    //3.0.3 type 0 转人工后创建订单卡片，1 人工状态每次返回再进入聊天页面是否再发送订单卡片
    private void createOrderCardContent(int type) {
        OrderCardContentModel orderCardContent = info.getOrderGoodsInfo();
        if (orderCardContent != null && !TextUtils.isEmpty(orderCardContent.getOrderCode()) && orderCardContent.isAutoSend()) {
            if (type == 1) {
                //人工状态下每次返回再进入聊天页面是否再发送订单卡片
                if (orderCardContent.isEveryTimeAutoSend()) {
                    sendOrderCardMsg(orderCardContent);
                }
            } else {
                sendOrderCardMsg(orderCardContent);
            }
        }
    }


    /**
     * 导航栏关闭按钮点击事件
     */
    protected void onCloseMenuClick() {
        hidePanelAndKeyboard(mPanelLayout);
        if (isActive()) {
            if (info.isShowCloseSatisfaction() || (initModel != null && initModel.getCommentFlag() == 1)) {
                if (current_client_model == ZhiChiConstant.client_model_robot && initModel.isAiAgent()) {
                    if (initModel.getAiAgentCommentFlag() == 1) {
                        showAiEvaluateDialog(isSessionOver, true, true, initModel, current_client_model, 1, currentUserName, 5, -2, "", false, true);
                        return;
                    }
                } else {
                    if (isAboveZero && !isComment) {
                        // 退出时 之前没有评价过的话 才能 弹评价框
                        Intent intent = showEvaluateDialog(getSobotActivity(), isSessionOver, true, true, initModel,
                                current_client_model, 1, currentUserName, 5, -2, "", false, true);
                        startActivity(intent);
                        return;
                    } else {
                        isSessionOver = true;
                        userOffline(initModel);
                        ChatUtils.userLogout(mAppContext, "onCloseMenuClick 点击右上角关闭按钮");
                    }
                }
            } else {
                isSessionOver = true;
                userOffline(initModel);
                ChatUtils.userLogout(mAppContext, "onCloseMenuClick 点击右上角关闭按钮");
            }
            mUnreadNum = 0;
            finish();
        }
    }

    /**
     * 导航栏左侧返回按钮  弹出是否结束会话框  结束回话 事件
     */
    protected void onLeftBackColseClick() {
        hidePanelAndKeyboard(mPanelLayout);
        if (isActive()) {
            if (info.isShowSatisfaction() || (initModel != null && initModel.getCommentFlag() == 1)) {
                if (current_client_model == ZhiChiConstant.client_model_robot && initModel.isAiAgent()) {
                    if (initModel.getAiAgentCommentFlag() == 1) {
                        showAiEvaluateDialog(isSessionOver, true, true, initModel, current_client_model, 1, currentUserName, 5, -2, "", false, true);
                        return;
                    }
                } else {
                    if (isAboveZero && !isComment) {
                        // 退出时 之前没有评价过的话 才能 弹评价框
                        Intent intent = showEvaluateDialog(getSobotActivity(), isSessionOver, true, true, initModel,
                                current_client_model, 1, currentUserName, 5, -2, "", false, true);
                        startActivity(intent);
                        return;
                    } else {
                        isSessionOver = true;
//                    customerServiceOffline(initModel, 1);
                        ChatUtils.userLogout(mAppContext, "onLeftBackColseClick 导航栏左侧返回按钮  弹出是否结束会话框  结束回话");
                    }
                }
            } else {
                isSessionOver = true;
//                customerServiceOffline(initModel, 1);
                ChatUtils.userLogout(mAppContext, "onLeftBackColseClick 导航栏左侧返回按钮  弹出是否结束会话框  结束回话");
            }
            mUnreadNum = 0;
            finish();
        }
    }

    /**
     * 导航栏动态判断是否显示 头像、昵称、公司名字
     *
     * @param title       此处传如的值为默认需要显示的昵称 或者提示等等
     * @param avatarUrl   头像
     * @param ignoreLogic 表示忽略逻辑直接显示
     */
    private void showLogicTitle(String title, String avatarUrl, boolean ignoreLogic) {
        try {
            //4.0.0之后导航栏显示判断优先顺序：客户设置>后端接口返回>本地
            if (initModel != null) {
                //是否显示头像,true 显示;false 隐藏,默认true
                boolean isShowAvatar = false;
                boolean isCircl = true;//头像是否是圆形
                //是否显示昵称,true 显示;false 隐藏,默认false
                boolean isShowTitle = false;
                //是否显示公司名称,true 显示;false 隐藏,默认false
                boolean isShowCompanyName = false;
                int topBarType = 1;//导航条样式 1展示接待客服+企业名称    2.展示企业信息 +接待状态
                if (initModel != null && initModel.getVisitorScheme() != null) {
                    topBarType = initModel.getVisitorScheme().getTopBarType();
                    //导航条样式 2.展示企业信息 +接待状态 直接判断是否显示公司logo
                    if (initModel.getVisitorScheme().getTopBarType() == 2) {
                        if (initModel.getVisitorScheme().getTopBarCompanyLogoFlag() == 1) {
                            //显示公司头像
                            isShowAvatar = true;
                            avatarUrl = initModel.getVisitorScheme().getTopBarCompanyLogoUrl();
                            isCircl = false;
                        } else {
                            //不显示公司头像
                            isShowAvatar = false;
                        }
                    } else {
                        if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                            //仅人工排队时显示公司logo
                            if (initModel.getVisitorScheme().getTopBarCompanyLogoFlag() == 1) {
                                isShowAvatar = true;
                                avatarUrl = initModel.getVisitorScheme().getTopBarCompanyLogoUrl();
                                isCircl = false;
                            } else {
                                isCircl = true;
                                isShowAvatar = false;
                            }
                        } else {
                            //判断服务端返回的导航条客服头像是否显示
                            if (initModel.getVisitorScheme().getTopBarStaffPhotoFlag() == 1) {
                                isShowAvatar = true;
                            } else {
                                isShowAvatar = false;
                            }
                        }
                    }
                }
                if (initModel != null && initModel.getVisitorScheme() != null) {
                    //导航条样式 2.展示企业信息 +接待状态 直接判断是否显示公司logo
                    if (initModel.getVisitorScheme().getTopBarType() == 2) {
                        isShowTitle = false;
                    } else {
                        //判断服务端返回的导航条客服昵称是否显示
                        if (initModel.getVisitorScheme().getTopBarStaffNickFlag() == 1) {
                            isShowTitle = true;
                        } else {
                            isShowTitle = false;
                        }
                    }
                }

                String companyName = "";
                if (initModel != null && initModel.getVisitorScheme() != null) {
                    companyName = initModel.getVisitorScheme().getTopBarCompanyName();
                }
                if (initModel != null && initModel.getVisitorScheme() != null) {
                    //判断服务端返回的导航条公司名称是否显示
                    if (initModel.getVisitorScheme().getTopBarCompanyNameFlag() == 1) {
                        //企业名称 显示开启
                        if (initModel.getVisitorScheme().getTopBarType() == 2 && initModel.getVisitorScheme().getTopBarCompanyLogoFlag() == 1) {
                            //企业接待当同时勾选“企业 Logo”时，“企业名称 不显示
                            isShowCompanyName = false;
                        } else {
                            isShowCompanyName = true;
                        }
                    } else {
                        isShowCompanyName = false;
                    }
                }
                sobot_titlebar_text_ll.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

                if (!isShowTitle && isShowCompanyName) {
                    mCompanyTextView.setTextSize(14);
                }
                if (isShowAvatar && sobot_header_center_ll != null) {
                    //企业头像
                    if (topBarType == 2) {
                        //企业接待
                        //只显示公司logo
                        sobot_header_center_ll.setGravity(Gravity.CENTER);
                        if (mAvatarIV != null) {
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ScreenUtils.dip2px(getSobotActivity(), 100), ScreenUtils.dip2px(getSobotActivity(), 40));
                            mAvatarIV.setImageWidthAndHeight(ScreenUtils.dip2px(getSobotActivity(), 100), ScreenUtils.dip2px(getSobotActivity(), 40));
                            int rightShowCount = 0;//右边有几个按钮显示
                            if (iv_close.getVisibility() == View.VISIBLE) {
                                rightShowCount = rightShowCount + 1;
                            }
                            if (sobot_iv_right.getVisibility() == View.VISIBLE) {
                                rightShowCount = rightShowCount + 1;
                            }
                            if (iv_right_third.getVisibility() == View.VISIBLE) {
                                rightShowCount = rightShowCount + 1;
                            }
                            if (iv_right_second.getVisibility() == View.VISIBLE) {
                                rightShowCount = rightShowCount + 1;
                            }
                            //4 是右边按钮到边框的间距
                            if (rightShowCount == 0) {
                                lp.setMarginStart(SobotDensityUtil.dp2px(getSobotActivity(), 4));
                                lp.setMarginEnd(SobotDensityUtil.dp2px(getSobotActivity(), 40));
                            } else if (rightShowCount == 1) {
                                lp.setMarginStart(SobotDensityUtil.dp2px(getSobotActivity(), 4));
                            } else if (rightShowCount == 2) {
                                lp.setMarginStart(SobotDensityUtil.dp2px(getSobotActivity(), 40 + 4));
                            } else if (rightShowCount == 3) {
                                lp.setMarginStart(SobotDensityUtil.dp2px(getSobotActivity(), 80 + 4));
                            }
                            mAvatarIV.setLayoutParams(lp);
                        }
                    } else {
                        //客服接待
                        sobot_header_center_ll.setGravity(Gravity.CENTER_VERTICAL);
                        int w = ScreenUtils.dip2px(getSobotActivity(), 32);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, w);
                        mAvatarIV.setImageWidthAndHeight(w, w);
                        mAvatarIV.setMinHeight(w);
                        mAvatarIV.setMaxHeight(w);
                        mAvatarIV.setLayoutParams(lp);
                    }
                }
                setAvatar(avatarUrl, isShowAvatar, isCircl);
                setTitle(title, isShowTitle);
                setCompany(companyName, isShowCompanyName);
            }
        } catch (Exception e) {
        }
    }

    // 设置导航栏昵称
    public void setTitle(CharSequence title, boolean isShowTitle) {
        if (isShowTitle) {
            mTitleTextView.setVisibility(View.VISIBLE);
        } else {
            mTitleTextView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(title)) {
            mTitleTextView.setText(title);
        } else {
            //昵称没有值，也隐藏
            mTitleTextView.setVisibility(View.GONE);
        }
    }

    // 设置设置导航栏公司名称
    public void setCompany(CharSequence title, boolean isShowTitle) {
        if (isShowTitle) {
            mCompanyTextView.setVisibility(View.VISIBLE);
        } else {
            mCompanyTextView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(title)) {
            mCompanyTextView.setText(title);
        }
    }

    // 设置设置导航栏头像
    public void setAvatar(String avatarUrl, boolean isShowAvatar, boolean isCircle) {
        if (isShowAvatar) {
            mAvatarIV.setVisibility(View.VISIBLE);
            mAvatarIV.setRoundAsCircle(isCircle);
            if (!TextUtils.isEmpty(avatarUrl)) {
                mAvatarIV.setImageUrl(avatarUrl);
            }
            mAvatarIV.setTag(avatarUrl);
        } else {
            mAvatarIV.setVisibility(View.GONE);
        }
    }

    private SobotCommonDialog sobotBackDialog;//左上角返回按钮弹窗

    /**
     * 导航栏左边点击事件
     */
    protected void onLeftMenuClick() {
        //返回时未知问题或引导答案触发智能转人工按钮，把次数改成0，防止机器人模式下次进来还会显示
        showTimeVisiableCustomBtn = 0;
        hidePanelAndKeyboard(mPanelLayout);
        if (!isSessionOver && info.isShowLeftBackPop()) {//会话没有结束并且有提示
            sobotBackDialog = new SobotCommonDialog(getSobotActivity(), getResources().getString(R.string.sobot_will_end_conversation), "", getResources().getString(R.string.sobot_temporary_leave), getResources().getString(R.string.sobot_cancle_conversation), new SobotCommonDialog.MyClickListener() {
                @Override
                public void clickOk() {
                    //暂时离开
                    if (isActive()) {
                        //按返回按钮的时候 如果面板显示就隐藏面板  如果面板已经隐藏那么就是用户想退出
                        if (mPanelLayout.getVisibility() == View.VISIBLE) {
                            hidePanelAndKeyboard(mPanelLayout);
                            return;
                        }
                        finish();
                    }
                }

                @Override
                public void clickCancle() {
                    //结束会话
                    onLeftBackColseClick();
                }
            });
            sobotBackDialog.show();
        } else {
            onBackPress();
        }
    }

    @Override
    public void onDestroy() {
        if (SobotOption.functionClickListener != null) {
            SobotOption.functionClickListener.onClickFunction(getSobotActivity(), SobotFunctionType.ZC_CloseChat);
        }
        try {
            // 取消广播接受者
            if (getSobotActivity() != null) {
                if (receiver != null) {
                    getSobotActivity().unregisterReceiver(receiver);
                }
                if (mKPSwitchListener != null) {
                    KeyboardUtil.detach(getSobotActivity(), mKPSwitchListener);
                }
            }
            if (localBroadcastManager != null) {
                localBroadcastManager.unregisterReceiver(localReceiver);
            }
            if (sobotBackDialog != null) {
                sobotBackDialog.dismiss();
                sobotBackDialog = null;
            }
            if (clearHistoryMsgDialog != null) {
                clearHistoryMsgDialog.dismiss();
                clearHistoryMsgDialog = null;
            }
        } catch (Exception e) {
            //ignor
        }
        super.onDestroy();
    }

    /**
     * 导航栏右边点击事件
     *
     * @param view
     */
    protected void onRightMenuClick(View view) {
        hidePanelAndKeyboard(mPanelLayout);
        Intent intent = new Intent(getSobotActivity(), SobotClearHistoryActivity.class);
        startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CLEAR_HISTORY);
//        ClearHistoryDialog clearHistoryDialog = new ClearHistoryDialog(getSobotActivity());
//        clearHistoryDialog.setCanceledOnTouchOutside(true);
//        clearHistoryDialog.setOnClickListener(new ClearHistoryDialog.DialogOnClickListener() {
//            @Override
//            public void onSure() {
//                clearHistory();
//            }
//        });
//        clearHistoryDialog.show();
    }

    private SobotCommonDialog clearHistoryMsgDialog;//清楚历史记录弹窗

    public void clearHistory() {
        if (clearHistoryMsgDialog == null) {
            clearHistoryMsgDialog = new SobotCommonDialog(getSobotActivity(), getResources().getString(R.string.sobot_clear_his_msg_describe), "", getResources().getString(R.string.sobot_clear_his_msg_empty), getResources().getString(R.string.sobot_btn_cancle), new SobotCommonDialog.MyClickListener() {
                @Override
                public void clickOk() {
                    zhiChiApi.deleteHisMsg(SobotChatFragment.this, initModel.getPartnerid(), new StringResultCallBack<CommonModelBase>() {
                        @Override
                        public void onSuccess(CommonModelBase modelBase) {
                            if (!isActive()) {
                                return;
                            }
                            messageList.clear();
                            cids.clear();
                            messageAdapter.notifyDataSetChanged();
                            srv_message.setEnableRefresh(true);// 设置下拉刷新列表
                        }

                        @Override
                        public void onFailure(Exception e, String des) {
                        }
                    });
                }

                @Override
                public void clickCancle() {
                }
            });
            clearHistoryMsgDialog.show();
        } else {
            clearHistoryMsgDialog.show();
        }
    }

    /**
     * 隐藏重新开始会话的菊花
     */
    public void hideReLoading() {
        image_reLoading.clearAnimation();
        image_reLoading.setVisibility(View.GONE);
    }

    /**
     * 仅人工的无客服在线的逻辑
     */
    private void showLeaveMsg() {
        LogUtils.i("仅人工，无客服在线");
//        showLogicTitle(getResources().getString(R.string.sobot_no_access), null, false);
        showLogicTitle("", null, false);
        setBottomView(ZhiChiConstant.bottomViewtype_custom_only_msgclose);
        mBottomViewtype = ZhiChiConstant.bottomViewtype_custom_only_msgclose;
        if (isUserBlack()) {
            showCustomerUanbleTip();
        } else {
            showCustomerOfflineTip();
        }
        isSessionOver = true;
    }

    /**
     * 输入表情的方法
     *
     * @param item
     */
    @Override
    public void inputEmoticon(EmojiconNew item) {
        InputHelper.input2OSC(et_sendmessage, item);
    }

    /**
     * 输入框删除的方法
     */
    @Override
    public void backspace() {
        InputHelper.backspace(et_sendmessage);
    }

    @Override
    public void setRobotOperatorCount(List<ChattingPanelMoreMenuView.SobotPlusEntity> robotMenuList) {
        int robotCount = 0;
        if (robotMenuList != null) {
            robotCount = robotMenuList.size();
        }
        if (current_client_model != ZhiChiConstant.client_model_customService) {
            if (robotCount == 0) {
                //隐藏加号底部按钮
                cb_plus_view.setVisibility(View.GONE);
            } else {
                cb_plus_view.setVisibility(View.VISIBLE);
            }
            mRobotOperatorCount = robotCount;
            if (mRobotPanleHeiht == 0) {
                mRobotPanleHeiht = getPanleHeight(robotMenuList);
            }
        }
    }

    @Override
    public void setOperatorCount(List<ChattingPanelMoreMenuView.SobotPlusEntity> operatorMenuList) {
        int operatorCount = 0;
        if (operatorMenuList != null) {
            operatorCount = operatorMenuList.size();
        }
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            if (operatorCount == 0) {
                //隐藏加号底部按钮
                cb_plus_view.setVisibility(View.GONE);
            } else {
                cb_plus_view.setVisibility(View.VISIBLE);
            }
        }
        mOperatorCount = operatorCount;
        if (mArtificialPanleHeiht == 0) {
            mArtificialPanleHeiht = getPanleHeight(operatorMenuList);
        }
    }

    //获取加号面板显示高度
    private int getPanleHeight(List<ChattingPanelMoreMenuView.SobotPlusEntity> meunList) {
        try {
            if (meunList == null && meunList.size() == 0) {
                return 0;
            }
            int validPanelHeight = KeyboardUtil.getValidPanelHeight(getSobotActivity());
            //竖向菜单文字高度，可能是两个菜单高度，也可能是2个
            int meunNameTotalHeiht = 0;
            //第一行菜单文字高度最大高度
            int firstMeunNameMaxHeiht = 0;
            //第二行菜单文字高度最大高度
            int secondMeunNameMaxHeiht = 0;
            TextPaint paint = new TextPaint();
            paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.sobot_text_font_12));
            // 设置字体大小
            //每个菜单的宽度
            int meunItemWidth = (ScreenUtils.getScreenWidth(getSobotActivity()) - ScreenUtils.dip2px(getSobotActivity(), 80)) / 3;
            if (meunList.size() >= 6) {
                //两行
                for (int i = 0; i < 3; i++) {
                    String nameStr = SobotStringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(nameStr, paint, meunItemWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 4f, false);
                    int height = layout.getHeight();
                    firstMeunNameMaxHeiht = Math.max(height, firstMeunNameMaxHeiht);
                }
                for (int i = 3; i < 6; i++) {
                    String nameStr = SobotStringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(nameStr, paint, meunItemWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 4f, false);
                    int height = layout.getHeight();
                    secondMeunNameMaxHeiht = Math.max(height, secondMeunNameMaxHeiht);
                }
            } else if (meunList.size() > 3) {
                //一页两行
                for (int i = 0; i < 3; i++) {
                    String text = SobotStringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 4f, false);
                    int height = layout.getHeight();
                    firstMeunNameMaxHeiht = Math.max(height, firstMeunNameMaxHeiht);
                }
                for (int i = 3; i < meunList.size(); i++) {
                    String text = SobotStringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 4f, false);
                    int height = layout.getHeight();
                    secondMeunNameMaxHeiht = Math.max(height, secondMeunNameMaxHeiht);
                }
            } else {
                //一行
                for (int i = 0; i < meunList.size(); i++) {
                    String text = SobotStringUtils.checkStringIsNull(meunList.get(i).name);
                    StaticLayout layout = new StaticLayout(text, paint, meunItemWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 4f, false);
                    int height = layout.getHeight();
                    firstMeunNameMaxHeiht = Math.max(height, firstMeunNameMaxHeiht);
                }
            }
            meunNameTotalHeiht = firstMeunNameMaxHeiht + secondMeunNameMaxHeiht;
            if (meunList.size() > 6) {
                //两页 高度=圆点指示器30+上下间距24*2+格子数据间距20+上下2个图标间距40*2+菜单文字计算出来的高度
                validPanelHeight = ScreenUtils.dip2px(getSobotActivity(), 30 + 24 * 2 + 20 + 40 * 2 + 8) + meunNameTotalHeiht;
            } else if (meunList.size() > 3) {
                //一页 高度=上下间距24*2+格子数据间距20+上下2个图标间距40*2+菜单文字计算出来的高度
                validPanelHeight = ScreenUtils.dip2px(getSobotActivity(), 24 * 2 + 20 + 40 * 2 + 8) + meunNameTotalHeiht;
            } else {
                //只有一行高度=上下间距24*2+1个图标间距30+菜单文字计算出来的高度
                validPanelHeight = ScreenUtils.dip2px(getSobotActivity(), 24 * 2 + 40 + 8) + meunNameTotalHeiht;
            }
            return validPanelHeight;
        } catch (Exception e) {
            return 0;
        }
    }

    //获取加号面板里边菜单的数量
    public int getAddPlanMemuCount() {
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            return mOperatorCount;
        } else {
            return mRobotOperatorCount;
        }
    }

    //获取加号面板高度
    public int getAddPlanMemuHeiht() {
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            return mArtificialPanleHeiht;
        } else {
            return mRobotPanleHeiht;
        }
    }

    /**
     * 提供给聊天面板执行的方法
     * 图库
     */
    @Override
    public void btnPicture() {
        hidePanelAndKeyboard(mPanelLayout);
        selectPicFromLocal();
        gotoLastItem();
    }

    /**
     * 提供给聊天面板执行的方法
     * 视频
     */
    @Override
    public void btnVedio() {
        hidePanelAndKeyboard(mPanelLayout);
        selectVedioFromLocal();
        gotoLastItem();
    }

    /**
     * 提供给聊天面板执行的方法
     * 照相
     */
    @Override
    public void btnCameraPicture() {
        hidePanelAndKeyboard(mPanelLayout);
        selectPicFromCamera(); // 拍照 上传
        gotoLastItem();
    }

    /**
     * 提供给聊天面板执行的方法
     * 满意度
     */
    @Override
    public void btnSatisfaction() {
        //满意度逻辑 点击时首先判断是否评价过 评价过 弹您已完成提示 未评价 判断是否达到可评价标准
        submitEvaluation(true, 5, -2, "");
        gotoLastItem();
    }

    /**
     * 提供给聊天面板执行的方法
     * 选择文件
     */
    @Override
    public void chooseFile() {
        hidePanelAndKeyboard(mPanelLayout);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE);
    }

    @Override
    public void openWeb(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (SobotOption.hyperlinkListener != null) {
            SobotOption.hyperlinkListener.onUrlClick(url);
            return;
        }

        if (SobotOption.newHyperlinkListener != null) {
            //如果返回true,拦截;false 不拦截
            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotActivity(), url);
            if (isIntercept) {
                return;
            }
        }
        Intent intent = new Intent(getSobotActivity(), WebViewActivity.class);
        intent.putExtra("url", url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * 打开留言页面
     *
     * @param flag_exit_sdk 当留言页面退出时 所需执行动作的标识
     */
    @Override
    public void startToPostMsgActivty(final boolean flag_exit_sdk) {
        if (getSobotActivity() != null && isAdded() && initModel != null) {

            if (SobotOption.sobotLeaveMsgListener != null) {
                SobotOption.sobotLeaveMsgListener.onLeaveMsg();
                return;
            }
            if (getSobotActivity() == null) {
                return;
            }
            hidePanelAndKeyboard();
            if (initModel.isMsgToTicketFlag()) {
                Intent intent = SobotPostLeaveMsgActivity.newIntent(getSobotActivity(), initModel.getMsgLeaveTxt()
                        , initModel.getMsgLeaveContentTxt(), initModel.getPartnerid());
                startActivityForResult(intent, SobotPostLeaveMsgActivity.EXTRA_MSG_LEAVE_REQUEST_CODE);
            } else {
                String tempGroupId = SharedPreferencesUtil.getStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, "");
                mPostMsgPresenter.obtainTemplateList(initModel.getPartnerid(), tempGroupId, flag_exit_sdk, new StPostMsgPresenter.ObtainTemplateListDelegate() {
                    @Override
                    public void onSuccess(Intent intent) {
                        intent.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, initModel.getCompanyId());
                        intent.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, initModel.getCustomerId());
                        intent.putExtra(ZhiChiConstant.FLAG_EXIT_SDK, flag_exit_sdk);
                        intent.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, info.getLeaveMsgGroupId());
                        startActivity(intent);
                        if (getSobotActivity() != null) {
                            getSobotActivity().overridePendingTransition(R.anim.sobot_push_left_in,
                                    R.anim.sobot_push_left_out);
                        }
                    }
                });
            }
        }
    }

    //切换键盘和面板的方法
    public void switchPanelAndKeyboard(final View panelLayout,
                                       final View switchPanelKeyboardBtn, final View focusView, boolean checked) {
        if (currentPanelId == 0 || currentPanelId == switchPanelKeyboardBtn.getId()) {
            //没选中的时候或者  点击是自身的时候正常切换面板和键盘
//            boolean switchToPanel = panelLayout.getVisibility() != View.VISIBLE;
            if (!checked) {
                KPSwitchConflictUtil.showKeyboard(panelLayout, focusView);
            } else {
                KPSwitchConflictUtil.showPanel(panelLayout);
                setPanelView(panelLayout, switchPanelKeyboardBtn.getId());
            }
        } else {
            //之前选过  但是现在点击的不是自己的时候  显示自己的面板
            KPSwitchConflictUtil.showPanel(panelLayout);
            setPanelView(panelLayout, switchPanelKeyboardBtn.getId());
        }
        currentPanelId = switchPanelKeyboardBtn.getId();
    }

    /*
     * 切换键盘和面板的方法   考虑了当键盘为按住说话时的情况 一般都用这个就行
     * 参数是按下的那个按钮
     */
    public void pressSpeakSwitchPanelAndKeyboard(final View switchPanelKeyboardBtn, boolean checked) {
        if (btn_press_to_speak.isShown()) {
            btn_model_edit.setVisibility(View.GONE);
            showVoiceBtn();
            btn_press_to_speak.setVisibility(View.GONE);
            edittext_layout.setVisibility(View.VISIBLE);

            et_sendmessage.requestFocus();
            KPSwitchConflictUtil.showPanel(mPanelLayout);
            setPanelView(mPanelLayout, switchPanelKeyboardBtn.getId());
            currentPanelId = switchPanelKeyboardBtn.getId();
        } else {
            //切换更多方法的面板
            switchPanelAndKeyboard(mPanelLayout, switchPanelKeyboardBtn, et_sendmessage, checked);
        }
    }

    /**
     * 设置聊天面板的view
     *
     * @param panelLayout
     * @param btnId
     */
    private void setPanelView(final View panelLayout, int btnId) {
        if (panelLayout == null) {
            return;
        }
        if (panelLayout instanceof KPSwitchPanelLinearLayout) {
            KPSwitchPanelLinearLayout tmpView = (KPSwitchPanelLinearLayout) panelLayout;
            View childView = tmpView.getChildAt(0);
            if (childView != null && childView instanceof CustomeChattingPanel) {
                CustomeChattingPanel customeChattingPanel = (CustomeChattingPanel) childView;
                Bundle bundle = new Bundle();
                bundle.putInt("current_client_model", current_client_model);
                customeChattingPanel.setupView(btnId, bundle, SobotChatFragment.this, SobotChatFragment.this);
            }
        }
    }

    /**
     * 获取当前显示的聊天面板的tag
     *
     * @param panelLayout
     */
    private String getPanelViewTag(final View panelLayout) {
        String str = "";
        if (panelLayout instanceof KPSwitchPanelLinearLayout) {
            KPSwitchPanelLinearLayout tmpView = (KPSwitchPanelLinearLayout) panelLayout;
            View childView = tmpView.getChildAt(0);
            if (childView != null && childView instanceof CustomeChattingPanel) {
                CustomeChattingPanel customeChattingPanel = (CustomeChattingPanel) childView;
                str = customeChattingPanel.getPanelViewTag();
            }
        }
        return str;
    }


    /*
     * 弹出提示
     */
    private void showHint(String content) {
        ZhiChiMessageBase zhichiMessageBase = new ZhiChiMessageBase();
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        zhichiMessageBase.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        reply.setMsg(content);
        reply.setRemindType(ZhiChiConstant.sobot_remind_type_tip);
        zhichiMessageBase.setAnswer(reply);
        zhichiMessageBase.setAction(ZhiChiConstant.action_remind_no_service);
        updateUiMessage(messageAdapter, zhichiMessageBase);
    }

    @Override
    public void onRobotGuessComplete(String question) {
        //分词联想 选中事件
        et_sendmessage.setText("");
        //大模型调用非流式
        String msgId = getMsgId();
        int questionFlag = 0;
        if (initModel != null && initModel.isAiAgent()) {
            questionFlag = 1;
            //调用非流式
        }
        sendTextMessageToHandler(msgId, question, handler, 2, SEND_TEXT);
        LogUtils.i("当前发送消息模式：" + current_client_model);
        setTimeTaskMethod(handler);
        sendMessageWithLogic(msgId, question, initModel, handler, current_client_model, questionFlag, "", null);
    }


    /**
     * 获取聊天记录
     *
     * @param isFirst 第一次查询历史记录
     */
    public void getHistoryMessage(final boolean isFirst) {
        if (initModel == null)
            return;

        if (queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_INITIAL || queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_FAILURE) {
            //cid列表接口未调用或获取失败的时候重新获取cid
            onLoad();
            queryCids();
        } else if ((queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_LOADING && !isFirst) || isInGethistory) {
            //1.查询cid接口调用中 又不是第一次查询历史记录  那么 直接什么也不做就返回
            //2.如果查询历史记录的接口正在跑   那么什么也不做
            onLoad();
        } else {
            String currentCid = ChatUtils.getCurrentCid(initModel, cids, currentCidPosition);
            if ("-1".equals(currentCid)) {
                showNoHistory();
                onLoad();
                return;
            }
            isInGethistory = true;
            zhiChiApi.getChatDetailByCid(SobotChatFragment.this, initModel.getPartnerid(), currentCid, new StringResultCallBack<ZhiChiHistoryMessage>() {
                @Override
                public void onSuccess(ZhiChiHistoryMessage zhiChiHistoryMessage) {
                    isInGethistory = false;
                    if (!isActive()) {
                        return;
                    }
                    onLoad();
                    currentCidPosition++;
                    List<ZhiChiHistoryMessageBase> data = zhiChiHistoryMessage.getData();
                    if (data != null && !data.isEmpty()) {
                        showData(data, isFirst);
                    } else {
                        //没有数据的时候继续拉
                        getHistoryMessage(isFirst);
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                    isInGethistory = false;
                    if (!isActive()) {
                        return;
                    }
                    mUnreadNum = 0;
                    onLoad();
                }
            });
        }
    }

    private void showData(List<ZhiChiHistoryMessageBase> result, boolean isFirst) {
        final List<ZhiChiMessageBase> msgLists = new ArrayList<>();
        List<ZhiChiMessageBase> msgList;
        for (int i = 0; i < result.size(); i++) {
            ZhiChiHistoryMessageBase historyMsg = result.get(i);
            msgList = historyMsg.getContent();
            for (ZhiChiMessageBase base : msgList) {
                base.setSugguestionsFontColor(1);
                if ((ZhiChiConstant.message_type_fraud_prevention + "").equals(base.getAction())) {
                } else if ((ZhiChiConstant.action_mulit_postmsg_tip_can_click).equals(base.getAction())) {
                    base.setSenderType(ZhiChiConstant.message_sender_type_system);
                } else if ((ZhiChiConstant.action_mulit_postmsg_tip_nocan_click).equals(base.getAction())) {
                    base.setSenderType(ZhiChiConstant.message_sender_type_system);
                } else if ((ZhiChiConstant.action_card_mind_msg).equals(base.getAction())) {
                } else {
                    if (base.getSdkMsg() != null) {
                        ZhiChiReplyAnswer answer = base.getSdkMsg().getAnswer();
                        if (answer != null) {

                            if (!TextUtils.isEmpty(answer.getMsg()) && answer.getMsg().length() > 4) {
                                String msg = answer.getMsg().replace("&lt;/p&gt;", "<br>");
                                if (msg.endsWith("<br>")) {
                                    msg = msg.substring(0, msg.length() - 4);
                                }
                                answer.setMsg(msg);
                            }
                        }
                        if (-1 == base.getSenderType()) {
                            base = null;
                            continue;
                        }
                        if (ZhiChiConstant.message_sender_type_robot == base.getSenderType()) {
                            base.setSenderName(TextUtils.isEmpty(base.getSenderName()) ? initModel
                                    .getRobotName() : base.getSenderName());
                            base.setSenderFace(TextUtils.isEmpty(base.getSenderFace()) ? initModel
                                    .getRobotLogo() : base.getSenderFace());
                        }
                        base.setAnswer(answer);
                        base.setAnswerType(base.getSdkMsg()
                                .getAnswerType());
                    }
                }
                if ((ZhiChiConstant.action_cai_msg).equals(base.getAction())) {
                    base.setSenderName(initModel.getRobotName());
                    base.setSender(initModel.getRobotName());
                    base.setSenderFace(initModel.getRobotLogo());
                    base.setSenderType(ZhiChiConstant.message_sender_type_system);
                    //点踩判断
                    if (base.getCid().equals(initModel.getCid())) {
                        if (base.getSubmitStatus() == 1) {
                            //显示问题
                            msgLists.add(base);
                        } else if (base.getSubmitStatus() == 2 && !TextUtils.isEmpty(base.getMsg())) {
                            msgLists.add(base);
                        }
                    } else if (base.getSubmitStatus() == 2 && !TextUtils.isEmpty(base.getMsg())) {
                        //显示已提交的点踩
                        msgLists.add(base);
                    }
                } else {
                    msgLists.add(base);
                }
            }
        }

        if (msgLists.size() > 0) {
            if (mUnreadNum > 0) {
                ZhiChiMessageBase unreadMsg = ChatUtils.getUnreadMode(getSobotActivity());
                unreadMsg.setCid(msgLists.get(msgLists.size() - 1).getCid());
                msgLists.add((msgLists.size() - mUnreadNum) < 0 ? 0 : (msgLists.size() - mUnreadNum)
                        , unreadMsg);
                checkUnReadMsg();
            }
            messageAdapter.addData(msgLists);
            if (isFirst) {
                gotoLastItem();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                       if (messageAdapter != null && SobotStringUtils.isNoEmpty(robotWelcomeMsgId)) {
//                           int welcomePosition = messageAdapter.getMsgPositionInfoByMsgId(robotWelcomeMsgId);
//                           if (welcomePosition != -1) {
//                               //第一次加载历史滚动到最新的位置
//                               gotoIndexItem(welcomePosition);
//                           } else {
//                               gotoLastItem();
//                           }
//                       } else {
                        gotoLastItem();
//                       }
                    }
                }, 800);
            }
        }
    }

    /**
     * 显示没有更多历史记录
     */
    private void showNoHistory() {
        gotoIndexItem(0);
        srv_message.setEnableRefresh(false);// 设置下拉刷新列表
        isNoMoreHistoryMsg = true;
        mUnreadNum = 0;
    }

    private void onLoad() {
        srv_message.finishRefresh();
    }

    // 键盘编辑模式转换为语音模式
    private void editModelToVoice(int typeModel, String str) {
        btn_model_edit.setVisibility(View.GONE == typeModel ? View.GONE
                : View.VISIBLE); // 键盘编辑隐藏
        btn_model_voice.setVisibility(View.VISIBLE != typeModel ? View.VISIBLE
                : View.GONE);// 语音模式开启
        btn_press_to_speak.setVisibility(View.GONE != typeModel ? View.VISIBLE
                : View.GONE);
        edittext_layout.setVisibility(View.VISIBLE == typeModel ? View.GONE
                : View.VISIBLE);

        if (!TextUtils.isEmpty(et_sendmessage.getText().toString()) && str.equals("123")) {
            btn_send_pic.setVisibility(View.VISIBLE);
            cb_plus_view.setVisibility(View.GONE);
        } else {
            btn_send_pic.setVisibility(View.GONE);
            if (getAddPlanMemuCount() > 0) {
                cb_plus_view.setVisibility(View.VISIBLE);
            } else {
                cb_plus_view.setVisibility(View.GONE);
            }
        }
    }

    public void setShowNetRemind(boolean isShow) {
        net_status_remide.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    /**
     * 广播接受者：
     */
    public class MyMessageReceiver extends BroadcastReceiver {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!CommonUtils.isNetWorkConnected(mAppContext)) {
                    //取消上传文件
                    HttpUtils.getInstance().cancelTag(ZhiChiConstant.SOBOT_GLOBAL_REQUEST_CANCEL_TAG);
                    //没有网络
                    if (welcome.getVisibility() != View.VISIBLE) {
                        setShowNetRemind(true);
                    }
                } else {
                    // 有网络
                    setShowNetRemind(false);
                }
            }
        }
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LogUtils.i("广播是  :" + intent.getAction());
                if (ZhiChiConstants.receiveMessageBrocast.equals(intent.getAction())) {
                    // 接受下推的消息
                    ZhiChiPushMessage pushMessage = null;
                    try {
                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            pushMessage = (ZhiChiPushMessage) extras.getSerializable(ZhiChiConstants.ZHICHI_PUSH_MESSAGE);
                            LogUtils.i("广播对象是  :" + pushMessage.toString());
                        }
                    } catch (Exception e) {
                        //ignor
                    }
                    if (pushMessage == null || !info.getApp_key().equals(pushMessage.getAppId())) {
                        return;
                    }
                    ZhiChiMessageBase base = new ZhiChiMessageBase();

                    //接收到系统消息，直接刷新数据
                    if (ZhiChiConstant.push_message_receverSystemMessage == pushMessage
                            .getType()) {// 接收系统消息
                        base.setT(Calendar.getInstance().getTime().getTime() + "");
                        base.setMsgId(pushMessage.getMsgId());
                        //两种超时 如果消息中没有头像和昵称 赋值转人工的客服的头像和昵称
                        if ("1".equals(pushMessage.getSysType()) || "2".equals(pushMessage.getSysType())) {
                            if (StringUtils.isEmpty(pushMessage.getAface())) {
                                base.setSenderFace(getAdminFace());
                            } else {
                                base.setSenderFace(pushMessage.getAface());
                            }
                            if (StringUtils.isEmpty(pushMessage.getAname())) {
                                base.setSenderName(getAdminName());
                                base.setSender(getAdminName());
                            } else {
                                base.setSenderName(pushMessage.getAname());
                                base.setSender(pushMessage.getAname());
                            }
                        }
                        if (!TextUtils.isEmpty(pushMessage.getSysType()) && ("1".equals(pushMessage.getSysType()) || "2".equals(pushMessage.getSysType()) || "5".equals(pushMessage.getSysType()))) {
                            //客服超时提示 1
                            //客户超时提示 2 都显示在左侧
                            //排队断开说辞系统消息 5 都显示在左侧
                            base.setSenderType(ZhiChiConstant.message_sender_type_service);
                            ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                            reply.setMsg(pushMessage.getContent());
                            reply.setMsgType(ZhiChiConstant.message_type_text);
                            base.setAnswer(reply);
                        } else {
                            base.setAction(ZhiChiConstant.message_type_fraud_prevention + "");
                            base.setMsgId(pushMessage.getMsgId());
                            base.setTempMsg(pushMessage.getContent());
                            stopCustomTimeTask();
                            startUserInfoTimeTask(handler);
                        }
                        //如果开启已读未读，设置未读，显示的时候在设置已读
                        LogUtils.d("======收到消息=======" + (!TextUtils.isEmpty(pushMessage.getMsgId())) + "======" + (pushMessage.getReadStatus() == 1));
                        if (!TextUtils.isEmpty(pushMessage.getMsgId()) && pushMessage.getReadStatus() == 1) {
                            base.setReadStatus(1);
                            unReadMsgIds.put(pushMessage.getMsgId(), base);
                        }
                        // 更新界面的操作
                        messageAdapter.justAddData(base);
                        ChatUtils.msgLogicalProcess(getSobotActivity(), initModel, messageAdapter, pushMessage);
                        messageAdapter.notifyDataSetChanged();
                        gotoLastItem();
                        return;
                    }

                    base.setT(Calendar.getInstance().getTime().getTime() + "");
                    base.setMsgId(pushMessage.getMsgId());
                    base.setSender(pushMessage.getAname());
                    base.setSenderName(pushMessage.getAname());
                    base.setSenderFace(pushMessage.getAface());
                    base.setOrderCardContent(pushMessage.getOrderCardContent());
                    base.setMiniProgramModel(pushMessage.getMiniProgramModel());
                    base.setCustomCard(pushMessage.getCustomCard());
                    base.setArticleModel(pushMessage.getArticleModel());
                    base.setConsultingContent(pushMessage.getConsultingContent());
                    base.setAppointMessage(pushMessage.getAppointMessage());
                    base.setSenderType(ZhiChiConstant.message_sender_type_service);
                    base.setAnswer(pushMessage.getAnswer());
                    base.setMessage(pushMessage.getMessage());

                    if (ZhiChiConstant.push_message_createChat == pushMessage.getType()) {
                        setAdminFace(pushMessage.getAface());
                        setAdminName(pushMessage.getAname());
                        if (initModel != null) {
                            initModel.setAdminHelloWord(!TextUtils.isEmpty(pushMessage.getAdminHelloWord()) ? pushMessage.getAdminHelloWord() : initModel.getAdminHelloWord());
                            initModel.setServiceEndPushMsg(!TextUtils.isEmpty(pushMessage.getServiceEndPushMsg()) ? pushMessage.getServiceEndPushMsg() : initModel.getServiceEndPushMsg());
                        }
                        createCustomerService(pushMessage.getAname(), pushMessage.getAface());
                    } else if (ZhiChiConstant.push_message_paidui == pushMessage.getType()) {
                        // 排队的消息类型
                        createCustomerQueue(pushMessage.getCount(), 0, pushMessage.getQueueDoc(), isShowQueueTip);
                    } else if (ZhiChiConstant.push_message_receverNewMessage == pushMessage.getType()) {
                        LogUtils.i2Local("收到消息4", "ChatFragment接受到新消息 msgId: " + pushMessage.getMsgId());
                        base.setMsgId(pushMessage.getMsgId());
                        base.setSender(pushMessage.getAname());
                        base.setSenderName(pushMessage.getAname());
                        base.setSenderFace(pushMessage.getAface());
                        base.setSenderType(ZhiChiConstant.message_sender_type_service);
                        base.setAnswer(pushMessage.getAnswer());
                        stopCustomTimeTask();
                        startUserInfoTimeTask(handler);
                        // 接收到新的消息
                        LogUtils.d("======收到消息=======" + (!TextUtils.isEmpty(pushMessage.getMsgId())) + "======" + (pushMessage.getReadStatus() == 1));
                        if (!TextUtils.isEmpty(pushMessage.getMsgId()) && pushMessage.getReadStatus() == 1) {
                            base.setReadStatus(1);
                            unReadMsgIds.put(pushMessage.getMsgId(), base);
                        }
                        messageAdapter.justAddData(base);
                        ChatUtils.msgLogicalProcess(getSobotActivity(), initModel, messageAdapter, pushMessage);
                        if (showNewMsg) {
                            //显示新消息
                            newMsgNum++;
                            handler.sendEmptyMessage(ZhiChiConstant.hander_show_newmsg_tip);
                        } else {
                            messageAdapter.notifyDataSetChanged();
                            gotoLastItem();
                            handler.sendEmptyMessage(ZhiChiConstant.hander_hide_newmsg_tip);

                        }
                        //修改客服状态为在线
                        customerState = CustomerState.Online;
                    } else if (ZhiChiConstant.push_message_outLine == pushMessage.getType()) {
                        if (6 == Integer.parseInt(pushMessage.getStatus())) {
                            // 打开新窗口 单独处理
                            String puid = SharedPreferencesUtil.getStringData(getSobotActivity(), Const.SOBOT_PUID, "");
                            if (!TextUtils.isEmpty(puid) && !TextUtils.isEmpty(pushMessage.getPuid()) && puid.equals(pushMessage.getPuid())) {
                                customerServiceOffline(initModel, Integer.parseInt(pushMessage.getStatus()));
                            }
                        } else {
                            // 用户被下线
                            customerServiceOffline(initModel, Integer.parseInt(pushMessage.getStatus()));
                            if (initModel.getCommentFlag() == 1) {
                                if (isAboveZero && !isComment) {
                                    // 满足评价条件，并且之前没有评价过的话 才能 弹评价框
                                    pushMessage.setIsQuestionFlag(1);
                                    //如果没有评价配置，请求评价配置，如果请求失败，再去请求一次
                                    if (mSatisfactionSet == null) {
                                        requestEvaluateConfig(true, pushMessage);
                                    } else {
                                        // 满足评价条件，并且之前没有评价过的话 才能 弹评价框
                                        ZhiChiMessageBase customEvaluateMode = ChatUtils.getCustomEvaluateMode(getSobotActivity(), pushMessage, mSatisfactionSet);
                                        // 更新界面的操作
                                        updateUiMessage(messageAdapter, customEvaluateMode);
                                    }
                                }
                            }
                        }
                    } else if (ZhiChiConstant.push_message_transfer == pushMessage.getType()) {
                        LogUtils.i("用户被转接--->" + pushMessage.getName());
                        //替换标题 转接后客服头像取face 和name
                        showLogicTitle(pushMessage.getName(), pushMessage.getFace(), false);
                        setAdminFace(pushMessage.getFace());
                        setAdminName(pushMessage.getAname());
                        currentUserName = pushMessage.getName();
                    } else if (ZhiChiConstant.push_message_user_get_session_lock_msg == pushMessage.getType()) {
                        if (customerState == CustomerState.Online) {
                            //1 会话锁定
                            if (1 == pushMessage.getLockType()) {
                                paseReplyTimeCustoms = noReplyTimeCustoms;
                                paseReplyTimeUserInfo = noReplyTimeUserInfo;
                                isChatLock = 1;
                                if (is_startCustomTimerTask) {
                                    LogUtils.i("客服定时任务 锁定--->" + noReplyTimeCustoms);
                                    stopCustomTimeTask();
                                    is_startCustomTimerTask = true;
                                    //如果会话锁定，客服计时器暂停计时,计时不归0；
                                    noReplyTimeCustoms = paseReplyTimeCustoms;
                                    customTimeTask = true;
                                } else {
                                    LogUtils.i("用户定时任务 锁定--->" + noReplyTimeUserInfo);
                                    stopUserInfoTimeTask();
                                    noReplyTimeUserInfo = paseReplyTimeUserInfo;
                                    userInfoTimeTask = true;
                                }
                            } else {
                                isChatLock = 2;
                                //2 会话解锁
                                if (current_client_model == ZhiChiConstant.client_model_customService) {
                                    if (is_startCustomTimerTask) {
                                        stopCustomTimeTask();
                                        startCustomTimeTask(handler);
                                        //如果会话锁定，客服计时器暂停计时,计时不归0；
                                        noReplyTimeCustoms = paseReplyTimeCustoms;
                                        customTimeTask = true;
                                        LogUtils.i("客服定时任务 解锁--->" + noReplyTimeCustoms);
                                    } else {
                                        stopUserInfoTimeTask();
                                        startUserInfoTimeTask(handler);
                                        userInfoTimeTask = true;
                                        noReplyTimeUserInfo = paseReplyTimeUserInfo;
                                        LogUtils.i("用户定时任务 解锁--->" + noReplyTimeUserInfo);
                                    }
                                }
                            }
                        }
                    } else if (ZhiChiConstant.push_message_custom_evaluate == pushMessage.getType()) {
                        LogUtils.i("客服推送满意度评价.................");
                        //显示推送消息体
                        if (isAboveZero && customerState == CustomerState.Online) {
                            //如果没有评价配置，请求评价配置，如果请求失败，再去请求一次
                            if (mSatisfactionSet == null) {
                                requestEvaluateConfig(true, pushMessage);
                            } else {
                                // 满足评价条件，并且之前没有评价过的话 才能 弹评价框
                                ZhiChiMessageBase customEvaluateMode = ChatUtils.getCustomEvaluateMode(getSobotActivity(), pushMessage, mSatisfactionSet);
                                // 更新界面的操作
                                updateUiMessage(messageAdapter, customEvaluateMode);
                                gotoLastItem();
                            }
                        }
                    } else if (ZhiChiConstant.push_message_retracted == pushMessage.getType()) {
                        if (!TextUtils.isEmpty(pushMessage.getRevokeMsgId())) {
                            List<ZhiChiMessageBase> datas = messageAdapter.getDatas();
                            for (int i = datas.size() - 1; i >= 0; i--) {
                                ZhiChiMessageBase msgData = datas.get(i);
                                if (pushMessage.getRevokeMsgId().equals(msgData.getMsgId())) {
                                    if (!msgData.isRetractedMsg()) {
                                        msgData.setRetractedMsg(true);
                                        messageAdapter.notifyDataSetChanged();
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (ZhiChiConstant.push_message_mark_read_msg == pushMessage.getType()) {
                        List<String> list = pushMessage.getMsgIdList();
                        if (list != null && list.size() > 0) {
                            messageAdapter.updateReadStatus(list);
                        }
                    }
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_LOCATION.equals(intent.getAction())) {
                    SobotLocationModel locationData = (SobotLocationModel) intent.getSerializableExtra(ZhiChiConstant.SOBOT_LOCATION_DATA);
                    if (locationData != null) {
                        sendLocation(null, locationData, handler, true);
                    }
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_TEXT.equals(intent.getAction())) {
                    String content = intent.getStringExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    String sendTextTo = intent.getStringExtra("sendTextTo");
                    if (ZhiChiConstant.client_model_robot == current_client_model && "robot".equals(sendTextTo)) { // 客户和机械人进行聊天
                        if (!TextUtils.isEmpty(content)) {
                            sendMsg(content);
                        }
                    } else if (ZhiChiConstant.client_model_customService == current_client_model && "user".equals(sendTextTo)) {
                        if (!TextUtils.isEmpty(content)) {
                            sendMsg(content);
                        }
                    }
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_OBJECT.equals(intent.getAction())) {
                    String content = intent.getStringExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    String type = intent.getStringExtra(ZhiChiConstant.SOBOT_TYPE_DATA);
                    if (ZhiChiConstant.client_model_customService == current_client_model) {
                        if (TextUtils.isEmpty(content)) {
                            LogUtils.i("发送内容不能为空");
                            return;
                        }
                        if ("0".equals(type)) {
                            //发送文本
                            sendMsg(content);
                        } else if ("1".equals(type)) {
                            //发送图片
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadFile(sendFile, handler, messageAdapter, false);
                            }
                        } else if ("3".equals(type)) {
                            //发送视频
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadVideo(sendFile, null, messageAdapter);
                            }
                        } else if ("4".equals(type)) {
                            //发送文件
                            File sendFile = new File(content);
                            if (sendFile.exists()) {
                                uploadFile(sendFile, handler, messageAdapter, false);
                            }
                        }
                    }
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_TRASNFER_TO_OPERATOR.equals(intent.getAction())) {
                    //外部调用转人工
                    SobotTransferOperatorParam transferParam = (SobotTransferOperatorParam) intent.getSerializableExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    if (transferParam != null) {
                        if (transferParam.getConsultingContent() != null) {
                            info.setConsultingContent(transferParam.getConsultingContent());
                        }
                        if (transferParam.getSummary_params() != null) {
                            info.setSummary_params(transferParam.getSummary_params());
                        }
                        SobotConnCusParam param = new SobotConnCusParam();
                        param.setGroupId(transferParam.getGroupId());
                        param.setGroupName(transferParam.getGroupName());
                        param.setKeyword(transferParam.getKeyword());
                        param.setKeywordId(transferParam.getKeywordId());
                        connectCustomerService(param, transferParam.isShowTips());
                    }
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_CARD.equals(intent.getAction())) {
                    ConsultingContent consultingContent = (ConsultingContent) intent.getSerializableExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    sendCardMsg(consultingContent);
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_ORDER_CARD.equals(intent.getAction())) {
                    OrderCardContentModel orderCardContent = (OrderCardContentModel) intent.getSerializableExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    sendOrderCardMsg(orderCardContent);
                } else if (ZhiChiConstant.SOBOT_BROCAST_ACTION_SEND_CUSTOM_CARD.equals(intent.getAction())) {
                    SobotChatCustomCard customCard = (SobotChatCustomCard) intent.getSerializableExtra(ZhiChiConstant.SOBOT_SEND_DATA);
                    createCustomCardContent(handler, customCard);
                }

                if (ZhiChiConstants.chat_remind_post_msg.equals(intent.getAction())) {
                    startToPostMsgActivty(false);
                } else if (ZhiChiConstants.chat_remind_ticket_list.equals(intent.getAction())) {
                    for (int i = messageList.size() - 1; i > 0; i--) {
                        if (messageList.get(i).getSenderType() == ZhiChiConstant.message_sender_type_remide_info
                                && messageList.get(i).getAnswer() != null
                                && ZhiChiConstant.sobot_remind_type_simple_tip == messageList.get(i).getAnswer().getRemindType()) {
                            messageList.remove(i);
                            messageAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    Intent intent2 = new Intent(getSobotActivity(), SobotTicketListActivity.class);
                    intent2.putExtra(StPostMsgPresenter.INTENT_KEY_UID, initModel.getPartnerid());
                    intent2.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, initModel.getCompanyId());
                    intent2.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, initModel.getCustomerId());
                    intent2.putExtra(ZhiChiConstant.FLAG_EXIT_SDK, false);
                    intent2.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, info.getLeaveMsgGroupId());
                    startActivity(intent2);
                    if (getSobotActivity() != null) {
                        getSobotActivity().overridePendingTransition(R.anim.sobot_push_left_in,
                                R.anim.sobot_push_left_out);
                    }

                } else if (ZhiChiConstants.sobot_click_cancle.equals(intent.getAction())) {
                    //打开技能组后点击了取消
                    if (type == ZhiChiConstant.type_custom_first && current_client_model ==
                            ZhiChiConstant.client_model_robot) {
                        remindRobotMessage(handler, initModel, info);
                    }
                } else if (ZhiChiConstants.chat_remind_to_customer.equals(intent.getAction())) {
                    //转人工
                    hidePanelAndKeyboard(mPanelLayout);
                    doEmoticonBtn2Blur();
                    transfer2Custom(0, null, null, null, null, true, 10, "", "", "1", "", "");
                } else if (ZhiChiConstants.SOBOT_POST_MSG_TMP_BROCAST.equals(intent.getAction())) {
                    //选完留言模版后跳转到留言界面
                    Intent postMsgIntent = new Intent(getSobotActivity(), SobotPostMsgActivity.class);
                    postMsgIntent.putExtra("intent_key_uid", intent.getStringExtra("uid"));
                    postMsgIntent.putExtra("intent_key_config", intent.getSerializableExtra("sobotLeaveMsgConfig"));
                    postMsgIntent.putExtra(StPostMsgPresenter.INTENT_KEY_COMPANYID, initModel.getCompanyId());
                    postMsgIntent.putExtra(StPostMsgPresenter.INTENT_KEY_CUSTOMERID, initModel.getCustomerId());
                    postMsgIntent.putExtra(ZhiChiConstant.FLAG_EXIT_SDK, intent.getBooleanExtra("mflag_exit_sdk", false));
                    postMsgIntent.putExtra(StPostMsgPresenter.INTENT_KEY_GROUPID, info.getLeaveMsgGroupId());
                    startActivity(postMsgIntent);
                    if (getSobotActivity() != null) {
                        getSobotActivity().overridePendingTransition(ResourceUtils.getIdByName(mAppContext, "anim", "sobot_push_left_in"),
                                ResourceUtils.getIdByName(mAppContext, "anim", "sobot_push_left_out"));
                    }
                } else if (ZhiChiConstants.SOBOT_POST_MSG_APPOINT_BROCAST.equals(intent.getAction())) {
                    //消息引用
                    appointMessage = (ZhiChiAppointMessage) intent.getSerializableExtra("appointMessage");
                    if (appointMessage != null && !StringUtils.isEmpty(appointMessage.getContent())) {
                        if (ll_appoint != null && tv_appoint_temp_content != null) {
                            if (et_sendmessage != null) {
                                changeBottomEditUI();
                                et_sendmessage.requestFocus();
                                KeyboardUtil.showKeyboard(et_sendmessage);
                            }
                            ll_appoint.setVisibility(View.VISIBLE);
                            String tempStr = "";
                            if (appointMessage.getAppointType() == 0) {
                                tempStr = getResources().getString(R.string.sobot_cus_service) + "：";
                            } else if (appointMessage.getAppointType() == 1) {
                                tempStr = getResources().getString(R.string.sobot_str_my) + "：";
                            } else if (appointMessage.getAppointType() == 2) {
                                tempStr = getResources().getString(R.string.sobot_cus_service) + "：";
                            }
                            // msgType：文本,图片,音频,视频,文件,对象
                            // msgType：0,1,2,3,4,5
                            if (appointMessage.getMsgType() == 1) {
                                tempStr = tempStr + "[" + getResources().getString(R.string.sobot_upload) + "]";
                            } else if (appointMessage.getMsgType() == 2) {
                                tempStr = tempStr + "[" + getResources().getString(R.string.sobot_chat_type_voice) + "]";
                            } else if (appointMessage.getMsgType() == 3) {
                                tempStr = tempStr + "[" + getResources().getString(R.string.sobot_upload_video) + "]";
                            } else if (appointMessage.getMsgType() == 4) {
                                JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                                if (contentJsonObject.has("fileName") && !StringUtils.isEmpty(contentJsonObject.optString("fileName"))) {
                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_choose_file) + "]" + contentJsonObject.optString("fileName");
                                } else {
                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_choose_file) + "]";
                                }
                            } else if (appointMessage.getMsgType() == 5) {
                                JSONObject contentJsonObject = new JSONObject(appointMessage.getContent());
                                if (contentJsonObject.has("type") && !StringUtils.isEmpty(contentJsonObject.optString("type"))) {
                                    if ("0".equals(contentJsonObject.optString("type"))) {
                                        //富文本类型
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            JSONObject msgJsonObject = new JSONObject(contentJsonObject.optString("msg"));
                                            if (msgJsonObject.has("richList") && !StringUtils.isEmpty(msgJsonObject.optString("richList"))) {
                                                JSONArray data = msgJsonObject.getJSONArray("richList");
                                                if (data != null) {
                                                    List<ChatMessageRichListModel> list = new ArrayList<>();
                                                    for (int i = 0; i < data.length(); i++) {
                                                        JSONObject obj = data.getJSONObject(i);
                                                        if (obj != null) {
                                                            //0：文本，1：图片，2：音频，3：视频，4：文件
                                                            if (obj.has("type") && obj.has("msg")) {
                                                                int type = obj.optInt("type");
                                                                if (type == 0) {
                                                                    tempStr = tempStr + GsonUtil.filterNull(obj.optString("msg"));
                                                                } else if (type == 1) {
                                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_upload) + "]";
                                                                } else if (type == 2) {
                                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_chat_type_voice) + "]";
                                                                } else if (type == 3) {
                                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_upload_video) + "]";
                                                                } else if (type == 4) {
                                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_choose_file) + "]";
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if ("2".equals(contentJsonObject.optString("type"))) {
                                        //位置
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            JSONObject jsonObject = new JSONObject(contentJsonObject.optString("msg"));
                                            if (jsonObject.has("title")) {
                                                tempStr = tempStr + "[" + getResources().getString(R.string.sobot_location) + "]" + jsonObject.optString("title");
                                            }
                                        }
                                    } else if ("3".equals(contentJsonObject.optString("type"))) {
                                        //商品卡片
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            JSONObject cardJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                            if (cardJsonObj.has("title")) {
                                                tempStr = tempStr + "[" + getResources().getString(R.string.sobot_chat_type_goods) + "]" + cardJsonObj.optString("title");
                                            }
                                        }
                                    } else if ("6".equals(contentJsonObject.optString("type"))) {
                                        //小程序卡片
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            try {
                                                JSONObject miniJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                                if (miniJsonObj.has("title")) {
                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_mini_program) + "]" + miniJsonObj.optString("title");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else if ("17".equals(contentJsonObject.optString("type"))) {
                                        //文章卡片
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            try {
                                                JSONObject miniJsonObj = new JSONObject(contentJsonObject.optString("msg"));
                                                if (miniJsonObj.has("title")) {
                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_str_article) + "]" + miniJsonObj.optString("title");
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else if ("21".equals(contentJsonObject.optString("type"))) {
                                        //自定义卡片类型
                                        if (contentJsonObject.has("msg") && !StringUtils.isEmpty(contentJsonObject.optString("msg"))) {
                                            try {
                                                SobotChatCustomCard model = SobotGsonUtil.jsonToBeans(contentJsonObject.optString("msg"),
                                                        new TypeToken<SobotChatCustomCard>() {
                                                        }.getType());
                                                if (model != null && model.getCustomCards() != null && model.getCardType() == 1 && model.getCustomCards().size() == 1) {
                                                    tempStr = tempStr + "[" + getResources().getString(R.string.sobot_chat_type_goods) + "]" + (StringUtils.isEmpty(model.getCustomCards().get(0).getCustomCardName()) ? "" : model.getCustomCards().get(0).getCustomCardName());
                                                }
                                            } catch (JsonSyntaxException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                            } else {
                                tempStr = tempStr + appointMessage.getContent();
                            }
                            tv_appoint_temp_content.setText(StringUtils.stripHtml(tempStr));
                        }
                    }
                } else if (ZhiChiConstants.dcrc_comment_state.equals(intent.getAction())) {
                    //评价完客户后所需执行的逻辑
                    isComment = intent.getBooleanExtra("commentState", false);
                    boolean isFinish = intent.getBooleanExtra("isFinish", false);
                    if (isComment) {
                        boolean isExitSession = intent.getBooleanExtra("isExitSession", false);
                        int commentType = intent.getIntExtra("commentType", 1);

                        //如果是邀请评价 更新ui
                        int score = intent.getIntExtra("score", 5);
                        int isResolved = intent.getIntExtra("isResolved", 0);
//                messageAdapter.submitEvaluateData(isResolved, score);
                        messageAdapter.removeEvaluateData();
                        messageAdapter.notifyDataSetChanged();
//                refreshItemByCategory(CusEvaluateMessageHolder.class);

                        if (isExitSession || ChatUtils.isEvaluationCompletedExit(mAppContext, isComment, current_client_model)) {
                            //如果是人工并且评价完毕就释放会话
                            isSessionOver = true;
//                        customerServiceOffline(initModel, 5);
                            ChatUtils.userLogout(mAppContext, "弹窗评价完成评价 结束会话");
                        }
                        if (getSobotActivity() != null) {
                            Message msg = new Message();
                            msg.what = ZhiChiConstant.hander_comment_finish;
                            msg.obj = isFinish;
                            handler.sendMessage(msg);
                        }
                    } else {
                        if (getSobotActivity() != null) {
                            Message msg = new Message();
                            msg.what = ZhiChiConstant.hander_comment_finish;
                            msg.obj = isFinish;
                            handler.sendMessage(msg);
                        }
                    }
                } else if (ZhiChiConstants.sobot_close_now.equals(intent.getAction())) {
                    if (intent.getBooleanExtra("isBackShowEvaluate", true)) {
                        //左上角 返回 满意度评价弹窗 暂不评价，直接返回
                        finish();
                    } else {
                        //右上角点击关闭，暂不评价 ，结束会话，在返回
                        userOffline(initModel);
                        isSessionOver = true;
                        ChatUtils.userLogout(mAppContext, "弹窗评价后点击暂不评价 结束会话");
                        finish();
                    }
                } else if (ZhiChiConstants.sobot_close_now_clear_cache.equals(intent.getAction())) {
                    isSessionOver = true;
                    finish();
                } else if (ZhiChiConstants.SOBOT_CHANNEL_STATUS_CHANGE.equals(intent.getAction())) {
                    if (customerState == CustomerState.Online || customerState == CustomerState.Queuing) {
                        int connStatus = intent.getIntExtra("connStatus", Const.CONNTYPE_IN_CONNECTION);
                        LogUtils.i("connStatus:" + connStatus);
                        switch (connStatus) {
                            case Const.CONNTYPE_IN_CONNECTION:
                                sobot_container_conn_status.setVisibility(View.VISIBLE);
                                sobot_title_conn_status.setText(R.string.sobot_conntype_in_connection);
                                if (sobot_header_center_ll != null) {
                                    sobot_header_center_ll.setVisibility(View.GONE);
                                }
                                sobot_conn_loading.setVisibility(View.VISIBLE);
                                break;
                            case Const.CONNTYPE_CONNECT_SUCCESS:
                                setShowNetRemind(false);
                                sobot_container_conn_status.setVisibility(View.GONE);
                                sobot_title_conn_status.setText("");
                                if (sobot_header_center_ll != null) {
                                    sobot_header_center_ll.setVisibility(View.VISIBLE);
                                }
                                sobot_conn_loading.setVisibility(View.GONE);
                                stopPolling();
                                break;
                            case Const.CONNTYPE_UNCONNECTED:
                                sobot_container_conn_status.setVisibility(View.VISIBLE);
                                sobot_title_conn_status.setText(R.string.sobot_conntype_unconnected);
                                if (sobot_header_center_ll != null) {
                                    sobot_header_center_ll.setVisibility(View.GONE);
                                }
                                sobot_conn_loading.setVisibility(View.GONE);
                                if (welcome.getVisibility() != View.VISIBLE) {
                                    setShowNetRemind(true);
                                }
                                break;
                        }
                    } else {
                        mTitleTextView.setVisibility(View.GONE);
                        mCompanyTextView.setVisibility(View.GONE);
                        mAvatarIV.setVisibility(View.VISIBLE);
                        sobot_container_conn_status.setVisibility(View.GONE);
                    }
                } else if (ZhiChiConstants.SOBOT_BROCAST_KEYWORD_CLICK.equals(intent.getAction())) {
                    String tempGroupId = intent.getStringExtra("tempGroupId");
                    String keyword = intent.getStringExtra("keyword");
                    String keywordId = intent.getStringExtra("keywordId");
                    String anwerMsgId = intent.getStringExtra("anwerMsgId");
                    String ruleld = intent.getStringExtra("ruleld");
                    transfer2Custom(tempGroupId, keyword, keywordId, true, 3, anwerMsgId, ruleld);
                } else if (ZhiChiConstants.SOBOT_BROCAST_SEMANTICS_KEYWORD_CLICK.equals(intent.getAction())) {
                    String tempGroupId = intent.getStringExtra("tempGroupId");
                    String semanticsKeyWordId = intent.getStringExtra("semanticsKeyWordId");
                    String semanticsKeyWordName = intent.getStringExtra("semanticsKeyWordName");
                    String semanticsKeyWordQuestionId = intent.getStringExtra("semanticsKeyWordQuestionId");
                    String semanticsKeyWordQuestion = intent.getStringExtra("semanticsKeyWordQuestion");
                    String anwerMsgId = intent.getStringExtra("anwerMsgId");
                    String ruleld = intent.getStringExtra("ruleld");
                    SobotConnCusParam param = new SobotConnCusParam();
                    param.setSemanticsKeyWordId(StringUtils.checkStringIsNull(semanticsKeyWordId));
                    param.setSemanticsKeyWordName(StringUtils.checkStringIsNull(semanticsKeyWordName));
                    param.setSemanticsKeyWordQuestion(StringUtils.checkStringIsNull(semanticsKeyWordQuestion));
                    param.setSemanticsKeyWordQuestionId(StringUtils.checkStringIsNull(semanticsKeyWordQuestionId));
                    transfer2Custom(tempGroupId, true, 12, anwerMsgId, ruleld, param);
                } else if (ZhiChiConstants.SOBOT_BROCAST_REMOVE_FILE_TASK.equals(intent.getAction())) {
                    try {
                        String msgId = intent.getStringExtra("sobot_msgId");
                        if (!TextUtils.isEmpty(msgId)) {
                            for (int i = messageList.size() - 1; i >= 0; i--) {
                                if (msgId.equals(messageList.get(i).getId())) {
                                    messageList.remove(i);
                                    break;
                                }
                            }
                            messageAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        //ignor
                    }
                } else if (ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_TO_CHATLIST.equals(intent.getAction())) {
                    //多伦工单节点留言弹窗留言提交后回显到聊天列表
                    if (intent != null) {
                        Bundle bundle = intent.getExtras();
                        SobotSerializableMap sobotSerializableMap = (SobotSerializableMap) bundle.get("leaveMsgData");
                        if (sobotSerializableMap != null) {
                            LinkedHashMap mapData = sobotSerializableMap.getMap();
                            StringBuilder tempSb = new StringBuilder();
                            Iterator iterator = mapData.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                                tempSb.append(entry.getKey()).append("\n").append(entry.getValue()).append("\n");
                            }
                            if (!TextUtils.isEmpty(tempSb.toString())) {
                                sendMuitidiaLeaveMsg(null, tempSb.toString().substring(0, tempSb.toString().lastIndexOf("\n")), handler, true);
                            }
                            String tipMsgId = (String) bundle.get("tipMsgId");
                            if (!TextUtils.isEmpty(tipMsgId)) {
                                ZhiChiMessageBase base =
                                        messageAdapter.getMsgInfoByMsgId(tipMsgId);
                                base.setAction(ZhiChiConstant.action_mulit_postmsg_tip_nocan_click);
                                messageAdapter.notifyDataSetChanged();
                            }
                        } else {
                            String msgId = intent.getStringExtra("msgId");
                            String msg = intent.getStringExtra("msg");
                            String deployId = intent.getStringExtra("deployId");
                            ZhiChiMessageBase base = new ZhiChiMessageBase();
                            base.setMsgId(msgId);
                            base.setDeployId(deployId);
                            base.setAction(ZhiChiConstant.action_mulit_postmsg_tip_can_click);
                            base.setMsg(msg);
                            updateUiMessage(messageAdapter, base);
                        }
                        gotoLastItem();
                    }
                } else if (ZhiChiConstants.SOBOT_CHAT_MUITILEAVEMSG_RE_COMMIT.equals(intent.getAction())) {
                    String templateId = intent.getStringExtra("templateId");
                    String msgId = intent.getStringExtra("msgId");
                    //多伦工单节点提醒点击后重复弹窗
                    mulitDiaToLeaveMsg(templateId, msgId);
                } else if (ZhiChiConstants.SOBOT_SEND_AI_CARD_MSG.equals(intent.getAction())) {
                    //发送大模型卡片
                    String btnText = intent.getStringExtra("btnText");
                    SobotChatCustomGoods goods = (SobotChatCustomGoods) intent.getSerializableExtra("SobotCustomGoods");
                    SobotChatCustomCard card = (SobotChatCustomCard) intent.getSerializableExtra("SobotCustomCard");
                    sendAiCardMsg(btnText, goods, card);
                }

            } catch (Exception e) {

            }
        }
    }

    /**
     * 大模型发送卡片
     *
     * @param goods 商品
     * @param card  卡片总信息
     */
    private void sendAiCardMsg(String btnText, SobotChatCustomGoods goods, SobotChatCustomCard card) {
        Map<String, Object> parame = ChatUtils.getSendAiCardParameter(btnText, goods, card);
        parame.put("robotId", initModel.getRobotid());
        parame.put("aiAgentCid", initModel.getAiAgentCid());
        String msgId = getMsgId();
        // 当发送成功的时候更新ui界面
        ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
        myMessage.setId(msgId);
        myMessage.setMsgId(msgId);
        myMessage.setSenderName(info.getUser_nick());
        myMessage.setSenderFace(info.getFace());
        myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
        myMessage.setReadStatus(isOpenUnread ? 1 : 0);
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsgType(ZhiChiConstant.message_type_ai_card_msg);
        myMessage.setAnswer(answer);
        myMessage.setT(Calendar.getInstance().getTime().getTime() + "");
        SobotChatCustomCard customCard = new SobotChatCustomCard();
        if (goods != null) {
            List<SobotChatCustomGoods> list = new ArrayList<>();
            list.add(goods);
            customCard.setCustomCards(list);
        } else {
            customCard.setCustomCards(card.getCustomCards());
        }
        myMessage.setCustomCard(customCard);
        Message handMyMessage = handler.obtainMessage();
        handMyMessage.what = ZhiChiConstant.hander_send_msg;
        handMyMessage.obj = myMessage;
        handler.sendMessage(handMyMessage);
        sendHttpRobotMessage("5", msgId, "", initModel.getPartnerid(),
                initModel.getCid(), "", handler, 0, "", "", "", parame);
    }

    //保存当前的数据，进行会话保持
    private void saveCache() {
        ZhiChiConfig config = SobotMsgManager.getInstance(mAppContext).getConfig(info.getApp_key());
        config.isShowUnreadUi = isOpenUnread;
        config.setMessageList(messageList);
        ZhiChiInitModeBase initModelBase = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotActivity(),
                ZhiChiConstant.sobot_last_current_initModel);
        config.setInitModel(initModelBase);
        config.current_client_model = current_client_model;
        if (queryCidsStatus == ZhiChiConstant.QUERY_CIDS_STATUS_SUCCESS) {
            config.cids = cids;
            config.currentCidPosition = currentCidPosition;
            config.queryCidsStatus = queryCidsStatus;
        }

        config.activityTitle = getActivityTitle();
        config.customerState = customerState;
        config.mSatisfactionSet = mSatisfactionSet;
        config.remindRobotMessageTimes = remindRobotMessageTimes;
        config.isAboveZero = isAboveZero;
        config.isComment = isComment;
        config.adminFace = getActivityLogo();
        config.adminName = getAdminName();
        config.paseReplyTimeCustoms = noReplyTimeCustoms;
        config.customTimeTask = customTimeTask;
        config.paseReplyTimeUserInfo = noReplyTimeUserInfo;
        config.userInfoTimeTask = userInfoTimeTask;
        config.isChatLock = isChatLock;
        config.currentUserName = currentUserName;
        config.isNoMoreHistoryMsg = isNoMoreHistoryMsg;
        config.showTimeVisiableCustomBtn = showTimeVisiableCustomBtn;
        config.bottomViewtype = mBottomViewtype;
        config.queueNum = queueNum;
        config.isShowQueueTip = isShowQueueTip;
        config.tempMsgContent = tempMsgContent;
        config.inPolling = inPolling;
        config.mRobotOperatorCount = mRobotOperatorCount;
        config.mOperatorCount = mOperatorCount;
        config.mRobotPanleHeiht = mRobotPanleHeiht;
        config.mArtificialPanleHeiht = mArtificialPanleHeiht;
        config.isOpenUnread = isOpenUnread;
        if (config.isChatLock == 2 || config.isChatLock == 0) {
            Intent intent = new Intent();
            intent.setAction(ZhiChiConstants.SOBOT_TIMER_BROCAST);
            intent.putExtra("info", info);
            intent.putExtra("isStartTimer", true);
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == ll_notReadInfo) {
            //新消息未读提醒按钮，向上
            if (unReadMsgIndex > 0) {
                gotoIndexItem(unReadMsgIndex);
            } else {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    if (messageList.get(i).getAnswer() != null && ZhiChiConstant.
                            sobot_remind_type_below_unread == messageList.get(i).getAnswer().getRemindType()) {
                        gotoIndexItem(i);
                        break;
                    }
                }
            }
            hideNotReadLayout();
        }

        if (view == btn_send_pic) {// 发送消息按钮
            //获取发送内容
            final String message_result = et_sendmessage.getText().toString().trim();
            if (TextUtils.isEmpty(message_result)) {
                et_sendmessage.setText("");
            }
            if (!TextUtils.isEmpty(message_result) && !isConnCustomerService) {
                //转人工接口没跑完的时候  屏蔽发送，防止统计出现混乱
                resetEmoticonBtn();
                try {
                    et_sendmessage.setText("");
                    sendMsg(message_result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (view == cb_plus_view) {
            LogUtils.i("-------点击加号-------" + cb_plus_view.isChecked());
            hideRobotVoiceHint();
            pressSpeakSwitchPanelAndKeyboard(cb_plus_view, cb_plus_view.isChecked());
            showPlusMenuHindKeyboard();
            doEmoticonBtn2Blur();
            gotoLastItem();
        }

        if (view == cb_emoticon_view) {//显示表情面板
            LogUtils.i("-------点击表情-------");
            cb_plus_view.setChecked(false);
//            showEmoHindKeyboard();
//            // 切换表情面板
//            handler.sendEmptyMessage(ZhiChiConstant.hander_btn_emotion_view);
//            pressSpeakSwitchPanelAndKeyboard(cb_emoticon_view, cb_emoticon_view.isChecked());
//            //切换表情按钮的状态
//            gotoLastItem();
        }

        if (view == btn_model_edit) {// 从语音转换到编辑模式
            changeBottomEditUI();
        }

        if (view == btn_model_voice) { // 从编辑模式转换到语音
            cb_plus_view.setChecked(false);
            showRobotVoiceHint();
            doEmoticonBtn2Blur();
            permissionListener = new PermissionListenerImpl() {
                @Override
                public void onPermissionSuccessListener() {
                    showAudioRecorder();
                }
            };
            if (!isHasPermission(2, 3)) {
                return;
            }
            showAudioRecorder();
        }

        if (view == ll_switch_robot) {
            // 打开机器人切换页面
            if (!isSessionOver) {
                Intent intent = new Intent(getSobotActivity(), SobotRobotListActivity.class);
                intent.putExtra("uid", initModel.getPartnerid());
                intent.putExtra("robotFlag", initModel.getRobotid());
                startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_SWITCH_ROBOT);
            }
        }
        if (view == iv_close) {
            onCloseMenuClick();
        }
        if (view == iv_right_second) {
            if (!TextUtils.isEmpty(SobotUIConfig.sobot_title_right_menu2_call_num)) {
                if (SobotOption.functionClickListener != null) {
                    SobotOption.functionClickListener.onClickFunction(getSobotActivity(), SobotFunctionType.ZC_PhoneCustomerService);
                }
                if (SobotOption.newHyperlinkListener != null) {
                    boolean isIntercept = SobotOption.newHyperlinkListener.onPhoneClick(getSobotActivity(), "tel:" + SobotUIConfig.sobot_title_right_menu2_call_num);
                    if (isIntercept) {
                        return;
                    }
                }
                ChatUtils.callUp(SobotUIConfig.sobot_title_right_menu2_call_num, getSobotActivity());
            } else {
                btnSatisfaction();
            }
        }

        if (view == iv_right_third) {
            if (!TextUtils.isEmpty(SobotUIConfig.sobot_title_right_menu3_call_num)) {
                if (SobotOption.functionClickListener != null) {
                    SobotOption.functionClickListener.onClickFunction(getSobotActivity(), SobotFunctionType.ZC_PhoneCustomerService);
                }
                if (SobotOption.newHyperlinkListener != null) {
                    boolean isIntercept = SobotOption.newHyperlinkListener.onPhoneClick(getSobotActivity(), "tel:" + SobotUIConfig.sobot_title_right_menu3_call_num);
                    if (isIntercept) {
                        return;
                    }
                }
                ChatUtils.callUp(SobotUIConfig.sobot_title_right_menu3_call_num, getSobotActivity());
            } else {
                LogUtils.e("电话号码不能为空");
            }
        }
        if (view == ll_newmsg) {
            //新消息未读提醒按钮，向下
            //滑倒最底部
            gotoLastItem();
            //隐藏新消息提示
            hideNewmsgLayout();
        }
    }

    //清空顶部未读消息数隐藏新消息提示
    private void hideNotReadLayout() {
        if (ll_notReadInfo != null) {
            ll_notReadInfo.setVisibility(View.GONE);
        }
        mUnreadNum = 0;
    }

    //清空新消息数隐藏新消息提示
    private void hideNewmsgLayout() {
        newMsgNum = 0;
        msgAnswersNum = 0;
        if (tv_newmsg != null) {
            tv_newmsg.setText("");
        }
        if (ll_newmsg != null) {
            ll_newmsg.setVisibility(View.GONE);
        }
    }

    //修改底部 从语音转换到编辑模式
    private void changeBottomEditUI() {
        cb_plus_view.setChecked(false);
        hideRobotVoiceHint();
        doEmoticonBtn2Blur();
        // 软件盘的处理
//            showKeyboardHindPanel();
        KPSwitchConflictUtil.showKeyboard(mPanelLayout, et_sendmessage);
        editModelToVoice(View.GONE, "123");// 编辑模式隐藏 ，语音模式显示
    }

    //开始录音
    private void showAudioRecorder() {
        try {
            mFileName = SobotPathManager.getInstance().getVoiceDir() + "sobot_tmp.wav";
            String state = android.os.Environment.getExternalStorageState();
            if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
                LogUtils.i("SD Card is not mounted,It is  " + state + ".");
            }
            File directory = new File(mFileName).getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                LogUtils.i("Path to file could not be created");
            }
            extAudioRecorder = ExtAudioRecorder.getInstanse(false);
            extAudioRecorder.setOutputFile(mFileName);
            extAudioRecorder.prepare();
            extAudioRecorder.start(new ExtAudioRecorder.AudioRecorderListener() {
                @Override
                public void onHasPermission() {
                    hidePanelAndKeyboard(mPanelLayout);
                    editModelToVoice(View.VISIBLE, "");// 编辑模式显示
                    if (btn_press_to_speak.getVisibility() == View.VISIBLE) {
                        btn_press_to_speak.setVisibility(View.VISIBLE);
                        btn_press_to_speak.setClickable(true);
                        btn_press_to_speak.setOnTouchListener(new PressToSpeakListen());
                        btn_press_to_speak.setEnabled(true);
                        txt_speak_content.setText(R.string.sobot_press_say);
                        txt_speak_content.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNoPermission() {
                    ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_no_record_audio_permission));
                }
            });
            stopVoice();
        } catch (Exception e) {
            LogUtils.i("prepare() failed");
        }
    }

    private void showRobotVoiceHint() {
        send_voice_robot_hint.setVisibility(current_client_model == ZhiChiConstant.client_model_robot ? View.VISIBLE : View.GONE);
    }

    //隐藏机器人语音转文字提示语
    private void hideRobotVoiceHint() {
        send_voice_robot_hint.setVisibility(View.GONE);
    }

    /**
     * 发送消息的方法
     *
     * @param content
     */
    @Override
    protected void sendMsg(String content) {
        if (initModel == null) {
            return;
        }

        String msgId = getMsgId();
        if (ZhiChiConstant.client_model_robot == current_client_model) {
            if (type == 4 && initModel.getInvalidSessionFlag() == 1 && customerState != CustomerState.Queuing && TextUtils.isEmpty(tempMsgContent)) {
                //人工优先,用户没有排队并且开启客户发送消息后分配客服,转人工发送该消息
                //如果排队，再发送的消息就是机器人的消息
                tempMsgContent = content;
                doClickTransferBtn();
                return;
            }
            if (type == 2) {
                if (initModel.getInvalidSessionFlag() == 1) {
                    //开启客户发送消息后分配客服,转人工发送该消息
                    tempMsgContent = content;
                }
                doClickTransferBtn();
                return;
            } else if ((type == ZhiChiConstant.type_robot_first || type == ZhiChiConstant.type_custom_first) && info.getTransferKeyWord() != null) {
                //用户可以输入关键字 进行转人工
                HashSet<String> transferKeyWord = info.getTransferKeyWord();
                if (!TextUtils.isEmpty(content) && transferKeyWord.contains(content)) {
                    sendTextMessageToHandler(msgId, content, handler, 1, SEND_TEXT);
                    doClickTransferBtn();
                    return;
                }
            }
        }
        if (!TextUtils.isEmpty(tempMsgContent) && tempMsgContent.equals(content) && tmpMsgType != 0) {
            //走发送图片接口
            if (tmpMsgType == 1) {
                //图片
                if (selectedFile != null) {
                    //拍摄时
                    ChatUtils.sendPicLimitBySize(isOpenUnread ? 1 : 0, selectedFile.getAbsolutePath(), initModel.getCid(),
                            initModel.getPartnerid(), handler, getSobotActivity(), messageAdapter, true, current_client_model, initModel, info);
                } else if (selectedImage != null) {
                    //相册选择
                    ChatUtils.sendPicByUri(getSobotActivity(), isOpenUnread ? 1 : 0, handler, selectedImage, initModel, messageAdapter, false, current_client_model, info);
                }
            } else if (tmpMsgType == 3) {
                //视频
                if (selectedFile != null) {
                    //拍摄时
                    uploadVideo(selectedFile, null, messageAdapter);
                } else if (selectedImage != null) {
                    String path = ImageUtils.getPath(getSobotActivity(), selectedImage);
                    File videoFile = new File(path);
                    if (videoFile.exists()) {
                        uploadVideo(videoFile, selectedImage, messageAdapter);
                    }
                }
            } else {
                //其他文件
                if (selectedFile != null) {
                    uploadFile(selectedFile, handler, messageAdapter, true);
                } else if (selectedImage != null) {
                    String path = ImageUtils.getPath(getSobotActivity(), selectedImage);
                    File selectedFile = new File(path);
                    if (selectedFile.exists()) {
                        uploadFile(selectedFile, handler, messageAdapter, true);
                    }
                }
            }
        } else {
            // 通知Handler更新 我的消息ui
            sendTextMessageToHandler(msgId, content, handler, 2, SEND_TEXT);
            LogUtils.i("当前发送消息模式：" + current_client_model);
            setTimeTaskMethod(handler);
            sendMessageWithLogic(msgId, content, initModel, handler, current_client_model, 0, "", null);
        }
    }

    /**
     * 发送卡片消息
     *
     * @param consultingContent
     */
    protected void sendCardMsg(ConsultingContent consultingContent) {
        if (initModel == null || consultingContent == null) {
            return;
        }
        final String title = consultingContent.getSobotGoodsTitle();
        final String fromUrl = consultingContent.getSobotGoodsFromUrl();
        if (customerState == CustomerState.Online
                && current_client_model == ZhiChiConstant.client_model_customService
                && !TextUtils.isEmpty(fromUrl) && !TextUtils.isEmpty(title)) {
            String msgId = getMsgId();
            setTimeTaskMethod(handler);
            sendHttpCardMsg(initModel.getPartnerid(), initModel.getCid(), handler, msgId, consultingContent);
        }
    }

    /**
     * 发送订单卡片消息
     *
     * @param orderCardContent
     */
    protected void sendOrderCardMsg(OrderCardContentModel orderCardContent) {
        if (initModel == null || orderCardContent == null) {
            return;
        }
        final String title = orderCardContent.getOrderCode();
        if (customerState == CustomerState.Online
                && current_client_model == ZhiChiConstant.client_model_customService
                && !TextUtils.isEmpty(title)) {
            String msgId = getMsgId();
            setTimeTaskMethod(handler);
            sendHttpOrderCardMsg(initModel.getPartnerid(), initModel.getCid(), handler, msgId, orderCardContent);
        }
    }

    /**
     * 满意度评价
     * 首先判断是否评价过 评价过 弹您已完成提示 未评价 判断是否达到可评价标准
     *
     * @param isActive    是否是主动评价  true 主动  flase 邀请
     * @param score       几颗星
     * @param isSolve     是否已解决 0 是已解决  1 未解决
     * @param checklables 主动邀请选中的标签
     */
    public void submitEvaluation(boolean isActive, int score, int isSolve, String checklables) {
        if (isActive && isComment) {
            //主动评价 并且已经评价过,就不能再次弹出评价
            hidePanelAndKeyboard(mPanelLayout);
            showHint(getResources().getString(R.string.sobot_completed_the_evaluation));
            return;
        }
        if (isUserBlack()) {
            showHint(getResources().getString(R.string.sobot_unable_to_evaluate));
        } else if (isAboveZero) {
            if (current_client_model == ZhiChiConstant.client_model_robot && initModel.isAiAgent()) {
                showAiEvaluateDialog(isSessionOver, false, false, initModel, current_client_model, 1, currentUserName, 5, -2, "", false, false);
            } else {
                if (isActive()) {
                    Intent intent = showEvaluateDialog(getSobotActivity(), isSessionOver, false, false, initModel, current_client_model, isActive ? 1 : 0, currentUserName, score, isSolve, checklables, false, false);
                    startActivity(intent);
                }
            }
        } else {
            showHint(getResources().getString(R.string.sobot_after_consultation_to_evaluate_custome_service));
        }
    }

    public void showVoiceBtn() {
        if (current_client_model == ZhiChiConstant.client_model_robot && type != 2) {
            btn_model_voice.setVisibility(info.isUseVoice() && info.isUseRobotVoice() ? View.VISIBLE : View.GONE);
        } else {
            btn_model_voice.setVisibility(info.isUseVoice() ? View.VISIBLE : View.GONE);
        }
    }

    private void sendMsgToRobot(ZhiChiMessageBase base, int sendType, int questionFlag, String
            docId) {
        sendMsgToRobot(base, sendType, questionFlag, docId, "", null);
    }

    private void sendMsgToRobot(ZhiChiMessageBase base, int sendType, int questionFlag, String
            docId, String multiRoundMsg, Map<String, Object> customerParams) {
        if (!TextUtils.isEmpty(multiRoundMsg)) {
            sendTextMessageToHandler(base.getId(), multiRoundMsg, handler, 2, sendType);
        } else {
            sendTextMessageToHandler(base.getId(), base.getContent(), handler, 2, sendType);
        }
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsgType(ZhiChiConstant.message_type_text);
        answer.setMsg(base.getContent());
        base.setAnswer(answer);
        base.setSenderType(ZhiChiConstant.message_sender_type_customer);
        sendMessageWithLogic(base.getId(), base.getContent(), initModel, handler, current_client_model, questionFlag, docId, customerParams);
    }

    /**
     * 更新 多轮会话的状态
     */
    private void restMultiMsg() {
        for (int i = 0; i < messageList.size(); i++) {
            ZhiChiMessageBase data = messageList.get(i);
            if (data.getAnswer() != null && data.getAnswer().getMultiDiaRespInfo() != null
                    && !data.getAnswer().getMultiDiaRespInfo().getEndFlag()) {
                data.setMultiDiaRespEnd(1);
            }
        }
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            LogUtils.i("多媒体返回的结果：" + requestCode + "--" + resultCode + "--" + data);

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == ZhiChiConstant.REQUEST_CODE_picture) { // 发送本地图片
                    if (data != null && data.getData() != null) {
                        selectedImage = data.getData();
                        if (selectedImage == null) {
                            selectedImage = ImageUtils.getUri(data, getSobotActivity());
                        }
                        String path = ImageUtils.getPath(getSobotActivity(), selectedImage);
                        if (MediaFileUtils.isVideoFileType(path)) {
                            try {
                                File selectedFile = new File(path);
                                if (selectedFile.exists()) {
                                    if (selectedFile.length() > 50 * 1024 * 1024) {
                                        ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_file_upload_failed));
                                        return;
                                    }
                                }
                                //SobotDialogUtils.startProgressDialog(getSobotActivity());
                                File videoFile = new File(path);
                                if (videoFile.exists()) {
                                    if (initModel.getInvalidSessionFlag() == 1 && type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                        //使用留言的上传
                                        tmpMsgType = 3;
                                        ChatUtils.sendPicByUriPost(getSobotActivity(), selectedImage, sendFileListener, false);
                                    } else {
                                        uploadVideo(videoFile, selectedImage, messageAdapter);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                //使用留言的上传
                                tmpMsgType = 1;
                                ChatUtils.sendPicByUriPost(getSobotActivity(), selectedImage, sendFileListener, false);
                            } else {
                                ChatUtils.sendPicByUri(getSobotActivity(), isOpenUnread && current_client_model == ZhiChiConstant.client_model_customService ? 1 : 0, handler, selectedImage, initModel, messageAdapter, false, current_client_model, info);
                            }
                        }
                        gotoLastItem();
                    } else {
                        ToastUtil.showLongToast(mAppContext, getResources().getString(R.string.sobot_did_not_get_picture_path));
                    }

                }
                hidePanelAndKeyboard(mPanelLayout);
            } else if (requestCode == ZhiChiConstant.REQUEST_COCE_TO_CLEAR_HISTORY && resultCode == ZhiChiConstant.REQUEST_COCE_TO_CLEAR_HISTORY) {
                clearHistory();
            }
            if (data != null) {
                switch (requestCode) {
                    case ZhiChiConstant.REQUEST_COCE_TO_GRROUP:
                        boolean toLeaveMsg = data.getBooleanExtra("toLeaveMsg", false);
                        int groupIndex = data.getIntExtra("groupIndex", -1);
                        if (toLeaveMsg) {
                            SharedPreferencesUtil.saveStringData(getSobotActivity(), ZhiChiConstant.sobot_connect_group_id, list_group != null ? list_group.get(groupIndex).getGroupId() : "");
                            startToPostMsgActivty(false);
                            return;
                        }
                        int tmpTransferType = data.getIntExtra("transferType", 0);
                        LogUtils.i("groupIndex-->" + groupIndex);
                        if (groupIndex >= 0) {
                            SobotConnCusParam param = (SobotConnCusParam) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM);
                            if (param != null) {
                                param.setGroupId(list_group.get(groupIndex).getGroupId());
                                param.setGroupName(list_group.get(groupIndex).getGroupName());
                                param.setTransferType(tmpTransferType);
                            }
                            requestQueryFrom(param, info.isCloseInquiryForm());
                        }
                        break;
                    case ZhiChiConstant.REQUEST_COCE_TO_QUERY_FROM:
                        //填完询前表单后的回调
                        if (resultCode == ZhiChiConstant.REQUEST_COCE_TO_QUERY_FROM) {
                            SobotConnCusParam param = (SobotConnCusParam) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_BUNDLE_DATA_CONNCUSPARAM);
                            connectCustomerService(param);
                        } else {
                            //询前表单取消
                            isHasRequestQueryFrom = false;
                            if (type == ZhiChiConstant.type_custom_only) {
                                //仅人工模式退出聊天
                                isSessionOver = true;
                                //清除会话信息
                                clearCache();
                                mUnreadNum = 0;
                                finish();
                            }
                        }
                        break;
                    case ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_FILE:
                        //选择文件
                        selectedImage = data.getData();
                        if (null == selectedImage) {
                            selectedFile = (File) data.getSerializableExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE);
                            if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                //文件的上传
                                tmpMsgType = 4;
                                selectedImage = ImageUtils.getMediaUriFromPath(getSobotActivity(), selectedFile.getAbsolutePath());
                                ChatUtils.sendPicByUriPost(getSobotActivity(), selectedImage, sendFileListener, false);
                            } else {
                                uploadFile(selectedFile, handler, messageAdapter, false);
                            }
                        } else {
                            String tmpMsgId = getMsgId();
                            if (selectedImage == null) {
                                selectedImage = ImageUtils.getUri(data, getSobotActivity());
                            }
                            String path = ImageUtils.getPath(getSobotActivity(), selectedImage);
                            if (TextUtils.isEmpty(path)) {
                                ToastUtil.showToast(mAppContext, getResources().getString(R.string.sobot_cannot_open_file));
                                return;
                            }
                            selectedFile = new File(path);
                            LogUtils.i("tmpMsgId:" + tmpMsgId);
                            if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                //使用留言的上传
                                tmpMsgType = 4;
                                ChatUtils.sendPicByUriPost(getSobotActivity(), selectedImage, sendFileListener, false);
                            } else {
                                uploadFile(selectedFile, handler, messageAdapter, true);
                            }
                        }
                        gotoLastItem();
                        break;
                    case ChatUtils.REQUEST_CODE_CAMERA:
                        int actionType = SobotCameraActivity.getActionType(data);
                        if (actionType == SobotCameraActivity.ACTION_TYPE_VIDEO) {
                            selectedFile = new File(SobotCameraActivity.getSelectedVideo(data));
                            if (selectedFile.exists()) {
                                String snapshotPath = SobotCameraActivity.getSelectedImage(data);
                                if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                    //使用留言的上传
                                    tmpMsgType = 3;
                                    ChatUtils.sendPicByFilePost(getSobotActivity(), selectedFile, sendFileListener);
                                } else {
                                    uploadVideo(selectedFile, null, messageAdapter);
                                }
                                gotoLastItem();
                            } else {
                                ToastUtil.showLongToast(mAppContext, getResources().getString(R.string.sobot_pic_select_again));
                            }
                        } else {
                            selectedFile = new File(SobotCameraActivity.getSelectedImage(data));
                            if (selectedFile.exists()) {
                                if (type == ZhiChiConstant.type_custom_only && customerState != CustomerState.Online) {
                                    //使用留言的上传
                                    tmpMsgType = 1;
                                    ChatUtils.sendPicByFilePost(getSobotActivity(), selectedFile, sendFileListener);
                                } else {
                                    ChatUtils.sendPicLimitBySize(isOpenUnread ? 1 : 0, selectedFile.getAbsolutePath(), initModel.getCid(),
                                            initModel.getPartnerid(), handler, getSobotActivity(), messageAdapter, true, current_client_model, initModel, info);
                                }
                                gotoLastItem();
                            } else {
                                ToastUtil.showLongToast(mAppContext, getResources().getString(R.string.sobot_pic_select_again));
                            }
                        }
                        break;
                    case SobotPostLeaveMsgActivity.EXTRA_MSG_LEAVE_REQUEST_CODE:
                        //离线留言
                        String content = SobotPostLeaveMsgActivity.getResultContent(data);
                        ZhiChiMessageBase tmpMsg = ChatUtils.getLeaveMsgTip(content);
                        isAboveZero = true;
                        messageAdapter.justAddData(tmpMsg);
                        messageAdapter.notifyDataSetChanged();
                        customerServiceOffline(initModel, 99);
                        break;
                    case ZhiChiConstant.REQUEST_COCE_TO_SWITCH_ROBOT:
                        if (resultCode == ZCSobotConstant.EXTRA_SWITCH_ROBOT_REQUEST_CODE) {
                            SobotRobot sobotRobot = (SobotRobot) data.getSerializableExtra("sobotRobot");
                            if (initModel != null && sobotRobot != null) {
                                initModel.setGuideFlag(sobotRobot.getGuideFlag());
                                initModel.setRobotid(sobotRobot.getRobotFlag());
                                initModel.setRobotLogo(sobotRobot.getRobotLogo());
                                initModel.setRobotName(sobotRobot.getRobotName());
                                initModel.setRobotHelloWord(sobotRobot.getRobotHelloWord());
                                initModel.setAiStatus(sobotRobot.getAiStatus());
                                initModel.setAiAgentCommentFlag(sobotRobot.getCommentFlag());
                                initModel.setTemplateId(sobotRobot.getTemplateId());
                                //当前机器人的接待方案id,常见问题会使用
                                initModel.setSessionPhaseAndFaqIdRespVos(sobotRobot.getSessionPhaseAndFaqIdRespVos());
                                showLogicTitle(initModel.getRobotName(), initModel.getRobotLogo(), false);
                                List<ZhiChiMessageBase> datas = messageAdapter.getDatas();
                                int count = 0;
                                for (int i = datas.size() - 1; i >= 0; i--) {
                                    if ((ZhiChiConstant.message_sender_type_robot_welcome_msg == datas.get(i).getSenderType())
                                            || (ZhiChiConstant.message_sender_type_questionRecommend == datas.get(i).getSenderType())
                                            || (ZhiChiConstant.message_sender_type_robot_guide == datas.get(i).getSenderType())) {
                                        datas.remove(i);
                                        count++;
                                        if (count >= 3) {
                                            break;
                                        }
                                    }
                                }
                                //请求快捷菜单
                                requestAllQuickMenu(quick_menu_robot);

                                messageAdapter.notifyDataSetChanged();
                                //切换机器人后调整UI
                                remindRobotMessageTimes = 0;
                                remindRobotMessage(handler, initModel, info);
                                if (initModel.getRealuateInfoFlag() == 1) {
                                    //切换机器人后重新获取点踩配置
                                    requestRealuateConfig(false, "", "");
                                }
                                if (initModel.isAiAgent()) {
                                    //获取大模型顶踩配置信息
                                    getAiRobotRealuateConfigInfo(false, "", "", "");
                                }
                            }
                        }
                        break;
                    case ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_LANGUAE:
                        SobotlanguaeModel languaeModel = (SobotlanguaeModel) data.getSerializableExtra("selectLanguaeModel");
                        String removeMsgId = data.getStringExtra("removeMsgId");
                        if (StringUtils.isNoEmpty(removeMsgId)) {
                            if (messageAdapter != null) {
                                messageAdapter.removeByMsgId(removeMsgId);
                            }
                        }
                        if (languaeModel != null) {
                            showSelectLanguaeTipMessage(languaeModel);
                        }
                        break;
                    case ZhiChiConstant.REQUEST_COCE_TO_FORMINFO:
//                        FormInfoModel formInfoModels = (FormInfoModel) data.getSerializableExtra("formInfoModels");
                        SobotConnCusParam param = (SobotConnCusParam) data.getSerializableExtra("param");
                        SobotTransferOperatorParam tparam = (SobotTransferOperatorParam) data.getSerializableExtra("tparam");
                        if (resultCode == ZhiChiConstant.REQUEST_COCE_TO_FORMINFO) {
                            connectCustomerService(param);
                        } else {
                            //询前表单取消
                            isHasRequestQueryFrom = false;
                            if (type == ZhiChiConstant.type_custom_only) {
                                //仅人工模式退出聊天
                                isSessionOver = true;
                                //清除会话信息
                                clearCache();
                                mUnreadNum = 0;
                                finish();
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            isCutVoice = false;
            // 获取说话位置的点击事件
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    //暂停播放语音
                    if (AudioTools.getInstance().isPlaying()) {
                        //停止播放
                        AudioTools.getInstance().stop();
                        messageRV.post(new Runnable() {

                            @Override
                            public void run() {
                                if (info == null) {
                                    return;
                                }
                                for (int i = 0, count = messageRV.getChildCount(); i < count; i++) {
                                    View child = messageRV.getChildAt(i);
                                    if (child == null || child.getTag() == null || !(child.getTag() instanceof VoiceMessageHolder)) {
                                        continue;
                                    }
                                    VoiceMessageHolder holder = (VoiceMessageHolder) child.getTag();
                                    if (holder != null) {
                                        holder.stopAnim();
                                        holder.checkBackground();
                                    }
                                }
                            }
                        });
                    }
                    //放弃音频焦点
                    abandonAudioFocus();
                    voiceMsgId = getMsgId();
                    // 在这个点击的位置
                    cb_plus_view.setClickable(false);
                    btn_model_edit.setClickable(false);
                    cb_plus_view.setEnabled(false);
                    btn_model_edit.setEnabled(false);
                    if (Build.VERSION.SDK_INT >= 11) {
//                        cb_plus_view.setAlpha(0.4f);
//                        btn_model_edit.setAlpha(0.4f);
                    }
                    stopVoiceTimeTask();
                    v.setPressed(true);
                    voice_time_long.setText("00" + "''");
                    voiceTimeLongStr = "00:00";
                    voiceTimerLong = 0;
                    currentVoiceLong = 0;
                    ll_sound_recording.setVisibility(View.VISIBLE);
                    fl_sound_recording_animation.setVisibility(View.VISIBLE);
                    iv_sound_recording_in_progress.setVisibility(View.VISIBLE);
                    voice_time_long.setVisibility(View.VISIBLE);
                    iv_sound_recording_cancle.setVisibility(View.GONE);
                    txt_speak_content.setText(R.string.sobot_up_send);
                    // 设置语音的定时任务
                    startVoice();
                    return true;
                // 第二根手指按下
                case MotionEvent.ACTION_POINTER_DOWN:
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (!is_startCustomTimerTask) {
                        noReplyTimeUserInfo = 0;
                    }

                    if (event.getY() < 10) {
                        // 取消界面的显示
                        fl_sound_recording_animation.setVisibility(View.VISIBLE);
                        iv_sound_recording_cancle.setVisibility(View.VISIBLE);
                        iv_sound_recording_in_progress.setVisibility(View.GONE);
                        txt_speak_content.setText(R.string.sobot_release_to_cancel);
                        tv_recording_hint.setText(R.string.sobot_release_to_cancel);
                        tv_recording_hint.setTextColor(ContextCompat.getColor(getSobotActivity(), R.color.sobot_common_red));
                    } else {
                        if (voiceTimerLong != 0) {
                            txt_speak_content.setText(R.string.sobot_up_send);
                            fl_sound_recording_animation.setVisibility(View.VISIBLE);
                            iv_sound_recording_in_progress.setVisibility(View.VISIBLE);
                            iv_sound_recording_cancle.setVisibility(View.GONE);
                            tv_recording_hint.setText(R.string.sobot_move_up_to_cancel);
                            tv_recording_hint.setTextColor(ContextCompat.getColor(getSobotActivity(), R.color.sobot_color_text_third));
                        }
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    // 手指抬起的操作
                    int toLongOrShort = 0;
                    cb_plus_view.setClickable(true);
                    btn_model_edit.setClickable(true);
                    cb_plus_view.setEnabled(true);
                    btn_model_edit.setEnabled(true);
                    if (Build.VERSION.SDK_INT >= 11) {
                        cb_plus_view.setAlpha(1f);
                        btn_model_edit.setAlpha(1f);
                    }
                    v.setPressed(false);
                    txt_speak_content.setText(R.string.sobot_press_say);
                    stopVoiceTimeTask();
                    stopVoice();
                    if (ll_sound_recording.getVisibility() == View.VISIBLE
                            && !isCutVoice) {
                        hidePanelAndKeyboard(mPanelLayout);
                        if (animationDrawable != null) {
                            animationDrawable.stop();
                        }
                        voice_time_long.setText("00" + "''");
                        voice_time_long.setVisibility(View.GONE);
                        if (event.getY() < 0) {
                            ll_sound_recording.setVisibility(View.GONE);
                            sendVoiceMap(2, voiceMsgId);
                            return true;
                            // 取消发送语音
                        } else {
                            // 发送语音
                            if (currentVoiceLong < 1 * 1000) {
                                fl_sound_recording_animation.setVisibility(View.VISIBLE);
                                tv_recording_hint.setText(R.string.sobot_voice_time_short);
                                voice_time_long.setVisibility(View.VISIBLE);
                                voice_time_long.setText("00:00");
                                iv_sound_recording_in_progress.setVisibility(View.GONE);
                                toLongOrShort = 0;
                                sendVoiceMap(2, voiceMsgId);
                                ll_sound_recording.setVisibility(View.GONE);
                                ToastUtil.showCustomToast(getSobotActivity(), getResources().getString(R.string.sobot_voice_time_short), R.drawable.sobot_icon_tanhao);
                            } else if (currentVoiceLong < minRecordTime * 1000) {
                                ll_sound_recording.setVisibility(View.GONE);
                                sendVoiceMap(1, voiceMsgId);
                                return true;
                            } else if (currentVoiceLong > minRecordTime * 1000) {
                                toLongOrShort = 1;
                                fl_sound_recording_animation.setVisibility(View.VISIBLE);
//                                recording_hint.setText(R.string.sobot_voiceTooLong);
                                iv_sound_recording_in_progress.setVisibility(View.GONE);
                            } else {
                                sendVoiceMap(2, voiceMsgId);
                            }
                        }
                        currentVoiceLong = 0;
                        closeVoiceWindows(toLongOrShort);
                    } else {
                        sendVoiceMap(2, voiceMsgId);
                    }
                    voiceTimerLong = 0;
                    restartMyTimeTask(handler);
                    // mFileName
                    return true;
                default:
                    sendVoiceMap(2, voiceMsgId);
                    closeVoiceWindows(2);
                    return true;
            }
        }
    }

    // 获取标题昵称
    public String getActivityTitle() {
        if (mTitleTextView == null) {
            return "";
        }
        return mTitleTextView.getText().toString();
    }

    // 获取导航栏logo
    public String getActivityLogo() {
        if (mAvatarIV == null || mAvatarIV.getTag() == null) {
            return "";
        }
        return mAvatarIV.getTag().toString();
    }

    /**
     * 返回键监听
     *
     * @return true 消费事件
     */
    public void onBackPress() {
        if (isActive()) {
            //按返回按钮的时候 如果面板显示就隐藏面板  如果面板已经隐藏那么就是用户想退出
            if (mPanelLayout.getVisibility() == View.VISIBLE) {
                hidePanelAndKeyboard(mPanelLayout);
                return;
            } else {
                if (info.isShowSatisfaction()) {
                    if (current_client_model == ZhiChiConstant.client_model_robot && initModel.isAiAgent()) {
                        showAiEvaluateDialog(isSessionOver, true, false, initModel, current_client_model, 1, currentUserName, 5, -2, "", true, true);
                        return;
                    } else {
                        if (isAboveZero && !isComment) {
                            // 退出时 之前没有评价过的话 才能 弹评价框
                            Intent intent = showEvaluateDialog(getSobotActivity(), isSessionOver, true, false, initModel,
                                    current_client_model, 1, currentUserName, 5, -2, "", true, true);
                            startActivity(intent);
                            return;
                        }
                    }
                }
            }
            mUnreadNum = 0;
            finish();
        }
    }

    protected String getSendMessageStr() {
        return et_sendmessage.getText().toString().trim();
    }

    /**
     * 切换机器人：显示切换机器人业务的按钮
     */
    private void showSwitchRobotBtn() {
        if (initModel != null && type != 2 && current_client_model == ZhiChiConstant.client_model_robot) {
            ll_switch_robot.setVisibility(initModel.isRobotSwitchFlag() ? View.VISIBLE : View.GONE);
            if (initModel.isRobotSwitchFlag() && tv_switch_robot != null && ll_switch_robot != null) {
                tv_switch_robot.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String language = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
                        if (SobotStringUtils.isNoEmpty(language)) {
                            //自动贴边
//                                if (mLastRawX <= center) {
                            if ("ar".equals(language)) {
                                ll_switch_robot.animate()
                                        .translationX(-tv_switch_robot.getWidth())
                                        .setDuration(300);// 设置动画持续时间，单位毫秒
                            } else {
                                ll_switch_robot.animate()
                                        .translationX(tv_switch_robot.getWidth())
                                        .setDuration(300);// 设置动画持续时间，单位毫秒
                            }
                        } else {
                            ll_switch_robot.animate()
                                    .translationX(tv_switch_robot.getWidth())
                                    .setDuration(300);// 设置动画持续时间，单位毫秒
                        }
                    }
                }, 3000);
            }
        } else {
            ll_switch_robot.setVisibility(View.GONE);
        }
    }

    /**
     * 切换机器人：根据主题色更改切换机器人图标颜色
     */
    public void updateUIByThemeColor() {
        if (ThemeUtils.isChangedThemeColor(getSobotActivity())) {
            int color = ThemeUtils.getThemeColor(getSobotActivity());
            tv_switch_robot.setTextColor(color);
            iv_switch_robot.setImageDrawable(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_icon_switch_robot), color));
            btn_send_pic.setBackground(ThemeUtils.applyColorToDrawable(getResources().getDrawable(R.drawable.sobot_pic_send_icon), color));
        }
    }

    private void applyUIConfig() {
        if (sobot_iv_right != null) {
            if (SobotUIConfig.sobot_title_right_menu1_display) {
                sobot_iv_right.setVisibility(View.VISIBLE);
            } else {
                sobot_iv_right.setVisibility(View.GONE);
            }
        }
        if (SobotUIConfig.sobot_title_right_menu2_display) {
            iv_right_second.setVisibility(View.VISIBLE);
            if (SobotUIConfig.DEFAULT != SobotUIConfig.sobot_title_right_menu2_bg) {
                Drawable img = getResources().getDrawable(SobotUIConfig.sobot_title_right_menu2_bg);
                iv_right_second.setImageDrawable(img);
            }
        }

        if (SobotUIConfig.sobot_title_right_menu3_display) {
            iv_right_third.setVisibility(View.VISIBLE);
            if (SobotUIConfig.DEFAULT != SobotUIConfig.sobot_title_right_menu3_bg) {
                Drawable img = getResources().getDrawable(SobotUIConfig.sobot_title_right_menu3_bg);
                iv_right_third.setImageDrawable(img);
            }
        }
    }

    /**
     * 快捷菜单: 第一个菜单
     * 切换状态都要调用一次，机器人状态显示的是转人工
     * 转人工后，现在取消排队或者结束会话
     *
     * @param viewType 0.仅机器人,1.机器人对话框,2.人工对话框,3.仅人工排队中,4.被下线,5.智能模式下排队中,6.仅人工模式没有客服在线
     */
    private void setMenuFrist(int viewType) {
        if (viewType == ZhiChiConstant.bottomViewtype_outline) {
            //隐藏自主菜单
            sobot_custom_menu.setVisibility(View.GONE);
            return;
        }
        View view = null;
        if (viewType == ZhiChiConstant.bottomViewtype_robot || viewType == ZhiChiConstant.bottomViewtype_paidui) {
            //转人工常显 按钮状态（1、显示，0、不显示），开启后如果
            if (initModel.getShowTurnManualBtn() == 1) {
                boolean isShowTurnManualBtn = true;
                //仅在客户消息累计触发n次机器人未知回答后，常驻显示 0-关闭 1-开启
                //满足了 多少次后显示转人工按钮
                if (initModel.getIsManualBtnFlag() == 1 && showTimeVisiableCustomBtn < initModel.getManualBtnCount()) {
                    //开启了触发n次机器人未知回答后，但是没有到达配置的次数 不显示
                    isShowTurnManualBtn = false;
                }
                if (isShowTurnManualBtn) {
                    view = View.inflate(getSobotActivity(), R.layout.sobot_layout_lable, null);
                    TextView name = view.findViewById(R.id.sobot_lable_name);
                    ImageView icon = view.findViewById(R.id.sobot_lable_icon);
                    icon.setVisibility(View.VISIBLE);
                    //机器人模式，显示转人工
                    name.setText(R.string.sobot_transfer_to_customer_service);
                    icon.setImageResource(R.drawable.sobot_icon_transfer);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            doClickTransferBtn();
                        }
                    });
                }
            }
        } else if (viewType == ZhiChiConstant.bottomViewtype_customer) {
            if (isAddedMenu) {
                sobot_custom_menu_linearlayout.removeViewAt(0);
                isAddedMenu = false;
            }
            //智能转人工
        }
        if (null != view) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginRight = getResources().getDimensionPixelSize(R.dimen.sobot_layout_lable_margin_right);
            layoutParams.setMargins(0, 0, marginRight, 0);
            view.setLayoutParams(layoutParams);

            if (isAddedMenu) {
                sobot_custom_menu_linearlayout.removeViewAt(0);
            }
            sobot_custom_menu_linearlayout.addView(view, 0);
            sobot_custom_menu.setVisibility(View.VISIBLE);
            isAddedMenu = true;
        }
    }

    /**
     * 快捷菜单: 显示快捷菜单
     * opportunity 1:机器人，2:人工
     */
    private void showQuickMenu(int opportunity) {
        LogUtils.d("====showQuickMenu=" + opportunity);
        if (allQuickMenuModel == null || allQuickMenuModel.size() == 0) {
            hideQuickMenu();
        }
        QuickMenuModel quickMenuModel = allQuickMenuModel.get(opportunity);
        if (quickMenuModel == null) {
            quickMenuModel = allQuickMenuModel.get(quick_menu_all);
        }
        if (quickMenuModel != null) {
            menuPlanTriggerCount(quickMenuModel.getPlanId());
            List<QuickMenuItemModel> infoLists = quickMenuModel.getMenuConfigRespVos();
            if (!isActive()) {
                return;
            }
            if (!isAddedMenu) {
                sobot_custom_menu_linearlayout.removeAllViews();
            } else {
                sobot_custom_menu_linearlayout.removeViews(1, sobot_custom_menu_linearlayout.getChildCount() - 1);
            }

            if (infoLists != null && infoLists.size() > 0) {
                creatQuickMenu(infoLists);
                sobot_custom_menu.setVisibility(View.VISIBLE);
            } else {
                hideQuickMenu();
            }
        } else {
            hideQuickMenu();
        }

    }

    /**
     * 快捷菜单: 显示快捷菜单
     *
     * @param infoLists
     */
    private void creatQuickMenu(List<QuickMenuItemModel> infoLists) {
        int marginRight = getResources().getDimensionPixelSize(R.dimen.sobot_layout_lable_margin_right);
        for (int i = 0; i < infoLists.size(); i++) {
            final View view = View.inflate(getSobotActivity(), R.layout.sobot_layout_lable, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, marginRight, 0);
            TextView lableName = view.findViewById(R.id.sobot_lable_name);
            ImageView lableIcon = view.findViewById(R.id.sobot_lable_icon);
            //是否显示菜单图标:0-不展示,1-展示
            if (infoLists.get(i).getExhibit() == 1) {
                if (infoLists.get(i) != null && !StringUtils.isEmpty(infoLists.get(i).getMenuPicUrl())) {
                    lableIcon.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(getSobotActivity(), infoLists.get(i).getMenuPicUrl(), lableIcon);
                } else if (infoLists.get(i) != null && !TextUtils.isEmpty(infoLists.get(i).getIconMaterial())) {
                    lableIcon.setVisibility(View.VISIBLE);
                    SobotBitmapUtil.display(getSobotActivity(), infoLists.get(i).getIconMaterial(), lableIcon);
                }
            } else {
                lableIcon.setVisibility(View.GONE);
            }
            view.setLayoutParams(layoutParams);
            //最大宽度是气泡的宽度
            int msgMaxWidth = 0;
            if (info != null && info.isShowRightMsgFace()) {
                //带有客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth(getSobotActivity()) * 60 / 100;
            } else {
                //不带客服头像和昵称
                msgMaxWidth = ScreenUtils.getScreenWidth(getSobotActivity()) * 70 / 100;
            }
            lableName.setMaxWidth(msgMaxWidth);
            lableName.setText(infoLists.get(i).getMenuName());
            view.setTag(infoLists.get(i));
            sobot_custom_menu_linearlayout.addView(view);
            if (view.getTag() != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        quickMenuClick((QuickMenuItemModel) v.getTag());
                    }
                });
            }
        }
    }

    /**
     * 快捷菜单: 点击事件
     *
     * @param itemModel
     */
    private void quickMenuClick(QuickMenuItemModel itemModel) {
        //快捷菜单点击事件
        zhiChiApi.addQuickMenuTriggerCount(getSobotActivity(), initModel.getPartnerid(), itemModel.getId(), new StringResultCallBack() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(Exception e, String s) {

            }
        });
        hidePanelAndKeyboard(mPanelLayout);
        //"menuType": 6, //菜单类型:0.发消息、1.转人工、2.触发知识库、3.结束会话、4.满意度评价、5.留言、6.跳转链接
        if (itemModel.getMenuType() == 0) {
            //发消息
            sendQuickMenuMsg(itemModel.getId(), itemModel.getMenuName(), 5, 1);
        } else if (itemModel.getMenuType() == 1) {
            //转人工
            doClickTransferBtn();
        } else if (itemModel.getMenuType() == 2) {
            //触发知识库
            // "robotType": null, //知识库类型 0:内部知识库 1:机器人知识库
            if (itemModel.getRobotType() == 0 || itemModel.getRobotType() == 2) {
                sendQuickMenuMsg(itemModel.getId(), itemModel.getMenuName(), 4, 1);
            } else if (itemModel.getRobotType() == 1) {
                sendQuickMenuMsg(itemModel.getId(), itemModel.getMenuName(), 3, 0);
            }
        } else if (itemModel.getMenuType() == 3) {
            //结束会话
            onCloseMenuClick();
        } else if (itemModel.getMenuType() == 4) {
            //满意度评价
            btnSatisfaction();
        } else if (itemModel.getMenuType() == 5) {
            //留言
            startToPostMsgActivty(false);
        } else if (itemModel.getMenuType() == 6) {
            String url = itemModel.getLabelLink();
            if (itemModel.getParamFlag() == 1) {
                //是否开启参数拼接 0:关闭 1:开启
                StringBuilder stringBuilder = new StringBuilder(url);
                if (url.contains("?")) {
                    stringBuilder.append("&");
                } else {
                    stringBuilder.append("?");
                }
                stringBuilder.append("partnerid=");
                stringBuilder.append(TextUtils.isEmpty(info.getPartnerid()) ? "" : info.getPartnerid());
                stringBuilder.append("&multiparams=");
                stringBuilder.append(TextUtils.isEmpty(info.getMulti_params()) ? "" : info.getMulti_params());
                stringBuilder.append("&params=");
                stringBuilder.append(TextUtils.isEmpty(info.getParams()) ? "" : info.getParams());
                url = stringBuilder.toString();
            }
            LogUtils.d("url=" + url);
            if (SobotOption.hyperlinkListener != null) {
                SobotOption.hyperlinkListener.onUrlClick(url);
                return;
            }
            if (SobotOption.newHyperlinkListener != null) {
                //如果返回true,拦截;false 不拦截
                boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotActivity(), itemModel.getLabelLink() + "");
                if (isIntercept) {
                    return;
                }
            }
            Intent intent = new Intent(getSobotActivity(), WebViewActivity.class);
            intent.putExtra("url", url);
            getSobotActivity().startActivity(intent);
        }
    }

    /**
     * 快捷菜单: 发消息
     *
     * @param menuId            菜单id
     * @param menuName          菜单name
     * @param fromEnum          3:机器人知识库，4:内部知识库，5:快捷菜单发消息
     * @param fromQuickMenuType 0:正常显示，1：不显示顶踩、转人工，2：显示人工头像
     */
    private void sendQuickMenuMsg(String menuId, String menuName, int fromEnum, int fromQuickMenuType) {
        //创建message对象
        // 点击发出问题
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setContent(menuName);
        base.setSenderName(info.getUser_nick());
        base.setSenderFace(info.getFace());
        // 消息的转换
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsgType(ZhiChiConstant.message_type_text);
        answer.setMsg(base.getContent());
        base.setAnswer(answer);
        base.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (base.getId() == null || TextUtils.isEmpty(base.getId())) {
            String msgId = getMsgId();
            base.setId(msgId);
            base.setMsgId(getMsgId());
        }
        //如果当前模式是人工模式
        if (current_client_model == ZhiChiConstant.client_model_customService && customerState == CustomerState.Online) {
            fromQuickMenuType = 2;
        }
        // 通知Handler更新 我的消息ui
        sendTextMessageToHandler(base.getId(), base.getContent(), handler, 2, SEND_TEXT);
        if (current_client_model == ZhiChiConstant.client_model_customService && customerState == CustomerState.Online) {
            //调用人工接口发消息接口
            sendMessageWithLogic(base.getId(), base.getContent(), initModel, handler, current_client_model, 0, "", null);
        } else {
            //调用发送接口
            sendHttpRobotMessage(base.getId(), base.getContent(), initModel.getPartnerid(),
                    initModel.getCid(), fromEnum + "", handler, 1, menuId, info.getLocale(), fromQuickMenuType + "", null);
        }
    }

    @Override
    public void clickIssueItem(FaqDocRespVo faq, String tag) {
        //会话结束不能发送
        if (isSessionOver) {
            return;
        }
        //questionType 问题类型：0-单轮，1-多轮，2-内部知识库文章，3-内部知识库普通问题
        // 点击发出问题
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setContent(faq.getQuestionName());
        base.setSenderName(info.getUser_nick());
        base.setSenderFace(info.getFace());
        // 消息的转换
        ZhiChiReplyAnswer answer = new ZhiChiReplyAnswer();
        answer.setMsgType(ZhiChiConstant.message_type_text);
        answer.setMsg(base.getContent());
        base.setAnswer(answer);
        base.setSenderType(ZhiChiConstant.message_sender_type_customer);
        if (base.getId() == null || TextUtils.isEmpty(base.getId())) {
            String msgId = getMsgId();
            base.setId(msgId);
            base.setMsgId(getMsgId());
        }
        // 通知Handler更新 我的消息ui
        sendTextMessageToHandler(base.getId(), faq.getQuestionName(), handler, 2, SEND_TEXT);
        //调用发送接口 question传getFaqDocRelId，requestText传的是Content()
        if (current_client_model == ZhiChiConstant.client_model_customService && customerState == CustomerState.Online) {
            //调用人工接口发消息接口
            sendMessageWithLogic(base.getId(), base.getContent(), initModel, handler, current_client_model, 0, "", null);
        } else {
            //1-常见问题机器人知识库类型
            //2-常见问题内部知识库类型
            String fromEnum = "";
            if (faq.getFrom() == 1) {
                fromEnum = "1";
            } else if (faq.getFrom() == 2) {
                fromEnum = "2";
            }
            sendHttpRobotMessage(base.getId(), faq.getQuestionName(), initModel.getPartnerid(),
                    initModel.getCid(), fromEnum, handler, 1, faq.getFaqDocRelId(), info.getLocale(), "", null);
        }
    }

    public boolean dispatchkeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && mPanelLayout.getVisibility() != View.GONE) {
            KPSwitchConflictUtil.hidePanelAndKeyboard(mPanelLayout);
            return true;
        }
        return false;
    }


    /**
     * 底部多面板切换：根据输入框里的内容切换显示  发送按钮还是加号（更多方法）
     */
    private void resetBtnUploadAndSend() {
        if (et_sendmessage != null && !et_sendmessage.getText().toString().trim().isEmpty()) {
            cb_plus_view.setVisibility(View.GONE);
            btn_send_pic.setVisibility(View.VISIBLE);
        } else {
            btn_send_pic.setVisibility(View.GONE);
            if (getAddPlanMemuCount() > 0) {
                cb_plus_view.setVisibility(View.VISIBLE);
            } else {
                cb_plus_view.setVisibility(View.GONE);
            }
            cb_plus_view.setEnabled(true);
            cb_plus_view.setClickable(true);
            if (Build.VERSION.SDK_INT >= 11) {
                cb_plus_view.setAlpha(1f);
            }
        }
    }

    /**
     * 底部多面板切换：隐藏键盘和面板
     *
     * @param layout
     */
    public void hidePanelAndKeyboard(KPSwitchPanelLinearLayout layout) {
        if (layout != null) {
            layout.setVisibility(View.GONE);
        }
        cb_plus_view.setChecked(false);
        et_sendmessage.dismissPop();
        KPSwitchConflictUtil.hidePanelAndKeyboard(layout);
        doEmoticonBtn2Blur();
        currentPanelId = 0;
    }

    /**
     * 底部多面板切换：重置表情按钮的焦点键盘
     */
    public void resetEmoticonBtn() {
        String panelViewTag = getPanelViewTag(mPanelLayout);
        String instanceTag = CustomeViewFactory.getInstanceTag(mAppContext, cb_emoticon_view.getId());
        if (mPanelLayout.getVisibility() == View.VISIBLE && instanceTag.equals(panelViewTag)) {
            doEmoticonBtn2Focus();
        } else {
            doEmoticonBtn2Blur();
        }
    }

    /**
     * 底部多面板切换：使表情按钮获取焦点
     */
    public void doEmoticonBtn2Focus() {
        cb_emoticon_view.setChecked(true);
    }

    /**
     * 底部多面板切换：使表情按钮失去焦点
     */
    public void doEmoticonBtn2Blur() {
        cb_emoticon_view.setChecked(false);
    }

    /**
     * 底部多面板切换：显示软键盘
     */
    public void showKeyboardHindPanel() {
        //获取系统键盘的高度
        int panel_hight = KeyboardUtil.getKeyboardHeight(getSobotActivity());
        //设置panel的高度
        ViewUtil.refreshHeight(mPanelLayout, panel_hight);
//        panel里的view设置成View.INVISIBLE
//        显示软键盘
//        validPanelHeight = KeyboardUtil.getValidPanelHeight(v.getContext());
//        ViewUtil.refreshHeight(panelLayout, validPanelHeight);
    }

    /**
     * 底部多面板切换：显示表情
     */
    public void showEmoHindKeyboard() {
        //获取表情的高度
        //设置panel的高度
//        隐藏软键盘
//        validPanelHeight = KeyboardUtil.getValidPanelHeight(v.getContext());
//        ViewUtil.refreshHeight(panelLayout, validPanelHeight);
    }

    /**
     * 底部多面板切换：显示多功能菜单
     */
    public void showPlusMenuHindKeyboard() {
        //获取菜单的高度
        //获取菜单的高度
        int panel_height = 0;
        bottomMenuLines = getAddPlanMemuCount() > 3 ? 2 : 1;
        try {
            //获取菜单面板高度
            panel_height = getAddPlanMemuHeiht();
        } catch (Exception e) {
            if (bottomMenuLines == 1) {
                panel_height = getResources().getDimensionPixelSize(R.dimen.sobot_chat_plus_one_line_height);
            } else {
                panel_height = getResources().getDimensionPixelSize(R.dimen.sobot_chat_plus_two_line_height);
            }
        }
        //设置panel的高度
        ViewUtil.refreshHeight(mPanelLayout, panel_height);
        //隐藏软键盘

//        validPanelHeight = KeyboardUtil.getValidPanelHeight(v.getContext());
//        ViewUtil.refreshHeight(panelLayout, validPanelHeight);
    }

    /**
     * 底部多面板切换：当屏幕分屏/多窗口变化时回调
     *
     * @param isInMultiWindowMode
     */
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        KPSwitchConflictUtil.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPanelLayout != null) {
            hidePanelAndKeyboard(mPanelLayout);
        }
    }

    /**
     * 打开评价对话框
     *
     * @param context
     * @param isSessionOver            当前会话是否结束
     * @param isFinish                 评价完是否关闭
     * @param isExitCommit             评价完是否结束会话
     * @param initModel                初始化信息
     * @param current_model            评价对象
     * @param commentType              commentType 评价类型 主动评价1 邀请评价0
     * @param isBackShowEvaluate       弹出评价窗 是否显示暂不评价（暂不评价和关闭图片只能显示一个）  true 是 false 否
     * @param canBackWithNotEvaluation 是否是返回时弹出评价窗  true 是 false 否
     */
    public Intent showEvaluateDialog(Activity context, boolean isSessionOver, boolean isFinish, boolean isExitCommit, ZhiChiInitModeBase
            initModel, int current_model, int commentType, String customName, int scroe, int isSolve, String checklables, boolean isBackShowEvaluate, boolean canBackWithNotEvaluation) {
        if (initModel == null) {
            return null;
        }
        Intent intent = new Intent(context, SobotEvaluateActivity.class);
        intent.putExtra("score", scroe);
        intent.putExtra("isSessionOver", isSessionOver);
        intent.putExtra("isFinish", isFinish);
        intent.putExtra("isExitSession", isExitCommit);
        intent.putExtra("initModel", initModel);
        intent.putExtra("current_model", current_model);
        intent.putExtra("commentType", commentType);
        intent.putExtra("customName", customName);
        intent.putExtra("isSolve", isSolve);
        intent.putExtra("checklables", checklables);
        intent.putExtra("isBackShowEvaluate", isBackShowEvaluate);
        intent.putExtra("canBackWithNotEvaluation", canBackWithNotEvaluation);
        return intent;
    }

    public void showAiEvaluateDialog(final boolean isSessionOver, final boolean isFinish, final boolean isExitCommit, final ZhiChiInitModeBase
            initModel, final int current_model, final int commentType, final String customName, final int scroe, final int isSolve, final String checklables, final boolean isBackShowEvaluate, final boolean canBackWithNotEvaluation) {
        if (initModel == null) {
            return;
        }
        zhiChiApi.aiIsComment(getSobotActivity(), initModel.getCid(), initModel.getPartnerid(), new StringResultCallBack() {
            @Override
            public void onSuccess(Object o) {
                //大模型机器人评价
                Intent intent = new Intent(getSobotActivity(), SobotAIEvaluateActivity.class);
                intent.putExtra("score", scroe);
                intent.putExtra("isSessionOver", isSessionOver);
                intent.putExtra("isFinish", isFinish);
                intent.putExtra("isExitSession", isExitCommit);
                intent.putExtra("initModel", initModel);
                intent.putExtra("current_model", current_model);
                intent.putExtra("commentType", commentType);
                intent.putExtra("customName", customName);
                intent.putExtra("isSolve", isSolve);
                intent.putExtra("checklables", checklables);
                intent.putExtra("isBackShowEvaluate", isBackShowEvaluate);
                intent.putExtra("canBackWithNotEvaluation", canBackWithNotEvaluation);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (SobotStringUtils.isNoEmpty(des)) {
                    showHint(des);
                    if (isExitCommit) {
                        //右上角点击关闭，暂不评价 ，结束会话，在返回
                        userOffline(initModel);
                        ChatUtils.userLogout(mAppContext, "弹窗评价后点击暂不评价 结束会话");
                    }
                    if (isFinish) {
                        finish();
                    }
                }
            }
        });
    }

    /**
     * 点击自定义卡片按钮
     *
     * @param menu
     */
    @Override
    public void clickCardMenu(SobotChatCustomMenu menu) {
        String menuTip = "";
        if (menu.getMenuType() == 1) {
            menuTip = menu.getMenuTip();
        }
        zhiChiApi.insertClickCardToSessionRecord(getSobotActivity(), initModel.getCid(), initModel.getPartnerid(), menu, new StringResultCallBack() {
            @Override
            public void onSuccess(Object o) {
                LogUtils.d("请求成功");
            }

            @Override
            public void onFailure(Exception e, String s) {
                LogUtils.d("请求成功");
            }
        });
        if (!TextUtils.isEmpty(menuTip)) {
            //显示系统消息 49
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            String msgId = getMsgId();
            base.setMsgId(msgId);
            base.setAction(ZhiChiConstant.action_card_mind_msg);
            base.setMsg(menuTip);
            updateUiMessage(messageAdapter, base);
        }
        //回调
        if (TextUtils.isEmpty(menu.getMenuLink())) {
            LogUtils.i("自定义卡片跳转链接为空，不跳转，不拦截");
            return;
        }
        if (SobotOption.hyperlinkListener != null) {
            SobotOption.hyperlinkListener.onUrlClick(menu.getMenuLink());
            return;
        }

        if (SobotOption.newHyperlinkListener != null) {
            //如果返回true,拦截;false 不拦截
            boolean isIntercept = SobotOption.newHyperlinkListener.onUrlClick(getSobotActivity(), menu.getMenuLink());
            if (isIntercept) {
                return;
            }
        }
        Intent intent = new Intent(getSobotActivity(), WebViewActivity.class);
        intent.putExtra("url", menu.getMenuLink());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 点击卡片的发送按钮
     *
     * @param menu
     * @param card
     */
    @Override
    public void sendCardMsg(SobotChatCustomMenu menu, SobotChatCustomCard card) {
        //如果会话结束了，不能发送
        if (isSessionOver) {
            return;
        }
        String menuTip = "";
        if (menu != null && menu.getMenuType() == 1) {
            menuTip = menu.getMenuTip();
        }
        //调用发送
        if (current_client_model == ZhiChiConstant.client_model_customService) {
            //发送给人工
            String msgId = getMsgId() + "";
            ZhiChiMessageBase messageBase = ChatUtils.getCustomerCard(initModel.getReadFlag(), msgId, card, info, initModel);
            if (messageBase != null) {
                sendNewMsgToHandler(messageBase, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
                sendMsgToCustomService(handler, SobotGsonUtil.beanToJson(card), "28", msgId, messageBase);
            }
        } else {
            zhiChiApi.insertClickCardToSessionRecord(getSobotActivity(), initModel.getCid(), initModel.getPartnerid(), menu, new StringResultCallBack() {
                @Override
                public void onSuccess(Object o) {
                    LogUtils.d("请求成功");
                }

                @Override
                public void onFailure(Exception e, String s) {
                    LogUtils.d("请求成功");
                }
            });
            //发送给机器人
            String msgId = getMsgId();
            ZhiChiMessageBase messageBase = ChatUtils.getCustomerCard(initModel.getAdminReadFlag(), msgId, card, info, initModel);
            if (messageBase != null) {
                sendNewMsgToHandler(messageBase, handler, ZhiChiConstant.MSG_SEND_STATUS_LOADING);
                String customCardQuestion = "";
                if (card.getCustomCards() != null && card.getCustomCards().size() > 0) {
                    for (SobotChatCustomGoods goods :
                            card.getCustomCards()) {
                        if (!TextUtils.isEmpty(goods.getCustomCardQuestion())) {
                            customCardQuestion = goods.getCustomCardQuestion();
                            break;
                        }
                    }
                }
                if (TextUtils.isEmpty(customCardQuestion)) {
                    customCardQuestion = SobotGsonUtil.beanToJson(card);
                }
                sendHttpRobotMessage("28", messageBase.getMsgId(), SobotGsonUtil.beanToJson(card), initModel.getPartnerid(),
                        initModel.getCid(), "", handler, 0, customCardQuestion, info.getLocale(), "", null);
            }
        }
        if (!TextUtils.isEmpty(menuTip)) {
            //显示系统消息 49
            ZhiChiMessageBase base = new ZhiChiMessageBase();
            String msgId = getMsgId();
            base.setMsgId(msgId);
            base.setAction(ZhiChiConstant.action_card_mind_msg);
            base.setMsg(menuTip);
            updateUiMessage(messageAdapter, base);

        }
    }

    int errorNum = 0;

    private void requestEvaluateConfig(final boolean sendMsg, final ZhiChiPushMessage pushMessage) {
        zhiChiApi.satisfactionMessage(getSobotActivity(), initModel.getPartnerid(), new ResultCallBack<SatisfactionSet>() {
            @Override
            public void onSuccess(SatisfactionSet satisfactionSet) {
                if (satisfactionSet != null) {
                    mSatisfactionSet = satisfactionSet;
                    errorNum = 0;
                    if (sendMsg) {
                        //请求成功后，显示消息
                        // 满足评价条件，并且之前没有评价过的话 才能 弹评价框
                        ZhiChiMessageBase customEvaluateMode = ChatUtils.getCustomEvaluateMode(getSobotActivity(), pushMessage, mSatisfactionSet);
                        // 更新界面的操作
                        updateUiMessage(messageAdapter, customEvaluateMode);
                        gotoLastItem();
                    }
                } else if (sendMsg) {
                    //失败后
                    errorNum++;
                    if (errorNum < 2) {
                        requestEvaluateConfig(sendMsg, pushMessage);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (sendMsg) {
                    //失败了
                    errorNum++;
                    if (errorNum < 2) {
                        requestEvaluateConfig(sendMsg, pushMessage);
                    }
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
            }
        });
    }

    /**
     * 获取大模型机器人赞踩配置
     */
    private void getAiRobotRealuateConfigInfo(final boolean isSend, final String msgId, final String cid, final String aiAgentCid) {
        if (zhiChiApi == null || initModel == null) {
            return;
        }
        zhiChiApi.getAiRobotRealuateConfigInfo(getSobotActivity(), initModel.getPartnerid(), initModel.getRobotid(), new SobotResultCallBack<SobotAiRobotRealuateConfigInfo>() {
            @Override
            public void onSuccess(SobotAiRobotRealuateConfigInfo sobotAiRobotRealuateConfigInfo) {
                if (sobotAiRobotRealuateConfigInfo != null) {
                    aiRobotRealuateConfigInfo = sobotAiRobotRealuateConfigInfo;
                    SharedPreferencesUtil.saveObject(mAppContext,
                            ZhiChiConstant.sobot_last_current_airobotrealuateconfiginfo, aiRobotRealuateConfigInfo);
                    if (isSend) {
                        showAIRobotTipMsg(msgId, cid, aiAgentCid);
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {

            }
        });
    }

    @Override
    public void checkUnReadMsg() {
        for (int i = 0; i < messageList.size(); i++) {
            ZhiChiMessageBase messageBase = messageList.get(i);
            if ((!TextUtils.isEmpty(messageBase.getMsgId())) && messageBase.getReadStatus() == 1) {
                if (messageBase.getSenderType() == ZhiChiConstant.message_sender_type_service) {
                    unReadMsgIds.put(messageBase.getMsgId(), messageBase);
                } else if (messageBase.getSenderType() == ZhiChiConstant.message_sender_type_robot && isOpenUnread) {
                    unReadMsgIds.put(messageBase.getMsgId(), messageBase);
                }
            }
        }
    }

    /**
     * 机器人自动转人工时，判断是否显示转人工提示语
     */
    private void showTransferPrompt() {
        //是否显示转人工提示语
        if (initModel.getTransferManualPromptFlag() == 1) {
            ZhiChiMessageBase robot = ChatUtils.getRobotTransferTip(initModel);
            messageAdapter.justAddData(robot);
        }
    }

    /**
     * 请求点踩配置
     *
     * @param isSend 请求完是否需要发送
     */
    private void requestRealuateConfig(final boolean isSend, final String docMsgId, final String cid) {
        if (initModel.getRealuateInfoFlag() == 1) {
            zhiChiApi.getRobotRealuateConfigInfo(getSobotActivity(), initModel.getPartnerid(), initModel.getRobotid() + "", new StringResultCallBack<SobotRealuateConfigInfo>() {
                @Override
                public void onSuccess(SobotRealuateConfigInfo o) {
                    if (o != null && o.getRealuateInfoFlag() > 0) {
                        mRealuateConfig = o;
                        if (isSend) {
                            sendRealuateConfig(docMsgId, cid);
                        }
                    } else {
                        mRealuateConfig = null;
                    }
                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }

    /**
     * 点踩--显示点踩问答消息
     */
    private void sendRealuateConfig(String docMsgId, String cid) {
        if (mRealuateConfig != null) {
            final SobotRealuateInfo realuateInfo = new SobotRealuateInfo();
            final String msgId = getMsgId();
            realuateInfo.setMsgId(msgId);
            realuateInfo.setMsg(mRealuateConfig.getRealuateAfterWord());
            realuateInfo.setChatRealuateConfigInfo(mRealuateConfig);
            realuateInfo.setCid(cid);
            realuateInfo.setUid(initModel.getPartnerid());
            realuateInfo.setDocMsgId(docMsgId);
            realuateInfo.setType("insert");
            realuateInfo.setSubmitStatus(1);
            //执行提交接口
            zhiChiApi.robotRealuateOperation(getSobotActivity(), realuateInfo, new StringResultCallBack() {
                @Override
                public void onSuccess(Object o) {
                    //显示点踩问答信息
                    if (!TextUtils.isEmpty(mRealuateConfig.getRealuateAfterWord())) {
                        ZhiChiMessageBase base = new ZhiChiMessageBase();
                        base.setId(msgId);
                        base.setMsgId(msgId);
                        base.setSenderName(initModel.getRobotName());
                        base.setSender(initModel.getRobotName());
                        base.setSenderFace(initModel.getRobotLogo());
                        base.setSenderType(ZhiChiConstant.message_sender_type_system);
                        base.setRealuateInfo(realuateInfo);
                        base.setAction(ZhiChiConstant.action_cai_msg);
                        updateUiMessage(messageAdapter, base);
                    }
                }

                @Override
                public void onFailure(Exception e, String s) {

                }
            });
        }
    }

    /**
     * hoder 提交点踩问答
     *
     * @param realuateInfo
     */
    @Override
    public void submitCai(final String msgId, final SobotRealuateInfo realuateInfo) {
        realuateInfo.setType("submit");
        realuateInfo.setSubmitStatus(2);
        realuateInfo.setMsgId(msgId);
        if (TextUtils.isEmpty(realuateInfo.getUid())) {
            realuateInfo.setUid(initModel.getPartnerid());
        }
        zhiChiApi.robotRealuateOperation(getSobotActivity(), realuateInfo, new StringResultCallBack() {
            @Override
            public void onSuccess(Object o) {
                //1、隐藏表单
                messageAdapter.updateDataById(msgId, realuateInfo);
                //2、显示成功消息，自己发一条，系统回一条
                if (realuateInfo.getRealuateTag() != null) {
                    String msg = realuateInfo.getRealuateTag().getRealuateTag();
                    ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
                    String msgId = getMsgId();
                    myMessage.setId(msgId);
                    myMessage.setMsgId(msgId);
                    ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                    reply.setMsg(msg);
                    myMessage.setAnswer(reply);
                    myMessage.setSenderName(info.getUser_nick());
                    myMessage.setSenderFace(info.getFace());
                    myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                    myMessage.setSendSuccessState(1);
                    updateUiMessage(messageAdapter, myMessage);
                }
                if (!TextUtils.isEmpty(realuateInfo.getRealuateDetail())) {
                    String msg = realuateInfo.getRealuateDetail();
                    ZhiChiMessageBase myMessage = new ZhiChiMessageBase();
                    String msgId = getMsgId();
                    myMessage.setId(msgId);
                    myMessage.setMsgId(msgId);
                    ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                    reply.setMsg(msg);
                    myMessage.setAnswer(reply);
                    myMessage.setSenderName(info.getUser_nick());
                    myMessage.setSenderFace(info.getFace());
                    myMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                    myMessage.setSendSuccessState(1);
                    updateUiMessage(messageAdapter, myMessage);
                }

                //3、显示系统消息
                ZhiChiMessageBase robotMessage = new ZhiChiMessageBase();
                String msgId = getMsgId();
                robotMessage.setId(msgId);
                robotMessage.setMsgId(msgId);
                robotMessage.setSenderName(initModel.getRobotName());
                robotMessage.setSender(initModel.getRobotName());
                robotMessage.setSenderFace(initModel.getRobotLogo());
                robotMessage.setSenderType(ZhiChiConstant.message_sender_type_robot);
                ZhiChiReplyAnswer robotReply = new ZhiChiReplyAnswer();
                robotReply.setMsg(realuateInfo.getChatRealuateConfigInfo().getRealuateSubmitWord());
                robotReply.setMsgType(ZhiChiConstant.message_type_text);
                robotMessage.setAnswer(robotReply);
                updateUiMessage(messageAdapter, robotMessage);

            }

            @Override
            public void onFailure(Exception e, String s) {

            }
        });
    }

    @Override
    public void submitAiRobotCai(final ZhiChiMessageBase message, final SobotAiRobotRealuateInfo realuateInfo) {
        if (message != null && realuateInfo != null) {
            //大模型点踩 原因卡片提交
            SobotAiRobotAnswerCommontParams commontParams = new SobotAiRobotAnswerCommontParams();
            commontParams.setUid(initModel.getPartnerid());
            commontParams.setCid(message.getCid());
            //0:踩 1:赞
            commontParams.setStatus("0");
            commontParams.setAiAgentCid(message.getAiAgentCid());
            commontParams.setCompanyId(initModel.getCompanyId());
            commontParams.setSourceEnum("APP");
            commontParams.setRobotFlag(initModel.getReadFlag() + "");
            commontParams.setMsgId(message.getMsgId());
            if (realuateInfo.getRealuateTag() != null) {
                commontParams.setTagId(realuateInfo.getRealuateTag().getid());
                commontParams.setTagName(realuateInfo.getRealuateTag().getRealuateTag());
            }
            final String realuateDetail = realuateInfo.getRealuateDetail();
            if (!TextUtils.isEmpty(realuateDetail)) {
                commontParams.setRealuateDetail(realuateDetail);
            }
            realuateInfo.setType("submit");
            realuateInfo.setSubmitStatus(2);
            if (aiRobotRealuateConfigInfo == null) {
                getAiRobotRealuateConfigInfo(false, "", "", "");
                LogUtils.i("配置信息为空，无法进行顶踩操作");
                return;
            } else {
                commontParams.setRealuateEvaluateWord(aiRobotRealuateConfigInfo.getRealuateEvaluateWord());
                commontParams.setRealuateSubmitWord(aiRobotRealuateConfigInfo.getRealuateSubmitWord());
                commontParams.setRealuateButtonStyle(aiRobotRealuateConfigInfo.getRealuateButtonStyle());
                commontParams.setRealuateFlag(aiRobotRealuateConfigInfo.getRealuateFlag());
                commontParams.setRealuateStyle(aiRobotRealuateConfigInfo.getRealuateStyle());
                commontParams.setRealuateInfoFlag(1);
            }
            zhiChiApi.aiAgentRobotAnswerComment(getSobotActivity(), commontParams, new StringResultCallBack<BaseListCodeV6>() {
                @Override
                public void onSuccess(BaseListCodeV6 data) {
                    if (!isActive()) {
                        return;
                    }
                    //1、隐藏表单
                    messageAdapter.updateDataById(message.getMsgId(), realuateInfo);
                    //2、显示成功消息，模拟用户发送消息：选中标签一条，输入建议一条
                    SobotAiRobotRealuateTag robotRealuateTag = realuateInfo.getRealuateTag();
                    if (robotRealuateTag != null) {
                        String realuateTagLan = robotRealuateTag.getRealuateTagLan();
                        ZhiChiMessageBase tagMessage = new ZhiChiMessageBase();
                        String msgId = getMsgId();
                        tagMessage.setId(msgId);
                        tagMessage.setMsgId(msgId);
                        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                        reply.setMsg(realuateTagLan);
                        tagMessage.setAnswer(reply);
                        tagMessage.setSenderName(info.getUser_nick());
                        tagMessage.setSenderFace(info.getFace());
                        tagMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                        tagMessage.setSendSuccessState(1);
                        updateUiMessage(messageAdapter, tagMessage);
                    }
                    if (!TextUtils.isEmpty(realuateDetail)) {
                        ZhiChiMessageBase detailMessage = new ZhiChiMessageBase();
                        String msgId = getMsgId();
                        detailMessage.setId(msgId);
                        detailMessage.setMsgId(msgId);
                        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
                        reply.setMsg(realuateDetail);
                        detailMessage.setAnswer(reply);
                        detailMessage.setSenderName(info.getUser_nick());
                        detailMessage.setSenderFace(info.getFace());
                        detailMessage.setSenderType(ZhiChiConstant.message_sender_type_customer);
                        detailMessage.setSendSuccessState(1);
                        updateUiMessage(messageAdapter, detailMessage);
                    }

                    //3、显示系统消息
                    ZhiChiMessageBase robotMessage = new ZhiChiMessageBase();
                    String msgId = getMsgId();
                    robotMessage.setId(msgId);
                    robotMessage.setMsgId(msgId);
                    robotMessage.setSenderName(initModel.getRobotName());
                    robotMessage.setSender(initModel.getRobotName());
                    robotMessage.setSenderFace(initModel.getRobotLogo());
                    robotMessage.setSenderType(ZhiChiConstant.message_sender_type_robot);
                    ZhiChiReplyAnswer robotReply = new ZhiChiReplyAnswer();
                    if (realuateInfo.getAiRobotRealuateConfigInfo() != null) {
                        robotReply.setMsg(realuateInfo.getAiRobotRealuateConfigInfo().getRealuateSubmitWordLan());
                    }
                    robotReply.setMsgType(ZhiChiConstant.message_type_text);
                    robotMessage.setAnswer(robotReply);
                    updateUiMessage(messageAdapter, robotMessage);
                }

                @Override
                public void onFailure(Exception e, String des) {
                }
            });
        }
    }

    //用户选中了某种语言的回调逻辑
    @Override
    public void chooseLangaue(SobotlanguaeModel sobotlanguaeModel, ZhiChiMessageBase messageBase) {
        if (sobotlanguaeModel != null) {
            showSelectLanguaeTipMessage(sobotlanguaeModel);
        }
        if (messageBase != null) {
            //移除选择语言的消息cell
            messageAdapter.removeByMsgId(StringUtils.checkStringIsNull(messageBase.getMsgId()));
        }
    }

    //显示用户选中的语言的系统提示语
    private void showSelectLanguaeTipMessage(final SobotlanguaeModel sobotlanguaeModel) {
        zhiChiApi.sendToAdminChooseLan(getSobotActivity(), initModel.getPartnerid(), sobotlanguaeModel.getCode(), new SobotResultCallBack<SobotlanguaeResultModel>() {
            @Override
            public void onSuccess(SobotlanguaeResultModel sobotlanguaeResultModel) {
                if (sobotlanguaeResultModel != null) {
                    String language = sobotlanguaeModel.getCode();
                    //覆盖数据
                    initModel.setAdminNonelineTitle(sobotlanguaeResultModel.getAdminNonelineTitle());
                    initModel.setManualCommentTitle(sobotlanguaeResultModel.getManualCommentTitle());
                    initModel.setRobotCommentTitle(sobotlanguaeResultModel.getRobotCommentTitle());
                    initModel.setServiceEndPushMsg(sobotlanguaeResultModel.getServiceEndPushMsg());
                    String oldLanguage = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
                    changeAppLanguage(language);
                    try {
                        if (StringUtils.isNoEmpty(oldLanguage) && StringUtils.isNoEmpty(sobotlanguaeModel.getCode()) && !oldLanguage.equals(sobotlanguaeModel.getCode()) && (("ar").equals(oldLanguage) || ("ar").equals(sobotlanguaeModel.getCode()))) {
                            //两次语言不一致，同时有一个是阿语 ar 才刷新
                            //保留当前界面状态，可通过 Intent 重新启动 Activity
                            long delayMillis = 400;
                            if (initModel.getSessionPhaseAndFaqIdRespVos() != null) {
                                delayMillis = 1500;
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (getSobotActivity() != null) {
                                        Intent intent = getSobotActivity().getIntent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        getSobotActivity().finish();
                                        startActivity(intent);
                                    }
                                }
                            }, delayMillis);
                        }
                    } catch (Exception e) {
                    }
                    showSelectLanguaeTip(sobotlanguaeModel);
                    //展示后续的逻辑
                    onInitResult(initModel);
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                //展示后续的逻辑
                onInitResult(initModel);
            }
        });
    }

    private void showSelectLanguaeTip(SobotlanguaeModel sobotlanguaeModel) {
        ZhiChiMessageBase base = new ZhiChiMessageBase();
        base.setT(Calendar.getInstance().getTime().getTime() + "");
        base.setId(getMsgId());
        base.setCid(initModel.getCid());
        base.setMsgId(getMsgId());
        base.setSenderType(ZhiChiConstant.message_sender_type_remide_info);
        ZhiChiReplyAnswer reply = new ZhiChiReplyAnswer();
        reply.setRemindType(ZhiChiConstant.sobot_remind_type_simple_tip);
        reply.setMsg(getResources().getString(R.string.sobot_change_language_zh).replaceAll("xxx", sobotlanguaeModel.getName()));
        base.setAnswer(reply);
        messageAdapter.addData(base);
    }

    //修改成缓存的语言
    public void changeAppLanguage() {
        Locale language = (Locale) SharedPreferencesUtil.getObject(getSobotActivity(), ZhiChiConstant.SOBOT_LANGUAGE);
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

    //修改成指定语言
    public void changeAppLanguage(String langaueCode) {
        if (StringUtils.isEmpty(langaueCode)) {
            return;
        }
        Locale locale = null;
        if ("he".equals(langaueCode)) {
            //添加sdk语言，设置成希伯来文
            locale = new Locale("iw");
        } else if ("zh-Hans".equals(langaueCode)) {
            //添加sdk语言，设置成中文
            locale = new Locale("zh");
        } else if ("zh-Hant".equals(langaueCode)) {
            //添加sdk语言，设置成中文繁体
            locale = new Locale("zh", "TW");
        } else {
            //添加sdk语言，设置成指定语言
            locale = new Locale(langaueCode);
        }
        SharedPreferencesUtil.saveBooleanData(getSobotActivity(), ZhiChiConstant.SOBOT_USE_LANGUAGE, true);
        SharedPreferencesUtil.saveObject(getSobotActivity(), ZhiChiConstant.SOBOT_LANGUAGE, locale);
        SharedPreferencesUtil.saveStringData(getSobotActivity(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, langaueCode);
        if (locale != null) {
            try {
                // 本地语言设置
                Resources res = getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = new Configuration();
                conf.locale = locale;
                res.updateConfiguration(conf, dm);


            } catch (Exception e) {
            }
        }
    }

    @Override
    public void chooseByAllLangaue(ArrayList<SobotlanguaeModel> sobotlanguaeModelList, ZhiChiMessageBase messageBase) {
        if (sobotlanguaeModelList != null) {
            Bundle bundle = new Bundle();
            Intent intent = new Intent(getSobotActivity(), SobotChooseLanguaeActivity.class);
            bundle.putSerializable("SobotlanguaeModelList", sobotlanguaeModelList);
            bundle.putSerializable("removeMsgId", messageBase != null ? StringUtils.checkStringIsNull(messageBase.getMsgId()) : "");
            intent.putExtras(bundle);
            startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_CHOOSE_LANGUAE);
        }
    }

    @Override
    public void unReadMsgIndex(int unReadMsgIndex) {
        //获取以下是未读消息的位置
        this.unReadMsgIndex = unReadMsgIndex;
    }

    int tmpMsgType = 0;
    Uri selectedImage;
    File selectedFile;
    private ChatUtils.SobotSendFileListener sendFileListener = new ChatUtils.SobotSendFileListener() {
        @Override
        public void onSuccess(final String filePath) {
            zhiChiApi.fileUploadForPostMsg(getActivity(), initModel.getCompanyId(), initModel.getPartnerid(), filePath, new ResultCallBack<ZhiChiMessage>() {
                @Override
                public void onSuccess(ZhiChiMessage zhiChiMessage) {
                    if (zhiChiMessage != null && zhiChiMessage.getData() != null) {
                        tempMsgContent = zhiChiMessage.getData().getUrl();
                        doClickTransferBtn();
                    }

                }

                @Override
                public void onFailure(Exception e, String des) {
                    showHint(TextUtils.isEmpty(des) ? getResources().getString(R.string.sobot_net_work_err) : des);
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {

                }
            });
        }

        @Override
        public void onError() {
            //发送失败
        }
    };

    /**
     * 新版询前表单
     *
     * @param param
     * @param tparam
     */
    public void requesetFormInfo(final SobotConnCusParam param, final SobotTransferOperatorParam tparam) {
        isHasRequestQueryFrom = true;
        String language = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
        zhiChiApi.findFormInfo(getSobotActivity(), initModel.getCid(), initModel.getPartnerid(), initModel.getCompanyId(), initModel.getInquiryPlanId(), language, new StringResultCallBack<FormInfoModel>() {
            @Override
            public void onSuccess(FormInfoModel formInfoModels) {
                if (formInfoModels != null) {
                    //打开询前表单
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(getSobotActivity(), SobotFormInfoActivity.class);
                    String FormExplain = "";
                    if (initModel.getFormAuthFlag() == 0 && SobotStringUtils.isNoEmpty(initModel.getFormExplain()) && initModel.getFormEffectiveScope().contains("2")) {
                        FormExplain = initModel.getFormExplain();
                    }
                    bundle.putString("FormExplain", FormExplain);
                    bundle.putString("cid", initModel.getCid());
                    bundle.putString("uid", initModel.getPartnerid());
                    bundle.putString("schemeId", initModel.getInquiryPlanId());
                    bundle.putSerializable("formInfoModels", formInfoModels);
                    bundle.putSerializable("param", param);
                    bundle.putSerializable("tparam", tparam);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, ZhiChiConstant.REQUEST_COCE_TO_FORMINFO);
                } else {
                    doTransfer2Custom(param, tparam, false);
                }
            }

            @Override
            public void onFailure(Exception e, String s) {
                doTransfer2Custom(param, tparam, false);
            }
        });
    }
}
