package com.oplay.giftcool.adapter.itemdecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.config.IndexTypeUtil;

/**
 * 绘制头部底端和底部顶端分割线
 * <p/>
 * Created by zsigui on 16-1-16.
 */
public class HeaderFooterDividerItemDecoration extends DividerItemDecoration {

	public HeaderFooterDividerItemDecoration(Context context, int orientation) {
		super(context, orientation);
	}

	public HeaderFooterDividerItemDecoration(Context context, int orientation, int dividerColor, int dividerSize) {
		super(context, orientation, dividerColor, dividerSize);
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mOrientation == LinearLayoutManager.VERTICAL) {
			drawVertical(c, parent);
		} else {
			drawHorizontal(c, parent);
		}
	}

	/**
	 * 绘制纵向 item 分割线
	 *
	 * @param canvas
	 * @param parent
	 */
	private void drawVertical(Canvas canvas, RecyclerView parent) {
		final int left = parent.getPaddingLeft();
		final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
		final View child = parent.getChildAt(0);
		RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
		final int top = child.getBottom() + layoutParams.bottomMargin;
		final int bottom = top + mDividerSize;
		canvas.drawRect(left, top, right, bottom, mPaint);
	}

	/**
	 * 绘制横向 item 分割线
	 *
	 * @param canvas
	 * @param parent
	 */
	private void drawHorizontal(Canvas canvas, RecyclerView parent) {
		final int top = parent.getPaddingTop();
		final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
		final View child = parent.getChildAt(0);
		RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
		final int left = child.getRight() + layoutParams.rightMargin;
		final int right = left + mDividerSize;
		canvas.drawRect(left, top, right, bottom, mPaint);
	}

	@Override
	public void getItemOffsets(Rect outRect, View view,
	                           RecyclerView parent, RecyclerView.State state) {
		int type = parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view));
		switch (type) {
			case IndexTypeUtil.ITEM_HEADER:
				if (mOrientation == LinearLayoutManager.VERTICAL) {
					outRect.set(0, 0, 0, mDividerSize);
				} else {
					outRect.set(0, 0, mDividerSize, 0);
				}
				break;
			case IndexTypeUtil.ITEM_FOOTER:
				if (mOrientation == LinearLayoutManager.VERTICAL) {
					outRect.set(0, mDividerSize, 0, 0);
				} else {
					outRect.set(mDividerSize, 0, 0, 0);
				}
				break;
		}
	}
}
