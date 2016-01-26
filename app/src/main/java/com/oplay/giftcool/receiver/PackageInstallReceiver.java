package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.download.InstallNotifier;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.basic.Basic_StringUtil;

import java.io.File;

/**
 * PackageInstallReceiver
 *
 * @author zacklpx
 *         date 16-1-24
 *         description
 */
public class PackageInstallReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		try {
			final String action = intent.getAction();
			// 获取包名
			final String pnStr = intent.getData().getSchemeSpecificPart();
			final String packName = Basic_StringUtil.getNotEmptyStringElseReturnNull(pnStr);
			if (packName == null || packName.equalsIgnoreCase(context.getPackageName())) {
				return;
			}
			// 安装新应用
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				final GameDownloadInfo appInfo = ApkDownloadManager.getInstance(context)
						.getAppInfoByPackageName(packName);
				try {
					if (appInfo != null) {
						// remove notification
						DownloadNotificationManager.clearDownloadComplete(context, appInfo.destUrl);
						// remove apk if necessary
						if (AssistantApp.getInstance().isShouldAutoDeleteApk()) {
							appInfo.initAppInfoStatus(context);
							final File file = appInfo.getDestFile();
							if (file != null && file.exists()) {
								final String delToast = String.format("已删除%s安装包，节省%s空间", appInfo.packageName, appInfo
										.size);
								final boolean delete = file.delete();
								if (delete) {
									ToastUtil.showShort(delToast);
									appInfo.initAppInfoStatus(context);
								}
								if (AppDebugConfig.IS_DEBUG) {
									AppDebugConfig.logMethodWithParams(this,
											String.format("File deleted [%b]: %s", delete, file));
								}
							}
						}
					}
				} catch (Throwable e) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.e(e);
					}
				}
			}
			if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {// 卸载应用
			}
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					InstallNotifier.getInstance().notifyInstallListeners(context, packName);
				}
			}, 500);
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, packName);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}
}
