package com.oplay.giftcool.download.silent.bean;

import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;

import net.youmi.android.libs.common.basic.Basic_StringUtil;
import net.youmi.android.libs.common.coder.Coder_Md5;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by zsigui on 16-4-26.
 */
public class DownloadInfo {

    private String mDownloadUrl;
    private String mDestUrl;
    private String mMd5Sum;
    private long mDownloadSize;
    private long mTotalSize;

    private String mTempFileName;
    private String mStoreFileName;
    private int mRetryTime;
    /**
     * 指示下载状态
     */
    private boolean mIsDownload = true;

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;
    }

    public String getDestUrl() {
        return mDestUrl;
    }

    public void setDestUrl(String destUrl) {
        mDestUrl = destUrl;
    }

    public String getMd5Sum() {
        return mMd5Sum;
    }

    public void setMd5Sum(String md5Sum) {
        mMd5Sum = md5Sum;
    }

    public long getDownloadSize() {
        return mDownloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        mDownloadSize = downloadSize;
    }

    public long getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(long totalSize) {
        mTotalSize = totalSize;
    }

    public String getTempFileName() {
        if (TextUtils.isEmpty(mTempFileName)) {
            mTempFileName = createTempFilePath(mDownloadUrl, mDestUrl);
        }
        return mTempFileName;
    }

    public String getStoreFileName() {
        if (TextUtils.isEmpty(mStoreFileName)) {
            mStoreFileName = createDownloadFilePath(mDownloadUrl, mDestUrl);
        }
        return mStoreFileName;
    }

    public int getRetryTime() {
        return mRetryTime;
    }

    public void setRetryTime(int retryTime) {
        mRetryTime = retryTime;
    }

    public boolean isDownload() {
        return mIsDownload;
    }

    public void setIsDownload(boolean isDownload) {
        mIsDownload = isDownload;
    }

    /**
     * new一个下载的缓存文件
     * <p/>
     * 目录下文件命名规范：有唯一标识identify时用MD5(identify)作文件名
     * 无唯一表示用下载url解析的文件名作文件名
     * 解析失败用MD5(下载url)作文件名
     * 临时文件名后缀.vmtf
     *
     * @param url      原始下载url(每个下载任务的默认唯一标识)
     * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
     * @return
     */
    public String createTempFilePath(String url, String identify) {
        String temp;
        if (!Basic_StringUtil.isNullOrEmpty(identify)) {
            final String decodedUrl;
            try {
                decodedUrl = URLDecoder.decode(identify, HTTP.UTF_8);
                final int start = decodedUrl.lastIndexOf(File.separatorChar) + 1;
                final int end = decodedUrl.lastIndexOf('.');
                temp = decodedUrl.substring(start, end);
            } catch (UnsupportedEncodingException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
                temp = Coder_Md5.md5(identify);
            }
        } else {
            temp = Coder_Md5.md5(url);
        }
        return temp + Global.TEMP_FILE_NAME_SUFFIX;
    }

    /**
     * new一个下载的最终文件
     * <p/>
     * 目录下文件命名规范：有唯一标识identify时用MD5(identify)作文件名
     * 无唯一表示用下载url解析的文件名作文件名
     * 解析失败用MD5(下载url)作文件名
     *
     * @param url      原始下载url(每个下载任务的默认唯一标识)
     * @param identify 每个下载任务的指定标识，如果本参数不为空，那么需要优先用这个进行创建文件
     * @return
     */
    public String createDownloadFilePath(String url, String identify) {
        String temp;
        if (!Basic_StringUtil.isNullOrEmpty(identify)) {
            final String decodedUrl;
            try {
                decodedUrl = URLDecoder.decode(identify, HTTP.UTF_8);
                final int start = decodedUrl.lastIndexOf(File.separatorChar) + 1;
                final int end = decodedUrl.lastIndexOf('.');
                temp = decodedUrl.substring(start, end);
            } catch (UnsupportedEncodingException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_DOWNLOAD, e);
                temp = Coder_Md5.md5(identify);
            }
        } else {
            temp = Coder_Md5.md5(url);
        }
        return temp + Global.APK_FILE_NAME_SUFFIX;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this)
                || (o != null
                && (o instanceof DownloadInfo)
                && ((DownloadInfo) o).getDownloadUrl().equals(getDownloadUrl()));
    }
}
