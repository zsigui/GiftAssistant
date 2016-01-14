package net.youmi.android.libs.common.v2.pool.core;

/**
 * 扩展线程池执行每个任务之前和之后的额外逻辑
 *
 * @author zhitao
 * @since 2015-09-21 22:56
 */
public interface IExecuteListener {

	/**
	 * 在线程池执行每个任务之前的额外操作
	 *
	 * @param t
	 * @param r
	 */
	void beforeExecute(Thread t, Runnable r);

	/**
	 * 在线程池执行完每个任务之后的额外操作
	 *
	 * @param r
	 * @param t
	 */
	void afterExecute(Runnable r, Throwable t);

}
