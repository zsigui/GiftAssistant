package com.oplay.giftcool.config;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.engine.NetEngine;
import com.oplay.giftcool.util.SystemUtil;

import net.youmi.android.libs.common.global.Global_Executor;

import java.util.HashSet;
import java.util.concurrent.Executor;

/**
 * Created by zsigui on 15-12-16.
 */
public class Global {

	/** 内部渠道文件存储位置 */
	public final static String INTERNAL_INFO_FILE = "gift_cool_info";
	/** 外部缓存存储位置 */
	public final static String EXTERNAL_CACHE = "/gift_cool/cache";
    /**
     * 外部下载文件及对应下载文件缓存存放位置
     */
	public final static String EXTERNAL_DOWNLOAD = "/gift_cool/download";
	/** 外部图片缓存存储位置 */
	public final static String IMG_CACHE_PATH = EXTERNAL_CACHE + "/imgs";
	/** 外部网络请求缓存存储位置 */
	public final static String NET_CACHE_PATH = EXTERNAL_CACHE + "/net";
	/** 外部LOG信息存储位置 */
	public final static String LOGGING_CACHE_PATH = EXTERNAL_CACHE + "/log";
	/** 礼包酷渠道名文件后缀 */
	public final static String CHANNEL_FILE_NAME_SUFFIX = ".gift_cool";
	/** 下载缓存文件的后缀 */
	public final static String TEMP_FILE_NAME_SUFFIX = ".vmtf";
	/** 下载的Apk文件的后缀 */
	public final static String APK_FILE_NAME_SUFFIX = ".apk";
	/** 礼包酷所属游戏ID，用于下载链接统计 */
	public final static int GIFTCOOL_GAME_ID = 2000705;
    /** 倒计时时间间隔，单位:ms */
	public final static int COUNTDOWN_INTERVAL = 1000;
    /** 重复点击的时间间隔，单位:ms */
    public final static int CLICK_TIME_INTERVAL = 500;

	/** 全局服务器与本地手机时间差，单位:ms */
	public static long sServerTimeDiffLocal;
	/** Retrofit网络请求接口引擎 */
	private static NetEngine sNetEngine;

	/** ImageLoader默认图片加载配置 */
	public static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_img_default)
			.showImageOnFail(R.drawable.ic_img_default)
			.showImageOnLoading(R.drawable.ic_img_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();
	/**
	 *  头像的ImageLoader加载配置
	 */
	public static final DisplayImageOptions AVATAR_IMAGE_LOADER = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_avatar_default)
			.showImageOnFail(R.drawable.ic_avatar_default)
			.showImageOnLoading(R.drawable.ic_avatar_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();
	/**
	 * 轮播图的ImageLoader加载配置
	 */
	public static final DisplayImageOptions BANNER_IMAGE_LOADER = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_banner_default)
			.showImageOnFail(R.drawable.ic_banner_default)
			.showImageOnLoading(R.drawable.ic_banner_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();

	/**
	 * 获取网络请求引擎
	 */
	public static NetEngine getNetEngine() {
		if (sNetEngine == null) {
			sNetEngine =AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
		}
		return sNetEngine;
	}

	/**
	 * 重置网络请求引擎，测试的使用重新初始化会调用
	 */
	public static void resetNetEngine() {
		sNetEngine = AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
	}

	/**
	 * 公用线程池，处理异步任务
	 */
	public final static Executor THREAD_POOL = Global_Executor.getCachedThreadPool();

	/**
	 * 手机已安装应用名Hash列表
	 */
	private static HashSet<String> sAppName = null;

	/**
	 * 获取已经安装的应用的Hash列表
	 */
	public static HashSet<String> getInstalledAppNames() {
		if (sAppName == null) {
			sAppName = SystemUtil.getInstalledAppName(AssistantApp.getInstance().getApplicationContext());
		}
		return sAppName;
	}

    private static int sBannerHeight = 0;

	/**
	 * 获取轮播图的高度
	 */
    public static int getBannerHeight(Context context) {
        if (sBannerHeight == 0) {
            sBannerHeight = 256 * context.getResources().getDisplayMetrics().widthPixels / 705;
        }
        return sBannerHeight;
    }
}
