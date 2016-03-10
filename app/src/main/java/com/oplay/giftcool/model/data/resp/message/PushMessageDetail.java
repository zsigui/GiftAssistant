package com.oplay.giftcool.model.data.resp.message;

import com.google.gson.annotations.SerializedName;

/**
 *  详情跳转处理
 *
 * Created by zsigui on 16-3-10.
 */
public class PushMessageDetail {

	/**
	 * 游戏/礼包ID
	 */
	@SerializedName("id")
	public int id;

	/**
	 * 游戏跳转类型
	 */
	@SerializedName("status")
	public int status;
}
