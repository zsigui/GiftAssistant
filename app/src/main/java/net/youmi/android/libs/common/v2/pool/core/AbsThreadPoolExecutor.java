package net.youmi.android.libs.common.v2.pool.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhitao
 * @since 2015-09-21 22:36
 */
public class AbsThreadPoolExecutor extends ThreadPoolExecutor {

	/**
	 * Creates a new {@code ThreadPoolExecutor} with the given initial
	 * parameters and default rejected execution handler.
	 *
	 * @param corePoolSize    the number of threads to keep in the pool, even
	 *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param maximumPoolSize the maximum number of threads to allow in the
	 *                        pool
	 * @param keepAliveTime   when the number of threads is greater than
	 *                        the core, this is the maximum time that excess idle threads
	 *                        will wait for new tasks before terminating.
	 * @param unit            the time unit for the {@code keepAliveTime} argument
	 * @param workQueue       the queue to use for holding tasks before they are
	 *                        executed.  This queue will hold only the {@code Runnable}
	 *                        tasks submitted by the {@code execute} method.
	 * @param threadFactory   the factory to use when the executor
	 *                        creates a new thread
	 *
	 * @throws IllegalArgumentException if one of the following holds:<br>
	 *                                  {@code corePoolSize < 0}<br>
	 *                                  {@code keepAliveTime < 0}<br>
	 *                                  {@code maximumPoolSize <= 0}<br>
	 *                                  {@code maximumPoolSize < corePoolSize}
	 * @throws NullPointerException     if {@code workQueue}
	 *                                  or {@code threadFactory} is null
	 */
	public AbsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	private IExecuteListener mIExecuteListener;

	public void setIExecuteListener(IExecuteListener iExecuteListener) {
		mIExecuteListener = iExecuteListener;
	}

	/**
	 * Method invoked prior to executing the given Runnable in the
	 * given thread.  This method is invoked by thread {@code t} that
	 * will execute task {@code r}, and may be used to re-initialize
	 * ThreadLocals, or to perform logging.
	 * <p/>
	 * <p>This implementation does nothing, but may be customized in
	 * subclasses. Note: To properly nest multiple overridings, subclasses
	 * should generally invoke {@code super.beforeExecute} at the end of
	 * this method.
	 *
	 * @param t the thread that will run task {@code r}
	 * @param r the task that will be executed
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		//		if (Debug_SDK.isDownloadLog) {
		//			Debug_SDK.tv(Debug_SDK.mDownloadTag, this, "准备执行任务 %d ", r.hashCode());
		//		}
		if (mIExecuteListener != null) {
			mIExecuteListener.beforeExecute(t, r);
		}
		super.beforeExecute(t, r);
	}

	/**
	 * Method invoked upon completion of execution of the given Runnable.
	 * This method is invoked by the thread that executed the task. If
	 * non-null, the Throwable is the uncaught {@code RuntimeException}
	 * or {@code Error} that caused execution to terminate abruptly.
	 * <p/>
	 * <p>This implementation does nothing, but may be customized in
	 * subclasses. Note: To properly nest multiple overridings, subclasses
	 * should generally invoke {@code super.afterExecute} at the
	 * beginning of this method.
	 * <p/>
	 * <p><b>Note:</b> When actions are enclosed in tasks (such as
	 * {@link FutureTask}) either explicitly or via methods such as
	 * {@code submit}, these task objects catch and maintain
	 * computational exceptions, and so they do not cause abrupt
	 * termination, and the internal exceptions are <em>not</em>
	 * passed to this method. If you would like to trap both kinds of
	 * failures in this method, you can further probe for such cases,
	 * as in this sample subclass that prints either the direct cause
	 * or the underlying exception if a task has been aborted:
	 * <p/>
	 * <pre> {@code
	 * class ExtendedExecutor extends ThreadPoolExecutor {
	 *   // ...
	 *   protected void afterExecute(Runnable r, Throwable t) {
	 *     super.afterExecute(r, t);
	 *     if (t == null && r instanceof Future<?>) {
	 *       try {
	 *         Object result = ((Future<?>) r).get();
	 *       } catch (CancellationException ce) {
	 *           t = ce;
	 *       } catch (ExecutionException ee) {
	 *           t = ee.getCause();
	 *       } catch (InterruptedException ie) {
	 *           Thread.currentThread().interrupt(); // ignore/reset
	 *       }
	 *     }
	 *     if (t != null)
	 *       System.out.println(t);
	 *   }
	 * }}</pre>
	 *
	 * @param r the runnable that has completed
	 * @param t the exception that caused termination, or null if
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		//		if (Debug_SDK.isDownloadLog) {
		//			Debug_SDK.tv(Debug_SDK.mDownloadTag, this, "任务执行完毕 %d ", r.hashCode());
		//		}

		//		if (t == null && r instanceof Future<?>) {
		//			try {
		//				Object result = ((Future<?>) r).get();
		//			} catch (CancellationException ce) {
		//				t = ce;
		//			} catch (ExecutionException ee) {
		//				t = ee.getCause();
		//			} catch (InterruptedException ie) {
		//			}
		//			Thread.currentThread().interrupt(); // ignore/reset
		//		}
		if (mIExecuteListener != null) {
			mIExecuteListener.afterExecute(r, t);
		}
	}
}
