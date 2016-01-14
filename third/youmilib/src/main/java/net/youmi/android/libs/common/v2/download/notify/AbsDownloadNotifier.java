package net.youmi.android.libs.common.v2.download.notify;

import net.youmi.android.libs.common.v2.download.base.FinalDownloadStatus;
import net.youmi.android.libs.common.v2.download.listener.IDownloadListener;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.template.TListenersManager;

/**
 * 下载观察者回调通知管理器
 *
 * @author zhitao
 * @since 2015-09-10 09:34
 */
public abstract class AbsDownloadNotifier extends TListenersManager<IDownloadListener> {

	protected IMaxPriorityDownloadListener mIMaxPriorityDownloadListener;

	/**
	 * @param IMaxPriorityDownloadListener 传入一个最高回调优先级的下载监听者，只有这个监听者通过聊，才能继续往下回调，可以为空
	 */
	public AbsDownloadNotifier(IMaxPriorityDownloadListener IMaxPriorityDownloadListener) {
		mIMaxPriorityDownloadListener = IMaxPriorityDownloadListener;
	}

	/**
	 * 通知任务当前处于文件锁
	 * <p/>
	 * 一般需要重写的场合为SDK下载，APP应用类不需要重写这个方法:
	 * <p/>
	 * 因为sdk一般是很多个app同时使用的，所以下载的文件基本是公用的，
	 * 这个时候，在下载之前就需要检查下下载文件是否已经被其他进程读取中，
	 * 如果是的话，这里要通知一下
	 *
	 * @param fileDownloadTask
	 */
	public abstract void onNotifyDownloadBeforeStart_FileLock(FileDownloadTask fileDownloadTask);

	/**
	 * 通知下载开始：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	public abstract void onNotifyDownloadStart(FileDownloadTask fileDownloadTask);

	/**
	 * 通知下载进度回调：之类需要实现具体业务逻辑
	 *
	 * @param task            下载任务模型
	 * @param totalLength     本次下载文件的总长度
	 * @param completeLength  已下载的长度
	 * @param percent         当前完成百分比
	 * @param speedBytesPerS  当前下载速度:每intervalTime_ms毫秒下载的长度(单位:bytes)
	 * @param intervalTime_ms 当前下载速度时间单位:每intervalTime_ms毫秒回回调一次本方法(单位:bytes)
	 */
	public abstract void onNotifyDownloadProgressUpdate(FileDownloadTask task, long totalLength, long completeLength, int
			percent,
			long speedBytes, long intervalTime_ms);

	/**
	 * 通知下载成功：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	public abstract void onNotifyDownloadSuccess(FileDownloadTask fileDownloadTask);

	/**
	 * 通知下载成功（文件本来已经存在）：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	public abstract void onNotifyFileAlreadyExist(FileDownloadTask fileDownloadTask);

	/**
	 * 通知下载失败：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 * @param finalDownloadStatus 下载任务失败数据模型
	 */
	public abstract void onNotifyDownloadFailed(FileDownloadTask fileDownloadTask, FinalDownloadStatus finalDownloadStatus);

	/**
	 * 通知下载停止：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 * @param totalLength      本次下载的总长度
	 * @param completeLength   已下载的长度
	 * @param percent          下载停止时，已经完成的百分比
	 */
	public abstract void onNotifyDownloadStop(FileDownloadTask fileDownloadTask, long totalLength, long completeLength,
			int percent);

}
