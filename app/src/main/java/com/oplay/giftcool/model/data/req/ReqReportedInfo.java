package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * 上报请求实体
 *
 * Created by zsigui on 16-4-19.
 */
public class ReqReportedInfo {

	/**
	 * 应用列表
	 */
	@SerializedName("app_list")
	public ArrayList<AppBaseInfo> appInfos;

	/**
	 * 手机型号代号
	 */
	@SerializedName("brand")
	public String brand;

	/**
	 * 手机安卓版本
	 */
	@SerializedName("android_version")
	public String sdkVersion;

	/**
	 * 手机系统版本
	 */
	@SerializedName("os_version")
	public String osVersion;
}
