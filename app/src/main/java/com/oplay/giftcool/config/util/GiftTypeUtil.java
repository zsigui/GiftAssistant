package com.oplay.giftcool.config.util;

import com.oplay.giftcool.config.TypeStatusCode;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/30
 */
public class GiftTypeUtil {

    // 礼包状态 0 删除, 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束 , 6 下架(0, 6状态不关注) 7 可预约 , 8 预约完
    public static final int STATUS_WAIT_SEIZE = 1;
    public static final int STATUS_SEIZE = 2;
    public static final int STATUS_WAIT_SEARCH = 3;
    public static final int STATUS_SEARCH = 4;
    public static final int STATUS_FINISHED = 5;
    public static final int STATUS_TAKE_OFF = 6;
    public static final int STATUS_RESERVE = 7;
    public static final int STATUS_RESERVE_FINISHED = 8;
    // 1 金币， 2 偶玩豆， 3 金币或偶玩豆
    public static final int PAY_TYPE_NONE = 0;
    public static final int PAY_TYPE_SCORE = 1;
    public static final int PAY_TYPE_BEAN = 2;
    public static final int PAY_TYPE_BOTH = 3;
    // 0 未抢 1 已抢 2 已淘 3 预约
    public static final int SEIZE_TYPE_NEVER = 0;
    public static final int SEIZE_TYPE_SEIZED = 1;
    public static final int SEIZE_TYPE_SEARCHED = 2;
    public static final int SEIZE_TYPE_RESERVED = 3;
    // 1 普通免费 2 普通 3 限量 4 限量免费 (由0元抢转换) 5 活动
    public static final int GIFT_TYPE_NORMAL_FREE = 1;
    public static final int GIFT_TYPE_NORMAL = 2;
    public static final int GIFT_TYPE_LIMIT = 3;
    public static final int GIFT_TYPE_LIMIT_FREE = 4;
    // 总类型值 0：未知(默认礼包) 1 2 礼包 3：首充券
    public static final int TOTAL_TYPE_UNKNOWN = 0;
    public static final int TOTAL_TYPE_GIFT = 1;
    public static final int TOTAL_TYPE_GIFT_LIMIT = 2;
    public static final int TOTAL_TYPE_COUPON = 3;
    // 统一定义按钮的状态
    public static final int BUTTON_TYPE_WAIT_SEIZE = 1;
    public static final int BUTTON_TYPE_SEIZE = 2;
    public static final int BUTTON_TYPE_EMPTY = 3;
    public static final int BUTTON_TYPE_SEARCH = 4;
    public static final int BUTTON_TYPE_FINISH = 5;
    public static final int BUTTON_TYPE_TAKE_OFF = 6;
    public static final int BUTTON_TYPE_RESERVE = 7;
    public static final int BUTTON_TYPE_RESERVE_EMPTY = 8;
    public static final int BUTTON_TYPE_WAIT_SEARCH = 9;
    public static final int BUTTON_TYPE_SEIZED = 10;
    public static final int BUTTON_TYPE_RESERVE_TAKE = 11;
    public static final int BUTTON_TYPE_RESERVED = 12;
    public static final int BUTTON_TYPE_ACTIVITY_WAIT = 13;
    public static final int BUTTON_TYPE_ACTIVITY_JOIN = 14;
    public static final int BUTTON_TYPE_ACTIVITY_FINISHED = 15;
    // 显示样式 (说明地址: https://conf.umlife.net/pages/viewpage.action?pageId=35062216)
    public static final int UI_TYPE_COUNT = 19;
    public static final int UI_TYPE_DEFAULT = 0;
    public static final int UI_TYPE_NORMAL_SEIZE = 1;
    public static final int UI_TYPE_NORMAL_WAIT_SEIZE = 2;
    public static final int UI_TYPE_NORMAL_WAIT_SEARCH = 3;
    public static final int UI_TYPE_NORMAL_SEARCH = 4;
    public static final int UI_TYPE_NORMAL_OTHER = 5;
    public static final int UI_TYPE_PRECIOUS_SEIZE = 6;
    public static final int UI_TYPE_PRECIOUS_WAIT_SEIZE = 7;
    public static final int UI_TYPE_PRECIOUS_WAIT_SEARCH = 8;
    public static final int UI_TYPE_PRECIOUS_SEARCH = 9;
    public static final int UI_TYPE_PRECIOUS_OTHER = 10;
    public static final int UI_TYPE_FREE_SEIZE = 11;
    public static final int UI_TYPE_FREE_RESERVE = 12;
    public static final int UI_TYPE_FREE_WAIT_SEARCH = 13;
    public static final int UI_TYPE_FREE_SEARCH = 14;
    public static final int UI_TYPE_FREE_OTHER = 15;
    public static final int UI_TYPE_COUPON_SEIZE = 16;
    public static final int UI_TYPE_COUPON_RESERVE = 17;
    public static final int UI_TYPE_COUPON_OTHER = 18;
    // 首充券使用状态
    public static final int COUPON_USAGE_NEVER = 0;
    public static final int COUPON_USAGE_USED = 1;
    public static final int COUPON_USAGE_OVER = 2;

