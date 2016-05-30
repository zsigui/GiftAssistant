package com.oplay.giftcool.config.util;

import com.oplay.giftcool.model.data.resp.IndexGiftNew;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/30
 */
public class GiftTypeUtil {
	public static final int TYPE_COUNT = 24;

	// 限量礼包类型，可抢，limit
	public static final int TYPE_LIMIT_SEIZE = 0;
	// 限量礼包类型，等待抢，disabled - text
	public static final int TYPE_LIMIT_WAIT_SEIZE = 1;
	// 限量礼包类型，已结束, disabled
	public static final int TYPE_LIMIT_FINISHED = 2;
    // 限量礼包类型，已抢完, disabled
    public static final int TYPE_LIMIT_EMPTY = 3;
    // 限量礼包类型，已抢号，disabled
    public static final int TYPE_LIMIT_SEIZED = 4;
	// 正常礼包类型，可抢，normal
	public static final int TYPE_NORMAL_SEIZE = 5;
	// 正常礼包类型，可淘号，disabled - text
	public static final int TYPE_NORMAL_SEARCH = 6;
	// 正常礼包类型，等待抢号，disabled - text
	public static final int TYPE_NORMAL_WAIT_SEIZE = 7;
	// 正常礼包类型，等待淘号，disabled - text
	public static final int TYPE_NORMAL_WAIT_SEARCH = 8;
	// 正常礼包类型，已结束, disabled
	public static final int TYPE_NORMAL_FINISHED = 9;
	// 正常礼包类型，已淘号，normal
	public static final int TYPE_NORMAL_SEARCHED = 10;
	// 正常礼包类型，已抢号, disabled
	public static final int TYPE_NORMAL_SEIZED = 11;
    // 限量免费类型，无量，等待免费或者免费已空
    public static final int TYPE_LIMIT_FREE_EMPTY = 12;
    // 限量免费类型，可抢
    public static final int TYPE_LIMIT_FREE_SEIZE = 13;
    // 限量免费类型，已抢号
    public static final int TYPE_LIMIT_FREE_SEIZED = 14;
	// 首充券类型,可抢
	public static final int TYPE_CHARGE_SEIZE = 15;
	// 首充券类型,可预约
	public static final int TYPE_CHARGE_UN_RESERVE = 16;
	// 首充券类型,已预约
	public static final int TYPE_CHARGE_RESERVED = 17;
	// 首充券类型,可领号
	public static final int TYPE_CHARGE_TAKE = 18;
	// 首充券类型,已抢号
	public static final int TYPE_CHARGE_SEIZED = 19;
	// 首充券类型,已抢完
	public static final int TYPE_CHARGE_EMPTY = 20;
	// 首充券类型,预约完
	public static final int TYPE_CHARGE_RESERVE_EMPTY = 21;
    // 首充券类型,不可预约
    public static final int TYPE_CHARGE_DISABLE_RESERVE = 22;

	public static final int TYPE_ERROR = 23;

	// 礼包状态 0 删除, 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束 , 6 下架(0, 6状态不关注) 7 可预约 , 8 不可预约 9 预约完
	public static final int STATUS_WAIT_SEIZE = 1;
	public static final int STATUS_SEIZE = 2;
	public static final int STATUS_WAIT_SEARCH = 3;
	public static final int STATUS_SEARCH = 4;
	public static final int STATUS_FINISHED = 5;
	public static final int STATUS_RESERVE = 7;
    public static final int STATUS_DISABLED_RESERVE = 8;
	public static final int STATUS_RESERVE_FINISHED = 9;
	// 1 金币， 2 偶玩豆， 3 金币或偶玩豆
	public static final int PAY_TYPE_NONE = 0;
	public static final int PAY_TYPE_SCORE = 1;
	public static final int PAY_TYPE_BEAN = 2;
	public static final int PAY_TYPE_BOTH = 3;
	// 0 未抢 1 已抢 2 已淘 3 未预约 4 已预约待抢
	public static final int SEIZE_TYPE_NEVER = 0;
	public static final int SEIZE_TYPE_SEIZED = 1;
	public static final int SEIZE_TYPE_SEARCHED = 2;
	public static final int SEIZE_TYPE_UN_RESERVE = 3;
	public static final int SEIZE_TYPE_RESERVED = 4;
	// 1 普通免费 2 普通 3 限量 4 限量免费 (由0元抢转换)
	public static final int GIFT_TYPE_NORMAL_FREE = 1;
	public static final int GIFT_TYPE_NORMAL = 2;
	public static final int GIFT_TYPE_LIMIT = 3;
	public static final int GIFT_TYPE_LIMIT_FREE = 4;
    // 总类型值 0：未知(默认礼包) 1 免费 2 珍贵 3：首充券
    public static final int TOTAL_TYPE_UNKNOWN = 0;
    public static final int TOTAL_TYPE_GIFT = 1;
    public static final int TOTAL_TYPE_GIFT_LIMIT = 2;
    public static final int TOTAL_TYPE_FIRST_CHARGE = 3;

