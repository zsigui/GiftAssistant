package net.ouwan.umipay.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author CsHeng
 * @Date 14-7-16
 * @Time 上午11:26
 */
public class NoBugListView extends ListView {

	public NoBugListView(Context context) {
		super(context);
	}

	public NoBugListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoBugListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		try {
			super.dispatchDraw(canvas);
		} catch (IndexOutOfBoundsException e) {
			// samsung error
		}
	}

}
