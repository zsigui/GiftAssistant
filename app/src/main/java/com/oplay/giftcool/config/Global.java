package com.oplay.giftcool.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.SparseIntArray;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.engine.NetEngine;
import com.oplay.giftcool.model.data.resp.message.CentralHintMessage;
import com.oplay.giftcool.model.data.resp.message.MessageCentralUnread;
import com.oplay.giftcool.util.SPUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.global.Global_Executor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executor;

/**
 * Created by zsigui on 15-12-16.
 */
public class Global {

    /**
     * 内部渠道文件存储位置
     */
    public final static String INTERNAL_INFO_FILE = "gift_cool_info";
    /**
     * 外部缓存存储位置
     */
    public final static String EXTERNAL_CACHE = "/gift_cool/cache";
    /**
     * 外部下载文件及对应下载文件缓存存放位置
     */
    public final static String EXTERNAL_DOWNLOAD = "/gift_cool/download";
    /**
     * 外部图片缓存存储位置
     */
    public final static String IMG_CACHE_PATH = EXTERNAL_CACHE + "/imgs";
    /**
     * 外部网络请求缓存存储位置
     */
    public final static String NET_CACHE_PATH = EXTERNAL_CACHE + "/net";
    /**
     * 外部LOG信息存储位置
     */
    public final static String LOGGING_CACHE_PATH = EXTERNAL_CACHE + "/log";
    /**
     * 礼包酷渠道名文件后缀
     */
    public final static String CHANNEL_FILE_NAME_SUFFIX = ".gift_cool";
    /**
     * 下载缓存文件的后缀
     */
    public final static String TEMP_FILE_NAME_SUFFIX = ".vmtf";
    /**
     * 下载的Apk文件的后缀
     */
    public final static String APK_FILE_NAME_SUFFIX = ".apk";
    /**
     * 礼包酷所属游戏ID，用于下载链接统计
     */
    public final static int GIFTCOOL_GAME_ID = 2000705;
    /**
     * 倒计时时间间隔，单位:ms
     */
    public final static int COUNTDOWN_INTERVAL = 1000;
    /**
     * 重复点击的时间间隔，单位:ms
     */
    public final static int CLICK_TIME_INTERVAL = 500;
    /**
     * 回复最大图片数
     */
    public final static int REPLY_IMG_COUNT = 6;

    /**
     * 全局服务器与本地手机时间差，单位:ms
     */
    public static long sServerTimeDiffLocal;

    /**
     * Retrofit网络请求接口引擎
     */
    private static NetEngine sNetEngine;

    /**
     * ImageLoader默认图片加载配置
     */
    private static DisplayImageOptions sDefaultImgOptions = null;
    /**
     * 头像的ImageLoader加载配置
     */
    private static DisplayImageOptions sAvatarImgOptions = null;
    /**
     * 轮播图的ImageLoader加载配置
     */
    private static DisplayImageOptions sBannerImgOptions = null;

    /**
     * 照片墙的ImageLoader加载配置
     */
    private static DisplayImageOptions sGalleryImgOptions = null;

    /**
     * 指示本轮活动页面抽奖红点是否已经展示过，防止刷新重新显示
     */
    public static boolean sHasShowedLotteryHint = false;
    /**
     * 指示本轮活动页面签到红点是否已经展示过，防止刷新重新显示
     */
    public static boolean sHasShowedSignInHint = false;

    private static SparseIntArray sLikeNewTime;

    public static SparseIntArray getLikeNewTimeArray() {
        if (sLikeNewTime == null) {
            sLikeNewTime = new SparseIntArray(10);
        }
        return sLikeNewTime;
    }

    /**
     * 获取网络请求引擎
     */
    public static NetEngine getNetEngine() {
        if (sNetEngine == null) {
            sNetEngine = AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
        }
        return sNetEngine;
    }

