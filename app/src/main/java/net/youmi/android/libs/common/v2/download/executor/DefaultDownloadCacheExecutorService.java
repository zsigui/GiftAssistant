package net.youmi.android.libs.common.v2.download.executor;

import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;
import net.youmi.android.libs.common.v2.pool.core.BaseThreadFactory;

public class DefaultDownloadCacheExecutorService extends AbsCacheExecutorService {

	private String mExecutorPoolName;

	public DefaultDownloadCacheExecutorService(String executorPoolName) {
		mExecutorPoolName = executorPoolName;
	}

	/**
	 * 设置线程工厂
	 *
	 * @return
	 */
	@Override
	public BaseThreadFactory newBaseThreadFatory() {
		return new BaseThreadFactory(Thread.NORM_PRIORITY, mExecutorPoolName);
	}
}
