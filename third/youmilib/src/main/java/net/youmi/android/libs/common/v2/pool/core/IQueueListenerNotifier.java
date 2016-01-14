package net.youmi.android.libs.common.v2.pool.core;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;

/**
 * 队列任务加入到队列和从队列中移出时的观察者监听管理器
 *
 * @param <T>
 */
public class IQueueListenerNotifier<T> {

	private final ArrayList<IQueueListener<T>> mListeners = new ArrayList<IQueueListener<T>>();

	public boolean registerListener(IQueueListener<T> listener) {
		try {
			synchronized (mListeners) {
				if (!mListeners.contains(listener)) {
					return mListeners.add(listener);
				} else {
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
			}
		}
		return false;
	}

	public boolean removeListener(IQueueListener<T> listener) {
		try {
			return mListeners.remove(listener);
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
			}
		}
		return false;
	}

	public void onNotifyOffer(T t, int currentWaitQueueLength) {
		try {
			if (mListeners.isEmpty()) {
				return;
			}

			for (IQueueListener<T> listener : mListeners) {
				try {
					listener.onOffer(t, currentWaitQueueLength);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
			}
		}
	}

	public void onNotifyTake(T t, int currentWaitQueueLength) {
		try {
			if (mListeners.isEmpty()) {
				return;
			}

			for (IQueueListener<T> listener : mListeners) {
				try {
					listener.onTake(t, currentWaitQueueLength);
				} catch (Throwable e) {
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, IQueueListenerNotifier.class, e);
			}
		}
	}

}
