package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-7.
 */
public class ReqHopeGift implements Serializable {

	@SerializedName("app_id")
	public int gameId = 0;

	@SerializedName("app_name")
	public String gameName;

	@SerializedName("remarks")
	public String note;
}
