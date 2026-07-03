package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.base.SobotDialogBaseActivity;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SobotOrderScoreModel;
import com.sobot.chat.api.model.SobotUserTicketEvaluate;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.chat.widget.toast.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单评价界面（半屏弹窗）
 * <p>
 * 该Activity用于对工单（Ticket）服务进行满意度评价，与会话评价（{@link SobotEvaluateActivity}）的区别：
 * <ul>
 *     <li>评价配置直接通过Intent传入{@link SobotUserTicketEvaluate}对象，不需要请求服务端接口</li>
 *     <li>评价结果通过setResult返回给调用方（而非发送广播），由调用方处理提交逻辑</li>
 *     <li>仅支持五星评价（scoreFlag=0）和十分制评价（scoreFlag=1），不支持二级评价</li>
 *     <li>不支持"暂不评价"功能（按钮固定隐藏）</li>
 *     <li>"问题是否已解决"的未选择状态值为2（而非-1）</li>
 * </ul>
 * <p>
 * 需要通过Intent传入以下参数：
 * <ul>
 *     <li>sobotUserTicketEvaluate - 工单评价配置对象{@link SobotUserTicketEvaluate}，包含评分模式、标签、默认值等</li>
 * </ul>
 * <p>
 * 评价完成后通过setResult返回以下数据：
 * <ul>
 *     <li>score - 评分值</li>
 *     <li>content - 文字建议内容</li>
 *     <li>labelTag - 选中标签的逗号分隔字符串</li>
 *     <li>defaultQuestionFlag - 问题是否已解决（0=未解决，1=已解决，2=未选择）</li>
 * </ul>
 *
 * @see SobotDialogBaseActivity 基类，提供半屏弹窗样式
 * @see SobotEvaluateActivity 人工客服/机器人评价界面
 * @see SobotAIEvaluateActivity 大模型机器人评价界面
 * Created by jinxl on 2017/6/12.
 */
public class SobotTicketEvaluateActivity extends SobotDialogBaseActivity implements View.OnClickListener {

    // ==================== UI控件 ====================

    private LinearLayout coustom_pop_layout;
    /**
     * 机器人评价区域布局（工单评价中不使用，固定隐藏）
     */
    private LinearLayout sobot_robot_relative;
    /**
     * 人工客服评价区域布局（工单评价中复用此布局显示评价内容）
     */
    private LinearLayout sobot_custom_relative;
    /**
     * 评价标签和建议输入的容器布局
     */
    private LinearLayout sobot_hide_layout;
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
     * "是否解决了您的问题？"标题
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
     * "暂不评价"按钮（工单评价中固定隐藏）
     */
    private TextView sobot_evaluate_cancel;
    /**
     * 评价提示文案（工单评价中不使用）
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
     * </ul>
     * 注意：工单评价不支持二级评价（满意/不满意）模式
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

    // ==================== 数据字段 ====================

    /**
     * 当前评分值，五星制取值1-5，十分制取值0-10
     */
    private int score;

    /**
     * 主题色，用于按钮的主题色渲染
     */
    private int themeColor;
    /**
     * 是否已自定义主题色（非默认色），决定是否对按钮应用主题色
     */
    private boolean changeThemeColor;

    /**
     * 问题是否已解决：1=已解决，0=未解决，2=未选择
     * <p>
     * 注意：与{@link SobotEvaluateActivity}和{@link SobotAIEvaluateActivity}不同，
     * 工单评价的未选择状态值为2（而非-1）
     */
    private int isSolve = 2;

    /**
     * 所有评价标签CheckBox的集合，用于遍历获取选中状态
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();
    /**
     * 工单评价配置数据，通过Intent传入
     */
    private SobotUserTicketEvaluate mEvaluate;
    /**
     * 当前选中评分对应的配置，随评分变化动态更新
     */
    private SobotOrderScoreModel satisfactionSetBase;
    /**
     * 输入框获得焦点时的背景Drawable
     */
    private Drawable bgDrawable;


