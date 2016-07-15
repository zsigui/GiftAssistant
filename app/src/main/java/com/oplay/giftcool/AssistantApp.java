package com.oplay.giftcool;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
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
import com.oplay.giftcool.assist.CrashHandler;
import com.oplay.giftcool.assist.UILImageLoader;
import com.oplay.giftcool.asynctask.AsyncTask_InitApplication;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.SPConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.ext.gson.NullStringToEmptyAdapterFactory;
import com.oplay.giftcool.ext.retrofit2.encrypt.GsonConverterFactory;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.manager.SocketIOManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftcool.util.ChannelUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SoundPlayer;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.log.GCLog;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.jpush.android.api.JPushInterface;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

    private static AssistantApp sInstance;
    private Retrofit mRetrofit;
    private Gson mGson;

    private UpdateInfo mUpdateInfo;
    private ArrayList<InitQQ> mQQInfo;
    private IndexBanner mBroadcastBanner;
    private int mChannelId = -1;

    private int mSoftInputHeight = 0;

    // 是否安装完成自动删除
    private boolean mShouldAutoDeleteApk = false;
    // 是否自动检查版本更新
    private boolean mShouldAutoCheckUpdate = true;
    // 是否自动关注
    private boolean mShouldAutoFocus = true;
    // 是否推送消息
    private boolean mShouldPushMsg = true;
    // 是否已经完成全局初始化
    private boolean mIsGlobalInit = false;
    // 是否允许显示下载，根据渠道获取而定
    private boolean mIsAllowDownload = true;
    // 是否记住密码
    private boolean mIsRememberPwd = true;
    // 活动页面的游戏资讯是否只查看已关注的
    private boolean mIsReadAttention = false;

    // 以下暂无
    // 是否下载完成自动安装
    private boolean mShouldAutoInstall = false;
    // 是否开启省流模式
    private boolean mIsSaveFlow = false;
    // 是否启用下载完成提示音
    private boolean mIsPlayDownloadComplete = false;

    // 说明今日是否推送过消息
    private boolean mIsPushedToday = false;

    // 是否此版本第一次启动
    private boolean mFirstOpenInThisVersion = false;

    // 头部信息
    private String mHeaderValue;

    private OkHttpClient mHttpClient;
    private long mLastLaunchTime;
    private int mPhoneLoginType;
    private int mPushSdk;

    // LeakCanary 用于检测内存泄露
//	private RefWatcher mRefWatcher;

    public static AssistantApp getInstance() {
        if (sInstance == null) {
            AppDebugConfig.d(AppDebugConfig.TAG_APP, "AssistantApp is init here!");
        }
        return sInstance;
    }

    public Retrofit getRetrofit() {
        if (mRetrofit == null) {
            initRetrofit();
        }
        return mRetrofit;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//		mRefWatcher = LeakCanary.install(this);
        // enabled StrictMode only in TEST
        sInstance = this;
        // 启动闹钟通知广播进程来唤醒服务
        AlarmClockManager.getInstance().startWakeAlarm(this);
        appInit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sInstance = this;
//        HotFixManager.getInstance().test();
    }

    /**
     * 执行APP的初始化工作
     */
    public void appInit() {
        if (mIsGlobalInit) {
            return;
        }
        if (AppDebugConfig.IS_DEBUG) {
            String data = SPUtil.getString(AssistantApp.getInstance().getApplicationContext(),
                    SPConfig.SP_APP_DEVICE_FILE,
                    SPConfig.KEY_TEST_REQUEST_URI,
                    String.format("%s\n%s", NetUrl.TEST_URL_BASE, WebViewUrl.TEST_URL_BASE));
            String[] s = data.split("\n");
            NetUrl.REAL_URL = s[0].trim();
            if (s.length > 1) {
                WebViewUrl.REAL_URL = s[1].trim();
            }
        }
        CrashHandler.getInstance().init();
        GCLog.init(AppDebugConfig.IS_DEBUG);
        initImageLoader();
        // 初始配置加载列表
        initLoadingView();
        // 初始化统计工具
        StatisticsManager.getInstance().init(this, getChannelId());
        Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));

    }

    public void initLoadingView() {
        AppDebugConfig.d(AppDebugConfig.TAG_APP, "初始化LogindView");
        LoadAndRetryViewManager.setDefaultEmptyViewId(R.layout.fragment_data_empty);
        LoadAndRetryViewManager.setDefaultLoadViewId(R.layout.fragment_data_loading);
        // 加载失败，错误或者重试
        LoadAndRetryViewManager.setDefaultErrorRetryViewId(R.layout.fragment_error_net);
    }


    public void initGalleryFinal() {
        if (GalleryFinal.isInit()) {
            return;
        }
        int bgColor = getResources().getColor(R.color.co_common_app_main_bg);
        int textColor = Color.WHITE;
        ThemeConfig theme = new ThemeConfig.Builder()
                .setTitleBarBgColor(bgColor)
                .setTitleBarIconColor(textColor)
                .setIconCheck(R.drawable.selector_check_box)
                .setIconBack(R.drawable.ic_bar_back)
                .setIconDelete(R.drawable.ic_photo_delete)
                .setFabNornalColor(getResources().getColor(R.color.co_btn_red))
                .setFabPressedColor(getResources().getColor(R.color.co_btn_red_pressed))
                .setPreviewBg(getResources().getDrawable(R.color.co_opacity_80))
                .build();
        FunctionConfig config = new FunctionConfig.Builder()
                .setEnableCamera(false)
                .setEnablePreview(false)
                .setEnableRotate(false)
                .setEnableCrop(false)
                .setEnableEdit(false)
                .build();
        UILImageLoader imageLoader = new UILImageLoader();
        File folder = StorageUtils.getOwnCacheDirectory(getApplicationContext(), Global.IMG_CACHE_PATH);
        CoreConfig coreConfig = new CoreConfig.Builder(getApplicationContext(), imageLoader, theme)
                .setFunctionConfig(config)
                .setTakePhotoFolder(folder)
                .setEditPhotoCacheFolder(folder)
                .build();
        GalleryFinal.init(coreConfig);
    }

    /**
     * get a watcher to watch whether exits the problem of memory leak
     */
