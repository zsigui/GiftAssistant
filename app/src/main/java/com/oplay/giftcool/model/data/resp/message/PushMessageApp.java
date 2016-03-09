package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

/**
 * 长时间未登录行为处理
 *
 * Created by zsigui on 16-3-10.
 */
public class PushMessageApp {

	/**
	 * 间隔时间
	 */
	@SerializedName("day")
	public int day = 3;

	/**
	 * 是否强制推送，即无论今天是否推送过其他消息
	 */
	@SerializedName("is_force_push")
	public boolean isForcePush = false;
}