    /**
     * 只针对无免费抢的新鲜出炉类型判断
     */
	public static int getItemViewType(IndexGiftNew gift) {
		//KLog.e("gift_data : status = " + gift.status + ", gifttype = " + gift.giftType + ", giftstatus = " + gift
		// .seizeStatus);
        switch (gift.giftType) {
            case GIFT_TYPE_NORMAL:
            case GIFT_TYPE_NORMAL_FREE:
                // 针对普通免费礼包，该处不判断是否为首充券
                return handleNormalType(gift);
            case GIFT_TYPE_LIMIT:
                return handleLimitType(gift);
            case GIFT_TYPE_LIMIT_FREE:
                switch (gift.totalType) {
                    case TOTAL_TYPE_UNKNOWN:
                    case TOTAL_TYPE_GIFT:
                    case TOTAL_TYPE_GIFT_LIMIT:
                        return handleFreeLimitGift(gift);
                    case TOTAL_TYPE_FIRST_CHARGE:
                        return handleFreeFirstCharge(gift);

                }
                break;
        }
        return TYPE_ERROR;
	}

    /**
     * 处理限时免费的限量礼包类型
     */
    private static int handleFreeLimitGift(IndexGiftNew gift) {
        switch (gift.status) {
            case STATUS_WAIT_SEIZE:
                // 免费抢尚未开始 (该状态表示该礼包无关联的非免费礼包)
                return TYPE_LIMIT_FREE_EMPTY;
            case STATUS_SEIZE:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_LIMIT_FREE_SEIZE;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_FREE_SEIZED;
                }
                break;
            case STATUS_WAIT_SEARCH:
                // 对珍贵礼包来说表示号已抢完 (该状态表示该礼包无关联的非免费礼包)
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        // 表示免费号已经被抢完
                        return TYPE_LIMIT_FREE_EMPTY;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_FREE_SEIZED;
                }
                break;
            case STATUS_FINISHED:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        // 表示免费抢号已经结束 (该状态表示该礼包无关联的非免费礼包)
                        return TYPE_LIMIT_FREE_EMPTY;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_FREE_SEIZED;
                }
                break;
        }
        return TYPE_ERROR;
    }

    /**
     * 处理限时免费的首充券类型
     */
    private static int handleFreeFirstCharge(IndexGiftNew gift) {
        // 处理限时免费的首充券类型
        switch (gift.status) {
            case STATUS_DISABLED_RESERVE:
                return TYPE_CHARGE_DISABLE_RESERVE;
            case STATUS_RESERVE:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                    case SEIZE_TYPE_UN_RESERVE:
                        // 首充券可预约
                        return TYPE_CHARGE_UN_RESERVE;
                    case SEIZE_TYPE_RESERVED:
                    case SEIZE_TYPE_SEIZED:
                        // 首充券已预约
                        return TYPE_CHARGE_RESERVED;
                }
                break;
            case STATUS_RESERVE_FINISHED:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                    case SEIZE_TYPE_UN_RESERVE:
                    case SEIZE_TYPE_SEIZED:
                        // 首充券预约已结束
                        return TYPE_CHARGE_RESERVE_EMPTY;
                    case SEIZE_TYPE_RESERVED:
                        // 首充券已预约
                        return TYPE_CHARGE_RESERVED;
                }
                break;
            case STATUS_SEIZE:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                    case SEIZE_TYPE_UN_RESERVE:
                        // 未预约首充券可以抢号
                        return TYPE_CHARGE_SEIZE;
                    case SEIZE_TYPE_RESERVED:
                        // 已预约首充券可以领取
                        return TYPE_CHARGE_TAKE;
                    case SEIZE_TYPE_SEIZED:
                        // 首充券已抢或已领取
                        return TYPE_CHARGE_SEIZED;
                }
                break;
            case STATUS_WAIT_SEARCH:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                    case SEIZE_TYPE_UN_RESERVE:
                        // 首充券已经抢完
                        return TYPE_CHARGE_EMPTY;
                    case SEIZE_TYPE_RESERVED:
                    case SEIZE_TYPE_SEIZED:
                        // 首充券已抢
                        return TYPE_CHARGE_SEIZED;
                }
                break;
            case STATUS_FINISHED:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                    case SEIZE_TYPE_UN_RESERVE:
                        // 首充券活动已经结束
                        return TYPE_CHARGE_EMPTY;
                    case SEIZE_TYPE_RESERVED:
                    case SEIZE_TYPE_SEIZED:
                        // 首充券已抢
                        return TYPE_CHARGE_SEIZED;
                }
                break;
        }
        return TYPE_ERROR;
    }

    /**
     * 处理非免费的限量礼包类型
     */
    private static int handleLimitType(IndexGiftNew gift) {
        switch (gift.status) {
            case STATUS_WAIT_SEIZE:
                return TYPE_LIMIT_WAIT_SEIZE;
            case STATUS_SEIZE:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_LIMIT_SEIZE;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_SEIZED;
                }
                break;
            case STATUS_WAIT_SEARCH:
                // 对珍贵礼包来说表示号已抢完
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_LIMIT_EMPTY;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_SEIZED;
                }
                break;
            case STATUS_FINISHED:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_LIMIT_FINISHED;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_LIMIT_SEIZED;
                }
                break;
        }
        return TYPE_ERROR;
    }

    /**
     * 处理普通礼包类型
     */
    private static int handleNormalType(IndexGiftNew gift) {
        switch (gift.status) {
            case STATUS_WAIT_SEIZE:
                return TYPE_NORMAL_WAIT_SEIZE;
            case STATUS_SEIZE:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_NORMAL_SEIZE;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_NORMAL_SEIZED;
                }
                break;
            case STATUS_WAIT_SEARCH:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_NORMAL_WAIT_SEARCH;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_NORMAL_SEIZED;
                }
                break;
            case STATUS_SEARCH:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_NORMAL_SEARCH;
                    case SEIZE_TYPE_SEIZED:
                        // 已抢号
                        return TYPE_NORMAL_SEIZED;
                    case SEIZE_TYPE_SEARCHED:
                        return TYPE_NORMAL_SEARCHED;
                }
                break;
            case STATUS_FINISHED:
                switch (gift.seizeStatus) {
                    case SEIZE_TYPE_NEVER:
                        // 未抢号
                        return TYPE_NORMAL_FINISHED;
                    case SEIZE_TYPE_SEIZED:
                    case SEIZE_TYPE_SEARCHED:
                        // 已抢号
                        return TYPE_NORMAL_SEIZED;
                }
                break;
        }
        return TYPE_ERROR;
    }
