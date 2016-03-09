package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 礼包推送消息类
 *
 * Created by zsigui on 16-3-9.
 */
public class PushMessageGift implements Serializable {

	/**
	 * 推送的消息的礼包类型
	 */
	@SerializedName("gift_plan_type")
	public int giftType;
}
