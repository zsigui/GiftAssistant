package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * ReqInitApp
 *
 * @author zacklpx
 *         date 16-1-22
 *         description
 */
public class ReqInitApp implements Serializable {

	@SerializedName("cur_version_code")
	int curVersionCode;

	@SerializedName("cur_version_name")
	String curVersionName;
}
