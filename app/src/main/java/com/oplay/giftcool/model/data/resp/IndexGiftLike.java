package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftLike implements Serializable{

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

	// 游戏实际大小(字节)
	@SerializedName("apk_filesize")
	public long fileSize;

	// 游戏ICON
	@SerializedName("icon")
	public String img;

	// 最新礼包名
	@SerializedName("gift_name")
	public String giftName;
}
