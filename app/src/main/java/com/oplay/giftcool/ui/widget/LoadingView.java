package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.oplay.giftcool.R;

/**
 * Created by zsigui on 16-1-27.
 */
public class LoadingView extends ImageView{

	private AnimationDrawable mDrawable;

	public LoadingView(Context context) {
		this(context, null);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setBackgroundResource(R.drawable.view_load_more);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mDrawable = (AnimationDrawable) getBackground();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mDrawable.start();
	}

	@Override
	protected void onDetachedFromWindow() {
		mDrawable.stop();
		super.onDetachedFromWindow();
	}
}
