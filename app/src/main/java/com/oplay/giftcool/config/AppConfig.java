package com.oplay.giftcool.config;

import com.oplay.giftcool.BuildConfig;

/**
 * Created by zsigui on 16-1-17.
 */
public class AppConfig {

    /* APP版本信息 */
    public static final int SDK_VER = BuildConfig.VERSION_CODE;
    public static final String SDK_VER_NAME = BuildConfig.VERSION_NAME;
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String OUWAN_SDK_VER = BuildConfig.OUWAN_SDK_VERSION;

    /* 测试环境 */
    public static boolean TEST_MODE = BuildConfig.TEST_MODE;

    /* 通讯密钥 */
    public static final String APP_KEY = BuildConfig.APP_KEY;
    public static final String APP_SECRET = BuildConfig.APP_SECRET;

    /* 上传头像的大小 */
    public static final int UPLOAD_PIC_WIDTH = 400;
    public static final int UPLOAD_PIC_HEIGHT = 400;
    // 头像大小为 50KB以下，现有加密传输模式下，稍大会出现长度溢出，导致服务器接收出错
    public static final int UPLOAD_PIC_SIZE = 50 * 1024;
    /* 评论回复图片大小 */
    public static final int REPLY_PIC_WIDTH = 1280;
    public static final int REPLY_PIC_HEIGHT = 1280;
    // 上传图片大小 4MB 以下
    public static final int REPLY_PIC_SIZE = 4 * 1024 * 1024;
    /* 轮播图播放时间 */
    public static final int BANNER_LOOP_TIME = 5000;


    /* Retrofit 访问网络连接超时时间，单位 ms */
    public static final int NET_CONNECT_TIMEOUT = 8_000;
    public static final int NET_READ_TIMEOUT = 60_000;
    public static final int NET_WRITE_TIMEOUT = 120_000;

}
