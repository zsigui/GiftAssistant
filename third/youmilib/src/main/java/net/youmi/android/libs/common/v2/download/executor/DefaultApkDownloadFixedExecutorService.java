package net.youmi.android.libs.common.v2.download.executor;

import net.youmi.android.libs.common.v2.download.executor.queue.RunnableLinkedBlockingQueue;
import net.youmi.android.libs.common.v2.pool.core.AbsFixedExecutorService;
import net.youmi.android.libs.common.v2.pool.core.AbsLinkedBlockingQueue;
import net.youmi.android.libs.common.v2.pool.core.BaseThreadFactory;

public class DefaultApkDownloadFixedExecutorService extends AbsFixedExecutorService {

	/**
	 * 设置线程工厂
	 *
	 * @return
	 */
	@Override
	public BaseThreadFactory newBaseThreadFatory() {
		return new BaseThreadFactory(Thread.NORM_PRIORITY, "ApkDownload");
	}

	/**
	 * 设置最大同时下载限制数
	 *
	 * @return
	 */
	@Override
	public int getMaxThreadsNumber() {
		return 1;
	}

	/**
	 * 设置等待队列
	 *
	 * @return
	 */
	@Override
	public AbsLinkedBlockingQueue newAbsLinkedBlockingQueue() {
		return new RunnableLinkedBlockingQueue();
	}

}
