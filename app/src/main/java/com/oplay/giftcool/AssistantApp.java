package com.oplay.giftcool;

import android.app.Application;
import android.app.PendingIntent;
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
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.ext.gson.NullStringToEmptyAdapterFactory;
import com.oplay.giftcool.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftcool.util.ChannelUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SoundPlayer;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

	private final static String TD_APP_ID = "0CC59F66C9823F0D3EF90AC61D9735FB";
	private final static String UMENG_APP_KEY = "56cbc68067e58e32bb00231a";
	private static AssistantApp sInstance;
	private Retrofit mRetrofit;
	private Gson mGson;

	private UpdateInfo mUpdateInfo;
	private ArrayList<InitQQ> mQQInfo;
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

	// 说明今日是否推送过消息
	private boolean mIsPushedToday = false;

	// LeakCanary 用于检测内存泄露
//	private RefWatcher mRefWatcher;

	public static AssistantApp getInstance() {
		return sInstance;
	}

	public Retrofit getRetrofit() {
		return mRetrofit;
	}

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

		//初始化TalkingData
//		TCAgent.init(this, TD_APP_ID, getChannelId() + "");
		initUmeng();
		initJPush();
		initImageLoader();
		// 初始配置加载列表
		initLoadingView();
		Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
	}

	/**
	 * 初始化极光推送
	 */
	private void initJPush() {
		JPushInterface.init(this);
		JPushInterface.setDebugMode(AppConfig.TEST_MODE);
		// 设置通知静默时间，不震动和响铃，晚上10点30分-早上8点
		JPushInterface.setSilenceTime(this, 22, 30, 8, 0);
		// 设置默认的通知栏样式
		BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(this);
		builder.statusBarDrawable = R.drawable.ic_stat_notify;
		builder.notificationFlags = PendingIntent.FLAG_UPDATE_CURRENT;
		JPushInterface.setDefaultPushNotificationBuilder(builder);
		// 设置保留最近通知条数 5
		JPushInterface.setLatestNotificationNumber(this, 5);
	}

	/**
	 * 初始化友盟
	 */
	private void initUmeng() {
		AnalyticsConfig.setAppkey(this, UMENG_APP_KEY);
		AnalyticsConfig.setChannel("m" + getChannelId());   //友盟渠道号不能纯数字
		AnalyticsConfig.enableEncrypt(true);
		MobclickAgent.openActivityDurationTrack(false);     //禁止默认的页面统计
	}

	public void initLoadingView() {
		LoadAndRetryViewManager.setDefaultEmptyViewId(R.layout.fragment_data_empty);
		LoadAndRetryViewManager.setDefaultLoadViewId(R.layout.fragment_data_loading);
		// 加载失败，错误或者重试
		LoadAndRetryViewManager.setDefaultErrorRetryViewId(R.layout.fragment_error_net);
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
			ThreadUtil.destroy();
			setGlobalInit(false);
			JPushInterface.clearLocalNotifications(this);
			JPushInterface.onKillProcess(this);
			if (ImageLoader.getInstance().isInited()) {
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().stop();
				ImageLoader.getInstance().destroy();
			}
			DownloadNotificationManager.cancelDownload(getApplicationContext());
			MobclickAgent.onKillProcess(this);
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

	public void initGson() {
		mGson = new GsonBuilder()
				.registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
				.serializeNulls()
				.setDateFormat("yyyy-MM-dd HH:mm:ss")
				.create();
	}

	public void initRetrofit() {
		initGson();
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
		addInterceptorToRetrofit();
	}

	/**
	 * 重设网络并重初始化，测试使用
	 */
	public void resetInitForTest() {
		if (AppConfig.TEST_MODE) {
			initRetrofit();
			Global.resetNetEngine();
			Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
		}
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

	/**
	 * 是否进行自动检查更新
	 */
	public boolean isShouldAutoCheckUpdate() {
		return mShouldAutoCheckUpdate;
	}

	/**
	 * 设置是否自动检查更新
	 */
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
		if (shouldPushMsg) {
			if (JPushInterface.isPushStopped(this)) {
				JPushInterface.resumePush(this);
			}
		} else {
			if (!JPushInterface.isPushStopped(this)) {
				JPushInterface.stopPush(this);
			}
		}
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

	public ArrayList<InitQQ> getQQInfo() {
		return mQQInfo;
	}

	public void setQQInfo(ArrayList<InitQQ> QQInfo) {
		mQQInfo = QQInfo;
	}

	public String getStartImg() {
		return SPUtil.getString(this, SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, null);
	}

	/**
	 * 设置启动闪屏图的地址
	 */
	public void setStartImg(String startImg) {
		if (TextUtils.isEmpty(startImg)) {
			return;
		}
		mStartImg = startImg;
		SPUtil.putString(AssistantApp.getInstance(), SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, mStartImg);
	}

	/**
	 * 获取活动弹窗
	 */
	public IndexBanner getBroadcastBanner() {
		return mBroadcastBanner;
	}

	/**
	 * 设置活动弹窗内容
	 */
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

	/**
	 * 获取渠道ID
	 */
	public int getChannelId() {
		if (mChannelId == -1) {
			mChannelId = ChannelUtil.getChannelId(this);
		}
		return mChannelId;
	}

	/**
	 * 今日是否已经进行了推送
	 */
	public boolean isPushedToday() {
		if (!mIsPushedToday) {
			long storeTime = SPUtil.getLong(this, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_LAST_PUSH_TIME, 0);
			if (storeTime != 0 && DateUtil.isToday(storeTime)) {
				mIsPushedToday = true;
			}
		}
		return mIsPushedToday;
	}

	/**
	 * 设置今日已经进行了推送
	 */
	public void setPushedToday() {
		mIsPushedToday = true;
		SPUtil.putLong(this, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_LAST_PUSH_TIME, System.currentTimeMillis());
	}

	/**
	 * 往Retrofit网络框架添加网络请求拦截器
	 */
	public void addInterceptorToRetrofit() {
		if (mRetrofit != null) {
			Interceptor interceptor = new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					String header = String.format(ConstString.TEXT_HEADER,
							AppConfig.PACKAGE_NAME, AppConfig.SDK_VER,
							AppConfig.SDK_VER_NAME, getChannelId());
					Request newRequest = chain.request().newBuilder()
							.addHeader("X-Client-Info", header)
							.build();
					return chain.proceed(newRequest);
				}
			};
			List<Interceptor> interceptors = mRetrofit.client().interceptors();
			if (interceptors.size() > 0) {
				interceptors.remove(0);
			}
			interceptors.add(0, interceptor);
		}
	}
}
