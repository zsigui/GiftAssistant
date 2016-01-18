package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-17.
 */
public class ModifyNick implements Serializable{

	@SerializedName("nick")
	public String nick;
}
