package com.oplay.giftassistant.model;

/**
 * 进行Pack加密解密需要的默认参数 <br/>
 * Note: 设置一个全局类进行通用设置<br />
 * Created by zsigui on 15-12-21.
 */
public class DecryptDataModel {

	private int mUid = 10001;
	private int mSdkVer = 1;
	private int mPlatform = 3;
	private String mAppkey = "abcdef0123456789";
	private String mAppSecret = "0123456789abcdef";
	private String mSession = "testsession";

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

	public void setAppkey(String appkey) {
		mAppkey = appkey;
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

	public String getAppkey() {
		return mAppkey;
	}

	public String getAppSecret() {
		return mAppSecret;
	}

	public String getSession() {
		return mSession;
	}

}
