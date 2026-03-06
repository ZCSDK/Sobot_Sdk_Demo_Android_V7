package com.sobot.chat.widget.html;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;

/**
 * 自定义圆角按钮Span
 */
public class SobotCircleButtonSpan extends ReplacementSpan {

    public enum ShapeType {
        RECTANGLE,    // 矩形
        CIRCLE        // 圆形
    }

    private Context context;
    private Drawable iconDrawable;
    private String text;
    private int backgroundColor;
    private int textColor;
    private float cornerRadius;
    private float padding;           // 内边距（仅对矩形有效）
    private float iconTextSpacing;   // 图标与文本间距
    private float textSize;          // 字体大小
    private Paint paint;
    private ShapeType shapeType;
    private float totalHeight;       // 按钮总高度（可配置）
    private float iconWidth;         // 自定义图标宽度
    private float iconHeight;        // 自定义图标高度
    private float leftMargin;        // 左外边距
    private float rightMargin;       // 右外边距
    private RectF buttonBounds;      // 按钮边界区域
    private float borderWidth;       // 边框宽度
    private int borderColor;         // 边框颜色
    private boolean hasBorder;       // 是否有边框
    private float iconAlpha = 1.0f;  // 图标透明度，默认为1.0（完全不透明）
    private float offsetY = 0; // 垂直偏移量，默认为0


