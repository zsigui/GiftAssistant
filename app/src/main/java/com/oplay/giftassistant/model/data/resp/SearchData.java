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
public class SearchData  {

	@SerializedName("keyword")
	private String mKeyword;

	@SerializedName("games")
	private List<GameModel> mGameList;

	@SerializedName("gifts")
	private List<GiftModel> mGiftList;

	public String getKeyword() {
		return mKeyword;
	}

	public void setKeyword(String keyword) {
		mKeyword = keyword;
	}

	public List<GameModel> getGameList() {
		return mGameList;
	}

	public void setGameList(List<GameModel> gameList) {
		mGameList = gameList;
	}

	public List<GiftModel> getGiftList() {
		return mGiftList;
	}

	public void setGiftList(List<GiftModel> giftList) {
		mGiftList = giftList;
	}
}
