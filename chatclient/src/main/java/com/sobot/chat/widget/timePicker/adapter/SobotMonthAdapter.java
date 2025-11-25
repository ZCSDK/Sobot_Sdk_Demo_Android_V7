package com.sobot.chat.widget.timePicker.adapter;


import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.ZhiChiConstant;

import java.util.Arrays;
import java.util.List;

/**
 * Month Wheel adapter.
 */
public class SobotMonthAdapter implements SobotWheelAdapter {

    private List<String> months;

    /**
     * Constructor
     */
    public SobotMonthAdapter() {
        String language = SharedPreferencesUtil.getStringData(SobotApp.getApplicationContext(), ZhiChiConstant.SOBOT_INIT_LANGUAGE, "zh");
        months = getMonths(language);
    }

    public Object getItem(int index) {
        if (index >= 0 && index < getItemsCount()) {
            return months.get(index);
        }
        return 0;
    }

    public int getItemsCount() {
        return months.size();
    }

    public int indexOf(Object o) {
        try {
            return months.indexOf(o);
        } catch (Exception e) {
            return -1;
        }

    }
    private List<String> getMonths(String language) {
        String[] monthArr;
        // 英语
        if (language.equals("en")) {
            monthArr = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        }
        // 西班牙语
        else if (language.equals("es")) {
            monthArr = new String[]{"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        }
        // 法语
        else if (language.equals("fr")) {
            monthArr = new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        }
        // 德语
        else if (language.equals("de")) {
            monthArr = new String[]{"Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};
        }
        // 日语+中文
        else if (language.equals("ja")||language.contains("zh")) {
            monthArr = new String[]{"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        }
        // 韩语
        else if (language.equals("ko")) {
            monthArr = new String[]{"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
        }
        // 葡萄牙语
        else if (language.equals("pt")) {
            monthArr = new String[]{"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        }
        // 俄语
        else if (language.equals("ru")) {
            monthArr = new String[]{"Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
        }
        // 阿拉伯语
        else if (language.equals("ar")) {
            monthArr = new String[]{"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
        }
        // 泰语
        else if (language.equals("th")) {
            monthArr = new String[]{"ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."};
        }
        // 意大利语
        else if (language.equals("it")) {
            monthArr = new String[]{"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"};
        }
        // 荷兰语
        else if (language.equals("nl")) {
            monthArr = new String[]{"Jan", "Feb", "Mrt", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"};
        }
        // 默认返回英文
        else {
            monthArr = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        }
        return Arrays.asList(monthArr);
    }
}
