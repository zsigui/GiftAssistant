package com.oplay.giftcool.download;

import android.content.Context;

import com.oplay.giftcool.download.listener.OnInstallListener;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * InstallNotifier
 *
 * @author zacklpx
 *         date 16-1-24
 *         description
 */
public class InstallNotifier {

	private static InstallNotifier mInstance;
	private List<OnInstallListener> mListeners;

	private InstallNotifier() {
		mListeners = new ArrayList<>();
	}

	public synchronized static InstallNotifier getInstance() {
		if (mInstance == null) {
			mInstance = new InstallNotifier();
		}
		return mInstance;
	}

	public void notifyInstallListeners(Context context, String packageName) {
		for (OnInstallListener listener : mListeners) {
			try {
				listener.onInstall(context, packageName);
			}catch (Throwable e) {
				KLog.e(e);
			}
		}
	}

	public synchronized void addListener(OnInstallListener listener) {
		if (listener != null && !mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public synchronized void removeListener(OnInstallListener listener) {
		if (listener != null) {
			mListeners.remove(listener);
		}
	}

}
