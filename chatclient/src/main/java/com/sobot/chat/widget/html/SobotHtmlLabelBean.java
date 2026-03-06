package com.sobot.chat.widget.html;

import java.util.List;
import java.util.Map;

public class SobotHtmlLabelBean {
    public String tag;//当前Tag
    public int startIndex;//tag开始角标
    public int endIndex;//tag结束的角标
    public List<SobotHtmlLabelRangeBean> ranges;
    public String color;
    public String fontSize;
    public String textdecoration;
    public String textdecorationline;
    public String backgroundColor;
    public String background;
    public String fontweight;
    public String fontstyle;

    // 添加属性存储字段
    public Map<String, String> attributes;
}
