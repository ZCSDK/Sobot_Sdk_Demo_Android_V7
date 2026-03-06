package com.sobot.chat.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.listener.SobotCusFieldListener;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.widget.dialog.SobotDialogUtils;

/**
 * 带标题，输入框 错误提示
 */
public class SobotInputView extends LinearLayout implements View.OnClickListener {

    //标题
    private TextView tvTitle;
    //错误提示
    private TextView tvError;
    //输入框
    private EditText singleLineInput;
    private EditText manyLineInput;
    //    选择框
    private TextView tvSelect;
    private LinearLayout llSelectOne;
    private ImageView iv_select_icon;
    //地区或者手机区号
    private LinearLayout sobot_input_two;
    private RelativeLayout sobot_select_two ;
    private View v_select_line, v_input_line;
    private TextView tv_select_two_left, tv_select_two_right, tv_input_two_left;
    private EditText et_input_two_right;


    private String titleText;//标题
    private String hintText;//提示
    private String value;//显示的默认值
    private String viweType;//输入框的输入内容的类型
    private Drawable selectIcon;//选择框右边的小图标
    private boolean isHintTitle;//是否隐藏标题，只是用输入框
    private String inputType;//输入类型：single_line、many_lines、select
    private int inputLengthLimit;//输入长度限制

    private Drawable focusDrawable;//

    private Context mContext;
    private SobotCusFieldListener cusCallBack;//回调方法
    private SobotCusFieldConfig cusFieldConfig;//字段配置
    private SobotFieldModel cusFields;//字段 包括选项
    private String valueId;
    private GradientDrawable defaultBg, focusBg, errorBg;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public SobotFieldModel getCusFields() {
        return cusFields;
    }

    public void setCusFields(SobotFieldModel cusFields) {
        this.cusFields = cusFields;
    }

    public SobotInputView(Context context) {
        this(context, null);
    }

    public SobotInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SobotInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        //atts 包括
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SobotInputView);
//        系统会在自定义属性前加上它所属的declare-styleable 的name_
        isHintTitle = array.getBoolean(R.styleable.SobotInputView_sobot_hint_title, false);
        inputType = array.getString(R.styleable.SobotInputView_sobot_input_type);
        titleText = array.getString(R.styleable.SobotInputView_sobot_input_title);
        hintText = array.getString(R.styleable.SobotInputView_sobot_input_hint);
        value = array.getString(R.styleable.SobotInputView_sobot_input_value);
        viweType = array.getString(R.styleable.SobotInputView_sobot_input_view_type);
        selectIcon = array.getDrawable(R.styleable.SobotInputView_sobot_input_select_icon);
        inputLengthLimit = array.getInt(R.styleable.SobotInputView_sobot_input_length_limit, -1);
        if (StringUtils.isEmpty(inputType)) {
            inputType = "single_line";
        }
        array.recycle();//回收
        // 默认状态
        defaultBg = createInputBackground(
                mContext,
                ContextCompat.getColor(mContext, R.color.sobot_dialog_input),  // 灰色边框
                1,                                                            // 1dp 边框
                ContextCompat.getColor(mContext, R.color.sobot_dialog_input_bg), // 填充色
                4                                                             // 4dp 圆角
        );

