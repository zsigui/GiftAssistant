package com.oplay.giftcool;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftcool.asynctask.AsyncTask_InitApplication;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.ext.gson.NullStringToEmptyAdapterFactory;
import com.oplay.giftcool.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.service.ClockService;
import com.oplay.giftcool.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftcool.util.ChannelUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SoundPlayer;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;
import com.squareup.okhttp.OkHttpClient;
import com.tendcloud.tenddata.TCAgent;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

	private final static String TD_APP_ID = "7E57533EDCF044DA1BF657D786E0FDF7";
	private static AssistantApp sInstance;
	private Retrofit mRetrofit;
	private Gson mGson;

	private UpdateInfo mUpdateInfo;
	private ArrayList<String> mQQInfo;
	private String mStartImg;
	private IndexBanner mBroadcastBanner;
	private int mChannelId = -1;

	// 是否安装完成自动删除
	private boolean mShouldAutoDeleteApk = false;
	// 是否自动检查版本更新
	private boolean mShouldAutoCheckUpdate = true;
	// 是否推送消息
	private boolean mShouldPushMsg = true;
	// 是否已经完成全局初始化
	private boolean mIsGlobalInit = false;
	// 是否允许显示下载，根据渠道获取而定
	private boolean mIsAllowDownload = true;
	// 是否记住密码
	private boolean mIsRememberPwd = true;

	// 以下暂无
	// 是否下载完成自动安装
	private boolean mShouldAutoInstall = false;
	// 是否开启省流模式
	private boolean mIsSaveFlow = false;
	// 是否启用下载完成提示音
	private boolean mIsPlayDownloadComplete = false;

	// LeakCanary 用于检测内存泄露
//	private RefWatcher mRefWatcher;

	@Override
	public void onCreate() {
		super.onCreate();
//		mRefWatcher = LeakCanary.install(this);
		// enabled StrictMode only in TEST
		KLog.init(AppDebugConfig.IS_DEBUG);
		if (AppDebugConfig.IS_DEBUG) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			}
			KLog.d("Gift Cool App is Start Now");
		}
		sInstance = this;
		TCAgent.init(this, TD_APP_ID, getChannelId() + "");
		initImageLoader();
		// 初始配置加载列表
		initLoadingView();
		Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
	}

	public void initLoadingView() {
		LoadAndRetryViewManager.DEFAULT_EMPTY_VIEW_ID = R.layout.fragment_data_empty;
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
	 * get a watcher to watch whether exits the problem of memory leak
	 */
//	public static RefWatcher getRefWatcher(Context context) {
//		return ((AssistantApp) context.getApplicationContext()).mRefWatcher;
//	}

	/**
	 * do work to release the resource when app exit
	 */
	public void exit() {
		try {
			ClockService.stopService(getApplicationContext());
			ThreadUtil.destory();
			setGlobalInit(false);
			if (ImageLoader.getInstance().isInited()) {
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().stop();
				ImageLoader.getInstance().destroy();
			}
			DownloadNotificationManager.cancelDownload(getApplicationContext());
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "exit exception : " + e);
			}
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMemoryInfo();
		}
		ImageLoader.getInstance().clearMemoryCache();
	}

	public void initRetrofit() {
		mGson = new GsonBuilder()
				.registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
				.serializeNulls()
				.setDateFormat("yyyy-MM-dd HH:mm")
				.create();
//		File httpCacheDir = StorageUtils.getOwnCacheDirectory(this, Global.NET_CACHE_PATH);
//		Cache cache = new Cache(httpCacheDir, 10 * 1024 * 1024);
//		Interceptor interceptor = new Interceptor() {
//			@Override
//			public Response intercept(Interceptor.Chain chain) throws IOException {
//				Request request = chain.request();
//				if (!NetworkUtil.isConnected(getApplicationContext())) {
//					request = request.newBuilder()
//							.cacheControl(CacheControl.FORCE_CACHE)
//							.build();
//				}
//				Response response = chain.proceed(request);
//				if (NetworkUtil.isConnected(getApplicationContext())) {
//					String cacheControl = request.cacheControl().toString();
//					response.newBuilder()
//							.header("Cache-Control", cacheControl)
//							.removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
//							.build();
//				} else {
//					response.newBuilder()
//							.header("Cache-Control", "public, only-if-cached, max-stale=" + (60 * 60 * 24 * 2))
//							.removeHeader("Pragma")
//							.build();
//				}
//				return response;
//			}
//		};
		OkHttpClient httpClient = new OkHttpClient();
//		httpClient.interceptors().add(interceptor);
//		httpClient.networkInterceptors().add(interceptor);
//		httpClient.setCache(cache);
		httpClient.setConnectTimeout(AppConfig.NET_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
		httpClient.setReadTimeout(AppConfig.NET_READ_TIMEOUT, TimeUnit.MILLISECONDS);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_APP, "request url = " + NetUrl.getBaseUrl());
		}
		mRetrofit = new Retrofit.Builder()
				.baseUrl(NetUrl.getBaseUrl())
				.addConverterFactory(GsonConverterFactory.create(mGson))
				.client(httpClient)
				.build();
	}

	/**
	 * initial the configuration of Universal-Image-Loader
	 */
	public void initImageLoader() {
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
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "ImageLoader.init()");
			}
		} catch (Throwable e) {
			ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_APP, "ImageLoader.init() in failed");
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
		mIsRememberPwd= SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
				SPConfig.KEY_REMEMBER_PWD, true);
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

	public void setIsRememberPwd(boolean isRememberPwd) {
		mIsRememberPwd = isRememberPwd;
		SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_REMEMBER_PWD,
				isRememberPwd);
	}

	public boolean isRememberPwd() {
		return mIsRememberPwd;
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

	public UpdateInfo getUpdateInfo() {
		return mUpdateInfo;
	}

	public void setUpdateInfo(UpdateInfo updateInfo) {
		mUpdateInfo = updateInfo;
	}

	public ArrayList<String> getQQInfo() {
		return mQQInfo;
	}

	public void setQQInfo(ArrayList<String> QQInfo) {
		mQQInfo = QQInfo;
	}

	public String getStartImg() {
		return SPUtil.getString(this, SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, null);
	}

	public void setStartImg(String startImg) {
		if (TextUtils.isEmpty(startImg)) {
			return;
		}
		mStartImg = startImg;
		SPUtil.putString(AssistantApp.getInstance(), SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, mStartImg);
	}

	public IndexBanner getBroadcastBanner() {
		return mBroadcastBanner;
	}

	public void setBroadcastBanner(IndexBanner broadcastBanner) {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_APP, "broadcastBanner = " + broadcastBanner);
		}
		if (broadcastBanner == null) {
			return;
		}
		mBroadcastBanner = broadcastBanner;
		if (!TextUtils.isEmpty(broadcastBanner.url) && ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().loadImage(broadcastBanner.url, null);
		}
	}

	public int getChannelId() {
		if (mChannelId == -1) {
			mChannelId = ChannelUtil.getChannelId(this);
		}
		return mChannelId;
	}


}
