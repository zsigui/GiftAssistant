package com.oplay.giftcool.adapter.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by zsigui on 16-1-10.
 */
public class AutoMeasureLinearLayoutManager extends LinearLayoutManager {

	private SparseArray<Integer> mCountHeightSparse;

	public AutoMeasureLinearLayoutManager(Context context) {
		super(context);
		mCountHeightSparse = new SparseArray<>();
	}

	public AutoMeasureLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
		mCountHeightSparse = new SparseArray<>();
	}

	public AutoMeasureLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mCountHeightSparse = new SparseArray<>();
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
		int childCount = getItemCount();
		if (childCount != 0 && mCountHeightSparse.get(childCount) == null) {
			int height = 0;
			for (int i = 0; i < childCount; i++) {
				View child = recycler.getViewForPosition(i);
				measureChild(child, widthSpec, heightSpec);
				int measuredHeight = child.getMeasuredHeight() + getDecoratedBottom(child);
				height += measuredHeight;
			}
			mCountHeightSparse.put(childCount, height);
		}
		Integer realHeight = mCountHeightSparse.get(childCount);
		setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), realHeight == null ? 0 : realHeight);
	}
}
