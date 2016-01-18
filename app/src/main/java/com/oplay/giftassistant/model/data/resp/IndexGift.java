package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGift implements Serializable {

	@SerializedName("banner")
	public ArrayList<IndexBanner> banner;

	@SerializedName("like")
	public ArrayList<IndexGiftLike> like;

	@SerializedName("limit")
	public ArrayList<IndexGiftNew> limit;

	@SerializedName("new")
	public ArrayList<IndexGiftNew> news;
}
