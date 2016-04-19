package com.oplay.giftcool.model.data.req;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * APP应用基本参数
 *
 * Created by zsigui on 16-4-19.
 */
public class AppBaseInfo implements Serializable {

	/**
	 * 应用名称
	 */
	@SerializedName("n")
	public String name;

	/**
	 * 应用包名
	 */
	@SerializedName("pkg")
	public String pkg;

	/**
	 * 应用版本号
	 */
	@SerializedName("vc")
	public String vc;

	/**
	 * 应用版本名称
	 */
	@SerializedName("vn")
	public String vn;
}
