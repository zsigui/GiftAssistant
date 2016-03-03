package com.oplay.giftcool.assist;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 根据需要重新修改 CountDownTimer
 *
 * Created by zsigui on 16-3-2.
 */
public abstract class CountTimer {

	/**
	 * Millis since epoch when alarm should stop.
	 */
	private long mMillisInFuture;

	/**
	 * The interval in millis that the user receives callbacks
	 */
	private final long mCountdownInterval;

	private long mStopTimeInFuture;

	/**
	 * boolean representing if the timer was cancelled
	 */
	private boolean mCancelled = false;

	private boolean mIsRunning = false;

	/**
	 * @param millisInFuture The number of millis in the future from the call
	 *   to {@link #start()} until the countdown is done and {@link #onFinish()}
	 *   is called.
	 * @param countDownInterval The interval along the way to receive
	 *   {@link #onTick(long)} callbacks.
	 */
	public CountTimer(long millisInFuture, long countDownInterval) {
		mMillisInFuture = millisInFuture;
		mCountdownInterval = countDownInterval;
	}

	/**
	 * Cancel the countdown.
	 */
	public synchronized final void cancel() {
		mCancelled = true;
		mIsRunning = false;
		mHandler.removeMessages(MSG);
	}

	/**
	 * Start the countdown.
	 */
	public synchronized final CountTimer start() {
		mCancelled = false;
		mIsRunning = true;
		if (mMillisInFuture <= 0) {
			onFinish();
			return this;
		}
		mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
		mHandler.sendMessage(mHandler.obtainMessage(MSG));
		return this;
	}

	/**
	 * Restart the countdown
	 *
	 * @param millisInFuture
	 * @return
	 */
	public synchronized final CountTimer restart(long millisInFuture) {
		mMillisInFuture = millisInFuture;
		cancel();
		return start();
	}

	/**
	 * Get the running state of the countdown
	 *
	 * @return
	 */
	public boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * Callback fired on regular interval.
	 * @param millisUntilFinished The amount of time until finished.
	 */
	public abstract void onTick(long millisUntilFinished);

	/**
	 * Callback fired when the time is up.
	 */
	public abstract void onFinish();


	private static final int MSG = 1;


	// handles counting down
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			synchronized (CountTimer.this) {
				if (mCancelled) {
					return;
				}

				final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

				if (millisLeft <= 0) {
					onFinish();
				} else if (millisLeft < mCountdownInterval) {
					// no tick, just delay until done
					sendMessageDelayed(obtainMessage(MSG), millisLeft);
				} else {
					long lastTickStart = SystemClock.elapsedRealtime();
					onTick(millisLeft);

					// take into account user's onTick taking time to execute
					long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

					// special case: user's onTick took more than interval to
					// complete, skip to next interval
					while (delay < 0) delay += mCountdownInterval;

					sendMessageDelayed(obtainMessage(MSG), delay);
				}
			}
		}
	};
}
