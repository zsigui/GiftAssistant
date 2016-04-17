package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-12.
 */
public class MissionReward implements Serializable {

	@SerializedName("c")
	public int code = -1;

	@SerializedName("e")
	public String etio;

	@SerializedName("d")
	public MissionRewardData data;

	@SerializedName("m")
	public String msg;
}
