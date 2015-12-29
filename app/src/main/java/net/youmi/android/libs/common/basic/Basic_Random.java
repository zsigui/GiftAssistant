package net.youmi.android.libs.common.basic;

import java.util.Random;

import net.youmi.android.libs.common.debug.Debug_SDK;

public class Basic_Random {

	private static final Random mRd = new Random(System.currentTimeMillis());

	/**
	 * 获取随机数
	 */
	public final static int nextInt() {
		return mRd.nextInt();
	}

	/**
	 * 获取指定上限内的随机数
	 */
	public final static int nextInt(int max) {
		return mRd.nextInt(max);
	}

	/**
	 * 创建一个随机数字组成的字符串
	 * 
	 * @param num
	 *            字符串的长度
	 * @return
	 */
	public final static String createRandom_Number_String(int num) {
		try {
			if (num <= 0) {
				return "";
			}

			StringBuilder sb = new StringBuilder(num);
			for (int i = 0; i < num; i++) {
				int a = nextInt(9);
				if (a == 0 && i == 0) {
					sb.append(1);
				} else {
					sb.append(a);
				}
			}
			return sb.toString();

		} catch (Throwable e) {
			if (Debug_SDK.isBasicLog) {
				Debug_SDK.te(Debug_SDK.mBasicTag, Basic_Random.class, e);
			}
		}

		return "";
	}
}
