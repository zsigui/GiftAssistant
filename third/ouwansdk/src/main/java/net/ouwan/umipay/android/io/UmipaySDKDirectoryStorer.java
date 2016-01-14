package net.ouwan.umipay.android.io;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import net.ouwan.umipay.android.config.ConstantString;
import net.ouwan.umipay.android.debug.Debug_Log;
import net.youmi.android.libs.common.download.filestorer.FileCacheDirectoryStorer;
import net.youmi.android.libs.platform.coder.Coder_SDKPswCoder;

import java.io.File;
import java.io.IOException;


/**
 * Created by liangpeixing on 14-2-25.
 */
public class UmipaySDKDirectoryStorer extends FileCacheDirectoryStorer {

	private static UmipaySDKDirectoryStorer mCacheFileStorer;

	private static UmipaySDKDirectoryStorer mPublicFileStorer;

	public UmipaySDKDirectoryStorer(File directory, long dirLimtMaxSize, long perFileLimtMaxTimeMillSecond) throws
			IOException {
		super(directory, dirLimtMaxSize, perFileLimtMaxTimeMillSecond);
	}

	private static String createCacheDirName() {
		try {
			return Coder_SDKPswCoder.decode(ConstantString.CACHE_DIR, ConstantString.CACHE_DIR_KEY);
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return ".cache";
	}

	private static File getUmipaySdcardDirectory(String dirName) {

		try {
			String path = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/Android/data/" + createCacheDirName() + "/" + dirName;
			Debug_Log.dd("path is %s", path);
			return new File(path);

		} catch (Throwable e) {
			Debug_Log.e(e);
		}
		return null;
	}

	public static UmipaySDKDirectoryStorer getCacheFileStorer(Context context) {
		if (mCacheFileStorer == null) {
			try {
				String packageName = context.getPackageName();
				if (TextUtils.isEmpty(packageName)) {
					packageName = "default";
				}
				mCacheFileStorer = new UmipaySDKDirectoryStorer(
						getUmipaySdcardDirectory("." + packageName),
						UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);// 无限
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return mCacheFileStorer;
	}

	public static UmipaySDKDirectoryStorer getPublicFileStorer(Context context) {
		if (mPublicFileStorer == null) {
			try {
				mPublicFileStorer = new UmipaySDKDirectoryStorer(getUmipaySdcardDirectory(""), UN_LIMT_STORE_SIZE,
						UN_LIMT_STORE_TIME);
			} catch (Throwable e) {
				Debug_Log.e(e);
			}
		}
		return mPublicFileStorer;
	}
}
