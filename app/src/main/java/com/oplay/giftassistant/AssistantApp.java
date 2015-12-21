package com.oplay.giftassistant;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.StrictMode;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.constant.Global;
import com.oplay.giftassistant.ext.retrofit2.GsonConverterFactory;
import com.oplay.giftassistant.util.SoundPlayer;
import com.socks.library.KLog;

import java.io.File;

import retrofit.Retrofit;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class AssistantApp extends Application {

    public final static String IMG_PATH = "/gift_assistant/cache/imgs";
    private static AssistantApp sInstance;
    private Retrofit mRetrofit;

    // 是否下载完成自动安装
    private boolean mShouldAutoInstall = false;
    // 是否安装完成自动删除
    private boolean mShouldAutoDeleteApk = false;
    private boolean mIsAutoMsgToast = false;
    // 渠道号
    private int mChannelNo;
    // 是否开启省流模式
    private boolean mIsSaveFlow = false;
    // 是否启用下载完成提示音
    private boolean mIsPlayDownloadComplete = false;
    // 是否已经登录
    private boolean mIsLogin;

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
        initImageLoader();
        KLog.init(true);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Global.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        setLogin(true);
    }

    public static AssistantApp getInstance() {
        return sInstance;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    /**
     * �˴�ִ��App�˳�ǰ��������
     */
    public void exit() {
        ImageLoader.getInstance().clearMemoryCache();
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

    /**
     * ��ʼ������ImageLoader����
     */
    private void initImageLoader() {
        try {
            final DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build();
            final File cacheDir = StorageUtils.getOwnCacheDirectory(this, IMG_PATH);
            final long maxAgeTimeInSeconds = 7 * 24 * 60 * 60;   // 7 days cache
            final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                    .threadPoolSize(5)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new WeakMemoryCache())
                    .diskCache(new LimitedAgeDiskCache(cacheDir, maxAgeTimeInSeconds))
                    .defaultDisplayImageOptions(options) // default
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

    public boolean isShouldAutoInstall() {
        return mShouldAutoInstall;
    }

    public void setShouldAutoInstall(boolean shouldAutoInstall) {
        mShouldAutoInstall = shouldAutoInstall;
    }

    public boolean isShouldAutoDeleteApk() {
        return mShouldAutoDeleteApk;
    }

    public void setShouldAutoDeleteApk(boolean shouldAutoDeleteApk) {
        mShouldAutoDeleteApk = shouldAutoDeleteApk;
    }

    public boolean isAutoMsgToast() {
        return mIsAutoMsgToast;
    }

    public void setIsAutoMsgToast(boolean isAutoMsgToast) {
        mIsAutoMsgToast = isAutoMsgToast;
    }

    public int getChannelNo() {
        return mChannelNo;
    }

    public void setChannelNo(int channelNo) {
        mChannelNo = channelNo;
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

    public boolean isLogin() {
        return mIsLogin;
    }

    public void setLogin(boolean isLogin) {
        mIsLogin = isLogin;
    }
}
