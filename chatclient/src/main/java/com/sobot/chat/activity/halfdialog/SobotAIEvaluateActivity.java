package com.sobot.chat.activity.halfdialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
 * 大模型机器人评价界面的显示 ，没有邀评
 * Created by gqf on 2025/3/12.
 */
public class SobotAIEvaluateActivity extends SobotDialogBaseActivity implements View.OnClickListener {
    private final String CANCEL_TAG = "SobotAIEvaluateActivity";
    private int score;//默认 选中几颗星 从前面界面传过来
    private int isSolve = -1;//是否解决问题 0:未解决，1：已解决，-1：都不选
    private boolean isFinish;
    private boolean isExitSession;
    private boolean isSessionOver;//当前会话是否结束
    private boolean canBackWithNotEvaluation;//是否显示暂不评价"customName"
    private boolean isBackShowEvaluate;//是否是 返回时弹出评价框
    private ZhiChiInitModeBase initModel;
    private Information information;
    private int commentType;/*commentType 评价类型 主动评价1 邀请评价0*/
    private String templateId;//模板id
    private List<SobotOrderScoreModel> satisFactionList;//不同分值下的配置
    private List<String> checkLables;//选中的标签
    private SobotOrderEvaluateModel mSatisfactionSet;//评价配置信息
    private SobotOrderScoreModel satisfactionSetBase;

    private LinearLayout sobot_evaluate_container;
    private LinearLayout coustom_pop_layout;
    private LinearLayout sobot_robot_relative;//评价 机器人布局
    private LinearLayout sobot_custom_relative;//评价人工布局
    private LinearLayout sobot_hide_layout;//评价机器人和人工未解决时显示出来的布局
    // ==============已解决、未解决 start==========
    private LinearLayout sobot_readiogroup,sobot_ll_ok_robot,sobot_ll_no_robot;//已解决、为解决
    private TextView sobot_btn_ok_robot;//评价  已解决
    private TextView sobot_btn_no_robot;//评价  未解决
    private ImageView iv_solved,iv_no_solve;
    //    ============已解决、为解决 end===========
    private TextView sobot_btn_submit;//提交评价按钮
    private View sobot_ratingBar_split_view;//如果有已解决按钮和未解决按钮就显示，否则隐藏；

    private EditText sobot_add_content;//评价  添加建议
    private TextView sobot_tv_evaluate_title;//评价   当前是评价机器人还是评价人工客服
    private TextView sobot_robot_center_title;//评价  机器人或人工是否解决了问题的标题
    private TextView sobot_text_other_problem;//评价  机器人或人工客服存在哪些问题的标题
    private TextView sobot_ratingBar_title;//评价  对人工客服打分不同显示不同的内容
    private TextView sobot_evaluate_cancel;//评价  暂不评价
    private TextView sobot_tv_evaluate_title_hint;//评价  提交后结束评价
    private SobotFiveStarsLayout sobot_ratingBar;//评价  打分
    private LinearLayout sobot_ten_root_ll;//评价  十分全布局
    private SobotTenRatingLayout sobot_ten_rating_ll;//评价  十分 父布局 动态添加10个textview
    private int ratingType;//评价  类型   0 5星 ；1 十分 默认5星,2 二级评价

    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//评价 标签 自动换行 最多可以有六个
    private SobotEditTextLayout setl_submit_content;//评价框
    //=======二级评价===start==
    private LinearLayout ll_2_type;//二级评价
    private LinearLayout sobot_btn_satisfied;//二级评价  满意
    private LinearLayout sobot_btn_dissatisfied;//二级评价  不满意
    private ImageView iv_satisfied,iv_dissatisfied;
    private TextView tv_satisfied,tv_dissatisfied;
    //==========二级评价===end======
    private int themeColor;
    private int maxWidth;

    private String sobot_question;//%s 是否解决了您的问题？