//	public static int getItemViewTypeWithChargeCode(IndexGiftNew gift) {
//		if (gift.giftType == GIFT_TYPE_FIRST_CHARGE) {
//
//			switch (gift.status) {
//				case STATUS_RESERVE:
//					switch (gift.seizeStatus) {
//						case SEIZE_TYPE_NEVER:
//						case SEIZE_TYPE_UN_RESERVE:
//							// 首充券可预约
//							return TYPE_CHARGE_UN_RESERVE;
//						case SEIZE_TYPE_RESERVED:
//							// 首充券已预约
//							return TYPE_CHARGE_RESERVED;
//					}
//					break;
//				case STATUS_RESERVE_FINISHED:
//					switch (gift.seizeStatus) {
//						case SEIZE_TYPE_NEVER:
//						case SEIZE_TYPE_UN_RESERVE:
//							// 首充券预约已结束
//							return TYPE_CHARGE_RESERVE_EMPTY;
//						case SEIZE_TYPE_RESERVED:
//							// 首充券已预约
//							return TYPE_CHARGE_RESERVED;
//					}
//					break;
//				case STATUS_SEIZE:
//					switch (gift.seizeStatus) {
//						case SEIZE_TYPE_NEVER:
//						case SEIZE_TYPE_UN_RESERVE:
//							// 未预约首充券可以抢号
//							return TYPE_CHARGE_SEIZE;
//						case SEIZE_TYPE_RESERVED:
//							// 已预约首充券可以领取
//							return TYPE_CHARGE_TAKE;
//					}
//					break;
//				case STATUS_FINISHED:
//					switch (gift.seizeStatus) {
//						case SEIZE_TYPE_NEVER:
//						case SEIZE_TYPE_UN_RESERVE:
//							// 首充券的预约已经结束
//							return TYPE_CHARGE_EMPTY;
//						case SEIZE_TYPE_RESERVED:
//							// 首充券已抢
//							return TYPE_CHARGE_SEIZED;
//					}
//					break;
//			}
//			// 礼包状态和抢号状态异常
//			return TYPE_ERROR;
//		} else {
//			return getItemViewType(gift);
//		}
//	}
}
