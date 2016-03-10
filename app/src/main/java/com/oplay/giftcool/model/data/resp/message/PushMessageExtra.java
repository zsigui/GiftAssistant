package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 推送消息的额外协定数据结构
 *
 * Created by zsigui on 16-3-8.
 */
public class PushMessageExtra implements Serializable {

	/**
	 * 推送类型
	 */
	@SerializedName("push_type")
	public int type;

	/**
	 * 广播通知样式
	 */
	@SerializedName("builder_id")
	public int builderId;

	/**
	 * 内容
	 */
	@SerializedName("content")
	public String content;

	/**
	 * 标题
	 */
	@SerializedName("title")
	public String title;

	/**
	 * 通知时间
	 */
	@SerializedName("broadcast_time")
	public String broadcastTime;

	/**
	 * 额外内容
	 */
	@SerializedName("data")
	public String extraJson;

}
