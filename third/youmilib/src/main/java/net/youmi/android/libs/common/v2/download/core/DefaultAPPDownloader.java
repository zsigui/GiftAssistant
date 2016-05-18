package net.youmi.android.libs.common.v2.download.core;

import android.content.Context;

import net.youmi.android.libs.common.v2.download.base.DefaultAutoRetryFileDownloader;
import net.youmi.android.libs.common.v2.download.base.IDownloader;
import net.youmi.android.libs.common.v2.download.listener.IFileAvailableChecker;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir;

import java.io.IOException;

/**
 * 下载实体类
 * <p/>
 * 1. 支持自动重试的
 * 2. 支持回调监听
 * 3. <b>不支持多进程下载<b/>
 *
 * @author zhitao
 * @since 2015-09-20 00:13
 */
public class DefaultAPPDownloader extends AbsDownloader {

	/**
	 * @param context
	 * @param absDownloadDir        一个带有自定义规则的下载目录(如：目录是否会自动清理，目录下的文件命名规范等)
	 * @param fileDownloadTask      下载任务描述数据模型
	 * @param absDownloadNotifier   下载状态监听观察者管理器
	 * @param iFileAvailableChecker 任务下载完成后的检查器，主要用于检查下载完成的文件是否有效
	 * @throws NullPointerException
	 * @throws java.io.IOException
	 */
	public DefaultAPPDownloader(Context context, AbsDownloadDir absDownloadDir, FileDownloadTask fileDownloadTask,
	                            AbsDownloadNotifier absDownloadNotifier, IFileAvailableChecker iFileAvailableChecker)
			throws NullPointerException, IOException {
		super(context, absDownloadDir, fileDownloadTask, absDownloadNotifier, iFileAvailableChecker);
	}

	/**
	 * 这里仅需要你new一个你需要调用的核心下载代码类
	 *
	 * @param context
	 * @param fileDownloadTask
	 * @param iFileAvailableChecker
	 * @return
	 */
	@Override
	protected IDownloader newDownloadHandler(Context context, FileDownloadTask fileDownloadTask,
	                                         IFileAvailableChecker iFileAvailableChecker) {
		return new DefaultAutoRetryFileDownloader(context, fileDownloadTask);
	}

	/**
	 * 是否支持多进程下载
	 *
	 * @return
	 */
	@Override
	protected boolean isSupportMultiProgressDownload() {
		return false;
	}
}
