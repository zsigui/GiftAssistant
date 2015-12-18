package net.youmi.android.libs.common.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import net.youmi.android.libs.common.debug.DLog;

public class Util_System_Phone {

	public static boolean isNetworkConnected(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager != null) {
				if (telephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
					return true;
				}
			}

		} catch (Throwable e) {
			if (DLog.isUtilLog) {
				DLog.te(DLog.mUtilTag, Util_System_Phone.class, e);
			}
		}
		return false;

	}
}
