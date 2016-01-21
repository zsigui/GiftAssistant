package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 16-1-5.
 */
public class ReqModifyAvatar implements Serializable {

	@SerializedName("img_file")
	public String avatar;
}
