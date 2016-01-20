package net.youmi.android.libs.common.v2.download.base;

import android.content.Context;

import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;

/**
 * 支持自动重试下载的下载逻辑扩展类
 * <p/>
 * 下载失败时，如果是属于可以重试下载的失败类型，那么将会重新进行下载
 *
 * @author zhitao
 * @since 2015-09-19 10:40
 */
public class DefaultAutoRetryFileDownloader extends AbsAutoRetryFileDownloader {

	/**
	 * @param context
	 * @param maxRetryTimes        最大重试次数
	 * @param retryIntervalTime_ms 下一次重试的时间间隔(ms)
	 *
	 * @pamam fileDownloadTask 下载任务模型
	 */
	public DefaultAutoRetryFileDownloader(Context context, FileDownloadTask fileDownloadTask, int maxRetryTimes,
			int retryIntervalTime_ms) {
		super(context, fileDownloadTask, maxRetryTimes, retryIntervalTime_ms);
	}

	/**
	 * @param context
	 *
	 * @pamam fileDownloadTask 下载任务模型
	 */
	public DefaultAutoRetryFileDownloader(Context context, FileDownloadTask fileDownloadTask) {
		this(context, fileDownloadTask, 0, 0);
	}

	/**
	 * new 一个具体执行下载的实体类
	 *
	 * @param context
	 * @param fileDownloadTask
	 *
	 * @return
	 */
	@Override
	public IDownloader newIDowlonader(Context context, FileDownloadTask fileDownloadTask) {
//		if (Build.VERSION.SDK_INT < 9) {
//			return new BaseHttpClientFileDownloader(context, fileDownloadTask);
//		} else {
//			return new BaseHttpURLConnectionFileDownloader(context, fileDownloadTask);
//		}
		//TODO BaseHttpURLConnectionFileDownloader可能有bug
		return new BaseHttpClientFileDownloader(context, fileDownloadTask);
	}

}