// 获取焦点状态（主题色边框）
        focusBg = createInputBackground(
                mContext,
                ThemeUtils.getThemeColor(mContext),  // 主题色边框
                1,
                ContextCompat.getColor(mContext, R.color.sobot_dialog_input_bg),
                4
        );
        errorBg = createInputBackground(
                mContext,
                ContextCompat.getColor(mContext, R.color.sobot_dialog_input_error),  // 主题色边框
                1,
                ContextCompat.getColor(mContext, R.color.sobot_dialog_input_bg),
                4
        );

        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.sobot_item_input_view, null);
        tvTitle = view.findViewById(R.id.sobot_title_lable);
        tvError = view.findViewById(R.id.tv_title_error);
        tvError.setVisibility(View.GONE);
        singleLineInput = view.findViewById(R.id.sobot_single_line);
        manyLineInput = view.findViewById(R.id.sobot_many_line);
        tvSelect = view.findViewById(R.id.sobot_select);
        iv_select_icon = view.findViewById(R.id.iv_select_icon);
        llSelectOne = view.findViewById(R.id.sobot_select_one);
        focusDrawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_line_4, null);

        Drawable selectRight = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_select_icon, null);
        selectRight.setBounds(0, 0, ScreenUtils.dip2px(getContext(), 14), ScreenUtils.dip2px(getContext(), 14));
        sobot_select_two = view.findViewById(R.id.sobot_select_two);
        sobot_input_two = view.findViewById(R.id.sobot_input_two);
        v_select_line = view.findViewById(R.id.v_select_line);
        v_input_line = view.findViewById(R.id.v_input_line);
        tv_select_two_left = view.findViewById(R.id.tv_select_two_left);
        tv_select_two_right = view.findViewById(R.id.tv_select_two_right);
        tv_input_two_left = view.findViewById(R.id.tv_input_two_left);
        et_input_two_right = view.findViewById(R.id.et_input_two_right);
        if (selectRight != null) {
            tv_select_two_left.setCompoundDrawables(null, null, selectRight, null);
            tv_select_two_right.setCompoundDrawables(null, null, selectRight, null);
            tv_input_two_left.setCompoundDrawables(null, null, selectRight, null);
        }
        llSelectOne.setOnClickListener(this);
        tv_input_two_left.setOnClickListener(this);
        tv_select_two_left.setOnClickListener(this);
        tv_select_two_right.setOnClickListener(this);

        if (StringUtils.isNoEmpty(titleText)) {
            tvTitle.setText(titleText);
        }

        if (isHintTitle) {
            tvTitle.setVisibility(View.GONE);
        }

        if (StringUtils.isNoEmpty(hintText)) {
            singleLineInput.setHint(hintText);
            manyLineInput.setHint(hintText);
            tvSelect.setHint(hintText);
        }
        if (StringUtils.isNoEmpty(value)) {
            singleLineInput.setText(value);
            manyLineInput.setText(value);
            tvSelect.setText(value);
        }
        singleLineInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    singleLineInput.setBackground(focusBg);
                } else {
                    singleLineInput.setBackground(defaultBg);
                }
            }
        });
        if (StringUtils.isNoEmpty(viweType)) {
            if ("email".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if ("phone".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if ("pass".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if ("number".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }
        manyLineInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    manyLineInput.setBackground(ThemeUtils.applyColorToDrawable(focusDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    manyLineInput.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null));
                }
            }
        });
        llSelectOne.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    llSelectOne.setBackground(ThemeUtils.applyColorToDrawable(focusDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    llSelectOne.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null));
                }
            }
        });
        sobot_select_two.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    sobot_select_two.setBackground(ThemeUtils.applyColorToDrawable(focusDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    sobot_select_two.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null));
                }
            }
        });
        sobot_input_two.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    sobot_input_two.setBackground(ThemeUtils.applyColorToDrawable(focusDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    sobot_input_two.setBackground(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null));
                }
            }
        });
        if (inputLengthLimit > 0) {
            singleLineInput.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (inputLengthLimit > 0 && temp.length() > inputLengthLimit) {
                        showError(titleText + mContext.getResources().getString(R.string.sobot_only_can_write) + inputLengthLimit + mContext.getResources().getString(R.string.sobot_char_length));
                    }
                }
            });
            manyLineInput.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (inputLengthLimit > 0 && temp.length() > inputLengthLimit) {
                        showError(titleText + mContext.getResources().getString(R.string.sobot_only_can_write) + inputLengthLimit + mContext.getResources().getString(R.string.sobot_char_length));
                    }
                }
            });
        } else {
            singleLineInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(GONE);
                    }
                }
            });
            manyLineInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (tvError.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(GONE);
                    }
                }
            });
        }
        if (null != selectIcon) {
            selectIcon.setBounds(0, 0, selectIcon.getMinimumWidth(), selectIcon.getMinimumHeight());
        }


        switch (inputType) {
            case "single_line":
                singleLineInput.setVisibility(View.VISIBLE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "many_lines":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.VISIBLE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "select":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.VISIBLE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "select_two":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.VISIBLE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "input_two":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.VISIBLE);
                break;
            case "phone":
                //选择区号
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.VISIBLE);
                break;
            case "timezone":
                //时区
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                llSelectOne.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.VISIBLE);
                sobot_input_two.setVisibility(View.GONE);
                break;
        }
        //点击选项，后背景颜色修改

        LinearLayout.LayoutParams lpUpdateText = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        view.setLayoutParams(lpUpdateText);
        addView(view);
    }

    public void setCusFieldConfig(SobotCusFieldConfig cusFieldConfig) {
        this.cusFieldConfig = cusFieldConfig;
    }

    public String getSingleValue() {
        return singleLineInput.getText().toString().trim();
    }

    public String getPhontValue() {
        return et_input_two_right.getText().toString().trim();
    }

    public String getManyValue() {
        return manyLineInput.getText().toString().trim();
    }

    public String getSelectValue() {
        return tvSelect.getText().toString().trim();
    }

    /**
     * 设置回调
     *
     * @param callBack
     */
    public void setCusCallBack(SobotCusFieldListener callBack) {
        this.cusCallBack = callBack;
        if (llSelectOne != null) {
            llSelectOne.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    llSelectOne.requestFocus();
                    cusCallBack.onClickCusField(tvSelect, cusFieldConfig, cusFields);
                }
            });
        }
    }

    /**
     * 设置标题
     *
     * @param title
     * @param isMust
     */
    public void setTitle(String title, boolean isMust) {
        String mustFill = "<font color='#F5222D'> *</font>";
        if (isMust) {
            titleText = title + mustFill;
        } else {
            titleText = title;
        }
        if (tvTitle == null) return;
        tvTitle.setText(Html.fromHtml(titleText));
    }

    /**
     * 设置标题
     *
     * @param title
     * @param isMust
     */
    public void setTitle(String title, boolean isMust, String finalExplain) {
        String endStr = "";
        if (StringUtils.isNoEmpty(finalExplain)) {
            endStr = "<span style='font-size:18px;'> ⓘ</span>";
            tvTitle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    弹框显示，finalExplain
                    SobotDialogUtils.startTipDialog(mContext, finalExplain);
                }
            });
        }
        String mustFill = "<font color='#F5222D'> *</font>";
        if (isMust) {
            endStr = endStr + mustFill;
        }
        if (ChatUtils.isRtl(mContext)) {
            endStr = "\u200F" + endStr;
            titleText = "\u200F" + title + endStr;
        } else {
            endStr = "\u200E" + endStr;
            titleText = "\u200E" + title + endStr;
        }

        if (tvTitle == null) return;
        tvTitle.setText(Html.fromHtml(titleText));
    }

    public TextView getTvTitle() {
        return tvTitle;
    }


    public EditText getSingleLineInput() {
        return singleLineInput;
    }

    public EditText getManyLineInput() {
        return manyLineInput;
    }

    public TextView getTv_select_two_left() {
        return tv_select_two_left;
    }

    public TextView getTv_select_two_right() {
        return tv_select_two_right;
    }

    public TextView getTv_input_two_left() {
        return tv_input_two_left;
    }

    public EditText getEt_input_two_right() {
        return et_input_two_right;
    }

    public TextView getTvSelect() {
        return tvSelect;
    }

    public LinearLayout getLlSelectOne() {
        return llSelectOne;
    }

    /**
     * 设置右侧图标
     *
     * @param selectIcon
     */
    public void setSelectIcon(Drawable selectIcon) {
        this.selectIcon = selectIcon;
        if (null != iv_select_icon && null != selectIcon) {
            iv_select_icon.setImageDrawable(selectIcon);
        }
    }

    /**
     * 设置输入的最大值
     *
     * @param inputLengthLimit
     */
    public void setInputLengthLimit(int inputLengthLimit) {
        this.inputLengthLimit = inputLengthLimit;
        if (inputLengthLimit > 0) {
            singleLineInput.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (inputLengthLimit > 0 && temp.length() > inputLengthLimit) {
                        showError(titleText + mContext.getResources().getString(R.string.sobot_only_can_write) + inputLengthLimit + mContext.getResources().getString(R.string.sobot_char_length));
                    }
                }
            });
        }
    }

    /**
     * 设置输入类型
     *
     * @param inputType
     */
    public void setInputType(String inputType) {
        this.inputType = inputType;
        if (null != singleLineInput) {
            switch (inputType) {
                case "single_line":
                    singleLineInput.setVisibility(View.VISIBLE);
                    manyLineInput.setVisibility(View.GONE);
                    llSelectOne.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "many_lines":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.VISIBLE);
                    llSelectOne.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "select":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    llSelectOne.setVisibility(View.VISIBLE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "phone":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    llSelectOne.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.VISIBLE);
                    break;
                case "timezone":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    llSelectOne.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.VISIBLE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
            }
        }
    }

    public void setViweType(String viweType) {
        this.viweType = viweType;
        if (null != singleLineInput && StringUtils.isNoEmpty(viweType)) {
            if ("email".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if ("phone".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_CLASS_PHONE);
                et_input_two_right.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if ("pass".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if ("number".equals(viweType)) {
                singleLineInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }
    }

    /**
     * 设置提示语
     *
     * @param hint 提示语
     */
    public void setInputHint(String hint) {
        hintText = hint;
        singleLineInput.setHint(Html.fromHtml(hint));
        manyLineInput.setHint(Html.fromHtml(hint));
        tvSelect.setHint(Html.fromHtml(hint));
    }

    /**
     * 设置输入内容
     */
    public void setInputValue(String value) {
        this.value = value;
        singleLineInput.setText(value);
        manyLineInput.setText(value);
        tvSelect.setText(value);
    }

    /**
     * 设置选择后结果显示
     */
    public void setInputLeftValue(String value) {
        if (tv_input_two_left != null) {
            tv_input_two_left.setText(value);
        }
    }

    /**
     * 设置选择后结果显示
     */
    public void setSelectLeftValue(String value) {
        if (tv_select_two_left != null) {
            tv_select_two_left.setText(value);
        }
    }

    /**
     * 设置选择后结果显示
     */
    public void setSelectRightValue(String value) {
        if (tv_select_two_right != null) {
            tv_select_two_right.setText(value);
        }
    }

    /**
     * 设置错误提示
     *
     * @param error 错误
     */
    public void showError(String error) {
        if (!TextUtils.isEmpty(error)) {
            tvError.setVisibility(View.VISIBLE);
//            Drawable db = ThemeUtils.applyColorToDrawable(mContext, focusDrawable, R.color.sobot_dialog_input_error);
            singleLineInput.setBackground(errorBg);
            manyLineInput.setBackground(errorBg);
            llSelectOne.setBackground(errorBg);
            tvError.setText(error);
        }
    }

    // 隐藏错误
    public void hideError() {
        tvError.setVisibility(GONE);
//        Drawable db = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sobot_bg_dialog_input, null);
        singleLineInput.setBackground(defaultBg);
        manyLineInput.setBackground(defaultBg);
        llSelectOne.setBackground(defaultBg);
    }

    //设置右边图标
    public String getValue() {
        String v = "";
        switch (inputType) {
            case "single_line":
                v = singleLineInput.getText().toString();
                break;
            case "many_lines":
                v = manyLineInput.getText().toString();
                break;
            case "select":
                v = tvSelect.getText().toString();
                break;
            case "phone":
                v = tv_input_two_left.getText().toString() + "," + et_input_two_right.getText().toString();
                break;
            case "timezone":
                v = tv_select_two_left.getText().toString() + "," + tv_select_two_right.getText().toString();
                break;
        }
        return v;
    }

    public SobotCusFieldConfig getCusFieldConfig() {
        return cusFieldConfig;
    }

    @Override
    public void onClick(View v) {
        if (v == tv_input_two_left) {
            if (cusCallBack != null) {
                cusCallBack.inputLeftOnclick();
            }
        } else if (v == tv_select_two_left) {
            if (cusCallBack != null) {
                cusCallBack.selectLeftOnclick(tv_select_two_left, cusFieldConfig);
            }
        } else if (v == tv_select_two_right) {
            if (cusCallBack != null) {
                cusCallBack.selectRightOnclick(tv_select_two_right, cusFieldConfig);
            }
        }
    }

    /**
     * 创建输入框背景 Drawable（对应 sobot_bg_dialog_input）
     * @param context 上下文
     * @param strokeColor 边框颜色
     * @param strokeWidth 边框宽度（dp）
     * @param fillColor 填充颜色
     * @param cornerRadius 圆角半径（dp）
     * @return GradientDrawable
     */
    public static GradientDrawable createInputBackground(
            Context context,
            int strokeColor,
            int strokeWidth,
            int fillColor,
            int cornerRadius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        // 设置边框
        drawable.setStroke(dp2px(context, strokeWidth), strokeColor);

        // 设置填充色
        drawable.setColor(fillColor);

        // 设置圆角
        drawable.setCornerRadius(dp2px(context, cornerRadius));

        return drawable;
    }

    /**
     * dp 转 px
     */
    private static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

}
