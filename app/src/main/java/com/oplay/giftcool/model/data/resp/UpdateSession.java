package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-11.
 */
public class UpdateSession extends TaskReward implements Serializable {
	@SerializedName("session_id")
	public String session;
}
