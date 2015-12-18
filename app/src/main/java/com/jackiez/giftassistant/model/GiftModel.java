package com.jackiez.giftassistant.model;

/**
 * 礼包类模型 <br />
 *
 * Created by zsigui on 15-12-18.
 */
public class GiftModel {
	// ID
	public int id;
	// 礼包名称
	public String name;
	// 礼包logo的URL
	public String logo;
	// 新增礼包数量
	public String new_count;
	// 开抢时间
	public long get_time;
	// 淘号时间
	public long pan_time;
	// 礼包内容
	public String content;
	// 淘号次数
	public int pan_total_count;
	// 特别说明
	public String note;
}
