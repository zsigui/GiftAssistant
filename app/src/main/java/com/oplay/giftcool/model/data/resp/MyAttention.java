package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-3-8.
 */
public class MyAttention extends IndexGiftLike {

	/**
	 * 是否关注
	 */
	@SerializedName("is_focus")
	public boolean isFocus = true;
}
