package net.youmi.android.libs.common.v2.template;

import net.youmi.android.libs.common.debug.Debug_SDK;

import java.util.ArrayList;
import java.util.List;

public abstract class TListenersManager<T> {

	private List<T> mListeners;

	protected synchronized List<T> getListeners() {
		if (mListeners == null) {
			mListeners = new ArrayList<T>();
		}
		return mListeners;
	}

	/**
	 * 注册监听器<br/>
	 * 注册之后，所有下载过程都会进行通知，需要通过回调参数中的FileDownloadTask来辨别是否是需要的监听。<br/>
	 * 建议在onCreate等时机调用注册。<br/>
	 * 必须调用removeListener进行注销。<br/>
	 *
	 * @param listener
	 */
	public boolean registerListener(T listener) {
		try {
			if (listener != null) {
				final List<T> list = getListeners();
				if (!list.contains(listener)) {
					return list.add(listener);
				} else {
					return true;
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
			}
		}
		return false;
	}

	/**
	 * 注销监听器。<br/>
	 * 建议在onDestory等时机调用注销。<br/>
	 *
	 * @param listener
	 */
	public boolean removeListener(T listener) {
		try {
			if (listener != null) {
				final List<T> list = getListeners();
				return list.remove(listener);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, this, e);
			}
		}
		return false;
	}

}
