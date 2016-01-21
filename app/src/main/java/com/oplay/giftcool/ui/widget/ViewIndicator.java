package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 15-12-22.
 */
public class ViewIndicator extends LinearLayout implements ViewPager.OnPageChangeListener{

	private ViewPager mPager;
	private int mCurIndex;
	public int[] mIds;

	public ViewIndicator(Context context) {
		super(context);
	}

	public ViewIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ViewIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewIndicator, defStyleAttr, 0);
	}

	public void initPager(ViewPager pager, int curSelectedIndex) {
		mPager = pager;
		mCurIndex = curSelectedIndex;
		mPager.addOnPageChangeListener(this);
		mPager.setCurrentItem(curSelectedIndex);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}
}
