package net.youmi.android.libs.common.v2.pool.core;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbsFixedExecutorService {

	/**
	 * 设置最大同时并行线程数
	 */
	public abstract int getMaxThreadsNumber();

	/**
	 * 设置线程工厂
	 */
	protected abstract BaseThreadFactory newBaseThreadFatory();

	/**
	 * 设置等待队列
	 */
	protected abstract AbsLinkedBlockingQueue newAbsLinkedBlockingQueue();

	public AbsThreadPoolExecutor mExecutorService;

	private BaseThreadFactory mBaseThreadFactory;

	private AbsLinkedBlockingQueue mAbsLinkedBlockingQueue;

	/**
	 * AbsQueueListenerNotifier为在达到下载上线线程数时，监听队列进出的监听广播器，可以为null
	 */
	private IQueueListenerNotifier mIQueueListenerNotifier;

	public AbsFixedExecutorService() {
		mBaseThreadFactory = newBaseThreadFatory();
		mAbsLinkedBlockingQueue = newAbsLinkedBlockingQueue();
		mExecutorService = new AbsThreadPoolExecutor(getMaxThreadsNumber(), getMaxThreadsNumber(), 0L, TimeUnit.MILLISECONDS,
				mAbsLinkedBlockingQueue, mBaseThreadFactory);
	}

	/**
	 * 设置每个任务执行前后需要做的逻辑
	 *
	 * @param iExecuteListener
	 */
	public void setIExecuteListener(IExecuteListener iExecuteListener) {
		mExecutorService.setIExecuteListener(iExecuteListener);
	}

	/**
	 * @param iQueueListenerNotifier 　IQueueListenerNotifier 为在达到下载上线线程数时，监听队列进出的监听广播器，可以为null
	 */
	public void setQueueListenerNotifier(IQueueListenerNotifier iQueueListenerNotifier) {
		mIQueueListenerNotifier = iQueueListenerNotifier;
		if (mAbsLinkedBlockingQueue != null) {
			mAbsLinkedBlockingQueue.setAbsListenerNotifier(iQueueListenerNotifier);
		}
	}

	/**
	 * 针对每个子类对象，注册该对象的等待队列，任务添加与删减的监听器
	 */
	@SuppressWarnings("unchecked")
	public boolean registerAbsQueueListener(IQueueListener listener) {
		if (mIQueueListenerNotifier != null) {
			return mIQueueListenerNotifier.registerListener(listener);
		} else {
			return true;
		}
	}

	/**
	 * 针对每个子类对象，注销该对象的等待队列，任务添加与删减的监听器
	 */
	@SuppressWarnings("unchecked")
	public boolean removeAbsQueueListener(IQueueListener listener) {
		if (mIQueueListenerNotifier != null) {
			return mIQueueListenerNotifier.removeListener(listener);
		} else {
			return true;
		}
	}

	public void execute(Runnable runnable) {
		try {
			mExecutorService.execute(runnable);
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, e);
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
				Debug_SDK.tw(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, "【%s线程池消息】:关闭线程池并返回还没有执行的任务，当前有%d个任务还没有被执行",
						mBaseThreadFactory.getPoolName(), list.size());
			}
			return list;
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, e);
			}
			return null;
		}
	}

	/**
	 * 中断所有阻塞中的线程
	 */
	public void interrupt() {
		try {
			mBaseThreadFactory.interrupt();
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.tw(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, "【%s线程池消息】:中断阻塞中的线程",
						mBaseThreadFactory.getPoolName());
			}
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, e);
			}
		}
	}

	/**
	 * 从缓冲队列中移除任务
	 *
	 * @param runnable
	 */
	public boolean remove(Runnable runnable) {
		try {
			if (mAbsLinkedBlockingQueue != null) {
				return mAbsLinkedBlockingQueue.remove(runnable);
			} else {
				return true;
			}
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, e);
			}
			return false;
		}
	}

	/**
	 * 检查缓冲队列中是否存在指定的任务
	 *
	 * @param runnable
	 */
	public boolean contain(Runnable runnable) {
		try {
			if (mAbsLinkedBlockingQueue != null) {
				return mAbsLinkedBlockingQueue.contains(runnable);
			} else {
				return true;
			}
		} catch (Exception e) {
			if (Debug_SDK.isPoolLog) {
				Debug_SDK.te(Debug_SDK.mPoolTag, AbsFixedExecutorService.class, e);
			}
			return false;
		}
	}

}
