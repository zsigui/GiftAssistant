package com.oplay.giftcool.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.BuildConfig;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.util.GameTypeUtil;
import com.oplay.giftcool.listener.impl.JPushTagsAliasCallback;
import com.oplay.giftcool.model.data.resp.message.PushMessageApp;
import com.oplay.giftcool.model.data.resp.message.PushMessageDetail;
import com.oplay.giftcool.model.data.resp.message.PushMessageExtra;
import com.oplay.giftcool.model.data.resp.task.TaskInfoOne;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.MixUtil;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import net.youmi.android.libs.common.coder.Coder_Md5;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;

/**
 * 推送消息管理器
 * <p/>
 * Created by zsigui on 16-3-8.
 */
public class PushMessageManager {

    /**
     * 推送消息的类型
     */
    public static abstract class Status {

        // 0元礼包推送
        public static final int ACTION_GIFT_ZERO = 1;
        // 每日限量礼包推送
        public static final int ACTION_GIFT_LIMIT = 2;
        // 普通礼包
        public static final int ACTION_GIFT_NEW = 3;
        // 长久未打开应用提示推送，该推送为静默推送
        public static final int ACTION_LONG_UNOPEN = 4;
        // 启动应用推送
        public static final int ACTION_WAKE = 5;
        // 跳转礼包详情页面
        public static final int ACTION_GIFT_DETAIL = 6;
        // 跳转游戏详情页面
        public static final int ACTION_GAME_DETAIL = 7;
        // 采取任务处理的方式进行处理
        public static final int ACTION_LIKE_AS_TASK = 11011;
    }

    /**
     * 设定通知的类型
     */
    public static abstract class NotifyType {
        /**
         * 默认状态，默认通知
         */
        public static final int DEFAULT = 0;
        /**
         * 通知提醒
         */
        public static final int NOTIFY = 1;
        /**
         * 静默，且不统计
         */
        public static final int SLIENT = 2;
        /**
         * 静默，但需要统计
         */
        public static final int SLIENT_BUT_STATICS = 3;
    }

    /**
     * 设定通知优先级
     */
    public static abstract class NotifyPriority {

        /**
         * 低优先级
         */
        public static final int LOW = -1;
        /**
         * 正常通知方式
         */
        public static final int NORMAL = 0;
        /**
         * 着重提醒方式
         */
        public static final int HIGH = 1;
    }


    public interface SdkType {
        byte ALL = 0;
        byte MI = 0x01;
        byte JPUSH = 0x02;

        // 用于状态清除
        byte MI_F = 0x7F;
        byte JPUSH_F = 0x7D;
    }

    /**
     * 部分自定义推送Action
     */
    public interface Action {
        String M_RECEIVED = AppConfig.PACKAGE_NAME + ".mipush.RECEIVE_ARRIVED";
        String M_OPENED = AppConfig.PACKAGE_NAME + ".mipush.RECEIVE_MESSAGE";
    }

    // 本地JPUSH推送的Id
    public static int LOCAL_PUSH_ID = 10012;
    private static final int BROADCAST_REQUEST = 1;
    private static PushMessageManager sInstance;
    private WeakReference<Bitmap> mBitmapWeakReference;
    private NotificationManager mNotificationManager;
    private boolean isInit = false;

    // 无震动响铃的起始接收时间点
    private final int START_HOUR = 22;
    private final int START_MINUTE = 30;
    private final int END_HOUR = 8;
    private final int END_MINUTE = 30;

    public static PushMessageManager getInstance() {
        if (sInstance == null) {
            sInstance = new PushMessageManager();
        }
        return sInstance;
    }

    private PushMessageManager() {
    }

