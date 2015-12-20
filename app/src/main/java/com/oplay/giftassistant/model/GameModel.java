package com.oplay.giftassistant.model;

import java.util.List;

/**
 * 游戏模型类
 *
 * Created by zsigui on 15-12-18.
 */
public class GameModel {

	// ID
	public int id;
	// 游戏名称
	public String name;
	// 游戏包名
	public String package_name;
	// 游戏icon的URL
	public String icon;
	// 礼包总数
	public String total_count;
	// 游戏标签
	public String tag;
	// 对应礼包
	public List<GiftModel> gifts;
}
