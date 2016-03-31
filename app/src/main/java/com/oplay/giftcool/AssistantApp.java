package com.oplay.giftcool;

import android.app.Application;
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
import com.oplay.giftcool.ext.gson.NullStringToEmptyAdapterFactory;
import com.oplay.giftcool.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.InitQQ;
import com.oplay.giftcool.model.data.resp.UpdateInfo;
import com.oplay.giftcool.ui.widget.LoadAndRetryViewManager;
import com.oplay.giftcool.util.ChannelUtil;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.SPUtil;
import com.oplay.giftcool.util.SoundPlayer;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.compatibility.Compatibility_AsyncTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    private String mStartImg;
    private IndexBanner mBroadcastBanner;
    private int mChannelId = -1;

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

    // 以下暂无
    // 是否下载完成自动安装
    private boolean mShouldAutoInstall = false;
    // 是否开启省流模式
    private boolean mIsSaveFlow = false;
    // 是否启用下载完成提示音
    private boolean mIsPlayDownloadComplete = false;

    // 说明今日是否推送过消息
    private boolean mIsPushedToday = false;
    // 是否在任务栏显示每日抽奖入口
    private boolean mHasLottery = true;

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
        sInstance = this;
        // 启动闹钟通知广播进程来唤醒服务
        AlarmClockManager.getInstance().startWakeAlarm(this);
        appInit();
    }


    /**
     * 执行APP的初始化工作
     */
    public void appInit() {
        KLog.init(AppDebugConfig.IS_DEBUG);
        initImageLoader();
        // 初始配置加载列表
        initLoadingView();
        Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
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
     * do work to release the resource when app appExit
     */
    public void appExit() {
        try {
//			AlarmClockManager.getInstance().stopWakeAlarm(this);
            ThreadUtil.destroy();
            setGlobalInit(false);
            PushMessageManager.getInstance().exit(this);
            if (ImageLoader.getInstance().isInited()) {
                ImageLoader.getInstance().clearMemoryCache();
                ImageLoader.getInstance().stop();
//				ImageLoader.getInstance().destroy();
            }
//            DownloadNotificationManager.cancelDownload(getApplicationContext());
//            StatisticsManager.getInstance().exit(this);
        } catch (Exception e) {
            if (AppDebugConfig.IS_DEBUG) {
                KLog.d(AppDebugConfig.TAG_APP, "appExit exception : " + e);
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (AppDebugConfig.IS_DEBUG) {
            AppDebugConfig.logMemoryInfo();
        }
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().clearMemoryCache();
        }
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
        File httpCacheDir = new File(getCacheDir(), Global.NET_CACHE_PATH);
        Cache cacheFile = new Cache(httpCacheDir, 100 * 1024 * 1024);
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 请求时携带版本信息
                String headerValue = String.format(ConstString.TEXT_HEADER,
                        AppConfig.PACKAGE_NAME, AppConfig.SDK_VER,
                        AppConfig.SDK_VER_NAME, getChannelId());
                String headerName = "X-Client-Info";
                Request newRequest;
                newRequest = chain.request().newBuilder()
                        .addHeader(headerName, headerValue)
                        .build();
                Response response = chain.proceed(newRequest);

                CacheControl cacheControl;
                if (NetworkUtil.isConnected(getApplicationContext())) {
                    cacheControl = new CacheControl.Builder()
                            .noCache()
                            .build();
                } else {
                    cacheControl = new CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(365, TimeUnit.DAYS)
                            .build();
                }
                String cacheControlStr = cacheControl.toString();
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", cacheControlStr)
                        .build();
            }
        };

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(AppConfig.NET_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(AppConfig.NET_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .cache(cacheFile)
                .addInterceptor(cacheInterceptor)
                .retryOnConnectionFailure(true)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(NetUrl.getBaseUrl())
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(mGson))
                .build();
    }

    /**
     * 重设网络并重初始化，测试使用
     */
    public void resetInitForTest() {
        if (AppConfig.TEST_MODE) {
            setGlobalInit(false);
            initRetrofit();
            Global.resetNetEngine();
            Compatibility_AsyncTask.executeParallel(new AsyncTask_InitApplication(this));
        }
    }

    /**
     * initial the configuration of Universal-Image-Loader
     */
    public void initImageLoader() {
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().resume();
            return;
        }
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

    public boolean isHasLottery() {
        return mHasLottery;
    }

    public void setHasLottery(boolean hasLottery) {
        mHasLottery = hasLottery;
    }


}
