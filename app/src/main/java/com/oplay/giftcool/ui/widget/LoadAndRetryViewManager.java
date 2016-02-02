package com.oplay.giftcool.ui.widget;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
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
    private OnRetryListener mOnRetryListener;


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
	    if (AppDebugConfig.IS_DEBUG) {
		    KLog.d(AppDebugConfig.TAG_APP, "retry - showLoading");
	    }
        showThread(TYPE_LOAD);
    }

    public void showErrorRetry() {
	    if (AppDebugConfig.IS_DEBUG) {
		    KLog.d(AppDebugConfig.TAG_APP, "retry - showErrorRetry");
	    }
        showThread(TYPE_ERROR_RETRY);
    }

    public void showEmpty() {
	    if (AppDebugConfig.IS_DEBUG) {
		    KLog.d(AppDebugConfig.TAG_APP, "retry - showEmpty");
	    }
        showThread(TYPE_EMPTY);
    }

    public void showContent() {
	    if (AppDebugConfig.IS_DEBUG) {
		    KLog.d(AppDebugConfig.TAG_APP, "retry - showContent");
	    }
        showThread(TYPE_CONTENT);
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

    private void showThread(final int type) {
        if (isMainThread())
        {
            show(type);
        } else
        {
            mContainer.post(new Runnable() {
	            @Override
	            public void run() {
		            show(type);
	            }
            });
        }
    }

	private boolean findView() {
		if (AppDebugConfig.IS_DEBUG) {
			KLog.file(StorageUtils.getOwnCacheDirectory(mContext, Global.IMG_CACHE_PATH), "retry - show - start find");
		}
		for (int i = 0; i<mContainer.getChildCount(); i++) {
			View cV = mContainer.getChildAt(i);
			if (AppDebugConfig.IS_DEBUG) {
				/*KLog.file(StorageUtils.getOwnCacheDirectory(mContext, Global.IMG_CACHE_PATH), "retry - show - findView = v = " + cV + ", " + (cV == mContentView)
				 + ", " + (cV == mEmptyView) + ", " + (cV == mErrorRetryView) + ", " + (cV == mLoadingView));*/
				KLog.d(AppDebugConfig.TAG_APP, "retry - show - findView = v = " + cV + ", " + (cV == mContentView)
						+ ", " + (cV == mEmptyView) + ", " + (cV == mErrorRetryView) + ", " + (cV == mLoadingView));
			}
			if (cV == mContentView || cV == mEmptyView
					|| cV == mErrorRetryView || cV == mLoadingView) {
				return true;
			}
		}
		return false;
	}

    private boolean isMainThread()
    {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void show(int type) {
	    if (AppDebugConfig.IS_DEBUG) {
		  /*  KLog.file(StorageUtils.getOwnCacheDirectory(mContext, Global.IMG_CACHE_PATH), "retry - show - type = " + type
				    + ", loadView = " + mLoadingView + ", contentView = " + mContentView
				    + ", emptyView = " + mEmptyView + ", errorView = " + mErrorRetryView);*/
		    KLog.d(AppDebugConfig.TAG_APP, "retry - show - type = " + type
				    + ", loadView = " + mLoadingView + ", contentView = " + mContentView
				    + ", emptyView = " + mEmptyView + ", errorView = " + mErrorRetryView);
	    }
        mContainer.removeAllViews();
        switch (type) {
            case TYPE_LOAD:
                if (mLoadingView == null) {
	                setLoadView(DEFAULT_LOAD_VIEW_ID);
                }
                mContainer.addView(mLoadingView, 0);

                break;
            case TYPE_CONTENT:
                if (mContentView == null) {
	                setContentView(DEFAULT_NO_VIEW_ID);
                }
                mContainer.addView(mContentView, 0);
                break;
            case TYPE_EMPTY:
                if (mEmptyView == null) {
	                setEmptyView(DEFAULT_EMPTY_VIEW_ID);
                }
                mContainer.addView(mEmptyView, 0);
                break;
            default:
                if (mErrorRetryView == null) {
	                setErrorRetryView(DEFAULT_ERROR_RETRY_VIEW_ID);
                }
	            mContainer.addView(mErrorRetryView, 0);
	            if (mOnRetryListener != null) {
		            mOnRetryListener.onRetry(mErrorRetryView);
	            }
        }
	    if (AppDebugConfig.IS_DEBUG) {
		    KLog.file(StorageUtils.getOwnCacheDirectory(mContext, Global.IMG_CACHE_PATH), "retry - show - find = " + findView());
	    }
    }

    /**
     * 进行重试回调接口
     */
    public interface OnRetryListener {
        void onRetry(View retryView);
    }
}