//	public static RefWatcher getRefWatcher(Context context) {
//		return ((AssistantApp) context.getApplicationContext()).mRefWatcher;
//	}

    /**
     * do work to release the resource when app appExit
     */
    public void appExit() {
        try {
            AlarmClockManager.getInstance().setObserverGame(false);
            ThreadUtil.destroy();
            setGlobalInit(false);
            PushMessageManager.getInstance().exit(this);
            SilentDownloadManager.getInstance().stopAllDownload();
            SocketIOManager.getInstance().close();
            if (ImageLoader.getInstance().isInited()) {
                ImageLoader.getInstance().clearMemoryCache();
                ImageLoader.getInstance().stop();
//				ImageLoader.getInstance().destroy();
            }
            DownloadNotificationManager.cancelDownload(getApplicationContext());
            StatisticsManager.getInstance().exit(this);
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_APP, e);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        AppDebugConfig.logMemoryInfo();
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().clearMemoryCache();
        }
    }

    public synchronized void initGson() {
        if (mGson == null) {
            mGson = new GsonBuilder()
                    .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
                    .serializeNulls()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
        }
    }

    public String getHeaderValue() {
        if (TextUtils.isEmpty(mHeaderValue)) {
            mHeaderValue = String.format(Locale.CHINA, ConstString.TEXT_HEADER,
                    AppConfig.PACKAGE_NAME, AppConfig.SDK_VER,
                    AppConfig.SDK_VER_NAME, getChannelId(), AppConfig.OUWAN_SDK_VER);
        }
        return mHeaderValue;
    }

    public OkHttpClient getHttpClient() {
        synchronized (Object.class) {
            if (mHttpClient == null) {
                File httpCacheDir = new File(getCacheDir(), Global.NET_CACHE_PATH);
                Cache cacheFile = new Cache(httpCacheDir, 100 * 1024 * 1024);
                Interceptor cacheInterceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        // 请求时携带版本信息
                        String headerName = "X-Client-Info";
                        Request newRequest;
                        newRequest = chain.request().newBuilder()
                                .addHeader(headerName, getHeaderValue())
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build();
//                        AppDebugConfig.d(AppDebugConfig.TAG_APP, "net request url = " + newRequest.url().uri().toString());
                        Response response = chain.proceed(newRequest);

                        CacheControl cacheControl;
//						if (NetworkUtil.isConnected(getApplicationContext())) {
                        cacheControl = new CacheControl.Builder()
                                .noCache()
                                .build();
//						} else {
//							cacheControl = new CacheControl.Builder()
//									.onlyIfCached()
//									.maxStale(365, TimeUnit.DAYS)
//									.build();
//						}
                        String cacheControlStr = cacheControl.toString();
                        return response.newBuilder()
                                .removeHeader("Pragma")
                                .header("Cache-Control", cacheControlStr)
                                .build();
//						return response;
                    }
                };

                mHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(AppConfig.NET_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                        .readTimeout(AppConfig.NET_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                        .cache(cacheFile)
                        .addInterceptor(cacheInterceptor)
                        .retryOnConnectionFailure(false)
                        .build();
            }
        }
        return mHttpClient;
    }

    public void initRetrofit() {
        initGson();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NetUrl.getBaseUrl())
                .client(getHttpClient())
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();
    }

