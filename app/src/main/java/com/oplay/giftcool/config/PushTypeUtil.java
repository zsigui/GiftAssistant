package com.oplay.giftcool.config;

import android.content.Context;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.model.data.resp.PushMessageExtra;
import com.oplay.giftcool.model.data.resp.PushMessageGift;
import com.oplay.giftcool.ui.activity.MainActivity;
import com.oplay.giftcool.ui.fragment.gift.GiftFragment;
import com.oplay.giftcool.util.IntentUtil;
import com.socks.library.KLog;

/**
 * 推送消息类型
 * <p/>
 * Created by zsigui on 16-3-8.
 */
public class PushTypeUtil {

	// 推送行
	public static final int ACTION_GIFT = 1;

	// 礼包推送的key类型
	public static final int GIFT_KEY_ZERO = 1;
	public static final int GIFT_KEY_LIMIT = 2;
	public static final int GIFT_KEY_NORMAL = 3;

	public static void handleMessage(Context context, PushMessageExtra data) {

		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_JPUSH, "MainActivity = " + MainActivity.sGlobalHolder + ", type = " + data.type
					+ " , extra = " + data.extraJson);
		}
		switch (data.type) {
			case ACTION_GIFT:
				PushMessageGift messageGift =
						AssistantApp.getInstance().getGson().fromJson(data.extraJson, PushMessageGift.class);
				switch (messageGift.giftType) {
					case GIFT_KEY_ZERO:
						if (MainActivity.sGlobalHolder == null) {
							IntentUtil.jumpHome(context, true);
						}
						if (MainActivity.sGlobalHolder != null) {
							MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_ZERO);
						}
						break;
					case GIFT_KEY_LIMIT:
						IntentUtil.jumpGiftLimitList(context, true);
						break;
					case GIFT_KEY_NORMAL:
						if (MainActivity.sGlobalHolder == null) {
							IntentUtil.jumpHome(context, true);
						}
						if (MainActivity.sGlobalHolder != null) {
							MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
						}
						break;
				}
				break;
		}
	}
}
