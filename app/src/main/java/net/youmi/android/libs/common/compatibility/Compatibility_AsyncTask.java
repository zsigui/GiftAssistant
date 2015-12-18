package net.youmi.android.libs.common.compatibility;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import net.youmi.android.libs.common.debug.DLog;

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
			if (DLog.isCompatLog) {
				DLog.te(DLog.mCompatTag, Compatibility_AsyncTask.class, e);
			}
		}
	}

}
