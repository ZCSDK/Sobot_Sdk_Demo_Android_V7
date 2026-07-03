package com.sobot.chat.activity.halfdialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.CommonModel;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SatisfactionSet;
import com.sobot.chat.api.model.SatisfactionSetBase;
import com.sobot.chat.api.model.SobotCommentParam;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 人工客服/机器人评价界面（半屏弹窗）
 * <p>
 * 该Activity用于在会话过程中或结束时，对机器人或人工客服进行满意度评价。
 * 支持以下功能：
 * <ul>
 *     <li>三种评分模式：五星评价（scoreFlag=0）、十分制评价（scoreFlag=1）、二级评价-满意/不满意（scoreFlag=2）</li>
 *     <li>"问题是否已解决"选项（可配置是否显示、是否必填）</li>
 *     <li>评价标签选择（支持多选，可配置是否必填）</li>
 *     <li>文字建议输入框（可配置是否显示、是否必填）</li>
 *     <li>主动评价（commentType=1）和邀请评价（commentType=0）两种场景</li>
 *     <li>"暂不评价"按钮（根据配置显示或隐藏）</li>
 * </ul>
 * <p>
 * 评价数据通过{@link ZhiChiApi#satisfactionMessage}接口获取评价配置（仅人工模式），
 * 评价提交通过{@link ZhiChiApi#comment}接口完成。
 * 评价结果通过本地广播{@link ZhiChiConstants#dcrc_comment_state}通知聊天界面。
 * <p>
 * 需要通过Intent传入以下参数：
 * <ul>
 *     <li>score - 默认评分</li>
 *     <li>isSessionOver - 当前会话是否已结束</li>
 *     <li>isFinish - 评价完成后是否关闭聊天界面</li>
 *     <li>isExitSession - 评价完成后是否退出会话</li>
 *     <li>initModel - 初始化数据模型{@link ZhiChiInitModeBase}</li>
 *     <li>current_model - 当前模式（机器人/人工客服）</li>
 *     <li>commentType - 评价类型（0=邀请评价，1=主动评价）</li>
 *     <li>customName - 客服名称（人工模式下显示）</li>
 *     <li>isSolve - 问题是否已解决的默认值（0=未解决，1=已解决，-1=未选择）</li>
 *     <li>checklables - 主动邀评时预选中的标签</li>
 *     <li>isBackShowEvaluate - 是否为返回时弹出的评价框</li>
 *     <li>canBackWithNotEvaluation - 是否允许暂不评价</li>
 * </ul>
 *
 * @see SobotDialogBaseActivity 基类，提供半屏弹窗样式
 * @see SobotAIEvaluateActivity 大模型机器人评价界面
 * @see SobotTicketEvaluateActivity 工单评价界面
 * Created by jinxl on 2017/6/12.
 */
public class SobotEvaluateActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    /**
     * 网络请求取消标识，用于在页面销毁时取消未完成的网络请求
     */
    private final String CANCEL_TAG = "SobotEvaluateActivity";

    /**
     * 当前评分值，五星制取值1-5，十分制取值0-10，二级评价5=满意/1=不满意
     */
    private int score;
    /**
     * 问题是否已解决：0=未解决，1=已解决，-1=未选择
     */
    private int isSolve = -1;
    /**
     * 评价完成后是否关闭聊天界面
     */
    private boolean isFinish;
    /**
     * 评价完成后是否退出当前会话
     */
    private boolean isExitSession;
    /**
     * 当前会话是否已结束，用于判断是否显示"提交后结束会话"提示
     */
    private boolean isSessionOver;
    /**
     * 是否允许暂不评价（显示"暂不评价"按钮）
     */
    private boolean canBackWithNotEvaluation;
    /**
     * 是否为用户点击返回时弹出的评价框
     */
    private boolean isBackShowEvaluate;
    /**
     * SDK初始化数据模型，包含cid、partnerId、robotId等关键信息
     */
    private ZhiChiInitModeBase initModel;
    /**
     * 用户配置信息，包含评价相关的显示/隐藏配置
     */
    private Information information;
    /**
     * 当前对话模式：机器人模式({@link ZhiChiConstant#client_model_robot})或人工客服模式({@link ZhiChiConstant#client_model_customService})
     */
    private int current_model;
    /**
     * 评价类型：0=邀请评价（客服发起），1=主动评价（用户发起）
     */
    private int commentType;
    /**
     * 人工客服名称，用于"xxx是否解决了您的问题？"的显示
     */
    private String customName;
    /**
     * 不同评分对应的配置列表（标签、输入框提示、分值说明等），从服务端获取
     */
    private List<SatisfactionSetBase> satisFactionList;
    /**
     * 评价总体配置信息，包含评分模式、是否显示问题解决选项、默认值等
     */
    private SatisfactionSet mSatisfactionSet;
    /**
     * 当前选中评分对应的配置，随评分变化动态更新
     */
    private SatisfactionSetBase satisfactionSetBase;

    // ==================== UI控件 ====================

    /**
     * 评价内容区域的根容器，加载评价配置前隐藏，加载成功后显示
     */
    private LinearLayout sobot_evaluate_container;
    private LinearLayout coustom_pop_layout;
    /**
     * 机器人评价区域布局（机器人模式下显示）
     */
    private LinearLayout sobot_robot_relative;
    /**
     * 人工客服评价区域布局（人工模式下显示）
     */
    private LinearLayout sobot_custom_relative;
    /**
     * 评价标签和建议输入的容器布局（选择未解决时或人工模式下显示）
     */
    private LinearLayout sobot_hide_layout;

    // ==================== 已解决/未解决选项 ====================
    /**
     * 已解决/未解决的按钮组容器
     */
    private LinearLayout sobot_readiogroup;
    /**
     * "已解决"按钮
     */
    private LinearLayout sobot_ll_ok_robot;
    /**
     * "未解决"按钮
     */
    private LinearLayout sobot_ll_no_robot;
    /**
     * "已解决"图标
     */
    private ImageView iv_solved;
    /**
     * "未解决"图标
     */
    private ImageView iv_no_solve;

    /**
     * 提交评价按钮
     */
    private TextView sobot_btn_submit;
    /**
     * 关闭按钮（右上角X）
     */
    private ImageView iv_closes;
    /**
     * 已解决/未解决选项与下方内容之间的分隔线
     */
    private View sobot_ratingBar_split_view;

    /**
     * 文字建议输入框
     */
    private EditText sobot_add_content;
    /**
     * 评价标题（如"服务评价"）
     */
    private TextView sobot_tv_evaluate_title;
    /**
     * "xxx是否解决了您的问题？"标题
     */
    private TextView sobot_robot_center_title;
    /**
     * 评价标签区域的提示标题（如"请选择您遇到的问题"）
     */
    private TextView sobot_text_other_problem;
    /**
     * 评分说明文字（如"非常满意"、"一般"等，随评分变化）
     */
    private TextView sobot_ratingBar_title;
    /**
     * "暂不评价"按钮
     */
    private TextView sobot_evaluate_cancel;
    /**
     * 评价提示文案（如"提交后将结束会话"）
     */
    private TextView sobot_tv_evaluate_title_hint;
    /**
     * 五星评分控件
     */
    private SobotFiveStarsLayout sobot_ratingBar;
    /**
     * 十分制评分的根布局
     */
    private LinearLayout sobot_ten_root_ll;
    /**
     * 十分制评分控件，动态添加0-10共11个评分项
     */
    private SobotTenRatingLayout sobot_ten_rating_ll;
    /**
     * 评分类型：
     * <ul>
     *     <li>0 - 五星评价</li>
     *     <li>1 - 十分制评价</li>
     *     <li>2 - 二级评价（满意/不满意）</li>
     * </ul>
     */
    private int ratingType;

    /**
     * 邀请评价时预选中的标签名称（逗号分隔），用于回显已选标签
     */
    private String evaluateChecklables;
    /**
     * 评价标签的自动换行布局容器
     */
    private SobotAntoLineLayout sobot_evaluate_lable_autoline;
    /**
     * 文字建议输入框的外层容器（带样式），控制整体显示/隐藏
     */
    private SobotEditTextLayout setl_submit_content;

    // ==================== 二级评价（满意/不满意）控件 ====================
    /**
     * 二级评价的整体容器
     */
    private LinearLayout ll_2_type;
    /**
     * "满意"按钮
     */
    private LinearLayout sobot_btn_satisfied;
    /**
     * "不满意"按钮
     */
    private LinearLayout sobot_btn_dissatisfied;
    /**
     * "满意"图标
     */
    private ImageView iv_satisfied;
    /**
     * "不满意"图标
     */
    private ImageView iv_dissatisfied;

    /**
     * 主题色，用于按钮和输入框的主题色渲染
     */
    private int themeColor;
    /**
     * 标签最大宽度，用于标签布局计算（屏幕宽度减去左右边距后的一半）
     */
    private int maxWidth;

    /**
     * 所有评价标签CheckBox的集合，用于遍历获取选中状态
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();

    /**
     * "xxx是否解决了您的问题？"的格式化字符串模板
     */
    private String sobot_question;

    /**
     * 弹窗根视图，点击外部区域可关闭弹窗
     */
    private View rootView;
    /**
     * 输入框获得焦点时的背景Drawable
     */
    private Drawable bgDrawable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 返回评价界面的布局资源ID
     */
    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_evaluate;
    }

    /**
     * 设置网络请求的取消标识，页面销毁时根据此标识取消未完成的请求
     */
    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotEvaluateActivity";
    }

    /**
     * 初始化视图：解析Intent参数、绑定控件、设置初始状态和监听器
     * <p>
     * 执行顺序：
     * 1. 从SharedPreferences获取用户配置信息
     * 2. 解析Intent传入的所有参数（评分、模式、评价类型等）
     * 3. findViewById绑定所有控件
     * 4. 根据配置设置"暂不评价"按钮的显示状态
     * 5. 调用{@link #setViewGone()}设置初始隐藏状态
     * 6. 调用{@link #setViewListener()}设置评分控件的监听器
     * 7. 处理全屏适配和输入框焦点样式
     */
    @Override
    protected void initView() {
        super.initView();
        // 从SharedPreferences获取用户配置信息（包含评价标签显示/隐藏等配置）
        information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");
        // 解析Intent传入的参数
        this.score = getIntent().getIntExtra("score", 5);
        this.isSessionOver = getIntent().getBooleanExtra("isSessionOver", false);
        this.isFinish = getIntent().getBooleanExtra("isFinish", false);
        this.isExitSession = getIntent().getBooleanExtra("isExitSession", false);
        this.initModel = (ZhiChiInitModeBase) getIntent().getSerializableExtra("initModel");
        this.current_model = getIntent().getIntExtra("current_model", 0);
        this.commentType = getIntent().getIntExtra("commentType", 0);
        this.customName = getIntent().getStringExtra("customName");
        this.isSolve = getIntent().getIntExtra("isSolve", -1);
        this.evaluateChecklables = getIntent().getStringExtra("checklables");
        this.isBackShowEvaluate = getIntent().getBooleanExtra("isBackShowEvaluate", false);
        this.canBackWithNotEvaluation = getIntent().getBooleanExtra("canBackWithNotEvaluation", false);

        rootView = findViewById(R.id.dialog_root);
        rootView.setOnClickListener(this);
        iv_closes = findViewById(R.id.iv_closes);
        iv_closes.setOnClickListener(this);
        sobot_evaluate_container = findViewById(R.id.sobot_evaluate_container);
        sobot_btn_submit = findViewById(R.id.sobot_close_now);
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        sobot_readiogroup = findViewById(R.id.sobot_readiogroup);
        sobot_tv_evaluate_title = (TextView) findViewById(R.id.sobot_tv_evaluate_title);
        //统一显示为服务评价
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        sobot_robot_center_title = (TextView) findViewById(R.id.sobot_robot_center_title);
        sobot_question = getResources().getString(R.string.sobot_question);
        sobot_robot_center_title.setText(String.format(sobot_question, "").trim());
        sobot_text_other_problem = (TextView) findViewById(R.id.sobot_text_other_problem);
        sobot_ratingBar_title = (TextView) findViewById(R.id.sobot_ratingBar_title);
        sobot_tv_evaluate_title_hint = (TextView) findViewById(R.id.sobot_tv_evaluate_title_hint);
        sobot_evaluate_cancel = (TextView) findViewById(R.id.sobot_evaluate_cancel);
        sobot_evaluate_cancel.setText(R.string.sobot_temporarily_not_evaluation);
        sobot_ratingBar_split_view = findViewById(R.id.sobot_ratingBar_split_view);

        ll_2_type = findViewById(R.id.ll_2_type);
        iv_satisfied = findViewById(R.id.iv_satisfied);
        iv_dissatisfied = findViewById(R.id.iv_dissatisfied);
        sobot_btn_satisfied = findViewById(R.id.sobot_btn_satisfied);
        sobot_btn_dissatisfied = findViewById(R.id.sobot_btn_dissatisfied);
        sobot_btn_satisfied.setOnClickListener(this);
        sobot_btn_dissatisfied.setOnClickListener(this);
        if (information != null && information.isCanBackWithNotEvaluation()) {
            sobot_evaluate_cancel.setVisibility(View.VISIBLE);
        } else {
            sobot_evaluate_cancel.setVisibility(View.GONE);
        }

        sobot_ratingBar = findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = findViewById(R.id.sobot_ten_root_ll);
        sobot_ten_rating_ll = findViewById(R.id.sobot_ten_rating_ll);

        sobot_evaluate_lable_autoline = findViewById(R.id.sobot_evaluate_lable_autoline);
        sobot_add_content = (EditText) findViewById(R.id.sobot_add_content);
        sobot_add_content.setHint(R.string.sobot_edittext_hint);
        sobot_ll_ok_robot = findViewById(R.id.sobot_ll_ok_robot);
        sobot_ll_no_robot = findViewById(R.id.sobot_ll_no_robot);
        sobot_ll_ok_robot.setOnClickListener(this);
        sobot_ll_no_robot.setOnClickListener(this);
        iv_solved = findViewById(R.id.iv_solved);
        iv_no_solve = findViewById(R.id.iv_no_solve);
        sobot_robot_relative = (LinearLayout) findViewById(R.id.sobot_robot_relative);
        sobot_custom_relative = (LinearLayout) findViewById(R.id.sobot_custom_relative);
        sobot_hide_layout = (LinearLayout) findViewById(R.id.sobot_hide_layout);
        setl_submit_content = (SobotEditTextLayout) findViewById(R.id.setl_submit_content);
        themeColor = ThemeUtils.getThemeColor(this);
        bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_line_4, null);
        setViewGone();
        setViewListener();
        if (ScreenUtils.isFullScreen(this)) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        maxWidth = (ScreenUtils.getScreenWidth(this) - ScreenUtils.dip2px(this, 40)) / 2;
        sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LogUtils.d("======是否失去焦点=====" + hasFocus);
                if (hasFocus) {
                    sobot_add_content.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, themeColor));
                } else {
                    sobot_add_content.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_evaluate_input, null));
                }
            }
        });
    }

    /**
     * 更新提交按钮的UI状态（可点击/不可点击）
     * <p>
     * 当用户未选择评分时按钮置灰不可点击，选择评分后恢复正常状态。
     * 按钮背景色会应用主题色，不可点击时透明度降低至90/255。
     *
     * @param isCanClick true=按钮可点击（正常状态），false=按钮不可点击（置灰状态）
     */
    private void changeCommitButtonUi(boolean isCanClick) {
        Drawable bg = sobot_btn_submit.getBackground();
        if (bg != null) {
            sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, themeColor));
        }
        sobot_btn_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        if (isCanClick) {
            sobot_btn_submit.setFocusable(true);
            sobot_btn_submit.setClickable(true);
            sobot_btn_submit.getBackground().setAlpha(255);
        } else {
            sobot_btn_submit.setFocusable(false);
            sobot_btn_submit.setClickable(false);
            sobot_btn_submit.getBackground().setAlpha(90);
        }
    }

    /**
     * 初始化数据：根据当前模式加载评价配置
     * <p>
     * 人工客服模式：调用{@link ZhiChiApi#satisfactionMessage}接口异步获取评价配置，
     * 配置包括评分模式（五星/十分/二级）、默认评分、标签列表、输入框配置等。
     * 获取成功后根据配置初始化各评分控件的显示状态。
     * <p>
     * 机器人模式：不需要请求服务端配置，直接使用默认UI，
     * 仅调整"已解决/未解决"按钮的等宽对齐，并应用主题色。
     */
    @Override
    protected void initData() {
        if (current_model == ZhiChiConstant.client_model_customService) {
            ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(this).getZhiChiApi();
            sobot_btn_submit.setVisibility(View.GONE);
            sobot_evaluate_container.setVisibility(View.GONE);
            zhiChiApi.satisfactionMessage(CANCEL_TAG, initModel.getPartnerid(), new ResultCallBack<SatisfactionSet>() {
                @Override
                public void onSuccess(SatisfactionSet satisfactionSet) {
                    sobot_btn_submit.setVisibility(View.VISIBLE);
                    sobot_evaluate_container.setVisibility(View.VISIBLE);
                    if (satisfactionSet != null) {
                        mSatisfactionSet = satisfactionSet;
                        satisFactionList = satisfactionSet.getList();
                        if (commentType == 1) {
                            //主动评价需要判断默认星级
                            if (satisfactionSet.getScoreFlag() == 0) {
                                //defaultType 0-默认5星,1-默认0星
                                score = (satisfactionSet.getDefaultType() == 0) ? 5 : 0;
                                sobot_ten_root_ll.setVisibility(View.GONE);
                                sobot_ratingBar.setVisibility(View.VISIBLE);
                                ll_2_type.setVisibility(View.GONE);
                                ratingType = 0;//5星
                            } else if (satisfactionSet.getScoreFlag() == 1) {
                                sobot_ten_root_ll.setVisibility(View.VISIBLE);
                                sobot_ratingBar.setVisibility(View.GONE);
                                ll_2_type.setVisibility(View.GONE);
                                ratingType = 1;//十分
                                //0-10分，1-5分，2-0分，3-不选中
                                if (satisfactionSet.getDefaultType() == 2) {
                                    score = 0;
                                } else if (satisfactionSet.getDefaultType() == 1) {
                                    score = 5;
                                } else if (satisfactionSet.getDefaultType() == 3) {
                                    score = -1;
                                } else {
                                    score = 10;
                                }
                            } else if (satisfactionSet.getScoreFlag() == 2) {
                                ratingType = 2;//二级
                                ll_2_type.setVisibility(View.VISIBLE);
                                sobot_ten_root_ll.setVisibility(View.GONE);
                                sobot_ratingBar.setVisibility(View.GONE);
                                //二级评价
                                //0-满意，1-不满意，2-不选中
                                if (satisfactionSet.getDefaultType() == 0) {
                                    score = 5;
                                } else if (satisfactionSet.getDefaultType() == 1) {
                                    score = 1;
                                } else if (satisfactionSet.getDefaultType() == 2) {
                                    score = -1;
                                }
                            }
                        } else {
                            if (satisfactionSet.getScoreFlag() == 0) {
                                //defaultType 0-默认5星,1-默认0星
                                sobot_ten_root_ll.setVisibility(View.GONE);
                                ll_2_type.setVisibility(View.GONE);
                                sobot_ratingBar.setVisibility(View.VISIBLE);
                                ratingType = 0;//5星
                            } else if (satisfactionSet.getScoreFlag() == 1) {
                                sobot_ten_root_ll.setVisibility(View.VISIBLE);
                                ll_2_type.setVisibility(View.GONE);
                                sobot_ratingBar.setVisibility(View.GONE);
                                ratingType = 1;//十分
                            } else if (satisfactionSet.getScoreFlag() == 2) {
                                ratingType = 2;//二级
                                sobot_ten_root_ll.setVisibility(View.GONE);
                                ll_2_type.setVisibility(View.VISIBLE);
                                sobot_ratingBar.setVisibility(View.GONE);
                            }
                        }
                        if (ratingType == 0) {
                            if (score == -1) {
                                score = 5;
                            }
                            sobot_ratingBar.init(score, true, 36);
                        } else if (ratingType == 1) {
                            sobot_ten_rating_ll.init(score, true, 16);
                        } else if (ratingType == 2) {
                            if (score == 5) {
                                //默认满意
//                                iv_satisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 43);
//                                iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 43);
//                                iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//                                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
//                                iv_dissatisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 35);
//                                iv_dissatisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//                                iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
                            } else if (score == 1) {
                                //默认不满意
//                                iv_satisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 43);
//                                iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 43);
//                                iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//                                iv_dissatisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 35);
//                                iv_dissatisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//                                iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
                                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

                            }
                        }

                        if (isSolve == -1) {
                            isSolve = satisfactionSet.getDefaultQuestionFlag();
                            //主动评价 问题是否解决 获取默认值 (0)-未解决 (1)-解决 (-1)-不选中
                        }
                        if (isSolve == 0) {
                            //(0)-未解决
                            if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                                sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
                                sobot_hide_layout.setVisibility(View.VISIBLE);
                                String tmpData[] = convertStrToArray(initModel.getRobotCommentTitle());
                                if (tmpData != null && tmpData.length > 0) {
                                    setLableViewVisible(tmpData);
                                } else {
                                    sobot_hide_layout.setVisibility(View.GONE);
                                    sobot_ratingBar_split_view.setVisibility(View.GONE);
                                }
                            }
                            iv_solved.setSelected(false);
                            iv_no_solve.setSelected(true);
                            sobot_ll_ok_robot.setSelected(false);
                            sobot_ll_no_robot.setSelected(true);
                        } else if (isSolve == 1) {
                            //(1)-解决
                            if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                                sobot_hide_layout.setVisibility(View.GONE);
                                sobot_ratingBar_split_view.setVisibility(View.GONE);
                            }
                            iv_solved.setSelected(true);
                            iv_no_solve.setSelected(false);
                            sobot_ll_ok_robot.setSelected(true);
                            sobot_ll_no_robot.setSelected(false);
                        }

                        setCustomLayoutViewVisible(score, satisFactionList);
                        if (ratingType == 0) {
                            if (0 == score) {
                                changeCommitButtonUi(false);
                                sobot_ratingBar_title.setVisibility(View.GONE);
//                                sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
//                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_text_third));
                            } else {
                                changeCommitButtonUi(true);
                                if (satisfactionSetBase != null) {
                                    sobot_ratingBar_title.setVisibility(View.VISIBLE);
                                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                                }
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_ten_evaluate_select));
                            }
                        } else if (ratingType == 1) {
                            if (-1 == score) {
                                changeCommitButtonUi(false);
                                sobot_ratingBar_title.setVisibility(View.GONE);
//                                sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
//                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_text_third));
                            } else {
                                changeCommitButtonUi(true);
                                if (satisfactionSetBase != null) {
                                    sobot_ratingBar_title.setVisibility(View.VISIBLE);
                                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                                }
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_ten_evaluate_select));
                            }
                        } else if (ratingType == 2) {
                            sobot_ratingBar_title.setVisibility(View.GONE);
                            if (-1 == score) {
                                changeCommitButtonUi(false);
//                                sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
//                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_text_third));
                            } else {
                                changeCommitButtonUi(true);
                                if (satisfactionSetBase != null) {
                                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                                }
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_ten_evaluate_select));
                            }
                        }
                        //1-开启 0-关闭
                        if (satisfactionSet.getIsQuestionFlag() == 1) {
                            sobot_robot_relative.setVisibility(View.VISIBLE);
                            //判断已解决 未解决长度是否相等
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    int width1 = sobot_ll_ok_robot.getMeasuredWidth();
                                    int width2 = sobot_ll_no_robot.getMeasuredWidth();
                                    sobot_ll_ok_robot.getPaddingStart();
                                    if (width1 < width2) {
                                        int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16);
                                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16));
                                        sobot_ll_ok_robot.setPadding(pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7));
                                    } else if (width1 > width2) {
                                        int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16);
                                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16));
                                        sobot_ll_no_robot.setPadding(pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7));
                                    }
                                }
                            });
                            sobot_readiogroup.setVisibility(View.VISIBLE);
                            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
                        } else {
                            sobot_robot_relative.setVisibility(View.GONE);
                            sobot_readiogroup.setVisibility(View.GONE);
                            sobot_ratingBar_split_view.setVisibility(View.GONE);
                        }

                        //是否是默认评价提示语
                        if (satisfactionSet.getIsDefaultGuide() == 0 && !TextUtils.isEmpty(satisfactionSet.getGuideCopyWriting())) {
                            sobot_tv_evaluate_title.setText(satisfactionSet.getGuideCopyWriting());
                        }
                        //是否显示评价输入框
                        if (satisfactionSet.getTxtFlag() == 0) {
                            //关闭评价输入框
                            setl_submit_content.setVisibility(View.GONE);
                        } else {
                            setl_submit_content.setVisibility(View.VISIBLE);
                        }
                        //是否是默认提交按钮
                        if (satisfactionSet.getIsDefaultButton() == 0 && !TextUtils.isEmpty(satisfactionSet.getButtonDesc())) {
                            sobot_btn_submit.setText(satisfactionSet.getButtonDesc());
                        }
                    }
                }

                @Override
                public void onFailure(Exception e, String des) {
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                }
            });
        } else {
            //判断已解决 未解决长度是否相等
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int width1 = sobot_ll_ok_robot.getMeasuredWidth();
                    int width2 = sobot_ll_no_robot.getMeasuredWidth();
                    if (width1 < width2) {
                        int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16));
                        sobot_ll_ok_robot.setPadding(pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7));
                    } else if (width1 > width2) {
                        int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotEvaluateActivity.this, 16));
                        sobot_ll_no_robot.setPadding(pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotEvaluateActivity.this, 7));
                    }
                }
            });
            //机器人模式下，默认选中已解决
            themeColor = ThemeUtils.getThemeColor(this);
            //定制化UI,纯UI显示，无逻辑
            Drawable bg = sobot_btn_submit.getBackground();
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, themeColor));
            }
            sobot_btn_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(this));
        }
    }

    /**
     * 设置评分控件的监听器
     * <p>
     * 包括：
     * - 五星评分控件的点击监听：选择星级后更新标签和提交按钮状态
     * - 十分制评分控件的点击监听：选择分值后更新标签和提交按钮状态
     * - 提交按钮、暂不评价按钮的点击监听
     */
    private void setViewListener() {
        sobot_ratingBar.setOnClickItemListener(new SobotFiveStarsLayout.OnClickItemListener() {
            @Override
            public void onClickItem(int selectIndex) {
                sobot_add_content.clearFocus();
                int score = selectIndex + 1;
                if (score > 5) {
                    score = 5;
                }
                if (score < 0) {
                    score = 0;
                }
                setCustomLayoutViewVisible(score, satisFactionList);
                sobot_btn_submit.setVisibility(View.VISIBLE);
                changeCommitButtonUi(true);
            }
        });

        sobot_btn_submit.setOnClickListener(this);

        sobot_evaluate_cancel.setOnClickListener(this);
        //监听10分评价选择变化
        if (sobot_ten_rating_ll != null) {
            sobot_ten_rating_ll.setOnClickItemListener(new SobotTenRatingLayout.OnClickItemListener() {
                @Override
                public void onClickItem(int selectIndex) {
                    sobot_btn_submit.setVisibility(View.VISIBLE);
                    changeCommitButtonUi(true);
                    setCustomLayoutViewVisible(selectIndex, satisFactionList);
                }
            });
        }

    }


    /**
     * 设置界面的初始显示状态
     * <p>
     * 根据当前对话模式（机器人/人工客服）设置不同的布局可见性：
     * - 机器人模式：显示机器人评价区域，隐藏人工客服区域，标题显示机器人名称
     * - 人工客服模式：显示人工客服区域，隐藏机器人区域，标题显示客服名称
     * - 统一隐藏标签区域和输入框（等待评价配置加载后再按需显示）
     */
    private void setViewGone() {
        sobot_hide_layout.setVisibility(View.GONE);
        setl_submit_content.setVisibility(View.GONE);
        sobot_evaluate_lable_autoline.removeAllViews();
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        if (current_model == ZhiChiConstant.client_model_robot) {
            sobot_robot_center_title.setText(String.format(sobot_question, initModel.getRobotName()));
            sobot_robot_relative.setVisibility(View.VISIBLE);
            sobot_custom_relative.setVisibility(View.GONE);
        } else {
            boolean isExitTalk = SharedPreferencesUtil.getBooleanData(SobotEvaluateActivity.this, ZhiChiConstant.SOBOT_CHAT_EVALUATION_COMPLETED_EXIT, false);
//            if (isExitTalk && !isSessionOver) {//设置了评价关闭且当前会话没有结束
//                sobot_tv_evaluate_title_hint.setText(R.string.sobot_evaluation_completed_exit);
//                sobot_tv_evaluate_title_hint.setVisibility(View.VISIBLE);
//            } else {
//                sobot_tv_evaluate_title_hint.setVisibility(View.GONE);
//            }
            sobot_robot_center_title.setText(String.format(sobot_question, customName));
            sobot_robot_relative.setVisibility(View.GONE);
            sobot_custom_relative.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 根据当前评分，更新评价标签、输入框、评分说明等UI元素的显示状态
     * <p>
     * 根据选中的评分值从配置列表中查找对应的配置项{@link SatisfactionSetBase}，
     * 然后依据该配置项：
     * - 设置评分说明文字（如"非常满意"）
     * - 根据txtFlag决定是否显示文字建议输入框
     * - 根据tags/labelName加载对应的评价标签
     * - 根据information配置决定是否隐藏评分说明和标签
     * - 如果配置了自定义的"问题是否解决"标题，则替换默认标题
     *
     * @param score            当前评分值
     * @param satisFactionList 所有评分配置列表
     */
    private void setCustomLayoutViewVisible(int score, List<SatisfactionSetBase> satisFactionList) {
        satisfactionSetBase = getSatisFaction(score, satisFactionList);
        for (int i = 0; i < checkBoxList.size(); i++) {
            checkBoxList.get(i).setChecked(false);
        }
        if (satisfactionSetBase != null) {
            sobot_ratingBar_title.setVisibility(View.VISIBLE);
            sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_ten_evaluate_select));
            if (satisfactionSetBase.getTxtFlag() == 1) {
                setl_submit_content.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(satisfactionSetBase.getInputLanguage())) {
                    if (satisfactionSetBase.getIsInputMust()) {
                        sobot_add_content.setHint(getResources().getString(R.string.sobot_required) + satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    } else {
                        sobot_add_content.setHint(satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    }
                } else {
                    sobot_add_content.setHint(getString(R.string.sobot_edittext_hint));
                }
            } else {
                //隐藏输入框
                setl_submit_content.setVisibility(View.GONE);
            }
            if (satisfactionSetBase.getTags() != null) {
                String[] tmpData = new String[satisfactionSetBase.getTags().size()];
                for (int i = 0; i < satisfactionSetBase.getTags().size(); i++) {
                    tmpData[i] = satisfactionSetBase.getTags().get(i).getLabelName();
                }
                setLableViewVisible(tmpData);
            } else if (!TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
                String tmpData[] = convertStrToArray(satisfactionSetBase.getLabelName());
                setLableViewVisible(tmpData);
            } else {
                setLableViewVisible(null);
            }
            //根据infomation 配置是否隐藏星星评价描述
            if (!information.isHideManualEvaluationLabels()) {
                sobot_ratingBar_title.setVisibility(View.VISIBLE);
            } else {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
            if (score == 5) {
                sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            }
            if (satisfactionSetBase.getScoreFlag() == 2) {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
            //是否自定义已解决标题
            if (satisfactionSetBase.getIsDefaultQuestion() == 0 && StringUtils.isNoEmpty(satisfactionSetBase.getQuestionCopywriting())) {
                sobot_robot_center_title.setText(satisfactionSetBase.getQuestionCopywriting());
            }
        } else {
            //根据infomation 配置是否隐藏星星评价描述
            if (!information.isHideManualEvaluationLabels()) {
                sobot_ratingBar_title.setVisibility(View.VISIBLE);
            } else {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 根据评分值从配置列表中查找对应的评价配置
     *
     * @param score            当前评分值
     * @param satisFactionList 评价配置列表
     * @return 匹配的配置项，未找到返回null
     */
    private SatisfactionSetBase getSatisFaction(int score, List<SatisfactionSetBase> satisFactionList) {
        if (satisFactionList == null) {
            return null;
        }
        for (int i = 0; i < satisFactionList.size(); i++) {
            if (satisFactionList.get(i).getScore().equals(score + "")) {
                return satisFactionList.get(i);
            }
        }
        return null;
    }

    /**
     * 在自动换行布局中动态创建评价标签CheckBox
     * <p>
     * 清空现有标签后，根据标签名称数组逐个创建CheckBox并添加到布局中。
     * 每个CheckBox使用sobot_layout_evaluate_item布局模板，并注册点击监听。
     * 创建的CheckBox同时加入{@link #checkBoxList}以便后续获取选中状态。
     *
     * @param antoLineLayout 自动换行布局容器
     * @param tmpData        标签名称数组
     */
    private void createChildLableView(SobotAntoLineLayout antoLineLayout, String tmpData[]) {
        if (antoLineLayout != null) {
            antoLineLayout.removeAllViews();
            for (int i = 0; i < tmpData.length; i++) {
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.sobot_layout_evaluate_item, null);
                CheckBox checkBox = view.findViewById(R.id.sobot_evaluate_cb_lable);
                //新版UI规范不要平均分的，显示不下换行
                checkBox.setText(tmpData[i]);
                checkBox.setOnClickListener(this);
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
            }
        }
    }


    /**
     * 设置评价标签区域的显示逻辑
     * <p>
     * 根据标签数据和当前模式（机器人/人工）以及information配置，
     * 决定标签区域的显示/隐藏：
     * - 标签数据为null时隐藏整个标签区域
     * - 机器人模式下根据isHideRototEvaluationLabels配置决定
     * - 人工模式下根据isHideManualEvaluationLabels配置决定
     * - 人工模式下还会设置标签区域的提示标题（tagTips）
     *
     * @param tmpData 标签名称数组，null表示无标签
     */
    private void setLableViewVisible(String tmpData[]) {
        if (tmpData == null) {
            sobot_hide_layout.setVisibility(View.GONE);
            return;
        } else {
            if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                //根据infomation 配置是否隐藏机器人评价标签
                if (!information.isHideRototEvaluationLabels()) {
                    sobot_hide_layout.setVisibility(View.VISIBLE);
                } else {
                    sobot_hide_layout.setVisibility(View.GONE);
                }
            }
            if (current_model == ZhiChiConstant.client_model_customService && initModel != null) {
                //根据infomation 配置是否隐藏人工评价标签
                if (!information.isHideManualEvaluationLabels()) {
                    sobot_hide_layout.setVisibility(View.VISIBLE);
                } else {
                    sobot_hide_layout.setVisibility(View.GONE);
                }
            }
            if (current_model == ZhiChiConstant.client_model_customService) {
                if (satisfactionSetBase != null) {
                    if (TextUtils.isEmpty(satisfactionSetBase.getTagTips())) {
                        sobot_text_other_problem.setVisibility(View.GONE);
                    } else {
                        sobot_text_other_problem.setVisibility(View.VISIBLE);
                        sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                    }
                }
            }
        }
        createChildLableView(sobot_evaluate_lable_autoline, tmpData);
        checkLable(tmpData);
    }

    /**
     * 获取当前"问题是否已解决"的选择值
     *
     * @return 0=未解决，1=已解决，-1=未选择
     */
    private int getResovled() {
        return isSolve;
    }

    /**
     * 构建评价提交参数对象
     * <p>
     * 收集当前界面上所有评价数据（评分、标签、建议文字、是否解决等），
     * 封装为{@link SobotCommentParam}对象用于提交评价接口。
     * <p>
     * 评分取值逻辑：
     * - 五星制：从sobot_ratingBar获取，取值1-5
     * - 十分制：从sobot_ten_rating_ll获取，取值0-10
     * - 二级评价：直接使用score变量（5=满意，1=不满意）
     *
     * @return 评价提交参数对象
     */
    private SobotCommentParam getCommentParam() {
        SobotCommentParam param = new SobotCommentParam();
        String type = current_model == ZhiChiConstant.client_model_robot ? "0" : "1";
        int tmpScore = 0;
        if (ratingType == 0) {
            param.setScoreFlag(0);//5星
            tmpScore = (int) Math.ceil(sobot_ratingBar.getSelectContent());
        } else if (ratingType == 1) {
            param.setScoreFlag(1);//10分
            tmpScore = sobot_ten_rating_ll.getSelectContent();
        } else if (ratingType == 2) {
            param.setScoreFlag(2);//二级
            tmpScore = score;
        }

        String suggest = sobot_add_content.getText().toString();
        param.setType(type);
        String problem = checkBoxIsChecked();
        param.setProblem(problem);
        if (satisfactionSetBase != null) {
            param.setScoreExplainLan(satisfactionSetBase.getScoreExplainLan());
            param.setScoreExplain(satisfactionSetBase.getScoreExplain());
        }
        param.setTagsJson(getCheckedLable());

        param.setSuggest(suggest);
        param.setIsresolve(getResovled());
        param.setCommentType(commentType);
        if (current_model == ZhiChiConstant.client_model_robot) {
            param.setRobotFlag(initModel.getRobotid() + "");
        } else {
            param.setScore(tmpScore + "");
        }
        return param;
    }

    /**
     * 提交评价入口方法
     * <p>
     * 先调用{@link #checkInput()}进行表单校验（评分、必填标签、必填建议），
     * 校验通过后调用{@link #comment()}提交评价数据到服务端。
     */
    private void subMitEvaluate() {
        if (!checkInput()) {
            return;
        }

        comment();
    }

    /**
     * 校验评价表单是否满足提交条件
     * <p>
     * 人工客服模式下的校验规则：
     * 1. 如果"问题是否解决"选项为必填（isQuestionMust=1），检查是否已选择
     * 2. 评分是否已选择（五星制不允许0分，十分制不允许未选择-1，二级评价不允许-1）
     * 3. 如果标签为必填（isTagMust=true），检查是否已选择标签
     * 4. 如果建议输入为必填（isInputMust=true），检查是否已填写
     * <p>
     * 机器人模式下直接返回true（无需校验）
     *
     * @return true=校验通过可提交，false=校验不通过（已Toast提示用户）
     */
    private boolean checkInput() {
        if (current_model == ZhiChiConstant.client_model_customService) {
            if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
                SobotCommentParam commentParam = getCommentParam();
                //“问题是否解决”是否为必填选项： 0-非必填 1-必填
                if (commentParam.getIsresolve() == -1 && mSatisfactionSet.getIsQuestionMust() == 1) {
                    ToastUtil.showToast(getApplicationContext(), getString(R.string.sobot_str_please_check_is_solve));//标签必选
                    return false;
                }
            }
            //评分是否未0
            int tmpScore = -1;
            if (ratingType == 0) {
                tmpScore = (int) Math.ceil(sobot_ratingBar.getSelectContent());
                //五星评价分不能传0
                if (tmpScore < 1) {
                    ToastUtil.showToast(SobotEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                    return false;
                }
            } else if (ratingType == 1) {
                tmpScore = sobot_ten_rating_ll.getSelectContent();
                //10分的评价分值可以传0，但不能不选
                if (tmpScore < 0) {
                    ToastUtil.showToast(SobotEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                    return false;
                }
            } else if (ratingType == 2) {
                tmpScore = score;
                //10分的评价分值可以传0，但不能不选
                if (tmpScore < 0) {
                    ToastUtil.showToast(SobotEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                    return false;
                }
            }

            if (satisfactionSetBase != null) {
                SobotCommentParam commentParam = getCommentParam();
                if (!TextUtils.isEmpty(satisfactionSetBase.getLabelName()) && satisfactionSetBase.getIsTagMust()) {
                    if (TextUtils.isEmpty(commentParam.getProblem()) && !information.isHideManualEvaluationLabels()) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                        return false;
                    }
                }

                if (satisfactionSetBase.getTxtFlag() == 1 && satisfactionSetBase.getIsInputMust()) {
                    if (TextUtils.isEmpty(commentParam.getSuggest().trim())) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_suggestions_are_required));//建议必填
                        return false;
                    }
                }
            }
        } else if (current_model == ZhiChiConstant.client_model_robot) {
            return true;
        }

        return true;
    }

    /**
     * 将逗号分隔的字符串转换为字符串数组
     *
     * @param str 逗号分隔的字符串（如"标签1,标签2,标签3"）
     * @return 拆分后的字符串数组，输入为空时返回null
     */
    private static String[] convertStrToArray(String str) {
        String[] strArray = null;
        if (!TextUtils.isEmpty(str)) {
            strArray = str.split(","); // 拆分字符为"," ,然后把结果交给数组strArray
        }
        return strArray;
    }

    /**
     * 调用评价提交接口
     * <p>
     * 通过{@link ZhiChiApi#comment}接口将评价数据提交到服务端。
     * 无论成功或失败，都会通过本地广播{@link ZhiChiConstants#dcrc_comment_state}
     * 通知聊天界面评价结果，广播携带以下参数：
     * - commentState: 是否提交成功
     * - isFinish: 是否关闭聊天界面
     * - isExitSession: 是否退出会话
     * - commentType: 评价类型
     * - score: 评分值
     * - isResolved: 问题是否已解决
     */
    private void comment() {
        final SobotCommentParam commentParam = getCommentParam();
        zhiChiApi.comment(CANCEL_TAG, initModel.getCid(), initModel.getPartnerid(), commentParam,
                new StringResultCallBack<CommonModel>() {
                    @Override
                    public void onSuccess(CommonModel result) {
                        //评论成功 发送广播
                        Intent intent = new Intent();
                        intent.setAction(ZhiChiConstants.dcrc_comment_state);
                        intent.putExtra("commentState", true);
                        intent.putExtra("isFinish", isFinish);
                        intent.putExtra("isExitSession", isExitSession);
                        intent.putExtra("commentType", commentType);
                        if (!TextUtils.isEmpty(commentParam.getScore())) {
                            intent.putExtra("score", Integer.parseInt(commentParam.getScore()));
                        }
                        intent.putExtra("isResolved", commentParam.getIsresolve());

                        CommonUtils.sendLocalBroadcast(SobotEvaluateActivity.this, intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception arg0, String msg) {
                        try {
                            ToastUtil.showToast(getApplicationContext(), msg);
                        } catch (Exception e) {
//                            LogUtils.e("uncaught", e);
                        }
                        //评论成功 发送广播
                        Intent intent = new Intent();
                        intent.setAction(ZhiChiConstants.dcrc_comment_state);
                        intent.putExtra("commentState", false);
                        intent.putExtra("isFinish", isFinish);
                        intent.putExtra("isExitSession", isExitSession);
                        intent.putExtra("commentType", commentType);
                        if (!TextUtils.isEmpty(commentParam.getScore())) {
                            intent.putExtra("score", Integer.parseInt(commentParam.getScore()));
                        }
                        intent.putExtra("isResolved", commentParam.getIsresolve());

                        CommonUtils.sendLocalBroadcast(SobotEvaluateActivity.this, intent);
                        finish();
                    }
                });
    }

    /**
     * 回显邀请评价时预选中的标签
     * <p>
     * 当邀请评价传入了预选标签（evaluateChecklables）时，
     * 遍历所有标签CheckBox，将包含在预选列表中的标签设为选中状态。
     *
     * @param tmpData 当前显示的标签名称数组
     */
    private void checkLable(String tmpData[]) {
        if (tmpData != null && tmpData.length > 0 && evaluateChecklables != null && !TextUtils.isEmpty(evaluateChecklables) && sobot_evaluate_lable_autoline != null) {
            for (int i = 0; i < tmpData.length; i++) {
                CheckBox checkBox = (CheckBox) sobot_evaluate_lable_autoline.getChildAt(i);
                if (checkBox != null) {
                    if (evaluateChecklables.contains(tmpData[i])) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }
                }
            }
        }
    }

    /**
     * 获取所有选中标签的名称，以逗号分隔
     *
     * @return 选中标签名称的逗号分隔字符串，无选中时返回空字符串
     */
    private String checkBoxIsChecked() {
        String str = "";
        for (int i = 0; i < checkBoxList.size(); i++) {
            if (checkBoxList.get(i).isChecked()) {
                str = str + checkBoxList.get(i).getText() + ",";
            }
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        return str + "";
    }

    /**
     * 获取选中标签的JSON数组字符串
     * <p>
     * 遍历所有选中的CheckBox，根据标签名称匹配配置中的标签数据，
     * 构建包含labelId、labelName、labelNameLan的JSON数组。
     * 用于提交评价时传递结构化的标签数据。
     *
     * @return 选中标签的JSON数组字符串，如[{"labelId":"1","labelName":"响应快","labelNameLan":"..."}]
     */
    private String getCheckedLable() {
        if (satisfactionSetBase != null && satisfactionSetBase.getTags() != null) {
            try {
                JSONArray array = new JSONArray();
                for (int i = 0; i < checkBoxList.size(); i++) {
                    if (checkBoxList.get(i).isChecked()) {
                        String str = checkBoxList.get(i).getText().toString();
                        for (int j = 0; j < satisfactionSetBase.getTags().size(); j++) {
                            if (str.equals(satisfactionSetBase.getTags().get(j).getLabelName())) {
                                JSONObject object = new JSONObject();
                                object.put("labelId", satisfactionSetBase.getTags().get(j).getLabelId());
                                object.put("labelName", satisfactionSetBase.getTags().get(j).getLabelName());
                                object.put("labelNameLan", satisfactionSetBase.getTags().get(j).getLabelNameLan());
                                array.put(object);
                                break;
                            }
                        }
                    }
                }
                String result = array.toString();
                return result;
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    /**
     * 分发触摸事件，实现点击输入框外部区域时隐藏软键盘
     */
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

    /**
     * 判断触摸事件是否发生在EditText输入框外部
     * <p>
     * 如果触摸点不在当前获焦的EditText区域内，则应隐藏软键盘。
     *
     * @param v     当前获得焦点的View
     * @param event 触摸事件
     * @return true=触摸在输入框外部（应隐藏键盘），false=触摸在输入框内部
     */
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

    public Activity getContext() {
        return SobotEvaluateActivity.this;
    }

    /**
     * 统一点击事件处理
     * <p>
     * 处理以下控件的点击：
     * - 满意/不满意按钮（二级评价模式）：切换图标选中状态，更新score
     * - 关闭按钮：直接关闭评价弹窗
     * - 已解决按钮：设置isSolve=1，机器人模式下隐藏标签区域
     * - 未解决按钮：设置isSolve=0，机器人模式下显示标签区域
     * - 提交按钮：调用{@link #subMitEvaluate()}提交评价
     * - 暂不评价按钮：如需关闭/退出则发送广播，然后关闭弹窗
     */
    @Override
    public void onClick(View v) {
        v.requestFocus();
        sobot_add_content.clearFocus();
        //隐藏软键盘
        hideKeyboard();
        if (v.getId() == R.id.sobot_btn_satisfied) {
            score = 5;
//            iv_satisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 43);
//            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 43);
//            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//            iv_dissatisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 35);
//            iv_dissatisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);

            changeCommitButtonUi(true);
            setCustomLayoutViewVisible(score, satisFactionList);
        } else if (v.getId() == R.id.sobot_btn_dissatisfied) {
//            iv_satisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 35);
//            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//            iv_dissatisfied.getLayoutParams().width = ScreenUtils.dip2px(getContext(), 43);
//            iv_dissatisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 43);
//            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

            score = 1;
            changeCommitButtonUi(true);
            setCustomLayoutViewVisible(score, satisFactionList);
        } else if (v.getId() == R.id.iv_closes) {
            finish();
        }
        if (v.getId() == R.id.sobot_ll_ok_robot) {
            //已解决
            isSolve = 1;
            if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                sobot_hide_layout.setVisibility(View.GONE);
                sobot_ratingBar_split_view.setVisibility(View.GONE);
            }
            // 获取系统默认的加粗字体
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_ok_robot.setSelected(true);
            sobot_ll_no_robot.setSelected(false);
        } else if (v.getId() == R.id.sobot_ll_no_robot) {
            //未解决
            isSolve = 0;
            if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
                sobot_hide_layout.setVisibility(View.VISIBLE);
                String tmpData[] = convertStrToArray(initModel.getRobotCommentTitle());
                if (tmpData != null && tmpData.length > 0) {
                    setLableViewVisible(tmpData);
                } else {
                    sobot_hide_layout.setVisibility(View.GONE);
                    sobot_ratingBar_split_view.setVisibility(View.GONE);
                }
            }
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_ok_robot.setSelected(false);
            sobot_ll_no_robot.setSelected(true);
        } else if (v == sobot_btn_submit) {
            //提交
            subMitEvaluate();
        } else if (v == sobot_evaluate_cancel) {
            //暂不评价
            sobot_add_content.clearFocus();
            //邀评的暂不评价，只返回，不发广播，其他暂不评价逻辑不动 /*commentType 评价类型 主动评价1 邀请评价0*/
            if (isFinish || isExitSession) {
                Intent intent = new Intent();
                intent.setAction(ZhiChiConstants.sobot_close_now);
                LogUtils.i("isExitSession:  " + isExitSession + "--------isFinish:   " + isFinish);
                intent.putExtra("isExitSession", isExitSession);
                CommonUtils.sendLocalBroadcast(SobotEvaluateActivity.this, intent);
            }
            finish();
        }
    }
}