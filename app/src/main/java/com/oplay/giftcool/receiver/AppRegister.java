package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppConfig;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by zsigui on 16-1-22.
 */
public class AppRegister extends BroadcastReceiver{

	public static abstract interface Action {
		public static final String REGISTER_APP_TO_WEIXIN = "";
		public static final String UNREGISTER_APP_FROM_WEIXIN = "";
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			if (intent.getAction() != null) {
				if (intent.getAction().equals(Action.REGISTER_APP_TO_WEIXIN)) {
					WXAPIFactory.createWXAPI(context, null).registerApp(AppConfig.SHARE_WEXIN_APP_ID);
				} else if (intent.getAction().equals(Action.UNREGISTER_APP_FROM_WEIXIN)) {
					WXAPIFactory.createWXAPI(context, null).unregisterApp();
				}
			}
		}
	}
}
