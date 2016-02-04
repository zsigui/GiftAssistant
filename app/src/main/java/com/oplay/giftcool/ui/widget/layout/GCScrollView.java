package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-2-2.
 */
public class GCScrollView extends ScrollView {

	private static final int MOVE_MIN_DISTANCE = 20;
	private static final int MOVE_MIN_VELOCITY = 200;
	// tan = abs(y/x) <= 1 = 45°
	private static final int MOVE_MIN_TAN = 1;
	private static final int POINTER_X = 0x1233ffff;
	private static final int POINTER_Y = 0x0233ffff;

	public GCScrollView(Context context) {
		this(context, null);
	}

	public GCScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GCScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private float startX;
	private float startY;
//	private VelocityTracker mTracker;
	private boolean mIsDragged = false;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {


		// 已经处于拖曳状态
		if (ev.getAction() == MotionEvent.ACTION_MOVE && mIsDragged) {
			return false;
		}

		boolean needIntercept = true;
		float xVelocity;
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = ev.getX();
				startY = ev.getY();
//				if (mTracker == null) {
//					mTracker = VelocityTracker.obtain();
//				} else {
//					mTracker.clear();
//				}
//				mTracker.addMovement(ev);
				break;
			case MotionEvent.ACTION_MOVE:
//				mTracker.addMovement(ev);
//				mTracker.computeCurrentVelocity(1, 1000);
//				if (AppDebugConfig.IS_DEBUG) {
//					KLog.d(AppDebugConfig.TAG_UTIL, "velocity = " + VelocityTrackerCompat.getXVelocity(mTracker,
//							POINTER_X)
//							+ ", tan = " + Math.abs((ev.getY() - startY) / (ev.getX() - startX)));
//				}
				if (abs(ev.getX(), startX) > MOVE_MIN_DISTANCE
						&& abs(ev.getY(), startY) / abs(ev.getX(), startX) <= MOVE_MIN_TAN) {
					mIsDragged = true;
					needIntercept = false;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsDragged = false;
//				xVelocity = VelocityTrackerCompat.getXVelocity(mTracker, POINTER_X);
//				if ((xVelocity > MOVE_MIN_VELOCITY
//						&& VelocityTrackerCompat.getYVelocity(mTracker, POINTER_Y) < xVelocity)
//						&& abs(ev.getY(), startY) / abs(ev.getX(), startX) <= MOVE_MIN_TAN) {
//					needIntercept = false;
//				}
//				if (mTracker != null) {
//					mTracker.recycle();
//					mTracker = null;
//				}
				if (abs(ev.getY(), startY) / abs(ev.getX(), startX) <= MOVE_MIN_TAN) {
					needIntercept = false;
				}
				break;
		}
		boolean finalResult = needIntercept && super.onInterceptTouchEvent(ev);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_UTIL, "needIntercept = " + needIntercept + ", finalResult = " + finalResult);
		}

		return finalResult;
	}

	private float abs(float old, float cur) {
		return Math.abs(cur - old);
	}

}
