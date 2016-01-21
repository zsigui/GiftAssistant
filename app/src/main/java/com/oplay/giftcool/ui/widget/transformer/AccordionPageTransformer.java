package com.oplay.giftcool.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-20.
 */
public class AccordionPageTransformer extends GCTransformer {
	public AccordionPageTransformer() {
	}

	public void handleInvisiblePage(View view, float position) {
	}

	public void handleLeftPage(View view, float position) {
		ViewHelper.setPivotX(view, view.getWidth());
		ViewHelper.setScaleX(view, 1.0F + position);
	}

	public void handleRightPage(View view, float position) {
		ViewHelper.setPivotX(view, 0.0F);
		ViewHelper.setScaleX(view, 1.0F - position);
	}
}