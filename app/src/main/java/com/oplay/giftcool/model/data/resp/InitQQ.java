package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-1.
 */
public class InitQQ implements Serializable {

	@SerializedName("share_secret_key")
	public String key;

	@SerializedName("qq")
	public String qq;
}
