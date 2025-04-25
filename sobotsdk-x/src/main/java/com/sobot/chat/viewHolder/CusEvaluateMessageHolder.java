package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.ResultCallBack;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.Information;
import com.sobot.chat.api.model.SatisfactionSet;
import com.sobot.chat.api.model.SatisfactionSetBase;
import com.sobot.chat.api.model.SobotEvaluateModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.api.model.ZhiChiMessageBase;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ToastUtil;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 客服主动邀请客户评价
 */
public class CusEvaluateMessageHolder extends MsgHolderBase implements RadioGroup.OnCheckedChangeListener,  CompoundButton.OnCheckedChangeListener {
    private TextView sobot_center_title;
    private RadioGroup sobot_readiogroup;
    private RadioButton sobot_btn_ok_robot;
    private RadioButton sobot_btn_no_robot;
    private TextView sobot_tv_star_title;
    private SobotFiveStarsLayout sobot_ratingBar;//评价  打分
    private LinearLayout sobot_ten_root_ll;//评价  十分全布局
    private TextView sobot_ten_very_dissatisfied;//评价 非常不满意
    private TextView sobot_ten_very_satisfaction;//评价  非常满意
    private SobotTenRatingLayout sobot_ten_rating_ll;//评价  十分 父布局 动态添加10个textview
    private int ratingType;//评价  类型   0 5星 ；1 十分 默认5星,2 二级评价
    private TextView sobot_ratingBar_title;//星星对应描述
    private TextView sobot_submit;//提交
    private View sobot_ratingBar_split_view;//如果有已解决按钮和未解决按钮就显示，否则隐藏；
    private Information information;
    private LinearLayout sobot_hide_layout;
    private SobotAntoLineLayout sobot_evaluate_lable_autoline;//评价 标签 自动换行
    private List<CheckBox> checkBoxList = new ArrayList<>();
    private SobotEvaluateModel sobotEvaluateModel;
    public ZhiChiMessageBase message;

    private SatisfactionSet mSatisfactionSet;//评价配置信息
    private List<SatisfactionSetBase> satisFactionList;
    private int deftaultScore = 0;
    private int themeColor;
    private boolean changeThemeColor;

    private LinearLayout ll_2_type;//二级评价
    private TextView sobot_btn_satisfied;//二级评价  满意
    private TextView sobot_btn_dissatisfied;//二级评价  不满意
    private View v_top;
    private TextView sobot_text_other_problem;//评价  机器人或人工客服存在哪些问题的标题

    //标签选中样式
    private GradientDrawable checkboxDrawable;

