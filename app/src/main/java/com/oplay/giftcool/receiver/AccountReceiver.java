package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.manager.OuwanSDKManager;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

/**
 * Created by zsigui on 16-9-5.
 */
public class AccountReceiver extends BroadcastReceiver {

    public static final String ACTION_SELECT = AppConfig.PACKAGE_NAME + ".account.select";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE.equalsIgnoreCase(action)) {
                OuwanSDKManager.getInstance().showChangeAccountView();
            } else if (ACTION_SELECT.equalsIgnoreCase(action)) {
                OuwanSDKManager.getInstance().showSelectAccountView();
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }
}
