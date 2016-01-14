package com.oplay.giftassistant.download;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.database.DownloadDBHelper;
import com.oplay.giftassistant.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftassistant.download.listener.OnProgressUpdateListener;
import com.oplay.giftassistant.model.DownloadStatus;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.socks.library.KLog;

import net.youmi.android.libs.common.v2.download.BaseApkCachedDownloadManager;
import net.youmi.android.libs.common.v2.download.base.FinalDownloadStatus;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.storer.AbsDownloadDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ApkDownloadManager
 *
 * @author zacklpx
 *         date 16-1-4
 *         description
 */
public class ApkDownloadManager extends BaseApkCachedDownloadManager {

	private final static int TAG_APPINFO = 0xffff0011;
	private static ApkDownloadManager mInstance = null;

	private static final int MAX_DOWNLOADING_COUNT = 3;
	private static DownloadDBHelper mDownloadDBHelper = null;

	private final int DOWNLOADFINISHED_PERCENT = 100;
	private final int DOWNLOADSTART_PERCENT = 0;
	private Map<String, IndexGameNew> mUrl_AppInfo;
	private Map<String, IndexGameNew> mPackageName_AppInfo;

	private List<IndexGameNew> mManagerList;
	private int mDownloadingCnt;
	private int mPendingCnt;
	private int mPausedCnt;
	private int mFinishedCnt;

	//监听器队列
	private List<OnDownloadStatusChangeListener> mDownloadStatusChangeListeners;
	private List<OnProgressUpdateListener> mOnProgressUpdateListeners;
	private ReentrantLock mNotifyStatusLock;

	protected ApkDownloadManager(Context context) throws NullPointerException, IOException {
		super(context);
		mManagerList = new ArrayList<>();
		mDownloadingCnt = 0;
		mPendingCnt = 0;
		mPausedCnt = 0;
		mFinishedCnt = 0;
		mUrl_AppInfo = new HashMap<>();
		mPackageName_AppInfo = new HashMap<>();
		mDownloadStatusChangeListeners = new LinkedList<>();
		mOnProgressUpdateListeners = new LinkedList<>();
		mDownloadDBHelper = DownloadDBHelper.getInstance(context);
		addDownloadStatusListener(mDownloadDBHelper);
		mNotifyStatusLock = new ReentrantLock();
	}

