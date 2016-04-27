package com.oplay.giftcool.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ScoreManager;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-4-15.
 */
public class GameObserverReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		if (intent == null || TextUtils.isEmpty(intent.getAction())) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_RECEIVER, "empty intent or action");
			}
			return;
		}
		if (AlarmClockManager.Action.ALARM_PLAY_GAME.equals(intent.getAction())
				&& intent.getCategories().contains(AlarmClockManager.Category.GCOOL_DEFAULT)) {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.d(AppDebugConfig.TAG_RECEIVER, "action = " + intent.getAction());
			}
			ThreadUtil.runInThread(new Runnable() {
				@Override
				public void run() {
					final int time = AlarmClockManager.ALARM_WAKE_ELAPSED_TIME / 1000;
					KLog.d(AppDebugConfig.TAG_WARN, "elpaseTime = " + time + ", time = " + (time /  1000));
					ScoreManager.getInstance().judgePlayTime(context, time);
				}
			});
		}
	}
}