    /**
     * 重置网络请求引擎，测试的使用重新初始化会调用
     */
    public static void resetNetEngine() {
        if (sNetEngine == null || AppConfig.TEST_MODE) {
            sNetEngine = AssistantApp.getInstance().getRetrofit().create(NetEngine.class);
        }
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
     * 依着已安装应用列表,最好在线程中执行
     *
     * @param appName
     */
    public static void setInstalledAppNames(HashSet<String> appName) {
        if (appName == null || appName.isEmpty()) {
            return;
        }
        sAppName = appName;
        final String s = AssistantApp.getInstance().getGson().toJson(sAppName);
        SPUtil.putString(AssistantApp.getInstance().getApplicationContext(),
                SPConfig.SP_APP_INFO_FILE,
                SPConfig.KEY_INSTALL_APP_NAMES, s);
    }

    /**
     * 获取已经安装的应用的Hash列表,需要判空
     */
    public static HashSet<String> getInstalledAppNames() {
        if (sAppName == null || sAppName.isEmpty()) {
            try {
                final String s = SPUtil.getString(AssistantApp.getInstance().getApplicationContext(),
                        SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_INSTALL_APP_NAMES, null);
                if (!TextUtils.isEmpty(s)) {
                    sAppName = AssistantApp.getInstance().getGson().fromJson(s, new TypeToken<HashSet<String>>() {
                    }.getType());
                }
            } catch (Throwable t) {
                if (AppDebugConfig.IS_DEBUG) {
                    KLog.d(AppDebugConfig.TAG_DEBUG_INFO, t);
                }
            }

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

    /**
     * @return 返回默认设置的图片加载器配置
     */
    public static DisplayImageOptions getDefaultImgOptions() {
        if (sDefaultImgOptions == null) {
            sDefaultImgOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_img_default)
                    .showImageOnFail(R.drawable.ic_img_default)
                    .showImageOnLoading(R.drawable.ic_img_default)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
        return sDefaultImgOptions;
    }

    /**
     * @return 返回加载头像信息的图片加载器配置
     */
    public static DisplayImageOptions getAvatarImgOptions() {
        if (sAvatarImgOptions == null) {
            sAvatarImgOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_avatar_default)
                    .showImageOnFail(R.drawable.ic_avatar_default)
                    .showImageOnLoading(R.drawable.ic_avatar_default)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
        return sAvatarImgOptions;
    }

    /**
     * @return 返回加载轮播图的图片加载器配置
     */
    public static DisplayImageOptions getBannerImgOptions() {
        if (sBannerImgOptions == null) {
            sBannerImgOptions = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.drawable.ic_banner_default)
                    .showImageOnFail(R.drawable.ic_banner_default)
                    .showImageOnLoading(R.drawable.ic_banner_default)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
        return sBannerImgOptions;
    }

    /**
     * @return 返回图片墙展示图片时的图片加载器配置
     */
    public static DisplayImageOptions getGalleryImgOptions() {
        if (sGalleryImgOptions == null) {
            sGalleryImgOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(false)
                    .cacheInMemory(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageOnLoading(R.drawable.ic_img_default)
                    .showImageOnFail(R.drawable.ic_img_default)
                    .showImageForEmptyUri(R.drawable.ic_img_default)
                    .build();
        }
        return sGalleryImgOptions;
    }

    /**
     * 消息中心的消息列表数据
     */
    private static ArrayList<CentralHintMessage> mMsgCentralData;
    /**
     * 状态标识消息中心页面需要刷新消息列表
     */
    public static boolean mMsgCentralTobeRefresh = false;

    private static void writeMsgDataToSP(Context context, ArrayList<CentralHintMessage> data) {
        String s = AssistantApp.getInstance().getGson().toJson(data);
        SPUtil.putString(context, SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_MSG_CENTRAL_LIST, s);
    }

    /**
     * 更新消息中心的消息列表项中指定代号项的数据
     */
    public static void updateMsgCentralData(Context context, String code, int count, String msg) {
        final ArrayList<CentralHintMessage> data = getMsgCentralData(context);
        for (CentralHintMessage item : data) {
            if (item.code.equalsIgnoreCase(code)) {
                item.count = count;
                if (!TextUtils.isEmpty(msg)) {
                    item.content = msg;
                }
                mMsgCentralTobeRefresh = true;
            }
        }
        writeMsgDataToSP(context, data);
    }

    public static void updateMsgCentralData(Context context, MessageCentralUnread unread) {
        final ArrayList<CentralHintMessage> data = getMsgCentralData(context);
        final String s = context.getResources().getString(R.string.st_msg_central_hint_no_notify);
        for (CentralHintMessage item : data) {
            if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_NEW_GIFT_NOTIFY)) {
                item.count = unread.unreadNewGiftCount;
                item.content = !TextUtils.isEmpty(unread.newestGift) ?
                        unread.newestGift : s;
                mMsgCentralTobeRefresh = true;
            } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_COMMENT)) {
                item.count = unread.unreadCommentCount;
                item.content = !TextUtils.isEmpty(unread.newestComment) ?
                        unread.newestComment : s;
                mMsgCentralTobeRefresh = true;
            } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_SYSTEM)) {
                item.count = unread.unreadSystemCount;
                item.content = !TextUtils.isEmpty(unread.newestSystem) ?
                        unread.newestSystem : s;
                mMsgCentralTobeRefresh = true;
            } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_ADMIRE)) {
                item.count = unread.unreadAdmireCount;
                item.content = !TextUtils.isEmpty(unread.newestAdmire) ?
                        unread.newestAdmire :
                        (String.format(context.getResources().getString(R.string.st_msg_central_get_a_admire), s));
                mMsgCentralTobeRefresh = true;
            }
        }
        writeMsgDataToSP(context, data);
    }

    /**
     * 获取消息中心消息列表数据
     */
    public static ArrayList<CentralHintMessage> getMsgCentralData(Context context) {
        if (mMsgCentralData == null) {
            String data = SPUtil.getString(context, SPConfig.SP_APP_INFO_FILE, SPConfig.KEY_MSG_CENTRAL_LIST, null);
            if (!TextUtils.isEmpty(data)) {
                mMsgCentralData = AssistantApp.getInstance().getGson().fromJson(data,
                        new TypeToken<ArrayList<CentralHintMessage>>() {
                        }.getType());
            }
            if (mMsgCentralData == null) {
                mMsgCentralData = new ArrayList<>();
                mMsgCentralData.add(new CentralHintMessage(KeyConfig.CODE_MSG_NEW_GIFT_NOTIFY,
                        R.drawable.ic_msg_new_gift_notify,
                        context.getResources().getString(R.string.st_msg_central_new_gift_notify),
                        context.getResources().getString(R.string.st_msg_central_hint_no_notify),
                        0));
                mMsgCentralData.add(new CentralHintMessage(KeyConfig.CODE_MSG_ADMIRE,
                        R.drawable.ic_msg_admire,
                        context.getResources().getString(R.string.st_msg_central_admire),
                        context.getResources().getString(R.string.st_msg_central_hint_no_notify),
                        0));
                mMsgCentralData.add(new CentralHintMessage(KeyConfig.CODE_MSG_COMMENT,
                        R.drawable.ic_msg_comment,
                        context.getResources().getString(R.string.st_msg_central_comment),
                        context.getResources().getString(R.string.st_msg_central_hint_no_reply),
                        0));
                mMsgCentralData.add(new CentralHintMessage(KeyConfig.CODE_MSG_SYSTEM,
                        R.drawable.ic_msg_system,
                        context.getResources().getString(R.string.st_msg_central_system),
                        context.getResources().getString(R.string.st_msg_central_hint_no_notify),
                        0));
            } else {
                for (CentralHintMessage item : mMsgCentralData) {
                    if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_NEW_GIFT_NOTIFY)) {
                        item.icon = R.drawable.ic_msg_new_gift_notify;
                    } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_COMMENT)) {
                        item.icon = R.drawable.ic_msg_comment;
                    } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_SYSTEM)) {
                        item.icon = R.drawable.ic_msg_system;
                    } else if (item.code.equalsIgnoreCase(KeyConfig.CODE_MSG_ADMIRE)) {
                        item.icon = R.drawable.ic_msg_admire;
                    }
                }
            }
        }
        return mMsgCentralData;
    }
}
