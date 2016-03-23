package com.oplay.giftcool.util;

import android.os.Handler;
import android.os.Looper;

import com.oplay.giftcool.config.Global;

/**
 * Created by zsigui on 15-12-17.
 */
public class ThreadUtil {
	private static Handler sHandler;

	private ThreadUtil() {
	}

	/**
	 * 在子线程执行任务
	 *
	 * @param task
	 */
	public static void runInThread(Runnable task) {
		Global.THREAD_POOL.execute(task);
	}


	/**
	 * 在UI线程执行任务
	 *
	 * @param task
	 */
	public static void runOnUiThread(Runnable task) {
		if (sHandler == null) {
			sHandler = new Handler(Looper.getMainLooper());
		}
		sHandler.post(task);
	}

	public static void remove(Runnable task) {
		if (sHandler != null) {
			sHandler.removeCallbacks(task);
		}
	}

	/**
	 * 在UI线程延时执行任务
	 *
	 * @param task
	 * @param delayMillis 延时时间，单位毫秒
	 */
	public static void runOnUiThread(Runnable task, long delayMillis) {
		if (sHandler == null) {
			sHandler = new Handler(Looper.getMainLooper());
		}
		sHandler.postDelayed(task, delayMillis);
	}

	public static void destroy() {
		if (sHandler != null) {
			sHandler.removeCallbacksAndMessages(null);
			sHandler = null;
		}
	}
}