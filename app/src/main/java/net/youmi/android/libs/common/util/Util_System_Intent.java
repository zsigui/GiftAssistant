package net.youmi.android.libs.common.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;

import java.util.List;

public class Util_System_Intent {

	public static boolean startActivityByUriWithChooser(Context context, String uri, int flags, String title) {
		try {
			Intent intent = Intent.parseUri(uri, flags);
			if (intent == null) {
				return false;
			}

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Intent startIntent = Intent.createChooser(intent, title);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(startIntent);
			return true;
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;
	}

	public static boolean startActivityByUri(Context context, String uri, int flags) {
		try {

			Intent intent = Intent.parseUri(uri, flags);
			if (intent == null) {
				return false;
			}

			PackageManager pm = context.getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list == null) {
				return false;
			}

			if (list.size() <= 0) {
				return false;
			}

			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			return true;
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;
	}

	public static boolean startActivityByPackageName(Context context, String packageName) {
		return startActivityByPackageName(context, packageName, -1);
	}

	public static boolean startActivityByPackageName(Context context, String packageName, int flags) {
		try {
			PackageManager pm = context.getPackageManager();
			if (pm != null) {
				Intent intent = pm.getLaunchIntentForPackage(packageName);
				if (intent != null) {
					if (flags >= 0) {
						intent.addFlags(flags);
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
					return true;
				}
			}

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}

		return false;
	}

	public static boolean startServiceByUri(Context context, String uri, int flags) {
		try {
			Intent intent = Intent.parseUri(uri, flags);

			if (intent == null) {
				return false;
			}

			context.startService(intent);
			return true;
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;
	}

	public static boolean stopServiceByUri(Context context, String uri, int flags) {
		try {
			Intent intent = Intent.parseUri(uri, flags);

			if (intent == null) {
				return false;
			}

			return context.stopService(intent);

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;
	}

	public static Intent getIntentForStartActivityByPackagename(Context context, String packageName) {
		try {
			PackageManager pm = context.getPackageManager();
			if (pm != null) {
				Intent intent = pm.getLaunchIntentForPackage(packageName);
				if (intent != null) {
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					return intent;
				}
			}

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return null;
	}

	/**
	 * 发送广播事件
	 *
	 * @param context
	 * @param uri
	 * @param flags
	 * @param receiverPermission
	 *
	 * @return
	 */
	public static boolean sendBroadcastByUri(Context context, String uri, int flags, String receiverPermission) {
		try {

			Intent intent = Intent.parseUri(uri, flags);

			if (intent != null) {

				if (receiverPermission != null) {
					context.sendBroadcast(intent, receiverPermission);
					return true;
				} else {
					context.sendBroadcast(intent);
					return true;
				}
			}

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}

		return false;
	}

	/**
	 * 根据包名创建App的快捷方式
	 *
	 * @param context
	 * @param packageName
	 *
	 * @return
	 */
	public static boolean createAppShortcut(Context context, String packageName) {
		try {

			if (context == null) {
				return false;
			}

			if (!Util_System_Permission.isWith_INSTALL_SHORTCUT_Permission(context)) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_System_Intent.class, "不具有创建快捷方式的权限");
				}
				return false;
			}

			Model_App_Launch_Info app_Info = Util_System_Package.getAppLaunchInfo(context, packageName);

			if (app_Info == null) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_System_Intent.class, "获取Model_App_Launch_Info失败");
				}
				return false;
			}

			String appName = app_Info.getAppName();
			int icon = app_Info.getIconResourceId();
			String activityName = app_Info.getMainActivityName();

			Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
			intent.putExtra("duplicate", false);
			ComponentName componentName = new ComponentName(packageName, activityName);
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(componentName));

			Context localContext = null;

			if (context.getPackageName().equals(packageName)) {
				localContext = context;
			} else {

				try {
					localContext = context.createPackageContext(packageName, 3);
				} catch (Throwable e) {
					if (DLog.isUtilLog) {
						DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
					}
				}
			}
			if (localContext != null) {
				Intent.ShortcutIconResource localShortcutIconResource =
						Intent.ShortcutIconResource.fromContext(localContext, icon);
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, localShortcutIconResource);
			}

			context.sendBroadcast(intent);

			return true;

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;

	}

