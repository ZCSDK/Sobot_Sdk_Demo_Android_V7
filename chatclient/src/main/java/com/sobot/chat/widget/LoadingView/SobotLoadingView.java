
package com.sobot.chat.widget.LoadingView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.sobot.chat.R;

//圆环加载中 控件
//效果：灰色圆环上边有一个1/4圆在转动
public class SobotLoadingView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float startAngle = 0;
    private int backgroundColor = Color.GRAY;
    private int progressColor = Color.BLUE;
    private float strokeWidth = 8f;
    private boolean isSpinning = false;
    private long lastUpdateTime = 0;
    private static final int SPIN_DURATION = 1000; // 1秒一圈

    public SobotLoadingView(Context context) {
        super(context);
        init(null);
    }

    public SobotLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SobotLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SobotLoadingView);
            backgroundColor = a.getColor(R.styleable.SobotLoadingView_backgroundColor, Color.GRAY);
            progressColor = a.getColor(R.styleable.SobotLoadingView_progressColor, Color.BLUE);
            strokeWidth = a.getDimension(R.styleable.SobotLoadingView_strokeWidth, 8f);
            a.recycle();
        }

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeWidth(strokeWidth);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - strokeWidth / 2;

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // 绘制背景圆环
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // 绘制进度弧线（1/4圆）
        canvas.drawArc(rectF, startAngle, 90, false, progressPaint);

        // 更新旋转角度并重绘
        if (isSpinning) {
            long currentTime = System.currentTimeMillis();
            if (lastUpdateTime == 0) {
                lastUpdateTime = currentTime;
            }

            // 优化动画性能，避免过度绘制
            if (currentTime > lastUpdateTime) {
                float deltaTime = (currentTime - lastUpdateTime) / (float) SPIN_DURATION;
                startAngle += 360 * deltaTime;
                if (startAngle > 360) {
                    startAngle -= 360;
                }
                lastUpdateTime = currentTime;
                invalidate();
            } else {
                // 如果时间没有变化，延迟重绘以节省资源
                postInvalidateDelayed(16); // 约60fps
            }
        }
    }

    /**
     * 开始旋转动画
     * 优化：每次开始都从同一位置开始，提升用户体验一致性
     */
    public void startSpinning() {
        if (!isSpinning) {
            isSpinning = true;
            lastUpdateTime = 0;
            startAngle = 0; // 重置起始角度，确保每次开始动画都从同一位置开始
            invalidate();
        }
    }

    /**
     * 停止旋转动画
     * 优化：添加状态检查，避免不必要的操作
     */
    public void stopSpinning() {
        if (isSpinning) {
            isSpinning = false;
            lastUpdateTime = 0;
            invalidate();
        }
    }

    /**
     * 设置背景圆环颜色
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置进度弧线颜色
     */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }

    /**
     * 设置线条宽度
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        backgroundPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeWidth(strokeWidth);
        invalidate();
    }

    /**
     * 确保View为正方形
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    /**
     * View从窗口分离时停止动画，防止内存泄漏
     */
    @Override
    protected void onDetachedFromWindow() {
        stopSpinning();
        super.onDetachedFromWindow();
    }

    /**
     * View可见性改变时的处理
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isSpinning) {
            if (visibility == VISIBLE) {
                // View变为可见时重置时间计算
                lastUpdateTime = 0;
                invalidate();
            }
            // View不可见时动画会自然停止，因为不会调用onDraw
        }
    }
}