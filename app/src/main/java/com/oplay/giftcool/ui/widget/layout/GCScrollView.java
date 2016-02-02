package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by zsigui on 16-2-2.
 */
public class GCScrollView extends ScrollView implements GestureDetector.OnGestureListener{

	private static final int FLING_MIN_DISTANCE = 20; // 移动最小距离
	private static final int FLING_MIN_VELOCITY = 200; // 移动最小速度
	private GestureDetector mGestureDetector;

	public GCScrollView(Context context) {
		this(context, null);
	}

	public GCScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GCScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mGestureDetector = new GestureDetector(context, this);
	}

	float startX;
	float startY;
	VelocityTrackerCompat mTracker;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = ev.getX();
				startY = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return super.onInterceptTouchEvent(ev);
	}


	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}
}
