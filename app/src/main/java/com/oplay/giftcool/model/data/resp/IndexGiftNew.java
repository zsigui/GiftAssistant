package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNew implements Serializable{

	// 礼包标志
	@SerializedName("plan_id")
	public int id;
    // 礼包名称
	@SerializedName("name")
	public String name;
	// 礼包内容
	@SerializedName("content")
	public String content;
    // 游戏名称
	@SerializedName("game_name")
	public String gameName;
    // 游戏图标
    @SerializedName("icon")
	public String img;
	// 礼包获取类型 0 未抢 1 抢号 2 淘号
	@SerializedName("code_type")
	public int seizeStatus;
	// 礼包状态 0 删除， 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束， 6 下架 (0, 6状态不关注)
	@SerializedName("status")
	public int status;
	// 支付类型 0 未知， 1 积分， 2 偶玩豆， 3 积分或偶玩豆
	@SerializedName("price_type")
	public int priceType;
    // 抢号需要换取积分
    @SerializedName("point_price")
    public int score;
    // 抢号需要换取的偶玩豆
	@SerializedName("owanb_price")
    public int bean;
    // 开抢时间
	@SerializedName("start_time")
    public String seizeTime;
    // 开淘时间
	@SerializedName("tao_time")
    public String searchTime;
    // 剩余礼包数量
	@SerializedName("remain_count")
    public int remainCount;
    // 礼包总数
	@SerializedName("total_count")
    public int totalCount;
	// 已淘号次数
	@SerializedName("tao_count")
	public int searchCount;
	// 使用开始时间
	@SerializedName("use_start_time")
	public String useStartTime;
	// 使用结束时间
	@SerializedName("use_end_time")
	public String useEndTime;
    // 特别说明
//	@SerializedName("remarks")
//    public String note;
	// 礼包兑换方式
	@SerializedName("usage")
	public String usage;
	// 礼包码
	@SerializedName("gift_key")
	public String code;
	// 礼包类型 1 普通免费 2 普通 3 限量 4 0元抢
	@SerializedName("plan_type")
	public int giftType;
	// 原价
	@SerializedName("original_price")
	public int originPrice;
	// 剩余开抢时间，单位:s
	@SerializedName("remain_time")
	public int remainStartTime;
	// 专享礼包类型, 1: 礼包酷专享
	@SerializedName("exclusive")
	public int exclusive;
}
