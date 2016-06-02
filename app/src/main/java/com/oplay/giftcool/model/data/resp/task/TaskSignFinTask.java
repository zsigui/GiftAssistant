package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 签到任务完成目标
 *
 * Created by zsigui on 16-4-20.
 */
public class TaskSignFinTask implements Serializable{

	@SerializedName("sign_today")
	public boolean signToday;
}
