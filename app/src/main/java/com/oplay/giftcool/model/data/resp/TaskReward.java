package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-21.
 */
public class TaskReward implements Serializable{

	@SerializedName("reward_info")
	public TaskRewardDetail data;
}
