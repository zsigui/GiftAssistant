package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-3-9.
 */
public class MessageCount implements Serializable {

	@SerializedName("jpush_message_count")
	public int count;
}
