package com.sobot.chat.widget.html;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.sobot.chat.R;
import com.sobot.chat.activity.SobotFileDetailActivity;
import com.sobot.chat.activity.WebViewActivity;
import com.sobot.chat.api.apiUtils.ZhiChiConstants;
import com.sobot.chat.api.model.SobotCacheFile;
import com.sobot.chat.camera.util.FileUtil;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.ScreenUtils;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ThemeUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.attachment.FileTypeConfig;

import org.xml.sax.XMLReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义HTML标签处理器
 * 处理自定义HTML标签，如sobotspan和sobotbutton，将它们转换为相应的UI组件
 * 注意：HTML中的span标签需替换为自定义的sobotfont，因为高版本系统html.fromHtml()已能识别span标签，
 * 不会走自定义处理器
 */
public class SobotCustomTagHandler implements Html.TagHandler {

    private final String TAG = "CustomTagHandler";

    // 标签常量定义
    public static final String NEW_SPAN = "sobotspan";     // 自定义span标签
    public static final String SOBOT_LINK = "sobotbutton";   // 自定义链接标签

    // 存储HTML标签解析过程中的标签信息
    private List<SobotHtmlLabelBean> labelBeanList = new ArrayList<>();      // 按顺序添加的标签Bean
    private List<SobotHtmlLabelBean> tempRemoveLabelList = new ArrayList<>(); // 临时移除的标签列表

    private ColorStateList mOriginColors;  // 原始文本颜色状态
    private Context mContext;              // 应用上下文
    private boolean mIsHistoryMsg; //   是否是大模型历史记录消息 历史记录中转人工按钮样式置灰且不能点击
    private boolean mIsCanClickAiButton;//大模型消息里边按钮是否能点击（回答结束后才能点击）实时消息 问答没结束前不能点击
    private boolean isAppNightMode = false;//是否深色模式

