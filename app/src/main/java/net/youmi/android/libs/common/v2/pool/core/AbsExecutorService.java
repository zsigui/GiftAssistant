package net.youmi.android.libs.common.v2.pool.core;

import java.util.List;

/**
 * 抽象线程池
 *
 * @author zhitao
 * @since 2015-09-21 14:53
 */
public abstract class AbsExecutorService {

	/**
	 * 提交一个runnable到线程池执行
	 *
	 * @param runnable
	 */
	public abstract void execute(Runnable runnable);

	/**
	 * shutdownNow会停止我们继续向线程池添加任务，同时会将还没有执行的任务列表抛出来不予以执行，会尝试终止正在执行的任务，但是不会等待起终止了才返回
	 *
	 * @return 还没有执行的任务
	 */
	public abstract List<Runnable> shutdownNow();

	/**
	 * 　中断所有阻塞中的线程
	 *
	 * @return
	 */
	public abstract void interrupt();
}
