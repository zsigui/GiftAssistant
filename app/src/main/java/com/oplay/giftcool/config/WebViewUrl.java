package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-15.
 */
public class WebViewUrl {

	public static final String URL_DOMAIN = ".ouwan.com";
	public static final String TEST_URL_BASE = "http://test.giftcool.ouwan.com/";
	public static final String URL_BASE = "http://libao.ouwan.com/";
	// 分享的Icon地址
	public static final String ICON_GCOOL = "http://lb-cdn.ymapp.com/static/img/icon192_192.png";

	public static String REAL_URL = TEST_URL_BASE;

	public static String getBaseUrl() {
		return AppConfig.TEST_MODE ? REAL_URL : URL_BASE;
	}

	public static final String GAME_DETAIL = "m/game-detail/";
	public static final String GIFT_DETAIL = "m/gift/";
	public static final String OUWAN_BEAN_DETAIL = "m/ouwan-coin/";
	public static final String OUWAN_BEAN_DETAIL_NOTE = "m/ouwanb-intro/";
	public static final String SCORE_DETAIL = "m/integral/";
	public static final String SCORE_DETAIL_NOTE = "m/integral-intro/";
	public static final String LOTTERY = "campaigns/2016/lottery/?need_validate=1";
	public static final String SIGN_IN = "campaigns/2016/checkin/?need_validate=1";
	public static final String ACTIVITY_DETAIL = "article/detail/%d/";
	public static final String ACTIVITY_COMMENT_LIST = "comment/?activity_id=%d&comment_id=%d";

	public static String getWebUrl(String url) {
		if (AppConfig.TEST_MODE) {
			if (getBaseUrl() != null && !getBaseUrl().endsWith("/")) {
				REAL_URL += "/";
			}
		}
		return getBaseUrl() + url;
	}
}
