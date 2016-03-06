package com.oplay.giftcool.config;

import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.model.data.resp.PushMessageExtra;
import com.oplay.giftcool.ui.activity.GiftListActivity;
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

	public static final int ACTION_GIFT_ZERO = 1;
	public static final int ACTION_GIFT_LIMIT = 2;
	public static final int ACTION_GIFT_NEW = 3;

	public static void handleMessage(Context context, PushMessageExtra data) {
		if (MainActivity.sGlobalHolder == null) {
			IntentUtil.jumpHome(context, true);
		}
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_JPUSH, "MainActivity = " + MainActivity.sGlobalHolder + ", type = " + data.type
					+ " , extra = " + data.extraJson);
		}
		switch (data.type) {
			case ACTION_GIFT_ZERO:
				if (MainActivity.sGlobalHolder != null) {
					MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_ZERO);
				}
				break;
			case ACTION_GIFT_LIMIT:
				Intent intent = new Intent(context, GiftListActivity.class);
				intent.putExtra(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_GIFT_LIMIT);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				break;
			case ACTION_GIFT_NEW:
				if (MainActivity.sGlobalHolder != null) {
					MainActivity.sGlobalHolder.jumpToIndexGift(GiftFragment.POS_NEW);
				}
				break;
		}
	}
}
