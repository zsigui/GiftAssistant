package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-18.
 */
public class SystemMessage implements Serializable {

	/**
	 * 系统消息ID
	 */
	@SerializedName("message_id")
	public int id;

	/**
	 * 消息标题
	 */
	@SerializedName("title")
	public String title;

	/**
	 * 是否已读
	 */
	@SerializedName("check")
	public int isRead;

	/**
	 * 消息内容
	 */
	@SerializedName("content")
	public String content;

	/**
	 * 消息时间
	 */
	@SerializedName("create_time")
	public String time;
}
