package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-16.
 */
public class UserModel extends TaskReward implements Serializable{

	@SerializedName("sess")
	public UserSession userSession;

	@SerializedName("info")
	public UserInfo userInfo;
}