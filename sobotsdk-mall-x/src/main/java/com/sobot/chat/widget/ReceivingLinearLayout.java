package com.sobot.chat.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sobot.chat.R;

//自定义LinearLayout 折叠收起控件
public class ReceivingLinearLayout extends LinearLayout {

    /**
     * 折叠按钮图标
     */
    private ImageView mExpandBtn;
    private TextView ExpandBtn;

    /**
     * 是否折叠的临界高度
     */
    private int mLimitHeight = 200;

    /**
     * 是否展开了
     */
    private boolean mIsExpand = false;

    /**
     * 是否支持展开折叠功能
     */
    private boolean mSupportExpand = true;

    public ReceivingLinearLayout(Context context) {
        this(context, null);
    }

    public ReceivingLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReceivingLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    /**
     * 初始化
     */
    public void init() {
        setOrientation(VERTICAL);
    }


    /**
     * 重写onMeasure动态控制控件的高度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取自身的测量高度
        int measureHeight = getMeasuredHeight();

        //设置折叠按钮是否显示
        setExpandBtnVisiable(measureHeight <= mLimitHeight ? View.GONE : View.VISIBLE);

        //如果不支持折叠或者需要展开或者高度不到折叠高度,就保持默认测量
        if (!mSupportExpand || mIsExpand || measureHeight <= mLimitHeight) {
            return;
        }

        //重新设置高度测量参数
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mLimitHeight, MeasureSpec.EXACTLY);

        //重新测量
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 绑定展开折叠按钮图标
     *
     * @param view
     * @param expandRes
     * @param foldRes
     */
    public void bindExpandButton(ImageView view, TextView textView, final int expandRes, final int foldRes) {
        mExpandBtn = view;
        ExpandBtn = textView;
        if (textView == null) {
            return;
        }
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsExpand(!mIsExpand);
                if(mIsExpand) {
                    mExpandBtn.setVisibility(GONE);
                    ExpandBtn.setVisibility(GONE);
                }else{
                    mExpandBtn.setImageResource( expandRes);
                    ExpandBtn.setText(getResources().getString(R.string.sobot_card_open));
                }
            }
        });
        setExpandBtnVisiable(GONE);
    }

    /**
     * 获取临界折叠高度
     *
     * @return
     */
    public int getLimitHeight() {
        return mLimitHeight;
    }

    /**
     * 设置临界折叠高度
     *
     * @param limitHeight
     */
    public void setLimitHeight(int limitHeight) {
        this.mLimitHeight = limitHeight;
    }

    /**
     * 获取是否展开了
     *
     * @return
     */
    public boolean isIsExpand() {
        return mIsExpand;
    }

    /**
     * 设置是否展开
     *
     * @param isExpand
     */
    public void setIsExpand(boolean isExpand) {
        this.mIsExpand = isExpand;
        if(isExpand){
            mExpandBtn.setImageResource(R.drawable.sobot_notice_arrow_up);
            ExpandBtn.setText( getResources().getString(R.string.sobot_card_open));
        }
        requestLayout();
    }

    /**
     * 获取当前是否支持折叠展开功能
     *
     * @return
     */
    public boolean isSupportExpand() {
        return mSupportExpand;
    }

    /**
     * 设置折叠还是展开
     *
     * @param supportExpand
     */
    public void setSupportExpand(boolean supportExpand) {
        this.mSupportExpand = supportExpand;
        setExpandBtnVisiable(GONE);
    }

    /**
     * 设置折叠图标是否显示
     *
     * @param visiable
     */
    public void setExpandBtnVisiable(int visiable) {
        if (mExpandBtn != null) {
            if (!mSupportExpand) {
                mExpandBtn.setVisibility(GONE);
                ExpandBtn.setVisibility(GONE);
            } else {
                mExpandBtn.setVisibility(visiable);
                ExpandBtn.setVisibility(visiable);
            }
        }
    }
    public void setExpandBtnVVisiable(int visiable) {
        if (mExpandBtn != null) {
            mExpandBtn.setVisibility(visiable);
            ExpandBtn.setVisibility(visiable);
        }
    }

}


