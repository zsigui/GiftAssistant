package com.oplay.giftassistant.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-28.
 */
public class IndexGameNew implements Serializable {

	// 游戏标志
	@SerializedName("app_id")
	public int id;

	// 游戏名称
	@SerializedName("game_name")
	public String name;

	// 新增礼包数量
	@SerializedName("new_add_count")
	public int newCount;

	// 拥有礼包总数
	@SerializedName("has_gift_count")
	public int totalCount;

	// 在玩人数
	@SerializedName("plays")
	public int playCount;

	// 游戏大小
	@SerializedName("apk_size")
	public String size;

	// 游戏ICON
	@SerializedName("icon")
	public String img;

	// 主推游戏Banner地址
	@SerializedName("banner")
	public String banner;

	// 最新礼包名
	@SerializedName("gift_name")
	public String giftName;

	// 下载地址
	@SerializedName("download_url")
	public String downloadUrl;

}