//	/**
//	 * 创建网页快捷方式
//	 *
//	 * @param context
//	 * @param shortCutName
//	 * @param webPageUrl
//	 * @param icon
//	 *
//	 * @return
//	 */
//	@SuppressLint("NewApi")
//	public static boolean createWebPageShortcut(Context context, String shortCutName, String webPageUrl, String icon) {
//		try {
//			if (context == null) {
//				return false;
//			}
//
//			if (!Util_System_Permission.isWith_INSTALL_SHORTCUT_Permission(context)) {
//				if (DLog.isUtilLog) {
//					DLog.te(DLog.mUtilTag, Util_System_Intent.class, "不具有创建快捷方式的权限");
//				}
//				return false;
//			}
//			if (Basic_StringUtil.isNullOrEmpty(shortCutName)) {
//				shortCutName = "网页";
//			}
//
//			Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//
//			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
//			intent.putExtra("duplicate", false);
//
//			Bitmap bp = IconLoader.syncLoadBitmap(context, icon);
//
//			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bp);
//			// 分别使用UC，QQ，百度浏览器，否则就使用默认浏览器
//			String browserPackageName = "";
//			String browserActivity = "";
//			if (Util_System_Package.isPakcageInstall(context, "com.UCMobile")) {
//				browserPackageName = "com.UCMobile";
//				browserActivity = "com.UCMobile.main.UCMobile";
//			} else if (Util_System_Package.isPakcageInstall(context, "com.tencent.mtt")) {
//				browserPackageName = "com.tencent.mtt";
//				browserActivity = "com.tencent.mtt.MainActivity";
//			} else if (Util_System_Package.isPakcageInstall(context, "com.baidu.browser.apps")) {
//				browserPackageName = "com.baidu.browser.apps";
//				browserActivity = "com.baidu.browser.framework.BdBrowserActivity";
//			}
//
//			Intent launcherIntent =
//					Util_System_Intent_Network.getToWebUrlIntent(context, browserPackageName, browserActivity, webPageUrl);
//
//			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
//
//			context.sendBroadcast(intent);
//
//			return true;
//
//		} catch (Exception e) {
//			if (DLog.isUtilLog) {
//				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
//			}
//		}
//		return false;
//
//	}

	/**
	 * 创建网页快捷方式
	 *
	 * @param context
	 * @param shortCutName  快捷方式名称
	 * @param webPageUrl    快捷方式说对应的页面地址
	 * @param icon          快捷方式图标
	 *
	 * @return
	 */
	public static boolean createWebPageShortcut(Context context, String shortCutName, String webPageUrl, Bitmap iconBm) {
		try {
			if (context == null) {
				return false;
			}

			if (!Util_System_Permission.isWith_INSTALL_SHORTCUT_Permission(context)) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_System_Intent.class, "不具有创建快捷方式的权限");
				}
				return false;
			}
			if (Basic_StringUtil.isNullOrEmpty(shortCutName)) {
				shortCutName = "网页";
			}

			Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
			intent.putExtra("duplicate", false);
			intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBm);
			// 分别使用UC，QQ，百度浏览器，否则就使用默认浏览器
			String browserPackageName = "";
			String browserActivity = "";
			if (Util_System_Package.isPakcageInstall(context, "com.UCMobile")) {
				browserPackageName = "com.UCMobile";
				browserActivity = "com.UCMobile.main.UCMobile";
			} else if (Util_System_Package.isPakcageInstall(context, "com.tencent.mtt")) {
				browserPackageName = "com.tencent.mtt";
				browserActivity = "com.tencent.mtt.MainActivity";
			} else if (Util_System_Package.isPakcageInstall(context, "com.baidu.browser.apps")) {
				browserPackageName = "com.baidu.browser.apps";
				browserActivity = "com.baidu.browser.framework.BdBrowserActivity";
			}

			Intent launcherIntent =
					Util_System_Intent_Network.getToWebUrlIntent(context, browserPackageName, browserActivity, webPageUrl);

			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

			context.sendBroadcast(intent);

			return true;

		} catch (Exception e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Intent.class, e);
			}
		}
		return false;

	}
}
