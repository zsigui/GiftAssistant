package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 回复消息实体
 *
 * Created by zsigui on 16-4-18.
 */
public class ReplyMessage implements Serializable {

	/**
	 * 活动ID
	 */
	@SerializedName("")
	public int postId;

	/**
	 * 评论ID
	 */
	@SerializedName("")
	public int replyId;

	/**
	 * 收到的赞或评论ID
	 */
	@SerializedName("")
	public int commentId;

	/**
	 * 用户头像
	 */
	@SerializedName("")
	public String icon;

	/**
	 * 用户昵称/手机号/偶玩号
	 */
	@SerializedName("")
	public String name;

	/**
	 * 回复内容
	 */
	@SerializedName("")
	public String content;

	/**
	 * 回复时间s
	 */
	@SerializedName("")
	public String time;

	/**
	 * 评论图片
	 */
	@SerializedName("")
	public String hintPic;

	/**
	 * 评论内容，当图片为空时采用
	 */
	@SerializedName("")
	public String hintText;
}
