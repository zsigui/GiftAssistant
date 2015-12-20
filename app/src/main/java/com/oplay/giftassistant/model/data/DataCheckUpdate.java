package com.oplay.giftassistant.model.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/19
 */
public class DataCheckUpdate implements Serializable {
    @SerializedName("apkMd5")
    private String mApkMD5;
    @SerializedName("iconUrl")
    private String mIconUrl;
    @SerializedName("rate")
    private int mRate;
    @SerializedName("versionName")
    private String mVersionName;
    @SerializedName("apkSize")
    private String apkSize;
    @SerializedName("forceUpdate")
    private int mForceUpdate;
    @SerializedName("msg")
    private String mMsg;
    @SerializedName("noDialog")
    private int mNoDialog;
    @SerializedName("apk")
    private String mApkUrl;
    @SerializedName("packageName")
    private String mPackName;
    @SerializedName("versionCode")
    private int mVersionCode;
    @SerializedName("releaseTime")
    private long mReleaseTime;

    public String getApkMD5() {
        return mApkMD5;
    }

    public void setApkMD5(String apkMD5) {
        mApkMD5 = apkMD5;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }

    public int getRate() {
        return mRate;
    }

    public void setRate(int rate) {
        mRate = rate;
    }

    public String getVersionName() {
        return mVersionName;
    }

    public void setVersionName(String versionName) {
        mVersionName = versionName;
    }

    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }

    public int getForceUpdate() {
        return mForceUpdate;
    }

    public void setForceUpdate(int forceUpdate) {
        mForceUpdate = forceUpdate;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public int getNoDialog() {
        return mNoDialog;
    }

    public void setNoDialog(int noDialog) {
        mNoDialog = noDialog;
    }

    public String getApkUrl() {
        return mApkUrl;
    }

    public void setApkUrl(String apkUrl) {
        mApkUrl = apkUrl;
    }

    public String getPackName() {
        return mPackName;
    }

    public void setPackName(String packName) {
        mPackName = packName;
    }

    public int getVersionCode() {
        return mVersionCode;
    }

    public void setVersionCode(int versionCode) {
        mVersionCode = versionCode;
    }

    public long getReleaseTime() {
        return mReleaseTime * 1000;
    }

    public void setReleaseTime(long releaseTime) {
        mReleaseTime = releaseTime;
    }
}
