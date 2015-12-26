package com.oplay.giftassistant.model;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-16.
 */
public class UserModel implements Serializable{

	/**
	 * 用户昵称
	 */
	public String username;
	/**
	 * 用户头像url
	 */
	public String img;
	/**
	 * 手机号码，跟偶玩账号惟一捆绑
	 */
	public String phone;
	/**
	 * session,用于进行请求访问
	 */
	public String session;
	/**
	 * 用户id
	 */
	public int uid;
	/**
	 * session的超时时间戳
	 */
	public long sessionExpired;

}
