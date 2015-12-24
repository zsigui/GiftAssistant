package com.oplay.giftassistant.model.json.base;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-22.
 */
public class JsonReqBase<T> implements Serializable{

	@SerializedName("imei")
	public String imei;

	@SerializedName("imsi")
	public String imsi;

	@SerializedName("cid")
	public String cid;

	@SerializedName("mac")
	public String mac;

	@SerializedName("apn")
	public String apn;

	@SerializedName("cn")
	public String cn;

	@SerializedName("dd")
	public String dd;

	@SerializedName("dv")
	public String dv;

	@SerializedName("os")
	public String os;

	@SerializedName("uid")
	public int uid;

	@SerializedName("chn")
	public int chn;

	@SerializedName("d")
	public T data;
}
