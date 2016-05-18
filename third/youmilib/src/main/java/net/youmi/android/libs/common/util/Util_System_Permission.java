package net.youmi.android.libs.common.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Util_System_Permission {

	/**
	 * {@link #isWithPermission(android.content.Context, String)} 方法是不能判断到下面这种权限的存在的
	 * <pre>
	 * <uses-permission
	 *     android:name="android.permission.PACKAGE_USAGE_STATS"
	 *     tools:ignore="ProtectedPermissions" />
	 * </pre>
	 * 因此就存在了这种一次性获取所有权限的，然后进行contains的方法来进行判断是否拥有某个权限的方法
	 *
	 * @param context
	 * @return
	 */
	public static List<String> getPkgNamePermissions(Context context, String pkgName) {
		try {
			return Arrays.asList(context.getPackageManager()
					.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS).requestedPermissions);
		} catch (PackageManager.NameNotFoundException e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 是否具有相应权限
	 *
	 * @param context
	 * @param permissionName
	 * @return
	 */
	public static boolean isWithPermission(Context context, String permissionName) {
		try {
			if (context.checkCallingOrSelfPermission(permissionName) == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return true;
	}

	/**
	 * 检查是否具有写入外部存储卡的权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_WRITE_EXTERNAL_STORAGE_Permission(Context context) {
		try {

			// Util_SDK_Compatibility.getSDKLevel();
			int sdkLevel = Build.VERSION.SDK_INT;

			// TODO return false吧？？？？
			if (sdkLevel < VERSION_CODES.DONUT) {
				return true;
			}

			return isWithPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		} catch (Throwable e) {
			if (Debug_SDK.isUtilLog) {
				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return false;
	}

	/**
	 * 检查是否具有联网INTERNET权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_INTERNET_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.INTERNET);
	}

	/**
	 * 检查是否具有获取手机信息READ_PHONE_STATE权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_READ_PHONE_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.READ_PHONE_STATE);
	}

	/**
	 * 检查是否具有ACCESS_NETWORK_STATE权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_NETWORK_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
	}

	/**
	 * 检查是否具有ACCESS_FINE_LOCATION权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_FINE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_COARSE_LOCATION权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_COARSE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_WIFI_STATE权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_ACCESS_WIFI_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
	}

	/**
	 * 检查是否具有创建快捷方式的权限。
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_INSTALL_SHORTCUT_Permission(Context context) {
		return isWithPermission(context, "com.android.launcher.permission.INSTALL_SHORTCUT");
	}

	/**
	 * 检查是否具有添加系统浏览器书签的权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_WRITE_HISTORY_BOOKMARKS(Context context) {
		return isWithPermission(context, "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS");
	}

	/**
	 * 检查是否具有SYSTEM_ALERT_WINDOW方法
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_SYSTEM_ALERT_WINDOW_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW);
	}

	/**
	 * 检查是否具有GET_TASK方法
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWith_GET_TASK_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.GET_TASKS);
	}

	//	/**
	//	 * 检查用户是否允许某个权限(仅仅支持API19以上)
	//	 *
	//	 * @param context
	//	 * @param opStr   {@link http://developer.android.com/reference/android/app/AppOpsManager.html}
	//	 *
	//	 * @return
	//	 */
	//	public static boolean isAllowPermissionAboveAPI19(Context context, String opStr) {
	//		try {
	//			if (Build.VERSION.SDK_INT >= 19) {
	//
	//				PackageManager packageManager = context.getPackageManager();
	//				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
	//				AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
	//				int mode = appOpsManager
	//						.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo
	// .packageName);
	//				return mode == AppOpsManager.MODE_ALLOWED;
	//			}
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isUtilLog) {
	//				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
	//			}
	//		}
	//		return false;
	//	}
	//
	//	/**
	//	 * 静默方式设置本应用允许查看应用统计信息，本来这个方法是需要用户允许的，但是我们可以静默实现
	//	 *
	//	 * @param context
	//	 *
	//	 * @return
	//	 */
	//	public static boolean turnOnUsageStatsPermissionSlient(Context context) {
	//		return turnOnPermission(context, 43);
	//	}
	//
	//	/**
	//	 * 通过反射调用直接开启某个权限，不用用户跳转到具体页面进行开启
	//	 *
	//	 * @param context
	//	 * @param opCode  你要为这个应用开启的权限的code，值请自行查阅{@link AppOpsManager} 中，“OP_”开头的静态变量
	//	 *
	//	 * @return
	//	 */
	//	public static boolean turnOnPermission(Context context, int opCode) {
	//		try {
	//
	////			PackageManager packageManager = context.getPackageManager();
	////			ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
	//
	//			// init param
	//			Class appOpsManagerClass = Class.forName("android.app.AppOpsManager");
	//			Method setModeMethod = appOpsManagerClass.getMethod("setMode", int.class, int.class, String.class, int
	// .class);
	//			Object appOpsManagerInstance = context.getSystemService("appops");
	//
	//			// allow app get permission
	//			setModeMethod.invoke(appOpsManagerInstance, opCode, Binder.getCallingUid(), context.getPackageName(),
	// 0);
	//
	//			//			// setMode 方法是@hide的，所以只能通过反射方法进行设置
	////						AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
	////						appOpsManager.setMode(AppOpsManager.OP_GET_USAGE_STATS, applicationInfo.uid,
	// applicationInfo
	// .packageName,
	////								AppOpsManager.MODE_ALLOWED);
	//			return true;
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isUtilLog) {
	//				Debug_SDK.te(Debug_SDK.mUtilTag, Util_System_Permission.class, e);
	//			}
	//		}
	//
	//		return false;
	//	}

}
