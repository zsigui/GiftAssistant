package com.oplay.giftcool.download;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.database.DownloadDBHelper;
import com.oplay.giftcool.download.listener.OnDownloadStatusChangeListener;
import com.oplay.giftcool.download.listener.OnInstallListener;
import com.oplay.giftcool.download.listener.OnProgressUpdateListener;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.util.SoundPlayer;
import com.socks.library.KLog;

import net.youmi.android.libs.common.v2.download.BaseApkCachedDownloadManager;
import net.youmi.android.libs.common.v2.download.base.FinalDownloadStatus;
import net.youmi.android.libs.common.v2.download.core.AbsDownloader;
import net.youmi.android.libs.common.v2.download.core.DefaultAPPDownloader;
import net.youmi.android.libs.common.v2.download.listener.IFileAvailableChecker;
import net.youmi.android.libs.common.v2.download.model.FileDownloadTask;
import net.youmi.android.libs.common.v2.download.notify.AbsDownloadNotifier;
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
public class ApkDownloadManager extends BaseApkCachedDownloadManager implements OnInstallListener {

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
		InstallNotifier.getInstance().addListener(this);
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
		DownloadNotificationManager.showDownload(mApplicationContext);
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

		// 完成下载任务，请求奖励积分
		ScoreManager.getInstance().setRewardType(ScoreManager.RewardType.BIND_DOWNLOAD);
		ScoreManager.getInstance().reward();
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
						appInfo.startInstall();
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
		DownloadNotificationManager.showDownload(mApplicationContext);
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
		DownloadNotificationManager.showDownload(mApplicationContext);
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
			FileDownloadTask task = new FileDownloadTask(appInfo.downloadUrl, null, -1, 500);
			task.setIdentify(appInfo.destUrl);
			stopDownload(task);
			if (mPendingCnt > 0) {
				IndexGameNew info = mManagerList.get(mDownloadingCnt);
				apkDownload(info);
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
		FileDownloadTask task = new FileDownloadTask(appInfo.downloadUrl, null, -1, 1000);
		task.setIdentify(appInfo.destUrl);
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
						try {
							arrays[i].onDownloadStatusChanged(appInfo);
						} catch (Throwable e) {
							KLog.e(e);
						}
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

	public long getCompleteSizeByUrl(String url) {
		final IndexGameNew appInfo = mUrl_AppInfo.get(url);
		if (appInfo != null) {
			return appInfo.completeSize;
		}
		return 0;
	}

	public int getProgressByUrl(String url) {
		final IndexGameNew appInfo = mUrl_AppInfo.get(url);
		if (appInfo != null && appInfo.apkFileSize > 0) {
			return (int) (appInfo.completeSize * 100 / appInfo.apkFileSize);
		}
		return 0;
	}

	public List<IndexGameNew> getDownloadList() {
		initStatus();
		return mManagerList;
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
			try {
				if (AssistantApp.getInstance().isPlayDownloadComplete()) {
					SoundPlayer.getInstance(mApplicationContext).playDownloadComplete();
				}
			} catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
			stopDownloadingTask(appInfo);
			appInfo.downloadStatus = DownloadStatus.FINISHED;
			appInfo.completeSize = appInfo.apkFileSize;
			mManagerList.add(mDownloadingCnt + mPendingCnt + mPausedCnt, appInfo);
			mFinishedCnt++;
			notifyDownloadStatusListeners(appInfo);
			DownloadNotificationManager.showDownloadComplete(mApplicationContext, appInfo);
			DownloadNotificationManager.showDownload(mApplicationContext);
			appInfo.startInstall();
		}
		return false;
	}

	@Override
	public boolean onFileAlreadyExist(FileDownloadTask fileDownloadTask) {
		IndexGameNew appInfo = mUrl_AppInfo.get(fileDownloadTask.getRawDownloadUrl());
		if (appInfo != null) {
			stopDownloadingTask(appInfo);
			mManagerList.add(getEndOfPaused(), appInfo);
			appInfo.downloadStatus = DownloadStatus.FINISHED;
			appInfo.completeSize = appInfo.apkFileSize;
			notifyDownloadStatusListeners(appInfo);
			mFinishedCnt++;
			DownloadNotificationManager.showDownload(mApplicationContext);
			appInfo.startApp();
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
				DownloadNotificationManager.showDownloadFailed(mApplicationContext, appInfo.destUrl, appInfo.name,
						"手机内存空间不足");
			} else {
				DownloadNotificationManager.showDownloadFailed(mApplicationContext, appInfo.destUrl, appInfo.name,
						null);
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

	@Override
	public AbsDownloader newDownloader(Context context, AbsDownloadDir absDownloadDir, FileDownloadTask
			fileDownloadTask, AbsDownloadNotifier absDownloadNotifier, IFileAvailableChecker iFileAvailableChecker)
			throws NullPointerException, IOException {
		return new DefaultAPPDownloader(context, absDownloadDir, fileDownloadTask, absDownloadNotifier,
				iFileAvailableChecker);
	}

	@Override
	public void onInstall(Context context, String packageName) {
		final IndexGameNew appInfo = getAppInfoByPackageName(packageName);
		if (appInfo != null) {
			appInfo.initAppInfoStatus(context);
			notifyDownloadStatusListeners(appInfo);
		}
	}
}
