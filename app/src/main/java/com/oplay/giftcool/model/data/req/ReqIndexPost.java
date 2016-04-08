package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

/**
 * 首页活动请求数据实体
 * Created by zsigui on 16-4-8.
 */
public class ReqIndexPost extends ReqIndexGift{

	/**
	 * 是否获取关注快讯
	 */
	@SerializedName("focus")
	public int isAttention;

}
