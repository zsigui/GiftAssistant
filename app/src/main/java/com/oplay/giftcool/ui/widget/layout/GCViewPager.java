package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zsigui on 16-3-14.
 */
public class GCViewPager extends ViewPager {

	public GCViewPager(Context context) {
		this(context, null);
	}

	public GCViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private int mLastX;
	private int mLastY;
	private boolean mIsDragged;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mIsDragged && ev.getAction() == MotionEvent.ACTION_MOVE) {
			return true;
		}
		switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				int diffX = Math.abs((int)ev.getX() - mLastX);
				int diffY = Math.abs((int)ev.getY() - mLastY);
				if (diffX > diffY) {
					mIsDragged = true;
					return true;
				}
				break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastX = (int) ev.getX();
				mLastY = (int) ev.getY();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mIsDragged = false;
				requestDisallowInterceptTouchEvent(false);
				break;
		}
		return super.onTouchEvent(ev);
	}
}
