package net.youmi.android.libs.common.v2.download.storer;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.util.Util_System_File;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 创建一个带有自动清理功能的文件缓存目录
 * <p/>
 * 不建议纳入到每个下载线程的下载之前执行，而应该在第一个下载任务线程处理之前执行
 *
 * @author zhitaocai edit on 2015-09-19
 */
public abstract class AbsDownloadDir {

	/**
	 * 不限制所有文件的缓存 总体积
	 */
	public static final long UN_LIMT_STORE_SIZE = -1;

	/**
	 * 不限制每个文件的缓存时间
	 */
	public static final long UN_LIMT_STORE_TIME = -1;

	/**
	 * 每个文件缓存的时间
	 */
	private long mDirPerFileCacheTime_ms = UN_LIMT_STORE_TIME;

	/**
	 * 所有缓存文件的总体积
	 */
	private long mDirMaxCacheSize_KB = UN_LIMT_STORE_SIZE;

	/**
	 * 文件缓存目录
	 */
	private File mCacheDir;

	/**
	 * 初始化
	 *
	 * @param dir                    目录位置
	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
	 */
	public AbsDownloadDir(File dir, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms)
			throws IOException, IllegalArgumentException {

		mCacheDir = dir;
		if (mCacheDir == null) {
			throw new IllegalArgumentException("directory must not be null");
		}

		if (mCacheDir.exists() && !mCacheDir.isDirectory()) {
			throw new IOException("please set a file cache directory");
		}

		// 检查文件夹是否存在，如果不存在，重新建立文件夹
		if (!mCacheDir.exists() && !mCacheDir.mkdirs()) {
			throw new IOException("mkdirs failed");
		}

		if (dirMaxCacheSize_KB <= 0 && dirMaxCacheSize_KB != UN_LIMT_STORE_SIZE) {
			throw new IllegalArgumentException("directory max size param error");
		}

		if (dirPerFileCacheTime_ms <= 0 && dirPerFileCacheTime_ms != UN_LIMT_STORE_SIZE) {
			throw new IllegalArgumentException("file cache time out param error");
		}

		mDirMaxCacheSize_KB = dirMaxCacheSize_KB;
		mDirPerFileCacheTime_ms = dirPerFileCacheTime_ms;
	}

	/**
	 * 初始化
	 *
	 * @param dirPath                目录路径地址
	 * @param dirMaxCacheSize_KB     全部文件的最大限制大小 (KB) 传入{@link #UN_LIMT_STORE_SIZE} 标识不限制目录缓存总体积
	 * @param dirPerFileCacheTime_ms 每个文件的缓存时间 (ms) 传入{@link #UN_LIMT_STORE_TIME} 标识不限制每个文件的缓存时间
	 */
	public AbsDownloadDir(String dirPath, long dirMaxCacheSize_KB, long dirPerFileCacheTime_ms)
			throws IOException, IllegalArgumentException {
		this(new File(dirPath), dirMaxCacheSize_KB, dirPerFileCacheTime_ms);
	}

	/**
	 * new一个下载的缓存文件
	 * <p/>
	 * 目录下文件命名规范：如new File(md5(url));
	 *
	 * @param url      原始下载url(每个下载任务的默认唯一标识)
	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
	 *
	 * @return
	 */
	public abstract File newDownloadTempFile(String url, String identify);

	/**
	 * new一个下载的最终文件
	 *
	 * @param url      原始下载url(每个下载任务的默认唯一标识)
	 * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
	 *
	 * @return
	 */
	public abstract File newDownloadStoreFile(String url, String identify);

	/**
	 * 获取下载文件目录
	 *
	 * @return
	 */
	public File getDir() {
		return mCacheDir;
	}

