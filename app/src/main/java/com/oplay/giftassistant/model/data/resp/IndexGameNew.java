package com.oplay.giftassistant.model.data.resp;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftassistant.config.AppDebugConfig;
import com.oplay.giftassistant.download.ApkDownloadDir;
import com.oplay.giftassistant.download.ApkDownloadManager;
import com.oplay.giftassistant.model.AppStatus;
import com.oplay.giftassistant.model.DownloadStatus;
import com.oplay.giftassistant.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.common.v2.download.model.IFileDownloadTaskExtendObject;

import java.io.File;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew implements IFileDownloadTaskExtendObject {

	static final long FILE_SIZE_KB = 1024;
	static final long FILE_SIZE_MB = 1024*1024;

	// 游戏标志
	@SerializedName("app_id")
	public int id;

	// 游戏名称
	@SerializedName("game_name")
	public String name;

	// 新增礼包数量
	@SerializedName("new_add_count")
	public int newCount;

	// 拥有礼包总数
	@SerializedName("has_gift_count")
	public int totalCount;

	// 在玩人数
	@SerializedName("plays")
	public int playCount;

	// 游戏大小
	@SerializedName("apk_size")
	public String size;

	@SerializedName("apk_file_size")
	public long apkFileSize;

	// 游戏ICON
	@SerializedName("icon")
	public String img;

	// 主推游戏Banner地址
	@SerializedName("banner")
	public String banner;

	// 最新礼包名
	@SerializedName("gift_name")
	public String giftName;

	// 下载地址
	@SerializedName("download_url")
	public String downloadUrl;

	@SerializedName("apkMd5")
	public String apkMd5;

	@SerializedName("package_name")
	public String packageName;

	@SerializedName("version_name")
	public String versionName;

	public DownloadStatus downloadStatus;

	public AppStatus appStatus;

	public long completeSize;

	private File mDestFile;

	private String mDestFilePath;

	transient Context mContext;

	public void initAppInfoStatus(Context context) {
		setContext(context);
		initDownloadStatus(context);
		appStatus = getAppStatus(downloadStatus);
	}

	private void initDownloadStatus(Context context) {
		downloadStatus = ApkDownloadManager.getInstance(context).getAppDownloadStatus(downloadUrl);
	}


	public final File getDestFile() {
		initFile();
		return mDestFile;
	}

	public final void setDestFile(File destFile) {
		if (destFile != null && !destFile.equals(mDestFile)) {
			mDestFile = destFile;
			mDestFilePath = destFile.getAbsolutePath();
		}
	}
	public final String getDestFilePath() {
		initFile();
		return mDestFilePath;
	}

	public final void setDestFilePath(String destFilePath) {
		if (!TextUtils.isEmpty(destFilePath) && !destFilePath.equalsIgnoreCase(mDestFilePath)) {
			try {
				mDestFilePath = destFilePath;
				mDestFile = new File(destFilePath);
			}catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
		}
	}

	public void initFile() {
		try {
			if (mDestFilePath == null || mDestFile == null) {
				mDestFile = ApkDownloadDir.getInstance(mContext).newDownloadStoreFile(downloadUrl,null);
				mDestFilePath = mDestFile.getAbsolutePath();
			}
		}catch (Exception e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		mContext = context;
	}

	@Override
	public boolean isExtendObjectValid() {
		return true;
	}

	@Override
	public String toSerializableString() {
		return null;
	}

	@Override
	public void fromSerializableString(String string) {

	}

	public void startDownload() {
		try {
			ApkDownloadManager.getInstance(mContext).addDownloadTask(this);
			ToastUtil.showShort("已添加新的下载任务");
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void stopDownload() {
		try {
			ApkDownloadManager.getInstance(mContext).stopDownloadTask(this);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void restartDownload() {
		try {
			ApkDownloadManager.getInstance(mContext).restartDownloadTask(this);
		}catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void startInstall() {
		//TODO 开始安装
	}

	private AppStatus getAppStatus(DownloadStatus ds) {
		boolean isInstalled = Util_System_Package.isPakcageInstall(mContext, packageName);
		if (ds == null) {
			return isInstalled ? AppStatus.OPENABLE : AppStatus.DOWNLOADABLE;
		}

		switch (ds) {
			case DISABLE:
				if (isInstalled) {
					return AppStatus.OPENABLE;
				}
				return AppStatus.DISABLE;
			case DOWNLOADING:
			case PENDING:
				return AppStatus.PAUSABLE;
			case PAUSED:
				return AppStatus.RESUMABLE;
			case FAILED:
				return AppStatus.RETRYABLE;
			case FINISHED:
				if (isInstalled) {
					return AppStatus.OPENABLE;
				}else {
					return AppStatus.INSTALLABLE;
				}
		}
		return null;
	}

	public String getApkFileSizeStr() {
		if (apkFileSize >= FILE_SIZE_MB) {
			return String.format("%1$.1fMB", (1.0f) * apkFileSize / FILE_SIZE_MB);
		}else {
			return String.format("%1$.1fKB", (1.0f) * apkFileSize / FILE_SIZE_KB);
		}
	}

	public String getCompleteSizeStr() {
		if (completeSize >= FILE_SIZE_MB) {
			return String.format("%1$.1fMB", (1.0f) * completeSize / FILE_SIZE_MB);
		}else {
			return String.format("%1$.1fKB", (1.0f) * completeSize / FILE_SIZE_KB);
		}
	}
}
