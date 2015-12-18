package net.youmi.android.libs.common.v2.download;

import android.content.Context;

import net.youmi.android.libs.common.v2.download.core.DefaultSDKDownloader;
import net.youmi.android.libs.common.v2.download.executor.DefaultApkDownloadFixedExecutorService;
import net.youmi.android.libs.common.v2.download.executor.queue.RunnableQueueListenerNotifier;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.notify.DefaultDownloadNotifier;
import net.youmi.android.libs.common.v2.pool.core.AbsFixedExecutorService;

import java.io.IOException;

/**
 * @author zhitao
 * @since 2015-09-21 14:58
 */
public abstract class BaseApkFixedDownloadManager extends AbsFixedDownloadManager implements IMaxPriorityDownloadListener {

	protected BaseApkFixedDownloadManager(Context context) throws NullPointerException, IOException {
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
	 * 子类new一个监听线程池缓冲队列任务进出的观察者监听管理器
	 *
	 * @return
	 */
	@Override
	public RunnableQueueListenerNotifier newQueueListenerNotifier() {
		return new RunnableQueueListenerNotifier();
	}

	/**
	 * 子类new一个具体使用的Fixed类型线程池
	 *
	 * @return
	 */
	@Override
	public AbsFixedExecutorService newFixedExecutorService() {
		return new DefaultApkDownloadFixedExecutorService();
	}

	/**
	 * 设置用的下载实体类
	 *
	 * @return
	 */
	@Override
	public Class getDownloaderClass() {
		return DefaultSDKDownloader.class;
	}

}