    /**
     * 私有构造函数，防止直接实例化
     */
    private SobotCircleButtonSpan(Builder builder) {
        this.context = builder.context;
        this.text = builder.text;
        this.backgroundColor = builder.backgroundColor;
        this.textColor = builder.textColor;
        this.cornerRadius = builder.cornerRadius;
        this.padding = builder.padding;
        this.iconTextSpacing = builder.iconTextSpacing;
        this.textSize = builder.textSize;
        this.shapeType = builder.shapeType;
        this.totalHeight = builder.totalHeight;
        this.iconWidth = builder.iconWidth;
        this.iconHeight = builder.iconHeight;
        this.leftMargin = builder.leftMargin;
        this.rightMargin = builder.rightMargin;
        this.borderWidth = builder.borderWidth;
        this.borderColor = builder.borderColor;
        this.hasBorder = builder.hasBorder;
        this.iconAlpha = builder.iconAlpha;
        this.offsetY = builder.offsetY;

        initPaint();

        if (builder.iconResId != 0) {
            this.iconDrawable = ContextCompat.getDrawable(context, builder.iconResId);
            if (this.iconDrawable != null) {
                int width = (int) (this.iconWidth > 0 ? this.iconWidth : textSize * 1.2f);
                int height = (int) (this.iconHeight > 0 ? this.iconHeight : textSize * 1.2f);
                this.iconDrawable.setBounds(0, 0, width, height);
            }
        }
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end,
                       Paint.FontMetricsInt fm) {
        float totalWidth = leftMargin + rightMargin;

        float contentWidth = 0;
        if (shapeType == ShapeType.RECTANGLE) {
            contentWidth = padding * 2;
        }

        if (iconDrawable != null) {
            int iconWidth = (int) (this.iconWidth > 0 ? this.iconWidth : iconDrawable.getIntrinsicWidth());
            contentWidth += iconWidth;
            if (this.text != null && !this.text.isEmpty()) {
                contentWidth += iconTextSpacing;
            }
        }

        if (this.text != null && !this.text.isEmpty()) {
            contentWidth += paint.measureText(this.text, 0, this.text.length());
        }

        totalWidth += contentWidth;
        return (int) totalWidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint originalPaint) {


        // 计算按钮实际高度
        float actualHeight = totalHeight;
        if (actualHeight <= 0) {
            actualHeight = 1; // 避免除零错误
        }

        // 中心位置
        int verticalCenter = top + (bottom - top) / 2 - (int) (offsetY / 2);
        float defLineHeigh = 0;//textview 行高（文字大小+行间距）
        try {
            defLineHeigh = context.getResources().getDimensionPixelSize(R.dimen.sobot_text_font_15) + context.getResources().getDimension(R.dimen.sobot_text_line_spacing_aianswar_button);
            if ((bottom - top) < defLineHeigh) {
                //小于行高 是最后一行显示按钮，默认会偏高，微调后能居中
                verticalCenter = top + (bottom - top) / 2 + (int) (offsetY / 2) - 7;
            } else {
                verticalCenter = top + (bottom - top) / 2 - (int) (offsetY / 2);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        // 调整按钮顶部和底部坐标
        int adjustedTop = verticalCenter - (int) (actualHeight / 2);
        int adjustedBottom = verticalCenter + (int) (actualHeight / 2);


        float contentWidth = 0;
        if (shapeType == ShapeType.RECTANGLE) {
            contentWidth = padding * 2;
        }

        if (iconDrawable != null) {
            int iconWidth = (int) (this.iconWidth > 0 ? this.iconWidth : iconDrawable.getIntrinsicWidth());
            contentWidth += iconWidth;
            if (this.text != null && !this.text.isEmpty()) {
                contentWidth += iconTextSpacing;
            }
        }

        if (this.text != null && !this.text.isEmpty()) {
            contentWidth += originalPaint.measureText(this.text, 0, this.text.length());
        }

        float totalWidth = leftMargin + contentWidth + rightMargin;
        float adjustedX = x + leftMargin;
        float centerX = adjustedX + contentWidth / 2;

        if (buttonBounds == null) {
            buttonBounds = new RectF();
        }
        buttonBounds.set(adjustedX, adjustedTop, adjustedX + contentWidth, adjustedBottom);

        int originalColor = originalPaint.getColor();
        float originalTextSize = originalPaint.getTextSize();

        originalPaint.setColor(textColor);
        originalPaint.setTextSize(textSize);

        // 使用传入的背景色
        paint.setColor(backgroundColor); // 修改为使用传入的背景色
        paint.setStyle(Paint.Style.FILL);

        switch (shapeType) {
            case CIRCLE:
                float circleSize = Math.max(contentWidth, actualHeight);
                float radius = circleSize / 2;
                canvas.drawCircle(centerX, verticalCenter, radius, paint);

                if (hasBorder && borderWidth > 0) {
                    paint.setColor(borderColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(borderWidth);
                    canvas.drawCircle(centerX, verticalCenter, radius - borderWidth / 2, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(backgroundColor); // 保持填充色为传入的背景色
                }
                break;
            case RECTANGLE:
            default:
                RectF rectF = new RectF(adjustedX, adjustedTop, adjustedX + contentWidth, adjustedBottom);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

                if (hasBorder && borderWidth > 0) {
                    paint.setColor(borderColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(borderWidth);
                    canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(backgroundColor); // 保持填充色为传入的背景色
                }
                break;
        }

        float iconAndTextTotalWidth = 0;
        if (iconDrawable != null) {
            int iconWidth = (int) (this.iconWidth > 0 ? this.iconWidth : iconDrawable.getIntrinsicWidth());
            iconAndTextTotalWidth += iconWidth;
            if (this.text != null && !this.text.isEmpty()) {
                iconAndTextTotalWidth += iconTextSpacing;
            }
        }
        if (this.text != null && !this.text.isEmpty()) {
            iconAndTextTotalWidth += originalPaint.measureText(this.text, 0, this.text.length());
        }

        float contentStartX;
        if (shapeType == ShapeType.RECTANGLE) {
            contentStartX = adjustedX + Math.round(padding) + (contentWidth - Math.round(padding) * 2 - iconAndTextTotalWidth) / 2;
        } else {
            contentStartX = adjustedX + (contentWidth - iconAndTextTotalWidth) / 2;
        }

        // 绘制图标
        if (iconDrawable != null) {
            int iconWidth = (int) (this.iconWidth > 0 ? this.iconWidth : iconDrawable.getIntrinsicWidth());
            int iconHeight = (int) (this.iconHeight > 0 ? this.iconHeight : iconDrawable.getIntrinsicHeight());
            int iconTop = verticalCenter - iconHeight / 2;

            // 创建临时图标drawable副本以避免影响原始资源
            Drawable tempIconDrawable = iconDrawable.getConstantState().newDrawable();

            // 设置图标透明度
            int alphaValue = Math.round(iconAlpha * 255);
            tempIconDrawable.setAlpha(alphaValue);

            tempIconDrawable.setBounds((int) contentStartX, iconTop,
                    (int) contentStartX + iconWidth,
                    iconTop + iconHeight);
            tempIconDrawable.draw(canvas);

            if (this.text != null && !this.text.isEmpty()) {
                contentStartX += iconWidth + iconTextSpacing;
            } else {
                contentStartX += iconWidth;
            }
        }

        // 绘制文本
        if (this.text != null && !this.text.isEmpty()) {
            float textY = verticalCenter - (originalPaint.descent() + originalPaint.ascent()) / 2;
            canvas.drawText(this.text, contentStartX, textY, originalPaint);
        }

        originalPaint.setColor(originalColor);
        originalPaint.setTextSize(originalTextSize);
    }

    public float getLeftMargin() {
        return leftMargin;
    }

    public float getRightMargin() {
        return rightMargin;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public boolean hasBorder() {
        return hasBorder;
    }

    public float getIconAlpha() {
        return iconAlpha;
    }

    /**
     * Builder类，用于链式构建SobotCircleButtonSpan
     */
    public static class Builder {
        private Context context;
        private int iconResId;
        private String text;
        private int backgroundColor;
        private int textColor;
        private float cornerRadius;
        private float padding = 0;           // 内边距（仅对矩形有效）
        private float iconTextSpacing = 0;   // 图标与文本间距
        private float textSize = 14;         // 字体大小
        private ShapeType shapeType = ShapeType.RECTANGLE;
        private float totalHeight = 0;       // 按钮总高度（可配置）
        private float iconWidth = 0;         // 自定义图标宽度
        private float iconHeight = 0;        // 自定义图标高度
        private float leftMargin = 0;        // 左外边距
        private float rightMargin = 0;       // 右外边距
        private float borderWidth = 0;       // 边框宽度
        private int borderColor = 0;         // 边框颜色
        private boolean hasBorder = false;   // 是否有边框
        private float iconAlpha = 1.0f;      // 图标透明度，默认为完全不透明
        private float offsetY = 0; // Builder中的偏移量字段

        public Builder offsetY(float offsetY) {
            this.offsetY = offsetY;
            return this;
        }


        public Builder(Context context) {
            this.context = context;
        }

        public Builder icon(int iconResId) {
            this.iconResId = iconResId;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder textColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder cornerRadius(float cornerRadius) {
            this.cornerRadius = cornerRadius;
            return this;
        }

        public Builder padding(float padding) {
            this.padding = padding;
            return this;
        }

        public Builder iconTextSpacing(float iconTextSpacing) {
            this.iconTextSpacing = iconTextSpacing;
            return this;
        }

        public Builder textSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder shapeType(ShapeType shapeType) {
            this.shapeType = shapeType;
            return this;
        }

        public Builder totalHeight(float totalHeight) {
            this.totalHeight = totalHeight;
            return this;
        }

        public Builder iconSize(float width, float height) {
            this.iconWidth = width;
            this.iconHeight = height;
            return this;
        }

        public Builder iconWidth(float iconWidth) {
            this.iconWidth = iconWidth;
            return this;
        }

        public Builder iconHeight(float iconHeight) {
            this.iconHeight = iconHeight;
            return this;
        }

        public Builder margins(float left, float right) {
            this.leftMargin = left;
            this.rightMargin = right;
            return this;
        }

        public Builder leftMargin(float leftMargin) {
            this.leftMargin = leftMargin;
            return this;
        }

        public Builder rightMargin(float rightMargin) {
            this.rightMargin = rightMargin;
            return this;
        }

        public Builder border(float borderWidth, int borderColor) {
            this.borderWidth = borderWidth;
            this.borderColor = borderColor;
            this.hasBorder = true;
            return this;
        }

        public Builder borderWidth(float borderWidth) {
            this.borderWidth = borderWidth;
            return this;
        }

        public Builder borderColor(int borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        public Builder hasBorder(boolean hasBorder) {
            this.hasBorder = hasBorder;
            return this;
        }

        public Builder iconAlpha(float iconAlpha) {
            this.iconAlpha = iconAlpha;
            return this;
        }

        public SobotCircleButtonSpan build() {
            if (totalHeight <= 0) {
                throw new IllegalArgumentException("totalHeight must be greater than 0");
            }
            return new SobotCircleButtonSpan(this);
        }
    }
}
