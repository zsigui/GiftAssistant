package net.youmi.android.libs.common.v2.pool.core;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhitao
 * @since 2015-09-03 00:56
 */
public abstract class AbsLinkedBlockingQueue<T> extends LinkedBlockingQueue<T> {

	private final static boolean isLogOpen = false;

	IQueueListenerNotifier<T> mAbsListenerNotifier;

	public AbsLinkedBlockingQueue(Collection<? extends T> c) {
		super(c);
	}

	public AbsLinkedBlockingQueue(int capacity) {
		super(capacity);
	}

	public AbsLinkedBlockingQueue() {
		super();
	}

	public IQueueListenerNotifier<T> getAbsListenerNotifier() {
		return mAbsListenerNotifier;
	}

	public void setAbsListenerNotifier(IQueueListenerNotifier<T> absListenerNotifier) {
		mAbsListenerNotifier = absListenerNotifier;
	}

	@Override
	public boolean remove(Object o) {
		boolean result = super.remove(o);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了remove(o)");
		}
		return result;
	}

	@Override
	public void clear() {
		super.clear();
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了clear()");
		}
	}

	@Override
	public boolean add(T t) {
		boolean result = super.add(t);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了add(t)");
		}
		return result;
	}

	@Override
	public T remove() {
		T t = super.remove();
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了remove()");
		}
		return t;
	}

	@Override
	public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
		boolean result = super.offer(t, timeout, unit);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了offer(t, timeout, unit)");
		}
		return result;
	}

	@Override
	public boolean offer(T t) {
		boolean result = super.offer(t);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了offer(t) 当前size %d", size());
		}
		// 只有size大于0 才表示真的有任务加入到队列中了
		if (size() > 0) {
			if (mAbsListenerNotifier != null) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "准备通知各个监听者，有新的任务加入到等待队列中，当前等待任务数量 %d", size());
				}
				mAbsListenerNotifier.onNotifyOffer(t, size());
			}
		}
		return result;
	}

	@Override
	public T take() throws InterruptedException {
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了super.take()之前 size %d", size());
		}
		int beforeTakeQueueSize = size();
		T t = super.take();
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了super.take()之后 size %d", size());
		}
		if (beforeTakeQueueSize > 0) {
			if (mAbsListenerNotifier != null) {
				if (Debug_SDK.isDownloadLog && isLogOpen) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "通知监听者，有任务从等待队列中提出到线程池中进行执行，提取出任务后，等待队列中长度%d", size());
				}
				mAbsListenerNotifier.onNotifyTake(t, size());
			}
		}
		return t;
	}

	@Override
	public T poll(long timeout, TimeUnit unit) throws InterruptedException {
		T t = super.poll(timeout, unit);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了poll(timeout, unit)");
		}
		return t;
	}

	@Override
	public T poll() {
		T t = super.poll();
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了poll()");
		}
		return t;
	}

	@Override
	public T peek() {
		T t = super.peek();
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了peek()");
		}
		return t;
	}

	@Override
	public void put(T t) throws InterruptedException {
		super.put(t);
		if (Debug_SDK.isDownloadLog && isLogOpen) {
			Debug_SDK.td(Debug_SDK.mDownloadTag, this, "调用了put(t)");
		}
	}
}
