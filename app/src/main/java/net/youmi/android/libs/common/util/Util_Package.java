package net.youmi.android.libs.common.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 运用 linux 内核中的 oom-killer机制来判断前台进程包名
 *
 * @author zhitao
 * @since 2015-09-23 19:27
 */
public class Util_Package {

	/**
	 * 获取当前在顶端运行的应用包名(适用于Andriod 5.0（不包括Android 5.0）之前的机器)
	 *
	 * @param context
	 *
	 * @return
	 */
	public static String getTopgPkgNameBelowAndroidL(Context context) {

		if (Build.VERSION.SDK_INT < 21) {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
			if (DLog.isUtilLog) {
				DLog.td(DLog.mUtilTag, Util_System_Package.class, "##当前顶端应用包名:%s", cn.getPackageName());
			}
			return cn.getPackageName();
		}
		return null;
	}

	/**
	 * 获取当前在顶端运行的应用包名(建议在Android 5.x机器上，使用适用于Andriod 5.0以上的机器)
	 * <p/>
	 * 这个方法只能获取当前顶端运行的进程下的所有包名列表，而且部分机型上还可能获取不到除了本应用外的其他运行进程
	 * <p/>
	 * 在android m 上已经不能获取到除自身应用外的其他进程信息 除非添加了 REAL_GET_TASK 权限（系统权限）
	 *
	 * @param context
	 *
	 * @return
	 */
	public static String[] getTopPkgNameAboveAndroidLThroughGetRunningAppProcesses(Context context) {

		// 方法一：通过获取当前在运行的进程，然后遍历该进程下的所有包名，大概得出当前运行在顶端的包名列表
		if (Build.VERSION.SDK_INT >= 21) {

			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
			if (DLog.isUtilLog) {
				DLog.td(DLog.mUtilTag, Util_System_Package.class, "##当前运行中的进程有%d个",
						processInfos == null ? 0 : processInfos.size());
			}
			if (processInfos == null || processInfos.isEmpty()) {
				return null;
			}
			for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
				if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					if (DLog.isUtilLog) {
						DLog.td(DLog.mUtilTag, Util_System_Package.class, "##当前顶端进程id:%d 进程名：%s", processInfo.pid,
								processInfo.processName);
						for (String pkgName : processInfo.pkgList) {
							DLog.td(DLog.mUtilTag, Util_System_Package.class, "####当前顶端进程可能包名：%s", pkgName);
						}
					}
					return processInfo.pkgList;
				}
			}
		}
		return null;
	}

	/**
	 * 获取当前在顶端运行的应用包名(适用于Andriod 5.0以上的机器)
	 * <p>
	 * 通过AndroidL的新API——UsageStatsManager来进行获取，获取到的结果十分准确，但是需要配置权限和需要用户允许获取
	 * </p>
	 * <p/>
	 * 使用本方法之前需要配置下面内容
	 * <pre>
	 * 1. 权限配置
	 *   a. 需要在AndroidManifest.xml中配置权限
	 *
	 * 	    < uses-permission
	 * 	        android:name="android.permission.PACKAGE_USAGE_STATS"
	 * 	        tools:ignore="ProtectedPermissions" />
	 *
	 *
	 *   b. 然后还要在AndroidManifest.xml中的manifest标签中配置
	 * 	    xmlns:tools="http://schemas.android.com/tools"
	 *
	 * 2. 需要用户允许这个应用能获取用户数据统计信息的权限
	 * 		Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
	 * 		startActivity(intent);
	 * </pre>
	 *
	 * @param time_ms 从${time_ms}内找出最近显示在顶端的包名<p>
	 *                这个时间不建议设置太短，假设设置为5秒的话，那么如果你在玩一个应用超过5s，那么之后的时间（如第6秒  第30秒 获取到的顶端包名会为空）
	 *
	 * @return
	 */
	public static String getTopPkgNameAboveAndroidLThroughUsageStatsManagerByReflect(Context context, long time_ms) {

		// 通过Android 5.0 之后提供的新api来获取最近一段时间内的应用的相关信息
		String topPackageName = null;

		if (Build.VERSION.SDK_INT < 21) {
			return null;
		}
		try {

			long time = System.currentTimeMillis();
			Class UsageStatsManagerClass = Class.forName("android.app.usage.UsageStatsManager");
			Object UsageStatsManagerObject = context.getSystemService("usagestats");
			Method queryUsageStatsMethod = UsageStatsManagerClass.getMethod("queryUsageStats", int.class, long.class, long
					.class);
			List list = (List) queryUsageStatsMethod.invoke(UsageStatsManagerObject, 4, time - time_ms, time);
			if (DLog.isUtilLog) {
				DLog.td(DLog.mUtilTag, Util_System_Package.class, "反射结果:获取最近 %d ms内的应用信息有%d个", time_ms,
						list == null ? 0 : list.size());
			}
			if (list != null && !list.isEmpty()) {

				Class UsageStatsClass = Class.forName("android.app.usage.UsageStats");
				Method getLastTimeUsedMethod = UsageStatsClass.getMethod("getLastTimeUsed");
				Method getPackageNameMethod = UsageStatsClass.getMethod("getPackageName");

				SortedMap<Long, Object> runningTask = new TreeMap<Long, Object>();
				for (Object obj : list) {
					try {
						Object temp = getLastTimeUsedMethod.invoke(obj);
						if (temp == null) {
							continue;
						}
						runningTask.put(Long.valueOf(temp.toString()), obj);
					} catch (Throwable e) {
						if (DLog.isUtilLog) {
							DLog.te(DLog.mUtilTag, Util_System_Package.class, e);

						}
					}

				}
				if (!runningTask.isEmpty()) {

					Object temp = getPackageNameMethod.invoke(runningTask.get(runningTask.lastKey()));
					topPackageName = temp == null ? null : temp.toString();
					if (DLog.isUtilLog) {
						DLog.td(DLog.mUtilTag, Util_System_Package.class, "##(反射方法获取)当前顶端应用包名:%s", topPackageName);
					}
				}
			}
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Package.class, e);

			}
		}

		return topPackageName;
	}
	//  非反射实现
	//	public static String getTopPkgNameAboveAndroidLThroughUsageStatsManager(Context context, long time_ms) {
	//
	//		// 通过Android 5.0 之后提供的新api来获取最近一段时间内的应用的相关信息
	//		String topPackageName = null;
	//
	//		if (Build.VERSION.SDK_INT >= 21) {
	//
	//			try {
	//
	//				// 根据最近5秒内的应用统计信息进行排序获取当前顶端的包名
	//				long time = System.currentTimeMillis();
	//				UsageStatsManager usage = (UsageStatsManager) context.getSystemService("usagestats");
	//				List<UsageStats> usageStatsList = usage.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - time_ms,
	// time);
	//				if (usageStatsList != null && usageStatsList.size() > 0) {
	//					SortedMap<Long, UsageStats> runningTask = new TreeMap<Long, UsageStats>();
	//					for (UsageStats usageStats : usageStatsList) {
	//						runningTask.put(usageStats.getLastTimeUsed(), usageStats);
	//					}
	//					if (runningTask.isEmpty()) {
	//						return null;
	//					}
	//					topPackageName = runningTask.get(runningTask.lastKey()).getPackageName();
	//					if (Debug_SDK.isUtilLog) {
	//						DLog.td(DLog.mUtilTag, Util_System_Package.class, "##当前顶端应用包名:%s", topPackageName);
	//					}
	//				}
	//
	//			} catch (Throwable e) {
	//				if (Debug_SDK.isUtilLog) {
	//					DLog.te(DLog.mUtilTag, Util_System_Package.class, e);
	//
	//				}
	//			}
	//		}
	//
	//		return topPackageName;
	//	}
	//
	//	/**
	//	 * first app user
	//	 */
	//	private static final int AID_APP = 10000;
	//
	//	/**
	//	 * offset for uid ranges for each user
	//	 */
	//	private static final int AID_USER = 100000;

	/**
	 * 获取前台运行中的包名(除了系统应用app之外)
	 * <p/>
	 * 原理：通过Linux 内核中的OOM-Killer机制进行识别当前在顶端运行的第三方包名的应用，这个方法是不能识别在顶端云心的系统包名的）
	 *
	 * @param context
	 *
	 * @return 系统app : null <br> 第三方app : 包名
	 */
	public static String getTopPkgNameExcludeSystemAppThroughOOMKiller(Context context) {
		File[] files = new File("/proc").listFiles();
		int lowestOomScore = Integer.MAX_VALUE;
		String foregroundProcess = null;

		for (File file : files) {
			if (!file.isDirectory()) {
				continue;
			}

			int pid;
			try {
				pid = Integer.parseInt(file.getName());
			} catch (NumberFormatException e) {
				continue;
			}

			try {

				// 1. check cgroup
				// 不同手机这个文件内容可能不同，但是目前测试基本都会有"cpu"这行

				// 1.1 检查cpu：
				//     "cpu"那行最后显示"bg_non_interactive"
				// 1.2 检查cpuacct：
				//     一些机子是没有这个的
				//     一些机子是:
				//         cpuacct:/uid/10424
				//     另一些机子是:
				//         cpuacct:/uid_10117/pid_32743
				String cgroup = read(String.format("/proc/%d/cgroup", pid));
				if (Basic_StringUtil.isNullOrEmpty(cgroup)) {
					continue;
				}

				if (cgroup.contains("bg_non_interactive")) {
					continue;
				}

				String cpuacctString = null;
				String[] cgroupArray = cgroup.split("\n");
				for (String line : cgroupArray) {
					if (line.contains("cpuacct")) {
						cpuacctString = line;
						break;
					}
				}
				if (!Basic_StringUtil.isNullOrEmpty(cpuacctString) && !cpuacctString.endsWith(Integer.toString(pid))) {
					// not an application process
					continue;
				}

				// 2. check cmdline
				// 获取进程所对应的包名
				String cmdline = read(String.format("/proc/%d/cmdline", pid)).trim();
				if (cmdline.contains("com.android.systemui")) {
					continue;
				}
				if (cmdline.contains("libairDog.so")) {
					continue;
				}
				if (cmdline.contains("/")) {
					continue;
				}

				// 3. check uid
				// 判断进程uid是否为系统所用

				int uid = -1;
				String status = read(String.format("/proc/%d/status", pid));
				String[] temp = status.split("\n");
				for (String line : temp) {
					if (line.startsWith("Uid:")) {
						String uidString = line.substring(4).trim();
						String realUidString = uidString.substring(0, uidString.indexOf("\t"));
						uid = Integer.parseInt(realUidString);
						break;
					}
				}
				// 系统进程
				//				if (uid == -1 || uid == 0) {
				//					continue;
				//				}
				//				if (uid >= 1000 && uid <= 1038) {
				//					continue;
				//				}
				if (uid < 10000) {
					continue;
				}
				if (getSystemPackageName(context).contains(uid)) {
					continue;
				}
				if (DLog.isUtilLog) {
					DLog.ti(DLog.mUtilTag, Util_Package.class, "==========");
					DLog.ti(DLog.mUtilTag, Util_Package.class, "cgroup:" + cgroup);
					DLog.ti(DLog.mUtilTag, Util_Package.class, "cmdline:" + cmdline);
					DLog.ti(DLog.mUtilTag, Util_Package.class, "pid:" + pid);
					DLog.ti(DLog.mUtilTag, Util_Package.class, "uid:" + uid);
				}

				//				int appId = uid - AID_APP;
				//				// loop until we get the correct user id.
				//				// 100000 is the offset for each user.
				//				while (appId > AID_USER) {
				//					appId -= AID_USER;
				//				}
				//
				//				if (appId < 0) {
				//					continue;
				//				}

				// 4. check oom_*
				// u{user_id}_a{app_id} is used on API 17+ for multiple user account support.
				// String uidName = String.format("u%d_a%d", userId, appId);
				String oomScoreAdjString = read(String.format("/proc/%d/oom_score_adj", pid));
				if (Basic_StringUtil.isNullOrEmpty(oomScoreAdjString)) {
					oomScoreAdjString = "0";
				}
				int oomScoreAdj = Integer.parseInt(oomScoreAdjString);
				if (DLog.isUtilLog) {
					DLog.ti(DLog.mUtilTag, Util_Package.class, "oom_score_adj:" + oomScoreAdj);
				}

				String oomAdjString = read(String.format("/proc/%d/oom_adj", pid));
				if (Basic_StringUtil.isNullOrEmpty(oomAdjString)) {
					oomAdjString = "0";
				}
				int oomAdj = Integer.parseInt(oomAdjString);
				if (DLog.isUtilLog) {
					DLog.ti(DLog.mUtilTag, Util_Package.class, "oom_adj:" + oomAdj);
				}

				// 获取最终分数
				String oomScoreString = read(String.format("/proc/%d/oom_score", pid));
				if (Basic_StringUtil.isNullOrEmpty(oomScoreString)) {
					oomScoreString = "0";
				}
				int oomscore = Integer.parseInt(oomScoreString);
				if (DLog.isUtilLog) {
					DLog.ti(DLog.mUtilTag, Util_Package.class, "oom_score:" + oomscore);
				}

				// 一般来说，前台运行的第三方app的oom_store值不会为0的
				if (oomscore == 0) {
					continue;
				}

				// 顶端运行中的会为0 因此可以剔除
				if (oomScoreAdj != 0) {
					continue;
				}

				// 顶端运行中的会为0 因此可以剔除
				if (oomAdj != 0) {
					continue;
				}

				if (oomscore < lowestOomScore) {
					lowestOomScore = oomscore;
					foregroundProcess = cmdline;
				}

			} catch (Throwable e) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_Package.class, e);
				}
			}
		}
		if (DLog.isUtilLog) {
			DLog.td(DLog.mUtilTag, Util_Package.class, "当前顶端包名:%s", foregroundProcess);
		}
		return foregroundProcess;
	}

	/**
	 * 缓存系统app的uid列表
	 */
	private static List<Integer> mSystemPackageUidList;

	/**
	 * 获取系统应用的uid列表，用来做过滤
	 *
	 * @param context
	 *
	 * @return
	 */
	private static List<Integer> getSystemPackageName(Context context) {
		if (mSystemPackageUidList == null || mSystemPackageUidList.isEmpty()) {
			try {
				PackageManager pm = context.getPackageManager();
				// 查询所有已经安装的应用程序
				List<ApplicationInfo> listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
				Collections.sort(listAppcations, new ApplicationInfo.DisplayNameComparator(pm));// 排序
				mSystemPackageUidList = new ArrayList<Integer>(); // 保存过滤查到的AppInfo
				for (ApplicationInfo app : listAppcations) {
					if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
						mSystemPackageUidList.add(app.uid);
					}
				}
			} catch (Throwable e) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_Package.class, e);
				}
				mSystemPackageUidList = Collections.emptyList();
			}
		}

		return mSystemPackageUidList;
	}

	private static String read(String path) throws IOException {
		return read(new File(path));
	}

	private static String read(File file) throws IOException {
		BufferedReader reader = null;
		try {
			if (file != null && file.exists() && file.isFile() && file.canRead()) {
				reader = new BufferedReader(new FileReader(file));
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine());
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					sb.append('\n').append(line);
				}
				return sb.toString();
			}
		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_Package.class, e);
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Throwable e) {
				if (DLog.isUtilLog) {
					DLog.te(DLog.mUtilTag, Util_Package.class, e);
				}
			}
		}
		return null;
	}
}
