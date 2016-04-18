package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-4-18.
 */
public class ReqPostToken implements Serializable {

	/**
	 * 活动ID
	 */
	@SerializedName("activity_id")
	public int postId;
}
