package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

/**
 * 任务类型一，跳转的结构体
 * Created by zsigui on 16-4-14.
 */
public class TaskInfoOne {

	@SerializedName("id")
	public int id;

	@SerializedName("data")
	public String data;

	@SerializedName("action")
	public String action = "";
}
