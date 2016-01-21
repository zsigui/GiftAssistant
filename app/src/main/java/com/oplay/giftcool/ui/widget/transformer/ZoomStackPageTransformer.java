package com.oplay.giftcool.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-20.
 */
public class ZoomStackPageTransformer extends GCTransformer
{
	public ZoomStackPageTransformer() {}

	public void handleInvisiblePage(View view, float position) {}

	public void handleLeftPage(View view, float position)
	{
		ViewHelper.setTranslationX(view, -view.getWidth() * position);

		ViewHelper.setPivotX(view, view.getWidth() * 0.5F);
		ViewHelper.setPivotY(view, view.getHeight() * 0.5F);
		ViewHelper.setScaleX(view, 1.0F + position);
		ViewHelper.setScaleY(view, 1.0F + position);
		if (position < -0.95F) {
			ViewHelper.setAlpha(view, 0.0F);
		} else {
			ViewHelper.setAlpha(view, 1.0F);
		}
	}

	public void handleRightPage(View view, float position)
	{
		ViewHelper.setTranslationX(view, -view.getWidth() * position);

		ViewHelper.setPivotX(view, view.getWidth() * 0.5F);
		ViewHelper.setPivotY(view, view.getHeight() * 0.5F);
		ViewHelper.setScaleX(view, 1.0F + position);
		ViewHelper.setScaleY(view, 1.0F + position);
		if (position > 0.95F) {
			ViewHelper.setAlpha(view, 0.0F);
		} else {
			ViewHelper.setAlpha(view, 1.0F);
		}
	}
}
