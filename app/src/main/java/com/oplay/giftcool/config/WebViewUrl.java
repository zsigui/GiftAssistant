package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-15.
 */
public class WebViewUrl {

	public static final String URL_DOMAIN = ".ouwan.com";
	public static final String TEST_URL_BASE = "http://test.giftcool.ouwan.com/";
	public static final String URL_BASE = "http://libao.ouwan.com/";
	public static final String ICON_GCOOL = "http://lb-cdn.ymapp.com/static/img/icon192_192.png";

	public static String getBaseUrl() {
		return AppConfig.TEST_MODE ? TEST_URL_BASE : URL_BASE;
	}

	public static final String GAME_DETAIL = getBaseUrl() + "m/game-detail/";
	public static final String GIFT_DETAIL = getBaseUrl() + "m/gift/";
	public static final String OUWAN_BEAN_DETAIL = getBaseUrl() + "m/ouwan-coin/";
	public static final String OUWAN_BEAN_DETAIL_NOTE = getBaseUrl() + "m/ouwanb-intro/";
	public static final String SCORE_DETAIL = getBaseUrl() + "m/integral/";
	public static final String SCORE_DETAIL_NOTE = getBaseUrl() + "m/integral-intro/";

}
