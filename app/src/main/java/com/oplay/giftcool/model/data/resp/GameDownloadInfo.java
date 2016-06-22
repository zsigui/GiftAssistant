package com.oplay.giftcool.model.data.resp;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.download.ApkDownloadDir;
import com.oplay.giftcool.download.ApkDownloadManager;
import com.oplay.giftcool.download.DownloadNotificationManager;
import com.oplay.giftcool.download.silent.SilentDownloadManager;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.model.AppStatus;
import com.oplay.giftcool.model.DownloadStatus;
import com.oplay.giftcool.ui.fragment.dialog.ConfirmDialog;
import com.oplay.giftcool.util.InstallAppUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import net.youmi.android.libs.common.util.Util_System_Intent;
import net.youmi.android.libs.common.util.Util_System_Package;
import net.youmi.android.libs.common.v2.download.model.IFileDownloadTaskExtendObject;

import java.io.File;
import java.util.Locale;

/**
 * GameDownloadInfo
 *
 * @author zacklpx
 *         date 16-1-26
 *         description
 */
public class GameDownloadInfo implements IFileDownloadTaskExtendObject {

    static final long FILE_SIZE_KB = 1024;
    static final long FILE_SIZE_MB = 1024 * 1024;
    public static final int FAKE_INIT_PROGRESS = 1;

    // 游戏标志
    @SerializedName("app_id")
    public int id;

    // 游戏名称
    @SerializedName("game_name")
    public String name;

    // 游戏大小
    @SerializedName("apk_size")
    public String size;

    @SerializedName("apk_filesize")
    public long apkFileSize;

    // 游戏ICON
    @SerializedName("icon")
    public String img;

    // 下载地址
    @SerializedName("download_url")
    public String downloadUrl;

    // 是否静默下载，默认都为否，非静默下载
    @SerializedName("is_silent_download")
    public boolean isSilent = false;

    //最终下载地址
    @SerializedName("cdn_download_url")
    public String destUrl;

    @SerializedName("apk_md5")
    public String apkMd5;

    @SerializedName("package_name")
    public String packageName;

    @SerializedName("version_name")
    public String versionName;

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
                AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
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
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    public void startDownload() {
        try {
            // 当处于下载，取消此处可能的下载
            SilentDownloadManager.getInstance().quickDownload(downloadUrl);
            ApkDownloadManager.getInstance(mContext).addDownloadTask(this);
            if (!isSilent) {
                ToastUtil.showShort(ConstString.TOAST_ADD_NEW_DOWNLOAD_TASK);
            }
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public void stopDownload() {
        try {
            ApkDownloadManager.getInstance(mContext).stopDownloadTask(this);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public void restartDownload() {
        try {
            if (appStatus == AppStatus.RETRYABLE) {
                DownloadNotificationManager.clearDownloadComplete(mContext, destUrl);
            }
            ApkDownloadManager.getInstance(mContext).restartDownloadTask(this);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public void startInstall() {
        try {
            if (isFileExists()) {
                InstallAppUtil.install(mContext, this);
                handlePlayDownloadTask(mContext, packageName);
            } else {
                ToastUtil.showShort(ConstString.TOAST_INSTALL_FAIL_FOR_NO_APK);
                DownloadNotificationManager.clearDownloadComplete(mContext, destUrl);
                ApkDownloadManager.getInstance(mContext).removeDownloadTask(downloadUrl, true);
                initAppInfoStatus(mContext);
            }
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    public void startApp() {
        try {
            Util_System_Intent.startActivityByPackageName(mContext, packageName);
            handlePlayDownloadTask(getContext(), packageName);
        } catch (Throwable e) {
            AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
        }
    }

    private void handlePlayDownloadTask(Context context, String packName) {
        final boolean contain = ScoreManager.getInstance().containDownloadTask(context, packName);
        if (contain) {
            AlarmClockManager.getInstance().setObserverGame(true);
        }
    }

    public AppStatus getAppStatus(DownloadStatus ds) {
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
            return String.format(Locale.CHINA, "%1$.1fMB", (1.0f) * apkFileSize / FILE_SIZE_MB);
        } else {
            return String.format(Locale.CHINA, "%1$.1fKB", (1.0f) * apkFileSize / FILE_SIZE_KB);
        }
    }

    public String getCompleteSizeStr() {
        if (completeSize >= FILE_SIZE_MB) {
            return String.format(Locale.CHINA, "%1$.1fMB", (1.0f) * completeSize / FILE_SIZE_MB);
        } else {
            return String.format(Locale.CHINA, "%1$.1fKB", (1.0f) * completeSize / FILE_SIZE_KB);
        }
    }

    public void handleOnClick(FragmentManager fragmentManager) {
        AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, appStatus);
        if (appStatus == null) {
            AppDebugConfig.v(AppDebugConfig.TAG_DOWNLOAD, packageName, "appStatus NULL!!!!");
            return;
        }
        initFile();
        switch (appStatus) {
            case DOWNLOADABLE:
            case UPDATABLE:
                showStartDialog(fragmentManager);
                break;
            case INSTALLABLE:
                startInstall();
                break;
            case OPENABLE:
                startApp();
                break;
            case PAUSABLE:
                if (isSilent) {
                    showStartDialog(fragmentManager);
                } else {
                    stopDownload();
                }
                break;
            case RESUMABLE:
            case RETRYABLE:
                if (NetworkUtil.isWifiConnected(mContext)) {
                    restartDownload();
                } else {
                    if (fragmentManager == null) {
                        ToastUtil.showShort(ConstString.TOAST_DOWNLOADING_NOT_IN_WIFI);
                        restartDownload();
                        return;
                    }
                    final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
                    confirmDialog.setTitle("提示");
                    confirmDialog.setContent("您当前是移动网络状态，下载游戏会消耗手机流量");
                    confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
                        @Override
                        public void onCancel() {
                            confirmDialog.dismissAllowingStateLoss();
                        }

                        @Override
                        public void onConfirm() {
                            restartDownload();
                            confirmDialog.dismissAllowingStateLoss();
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

    private void showStartDialog(FragmentManager fragmentManager) {
        if (NetworkUtil.isWifiConnected(mContext)) {
            startDownload();
        } else {
            if (!isSilent) {
                if (fragmentManager == null) {
                    ToastUtil.showShort(ConstString.TOAST_DOWNLOADING_NOT_IN_WIFI);
                    startDownload();
                    return;
                }
                final ConfirmDialog confirmDialog = ConfirmDialog.newInstance();
                confirmDialog.setTitle("提示");
                confirmDialog.setContent("您当前是移动网络状态，下载游戏会消耗手机流量");
                confirmDialog.setListener(new ConfirmDialog.OnDialogClickListener() {
                    @Override
                    public void onCancel() {
                        confirmDialog.dismissAllowingStateLoss();
                    }

                    @Override
                    public void onConfirm() {
                        startDownload();
                        confirmDialog.dismissAllowingStateLoss();
                    }
                });
                confirmDialog.show(fragmentManager, "download");
            }
        }
    }

    public boolean isValid() {
        return !(id == 0 || TextUtils.isEmpty(name) || apkFileSize == 0 || TextUtils.isEmpty(img) || TextUtils.isEmpty
                (downloadUrl) || TextUtils.isEmpty(destUrl) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty
                (versionName));
    }
}
