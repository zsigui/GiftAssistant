package com.oplay.giftassistant.config;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.R;
import com.oplay.giftassistant.engine.NetEngine;

import net.youmi.android.libs.common.global.Global_Executor;

import java.util.concurrent.Executor;

/**
 * Created by zsigui on 15-12-16.
 */
public class Global {

	/* 外部缓存存储位置 */
	public final static String EXTERNAL_CACHE = "/gift_cool/cache";
	public final static String IMG_CACHE_PATH = EXTERNAL_CACHE + "/imgs";

	/* Retrofit网络请求接口引擎 */
	private static NetEngine sNetEngine;

	/* ImageLoader默认图片加载配置 */
	public static final DisplayImageOptions IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_img_empty)
			.showImageOnFail(R.drawable.ic_img_fail)
			.showImageOnLoading(R.drawable.ic_img_loading)
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
}
