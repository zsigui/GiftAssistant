package com.oplay.giftcool.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-20.
 */
public class RotatePageTransformer
		extends GCTransformer
{
	private float mMaxRotation = 15.0F;

	public RotatePageTransformer() {}

	public RotatePageTransformer(float maxRotation)
	{
		setMaxRotation(maxRotation);
	}

	public void handleInvisiblePage(View view, float position)
	{
		ViewHelper.setPivotX(view, view.getMeasuredWidth() * 0.5F);
		ViewHelper.setPivotY(view, view.getMeasuredHeight());
		ViewHelper.setRotation(view, 0.0F);
	}

	public void handleLeftPage(View view, float position)
	{
		float rotation = this.mMaxRotation * position;
		ViewHelper.setPivotX(view, view.getMeasuredWidth() * 0.5F);
		ViewHelper.setPivotY(view, view.getMeasuredHeight());
		ViewHelper.setRotation(view, rotation);
	}

	public void handleRightPage(View view, float position)
	{
		handleLeftPage(view, position);
	}

	public void setMaxRotation(float maxRotation)
	{
		if ((maxRotation >= 0.0F) && (maxRotation <= 40.0F)) {
			this.mMaxRotation = maxRotation;
		}
	}
}
