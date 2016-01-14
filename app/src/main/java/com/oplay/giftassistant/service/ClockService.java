package com.oplay.giftassistant.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.oplay.giftassistant.manager.ObserverManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zsigui on 16-1-14.
 */
public class ClockService extends Service {

	private Timer mTimer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mTimer == null) {
			mTimer = new Timer();
		}
		// 每隔10秒通知所有可见UI界面重新请求数据刷新界面
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				ObserverManager.getInstance().notifyGiftUpdate();
			}
		}, 0, 10 * 1000);
		return super.onStartCommand(intent, flags, startId);
	}
}
