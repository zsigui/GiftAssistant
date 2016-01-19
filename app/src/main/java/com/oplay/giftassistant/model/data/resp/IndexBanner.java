package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexBanner implements Serializable {

	// 轮播图url
	@SerializedName("img")
	public String url;

	// 跳转类型
	@SerializedName("action")
	public int type;

	// 跳转需要数据
	@SerializedName("data")
	public String extData;
}
