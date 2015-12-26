package net.youmi.android.libs.platform.debug;

import android.util.Log;

import net.youmi.android.libs.common.debug.BasicLog;
import net.youmi.android.libs.platform.PlatformConstant;

/**
 * 本类主要用于打印公开给开发者查看问题的log（默认开启），使用默认标签（广告这边为：YoumiSdk），同时提供一个开关给开发者屏蔽这个log
 *
 * @author zhitaocai
 */
public class Debug_AdLog extends BasicLog {

	private static String mTag = PlatformConstant.get_PlKey_YoumiSdk();// YoumiSdk

	public static void setAdLogEnabled(boolean flag) {
		mIsEnableLog = flag;
	}

	// INFO(Default Tag)
	public static void i(String msg) {
		if (mIsEnableLog) {
			printLog(Log.INFO, mTag, null, msg);
		}
	}

	public static void i(String fmt, Object... args) {
		if (mIsEnableLog) {
			printLog(Log.INFO, mTag, null, fmt, args);
		}
	}

	public static void i(String msg, Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.INFO, mTag, tr, msg);
		}
	}

	public static void i(Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.INFO, mTag, tr, null);
		}
	}

	// ERROR(Default Tag)
	public static void e(String msg) {
		if (mIsEnableLog) {
			printLog(Log.ERROR, mTag, null, msg);
		}
	}

	public static void e(String fmt, Object... args) {
		if (mIsEnableLog) {
			printLog(Log.ERROR, mTag, null, fmt, args);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.ERROR, mTag, tr, msg);
		}
	}

	public static void e(Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.ERROR, mTag, tr, null);
		}
	}

	// WARN(Default Tag)
	public static void w(String msg) {
		if (mIsEnableLog) {
			printLog(Log.WARN, mTag, null, msg);
		}
	}

	public static void w(String fmt, Object... args) {
		if (mIsEnableLog) {
			printLog(Log.WARN, mTag, null, fmt, args);
		}
	}

	public static void w(String msg, Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.WARN, mTag, tr, msg);
		}
	}

	public static void w(Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.WARN, mTag, tr, null);
		}
	}

	// DEBUG(Default Tag)
	public static void d(String msg) {
		if (mIsEnableLog) {
			printLog(Log.DEBUG, mTag, null, msg);
		}
	}

	public static void d(String fmt, Object... args) {
		if (mIsEnableLog) {
			printLog(Log.DEBUG, mTag, null, fmt, args);
		}
	}

	public static void d(String msg, Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.DEBUG, mTag, tr, msg);
		}
	}

	public static void d(Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.DEBUG, mTag, tr, null);
		}
	}

	// VERBOSE(Default Tag)
	public static void v(String msg) {
		if (mIsEnableLog) {
			printLog(Log.VERBOSE, mTag, null, msg);
		}
	}

	public static void v(String fmt, Object... args) {
		if (mIsEnableLog) {
			printLog(Log.VERBOSE, mTag, null, fmt, args);
		}
	}

	public static void v(String msg, Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.VERBOSE, mTag, tr, msg);
		}
	}

	public static void v(Throwable tr) {
		if (mIsEnableLog) {
			printLog(Log.VERBOSE, mTag, tr, null);
		}
	}
}
