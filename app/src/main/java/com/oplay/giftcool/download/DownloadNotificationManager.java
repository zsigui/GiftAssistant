package com.oplay.giftcool.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.util.IntentUtil;

import net.youmi.android.libs.common.util.Util_System_Package;

import java.util.Locale;

/**
 * NotificationManager
 *
 * @author zacklpx
 *         date 16-1-18
 *         description
 */
public class DownloadNotificationManager {

    public static final int REQUEST_CODE_DOWNLOAD = 12040;
    public static final int REQUEST_ID_DOWNLOAD = 10240;

    public static final int REQUEST_CODE_USERMESSAGE = 1204;
    public static final int REQUEST_ID_USERMESSAGE = 1024;

    public static final String ACTION_OPEN_DOWNLOAD_FRAGMENT = "giftcool.action.download.SHOW_VIEW";
    public static final String CATEGORY_DOWNLOAD = AppConfig.PACKAGE_NAME();

    public static class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent != null) {
                    AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "action = " + intent.getAction());
                    if (ACTION_OPEN_DOWNLOAD_FRAGMENT.equals(intent.getAction())) {
                        IntentUtil.jumpDownloadManager(context);
                    }
                }
            } catch (Throwable t) {
                AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, t);
            }
        }
    }

    public static void showDownload(Context context) {
        try {

            int count = ApkDownloadManager.getInstance(context).getEndOfPaused();
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "显示下载Notification，当前正在下载数:" + count);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
                    .NOTIFICATION_SERVICE);
            if (count > 0) {
                Intent downloadIntent = getJumpDownloadManagerIntent(context);
                // 由于使用getActivity在应用打开的时候，由于taskAffinity的问题
                PendingIntent pi = PendingIntent.getBroadcast(context, REQUEST_CODE_DOWNLOAD, downloadIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                String tickerText = String.format(Locale.CHINA, "您有%d个游戏正在下载中", count);
                String title = "点击查看详情";
                Builder builder = buildNotification(context, pi, tickerText, title);
                builder.setOngoing(true);
                builder.setAutoCancel(false);
                notificationManager.notify(REQUEST_ID_DOWNLOAD, builder.build());
            } else {
                notificationManager.cancel(REQUEST_ID_DOWNLOAD);
            }
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public static void cancelDownload(Context context) {
        try {
            AppDebugConfig.v();
            final NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(REQUEST_ID_DOWNLOAD);
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public static void showDownloadComplete(Context context, GameDownloadInfo appInfo) {
        try {
            AppDebugConfig.v();
            String filePath = appInfo.getDestFilePath();
            Intent intent;
            intent = Util_System_Package.getInstallApkIntentByApkFilePath(context, filePath);

            int notificationId = appInfo.destUrl.hashCode();
            PendingIntent pi = PendingIntent.getActivity(context, notificationId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Service.NOTIFICATION_SERVICE);
            String tickerText = String.format("\"%s\"已下载完成", appInfo.name);
            final String title = "点击进行安装";

            final Builder builder = buildNotification(context, pi, tickerText, title);
//        builder.setDefaults(Notification.DEFAULT_ALL);
            builder.setAutoCancel(true);
            notificationManager.cancel(notificationId);
            notificationManager.notify(notificationId, builder.build());
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public static Builder buildNotification(Context context, PendingIntent pi, String tickerText, String title) {
        Builder builder = new Builder(context);
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setTicker(tickerText);
        builder.setContentText(tickerText);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_notify));
        updateBySDKVersion(builder);
//        builder.setDefaults(Notification.DEFAULT_ALL);
//        builder.setOngoing(true);
//        builder.setAutoCancel(false);
        return builder;
    }

    private static void updateBySDKVersion(Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_notification_144);
            builder.setColor(Color.RED);
        } else {
            builder.setSmallIcon(R.drawable.ic_stat_notify);
        }
    }

    public static void showDownloadFailed(Context context, String destUrl, String appName, String reason) {
        try {
            AppDebugConfig.v();
            Intent download = getJumpDownloadManagerIntent(context);

            int notificationId = destUrl.hashCode();
            PendingIntent pi = PendingIntent.getBroadcast(context, notificationId, download,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            final NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Service.NOTIFICATION_SERVICE);
            String tickerText = null;
            if (TextUtils.isEmpty(reason)) {
                tickerText = String.format("%s下载失败", appName);
            } else {
                tickerText = String.format("%s, %s下载失败", reason, appName);
            }
            final String title = "点击查看详情";

            Builder builder = buildNotification(context, pi, tickerText, title);
            builder.setAutoCancel(true);
            notificationManager.cancel(notificationId);
            notificationManager.notify(notificationId, builder.build());
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    /**
     * 获取发送下载广播的意图
     */
    private static Intent getJumpDownloadManagerIntent(Context context) {
        Intent intent = new Intent(context, DownloadReceiver.class);
        intent.setAction(ACTION_OPEN_DOWNLOAD_FRAGMENT);
        intent.addCategory(CATEGORY_DOWNLOAD);
        return intent;
    }

    public static void clearDownloadComplete(Context context, String destUrl) {
        try {
            AppDebugConfig.v();
            int notificationId = destUrl.hashCode();
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
                    .NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public static void clearAllNotification(Context context) {
        try {
            AppDebugConfig.v();
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
                    .NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } catch (Exception e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }
}
