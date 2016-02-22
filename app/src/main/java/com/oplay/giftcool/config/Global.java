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

	/* 外部缓存存储位置 */
	public final static String EXTERNAL_CACHE = "/gift_cool/cache";
	public final static String IMG_CACHE_PATH = EXTERNAL_CACHE + "/imgs";
	public final static String NET_CACHE_PATH = EXTERNAL_CACHE + "/net";
	public final static String CHANNEL_FILE_NAME_SUFFIX = ".gift_cool";
	public final static int GIFTCOOL_GAME_ID = 2000705;

	/* 全局服务器与本地手机时间差，单位:ms */
	public static long sServerTimeDiffLocal;
	/* Retrofit网络请求接口引擎 */
	private static NetEngine sNetEngine;

	/* ImageLoader默认图片加载配置 */
	public static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_img_default)
			.showImageOnFail(R.drawable.ic_img_default)
			.showImageOnLoading(R.drawable.ic_img_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();
	/* 头像的ImageLoader加载配置 */
	public static final DisplayImageOptions AVATAR_IMAGE_LOADER = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_avatar_default)
			.showImageOnFail(R.drawable.ic_avatar_default)
			.showImageOnLoading(R.drawable.ic_avatar_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();
	public static final DisplayImageOptions BANNER_IMAGE_LOADER = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_banner_default)
			.showImageOnFail(R.drawable.ic_banner_default)
			.showImageOnLoading(R.drawable.ic_banner_default)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.build();
	public static NetEngine getNetEngine() {
		if (sNetEngine == null) {
			sNetEngine =AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
		}
		return sNetEngine;
	}

	/**
	 * 公用线程池，处理异步任务
	 */
	public final static Executor THREAD_POOL = Global_Executor.getCachedThreadPool();

	private static HashSet<String> sAppName = null;

	public static HashSet<String> getInstalledAppNames() {
		if (sAppName == null) {
			sAppName = SystemUtil.getInstalledAppName(AssistantApp.getInstance().getApplicationContext());
		}
		return sAppName;
	}

    private static int sBannerHeight = 0;

    public static int getBannerHeight(Context context) {
        if (sBannerHeight == 0) {
            sBannerHeight = 256 * context.getResources().getDisplayMetrics().widthPixels / 705;
        }
        return sBannerHeight;
    }
}
