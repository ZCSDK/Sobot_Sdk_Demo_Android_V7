package com.sobot.chat.widget.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

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
import com.sobot.chat.notchlib.utils.ScreenUtil;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.network.http.callback.StringResultCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * 评价界面的显示
 * Created by jinxl on 2017/6/12.
 */
public class SobotEvaluateActivity extends SobotDialogBaseActivity implements CompoundButton.OnCheckedChangeListener {
    private final String CANCEL_TAG = "SobotEvaluateActivity";
    private int score;//默认 选中几颗星 从前面界面传过来
    private int isSolve = -1;//是否解决问题 0:已解决，1：未解决，-1：都不选
    private boolean isFinish;
    private boolean isExitSession;
    private boolean isSessionOver;//当前会话是否结束
    private boolean canBackWithNotEvaluation;//是否显示暂不评价
    private boolean isBackShowEvaluate;//是否是 返回时弹出评价框
    private ZhiChiInitModeBase initModel;
    private Information information;
    private int current_model;
    private int commentType;/*commentType 评价类型 主动评价1 邀请评价0*/
    private String customName;
    private List<SatisfactionSetBase> satisFactionList;
    private SatisfactionSet mSatisfactionSet;//评价配置信息
    private SatisfactionSetBase satisfactionSetBase;
    private LinearLayout sobot_negativeButton;
    private LinearLayout coustom_pop_layout;
    private LinearLayout sobot_robot_relative;//评价 机器人布局
    private LinearLayout sobot_custom_relative;//评价人工布局
    private LinearLayout sobot_hide_layout;//评价机器人和人工未解决时显示出来的布局
    private RadioGroup sobot_readiogroup;//
    private RadioButton sobot_btn_ok_robot;//评价  已解决
    private RadioButton sobot_btn_no_robot;//评价  未解决
    private Button sobot_close_now;//提交评价按钮
    private View sobot_ratingBar_split_view;//如果有已解决按钮和未解决按钮就显示，否则隐藏；

    private EditText sobot_add_content;//评价  添加建议
    private TextView sobot_tv_evaluate_title;//评价   当前是评价机器人还是评价人工客服
    private TextView sobot_robot_center_title;//评价  机器人或人工是否解决了问题的标题
    private TextView sobot_text_other_problem;//评价  机器人或人工客服存在哪些问题的标题
    private TextView sobot_custom_center_title;//评价  对 哪个人工客服 打分  的标题
    private TextView sobot_ratingBar_title;//评价  对人工客服打分不同显示不同的内容
    private TextView sobot_evaluate_cancel;//评价  暂不评价
    private TextView sobot_tv_evaluate_title_hint;//评价  提交后结束评价
    private RatingBar sobot_ratingBar;//评价  打分
    private LinearLayout sobot_ten_root_ll;//评价  十分全布局
    private TextView sobot_ten_very_dissatisfied;//评价 非常不满意
    private TextView sobot_ten_very_satisfaction;//评价  非常满意
    private SobotTenRatingLayout sobot_ten_rating_ll;//评价  十分 父布局 动态添加10个textview
    private int ratingType;//评价  类型   0 5星 ；1 十分 默认5星

    private String evaluateChecklables;//主动邀请评价选中的标签
    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//评价 标签 自动换行 最多可以有六个
    private SobotEditTextLayout setl_submit_content;//评价框

    private int themeColor;
    private boolean changeThemeColor;

