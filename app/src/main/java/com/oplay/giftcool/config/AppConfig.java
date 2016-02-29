package com.oplay.giftcool.config;

/**
 * Created by zsigui on 16-1-17.
 */
public class AppConfig {

	/* APP版本信息 */
	public static final int SDK_VER = 5;
	public static final String SDK_VER_NAME = "V1.0.4";

	/* 上传头像的大小 */
	public static final int UPLOAD_PIC_WIDTH = 640;
	public static final int UPLOAD_PIC_HEIGHT = 640;
	public static final int UPLOAD_PIC_QUALITY = 80;
	/* 轮播图播放时间 */
	public static final int BANNER_LOOP_TIME = 5000;

	/* 通讯密钥 */
	public static final String APP_KEY = "3c453306edd43bbc";
	public static final String APP_SECRET = "3b4446772144ade3";

	/* 测试环境 */
//	public static final boolean TEST_MODE = true;
	public static final boolean TEST_MODE = false;


	/* Retrofit 访问网络连接超时时间，单位 ms */
	public static final int NET_CONNECT_TIMEOUT = 8 * 1000;
	public static final int NET_READ_TIMEOUT = 30 * 1000;
}
