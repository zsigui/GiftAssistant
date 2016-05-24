package com.oplay.giftcool.config.util;

import com.oplay.giftcool.model.data.resp.IndexGiftNew;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/30
 */
public class GiftTypeUtil {
	public static final int TYPE_COUNT = 13;

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
	// 正常礼包类型，已结束, disabled
	public static final int TYPE_NORMAL_FINISHED = 7;
	// 限量礼包类型，已抢号，disabled
	public static final int TYPE_LIMIT_SEIZED = 8;
	// 正常礼包类型，已淘号，normal
	public static final int TYPE_NORMAL_SEARCHED = 9;
	// 限量礼包类型，已抢完, disabled
	public static final int TYPE_LIMIT_EMPTY = 10;
	// 正常礼包类型，已抢号, disabled
	public static final int TYPE_NORMAL_SEIZED = 11;
	// 0元疯抢类型，可抢 disabled，其他状态同限量礼包类型相同
	public static final int TYPE_ZERO_SEIZE = 12;
	// 首充券类型,可抢
	public static final int TYPE_CHARGE_SEIZE = 13;
	// 首充券类型,可预约
	public static final int TYPE_CHARGE_UN_RESERVE = 14;
	// 首充券类型,已预约
	public static final int TYPE_CHARGE_RESERVED = 15;
	// 首充券类型,可领号
	public static final int TYPE_CHARGE_TAKE = 16;
	// 首充券类型,已抢号
	public static final int TYPE_CHARGE_SEIZED = 17;
	// 首充券类型,已抢完
	public static final int TYPE_CHARGE_EMPTY = 18;
	// 首充券类型,预约完
	public static final int TYPE_CHARGE_RESERVE_EMPTY = 19;
	public static final int TYPE_ERROR = 233;

	// 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束 , 6 可预约 , 7 预约完
	public static final int STATUS_WAIT_SEIZE = 1;
	public static final int STATUS_SEIZE = 2;
	public static final int STATUS_WAIT_SEARCH = 3;
	public static final int STATUS_SEARCH = 4;
	public static final int STATUS_FINISHED = 5;
	public static final int STATUS_RESERVE = 6;
	public static final int STATUS_RESERVE_FINISHED = 7;
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
	// 1 普通免费 2 普通 3 限量 4 0元抢 5 首充券
	public static final int GIFT_TYPE_NORMAL_FREE = 1;
	public static final int GIFT_TYPE_NORMAL = 2;
	public static final int GIFT_TYPE_LIMIT = 3;
	public static final int GIFT_TYPE_ZERO_SEIZE = 4;
	public static final int GIFT_TYPE_FIRST_CHARGE = 5;

	public static int getItemViewType(IndexGiftNew gift) {
		//KLog.e("gift_data : status = " + gift.status + ", gifttype = " + gift.giftType + ", giftstatus = " + gift
		// .seizeStatus);
		if (gift.seizeStatus != SEIZE_TYPE_NEVER) {
			switch (gift.seizeStatus) {
				case SEIZE_TYPE_SEIZED:
					if (gift.status == STATUS_SEARCH
							&& (gift.giftType == GIFT_TYPE_NORMAL
							|| gift.giftType == GIFT_TYPE_NORMAL_FREE)) {
						return TYPE_NORMAL_SEARCH;
					}
					if (gift.giftType == GIFT_TYPE_ZERO_SEIZE
							|| gift.giftType == GIFT_TYPE_LIMIT) {
						return TYPE_LIMIT_SEIZED;
					} else {
						return TYPE_NORMAL_SEIZED;
					}
				case SEIZE_TYPE_SEARCHED:
					if (gift.giftType == GiftTypeUtil.GIFT_TYPE_LIMIT
							|| gift.giftType == GiftTypeUtil.GIFT_TYPE_ZERO_SEIZE) {
						return TYPE_LIMIT_SEIZED;
					}
					return TYPE_NORMAL_SEARCHED;
			}
		} else {
			if (gift.giftType == GIFT_TYPE_ZERO_SEIZE) {
				// 0元抢
				switch (gift.status) {
					case STATUS_WAIT_SEIZE:
						return TYPE_LIMIT_WAIT_SEIZE;
					case STATUS_SEIZE:
						return TYPE_ZERO_SEIZE;
					case STATUS_WAIT_SEARCH:
						return TYPE_LIMIT_EMPTY;
					case STATUS_FINISHED:
						return TYPE_LIMIT_FINISHED;
				}
			} else if (gift.giftType == GIFT_TYPE_LIMIT) {
				// 珍贵礼包 状态判断
				switch (gift.status) {
					case STATUS_WAIT_SEIZE:
						return TYPE_LIMIT_WAIT_SEIZE;
					case STATUS_SEIZE:
						return TYPE_LIMIT_SEIZE;
					case STATUS_WAIT_SEARCH:
						return TYPE_LIMIT_EMPTY;
					case STATUS_FINISHED:
						return TYPE_LIMIT_FINISHED;
				}
			} // if finished
			else {
				switch (gift.status) {
					case STATUS_WAIT_SEIZE:
						return TYPE_NORMAL_WAIT_SEIZE;
					case STATUS_SEIZE:
						return TYPE_NORMAL_SEIZE;
					case STATUS_SEARCH:
						return TYPE_NORMAL_SEARCH;
					case STATUS_WAIT_SEARCH:
						return TYPE_NORMAL_WAIT_SEARCH;
					case STATUS_FINISHED:
						// 礼包到期，会显示结束
						return TYPE_NORMAL_FINISHED;
				}
			} // else finished
		}
		return TYPE_ERROR;
	}

	/**
	 * 获取类型,包含首充券的类型
	 */
	public static int getItemViewTypeWithChargeCode(IndexGiftNew gift) {
		if (gift.giftType == GIFT_TYPE_FIRST_CHARGE) {

			switch (gift.status) {
				case STATUS_RESERVE:
					switch (gift.seizeStatus) {
						case SEIZE_TYPE_NEVER:
						case SEIZE_TYPE_UN_RESERVE:
							// 首充券可预约
							return TYPE_CHARGE_UN_RESERVE;
						case SEIZE_TYPE_RESERVED:
							// 首充券已预约
							return TYPE_CHARGE_RESERVED;
					}
					break;
				case STATUS_RESERVE_FINISHED:
					switch (gift.seizeStatus) {
						case SEIZE_TYPE_NEVER:
						case SEIZE_TYPE_UN_RESERVE:
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
					}
					break;
				case STATUS_FINISHED:
					switch (gift.seizeStatus) {
						case SEIZE_TYPE_NEVER:
						case SEIZE_TYPE_UN_RESERVE:
							// 首充券的预约已经结束
							return TYPE_CHARGE_EMPTY;
						case SEIZE_TYPE_RESERVED:
							// 首充券已抢
							return TYPE_CHARGE_SEIZED;
					}
					break;
			}
			// 礼包状态和抢号状态异常
			return TYPE_ERROR;
		} else {
			return getItemViewType(gift);
		}
	}
}
