package com.jackiez.giftassistant.util;

import android.os.Handler;

/**
 * Created by zsigui on 15-12-17.
 */
public class ThreadUtil {
	private static Handler sHandler = new Handler();

	private ThreadUtil() {
	}

	/**
	 * 在子线程执行任务
	 *
	 * @param task
	 */
	public static void runInThread(Runnable task) {
		new Thread(task).start();
	}

	/**
	 * 在UI线程执行任务
	 *
	 * @param task
	 */
	public static void runInUIThread(Runnable task) {
		sHandler.post(task);
	}

	/**
	 * 在UI线程延时执行任务
	 *
	 * @param task
	 * @param delayMillis 延时时间，单位毫秒
	 */
	public static void runInUIThread(Runnable task, long delayMillis) {
		sHandler.postDelayed(task, delayMillis);
	}
}