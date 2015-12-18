package net.youmi.android.libs.common.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import net.youmi.android.libs.common.debug.DLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Util_System_Permission {

	/**
	 * 检查是否具有写入外部存储卡的权限
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_WRITE_EXTERNAL_STORAGE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	/**
	 * 检查是否具有联网INTERNET权限
	 * <p/>
	 * 允许该应用创建网络套接字和使用自定义网络协议。浏览器和其他某些应用提供了向互联网发送数据的途径，因此应用无需该权限即可向互联网发送数据。
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_INTERNET_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.INTERNET);
	}

	/**
	 * 检查是否具有获取手机信息READ_PHONE_STATE权限
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_READ_PHONE_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.READ_PHONE_STATE);
	}

	/**
	 * 检查是否具有ACCESS_NETWORK_STATE权限
	 * <p/>
	 * 允许该应用查看网络连接的相关信息，例如存在和连接的网络。
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_ACCESS_NETWORK_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
	}

	/**
	 * 检查是否具有ACCESS_FINE_LOCATION权限
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_ACCESS_FINE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_COARSE_LOCATION权限
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_ACCESS_COARSE_LOCATION_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
	}

	/**
	 * 检查是否具有ACCESS_WIFI_STATE权限
	 * <p/>
	 * 允许该应用查看WLAN网络的相关信息，例如是否启用了WLAN以及连接的WLAN设备的名称。
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_ACCESS_WIFI_STATE_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.ACCESS_WIFI_STATE);
	}

	/**
	 * 检查是否具有创建快捷方式的权限。
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_INSTALL_SHORTCUT_Permission(Context context) {
		return isWithPermission(context, "com.android.launcher.permission.INSTALL_SHORTCUT");
	}

	/**
	 * 检查是否具有添加系统浏览器书签的权限
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_WRITE_HISTORY_BOOKMARKS(Context context) {
		return isWithPermission(context, "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS");
	}

	/**
	 * 检查是否具有SYSTEM_ALERT_WINDOW方法
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_SYSTEM_ALERT_WINDOW_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW);
	}

	/**
	 * 检查是否具有GET_TASK方法
	 *
	 * @param context
	 *
	 * @return
	 */
	public static boolean isWith_GET_TASK_Permission(Context context) {
		return isWithPermission(context, Manifest.permission.GET_TASKS);
	}

	/**
	 * 获取开发者在AndroidManifest.xml文件中配置的所有权限信息，注意：仅仅是获取是否有没有在AndroidManifest.xml中配置，并不是是否已经被允许了
	 * {@link #isWithPermission(android.content.Context, String)} 方法是不能判断到下面这种权限的存在的
	 * <pre>
	 * <uses-permission
	 *     android:name="android.permission.PACKAGE_USAGE_STATS"
	 *     tools:ignore="ProtectedPermissions" />
	 * </pre>
	 * 因此就存在了这种一次性获取所有权限的，然后进行contains的方法来进行判断是否拥有某个权限的方法
	 *
	 * @param context
	 *
	 * @return
	 */
	public static List<String> getPkgNamePermissions(Context context, String pkgName) {
		try {
			return Arrays.asList(context.getPackageManager()
					.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS).requestedPermissions);
		} catch (PackageManager.NameNotFoundException e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 是否具有相应权限
	 * <p/>
	 * 效果等于{@link #isPermissionGranted(android.content.Context, String, String)}
	 *
	 * @param context
	 * @param permissionName
	 *
	 * @return
	 */
	public static boolean isWithPermission(Context context, String permissionName) {
		try {
			if (context.checkCallingOrSelfPermission(permissionName) == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return true;
	}

	/**
	 * 检查某个应用是否允许某个权限 support api 23+ @since 2015-12-09
	 * <p/>
	 * 效果等于 {@link #isWithPermission(android.content.Context, String)}
	 *
	 * @return
	 */
	public static boolean isPermissionGranted(Context context, String pkgName, String permission) {
		try {
			return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(permission, pkgName);
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return false;
	}

	//	/**
	//	 * 代码的方法直接请求权限
	//	 *
	//	 * @param context
	//	 * @param permissionsList
	//	 *
	//	 * @return
	//	 */
	//	public static List<String> requestPermissions(Activity activity, List<String> permissionsList) {
	//		try {
	//
	//			if (Build.VERSION.SDK_INT >=23 ) {
	//				Class c = Class.forName(Activity.class.getName());
	//				Method method = c.getMethod("requestPermissions", String[].class, int.class);
	//
	//			}
	//			PackageManager packageManager = context.getPackageManager();
	//
	//		} catch (Throwable e) {
	//			if (DLog.isUtilLog) {
	//				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
	//			}
	//		}
	//		return Collections.emptyList();
	//
	//
	//		public final void requestPermissions(@NonNull String[] permissions, int requestCode) {
	//			Intent intent = getPackageManager().buildRequestPermissionsIntent(permissions);
	//			startActivityForResult(REQUEST_PERMISSIONS_WHO_PREFIX, intent, requestCode, null);
	//		}
	//	}

	/**
	 * 为用户导航到指定应用的详情页面，让用户自己主动开启权限
	 * <p/>
	 * 目前只能导航到最近一级的界面，没法直接到达指定应用的权限设置界面
	 * <p/>
	 * <b>需要注意Intent不过去的时候会抛异常的情况，如一些厂商去掉了这个界面之类的</b>
	 *
	 * @return true: 能成功导航到应用详情页面 <p>false: 不能成功导航到应用详情页面</p>
	 *
	 * @since 2015-12-09
	 */
	public static boolean openAppPermissionSetting(Context context, String pkgName) {
		try {
			Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkgName));
			appSettingsIntent.addCategory(Intent.CATEGORY_DEFAULT);
			appSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(appSettingsIntent);
			return true;
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return false;
	}

	/**
	 * 检查用户是否允许查看指定包名的是否允许查看组件信息
	 *
	 * @param context
	 * @param opStr   {@link http://developer.android.com/reference/android/app/AppOpsManager.html}
	 *
	 * @return
	 */
	@TargetApi(21)
	public static boolean isPkgAllow2GetUsageStatus(Context context, String pkgName) {
		try {
			if (Build.VERSION.SDK_INT >= 19) {
				PackageManager packageManager = context.getPackageManager();
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkgName, 0);
				AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
				int mode = appOpsManager
						.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
				return mode == AppOpsManager.MODE_ALLOWED;
			}
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
			}
		}
		return false;
	}

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
	//			Method setModeMethod = appOpsManagerClass.getMethod("setMode", int.class, int.class, String.class, int.class);
	//			Object appOpsManagerInstance = context.getSystemService("appops");
	//
	//			// allow app get permission
	//			setModeMethod.invoke(appOpsManagerInstance, opCode, Binder.getCallingUid(), context.getPackageName(), 0);
	//
	//			//			// setMode 方法是@hide的，所以只能通过反射方法进行设置
	////						AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService("appops");
	////						appOpsManager.setMode(AppOpsManager.OP_GET_USAGE_STATS, applicationInfo.uid, applicationInfo
	// .packageName,
	////								AppOpsManager.MODE_ALLOWED);
	//			return true;
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isUtilLog) {
	//				DLog.te(DLog.mUtilTag, Util_System_Permission.class, e);
	//			}
	//		}
	//
	//		return false;
	//	}

}
