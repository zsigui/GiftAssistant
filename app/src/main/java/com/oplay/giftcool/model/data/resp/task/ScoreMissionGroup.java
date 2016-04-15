package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * 任务组信息
 *
 * Created by zsigui on 16-4-14.
 */
public class ScoreMissionGroup {

	/**
	 * 该组完成任务数
	 */
	@SerializedName("completed_count")
	public int completedCount;

	/**
	 * 该组任务数
	 */
	@SerializedName("total")
	public int totalCount;

	/**
	 * 组名称
	 */
	@SerializedName("name")
	public String name;

	/**
	 * 该组任务列表
	 */
	@SerializedName("missions")
	public ArrayList<ScoreMission> missions;
}
