package net.ouwan.umipay.android.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.youmi.android.libs.common.global.Global_Final_Common_Millisecond;

/**
 * PushPullAlarmManager
 *
 * @author zacklpx
 *         date 15-4-7
 *         description
 */
public class PushPullAlarmManager {
	public final static String ACTION_POLL_PUSH = "net.ouwan.umipay.android.poll.push";
	private static PushPullAlarmManager mInstance = null;
	private Context mContext;

	private PushPullAlarmManager(Context context) {
		this.mContext = context;
	}

	public synchronized static PushPullAlarmManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PushPullAlarmManager(context.getApplicationContext());
		}
		return mInstance;
	}

	private void setPushPollingFrequency(long after_start, long interval) {
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(ACTION_POLL_PUSH);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, alarmIntent, 0);
		alarmManager.cancel(pendingIntent);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + after_start, interval,
				pendingIntent);
		mContext.sendBroadcast(alarmIntent);
	}

	public void startPolling() {
//		setPushPollingFrequency(3 * Global_Final_Common_Millisecond.oneHour_ms, 3 * Global_Final_Common_Millisecond
//				.oneHour_ms);
		setPushPollingFrequency(3 * Global_Final_Common_Millisecond.oneHour_ms, 3 * Global_Final_Common_Millisecond
				.oneHour_ms);
	}
}
