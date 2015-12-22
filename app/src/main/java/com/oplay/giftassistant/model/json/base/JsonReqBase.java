package com.oplay.giftassistant.model.json.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-22.
 */
public class JsonReqBase<T> implements Serializable{

	@SerializedName("imei")
	private String mImei;

	@SerializedName("imsi")
	private String mImsi;

	@SerializedName("uid")
	private int mUid;

	@SerializedName("chn")
	private int mChn;

	@SerializedName("subchn")
	private int mSubChn;

	@SerializedName("data")
	private T mData;

	public String getImei() {
		return mImei;
	}

	public void setImei(String imei) {
		mImei = imei;
	}

	public String getImsi() {
		return mImsi;
	}

	public void setImsi(String imsi) {
		mImsi = imsi;
	}

	public int getUid() {
		return mUid;
	}

	public void setUid(int uid) {
		mUid = uid;
	}

	public int getChn() {
		return mChn;
	}

	public void setChn(int chn) {
		mChn = chn;
	}

	public int getSubChn() {
		return mSubChn;
	}

	public void setSubChn(int subChn) {
		mSubChn = subChn;
	}

	public T getData() {
		return mData;
	}

	public void setData(T data) {
		mData = data;
	}
}
