package com.oplay.giftcool.util;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zsigui on 16-1-22.
 */
public class URLUtil {
	public static String getHost(String url) {
		if (!TextUtils.isEmpty(url)) {
			String[] parts = url.split("\\?");
			if (parts.length > 0) {
				return parts[0];
			}
		}
		return url;
	}

	public static Map<String, String> getParams(String url) {
		Map<String, String> params = new HashMap<>();
		if (!TextUtils.isEmpty(url)) {
			String[] parts = url.split("\\?");
			if (parts.length > 1) {
				String paramStr = parts[1];
				String[] pair = paramStr.split("&");
				if (pair.length > 0) {
					for (int i = 0; i < pair.length; i++) {
						String[] kv = pair[i].split("=");
						params.put(kv[0].trim(), kv[1].trim());
					}
				}
			}
		}
		return params;
	}
}
