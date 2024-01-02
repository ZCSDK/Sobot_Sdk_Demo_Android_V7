package com.sobot.chat;

import com.sobot.chat.listener.SobotPlusMenuListener;
import com.sobot.chat.widget.kpswitch.view.ChattingPanelUploadView;

import java.util.List;

public class SobotUIConfig {
    public static final int DEFAULT = -1;
    public static boolean sobot_title_right_menu1_display = true;//toolbar右边第一个按钮是否显示 默认显示（更多）
    public static boolean sobot_title_right_menu2_display = false;//toolbar右边第二个按钮是否显示 默认隐藏（评价）
    public static boolean sobot_title_right_menu3_display = false;//toolbar右边第三个个按钮是否显示（打电话） 默认隐藏 2.9.5新增
    public static int sobot_title_right_menu2_bg = DEFAULT;//修改toolbar右边第二个按钮的图片
    public static int sobot_title_right_menu3_bg = DEFAULT;//修改toolbar右边第三个按钮的图片 2.9.5新增
    //toolbar右边第二个按钮需要拨打电话的号码
    public static String sobot_title_right_menu2_call_num = "";
    //toolbar右边第三个按钮需要拨打电话的号码 2.9.5新增
    public static String sobot_title_right_menu3_call_num = "";
    public static boolean sobot_webview_title_display = true;//网页跳转页是否显示标题
    /**
     * 更多面板中的菜单配置
     */
    public static final class pulsMenu {
        public static List<ChattingPanelUploadView.SobotPlusEntity> menus;
        public static List<ChattingPanelUploadView.SobotPlusEntity> operatorMenus;

        public static SobotPlusMenuListener sSobotPlusMenuListener;
    }
}