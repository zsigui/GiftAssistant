package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLimit implements Serializable {

	@SerializedName("plan_id")
	public String id;
	@SerializedName("name")
	public String name;
	@SerializedName("game_name")
	public String gameName;
	@SerializedName("icon")
	public String img;
	@SerializedName("remain_count")
	public int remainCount;
}
