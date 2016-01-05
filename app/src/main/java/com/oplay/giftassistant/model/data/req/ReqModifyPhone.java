package com.oplay.giftassistant.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-5.
 */
public class ReqModifyPhone implements Serializable {

	@SerializedName("openid")
	public String openId;

	@SerializedName("old_mobile")
	public String oldPhone;

	@SerializedName("new_mobile")
	public String newPhone;

	@SerializedName("code")
	public String code;
}
