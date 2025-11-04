package com.sobot.chat.viewHolder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.viewHolder.base.MsgHolderBase;
import com.sobot.chat.widget.SobotAntoLineLayout;
import com.sobot.chat.widget.SobotEditTextLayout;
import com.sobot.chat.widget.SobotFiveStarsLayout;
import com.sobot.chat.widget.SobotTenRatingLayout;
import com.sobot.chat.widget.toast.ToastUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 客服主动邀请客户评价
 */
public class CusEvaluateMessageHolder extends MsgHolderBase implements View.OnClickListener {
    // ==============已解决、未解决 start==========
    private LinearLayout sobot_readiogroup,sobot_ll_ok_robot,sobot_ll_no_robot;//已解决、为解决
    private TextView sobot_btn_ok_robot;//评价  已解决
    private TextView sobot_btn_no_robot;//评价  未解决
    private ImageView iv_solved,iv_no_solve;
    //    ============已解决、为解决 end===========
    private TextView sobot_tv_star_title;
    private SobotFiveStarsLayout sobot_ratingBar;//评价  打分
    private LinearLayout sobot_ten_root_ll;//评价  十分全布局
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
    private int themeColor;
    private boolean changeThemeColor;

    //=======二级评价===start==
    private LinearLayout ll_2_type;//二级评价
    private LinearLayout sobot_btn_satisfied;//二级评价  满意
    private LinearLayout sobot_btn_dissatisfied;//二级评价  不满意
    private ImageView iv_satisfied,iv_dissatisfied;
    private TextView tv_satisfied,tv_dissatisfied;
    //==========二级评价===end======
    private TextView sobot_text_other_problem;//评价  机器人或人工客服存在哪些问题的标题
    private SobotEditTextLayout setl_submit_content;//评价框
    private EditText sobot_add_content;//评价  添加建议

    //标签选中样式
    private GradientDrawable checkboxDrawable;

