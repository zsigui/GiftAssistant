package com.oplay.giftassistant.model.json.base;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftassistant.util.CommonUtil;

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

	@SerializedName("chn")
	public int chn;

	@SerializedName("cmd")
	public int cmd;

	@SerializedName("d")
	public T data;

	public JsonReqBase() {
		CommonUtil.addCommonParams(this, 0);
	}

	public JsonReqBase(int cmd) {
		CommonUtil.addCommonParams(this, cmd);
	}
}
