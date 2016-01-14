package net.ouwan.umipay.android.debug;

import android.util.Log;

import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.config.SDKDebugConfig;
import net.youmi.android.libs.common.debug.BasicLog;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

/**
 * Debug_Log
 *
 * @author zacklpx
 *         date 15-1-28
 *         description
 */
public class Debug_Log extends BasicLog {

	private static String mTag = createDefaultTag();

	private static String createDefaultTag() {
		try {
			return Coder_SDKPswCoder.decode(ConstantString.LOGTAG, ConstantString.LOGTAG_KEY);
		} catch (Throwable ignored) {

		}
		return "Test";
	}

	private static boolean mIsEnableDebug = true;

	public static void setDebugEnable(boolean flag) {
		mIsEnableDebug = flag;
	}


	// INFO(Default Tag)
	public static void i(String msg) {
		if (mIsEnableDebug) {
			printLog(Log.INFO, mTag, null, msg);
		}
	}

	public static void i(String fmt, Object... args) {
		if (mIsEnableDebug) {
			printLog(Log.INFO, mTag, null, fmt, args);
		}
	}

	public static void i(String msg, Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.INFO, mTag, tr, msg);
		}
	}

	public static void i(Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.INFO, mTag, tr, null);
		}
	}


	// INFO(Default Tag)
	public static void di(String msg) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.INFO, mTag, null, msg);
		}
	}

	public static void di(String fmt, Object... args) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.INFO, mTag, null, fmt, args);
		}
	}

	public static void di(String msg, Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.INFO, mTag, tr, msg);
		}
	}

	public static void di(Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.INFO, mTag, tr, null);
		}
	}

	// ERROR(Default Tag)
	public static void e(String msg) {
		if (mIsEnableDebug) {
			printLog(Log.ERROR, mTag, null, msg);
		}
	}

	public static void e(String fmt, Object... args) {
		if (mIsEnableDebug) {
			printLog(Log.ERROR, mTag, null, fmt, args);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.ERROR, mTag, tr, msg);
		}
	}

	public static void e(Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.ERROR, mTag, tr, null);
		}
	}


	// ERROR(Default Tag)
	public static void de(String msg) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.ERROR, mTag, null, msg);
		}
	}

	public static void de(String fmt, Object... args) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.ERROR, mTag, null, fmt, args);
		}
	}

	public static void de(String msg, Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.ERROR, mTag, tr, msg);
		}
	}

	public static void de(Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.ERROR, mTag, tr, null);
		}
	}

	// WARN(Default Tag)
	public static void w(String msg) {
		if (mIsEnableDebug) {
			printLog(Log.WARN, mTag, null, msg);
		}
	}

	public static void w(String fmt, Object... args) {
		if (mIsEnableDebug) {
			printLog(Log.WARN, mTag, null, fmt, args);
		}
	}

	public static void w(String msg, Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.WARN, mTag, tr, msg);
		}
	}

	public static void w(Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.WARN, mTag, tr, null);
		}
	}


	// WARN(Default Tag)
	public static void dw(String msg) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.WARN, mTag, null, msg);
		}
	}

	public static void dw(String fmt, Object... args) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.WARN, mTag, null, fmt, args);
		}
	}

	public static void dw(String msg, Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.WARN, mTag, tr, msg);
		}
	}

	public static void dw(Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.WARN, mTag, tr, null);
		}
	}

	// DEBUG(Default Tag)
	public static void d(String msg) {
		if (mIsEnableDebug) {
			printLog(Log.DEBUG, mTag, null, msg);
		}
	}

	public static void d(String fmt, Object... args) {
		if (mIsEnableDebug) {
			printLog(Log.DEBUG, mTag, null, fmt, args);
		}
	}

	public static void d(String msg, Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.DEBUG, mTag, tr, msg);
		}
	}

	public static void d(Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.DEBUG, mTag, tr, null);
		}
	}

	// DEBUG(Default Tag)
	public static void dd(String msg) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.DEBUG, mTag, null, msg);
		}
	}

	public static void dd(String fmt, Object... args) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.DEBUG, mTag, null, fmt, args);
		}
	}

	public static void dd(String msg, Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.DEBUG, mTag, tr, msg);
		}
	}

	public static void dd(Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.DEBUG, mTag, tr, null);
		}
	}

	// VERBOSE(Default Tag)
	public static void v(String msg) {
		if (mIsEnableDebug) {
			printLog(Log.VERBOSE, mTag, null, msg);
		}
	}

	public static void v(String fmt, Object... args) {
		if (mIsEnableDebug) {
			printLog(Log.VERBOSE, mTag, null, fmt, args);
		}
	}

	public static void v(String msg, Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.VERBOSE, mTag, tr, msg);
		}
	}

	public static void v(Throwable tr) {
		if (mIsEnableDebug) {
			printLog(Log.VERBOSE, mTag, tr, null);
		}
	}

	// VERBOSE(Default Tag)
	public static void dv(String msg) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.VERBOSE, mTag, null, msg);
		}
	}

	public static void dv(String fmt, Object... args) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.VERBOSE, mTag, null, fmt, args);
		}
	}

	public static void dv(String msg, Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.VERBOSE, mTag, tr, msg);
		}
	}

	public static void dv(Throwable tr) {
		if (!SDKDebugConfig.isRelease) {
			printLog(Log.VERBOSE, mTag, tr, null);
		}
	}
}
