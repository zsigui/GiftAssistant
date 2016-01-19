package com.oplay.giftassistant.config;

import android.content.Context;

import com.oplay.giftassistant.AssistantApp;
import com.oplay.giftassistant.model.data.resp.IndexBanner;
import com.oplay.giftassistant.model.data.resp.IndexGameNew;
import com.oplay.giftassistant.model.data.resp.IndexGiftNew;
import com.oplay.giftassistant.util.IntentUtil;
import com.socks.library.KLog;

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
		try {
			switch (banner.type) {
				case ACTION_WEB:
					break;
				case ACTION_GAME_DETAIL:
					KLog.e(banner.extData);
					IndexGameNew game_d = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew.class);
					IntentUtil.jumpGameDetail(context, game_d.id, GameTypeUtil.JUMP_STATUS_DETAIL);
					break;
				case ACTION_SCORE_TASK:
					IntentUtil.jumpEarnScore(context);
					break;
				case ACTION_GIFT_DETAIL:
					KLog.e(banner.extData);
					IndexGiftNew gift_o = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGiftNew.class);
					IntentUtil.jumpGiftDetail(context, gift_o.id);
					break;
				case ACTION_GAME_DETAIL_GIFT:
					KLog.e(banner.extData);
					IndexGameNew game_g = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew.class);
					IntentUtil.jumpGameDetail(context, game_g.id, GameTypeUtil.JUMP_STATUS_GIFT);
					break;
				case ACTION_DEFAULT:
				default:

			}
		} catch (Throwable t) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_UTIL, t);
			}
		}
	}
}
