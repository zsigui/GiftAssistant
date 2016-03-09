package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 推送消息的额外协定数据结构
 *
 * Created by zsigui on 16-3-8.
 */
public class PushMessageExtra implements Serializable {

	@SerializedName("push_type")
	public int type;

	@SerializedName("builder_id")
	public int builderId;

	@SerializedName("content")
	public String content;

	@SerializedName("title")
	public String title;

	@SerializedName("broadcast_time")
	public String broadcastTime;

	@SerializedName("data")
	public String extraJson;

}
