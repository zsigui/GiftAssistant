package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-15.
 */
public class WebViewUrl {

	//public static final String URL_BASE = "http://172.16.1.250:8977/";
	public static final String TEST_URL_BASE = "http://test.giftcool.ouwan.com/";
	public static final String URL_BASE = "http://libao.ouwan.com/";

	public static String getBaseUrl() {
		return AppConfig.TEST_MODE ? TEST_URL_BASE : URL_BASE;
	}

	public static final String GAME_DETAIL = getBaseUrl() + "m/game-detail/";
	public static final String GIFT_DETAIL = getBaseUrl() + "m/gift/";
	public static final String OUWAN_BEAN_DETAIL = getBaseUrl() + "m/ouwan-coin/";
	public static final String SCORE_DETAIL = getBaseUrl() + "m/integral/";
}
