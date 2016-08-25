package com.oplay.giftcool.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.BuildConfig;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.tendcloud.tenddata.TCAgent;
import com.umeng.analytics.MobclickAgent;

import java.util.Locale;
import java.util.Map;

/**
 * 统计工具管理器
 * <p/>
 * Created by zsigui on 16-3-11.
 */
public class StatisticsManager {

    /*
         说明: ID组成 2字母 + 6位数字 (2字母代表类型缩写)
         gm = game 游戏事件  ;  gf = gift 礼包事件  ; us = 用户事件  ; ap = 应用事件
         */
    public interface ID {
        // 礼包首页轮播图
        String GIFT_BANNER = "gift_banner";
        String STR_GIFT_BANNER = "礼包首页轮播图";
        // 抢礼包点击事件
        String GIFT_SEIZE_CLICK = "gift_seize_click";
        String STR_GIFT_SEIZE_CLICK = "抢礼包点击";
        String STR_GIFT_SEIZE_NO_PAY = "尚未支付";
        // 抢礼包-偶玩豆支付
        String GIFT_BEAN_SEIZE = "seize_gift_pay_by_bean";
        String STR_GIFT_BEAN_SEIZE = "抢礼包-偶玩豆支付";
        // 抢礼包-金币支付
        String GIFT_GOLD_SEIZE = "seize_gift_pay_by_gold";
        String STR_GIFT_GOLD_SEIZE = "抢礼包-金币支付";
        // 首页0元礼包项点击事件
        String GIFT_ZERO_ITEM = "index_gift_zero_click";
        String STR_GIFT_ZERO_ITEM = "首页0元礼包项点击";
        // 首页限量礼包项点击事件
        String GIFT_LIMIT_ITEM = "index_gift_limit_click";
        String STR_GIFT_LIMIT_ITEM = "首页限量礼包项点击";
        // 游戏首页轮播图
        String GAME_BANNER = "game_banner";
        String STR_GAME_BANNER = "游戏首页轮播图";
        // 搜索
        String USER_SEARCH = "search";
        String STR_USER_SEARCH = "搜索";
        // 求礼包-提交
        String STR_USER_HOPE_GIFT = "求礼包";
        String USER_HOPE_GIFT_SUCCESS = "request_gift_success";
        String STR_USER_HOPE_GIFT_SUCCESS = "求礼包-提交";
        // 求礼包-取消
        String USER_HOPE_GIFT_QUICK = "request_gift_quick";
        String STR_USER_HOPE_GIFT_QUICK = "求礼包-取消";
        // 手机登录
        String USER_PHONE_LOGIN = "user_phone_login";
        String STR_USER_PHONE_LOGIN = "使用手机登录";
        // 偶玩账号登录
        String USER_OUWAN_LOGIN = "user_ouwan_login";
        String STR_USER_OUWAN_LOGIN = "偶玩账号登录";
        // 每日自动登录
        String USER_LOGIN_WITH_SESSION = "auto_login_with_session";
        String STR_USER_LOGIN_WITH_SESSION = "每日自动登录";
        // 新礼包消息被点击
        String NEW_GIFT_MESSAGE_CLICK = "new_gift_notify_message_click";
        String STR_NEW_GIFT_MESSAGE_CLICK = "新礼包消息被点击";
        // 更新弹窗
        String UPGRADE = "upgrade";
        String STR_UPGRADE = "更新应用";
        // 活动弹窗
        String CLICK_FIRST_LOGIN_DIALOG = "first_login_dialog_click";
        String STR_CLICK_FIRST_LOGIN_DIALOG = "点击首次登录弹窗";
        // 推送消息已接收
        String PUSH_MESSAGE_RECEIVED = "push_message_received";
        String STR_PUSH_MESSAGE_RECEIVED = "推送消息已接收";
        // 推送消息已打开
        String PUSH_MESSAGE_OPENED = "push_message_opened";
        String STR_PUSH_MESSAGE_OPENED = "推送消息已打开";
        // 活动页面的每日签到点击
        String SIGN_IN_FROM_ACTIVITY = "jump_sign_in_page_from_activity";
        String STR_SIGN_IN_FROM_ACTIVITY = "活动页面的每日签到点击";
        // 活动页面的每日抽奖点击
        String LOTTERY_FROM_ACTIVITY = "jump_lottery_page_from_activity";
        String STR_LOTTERY_FROM_ACTIVITY = "活动页面的每日抽奖点击";
        // 活动页面的每日任务点击
        String TASK_FROM_ACTIVITY = "jump_task_page_from_activity";
        String STR_TASK_FROM_ACTIVITY = "活动页面的每日任务点击";
        // 活动页面的开测开服
        String SERVER_INFO_FROM_ACTIVITY = "jump_server_info_from_activity";
        String STR_SERVER_INFO_FROM_ACTIVITY = "活动页面的开测开服点击";
    }

    private final String TC_APP_KEY = BuildConfig.TD_APPKEY;
    private final String UMENG_APP_KEY = BuildConfig.UMENG_APPKEY;

    private static StatisticsManager sInstance;
    private boolean mIsInit = false;

    public static StatisticsManager getInstance() {
        if (sInstance == null) {
            sInstance = new StatisticsManager();
        }
        return sInstance;
    }

