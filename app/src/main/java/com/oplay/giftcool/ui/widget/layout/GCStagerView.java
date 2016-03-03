package com.oplay.giftcool.ui.widget.layout;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by zsigui on 16-3-4.
 */
public class GCStagerView extends RelativeLayout {

	private StagerAdapter mAdapter;
	private Handler mHandler;

	public GCStagerView(Context context) {
		this(context, null);
	}

	public GCStagerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GCStagerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mHandler = new Handler(Looper.myLooper());
	}

	public void setAdapter(StagerAdapter adapter) {
		this.mAdapter = adapter;
		notifyDataSetChange();
	}

	public void notifyDataSetChange() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mAdapter == null || mAdapter.getCount() == 0) {
					removeAllViews();
					return;
				}
				layoutChild();
			}
		});
	}

	private void layoutChild() {
		int count = mAdapter.getCount();
		for (int pos = 0; pos < count; pos ++) {
			View childView = mAdapter.getView(null, pos, this);
			ViewGroup.LayoutParams lp = childView.getLayoutParams();

		}
	}


	static abstract class StagerAdapter<T> {

		public abstract View getView(View convertView, int position, ViewGroup parent);

		public abstract int getCount();
	}

}
