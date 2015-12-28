package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * 搜索结果返回的数据类型
 *
 * Created by zsigui on 15-12-22.
 */
public class SearchDataResult {

	@SerializedName("keyword")
	public String keyword;

	@SerializedName("games")
	public ArrayList<IndexGameNew> games;

	@SerializedName("gifts")
	public ArrayList<IndexGiftNew> gifts;
}
