package com.oplay.giftcool.ui.fragment.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.listener.OnLoadListener;
import com.oplay.giftcool.ui.widget.layout.RefreshLayout;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public abstract class BaseFragment_Refresh<DataType> extends BaseFragment implements SwipeRefreshLayout
        .OnRefreshListener, OnLoadListener {

    public static final int PAGE_FIRST = 1;

    protected ArrayList<DataType> mData;
    protected RefreshLayout mRefreshLayout;
    protected boolean mIsLoadMore = false;
    protected boolean mNoMoreLoad = false;
    protected boolean mNeedHideToast = false;
    protected int mLastPage = PAGE_FIRST;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRefreshLayout = getViewById(R.id.srl_layout);
        if (mRefreshLayout != null) {
            mRefreshLayout.setOnLoadListener(this);
            mRefreshLayout.setOnRefreshListener(this);
        }
        return mContentView;
    }

    @Override
    public void onRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(false);
        }
        if (mIsSwipeRefresh || mIsLoading) {
            return;
        }
        mIsSwipeRefresh = true;
        lazyLoad();
    }

    protected void refreshInitConfig() {
        super.refreshInitConfig();
    }

    protected void refreshFailEnd() {
        if (mIsSwipeRefresh) {
            if (NetworkUtil.isConnected(getContext())) {
                ToastUtil.showShort(ConstString.TOAST_SERVER_BAD_CALLBACK);
            } else {
                ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            }
        }
        super.refreshFailEnd();
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
            mRefreshLayout.setRefreshing(false);
        }
    }

    protected void refreshCacheFailEnd() {
        if (mIsSwipeRefresh && !mNeedHideToast) {
            if (NetworkUtil.isConnected(getContext())) {
                ToastUtil.showShort(ConstString.TOAST_SERVER_BAD_CALLBACK);
            } else {
                ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            }
            mNeedHideToast = false;
        }
        mIsLoading = mIsSwipeRefresh = mIsNotifyRefresh = false;
//        if (mViewManager != null && !mViewManager.isShowContent()) {
//            mViewManager.showContent();
//        }
        if (mHasData) {
            mViewManager.showContent();
        } else {
            mViewManager.showErrorRetry();
        }
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
            mRefreshLayout.setRefreshing(false);
        }
    }

    protected void refreshSuccessEnd() {
        super.refreshSuccessEnd();
        mLastPage = PAGE_FIRST;
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(false);
            mRefreshLayout.setEnabled(true);
            mRefreshLayout.setCanShowLoad(true);
        }
        mNoMoreLoad = false;
    }

    protected void refreshLoadState(Object data, boolean isEndPage) {
        if (mRefreshLayout != null) {
            if (data != null && data instanceof List) {
                mRefreshLayout.setCanShowLoad(!isEndPage);
                mNoMoreLoad = isEndPage;
            } else {
                mNoMoreLoad = isEndPage || data == null;
                mRefreshLayout.setCanShowLoad(true);
            }
            mRefreshLayout.setRefreshing(false);
        }
    }

    protected void setLoadState(Object data, boolean isEndPage) {
        mNoMoreLoad = isEndPage || data == null;
    }

    @Override
    protected void onUserVisible() {
        super.onUserVisible();
    }

    @Override
    protected void onUserInvisible() {
        super.onUserInvisible();
    }

    protected void moreLoadSuccessEnd() {
        mIsLoadMore = false;
        if (mRefreshLayout != null) mRefreshLayout.setLoading(false);
    }

    protected void moreLoadFailEnd() {
        mIsLoadMore = false;
        if (mRefreshLayout != null) mRefreshLayout.setLoading(false);
        if (NetworkUtil.isConnected(getContext())) {
            ToastUtil.showShort(ConstString.TOAST_SERVER_BAD_CALLBACK);
        } else {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
        }
    }

    @Override
    public void onLoad() {
        if (mNoMoreLoad) {
            // return true 显示正在加载更多，return false 不显示
//			ToastUtil.showShort("没有更多新数据");
            if (mRefreshLayout != null) {
                mRefreshLayout.setLoading(false);
                mRefreshLayout.setCanShowLoad(false);
            }
            return;
        }
        loadMoreData();
    }

    @Override
    public void release() {
        super.release();
        mRefreshLayout = null;
    }

    protected void loadMoreData() {
    }
}
