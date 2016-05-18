package net.youmi.android.libs.common.global;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.List;

/**
 * TODO： 后续可以归纳出一些通用的模拟器特征了，有一些想法，只是需要大量测试
 *
 * @author zhitaocai
 * @date 2015-3-17-下午9:43:46
 */
public class Global_Runtime_SystemInfo_Simulator {

	private static int mSimulatorType = 0;

	/**
	 * 获取当前设备模拟器类型
	 * <ul>
	 * <li>0 : 不是模拟器</li>
	 * <li>1 : 未知类型模拟器</li>
	 * <li>2 : Genymotion模拟器</li>
	 * <li>3 : 天天模拟器</li>
	 * <li>4 : Droid4X(海马玩模拟器)模拟器</li>
	 * <li>5 : BlueStacks模拟器</li>
	 * </ul>
	 */
	public final static int getEmulatorType() {
		return mSimulatorType;

	}

	/**
	 * 判断当前设别是否为模拟器
	 * <p/>
	 * 这里不能用全局静态变量来缓存这个值，实时获取结果好点
	 *
	 * @param context
	 * @return
	 */
	public final static boolean isEmulator(Context context) {
		try {
			if (("unknown".equals(Build.BOARD)) && ("generic".equals(Build.DEVICE)) && ("generic".equals(Build.BRAND)
			)) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, "device is " +
							"emulator");
				}
				mSimulatorType = 1;
				return true;
			}

			// 根据设备名称（手机型号）检查是否为模拟器
			if (!Basic_StringUtil.isNullOrEmpty(Build.MODEL)) {
				String deviceName = Build.MODEL.trim().toLowerCase();

				// 模拟器
				if ("sdk".equals(deviceName)) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, "device is " +
								"emulator");
					}
					mSimulatorType = 1;
					return true;
				}

				// 谷歌地图模拟器
				if ("google_sdk".equals(deviceName)) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class,
								"device is google map emulator");
					}
					mSimulatorType = 1;
					return true;
				}
			}

			// Genymotion模拟器
			if (isGenymotionSimulator(context)) {
				mSimulatorType = 2;
				return true;
			}

			// 天天模拟器
			if (isTiantianSimulator(context)) {
				mSimulatorType = 3;
				return true;
			}

			// Droid4X模拟器
			if (isDroid4XSimulator(context)) {
				mSimulatorType = 4;
				return true;
			}

			// BlueStacks模拟器（升级模拟器）
			if (isBlucStacksSimulator(context)) {
				mSimulatorType = 5;
				return true;
			}

		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, e);
			}
		}
		return false;
	}

	/**
	 * 判断是否为Genymotion模拟器
	 *
	 * @param context
	 * @return
	 * @author zhitaocai
	 * @since 2015-03-17
	 */
	private final static boolean isGenymotionSimulator(Context context) {
		try {
			if (("unknown".equals(Build.BOARD)) && ("vbox86p".equals(Build.DEVICE)) && ("generic".equals(Build.BRAND)
			) &&
					("test-keys".equals(Build.TAGS))) {
				if (Debug_SDK.isGlobalLog) {
					Debug_SDK
							.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, "device is Genymotion" +
									" " +
									"emulator");
				}
				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, e);
			}
		}
		return false;
	}

	/**
	 * 判断是否为天天模拟器
	 *
	 * @param context
	 * @return
	 */
	private final static boolean isTiantianSimulator(Context context) {
		try {
			if (!Basic_StringUtil.isNullOrEmpty(Build.MODEL)) {
				String deviceName = Build.MODEL.trim().toLowerCase();
				if (deviceName.contains("tiantian")) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class,
								"device is Tiantian emulator");
					}
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, e);
			}
		}
		return false;
	}

	/**
	 * 判断是否为Droid4X(海马玩模拟器)模拟器
	 *
	 * @param context
	 * @return
	 * @author zhitaocai
	 * @since 2015-03-17
	 */
	private final static boolean isDroid4XSimulator(Context context) {
		try {
			if (Basic_StringUtil.isNullOrEmpty(Build.MODEL)) {
				if (("Droid4X".equals(Build.DEVICE)) && ("test-keys".equals(Build.TAGS)) && (Build.MODEL.contains
						("Droid4X"))) {
					if (Debug_SDK.isGlobalLog) {
						Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class,
								"device is Droid4X emulator");
					}
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, e);
			}
		}
		return false;
	}

	// ------------------------------------------------------------------------------
	// BlueStacks 检查

	/**
	 * 是否检查过当前机器是否为BlueStacks模拟器
	 */
	private static boolean isCheckBlusStacksSimulator = false;

	/**
	 * 判断是否为BlueStacks模拟器
	 */
	private static boolean isBlueStacksSimulator = false;

	/**
	 * 判断是否为BlueStacks模拟器：暂时通过检查是否有下列的系统级应用包名来决定（实测用时10ms左右）
	 * <ul>
	 * <li>com.bluestacks.settings</li>
	 * <li>com.bluestacks.appsetting</li>
	 * <li>com.bluestacks.bluestackslocationprovider</li>
	 * </ul>
	 *
	 * @return
	 */
	private final static boolean isBlucStacksSimulator(Context context) {

		// 这里主要是因为这个是检查会比较耗时，因此只进行一次检查，然后将结果缓存为静态变量
		// 如果之前已经检查过是否为bluestacks模拟器的话，直接返回之前的结果
		if (isCheckBlusStacksSimulator) {
			return isBlueStacksSimulator;
		}
		try {
			// long st = System.currentTimeMillis();
			// 采用等级来判断BlueStacks模拟器，等级越高就表示越确定
			int simulatorLevel = 0;

			// 方法一：采用遍历系统已安装的包名进行检索
			List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
			for (int i = 0; i < packages.size(); ++i) {
				try {
					PackageInfo packageInfo = packages.get(i);
					String pkgName = packageInfo.packageName;
					// 如果是系统应用
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
						if (pkgName.trim().toLowerCase().equals("com.blue".trim() + "stacks.appsettings")) {
							++simulatorLevel;
						}
						if (pkgName.trim().toLowerCase().equals("com.blues".trim() + "tacks.settings")) {
							++simulatorLevel;
						}
						if (pkgName.trim().toLowerCase()
								.equals("com.blues".trim() + "tacks.blue".trim() + "stackslocationprovider")) {
							++simulatorLevel;
						}
						// 这里如果已经满足上面3个条件就直接结束循环了
						if (simulatorLevel > 2) {
							break;
						}
					}
				} catch (Throwable e) {
				}
			}

			isCheckBlusStacksSimulator = true;
			if (simulatorLevel >= 2) {
				isBlueStacksSimulator = true;
			}
			// long et = System.currentTimeMillis();
			// if (Debug_SDK.isGlobalLog) {
			// Debug_SDK.td(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class,
			// "判断是否为BlueStacks模拟器，起始时间：%d ms 结束时间:%d ms, 用时： %d ms", st, et, et - st);
			// }
		} catch (Exception e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Global_Runtime_SystemInfo_Simulator.class, e);
			}
		}

		return isBlueStacksSimulator;
	}

}
