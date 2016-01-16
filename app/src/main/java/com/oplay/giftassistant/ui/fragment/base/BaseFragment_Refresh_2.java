package com.oplay.giftassistant.ui.fragment.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.ui.widget.RefreshLayout;
import com.oplay.giftassistant.util.ToastUtil;

import java.util.ArrayList;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public abstract class BaseFragment_Refresh_2<DataType> extends BaseFragment implements SwipeRefreshLayout
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
		if (mIsRefresh || mIsLoading) {
			return;
		}
		mIsRefresh = true;
		lazyLoad();
	}

	protected void refreshInitConfig() {
		super.refreshInitConfig();
	}

	protected void refreshFailEnd() {
		if (mIsRefresh) {
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
	protected void setLoadState(Object data, boolean isEndPage) {
		if (isEndPage || data == null) {
			// 无更多不再请求加载
			mNoMoreLoad = true;
			mRefreshLayout.setCanShowLoad(false);
		} else {
			mNoMoreLoad = false;
			mRefreshLayout.setCanShowLoad(true);
		}
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
