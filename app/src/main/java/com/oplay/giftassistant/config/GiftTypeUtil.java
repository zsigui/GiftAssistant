package com.oplay.giftassistant.config;

import com.oplay.giftassistant.model.data.resp.IndexGiftNew;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/30
 */
public class GiftTypeUtil {
    public static final int TYPE_COUNT = 12;

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
	public static final int TYPE_ERROR = 233;

	// 1 等待开始， 2 开始， 3 抢完， 4 淘号， 5 结束
	public static final int STATUS_WAIT_SEIZE = 1;
	public static final int STATUS_SEIZE = 2;
	public static final int STATUS_WAIT_SEARCH = 3;
	public static final int STATUS_SEARCH = 4;
	public static final int STATUS_FINISHED = 5;
	// 1 积分， 2 偶玩豆， 3 积分或偶玩豆
	public static final int PAY_TYPE_NONE = 0;
	public static final int PAY_TYPE_SCORE = 1;
	public static final int PAY_TYPE_BEAN = 2;
	public static final int PAY_TYPE_BOTH = 3;
	// 0 未抢 1 已抢 2 已淘
	public static final int SEIZE_TYPE_NEVER = 0;
	public static final int SEIZE_TYPE_SEIZED = 1;
	public static final int SEIZE_TYPE_SEARCHED = 2;

	public static int getItemViewType(IndexGiftNew gift) {
		if (gift.seizeStatus != SEIZE_TYPE_NEVER) {
			switch (gift.seizeStatus) {
				case SEIZE_TYPE_SEIZED:
					if (gift.isLimit) {
						return TYPE_LIMIT_SEIZED;
					} else {
						return TYPE_NORMAL_SEIZED;
					}
				case SEIZE_TYPE_SEARCHED:
					return TYPE_NORMAL_SEARCHED;
			}
		} else {
			if (gift.isLimit) {
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
}
