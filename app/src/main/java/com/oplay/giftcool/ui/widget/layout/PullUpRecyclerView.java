//package com.oplay.giftcool.ui.widget.layout;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewConfiguration;
//
//import com.oplay.giftcool.listener.FooterListener;
//import com.oplay.giftcool.listener.OnLoadListener;
//
///**
// * 实现上拉的RecyclerView
// *
// * Created by zsigui on 16-4-29.
// */
//public class PullUpRecyclerView extends RecyclerView {
//
//	public PullUpRecyclerView(Context context) {
//		this(context, null);
//	}
//
//	public PullUpRecyclerView(Context context, AttributeSet attrs) {
//		this(context, attrs, 0);
//	}
//
//	public PullUpRecyclerView(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//	}
//
//	private int mDownY;
//	private int mLastY;
//	private boolean mCanShowLoad = true;
//	private boolean isLoading = false;
//	private OnLoadListener mOnLoadListener;
//	private int mTouchSlop;
//
//	@Override
//	public boolean onTouchEvent(MotionEvent e) {
//		switch (e.getAction() & MotionEvent.ACTION_MASK) {
//			case MotionEvent.ACTION_DOWN:
//				mDownY = (int) e.getY();
//				break;
//		}
//		return super.onTouchEvent(e);
//	}
//
//	@Override
//	public void onScrollStateChanged(int state) {
//		super.onScrollStateChanged(state);
//	}
//
//	@Override
//	public void onScrolled(int dx, int dy) {
//		super.onScrolled(dx, dy);
//		mLastY = dy;
//		if (canLoad()) {
//			//mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
//			loadData();
//		}
//	}
//
//	/**
//	 * 是否可以加载更多, 条件是到了最底部, 且为上拉操作.
//	 *
//	 * @return
//	 */
//	private boolean canLoad() {
//		return mCanShowLoad && !isLoading && isBottom() && isPullUp();
//	}
//
//	/**
//	 * 判断是否到了最底部
//	 */
//	private boolean isBottom() {
//		View lastChildView = getLayoutManager()
//				.getChildAt(getLayoutManager().getChildCount() - 1);
//		if (lastChildView != null) {
//			int lastPosition = getLayoutManager().getPosition(lastChildView);
//			if (lastPosition == getLayoutManager().getItemCount() - 1) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * 是否是上拉操作
//	 *
//	 * @return
//	 */
//	private boolean isPullUp() {
//		return (mDownY - mLastY) >= mTouchSlop;
//	}
//
//	/**
//	 * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
//	 */
//	private void loadData() {
//		if (mOnLoadListener != null) {
//			// 设置状态
//			setLoading(true);
//			//
//			mOnLoadListener.onLoad();
//		}
//	}
//
//	public void setOnLoadListener(OnLoadListener onLoadListener) {
//		mOnLoadListener = onLoadListener;
//	}
//
//	public void setCanShowLoad(boolean canShowLoad) {
//		mCanShowLoad = canShowLoad;
//	}
//
//	/**
//	 * @param loading
//	 */
//	public void setLoading(boolean loading) {
//		isLoading = loading;
//		if (isLoading) {
//			if (getAdapter() instanceof FooterListener) {
//				((FooterListener) getAdapter()).showFooter(true);
//			}
//		} else {
//			if (getAdapter() instanceof FooterListener) {
//				((FooterListener) getAdapter()).showFooter(false);
//			}
//			mDownY = 0;
//			mLastY = 0;
//		}
//	}
//}
