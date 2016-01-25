package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by zsigui on 16-1-25.
 */
public class ReqRefreshGift implements Serializable {

	@SerializedName("plan_ids")
	public HashSet<Integer> ids;
}
