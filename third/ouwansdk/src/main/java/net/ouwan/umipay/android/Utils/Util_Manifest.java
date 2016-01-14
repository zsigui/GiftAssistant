package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import net.ouwan.umipay.android.debug.Debug_Log;

public class Util_Manifest {

	private static Object readKey(Context context, String keyName) {

		try {
			ApplicationInfo appi = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			Bundle bundle = appi.metaData;
			return bundle.get(keyName);
		} catch (NameNotFoundException e) {
			Debug_Log.e(e);
			return null;
		}
	}

	public static int getInt(Context context, String keyName) throws Exception {
		Object obj = readKey(context, keyName);
		int ret;
		try {
			ret = (Integer) obj;
			return ret;
		}catch (Throwable e) {
			throw  new Exception("Get META_DATA failed (Integer)");
		}
	}

	public static int getInt(Context context, String keyName, int defVal) {
		try {
			int res = defVal;
			Object obj = readKey(context, keyName);
			if (obj instanceof Integer) {
				res = (Integer) obj;
			} else if (obj instanceof String) {
				res = Integer.valueOf((String) obj);
			}
			return res;
		} catch (Exception e) {
			Debug_Log.e(e);
			return defVal;
		}
	}

	public static String getString(Context context, String keyName, String defVal) {
		try {
			String res = defVal;
			Object obj = readKey(context, keyName);
			if (obj instanceof String) {
				res = (String) obj;
			} else if (obj instanceof Integer) {
				res = obj + "";
			} else if (obj instanceof Boolean) {
				res = obj + "";
			}
			return res;
		} catch (Exception e) {
			Debug_Log.e(e);
			return defVal;
		}
	}

	public static Boolean getBoolean(Context context, String keyName, boolean defVal) {
		try {
			Boolean res = defVal;
			Object obj = readKey(context, keyName);
			if (obj instanceof Boolean) {
				res = (Boolean) obj;
			} else if (obj instanceof String) {
				res = Boolean.valueOf((String) obj);
			}

			return res;
		} catch (Exception e) {
			Debug_Log.e(e);
			return defVal;
		}
	}

	public static Object get(Context context, String keyName) {
		return readKey(context, keyName);
	}

}
