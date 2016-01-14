package net.ouwan.umipay.android.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import net.ouwan.umipay.android.debug.Debug_Log;

/**
 * Util_Resource
 *
 * @author zacklpx
 *         date 15-3-6
 *         description
 */
public class Util_Resource {
	public static int getIdByReflection(Context c, String className, String fieldName) {
		try {
			Resources resources = c.getResources();
			String packageName = c.getPackageName();
			int id = resources.getIdentifier(fieldName, className, packageName);
			if (id == 0) {
				String msg = new StringBuilder("resource not found, ").append(fieldName).toString();
				Debug_Log.d(msg);
			}
			return id;
		} catch (Exception e) {
			String msg = new StringBuilder("resource not found, ").append(e.getMessage()).toString();
			Debug_Log.e(msg);
			Toast toast = Toast.makeText(c, msg,
					Toast.LENGTH_SHORT
			);
			toast.setGravity(17, 0, 150);
			toast.show();
			return 0;
		}
	}
}
