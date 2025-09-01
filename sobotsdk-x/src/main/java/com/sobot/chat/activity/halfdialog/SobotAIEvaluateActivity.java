package com.sobot.chat.activity.halfdialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.network.http.callback.StringResultCallBack;
import com.sobot.utils.SobotStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 大模型机器人评价界面的显示 ，没有邀评
 * Created by gqf on 2025/3/12.
 */
public class SobotAIEvaluateActivity extends SobotDialogBaseActivity implements CompoundButton.OnCheckedChangeListener {
    private final String CANCEL_TAG = "SobotAIEvaluateActivity";
    private int score;//默认 选中几颗星 从前面界面传过来
    private int isSolve = -1;//是否解决问题 0:已解决，1：未解决，-1：都不选
    private boolean isFinish;
    private boolean isExitSession;
    private boolean isSessionOver;//当前会话是否结束
    private boolean canBackWithNotEvaluation;//是否显示暂不评价
    private boolean isBackShowEvaluate;//是否是 返回时弹出评价框
    private ZhiChiInitModeBase initModel;
    private Information information;
    private int commentType;/*commentType 评价类型 主动评价1 邀请评价0*/
    private String customName;
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
    private RadioGroup sobot_readiogroup;//
    private RadioButton sobot_btn_ok_robot;//评价  已解决
    private RadioButton sobot_btn_no_robot;//评价  未解决
    private TextView sobot_close_now;//提交评价按钮
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
    private TextView sobot_ten_very_dissatisfied;//评价 非常不满意
    private TextView sobot_ten_very_satisfaction;//评价  非常满意
    private SobotTenRatingLayout sobot_ten_rating_ll;//评价  十分 父布局 动态添加10个textview
    private int ratingType;//评价  类型   0 5星 ；1 十分 默认5星,2 二级评价

    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//评价 标签 自动换行 最多可以有六个
    private SobotEditTextLayout setl_submit_content;//评价框
    private LinearLayout ll_2_type;//二级评价
    private RadioGroup sobot_seconde_type;//二级评价
    private RadioButton sobot_btn_satisfied;//二级评价  满意
    private RadioButton sobot_btn_dissatisfied;//二级评价  不满意
    private int themeColor;
    private int maxWidth;

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
    protected void initView() {
        checkLables = new ArrayList<>();
        information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");
        this.score = getIntent().getIntExtra("score", 5);
        this.isSessionOver = getIntent().getBooleanExtra("isSessionOver", false);
        this.isFinish = getIntent().getBooleanExtra("isFinish", false);
        this.isExitSession = getIntent().getBooleanExtra("isExitSession", false);
        this.initModel = (ZhiChiInitModeBase) getIntent().getSerializableExtra("initModel");
        this.commentType = getIntent().getIntExtra("commentType", 0);
        this.customName = getIntent().getStringExtra("customName");
        this.isSolve = getIntent().getIntExtra("isSolve", -1);
        this.isBackShowEvaluate = getIntent().getBooleanExtra("isBackShowEvaluate", false);
        this.canBackWithNotEvaluation = getIntent().getBooleanExtra("canBackWithNotEvaluation", false);
        this.templateId = initModel.getTemplateId();
        sobot_evaluate_container = findViewById(R.id.sobot_evaluate_container);
        sobot_close_now = findViewById(R.id.sobot_close_now);
        sobot_close_now.setText(R.string.sobot_btn_submit_text);
        sobot_readiogroup = (RadioGroup) findViewById(R.id.sobot_readiogroup);
        sobot_tv_evaluate_title = (TextView) findViewById(R.id.sobot_tv_evaluate_title);
        //统一显示为服务评价
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        sobot_robot_center_title = (TextView) findViewById(R.id.sobot_robot_center_title);
        sobot_robot_center_title.setText(R.string.sobot_question);
        sobot_text_other_problem = (TextView) findViewById(R.id.sobot_text_other_problem);
        sobot_ratingBar_title = (TextView) findViewById(R.id.sobot_ratingBar_title);
        sobot_tv_evaluate_title_hint = (TextView) findViewById(R.id.sobot_tv_evaluate_title_hint);
        sobot_evaluate_cancel = (TextView) findViewById(R.id.sobot_evaluate_cancel);
        sobot_evaluate_cancel.setText(R.string.sobot_temporarily_not_evaluation);
        sobot_ratingBar_split_view = findViewById(R.id.sobot_ratingBar_split_view);
        ll_2_type = findViewById(R.id.ll_2_type);
        sobot_seconde_type = findViewById(R.id.sobot_seconde_type);
        sobot_btn_satisfied = findViewById(R.id.sobot_btn_satisfied);
        sobot_btn_dissatisfied = findViewById(R.id.sobot_btn_dissatisfied);
        if (information != null && information.isCanBackWithNotEvaluation()) {
            sobot_evaluate_cancel.setVisibility(View.VISIBLE);
        } else {
            sobot_evaluate_cancel.setVisibility(View.GONE);
        }

        sobot_ratingBar = findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = findViewById(R.id.sobot_ten_root_ll);
        sobot_ten_rating_ll = findViewById(R.id.sobot_ten_rating_ll);
        sobot_ten_very_dissatisfied = findViewById(R.id.sobot_ten_very_dissatisfied);
        sobot_ten_very_satisfaction = findViewById(R.id.sobot_ten_very_satisfaction);
        sobot_ten_very_dissatisfied.setText(R.string.sobot_very_dissatisfied);
        sobot_ten_very_satisfaction.setText(R.string.sobot_great_satisfaction);

        sobot_evaluate_lable_autoline = findViewById(R.id.sobot_evaluate_lable_autoline);
        sobot_add_content = (EditText) findViewById(R.id.sobot_add_content);
        sobot_add_content.setHint(R.string.sobot_edittext_hint);
        sobot_btn_ok_robot = (RadioButton) findViewById(R.id.sobot_btn_ok_robot);
        sobot_btn_no_robot = (RadioButton) findViewById(R.id.sobot_btn_no_robot);
        sobot_btn_ok_robot.setText(R.string.sobot_evaluate_yes);
        sobot_btn_no_robot.setText(R.string.sobot_evaluate_no);
//        doTwoViewWidthConsistent(sobot_btn_ok_robot,getResources().getString(R.string.sobot_evaluate_yes),sobot_btn_no_robot,getResources().getString(R.string.sobot_evaluate_no));
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
        maxWidth = (ScreenUtils.getScreenWidth(this) - ScreenUtils.dip2px(this, 40)) / 2;

    }

