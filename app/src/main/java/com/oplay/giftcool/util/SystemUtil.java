package com.oplay.giftcool.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 *
 * Created by zsigui on 15-12-18.
 */
public class SystemUtil {

	public static HashSet<String> getInstalledAppName(Context context) {
		HashSet<String> data = new HashSet<>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			try {
				PackageInfo packageInfo = packages.get(i);
				data.add(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, e);
				}
			}
		}
		return data;
	}

	public static int getVerCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
			return -1;
		}
	}
	
	public static boolean deletePackage(String filaPath) {
		boolean ret = false;
		try {
			final File file = new File(filaPath);
			if (file.exists()) {
				ret = file.delete();
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
		}
		return ret;
	}

	/**
	 * 根据应用的包名获取该应用的名称
	 */
	public static String getAppNameByPackName(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		String name = null;
		try {
			name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
		} catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
		}
		return name;
	}

	/**
	 * 判断指定服务是否处于运行中
	 */
	public static boolean isServiceRunning(Context context, String serviceName) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo serviceInfo: manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceName.equals(serviceInfo.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	public static boolean isBackground(Context context) {


		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}
}
