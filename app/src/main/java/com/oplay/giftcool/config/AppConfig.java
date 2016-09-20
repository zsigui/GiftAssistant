package com.oplay.giftcool.config;

import com.oplay.giftcool.BuildConfig;

/**
 * Created by zsigui on 16-1-17.
 */
public class AppConfig {

    /* APP版本信息 */
    public static int SDK_VER() { return BuildConfig.VERSION_CODE; }
    public static String SDK_VER_NAME() { return BuildConfig.VERSION_NAME; }
    public static String PACKAGE_NAME() { return BuildConfig.APPLICATION_ID; }
    public static String OUWAN_SDK_VER() { return BuildConfig.OUWAN_SDK_VERSION; }

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
    public static final int NET_CONNECT_TIMEOUT = 10_000;
    public static final int NET_READ_TIMEOUT = 40_000;
    public static final int NET_WRITE_TIMEOUT = 40_000;
    /* 针对发布图片文字的超时时间 */
    public static final int NET_POST_CONNECT_TIMEOUT = 10_000;
    public static final int NET_POST_READ_TIMEOUT = 80_000;
    public static final int NET_POST_WRITE_TIMEOUT = 80_000;

}