    private StatisticsManager() {
    }

    /**
     * 进行统计工具初始化
     *
     * @param context   上下文
     * @param channelId 渠道Id
     */
    public void init(Context context, int channelId) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statics init = " + mIsInit);
            initUmeng(context, channelId);
            initTCAgent(context, channelId);
            initStrictMode(context);
            mIsInit = true;
        }
    }

    private void initStrictMode(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyDeathOnNetwork()
                        .penaltyLog()
                        .build());
                AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "StrictMode Data init");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initUmeng(Context context, int channelId) {
        try {
            // 友盟
            MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(
                    context, UMENG_APP_KEY, String.format(Locale.CHINA, "m%d", channelId)));
            boolean debugMode = false;
            if (AppConfig.TEST_MODE) {
                debugMode = true;
            } else if (AssistantApp.getInstance().getChannelId() == AppDebugConfig.TEST_CHANNEL_ID) {
                debugMode = true;
            }
            MobclickAgent.setDebugMode(debugMode);
            MobclickAgent.openActivityDurationTrack(false);     //禁止默认的页面统计
            MobclickAgent.setCatchUncaughtExceptions(true);

            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "Umeng Data init");
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statistics sdk is initialed");
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, getDeviceInfo(context));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void initTCAgent(Context context, int channelId) {
        try {
            // TalkingData
            TCAgent.LOG_ON = true;
            TCAgent.init(context, TC_APP_KEY, String.valueOf(channelId));
            TCAgent.setReportUncaughtExceptions(false);
            TCAgent.setAdditionalVersionNameAndCode(AppConfig.SDK_VER_NAME, AppConfig.SDK_VER);
            TCAgent.setPushDisabled();
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "Talking Data init");
        } catch (Throwable t) {
            AppDebugConfig.w(AppDebugConfig.TAG_STATICS, t);
            mIsInit = false;
        }
    }

    public void onResume(Activity context) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statistics onResume");
            MobclickAgent.onResume(context);
            TCAgent.onResume(context);
        }
    }

    public void onPause(Activity context) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statistics onPause");
            MobclickAgent.onPause(context);
            TCAgent.onPause(context);
        }
    }

    public void onPageStart(Context context, String name) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            if (!TextUtils.isEmpty(name)) {
                AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statistics onPageStart");
                MobclickAgent.onPageStart(name);
                TCAgent.onPageStart(context, name);
            }
        }
    }

    public void onPageEnd(Context context, String name) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            if (!TextUtils.isEmpty(name)) {
                AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "statistics onPageEnd");
                MobclickAgent.onPageEnd(name);
                TCAgent.onPageEnd(context, name);
            }
        }
    }

    /**
     * 统计登录
     *
     * @param uid 用户ID
     */
    public void login(String uid) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            MobclickAgent.onProfileSignIn(uid);
        }
    }

    /**
     * 统计登录
     *
     * @param provider 登录来源
     * @param uid      用户ID
     */
    public void login(String provider, String uid) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            MobclickAgent.onProfileSignIn(provider, uid);
        }
    }

    /**
     * 统计退出
     */
    public void logout() {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            MobclickAgent.onProfileSignOff();
        }
    }

    public void trace(final Context context, final String id, final String title) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", isInit = " + mIsInit);
            MobclickAgent.onEvent(context, id);
            TCAgent.onEvent(context, title);
        }
    }

    public void reportThrowable(Throwable t) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            MobclickAgent.reportError(AssistantApp.getInstance(), t);
            TCAgent.onError(AssistantApp.getInstance(), t);
        }
    }

    public void trace(final Context context, final String id, final String title, final String subtitle) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", subtitle = " + subtitle + ", isInit = " +
                    mIsInit);
            MobclickAgent.onEvent(context, id, subtitle);
            TCAgent.onEvent(context, title, subtitle);
        }
    }

    public void trace(final Context context, final String id, final String title,
                      final Map<String, String> keyMap, final int val) {
        trace(context, id, title, "", keyMap, val);
    }

    public void trace(final Context context, final String id, final String title, final String subTitle,
                      final Map<String, String> keyMap, final int val) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            AppDebugConfig.d(AppDebugConfig.TAG_STATICS, "id = " + id + ", keyMap = " + keyMap + ", val = " + val +
                    ", " + "isInit = " + mIsInit);
            if (!TextUtils.isEmpty(subTitle)) {
                keyMap.put("SubTitle", subTitle);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                keyMap.put("__ct__", String.valueOf(val));
                MobclickAgent.onEvent(context, id, keyMap);
            } else {
                MobclickAgent.onEventValue(context, id, keyMap, val);
            }
            TCAgent.onEvent(context, title, subTitle, keyMap);
        }
    }

    public void exit(Context context) {
        if (AppDebugConfig.IS_STATISTICS_SHOW) {
            MobclickAgent.onKillProcess(context);
        }
    }


    @SuppressLint("NewApi")

    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;

        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        } else {
            PackageManager pm = context.getPackageManager();

            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }

        return result;
    }

    /**
     * 友盟测试设备数据采集
     *
     * @param context
     * @return
     */
    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService
                    (Context.TELEPHONY_SERVICE);

            String device_id = null;

            if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context
                    .WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }


            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
