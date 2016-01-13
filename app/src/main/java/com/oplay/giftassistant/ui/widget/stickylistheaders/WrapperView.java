package com.oplay.giftassistant.ui.widget.stickylistheaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * the view that wrapps a divider header_list_section and a normal list item. The listview sees this as 1 item
 *
 * @author Emil Sjölander
 */
public class WrapperView extends ViewGroup {

	public View mItem;
	public Drawable mDivider;
	public int mDividerHeight;
	public View mHeader;
	public int mItemTop;

	public WrapperView(Context c) {
		super(c);
	}

	void update(View item, View header, Drawable divider, int dividerHeight) {

		//every wrapperview must have a list item
		if (item == null) {
			throw new NullPointerException("List view item must not be null.");
		}

		//only remove the current item if it is not the same as the new item. this can happen if wrapping a recycled
		// view
		if (this.mItem != item) {
			removeView(this.mItem);
			this.mItem = item;
			final ViewParent parent = item.getParent();
			if (parent != null && parent != this) {
				if (parent instanceof ViewGroup) {
					((ViewGroup) parent).removeView(item);
				}
			}
			addView(item);
		}

		//same logik as above but for the header_list_section
		if (this.mHeader != header) {
			if (this.mHeader != null) {
				removeView(this.mHeader);
			}
			this.mHeader = header;
			if (header != null) {
				addView(header);
			}
		}

		if (this.mDivider != divider) {
			this.mDivider = divider;
			this.mDividerHeight = dividerHeight;
			invalidate();
		}
	}

	public boolean hasHeader() {
		return mHeader != null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
				MeasureSpec.EXACTLY);
		int measuredHeight = 0;

		//measure header_list_section or divider. when there is a header_list_section visible it acts as the divider
		if (mHeader != null) {
			LayoutParams params = mHeader.getLayoutParams();
			if (params != null && params.height > 0) {
				mHeader.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
			} else {
				mHeader.measure(childWidthMeasureSpec,
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			}
			measuredHeight += mHeader.getMeasuredHeight();
		} else if (mDivider != null) {
			measuredHeight += mDividerHeight;
		}

		//measure item
		LayoutParams params = mItem.getLayoutParams();
		if (params != null && params.height > 0) {
			mItem.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY));
		} else {
			mItem.measure(childWidthMeasureSpec,
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}
		measuredHeight += mItem.getMeasuredHeight();

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeader == null && mDivider != null) {
			// Drawable.setBounds() does not seem to work pre-honeycomb. So have
			// to do this instead
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				canvas.clipRect(0, 0, getWidth(), mDividerHeight);
			}
			mDivider.draw(canvas);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		l = 0;
		t = 0;
		r = getWidth();
		b = getHeight();

		if (mHeader != null) {
			int headerHeight = mHeader.getMeasuredHeight();
			mHeader.layout(l, t, r, headerHeight);
			mItemTop = headerHeight;
			mItem.layout(l, headerHeight, r, b);
		} else if (mDivider != null) {
			mDivider.setBounds(l, t, r, mDividerHeight);
			mItemTop = mDividerHeight;
			mItem.layout(l, mDividerHeight, r, b);
		} else {
			mItemTop = t;
			mItem.layout(l, t, r, b);
		}
	}
}
