package com.oplay.giftassistant.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-19.
 */
public class StackTransFormer extends GCTransformer {
	public StackTransFormer() {
	}

	public void transformPage(View view, float position) {
	}

	public void handleInvisiblePage(View view, float position) {
	}

	public void handleLeftPage(View view, float position) {
	}

	public void handleRightPage(View view, float position) {
		ViewHelper.setTranslationX(view, -view.getWidth() * position);
	}
}