package com.oplay.giftassistant.adapter.util;

import com.oplay.giftassistant.model.data.resp.IndexGiftNew;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/30
 */
public class GiftTypeUtil {
    public static final int TYPE_COUNT = 7;

    // 限量礼包类型，可抢，limit
    public static final int TYPE_LIMIT_SEIZE = 0;
    // 限量礼包类型，等待抢，disabled - text
    public static final int TYPE_LIMIT_WAIT_SEIZE = 1;
    // 限量礼包类型，已结束, disabled
    public static final int TYPE_LIMIT_FINISHED = 2;
    // 正常礼包类型，可抢，normal
    public static final int TYPE_NORMAL_SEIZE = 3;
    // 正常礼包类型，可淘号，disabled - text
    public static final int TYPE_NORMAL_SEARCH = 4;
    // 正常礼包类型，等待抢号，disabled - text
    public static final int TYPE_NORMAL_WAIT_SEIZE = 5;
    // 正常礼包类型，等待淘号，disabled - text
    public static final int TYPE_NORMAL_WAIT_SEARCH = 6;

    public static int getItemViewType(IndexGiftNew gift) {
        long currentTime = System.currentTimeMillis();
        if (gift.isLimit == 1) {
            if (currentTime > gift.seizeTime) {
                // 已经开抢
                if (gift.remainCount == 0) {
                    // 已经结束
                    return TYPE_LIMIT_FINISHED;
                } else {
                    // 抢号中
                    return TYPE_LIMIT_SEIZE;
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
                return TYPE_LIMIT_WAIT_SEIZE;
            }
        } // if finished
        else {
            if (currentTime > gift.seizeTime) {
                // 已经开抢
                if (gift.remainCount == 0) {
                    // 已经结束
                    // 淘号逻辑
                    if (currentTime > gift.searchTime) {
                        // 处于淘号状态
                        return TYPE_NORMAL_SEARCH;
                    } else {
                        // 等待淘号
                        return TYPE_NORMAL_WAIT_SEARCH;
                    }
                } else {
                    // 抢号中
                    return TYPE_NORMAL_SEIZE;
                }
            } else {
                // 等待抢号
                // time 代表抢号时间
                return TYPE_NORMAL_WAIT_SEIZE;
            }
        } // else finished
    }
}
