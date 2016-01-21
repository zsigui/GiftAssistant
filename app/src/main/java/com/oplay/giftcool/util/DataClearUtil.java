package com.oplay.giftcool.util;

import android.content.Context;
import android.os.Environment;

import net.youmi.android.libs.common.util.Util_System_File;

import java.io.File;

/**
 * Created by zsigui on 16-1-6.
 */
public class DataClearUtil {

	/**
	 * 清除本应用SharedPreference(/data/data/com.xxx.xxx/shared_prefs)
	 */
	public static void cleanSharedPreference(Context context) {
		Util_System_File.delete(new File("/data/data/" + context.getPackageName() + "/shared_prefs"));
	}

	/**
	 * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
	 */
	public static void cleanDatabase(Context context) {
		Util_System_File.delete(new File("/data/data/" + context.getPackageName() + "/databases"));
	}

	/**
	 * 清除内部缓存文件夹
	 */
	public static void cleanInternalCache(Context context) {
		Util_System_File.delete(context.getCacheDir());
	}

	/**
	 * 清除外部缓存文件夹
	 */
	public static void cleanExternalCache(Context context) {
		if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) {
			Util_System_File.delete(context.getExternalCacheDir());
		}
	}

	/**
	 * 清除/data/data/com.xxx.xxx/files下的内容
	 */
	public static void cleanFiles(Context context) {
		Util_System_File.delete(context.getFilesDir());
	}

	/**
	 * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除
	 */
	public static void cleanCustomCache(String filePath) {
		Util_System_File.delete(new File(filePath));
	}
}
