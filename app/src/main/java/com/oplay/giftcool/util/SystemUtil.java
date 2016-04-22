package com.oplay.giftcool.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
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

	/**
	 * 获取已经安装的应用的应用名称
	 */
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

	/**
	 * 获取已经安装应用的包名
	 */
	public static HashSet<String> getInstalledPackName(Context context) {
		HashSet<String> data = new HashSet<>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			try {
				PackageInfo packageInfo = packages.get(i);
				data.add(packageInfo.packageName);
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

	/**
	 * 判断本地应用是否处于前台
	 */
	public static boolean isMyAppInForeground(Context context) {
		return ProcessManager.isMyProcessInTheForeground();
	}

	/**
	 * 判断APP是否处于前台，采用proc文件获取方式，由于受/proc文件多少影响，属于耗时操作<br />
	 * 5.0前后都支持
	 * @return
	 */
	public static boolean isForeground(Context context, String packName) {
		List<AndroidAppProcess> processes = ProcessManager.getRunningForegroundApps(context);
		if (processes != null) {
			for (AndroidAppProcess process : processes) {
				if (process != null && process.getPackageName() != null
						&& process.getPackageName().equalsIgnoreCase(packName)) {
					return true;
				}
			}
		}
		return false;
	}

//	/**
//	 * 使用RunningTask方式判断应用是否在前台，5.0以上废弃
//	 */
//	private static boolean isForegroundBelowM(Context context, String packName) {
//
//	}
//
//	/**
//	 * 读取/proc目录下信息判断应用是否在前台，5.0以上可用，受文件影响，属于耗时操作
//	 */
//	private static boolean isForegroundAboveM(Context context, String packName) {
//
//	}

}
