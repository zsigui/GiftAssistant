package com.oplay.giftassistant.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-5.
 */
public class ReqModifyAvater implements Serializable {

	@SerializedName("avater")
	public String avater;
}