    public CusEvaluateMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_text_other_problem = convertView.findViewById(R.id.sobot_text_other_problem);
        setl_submit_content = convertView.findViewById(R.id.setl_submit_content);
        sobot_add_content = convertView.findViewById(R.id.sobot_add_content);
        sobot_add_content.setHint(R.string.sobot_edittext_hint);
        sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Drawable db = ResourcesCompat.getDrawable( mContext.getResources(),R.drawable.sobot_bg_evaluate_input,null);
                if(hasFocus) {
                    if (db != null) {
                        setl_submit_content.setBackground(ThemeUtils.applyColorToDrawable(db, themeColor));
                    }
                }else{
                    setl_submit_content.setBackground(db);
                }
            }
        });
        //是否已解决
        sobot_readiogroup = convertView.findViewById(R.id.sobot_readiogroup);
        sobot_ll_ok_robot = convertView.findViewById(R.id.sobot_ll_ok_robot);
        sobot_ll_no_robot = convertView.findViewById(R.id.sobot_ll_no_robot);
        sobot_ll_ok_robot.setOnClickListener(this);
        sobot_ll_no_robot.setOnClickListener(this);
        sobot_btn_ok_robot = convertView.findViewById(R.id.sobot_btn_ok_robot);
        sobot_btn_no_robot = convertView.findViewById(R.id.sobot_btn_no_robot);
        iv_solved = convertView.findViewById(R.id.iv_solved);
        iv_no_solve = convertView.findViewById(R.id.iv_no_solve);
        sobot_btn_ok_robot.setText(context.getResources().getString(R.string.sobot_evaluate_yes));
        sobot_btn_no_robot.setText(R.string.sobot_evaluate_no);

        sobot_tv_star_title = (TextView) convertView.findViewById(R.id.sobot_tv_star_title);
        sobot_tv_star_title.setText(R.string.sobot_please_evaluate);
        sobot_ratingBar =  convertView.findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = convertView.findViewById(R.id.sobot_ten_root_ll);

        //二级评价
        ll_2_type = convertView.findViewById(R.id.ll_2_type);
        iv_satisfied = convertView.findViewById(R.id.iv_satisfied);
        iv_dissatisfied = convertView.findViewById(R.id.iv_dissatisfied);
        tv_satisfied = convertView.findViewById(R.id.tv_satisfied);
        tv_dissatisfied = convertView.findViewById(R.id.tv_dissatisfied);
        sobot_btn_satisfied = convertView.findViewById(R.id.sobot_btn_satisfied);
        sobot_btn_dissatisfied = convertView.findViewById(R.id.sobot_btn_dissatisfied);
        sobot_btn_satisfied.setOnClickListener(this);
        sobot_btn_dissatisfied.setOnClickListener(this);

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
            Drawable bg = sobot_submit.getBackground();
            sobot_submit.setBackground(ThemeUtils.applyColorToDrawable(bg, themeColor));
            sobot_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(mContext));
        }
        if (!TextUtils.isEmpty(initMode.getVisitorScheme().getRebotTheme())) {
            String themeColorStr = initMode.getVisitorScheme().getRebotTheme();
            if (!themeColorStr.contains(",")) {
                //单色 需要变成两个一样
                themeColorStr = themeColorStr + "," + themeColorStr;
            }
            String themeColorArr[] = themeColorStr.split(",");
            if (themeColorArr.length > 1) {
                int[] colors = new int[themeColorArr.length];
                for (int i = 0; i < themeColorArr.length; i++) {
                    colors[i] = Color.parseColor(themeColorArr[i]);
                }
                GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                aDrawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.sobot_msg_corner_radius));
            }
        }

        sobot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deftaultScore = sobotEvaluateModel==null ?-1:sobotEvaluateModel.getScore();
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
                    //“问题是否解决”是否为必填选项： 0-非必填 1-必填
                    if ( sobotEvaluateModel.getIsResolved() == -1 && mSatisfactionSet.getIsQuestionMust() == 1) {
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
                    setCustomLayoutViewVisible(selectIndex, satisFactionList);
                }
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
                sobot_add_content.clearFocus();
                sobotEvaluateModel.setScore(score);
                //显示标签
                setCustomLayoutViewVisible(score, satisFactionList);
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
        int deftaultScore = sobotEvaluateModel==null ?-1:sobotEvaluateModel.getIsResolved();
        if (mSatisfactionSet.getScoreFlag() == 0) {
            //defaultType 0-默认5星,1-默认0星
            score = (mSatisfactionSet.getDefaultType() == 0) ? 5 : 0;
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
            sobot_ratingBar.init(score,false,35);
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
                sobot_ten_rating_ll.init(score, true, 16);
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
                iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 43);
                iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 43);
                iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 35);
                iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 35);
                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
                tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                tv_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_common_hese));
                tv_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_second));
            } else if (mSatisfactionSet.getDefaultType() == 1) {
                score = 1;
                iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 35);
                iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(mContext, 35);
                iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 43);
                iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 43);
                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);
                tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                tv_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_second));
                tv_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_common_hese));
            } else if (mSatisfactionSet.getDefaultType() == 2) {
                score = -1;
            }
            if ( score > 0) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
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
            if (StringUtils.isNoEmpty(satisfactionSetBase.getInputLanguage())) {
                if (satisfactionSetBase.getIsInputMust()) {
                    sobot_add_content.setHint(mContext.getResources().getString(R.string.sobot_required) + satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                } else {
                    sobot_add_content.setHint(satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                }
            } else {
                sobot_add_content.setHint(mContext.getResources().getString(R.string.sobot_edittext_hint));
            }
        }
        //是否显示评价输入框
        if (mSatisfactionSet.getTxtFlag() == 0) {
            //关闭评价输入框
            setl_submit_content.setVisibility(View.GONE);
        } else {
            setl_submit_content.setVisibility(View.VISIBLE);
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
            //是否已解决开启 判断已解决 未解决长度是否相等
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    int width1 = sobot_ll_ok_robot.getMeasuredWidth();
                    int width2 = sobot_ll_no_robot.getMeasuredWidth();
                    sobot_ll_ok_robot.getPaddingStart();
                    if (width1 < width2) {
                        int pading = (width2-width1)/2+ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading=="+pading+"====16="+ScreenUtils.dip2px(mContext, 16));
                        sobot_ll_ok_robot.setPadding(pading, paddingTop,pading ,paddingTop );
                    } else if (width1 > width2) {
                        int pading = (width1-width2)/2+ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading=="+pading+"====16="+ScreenUtils.dip2px(mContext, 16));
                        sobot_ll_no_robot.setPadding(pading,paddingTop ,pading ,paddingTop );
                    }
                }
            });
            sobot_readiogroup.setVisibility(View.VISIBLE);
            sobot_btn_ok_robot.setVisibility(View.VISIBLE);
            sobot_btn_no_robot.setVisibility(View.VISIBLE);
            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);

        } else {
//            是否已解决关闭
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
        sobot_btn_ok_robot.setVisibility(View.VISIBLE);
        sobot_btn_no_robot.setVisibility(View.VISIBLE);
        //是否解决问题 0:已解决，1：未解决，-1：都不选
        if (sobotEvaluateModel.getIsResolved() == 0) {
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
            sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
        } else if (sobotEvaluateModel.getIsResolved() == 1) {
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
            sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
        } else if (sobotEvaluateModel.getIsResolved() == -1) {
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(false);
            sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
            sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
        }
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
                message.getSobotEvaluateModel().setLabels(checkBoxIsChecked());
                message.getSobotEvaluateModel().setProblem(sobot_add_content.getText().toString());
            }
            if (msgCallBack != null) {
                msgCallBack.doEvaluate(evaluateFlag, message);
            }
        }
    }

    private int getResovled() {
        if (sobotEvaluateModel == null) {
            return -1;
        }
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            return sobotEvaluateModel.getIsResolved();
        }
        return -1;
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
                antoLineLayout.addView(view);
                checkBoxList.add(checkBox);
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

    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId() == R.id.sobot_btn_satisfied) {
            sobotEvaluateModel.setScore(5);
            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 43);
            iv_satisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 43);
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 35);;
            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 35);;
            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_common_hese));
            tv_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_second));
        } else if (v.getId() == R.id.sobot_btn_dissatisfied) {
            iv_satisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 35);;
            iv_satisfied.getLayoutParams().height = ScreenUtils.dip2px(mContext, 35);;
            iv_dissatisfied.getLayoutParams().width= ScreenUtils.dip2px(mContext, 43);
            iv_dissatisfied.getLayoutParams().height= ScreenUtils.dip2px(mContext, 43);
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);
            tv_satisfied.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_dissatisfied.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv_satisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_color_text_second));
            tv_dissatisfied.setTextColor(mContext.getResources().getColor(R.color.sobot_common_hese));
            sobotEvaluateModel.setScore(1);
        }
        if (v.getId()  == R.id.sobot_ll_ok_robot) {
            sobotEvaluateModel.setIsResolved(0);
            // 获取系统默认的加粗字体
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_btn_ok_robot.setTypeface(null, Typeface.BOLD);
            sobot_btn_no_robot.setTypeface(null, Typeface.NORMAL);
        } else if (v.getId()  == R.id.sobot_ll_no_robot) {
            sobotEvaluateModel.setIsResolved(1);
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_btn_ok_robot.setTypeface(null, Typeface.NORMAL);
            sobot_btn_no_robot.setTypeface(null, Typeface.BOLD);
        }
    }
    private void setCustomLayoutViewVisible(int score, List<SatisfactionSetBase> satisFactionList) {
        SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
        for (int i = 0; i < checkBoxList.size(); i++) {
            checkBoxList.get(i).setChecked(false);
        }
        if (satisfactionSetBase != null) {
            sobot_ratingBar_title.setVisibility(View.VISIBLE);
            sobot_ratingBar_title.setText(satisfactionSetBase.getScoreExplain());
            sobot_ratingBar_title.setTextColor(ContextCompat.getColor(mContext, R.color.sobot_ten_evaluate_select));
            if (satisfactionSetBase.getTxtFlag() == 1) {
                setl_submit_content.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(satisfactionSetBase.getInputLanguage())) {
                    if (satisfactionSetBase.getIsInputMust()) {
                        sobot_add_content.setHint(mContext.getResources().getString(R.string.sobot_required) + satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    } else {
                        sobot_add_content.setHint(satisfactionSetBase.getInputLanguage().replace("<br/>", "\n"));
                    }
                } else {
                    sobot_add_content.setHint(mContext.getResources().getString(R.string.sobot_edittext_hint));
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
        } else {
            //根据infomation 配置是否隐藏星星评价描述
            if (!information.isHideManualEvaluationLabels()) {
                sobot_ratingBar_title.setVisibility(View.VISIBLE);
            } else {
                sobot_ratingBar_title.setVisibility(View.GONE);
            }
        }
    }
}