package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;
import com.oplay.giftassistant.model.GameModel;
import com.oplay.giftassistant.model.GiftModel;

import java.util.List;

/**
 * 搜索结果返回的数据类型
 *
 * Created by zsigui on 15-12-22.
 */
public class SearchDataResult {

	@SerializedName("keyword")
	public String keyword;

	@SerializedName("games")
	public List<GameModel> gameList;

	@SerializedName("gifts")
	public List<GiftModel> giftList;
}
