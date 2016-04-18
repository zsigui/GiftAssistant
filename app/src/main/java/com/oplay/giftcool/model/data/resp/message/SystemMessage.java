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
	@SerializedName("")
	public int id;

	/**
	 * 消息标题
	 */
	@SerializedName("")
	public String title;

	/**
	 * 是否已读
	 */
	@SerializedName("")
	public int isRead;

	/**
	 * 消息内容
	 */
	@SerializedName("")
	public String content;

	/**
	 * 消息时间
	 */
	@SerializedName("")
	public String time;
}
