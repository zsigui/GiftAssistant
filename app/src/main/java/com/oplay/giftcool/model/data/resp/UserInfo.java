package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-11.
 */
public class UserInfo implements Serializable {

	/**
	 * 登陆类型(采用何种方式登录)
	 */
	public int loginType;
	/**
	 * 第三方OpenId(用于识别该账号是否首次手机登录)
	 */
	@SerializedName("third_openid")
	public String thirdOpenId;
	/**
	 * 0未绑定 1绑定 ， 当且仅当login_type为手机时可用
	 */
	@SerializedName("bind_ouwan_status")
	public int bindOuwanStatus;
	/**
	 * 用户id
	 */
	@SerializedName("user_id")
	public int uid;
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
	public String avatar;

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
