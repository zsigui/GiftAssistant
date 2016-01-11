package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-11.
 */
public class UpdateSesion implements Serializable {
	@SerializedName("session_id")
	public String session;
}
