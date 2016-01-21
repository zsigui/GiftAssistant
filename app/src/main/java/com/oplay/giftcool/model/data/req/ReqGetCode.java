package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-14.
 */
public class ReqGetCode implements Serializable {

	@SerializedName("trade_no")
	public String tradeNo;
}
