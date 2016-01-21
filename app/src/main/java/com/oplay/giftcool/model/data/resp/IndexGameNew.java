package com.oplay.giftcool.model.data.resp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.download.ApkDownloadDir;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.InstallAppUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import net.youmi.android.libs.common.util.Util_System_Intent;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.common.v2.download.model.IFileDownloadTaskExtendObject;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

import java.io.File;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew implements IFileDownloadTaskExtendObject {

	static final long FILE_SIZE_KB = 1024;
	static final long FILE_SIZE_MB = 1024 * 1024;
	public static final int FAKE_INIT_PROGRESS = 1;

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

	@SerializedName("apk_filesize")
	public long apkFileSize;

	// 游戏ICON
	@SerializedName("icon")
	public String img;

	// 主推游戏Banner地址
	@SerializedName("stroll_img_url")
	public String banner;

	// 最新礼包名
	@SerializedName("gift_name")
	public String giftName;

	// 下载地址
	@SerializedName("download_url")
	public String downloadUrl;

	//最终下载地址
	@SerializedName("cdn_download_url")
	public String destUrl;

	@SerializedName("apk_md5")
	public String apkMd5;

	@SerializedName("package_name")
	public String packageName;

	@SerializedName("version_name")
	public String versionName;

	@Expose
	public DownloadStatus downloadStatus;

	@Expose
	public AppStatus appStatus = AppStatus.DOWNLOADABLE;

	@Expose
	public long completeSize;

	@Expose
	private File mDestFile;

	@Expose
	private String mDestFilePath;

	@Expose
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
			} catch (Throwable e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.e(e);
				}
			}
		}
	}

	public boolean isFileExists() {
		return mDestFile != null && mDestFile.exists();
	}

	public void initFile() {
		try {
			if (mDestFilePath == null || mDestFile == null) {
				mDestFile = ApkDownloadDir.getInstance(mContext).newDownloadStoreFile(downloadUrl, destUrl);
				mDestFilePath = mDestFile.getAbsolutePath();
			}
		} catch (Exception e) {
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
		} catch (Throwable e) {
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
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void startInstall() {
		try {
			if (AssistantApp.getInstance().isShouldAutoInstall()) {
				InstallAppUtil.install(mContext, this);
			}
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
	}

	public void startApp() {
		try {
			Util_System_Intent.startActivityByPackageName(mContext, packageName);
		} catch (Throwable e) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(e);
			}
		}
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
				} else {
					return AppStatus.INSTALLABLE;
				}
		}
		return null;
	}

	public String getApkFileSizeStr() {
		if (apkFileSize >= FILE_SIZE_MB) {
			return String.format("%1$.1fMB", (1.0f) * apkFileSize / FILE_SIZE_MB);
		} else {
			return String.format("%1$.1fKB", (1.0f) * apkFileSize / FILE_SIZE_KB);
		}
	}

	public String getCompleteSizeStr() {
		if (completeSize >= FILE_SIZE_MB) {
			return String.format("%1$.1fMB", (1.0f) * completeSize / FILE_SIZE_MB);
		} else {
			return String.format("%1$.1fKB", (1.0f) * completeSize / FILE_SIZE_KB);
		}
	}

	public void handleOnClick(FragmentManager fragmentManager) {
		if (AppDebugConfig.IS_DEBUG) {
			AppDebugConfig.logMethodWithParams(this, appStatus);
		}
		if (appStatus == null) {
			if (AppDebugConfig.IS_DEBUG) {
				AppDebugConfig.logMethodWithParams(this, packageName, "appStatus NULL!!!");
			}
			return;
		}
		switch (appStatus) {
			case DOWNLOADABLE:
			case UPDATABLE:
				if (NetworkUtil.isWifiAvailable(mContext)) {
					startDownload();
				} else {
					final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
					confirmDialog.setTitle("提示");
					confirmDialog.setContent("您当前是移动网络状态，下载游戏会消耗手机流量");
					confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
						@Override
						public void onCancel() {
							confirmDialog.dismiss();
						}

						@Override
						public void onConfirm() {
							startDownload();
							confirmDialog.dismiss();
						}
					});
					confirmDialog.show(fragmentManager, "download");
				}
				break;
			case INSTALLABLE:
				initFile();
				startInstall();
				break;
			case OPENABLE:
				startApp();
				break;
			case PAUSABLE:
				stopDownload();
				break;
			case RESUMABLE:
			case RETRYABLE:
				if (NetworkStatus.getNetworkType(mContext) == NetworkStatus.Type.TYPE_WIFI) {
					restartDownload();
				} else {
					ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
					confirmDialog.setTitle("提示");
					confirmDialog.setContent("您当前是移动网络状态，下载游戏会消耗手机流量");
					confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
						@Override
						public void onCancel() {

						}

						@Override
						public void onConfirm() {
							startDownload();
						}
					});
					confirmDialog.show(fragmentManager, "download");
				}
				break;
			case DISABLE:
			default:
				break;

		}
	}

	public boolean isValid() {
		return !(id == 0 || TextUtils.isEmpty(name) || apkFileSize == 0 || TextUtils.isEmpty(img) || TextUtils.isEmpty
				(downloadUrl) || TextUtils.isEmpty(destUrl) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty
				(versionName));
	}
}
