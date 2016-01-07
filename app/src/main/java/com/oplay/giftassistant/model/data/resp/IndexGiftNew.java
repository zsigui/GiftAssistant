package com.oplay.giftassistant.model.data.resp;

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
    // 是否为限量礼包
    @SerializedName("is_limit")
    public boolean isLimit = false;
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
	@SerializedName("ouwanb_price")
    public int bean;
    // 开抢时间戳
	@SerializedName("start_time")
    public long seizeTime;
    // 开淘时间戳
	@SerializedName("tao_time")
    public long searchTime;
    // 剩余礼包数量
	@SerializedName("remain_count")
    public int remainCount;
    // 礼包总数
	@SerializedName("total_count")
    public int totalCount;
	// 已淘号次数
	@SerializedName("tao_count")
	public int searchCount;
	// 礼包使用期限
	@SerializedName("use_time")
	public String useDeadline;
    // 使用说明
	@SerializedName("note")
    public String note;
	// 礼包码
	@SerializedName("code")
	public String code;
}
