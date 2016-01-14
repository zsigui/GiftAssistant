package net.youmi.android.libs.common.v2.global;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.pool.DefaultCacheExecutorService;
import net.youmi.android.libs.common.v2.pool.core.AbsCacheExecutorService;

import java.util.List;

/**
 * 全局公用线程池，没什么特殊需求都用这个吧
 *
 * @author zhitaocai
 */
public class GlobalCacheExecutor {

	private final static AbsCacheExecutorService mCachedThreadPool = new DefaultCacheExecutorService();

	/**
	 * 公用线程池
	 *
	 * @return
	 */
	public static AbsCacheExecutorService getCachedThreadPool() {
		return mCachedThreadPool;
	}

	public static void execute(Runnable task) {
		try {
			mCachedThreadPool.execute(task);
		} catch (Exception e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, GlobalCacheExecutor.class, e);
			}
		}
	}

	public static List<Runnable> shutdownNow() {
		try {
			return mCachedThreadPool.shutdownNow();
		} catch (Exception e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, GlobalCacheExecutor.class, e);
			}
		}
		return null;
	}

}
