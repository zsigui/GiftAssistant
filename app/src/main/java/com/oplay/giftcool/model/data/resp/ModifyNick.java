package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-17.
 */
public class ModifyNick extends TaskReward implements Serializable{

	@SerializedName("nick")
	public String nick;
}
