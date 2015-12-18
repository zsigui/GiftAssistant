package net.youmi.android.libs.common.v2.pool.core;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.debug.DLog;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseThreadFactory implements ThreadFactory {

	/**
	 * 用来输出一下当前究竟用了多少个线程工厂
	 */
	protected static AtomicInteger mPoolCounter = new AtomicInteger(0);

	/**
	 * 用来输出一下当前线程工厂已经创建了的线程数
	 */
	protected AtomicInteger mThreadCounter = new AtomicInteger(0);

	protected ThreadGroup mThreadGroup;

	protected String mPoolName;

	protected int mThreadPriority;

	/**
	 * @param threadPriority 线程优先级 一般为 Thread.NORM_PRIORITY - 1 就可以
	 * @param poolName       线程工厂名字，如ImageDownload
	 */
	public BaseThreadFactory(int threadPriority, String poolName) {
		if (Basic_StringUtil.isNullOrEmpty(poolName)) {
			mPoolName = "default" + mPoolCounter.get();
		} else {
			mPoolName = poolName;
		}
		mThreadPriority = threadPriority;
		mThreadGroup = new ThreadGroup(mPoolName);
		mPoolCounter.getAndIncrement();
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = null;
		String threadName = null;
		try {

			threadName = String.format("%s-pool-thread-%d", mPoolName, mThreadCounter.incrementAndGet());

			// 线程所在组，runable，线程名字，stacksize
			thread = new Thread(mThreadGroup, r, threadName, 0);
			if (thread.isDaemon()) {
				thread.setDaemon(false);
			}
			thread.setPriority(mThreadPriority);
		} catch (Exception e) {
			if (DLog.isPoolLog) {
				DLog.te(DLog.mPoolTag, this, e);
			}
		} finally {
			if (DLog.isPoolLog) {
				if (thread == null) {
					DLog.tw(DLog.mPoolTag, this, "【id:%d】【%s线程工厂消息】:创建第%d个线程失败 线程名字:%s", mPoolCounter.get(), mPoolName,
							mThreadCounter.get(), threadName);
				} else {
					DLog.ti(DLog.mPoolTag, this, "【id:%d】【%s线程工厂消息】:创建第%d个线程成功 线程名字:%s", mPoolCounter.get(), mPoolName,
							mThreadCounter.get(), threadName);
				}
			}
		}
		return thread;
	}

	/**
	 * 只能将阻塞中的线程中断，并不能停止线程组中的所有线程的运行
	 */
	public void interrupt() {
		mThreadGroup.interrupt();
	}

	/**
	 * 获取线程池名字
	 *
	 * @return
	 */
	public String getPoolName() {
		return mPoolName;
	}

	/**
	 * 获取当前已经创建了多少个线程工厂
	 *
	 * @return
	 */
	public static int getTotalPoolNumber() {
		return mPoolCounter.get();
	}
}