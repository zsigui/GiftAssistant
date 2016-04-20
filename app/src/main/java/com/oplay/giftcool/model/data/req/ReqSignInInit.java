package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 请求特定日期的签到信息
 *
 * Created by zsigui on 16-4-20.
 */
public class ReqSignInInit implements Serializable{

	/**
	 * 请求签到日期，格式：yyyy-MM-dd
	 */
	@SerializedName("date")
	public String date;
}
