package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-18.
 */
public class PostToken implements Serializable {

	/**
	 * 用于添加评论的token
	 */
	@SerializedName("token")
	public String token;
}
