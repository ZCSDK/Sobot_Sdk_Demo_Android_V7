package com.sobot.chat.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotCusFieldConfig;
import com.sobot.chat.api.model.SobotFieldModel;
import com.sobot.chat.listener.ISobotCusField;
import com.sobot.chat.listener.SobotInputCallBack;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;

/**
 * 带标题，输入框 错误提示
 */
public class SobotInputView extends LinearLayout implements View.OnClickListener{

    //标题
    private TextView tvTitle;
    //错误提示
    private TextView tvError;
    //输入框
    private EditText singleLineInput;
    private EditText manyLineInput;
    //    选择框
    private TextView tvSelect;
    //地区或者手机区号
    private RelativeLayout sobot_select_two, sobot_input_two;
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

    private Drawable bgDrawable;//

    private Context mContext;
    private SobotInputCallBack callBack;//回调方法
    private ISobotCusField cusCallBack;//回调方法
    private SobotCusFieldConfig cusFieldConfig;//字段配置
    private SobotFieldModel cusFields;//字段 包括选项
    private String valueId;
    private InputListen clickLister;

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
        initView();
    }

    public InputListen getClickLister() {
        return clickLister;
    }

    public void setClickLister(InputListen clickLister) {
        this.clickLister = clickLister;
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.sobot_item_input_view, null);
        tvTitle = view.findViewById(R.id.sobot_title_lable);
        tvError = view.findViewById(R.id.tv_title_error);
        tvError.setVisibility(View.GONE);
        singleLineInput = view.findViewById(R.id.sobot_single_line);
        manyLineInput = view.findViewById(R.id.sobot_many_line);
        tvSelect = view.findViewById(R.id.sobot_select);
        bgDrawable = ResourcesCompat.getDrawable(mContext.getResources(),R.drawable.sobot_bg_line_4,null);
        singleLineInput.setBackground(bgDrawable);

        sobot_select_two = view.findViewById(R.id.sobot_select_two);
        sobot_input_two = view.findViewById(R.id.sobot_input_two);
        v_select_line = view.findViewById(R.id.v_select_line);
        v_input_line = view.findViewById(R.id.v_input_line);
        tv_select_two_left = view.findViewById(R.id.tv_select_two_left);
        tv_select_two_right = view.findViewById(R.id.tv_select_two_right);
        tv_input_two_left = view.findViewById(R.id.tv_input_two_left);
        et_input_two_right = view.findViewById(R.id.et_input_two_right);
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
                    singleLineInput.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    singleLineInput.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_bg_line_4));
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
                    manyLineInput.setBackground(ThemeUtils.applyColorToDrawable(bgDrawable, ThemeUtils.getThemeColor(mContext)));
                } else {
                    manyLineInput.setBackground(mContext.getResources().getDrawable(R.drawable.sobot_bg_line_4));
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
                    if(tvError.getVisibility()==View.VISIBLE) {
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
                    if(tvError.getVisibility()==View.VISIBLE) {
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
        }else{
            singleLineInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(tvError.getVisibility()==View.VISIBLE) {
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
                    if(tvError.getVisibility()==View.VISIBLE) {
                        tvError.setVisibility(GONE);
                    }
                }
            });
        }
        if (null != selectIcon) {
            selectIcon.setBounds(0, 0, selectIcon.getMinimumWidth(), selectIcon.getMinimumHeight());
            //设置右侧图标
            tvSelect.setCompoundDrawables(null, null, selectIcon, null);
        }


        switch (inputType) {
            case "single_line":
                singleLineInput.setVisibility(View.VISIBLE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "many_lines":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.VISIBLE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "select":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.VISIBLE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "select_two":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.VISIBLE);
                sobot_input_two.setVisibility(View.GONE);
                break;
            case "input_two":
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.VISIBLE);
                break;
            case "phone":
                //选择区号
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.GONE);
                sobot_input_two.setVisibility(View.VISIBLE);
            case "timezone":
                //时区
                singleLineInput.setVisibility(View.GONE);
                manyLineInput.setVisibility(View.GONE);
                tvSelect.setVisibility(View.GONE);
                sobot_select_two.setVisibility(View.VISIBLE);
                sobot_input_two.setVisibility(View.GONE);
                break;
        }
        LinearLayout.LayoutParams lpUpdateText = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        view.setLayoutParams(lpUpdateText);
        addView(view);
    }

    public void setCusFieldConfig(SobotCusFieldConfig cusFieldConfig) {
        this.cusFieldConfig = cusFieldConfig;
    }
    public String getSingleValue(){
        return singleLineInput.getText().toString().trim();
    }
    public String getPhontValue(){
        return et_input_two_right.getText().toString().trim();
    }
    public String getManyValue(){
        return manyLineInput.getText().toString().trim();
    }
    public String getSelectValue(){
        return tvSelect.getText().toString().trim();
    }
    /**
     * 设置回调
     * @param callBack
     */
    public void setCusCallBack(ISobotCusField callBack) {
        this.cusCallBack = callBack;
        if (tvSelect != null) {
            tvSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvSelect.requestFocus();
                    cusCallBack.onClickCusField(tvSelect, cusFieldConfig,cusFields);
                }
            });
        }
        if (tv_input_two_left != null) {
            tv_input_two_left.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_input_two_left.requestFocus();
                    cusCallBack.onClickCusField(tv_input_two_left, cusFieldConfig,cusFields);
                }
            });
        }
        if (tv_select_two_left != null) {
            tv_select_two_left.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_select_two_left.requestFocus();
                    cusCallBack.onClickCusField(tv_select_two_left, cusFieldConfig,cusFields);
                }
            });
        }
    }

    /**
     * 设置标题
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

    /**
     * 设置右侧图标
     * @param selectIcon
     */
    public void setSelectIcon(Drawable selectIcon) {
        this.selectIcon = selectIcon;
        if (null != tvSelect && null != selectIcon) {
            selectIcon.setBounds(0, 0, selectIcon.getMinimumWidth(), selectIcon.getMinimumHeight());
            //设置右侧图标
            tvSelect.setCompoundDrawables(null, null, selectIcon, null);
        }
    }

    /**
     * 设置输入的最大值
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
     * @param inputType
     */
    public void setInputType(String inputType) {
        this.inputType = inputType;
        if (null != singleLineInput) {
            switch (inputType) {
                case "single_line":
                    singleLineInput.setVisibility(View.VISIBLE);
                    manyLineInput.setVisibility(View.GONE);
                    tvSelect.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "many_lines":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.VISIBLE);
                    tvSelect.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "select":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    tvSelect.setVisibility(View.VISIBLE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.GONE);
                    break;
                case "phone":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    tvSelect.setVisibility(View.GONE);
                    sobot_select_two.setVisibility(View.GONE);
                    sobot_input_two.setVisibility(View.VISIBLE);
                case "timezone":
                    singleLineInput.setVisibility(View.GONE);
                    manyLineInput.setVisibility(View.GONE);
                    tvSelect.setVisibility(View.GONE);
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
        if(tv_input_two_left!=null) {
            tv_input_two_left.setText(value);
        }
    }
    /**
     * 设置选择后结果显示
     */
    public void setSelectLeftValue(String value) {
        if(tv_select_two_left!=null) {
            tv_select_two_left.setText(value);
        }
    }
    /**
     * 设置选择后结果显示
     */
    public void setSelectRightValue(String value) {
        if(tv_select_two_right!=null) {
            tv_select_two_right.setText(value);
        }
    }

    /**
     * 设置错误提示
     * @param error 错误
     */
    public void showError(String error) {
        if (!TextUtils.isEmpty(error)) {
            tvError.setVisibility(View.VISIBLE);
            Drawable db = ThemeUtils.applyColorToDrawable(mContext, bgDrawable, R.color.sobot_dialog_input_error);
            singleLineInput.setBackground(db);
            manyLineInput.setBackground(db);
            tvSelect.setBackground(db);
            tvError.setText(error);
        }
    }

    // 隐藏错误
    public void hideError() {
        tvError.setVisibility(GONE);
        Drawable db = ThemeUtils.applyColorToDrawable(mContext, bgDrawable, R.color.sobot_dialog_input);
        singleLineInput.setBackground(db);
        manyLineInput.setBackground(db);
        tvSelect.setBackground(db);
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
                v = tv_input_two_left.getText().toString()+","+et_input_two_right.getText().toString();
                break;
        }
        return v;
    }
    public SobotCusFieldConfig getCusFieldConfig() {
        return cusFieldConfig;
    }

    @Override
    public void onClick(View v) {
        if(v==tv_input_two_left){
            if(clickLister!=null){
                clickLister.inputLeftOnclick();
            }
        }else if(v==tv_select_two_left){
            if(clickLister!=null){
                clickLister.selectLeftOnclick();
            }
        }else if(v==tv_select_two_right){
            if(clickLister!=null){
                clickLister.selectRightOnclick();
            }
        }
    }
    public interface InputListen{
        void inputLeftOnclick();
        void selectLeftOnclick();
        void selectRightOnclick();
    }
}
