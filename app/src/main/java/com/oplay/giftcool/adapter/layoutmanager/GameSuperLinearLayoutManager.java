package com.oplay.giftcool.adapter.layoutmanager;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.config.util.IndexTypeUtil;

/**
 * Created by zsigui on 16-1-10.
 */
public class GameSuperLinearLayoutManager extends LinearLayoutManager {

	private RecyclerView mRecyclerView;
	private int mItemHeight = -1;
	private int mHeaderHeight = -1;
	private int mFooterHeight = -1;

	public GameSuperLinearLayoutManager(RecyclerView recyclerView) {
		this(recyclerView, LinearLayoutManager.VERTICAL, false);
	}

	public GameSuperLinearLayoutManager(RecyclerView recyclerView, int orientation, boolean reverseLayout) {
		super(recyclerView.getContext(), orientation, reverseLayout);
		mRecyclerView = recyclerView;
	}

	@Override
	public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
		if (mRecyclerView == null || mRecyclerView.getAdapter() == null) {
			setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), 0);
			return;
		}
		int height = 0;
		for (int i = 0; i < getItemCount(); i++) {
			switch (mRecyclerView.getAdapter().getItemViewType(i)) {
				case IndexTypeUtil.ITEM_HEADER:
					if (mHeaderHeight == -1) {
						mHeaderHeight = measureHeight(recycler.getViewForPosition(i), widthSpec, heightSpec);
					}
					height += mHeaderHeight;
					break;
				case IndexTypeUtil.ITEM_FOOTER:
					if (mFooterHeight == -1) {
						mFooterHeight = measureHeight(recycler.getViewForPosition(i), widthSpec, heightSpec);
					}
					height += mFooterHeight;
				break;
				case IndexTypeUtil.ITEM_NORMAL:
					if (mItemHeight == -1) {
						mItemHeight = measureHeight(recycler.getViewForPosition(i), widthSpec, heightSpec);
					}
					height += mItemHeight;
					break;
			}
		}
		setMeasuredDimension(View.MeasureSpec.getSize(widthSpec), height);
	}

	public int measureHeight(View v, int widthSpec, int heightSpec) {
		measureChild(v, widthSpec, heightSpec);
		return v.getMeasuredHeight() + getDecoratedBottom(v);
	}
}
