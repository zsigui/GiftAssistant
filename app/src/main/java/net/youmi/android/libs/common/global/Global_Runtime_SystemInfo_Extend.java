package net.youmi.android.libs.common.global;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.DLog;
import net.youmi.android.libs.common.global.model.Model_Battery;
import net.youmi.android.libs.common.util.Util_System_Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 获取系统一些辅助性的设备信息
 *
 * @author zhitaocai
 * @since 2015-2-2上午11:13:27
 */
public class Global_Runtime_SystemInfo_Extend {

	/**
	 * 手机是否开启了开发者调试模式，如果获取不到，就返回false
	 *
	 * @return
	 */
	public final static boolean isOpenAdbDebugModel(Context context) {
		try {
			if (Build.VERSION.SDK_INT < 17) {
				return Settings.Secure.getInt(context.getContentResolver(), "adb_enabled", 0) > 0;
			} else {
				return Settings.Global.getInt(context.getContentResolver(), "adb_enabled", 0) > 0;
			}
		} catch (Throwable e) {
			if (DLog.isGlobalLog) {
				DLog.te(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, e);
			}
		}
		return false;
	}

	/**
	 * 获取屏幕空闲时间
	 *
	 * @return
	 */
	public final static float getScreenFreeTime_s() {

		return 0;
	}

	/**
	 * 获取屏幕解锁次数
	 *
	 * @return
	 */
	public final static int getScreenUnLockTimes() {
		return 0;
	}

	/**
	 * 获取电池相关信息
	 *
	 * @param context
	 *
	 * @return
	 */
	public final static Model_Battery getBatteryMsg(Context context) {
		Model_Battery batteryInfo = new Model_Battery();
		try {
			Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

			// 获取剩余电量百分比
			int currLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int total = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
			int percent = currLevel * 100 / total;
			batteryInfo.currentBatteryPercent = percent;

			// 判断是否正在进行充电
			int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean isInCharge = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager
					.BATTERY_STATUS_FULL;
			batteryInfo.isInCharge = isInCharge;

			// 如果是正在充电的话，就判断当前是什么充电方式
			if (isInCharge) {
				batteryInfo.inChargeType = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			}

			// List<String> temp = RootHelper.execRootCmd("cat /sys/class/power_supply/battery/serial_number");
			// for (String result : temp) {
			// if (Debug_SDK.isGlobalLog) {
			// DLog.td(DLog.mGlobalTag, Global_Runtime_SystemInfo_20150111.class, "电池序列号:%s", result);
			// }
			// }

			if (DLog.isGlobalLog) {
				DLog.td(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, "电池序列号:%s \n%s", "", batteryInfo.toString());
			}
		} catch (Throwable e) {
			if (DLog.isGlobalLog) {
				DLog.te(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, e);
			}
		}
		return batteryInfo;
	}

	/**
	 * 获取操作系统描述
	 *
	 * @param context
	 *
	 * @return
	 */
	public final static String getSystemDescription(Context context) {
		try {
			String desc = getSystemProp(context, "ro.build.description", "");
			if (Basic_StringUtil.isNullOrEmpty(desc)) {
				desc = getSystemProp(context, "ro.build.fingerprint", "");
			}
			return desc;
		} catch (Throwable e) {
			if (DLog.isGlobalLog) {
				DLog.te(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, e);
			}
		}
		return null;
	}

	/**
	 * 传入指定的key值即可获取指定的系统信息
	 *
	 * @param context
	 * @param key
	 * @param defValue
	 *
	 * @return
	 */
	public final static String getSystemProp(Context context, String key, String defValue) {

		// 方案一：采用反射的方法直接调用javaapi进行获取prop的指定数据
		try {
			Class<?> c = Class.forName("android.os.Build");
			Method m = c.getDeclaredMethod("getString", String.class);
			m.setAccessible(true);
			Object result = m.invoke(c, key);
			if (result != null) {
				if (DLog.isGlobalLog) {
					DLog.te(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, "getprop %s : %s", key, result.toString());
				}
				return result.toString();
			}
		} catch (Throwable e) {
		}
		return defValue;

		// 方案二：采用getprop的命令方式进行获取

		// 具体实现需要依靠process类来进行，但是实际使用过程中，发现在一些机型上（如：Alcatel One Touch
		// 986（360出品）），运行几次之后就不能继续创建process的实例，并且还会堵塞，因此这个方法暂时注释

		// String prop = Util_System_Process.execute("getprop", key);
		// if (!Basic_StringUtil.isNullOrEmpty(prop)) {
		// return prop;
		// }
		// return defValue;

	}

