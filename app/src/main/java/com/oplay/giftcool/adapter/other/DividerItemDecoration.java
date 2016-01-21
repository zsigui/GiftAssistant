package com.oplay.giftcool.adapter.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.oplay.giftcool.R;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2015/12/27
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

	/*
	 * RecyclerView的布局方向，默认先赋值
	 * 为纵向布局
	 * RecyclerView 布局可横向，也可纵向
	 * 横向和纵向对应的分割想画法不一样
	 * */
	protected int mOrientation = LinearLayoutManager.VERTICAL;

	/**
	 * item之间分割线的size，默认为1
	 */
	protected int mDividerSize = 1;
	protected DisplayMetrics mMetrics;

	/**
	 * 绘制item分割线的画笔，和设置其属性
	 * 来绘制个性分割线
	 */
	protected Paint mPaint;

	public DividerItemDecoration(Context context, int orientation) {
		this(context, orientation, context.getResources().getColor(R.color.co_divider_bg),
				context.getResources().getDimensionPixelSize(R.dimen.di_divider_height));
	}

	public DividerItemDecoration(Context context, int orientation, int dividerColor, int dividerSize) {
		this.mOrientation = orientation;
		if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
			throw new IllegalArgumentException("请传入正确的参数");
		}
		mMetrics = context.getResources().getDisplayMetrics();
		setDividerSize(dividerSize);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(dividerColor);
	     /*设置填充*/
		mPaint.setStyle(Paint.Style.FILL);
	}

	public void setOrientation(int orientation) {
		mOrientation = orientation;
	}

	public void setDividerColor(@ColorInt int dividerColor) {
		mPaint.setColor(dividerColor);
	}

	public void setDividerSize(int dividerSize) {
		mDividerSize = (int) TypedValue.applyDimension(dividerSize, TypedValue.COMPLEX_UNIT_DIP, mMetrics);
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
		final int childSize = parent.getChildCount();
		for (int i = 0; i < childSize - 1; i++) {
			final View child = parent.getChildAt(i);
			RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int top = child.getBottom() + layoutParams.bottomMargin;
			final int bottom = top + mDividerSize;
			canvas.drawRect(left, top, right, bottom, mPaint);
		}
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
		final int childSize = parent.getChildCount();
		for (int i = 0; i < childSize - 1; i++) {
			final View child = parent.getChildAt(i);
			RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
			final int left = child.getRight() + layoutParams.rightMargin;
			final int right = left + mDividerSize;
			canvas.drawRect(left, top, right, bottom, mPaint);
		}
	}

	/**
	 * 设置item分割线的size
	 *
	 * @param outRect
	 * @param view
	 * @param parent
	 * @param state
	 */
	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mOrientation == LinearLayoutManager.VERTICAL) {
			outRect.set(0, 0, 0, mDividerSize);
		} else {
			outRect.set(0, 0, mDividerSize, 0);
		}
	}

}
