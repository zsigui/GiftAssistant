package com.oplay.giftcool.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Package;

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

	public static void showDownload(Context context) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams("NotificationManager", "showDownload");
			}

			int count = ApkDownloadManager.getInstance(context).getEndOfPaused();
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
					.NOTIFICATION_SERVICE);
			if (count > 0) {
				Intent downloadIntent = IntentUtil.getJumpDownloadManagerIntent(context);
				PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE_DOWNLOAD, downloadIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				String tickerText = String.format("您有%d个游戏正在下载中", count);
				String title = "点击查看详情";

				Builder builder = buildNotification(context, pi, tickerText, title);
				builder.setOngoing(true);
				builder.setAutoCancel(false);
				notificationManager.notify(REQUEST_ID_DOWNLOAD, builder.build());
			} else {
				notificationManager.cancel(REQUEST_ID_DOWNLOAD);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public static void cancelDownload(Context context) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(DownloadNotificationManager.class);
			}
			final NotificationManager notificationManager = (NotificationManager)
					context.getSystemService(Service.NOTIFICATION_SERVICE);
			notificationManager.cancel(REQUEST_ID_DOWNLOAD);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public static void showDownloadComplete(Context context, GameDownloadInfo appInfo) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(DownloadNotificationManager.class);
			}
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
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public static Builder buildNotification(Context context, PendingIntent pi, String tickerText, String title) {
		Builder builder = new Builder(context);
		builder.setContentIntent(pi);
		builder.setContentTitle(title);
		builder.setTicker(tickerText);
		builder.setContentText(tickerText);
		//TODO 改成礼包酷logo
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
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(DownloadNotificationManager.class);
			}
			Intent download = IntentUtil.getJumpDownloadManagerIntent(context);

			int notificationId = destUrl.hashCode();
			PendingIntent pi = PendingIntent.getActivity(context, notificationId, download,
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
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public static void clearDownloadComplete(Context context, String destUrl) {
		try {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodName(DownloadNotificationManager.class);
			}
			int notificationId = destUrl.hashCode();
			final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Service
					.NOTIFICATION_SERVICE);
			notificationManager.cancel(notificationId);
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}
}
