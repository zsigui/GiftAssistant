package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by zsigui on 16-5-5.
 */
public class GameDetail extends IndexGameNew {

	/**
	 * 标签
	 */
	@SerializedName("labels")
	public ArrayList<String> labels;

	/**
	 * 应用描述
	 */
	@SerializedName("app_description")
	public String description;

	/**
	 * 轮播图合集
	 */
	@SerializedName("screenshot_urls")
	public ArrayList<String> mScreenShotPics;

	/**
	 * 是否关注 0 没有 1 关注
	 */
	@SerializedName("focus_status")
	public int isFocus;

	/**
	 * 游戏快讯
	 */
	public ArrayList<String> mPostMessages;
}
