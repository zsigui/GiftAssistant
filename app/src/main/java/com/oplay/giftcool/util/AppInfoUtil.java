package com.oplay.giftcool.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Permission;

/**
 * Created by zsigui on 15-12-25.
 */
public class AppInfoUtil {

	public static String getSPN(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
					.TELEPHONY_SERVICE);
			return telephonyManager.getSimOperatorName();
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
		}
		return "UNKNOWN_SPN";
	}

	public static String getAPN(Context context) {
		// 不具有连接网络权限
		if (!Util_System_Permission.isWith_INTERNET_Permission(context)) {
			if (AppDebugConfig.IS_PERM_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "Please Add \"android.permission.INTERNET\" To AndroidManifest.xml");
			}
		}

		// 1、判断是否允许程序访问有关GSM网络信息
		// 不具有访问GSM网络权限
		if (!Util_System_Permission.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
			if (AppDebugConfig.IS_PERM_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "Please Add \"android.permission.ACCESS_NETWORK_STATE\" To " +
						"AndroidManifest.xml");
			}
		}
		// 2、判断是否允许程序访问Wi-Fi网络状态信息,经过测试，不需要检测这个权限也可以获取到网络信息,
		// 包括当前是wifi网络也可以判断出来，但是不配置的话就不能使用wifi网络，不具有访问WIFI网络权限
		if (!Util_System_Permission.isWith_ACCESS_WIFI_STATE_Permission(context)) {
			if (AppDebugConfig.IS_PERM_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, "Please Add \"android.permission.ACCESS_WIFI_STATE\" To " +
						"AndroidManifest.xml");
			}
		}

		try {
			NetworkInfo activeNetworkInfo = getNetWorkInfo(context);
			if (activeNetworkInfo != null) {
				// 网络不可用
				if (!activeNetworkInfo.isAvailable()) {
					if (AppDebugConfig.IS_DEBUG) {
						KLog.d(AppDebugConfig.TAG_UTIL, "当前网络不可用");
					}
					return "UNKNOW_APN";
				}
				// 网络可用
				else {
					// 判断当前网络类型
					switch (activeNetworkInfo.getType()) {
						case ConnectivityManager.TYPE_WIFI: // wifi网络
							if (AppDebugConfig.IS_DEBUG) {
								KLog.d(AppDebugConfig.TAG_UTIL, "当前网络为wifi网络");
							}
							return "WIFI";
						case ConnectivityManager.TYPE_MOBILE: // 手机网络
							if (AppDebugConfig.IS_DEBUG) {
								KLog.d(AppDebugConfig.TAG_UTIL, "当前网络为手机网络");
							}
							return activeNetworkInfo.getSubtypeName();
						default:
							break;
					}
				}
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
		}
		return "UNKNOW_APN";
	}

	private static NetworkInfo getNetWorkInfo(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			return connectivityManager.getActiveNetworkInfo();
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_UTIL, e);
			}
		}
		return null;
	}
}
