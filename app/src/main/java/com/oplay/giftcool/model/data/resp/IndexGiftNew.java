package com.oplay.giftcool.model.data.resp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNew implements Serializable {

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
    /**
     * 礼包获取类型 0 未抢 1 已抢 2 已淘 3 未预约 4 已预约待抢
     */
    @SerializedName("code_type")
    public int seizeStatus;
    /**
     * 礼包状态 0 删除, 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束 , 6 下架(0, 6状态不关注) 7 可预约 , 8 预约完
     */
    @SerializedName("status")
    public int status;
    /**
     * 支付类型 0 未知， 1 金币， 2 偶玩豆， 3 金币或偶玩豆
     */
    @SerializedName("price_type")
    public int priceType;
    // 抢号需要换取金币
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
    @SerializedName("description")
    public String remark;
    // 礼包兑换方式
    @SerializedName("usage")
    public String usage;
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
    // 剩余开抢时间，单位:s
    @SerializedName("remain_time")
    public int remainStartTime;
    /**
     * 专享礼包类型, 1: 礼包酷专享
     */
    @SerializedName("exclusive")
    public int exclusive;
    // 支持平台
    @SerializedName("platform_label")
    public String platform;
    /**
     * 总分类型，0：礼包 1 免费 2 珍贵 3：首充券
     */
    @SerializedName("type")
    public int totalType;
    // 免费开抢时间, 0代表无免费礼包
    @SerializedName("free_start_time")
    public long freeStartTime;
    // 首充券预留时间
    @SerializedName("order_until_time")
    public String reserveDeadline;

    @SerializedName("img_urls")
    public ArrayList<String> usagePicsThumb;

    @SerializedName("full_img_urls")
    public ArrayList<String> usagePicsBig;

    /**
     * 布局样式
     */
    @SerializedName("ui_style")
    public int uiStyle;

    /**
     * 按钮状态
     */
    @SerializedName("button_state")
    public int buttonState;

    /**
     * 性质 0 普通 1 活动
     */
    @SerializedName("special")
    public int nature;

    /**
     * 跳转的活动ID
     */
    @SerializedName("jump_id")
    public int activityId;

    /**
     * 活动消息内容
     */
    @SerializedName("activity_title")
    public String activityTitle;

    /**
     * 活动状态
     */
    @SerializedName("activity_status")
    public int activityStatus;

    /**
     * 首充券使用状态：0 未使用 1 已使用 2 已过期
     */
    @SerializedName("use_status")
    public int usageStatus;
}