    public CusEvaluateMessageHolder(Context context, View convertView) {
        super(context, convertView);
        v_top = convertView.findViewById(R.id.v_top);
        sobot_text_other_problem = convertView.findViewById(R.id.sobot_text_other_problem);
        sobot_center_title =  convertView.findViewById(R.id.sobot_center_title);
        sobot_readiogroup = convertView.findViewById(R.id.sobot_readiogroup);
         sobot_btn_ok_robot =  convertView.findViewById(R.id.sobot_btn_ok_robot);
        sobot_btn_ok_robot.setText(context.getResources().getString(R.string.sobot_evaluate_yes));
        sobot_btn_no_robot =   convertView.findViewById(R.id.sobot_btn_no_robot);
        sobot_btn_no_robot.setText(R.string.sobot_evaluate_no);
        sobot_tv_star_title = (TextView) convertView.findViewById(R.id.sobot_tv_star_title);
        sobot_tv_star_title.setText(R.string.sobot_please_evaluate);
        sobot_ratingBar =  convertView.findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = convertView.findViewById(R.id.sobot_ten_root_ll);
        sobot_ten_very_dissatisfied = convertView.findViewById(R.id.sobot_ten_very_dissatisfied);
        sobot_ten_very_satisfaction = convertView.findViewById(R.id.sobot_ten_very_satisfaction);
        sobot_ten_very_dissatisfied.setText(R.string.sobot_very_dissatisfied);
        sobot_ten_very_satisfaction.setText(R.string.sobot_great_satisfaction);
        ll_2_type = convertView.findViewById(R.id.ll_2_type);
        sobot_btn_satisfied = convertView.findViewById(R.id.sobot_btn_satisfied);
        sobot_btn_dissatisfied = convertView.findViewById(R.id.sobot_btn_dissatisfied);
        sobot_ten_rating_ll = convertView.findViewById(R.id.sobot_ten_rating_ll);
        sobot_submit = (TextView) convertView.findViewById(R.id.sobot_submit);
        sobot_submit.setText(R.string.sobot_btn_submit_text);
        sobot_ratingBar_split_view = convertView.findViewById(R.id.sobot_ratingBar_split_view);
        sobot_ratingBar_title = (TextView) convertView.findViewById(R.id.sobot_ratingBar_title);
        sobot_ratingBar_title.setText(R.string.sobot_great_satisfaction);
        sobot_hide_layout = (LinearLayout) convertView.findViewById(R.id.sobot_hide_layout);
        sobot_evaluate_lable_autoline = convertView.findViewById(R.id.sobot_evaluate_lable_autoline);
        changeThemeColor = ThemeUtils.isChangedThemeColor(context);
        if (changeThemeColor) {
            themeColor = ThemeUtils.getThemeColor(context);
            sobot_submit.setTextColor(themeColor);
        }
        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
            String themeColor[] = initMode.getVisitorScheme().getRebotTheme().split(",");
            if (themeColor.length > 1) {
                if (mContext.getResources().getColor(R.color.sobot_gradient_start) != Color.parseColor(themeColor[0]) || mContext.getResources().getColor(R.color.sobot_gradient_end) != Color.parseColor(themeColor[1])) {
                    int[] colors = new int[themeColor.length];
                    for (int i = 0; i < themeColor.length; i++) {
                        colors[i] = Color.parseColor(themeColor[i]);
                    }
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                    if (v_top != null) {
                        v_top.setBackground(aDrawable);
                    }
                } else {
                    int[] colors = new int[]{mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_start), mContext.getResources().getColor(R.color.sobot_chat_right_bgColor_end)};
                    GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                    aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
                    if (v_top != null) {
                        v_top.setBackground(aDrawable);
                    }
                }
            }
        }
        sobot_readiogroup.setOnCheckedChangeListener(this);

        sobot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SatisfactionSetBase satisfactionSetBase = getSatisFaction(deftaultScore, satisFactionList);
                if (ratingType == 0) {
                    if (satisFactionList != null && satisFactionList.size() == 5
                            && satisFactionList.get(4).getIsInputMust()) {
                        //校验5星评价建议是否必填写，如果是，弹出评价pop再去提交
                        doEvaluate(false, deftaultScore);
                        return;
                    }
                    //校验评5星评价标签是否必选
                    if (TextUtils.isEmpty(checkBoxIsChecked()) && satisFactionList != null && satisFactionList.size() == 5
                            && satisFactionList.get(4).getIsTagMust()
                            && !TextUtils.isEmpty(satisFactionList.get(4).getLabelName()) && !information.isHideManualEvaluationLabels()) {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                        return;
                    }
                } else if(ratingType == 1){
                    if (deftaultScore >= 0 && satisFactionList != null && satisFactionList.size() == 11 && deftaultScore < satisFactionList.size()
                            && satisfactionSetBase.getIsInputMust()) {
                        //校验10分评价建议是否必填写，如果是，弹出评价pop再去提交
                        doEvaluate(false, deftaultScore);
                        return;
                    }
                    //校验评价标签是否必选
                    if (TextUtils.isEmpty(checkBoxIsChecked()) && satisFactionList != null && satisFactionList.size() == 11 && deftaultScore >= 0 && deftaultScore < satisFactionList.size()
                            && satisfactionSetBase.getIsTagMust()
                            && !TextUtils.isEmpty(satisfactionSetBase.getLabelName()) && !information.isHideManualEvaluationLabels()) {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                        return;
                    }

                }else if(ratingType == 2){
                    int score = 0;
                    if (sobot_btn_satisfied.isSelected()) {
                        score =5;
                    }else if(sobot_btn_dissatisfied.isSelected()){
                        score =1;
                    }
                    deftaultScore = score;
                    if (deftaultScore >= 0   && satisfactionSetBase.getIsInputMust()) {
                        doEvaluate(false, deftaultScore);
                        return;
                    }
                    //校验评价标签是否必选
                    if (TextUtils.isEmpty(checkBoxIsChecked())
                            && satisfactionSetBase.getIsTagMust()
                            && !TextUtils.isEmpty(satisfactionSetBase.getLabelName()) ) {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                        return;
                    }
                }
                if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
                    int tempResolved = -1;
                    if (sobot_btn_ok_robot.isChecked()) {
                        tempResolved = 1;
                    } else if (sobot_btn_no_robot.isChecked()) {
                        tempResolved = 0;
                    }
                    //“问题是否解决”是否为必填选项： 0-非必填 1-必填
                    if (tempResolved == -1 && mSatisfactionSet.getIsQuestionMust() == 1) {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_str_please_check_is_solve));//标签必选
                        return;
                    }
                }
                // true 直接提交  false 打开评价窗口 显示提交 肯定是5星
                doEvaluate(true, deftaultScore);
            }
        });
        sobot_ten_rating_ll.setOnClickItemListener(new SobotTenRatingLayout.OnClickItemListener() {
            @Override
            public void onClickItem(int selectIndex) {
                if (sobotEvaluateModel != null && 0 == sobotEvaluateModel.getEvaluateStatus() && selectIndex >= 0) {
                    //未评价时进行评价
                    sobotEvaluateModel.setScore(selectIndex);
                    doEvaluate(false, selectIndex);
                }
            }
        });
        sobot_btn_satisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEvaluate(false, 5);
