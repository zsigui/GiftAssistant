package net.youmi.android.libs.common.compatibility;

import net.youmi.android.libs.common.debug.Debug_SDK;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class Compatibility_AsyncTask {

	@SuppressLint("NewApi")
	public static <P, T extends AsyncTask<P, ?, ?>> void executeParallel(T task, P... params) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
			} else {
				task.execute(params);
			}
		} catch (Throwable e) {
			if (Debug_SDK.isCompatLog) {
				Debug_SDK.te(Debug_SDK.mCompatTag, Compatibility_AsyncTask.class, e);
			}
		}
	}

}
