package net.ouwan.umipay.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.UmipayCommonAccountCacheManager;

import java.util.Date;

/**
 * UmipayAlarmReceiver
 *
 * @author jimmy
 *         date 16-8-29
 *         description
 */
public class UmipayAccountChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (UmipayCommonAccountCacheManager.ACTION_ACCOUNT_CHANGE.equalsIgnoreCase(action)) {
                Debug_Log.dd("receiver account change at " + new Date().toString());
                Intent serviceIntent = new Intent(context, UmipayService.class);
                serviceIntent.putExtra("action", UmipayService.ACTION_RESTART_CHANGE_ACCOUNT);
                context.startService(serviceIntent);
            }
        } catch (Throwable e) {
            Debug_Log.e(e);
        }
    }
}