	/**
	 * 优化传入来的目录
	 * <p/>
	 * <ol>
	 * <li>根据是否设置了缓存时间，将已经超过缓存时间的文件进行删除</li>
	 * <li>检查当前目录的总体积是否还是超过最大限制大小，如果是的话，在根据文件的最后编辑时间排序，将最久的文件删除，直至当前目录总体积小于限制体积</li>
	 * </ol>
	 * <p/>
	 * <p/>
	 * 目前有个取舍的问题:
	 * 为了防止删除太久，会设置一个定时器10000，也就是说如果目录里面有超过10000个文件的话，那么最多可能删除10000个文件，
	 * 如果删除了10000个文件还是不能满足最大限制要求，那么也会停止
	 * <p/>
	 *
	 * @return
	 */
	public boolean optDir() {
		// 如果不限制的话，那么就返回已经优化成功
		if (mDirMaxCacheSize_KB == UN_LIMT_STORE_SIZE && mDirPerFileCacheTime_ms == UN_LIMT_STORE_TIME) {
			return true;
		}
		try {
			// 获取当前目录的所有文件列表
			File[] files = mCacheDir.listFiles();
			if (files == null || files.length == 0) {
				return true;
			}

			// 所有文件的总长度
			long countLen = 0;

			// 用于收集还没有超过缓存时间的文件待排序列表
			List<File> fileList = new ArrayList<File>();

			// 将已经超过缓存时间的文件删除，同时收集剩下的文件
			for (File file : files) {
				if (file == null || !file.exists()) {
					continue;
				}

				// 1. 先检查有没有超期了
				if (isFileTimeOut(file)) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "文件[%s]已经过期,准备删除", file.getAbsolutePath());
					}
					// 文件超期就删除
					boolean result = Util_System_File.delete(file);
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "文件[%s]删除%s", file.getAbsolutePath(), result ? "成功" : "失败");
					}
					continue;
				}

				// 2. 如果这个目录设置了总体积限制，那么就要收集没有超级的文件
				// 只有需要检查目录大小的情况下才需要计算文件大小，以及把文件放入待排序列表中
				if (mDirMaxCacheSize_KB != UN_LIMT_STORE_SIZE) {
					countLen += file.length();// 文件未超期或未被删除，就用来检查总容量
					fileList.add(file);// 加入到待排序列表中
				}
			}

			// 按lastModify进行，从旧到新
			Collections.sort(fileList, new FileLastModifyCom());// 这里需要添加排序算法

			// 使用链接将文件进行排序，文件比较旧的排在前面，如果超过目录缓存的总大小，删除排在前面的文件。
			Iterator<File> iterator = fileList.iterator();
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.td(Debug_SDK.mDownloadTag, this, "准备删除旧的但未过时的文件");
			}

			int a = 10000;

			// 如果剩余文件长度大于限制长度，就需要不断循环删除
			while (countLen > mDirMaxCacheSize_KB && iterator.hasNext()) {
				try {
					File gfFile = iterator.next();

					// ！！！！下面两句代码必须按照的现在的执行顺序进行，不能调乱顺序执行，否则如果按照这个顺序执行的话就会被ESET扫描出来
					iterator.remove();
					countLen -= gfFile.length();

					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "准备删除旧的但未过时的文件[%s]", gfFile.getPath());
					}
					// 删除旧的文件
					boolean isSuccess = Util_System_File.delete(gfFile);
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "%s删除旧的但未过时的文件[%s]删除%s", isSuccess ? "成功" : "失败",
								gfFile.getAbsolutePath());
					}
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
				--a;
				if (a < 0) {
					break;// 防止死循环
				}
			}
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 检查当前目录的指定文件是否已经超过缓存时间
	 *
	 * @param file
	 *
	 * @return
	 */
	private boolean isFileTimeOut(File file) {
		if (file == null) {
			return false;
		}

		if (mDirPerFileCacheTime_ms == UN_LIMT_STORE_TIME) {
			return false;
		}

		if (System.currentTimeMillis() - file.lastModified() > mDirPerFileCacheTime_ms) {
			return true;
		}

		return false;
	}

	class FileLastModifyCom implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			try {
				if (lhs.lastModified() < rhs.lastModified()) {
					return -1;
				}
				return 1;
			} catch (Throwable e) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				}

			}
			return 0;
		}

	}

	//	/**
	//	 * 获取在文件夹里面的文件列表
	//	 *
	//	 * @return
	//	 */
	//	public File[] getFilesInDir() {
	//		return mCacheDir.listFiles();
	//	}
	//
	//	/**
	//	 * 获取在目录下的文件名列表
	//	 *
	//	 * @return
	//	 */
	//	public String[] getFileNamesInDir() {
	//		return mCacheDir.list();
	//	}
	//
	//
	//	/**
	//	 * 根据文件名获取在该目录下的完整路径
	//	 *
	//	 * @param fileName
	//	 *
	//	 * @return
	//	 */
	//	public String getFilePathInDirByFileName(String fileName) {
	//		return mCacheDir.getAbsolutePath() + "/" + fileName;
	//	}
	//
	//	/**
	//	 * 根据文件名获取在该目录下的完整url路径
	//	 *
	//	 * @param fileName
	//	 *
	//	 * @return
	//	 */
	//	public String getFileUrlInDirByFileName(String fileName) {
	//		return "file://" + mCacheDir.getAbsolutePath() + "/" + fileName;
	//	}
	//
	//	/**
	//	 * 获取子目录
	//	 *
	//	 * @param dirName
	//	 *
	//	 * @return
	//	 */
	//	public File getSubDirectory(String dirName) {
	//		return getFileByFileName(dirName);
	//	}
	//
	//
	//	/**
	//	 * 根据指定的fileName，返回完整路径的File
	//	 *
	//	 * @param fileName
	//	 *
	//	 * @return
	//	 */
	//	public File getFileByFileName(String fileName) {
	//		try {
	//			String filePath = getFilePathInDirByFileName(fileName);
	//			return new File(filePath);
	//		} catch (Throwable e) {
	//		}
	//		return null;
	//	}
	//
	//	/**
	//	 * 删除目录内指定fileName的文件
	//	 *
	//	 * @param fileName
	//	 *
	//	 * @return
	//	 */
	//	public boolean deleteFileByFileName(String fileName) {
	//		try {
	//			File file = getFileByFileName(fileName);
	//			if (file != null) {
	//				if (file.exists()) {
	//					return file.delete();
	//				}
	//			}
	//			return true;
	//		} catch (Throwable e) {
	//			if (Debug_SDK.isDownloadLog) {
	//				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
	//			}
	//		}
	//		return false;
	//	}
	//
	//	/**
	//	 * 检查目标文件是否存在于缓存目录中
	//	 *
	//	 * @param fileName 目标文件名,格式为 "缓存目录/fileName"
	//	 *
	//	 * @return
	//	 */
	//	public boolean isFileExistInDirectory(String fileName) {
	//		File file = getFileByFileName(fileName);
	//		if (file.exists()) {
	//			// return file.canRead();//这里需要再考虑一下
	//			return true;
	//		}
	//		return false;
	//	}

}
