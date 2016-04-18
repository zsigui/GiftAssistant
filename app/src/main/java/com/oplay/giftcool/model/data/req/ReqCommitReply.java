package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 16-4-18.
 */
public class ReqCommitReply implements Serializable {

	@SerializedName("cuid")
	public int cuid;

	@SerializedName("token")
	public String token;

	@SerializedName("activity_id")
	public int postId;

	@SerializedName("content")
	public String content;

	@SerializedName("comment_id")
	public int commentId;

	@SerializedName("imgs")
	public ArrayList<String> imgs;
}
