package com.oplay.giftcool.util;

import android.content.Context;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_File;
import net.youmi.android.libs.common.util.Util_System_Package;

import java.io.File;

/**
 * InstallAppUtil
 *
 * @author zacklpx
 *         date 16-1-17
 *         description
 */
public class InstallAppUtil {

	public static void install(Context context, GameDownloadInfo appInfo) {
		try {
			File destFile = appInfo.getDestFile();
			String destFilePath = appInfo.getDestFilePath();

			if (destFile == null || !destFile.exists()) {
				return;
			}
			if (checkIfDataDir(destFilePath)) {
				Util_System_File.chmod(destFile, "777");
			}

			Util_System_Package.InstallApkByFilePath(context, destFilePath);
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public static boolean checkIfDataDir(String dirPath) {
		boolean res = false;
		String prefix = dirPath.substring(0, 10);
		if (prefix.equals("/data/data")) {
			res = true;
		}
		return res;
	}

}
