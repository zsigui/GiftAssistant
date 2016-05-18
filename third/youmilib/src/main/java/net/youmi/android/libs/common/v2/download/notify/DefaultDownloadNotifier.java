package net.youmi.android.libs.common.v2.download.notify;

import net.youmi.android.libs.common.debug.Debug_SDK;
import net.youmi.android.libs.common.v2.download.base.FinalDownloadStatus;
import net.youmi.android.libs.common.v2.download.listener.IDownloadListener;
import net.youmi.android.libs.common.v2.download.listener.IMaxPriorityDownloadListener;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;

import java.util.List;

/**
 * @author zhitao
 * @since 2015-09-19 16:48
 */
public class DefaultDownloadNotifier extends AbsDownloadNotifier {

	/**
	 * @param IMaxPriorityDownloadListener 传入一个最高回调优先级的下载监听者，只有这个监听者通过聊，才能继续往下回调，可以为空
	 */
	public DefaultDownloadNotifier(IMaxPriorityDownloadListener IMaxPriorityDownloadListener) {
		super(IMaxPriorityDownloadListener);
	}

	public DefaultDownloadNotifier() {
		this(null);
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
	@Override
	public void onNotifyDownloadBeforeStart_FileLock(FileDownloadTask fileDownloadTask) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onDownloadBeforeStart_FileLock(fileDownloadTask)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载前，文件处于文件锁中：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadBeforeStart_FileLock(fileDownloadTask);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载开始：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	@Override
	public void onNotifyDownloadStart(FileDownloadTask fileDownloadTask) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onDownloadStart(fileDownloadTask)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载开始：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadStart(fileDownloadTask);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载进度回调：之类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask 下载任务模型
	 * @param totalLength      本次下载文件的总长度
	 * @param completeLength   已下载的长度
	 * @param percent          当前完成百分比
	 * @param speedBytes
	 * @param intervalTime_ms  当前下载速度时间单位:每intervalTime_ms毫秒回回调一次本方法(单位:bytes)
	 */
	@Override
	public void onNotifyDownloadProgressUpdate(FileDownloadTask fileDownloadTask, long totalLength, long
			completeLength,
	                                           int percent, long speedBytes, long intervalTime_ms) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener
					.onDownloadProgressUpdate(fileDownloadTask, totalLength, completeLength, percent, speedBytes,
							intervalTime_ms)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载中：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadProgressUpdate(fileDownloadTask, totalLength, completeLength, percent,
								speedBytes,
								intervalTime_ms);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载成功：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	@Override
	public void onNotifyDownloadSuccess(FileDownloadTask fileDownloadTask) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onDownloadSuccess(fileDownloadTask)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载成功：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadSuccess(fileDownloadTask);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载成功（文件本来已经存在）：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 */
	@Override
	public void onNotifyFileAlreadyExist(FileDownloadTask fileDownloadTask) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onFileAlreadyExist(fileDownloadTask)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载文件已经存在于本地中：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onFileAlreadyExist(fileDownloadTask);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载失败：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 * @param finalDownloadStatus 下载任务失败数据模型
	 */
	@Override
	public void onNotifyDownloadFailed(FileDownloadTask fileDownloadTask, FinalDownloadStatus finalDownloadStatus) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onDownloadFailed(fileDownloadTask, finalDownloadStatus)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载失败：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadFailed(fileDownloadTask, finalDownloadStatus);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}

	/**
	 * 通知下载停止：子类需要实现具体业务逻辑
	 *
	 * @param fileDownloadTask
	 * @param totalLength      本次下载的总长度
	 * @param completeLength   已下载的长度
	 * @param percent          下载停止时，已经完成的百分比
	 */
	@Override
	public void onNotifyDownloadStop(FileDownloadTask fileDownloadTask, long totalLength, long completeLength, int
			percent) {

		// 如果有传入最高优先级的通知监听器，那么先处理他，如果返回false，则不会通知后面的监听者
		if (mIMaxPriorityDownloadListener != null) {
			if (!mIMaxPriorityDownloadListener.onDownloadStop(fileDownloadTask, totalLength, completeLength,
					percent)) {
				return;
			}
		}

		try {
			final List<IDownloadListener> list = getListeners();
			if (list != null && !list.isEmpty()) {
				if (Debug_SDK.isDownloadLog) {
					Debug_SDK.td(Debug_SDK.mDownloadTag, this, "下载暂停：当前共有[%d]个监听者要处理", list.size());
				}
				for (int i = 0; i < list.size(); ++i) {
					try {
						if (Debug_SDK.isDownloadLog) {
							Debug_SDK
									.ti(Debug_SDK.mDownloadTag, this, "处理第[%d]个监听者[%s]", i + 1, list.get(i).getClass()
											.getName
													());
						}
						list.get(i).onDownloadStop(fileDownloadTask, totalLength, completeLength, percent);
					} catch (Throwable e) {
						Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
					}
				}
			}
		} catch (Throwable e) {
			if (Debug_SDK.isDownloadLog) {
				Debug_SDK.te(Debug_SDK.mDownloadTag, this, e);
			}
		}
	}
}
