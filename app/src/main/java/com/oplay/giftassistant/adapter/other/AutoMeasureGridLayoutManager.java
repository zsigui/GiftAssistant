package com.oplay.giftassistant.adapter.other;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zsigui on 16-1-10.
 */
public class AutoMeasureGridLayoutManager extends GridLayoutManager {
	public AutoMeasureGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public AutoMeasureGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}

	public AutoMeasureGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
		int height = 0;
		int childCount = getItemCount();

		for (int i = 0; i < childCount; i++) {
			if (i % getSpanCount() == 0) {
				View child = recycler.getViewForPosition(i);
				measureChild(child, widthSpec, heightSpec);
				int measuredHeight = child.getMeasuredHeight() + getDecoratedBottom(child);
				height += measuredHeight;
			}
		}
		setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);

	}

}
