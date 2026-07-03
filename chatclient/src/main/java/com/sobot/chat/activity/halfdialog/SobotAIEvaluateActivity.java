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
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotCommentParam;
import com.sobot.chat.api.model.SobotOrderEvaluateModel;
import com.sobot.chat.api.model.SobotOrderScoreModel;
import com.sobot.chat.api.model.SobotOrderTagModel;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型（AI Agent）机器人评价界面（半屏弹窗）
 * <p>
 * 该Activity专用于大模型机器人会话的满意度评价，与{@link SobotEvaluateActivity}的区别：
 * <ul>
 *     <li>不支持邀请评价场景，仅支持主动评价（commentType=1）和会话结束评价</li>
 *     <li>评价配置通过独立接口{@link ZhiChiApi#getAiSatisfactionTemplate}获取，基于模板ID</li>
 *     <li>评价提交通过{@link ZhiChiApi#aiAgentComment}接口完成</li>
 *     <li>使用{@link SobotOrderScoreModel}和{@link SobotOrderEvaluateModel}作为数据模型</li>
 *     <li>标签使用labelId进行选中状态管理（而非标签名称）</li>
 * </ul>
 * <p>
 * 支持三种评分模式：五星评价（scoreFlag=0）、十分制评价（scoreFlag=1）、二级评价-满意/不满意（scoreFlag=2）
 * <p>
 * 需要通过Intent传入以下参数：
 * <ul>
 *     <li>score - 默认评分</li>
 *     <li>isSessionOver - 当前会话是否已结束</li>
 *     <li>isFinish - 评价完成后是否关闭聊天界面</li>
 *     <li>isExitSession - 评价完成后是否退出会话</li>
 *     <li>initModel - 初始化数据模型{@link ZhiChiInitModeBase}，其中templateId用于获取评价模板</li>
 *     <li>commentType - 评价类型（0=邀请评价，1=主动评价）</li>
 *     <li>isSolve - 问题是否已解决的默认值（0=未解决，1=已解决，-1=未选择）</li>
 *     <li>isBackShowEvaluate - 是否为返回时弹出的评价框</li>
 *     <li>canBackWithNotEvaluation - 是否允许暂不评价</li>
 * </ul>
 * <p>
 * 评价结果通过本地广播{@link ZhiChiConstants#dcrc_comment_state}通知聊天界面。
 *
 * @see SobotDialogBaseActivity 基类，提供半屏弹窗样式
 * @see SobotEvaluateActivity 人工客服/机器人评价界面
 * @see SobotTicketEvaluateActivity 工单评价界面
 * Created by gqf on 2025/3/12.
 */
public class SobotAIEvaluateActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    /**
     * 网络请求取消标识，用于在页面销毁时取消未完成的网络请求
     */
    private final String CANCEL_TAG = "SobotAIEvaluateActivity";

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
     * SDK初始化数据模型，包含cid、partnerId、templateId、aiAgentCid等关键信息
     */
    private ZhiChiInitModeBase initModel;
    /**
     * 用户配置信息，包含评价相关的显示/隐藏配置
     */
    private Information information;
    /**
     * 评价类型：0=邀请评价（客服发起），1=主动评价（用户发起）
     */
    private int commentType;
    /**
     * 大模型评价模板ID，用于从服务端获取对应的评价配置
     */
    private String templateId;
    /**
     * 不同评分对应的配置列表（标签、输入框提示、分值说明等），从服务端模板获取
     */
    private List<SobotOrderScoreModel> satisFactionList;
    /**
     * 当前已选中的标签ID列表，用于提交评价时传递
     */
    private List<String> checkLables;
    /**
     * 大模型评价总体配置信息，包含评分模式、默认值、标签等
     */
    private SobotOrderEvaluateModel mSatisfactionSet;
    /**
     * 当前选中评分对应的配置，随评分变化动态更新
     */
    private SobotOrderScoreModel satisfactionSetBase;

    // ==================== UI控件 ====================

    /**
     * 评价内容区域的根容器，加载评价配置前隐藏，加载成功后显示
     */
    private LinearLayout sobot_evaluate_container;
    private LinearLayout coustom_pop_layout;
    /**
     * 机器人评价区域布局（大模型评价中不使用，固定隐藏）
     */
    private LinearLayout sobot_robot_relative;
    /**
     * 人工客服评价区域布局（大模型评价中复用此布局显示评价内容）
     */
    private LinearLayout sobot_custom_relative;
    /**
     * 评价标签和建议输入的容器布局
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
     * "xxx是否解决了您的问题？"的格式化字符串模板
     */
    private String sobot_question;

    /**
     * 所有评价标签CheckBox的集合，用于遍历获取选中状态
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 返回评价界面的布局资源ID，复用与普通评价相同的布局
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
        REQUEST_TAG = "SobotAIEvaluateActivity";
    }

    /**
     * 初始化视图：解析Intent参数、绑定控件、设置初始状态和监听器
     * <p>
     * 与{@link SobotEvaluateActivity#initView()}的区别：
     * - 不需要current_model参数（固定为大模型机器人）
     * - 不需要customName参数（使用机器人名称）
     * - 不需要evaluateChecklables参数（不支持邀请评价预选标签）
     * - 从initModel中获取templateId用于请求评价模板
     */
    @Override
    protected void initView() {
        super.initView();
        checkLables = new ArrayList<>();
        information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");
        this.score = getIntent().getIntExtra("score", 5);
        this.isSessionOver = getIntent().getBooleanExtra("isSessionOver", false);
        this.isFinish = getIntent().getBooleanExtra("isFinish", false);
        this.isExitSession = getIntent().getBooleanExtra("isExitSession", false);
        this.initModel = (ZhiChiInitModeBase) getIntent().getSerializableExtra("initModel");
        this.commentType = getIntent().getIntExtra("commentType", 0);
        this.isSolve = getIntent().getIntExtra("isSolve", -1);
        this.isBackShowEvaluate = getIntent().getBooleanExtra("isBackShowEvaluate", false);
        this.canBackWithNotEvaluation = getIntent().getBooleanExtra("canBackWithNotEvaluation", false);
        this.templateId = initModel.getTemplateId();
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
        setViewGone();
        setViewListener();
        if (ScreenUtils.isFullScreen(this)) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        Drawable bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_line_4, null);
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
     * 初始化数据：通过模板ID请求大模型评价配置
     * <p>
     * 调用{@link ZhiChiApi#getAiSatisfactionTemplate}接口获取评价模板配置。
     * 如果模板不存在（返回null），直接关闭评价界面。
     * <p>
     * 获取成功后的处理流程：
     * 1. 根据scoreFlag确定评分模式（五星/十分/二级）并初始化对应控件
     * 2. 根据commentType和defaultType计算默认评分值
     * 3. 根据defaultQuestionFlag设置"问题是否已解决"的默认状态
     * 4. 根据isQuestionFlag决定是否显示"问题是否已解决"选项
     * 5. 根据isDefaultGuide、txtFlag、isDefaultButton等配置自定义UI文案
     */
    @Override
    protected void initData() {
        ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(this).getZhiChiApi();
        sobot_btn_submit.setVisibility(View.GONE);
        sobot_evaluate_container.setVisibility(View.GONE);
        zhiChiApi.getAiSatisfactionTemplate(this, initModel.getCid(), initModel.getPartnerid(), templateId, new StringResultCallBack<SobotOrderEvaluateModel>() {
            @Override
            public void onSuccess(SobotOrderEvaluateModel satisfactionSet) {
                sobot_btn_submit.setVisibility(View.VISIBLE);
                sobot_evaluate_container.setVisibility(View.VISIBLE);
                if (satisfactionSet != null) {
                    mSatisfactionSet = satisfactionSet;
                    satisFactionList = satisfactionSet.getScoreInfo();
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
                            } else {
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
//                            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
//                            iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
//                            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//                            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
//                            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 35);
//                            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
                            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);

                        } else if (score == 1) {
                            //默认不满意
//                            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
//                            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//                            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//                            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
//                            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
//                            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
                            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

                        }
                    }

                    //主动评价 问题是否解决 获取默认值
                    if (satisfactionSet.getDefaultQuestionFlag() == 1) {
                        isSolve = 1;
                        //(1)-解决
                        iv_solved.setSelected(true);
                        sobot_ll_ok_robot.setSelected(true);
                        iv_no_solve.setSelected(false);
                        sobot_ll_no_robot.setSelected(false);
                    } else if (satisfactionSet.getDefaultQuestionFlag() == 0) {
                        isSolve = 0;
                        //(0)-未解决
                        iv_solved.setSelected(false);
                        iv_no_solve.setSelected(false);
                        sobot_ll_ok_robot.setSelected(true);
                        sobot_ll_no_robot.setSelected(true);
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
                                    int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16);
                                    LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16));
                                    sobot_ll_ok_robot.setPadding(pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7));
                                } else if (width1 > width2) {
                                    int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16);
                                    LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16));
                                    sobot_ll_no_robot.setPadding(pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7));
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
                } else {
                    //没有模板
                    LogUtils.d("=====大模型评价==没有模板====");
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if (null != des) {
                    LogUtils.d(des);
                    finish();
                }
            }

        });
    }

    /**
     * 设置评分控件和操作按钮的监听器
     * <p>
     * 包括：
     * - 五星评分控件的点击监听：选择星级后更新标签和提交按钮状态
     * - 十分制评分控件的点击监听：选择分值后更新标签和提交按钮状态
     * - 提交按钮：调用{@link #subMitEvaluate()}提交评价
     * - 暂不评价按钮：如需关闭/退出则发送广播，然后关闭弹窗
     */
    private void setViewListener() {
        sobot_ratingBar.setOnClickItemListener(new SobotFiveStarsLayout.OnClickItemListener() {
            @Override
            public void onClickItem(int selectIndex) {
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


        sobot_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subMitEvaluate();
            }
        });

        sobot_evaluate_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinish || isExitSession) {
                    Intent intent = new Intent();
                    intent.setAction(ZhiChiConstants.sobot_close_now);
                    LogUtils.i("isExitSession:  " + isExitSession + "--------isFinish:   " + isFinish);
                    intent.putExtra("isExitSession", isExitSession);
                    CommonUtils.sendLocalBroadcast(SobotAIEvaluateActivity.this, intent);
                }
                finish();
            }
        });
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
     * 大模型评价固定为机器人模式，因此：
     * - 隐藏标签区域和输入框（等待评价配置加载后再按需显示）
     * - 标题显示机器人名称
     * - 隐藏机器人评价区域，显示人工客服区域（复用其布局显示内容）
     * - 根据评价关闭配置决定是否显示"提交后将结束会话"提示
     */
    private void setViewGone() {
        sobot_hide_layout.setVisibility(View.GONE);
        setl_submit_content.setVisibility(View.GONE);
        sobot_evaluate_lable_autoline.removeAllViews();
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        boolean isExitTalk = SharedPreferencesUtil.getBooleanData(SobotAIEvaluateActivity.this, ZhiChiConstant.SOBOT_CHAT_EVALUATION_COMPLETED_EXIT, false);
        if (isExitTalk && !isSessionOver) {//设置了评价关闭且当前会话没有结束
            sobot_tv_evaluate_title_hint.setText(R.string.sobot_evaluation_completed_exit);
            sobot_tv_evaluate_title_hint.setVisibility(View.VISIBLE);
        } else {
            sobot_tv_evaluate_title_hint.setVisibility(View.GONE);
        }

        sobot_robot_center_title.setText(String.format(sobot_question, initModel.getRobotName()));
        sobot_robot_relative.setVisibility(View.GONE);
        sobot_custom_relative.setVisibility(View.VISIBLE);
    }

    /**
     * 根据当前评分，更新评价标签、输入框、评分说明等UI元素的显示状态
     * <p>
     * 根据选中的评分值从配置列表中查找对应的配置项{@link SobotOrderScoreModel}，
     * 然后依据该配置项：
     * - 设置评分说明文字（如"非常满意"）
     * - 根据总体配置的txtFlag决定是否显示文字建议输入框
     * - 根据tags加载对应的评价标签（使用{@link SobotOrderTagModel}结构）
     * - 根据information配置决定是否隐藏评分说明和标签
     * - 如果配置了自定义的"问题是否解决"标题，则替换默认标题
     *
     * @param score            当前评分值
     * @param satisFactionList 所有评分配置列表
     */
    private void setCustomLayoutViewVisible(int score, List<SobotOrderScoreModel> satisFactionList) {
        satisfactionSetBase = getSatisFaction(score, satisFactionList);
        for (int i = 0; i < checkBoxList.size(); i++) {
            checkBoxList.get(i).setChecked(false);
        }
        if (satisfactionSetBase != null) {
            sobot_ratingBar_title.setVisibility(View.VISIBLE);
            sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_ten_evaluate_select));
            if (mSatisfactionSet.getTxtFlag() == 1) {
                setl_submit_content.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(satisfactionSetBase.getInputLanguage())) {
                    if (satisfactionSetBase.getIsInputMust() == 1) {
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
            if (satisfactionSetBase.getTags() != null && satisfactionSetBase.getTags().size() > 0) {
                setLableViewVisible(satisfactionSetBase.getTags());
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
            if (mSatisfactionSet.getScoreFlag() == 2) {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
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
    private SobotOrderScoreModel getSatisFaction(int score, List<SobotOrderScoreModel> satisFactionList) {
        if (satisFactionList == null) {
            return null;
        }
        for (int i = 0; i < satisFactionList.size(); i++) {
            if (satisFactionList.get(i).getScore() == score) {
                return satisFactionList.get(i);
            }
        }
        return null;
    }

    /**
     * 在自动换行布局中动态创建评价标签CheckBox
     * <p>
     * 与{@link SobotEvaluateActivity# createChildLableView}的区别：
     * 使用{@link SobotOrderTagModel}列表，每个CheckBox的tag存储labelId，
     * 便于后续通过labelId进行选中状态管理和提交。
     *
     * @param antoLineLayout 自动换行布局容器
     * @param tmpData        标签数据列表（包含labelId和labelName）
     */
    private void createChildLableView(SobotAntoLineLayout antoLineLayout, List<SobotOrderTagModel> tmpData) {
        if (antoLineLayout != null) {
            antoLineLayout.removeAllViews();
            for (int i = 0; i < tmpData.size(); i++) {
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.sobot_layout_evaluate_item, null);
                CheckBox checkBox = view.findViewById(R.id.sobot_evaluate_cb_lable);
                //新版UI规范不要平均分的，显示不下换行
                //50 =antoLineLayout 左间距20+右间距20 +antoLineLayout 子控件行间距10
//                if(ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)){
//                    //横屏
//                    checkBox.setMinWidth((ScreenUtil.getScreenSize(this)[0] - ScreenUtils.dip2px(getContext(), 100)) / 2);
//                }else {
//                    checkBox.setMinWidth((ScreenUtil.getScreenSize(this)[0] - ScreenUtils.dip2px(getContext(), 50)) / 2);
//                }
                checkBox.setText(tmpData.get(i).getLabelName());
                checkBox.setTag(tmpData.get(i).getLabelId());
                checkBox.setOnClickListener(this);
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
            }
        }
    }


    /**
     * 设置评价标签区域的显示逻辑
     * <p>
     * 根据标签数据和information配置决定标签区域的显示/隐藏。
     * 与{@link SobotEvaluateActivity# setLableViewVisible}的区别：
     * 不区分机器人/人工模式，统一使用isHideManualEvaluationLabels配置。
     *
     * @param tmpData 标签数据列表，null或空表示无标签（隐藏标签区域）
     */
    private void setLableViewVisible(List<SobotOrderTagModel> tmpData) {
        if (tmpData != null && tmpData.size() > 0) {
            //根据infomation 配置是否隐藏人工评价标签
            if (!information.isHideManualEvaluationLabels()) {
                sobot_hide_layout.setVisibility(View.VISIBLE);
            } else {
                sobot_hide_layout.setVisibility(View.GONE);
            }
            if (satisfactionSetBase != null) {
                if (TextUtils.isEmpty(satisfactionSetBase.getTagTips())) {
                    sobot_text_other_problem.setVisibility(View.GONE);
                } else {
                    sobot_text_other_problem.setVisibility(View.VISIBLE);
                    sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                }
            }
            createChildLableView(sobot_evaluate_lable_autoline, tmpData);
            checkLable(tmpData);
        } else {
            sobot_hide_layout.setVisibility(View.GONE);
            return;
        }
    }

    /**
     * 获取当前"问题是否已解决"的选择值
     * <p>
     * 仅在评价配置开启了"问题是否解决"选项（isQuestionFlag=1）时才返回实际值，
     * 否则返回-1（未选择）。
     *
     * @return 0=未解决，1=已解决，-1=未选择或未开启该选项
     */
    private int getResovled() {
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            if (isSolve >= 0) {
                return isSolve;
            } else {
                return -1;
            }
        }
        return -1;
    }

    /**
     * 构建评价提交参数对象
     * <p>
     * 与{@link SobotEvaluateActivity# getCommentParam()}的区别：
     * - type固定为"0"（机器人类型）
     * - 使用labelIds（标签ID列表）而非tagsJson（JSON字符串）
     * - 始终设置score（包括机器人模式）
     *
     * @return 评价提交参数对象
     */
    private SobotCommentParam getCommentParam() {
        SobotCommentParam param = new SobotCommentParam();
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
        String problem = checkBoxIsChecked();
        param.setProblem(problem);
        param.setScore(tmpScore + "");//评分
        param.setScoreExplain((satisfactionSetBase != null && StringUtils.isNoEmpty(satisfactionSetBase.getScoreExplain())) ? satisfactionSetBase.getScoreExplain() : "");//星级说明
        param.setScoreExplainLan((satisfactionSetBase != null && StringUtils.isNoEmpty(satisfactionSetBase.getScoreExplainLan())) ? satisfactionSetBase.getScoreExplainLan() : "");
        param.setLabelIds(checkLables);//标签Id集合 ["ID1","ID2"]
        String suggest = sobot_add_content.getText().toString();
        param.setSuggest(suggest);//备注
        param.setCommentType(commentType);//评价类型， 0-邀请评价，1-主动评价
        param.setType("0");
        param.setIsresolve(getResovled());//是否解决 0：未解决，1：已解决，-1：未选择
        return param;
    }

    /**
     * 提交评价入口方法
     * <p>
     * 先调用{@link #checkInput()}进行表单校验，
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
     * 校验规则：
     * 1. 如果"问题是否解决"选项为必填（isQuestionMust=1），检查是否已选择
     * 2. 评分是否已选择（五星制不允许0分，十分制不允许未选择-1，二级评价不允许-1）
     * 3. 如果标签为必填（isTagMust=true），检查checkLables是否有选中项
     * 4. 如果建议输入为必填（isInputMust=1），检查是否已填写
     *
     * @return true=校验通过可提交，false=校验不通过（已Toast提示用户）
     */
    private boolean checkInput() {
        //是否已解决
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
                ToastUtil.showToast(SobotAIEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                return false;
            }
        } else if (ratingType == 1) {
            tmpScore = sobot_ten_rating_ll.getSelectContent();
            //10分的评价分值可以传0，但不能不选
            if (tmpScore < 0) {
                ToastUtil.showToast(SobotAIEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                return false;
            }
        } else if (ratingType == 2) {
            tmpScore = score;
            //10分的评价分值可以传0，但不能不选
            if (tmpScore < 0) {
                ToastUtil.showToast(SobotAIEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                return false;
            }
        }

        if (satisfactionSetBase != null) {
            SobotCommentParam commentParam = getCommentParam();
            if (satisfactionSetBase.getIsTagMust()) {
                if (checkLables == null || (checkLables != null && checkLables.size() == 0)) {
                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                    return false;
                }
            }

            if (mSatisfactionSet.getTxtFlag() == 1 && satisfactionSetBase.getIsInputMust() == 1) {
                if (TextUtils.isEmpty(commentParam.getSuggest().trim())) {
                    ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.sobot_suggestions_are_required));//建议必填
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 将逗号分隔的字符串转换为字符串数组
     *
     * @param str 逗号分隔的字符串
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
     * 调用大模型评价提交接口
     * <p>
     * 通过{@link ZhiChiApi#aiAgentComment}接口将评价数据提交到服务端，
     * 该接口与普通评价接口不同，需要额外传入aiAgentCid参数。
     * 无论成功或失败，都会通过本地广播{@link ZhiChiConstants#dcrc_comment_state}
     * 通知聊天界面评价结果。
     */
    private void comment() {
        final SobotCommentParam commentParam = getCommentParam();
        zhiChiApi.aiAgentComment(this, initModel.getCid(), initModel.getPartnerid(), initModel.getAiAgentCid(), commentParam,
                new StringResultCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
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

                        CommonUtils.sendLocalBroadcast(SobotAIEvaluateActivity.this, intent);
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

                        CommonUtils.sendLocalBroadcast(SobotAIEvaluateActivity.this, intent);
                        finish();
                    }
                });
    }

    /**
     * 回显预选中的标签
     * <p>
     * 遍历所有标签CheckBox，将labelId包含在checkLables列表中的标签设为选中状态。
     * 与{@link SobotEvaluateActivity# checkLable}的区别：通过labelId匹配而非标签名称。
     *
     * @param tmpData 当前显示的标签数据列表
     */
    private void checkLable(List<SobotOrderTagModel> tmpData) {
        if (tmpData != null && checkLables != null && checkLables.size() > 0 && sobot_evaluate_lable_autoline != null) {
            for (int i = 0; i < tmpData.size(); i++) {
                CheckBox checkBox = (CheckBox) sobot_evaluate_lable_autoline.getChildAt(i);
                if (checkBox != null) {
                    if (checkLables.contains(tmpData.get(i).getLabelId())) {
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
        return SobotAIEvaluateActivity.this;
    }

    /**
     * 统一点击事件处理
     * <p>
     * 处理以下控件的点击：
     * - 满意按钮（二级评价）：设置score=5，切换图标选中状态
     * - 不满意按钮（二级评价）：设置score=1，切换图标选中状态
     * - 已解决按钮：设置isSolve=1，更新选中状态
     * - 未解决按钮：设置isSolve=0，更新选中状态
     */
    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId() == R.id.sobot_btn_satisfied) {
            score = 5;
//            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
//            iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
//            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
//            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 35);
//            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);

            changeCommitButtonUi(true);
        } else if (v.getId() == R.id.sobot_btn_dissatisfied) {
//            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
//            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
//            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
//            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
//            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
//            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

            score = 1;
            changeCommitButtonUi(true);
        }
        if (v.getId() == R.id.sobot_ll_ok_robot) {
            isSolve = 1;
            iv_solved.setSelected(true);
            sobot_ll_ok_robot.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_no_robot.setSelected(false);
        } else if (v.getId() == R.id.sobot_ll_no_robot) {
            isSolve = 0;
            iv_solved.setSelected(false);
            sobot_ll_ok_robot.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_no_robot.setSelected(true);
        }
    }
}