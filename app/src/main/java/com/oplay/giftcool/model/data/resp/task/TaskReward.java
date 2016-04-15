package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-21.
 * @Deprecated 新版通过长连接推送来通知更新
 */
@Deprecated
public class TaskReward implements Serializable{

	@SerializedName("reward_points")
	public int rewardPoints;

	@SerializedName("mission_name")
	public String taskName;
}
