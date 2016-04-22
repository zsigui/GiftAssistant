package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 消息中心未读消息数量
 *
 * Created by zsigui on 16-4-21.
 */
public class MessageCentralUnread implements Serializable {

	/**
	 * 未读点赞数量
	 */
	@SerializedName("uncheck_like")
	public int unreadAdmireCount;

	/**
	 * 未读评论数量
	 */
	@SerializedName("uncheck_comment")
	public int unreadCommentCount;

	/**
	 * 未读系统消息数量
	 */
	@SerializedName("uncheck_system_message")
	public int unreadSystemCount;

	/**
	 * 未读礼包消息数量
	 */
	@SerializedName("uncheck_jpush_message")
	public int unreadNewGiftCount;
}
