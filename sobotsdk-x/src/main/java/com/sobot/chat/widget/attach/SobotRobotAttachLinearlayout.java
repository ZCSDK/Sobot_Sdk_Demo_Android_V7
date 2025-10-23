package com.sobot.chat.widget.attach;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;

import com.sobot.chat.R;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;

//拖拽吸附控件
public class SobotRobotAttachLinearlayout extends LinearLayout {

    private float mLastRawX;
    private float mLastRawY;
    private boolean isDrug = false;
    private int mRootMeasuredWidth = 0;
    private int mRootMeasuredHeight = 0;
    private int mRootTopY = 0;
    private boolean customIsAttach;
    private boolean customIsDrag;

    public SobotRobotAttachLinearlayout(Context context) {
        this(context, null);
    }

    public SobotRobotAttachLinearlayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SobotRobotAttachLinearlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        initAttrs(context, attrs);
    }

    /**
     * 初始化自定义属性
     */
    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedAttay = context.obtainStyledAttributes(attrs, R.styleable.sobot_attach_layout);
        customIsAttach = mTypedAttay.getBoolean(R.styleable.sobot_attach_layout_customIsAttach, true);
        customIsDrag = mTypedAttay.getBoolean(R.styleable.sobot_attach_layout_customIsDrag, true);
        mTypedAttay.recycle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }

    private float downX, downY;
    private static final int MIN_DISTANCE = 10; // 最小移动距离

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //判断是否需要滑动
        if (customIsDrag) {
            //当前手指的坐标
            float mRawX = ev.getRawX();
            float mRawY = ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://手指按下
                    downX = ev.getX();
                    downY = ev.getY();
                    isDrug = false;
                    //记录按下的位置
                    mLastRawX = mRawX;
                    mLastRawY = mRawY;
                    ViewGroup mViewGroup = (ViewGroup) getParent();
                    if (mViewGroup != null) {
                        int[] location = new int[2];
                        mViewGroup.getLocationInWindow(location);
                        //获取父布局的高度
                        mRootMeasuredHeight = mViewGroup.getMeasuredHeight();
                        mRootMeasuredWidth = mViewGroup.getMeasuredWidth();
                        //获取父布局顶点的坐标
                        mRootTopY = location[1];
                    }
                    break;
                case MotionEvent.ACTION_MOVE://手指滑动
                    float moveX = ev.getX();
                    float moveY = ev.getY();
                    if (Math.abs(moveX - downX) > 10 || Math.abs(moveY - downY) > 10) {
                        // 拖动事件处理
                        if (mRawX >= 0 && mRawX <= mRootMeasuredWidth && mRawY >= mRootTopY && mRawY <= (mRootMeasuredHeight + mRootTopY)) {
                            //手指X轴滑动距离
                            float differenceValueX = mRawX - mLastRawX;
                            //手指Y轴滑动距离
                            float differenceValueY = mRawY - mLastRawY;
                            //判断是否为拖动操作
                            if (!isDrug) {
                                if (Math.sqrt(differenceValueX * differenceValueX + differenceValueY * differenceValueY) < 2) {
                                    isDrug = false;
                                } else {
                                    isDrug = true;
                                }
                            }
                            //获取手指按下的距离与控件本身X轴的距离
                            float ownX = getX();
                            //获取手指按下的距离与控件本身Y轴的距离
                            float ownY = getY();
                            //理论中X轴拖动的距离
                            float endX = ownX + differenceValueX;
                            //理论中Y轴拖动的距离
                            float endY = ownY + differenceValueY;
                            //X轴可以拖动的最大距离
                            float maxX = mRootMeasuredWidth - getWidth();
                            //Y轴可以拖动的最大距离 95 =底部输入框加快捷菜单
                            float maxY = mRootMeasuredHeight - getHeight() - ScreenUtils.dip2px(getContext(), 95);
                            //X轴边界限制
                            endX = endX < 0 ? 0 : endX > maxX ? maxX : endX;
                            //Y轴边界限制
                            endY = endY < 0 ? 0 : endY > maxY ? maxY : endY;
                            //开始移动
                            setX(endX);
                            setY(endY);
                            //记录位置
                            mLastRawX = mRawX;
                            mLastRawY = mRawY;
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP://手指离开
                    //根据自定义属性判断是否需要贴边
                    if (customIsAttach) {
                        //判断是否为点击事件
                        if (isDrug) {
                            float center = mRootMeasuredWidth / 2;
                            Paint paint = new Paint();
                            // 设置Paint的文字大小与TextView相同
                            paint.setTextSize(ScreenUtils.sp2px(getContext(), 14));
                            // 测量 换业务 文字的宽度，向右移动该宽度
                            float width = paint.measureText(getResources().getString(R.string.sobot_switch_business));
                            String language = SharedPreferencesUtil.getStringData(getContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
                            if (StringUtils.isNoEmpty(language)) {
                                //自动贴边
//                                if (mLastRawX <= center) {
                                if ("ar".equals(language)) {
                                    //向左贴边
                                    this.animate()
                                            .setInterpolator(new BounceInterpolator())
                                            .setDuration(500)
                                            .x(0 - width - ScreenUtils.dip2px(getContext(), 8))
                                            .start();
                                } else {
                                    //向右贴边
                                    this.animate()
                                            .setInterpolator(new BounceInterpolator())
                                            .setDuration(500)
                                            .x(mRootMeasuredWidth - getWidth() + width + ScreenUtils.dip2px(getContext(), 8))
                                            .start();
                                }
                            } else {
                                //向右贴边
                                this.animate()
                                        .setInterpolator(new BounceInterpolator())
                                        .setDuration(500)
                                        .x(mRootMeasuredWidth - getWidth() + width + ScreenUtils.dip2px(getContext(), 8))
                                        .start();
                            }
                        }
                    }
                    break;
            }
        }
        //是否拦截事件
        return isDrug ? isDrug : super.onTouchEvent(ev);
    }
}