    private void changeCommitButtonUi(boolean isCanClick) {
        Drawable bg = sobot_close_now.getBackground();
        if (bg != null) {
            sobot_close_now.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
        }
        if (isCanClick) {
            sobot_close_now.setFocusable(true);
            sobot_close_now.setClickable(true);
            sobot_close_now.getBackground().setAlpha(255);
        } else {
            sobot_close_now.setFocusable(false);
            sobot_close_now.setClickable(false);
            sobot_close_now.getBackground().setAlpha(90);
        }
    }

    @Override
    protected void initData() {
        ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(this).getZhiChiApi();
        sobot_close_now.setVisibility(View.GONE);
        sobot_evaluate_container.setVisibility(View.GONE);
        zhiChiApi.getAiSatisfactionTemplate(this, initModel.getCid(), initModel.getPartnerid(), templateId, new StringResultCallBack<SobotOrderEvaluateModel>() {
            @Override
            public void onSuccess(SobotOrderEvaluateModel satisfactionSet) {
                sobot_close_now.setVisibility(View.VISIBLE);
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
                        sobot_ratingBar.init(score, true, 32);
                    } else if (ratingType == 1) {
                        sobot_ten_rating_ll.init(score, true, 20);
                    } else if (ratingType == 2) {
                        if (score == 5) {
                            //默认满意
                            sobot_btn_satisfied.setChecked(true);
                        } else if (score == 1) {
                            //默认不满意
                            sobot_btn_dissatisfied.setChecked(true);
                        }
                    }

                    //主动评价 问题是否解决 获取默认值
                    if (satisfactionSet.getDefaultQuestionFlag() == 1) {
                        isSolve = 0;
                    } else if (satisfactionSet.getDefaultQuestionFlag() == 0) {
                        isSolve = 1;
                    }
                    if (isSolve == 1) {
                        //(0)-未解决
                        sobot_btn_ok_robot.setChecked(false);
                        sobot_btn_no_robot.setChecked(true);
                    } else if (isSolve == 0) {
                        //(1)-解决
                        sobot_btn_ok_robot.setChecked(true);
                        sobot_btn_no_robot.setChecked(false);
                    } else {
                        sobot_btn_ok_robot.setChecked(false);
                        sobot_btn_no_robot.setChecked(false);
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
                    }
                    //1-开启 0-关闭
                    if (satisfactionSet.getIsQuestionFlag() == 1) {
                        sobot_robot_relative.setVisibility(View.VISIBLE);
                        //判断已解决 未解决长度是否相等
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                int width1 = sobot_btn_ok_robot.getMeasuredWidth();
                                int width2 = sobot_btn_no_robot.getMeasuredWidth();
                                sobot_btn_ok_robot.getPaddingStart();
                                if (width1 < width2) {
                                    int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16);
                                    LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16));
                                    sobot_btn_ok_robot.setPadding(pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7));
                                } else if (width1 > width2) {
                                    int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16);
                                    LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 16));
                                    sobot_btn_no_robot.setPadding(pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7), pading, ScreenUtils.dip2px(SobotAIEvaluateActivity.this, 7));
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
                        sobot_close_now.setText(satisfactionSet.getButtonDesc());
                    }
                }
            }

            @Override
            public void onFailure(Exception e, String des) {
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
                sobot_close_now.setVisibility(View.VISIBLE);
                changeCommitButtonUi(true);
            }
        });


        sobot_close_now.setOnClickListener(new View.OnClickListener() {
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
                    sobot_close_now.setVisibility(View.VISIBLE);
                    changeCommitButtonUi(true);
                    setCustomLayoutViewVisible(selectIndex, satisFactionList);
                }
            });
        }
        sobot_seconde_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                int score = 0;
                if (id == R.id.sobot_btn_satisfied) {
                    score = 5;
                    sobot_btn_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    sobot_btn_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                } else if (id == R.id.sobot_btn_dissatisfied) {
                    sobot_btn_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    sobot_btn_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    score = 1;
                }
                changeCommitButtonUi(true);
                setCustomLayoutViewVisible(score, satisFactionList);
            }
        });
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

        sobot_robot_center_title.setText(customName + " " + getString(R.string.sobot_question));
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
                checkBox.setOnCheckedChangeListener(this);
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
            if (sobot_btn_ok_robot.isChecked()) {
                return 1;
            } else if (sobot_btn_no_robot.isChecked()) {
                return 0;
            } else {
                return -1;
            }
        }
        return -1;
    }

    private SobotCommentParam getCommentParam() {
        SobotCommentParam param = new SobotCommentParam();
        int score = 0;
        if (ratingType == 0) {
            param.setScoreFlag(0);//5星
            score = (int) Math.ceil(sobot_ratingBar.getSelectContent());
        } else if (ratingType == 1) {
            param.setScoreFlag(1);//10分
            score = sobot_ten_rating_ll.getSelectContent();
        } else if (ratingType == 2) {
            param.setScoreFlag(2);//二级
            if (sobot_btn_satisfied.isChecked()) {
                score = 5;
            } else if (sobot_btn_dissatisfied.isChecked()) {
                score = 1;
            }
        }
        param.setScore(score + "");//评分
        param.setScoreExplain((satisfactionSetBase!=null && SobotStringUtils.isNoEmpty(satisfactionSetBase.getScoreExplain()))?satisfactionSetBase.getScoreExplain():"");//星级说明
        param.setScoreExplainLan((satisfactionSetBase!=null && SobotStringUtils.isNoEmpty(satisfactionSetBase.getScoreExplainLan()))?satisfactionSetBase.getScoreExplainLan():"");
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
        int score = -1;
        if (ratingType == 0) {
            score = (int) Math.ceil(sobot_ratingBar.getSelectContent());
            //五星评价分不能传0
            if (score < 1) {
                ToastUtil.showToast(SobotAIEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                return false;
            }
        } else if (ratingType == 1) {
            score = sobot_ten_rating_ll.getSelectContent();
            //10分的评价分值可以传0，但不能不选
            if (score < 0) {
                ToastUtil.showToast(SobotAIEvaluateActivity.this, getString(R.string.sobot_rating_score) + getString(R.string.sobot__is_null));//评分必选
                return false;
            }
        } else if (ratingType == 2) {
            if (sobot_btn_satisfied.isChecked()) {
                score = 5;
            } else if (sobot_btn_dissatisfied.isChecked()) {
                score = 1;
            }
            //10分的评价分值可以传0，但不能不选
            if (score < 0) {
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            StateListDrawable stateListDrawable = (StateListDrawable) ContextCompat.getDrawable(this, R.drawable.sobot_btn_bg_lable_select);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                for (int i = 0; i < stateListDrawable.getStateCount(); i++) {
                    Drawable drawable = stateListDrawable.getStateDrawable(i);
                    if (drawable instanceof GradientDrawable) {
                        // 修改边框颜色
                        GradientDrawable shapeDrawable = (GradientDrawable) drawable;
                        shapeDrawable.setStroke(2, themeColor); // 修改边框的宽度和颜色
                        shapeDrawable.setColor(ThemeUtils.modifyAlpha(themeColor, 10));
                    }
                }
            }
            compoundButton.setTextColor(themeColor);
            compoundButton.setBackground(stateListDrawable);
            if (compoundButton.getTag() != null) {
                String tagModel = (String) compoundButton.getTag();
                if (!checkLables.contains(tagModel)) {
                    checkLables.add(tagModel);
                }
            }
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.sobot_bg_cai_reason_lable_checkbox_bg);
            compoundButton.setTextColor(getResources().getColor(R.color.sobot_color_text_first));
            compoundButton.setBackground(drawable);

            if (compoundButton.getTag() != null) {
                String tagModel = (String) compoundButton.getTag();
                if (checkLables.contains(tagModel)) {
                    checkLables.remove(tagModel);
                }
            }
        }
    }

    public void doTwoViewWidthConsistent(RadioButton ll1, String text1, RadioButton ll2, String text2) {
        if (ll1 == null || ll2 == null || SobotStringUtils.isEmpty(text1) || SobotStringUtils.isEmpty(text1)) {
            return;
        }
        // 创建Paint对象
        Paint paint = new Paint();
        // 设置Paint的文字大小与TextView相同
        paint.setTextSize(ll1.getTextSize());
        // 测量两段文字的宽度
        float width1 = paint.measureText(text1);
        float width2 = paint.measureText(text2);
        if (width1 == width2) {
            return;
        }
        // 设置最大宽度为两段文字中的最大值
        float maxWidth = Math.max(width1, width2);
        // 设置TextView的宽度为最大宽度
        ll1.setWidth((int) maxWidth);
        ll2.setWidth((int) maxWidth);
//            if (width1 > width2) {
//                ll2.setPadding(ScreenUtils.dip2px(mContext, 12) + (int) (width1 - width2) / 2, ScreenUtils.dip2px(mContext, 7), ScreenUtils.dip2px(mContext, 12) + (int) (width1 - width2) / 2, ScreenUtils.dip2px(mContext, 7));
//            } else {
//                ll1.setPadding(ScreenUtils.dip2px(mContext, 12) + (int) (width2 - width1) / 2, ScreenUtils.dip2px(mContext, 7), ScreenUtils.dip2px(mContext, 12) + (int) (width2 - width1) / 2, ScreenUtils.dip2px(mContext, 7));
//            }
//        // 设置文字
//        ll1.setText(text1);
//        ll2.setText(text2);
    }

}