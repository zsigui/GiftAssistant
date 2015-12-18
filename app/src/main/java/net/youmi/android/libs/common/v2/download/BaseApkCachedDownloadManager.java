package net.youmi.android.libs.common.v2.download;

import android.content.Context;

import net.youmi.android.libs.common.v2.download.core.DefaultSDKDownloader;
import net.youmi.android.libs.common.v2.download.executor.DefaultDownloadCacheExecutorService;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
import net.youmi.android.libs.common.v2.download.notify.DefaultDownloadNotifier;
import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;

import java.io.IOException;

/**
 * @author zhitao
 * @since 2015-09-21 14:58
 */
public abstract class BaseApkCachedDownloadManager extends AbsCachedDownloadManager implements IMaxPriorityDownloadListener {

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
	 * 设置用的下载实体类
	 *
	 * @return
	 */
	@Override
	public Class getDownloaderClass() {
		return DefaultSDKDownloader.class;
	}

}
