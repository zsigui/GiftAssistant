package com.oplay.giftassistant.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-16.
 */
public class UserModel implements Serializable{

	/**
	 * 登陆类型
	 */
	@SerializedName("login_type")
	public int loginType;

	/**
	 * 用户第三方id (手机登录才有)
	 */
	@SerializedName("openid")
	public int openId;

	/**
	 * 用户id
	 */
	@SerializedName("user_id")
	public int uid;

	/**
	 * session,用于进行请求访问
	 */
	public String session;

	/**
	 * 用户名 (偶玩账号)
	 */
	@SerializedName("username")
	public String username;

	/**
	 * 用户昵称
	 */
	@SerializedName("nick")
	public String nick;

	/**
	 * 用户头像url
	 */
	@SerializedName("user_img")
	public String img;

	/**
	 * 手机号码，绑定的手机号
	 */
	@SerializedName("mobile")
	public String phone;

	/**
	 * 用户积分
	 */
	@SerializedName("points")
	public int score;

	/**
	 * 偶玩豆
	 */
	@SerializedName("mili")
	public int bean;

	/**
	 * 拥有礼包数目
	 */
	@SerializedName("gift_count")
	public int giftCount;

}
