package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-1-18.
 */
public class ReqTaskReward {

	/**
	 * 任务代号
	 */
	@SerializedName("code")
	public String code;

	/**
	 * 是否直接HTTP返回通知不走SocketIO
	 */
	@SerializedName("reply_notify")
	public int replyNotify;

	/**
	 * 当任务为签到时写入，空则取今日
	 */
	@SerializedName("date")
	public String date;

}
