package com.oplay.giftassistant.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsigui on 15-12-18.
 */
public class SystemUtil {

	public static List<String> getInstalledAppName(Context context) {
		List<String> data = new ArrayList<>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int i=0; i<packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			data.add(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
		}
		return data;
	}

	public static int getVerCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
