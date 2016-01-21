package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-12.
 */
public class ReqPayCode implements Serializable {

	/**
	 * 礼包ID
	 */
	@SerializedName("plan_id")
	public int id;

	/**
	 * 礼包支付类型
	 */
	@SerializedName("price_type")
	public int type;
}
