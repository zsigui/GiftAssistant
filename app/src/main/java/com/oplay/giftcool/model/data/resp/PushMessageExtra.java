package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-8.
 */
public class PushMessageExtra implements Serializable {

	@SerializedName("push_type")
	public int type;

	@SerializedName("data")
	public String extraJson;
}
