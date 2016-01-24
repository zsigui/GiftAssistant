package com.oplay.giftcool.model.data.resp;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import net.youmi.android.libs.common.util.Util_System_Package;

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
	public String versionName;

	@SerializedName("new_version_code")
	public int versionCode;

	@SerializedName("cdn_download_url")
	public String downloadUrl;

	@SerializedName("package_name")
	public String packageName;

	@SerializedName("new_version_content")
	public String content;

	@SerializedName("release_time")
	public String releaseTime;

	@SerializedName("filesize")
	public long apkFileSize;

	public boolean checkoutUpdateInfo(Context context) {
		return !(TextUtils.isEmpty(downloadUrl) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(content) ||
				!Util_System_Package.checkAppUpdate(context, context.getPackageName(), versionCode) || apkFileSize ==
				0);
	}
}
