package com.oplay.giftassistant.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-19.
 */
public class ReqSearchKey implements Serializable{

	@SerializedName("search_word")
	public String searchKey;
}
