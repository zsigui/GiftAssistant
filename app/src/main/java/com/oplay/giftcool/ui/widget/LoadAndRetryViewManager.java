package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/2
 */
public class LoadAndRetryViewManager {

	private static final int DEFAULT_NO_VIEW_ID = 0;
	private static int DEFAULT_LOAD_VIEW_ID = DEFAULT_NO_VIEW_ID;
	private static int DEFAULT_ERROR_RETRY_VIEW_ID = DEFAULT_NO_VIEW_ID;
	private static int DEFAULT_EMPTY_VIEW_ID = DEFAULT_NO_VIEW_ID;

	/**
	 * 设置默认全局加载视图资源ID
	 */
	public static void setDefaultLoadViewId(int defaultLoadViewId) {
		if (defaultLoadViewId < 0) {
			return;
		}
		DEFAULT_LOAD_VIEW_ID = defaultLoadViewId;
	}

	/**
	 * 设置默认全局访问出错重试视图资源ID
	 */
	public static void setDefaultErrorRetryViewId(int defaultErrorRetryViewId) {
		if (defaultErrorRetryViewId < 0) {
			return;
		}
		DEFAULT_ERROR_RETRY_VIEW_ID = defaultErrorRetryViewId;
	}

	/**
	 * 设置默认全局空视图资源ID
	 */
	public static void setDefaultEmptyViewId(int defaultEmptyViewId) {
		if (defaultEmptyViewId < 0) {
			return;
		}
		DEFAULT_EMPTY_VIEW_ID = defaultEmptyViewId;
	}

	/**
	 * 不同视图状态
	 */
	private static final int TYPE_DEFAULT = -1;
	private static final int TYPE_CONTENT = 0;
	private static final int TYPE_LOAD = 1;
	private static final int TYPE_ERROR_RETRY = 2;
	private static final int TYPE_EMPTY = 3;

	private FrameLayout mContainer;
	private Context mContext;

	private View mContentView;
	private View mLoadingView;
	private View mErrorRetryView;
	private View mEmptyView;
	private OnRetryListener mOnRetryListener;

	private Handler mHandler;
	private int mLastType = TYPE_DEFAULT;


	public static LoadAndRetryViewManager generate(Context context) {
		return new LoadAndRetryViewManager(context);
	}

	public static LoadAndRetryViewManager generate(Context context, @LayoutRes int id) {
		return new LoadAndRetryViewManager(context, id);
	}

	public LoadAndRetryViewManager(Context context, @LayoutRes int id) {
		this(context);
		setContentView(id);
	}

	public LoadAndRetryViewManager(Context context) {
		if (context == null) {
			return;
		}
		mContext = context;
		mContainer = new FrameLayout(context);
		mContainer.setLayoutParams(new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		setContentView(DEFAULT_NO_VIEW_ID);
		setErrorRetryView(DEFAULT_ERROR_RETRY_VIEW_ID);
		setEmptyView(DEFAULT_EMPTY_VIEW_ID);
		setLoadView(DEFAULT_LOAD_VIEW_ID);
	}

	public View getContainer() {
		return mContainer;
	}

	public View getContentView() {
		return mContentView;
	}

	public OnRetryListener getOnRetryListener() {
		return mOnRetryListener;
	}

	public void setOnRetryListener(OnRetryListener onRetryListener) {
		mOnRetryListener = onRetryListener;
	}

	public void showLoading() {
		showThread(TYPE_LOAD);
	}

	public void showErrorRetry() {
		showThread(TYPE_ERROR_RETRY);
	}

	public void showEmpty() {
		showThread(TYPE_EMPTY);
	}

	public void showContent() {
		showThread(TYPE_CONTENT);
	}

	public void showLast() {
		showThread(mLastType);
	}

	public void setContentView(@LayoutRes int id) {
		if (id != DEFAULT_NO_VIEW_ID) {
			mContentView = LayoutInflater.from(mContext).inflate(id, mContainer, false);
		}
	}

	public void setContentView(View v) {
		mContentView = v;
	}

	public void setEmptyView(@LayoutRes int id) {
		if (id != DEFAULT_NO_VIEW_ID) {
			mEmptyView = LayoutInflater.from(mContext).inflate(id, mContainer, false);
		}
	}

	public void setLoadView(@LayoutRes int id) {
		if (id != DEFAULT_NO_VIEW_ID) {
			mLoadingView = LayoutInflater.from(mContext).inflate(id, mContainer, false);
		}
	}

	public void setErrorRetryView(@LayoutRes int id) {
		if (id != DEFAULT_NO_VIEW_ID) {
			mErrorRetryView = LayoutInflater.from(mContext).inflate(id, mContainer, false);
		}
	}

	public void setEmptyView(View v) {
		mEmptyView = v;
	}

	public void setErrorRetryView(View v) {
		mErrorRetryView = v;
	}

	public void setLoadView(View v) {
		mLoadingView = v;
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	private void showThread(final int type) {
		if (isMainThread())
		{
			show(type);
		} else
		{
			Runnable r = new Runnable() {
				@Override
				public void run() {
					show(type);
				}
			};
			boolean b = handleRun(r);
			while (!b) {
				b = handleRun(r);
			}
		}
	}

	private boolean handleRun(Runnable r) {
		boolean b;
		if (mHandler != null) {
			b = mHandler.post(r);
		} else {
			b = mContainer.post(r);
		}
		return b;
	}

	private boolean isMainThread()
	{
		return Looper.myLooper() == Looper.getMainLooper();
	}

	private void show(int type) {
		if (type == mLastType) {
			return;
		}
		mContainer.removeAllViews();
		switch (type) {
			case TYPE_LOAD:
				if (mLoadingView != null) {
					mContainer.addView(mLoadingView);
					mLastType = TYPE_LOAD;
				}
				break;
			case TYPE_CONTENT:
				if (mContentView != null) {
					mContainer.addView(mContentView);
					mLastType =TYPE_CONTENT;
				}
				break;
			case TYPE_EMPTY:
				if (mEmptyView != null) {
					mContainer.addView(mEmptyView);
					mLastType = TYPE_EMPTY;
				}
				break;
			case TYPE_ERROR_RETRY:
				if (mErrorRetryView != null) {
					mContainer.addView(mErrorRetryView);
					mLastType = TYPE_ERROR_RETRY;
					if (mOnRetryListener != null) {
						mOnRetryListener.onRetry(mErrorRetryView);
					}
				}
				break;
		}
	}

    /**
     * 进行重试回调接口
     */
    public interface OnRetryListener {
        void onRetry(View retryView);
    }
}
