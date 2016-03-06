package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-8.
 */
public class ReqChangeFocus implements Serializable {

	@SerializedName("app_id")
	public int gameId;

	@SerializedName("status")
	public int status;
}
