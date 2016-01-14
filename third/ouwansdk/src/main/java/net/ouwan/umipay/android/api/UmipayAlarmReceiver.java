package net.ouwan.umipay.android.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.ouwan.umipay.android.debug.Debug_Log;
import net.ouwan.umipay.android.manager.PushPullAlarmManager;
import net.youmi.android.libs.common.v2.network.NetworkStatus;

import java.util.Date;

/**
 * UmipayAlarmReceiver
 *
 * @author zacklpx
 *         date 15-4-7
 *         description
 */
public class UmipayAlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
				Debug_Log.dd("Network change");
				if (NetworkStatus.isNetworkAvailable(context)) {
					if ("wifi" == NetworkStatus.getApn(context)) {
						PushPullAlarmManager.getInstance(context).startPolling();
					}
				}
			} else if (PushPullAlarmManager.ACTION_POLL_PUSH.equalsIgnoreCase(action)) {
				Debug_Log.dd("receiver alarm at " + new Date().toString());
				Intent serviceIntent = new Intent(context, UmipayService.class);
				serviceIntent.putExtra("action", UmipayService.ACTION_PULL);
				context.startService(serviceIntent);
			}
		} catch (Throwable e) {
			Debug_Log.e(e);
		}
	}
}
