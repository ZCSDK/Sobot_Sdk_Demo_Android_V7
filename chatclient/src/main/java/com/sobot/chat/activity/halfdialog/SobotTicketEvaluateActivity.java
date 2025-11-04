package com.sobot.chat.activity.halfdialog;

import android.content.Intent;
import android.graphics.Typeface;
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
import com.sobot.chat.notchlib.utils.ScreenUtil;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.chat.widget.toast.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 评价界面的显示
 * Created by jinxl on 2017/6/12.
 */
public class SobotTicketEvaluateActivity extends SobotDialogBaseActivity implements  View.OnClickListener{
    private LinearLayout coustom_pop_layout;
    private LinearLayout sobot_robot_relative;//评价 机器人布局
    private LinearLayout sobot_custom_relative;//评价人工布局
    private LinearLayout sobot_hide_layout;//评价机器人和人工未解决时显示出来的布局
    private LinearLayout sobot_readiogroup,sobot_ll_ok_robot,sobot_ll_no_robot;//已解决、为解决
    private TextView sobot_btn_ok_robot;//评价  已解决
    private TextView sobot_btn_no_robot;//评价  未解决
    private ImageView iv_solved,iv_no_solve;
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
    private int ratingType;//评价  类型   0 5星 ；1 十分 默认5星

    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//评价 标签 自动换行 最多可以有六个
    private SobotEditTextLayout setl_submit_content;//评价框
    private int score;

    private int themeColor;
    private boolean changeThemeColor;

    private int isSolve = 2;//是否解决问题 1:已解决，0：未解决，2：都不选

    private List<CheckBox> checkBoxList = new ArrayList<>();
    private SobotUserTicketEvaluate mEvaluate;
    private SobotOrderScoreModel satisfactionSetBase;


    @Override
    protected int getContentViewResId() {
        return R.layout.sobot_layout_evaluate;
    }

    @Override
    protected void setRequestTag() {
        REQUEST_TAG = "SobotTicketEvaluateActivity";
    }

    @Override
    protected void initView() {
        super.initView();
        mEvaluate = (SobotUserTicketEvaluate) getIntent().getSerializableExtra("sobotUserTicketEvaluate");
        sobot_btn_submit = findViewById(R.id.sobot_close_now);
        sobot_btn_submit.setText(R.string.sobot_btn_submit_text);
        sobot_readiogroup = findViewById(R.id.sobot_readiogroup);
        sobot_tv_evaluate_title = (TextView) findViewById(R.id.sobot_tv_evaluate_title);
        //统一显示为服务评价
        sobot_tv_evaluate_title.setText(R.string.sobot_please_evaluate_this_service);
        sobot_robot_center_title = (TextView) findViewById(R.id.sobot_robot_center_title);
        String sobot_question = getResources().getString(R.string.sobot_question);
        sobot_robot_center_title.setText(String.format(sobot_question, "").trim());
        sobot_text_other_problem = (TextView) findViewById(R.id.sobot_text_other_problem);
        sobot_ratingBar_title = (TextView) findViewById(R.id.sobot_ratingBar_title);
        sobot_ratingBar_title.setText(R.string.sobot_great_satisfaction);
        sobot_tv_evaluate_title_hint = (TextView) findViewById(R.id.sobot_tv_evaluate_title_hint);
        sobot_evaluate_cancel = (TextView) findViewById(R.id.sobot_evaluate_cancel);
        sobot_evaluate_cancel.setText(R.string.sobot_temporarily_not_evaluation);
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
        sobot_btn_ok_robot = findViewById(R.id.sobot_btn_ok_robot);
        sobot_btn_no_robot = findViewById(R.id.sobot_btn_no_robot);
        iv_solved = findViewById(R.id.iv_solved);
        iv_no_solve = findViewById(R.id.iv_no_solve);
        sobot_btn_ok_robot.setText(R.string.sobot_evaluate_yes);
        sobot_btn_no_robot.setText(R.string.sobot_evaluate_no);
        sobot_robot_relative = (LinearLayout) findViewById(R.id.sobot_robot_relative);
        sobot_custom_relative = (LinearLayout) findViewById(R.id.sobot_custom_relative);
        sobot_hide_layout = (LinearLayout) findViewById(R.id.sobot_hide_layout);
        setl_submit_content = (SobotEditTextLayout) findViewById(R.id.setl_submit_content);
        changeThemeColor = ThemeUtils.isChangedThemeColor(this);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(this);
            Drawable bg = sobot_btn_submit.getBackground();
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
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
                // 获取系统默认的加粗字体
                sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
                sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
            } else if (mEvaluate.getDefaultQuestionFlag() == 0) {
                //(0)-未解决
                iv_solved.setSelected(false);
                iv_no_solve.setSelected(true);
                sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
                sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
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
                    sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
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
                    sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
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
            sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Drawable db = ResourcesCompat.getDrawable( getResources(),R.drawable.sobot_bg_evaluate_input,null);
                    if(hasFocus) {
                        if (db != null) {
                            setl_submit_content.setBackground(ThemeUtils.applyColorToDrawable(db, themeColor));
                        }
                    }else{
                        setl_submit_content.setBackground(db);
                    }
                }
            });
            setViewListener();
        }
    }

    @Override
    protected void initData() {

    }

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

    private void changeCommitButtonUi(boolean isCanClick) {
        if (changeThemeColor) {
            Drawable bg = sobot_btn_submit.getBackground();
            if (bg != null) {
                sobot_btn_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
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

    //设置人工客服评价的布局显示逻辑
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

    //设置评价标签的显示逻辑
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

    //检查标签是否选中（根据主动邀评传过来的选中标签判断）
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

    private boolean checkInput() {
        //如果开启了是否解决问题
        if (mEvaluate != null && mEvaluate.getIsQuestionFlag() == 1 && mEvaluate.getIsQuestionMust() == 1) {
            //“问题是否解决”是否为必填选项： 0-非必填 1-必填
            if (isSolve==2) {
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

    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId()  == R.id.sobot_ll_ok_robot) {
            isSolve = 1;
            // 获取系统默认的加粗字体
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
            sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
        } else if (v.getId()  == R.id.sobot_ll_no_robot) {
            isSolve = 0;
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
            sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
        }
    }
}