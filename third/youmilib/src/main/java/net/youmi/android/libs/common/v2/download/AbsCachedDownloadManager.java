package net.youmi.android.libs.common.v2.download;

import android.content.Context;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;
import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.download.core.AbsDownloader;
import net.youmi.android.libs.common.v2.download.listener.IDownloadListener;
import net.youmi.android.libs.common.v2.download.listener.IFileAvailableChecker;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir;
import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;
import net.youmi.android.libs.common.v2.pool.core.IExecuteListener;

import java.io.IOException;
import java.util.HashMap;

/**
 * cache类型的下载管理器，子类建议采用单例使用
 *
 * @author zhitao
 * @since 2015-09-20 14:04
 */
public abstract class AbsCachedDownloadManager implements IFileAvailableChecker, IExecuteListener {

	protected Context mApplicationContext;

	/**
	 * 是否为首次下载，如果是首次下载的话，那么需要优化一下下载目录空间
	 */
	private boolean isFirstDownload = true;

	/**
	 * 记录正在执行的下载实体类
	 */
	private HashMap<FileDownloadTask, AbsDownloader> mTask_Downloader;

	/**
	 * 一个带有自定义规则的下载目录(如：目录是否会自动清理，目录下的文件命名规范等)
	 */
	protected AbsDownloadDir mAbsDownloadDir;

	/**
	 * 本下载管理器锁采用的下载线程池
	 */
	private AbsCacheExecutorService mAbsCacheExecutorService;

	/**
	 * 下载监听观察者管理器
	 */
	private AbsDownloadNotifier mAbsDownloadNotifier;

	protected AbsCachedDownloadManager(Context context) throws NullPointerException, IOException {
		mApplicationContext = context.getApplicationContext();
		mTask_Downloader = new HashMap<FileDownloadTask, AbsDownloader>();

		// 设置目录
		mAbsDownloadDir = newDownloadDir();

		//　设置下载任务的观察者监听管理器
		mAbsDownloadNotifier = newDownloadNotifier();

		// 1. 创建线程池
		// 2. 设置线程池中每个任务执行前后需要做的逻辑
		mAbsCacheExecutorService = newAbsCacheExecutorService();
		mAbsCacheExecutorService.setIExecuteListener(this);

	}

	/**
	 * 子类new一个下载的最终目录,{@link net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir}是一个带有自定义规则的下载目录(如：目录是否会自动清理，目录下的文件命名规范等)
	 *
	 * @return
	 *
	 * @throws java.io.IOException
	 */
	public abstract AbsDownloadDir newDownloadDir() throws IOException;

	/**
	 * 子类new一个下载任务的观察者监听管理器
	 *
	 * @return
	 */
	public abstract AbsDownloadNotifier newDownloadNotifier();

	/**
	 * 子类new一个具体使用的Cache类型线程池
	 *
	 * @return
	 */
	public abstract AbsCacheExecutorService newAbsCacheExecutorService();

	/**
	 * 子类new一个下载的实体类，该类继承自 {@link net.youmi.android.libs.common.v2.download.core.AbsDownloader}
	 *
	 * @param context
	 * @param absDownloadDir        一个带有自定义规则的下载目录(如：目录是否会自动清理，目录下的文件命名规范等)
	 * @param fileDownloadTask      下载任务描述数据模型
	 * @param absDownloadNotifier   下载状态监听观察者管理器
	 * @param iFileAvailableChecker 任务下载完成后的检查器，主要用于检查下载完成的文件是否有效
	 *
	 * @return
	 */
	public abstract AbsDownloader newDownloader(Context context, AbsDownloadDir absDownloadDir, FileDownloadTask
			fileDownloadTask,
			AbsDownloadNotifier absDownloadNotifier, IFileAvailableChecker iFileAvailableChecker)
			throws NullPointerException, IOException;

	/**
	 * 返回下载目录
	 *
	 * @return
	 */
	public AbsDownloadDir getDownloadDir() {
		return mAbsDownloadDir;
	}

	public boolean download(String rawUrl) {
		return download(rawUrl, null, -1);
	}

	public boolean download(String rawUrl, String md5SumString) {
		return download(rawUrl, md5SumString, -1);
	}

	public boolean download(String rawUrl, String md5SumString, long contentLength) {
		FileDownloadTask fileDownloadTask = new FileDownloadTask(rawUrl, md5SumString, contentLength, 500);
		return download(fileDownloadTask, true);
	}

