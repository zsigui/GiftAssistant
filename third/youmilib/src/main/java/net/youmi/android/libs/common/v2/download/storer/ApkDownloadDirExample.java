package net.youmi.android.libs.common.v2.download.storer;//package net.youmi.android.net.youmi.android.libs.common.v2
// .download.storer;
//
//import android.os.Environment;
//
//import net.youmi.android.net.youmi.android.libs.common.basic.Basic_StringUtil;
//import net.youmi.android.net.youmi.android.libs.common.coder.Coder_Md5;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * 一个下载目录的例子
// *
// * @author zhitao
// * @since 2015-09-21 11:23
// */
//public class ApkDownloadDirExample extends AbsDownloadDir {
//
//	/**
//	 * 初始化
//	 *
//	 * @param dir                    目录位置
//	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
//	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
//	 */
//	protected ApkDownloadDirExample(File dir, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms)
//			throws IOException, IllegalArgumentException {
//		super(dir, dirMaxCacheSize_KB, dirPerFileCacheTime_ms);
//	}
//
//	/**
//	 * 初始化
//	 *
//	 * @param dirPath
//	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
//	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
//	 */
//	protected ApkDownloadDirExample(String dirPath, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms)
//			throws IOException, IllegalArgumentException {
//		super(dirPath, dirMaxCacheSize_KB, dirPerFileCacheTime_ms);
//	}
//
//	public ApkDownloadDirExample() throws IOException, IllegalArgumentException {
//		// 实际不建议这么简单写，这里只是一个例子
//		super(new File(Environment.getExternalStorageDirectory(), ".123"), UN_LIMT_STORE_SIZE, UN_LIMT_STORE_TIME);
//	}
//
//	/**
//	 * new一个下载的缓存文件
//	 * <p/>
//	 * 目录下文件命名规范：如new File(md5(url));
//	 *
//	 * @param url      原始下载url(每个下载任务的默认唯一标识)
//	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
//	 *
//	 * @return
//	 */
//	@Override
//	public File newDownloadTempFile(String url, String identify) {
//		String temp;
//		if (!Basic_StringUtil.isNullOrEmpty(identify)) {
//			temp = identify;
//		} else {
//			temp = url;
//		}
//		return new File(getDir(), Coder_Md5.md5(temp) + ".y".trim() + "mt".trim() + "f");
//	}
//
//	/**
//	 * new一个下载的最终文件
//	 *
//	 * @param url      原始下载url(每个下载任务的默认唯一标识)
//	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
//	 *
//	 * @return
//	 */
//	@Override
//	public File newDownloadStoreFile(String url, String identify) {
//		String temp;
//		if (!Basic_StringUtil.isNullOrEmpty(identify)) {
//			temp = identify;
//		} else {
//			temp = url;
//		}
//		return new File(getDir(), Coder_Md5.md5(temp));
//	}
//
//}
