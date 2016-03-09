package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-7.
 */
public class PushMessage implements Serializable {

	/**
	 * 推送消息ID
	 */
	@SerializedName("jpush_message_id")
	public int id;

	/**
	 * 游戏ID
	 */
	@SerializedName("app_id")
	public int gameId;

	/**
	 * 跳转礼包详情ID
	 */
	@SerializedName("gift_type_id")
	public int giftId;

	/**
	 * 推送消息游戏图标url
	 */
	@SerializedName("icon")
	public String img;

	/**
	 * 游戏名称
	 */
	@SerializedName("game_name")
	public String gameName;

	/**
	 * 礼包内容
	 */
	@SerializedName("gift_content")
	public String giftContent;

	/**
	 * 日期
	 */
	@SerializedName("time")
	public String time;

	/**
	 * 是否已读, 0未读，1已读
	 */
	@SerializedName("status")
	public int readState;
}
