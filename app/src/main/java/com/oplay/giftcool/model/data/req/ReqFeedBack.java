package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-21.
 */
public class ReqFeedBack implements Serializable{

	@SerializedName("type")
	public int type;

	@SerializedName("content")
	public String content;

	@SerializedName("contact")
	public String contact;

	@SerializedName("version")
	public int version;
}
