package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.PushMessageManager;
import com.oplay.giftcool.util.SystemUtil;
import com.socks.library.KLog;

import cn.jpush.android.service.PushService;

/**
 * Created by zsigui on 16-3-14.
 */
public class StartReceiver extends BroadcastReceiver {


	public static abstract class Action {
		public final static String ALARM_WAKE = "com.oplay.gitcool.action.ALARM_WAKE";
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || TextUtils.isEmpty(intent.getAction())) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_RECEIVER, "empty intent or action");
			}
			return;
		}
		if (context == null) {
			context = AssistantApp.getInstance().getApplicationContext();
		}
		String action = intent.getAction();
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_RECEIVER, "action = " + action);
			KLog.d(AppDebugConfig.TAG_RECEIVER, "category = " + intent.getCategories());
		}
//			PushMessageManager.getInstance().reInitPush(context);
		if (!SystemUtil.isServiceRunning(context, PushService.class.getName())) {
			// 服务不处于运行中，重启该服务
			if (!AssistantApp.getInstance().isGlobalInit()) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_RECEIVER, "app is exit, re-initial again!");
				}
				AssistantApp.getInstance().appInit();
			} else {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_RECEIVER, "push service is stop, re-started again");
				}
				PushMessageManager.getInstance().reInitPush(context);
			}
		} else {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_RECEIVER, "push service is running");
			}
		}
		if (Intent.ACTION_BATTERY_LOW.equals(action)) {
			// 手机没电,降低运行频率
			AlarmClockManager.getInstance().setElapsedTime(AlarmClockManager.ALARM_WAKE_ELAPSED_TIME * 3);
		} else if (Intent.ACTION_BATTERY_OKAY.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action)) {
			AlarmClockManager.getInstance().setElapsedTime(AlarmClockManager.ALARM_WAKE_ELAPSED_TIME);
		}
		AlarmClockManager.getInstance().startWakeAlarm(context.getApplicationContext());
	}
}