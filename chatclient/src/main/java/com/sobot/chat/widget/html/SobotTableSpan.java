package com.sobot.chat.widget.html;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义表格Span，用于在TextView中渲染HTML表格
 */
public class SobotTableSpan extends ReplacementSpan {

    private final Context context;
    private final List<List<String>> tableData; // 表格数据（二维数组）
    private final boolean hasHeader; // 是否有表头
    private final int columnCount; // 列数

    // 样式配置
    private final int backgroundColor; // 表格背景色
    private final int headerBgColor; // 表头背景色
    private final int cellBgColor; // 单元格背景色
    private final int borderColor; // 边框颜色
    private final int textColor; // 文字颜色
    private final float cornerRadius; // 圆角半径
    private final float borderWidth; // 内部线宽度
    private final float outerBorderWidth; // 外边框宽度
    private final float padding; // 内边距
    private final float textSize; // 字体大小
    private final float minWidth; // 最小列宽
    private final float maxWidth; // 最大列宽

    private Paint borderPaint;
    private Paint bgPaint;
    private TextPaint textPaint;

    // 缓存布局信息
    private List<List<StaticLayout>> cellLayouts;
    private float totalWidth;
    private float totalHeight;
    private boolean isCalculated = false;

    // RTL支持
    private boolean isRtl = false;

    /**
     * 构造函数
     *
     * @param context   上下文
     * @param htmlTable HTML表格字符串
     */
    public SobotTableSpan(Context context, String htmlTable) {
        this.context = context;

        // 解析HTML表格
        TableParser parser = new TableParser(htmlTable);
        this.tableData = parser.getTableData();
        this.hasHeader = parser.hasHeader();
        this.columnCount = parser.getColumnCount();

        // 检测RTL语言环境
        this.isRtl = ChatUtils.isRtl(context);

        // 初始化样式
        this.backgroundColor = 0xFFFFFFFF;
        this.headerBgColor = 0xFFF0F0F0;
        this.cellBgColor = 0xFFFFFFFF;
        this.borderColor = 0xFFE6E6E6;
        this.textColor = 0xFF161616;
        this.cornerRadius = ScreenUtils.dip2px(context, 12);
        this.borderWidth = ScreenUtils.dip2px(context, 1.4f);
        this.outerBorderWidth = ScreenUtils.dip2px(context, 1f);
        this.padding = ScreenUtils.dip2px(context, 9);
        this.textSize = ScreenUtils.sp2px(context, 14);
        this.minWidth = ScreenUtils.dip2px(context, 60);
        this.maxWidth = ScreenUtils.dip2px(context, 300);

        initPaints();
    }

    private void initPaints() {
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(Math.max(borderWidth, ScreenUtils.dip2px(context, 1)));
        borderPaint.setColor(borderColor);

        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        if (!isCalculated) {
            calculateLayout();
        }

        if (fm != null && totalHeight > 0) {
            fm.top = -(int) totalHeight;
            fm.ascent = -(int) totalHeight;
            fm.descent = 0;
            fm.bottom = 0;
        }

        return (int) totalWidth;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        if (!isCalculated) {
            calculateLayout();
        }

        drawCells(canvas, x, top);
    }

    /**
     * 计算表格布局
     */
    private void calculateLayout() {
        if (tableData == null || tableData.isEmpty()) {
            totalWidth = 0;
            totalHeight = 0;
            isCalculated = true;
            return;
        }

        cellLayouts = new ArrayList<>();
        float[] columnWidths = new float[columnCount];

        for (int row = 0; row < tableData.size(); row++) {
            List<String> rowData = tableData.get(row);
            List<StaticLayout> rowLayouts = new ArrayList<>();

            for (int col = 0; col < rowData.size(); col++) {
                String cellText = rowData.get(col);

                float textWidth = textPaint.measureText(cellText != null ? cellText : "");
                float contentWidth = Math.max(textWidth + padding * 2, minWidth);
                float actualCellWidth = Math.min(contentWidth, maxWidth);

                columnWidths[col] = Math.max(columnWidths[col], actualCellWidth);

                StaticLayout layout = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    layout = StaticLayout.Builder.obtain(
                            cellText != null ? cellText : "",
                            0,
                            cellText != null ? cellText.length() : 0,
                            textPaint,
                            (int) (actualCellWidth - padding * 2)
                    ).build();
                }

                rowLayouts.add(layout);
            }

            cellLayouts.add(rowLayouts);
        }

        totalWidth = outerBorderWidth * 2;
        for (int i = 0; i < columnWidths.length; i++) {
            totalWidth += columnWidths[i];
        }

        totalHeight = outerBorderWidth * 2;
        for (int row = 0; row < tableData.size(); row++) {
            float rowHeight = 0;
            List<StaticLayout> rowLayouts = cellLayouts.get(row);

            for (StaticLayout layout : rowLayouts) {
                if (layout != null) {
                    rowHeight = Math.max(rowHeight, layout.getHeight() + padding * 2);
                }
            }

            totalHeight += rowHeight;
        }