	/**
	 * 是否能成功提交下载任务到线程池中异步执行
	 *
	 * @param fileDownloadTask 下载任务描述
	 * @param isCallBack       是否回调下载过程中相关的内容，如：是否回调下载中，回调下载成功等
	 *
	 * @return true：成功提交下载任务到线程池中异步执行
	 * false：提交下载任务到线程池中异步执行失败
	 */
	public boolean download(FileDownloadTask fileDownloadTask, boolean isCallBack) {
		// 检查任务是否在执行中
		try {
			if (mTask_Downloader.keySet().contains(fileDownloadTask)) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载管理器[%s]:当前任务[%d]已经在执行中", this.getClass().getSimpleName(),
							fileDownloadTask.hashCode());
				}
				return true;
			} else {

				AbsDownloader mAbsDownloader;
				if (isCallBack) {
					mAbsDownloader =
							newDownloader(mApplicationContext, mAbsDownloadDir, fileDownloadTask, mAbsDownloadNotifier, this);
				} else {
					mAbsDownloader = newDownloader(mApplicationContext, mAbsDownloadDir, fileDownloadTask, null, this);
				}

				// 如果任务没有在执行中的话，就创建一个下载任务并加入到线程池中执行
				// 仅仅在线程池执行第一个任务的时候采进行下载目录优化
				if (isFirstDownload) {
					mAbsDownloader.setNeed2OptCacheDir(isFirstDownload);
					isFirstDownload = false;
				}

				// 因为是Cache类型的线程池，所以可以在下面的beforeExecute中进行任务记录
				//				// 在放入到线程池中之前添加到缓存中
				//				// 不在下面的beforeExecute中写这段代码是因为
				//				// 加入到线程池的任务不一定立即执行，可能会进入到缓冲队列，如果进入队列的话，下面的beforeExecute是不会执行的
				//				// 这个时候也就不会添加一个新的任务，那么就会有问题
				//				try {
				//					mTask_Downloader.put(fileDownloadTask, mAbsDownloader);
				//					if (Debug_SDK.isDownloadLog) {
				//						Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "==================================");
				//						Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载管理器[%s]:添加下载任务[%d] %s", this.getClass()
				// .getSimpleName(),
				//								fileDownloadTask.hashCode(), fileDownloadTask.getRawDownloadUrl());
				//					}
				//				} catch (Throwable e) {
				//					if (Debug_SDK.isDownloadLog) {
				//						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
				//					}
				//				}

				mAbsCacheExecutorService.execute(mAbsDownloader);

				return true;
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
			return false;
		}
	}

	public boolean stopDownload(String rawUrl) {
		FileDownloadTask fileDownloadTask = new FileDownloadTask(rawUrl, null, -1, 500);
		return stopDownload(fileDownloadTask);
	}

	public boolean stopDownload(FileDownloadTask fileDownloadTask) {
		try {
			// 检查任务是否处于缓存中
			if (mTask_Downloader.keySet().contains(fileDownloadTask)) {

				// 检查任务是否处于队列中，如果是的话就只需要从缓冲队列中移除该任务即可，而不用stopdownload
				// 因为缓冲队列中的任务是还没有执行的
				AbsDownloader downloader = mTask_Downloader.get(fileDownloadTask);
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "任务正在执行中，准备停止任务");
				}
				downloader.stopDownload();
			}
			return true;
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
			return false;
		}
	}

	/**
	 * <p/>
	 * 使用场合：
	 * 如果你觉得你下载的文件被劫持为另一个url，那么就可以实现一下下面的接口
	 * <p/>
	 * 如：
	 * <ul>
	 * <li>检查文件长度是否等于服务器三的长度</li>
	 * <li>重新计算文件的md5和task中的md5(服务器那边返回的比较一下)</li>
	 * <li>或者重新向服务器请求这个文件的md5然后做对比</li>
	 * </ul>
	 * <p/>
	 * 子类可以重写，但是重写时，建议在最后 return super.isStoreFileAvailable
	 *
	 * @param fileDownloadTask 下载任务
	 *
	 * @return
	 */
	@Override
	public boolean isStoreFileAvailable(FileDownloadTask fileDownloadTask) {
		try {

			if (!fileDownloadTask.getStoreFile().exists()) {
				return false;
			}
			if (!fileDownloadTask.getStoreFile().isFile()) {
				return false;
			}

			// 如果这个任务有设置长度的话，就比较一下
			if (fileDownloadTask.getTotalLength() > 0) {
				if (fileDownloadTask.getStoreFile().length() != fileDownloadTask.getTotalLength()) {
					return false;
				}
			}

			// 如果这个任务有传入MD5的话
			if (!Basic_StringUtil.isNullOrEmpty(fileDownloadTask.getDownloadFileMd5sum())) {
				String destFileMd5 = Coder_Md5.getMD5SUM(fileDownloadTask.getStoreFile());
				if (!fileDownloadTask.getDownloadFileMd5sum().equals(destFileMd5)) {
					return false;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
		return true;
	}

	/**
	 * 注册下载监听观察者
	 *
	 * @param iDownloadListener
	 *
	 * @return
	 */
	public boolean registerIDownloadListener(IDownloadListener iDownloadListener) {
		try {
			return mAbsDownloadNotifier.registerListener(iDownloadListener);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
			return false;
		}
	}

	/**
	 * 移除下载监听观察者
	 *
	 * @param iDownloadListener
	 *
	 * @return
	 */
	public boolean removeIDownloadListener(IDownloadListener iDownloadListener) {
		try {
			return mAbsDownloadNotifier.removeListener(iDownloadListener);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
			return false;
		}
	}

	/**
	 * 在线程池执行每个任务之前的额外操作
	 *
	 * @param t
	 * @param r
	 */
	@Override
	public void beforeExecute(Thread t, Runnable r) {
		try {
			AbsDownloader absDownloader = (AbsDownloader) r;
			FileDownloadTask absFileDownloadTask = absDownloader.getFileDownloadTask();
			if (absFileDownloadTask != null) {
				mTask_Downloader.put(absFileDownloadTask, absDownloader);
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.tw(Debug_SDK.mDownloadTag, this, "==================================");
					Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "下载管理器[%s]:添加下载任务[%d] %s", this.getClass().getSimpleName(),
							absFileDownloadTask.hashCode(), absFileDownloadTask.getRawDownloadUrl());
				}

			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 在线程池执行完每个任务之后的额外操作
	 *
	 * @param r
	 * @param t
	 */
	@Override
	public void afterExecute(Runnable r, Throwable t) {
		try {
			AbsDownloader absDownloader = (AbsDownloader) r;
			FileDownloadTask fileDownloadTask = absDownloader.getFileDownloadTask();
			if (fileDownloadTask != null) {
				mTask_Downloader.remove(fileDownloadTask);
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载管理器[%s]:移除下载任务[%d] %s", this.getClass().getSimpleName(),
							fileDownloadTask.hashCode(), fileDownloadTask.getRawDownloadUrl());
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}
}
