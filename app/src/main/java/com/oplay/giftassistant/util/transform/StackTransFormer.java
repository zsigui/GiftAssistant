package com.oplay.giftassistant.util.transform;

import android.support.v4.view.ViewPager;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-19.
 */
public class StackTransFormer implements ViewPager.PageTransformer {
	public StackTransFormer() {
	}

	public void transformPage(View view, float position) {
		if (position < -1.0F) {
			handleInvisiblePage(view, position);
		} else if (position <= 0.0F) {
			handleLeftPage(view, position);
		} else if (position <= 1.0F) {
			handleRightPage(view, position);
		} else {
			handleInvisiblePage(view, position);
		}
	}

	public void handleInvisiblePage(View view, float position) {
	}

	;

	public void handleLeftPage(View view, float position) {
	}

	;

	public void handleRightPage(View view, float position) {
		ViewHelper.setTranslationX(view, -view.getWidth() * position);
	}
}