    private List<CheckBox> checkBoxList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_evaluate;
    }
    @Override
    protected void setRequestTag() {
        REQUEST_TAG="SobotAIEvaluateActivity";
    }
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
        tv_satisfied = findViewById(R.id.tv_satisfied);
        tv_dissatisfied = findViewById(R.id.tv_dissatisfied);
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
        sobot_btn_ok_robot = findViewById(R.id.sobot_btn_ok_robot);
        sobot_btn_no_robot = findViewById(R.id.sobot_btn_no_robot);
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
        Drawable bgDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.sobot_bg_line_4,null);
        maxWidth = (ScreenUtils.getScreenWidth(this) - ScreenUtils.dip2px(this, 40)) / 2;
        sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LogUtils.d("======是否失去焦点=====" + hasFocus);
                if (hasFocus) {
                    sobot_add_content.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, themeColor));
                } else {
                    sobot_add_content.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.sobot_bg_dialog_input,null));
                }
            }
        });
    }

    private void changeCommitButtonUi(boolean isCanClick) {
        Drawable bg = sobot_btn_submit.getBackground();
        if (bg != null) {
            sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
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
                            } else  {
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
                            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
                            iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
                            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
                            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
                            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 35);
                            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
                            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
                            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            tv_satisfied.setTextColor(getResources().getColor(R.color.sobot_common_hese));
                            tv_dissatisfied.setTextColor(getResources().getColor(R.color.sobot_color_text_second));
                        } else if (score == 1) {
                            //默认不满意
                            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
                            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
                            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
                            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
                            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
                            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
                            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
                            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);
                            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            tv_satisfied.setTextColor(getResources().getColor(R.color.sobot_color_text_second));
                            tv_dissatisfied.setTextColor(getResources().getColor(R.color.sobot_common_hese));
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
                        // 获取系统默认的加粗字体
                        sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
                        sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
                    } else if (satisfactionSet.getDefaultQuestionFlag() == 0) {
                        isSolve = 0;
                        //(0)-未解决
                        iv_solved.setSelected(false);
                        iv_no_solve.setSelected(false);
                        sobot_ll_ok_robot.setSelected(true);
                        sobot_ll_no_robot.setSelected(true);
                        sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
                        sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
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
                }else{
                    //没有模板
                    LogUtils.d("=====大模型评价==没有模板====");
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
                if(null!=des) {
                    LogUtils.d(des);
                    finish();
                }
            }

        });
    }

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
                if(isFinish || isExitSession) {
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

        sobot_robot_center_title.setText(String.format(sobot_question,initModel.getRobotName()));
        sobot_robot_relative.setVisibility(View.GONE);
        sobot_custom_relative.setVisibility(View.VISIBLE);
    }

    //设置评价标签的布局显示逻辑
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
        } else {
            //根据infomation 配置是否隐藏星星评价描述
            if (!information.isHideManualEvaluationLabels()) {
                sobot_ratingBar_title.setVisibility(View.VISIBLE);
            } else {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
        }
    }

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

    //隐藏所有自动换行的标签
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


    //设置评价标签的显示逻辑
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

    private int getResovled() {
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            if(isSolve>=0) {
                return isSolve;
            }else{
                return -1;
            }
        }
        return -1;
    }

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
        param.setScoreExplain((satisfactionSetBase!=null && StringUtils.isNoEmpty(satisfactionSetBase.getScoreExplain()))?satisfactionSetBase.getScoreExplain():"");//星级说明
        param.setScoreExplainLan((satisfactionSetBase!=null && StringUtils.isNoEmpty(satisfactionSetBase.getScoreExplainLan()))?satisfactionSetBase.getScoreExplainLan():"");
        param.setLabelIds(checkLables);//标签Id集合 ["ID1","ID2"]
        String suggest = sobot_add_content.getText().toString();
        param.setSuggest(suggest);//备注
        param.setCommentType(commentType);//评价类型， 0-邀请评价，1-主动评价
        param.setType("0");
        param.setIsresolve(getResovled());//是否解决 0：未解决，1：已解决，-1：未选择
        return param;
    }

    //提交评价
    private void subMitEvaluate() {
        if (!checkInput()) {
            return;
        }

        comment();
    }

    /**
     * 检查是否能提交评价
     *
     * @return
     */
    private boolean checkInput() {
        //是否已解决
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            SobotCommentParam commentParam = getCommentParam();
            //“问题是否解决”是否为必填选项： 0-非必填 1-必填
            if (commentParam.getIsresolve() == -1 && mSatisfactionSet.getIsQuestionMust()==1) {
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
                if (checkLables ==null || (checkLables != null && checkLables.size() == 0 )) {
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

    // 使用String的split 方法把字符串截取为字符串数组
    private static String[] convertStrToArray(String str) {
        String[] strArray = null;
        if (!TextUtils.isEmpty(str)) {
            strArray = str.split(","); // 拆分字符为"," ,然后把结果交给数组strArray
        }
        return strArray;
    }

    //提交评价调用接口
    private void comment() {
        final SobotCommentParam commentParam = getCommentParam();
        zhiChiApi.aiAgentComment(this, initModel.getCid(), initModel.getPartnerid(),initModel.getAiAgentCid(), commentParam,
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
//                            e.printStackTrace();
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

    //检查标签是否选中（根据主动邀评传过来的选中标签判断）
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

    //检测选中的标签
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

    public Activity getContext() {
        return SobotAIEvaluateActivity.this;
    }

    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId() == R.id.sobot_btn_satisfied) {
            score = 5;
            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
            iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 35);
            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_satisfied.setTextColor(getResources().getColor(R.color.sobot_common_hese));
            tv_dissatisfied.setTextColor(getResources().getColor(R.color.sobot_color_text_second));
            changeCommitButtonUi(true);
        } else if (v.getId() == R.id.sobot_btn_dissatisfied) {
            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 35);
            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(getContext(), 35);
            iv_satisfied.setLayoutParams(iv_satisfied.getLayoutParams());
            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(getContext(), 43);
            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(getContext(), 43);
            iv_dissatisfied.setLayoutParams(iv_dissatisfied.getLayoutParams());
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);
            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv_satisfied.setTextColor(getResources().getColor(R.color.sobot_color_text_second));
            tv_dissatisfied.setTextColor(getResources().getColor(R.color.sobot_common_hese));
            score = 1;
            changeCommitButtonUi(true);
        }
        if (v.getId() == R.id.sobot_ll_ok_robot) {
            isSolve = 1;
            iv_solved.setSelected(true);
            sobot_ll_ok_robot.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_no_robot.setSelected(false);
            // 获取系统默认的加粗字体
            sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
            sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
        } else if (v.getId() == R.id.sobot_ll_no_robot) {
            isSolve = 0;
            iv_solved.setSelected(false);
            sobot_ll_ok_robot.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_no_robot.setSelected(true);
            sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
            sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
        }
    }
}