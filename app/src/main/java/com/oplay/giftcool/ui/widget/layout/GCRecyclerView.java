package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

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
	private boolean mIsHorizontal = false;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {


		if (ev.getAction() == MotionEvent.ACTION_MOVE && mIsHorizontal) {
			getParent().requestDisallowInterceptTouchEvent(true);
			return false;
		}

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
				int xDiff = (int) abs(ev.getX(), startX);
				int yDiff = (int) abs(ev.getY(), startY);
				if (xDiff > yDiff
						|| VelocityTrackerCompat.getXVelocity(mTracker, POINTER_X)
						> VelocityTrackerCompat.getYVelocity(mTracker, POINTER_Y)) {
					mIsHorizontal = true;
					requestDisallowInterceptTouchEvent(true);
					return false;
				}
				break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsHorizontal = false;
				requestDisallowInterceptTouchEvent(false);
				if (mTracker != null) {
					mTracker.recycle();
					mTracker = null;
				}
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private float abs(float old, float cur) {
		return Math.abs(cur - old);
	}
}
