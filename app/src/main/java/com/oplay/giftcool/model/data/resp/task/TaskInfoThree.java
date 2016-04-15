package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 任务额外信息类型三
 *
 * Created by zsigui on 16-4-15.
 */
public class TaskInfoThree implements Serializable {

	/**
	 * 游戏ID
	 */
	@SerializedName("id")
	public int appId;

	/**
	 * 试用时间，单位 s
	 */
	@SerializedName("time")
	public int time;

	/**
	 * 游戏包名
	 */
	@SerializedName("package_name")
	public String packName;

}
