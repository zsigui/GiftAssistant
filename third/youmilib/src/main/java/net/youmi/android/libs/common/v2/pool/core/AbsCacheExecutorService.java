package net.youmi.android.libs.common.v2.pool.core;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public abstract class AbsCacheExecutorService {

	protected AbsThreadPoolExecutor mExecutorService;

	protected BaseThreadFactory mBaseThreadFactory;

	public AbsCacheExecutorService() {
		mBaseThreadFactory = newBaseThreadFatory();
		mExecutorService =
				new AbsThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new
						SynchronousQueue<Runnable>(),
						mBaseThreadFactory);
	}

	/**
	 * 设置线程工厂
	 */
	protected abstract BaseThreadFactory newBaseThreadFatory();

	/**
	 * 设置每个任务执行前后需要做的逻辑
	 *
	 * @param iExecuteListener
	 */
	public void setIExecuteListener(IExecuteListener iExecuteListener) {
		mExecutorService.setIExecuteListener(iExecuteListener);
	}

	public void execute(Runnable runnable) {
		try {
			mExecutorService.execute(runnable);
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.ti(Debug_SDK.mPoolTag, AbsCacheExecutorService.class, "【%s线程池消息】:下载任务提交成功 hashcode = %d",
						mBaseThreadFactory.getPoolName(), runnable == null ? 0 : runnable.hashCode());
			}
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsCacheExecutorService.class, e);
			}
		}
	}

	/**
	 * shutdownNow会停止我们继续向线程池添加任务，同时会将还没有执行的任务列表抛出来不予以执行，会尝试终止正在执行的任务，但是不会等待起终止了才返回
	 *
	 * @return 还没有执行的任务
	 */
	public List<Runnable> shutdownNow() {

		try {

			List<Runnable> list = mExecutorService.shutdownNow();

			// 可以在调用 shutdown 或者 shutdownNow之后，通过方法isTermination来判断线程池中的任务是否都已经停止了

			if (Debug_SDK.isPoolLog) {
				Debug_SDK.tw(Debug_SDK.mPoolTag, AbsCacheExecutorService.class,
						"【%s线程池消息】:关闭线程池并返回还没有执行的任务，当前有%d个任务还没有被执行",
						mBaseThreadFactory.getPoolName(), list.size());
			}
			return list;
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsCacheExecutorService.class, e);
			}
			return null;
		}
	}

	/**
	 * 中断所有阻塞中的线程
	 *
	 * @return
	 */
	public void interrupt() {
		try {
			mBaseThreadFactory.interrupt();
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.tw(Debug_SDK.mPoolTag, AbsCacheExecutorService.class, "【%s线程池消息】:中断阻塞中的线程",
						mBaseThreadFactory.getPoolName());
			}
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsCacheExecutorService.class, e);
			}
		}
	}
}
