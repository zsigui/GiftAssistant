package com.oplay.giftcool.model.data.resp.task;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 返回签到信息实体
 *
 * Created by zsigui on 16-4-20.
 */
public class TaskSignInfo implements Serializable {

	/**
	 * 签到信息月份
	 */
	@SerializedName("month")
	public String month;

	/**
	 * 签到完成数，二进制0000000001表示，从个位开始对应1号...  0未签到 1已签到
	 */
	@SerializedName("month_sign_map")
	public int mothSignMap;

	/**
	 * 签到里程碑
	 */
	@SerializedName("reward_milestones")
	public TaskSignFinTask taskFinished;

	/**
	 * 系统时间，以结合 month_sign_map 判断当天签到状态，个人觉得此项无意义
	 */
	@SerializedName("server_time")
	public String serverTime;
}
