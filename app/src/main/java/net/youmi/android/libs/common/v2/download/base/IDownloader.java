package net.youmi.android.libs.common.v2.download.base;

import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;

/**
 * 下载核心逻辑
 *
 * @author zhitao
 * @since 2015-09-19 09:03
 */
public interface IDownloader {

	/**
	 * 获取本次下载的任务模型
	 *
	 * @return
	 */
	FileDownloadTask getFileDownloadTask();

	/**
	 * 获取本次下载文件的总长度
	 *
	 * @return
	 */
	long getDownloadFileFinalLength();

	/**
	 * 获取已经完成的长度
	 *
	 * @return
	 */
	long getCompleteLength();

	/**
	 * 获取下载进度的百分比
	 *
	 * @return
	 */
	int getDownloadPercent();

	/**
	 * 开始下载
	 *
	 * @return 下载的最终状态
	 */
	FinalDownloadStatus download();

	/**
	 * 停止下载过程
	 * <p/>
	 * 调用下载停止之后，并不会立即停止下载的，而是程序跑到判断是否需要停止下载的地方才会停止的，
	 */
	void stop();

	/**
	 * 是否被停止，只有调用了{@link #stop()}才会变为true，而调用了{@link #download()}之后就会变为false
	 *
	 * @return
	 * @see net.youmi.android.libs.common.v2.download.base.IDownloader#isRunning
	 */
	boolean isStop();

	/**
	 * 是否正在下载中，调用了{@link #download()}之后就会变为true，下载结束或者调用了{@link #stop()}都会变为false
	 * <p/>
	 * {@link #isRunning()} 和 {@link #isStop()}的区别
	 * 1. 任务下载结束(成功或者失败)，或者被用户主动停止下载任务，isRunning都为false，
	 * 2. 只有用户主动停止下载任务（即调用{@link #stop()}，isStop才会是true，
	 * 当再次调用{@link #download()}方法之后，isStop就会变为false
	 *
	 * @return
	 */
	boolean isRunning();

}
