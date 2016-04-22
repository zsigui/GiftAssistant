package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 回复消息实体
 *
 * Created by zsigui on 16-4-18.
 */
public class ReplyMessage implements Serializable {

	/**
	 * 活动ID
	 */
	@SerializedName("activity_id")
	public int postId;

	/**
	 * 评论ID
	 */
	@SerializedName("comment_id")
	public int commentId;

	/**
	 * 我的回复ID
	 */
	@SerializedName("ref_comment_id")
	public int reCommentId;

	/**
	 * 评论用户头像
	 */
	@SerializedName("avatar")
	public String icon;

	/**
	 * 评论用户昵称/手机号/偶玩号
	 */
	@SerializedName("nick")
	public String name;

	/**
	 * 评论内容
	 */
	@SerializedName("content")
	public String content;

	/**
	 * 评论时间s
	 */
	@SerializedName("create_time")
	public String time;

	/**
	 * 我的回复的图片
	 */
	@SerializedName("ref_imgs")
	public ArrayList<String> hintPics;

	/**
	 * 我的回复的内容，当图片为空时采用
	 */
	@SerializedName("ref_content")
	public String hintText;

	/**
	 * 发表评论的用户ID
	 */
	@SerializedName("user_id")
	public int userId;
}
