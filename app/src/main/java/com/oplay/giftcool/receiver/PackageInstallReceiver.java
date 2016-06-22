package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.download.InstallNotifier;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.oplay.giftcool.util.SystemUtil;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.youmi.android.libs.common.basic.Basic_StringUtil;

import java.io.File;
import java.util.Locale;

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
            AppDebugConfig.d(AppDebugConfig.TAG_DOWNLOAD, "action = " + action + ", packName = " + packName);
            if (packName == null || packName.equalsIgnoreCase(context.getPackageName())) {
                return;
            }
            // 安装新应用
            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                try {
                    handleAppState(context, packName);
//					handlePlayDownloadTask(context, packName);
                } catch (Throwable e) {
                    AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD,e);
                }
            }
            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {// 卸载应用
                if (Global.getInstalledAppNames() != null) {
                    Global.getInstalledAppNames().remove(SystemUtil.getAppNameByPackName(context, packName));
                }
            }
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InstallNotifier.getInstance().notifyInstallListeners(context, packName);
                }
            }, 500);
            AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, packName);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD,e);
        }
    }

//	/**
//	 * 判断是否指定试玩游戏，并开启服务监听游戏的启动状态
//	 */
//	private void handlePlayDownloadTask(Context context, String packName) {
//		final boolean contain = ScoreManager.getInstance().containDownloadTask(context, packName);
//		if (contain) {
//			AlarmClockManager.getInstance().setObserverGame(true);
//		}
//	}

    /**
     * 处理游戏下载安装后的APP状态变化
     */
    private void handleAppState(Context context, String packName) {
        final GameDownloadInfo appInfo = ApkDownloadManager.getInstance(context)
                .getAppInfoByPackageName(packName);
        if (appInfo != null) {
            if (Global.getInstalledAppNames() != null) {
                Global.getInstalledAppNames().add(SystemUtil.getAppNameByPackName(context, packName));
            }
            // remove notification
            DownloadNotificationManager.clearDownloadComplete(context, appInfo.destUrl);
            // remove apk if necessary
            if (AssistantApp.getInstance().isShouldAutoDeleteApk()) {
                appInfo.initAppInfoStatus(context);
                final File file = appInfo.getDestFile();
                if (file != null && file.exists()) {
                    final String delToast = String.format(Locale.CHINA, ConstString.TOAST_REMOVE_AFTER_SUCCESS_INSTALL,
                            appInfo.packageName, appInfo.size);
                    final boolean delete = file.delete();
                    if (delete) {
                        ToastUtil.showShort(delToast);
                        appInfo.initAppInfoStatus(context);
                    }
                    AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, String.format("File deleted [%b]: %s", delete, file));
                }
            }
        }
    }
}
