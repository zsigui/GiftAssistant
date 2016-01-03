package com.oplay.giftassistant.ui.fragment.base;

import com.oplay.giftassistant.util.ToastUtil;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/1/3
 */
public abstract class BaseFragment_Refresh extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate{

    protected BGARefreshLayout mRefreshLayout;
    protected boolean mIsRefresh = false;
    protected boolean mIsLoadMore = false;
    protected boolean mNoMoreLoad = false;
    protected int mLastPage = 0;

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        if (mIsRefresh || mIsLoading){
            return;
        }
        mIsRefresh = true;
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

    protected void loadMoreData() {
    }
}
