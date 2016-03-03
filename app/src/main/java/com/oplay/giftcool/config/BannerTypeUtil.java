package com.oplay.giftcool.config;

import android.content.Context;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.WebData;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.ToastUtil;
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
	public static final int ACTION_JOIN_QQ_GROUP = 6;

	public static void handleBanner(Context context, IndexBanner banner) {
		if (banner == null) {
			return;
		}
		try {
			switch (banner.type) {
				case ACTION_WEB:
					if (TextUtils.isEmpty(banner.extData)) {
						return;
					}
					if (TextUtils.isEmpty(banner.title)) {
						banner.title = context.getResources().getString(R.string.st_web_default_title_name);
					}
					WebData data = AssistantApp.getInstance().getGson().fromJson(banner.extData, WebData.class);
					IntentUtil.jumpActivityWeb(context, data.url, banner.title);
					break;
				case ACTION_GAME_DETAIL:
					IndexGameNew game_d = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew.class);
					IntentUtil.jumpGameDetail(context, game_d.id, GameTypeUtil.JUMP_STATUS_DETAIL);
					break;
				case ACTION_SCORE_TASK:
					if (!AccountManager.getInstance().isLogin()) {
						IntentUtil.jumpLogin(context);
						return;
					}
					IntentUtil.jumpEarnScore(context);
					break;
				case ACTION_GIFT_DETAIL:
					IndexGiftNew gift_o = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGiftNew.class);
					IntentUtil.jumpGiftDetail(context, gift_o.id);
					break;
				case ACTION_GAME_DETAIL_GIFT:
					IndexGameNew game_g = AssistantApp.getInstance().getGson().fromJson(banner.extData, IndexGameNew.class);
					IntentUtil.jumpGameDetail(context, game_g.id, GameTypeUtil.JUMP_STATUS_GIFT);
					break;
				case ACTION_JOIN_QQ_GROUP:
					if (TextUtils.isEmpty(banner.extData)) {
						return;
					}
					IntentUtil.joinQQGroup(context, banner.extData);
					break;
				case ACTION_DEFAULT:
				default:
					ToastUtil.showShort("请下载最新版本，当前版本尚未实现该功能！");

			}
		} catch (Throwable t) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.e(AppDebugConfig.TAG_UTIL, t);
			}
		}
	}
}
