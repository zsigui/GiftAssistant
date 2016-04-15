package com.oplay.giftcool.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.util.ThreadUtil;
import com.socks.library.KLog;

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
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_SERVICE, "Start Round Connect");
		}
		if (mTimer == null) {
			mTimer = new Timer();
			// 每隔30秒通知所有可见UI界面重新请求数据刷新界面
			mTimer.schedule(new ClockTimer(), 0, 30 * 1000);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_SERVICE, "Stop Round Connect");
		}
		if (mTimer != null) {
			// 结束时关闭轮询
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}

	static class ClockTimer extends TimerTask {

		@Override
		public void run() {
			if (AppDebugConfig.IS_DEBUG) {
				KLog.v(AppDebugConfig.TAG_SERVICE, "Time Clock execute, Request Refresh UI");
			}
			ThreadUtil.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ObserverManager.getInstance().notifyGiftUpdate(ObserverManager.STATUS.GIFT_UPDATE_PART);
				}
			});
		}
	}
}