    // 礼包性质 0 普通 1 活动
    public static final int NATURE_COMMON = 0;
    public static final int NATURE_ACTIVITY = 1;

    public static int getButtonState(IndexGiftNew gift) {
        if (gift.nature == NATURE_ACTIVITY) {
            if (gift.seizeStatus == SEIZE_TYPE_SEIZED) {
                return BUTTON_TYPE_SEIZED;
            } else {
                switch (gift.activityStatus) {
                    case TypeStatusCode.POST_WAIT:
                        return BUTTON_TYPE_ACTIVITY_WAIT;
                    case TypeStatusCode.POST_FINISHED:
                        return BUTTON_TYPE_ACTIVITY_FINISHED;
                    case TypeStatusCode.POST_BEING:
                    default:
                        switch (gift.status) {
                            case STATUS_SEIZE:
                                return BUTTON_TYPE_SEIZE;
                        }
                        return BUTTON_TYPE_ACTIVITY_JOIN;
                }
            }
        } else {
            switch (gift.seizeStatus) {
                case SEIZE_TYPE_SEIZED:
                    return BUTTON_TYPE_SEIZED;
                case SEIZE_TYPE_RESERVED:
                    switch (gift.status) {
                        case STATUS_WAIT_SEIZE:
                            return BUTTON_TYPE_WAIT_SEIZE;
                        case STATUS_FINISHED:
                            return BUTTON_TYPE_FINISH;
                        case STATUS_TAKE_OFF:
                            return BUTTON_TYPE_TAKE_OFF;
                        case STATUS_RESERVE:
                        case STATUS_RESERVE_FINISHED:
                            return BUTTON_TYPE_RESERVED;
                        case STATUS_SEIZE:
                        case STATUS_WAIT_SEARCH:
                        case STATUS_SEARCH:
                        default:
                            return BUTTON_TYPE_RESERVE_TAKE;
                    }
                case SEIZE_TYPE_NEVER:
                case SEIZE_TYPE_SEARCHED:
                default:
                    switch (gift.status) {
                        case STATUS_WAIT_SEIZE:
                            return BUTTON_TYPE_WAIT_SEIZE;
                        case STATUS_WAIT_SEARCH:
                            if (gift.giftType == GIFT_TYPE_NORMAL
                                    || gift.giftType == GIFT_TYPE_NORMAL_FREE) {
                                return BUTTON_TYPE_WAIT_SEARCH;
                            } else {
                                return BUTTON_TYPE_EMPTY;
                            }
                        case STATUS_SEARCH:
                            return BUTTON_TYPE_SEARCH;
                        case STATUS_FINISHED:
                            return BUTTON_TYPE_FINISH;
                        case STATUS_TAKE_OFF:
                            return BUTTON_TYPE_TAKE_OFF;
                        case STATUS_RESERVE:
                            return BUTTON_TYPE_RESERVE;
                        case STATUS_RESERVE_FINISHED:
                            return BUTTON_TYPE_RESERVE_EMPTY;
                        case STATUS_SEIZE:
                        default:
                            return BUTTON_TYPE_SEIZE;
                    }
            }
        }
    }

    /**
     * 只针对无免费抢的新鲜出炉类型判断
     */
    public static int getUiStyle(IndexGiftNew gift) {
        switch (gift.totalType) {
            case TOTAL_TYPE_COUPON:
                return UI_TYPE_COUPON_RESERVE;
            case TOTAL_TYPE_GIFT_LIMIT:
                switch (gift.giftType) {
                    case GIFT_TYPE_LIMIT_FREE:
                    case GIFT_TYPE_NORMAL_FREE:
                        return UI_TYPE_FREE_RESERVE;
                    default:
                        switch (gift.status) {
                            case STATUS_SEIZE:
                                return UI_TYPE_PRECIOUS_SEIZE;
                            case STATUS_WAIT_SEIZE:
                            case STATUS_RESERVE:
                            case STATUS_RESERVE_FINISHED:
                                return UI_TYPE_PRECIOUS_WAIT_SEIZE;
                            case STATUS_WAIT_SEARCH:
                                return UI_TYPE_PRECIOUS_WAIT_SEARCH;
                            case STATUS_SEARCH:
                                return UI_TYPE_PRECIOUS_SEARCH;
                            default:
                                return UI_TYPE_PRECIOUS_OTHER;
                        }
                }
            default:
                switch (gift.status) {
                    case STATUS_SEIZE:
                        return UI_TYPE_NORMAL_SEIZE;
                    case STATUS_WAIT_SEIZE:
                    case STATUS_RESERVE:
                    case STATUS_RESERVE_FINISHED:
                        return UI_TYPE_NORMAL_WAIT_SEIZE;
                    case STATUS_WAIT_SEARCH:
                        return UI_TYPE_NORMAL_WAIT_SEARCH;
                    case STATUS_SEARCH:
                        return UI_TYPE_NORMAL_SEARCH;
                    default:
                        return UI_TYPE_NORMAL_OTHER;
                }
        }
    }
}
