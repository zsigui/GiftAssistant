package com.oplay.giftcool.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-1-29.
 */
public class ActivityModel {

	@SerializedName("title")
	public String title;

	@SerializedName("url")
	public String url;

	@SerializedName("need_login")
	public boolean needLogin;
}
