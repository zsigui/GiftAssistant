package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-12.
 */
public class PayCode implements Serializable {

	/**
	 * 礼包码
	 */
	@SerializedName("gift_key")
	public String giftCode;

	@SerializedName("game_info")
	public GameDownloadInfo gameInfo;
}