	/**
	 * 采用静态变量来缓存pid 的md5值
	 */
	private static String mMd5SyatemPkgPid;

	/**
	 * 获取系统包名所在进程的pid列表，由小到大排序pid后，拼接在一起，计算md5，返回md5的值
	 */
	public final static String getMd5SystemPkgPid(Context context) {

		if (!Basic_StringUtil.isNullOrEmpty(mMd5SyatemPkgPid)) {
			return mMd5SyatemPkgPid;
		}

		try {
			List<String> pkgList = new ArrayList<String>();

			// 默认一定有的几个运行中的进程
			pkgList.add("android"); // android 系统
			pkgList.add("com.android.phone"); // 电话
			pkgList.add("com.android.settings"); // 设置
			pkgList.add("com.android.systemui");// 系统用户界面 (升级之后,uid就不是1000了)

			//pkgList.add("com.android.keychain"); // 密码链(不一定会运行 xiaomi不会)
			//pkgList.add("com.android.musicfx"); // Music
			//pkgList.add("com.android.packageinstaller");// 应用程序包安装
			//pkgList.add("com.android.calendar");// 日历
			//pkgList.add("com.android.certinstaller");//  证书安装程序
			//
			//pkgList.add("com.android.launcher"); // 桌面启动器(定制手机(MX4) 不一定是以这个为桌面的)
			//pkgList.add("com.android.bluetooth"); // 蓝牙分享(需要手机开启过蓝牙才会激活这个进程)
			//pkgList.add("com.android.mms"); // 信息(不一定运行)
			//pkgList.add("com.android.browser"); // 浏览器 (不一定都有)
			//pkgList.add("com.android.contacts"); // 联系人(不一定在运行)
			//pkgList.add("com.android.shell"); // shell（不一定处于运行中）
			//pkgList.add("com.android.providers.downloads.ui"); // 下载管理器ui（实际测试不稳定，容易被杀）
			//pkgList.add("com.android.providers.downloads"); // 下载管理器（实际测试不稳定，容易被杀）

			int count = pkgList.size();
			int[] pidArray = new int[pkgList.size()];

			ActivityManager mActivityManager = Util_System_Service.getActivityManager(context);
			List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager.getRunningAppProcesses();

			if (appProcessList == null || appProcessList.size() < 2) {
				return null;
			}
			for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {

				// 如果已经检查完所有待检索的包名列表就提早结束
				if (count == 0) {
					break;
				}

				// 进程名，默认是包名或者由属性android：process=""指定
				String processName = appProcessInfo.processName;

				// 本进程下的所有包名列表
				for (String pkg : appProcessInfo.pkgList) {

					// 如果当前进程的包名列表中存在与待检索的包名列表中,就返回本进程
					if (pkgList.contains(pkg)) {

						// 特殊处理一下小米的手机
						// 小米上包名com.android.settings会被3个进程拥有，我们只获取和包名一致的进程名的pid
						if ("com.android.settings".equals(pkg)) {
							if (!"com.android.settings".equals(appProcessInfo.processName)) {
								break;
							}
						}
						pidArray[--count] = appProcessInfo.pid;
						if (DLog.isGlobalLog) {
							DLog.td(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, "收录包名：%s - 数组位置：%d - pid：%d", pkg,
									count, appProcessInfo.pid);
						}
						break;
					}
				}
			}

			// 由小到大排序pid
			Arrays.sort(pidArray);
			StringBuilder sb = new StringBuilder();
			for (int pid : pidArray) {
				// 这里如果pid为0就表示没有为这个数组的某个下标赋值pid，也就是说没有获取到某个包名的pid（部分深度定制的机子可能会出现这个问题）
				// 对于这些pid为0的就加入md5中
				if (pid == 0) {
					continue;
				}
				sb.append(pid);
			}
			mMd5SyatemPkgPid = Coder_Md5.md5(sb.toString());
			if (DLog.isGlobalLog) {
				DLog.td(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, "排序后pid：%s md5：%s", sb.toString(),
						mMd5SyatemPkgPid);
			}
			return mMd5SyatemPkgPid;
		} catch (Exception e) {
			if (DLog.isGlobalLog) {
				DLog.te(DLog.mGlobalTag, Global_Runtime_SystemInfo_Extend.class, e);
			}
		}
		return null;
	}

}
