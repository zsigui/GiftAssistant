package com.oplay.giftcool.adapter.layoutmanager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

/**
 * Created by zsigui on 16-3-4.
 */
public class NoFixWidthGridLayoutManager extends StaggeredGridLayoutManager {

	public NoFixWidthGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public NoFixWidthGridLayoutManager(int spanCount, int orientation) {
		super(spanCount, orientation);
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
		super.onMeasure(recycler, state, widthSpec, heightSpec);
	}
}
