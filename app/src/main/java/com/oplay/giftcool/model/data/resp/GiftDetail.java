package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-20.
 */
public class GiftDetail implements Serializable{

	@SerializedName("app_data")
	public IndexGameNew gameData;

	@SerializedName("plan_data")
	public IndexGiftNew giftData;
}
