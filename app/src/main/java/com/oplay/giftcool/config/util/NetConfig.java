package com.oplay.giftcool.config.util;

/**
 * Created by zsigui on 16-4-29.
 */
public class NetConfig {

	private static final class SingletonHolder {
		static final NetConfig sInstance = new NetConfig();
	}

	private NetConfig() {

	}

	public static NetConfig getInstance() {
		return SingletonHolder.sInstance;
	}

	private final String STR_INIT = "init";
	private final String STR_UPGRADE = "upgrade";
	private final String STR_REPORTED_INFO = "reported_info";
	private final String STR_SEARCH = "search";
	private final String STR_SEARCH_PROMT = "search_prompt";
	private final String STR_REQUEST_GIFT = "request_gift";
	private final String STR_INDEX_NEW = "new_gift_list";
	private final String STR_GET_LIKE = "";
}
