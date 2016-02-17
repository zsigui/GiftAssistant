package com.oplay.giftcool.ui.fragment.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftcool.R;
import com.oplay.giftcool.ui.widget.layout.RefreshLayout;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public abstract class BaseFragment_Refresh<DataType> extends BaseFragment implements SwipeRefreshLayout
		.OnRefreshListener, RefreshLayout.OnLoadListener {

	protected ArrayList<DataType> mData;
	protected RefreshLayout mRefreshLayout;
	protected boolean mIsLoadMore = false;
	protected boolean mNoMoreLoad = false;
	protected int mLastPage = 0;

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
		mRefreshLayout.setEnabled(false);
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
			ToastUtil.showShort("刷新请求出错");
		}
		super.refreshFailEnd();
		mRefreshLayout.setEnabled(true);
		mRefreshLayout.setRefreshing(false);
	}

	protected void refreshSuccessEnd() {
		super.refreshSuccessEnd();
		mRefreshLayout.setRefreshing(false);
		mRefreshLayout.setEnabled(true);
	}

	protected void refreshLoadState(Object data, boolean isEndPage) {
		if (data != null && data instanceof List) {
			mRefreshLayout.setCanShowLoad(((List)data).size() > 5);
			mNoMoreLoad = isEndPage || ((List)data).size() < 10;
		} else {
			mNoMoreLoad = isEndPage || data == null;
			mRefreshLayout.setCanShowLoad(true);
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
		mRefreshLayout.setLoading(false);
	}

	protected void moreLoadFailEnd() {
		mIsLoadMore = false;
		mRefreshLayout.setLoading(false);
		showToast("异常，加载失败");
	}

	@Override
	public void onLoad() {
		if (mNoMoreLoad) {
			// return true 显示正在加载更多，return false 不显示
			ToastUtil.showShort("没有更多新数据");
			mRefreshLayout.setLoading(false);
			//mRefreshLayout.setCanShowLoad(false);
			return;
		}
		loadMoreData();
	}

	public RefreshLayout getRefreshLayout() {
		return mRefreshLayout;
	}


	@Override
	public void release() {
		super.release();
		mRefreshLayout = null;
	}

	protected void loadMoreData() {
	}
}
