package net.youmi.android.libs.common.v2.download.base;

import android.content.Context;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

/**
 * 支持自动重试下载的下载逻辑扩展类
 * <p/>
 * 下载失败时，如果是属于可以重试下载的失败类型，那么将会重新进行下载
 *
 * @author zhitao
 * @since 2015-09-19 10:40
 */
public abstract class AbsAutoRetryFileDownloader implements IDownloader {

	/**
	 * 默认下一次重试的间隔时间:10秒
	 */
	private final static int RETRY_INTERVAL_TIME_MS = 10000;

	/**
	 * 默认最大重试次数:8次
	 */
	private final static int MAX_RETRY_TIMES = 8;

	/**
	 * 下一次重试的时间间隔
	 */
	private int mRetryIntervalTime_ms;

	/**
	 * 最大重试次数
	 */
	private int mMaxRetryTimes;

	/**
	 * 是否正在运行
	 */
	private boolean mIsRunning;

	/**
	 * 是否被主动停止了本次下载任务
	 */
	private boolean mIsStop;

	/**
	 * 抽象的基本下载逻辑类
	 */
	private IDownloader mDownloader;

	/**
	 * 下载任务数据模型
	 */
	private FileDownloadTask mFileDownloadTask;

	private Context mContext;

	/**
	 * @param context
	 * @param maxRetryTimes        最大重试次数
	 * @param retryIntervalTime_ms 下一次重试的时间间隔(ms)
	 *
	 * @pamam fileDownloadTask 下载任务模型
	 */
	public AbsAutoRetryFileDownloader(Context context, FileDownloadTask fileDownloadTask, int maxRetryTimes,
			int retryIntervalTime_ms) {
		mContext = context.getApplicationContext();
		mFileDownloadTask = fileDownloadTask;

		mMaxRetryTimes = maxRetryTimes > 0 ? maxRetryTimes : MAX_RETRY_TIMES;
		mRetryIntervalTime_ms = retryIntervalTime_ms > 0 ? retryIntervalTime_ms : RETRY_INTERVAL_TIME_MS;

		mDownloader = newIDowlonader(mContext, mFileDownloadTask);
		mIsRunning = false;
		mIsStop = false;
	}

	/**
	 * new 一个具体执行下载的实体类
	 *
	 * @return
	 */
	public abstract IDownloader newIDowlonader(Context context, FileDownloadTask fileDownloadTask);

	/**
	 * 开始下载
	 *
	 * @return 下载的最终状态
	 */
	@Override
	public FinalDownloadStatus download() {
		mIsRunning = true;
		mIsStop = false;
		try {
			int mRunCounter = 0;
			while (mIsRunning) {
				++mRunCounter;
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "===第[%d]次下载===", mRunCounter);
				}

				// 从第二次可重试下载开始，要检查一下网络情况
				if (mRunCounter >= 2) {

					if (!NetworkStatus.isNetworkAvailable(mContext)) {

						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "当前网络不可用，等待[%d]毫秒后再次检查网络状态", mRetryIntervalTime_ms);
						}
						try {
							Thread.sleep(mRetryIntervalTime_ms);
						} catch (Throwable e) {
							if (Debug_SDK.isDownloadLog) {
								Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
							}
						}

						// 如果之前检查到网络不行的话，那么一段时间之后，进行下载之前还需要检查一遍网络
						// 如果这个时候网络还是不行的话，就没必要进行下载了
						if (!NetworkStatus.isNetworkAvailable(mContext)) {

							// 如果网络还是不行，则判断是否是否已经达到重试上限
							if (mRunCounter >= mMaxRetryTimes) {
								if (Debug_SDK.isDownloadLog) {
									Debug_SDK.te(Debug_SDK.mDownloadTag, this, "当前网络不可用，重试次数已经达到上限[%d]，结束下载", mMaxRetryTimes);
								}
								// 由于网络不成功导致的重试，达到最大限定次数后取消，同时标记为下载失败
								return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_REACH_MAX_DOWNLOAD_TIMES);
							}
							if (Debug_SDK.isDownloadLog) {
								Debug_SDK.ti(Debug_SDK.mDownloadTag, this, "当前网络不可用");
							}

							// 如果还没有达最大次数就进行下一次循环
							continue;
						}
					}
				}

				FinalDownloadStatus finalDownloadStatus = mDownloader.download();
				if (finalDownloadStatus == null) {
					return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_UNKOWN, new Exception("null status 1"));
				}

				if (FinalDownloadStatus.Code.SUCCESS == finalDownloadStatus.getDownloadStatusCode()) {
					return finalDownloadStatus;

				} else if (FinalDownloadStatus.Code.STOP == finalDownloadStatus.getDownloadStatusCode()) {
					return finalDownloadStatus;

				} else if (finalDownloadStatus.getDownloadStatusCode() >= 150 &&
				           finalDownloadStatus.getDownloadStatusCode() <= 199) {
					// 不可重试的下载失败类型
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, "不可重试下载失败\n%s", finalDownloadStatus.toString());
					}
					return finalDownloadStatus;

				} else if (finalDownloadStatus.getDownloadStatusCode() >= 100 &&
				           finalDownloadStatus.getDownloadStatusCode() <= 149) {
					// 可重试的下载失败类型
					if (Debug_SDK.isDownloadLog) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, "可重试下载失败\n%s", finalDownloadStatus.toString());
					}

					// 如果网络还是不行，则判断是否是否已经达到重试上限
					if (mRunCounter >= mMaxRetryTimes) {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK.te(Debug_SDK.mDownloadTag, this, "下载失败，属于不可重试类型失败，重试次数已经达到上限[%d]，结束下载", mMaxRetryTimes);
						}
						return finalDownloadStatus;
					}
				}
			}

			// 到这里就表示循环重试过程中，下载任务被停止聊
			// 那么这里就返回任务被停止而不是下载失败
			if (isStop()) {
				return new FinalDownloadStatus(FinalDownloadStatus.Code.STOP);
			}

		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
			return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_UNKOWN, e);
		} finally {
			mIsRunning = false;
		}
		return new FinalDownloadStatus(FinalDownloadStatus.Code.FAILED_ERROR_UNKOWN, new Exception("null status 2"));
	}

	/**
	 * 获取本次下载的任务模型
	 *
	 * @return
	 */
	@Override
	public FileDownloadTask getFileDownloadTask() {
		return mFileDownloadTask;
	}

	/**
	 * 获取已经完成的长度
	 *
	 * @return
	 */
	@Override
	public long getCompleteLength() {
		return mDownloader.getCompleteLength();
	}

	/**
	 * 获取下载进度的百分比
	 *
	 * @return
	 */
	@Override
	public int getDownloadPercent() {
		return mDownloader.getDownloadPercent();
	}

	/**
	 * 获取本次下载文件的总长度
	 *
	 * @return
	 */
	@Override
	public long getDownloadFileFinalLength() {
		return mDownloader.getDownloadFileFinalLength();
	}

	/**
	 * 停止下载过程
	 * <p/>
	 * 调用下载停止之后，并不会立即停止下载的，而是程序跑到判断是否需要停止下载的地方才会停止的，
	 */
	@Override
	public void stop() {
		mIsRunning = false;
		mIsStop = true;
		if (mDownloader != null) {
			mDownloader.stop();
		}
	}

	/**
	 * 是否正在下载中
	 *
	 * @return
	 */
	@Override
	public boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * 是否被停止，只有调用了{@link #stop()}才会变为true，而调用了{@link #download()}之后就会变为false
	 *
	 * @return
	 */
	@Override
	public boolean isStop() {
		return mIsStop;
	}
}