    // 构造函数 - 原有构造函数
    public SobotCustomTagHandler(Context context, ColorStateList originColors, boolean isHistoryMsg, boolean isCanClickAiButton) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.mContext = context;
        this.mOriginColors = originColors;
        this.mIsHistoryMsg = isHistoryMsg;
        this.mIsCanClickAiButton = isCanClickAiButton;
    }

    // ... existing code ...
    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag == null || output == null) {
            return; // 关键参数为空时不处理
        }

        try {
            // 处理字体和span标签
            if (tag.equalsIgnoreCase(NEW_SPAN)) {
                processAttributes(xmlReader); // 解析标签属性
                if (opening) {
                    startFont(tag, output, xmlReader);
                } else {
                    endFont(tag, output, xmlReader);
                    attributes.clear(); // 清除已处理的属性
                }
            }
            // 处理自定义链接标签
            else if (tag.equalsIgnoreCase(SOBOT_LINK)) {
                processAttributes(xmlReader); // 解析sobotbutton标签属性
                if (opening) {
                    // 保存当前属性到临时变量，以确保每个开始标签捕获正确的属性
                    Map<String, String> currentAttributes = new HashMap<>(attributes);
                    startSobotbutton(output, currentAttributes);
                } else {
                    // 对于结束标签，我们也需要确保使用对应的属性
                    Map<String, String> currentAttributes = new HashMap<>(attributes);
                    endSobotbutton(output, currentAttributes);
                    attributes.clear(); // 清除已处理的属性
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 处理sobotbutton开始标签
     *
     * @param output        输出文本
     * @param tagAttributes 当前标签的属性
     */
    private void startSobotbutton(Editable output, Map<String, String> tagAttributes) {
        int startIndex = output.length();
        SobotHtmlLabelBean bean = new SobotHtmlLabelBean();
        bean.startIndex = startIndex;
        bean.endIndex = 0;  // 初始值设为0，表示未处理
        bean.tag = SOBOT_LINK;
        // 将属性也保存到bean中，确保每个按钮都有独立的属性
        bean.attributes = new HashMap<>(tagAttributes);
        labelBeanList.add(bean);
    }

    /**
     * 处理sobotbutton结束标签
     * 创建带点击功能的圆形按钮
     *
     * @param output        输出文本
     * @param tagAttributes 当前标签的属性（备用，以防需要）
     */
    private void endSobotbutton(Editable output, Map<String, String> tagAttributes) {
        try {
            int endIndex = output.length();
            // 查找最近的未处理的sobotbutton标签，而不是任意一个
            int targetIndex = -1;
            for (int i = labelBeanList.size() - 1; i >= 0; i--) {
                SobotHtmlLabelBean bean = labelBeanList.get(i);
                if (SOBOT_LINK.equals(bean.tag) && bean.endIndex == 0) { // 未处理的标签
                    targetIndex = i;
                    break;
                }
            }

            if (targetIndex != -1 && mContext != null) {
                SobotHtmlLabelBean bean = labelBeanList.get(targetIndex);
                bean.endIndex = endIndex;

                // 从bean中获取属性，而不是使用全局的attributes
                Map<String, String> localAttributes = bean.attributes;

                // 获取sobotbutton标签的属性
                String sectionType = localAttributes != null ? localAttributes.get("type") : null;      // 链接类型(web/file/transferagent)
                String value = localAttributes != null ? localAttributes.get("value") : null;      // 链接地址
                String title = localAttributes != null ? localAttributes.get("title") : null;  // 链接标题
                String desc = localAttributes != null ? localAttributes.get("desc") : null;    // 链接描述
                String shapetype = localAttributes != null ? localAttributes.get("shapetype") : null;    // 按钮样式 1：圆型按钮 0：矩形按钮
                String transferGroupName = localAttributes != null ? localAttributes.get("transfergroupname") : null;    // 转人工按钮技能组名字
                String transferGroupId = localAttributes != null ? localAttributes.get("transfergroupid") : null;    // 转人工按钮技能组名字
                if (TextUtils.isEmpty(shapetype)) {
                    shapetype = "0";
                }
                // 验证必要参数
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(sectionType)) {
                    // 获取行间距资源值
                    float offsetY = mContext.getResources().getDimension(R.dimen.sobot_text_line_spacing_aianswar_button);
                    isAppNightMode = ThemeUtils.isAppNightMode(mContext);
                    // 根据链接类型获取对应图标资源ID
                    int drawableResId = getDrawableResourceId(sectionType, shapetype);
                    // 创建圆形按钮显示样式
                    SobotCircleButtonSpan customButtonSpan = null;
                    if (!TextUtils.isEmpty(shapetype) && shapetype.equals("1")) {
                        //圆形按钮 来源
                        customButtonSpan = new SobotCircleButtonSpan.Builder(mContext)
                                .offsetY(offsetY)
                                .icon(drawableResId)
                                .text("")
                                .backgroundColor(
                                        getColorWithAlpha(mContext, R.color.sobot_rich_textview_icon_bg, isAppNightMode ? 0.1f : 0.06f)
                                )
                                .textColor(Color.WHITE)
                                .cornerRadius(0f)
                                .padding(12)
                                .iconTextSpacing(0f)
                                .textSize(0f)
                                .shapeType(SobotCircleButtonSpan.ShapeType.CIRCLE)
                                .totalHeight(ScreenUtils.dip2px(mContext, 19))
                                .iconSize(33, 33)
                                .margins(20, 20)
                                .build();
                    } else {
                        boolean isClickable = true;
                        if ("transfer".equalsIgnoreCase(sectionType) && mIsHistoryMsg) {
                            //历史记录中转人工按钮样式置灰且不能点击
                            isClickable = false;
                        }
                        if ("web".equalsIgnoreCase(sectionType)) {
                            //超链接一直能点
                            isClickable = true;
                        }
                        //矩形圆角按钮
                        customButtonSpan = new SobotCircleButtonSpan.Builder(mContext)
                                .offsetY(offsetY)
                                .icon(drawableResId)
                                .iconAlpha(isClickable ? 1.0f : 0.5f)
                                .text(title)
                                .backgroundColor(getColorWithAlpha(mContext, R.color.sobot_rich_textview_icon_bg_second, isAppNightMode ? (isClickable ? 0.1f : 0.05f) : (isClickable ? 1.0f : 0.5f)))
                                .textColor(getColorWithAlpha(mContext, R.color.sobot_color_text_first, isClickable ? 1.0f : 0.5f))
                                .cornerRadius(ScreenUtils.dip2px(mContext, 20))
                                .padding(9)
                                .iconTextSpacing(ScreenUtils.dip2px(mContext, 4))
                                .textSize(ScreenUtils.dip2px(mContext, 12))
                                .shapeType(SobotCircleButtonSpan.ShapeType.RECTANGLE)
                                .totalHeight(ScreenUtils.dip2px(mContext, 23.5f))
                                .iconSize(ScreenUtils.dip2px(mContext, 12), ScreenUtils.dip2px(mContext, 12))
                                .margins(12, 12)
                                .border(ScreenUtils.dip2px(mContext, 1), getColorWithAlpha(mContext, R.color.sobot_color_line_divider_3, isClickable ? 1.0f : 0.5f))
                                .build();
                    }


                    // 创建可点击的Span，处理点击事件
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            if (mContext == null) {
                                return; // Context 为空时不执行任何操作
                            }
                            if (!mIsCanClickAiButton) {
                                LogUtils.d("大模型结束前，不能点击按钮");
                                return;
                            }
                            // 根据链接类型执行相应操作
                            if ("web".equalsIgnoreCase(sectionType) && !TextUtils.isEmpty(value)) {
                                // 打开网页
                                Intent intent = new Intent(mContext, WebViewActivity.class);
                                intent.putExtra("url", value);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else if ("file".equalsIgnoreCase(sectionType) && !TextUtils.isEmpty(value)) {
                                // 打开文件详情页
                                Intent intent = new Intent(mContext, SobotFileDetailActivity.class);
                                SobotCacheFile cacheFile = new SobotCacheFile();
                                cacheFile.setFileName(title);
                                cacheFile.setFileSize("");
                                cacheFile.setUrl(value);
                                cacheFile.setFileType(FileTypeConfig.getFileType(FileUtil.checkFileEndWith(value)));
                                cacheFile.setMsgId(title);
                                intent.putExtra(ZhiChiConstant.SOBOT_INTENT_DATA_SELECTED_FILE, cacheFile);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            } else if ("transfer".equalsIgnoreCase(sectionType)) {
                                if (mIsHistoryMsg) {
                                    LogUtils.d("历史记录，不能点击转人工按钮");
                                    return;
                                }
                                // 转人工
                                Intent intent = new Intent();
                                intent.setAction(ZhiChiConstants.SOBOT_BROCAST_AI_BUTTON_CLICK_TRANSFER);
                                intent.putExtra("tempGroupName", StringUtils.checkStringIsNull(transferGroupName));
                                intent.putExtra("tempGroupId", StringUtils.checkStringIsNull(transferGroupId));
                                CommonUtils.sendLocalBroadcast(mContext, intent);
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setUnderlineText(false); // 不显示下划线
                        }
                    };

                    // 在输出文本中应用Span
                    output.setSpan(clickableSpan, bean.startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    output.setSpan(customButtonSpan, bean.startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                labelBeanList.remove(targetIndex);
                optRemoveByAddBean(bean);
            }
        } catch (Resources.NotFoundException e) {
        }
    }
// ... existing code ...


    /**
     * 根据颜色资源ID和透明度创建带透明度的颜色值
     *
     * @param context    上下文
     * @param colorResId 颜色资源ID
     * @param alpha      透明度 (0.0f-1.0f, 0.0f为完全透明，1.0f为完全不透明)
     * @return 带透明度的颜色值
     */
    private int getColorWithAlpha(Context context, int colorResId, float alpha) {
        int color = ContextCompat.getColor(context, colorResId);
        int alphaValue = Math.round(alpha * 255);
        return (color & 0x00FFFFFF) | (alphaValue << 24);
    }

    /**
     * 根据链接类型获取对应的图标资源ID
     *
     * @param type 链接类型
     * @return 图标资源ID，找不到则返回0
     */
    private int getDrawableResourceId(String type, String shapetype) {
        if (TextUtils.isEmpty(type)) {
            return 0; // 类型为空时返回默认图标ID
        }

        String lowerCaseType = type.toLowerCase();
        if (lowerCaseType.contains("web")) {
            if (!TextUtils.isEmpty(shapetype) && "1".equals(shapetype)) {
                return R.drawable.sobot_icon_goto_web; // 圆形 网页链接图标
            } else {
                return R.drawable.sobot_icon_goto_web_black; // 矩形  网页链接图标
            }
        } else if (lowerCaseType.contains("file")) {
            return R.drawable.sobot_icon_goto_file; // 文件图标
        } else if (lowerCaseType.contains("transfer")) {
            return R.drawable.sobot_icon_goto_transfer; // 转人工
        }

        // 可根据具体需求添加更多匹配规则

        return 0; // 默认返回0，表示无图标
    }

    // 用于存储XML标签属性的映射
    final HashMap<String, String> attributes = new HashMap<String, String>();

    /**
     * 通过反射获取XML标签的属性
     *
     * @param xmlReader XML解析器
     */
    private void processAttributes(final XMLReader xmlReader) {
        if (xmlReader == null) {
            return; // XML解析器为空时不处理
        }

        try {
            // 使用反射访问XML解析器内部字段获取标签属性
            Field elementField = xmlReader.getClass().getDeclaredField("theNewElement");
            elementField.setAccessible(true);
            Object element = elementField.get(xmlReader);
            if (element != null) {

                Field attsField = element.getClass().getDeclaredField("theAtts");
                attsField.setAccessible(true);
                Object atts = attsField.get(element);
                if (atts != null) {
                    Field dataField = atts.getClass().getDeclaredField("data");
                    dataField.setAccessible(true);
                    String[] data = (String[]) dataField.get(atts);

                    Field lengthField = atts.getClass().getDeclaredField("length");
                    lengthField.setAccessible(true);
                    int len = (Integer) lengthField.get(atts);

                    if (data != null) {
                        // 遍历属性数组，提取键值对
                        for (int i = 0; i < len; i++) {
                            String key = data[i * 5 + 1];    // 属性名
                            String value = data[i * 5 + 4];  // 属性值
                            attributes.put(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 异常处理，不抛出避免影响主要流程
        }
    }

    /**
     * 处理开始标签的通用方法
     *
     * @param tag       标签名
     * @param output    输出文本
     * @param xmlReader XML解析器
     */
    public void startFont(String tag, Editable output, XMLReader xmlReader) {
        int startIndex = output.length();
        SobotHtmlLabelBean bean = new SobotHtmlLabelBean();
        bean.startIndex = startIndex;
        bean.tag = tag;

        String color = null;
        String size = null;
        // 字体加粗的值CSS font-weight属性: normal, bold, bolder, lighter,
        // 也可以指定的值(100-900,其中400是normal)，这里只支持bold
        String fontWeight = null;

        if (NEW_SPAN.equals(tag)) {
            String colorstr = attributes.get("color");
            if (!TextUtils.isEmpty(colorstr)) {
                color = colorstr;
            }
            String sizestr = attributes.get("size");
            if (!TextUtils.isEmpty(sizestr)) {
                size = sizestr;
            }

            String style = attributes.get("style");
            if (!TextUtils.isEmpty(style)) {
                analysisStyle(bean, style); // 解析style属性
            }
        }
        labelBeanList.add(bean);
    }

    /**
     * 解析CSS style属性
     *
     * @param bean  标签Bean
     * @param style CSS样式字符串
     */
    private void analysisStyle(SobotHtmlLabelBean bean, String style) {
        String[] attrArray = style.split(";");
        Map<String, String> attrMap = new HashMap<>();

        for (String attr : attrArray) {
            String[] keyValueArray = attr.split(":");
            if (keyValueArray.length == 2) {
                // 去除前后空格
                attrMap.put(keyValueArray[0].trim(), keyValueArray[1].trim());
            }
        }

        // 设置样式属性到Bean
        bean.color = attrMap.get("color");
        bean.fontSize = attrMap.get("font-size");
        bean.textdecoration = attrMap.get("text-decoration");
        bean.textdecorationline = attrMap.get("text-decoration-line");
        bean.backgroundColor = attrMap.get("background-color");
        bean.background = attrMap.get("background");
        bean.fontweight = attrMap.get("font-weight");
        bean.fontstyle = attrMap.get("font-style");
    }

    /**
     * 计算标签影响的范围
     *
     * @param bean 标签Bean
     */
    private void optBeanRange(SobotHtmlLabelBean bean) {
        if (bean.ranges == null) {
            bean.ranges = new ArrayList<>();
        }

        if (tempRemoveLabelList.isEmpty()) {
            SobotHtmlLabelRangeBean range = new SobotHtmlLabelRangeBean();
            range.start = bean.startIndex;
            range.end = bean.endIndex;
            bean.ranges.add(range);
        } else {
            int size = tempRemoveLabelList.size();
            // 逆向查找第一个结束位置 <= 当前结束位置的标签
            // 逆向查找最后一个开始位置 >= 当前开始位置的标签
            int endRangePosition = -1;
            int startRangePosition = -1;
            for (int i = size - 1; i >= 0; i--) {
                SobotHtmlLabelBean bean1 = tempRemoveLabelList.get(i);
                if (bean1.endIndex <= bean.endIndex) {
                    if (endRangePosition == -1)
                        endRangePosition = i;
                }
                if (bean1.startIndex >= bean.startIndex) {
                    startRangePosition = i;
                }
            }

            if (startRangePosition != -1 && endRangePosition != -1) {
                SobotHtmlLabelBean lastBean = null;
                // 处理嵌套关系
                for (int i = startRangePosition; i <= endRangePosition; i++) {
                    SobotHtmlLabelBean removeBean = tempRemoveLabelList.get(i);
                    lastBean = removeBean;
                    SobotHtmlLabelRangeBean range;
                    if (i == startRangePosition) {
                        range = new SobotHtmlLabelRangeBean();
                        range.start = bean.startIndex;
                        range.end = removeBean.startIndex;
                        bean.ranges.add(range);
                    } else {
                        range = new SobotHtmlLabelRangeBean();
                        SobotHtmlLabelBean bean1 = tempRemoveLabelList.get(i - 1);
                        range.start = bean1.endIndex;
                        range.end = removeBean.startIndex;
                        bean.ranges.add(range);
                    }
                }
                SobotHtmlLabelRangeBean range = new SobotHtmlLabelRangeBean();
                if (lastBean != null)
                    range.start = lastBean.endIndex;
                range.end = bean.endIndex;
                bean.ranges.add(range);
            } else {
                // 并列关系，范围就是自己的角标范围
                SobotHtmlLabelRangeBean range = new SobotHtmlLabelRangeBean();
                range.start = bean.startIndex;
                range.end = bean.endIndex;
                bean.ranges.add(range);
            }
        }
    }

    /**
     * 处理结束标签的通用方法
     * 应用各种文本样式到指定范围
     *
     * @param tag       标签名
     * @param output    输出文本
     * @param xmlReader XML解析器
     */
    public void endFont(String tag, Editable output, XMLReader xmlReader) {
        int stopIndex = output.length();
        int lastLabelByTag = getLastLabelByTag(tag);

        if (lastLabelByTag != -1) {
            SobotHtmlLabelBean bean = labelBeanList.get(lastLabelByTag);
            bean.endIndex = stopIndex;
            optBeanRange(bean);

            // 对每个范围应用样式
            for (SobotHtmlLabelRangeBean range : bean.ranges) {
                String color = bean.color;
                String fontSize = bean.fontSize;
                String textdecoration = bean.textdecoration;
                String textdecorationline = bean.textdecorationline;
                String backgroundColor = bean.backgroundColor;
                String background = bean.background;
                String fontweight = bean.fontweight;
                String fontstyle = bean.fontstyle;

                // 斜体样式
                if (!TextUtils.isEmpty(fontstyle) &&
                        (("italic".equalsIgnoreCase(fontstyle) || "oblique".equalsIgnoreCase(fontstyle)))) {
                    output.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // 粗体样式
                if (!TextUtils.isEmpty(fontweight) && StringUtils.isNumber(fontweight) &&
                        Integer.parseInt(fontweight) >= 700) {
                    output.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (!TextUtils.isEmpty(fontweight) && "bold".equalsIgnoreCase(fontweight)) {
                    output.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // 字体大小
                if (!TextUtils.isEmpty(fontSize)) {
                    fontSize = fontSize.split("px")[0];
                }
                if (!TextUtils.isEmpty(fontSize)) {
                    int fontSizePx = 16;
                    if (null != mContext) {
                        fontSizePx = ScreenUtils.sp2px(mContext, Integer.parseInt(fontSize));
                    }
                    output.setSpan(new AbsoluteSizeSpan(fontSizePx),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // 文本装饰（下划线、删除线等）
                if (!TextUtils.isEmpty(textdecoration) &&
                        !textdecoration.equalsIgnoreCase("none") &&
                        !textdecoration.equalsIgnoreCase("overline") &&
                        !textdecoration.equalsIgnoreCase("blink")) {
                    if (textdecoration.equalsIgnoreCase("line-through")) {
                        output.setSpan(new StrikethroughSpan(),
                                range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        output.setSpan(new UnderlineSpan(),
                                range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                if (!TextUtils.isEmpty(textdecorationline) &&
                        !textdecorationline.equalsIgnoreCase("none") &&
                        !textdecorationline.equalsIgnoreCase("overline") &&
                        !textdecorationline.equalsIgnoreCase("blink")) {
                    if (textdecorationline.equalsIgnoreCase("line-through")) {
                        output.setSpan(new StrikethroughSpan(),
                                range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        output.setSpan(new UnderlineSpan(),
                                range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                // 前景色
                if (!TextUtils.isEmpty(color)) {
                    if (color.startsWith("@")) {
                        Resources res = Resources.getSystem();
                        String name = color.substring(1);
                        int colorRes = res.getIdentifier(name, "color", "android");
                        if (colorRes != 0) {
                            output.setSpan(new ForegroundColorSpan(colorRes),
                                    range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    } else {
                        try {
                            output.setSpan(new ForegroundColorSpan(parseHtmlColor(color)),
                                    range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } catch (Exception e) {
                            reductionFontColor(range.start, stopIndex, output);
                        }
                    }
                }

                // 背景色
                if (!TextUtils.isEmpty(backgroundColor)) {
                    output.setSpan(new BackgroundColorSpan(parseHtmlColor(backgroundColor)),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (!TextUtils.isEmpty(background)) {
                    output.setSpan(new BackgroundColorSpan(parseHtmlColor(background)),
                            range.start, range.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // 从顺序添加的集合中删除已处理完结束标签的项
            labelBeanList.remove(lastLabelByTag);
            optRemoveByAddBean(bean);
        }
    }

    /**
     * 获取最后一个与当前tag匹配的Bean的位置
     *
     * @param tag 标签名
     * @return 位置索引，未找到返回-1
     */
    private int getLastLabelByTag(String tag) {
        for (int size = labelBeanList.size(), i = size - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(tag) &&
                    !TextUtils.isEmpty(labelBeanList.get(i).tag) &&
                    labelBeanList.get(i).tag.equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将处理完成的Bean添加到移除列表
     *
     * @param removeBean 要移除的Bean
     */
    private void optRemoveByAddBean(SobotHtmlLabelBean removeBean) {
        int isAdd = 0;
        for (int size = tempRemoveLabelList.size(), i = size - 1; i >= 0; i--) {
            SobotHtmlLabelBean bean = tempRemoveLabelList.get(i);
            if (removeBean.startIndex <= bean.startIndex && removeBean.endIndex >= bean.endIndex) {
                if (isAdd == 0) {
                    tempRemoveLabelList.set(i, removeBean);
                    isAdd = 1;
                } else {
                    // 如果已有Bean被替换，则移除旧的
                    tempRemoveLabelList.remove(i);
                }
            }
        }
        if (isAdd == 0) {
            tempRemoveLabelList.add(removeBean);
        }
    }

    /**
     * 将dp单位转换为px单位
     *
     * @param context  上下文对象
     * @param dipValue dp值
     * @return px值
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    /**
     * 将px单位转换为dp单位
     *
     * @param context 上下文对象
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5F);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context 上下文
     * @param spValue sp值（DisplayMetrics类中属性scaledDensity）
     * @return px值
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 解析HTML颜色值
     * 支持以下几种格式：
     * 1. #RGB或#RRGGBB格式
     * 2. rgb(r,g,b)或rgba(r,g,b,a)格式
     * 3. 颜色名称（如red, blue等）
     *
     * @param colorString 颜色字符串
     * @return 颜色值
     */
    public static int parseHtmlColor(String colorString) {
        if (colorString.charAt(0) == '#') {
            if (colorString.length() == 4) {
                // 处理#RGB格式，转换为#RRGGBB
                StringBuilder sb = new StringBuilder("#");
                for (int i = 1; i < colorString.length(); i++) {
                    char c = colorString.charAt(i);
                    sb.append(c).append(c);
                }
                colorString = sb.toString();
            }
            long color = Long.parseLong(colorString.substring(1), 16);
            if (colorString.length() == 7) {
                // 设置alpha值
                color |= 0x00000000ff000000;
            } else if (colorString.length() != 9) {
                return 0x000000;
            }
            return (int) color;
        } else if ((colorString.startsWith("rgb(") || colorString.startsWith("rgba(")) &&
                colorString.endsWith(")")) {
            colorString = colorString.substring(colorString.indexOf("(") + 1, colorString.indexOf(")"));
            colorString = colorString.replaceAll(" ", "");
            String[] colorArray = colorString.split(",");
            if (colorArray.length == 3) {
                return Color.argb(255, Integer.parseInt(colorArray[0]),
                        Integer.parseInt(colorArray[1]), Integer.parseInt(colorArray[2]));
            } else if (colorArray.length == 4) {
                return Color.argb(Integer.parseInt(colorArray[3]),
                        Integer.parseInt(colorArray[0]),
                        Integer.parseInt(colorArray[1]), Integer.parseInt(colorArray[2]));
            }
        } else if ("red".equalsIgnoreCase(colorString.trim())) {
            return Color.RED;
        } else if ("blue".equalsIgnoreCase(colorString.trim())) {
            return Color.BLUE;
        } else if ("black".equalsIgnoreCase(colorString.trim())) {
            return Color.BLACK;
        } else if ("gray".equalsIgnoreCase(colorString.trim())) {
            return Color.GRAY;
        } else if ("green".equalsIgnoreCase(colorString.trim())) {
            return Color.GREEN;
        } else if ("yellow".equalsIgnoreCase(colorString.trim())) {
            return Color.YELLOW;
        } else if ("white".equalsIgnoreCase(colorString.trim())) {
            return Color.WHITE;
        }
        return 0x000000;
    }

    /**
     * 还原为原始颜色
     *
     * @param startIndex 开始位置
     * @param stopIndex  结束位置
     * @param editable   可编辑文本
     */
    private void reductionFontColor(int startIndex, int stopIndex, Editable editable) {
        if (null != mOriginColors) {
            editable.setSpan(new TextAppearanceSpan(null, 0, 0, mOriginColors, null),
                    startIndex, stopIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            editable.setSpan(new ForegroundColorSpan(0xff2b2b2b),
                    startIndex, stopIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
