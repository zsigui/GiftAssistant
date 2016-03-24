package com.oplay.giftcool.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.receiver.StartReceiver;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-3-14.
 */
public class AlarmClockManager {

	// 10秒唤醒闹钟设置
	public static final int ALARM_WAKE_ELAPSED_TIME = 10 * 1000;
	private static final int ALARM_WAKE_REQUEST_CODE = 0xF01;
	private static final int NOTIFY_GIFT_UPDATE_ELAPSED_COUNT = 3;

	public static AlarmClockManager sInstance;

	public static AlarmClockManager getInstance() {
		if (sInstance == null) {
			sInstance = new AlarmClockManager();
		}
		return sInstance;
	}

	private AlarmClockManager(){}


	private AlarmManager mManager;
	private PendingIntent alarmSender = null;
	// 一次唤醒间隔时间
	private int mElapsedTime = ALARM_WAKE_ELAPSED_TIME;
	// 唤醒的次数
	private int mWakeCount = 0;
	// 是否允许通知礼包更新
	private boolean mAllowNotifyGiftUpdate;

	public AlarmManager getAlarmManager(Context context) {
		if (mManager == null) {
			mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		}
		return mManager;
	}

	public void setElapsedTime(int elapsedTime) {
		mElapsedTime = elapsedTime;
	}

	/**
	 * 获取唤醒允许礼包更新状态
	 */
	public boolean isAllowNotifyGiftUpdate() {
		return mAllowNotifyGiftUpdate;
	}

	/**
	 * 设置是否允许通知礼包更新
 	 */
	public void setAllowNotifyGiftUpdate(boolean allowNotifyGiftUpdate) {
		mAllowNotifyGiftUpdate = allowNotifyGiftUpdate;
	}

	/**
	 * 启动唤醒闹钟
	 */
	public void startWakeAlarm(Context context) {
		if (alarmSender == null) {
			Intent startIntent = new Intent(context, StartReceiver.class);
			startIntent.setAction(StartReceiver.Action.ALARM_WAKE);
			try {
				alarmSender = PendingIntent.getBroadcast(context, ALARM_WAKE_REQUEST_CODE,
						startIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			} catch (Exception e) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_RECEIVER, "unable to start broadcast");
				}
			}
		}
		mWakeCount++;
		if (mAllowNotifyGiftUpdate && mWakeCount % NOTIFY_GIFT_UPDATE_ELAPSED_COUNT == 0) {
			// 允许的情况下，每唤醒3次通知一次更新
			Global.THREAD_POOL.execute(new Runnable() {
				@Override
				public void run() {
					ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
				}
			});
		}
		AlarmManager am = getAlarmManager(context);
		am.cancel(alarmSender);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mElapsedTime, alarmSender);
	}


	/**
	 * 停止唤醒闹钟
	 */
	public void stopWakeAlarm(Context context) {
		getAlarmManager(context).cancel(alarmSender);
	}
}
