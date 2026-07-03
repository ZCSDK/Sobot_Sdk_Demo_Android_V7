package com.sobot.chat.viewHolder;

import android.content.Context;
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
 * 客服主动邀请客户评价 ViewHolder
 * <p>
 * 当客服主动发起满意度评价邀请时，在聊天列表中展示评价卡片。
 * 支持三种评价模式：
 * <ul>
 *   <li>五星评价（ratingType=0）：通过 {@link SobotFiveStarsLayout} 展示 1-5 星评分</li>
 *   <li>十分评价（ratingType=1）：通过 {@link SobotTenRatingLayout} 展示 0-10 分评分</li>
 *   <li>二级评价（ratingType=2）：满意/不满意两个选项</li>
 * </ul>
 * <p>
 * 评价卡片包含以下功能区域：
 * <ul>
 *   <li>"问题是否解决"选择区（已解决/未解决），受 {@link SatisfactionSet#getIsQuestionFlag()} 控制显隐</li>
 *   <li>评分区：根据 {@link SatisfactionSet#getScoreFlag()} 决定展示五星、十分或二级评价</li>
 *   <li>标签选择区：根据 {@link SatisfactionSetBase} 配置动态生成 CheckBox 标签</li>
 *   <li>文本输入区：用户补充建议，受 {@link SatisfactionSet#getTxtFlag()} 控制显隐</li>
 *   <li>提交按钮：校验必填项后调用 {@link #doEvaluate(boolean, int)} 提交评价</li>
 * </ul>
 * <p>
 * 数据绑定流程：
 * <ol>
 *   <li>{@link #bindData(Context, ZhiChiMessageBase)} 获取满意度配置（{@link SatisfactionSet}）</li>
 *   <li>{@link #showData()} 根据配置初始化评分组件、默认分值、标签和输入框</li>
 *   <li>用户交互后通过 {@link MsgHolderBase#msgCallBack} 回调评价结果</li>
 * </ol>
 *
 * @see MsgHolderBase
 * @see SobotEvaluateModel
 * @see SatisfactionSet
 */
public class CusEvaluateMessageHolder extends MsgHolderBase implements View.OnClickListener {
    // ==============已解决、未解决 start==========
    /**
     * 已解决/未解决 按钮容器
     */
    private LinearLayout sobot_readiogroup;
    /**
     * 已解决按钮布局
     */
    private LinearLayout sobot_ll_ok_robot;
    /**
     * 未解决按钮布局
     */
    private LinearLayout sobot_ll_no_robot;
    /**
     * 已解决图标
     */
    private ImageView iv_solved;
    /**
     * 未解决图标
     */
    private ImageView iv_no_solve;
    //    ============已解决、未解决 end===========

    /**
     * "问题是否解决"标题文本，可通过 SatisfactionSetBase 自定义文案
     */
    private TextView sobot_tv_star_title;
    /**
     * 五星评价控件（ratingType=0 时显示）
     */
    private SobotFiveStarsLayout sobot_ratingBar;
    /**
     * 十分评价根布局（ratingType=1 时显示）
     */
    private LinearLayout sobot_ten_root_ll;
    /**
     * 十分评价控件，动态添加 0-10 分的 TextView（ratingType=1 时显示）
     */
    private SobotTenRatingLayout sobot_ten_rating_ll;
    /**
     * 评价类型：0-五星评价，1-十分评价，2-二级评价（满意/不满意）
     */
    private int ratingType;
    /**
     * 评分描述文本（如"非常满意"），跟随分值变化
     */
    private TextView sobot_ratingBar_title;
    /**
     * 提交按钮
     */
    private TextView sobot_submit;
    /**
     * 已解决/未解决区域与评分区域之间的分割线
     */
    private View sobot_ratingBar_split_view;
    /**
     * 用户信息，用于获取评价标签隐藏配置等
     */
    private Information information;
    /**
     * 标签区域容器布局
     */
    private LinearLayout sobot_hide_layout;
    /**
     * 评价标签自动换行布局
     */
    private SobotAntoLineLayout sobot_evaluate_lable_autoline;
    /**
     * 标签 CheckBox 列表，用于获取选中状态
     */
    private List<CheckBox> checkBoxList = new ArrayList<>();
    /**
     * 当前评价数据模型，存储评分、是否解决、标签等信息
     */
    private SobotEvaluateModel sobotEvaluateModel;
    /**
     * 当前绑定的消息对象
     */
    public ZhiChiMessageBase message;

    /**
     * 满意度评价配置信息（包含评价类型、默认分值、标签列表等）
     */
    private SatisfactionSet mSatisfactionSet;
    /**
     * 各分值对应的评价配置列表（标签、输入框提示语、是否必填等）
     */
    private List<SatisfactionSetBase> satisFactionList;
    /**
     * 主题色
     */
    private int themeColor;
    /**
     * 是否使用了自定义主题色
     */
    private boolean changeThemeColor;

    //=======二级评价===start==
    /**
     * 二级评价容器布局（ratingType=2 时显示）
     */
    private LinearLayout ll_2_type;
    /**
     * 二级评价"满意"按钮
     */
    private LinearLayout sobot_btn_satisfied;
    /**
     * 二级评价"不满意"按钮
     */
    private LinearLayout sobot_btn_dissatisfied;
    /**
     * 满意图标
     */
    private ImageView iv_satisfied;
    /**
     * 不满意图标
     */
    private ImageView iv_dissatisfied;
    //==========二级评价===end======

    /**
     * 评价标签引导语标题（如"客服存在哪些问题"）
     */
    private TextView sobot_text_other_problem;
    /**
     * 评价输入框容器
     */
    private SobotEditTextLayout setl_submit_content;
    /**
     * 评价文本输入框，用于用户输入补充建议
     */
    private EditText sobot_add_content;

    /**
     * 标签选中时的背景样式
     */
    private GradientDrawable checkboxDrawable;

    /**
     * 构造方法，初始化评价卡片中的所有子视图并设置点击监听
     *
     * @param context     上下文
     * @param convertView 评价卡片的根视图
     */
    public CusEvaluateMessageHolder(Context context, View convertView) {
        super(context, convertView);
        sobot_text_other_problem = convertView.findViewById(R.id.sobot_text_other_problem);
        setl_submit_content = convertView.findViewById(R.id.setl_submit_content);
        sobot_add_content = convertView.findViewById(R.id.sobot_add_content);
        sobot_add_content.setHint(R.string.sobot_edittext_hint);
        Drawable bgDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_line_4, null);
        sobot_add_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    sobot_add_content.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    sobot_add_content.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null));
                }
            }
        });
        //是否已解决
        sobot_readiogroup = convertView.findViewById(R.id.sobot_readiogroup);
        sobot_ll_ok_robot = convertView.findViewById(R.id.sobot_ll_ok_robot);
        sobot_ll_no_robot = convertView.findViewById(R.id.sobot_ll_no_robot);
        sobot_ll_ok_robot.setOnClickListener(this);
        sobot_ll_no_robot.setOnClickListener(this);
        iv_solved = convertView.findViewById(R.id.iv_solved);
        iv_no_solve = convertView.findViewById(R.id.iv_no_solve);
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) iv_solved.getLayoutParams();
        params1.setMarginEnd(0);
        iv_solved.setLayoutParams(params1);
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) iv_no_solve.getLayoutParams();
        params2.setMarginEnd(0);
        iv_no_solve.setLayoutParams(params2);

        sobot_tv_star_title = (TextView) convertView.findViewById(R.id.sobot_tv_star_title);
        sobot_ratingBar = convertView.findViewById(R.id.sobot_ratingBar);
        sobot_ten_root_ll = convertView.findViewById(R.id.sobot_ten_root_ll);

        //二级评价
        ll_2_type = convertView.findViewById(R.id.ll_2_type);
        iv_satisfied = convertView.findViewById(R.id.iv_satisfied);
        iv_dissatisfied = convertView.findViewById(R.id.iv_dissatisfied);

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
            sobot_submit.setBackground(ThemeUtils.applyColorWithMultiplyMode(bg, themeColor));
            sobot_submit.setTextColor(ThemeUtils.getThemeTextAndIconColor(mContext));
        }

        sobot_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deftaultScore = sobotEvaluateModel == null ? -1 : sobotEvaluateModel.getScore();
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
                } else if (ratingType == 1) {
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

                } else if (ratingType == 2) {

                    if (deftaultScore >= 0 && satisfactionSetBase.getIsInputMust()) {
                        doEvaluate(false, deftaultScore);
                        return;
                    }
                    //校验评价标签是否必选
                    if (TextUtils.isEmpty(checkBoxIsChecked())
                            && satisfactionSetBase.getIsTagMust()
                            && !TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
                        ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.sobot_the_label_is_required));//标签必选
                        return;
                    }
                }
                if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
                    //“问题是否解决”是否为必填选项： 0-非必填 1-必填
                    if (sobotEvaluateModel.getIsResolved() == -1 && mSatisfactionSet.getIsQuestionMust() == 1) {
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
                int score = selectIndex + 1;
                if (score > 5) {
                    score = 5;
                }
                if (score < 0) {
                    score = 0;
                }
                sobot_add_content.clearFocus();
                sobotEvaluateModel.setScore(score);
                //显示标签
                setCustomLayoutViewVisible(score, satisFactionList);
            }
        });
    }

    /**
     * 绑定消息数据，加载满意度评价配置并初始化 UI
     * <p>
     * 首次绑定时通过 API 获取满意度配置（{@link SatisfactionSet}），
     * 后续复用缓存的配置数据。配置加载成功后调用 {@link #showData()} 渲染评价界面。
     *
     * @param context 上下文
     * @param message 包含评价数据的消息对象
     */
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

    /**
     * 根据满意度配置渲染评价界面
     * <p>
     * 根据 {@link SatisfactionSet#getScoreFlag()} 决定展示哪种评价模式：
     * <ul>
     *   <li>0：五星评价，通过 {@link SobotFiveStarsLayout} 展示</li>
     *   <li>1：十分评价，通过 {@link SobotTenRatingLayout} 展示 0-10 分</li>
     *   <li>2：二级评价，展示满意/不满意两个按钮</li>
     * </ul>
     * 同时根据配置设置默认分值、标签、输入框提示语、提交按钮文案等。
     */
    private void showData() {
        int score = 0;
        int deftaultScore = sobotEvaluateModel == null ? -1 : sobotEvaluateModel.getIsResolved();
        if (mSatisfactionSet.getScoreFlag() == 0) {
            //defaultType 0-默认5星,1-默认0星
            score = (mSatisfactionSet.getDefaultType() == 0) ? 5 : 0;
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
            sobot_ratingBar.init(score, true, 35);
            sobot_ten_root_ll.setVisibility(View.GONE);
            ll_2_type.setVisibility(View.GONE);
            sobot_ratingBar.setVisibility(View.VISIBLE);
            ratingType = 0;//5星
            if (mSatisfactionSet.getDefaultType() == 0 && score > 0) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
        } else if (mSatisfactionSet.getScoreFlag() == 1) {
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
        } else if (mSatisfactionSet.getScoreFlag() == 2) {
            //二级评价
            ratingType = 2;//二级评价
            sobot_ten_root_ll.setVisibility(View.GONE);
            ll_2_type.setVisibility(View.VISIBLE);
            sobot_ratingBar.setVisibility(View.GONE);
            //二级评价
            //0-满意，1-不满意，2-不选中
            if (mSatisfactionSet.getDefaultType() == 0) {
                score = 5;

                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);
                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);

            } else if (mSatisfactionSet.getDefaultType() == 1) {
                score = 1;

                iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);
                iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

            } else if (mSatisfactionSet.getDefaultType() == 2) {
                score = -1;
            }
            if (score > 0) {
                sobot_submit.setVisibility(View.VISIBLE);
            }
            deftaultScore = score;
            sobotEvaluateModel.setScore(deftaultScore);
            setCustomLayoutViewVisible(score, satisFactionList);
        }

        SatisfactionSetBase satisfactionSetBase = getSatisFaction(score, satisFactionList);
        if (satisfactionSetBase != null) {
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
        if (mSatisfactionSet != null && mSatisfactionSet.getIsDefaultButton() == 0 && !TextUtils.isEmpty(mSatisfactionSet.getButtonDesc())) {
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
        } else if (satisfactionSetBase != null && !TextUtils.isEmpty(satisfactionSetBase.getLabelName())) {
            String tmpData[] = convertStrToArray(satisfactionSetBase.getLabelName());
            setLableViewVisible(tmpData);
        } else {
            setLableViewVisible(null);
        }
        //是否自定义已解决标题
        if (satisfactionSetBase != null && satisfactionSetBase.getIsDefaultQuestion() == 0 && StringUtils.isNoEmpty(satisfactionSetBase.getQuestionCopywriting())) {
            sobot_tv_star_title.setText(satisfactionSetBase.getQuestionCopywriting());
        } else {
            sobot_tv_star_title.setText(String.format(mContext.getString(R.string.sobot_question), message.getSenderName()));
        }

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
                        int pading = (width2 - width1) / 2 + ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(mContext, 16));
                        sobot_ll_ok_robot.setPadding(pading, paddingTop, pading, paddingTop);
                    } else if (width1 > width2) {
                        int pading = (width1 - width2) / 2 + ScreenUtils.dip2px(mContext, 16);
                        int paddingTop = ScreenUtils.dip2px(mContext, 7);
                        LogUtils.d("==pading==" + pading + "====16=" + ScreenUtils.dip2px(mContext, 16));
                        sobot_ll_no_robot.setPadding(pading, paddingTop, pading, paddingTop);
                    }
                }
            });
            sobot_readiogroup.setVisibility(View.VISIBLE);
            sobot_ratingBar_split_view.setVisibility(View.VISIBLE);
