package com.oplay.giftcool.ui.widget.transformer;

import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zsigui on 16-1-20.
 */
public class CubePageTransformer extends GCTransformer
{
	private float mMaxRotation = 90.0F;

	public CubePageTransformer() {}

	public CubePageTransformer(float maxRotation)
	{
		setMaxRotation(maxRotation);
	}

	public void handleInvisiblePage(View view, float position)
	{
		ViewHelper.setPivotX(view, view.getMeasuredWidth());
		ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5F);
		ViewHelper.setRotationY(view, 0.0F);
	}

	public void handleLeftPage(View view, float position)
	{
		ViewHelper.setPivotX(view, view.getMeasuredWidth());
		ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5F);
		ViewHelper.setRotationY(view, this.mMaxRotation * position);
	}

	public void handleRightPage(View view, float position)
	{
		ViewHelper.setPivotX(view, 0.0F);
		ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5F);
		ViewHelper.setRotationY(view, this.mMaxRotation * position);
	}

	public void setMaxRotation(float maxRotation)
	{
		if ((maxRotation >= 0.0F) && (maxRotation <= 90.0F)) {
			this.mMaxRotation = maxRotation;
		}
	}
}
