package com.oplay.giftcool.manager;

import android.content.Context;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;

/**
 * Created by zsigui on 16-1-21.
 */
public class ShareManager {

	private static ShareManager manager;
	private static Context mContext = AssistantApp.getInstance().getApplicationContext();

	private ShareManager() {
	}

	public static ShareManager getInstance() {
		if (manager == null) {
			manager = new ShareManager();
		}
		return manager;
	}


	public void shareText(IndexGiftNew gift) {
		String url = WebViewUrl.GIFT_DETAIL + "plan_id=" + gift.id;
		String content = gift.content;
		String icon = gift.img;
		String title = gift.name;
		if (gift.isLimit) {
			// 分享限量礼包
			title += "(限今天)";
		} else {
			// 分享普通礼包
		}
	}

	public void shareSMS() {

	}
}
