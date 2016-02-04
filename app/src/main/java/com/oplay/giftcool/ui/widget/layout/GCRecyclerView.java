package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import com.oplay.giftcool.config.AppDebugConfig;
import com.socks.library.KLog;

/**
 * Created by zsigui on 16-2-2.
 */
public class GCRecyclerView extends RecyclerView {

	private static final int MOVE_MIN_DISTANCE = 20;
	private static final int MOVE_MIN_VELOCITY = 200;
	// tan = abs(y/x) <= 1 = 45°
	private static final int MOVE_MIN_TAN = 1;
	private static final int POINTER_X = 0x1233ffff;
	private static final int POINTER_Y = 0x0233ffff;

	public GCRecyclerView(Context context) {
		this(context, null);
	}

	public GCRecyclerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GCRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private float startX;
	private float startY;
	private VelocityTracker mTracker;
	private boolean mIsDragged = false;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {


		// 已经处于拖曳状态
		if (ev.getAction() == MotionEvent.ACTION_MOVE && mIsDragged) {
			return true;
		}

		boolean needIntercept = false;
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = ev.getX();
				startY = ev.getY();
				if (mTracker == null) {
					mTracker = VelocityTracker.obtain();
				} else {
					mTracker.clear();
				}
				mTracker.addMovement(ev);
				break;
			case MotionEvent.ACTION_MOVE:
				mTracker.addMovement(ev);
				mTracker.computeCurrentVelocity(1, 1000);
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d(AppDebugConfig.TAG_UTIL, "velocity = " + VelocityTrackerCompat.getXVelocity(mTracker,
							POINTER_X)
							+ ", tan = " + Math.abs((ev.getY() - startY) / (ev.getX() - startX)));
				}
				if (abs(ev.getX(), startX) > MOVE_MIN_DISTANCE
						&& abs((ev.getY() - startY), (ev.getX() - startX)) <= MOVE_MIN_TAN) {
					mIsDragged = true;
					needIntercept = true;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsDragged = false;
				if (VelocityTrackerCompat.getXVelocity(mTracker, POINTER_X) > MOVE_MIN_VELOCITY
						&& abs((ev.getY() - startY), (ev.getX() - startX)) <= MOVE_MIN_TAN) {
					needIntercept = true;
				}
				if (mTracker != null) {
					mTracker.recycle();
					mTracker = null;
				}
				break;
		}
		boolean finalResult = needIntercept || super.onInterceptTouchEvent(ev);
		if (AppDebugConfig.IS_DEBUG) {
			KLog.d(AppDebugConfig.TAG_UTIL, "needIntercept = " + needIntercept + ", finalResult = " + finalResult);
		}

		return finalResult;
	}

	private float abs(float old, float cur) {
		return Math.abs(cur - old);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {

		return super.onTouchEvent(e);
	}
}
