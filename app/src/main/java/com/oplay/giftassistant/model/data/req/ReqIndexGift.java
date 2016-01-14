package com.oplay.giftassistant.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * 请求礼包主页数据
 *
 * Created by zsigui on 15-12-28.
 */
public class ReqIndexGift implements Serializable {

	@SerializedName("games")
	public List<String> appNames;

	@SerializedName("page_id")
	public int pageId = 0;

	@SerializedName("page_size")
	public int pageSize = 10;
}