    /**
     * 返回评价界面的布局资源ID，复用与普通评价相同的布局
     */
    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_evaluate;
    }

    /**
     * 设置网络请求的取消标识
     */
    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketEvaluateActivity";
    }

    /**
     * 初始化视图：解析Intent参数、绑定控件、设置初始状态
     * <p>
     * 与会话评价不同，工单评价的配置数据直接通过Intent传入{@link SobotUserTicketEvaluate}对象，
     * 不需要异步请求服务端接口。初始化流程：
     * 1. 从Intent获取工单评价配置对象
     * 2. 绑定所有控件
     * 3. 根据配置的scoreFlag确定评分模式（五星/十分）
     * 4. 根据defaultType和defaultQuestionFlag设置默认评分和"问题是否解决"状态
     * 5. 等宽对齐"已解决/未解决"按钮
     * 6. 根据isQuestionFlag决定是否显示"问题是否解决"选项
     * 7. 设置自定义评价提示语和提交按钮文案
     * 8. 调用{@link #setViewListener()}注册评分和提交的监听器
     */
    @Override
    protected void initView() {
        super.initView();
        mEvaluate = (SobotUserTicketEvaluate) getIntent().getSerializableExtra("sobotUserTicketEvaluate");
        sobot_btn_submit = findViewById(R.id.sobot_close_now);
        sobot_btn_submit.setText(getSafeStringResource(R.string.sobot_btn_submit_text));
        sobot_readiogroup = findViewById(R.id.sobot_readiogroup);
        sobot_tv_evaluate_title = (TextView) findViewById(R.id.sobot_tv_evaluate_title);
        //统一显示为服务评价
        sobot_tv_evaluate_title.setText(getSafeStringResource(R.string.sobot_please_evaluate_this_service));
        sobot_robot_center_title = (TextView) findViewById(R.id.sobot_robot_center_title);
        String sobot_question = getResources().getString(R.string.sobot_question);
        sobot_robot_center_title.setText(String.format(sobot_question, "").trim());
        sobot_text_other_problem = (TextView) findViewById(R.id.sobot_text_other_problem);
        sobot_ratingBar_title = (TextView) findViewById(R.id.sobot_ratingBar_title);
        sobot_ratingBar_title.setText(getSafeStringResource(R.string.sobot_great_satisfaction));
        sobot_tv_evaluate_title_hint = (TextView) findViewById(R.id.sobot_tv_evaluate_title_hint);
        sobot_evaluate_cancel = (TextView) findViewById(R.id.sobot_evaluate_cancel);
        sobot_evaluate_cancel.setText(getSafeStringResource(R.string.sobot_temporarily_not_evaluation));
        sobot_ratingBar_split_view = findViewById(R.id.sobot_ratingBar_split_view);
        sobot_evaluate_cancel.setVisibility(View.GONE);

        sobot_ratingBar = findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = findViewById(R.id.sobot_ten_root_ll);
        sobot_ten_rating_ll = findViewById(R.id.sobot_ten_rating_ll);

        sobot_evaluate_lable_autoline = findViewById(R.id.sobot_evaluate_lable_autoline);
        sobot_add_content = (EditText) findViewById(R.id.sobot_add_content);
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
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = sobot_btn_submit.getBackground();
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, themeColor));
            }
        }
        if (ScreenUtils.isFullScreen(this)) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        if (mEvaluate != null) {
            //主动评价需要判断默认星级
            if (mEvaluate.getScoreFlag() == 0) {
                //defaultType 0-默认5星,1-默认0星
                score = (mEvaluate.getDefaultType() == 0) ? 5 : 0;
                sobot_ten_root_ll.setVisibility(View.GONE);
                sobot_ratingBar.setVisibility(View.VISIBLE);
                ratingType = 0;//5星
            } else {
                sobot_ten_root_ll.setVisibility(View.VISIBLE);
                sobot_ratingBar.setVisibility(View.GONE);
                ratingType = 1;//十分
                //0-10分，1-5分，2-0分，3-不选中
                if (mEvaluate.getDefaultType() == 2) {
                    score = 0;
                } else if (mEvaluate.getDefaultType() == 1) {
                    score = 5;
                } else if (mEvaluate.getDefaultType() == 3) {
                    score = -1;
                } else {
                    score = 10;
                }
            }
            if (ratingType == 0) {
                if (score == -1) {
                    score = 5;
                }
                sobot_ratingBar.init(score, true, 36);
            } else {
                sobot_ten_rating_ll.init(score, true, 16);
            }

            //是否显示评价输入框
            if (mEvaluate.getTxtFlag() == 1) {
                setl_submit_content.setVisibility(View.VISIBLE);
            } else {
                //关闭评价输入框
                setl_submit_content.setVisibility(View.GONE);
            }
            isSolve = mEvaluate.getDefaultQuestionFlag();
            if (mEvaluate.getDefaultQuestionFlag() == 1) {
                //(1)-解决
                iv_solved.setSelected(true);
                iv_no_solve.setSelected(false);
                sobot_ll_ok_robot.setSelected(true);
                sobot_ll_no_robot.setSelected(false);
            } else if (mEvaluate.getDefaultQuestionFlag() == 0) {
                //(0)-未解决
                iv_solved.setSelected(false);
                iv_no_solve.setSelected(true);
                sobot_ll_ok_robot.setSelected(false);
                sobot_ll_no_robot.setSelected(true);
            }
//判断已解决 未解决长度是否相等
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int width1 = sobot_ll_ok_robot.getMeasuredWidth();
                    int width2 = sobot_ll_no_robot.getMeasuredWidth();
                    sobot_ll_ok_robot.getPaddingStart();
                    if (width1 < width2) {
                        int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 16);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 16));
                        sobot_ll_ok_robot.setPadding(pading, ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 7));
                    } else if (width1 > width2) {
                        int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 16);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 16));
                        sobot_ll_no_robot.setPadding(pading, ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotTicketEvaluateActivity.this, 7));
                    }
                }
            });
            setCustomLayoutViewVisible(score, mEvaluate.getScoreInfo());
            if (ratingType == 0) {
                if (0 == score) {
                    changeCommitButtonUi(false);
                    sobot_ratingBar_title.setText(getSafeStringResource(R.string.sobot_evaluate_zero_score_des));
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_text_third));
                } else {
                    changeCommitButtonUi(true);
                    if (satisfactionSetBase != null) {
                        sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                    }
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_evaluate_ratingBar_des_tv));
                }
            } else {
                if (-1 == score) {
                    changeCommitButtonUi(false);
                    sobot_ratingBar_title.setText(getSafeStringResource(R.string.sobot_evaluate_zero_score_des));
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_text_third));
                } else {
                    changeCommitButtonUi(true);
                    if (satisfactionSetBase != null) {
                        sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                    }
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_evaluate_ratingBar_des_tv));
                }
            }
            //1-开启 0-关闭
            if (mEvaluate.getIsQuestionFlag() == 1) {
                sobot_robot_relative.setVisibility(View.VISIBLE);
                sobot_readiogroup.setVisibility(View.VISIBLE);
                sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
            } else {
                sobot_robot_relative.setVisibility(View.GONE);
                sobot_readiogroup.setVisibility(View.GONE);
                sobot_ratingBar_split_view.setVisibility(View.GONE);
            }
            //是否是默认评价提示语
            if (mEvaluate.getIsDefaultGuide() == 0 && !TextUtils.isEmpty(mEvaluate.getGuideCopyWriting())) {
                sobot_tv_evaluate_title.setText(mEvaluate.getGuideCopyWriting());
            }

            //是否是默认提交按钮
            if (mEvaluate.getIsDefaultButton() == 0 && !TextUtils.isEmpty(mEvaluate.getButtonDesc())) {
                sobot_btn_submit.setText(mEvaluate.getButtonDesc());
            }
            bgDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_line_4, null);
            sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        sobot_add_content.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, themeColor));
                    } else {
                        sobot_add_content.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.sobot_bg_dialog_input, null));
                    }
                }
            });
            setViewListener();
        }
    }

    /**
     * 初始化数据（工单评价不需要异步加载数据，所有配置已在initView中处理）
     */
    @Override
    protected void initData() {

    }

    /**
     * 设置评分控件和操作按钮的监听器
     * <p>
     * 包括：
     * - 根据information配置决定是否显示评分说明文字
     * - 五星评分控件的点击监听：选择星级后更新标签和提交按钮状态
     * - 十分制评分控件的点击监听：选择分值后更新标签和提交按钮状态
     * - 提交按钮：校验通过后通过setResult返回评价数据给调用方
     */
    private void setViewListener() {
        Information information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");
        //根据infomation 配置是否隐藏星星评价描述
        if (!information.isHideManualEvaluationLabels()) {
            sobot_ratingBar_title.setVisibility(View.VISIBLE);
        } else {
            sobot_ratingBar_title.setVisibility(View.GONE);
        }
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
                if (score > 0 && score <= 5) {
                    changeCommitButtonUi(true);
                    setCustomLayoutViewVisible(score, mEvaluate.getScoreInfo());
                }
                sobot_btn_submit.setVisibility(View.VISIBLE);
                changeCommitButtonUi(true);
            }
        });

        sobot_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sobot_add_content.clearFocus();
                if (!checkInput()) {
                    return;
                }
                sobot_btn_submit.setClickable(false);
                //提交评价
                int score = (int) Math.ceil(sobot_ratingBar.getSelectContent());
                String labelTag = checkBoxIsChecked();
                hideKeyboard();
                Intent intent = new Intent();
                intent.putExtra("score", score);
                intent.putExtra("content", sobot_add_content.getText().toString());
                intent.putExtra("labelTag", labelTag);
                intent.putExtra("defaultQuestionFlag", isSolve);
                setResult(RESULT_OK, intent);
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
                    setCustomLayoutViewVisible(selectIndex, mEvaluate.getScoreInfo());
                }
            });
        }

    }

    /**
     * 更新提交按钮的UI状态（可点击/不可点击）
     * <p>
     * 与会话评价的区别：仅在自定义主题色（changeThemeColor=true）时应用主题色背景。
     *
     * @param isCanClick true=按钮可点击（正常状态），false=按钮不可点击（置灰状态）
     */
    private void changeCommitButtonUi(boolean isCanClick) {
        if (changeThemeColor) {
            Drawable bg = sobot_btn_submit.getBackground();
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, themeColor));
            }
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
     * 根据当前评分，更新评价标签、输入框、评分说明等UI元素的显示状态
     * <p>
     * 根据选中的评分值从配置列表中查找对应的配置项{@link SobotOrderScoreModel}，
     * 然后依据该配置项更新评分说明、输入框提示、标签列表等。
     * 与会话评价相比逻辑更简洁，不需要处理information配置的标签隐藏。
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
            sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_evaluate_ratingBar_des_tv));

            if (mEvaluate.getTxtFlag() == 1) {
                setl_submit_content.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(satisfactionSetBase.getInputLanguage())) {
                    if (satisfactionSetBase.getIsInputMust() == 1) {
                        sobot_add_content.setHint(getResources().getString(R.string.sobot_required) + satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    } else {
                        sobot_add_content.setHint(satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    }
                } else {
                    sobot_add_content.setHint(String.format(getString(R.string.sobot_edittext_hint)));
                }
            } else {
                //隐藏输入框
                setl_submit_content.setVisibility(View.GONE);
            }

            if (null != satisfactionSetBase.getTags() && satisfactionSetBase.getTags().size() > 0) {
                String tmpData[] = satisfactionSetBase.getTagNames();
                setLableViewVisible(tmpData);
            } else {
                setLableViewVisible(null);
            }
            if (score == 5) {
                sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            }
            //是否自定义已解决标题
            if (satisfactionSetBase.getIsDefaultQuestion() == 0 && StringUtils.isNoEmpty(satisfactionSetBase.getQuestionCopywriting())) {
                sobot_robot_center_title.setText(satisfactionSetBase.getQuestionCopywriting());
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
     * 设置评价标签区域的显示逻辑
     * <p>
     * 与会话评价相比更简洁：标签数据为null时隐藏，否则直接显示，
     * 不需要根据information配置判断隐藏。
     *
     * @param tmpData 标签名称数组，null表示无标签（隐藏标签区域）
     */
    private void setLableViewVisible(String tmpData[]) {
        if (tmpData == null) {
            sobot_hide_layout.setVisibility(View.GONE);
            return;
        } else {
            sobot_hide_layout.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(satisfactionSetBase.getTagTips())) {
                sobot_text_other_problem.setVisibility(View.GONE);
            } else {
                sobot_text_other_problem.setVisibility(View.VISIBLE);
                if (satisfactionSetBase.getIsTagMust()) {
                    sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                } else {
                    sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                }
            }
        }
        createChildLableView(sobot_evaluate_lable_autoline, tmpData);
        checkLable(tmpData);
    }

    /**
     * 回显工单评价中已有的标签选中状态
     * <p>
     * 根据工单评价配置中的tag字段（已选标签），回显对应标签的选中状态。
     *
     * @param tmpData 当前显示的标签名称数组
     */
    private void checkLable(String tmpData[]) {
        if (tmpData != null && tmpData.length > 0 && sobot_evaluate_lable_autoline != null) {
            for (int i = 0; i < tmpData.length; i++) {
                CheckBox checkBox = (CheckBox) sobot_evaluate_lable_autoline.getChildAt(i);
                if (checkBox != null) {
                    if (mEvaluate != null && !TextUtils.isEmpty(mEvaluate.getTag()) && mEvaluate.getTag().contains(tmpData[i])) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }
                }
            }
        }
    }

    /**
     * 校验评价表单是否满足提交条件
     * <p>
     * 校验规则：
     * 1. 如果"问题是否解决"为必填（isQuestionFlag=1且isQuestionMust=1），检查isSolve是否不为2
     * 2. 评分是否已选择（不允许未选择-1）
     * 3. 如果标签为必填（isTagMust=true），检查是否已选择标签
     * 4. 如果建议输入为必填（isInputMust=1），检查是否已填写
     *
     * @return true=校验通过可提交，false=校验不通过（已Toast提示用户）
     */
    private boolean checkInput() {
        //如果开启了是否解决问题
        if (mEvaluate != null && mEvaluate.getIsQuestionFlag() == 1 && mEvaluate.getIsQuestionMust() == 1) {
            //“问题是否解决”是否为必填选项： 0-非必填 1-必填
            if (isSolve == 2) {
                ToastUtil.showToast(this, getString(R.string.sobot_str_please_check_is_solve));//标签必选
                return false;
            }
        }
        //评分是否未0
        int tmpScore = -1;
        if (ratingType == 0) {
            tmpScore = (int) Math.ceil(sobot_ratingBar.getSelectContent());
        } else {
            tmpScore = sobot_ten_rating_ll.getSelectContent();
        }
        if (tmpScore < 0) {
            ToastUtil.showToast(this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
            return false;
        }
        if (satisfactionSetBase != null) {
            if (null != satisfactionSetBase.getTags() && satisfactionSetBase.getTags().size() > 0 && satisfactionSetBase.getIsTagMust()) {
                if (TextUtils.isEmpty(checkBoxIsChecked())) {
                    ToastUtil.showToast(this, getString(R.string.sobot_the_label_is_required));//标签必选
                    return false;
                }
            }

            if (mEvaluate.getTxtFlag() == 1 && satisfactionSetBase.getIsInputMust() == 1) {
                String suggest = sobot_add_content.getText().toString();
                if (TextUtils.isEmpty(suggest.trim())) {
                    ToastUtil.showToast(this, getString(R.string.sobot_suggestions_are_required));//建议必填
                    return false;
                }
            }
        }
        return true;
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
     * 在自动换行布局中动态创建评价标签CheckBox
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
                //50 =antoLineLayout 左间距20+右间距20 +antoLineLayout 子控件行间距10
//                checkBox.setMinWidth((ScreenUtil.getScreenSize(this)[0] - ScreenUtils.dip2px(getContext(), 50)) / 2);
                checkBox.setText(tmpData[i]);
                checkBox.setOnClickListener(this);
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
            }
        }
    }

    /**
     * 统一点击事件处理
     * <p>
     * 仅处理"已解决/未解决"按钮的点击：
     * - 已解决按钮：设置isSolve=1，更新选中状态
     * - 未解决按钮：设置isSolve=0，更新选中状态
     */
    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId() == R.id.sobot_ll_ok_robot) {
            isSolve = 1;
            // 获取系统默认的加粗字体
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_ok_robot.setSelected(true);
            sobot_ll_no_robot.setSelected(false);
        } else if (v.getId() == R.id.sobot_ll_no_robot) {
            isSolve = 0;
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_ok_robot.setSelected(false);
            sobot_ll_no_robot.setSelected(true);
        }
    }
}