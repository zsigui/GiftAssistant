package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by zsigui on 16-2-2.
 */
public class GCScrollView extends ScrollView {

	private GestureDetector mGestureDetector;

	public GCScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, new YScrollDetector());
//		setFadingEdgeLength(0);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
//		return super.onInterceptTouchEvent(ev);
	}

	// Return false if we're scrolling in the x direction
	class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return Math.abs(distanceY) > Math.abs(distanceX) * 1.3;
		}
	}

}
