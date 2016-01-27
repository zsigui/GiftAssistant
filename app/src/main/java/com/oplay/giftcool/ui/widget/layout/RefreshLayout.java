package com.oplay.giftcool.ui.widget.layout;

/**
 * Created by zsigui on 16-1-11.
 */

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.listener.FooterListener;
import com.socks.library.KLog;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * @author mrsimple
 */
public class RefreshLayout extends SwipeRefreshLayout implements AbsListView.OnScrollListener {

	/**
	 * 滑动到最下面时的上拉操作
	 */

	private int mTouchSlop;
	/**
	 * listview实例
	 */
	private ListView mListView;
	private RecyclerView mRecyclerView;

	/**
	 * 上拉监听器, 到了最底部的上拉加载操作
	 */
	private OnLoadListener mOnLoadListener;

	/**
	 * ListView的加载中footer
	 */
	private View mViewFooter;

	/**
	 * 按下时的y坐标
	 */
	private int mYDown;
	/**
	 * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
	 */
	private int mLastY;
	/**
	 * 是否在加载中 ( 上拉加载更多 )
	 */
	private boolean isLoading = false;
	/**
	 * 是否显示加载更多
	 */
	private boolean mCanShowLoad = true;
	private AnimationDrawable mFooterAnimDrawable;

	/**
	 * @param context
	 */
	public RefreshLayout(Context context) {
		this(context, null);
	}

	public RefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);


		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		mViewFooter = LayoutInflater.from(context).inflate(R.layout.view_item_footer, null,
				false);
		mFooterAnimDrawable  = (AnimationDrawable)(mViewFooter.findViewById(R.id.iv_anim).getBackground());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// 初始化ListView对象
		if (mListView == null && mRecyclerView == null) {
			getListView();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setColorSchemeResources(R.color.co_btn_red, R.color.co_btn_orange, R.color.co_btn_blue,
				R.color.co_btn_green);
	}

	public void setAdapter(ListAdapter adapter) {
		if (mListView != null) {
			if (mListView.getFooterViewsCount() == 0) {
				mListView.addFooterView(mViewFooter);
			}
			mListView.setAdapter(adapter);
			// 添加只是为了在ListView的setAdapter方法时将Adapter包装成HeaderViewListAdapter。因此并不需要footer，因此添加后再移除,
			mListView.removeFooterView(mViewFooter);
		}
	}

	/**
	 * 获取ListView对象
	 */
	private void getListView() {

		int childCount = getChildCount();
		if (childCount > 0) {
			for (int i = 0 ; i < getChildCount(); i++) {
				View childView = getChildAt(i);
				if (childView instanceof ListView) {
					mListView = (ListView) childView;
					// 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
					mListView.setOnScrollListener(this);
					return;
				}
				if (childView instanceof  RecyclerView) {
					mRecyclerView = (RecyclerView) childView;
					mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
						@Override
						public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
							super.onScrollStateChanged(recyclerView, newState);
						}

						@Override
						public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
							super.onScrolled(recyclerView, dx, dy);
							mLastY = dy;
							if (canLoad()) {
								KLog.e("refresh", mRecyclerView.getAdapter().getItemCount());
								mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
								loadData();
							}
						}
					});
					return;
				}
			}

		}
	}

	public boolean isCanShowLoad() {
		return mCanShowLoad;
	}

	public void setCanShowLoad(boolean canShowLoad) {
		mCanShowLoad = canShowLoad;
	}

	/*
		 * (non-Javadoc)
		 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
		 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				// 按下
				mYDown = (int) event.getRawY();
				break;

			case MotionEvent.ACTION_MOVE:
				// 移动
				mLastY = (int) event.getRawY();
				break;

			case MotionEvent.ACTION_UP:
				// 抬起
				if (canLoad()) {
					loadData();
				}
				break;
			default:
				break;
		}

		return super.dispatchTouchEvent(event);
	}

	/**
	 * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
	 *
	 * @return
	 */
	private boolean canLoad() {
		return mCanShowLoad && (isBottom() && !isLoading && isPullUp());
	}

	/**
	 * 判断是否到了最底部
	 */
	private boolean isBottom() {
		if (mListView != null && mListView.getAdapter() != null) {
			return mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
		} else if (mRecyclerView != null && mRecyclerView.getLayoutManager() != null) {
			View lastChildView = mRecyclerView.getLayoutManager()
					.getChildAt(mRecyclerView.getLayoutManager().getChildCount() - 1);
			int lastChildBottom = lastChildView.getBottom();
			int recyclerBottom = mRecyclerView.getBottom() - mRecyclerView.getPaddingBottom();

			int lastPosition = mRecyclerView.getLayoutManager().getPosition(lastChildView);
			if (lastChildBottom <= recyclerBottom
					&& lastPosition == mRecyclerView.getLayoutManager().getItemCount() - 1) {
				if (AppDebugConfig.IS_DEBUG) {
					KLog.d("slip to end ");
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否是上拉操作
	 *
	 * @return
	 */
	private boolean isPullUp() {
		return (mYDown - mLastY) >= mTouchSlop;
	}

	/**
	 * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
	 */
	private void loadData() {
		if (mOnLoadListener != null) {
			// 设置状态
			setLoading(true);
			//
			mOnLoadListener.onLoad();
		}
	}

	/**
	 * @param loading
	 */
	public void setLoading(boolean loading) {
		isLoading = loading;
		if (isLoading) {
			if (mListView != null && mListView.getAdapter() != null) {
				if (mListView.getFooterViewsCount() == 0) {
					mListView.addFooterView(mViewFooter);
				} else {
					mViewFooter.setVisibility(View.VISIBLE);
				}
				if (mFooterAnimDrawable != null) {
					mFooterAnimDrawable.start();
				}

			} else if (mRecyclerView != null && mRecyclerView.getAdapter() != null
					&& mRecyclerView.getAdapter() instanceof FooterListener) {
				((FooterListener)mRecyclerView.getAdapter()).showFooter(true);
			}
		} else {
			if (mListView != null && mListView.getAdapter() != null) {
				if (mListView.getAdapter() instanceof HeaderViewListAdapter) {
					mListView.removeFooterView(mViewFooter);
				} else {
					mViewFooter.setVisibility(View.GONE);
				}
				if (mFooterAnimDrawable != null) {
					mFooterAnimDrawable.stop();
				}
			} else if (mRecyclerView != null && mRecyclerView.getAdapter() != null
					&& mRecyclerView.getAdapter() instanceof FooterListener) {
				((FooterListener)mRecyclerView.getAdapter()).showFooter(false);
			}
			mYDown = 0;
			mLastY = 0;
		}
	}

	/**
	 * @param loadListener
	 */
	public void setOnLoadListener(OnLoadListener loadListener) {
		mOnLoadListener = loadListener;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
	                     int totalItemCount) {
		// 滚动时到了最底部也可以加载更多
		if (canLoad()) {
			loadData();
		}
	}

	/**
	 * 加载更多的监听器
	 *
	 * @author mrsimple
	 */
	public static interface OnLoadListener {
		public void onLoad();
	}
}

