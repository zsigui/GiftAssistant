package net.youmi.android.libs.common.v2.pool;

import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;
import net.youmi.android.libs.common.v2.pool.core.BaseThreadFactory;

/**
 * @author zhitao
 * @since 2015-09-16 16:06
 */
public class DefaultCacheExecutorService extends AbsCacheExecutorService {

	public DefaultCacheExecutorService() {
		super();
	}

	@Override
	public BaseThreadFactory newBaseThreadFatory() {
		return new BaseThreadFactory(Thread.NORM_PRIORITY, "DefalutThreadFactory");
	}
}
