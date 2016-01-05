package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-5.
 */
public class ModifyPhone implements Serializable {

	@SerializedName("new_openid")
	public String newOpenId;
}
