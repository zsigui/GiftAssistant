package com.oplay.giftcool.download.silent;

import java.net.HttpURLConnection;

/**
 * Created by zsigui on 16-4-26.
 */
public class HttpUtil {

	/**
	 * 判断是否是gzip压缩流
	 */
	public static boolean isGzipStream(final HttpURLConnection urlConnection) {
		String encoding = urlConnection.getContentEncoding();
		return encoding != null && encoding.contains("gzip");
	}
}
