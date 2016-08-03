package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-11.
 */
public class UserSession implements Serializable {
	/**
	 * 用户第三方id (手机登录才有)
	 */
	@SerializedName("openid")
	public String openId;

	/**
	 * 用户id
	 */
	@SerializedName("user_id")
	public int uid;

	/**
	 * session,用于进行请求访问
	 */
	@SerializedName("session_id")
	public String session;
}