	public static ApkDownloadManager getInstance(Context context) {
		try {
			if (mInstance == null) {
				mInstance = new ApkDownloadManager(context);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
		return mInstance;
	}

	public void initDownloadList() {
		mDownloadDBHelper.getDownloadList();
	}

	//仅限于初始化的时候用
	public void addPausedTask(IndexGameNew info) {
		if (!checkDownloadTask(info)) {
			return;
		}
		mManagerList.add(getEndOfPaused(), info);
		mPausedCnt++;
		mUrl_AppInfo.put(info.downloadUrl, info);
		mPackageName_AppInfo.put(info.packageName, info);
	}

	public void addFinishedTask(IndexGameNew info) {
		if (!checkDownloadTask(info)) {
			return;
		}
		mManagerList.add(getEndOfFinished(), info);
		mFinishedCnt++;
		mUrl_AppInfo.put(info.downloadUrl, info);
		mPackageName_AppInfo.put(info.packageName, info);
	}

	public void addDownloadTask(IndexGameNew appInfo) {
		if (!checkDownloadTask(appInfo)) {
			return;
		}
		final String apkUrl = appInfo.downloadUrl;
		//下载包已经在队列中
		if (mUrl_AppInfo.containsKey(apkUrl)) {
			final IndexGameNew info = mUrl_AppInfo.get(apkUrl);
			if (info == null || appInfo.downloadStatus == null) {
				AppDebugConfig.logMethodWithParams(this, "下载内容错误");
				return;
			}
			final DownloadStatus status = info.downloadStatus;
			switch (status) {
				case DISABLE:
					AppDebugConfig.logMethodWithParams(this, "The Task status invalid: DISABLE CAN NOT BE DOWNLOAD!");
					return;
				case PENDING:
				case DOWNLOADING: {
					AppDebugConfig.logMethodWithParams(this, "The Task is downloading!");
					return;
				}
				case PAUSED:
				case FAILED: {
					AppDebugConfig.logMethodWithParams(this, "The Task is not downloading, restart task!");
					restartDownloadTask(appInfo);
					return;
				}
				case FINISHED: {
					File apkFile = appInfo.getDestFile();
					if (apkFile != null && apkFile.exists()) {
						AppDebugConfig.logMethodWithParams(this, "文件已存在,直接安装");
						//TODO 安装APK
						return;
					} else {
						AppDebugConfig.logMethodWithParams(this, "文件不存在,重置任务,重新下载安装");
						deleteDownloadInfo(info);
						break;
					}
				}
				default: {
					AppDebugConfig.logMethodWithParams(this, "appInfo status invalid");
					deleteDownloadInfo(info);
					return;
				}
			}
		}
		mUrl_AppInfo.put(appInfo.downloadUrl, appInfo);
		mPackageName_AppInfo.put(appInfo.packageName, appInfo);
		addPendingTask(appInfo);
		//TODO 显示Notification
	}

	public void restartDownloadTask(IndexGameNew appInfo) {
		if (!checkDownloadTask(appInfo)) {
			return;
		}
		IndexGameNew info = mUrl_AppInfo.get(appInfo.downloadUrl);
		if (info != null) {
			if (mManagerList.remove(info)) {
				mPausedCnt = decrease(mPausedCnt);
				addPendingTask(info);
			}
		}
	}

	public void stopDownloadTask(IndexGameNew appInfo) {
		appInfo = mUrl_AppInfo.get(appInfo.downloadUrl);
		if (!checkDownloadTask(appInfo)) {
			return;
		}
		appInfo = stopDownloadingTask(appInfo);
		if (appInfo != null) {
			mManagerList.add(mDownloadingCnt + mPendingCnt, appInfo);
			appInfo.downloadStatus = DownloadStatus.PAUSED;
			mPausedCnt++;
			notifyDownloadStatusListeners(appInfo);
		}
	}

	public synchronized void removeDownloadTask(String url) {
		IndexGameNew info = mUrl_AppInfo.get(url);
		if (!checkDownloadTask(info)) {
			return;
		}
		DownloadStatus ds = info.downloadStatus;
		switch (ds) {
			case DOWNLOADING:
				stopDownloadingTask(info);
				break;
			case PENDING:
				mManagerList.remove(info);
				mPendingCnt = decrease(mPendingCnt);
				break;
			case PAUSED:
			case FAILED:
				mManagerList.remove(info);
				mPausedCnt = decrease(mPausedCnt);
				break;
			case FINISHED:
				mManagerList.remove(info);
				mFinishedCnt = decrease(mFinishedCnt);
				break;
		}
		mUrl_AppInfo.remove(info.downloadUrl);
		mPackageName_AppInfo.remove(info.packageName);
		notifyDownloadStatusListeners(info);
		//TODO 更新Notification
	}

	public int getEndOfDownloading() {
		return mDownloadingCnt + mPendingCnt;
	}

	public int getEndOfPaused() {
		return getEndOfDownloading() + mPausedCnt;
	}

	public int getEndOfFinished() {
		return getEndOfPaused() + mFinishedCnt;
	}

	private synchronized void addPendingTask(IndexGameNew appInfo) {
		mManagerList.add(getEndOfDownloading(), appInfo);
		appInfo.downloadStatus = DownloadStatus.PENDING;
		mPendingCnt++;
		notifyDownloadStatusListeners(appInfo);
		if (mDownloadingCnt < MAX_DOWNLOADING_COUNT) {
			apkDownload(appInfo);
			IndexGameNew info = mManagerList.remove(mDownloadingCnt);
			mPendingCnt = decrease(mPendingCnt);
			info.downloadStatus = DownloadStatus.DOWNLOADING;
			mManagerList.add(mDownloadingCnt, info);
			mDownloadingCnt++;
			notifyDownloadStatusListeners(info);
		}
	}

	private synchronized IndexGameNew stopDownloadingTask(IndexGameNew appInfo) {
		if (appInfo == null) {
			return null;
		}
		mManagerList.remove(appInfo);
		if (DownloadStatus.DOWNLOADING.equals(appInfo.downloadStatus)) {
			mDownloadingCnt = decrease(mDownloadingCnt);
			stopDownload(appInfo.downloadUrl);
			if (mPendingCnt > 0) {
				IndexGameNew info = mManagerList.get(mDownloadingCnt);
				apkDownload(appInfo);
				mManagerList.remove(info);
				mPendingCnt = decrease(mPendingCnt);
				info.downloadStatus = DownloadStatus.DOWNLOADING;
				mManagerList.add(mDownloadingCnt, info);
				mDownloadingCnt++;
			}
		} else {
			mPendingCnt = decrease(mPendingCnt);
		}
		return appInfo;
	}

	private void deleteDownloadInfo(IndexGameNew appInfo) {
		if (appInfo != null) {
			final IndexGameNew info = mUrl_AppInfo.get(appInfo.downloadUrl);
			if (info != null) {
				info.downloadStatus = null;
				mUrl_AppInfo.remove(appInfo.downloadUrl);
				mPackageName_AppInfo.remove(appInfo.packageName);
				mManagerList.remove(info);
				appInfo.initAppInfoStatus(mApplicationContext);
			}
		}
	}

	private void apkDownload(IndexGameNew appInfo) {
		FileDownloadTask task = new FileDownloadTask(appInfo.downloadUrl, appInfo.apkMd5, appInfo.apkFileSize, 1000);
		task.addIFileDownloadTaskExtendObject(TAG_APPINFO, appInfo);
		download(task, true);
	}

	private int decrease(int number) {
		return number - 1 < 0 ? 0 : number - 1;
	}

	/**
	 * ***************************************************************************
	 * 下载状态改变后，负责给监听器发消息
	 * ***************************************************************************
	 */
	public synchronized void addDownloadStatusListener(OnDownloadStatusChangeListener listener) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			if (listener != null && !mDownloadStatusChangeListeners.contains(listener)) {
				mDownloadStatusChangeListeners.add(listener);
			}
		} catch (Exception e) {
			KLog.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	public synchronized void removeDownloadStatusListener(OnDownloadStatusChangeListener listener) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			if (listener != null) {
				mDownloadStatusChangeListeners.remove(listener);
			}
		} catch (Exception e) {
			KLog.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	private void notifyDownloadStatusListeners(IndexGameNew appInfo) {
		try {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.lock();
			}
			int size;
			OnDownloadStatusChangeListener[] arrays;
			size = mDownloadStatusChangeListeners.size();
			arrays = new OnDownloadStatusChangeListener[size];
			mDownloadStatusChangeListeners.toArray(arrays);
			if (arrays != null) {
				for (int i = 0; i < size; i++) {
					if (arrays[i] != null) {
						arrays[i].onDownloadStatusChanged(appInfo);
					}
				}
			}
		} catch (Exception e) {
			KLog.e(e);
		} finally {
			if (mNotifyStatusLock != null) {
				mNotifyStatusLock.unlock();
			}
		}
	}

	public synchronized void removeProgressUpdateListener(OnProgressUpdateListener listener) {
		if (listener != null) {
			mOnProgressUpdateListeners.remove(listener);
		}
	}

	public synchronized void addProgressUpdateListener(OnProgressUpdateListener listener) {
		if (listener != null && !mOnProgressUpdateListeners.contains(listener)) {
			mOnProgressUpdateListeners.add(listener);
		}
	}

	private void notifyProgressUpdateListeners(String url, int percent, long speedBytePerS) {
		int size;
		OnProgressUpdateListener[] arrays;
		synchronized (this) {
			size = mOnProgressUpdateListeners.size();
			arrays = new OnProgressUpdateListener[size];
			mOnProgressUpdateListeners.toArray(arrays);
		}
		if (arrays != null) {
			for (int i = 0; i < arrays.length; i++) {
				if (arrays[i] != null) {
					arrays[i].onProgressUpdate(url, percent, speedBytePerS);
				}
			}
		}
	}

	private boolean checkDownloadTask(IndexGameNew appInfo) {
		return !(appInfo == null || TextUtils.isEmpty(appInfo.packageName) || TextUtils.isEmpty(appInfo.downloadUrl));
	}

	public DownloadStatus getAppDownloadStatus(String url) {
		IndexGameNew info = mUrl_AppInfo.get(url);
		return info != null ? info.downloadStatus : null;
	}

	public IndexGameNew getAppInfoByPackageName(String packageName) {
		return mPackageName_AppInfo.get(packageName);
	}

	public IndexGameNew getAppInfoByUrl(String url) {
		return mUrl_AppInfo.get(url);
	}

	public List<IndexGameNew> getDownloadList() {
		initStatus();
		return new ArrayList<>(mManagerList);
	}

	private void initStatus() {
		for (IndexGameNew i : mManagerList) {
			i.initAppInfoStatus(mApplicationContext);
		}
	}

	@Override
	public AbsDownloadDir newDownloadDir() throws IOException {
		return ApkDownloadDir.getInstance(mApplicationContext);
	}

	@Override
	public boolean onDownloadBeforeStart_FileLock(FileDownloadTask fileDownloadTask) {
		return false;
	}

	@Override
	public boolean onDownloadStart(FileDownloadTask fileDownloadTask) {
		return false;
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
	public boolean onDownloadProgressUpdate(FileDownloadTask fileDownloadTask, long totalLength, long completeLength,
	                                        int percent, long speedBytes, long intervalTime_ms) {
		final String downloadUrl = fileDownloadTask.getRawDownloadUrl();
		if (downloadUrl != null) {
			IndexGameNew info = mUrl_AppInfo.get(downloadUrl);
			if (info != null && totalLength > 0) {
				info.completeSize = completeLength;
			}
		}
		notifyProgressUpdateListeners(downloadUrl, percent, speedBytes);
		return false;
	}

	@Override
	public boolean onDownloadSuccess(FileDownloadTask fileDownloadTask) {
		IndexGameNew appInfo = mUrl_AppInfo.get(fileDownloadTask.getRawDownloadUrl());
		if (appInfo != null) {
			//TODO 播放完成音乐
			stopDownloadingTask(appInfo);
			appInfo.downloadStatus = DownloadStatus.FINISHED;
			appInfo.completeSize = appInfo.apkFileSize;
			mManagerList.add(mDownloadingCnt + mPendingCnt + mPausedCnt, appInfo);
			mFinishedCnt++;
			notifyDownloadStatusListeners(appInfo);
			//TODO 更新Notification
			//TODO 自动安装
		}
		return false;
	}

	@Override
	public boolean onFileAlreadyExist(FileDownloadTask fileDownloadTask) {
		IndexGameNew appInfo = mUrl_AppInfo.get(fileDownloadTask.getRawDownloadUrl());
		if (appInfo != null) {
			stopDownloadingTask(appInfo);
			mManagerList.add(getEndOfPaused(), appInfo);
			mFinishedCnt++;
			appInfo.downloadStatus = DownloadStatus.FINISHED;
			appInfo.completeSize = appInfo.apkFileSize;
			notifyDownloadStatusListeners(appInfo);
			//TODO 更新Notification
			//TODO 自动安装
		}
		return false;
	}

	@Override
	public boolean onDownloadFailed(FileDownloadTask fileDownloadTask, FinalDownloadStatus finalDownloadStatus) {
		IndexGameNew appInfo = mUrl_AppInfo.get(fileDownloadTask.getRawDownloadUrl());
			if (appInfo != null) {
				stopDownloadingTask(appInfo);
				appInfo.downloadStatus = DownloadStatus.FAILED;
				mManagerList.add(mDownloadingCnt + mPendingCnt, appInfo);
				mPausedCnt++;
				File parent = new File(fileDownloadTask.getStoreFile().getParent());
				if (parent.getUsableSpace() < fileDownloadTask.getTotalLength()) {
					//TODO 提示空间不足
				}else {
					//TODO 提示下载失败
				}
				notifyDownloadStatusListeners(appInfo);
		}
		return false;
	}

	@Override
	public boolean onDownloadStop(FileDownloadTask fileDownloadTask, long totalLength, long completeLength, int
			percent) {
		return false;
	}
}