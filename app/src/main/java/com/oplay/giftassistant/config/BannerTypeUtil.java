package com.oplay.giftassistant.config;

import android.content.Context;

import com.oplay.giftassistant.model.data.resp.IndexBanner;
import com.oplay.giftassistant.util.IntentUtil;

/**
 * Created by zsigui on 16-1-19.
 */
public class BannerTypeUtil {

	public static final int ACTION_DEFAULT = 0;
	public static final int ACTION_WEB = 1;
	public static final int ACTION_GAME_DETAIL = 2;
	public static final int ACTION_SCORE_TASK = 3;
	public static final int ACTION_GIFT_DETAIL = 4;
	public static final int ACTION_GAME_DETAIL_GIFT = 5;

	public static void handleBanner(Context context, IndexBanner banner) {
		if (banner == null) {
			return;
		}
		switch (banner.type) {
			case ACTION_WEB:
				break;
			case ACTION_GAME_DETAIL:
				IntentUtil.jumpGiftDetail(context, 1);
				break;
			case ACTION_SCORE_TASK:
				break;
			case ACTION_GIFT_DETAIL:
				break;
			case ACTION_GAME_DETAIL_GIFT:
				break;
		}
	}
}
