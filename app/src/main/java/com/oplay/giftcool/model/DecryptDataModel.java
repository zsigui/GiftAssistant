package com.oplay.giftcool.model;

import com.oplay.giftcool.config.AppConfig;

/**
 * 进行Pack加密解密需要的默认参数 <br/>
 * Note: 设置一个全局类进行通用设置<br />
 * Created by zsigui on 15-12-21.
 */
public class DecryptDataModel {

	public static final int SDK_VER = 1;
	public static final String SDK_VER_NAME = "V1.0";

	/**
	 * 用户uid
	 */
	private int mUid = 0;
	/**
	 * Session已证明用户处于登录
	 */
	private String mSession = "";
	/**
	 * SDK版本号，默认
	 */
	private int mSdkVer = SDK_VER;
	/**
	 * 平台系统，默认
	 */
	private int mPlatform = 3;
	private String mAppKey = AppConfig.APP_KEY;
	private String mAppSecret = AppConfig.APP_SECRET;

	public DecryptDataModel() {
	}

	public void setUid(int uid) {
		mUid = uid;
	}

	public void setSdkVer(int sdkVer) {
		mSdkVer = sdkVer;
	}

	public void setPlatform(int platform) {
		mPlatform = platform;
	}

	public void setAppkey(String appKey) {
		mAppKey = appKey;
	}

	public void setAppSecret(String appSecret) {
		mAppSecret = appSecret;
	}

	public void setSession(String session) {
		mSession = session;
	}

	public int getUid() {
		return mUid;
	}

	public int getSdkVer() {
		return mSdkVer;
	}

	public int getPlatform() {
		return mPlatform;
	}

	public String getAppKey() {
		return mAppKey;
	}

	public String getAppSecret() {
		return mAppSecret;
	}

	public String getSession() {
		return mSession;
	}

}
