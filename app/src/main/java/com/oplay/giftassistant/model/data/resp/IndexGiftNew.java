package com.oplay.giftassistant.model.data.resp;

import java.io.Serializable;

/**
 * Created by zsigui on 15-12-24.
 */
public class IndexGiftNew implements Serializable{

    // 逻辑说明：
    // isLimit = 1，限量礼包，seizeTime
    /*
    public void logic() {
        long currentTime = System.currentTimeMillis();
        if (isLimit == 1) {
            if (currentTime > seizeTime) {
                // 已经开抢
                if (remainCount == 0) {
                    // 已经结束
                } else {
                    // 抢号中
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
            }
        } else {
            if (currentTime > seizeTime) {
                // 已经开抢
                if (remainCount == 0) {
                    // 已经结束
                    // 淘号逻辑
                    if (currentTime > searchTime) {
                        // 处于淘号状态
                    } else {
                        // 等待淘号
                    }
                } else {
                    // 抢号中
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
            }
        }
    }
    */
    // 礼包名称
	public String name;
	// 礼包内容
	public String content;
    // 游戏名称
	public String gameName;
    // 游戏图标
	public String img;
    // 是否为限量礼包,，是1，否0
    public int isLimit;
    // 抢号需要换取积分，-1代表不可用该方式兑换
    public int score;
    // 抢号需要换取的偶玩豆，-1代表不可用该方式兑换
    public int bean;
    // 开抢时间戳
    public long seizeTime;
    // 开淘时间戳
    public long searchTime;
    // 剩余礼包数量
    public int remainCount;
    // 礼包总数
    public int totalCount;
	// 已淘号次数
	public int searchCount;
}
