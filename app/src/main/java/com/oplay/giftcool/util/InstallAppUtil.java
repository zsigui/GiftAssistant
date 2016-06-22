package com.oplay.giftcool.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.model.data.resp.GameDownloadInfo;

import net.youmi.android.libs.common.util.Util_System_File;
import net.youmi.android.libs.common.util.Util_System_Package;

import java.io.File;
import java.util.List;

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
                Util_System_File.chmod(destFile.getParentFile(), "701");
                Util_System_File.chmod(destFile, "604");
            }

            Util_System_Package.InstallApkByFilePath(context, destFilePath);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_UTIL, e);
        }
    }

    public static boolean checkIfDataDir(String dirPath) {
        boolean res = false;
        if (dirPath.startsWith("/data/")) {
            res = true;
        }
        return res;
    }

    public static boolean isAppInstalled(Context context, String qqPackageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(qqPackageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
