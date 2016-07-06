package com.oplay.giftcool.util;

import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zsigui on 15-12-29.
 */
public class DateUtil {

    /**
     * 提取时间，针对 yyyy-MM-dd HH:mm:ss 格式
     */
    public static String optDate(String date) {
        String result;
        if (isToday(date)) {
            result = "今日 " + date.substring(11, 16);
        } else {
            result = String.format("%s月%s日 %s", date.substring(5, 7), date.substring(8, 10), date.substring(11, 16));
        }
        return result;
    }

    /**
     * 提取时间，针对 MM-dd HH:mm 格式
     */
    public static String optDate(String date, long time) {
        String result;
        if (isToday(time)) {
            result = "今日 " + date.substring(6, date.length());
        } else {
            result = String.format("%s月%s日 %s", date.substring(0, 2), date.substring(3, 5),
                    date.substring(6, date.length()));
        }
        return result;
    }

    /**
     * 提取时间，针对 yyyy-MM-dd HH:mm:ss 格式
     */
    public static String optDateLong(String date, long time) {
        String result;
        if (isToday(time)) {
            result = "今日 " + date.substring(11, 16);
        } else {
            result = String.format("%s月%s日 %s", date.substring(5, 7), date.substring(8, 10),
                    date.substring(11, 16));
        }
        return result;
    }

    public static String formatTime(String timeStr, String format) {
        if (TextUtils.isEmpty(timeStr))
            return null;
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            result = sdf.format(sdf.parse(timeStr));
        } catch (ParseException e) {
            // not print
        }
        return (result == null ?
                (timeStr.length() > format.length() ? timeStr.substring(0, format.length()) : timeStr)
                : result);
    }

    public static String formatTime(long time, String format) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(time));
    }

    public static Date getDate(Date curDate, int day) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    public static long getTime(String dateStr) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        long curTimeMillisecond = 0;
        if (!TextUtils.isEmpty(dateStr)) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateStr);
                curTimeMillisecond = date.getTime();
            } catch (ParseException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
            }
        }
        return curTimeMillisecond;
    }

    /**
     * 获取当前日期n天前后的日期字符串
     *
     * @param format 日期字符串格式化格式
     * @param day    日期，0代表当天，-n之前，+n之后
     * @return
     */
    public static String getDate(String format, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(getDate(date, day));
    }

    /**
     * 判断所给日期字符串时间是否为今天
     */
    public static boolean isToday(String date) {
        try {
            if (TextUtils.isEmpty(date) || date.length() < 10) {
                // 日期异常
                return false;
            }
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String before = sdf.format(sdf.parse(date));
            return before.equals(sdf.format(new Date(System.currentTimeMillis())));
        } catch (ParseException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
        }
        return false;
    }

    /**
     * 判断所给日期字符串时间是否为今天
     */
    public static boolean isToday(long dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String before = sdf.format(new Date(dateTime));
        return before.equals(sdf.format(new Date(System.currentTimeMillis())));
    }

    public static String getGmtDate(int hours) {
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM y HH:mm:ss 'GMT'", Locale.US);
        TimeZone gmtZone = TimeZone.getTimeZone("GMT+8");
        sdf.setTimeZone(gmtZone);
        Date date = new Date(System.currentTimeMillis() + 3600 * 1000 * hours);
        return sdf.format(date);
    }

    /**
     * @time 单位: s 秒
     */
    public static String formatUserReadDate(long time) {
        if (time == 0) {
            return "";
        }
        Date date = null;

        date = new Date(time * 1000);
        Calendar current = Calendar.getInstance();

        Calendar today = Calendar.getInstance();    //今天
        today.set(Calendar.YEAR, current.get(Calendar.YEAR));
        today.set(Calendar.MONTH, current.get(Calendar.MONTH));
        today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) + 1);
        //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar tomorrow = Calendar.getInstance();    //明天
        tomorrow.set(Calendar.YEAR, current.get(Calendar.YEAR));
        tomorrow.set(Calendar.MONTH, current.get(Calendar.MONTH));
        tomorrow.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) + 2);
        //  Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        current.setTime(date);
        current.set(Calendar.SECOND, 0);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
//        SimpleDateFormat f = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat format;
        if (current.after(today) && current.before(tomorrow)) {
            // current > today 00 && current < tomorrow 00
            format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "明天" + format.format(date);
        } else if (current.after(tomorrow) || current.getTime().compareTo(tomorrow.getTime()) == 0) {
            // current > tomorrow 00 && current == tomorrow 00
            if (current.get(Calendar.HOUR) == 0
                    && current.get(Calendar.MINUTE) == 0) {
                format = new SimpleDateFormat("MM-dd", Locale.getDefault());
                return format.format(date);
            } else {
                format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                return format.format(date);
            }
        } else if (current.getTime().compareTo(today.getTime()) == 0) {
            // current = today 00
            return "明天";
        } else {
            // current < today 00
            format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "今天" + format.format(date);
        }
    }
}
