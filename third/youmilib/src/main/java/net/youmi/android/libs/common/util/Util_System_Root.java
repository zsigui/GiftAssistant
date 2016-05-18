package net.youmi.android.libs.common.util;

import java.io.File;

public class Util_System_Root {

	/**
	 * 判断当前手机是否有ROOT权限
	 *
	 * @return
	 */
	public static boolean isRoot() {
		boolean bool = false;

		try {
			if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
				bool = false;
			} else {
				bool = true;
			}
		} catch (Exception e) {

		}
		return bool;
	}

}
