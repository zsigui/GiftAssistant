package net.youmi.android.libs.platform;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.global.GlobalCacheExecutor;

import java.util.List;

/**
 * SDK默认cached线程池，无特殊需求都用这个线程池来执行线程
 *
 * @author zhitaocai
 */
public class Default_SDK_Executor {

	public static void execute(Runnable task) {
		try {
			GlobalCacheExecutor.getCachedThreadPool().execute(task);
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.td(Debug_SDK.mGlobalTag, Default_SDK_Executor.class, "任务已提交至 GlobalCacheExecutor 中执行");
			}
		} catch (Exception e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Default_SDK_Executor.class, e);
			}
		}
	}

	/**
	 * 关闭所有正在执行的任务
	 */
	public static void shutdownNow() {
		try {
			List<Runnable> list = GlobalCacheExecutor.getCachedThreadPool().shutdownNow();
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.td(Debug_SDK.mGlobalTag, Default_SDK_Executor.class, "尝试停止所有正在执行的线程，以及准备执行的线程，成功结束数量： %d", list.size
						());
			}
		} catch (Exception e) {
			if (Debug_SDK.isGlobalLog) {
				Debug_SDK.te(Debug_SDK.mGlobalTag, Default_SDK_Executor.class, e);
			}
		}

	}

}
