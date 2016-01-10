package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGiftBanner implements Serializable {

	// 轮播图url
	@SerializedName("img")
	public String url;

	// 游戏名，后期可能会有跳转需要，用于跳转对应论坛
	//@SerializedName("jump")
	// public String jump;
}
