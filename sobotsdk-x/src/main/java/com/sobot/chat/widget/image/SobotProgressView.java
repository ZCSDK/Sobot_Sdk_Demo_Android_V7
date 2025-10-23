package com.sobot.chat.widget.image;

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

//进度显示的圆环控件
public class SobotProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private int backgroundColor = Color.GRAY;
    private int progressColor = Color.BLUE;
    private float strokeWidth = 8f;
    private int progress = 0; // 进度值 0-100
    private static final int MAX_PROGRESS = 100;

    public SobotProgressView(Context context) {
        super(context);
        init(null);
    }

    public SobotProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SobotProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        // 绘制进度弧线
        float sweepAngle = (progress * 360f) / MAX_PROGRESS;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }

    /**
     * 设置进度值
     * @param progress 进度值 0-100
     */
    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, MAX_PROGRESS));
        invalidate();
    }

    /**
     * 获取当前进度值
     * @return 当前进度值
     */
    public int getProgress() {
        return progress;
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
}
