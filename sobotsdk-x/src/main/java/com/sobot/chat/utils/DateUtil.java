package com.sobot.chat.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期时间工具类
 */
public class DateUtil {
    /**
     * 时:分
     */
    public final static SimpleDateFormat DATE_FORMAT0 = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    /**
     * 年-月-日 时:分:秒
     */
    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    /**
     * 年-月-日
     */
    public final static SimpleDateFormat DATE_FORMAT2 = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault());
    /**
     * 时:分
     */
    public final static SimpleDateFormat DATE_FORMAT3 = new SimpleDateFormat(
            "HH:mm", Locale.getDefault());
    /**
     * 分:秒
     */
    public final static SimpleDateFormat DATE_FORMAT4 = new SimpleDateFormat(
            "mm:ss", Locale.getDefault());

    /**
     * x月x日
     */
    public final static SimpleDateFormat DATE_FORMAT5 = new SimpleDateFormat(
            "MM月dd日", Locale.getDefault());

    /**
     * 月-日
     */
    public final static SimpleDateFormat DATE_FORMAT6 = new SimpleDateFormat(
            "MM-dd", Locale.getDefault());

    /**
     * 将毫秒级整数转换为字符串格式时间
     *
     * @param millisecondDate 毫秒级时间整数
     * @param format          要转换成的时间格式(参见 DateUtil常量)
     * @return 返回相应格式的时间字符串
     */
    public static String toDate(long millisecondDate, SimpleDateFormat format) {
        String time = "";
        try {
            time = format.format(new Date(millisecondDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    public static long stringToLongMs(String date) {
        if (!TextUtils.isEmpty(date)) {
            try {
                Calendar seconds = Calendar.getInstance();
                seconds.setTime(DATE_FORMAT4.parse(date));
                return seconds.get(Calendar.SECOND);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static long stringToLong(String date) {
        if (!TextUtils.isEmpty(date)) {
            try {
                return DATE_FORMAT.parse(date).getTime() / 1000;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 把long 转换成 日期 再转换成String类型
     */
    public static String longToDateStr(Long millSec, String dateFormat, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, locale != null ? locale : Locale.getDefault());
        Date date = new Date(millSec);
        return sdf.format(date);
    }

    /**
     * 把字符串形式的时间戳转换为指定格式的日期字符串
     *
     * @param millSecStr 字符串形式的时间戳(毫秒)
     * @param dateFormat 目标日期格式
     * @return 指定格式的日期字符串
     */
    public static String longStrToDateStr(String millSecStr, String dateFormat, Locale locale) {
        if (TextUtils.isEmpty(millSecStr)) {
            return "";
        }
        try {
            long timestamp = Long.parseLong(millSecStr);
            // 判断是否为秒级时间戳（长度为10位），如果是则转换为毫秒级（乘以1000）
            if (String.valueOf(timestamp).length() == 10) {
                timestamp = timestamp * 1000;
            }
            return longToDateStr(timestamp, dateFormat, locale);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式化时间
     * 时间是当日内时，显示小时和分钟，时间不是当日内时，时间显示月-日
     *
     * @param time
     * @return
     */
    public static String formatDateTime2(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }
        try {
            Calendar current = Calendar.getInstance();
            Calendar today = Calendar.getInstance();    //今天
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            current.setTime(new Date(Long.parseLong(time)));
            if (current.before(today)) {
                return toDate(Long.parseLong(time), DATE_FORMAT5);
            } else {
                return toDate(Long.parseLong(time), DATE_FORMAT3);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            return "";
        }
    }


    public static void main(String[] args) {

//        String time = formatDateTime("2016-01-07 15:41:00", true, "今天");
//        System.out.println("time:" + time);
//        time = formatDateTime("2016-01-03 11:41:00");
//        System.out.println("time:" + time);
//        time = formatDateTime("2016-01-01 15:43:00");
//        System.out.println("time:" + time);
    }


    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTime() {
        return toDate(System.currentTimeMillis(), DATE_FORMAT3);
    }

    public static Date parse(String str, SimpleDateFormat format) {
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return date;
    }

    //-----------------------------
    public static String YEAR_DATE_FORMAT = "yyyy-MM-dd";

    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String DATE_FORMAT_CHINESE = "yyyy年M月d日";

    /**
     * 获取当前日期
     *
     * @return
     */
    public static String getCurrentDate() {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(DateUtil.YEAR_DATE_FORMAT);
        datestr = df.format(new Date());
        return datestr;
    }

    /**
     * 获取当前日期时间
     *
     * @return
     */
    public static String getCurrentDateTime() {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT);
        datestr = df.format(new Date());
        return datestr;
    }

    /**
     * 获取当前日期时间
     *
     * @return
     */
    public static String getCurrentDateTime(String Dateformat) {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(Dateformat);
        datestr = df.format(new Date());
        return datestr;
    }

    public static String dateToDateTime(Date date) {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(DateUtil.DATE_TIME_FORMAT);
        datestr = df.format(date);
        return datestr;
    }

    /**
     * 将字符串日期转换为日期格式
     *
     * @param datestr
     * @return
     */
    public static Date stringToDate(String datestr) {

        if (datestr == null || datestr.equals("")) {
            return null;
        }
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(DateUtil.YEAR_DATE_FORMAT);
        try {
            date = df.parse(datestr);
        } catch (ParseException e) {
            date = DateUtil.stringToDate(datestr, "yyyyMMdd");
        }
        return date;
    }

    /**
     * 将字符串日期转换为日期格式
     * 自定義格式
     *
     * @param datestr
     * @return
     */
    public static Date stringToDate(String datestr, String dateformat) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        try {
            date = df.parse(datestr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    /**
     * 将日期格式日期转换为字符串格式
     *
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(DateUtil.YEAR_DATE_FORMAT);
        datestr = df.format(date);
        return datestr;
    }

    /**
     * 将日期格式日期转换为字符串格式 自定義格式
     *
     * @param date
     * @param dateformat
     * @return
     */
    public static String dateToString(Context context, Date date, String dateformat) {
        if (date == null || TextUtils.isEmpty(dateformat)) {
            return "";
        }
        String datestr = "";
        try {
            SimpleDateFormat df = null;
            if (context != null) {
                Locale language = (Locale) SharedPreferencesUtil.getObject(context, ZhiChiConstant.SOBOT_LANGUAGE);
                if (language != null) {
                    df = new SimpleDateFormat(dateformat, language);
                } else {
                    df = new SimpleDateFormat(dateformat, Locale.getDefault());
                }
            } else {
                df = new SimpleDateFormat(dateformat, Locale.getDefault());
            }
            datestr = df.format(date);
        } catch (Exception ignored) {
        }
        return datestr;
    }

    /**
     * 获取日期的DAY值
     *
     * @param date 输入日期
     * @return
     */
    public static int getDayOfDate(Date date) {
        int d = 0;
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        d = cd.get(Calendar.DAY_OF_MONTH);
        return d;
    }

    /**
     * 获取日期的MONTH值
     *
     * @param date 输入日期
     * @return
     */
    public static int getMonthOfDate(Date date) {
        int m = 0;
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        m = cd.get(Calendar.MONTH) + 1;
        return m;
    }

    /**
     * 获取日期的YEAR值
     *
     * @param date 输入日期
     * @return
     */
    public static int getYearOfDate(Date date) {
        int y = 0;
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        y = cd.get(Calendar.YEAR);
        return y;
    }

    /**
     * 获取星期几
     *
     * @param date 输入日期
     * @return
     */
    public static int getWeekOfDate(Date date) {
        int wd = 0;
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        wd = cd.get(Calendar.DAY_OF_WEEK) - 1;
        return wd;
    }


    /**
     * 判断给定的时间字符串是否是今年
     *
     * @param timeStr 时间字符串
     * @return true表示是今年，false表示不是今年
     */
    public static boolean isThisYear(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) {
            return false;
        }
        try {
            // 首先尝试解析为时间戳
            if (isNumeric(timeStr)) {
                long timestamp = Long.parseLong(timeStr);
                return isThisYear(timestamp);
            }
            // 如果不是纯数字，则按照日期格式解析
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date inputDate = sdf.parse(timeStr);
            return isThisYear(inputDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 判断给定的时间戳是否是今年
     *
     * @param timestamp 时间戳(毫秒)
     * @return true表示是今年，false表示不是今年
     */
    public static boolean isThisYear(long timestamp) {
        try {
            // 获取当前年份
            Calendar currentCalendar = Calendar.getInstance();
            int currentYear = currentCalendar.get(Calendar.YEAR);
            // 获取输入日期的年份
            Calendar inputCalendar = Calendar.getInstance();
            inputCalendar.setTimeInMillis(timestamp);
            int inputYear = inputCalendar.get(Calendar.YEAR);
            // 比较年份是否相同
            return currentYear == inputYear;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 判断给定的时间戳是否是当天
     *
     * @param timestamp 时间戳(毫秒)
     * @return true表示是当天，false表示不是当天
     */
    public static boolean isToday(long timestamp) {
        try {
            // 获取当前日期
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTimeInMillis(System.currentTimeMillis());
            // 获取输入日期
            Calendar inputCalendar = Calendar.getInstance();
            inputCalendar.setTimeInMillis(timestamp);
            // 比较年、月、日是否相同
            return currentCalendar.get(Calendar.YEAR) == inputCalendar.get(Calendar.YEAR)
                    && currentCalendar.get(Calendar.MONTH) == inputCalendar.get(Calendar.MONTH)
                    && currentCalendar.get(Calendar.DAY_OF_MONTH) == inputCalendar.get(Calendar.DAY_OF_MONTH);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断给定的时间字符串是否是当天
     *
     * @param timeStr 时间字符串(可以是时间戳或日期格式字符串)
     * @return true表示是当天，false表示不是当天
     */
    public static boolean isToday(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) {
            return false;
        }

        try {
            // 首先尝试解析为时间戳
            if (isNumeric(timeStr)) {
                long timestamp = Long.parseLong(timeStr);
                return isToday(timestamp);
            }

            // 如果不是纯数字，则按照日期格式解析
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date inputDate = sdf.parse(timeStr);
            return isToday(inputDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断字符串是否为纯数字
     *
     * @param str 要检查的字符串
     * @return true表示是纯数字，false表示不是
     */
    private static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据语言环境获取日期时间格式化规则字符串
     *
     * @param locale       语言环境
     * @param isFullFormat true表示返回年月日时分格式，false表示返回月日时分格式
     * @return 格式化规则字符串
     */
    public static String getDateTimePatternByLanguage(Locale locale, boolean isFullFormat) {
        if (locale == null) {
            return isFullFormat ? "MMM d, yyyy HH:mm" : "MMM d, HH:mm";
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();

        // 中文（简体和繁体）
        if ("zh".equals(language)) {
            if (isFullFormat) {
                if (!TextUtils.isEmpty(country) && ("TW".equals(country) || "HK".equals(country))) {
                    // 繁体中文
                    return "yyyy年M月d日 HH:mm";
                } else {
                    // 简体中文
                    return "yyyy年M月d日 HH:mm";
                }
            } else {
                return "M月d日 HH:mm";
            }
        }
        // 英语
        else if ("en".equals(language)) {
            return isFullFormat ? "MMM d, yyyy HH:mm" : "MMM d, HH:mm";
        }
        // 法语
        else if ("fr".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 葡萄牙语
        else if ("pt".equals(language)) {
            return isFullFormat ? "d 'de' MMM 'de' yyyy HH:mm" : "d 'de' MMM HH:mm";
        }
        // 西班牙语
        else if ("es".equals(language)) {
            return isFullFormat ? "d 'de' MMM 'de' yyyy HH:mm" : "d 'de' MMM HH:mm";
        }
        // 俄语
        else if ("ru".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 意大利语
        else if ("it".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 日语
        else if ("ja".equals(language)) {
            return isFullFormat ? "yyyy年M月d日 HH:mm" : "M月d日 HH:mm";
        }
        // 韩语
        else if ("ko".equals(language)) {
            return isFullFormat ? "yyyy년 M월 d일 HH:mm" : "M월 d일 HH:mm";
        }
        // 德语
        else if ("de".equals(language)) {
            return isFullFormat ? "d. MMM yyyy HH:mm" : "d. MMM HH:mm";
        }
        // 印度尼西亚语
        else if ("in".equals(language) || "id".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 荷兰语
        else if ("nl".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 马来语
        else if ("ms".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 泰语
        else if ("th".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 越南语
        else if ("vi".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 阿拉伯语
        else if ("ar".equals(language)) {
            return isFullFormat ? "d MMM yyyy HH:mm" : "d MMM HH:mm";
        }
        // 土耳其语
        else if ("tr".equals(language)) {
            return isFullFormat ? "d MMM yyyy, HH:mm" : "d MMM HH:mm";
        }
        // 默认格式 英文
        else {
            return isFullFormat ? "MMM d, yyyy HH:mm" : "MMM d, HH:mm";
        }
    }


}