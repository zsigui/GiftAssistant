package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * initAppResult
 *
 * @author zacklpx
 *         date 16-1-22
 *         description
 */
public class InitAppResult implements Serializable {

	@SerializedName("config")
	public InitAppConfig initAppConfig;

	@SerializedName("update")
	public UpdateInfo updateInfo;
}
