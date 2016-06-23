package com.oplay.giftcool.sharesdk;

import com.oplay.giftcool.BuildConfig;

/**
 * 分享SDK的配置类
 * Created by yxf on 14-11-7.
 */
public class ShareSDKConfig {
	public static final int THUMB_SIZE = 150;

	public static final String ARGS_TITLE = "title";
	public static final String ARGS_DESCRIPTION = "description";
	public static final String ARGS_URL = "url";
	public static final String ARGS_ICON_URL = "iconUrl";
	public static final String ARGS_SHARE_TYPE = "type";
	public static final String ARGS_CONTENT_TYPE = "content_type";

	public static final int QQFRIENDS = 1;
	public static final int QZONE = 2;

	/* 分享 */
	public static final String SHARE_WEXIN_APP_ID = BuildConfig.WX_APPKEY;
	public static final String SHARE_QQ_APP_ID = BuildConfig.QQ_APPKEY;
	public static final int SHARE_REQUEST_CODE = 0xA10;
}
