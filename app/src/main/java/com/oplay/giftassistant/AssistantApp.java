package com.oplay.giftassistant;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftassistant.asynctask.AsyncTask_InitApplication;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.config.Global;
import com.oplay.giftassistant.config.NetUrl;
import com.oplay.giftassistant.config.SPConfig;
import com.oplay.giftassistant.ext.gson.NullStringToEmptyAdapterFactory;
import com.oplay.giftassistant.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftassistant.manager.OuwanSDKManager;
import com.oplay.giftassistant.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftassistant.util.SPUtil;
import com.oplay.giftassistant.util.SoundPlayer;
import com.socks.library.KLog;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;

import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

	private static AssistantApp sInstance;
	private Retrofit mRetrofit;
	private Gson mGson;

	// 是否安装完成自动删除
	private boolean mShouldAutoDeleteApk = false;
	// 是否自动检查版本更新
	private boolean mShouldAutoCheckUpdate = true;
	// 是否推送消息
	private boolean mShouldPushMsg = true;

	// 以下暂无
	// 是否下载完成自动安装
	private boolean mShouldAutoInstall = false;
	// 是否开启省流模式
	private boolean mIsSaveFlow = false;
	// 是否启用下载完成提示音
	private boolean mIsPlayDownloadComplete = false;
	// 是否已经完成全局初始化
	private boolean mIsGlobalInit;
	// 是否允许显示下载，根据渠道获取而定
	private boolean mIsAllowDownload = true;

	@Override
	public void onCreate() {
		super.onCreate();
		// enabled StrictMode only in TEST
		if (AppDebugConfig.IS_DEBUG) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			}
		}
		sInstance = this;
		KLog.init(true);
		initImageLoader();
		initRetrofit();
		initLoadingView();
		initDrawerImageLoader();
		OuwanSDKManager.getInstance().init();
		Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
	}

	private void initLoadingView() {
		LoadAndRetryViewManager.DEFAULT_EMPTY_VIEW_ID = R.layout.fragment_empty_search;
		LoadAndRetryViewManager.DEFAULT_LOAD_VIEW_ID = R.layout.fragment_data_loading;
		// 加载失败，错误或者重试
		LoadAndRetryViewManager.DEFAULT_ERROR_RETRY_VIEW_ID = R.layout.fragment_error_net;
	}

	public static AssistantApp getInstance() {
		return sInstance;
	}

	public Retrofit getRetrofit() {
		return mRetrofit;
	}

	/**
	 * do work to release the resource when app exit
	 */
	public void exit() {
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().stop();
		ImageLoader.getInstance().destroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMemoryInfo();
		}
		ImageLoader.getInstance().clearMemoryCache();
	}

	private void initRetrofit() {
		mGson = new GsonBuilder()
				.registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
				.serializeNulls()
				.setDateFormat("yyyy-MM-dd HH:mm")
				.create();
		mRetrofit = new Retrofit.Builder()
				.baseUrl(NetUrl.URL_BASE)
				.addConverterFactory(GsonConverterFactory.create(mGson))
				.build();
	}

	private void initDrawerImageLoader() {
		DrawerImageLoader.init(new AbstractDrawerImageLoader() {
			@Override
			public void set(ImageView imageView, Uri uri, Drawable placeholder) {
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				ImageLoader.getInstance().displayImage(uri.toString(), imageView, Global.IMAGE_OPTIONS);
			}

			@Override
			public void cancel(ImageView imageView) {
				ImageLoader.getInstance().cancelDisplayTask(imageView);
			}

			@Override
			public Drawable placeholder(Context ctx) {
				return ctx.getResources().getDrawable(R.drawable.ic_img_default);
			}
		});
	}

	/**
	 * initial the configuration of Universal-Image-Loader
	 */
	private void initImageLoader() {
		try {
			final DisplayImageOptions options = Global.IMAGE_OPTIONS;
			final File cacheDir = StorageUtils.getOwnCacheDirectory(this, Global.IMG_CACHE_PATH);
			final long maxAgeTimeInSeconds = 7 * 24 * 60 * 60;   // 7 days cache
			final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
					.threadPoolSize(3)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(new WeakMemoryCache())
					.diskCache(new LimitedAgeDiskCache(cacheDir, maxAgeTimeInSeconds))
					.defaultDisplayImageOptions(options) // default
					.memoryCacheSize(5 * 1024 * 1024)   // memory cache size 5M
					.diskCacheSize(100 * 1024 * 1024)   // disk cache size 100M
					.threadPriority(Thread.NORM_PRIORITY - 2)
					.build();
			ImageLoader.getInstance().init(config);
			L.writeLogs(false);
		} catch (Throwable e) {
			ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	/**
	 * 读写SP里的全局APP配置，最好在线程中调用
	 */
	public void initAppConfig() {
		mShouldAutoCheckUpdate = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_AUTO_CHECK_UPDATE, true);
		mShouldAutoDeleteApk = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_AUTO_DELETE_APK, false);
		mShouldPushMsg = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_ACCEPT_PUSH, true);
		mShouldAutoInstall = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_AUTO_INSTALL, false);
		mIsAllowDownload = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_IS_ALLOW_DOWNLOAD, true);
		mIsSaveFlow = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_IS_SAVE_FLOW, false);
		setIsSaveFlow(mIsSaveFlow);
		mIsPlayDownloadComplete = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_IS_PLAY_DOWNLOAD_COMPLETE, true);
		setIsPlayDownloadComplete(mIsPlayDownloadComplete);
	}

	public boolean isShouldAutoCheckUpdate() {
		return mShouldAutoCheckUpdate;
	}

	public void setShouldAutoCheckUpdate(boolean shouldAutoCheckUpdate) {
		mShouldAutoCheckUpdate = shouldAutoCheckUpdate;
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_AUTO_CHECK_UPDATE,
				shouldAutoCheckUpdate);
	}

	public boolean isShouldPushMsg() {
		return mShouldPushMsg;
	}

	public void setShouldPushMsg(boolean shouldPushMsg) {
		mShouldPushMsg = shouldPushMsg;
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_ACCEPT_PUSH,
				shouldPushMsg);
	}

	public boolean isShouldAutoInstall() {
		return mShouldAutoInstall;
	}

	public void setShouldAutoInstall(boolean shouldAutoInstall) {
		mShouldAutoInstall = shouldAutoInstall;
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_AUTO_INSTALL,
				shouldAutoInstall);
	}

	public boolean isShouldAutoDeleteApk() {
		return mShouldAutoDeleteApk;
	}

	public void setShouldAutoDeleteApk(boolean shouldAutoDeleteApk) {
		mShouldAutoDeleteApk = shouldAutoDeleteApk;
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_AUTO_DELETE_APK,
				shouldAutoDeleteApk);
	}


	public boolean isSaveFlow() {
		return mIsSaveFlow;
	}

	public void setIsSaveFlow(boolean isSaveFlow) {
		mIsSaveFlow = isSaveFlow;
		if (mIsSaveFlow) {
			ImageLoader.getInstance().clearMemoryCache();
			ImageLoader.getInstance().pause();
			ImageLoader.getInstance().denyNetworkDownloads(true);
		} else {
			ImageLoader.getInstance().denyNetworkDownloads(false);
			ImageLoader.getInstance().resume();
		}
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_AUTO_DELETE_APK,
				isSaveFlow);
	}

	public boolean isAllowDownload() {
		return mIsAllowDownload;
	}

	public void setAllowDownload(boolean isAllowDownload) {
		mIsAllowDownload = isAllowDownload;
	}

	public boolean isPlayDownloadComplete() {
		return mIsPlayDownloadComplete;
	}

	public void setIsPlayDownloadComplete(boolean isPlayDownloadComplete) {
		mIsPlayDownloadComplete = isPlayDownloadComplete;
		if (mIsPlayDownloadComplete) {
			// preload download complete sound
			SoundPlayer.getInstance(this);
		}
	}

	public boolean isGlobalInit() {
		return mIsGlobalInit;
	}

	public void setGlobalInit(boolean isGlobalInit) {
		mIsGlobalInit = isGlobalInit;
	}

	public Gson getGson() {
		return mGson;
	}
}