        isCalculated = true;
    }

    private void drawCells(Canvas canvas, float x, int top) {
        if (cellLayouts == null || cellLayouts.isEmpty()) {
            return;
        }

        float currentY = top;
        float[] columnWidths = getColumnWidths();

        int totalRows = tableData.size();
        int totalCols = columnCount;

        Paint.Style originalStyle = textPaint.getStyle();
        Paint.FontMetricsInt originalMetrics = new Paint.FontMetricsInt();
        textPaint.getFontMetricsInt(originalMetrics);

        RectF outerRect = new RectF(x, top, x + totalWidth, top + totalHeight);

        bgPaint.setColor(borderColor);
        android.graphics.Path outerPath = new android.graphics.Path();
        outerPath.addRoundRect(outerRect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW);
        canvas.drawPath(outerPath, bgPaint);

        float contentX = x + outerBorderWidth;
        currentY = top + outerBorderWidth;
        float innerCornerRadius = Math.max(0, cornerRadius - outerBorderWidth);

        for (int row = 0; row < totalRows; row++) {
            List<String> rowData = tableData.get(row);
            List<StaticLayout> rowLayouts = cellLayouts.get(row);

            float rowHeight = 0;
            for (StaticLayout layout : rowLayouts) {
                if (layout != null) {
                    rowHeight = Math.max(rowHeight, layout.getHeight() + padding * 2);
                }
            }

            float currentX = contentX;
            boolean isHeader = hasHeader && row == 0;
            boolean isFirstRow = (row == 0);
            boolean isLastRow = (row == totalRows - 1);
            int rowBgColor = isHeader ? headerBgColor : cellBgColor;

            for (int colIndex = 0; colIndex < totalCols; colIndex++) {
                int col = isRtl ? (totalCols - 1 - colIndex) : colIndex;

                String cellText = (col < rowData.size()) ? rowData.get(col) : "";
                StaticLayout layout = (col < rowLayouts.size()) ? rowLayouts.get(col) : null;
                float cellWidth = columnWidths[col];

                boolean isTopLeft = (isFirstRow && col == 0);
                boolean isTopRight = (isFirstRow && col == totalCols - 1);
                boolean isBottomLeft = (isLastRow && col == 0);
                boolean isBottomRight = (isLastRow && col == totalCols - 1);

                if (isRtl) {
                    boolean temp = isTopLeft;
                    isTopLeft = isTopRight;
                    isTopRight = temp;

                    temp = isBottomLeft;
                    isBottomLeft = isBottomRight;
                    isBottomRight = temp;
                }

                boolean isCornerCell = isTopLeft || isTopRight || isBottomLeft || isBottomRight;

                if (isCornerCell) {
                    RectF cellRect = new RectF(currentX, currentY, currentX + cellWidth, currentY + rowHeight);

                    float topLeftRadius = isTopLeft ? innerCornerRadius : 0;
                    float topRightRadius = isTopRight ? innerCornerRadius : 0;
                    float bottomRightRadius = isBottomRight ? innerCornerRadius : 0;
                    float bottomLeftRadius = isBottomLeft ? innerCornerRadius : 0;

                    bgPaint.setColor(rowBgColor);
                    android.graphics.Path cellPath = new android.graphics.Path();
                    cellPath.addRoundRect(cellRect,
                            new float[]{
                                    topLeftRadius, topLeftRadius,
                                    topRightRadius, topRightRadius,
                                    bottomRightRadius, bottomRightRadius,
                                    bottomLeftRadius, bottomLeftRadius
                            },
                            android.graphics.Path.Direction.CW);
                    canvas.drawPath(cellPath, bgPaint);
                } else {
                    RectF cellRect = new RectF(currentX, currentY, currentX + cellWidth, currentY + rowHeight);
                    bgPaint.setColor(rowBgColor);
                    canvas.drawRect(cellRect, bgPaint);
                }

                if (colIndex < totalCols - 1) {
                    float lineX = currentX + cellWidth;
                    canvas.drawLine(lineX, currentY, lineX, currentY + rowHeight, borderPaint);
                }

                if (layout != null) {
                    canvas.save();
                    canvas.translate(currentX + padding, currentY + padding);

                    if (isHeader) {
                        TextPaint headerTextPaint = new TextPaint(textPaint);
                        headerTextPaint.setFakeBoldText(true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            StaticLayout headerLayout = StaticLayout.Builder.obtain(
                                    cellText != null ? cellText : "",
                                    0,
                                    cellText != null ? cellText.length() : 0,
                                    headerTextPaint,
                                    (int) (cellWidth - padding * 2)
                            ).build();
                            headerLayout.draw(canvas);
                        }
                    } else {
                        layout.draw(canvas);
                    }

                    canvas.restore();
                }

                currentX += cellWidth;
            }

            if (row < totalRows - 1) {
                float lineY = currentY + rowHeight;
                canvas.drawLine(contentX, lineY, contentX + totalWidth - outerBorderWidth * 2, lineY, borderPaint);
            }

            currentY += rowHeight;
        }

        textPaint.setStyle(originalStyle);
    }


    /**
     * 获取列宽数组
     */
    private float[] getColumnWidths() {
        if (cellLayouts == null || cellLayouts.isEmpty() || columnCount == 0) {
            return new float[0];
        }

        float[] widths = new float[columnCount];

        for (int col = 0; col < columnCount; col++) {
            float maxCellWidth = 0;

            for (int row = 0; row < cellLayouts.size(); row++) {
                List<StaticLayout> rowLayouts = cellLayouts.get(row);

                if (col < rowLayouts.size() && row < tableData.size()) {
                    String cellText = tableData.get(row).get(col);
                    float textWidth = textPaint.measureText(cellText != null ? cellText : "");
                    float contentWidth = Math.max(textWidth + padding * 2, minWidth);
                    float actualCellWidth = Math.min(contentWidth, maxWidth);

                    maxCellWidth = Math.max(maxCellWidth, actualCellWidth);
                } else {
                    LogUtils.d("SobotTableSpan  行" + row + "列" + col + ": 无数据");
                }
            }

            widths[col] = maxCellWidth;
        }

        return widths;
    }

    /**
     * 表格解析器
     */
    private static class TableParser {
        private List<List<String>> tableData;
        private boolean hasHeader;
        private int columnCount;

        public TableParser(String html) {
            parseHtmlTable(html);
        }

        private void parseHtmlTable(String html) {
            tableData = new ArrayList<>();
            hasHeader = false;
            columnCount = 0;

            if (html == null || html.isEmpty()) {
                return;
            }

            try {
                List<String> rows = extractRows(html);
                for (String row : rows) {
                    List<String> cells = extractCells(row);

                    if (!cells.isEmpty()) {
                        tableData.add(cells);
                        columnCount = Math.max(columnCount, cells.size());
                    }
                }

                LogUtils.d("SobotTableSpan 总列数: " + columnCount + ", 总行数: " + tableData.size());
                LogUtils.d("SobotTableSpan表格数据: " + tableData);

                boolean hasThTag = html.contains("<th") || html.contains("<TH");

                if (hasThTag) {
                    hasHeader = true;
                } else if (tableData.size() > 1) {
                    hasHeader = true;
                } else {
                    hasHeader = false;
                }
            } catch (Exception e) {
                LogUtils.e("uncaught", e);
            }
        }


        private List<String> extractRows(String html) {
            List<String> rows = new ArrayList<>();
            int startIndex = 0;

            while (true) {
                int trStart = html.indexOf("<tr", startIndex);
                if (trStart == -1) break;

                int trEnd = html.indexOf(">", trStart);
                if (trEnd == -1) break;

                int trClose = html.indexOf("</tr>", trEnd);
                if (trClose == -1) break;

                String rowContent = html.substring(trEnd + 1, trClose);
                rows.add(rowContent);
                startIndex = trClose + 5;
            }

            return rows;
        }

        private List<String> extractCells(String row) {
            List<String> cells = new ArrayList<>();
            int startIndex = 0;

            while (true) {
                int tdStart = row.indexOf("<td", startIndex);
                int thStart = row.indexOf("<th", startIndex);

                if (tdStart == -1 && thStart == -1) break;

                int cellStart = (tdStart == -1) ? thStart :
                        (thStart == -1) ? tdStart :
                                Math.min(tdStart, thStart);

                int cellEnd = row.indexOf(">", cellStart);
                if (cellEnd == -1) break;

                String cellOpenTag = row.substring(cellStart, cellEnd).toUpperCase();
                String closeTag = cellOpenTag.contains("<TH") ? "</th>" : "</td>";

                int cellClose = row.toLowerCase().indexOf(closeTag.toLowerCase(), cellEnd);
                if (cellClose == -1) break;

                String cellContent = row.substring(cellEnd + 1, cellClose);
                cells.add(cleanHtmlContent(cellContent));
                startIndex = cellClose + closeTag.length();
            }

            return cells;
        }

        private String cleanHtmlContent(String content) {
            if (content == null) return "";

            String cleaned = content.replaceAll("<[^>]+>", "");
            cleaned = cleaned.replace("&nbsp;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"");
            return cleaned.trim();
        }

        public List<List<String>> getTableData() {
            return tableData;
        }

        public boolean hasHeader() {
            return hasHeader;
        }

        public int getColumnCount() {
            return columnCount;
        }
    }
}
