package com.oplay.giftcool.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-20.
 */
public class FlipPageTransformer extends GCTransformer
{
	private static final float ROTATION = 180.0F;

	public FlipPageTransformer() {}

	public void handleInvisiblePage(View view, float position) {}

	public void handleLeftPage(View view, float position)
	{
		ViewHelper.setTranslationX(view, -view.getWidth() * position);
		float rotation = 180.0F * position;
		ViewHelper.setRotationY(view, rotation);
		if (position > -0.5D) {
			view.setVisibility(0);
		} else {
			view.setVisibility(4);
		}
	}

	public void handleRightPage(View view, float position)
	{
		ViewHelper.setTranslationX(view, -view.getWidth() * position);
		float rotation = 180.0F * position;
		ViewHelper.setRotationY(view, rotation);
		if (position < 0.5D) {
			view.setVisibility(0);
		} else {
			view.setVisibility(4);
		}
	}
}
