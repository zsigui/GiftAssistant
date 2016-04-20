package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 签到任务完成目标
 *
 * Created by zsigui on 16-4-20.
 */
public class TaskSignFinTask implements Serializable{

	@SerializedName("sign_month")
	public boolean signMonth;

	@SerializedName("sign_21_days")
	public boolean sign21Day;

	@SerializedName("sign_14_days")
	public boolean sign14Day;

	@SerializedName("sign_7_days")
	public boolean sign7Day;

	@SerializedName("sign_3_days")
	public boolean sign3Day;

	@SerializedName("sign_today")
	public boolean signToday;
}
