package com.oplay.giftassistant.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by zsigui on 15-12-25.
 */
public class StringUtil {

	private static Matcher getMatcher(CharSequence data, String pattern, int flag) {
		Pattern p = Pattern.compile(pattern, flag);
		return p.matcher(data);
	}

	public static boolean find(CharSequence data, String pattern, boolean isSensitive) {
		return getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE).find();
	}

	public static boolean matches(CharSequence data, String pattern, boolean isSensitive) {
		return getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE).matches();
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex, boolean isSensitive) {
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		String result = null;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		}
		return result;
	}

	public static String findMatch(CharSequence data, String pattern, int groupIndex, String defaultNotFound,
	                               boolean isSensitive) {
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		String result;
		if (matcher.find()) {
			result = matcher.group(groupIndex);
		} else {
			result = defaultNotFound;
		}
		return result;
	}

	public static List<String> findAllMatch(CharSequence data, String pattern, int groupIndex, boolean isSensitive) {
		List<String> result = new ArrayList<>();
		Matcher matcher = getMatcher(data, pattern, isSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		while (matcher.find()) {
			result.add(matcher.group(groupIndex));
		}
		return result;
	}

	/**
	 * 转换输入手机号码的显示格式，中间改为****
	 */
	public static String transePhone(String originPhone) {
		if (TextUtils.isEmpty(originPhone) || originPhone.length() < 11) {
			return originPhone;
		}
		String lastFour = originPhone.substring(originPhone.length() - 4, originPhone.length());
		String front = originPhone.substring(0, originPhone.length() - 8);
		return front + "****" + lastFour;
	}

	/**
	 * 判断字符是否是中文
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否是乱码
	 */
	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|t*|r*|n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					count = count + 1;
				}
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 账号密码输入字符串过滤
	 */
	public static String stringFilter(String text) throws PatternSyntaxException {
		String returnStr = "";
		for (char c : text.toCharArray()) {
			if (c > 32 & c <= 126) {
				returnStr += c;
			}
		}
		return returnStr;
	}

}
