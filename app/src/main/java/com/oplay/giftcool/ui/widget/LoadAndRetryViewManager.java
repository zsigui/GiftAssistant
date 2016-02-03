package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/2
 */
public class LoadAndRetryViewManager {

    public static final int DEFAULT_NO_VIEW_ID = 0;
    public static int DEFAULT_LOAD_VIEW_ID = DEFAULT_NO_VIEW_ID;
    public static int DEFAULT_ERROR_RETRY_VIEW_ID = DEFAULT_NO_VIEW_ID;
    public static int DEFAULT_EMPTY_VIEW_ID = DEFAULT_NO_VIEW_ID;

    private static OnRetryListener DEFAULT_LISTENER = new OnRetryListener() {
        @Override
        public void onRetry(View retryView) {

        }
    };
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
	private int mContentViewId;
	private int mLoadingViewId;
	private int mErrorRetryViewId;
	private int mEmptyViewId;
	private int mLastType = -1;
	private Handler mHandler;

    private OnRetryListener mOnRetryListener;


    public static LoadAndRetryViewManager generate(Context context, @LayoutRes int id) {
        return new LoadAndRetryViewManager(context, id);
    }

    public LoadAndRetryViewManager(Context context, @LayoutRes int id) {
	    if (context == null) {
		    return;
	    }
	    mContext = context;
	    mContainer = (FrameLayout) getContainer();

        setContentViewId(id);
	    setErrorRetryViewId(DEFAULT_ERROR_RETRY_VIEW_ID);
	    setEmptyViewId(DEFAULT_EMPTY_VIEW_ID);
	    setLoadingViewId(DEFAULT_LOAD_VIEW_ID);
    }

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	public View getContainer() {
	    if (mContainer == null) {
		    mContainer = new FrameLayout(mContext);
		    mContainer.setLayoutParams(new FrameLayout.LayoutParams(
				    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    }
        return mContainer;
    }

    public View getContentView() {
	    if (mContentView == null && mContentViewId != DEFAULT_NO_VIEW_ID) {
		    mContentView = LayoutInflater.from(mContext).inflate(mContentViewId, mContainer, false);
		    mContainer.addView(mContentView);
	    }
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

	public void setContentViewId(int contentViewId) {
		mContentViewId = contentViewId;
	}

	public void setLoadingViewId(int loadingViewId) {
		mLoadingViewId = loadingViewId;
	}

	public void setErrorRetryViewId(int errorRetryViewId) {
		mErrorRetryViewId = errorRetryViewId;
	}

	public void setEmptyViewId(int emptyViewId) {
		mEmptyViewId = emptyViewId;
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

            boolean b;
	        if (mHandler == null) {
		        b = mContainer.post(r);
	        } else {
		        b = mHandler.postAtFrontOfQueue(r);
	        }
	        if (AppDebugConfig.IS_DEBUG) {
		        if (!b) {
			        KLog.d(AppDebugConfig.TAG_UTIL, "showThread is failed!");
		        }
	        }
	        if (!b) {
		        ThreadUtil.runInUIThread(r);
	        }
        }
    }

	private void setViewVisibility(View v, int visibility) {
		if (v != null) {
			v.setVisibility(visibility);
		}
	}

    private boolean isMainThread()
    {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void show(int type) {
		if (mLastType == type) {
			return;
		}
        switch (type) {
            case TYPE_LOAD:
                if (mLoadingView == null) {
	                if (mLoadingViewId == DEFAULT_NO_VIEW_ID) {
		                ToastUtil.showShort("bad id of mLoadingViewId = " + mLoadingViewId);
		                if (AppDebugConfig.IS_DEBUG) {
			                KLog.d(AppDebugConfig.TAG_APP, "错误的加载页面");
		                }
		                return;
	                }
	                mLoadingView = LayoutInflater.from(mContext).inflate(mLoadingViewId, mContainer, false);
	                mContainer.addView(mLoadingView);
                }
	            setViewVisibility(mLoadingView, View.VISIBLE);
	            setViewVisibility(mContentView, View.GONE);
	            setViewVisibility(mEmptyView, View.GONE);
	            setViewVisibility(mErrorRetryView, View.GONE);
                break;
            case TYPE_CONTENT:
	            if (mContentView == null) {
		            if (mContentViewId == DEFAULT_NO_VIEW_ID) {
			            ToastUtil.showShort("bad id of mContentViewId = " + mContentViewId);
			            if (AppDebugConfig.IS_DEBUG) {
				            KLog.d(AppDebugConfig.TAG_APP, "错误的内容页面");
			            }
			            return;
		            }
		            mContentView = LayoutInflater.from(mContext).inflate(mContentViewId, mContainer, false);
		            mContainer.addView(mContentView);
	            }
	            setViewVisibility(mLoadingView, View.GONE);
	            setViewVisibility(mContentView, View.VISIBLE);
	            setViewVisibility(mEmptyView, View.GONE);
	            setViewVisibility(mErrorRetryView, View.GONE);
                break;
            case TYPE_EMPTY:
	            if (mEmptyView == null) {
		            if (mEmptyViewId == DEFAULT_NO_VIEW_ID) {
			            ToastUtil.showShort("bad id of mContentViewId = " + mEmptyViewId);
			            if (AppDebugConfig.IS_DEBUG) {
				            KLog.d(AppDebugConfig.TAG_APP, "错误的内容页面");
			            }
			            return;
		            }
		            mEmptyView = LayoutInflater.from(mContext).inflate(mEmptyViewId, mContainer, false);
		            mContainer.addView(mEmptyView);
	            }
	            setViewVisibility(mLoadingView, View.GONE);
	            setViewVisibility(mContentView, View.GONE);
	            setViewVisibility(mEmptyView, View.VISIBLE);
	            setViewVisibility(mErrorRetryView, View.GONE);
                break;
            case TYPE_ERROR_RETRY:
	            if (mErrorRetryView == null) {
		            if (mErrorRetryViewId == DEFAULT_NO_VIEW_ID) {
			            ToastUtil.showShort("bad id of mContentViewId = " + mErrorRetryViewId);
			            if (AppDebugConfig.IS_DEBUG) {
				            KLog.d(AppDebugConfig.TAG_APP, "错误的内容页面");
			            }
			            return;
		            }
		            mErrorRetryView = LayoutInflater.from(mContext).inflate(mErrorRetryViewId, mContainer, false);
		            mContainer.addView(mErrorRetryView);
	            }
	            setViewVisibility(mLoadingView, View.GONE);
	            setViewVisibility(mContentView, View.GONE);
	            setViewVisibility(mEmptyView, View.GONE);
	            setViewVisibility(mErrorRetryView, View.VISIBLE);
	            if (mOnRetryListener != null) {
		            mOnRetryListener.onRetry(mErrorRetryView);
	            }
	            break;
        }
	    mLastType = type;
    }

    /**
     * 进行重试回调接口
     */
    public interface OnRetryListener {
        void onRetry(View retryView);
    }
}
