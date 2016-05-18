package net.youmi.android.libs.common.v2.download;

import android.content.Context;

import net.youmi.android.libs.common.v2.download.core.AbsDownloader;
import net.youmi.android.libs.common.v2.download.core.DefaultSDKDownloader;
import net.youmi.android.libs.common.v2.download.executor.DefaultDownloadCacheExecutorService;
import net.youmi.android.libs.common.v2.download.listener.IFileAvailableChecker;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.notify.DefaultDownloadNotifier;
import net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir;
import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;

import java.io.IOException;

/**
 * @author zhitao
 * @since 2015-09-21 14:58
 */
public abstract class BaseApkCachedDownloadManager extends AbsCachedDownloadManager implements
		IMaxPriorityDownloadListener {

	protected BaseApkCachedDownloadManager(Context context) throws NullPointerException, IOException {
		super(context);
	}

	/**
	 * 子类new一个下载任务的观察者监听管理器
	 *
	 * @return
	 */
	@Override
	public AbsDownloadNotifier newDownloadNotifier() {
		return new DefaultDownloadNotifier(this);
	}

	/**
	 * 子类new一个具体使用的Cache类型线程池
	 *
	 * @return
	 */
	@Override
	public AbsCacheExecutorService newAbsCacheExecutorService() {
		return new DefaultDownloadCacheExecutorService("ApkDownload-Cache");
	}

	/**
	 * 子类new一个下载的实体类，该类继承自 {@link net.youmi.android.libs.common.v2.download.core.AbsDownloader}
	 *
	 * @param context
	 * @param absDownloadDir        一个带有自定义规则的下载目录(如：目录是否会自动清理，目录下的文件命名规范等)
	 * @param fileDownloadTask      下载任务描述数据模型
	 * @param absDownloadNotifier   下载状态监听观察者管理器
	 * @param iFileAvailableChecker 任务下载完成后的检查器，主要用于检查下载完成的文件是否有效
	 * @return
	 */
	@Override
	public AbsDownloader newDownloader(Context context, AbsDownloadDir absDownloadDir, FileDownloadTask
			fileDownloadTask,
	                                   AbsDownloadNotifier absDownloadNotifier, IFileAvailableChecker
			                                       iFileAvailableChecker)
			throws NullPointerException, IOException {
		return new DefaultSDKDownloader(context, absDownloadDir, fileDownloadTask, absDownloadNotifier, iFileAvailableChecker);
	}

}
