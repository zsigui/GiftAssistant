package com.oplay.giftassistant.adapter.other;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zsigui on 16-1-10.
 */
public class AutoMeasureLinearLayoutManager extends LinearLayoutManager {
	public AutoMeasureLinearLayoutManager(Context context) {
		super(context);
	}

	public AutoMeasureLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
	}

	public AutoMeasureLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec,int heightSpec) {
		View view = recycler.getViewForPosition(0);
		if(view != null){
			measureChild(view, widthSpec, heightSpec);
			int measuredWidth = View.MeasureSpec.getSize(widthSpec);
			int measuredHeight = view.getMeasuredHeight();
			setMeasuredDimension(measuredWidth, measuredHeight);
		}
	}
}
