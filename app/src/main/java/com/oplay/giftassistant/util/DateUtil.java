package com.oplay.giftassistant.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by zsigui on 15-12-29.
 */
public class DateUtil {
	public static Date getDate(Date curDate, int day) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(curDate);
		calendar.add(Calendar.DATE, day);
		return calendar.getTime();
	}

	/**
	 * 获取当前日期n天前后的日期字符串
	 *
	 * @param format 日期字符串格式化格式
	 * @param day 日期，0代表当天，-n之前，+n之后
	 * @return
	 */
	public static String getDate(String format, int day) {
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		Date date = new Date(System.currentTimeMillis());
		return sdf.format(getDate(date, day));
	}
}
