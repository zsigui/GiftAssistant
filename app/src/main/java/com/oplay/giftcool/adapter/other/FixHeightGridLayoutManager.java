package com.oplay.giftcool.adapter.other;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zsigui on 16-1-22.
 */
public class FixHeightGridLayoutManager extends GridLayoutManager {
	public FixHeightGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}

	public FixHeightGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	private int[] mMeasuredDimension = new int[2];

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
		final int widthMode = View.MeasureSpec.getMode(widthSpec);
		final int heightMode = View.MeasureSpec.getMode(heightSpec);
		final int widthSize = View.MeasureSpec.getSize(widthSpec);
		final int heightSize = View.MeasureSpec.getSize(heightSpec);
		int width = 0;
		int height = 0;
		for (int i = 0; i < getItemCount(); i++) {
			measureScrapChild(recycler, i,
					View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
					View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
					mMeasuredDimension
			);
			if (getOrientation() == HORIZONTAL) {
				if (i % getSpanCount() == 0) {
					width += mMeasuredDimension[0];
				}
				if (i == 0) {
					height = heightSize;
				}
			} else {
				if (i % getSpanCount() == 0) {
					height += mMeasuredDimension[1];
				}
				if (i == 0) {
					width = widthSize;
				}
			}
		}
		if (getOrientation() == HORIZONTAL) {
			width += getPaddingLeft();
			width += getPaddingRight();
		} else if (getOrientation() == VERTICAL) {
			height += getPaddingTop();
			height += getPaddingBottom();
		}
		switch (widthMode) {
			case View.MeasureSpec.EXACTLY: {
				width = widthSize;
			}
			case View.MeasureSpec.AT_MOST:
			case View.MeasureSpec.UNSPECIFIED:
		}
		switch (heightMode) {
			case View.MeasureSpec.EXACTLY: {
				height = heightSize;
			}
			case View.MeasureSpec.AT_MOST:
			case View.MeasureSpec.UNSPECIFIED:
		}
		setMeasuredDimension(width, height);
	}

	private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
	                               int heightSpec, int[] measuredDimension) {
		View view = recycler.getViewForPosition(position);
		if (view != null) {
			RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
			int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
					getPaddingLeft() + getPaddingRight(), p.width);
			int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
					getPaddingBottom() + getPaddingTop(), p.height);
			view.measure(childWidthSpec, childHeightSpec);
			measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
			measuredDimension[1] = view.getMeasuredHeight() + p.leftMargin + p.rightMargin;
			recycler.recycleView(view);
		}
	}
}
