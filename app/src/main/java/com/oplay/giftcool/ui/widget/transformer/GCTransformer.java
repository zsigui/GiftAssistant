package com.oplay.giftcool.ui.widget.transformer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by zsigui on 16-1-20.
 */
public abstract class GCTransformer
		implements ViewPager.PageTransformer {
	public GCTransformer() {
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

	public abstract void handleInvisiblePage(View paramView, float paramFloat);

	public abstract void handleLeftPage(View paramView, float paramFloat);

	public abstract void handleRightPage(View paramView, float paramFloat);
}