    private List<CheckBox> checkBoxList = new ArrayList<>();

    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_evaluate;
    }

    @Override
    protected void initView() {
        information = (Information) SharedPreferencesUtil.getObject(getContext(), "sobot_last_current_info");


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

        sobot_close_now = (Button) findViewById(R.id.sobot_close_now);
        sobot_close_now.setText(R.string.sobot_btn_submit_text);
        sobot_readiogroup = (RadioGroup) findViewById(R.id.sobot_readiogroup);
        sobot_tv_evaluate_title = (TextView) findViewById(R.id.sobot_tv_evaluate_title);
        //统一显示为服务评价
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        sobot_robot_center_title = (TextView) findViewById(R.id.sobot_robot_center_title);
        sobot_robot_center_title.setText(R.string.sobot_question);
        sobot_text_other_problem = (TextView) findViewById(R.id.sobot_text_other_problem);
        sobot_custom_center_title = (TextView) findViewById(R.id.sobot_custom_center_title);
        sobot_custom_center_title.setText(R.string.sobot_please_evaluate);
        sobot_ratingBar_title = (TextView) findViewById(R.id.sobot_ratingBar_title);
        sobot_ratingBar_title.setText(R.string.sobot_great_satisfaction);
        sobot_tv_evaluate_title_hint = (TextView) findViewById(R.id.sobot_tv_evaluate_title_hint);
        sobot_evaluate_cancel = (TextView) findViewById(R.id.sobot_evaluate_cancel);
        sobot_evaluate_cancel.setText(R.string.sobot_temporarily_not_evaluation);
        sobot_ratingBar_split_view = findViewById(R.id.sobot_ratingBar_split_view);
        sobot_negativeButton = (LinearLayout) findViewById(R.id.sobot_negativeButton);
        sobot_negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (information != null && information.isCanBackWithNotEvaluation()) {
            //不清楚这个开关是什么意思，影响到暂不评价的显示，暂时注释2023.05.17
//            if (canBackWithNotEvaluation) {
            sobot_evaluate_cancel.setVisibility(View.VISIBLE);
//            } else {
//                sobot_evaluate_cancel.setVisibility(View.GONE);
//            }
        } else {
            sobot_evaluate_cancel.setVisibility(View.GONE);
        }

        sobot_ratingBar = (RatingBar) findViewById(R.id.sobot_ratingBar);
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
        sobot_btn_ok_robot.setText(R.string.sobot_evaluate_yes);
        sobot_btn_no_robot = (RadioButton) findViewById(R.id.sobot_btn_no_robot);
        sobot_btn_no_robot.setText(R.string.sobot_evaluate_no);
        sobot_robot_relative = (LinearLayout) findViewById(R.id.sobot_robot_relative);
        sobot_custom_relative = (LinearLayout) findViewById(R.id.sobot_custom_relative);
        sobot_hide_layout = (LinearLayout) findViewById(R.id.sobot_hide_layout);
        setl_submit_content = (SobotEditTextLayout) findViewById(R.id.setl_submit_content);
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
        }
        setViewGone();
        setViewListener();
        if (ScreenUtils.isFullScreen(this)) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void changeCommitButtonUi(boolean isCanClick) {
        if (changeThemeColor) {
            Drawable bg = sobot_close_now.getBackground();
            if (bg != null) {
                sobot_close_now.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            }
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
        if (current_model == ZhiChiConstant.client_model_customService) {
            ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(this).getZhiChiApi();
            sobot_close_now.setVisibility(View.GONE);
            zhiChiApi.satisfactionMessage(CANCEL_TAG, initModel.getPartnerid(), new ResultCallBack<SatisfactionSet>() {
                @Override
                public void onSuccess(SatisfactionSet satisfactionSet) {
                    sobot_close_now.setVisibility(View.VISIBLE);
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
                                ratingType = 0;//5星
                            } else {
                                sobot_ten_root_ll.setVisibility(View.VISIBLE);
                                sobot_ratingBar.setVisibility(View.GONE);
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
                            }
                        } else {
                            if (satisfactionSet.getScoreFlag() == 0) {
                                //defaultType 0-默认5星,1-默认0星
                                sobot_ten_root_ll.setVisibility(View.GONE);
                                sobot_ratingBar.setVisibility(View.VISIBLE);
                                ratingType = 0;//5星
                            } else {
                                sobot_ten_root_ll.setVisibility(View.VISIBLE);
                                sobot_ratingBar.setVisibility(View.GONE);
                                ratingType = 1;//十分
                            }
                        }
                        if (ratingType == 0) {
                            if (score == -1) {
                                score = 5;
                            }
                            sobot_ratingBar.setRating(score);
                        } else {
                            sobot_ten_rating_ll.init(score, true,41);
                        }

                        if (isSolve == -1) {
                            //主动评价 问题是否解决 获取默认值
                            if(satisfactionSet.getDefaultQuestionFlag()==1){
                                isSolve = 0;
                            }else if(satisfactionSet.getDefaultQuestionFlag()==0){
                                isSolve = 1;
                            }
                        }
                        if (isSolve == 1) {
                            //(0)-未解决
                            sobot_btn_ok_robot.setChecked(false);
                            sobot_btn_no_robot.setChecked(true);
                        } else if (isSolve == 0) {
                            //(1)-解决
                            sobot_btn_ok_robot.setChecked(true);
                            sobot_btn_no_robot.setChecked(false);
                        }else{
                            sobot_btn_ok_robot.setChecked(false);
                            sobot_btn_no_robot.setChecked(false);
                        }

                        setCustomLayoutViewVisible(score, satisFactionList);
                        if (ratingType == 0) {
                            if (0 == score) {
                                changeCommitButtonUi(false);
                                sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_common_gray3));
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
                                sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_common_gray3));
                            } else {
                                changeCommitButtonUi(true);
                                if (satisfactionSetBase != null) {
                                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                                }
                                sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_evaluate_ratingBar_des_tv));
                            }
                        }
                        //1-开启 0-关闭
                        if (satisfactionSet.getIsQuestionFlag() == 1) {
                            sobot_robot_relative.setVisibility(View.VISIBLE);
                            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
                        } else {
                            sobot_robot_relative.setVisibility(View.GONE);
                            sobot_ratingBar_split_view.setVisibility(View.GONE);
                        }
                        //是否是默认评价提示语
                        if(satisfactionSet.getIsDefaultGuide()==0 && !TextUtils.isEmpty(satisfactionSet.getGuideCopyWriting())){
                            sobot_tv_evaluate_title.setText(satisfactionSet.getGuideCopyWriting());
                        }
                        //是否显示评价输入框
                        if(satisfactionSet.getTxtFlag()==0){
                            //关闭评价输入框
                            setl_submit_content.setVisibility(View.GONE);
                        }else{
                            setl_submit_content.setVisibility(View.VISIBLE);
                        }
                        //是否是默认提交按钮
                        if(satisfactionSet.getIsDefaultButton()==0 && !TextUtils.isEmpty(satisfactionSet.getButtonDesc())){
                            sobot_close_now.setText(satisfactionSet.getButtonDesc());
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
        }else{
            //机器人模式下，默认选中已解决
            sobot_btn_ok_robot.setChecked(true);
            if(changeThemeColor){
                themeColor = ThemeUtils.getThemeColor(this);
                //定制化UI,纯UI显示，无逻辑
                updateButtonByThemeColor(true);
                Drawable bg= sobot_close_now.getBackground();
                if(bg!=null){
                    sobot_close_now.setBackground(ThemeUtils.applyColorToDrawable( bg,themeColor));
                }
            }
        }
    }

    private void setViewListener() {
        sobot_ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int score = (int) Math.ceil(sobot_ratingBar.getRating());
                if (score == 0) {
                    sobot_ratingBar.setRating(1);
                }
                if (score > 0 && score <= 5) {
                    changeCommitButtonUi(true);
                    setCustomLayoutViewVisible(score, satisFactionList);
                }
                sobot_close_now.setVisibility(View.VISIBLE);
                changeCommitButtonUi(true);
            }
        });

        sobot_readiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (current_model == ZhiChiConstant.client_model_robot && initModel != null) {
                    if (checkedId == R.id.sobot_btn_ok_robot) {
                        sobot_hide_layout.setVisibility(View.GONE);
                        if (changeThemeColor) {
                            //定制化UI,纯UI显示，无逻辑
                            updateButtonByThemeColor(true);
                        }
                    } else if (checkedId == R.id.sobot_btn_no_robot) {
                        sobot_hide_layout.setVisibility(View.VISIBLE);
                        String tmpData[] = convertStrToArray(initModel.getRobotCommentTitle());
                        if (tmpData != null && tmpData.length > 0) {
                            setLableViewVisible(tmpData);
                        } else {
                            sobot_hide_layout.setVisibility(View.GONE);
                        }
                        if (changeThemeColor) {
                            //定制化UI,纯UI显示，无逻辑
                            updateButtonByThemeColor(false);
                        }
                    }
                }
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
                Intent intent = new Intent();
                intent.setAction(ZhiChiConstants.sobot_close_now);
                LogUtils.i("isBackShowEvaluate:  " + isBackShowEvaluate + "--------canBackWithNotEvaluation:   " + canBackWithNotEvaluation);
                intent.putExtra("isBackShowEvaluate", isBackShowEvaluate);
                CommonUtils.sendLocalBroadcast(SobotEvaluateActivity.this, intent);
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
    }


    private void setViewGone() {
        sobot_hide_layout.setVisibility(View.GONE);
        setl_submit_content.setVisibility(View.GONE);
        sobot_evaluate_lable_autoline.removeAllViews();
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        if (current_model == ZhiChiConstant.client_model_robot) {
            sobot_robot_center_title.setText(initModel.getRobotName() + "" + getString(R.string.sobot_question));
            sobot_robot_relative.setVisibility(View.VISIBLE);
            sobot_custom_relative.setVisibility(View.GONE);
        } else {
            boolean isExitTalk = SharedPreferencesUtil.getBooleanData(SobotEvaluateActivity.this, ZhiChiConstant.SOBOT_CHAT_EVALUATION_COMPLETED_EXIT, false);
            if (isExitTalk && !isSessionOver) {//设置了评价关闭且当前会话没有结束
                sobot_tv_evaluate_title_hint.setText(R.string.sobot_evaluation_completed_exit);
                sobot_tv_evaluate_title_hint.setVisibility(View.VISIBLE);
            } else {
                sobot_tv_evaluate_title_hint.setVisibility(View.GONE);
            }

            sobot_robot_center_title.setText(customName + " " + getString(R.string.sobot_question));
            sobot_custom_center_title.setText(customName + " " + getString(R.string.sobot_please_evaluate));
            sobot_robot_relative.setVisibility(View.GONE);
            sobot_custom_relative.setVisibility(View.VISIBLE);
        }
    }

    //设置人工客服评价的布局显示逻辑
    private void setCustomLayoutViewVisible(int score, List<SatisfactionSetBase> satisFactionList) {
        satisfactionSetBase = getSatisFaction(score, satisFactionList);
        for (int i = 0; i < checkBoxList.size(); i++) {
            checkBoxList.get(i).setChecked(false);
        }
        if (satisfactionSetBase != null) {
            sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            sobot_ratingBar_title.setTextColor(ContextCompat.getColor(getContext(), R.color.sobot_color_evaluate_ratingBar_des_tv));
            if(satisfactionSetBase.getTxtFlag()==1) {
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
            }else{
                //隐藏输入框
                setl_submit_content.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
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
        } else {
            //根据infomation 配置是否隐藏星星评价描述
            if (!information.isHideManualEvaluationLabels()) {
                sobot_ratingBar_title.setVisibility(View.VISIBLE);
            } else {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
        }
    }

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

    //隐藏所有自动换行的标签
    private void createChildLableView(SobotAntoLineLayout antoLineLayout, String tmpData[]) {
        if (antoLineLayout != null) {
            antoLineLayout.removeAllViews();
            for (int i = 0; i < tmpData.length; i++) {
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.sobot_layout_evaluate_item, null);
                CheckBox checkBox = view.findViewById(R.id.sobot_evaluate_cb_lable);
                //50 =antoLineLayout 左间距20+右间距20 +antoLineLayout 子控件行间距10
                checkBox.setMinWidth((ScreenUtil.getScreenSize(this)[0] - ScreenUtils.dip2px(getContext(), 50)) / 2);
                checkBox.setText(tmpData[i]);
                if (changeThemeColor) {
                    checkBox.setTextColor(themeColor);
                }
                checkBox.setOnCheckedChangeListener(this);
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
            }
        }
    }


    //设置评价标签的显示逻辑
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
                        if (satisfactionSetBase.getIsTagMust()) {
                            sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                        } else {
                            sobot_text_other_problem.setText(satisfactionSetBase.getTagTips());
                        }
                    }
                }
            }
        }
        createChildLableView(sobot_evaluate_lable_autoline, tmpData);
        checkLable(tmpData);
    }

    private int getResovled() {
        if (current_model == ZhiChiConstant.client_model_robot) {
            if (sobot_btn_ok_robot.isChecked()) {
                return 0;
            } else {
                return 1;
            }
        } else if (current_model == ZhiChiConstant.client_model_customService) {
            if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
                if (sobot_btn_ok_robot.isChecked()) {
                    return 0;
                } else if (sobot_btn_no_robot.isChecked()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    private SobotCommentParam getCommentParam() {
        SobotCommentParam param = new SobotCommentParam();
        String type = current_model == ZhiChiConstant.client_model_robot ? "0" : "1";
        int score;
        if (ratingType == 0) {
            param.setScoreFlag(0);//5星
            score = (int) Math.ceil(sobot_ratingBar.getRating());
        } else {
            param.setScoreFlag(1);//10分
            score = sobot_ten_rating_ll.getSelectContent();
        }

        String problem = checkBoxIsChecked();
        String suggest = sobot_add_content.getText().toString();
        param.setType(type);
        param.setProblem(problem);
        param.setSuggest(suggest);
        param.setIsresolve(getResovled());
        param.setCommentType(commentType);
        if (current_model == ZhiChiConstant.client_model_robot) {
            param.setRobotFlag(initModel.getRobotid());
        } else {
            param.setScore(score + "");
        }
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
            int score=-1;
            if (ratingType == 0) {
                score = (int) Math.ceil(sobot_ratingBar.getRating());
                //五星评价分不能传0
                if(score<1){
                    ToastUtil.showToast(SobotEvaluateActivity.this, getString(R.string.sobot_rating_score)+getString(R.string.sobot__is_null));//评分必选
                    return false;
                }
            } else {
                score = sobot_ten_rating_ll.getSelectContent();
                //10分的评价分值可以传0，但不能不选
                if(score<0){
                    ToastUtil.showToast(SobotEvaluateActivity.this, getString(R.string.sobot_rating_score)+getString(R.string.sobot__is_null));//评分必选
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

                if (satisfactionSetBase.getTxtFlag()==1 && satisfactionSetBase.getIsInputMust()) {
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
        ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(this).getZhiChiApi();
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
//                            e.printStackTrace();
                        }
                    }
                });
    }

    //检查标签是否选中（根据主动邀评传过来的选中标签判断）
    private void checkLable(String tmpData[]) {
        if (tmpData != null && tmpData.length > 0 && !TextUtils.isEmpty(evaluateChecklables) && sobot_evaluate_lable_autoline != null) {
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
        return SobotEvaluateActivity.this;
    }

    /**
     * 根据主题色更新按钮的颜色
     *
     * @param isSolve 是否已解决
     */
    private void updateButtonByThemeColor(final boolean isSolve) {
//                Drawable zan = context.getResources().getDrawable(R.drawable.sobot_icon_zan_white);
//                Drawable cai = context.getResources().getDrawable(R.drawable.sobot_icon_no_white);
        if (isSolve) {
            //赞
//                    sobot_btn_ok_robot.setCompoundDrawablesWithIntrinsicBounds(ThemeUtils.applyColorToDrawable(zan,themeColor), null, null, null);
            sobot_btn_ok_robot.setTextColor(themeColor);
//                    sobot_btn_no_robot.setCompoundDrawablesWithIntrinsicBounds(cai, null, null, null);
            sobot_btn_no_robot.setTextColor(getResources().getColor(R.color.sobot_common_gray2));
        } else {
            //踩
//                    sobot_btn_no_robot.setCompoundDrawablesWithIntrinsicBounds(ThemeUtils.applyColorToDrawable(cai,themeColor), null, null, null);
            sobot_btn_no_robot.setTextColor(themeColor);
//                    sobot_btn_ok_robot.setCompoundDrawablesWithIntrinsicBounds(zan, null, null, null);
            sobot_btn_ok_robot.setTextColor(getResources().getColor(R.color.sobot_common_gray2));

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (changeThemeColor) {
            Drawable drawable = getResources().getDrawable(R.drawable.sobot_dialog_button_selector);
            if (b) {
                compoundButton.setBackground(ThemeUtils.applyColorToDrawable(drawable, themeColor));
                compoundButton.setTextColor(getResources().getColor(R.color.sobot_common_white));
            } else {
                compoundButton.setBackground(drawable);
                compoundButton.setTextColor(themeColor);
            }
        }
    }
}