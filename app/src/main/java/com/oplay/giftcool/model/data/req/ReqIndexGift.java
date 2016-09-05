package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 请求礼包主页数据
 *
 * Created by zsigui on 15-12-28.
 */
public class ReqIndexGift implements Serializable {

	@SerializedName("page_id")
	public int page = 0;

	@SerializedName("page_size")
	public int pageSize = 10;
}
