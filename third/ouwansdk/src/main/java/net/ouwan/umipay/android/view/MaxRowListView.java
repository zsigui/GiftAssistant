package net.ouwan.umipay.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.umipay.android.pro.R;


/**
 * 支持设置android:maxRows来设置可见的项目数，要求列表项高度一致的情况可行
 * 默认情况为不设置上限，则为所有项目可见，列表变为不可滚动，可将此列表放置于ScrollView或其他滚动容器中
 *
 * @author CsHeng
 * @Date 14-8-19
 * @Time 下午5:47
 */
public class MaxRowListView extends NoBugListView {

	private int mMaxHeight;
	private int mMaxRows;

	public MaxRowListView(Context context) {
		this(context, null);
	}

	public MaxRowListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MaxRowListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaxAdapterView);
		mMaxRows = a.getInt(R.styleable.MaxAdapterView_android_maxRows, 0);
		a.recycle();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		adjustHeightBaseOnRows(mMaxRows);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightSpec);
		if (mMaxHeight != 0) {
			final float height = getMeasuredHeight();
			if (height > mMaxHeight) {
				setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mMaxHeight);
			}
		}
	}

	/**
	 * adjust listview height
	 *
	 * @param visibleRows
	 */
	private void adjustHeightBaseOnRows(int visibleRows) {

		// if not set visibleRows, that's measure all childs
		if (visibleRows == 0 || getAdapter() == null || visibleRows <= 0) {
			return;
		}

		final int count = getAdapter().getCount();
		if (count <= visibleRows) {
			return;
		}
		mMaxHeight = 0;

		final View listItem = getAdapter().getView(0, null, this);
		listItem.measure(0, 0);
		mMaxHeight = listItem.getMeasuredHeight() * visibleRows;
		invalidate();
//		requestLayout();
	}

	public int getMaxRows() {
		return mMaxRows;
	}

	public void setMaxRows(int maxRows) {
		mMaxRows = maxRows;
		adjustHeightBaseOnRows(mMaxRows);
	}

	public int getMaxHeight() {
		return mMaxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		mMaxHeight = maxHeight;
		invalidate();
//		requestLayout();
	}
}
