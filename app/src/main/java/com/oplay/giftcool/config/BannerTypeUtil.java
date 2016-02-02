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

	public static void handleBanner(Context context, IndexBanner banner) {
		if (banner == null) {
			return;
		}
		try {
			switch (banner.type) {
				case ACTION_WEB:
					WebData model = AssistantApp.getInstance().getGson().fromJson(banner.extData, WebData.class);
					if (TextUtils.isEmpty(model.url)) {
						return;
					}
					if (TextUtils.isEmpty(model.titleName)) {
						model.titleName = context.getResources().getString(R.string.st_web_default_title_name);
					}
					int index = model.url.indexOf("need_validate");
					if ((model.needValidate || (index != -1 && "1".equals(model.url.substring(index + 14, index + 15))))
							&& !AccountManager.getInstance().isLogin()) {
						ToastUtil.showShort("请先登录!");
						IntentUtil.jumpLogin(context);
						return;
					}
					IntentUtil.jumpActivityWeb(context, model.url, model.titleName);
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