//    /**
//     * 重设网络并重初始化，测试使用
//     */
//    public void resetInitForTest() {
//        if (AppConfig.TEST_MODE) {
//            setGlobalInit(false);
//            appInit();
//        }
//    }

    /**
     * initial the configuration of Universal-Image-Loader
     */
    public void initImageLoader() {
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().resume();
            return;
        }
        try {
            final DisplayImageOptions options = Global.getDefaultImgOptions();
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
            AppDebugConfig.d(AppDebugConfig.TAG_APP, "初始化ImageLoader");
        } catch (Throwable e) {
            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
            AppDebugConfig.w(AppDebugConfig.TAG_APP, "ImageLoader.init() in failed : ", e);
        }
    }

    /**
     * 读写SP里的全局APP配置，最好在线程中调用
     */
    public void initAppConfig() {
        mShouldAutoCheckUpdate = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_AUTO_CHECK_UPDATE, true);
        mShouldAutoDeleteApk = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_AUTO_DELETE_APK, true);
        mShouldPushMsg = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_ACCEPT_PUSH, true);
        mShouldAutoInstall = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_AUTO_INSTALL, true);
        mShouldAutoFocus = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_AUTO_FOCUS, true);
        mIsAllowDownload = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_IS_ALLOW_DOWNLOAD, true);
        mIsSaveFlow = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_IS_SAVE_FLOW, false);
        setIsSaveFlow(mIsSaveFlow);
        mIsPlayDownloadComplete = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_IS_PLAY_DOWNLOAD_COMPLETE, true);
        mIsRememberPwd = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_REMEMBER_PWD, true);
        setIsPlayDownloadComplete(mIsPlayDownloadComplete);
        mIsReadAttention = SPUtil.getBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE,
                SPConfig.KEY_IS_READ_ATTENTION, true);
        getSoftInputHeight(null);
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

    /**
     * 是否在抢礼包后自动关注游戏
     */
    public boolean isShouldAutoFocus() {
        return mShouldAutoFocus;
    }

    /**
     * 设置是否在抢礼包之后自动关注所属游戏
     */
    public void setShouldAutoFocus(boolean shouldAutoFocus) {
        mShouldAutoFocus = shouldAutoFocus;
        SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_AUTO_FOCUS,
                shouldAutoFocus);
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
        if (ImageLoader.getInstance().isInited()) {
            if (mIsSaveFlow) {
                ImageLoader.getInstance().clearMemoryCache();
                ImageLoader.getInstance().pause();
                ImageLoader.getInstance().denyNetworkDownloads(true);
            } else {
                ImageLoader.getInstance().denyNetworkDownloads(false);
                ImageLoader.getInstance().resume();
            }
        }
        SPUtil.putBoolean(getApplicationContext(), SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_IS_SAVE_FLOW,
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
        if (mGson == null) {
            initGson();
        }
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
        if (ImageLoader.getInstance().isInited()) {
            // 先进行预加载
            ImageLoader.getInstance().loadImage(startImg, null);
        }
        SPUtil.putString(AssistantApp.getInstance(), SPConfig.SP_CACHE_FILE, SPConfig.KEY_SPLASH_URL, startImg);
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
        if (broadcastBanner == null) {
            return;
        }
        mBroadcastBanner = broadcastBanner;
        if (!TextUtils.isEmpty(broadcastBanner.url)) {
            AssistantApp.getInstance().initImageLoader();
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

    public void setIsReadAttention(boolean isReadAttention) {
        mIsReadAttention = isReadAttention;
        SPUtil.putBoolean(this, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_IS_READ_ATTENTION, mIsReadAttention);
    }

    public boolean isReadAttention() {
        return mIsReadAttention;
    }

    public int getSoftInputHeight(Activity activity) {
        if (mSoftInputHeight == 0) {
            mSoftInputHeight = SPUtil.getInt(this, SPConfig.SP_APP_DEVICE_FILE, SPConfig.KEY_SOFT_INPUT_HEIGHT, 0);
            if (activity != null && mSoftInputHeight == 0) {
                mSoftInputHeight = InputMethodUtil.getSoftInputHeight(activity);
                setSoftInputHeight(mSoftInputHeight);
            }
        }
        return mSoftInputHeight;
    }

    public void setSoftInputHeight(int softInputHeight) {
        if (softInputHeight != 0) {
            mSoftInputHeight = softInputHeight;
            SPUtil.putInt(this, SPConfig.SP_APP_DEVICE_FILE, SPConfig.KEY_SOFT_INPUT_HEIGHT, softInputHeight);
        }
    }

    public long getLastLaunchTime() {
        if (mLastLaunchTime == 0) {
            mLastLaunchTime = SPUtil.getLong(this, SPConfig.SP_APP_CONFIG_FILE, SPConfig.KEY_LAST_OPEN_APP_TIME, 1);
            SPUtil.putLong(this, SPConfig.SP_APP_CONFIG_FILE,
                    SPConfig.KEY_LAST_OPEN_APP_TIME, System.currentTimeMillis());
        }
        return mLastLaunchTime;
    }

    public boolean isFirstOpenInThisVersion() {
        return mFirstOpenInThisVersion;
    }

    public void setFirstOpenInThisVersion(boolean firstOpenInThisVersion) {
        mFirstOpenInThisVersion = firstOpenInThisVersion;
    }

    public void setPhoneLoginType(int phoneLoginType) {
        mPhoneLoginType = phoneLoginType;
    }

    /**
     * 获取手机登录的展示UI样式，0 旧版 1 新版
     */
    public int getPhoneLoginType() {
        return mPhoneLoginType;
    }

    public void setPushSdk(int pushSdk) {
        mPushSdk = pushSdk;
    }

    public int getPushSdk() {
        return mPushSdk;
    }
}