//            if()

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
        sobot_ratingBar.setEnabled(false);
    }

    private void setNotEvaluatedLayout() {
        if (sobotEvaluateModel == null) {
            return;
        }
        //是否解决问题 0:未解决，1：已解决，-1：都不选
        if (sobotEvaluateModel.getIsResolved() == 1) {
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_ok_robot.setSelected(true);
            sobot_ll_no_robot.setSelected(false);
        } else if (sobotEvaluateModel.getIsResolved() == 0) {
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_ok_robot.setSelected(false);
            sobot_ll_no_robot.setSelected(true);
        } else if (sobotEvaluateModel.getIsResolved() == -1) {
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(false);
            sobot_ll_ok_robot.setSelected(false);
            sobot_ll_no_robot.setSelected(true);
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
            if (satisfactionSetBase != null) {
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

    /**
     * 获取"问题是否解决"的状态值
     *
     * @return 1-已解决，0-未解决，-1-未选择或未开启该功能
     */
    private int getResovled() {
        if (sobotEvaluateModel == null) {
            return -1;
        }
        if (mSatisfactionSet != null && mSatisfactionSet.getIsQuestionFlag() == 1) {
            return sobotEvaluateModel.getIsResolved();
        }
        return -1;
    }

    /**
     * 根据评分值从满意度配置列表中查找对应的配置项
     *
     * @param score            当前评分值
     * @param satisFactionList 满意度配置列表
     * @return 匹配的配置项，未找到则返回 null
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

    /**
     * 获取所有已选中标签的名称，以逗号分隔
     *
     * @return 选中标签名称的拼接字符串，无选中时返回空字符串
     */
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

    /**
     * 获取已选中标签的 JSON 数组字符串（包含 labelId、labelName、labelNameLan）
     *
     * @param sorce 当前评分值，用于查找对应的标签配置
     * @return 选中标签的 JSON 数组字符串，无选中或异常时返回空字符串
     */
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

    /**
     * 处理二级评价（满意/不满意）和已解决/未解决按钮的点击事件
     *
     * @param v 被点击的视图
     */
    @Override
    public void onClick(View v) {
        sobot_add_content.clearFocus();
        if (v.getId() == R.id.sobot_btn_satisfied) {
            sobotEvaluateModel.setScore(5);
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_sel);

            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_def);
            setCustomLayoutViewVisible(5, satisFactionList);
        } else if (v.getId() == R.id.sobot_btn_dissatisfied) {
            iv_satisfied.setImageResource(R.drawable.sobot_icon_manyi_def);

            iv_dissatisfied.setImageResource(R.drawable.sobot_icon_no_manyi_sel);

            sobotEvaluateModel.setScore(1);
            setCustomLayoutViewVisible(1, satisFactionList);
        }
        if (v.getId() == R.id.sobot_ll_ok_robot) {
            sobotEvaluateModel.setIsResolved(1);
            // 获取系统默认的加粗字体
            iv_solved.setSelected(true);
            iv_no_solve.setSelected(false);
            sobot_ll_ok_robot.setSelected(true);
            sobot_ll_no_robot.setSelected(false);

        } else if (v.getId() == R.id.sobot_ll_no_robot) {
            sobotEvaluateModel.setIsResolved(0);
            iv_solved.setSelected(false);
            iv_no_solve.setSelected(true);
            sobot_ll_ok_robot.setSelected(false);
            sobot_ll_no_robot.setSelected(true);
        }
    }

    /**
     * 根据用户选择的评分值，动态更新标签区域、输入框和评分描述的显示
     * <p>
     * 当用户切换评分时调用，重置所有标签选中状态，并加载新评分对应的标签列表和输入框配置。
     *
     * @param score            用户选择的评分值
     * @param satisFactionList 满意度配置列表
     */
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