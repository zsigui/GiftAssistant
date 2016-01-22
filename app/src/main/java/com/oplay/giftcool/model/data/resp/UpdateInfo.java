package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * UpdateInfo
 *
 * @author zacklpx
 *         date 16-1-22
 *         description
 */
public class UpdateInfo implements Serializable {

	@SerializedName("new_version_name")
	String versionName;

	@SerializedName("new_version_code")
	String versionCode;

	@SerializedName("is_force_update")
	boolean isForceUpdate;

	@SerializedName("download_url")
	String downloadUrl;

	@SerializedName("packagename")
	String packageName;
}
