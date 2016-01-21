package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-12.
 */
public class PayCode implements Serializable {

	/**
	 * 礼包码
	 */
	@SerializedName("gift_key")
	public String giftCode;

	/**
	 * 订单号
	 */
	@SerializedName("trade_no")
	public String tradeNo;

	/**
	 * 支付金额，单位元
	 */
	@SerializedName("pay_money")
	public int payNumber;

	/**
	 * 商品描述
	 */
	@SerializedName("description")
	public String orderDesc;

	/**
	 * 用户ID
	 */
	@SerializedName("cuid")
	public int uid;
}
