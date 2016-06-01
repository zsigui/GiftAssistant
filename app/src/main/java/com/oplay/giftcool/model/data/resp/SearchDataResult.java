package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 搜索结果返回的数据类型
 *
 * Created by zsigui on 15-12-22.
 */
public class SearchDataResult implements Serializable{

	@SerializedName("search_key")
	public String keyword;

	/**
	 * 搜到的游戏
	 */
	@SerializedName("game_data")
	public ArrayList<IndexGameNew> games;

	/**
	 * 搜到的礼包
	 */
	@SerializedName("plan_data")
	public ArrayList<IndexGiftNew> gifts;

//	@SerializedName("guess_data")
//	public ArrayList<IndexGiftNew> guessGift;

	/**
	 * 搜到的首充券
	 */
	@SerializedName("coupon_data")
	public ArrayList<IndexGiftNew> coupons;
}
