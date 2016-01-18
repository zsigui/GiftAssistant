package com.oplay.giftassistant.ui.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oplay.giftassistant.R;
import com.oplay.giftassistant.util.ToastUtil;
import com.oplay.giftassistant.util.ViewUtil;

import java.util.ArrayList;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public abstract class BaseFragment_Refresh<DataType> extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate{

	protected ArrayList<DataType> mData;
    protected BGARefreshLayout mRefreshLayout;
    protected boolean mIsLoadMore = false;
    protected boolean mNoMoreLoad = false;
    protected int mLastPage = 0;

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        if (mIsSwipeRefresh || mIsLoading){
            return;
        }
        mIsSwipeRefresh = true;
        lazyLoad();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        if (mNoMoreLoad) {
            // return true 显示正在加载更多，return false 不显示
            ToastUtil.showShort("没有更多新数据");
            return false;
        }
        loadMoreData();
        return true;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mRefreshLayout = getViewById(R.id.srl_layout);
		if (mRefreshLayout != null) {
			mRefreshLayout.setDelegate(this);
			ViewUtil.initRefreshLayout(getContext(), mRefreshLayout);
		}
		return mContentView;
	}

	protected void loadMoreData() {
    }

	protected void refreshInitConfig() {
		super.refreshInitConfig();
	}

	protected void refreshFailEnd() {
		if (mIsSwipeRefresh) {
			ToastUtil.showShort("刷新请求出错");
		}
		super.refreshFailEnd();
		mRefreshLayout.endRefreshing();
	}

	protected void refreshSuccessEnd() {
		super.refreshSuccessEnd();
		mRefreshLayout.endRefreshing();
	}

	protected void setLoadState(Object data, boolean isEndPage) {
		if (isEndPage || data == null) {
			// 无更多不再请求加载
			mNoMoreLoad = true;
			mRefreshLayout.setIsShowLoadingMoreView(false);
		} else {
			mNoMoreLoad = false;
			mRefreshLayout.setIsShowLoadingMoreView(true);
		}
	}

	protected void moreLoadSuccessEnd() {
		mIsLoadMore = false;
		mRefreshLayout.endLoadingMore();
	}

	protected void moreLoadFailEnd() {
		mIsLoadMore = false;
		mRefreshLayout.endLoadingMore();
		showToast("异常，加载失败");
	}
}
