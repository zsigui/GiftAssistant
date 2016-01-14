package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-7.
 */
public class ScoreMission implements Serializable {

	/**
	 * 标识名
	 */
	@SerializedName("name")
	public String id;

	/**
	 * 任务名称
	 */
	@SerializedName("show_name")
	public String name;

	/**
	 * 任务类型: 1.新手任务 2.每日任务 3.连续性任务
	 */
	@SerializedName("type")
	public int type;

	/**
	 * Icon图标
	 */
	public int icon;

	/**
	 * 奖励积分
	 */
	@SerializedName("reward_points")
	public int rewardScore;

	/**
	 * 连续天数(针对连续性任务)
	 */
	@SerializedName("continuous_day")
	public int continuousDay;

	/**
	 * 完成了几次(针对连续性任务)
	 */
	@SerializedName("complete_times")
	public int completeTime;

	/**
	 * 任务最后一次完成时间
	 */
	@SerializedName("last_complete_time")
	public String lastCompleteTime;
}