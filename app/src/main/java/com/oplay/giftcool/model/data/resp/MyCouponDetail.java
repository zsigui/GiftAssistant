package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zsigui on 16-8-31.
 */
public class MyCouponDetail extends GameDownloadInfo {

    // 礼包标志
    @SerializedName("plan_id")
    public int id;
    // 礼包名称
    @SerializedName("name")
    public String giftName;
    // 礼包内容
    @SerializedName("content")
    public String content;
    /**
     * 礼包状态 0 删除, 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束 , 6 下架(0, 6状态不关注) 7 可预约 , 8 预约完
     */
    @SerializedName("status")
    public int status;
    // 抢号需要换取金币
    @SerializedName("point_price")
    public int score;
    // 抢号需要换取的偶玩豆
    @SerializedName("owanb_price")
    public int bean;
    // 礼包总数
    @SerializedName("total_count")
    public int totalCount;
    // 使用开始时间
    @SerializedName("use_start_time")
    public String useStartTime;
    // 使用结束时间
    @SerializedName("use_end_time")
    public String useEndTime;
    // 礼包码
    @SerializedName("gift_key")
    public String code;
    /**
     * 礼包类型 1 普通免费 2 普通 3 限量 4 0元抢
     */
    @SerializedName("plan_type")
    public int giftType;
    // 原价
    @SerializedName("original_price")
    public int originPrice;
    // 支持平台
    @SerializedName("platform_label")
    public String platform;
    /**
     * 总分类型，0：礼包 1 免费 2 珍贵 3：首充券
     */
    @SerializedName("type")
    public int totalType;
    /**
     * 首充券使用状态：0 未使用 1 已使用 2 已过期
     */
    @SerializedName("use_status")
    public int usageStatus;
}
