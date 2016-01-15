package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-31.
 */
public class IndexGameSuper implements Serializable {

	@SerializedName("banner")
	public ArrayList<IndexGameBanner> banner;
	@SerializedName("hot")
	public ArrayList<IndexGameNew> hot;
	@SerializedName("recommend")
	public IndexGameNew recommend;
	@SerializedName("new")
	public ArrayList<IndexGameNew> news;
}
