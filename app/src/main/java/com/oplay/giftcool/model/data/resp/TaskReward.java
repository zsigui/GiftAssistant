package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-18.
 */
public class TaskReward implements Serializable {

	@SerializedName("reward_points")
	public int rewardPoints;
}
