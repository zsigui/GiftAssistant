package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-12.
 */
public class MissionRewardData implements Serializable {

	@SerializedName("name")
	public String name;

	@SerializedName("display_name")
	public String displayName;

	@SerializedName("finish_time")
	public String finishTime;

	@SerializedName("reward_points")
	public int rewardPoint;

	@SerializedName("display_msg")
	public String dispayMsg;
}
