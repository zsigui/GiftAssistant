package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.coder.Coder_Md5;

/**
 * Utils_Package
 *
 * @author zacklpx
 *         date 15-1-30
 *         description
 */
public class Util_Package {

	public static String getPackageSignature(Context context) {
		try {
			Signature[] sign = context.getPackageManager().getPackageInfo(context.getPackageName(),
					PackageManager.GET_SIGNATURES).signatures;
			return Coder_Md5.md5(sign[0].toByteArray());
		} catch (Throwable e) {
			Debug_Log.e(e);
			return null;
		}
	}
}
