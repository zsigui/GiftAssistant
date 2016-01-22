package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-17.
 */
public class ModifyAvatar extends TaskReward implements Serializable{

	@SerializedName("avatar")
	public String avatar;
}
