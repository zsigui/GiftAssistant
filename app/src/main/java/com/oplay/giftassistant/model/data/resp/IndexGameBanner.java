package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameBanner implements Serializable {

	// 轮播图url
	@SerializedName("url")
	public String url;

	// 游戏名，后期可能会有跳转需要，用于跳转对应论坛
	// public String gameName;
}