//                int score = 0;
//                if (sobot_btn_satisfied.isSelected()) {
//                    score =5;
//                }else if(sobot_btn_dissatisfied.isSelected()){
//                    score =1;
//                }
//                deftaultScore =score;
//                sobotEvaluateModel.setScore(score);

//                SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
//                if(satisfactionSetBase!=null) {
//                    if (satisfactionSetBase != null && !TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
//                        String tmpData[] = convertStrToArray(satisfactionSetBase.getLabelName());
//                        setLableViewVisible(tmpData);
//                    } else {
//                        setLableViewVisible(null);
//                    }
//                }else{
//                    setLableViewVisible(null);
//
//                }
            }
        });
        sobot_btn_dissatisfied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doEvaluate(false, 1);
//                int score = 0;
//                if (sobot_btn_satisfied.isSelected()) {
//                    score =5;
//                }else if(sobot_btn_dissatisfied.isSelected()){
//                    score =1;
//                }
//                deftaultScore =score;
//                sobotEvaluateModel.setScore(score);

                /*SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
                if(satisfactionSetBase!=null) {
                    if (satisfactionSetBase != null && !TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
                        String tmpData[] = convertStrToArray(satisfactionSetBase.getLabelName());
                        setLableViewVisible(tmpData);
                    } else {
                        setLableViewVisible(null);
                    }
                }else{
                    setLableViewVisible(null);
                }*/
            }
        });
        sobot_ratingBar.setOnClickItemListener(new SobotFiveStarsLayout.OnClickItemListener() {
            @Override
            public void onClickItem(int selectIndex) {
                int score = selectIndex+1;
                if (score > 5) {
                    score = 5;
                }
                if (score < 0 ) {
                    score=0;
                }
                LogUtils.i(sobotEvaluateModel.getScore() + "-----" + deftaultScore + "=====" + score);
                if (sobotEvaluateModel != null && 0 == sobotEvaluateModel.getEvaluateStatus() && score > 0) {
                    //未评价时进行评价
                    sobotEvaluateModel.setScore(score);

                    doEvaluate(false, score);
                }
            }
        });
    }

    @Override
    public void bindData(final Context context, final ZhiChiMessageBase message) {
        information = (Information) SharedPreferencesUtil.getObject(context, "sobot_last_current_info");
        if (!information.isHideManualEvaluationLabels()) {
            sobot_ratingBar_title.setVisibility(View.VISIBLE);
        } else {
            sobot_ratingBar_title.setVisibility(View.GONE);
        }
        this.message = message;
        boolean refrashSatisfactionConfig = SharedPreferencesUtil.getBooleanData(mContext, "refrashSatisfactionConfig", false);
        this.sobotEvaluateModel = message.getSobotEvaluateModel();
        if (refrashSatisfactionConfig) {
            SharedPreferencesUtil.saveBooleanData(mContext, "refrashSatisfactionConfig", false);
            satisFactionList = null;
        }
        if (satisFactionList == null || satisFactionList.size() == 0) {
            //2.8.5 获取人工满意度配置信息，默认几星和5星时展示对应标签
            ZhiChiApi zhiChiApi = SobotMsgManager.getInstance(context).getZhiChiApi();
            ZhiChiInitModeBase initMode = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(context,
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initMode != null) {

                zhiChiApi.satisfactionMessage(CusEvaluateMessageHolder.this, initMode.getPartnerid(), new ResultCallBack<SatisfactionSet>() {
                    @Override
                    public void onSuccess(SatisfactionSet satisfactionSet) {
                        if (satisfactionSet != null) {
                            mSatisfactionSet = satisfactionSet;
                            satisFactionList = satisfactionSet.getList();
                            sobotEvaluateModel.setIsResolved(satisfactionSet.getDefaultQuestionFlag());
                            showData();
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        sobot_submit.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                    }
                });
            }
        } else {
            showData();
        }
        refreshReadStatus();
    }

    private void showData() {
        int score = 0;
        if (mSatisfactionSet.getScoreFlag() == 0) {
            //defaultType 0-默认5星,1-默认0星
            score = (mSatisfactionSet.getDefaultType() == 0) ? 5 : 0;
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
            sobot_ratingBar.init(score,false,32);
            sobot_ten_root_ll.setVisibility(View.GONE);
            ll_2_type.setVisibility(View.GONE);
            sobot_ratingBar.setVisibility(View.VISIBLE);
            ratingType = 0;//5星
            if (mSatisfactionSet.getDefaultType() == 0 && score > 0) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
        } else if(mSatisfactionSet.getScoreFlag() == 1){
            //十分
            sobot_ten_root_ll.setVisibility(View.VISIBLE);
            ll_2_type.setVisibility(View.GONE);
            sobot_ratingBar.setVisibility(View.GONE);
            ratingType = 1;//十分
            //0-10分，1-5分，2-0分，3-不选中
            if (mSatisfactionSet.getDefaultType() == 2) {
                score = 0;
            } else if (mSatisfactionSet.getDefaultType() == 1) {
                score = 5;
            } else if (mSatisfactionSet.getDefaultType() == 3) {
                score = -1;
            } else {
                score = 10;
            }
            if (mSatisfactionSet.getDefaultType() != 3) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
            if (sobot_ten_rating_ll.isInit()) {
                sobot_ten_rating_ll.init(score, false, 20);
            }
        }else if(mSatisfactionSet.getScoreFlag() == 2){
            //二级评价
            ratingType = 2;//二级评价
            sobot_ten_root_ll.setVisibility(View.GONE);
            ll_2_type.setVisibility(View.VISIBLE);
            sobot_ratingBar.setVisibility(View.GONE);
            //二级评价
            //0-满意，1-不满意，2-不选中
            if (mSatisfactionSet.getDefaultType() == 0) {
                score = 5;
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_icon_manyi_sel);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    sobot_btn_satisfied.setCompoundDrawables(null, img,null,  null);
                }
                sobot_btn_satisfied.setSelected(true);
                sobot_btn_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_first));
                sobot_btn_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_third));
                sobot_btn_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                sobot_btn_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            } else if (mSatisfactionSet.getDefaultType() == 1) {
                score = 1;
                Drawable img = mContext.getResources().getDrawable(R.drawable.sobot_icon_no_manyi_sel);
                if (img != null) {
                    img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
                    sobot_btn_dissatisfied.setCompoundDrawables(null, img,null,  null);
                }
                sobot_btn_dissatisfied.setSelected(true);
                sobot_btn_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_first));
                sobot_btn_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_third));
                sobot_btn_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                sobot_btn_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            } else if (mSatisfactionSet.getDefaultType() == 2) {
                score = -1;
            }
            if ( score > 0) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
            deftaultScore = score;
        }

        SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
        if(satisfactionSetBase!=null) {
            if (ratingType == 0) {
                if (0 == score) {
                    sobot_hide_layout.setVisibility(View.GONE);
                    sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_third));
                } else {
                    //根据infomation 配置是否隐藏人工评价标签
                    if (!information.isHideManualEvaluationLabels()) {
                        sobot_hide_layout.setVisibility(View.VISIBLE);
                    } else {
                        sobot_hide_layout.setVisibility(View.GONE);
                    }
                    if (satisFactionList != null && satisFactionList.size() == 5) {
                        sobot_ratingBar_title.setText(satisFactionList.get(4).getScoreExplain());
                        sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_evaluate_ratingBar_des_tv));
                    }
                }
            } else if (ratingType == 1) {
                //根据infomation 配置是否隐藏人工评价标签
                if (!information.isHideManualEvaluationLabels()) {
                    sobot_hide_layout.setVisibility(View.VISIBLE);
                } else {
                    sobot_hide_layout.setVisibility(View.GONE);
                }
                if (-1 == score) {
                    sobot_hide_layout.setVisibility(View.GONE);
                    sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_third));
                } else {
                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_evaluate_ratingBar_des_tv));
                }
            } else if (ratingType == 2) {
                //
                //根据infomation 配置是否隐藏人工评价标签
                if (!information.isHideManualEvaluationLabels()) {
                    sobot_hide_layout.setVisibility(View.VISIBLE);
                } else {
                    sobot_hide_layout.setVisibility(View.GONE);
                }
                sobot_ratingBar_title.setVisibility(View.GONE);
                if (-1 == score) {
                    sobot_hide_layout.setVisibility(View.GONE);
                    sobot_ratingBar_title.setText(R.string.sobot_evaluate_zero_score_des);
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_text_third));
                } else {
                    sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
                    sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_color_evaluate_ratingBar_des_tv));
                }
            }
        }
        //是否是默认提交按钮
        if(mSatisfactionSet!=null && mSatisfactionSet.getIsDefaultButton()==0 && !TextUtils.isEmpty(mSatisfactionSet.getButtonDesc())){
            sobot_submit.setText(mSatisfactionSet.getButtonDesc());
        }
        //标签引导语
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

        if (satisfactionSetBase != null && satisfactionSetBase.getTags() != null) {
            String[] tmpData = new String[satisfactionSetBase.getTags().size()];
            for (int i = 0; i < satisfactionSetBase.getTags().size(); i++) {
                tmpData[i] = satisfactionSetBase.getTags().get(i).getLabelName();
            }
            setLableViewVisible(tmpData);
        } else if  (satisfactionSetBase != null && !TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
            String tmpData[] = convertStrToArray(satisfactionSetBase.getLabelName());
            setLableViewVisible(tmpData);
        } else {
            setLableViewVisible(null);
        }

        sobot_center_title.setText(message.getSenderName() + " " + mContext.getResources().getString(R.string.sobot_question));
        sobot_tv_star_title.setText(message.getSenderName() + " " + mContext.getResources().getString(R.string.sobot_please_evaluate));

        checkQuestionFlag();
        refreshItem();

    }

    /**
     * 检查是否开启   是否已解决配置
     */
    private void checkQuestionFlag() {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (ChatUtils.isQuestionFlag(sobotEvaluateModel)) {
            //是否已解决开启
            sobot_center_title.setVisibility(View.VISIBLE);
            //判断已解决 未解决长度是否相等
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int width1 = sobot_btn_ok_robot.getMeasuredWidth();
                    int width2 = sobot_btn_no_robot.getMeasuredWidth();
                    sobot_btn_ok_robot.getPaddingStart();
                    if (width1 < width2) {
                        int pading = (width2-width1)/2+ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading=="+pading+"====16="+ScreenUtils.dip2px(mContext, 16));
                        sobot_btn_ok_robot.setPadding(pading, paddingTop,pading ,paddingTop );
                    } else if (width1 > width2) {
                        int pading = (width1-width2)/2+ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading=="+pading+"====16="+ScreenUtils.dip2px(mContext, 16));
                        sobot_btn_no_robot.setPadding(pading,paddingTop ,pading ,paddingTop );
                    }
                }
            });
            sobot_readiogroup.setVisibility(View.VISIBLE);
            sobot_btn_ok_robot.setVisibility(View.VISIBLE);
            sobot_btn_no_robot.setVisibility(View.VISIBLE);
            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);

        } else {
//            是否已解决关闭
            sobot_center_title.setVisibility(View.GONE);
            sobot_readiogroup.setVisibility(View.GONE);
            sobot_ratingBar_split_view.setVisibility(View.GONE);
        }
    }


    /**
     * 根据是否已经评价设置UI
     */
    public void refreshItem() {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (0 == sobotEvaluateModel.getEvaluateStatus()) {
            //未评价
            setNotEvaluatedLayout();
        } else if (1 == sobotEvaluateModel.getEvaluateStatus()) {
            //已评价
            setEvaluatedLayout();
        }
    }

    private void setEvaluatedLayout() {
//        if (sobot_readiogroup.getVisibility() == View.VISIBLE) {
//            sobot_btn_ok_robot.setVisibility(View.VISIBLE);
//            sobot_btn_no_robot.setVisibility(View.VISIBLE);
//            if (sobotEvaluateModel.getIsResolved() == 0) {
//                sobot_btn_ok_robot.setChecked(false);
//                sobot_btn_no_robot.setChecked(true);
//            } else if (sobotEvaluateModel.getIsResolved() == 1) {
//                sobot_btn_ok_robot.setChecked(true);
//                sobot_btn_no_robot.setChecked(false);
//            }
//        }
//        sobot_ratingBar.setRating(sobotEvaluateModel.getScore());
        sobot_ratingBar.setEnabled(false);
    }

    private void setNotEvaluatedLayout() {
        if (sobotEvaluateModel == null) {
            return;
        }
//        if (sobot_readiogroup.getVisibility() == View.VISIBLE) {
            sobot_btn_ok_robot.setVisibility(View.VISIBLE);
            sobot_btn_no_robot.setVisibility(View.VISIBLE);
            //是否解决问题 0:已解决，1：未解决，-1：都不选
            if (sobotEvaluateModel.getIsResolved() == 0) {
                sobot_btn_ok_robot.setChecked(true);
                sobot_btn_no_robot.setChecked(false);
            } else if (sobotEvaluateModel.getIsResolved() == 1) {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(true);
            } else if (sobotEvaluateModel.getIsResolved() == -1) {
                sobot_btn_ok_robot.setChecked(false);
                sobot_btn_no_robot.setChecked(false);
            }
//        }
        sobot_ratingBar.setEnabled(true);
    }

    /**
     * 评价 操作
     *
     * @param evaluateFlag true 直接提交  false 打开评价窗口
     */
    private void doEvaluate(boolean evaluateFlag, int score) {
        if (mContext != null && message != null && message.getSobotEvaluateModel() != null) {
            message.getSobotEvaluateModel().setIsResolved(getResovled());
            message.getSobotEvaluateModel().setScore(score);
            message.getSobotEvaluateModel().setScoreFlag(ratingType);
            SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
            if(satisfactionSetBase!=null){
                message.getSobotEvaluateModel().setScoreExplainLan(satisfactionSetBase.getScoreExplainLan());
                message.getSobotEvaluateModel().setScoreExplain(satisfactionSetBase.getScoreExplain());
                message.getSobotEvaluateModel().setTagsJson(getCheckedLable(score));
            }
            if (msgCallBack != null) {
                msgCallBack.doEvaluate(evaluateFlag, message);
            }
        }
    }

    private int getResovled() {
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            if (sobot_btn_ok_robot.isChecked()) {
                return 0;
            } else if (sobot_btn_no_robot.isChecked()) {
                return 1;
            } else {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (sobotEvaluateModel == null) {
            return;
        }
        if (checkedId == sobot_btn_ok_robot.getId()) {
            sobotEvaluateModel.setIsResolved(0);
            sobot_btn_ok_robot.setChecked(true);
            sobot_btn_no_robot.setChecked(false);
            sobot_btn_ok_robot.setSelected(true);
            sobot_btn_no_robot.setSelected(false);
        }
        if (checkedId == sobot_btn_no_robot.getId()) {
            sobotEvaluateModel.setIsResolved(1);
            sobot_btn_ok_robot.setChecked(false);
            sobot_btn_no_robot.setChecked(true);
            sobot_btn_ok_robot.setSelected(false);
            sobot_btn_no_robot.setSelected(true);
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

    // 使用String的split 方法把字符串截取为字符串数组
    private static String[] convertStrToArray(String str) {
        String[] strArray = null;
        if (!TextUtils.isEmpty(str)) {
            strArray = str.split(","); // 拆分字符为"," ,然后把结果交给数组strArray
        }
        return strArray;
    }

    //设置评价标签的显示逻辑
    private void setLableViewVisible(String tmpData[]) {
        if (tmpData == null) {
            sobot_hide_layout.setVisibility(View.GONE);
            return;
        } else {
            //根据infomation 配置是否隐藏人工评价标签
            if (!information.isHideManualEvaluationLabels()) {
                sobot_hide_layout.setVisibility(View.VISIBLE);
            } else {
                sobot_hide_layout.setVisibility(View.GONE);
            }
        }

        createChildLableView(sobot_evaluate_lable_autoline, tmpData);
    }

    //隐藏所有自动换行的标签
    private void createChildLableView(SobotAntoLineLayout antoLineLayout, String tmpData[]) {
        if (antoLineLayout != null) {
            antoLineLayout.removeAllViews();
            checkBoxList.clear();
            for (int i = 0; i < tmpData.length; i++) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.sobot_layout_evaluate_item, null);
                CheckBox checkBox = view.findViewById(R.id.sobot_evaluate_cb_lable);
                //左侧（左间距18+内间距15+antoLineLayout 外间距20）* 2 +antoLineLayout 子控件行间距10
                //新版UI 根据内容显示宽度
//                checkBox.setMinWidth((ScreenUtil.getScreenSize(mContext)[0] - ScreenUtils.dip2px(mContext, (18 + 15 + 20) * 2 + 10)) / 2);
                checkBox.setText(tmpData[i]);
                checkBox.setOnCheckedChangeListener(this);
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (changeThemeColor) {
            if (b) {
                StateListDrawable stateListDrawable = (StateListDrawable) ContextCompat.getDrawable(mContext, R.drawable.sobot_btn_bg_lable_select);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    for (int i = 0; i < stateListDrawable.getStateCount(); i++) {
                        Drawable drawable = stateListDrawable.getStateDrawable(i);
                        if (drawable instanceof GradientDrawable) {
                            // 修改边框颜色
                            GradientDrawable shapeDrawable = (GradientDrawable) drawable;
                            shapeDrawable.setStroke(2, themeColor); // 修改边框的宽度和颜色
                            shapeDrawable.setColor(ThemeUtils.modifyAlpha(themeColor,10));
                        }
                    }
                }
                compoundButton.setTextColor(themeColor);
                compoundButton.setBackground(stateListDrawable);

            } else {
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.sobot_bg_cai_reason_lable_checkbox_bg);
                compoundButton.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_first));
                compoundButton.setBackground(drawable);
            }
        }
    }


    //检测选中的标签
    private String checkBoxIsChecked() {
        String str = new String();
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
    //检测选中的标签
    private String getCheckedLable(int sorce) {
        SatisfactionSetBase satisfactionSetBase = getSatisFaction(sorce, satisFactionList);
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
                return array.toString();
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}