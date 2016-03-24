package com.oplay.giftcool.download;

import android.content.Context;
import android.os.Environment;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.util.Util_System_SDCard_Util;
import net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * ApkDownloadDir
 *
 * @author zacklpx
 *         date 16-1-5
 *         description
 */
public class ApkDownloadDir extends AbsDownloadDir {
	public static final String DIR_DOWNLOAD = "/gift_cool/download";
	private static ApkDownloadDir mSdcardFileDirInstance;
	private static ApkDownloadDir mDataFileDirInstance;

	/**
	 * 初始化
	 *
	 * @param dir                    目录位置
	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
	 */
	public ApkDownloadDir(File dir, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms) throws IOException,
			IllegalArgumentException {
		super(dir, dirMaxCacheSize_KB, dirPerFileCacheTime_ms);
	}

	/**
	 * 初始化
	 *
	 * @param dirPath                目录路径地址
	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
	 */
	public ApkDownloadDir(String dirPath, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms) throws IOException,
			IllegalArgumentException {
		super(dirPath, dirMaxCacheSize_KB, dirPerFileCacheTime_ms);
	}

	/**
	 * 获取APK下载目录:
	 * 1.检查SDK卡是否可用,可用返回/gift_cool/download
	 * 2.不可用则使用files/
	 * 3.仍不可用返回null
	 *
	 * @param context
	 * @return
	 */
	public static ApkDownloadDir getInstance(Context context) {
		if (Util_System_SDCard_Util.IsSdCardCanWrite(context)) {
			return getSdcardFileDirInstance();
		}
		return getDataFileDirInstance(context);
	}

	private static synchronized ApkDownloadDir getSdcardFileDirInstance() {
		try {
			if (mSdcardFileDirInstance == null) {
				final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DIR_DOWNLOAD);
				mSdcardFileDirInstance = new ApkDownloadDir(dir, UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);
			}
			return mSdcardFileDirInstance;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return mDataFileDirInstance;
	}

	private static synchronized ApkDownloadDir getDataFileDirInstance(Context context) {
		try {
			if (mDataFileDirInstance == null) {
				final File dir = new File(context.getFilesDir().getAbsolutePath());
				mDataFileDirInstance = new ApkDownloadDir(dir, UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);
			}
			return mDataFileDirInstance;
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return mDataFileDirInstance;
	}

	/**
	 * new一个下载的缓存文件
	 * <p/>
	 * 目录下文件命名规范：有唯一标识identify时用MD5(identify)作文件名
	 * 无唯一表示用下载url解析的文件名作文件名
	 * 解析失败用MD5(下载url)作文件名
	 * 临时文件名后缀.vmtf
	 *
	 * @param url      原始下载url(每个下载任务的默认唯一标识)
	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
	 * @return
	 */
	@Override
	public File newDownloadTempFile(String url, String identify) {
		String temp;
		if (!Basic_StringUtil.isNullOrEmpty(identify)) {
			final String decodedUrl;
			try {
				decodedUrl = URLDecoder.decode(identify, HTTP.UTF_8);
				final int start = decodedUrl.lastIndexOf(File.separatorChar) + 1;
				final int end = decodedUrl.lastIndexOf('.');
				temp = decodedUrl.substring(start, end);
			} catch (UnsupportedEncodingException e) {
				KLog.e(e);
				temp = Coder_Md5.md5(identify);
			}
		} else {
			temp = Coder_Md5.md5(url);
		}
		return new File(getDir(), temp + ".vmtf");
	}

	/**
	 * new一个下载的最终文件
	 * <p/>
	 * 目录下文件命名规范：有唯一标识identify时用MD5(identify)作文件名
	 * 无唯一表示用下载url解析的文件名作文件名
	 * 解析失败用MD5(下载url)作文件名
	 *
	 * @param url      原始下载url(每个下载任务的默认唯一标识)
	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
	 * @return
	 */
	@Override
	public File newDownloadStoreFile(String url, String identify) {
		String temp;
		if (!Basic_StringUtil.isNullOrEmpty(identify)) {
			final String decodedUrl;
			try {
				decodedUrl = URLDecoder.decode(identify, HTTP.UTF_8);
				final int start = decodedUrl.lastIndexOf(File.separatorChar) + 1;
				final int end = decodedUrl.lastIndexOf('.');
				temp = decodedUrl.substring(start, end);
			} catch (UnsupportedEncodingException e) {
				KLog.e(e);
				temp = Coder_Md5.md5(identify);
			}
		} else {
			temp = Coder_Md5.md5(url);
		}
		return new File(getDir(), temp + ".apk");
	}
}