    public NotificationManager getNotificationManager(Context context) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * 初始化推送设置
     */
    public void initPush(Context context) {
        if (MixUtil.isInMainProcess(context)) {

            int pushSdk = AssistantApp.getInstance().getPushSdk();
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "推送服务开始初始化，SDK = " + pushSdk);
            switch (pushSdk) {
                case SdkType.ALL:
                    initJPush(context);
                    initMPush(context);
                    break;
                case SdkType.MI:
                    initMPush(context);
                    break;
                case SdkType.JPUSH:
                default:
                    initJPush(context);
            }
            isInit = true;
            updateJPushTagAndAlias(context);
        }
    }

    /**
     * 初始化小米推送
     */
    private void initMPush(Context context) {
        // 小米推送
        AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "初始化小米推送");
        MiPushClient.registerPush(context, BuildConfig.XM_APPID, BuildConfig.XM_APPKEY);
        Logger.setLogger(context, new LoggerInterface() {
            @Override
            public void setTag(String s) {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "小米 setTag : " + s);
            }

            @Override
            public void log(String s) {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "小米 log : " + s);
            }

            @Override
            public void log(String s, Throwable throwable) {
                AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "小米 log : " + s + ", throwable = " + throwable);
            }
        });
        MiPushClient.setAcceptTime(context, END_HOUR, END_MINUTE, START_HOUR, START_MINUTE, null);
    }

    /**
     * 初始化极光推送
     */
    private void initJPush(Context context) {
        if (!JPushInterface.isPushStopped(context)) {
            // 极光推送
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "极光推送开始初始化");
            JPushInterface.init(context);
            JPushInterface.setDebugMode(AppConfig.TEST_MODE);
            // 设置通知静默时间，不震动和响铃，晚上10点30分-早上8点
            JPushInterface.setSilenceTime(context, START_HOUR, START_MINUTE, END_HOUR, END_MINUTE);
            // 设置默认的通知栏样式
            CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(
                    context, R.layout.view_status_bar_notify, R.id.iv_icon, R.id.tv_title, R.id.tv_content);
            builder.statusBarDrawable = R.mipmap.ic_launcher;
            builder.notificationFlags = PendingIntent.FLAG_ONE_SHOT;
            builder.layoutIconDrawable = R.mipmap.ic_launcher;
            JPushInterface.setDefaultPushNotificationBuilder(builder);
            // 设置保留最近通知条数 5
            JPushInterface.setLatestNotificationNumber(context, 5);

        }
    }

    /**
     * 处理多日未打开应用的唤醒行为
     */
    private void handleLongUnOpenAction(Context context, PushMessageExtra message, Intent intent, boolean isMiPush) {

        long lastOpenTime =
                AssistantApp.getInstance().getLastLaunchTime();
        long curTime = System.currentTimeMillis();

        Gson gson = AssistantApp.getInstance().getGson();
        int diffDay = 3;
        boolean isForcePush = false;
        if (!TextUtils.isEmpty(message.extraJson)) {
            PushMessageApp data = gson.fromJson(message.extraJson, PushMessageApp.class);
            diffDay = data.day;
            isForcePush = data.isForcePush;
        }

        if (!isForcePush && (AssistantApp.getInstance().isPushedToday()
                || curTime - lastOpenTime < 1000 * 60 * 60 * 24 * diffDay)) {
            // 今天已经推送过其他
            return;
        }

        // 进行本地推送通知
        buildNotification(context, message, intent, isMiPush);
        // 构建通知
        message.type = Status.ACTION_WAKE;

        Notification n = buildNotification(context, message, intent, isMiPush);
        getNotificationManager(context).notify(LOCAL_PUSH_ID++, n);
        AssistantApp.getInstance().setPushedToday();
    }

    /**
     * 处理礼包/游戏详情页面
     */
    private void handleDetailAction(Context context, PushMessageExtra message, boolean isGift) {
        if (TextUtils.isEmpty(message.extraJson)) {
            // 额外数据部分不能为空
            return;
        }
        Gson gson = AssistantApp.getInstance().getGson();
        PushMessageDetail data = gson.fromJson(message.extraJson, PushMessageDetail.class);
        if (data.status == 0) {
            data.status = GameTypeUtil.JUMP_STATUS_DETAIL;
        }
        if (isGift) {
            IntentUtil.jumpGiftDetail(context, data.id, true);
        } else {
            IntentUtil.jumpGameDetail(context, data.id, data.status);
        }
    }

    /**
     * 处理广播收到的通知栏消息
     */
    public void handleNotifyMessage(Context context, PushMessageExtra data) {

        switch (data.type) {
            case Status.ACTION_GIFT_ZERO:
                IntentUtil.jumpHome(context, true);
                if (MainActivity.sGlobalHolder != null) {
                    MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_ZERO);
                }
                break;
            case Status.ACTION_GIFT_LIMIT:
                IntentUtil.jumpGiftLimitList(context, true);
                break;
            case Status.ACTION_GIFT_NEW:
                IntentUtil.jumpHome(context, true);
                if (MainActivity.sGlobalHolder != null) {
                    MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
                }
                break;
            case Status.ACTION_WAKE:
                IntentUtil.jumpHome(context, true);
                break;
            case Status.ACTION_GIFT_DETAIL:
                handleDetailAction(context, data, true);
                break;
            case Status.ACTION_GAME_DETAIL:
                handleDetailAction(context, data, false);
                break;
            case Status.ACTION_LIKE_AS_TASK:
                handleTaskAction(context, data);
                break;
        }
    }

    private void handleTaskAction(Context context, PushMessageExtra data) {
        TaskInfoOne infoOne = AssistantApp.getInstance().getGson().fromJson(
                data.extraJson, TaskInfoOne.class);
        IntentUtil.handleJumpInfo(context, infoOne);
    }


    /**
     * 处理极光推送的自定义消息
     */
    public void handleCustomMessage(Context context, PushMessageExtra msg, Intent intent, boolean isMiPush) {
        if (isFilter(msg)) {
            //
            return;
        }
        switch (msg.notifyType) {
            case NotifyType.SLIENT:
                // 直接处理静默推送
//                handleSilentMessage(context, msg, intent, isMiPush);
                break;
            case NotifyType.SLIENT_BUT_STATICS:
                // 复制意图消息，广播已经接收到了通知推送
//                broadcastReceived(context, intent, isMiPush);
                break;
            case NotifyType.NOTIFY:
            case NotifyType.DEFAULT:
                // 复制意图消息，广播已经接收到了通知推送
//                broadcastReceived(context, intent, isMiPush);
                // 创建显示在状态栏的通知消息
                Notification notification = buildNotification(context, msg, intent, isMiPush);
                getNotificationManager(context).notify(PushMessageManager.LOCAL_PUSH_ID, notification);
        }
    }

    /**
     * 当前应用所在渠道是否过滤对该信息的处理
     */
    private boolean isFilter(PushMessageExtra msg) {
        if (!TextUtils.isEmpty(msg.chnIdList)) {
            String curChnId = String.valueOf(AssistantApp.getInstance().getChannelId());
            for (String chnId : msg.chnIdList.split(",")) {
                if (chnId.length() > 0 && curChnId.equals(chnId.trim())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 处理静默消息
     */
    private void handleSilentMessage(Context context, PushMessageExtra data, Intent intent, boolean isMiPush) {
        switch (data.type) {
            case Status.ACTION_LONG_UNOPEN:
                data.type = Status.ACTION_WAKE;
                Bundle bundle = intent.getExtras();
                bundle.putString(JPushInterface.EXTRA_EXTRA,
                        AssistantApp.getInstance().getGson().toJson(data, PushMessageExtra.class));
                intent.putExtras(bundle);
                handleLongUnOpenAction(context, data, intent, isMiPush);
                break;
        }
    }

    /**
     * 通知广播消息已经接收到
     *
     * @param context 上下文
     * @param intent  接收到的消息意图
     */
    private void broadcastReceived(Context context, Intent intent, boolean isMiPush) {
        Intent broadcastIntent = (Intent) intent.clone();
        if (isMiPush) {
            broadcastIntent.setAction(Action.M_RECEIVED);
            broadcastIntent.putExtra("isNotified", false);
        } else {
            broadcastIntent.setAction(JPushInterface.ACTION_NOTIFICATION_RECEIVED);
        }
        context.sendBroadcast(broadcastIntent);
    }

    /**
     * 创建默认的通知消息
     */
    private Notification buildNotification(Context context, PushMessageExtra msg, Intent intent, boolean isMiPush) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if (mBitmapWeakReference == null || mBitmapWeakReference.get() == null) {
            Bitmap bp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            mBitmapWeakReference = new WeakReference<Bitmap>(bp);
        }
        builder.setLargeIcon(mBitmapWeakReference.get());
        builder.setContentText(msg.content);
        builder.setContentTitle(TextUtils.isEmpty(msg.title) ?
                context.getResources().getString(R.string.app_name) : msg.title);
        builder.setAutoCancel(true);
//		builder.setExtras(intent.getExtras());
        // 设置通知时间
        String date;
        if (!TextUtils.isEmpty(msg.broadcastTime)) {
            date = msg.broadcastTime;
            builder.setWhen(DateUtil.getTime(msg.broadcastTime));
        } else {
            long curTime = System.currentTimeMillis();
            date = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.CHINA).format(curTime);
            builder.setWhen(curTime);
        }
        int hour = 12;
        int minute = 0;
        try {
            hour = Integer.parseInt(date.substring(12, 14));
            minute = Integer.parseInt(date.substring(15, 17));
        } catch (Exception ignored) {
        }
        if ((hour == START_HOUR && minute > START_MINUTE)
                || (hour == END_HOUR && minute <= END_MINUTE)
                || hour > START_HOUR
                || hour < END_HOUR) {
            builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }


        // 设置状态栏图标
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_notification_144);
            builder.setColor(Color.RED);
        } else {
            builder.setSmallIcon(R.drawable.ic_stat_notify);
        }


        builder.setTicker(msg.content);
        builder.setPriority(msg.notifyPriority);

        // 设置通知意图
        if (isMiPush) {
            intent.setAction(Action.M_OPENED);
            intent.putExtra("isNotified", false);
        } else {
            intent.setAction(JPushInterface.ACTION_NOTIFICATION_OPENED);
        }
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, BROADCAST_REQUEST, intent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags = NotificationCompat.FLAG_SHOW_LIGHTS | NotificationCompat.FLAG_AUTO_CANCEL;
        return notification;
    }

    /**
     * 退出前进行清理工作
     */
    public void exit(Context context) {
        JPushInterface.clearLocalNotifications(context);
        JPushInterface.onKillProcess(context);
        isInit = false;
    }

    private byte mHasSetAliasSign = 0;

    /**
     * 进行与操作，用来清除状态 <br />
     * 示例: <br />
     * 0x01110001 & 0x01111101(sdkType.JPUSH_F) = 0x01110001
     */
    public void andHasSetAliasSign(byte hasSetAliasSign) {
        mHasSetAliasSign &= hasSetAliasSign;
    }

    /**
     * 进行或操作，用来设置状态 <br />
     * 示例: <br />
     * 0x01110001 | 0x01110010(sdkType.JPUSH) = 0x01110011
     */
    public void orHasSetAliasSign(byte hasSetAliasSign) {
        mHasSetAliasSign |= hasSetAliasSign;
    }

    public boolean needSetJPush() {
        int sdkType = AssistantApp.getInstance().getPushSdk();
        return (sdkType == 0 || sdkType == SdkType.JPUSH)
                && (mHasSetAliasSign & SdkType.JPUSH) != SdkType.JPUSH;
    }

    public boolean needSetMPush() {
        int sdkType = AssistantApp.getInstance().getPushSdk();
        return (sdkType == 0 || sdkType == SdkType.MI)
                && (mHasSetAliasSign & SdkType.MI) != SdkType.MI;
    }

    public void updateJPushTagAndAlias(Context context) {
        if (!isInit) {
            return;
        }
        if (!AccountManager.getInstance().isLogin()) {
            // 用户不处于登录状态，不进行别名标记
            mHasSetAliasSign = 0;
            return;
        }
        // 使用uid进行别名标记
        String alias = Coder_Md5.md5(String.valueOf(AccountManager.getInstance().getUserInfo().uid));

        if (needSetMPush()) {
            // 说明需要初始化小米Sdk，但是还没有设置
            MiPushClient.setAlias(context, alias, null);
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "设置小米Alias");
        }
        if (needSetJPush()) {
            JPushInterface.setAlias(context, alias, new JPushTagsAliasCallback(context));
            AppDebugConfig.d(AppDebugConfig.TAG_PUSH, "设置极光Alias");
        }
    }
}
