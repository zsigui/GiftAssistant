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
			mRefreshLayout.setColorSchemeResources(R.color.co_btn_red, R.color.co_btn_orange, R.color.co_btn_blue,
					R.color.co_btn_green);
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

	// 刷新时，默认可以头一次加载可以下拉更多
	protected void refreshLoadState() {
		mIsLoadMore = true;
		mRefreshLayout.setCanShowLoad(true);
	}

	protected void setLoadState(Object data, boolean isEndPage) {
		if (isEndPage || data == null) {
			// 无更多不再请求加载
			ToastUtil.showShort("没有更多");
			mNoMoreLoad = true;
			mRefreshLayout.setCanShowLoad(false);
		} else {
			mNoMoreLoad = false;
			mRefreshLayout.setCanShowLoad(true);
		}
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
		}
		loadMoreData();
	}

	protected void loadMoreData() {
	}
}