package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.FreeAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.LimitGiftListData;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.json.JsonRespLimitGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersListView;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-5-23.
 */
public class GiftFreeFragment extends BaseFragment_Refresh<TimeData<IndexGiftNew>> {

    final int PAGE_SIZE = 20;

    private StickyListHeadersListView mDataView;

    private FreeAdapter mAdapter;
    private int mPageSize = PAGE_SIZE;

    public static GiftFreeFragment newInstance() {
        return new GiftFreeFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_gift_limit_lv_container);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addGiftUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mAdapter = new FreeAdapter(getActivity(), mData);
        mDataView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            Serializable s = savedInstanceState.getSerializable(KeyConfig.KEY_DATA);
            if (s != null) {
                mHasData = true;
                refreshData((ArrayList<TimeData<IndexGiftNew>>) s);
                mLastPage = savedInstanceState.getInt(KeyConfig.KEY_DATA_O);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KeyConfig.KEY_DATA, mData);
        outState.putInt(KeyConfig.KEY_DATA_O, mLastPage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeGiftUpdateListener(this);
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();
        if (mData == null) {
            readCacheData();
        }
        Global.THREAD_POOL.execute(new LoadDataByPageRunnable(1, mPageSize));
    }

    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GIFT_GET_FREE_BY_PAGE,
                new CallbackListener<ArrayList<TimeData<IndexGiftNew>>>() {

                    @Override
                    public void doCallBack(ArrayList<TimeData<IndexGiftNew>> data) {
                        if (mData == null) {
                            if (data != null) {
                                // 获取数据成功
                                refreshSuccessEnd();
                                refreshData(data);
                                mLastPage = PAGE_FIRST;
                            } else {
                                refreshFailEnd();
                            }
                        } else {
                            refreshCacheFailEnd();
                        }
                    }
                }, new TypeToken<ArrayList<TimeData<IndexGiftNew>>>() {}.getType());
    }

    @Override
    public void onGiftUpdate(int action) {
        if (action != ObserverManager.STATUS.GIFT_UPDATE_PART
                && action != ObserverManager.STATUS.GIFT_UPDATE_ALL) {
            return;
        }
        switch (action) {
            case ObserverManager.STATUS.GIFT_UPDATE_ALL:
                if (mIsSwipeRefresh) {
                    return;
                }
                mIsSwipeRefresh = true;
                lazyLoad();
                break;
        }
    }


    @Override
    public void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            Global.THREAD_POOL.execute(new LoadDataByPageRunnable(++mLastPage, mPageSize));
        }
    }

    @Override
    protected void moreLoadSuccessEnd() {
        super.moreLoadSuccessEnd();
    }

    @Override
    protected void moreLoadFailEnd() {
        super.moreLoadFailEnd();
    }

    //刷新重置页面
    public void refreshData(ArrayList<TimeData<IndexGiftNew>> data) {
        AppDebugConfig.w(AppDebugConfig.TAG_WARN, "show data = " + data);
        if (data == null) {
            if (mData == null) {
                mViewManager.showErrorRetry();
            } else {
                mAdapter.updateData(mData);
                mViewManager.showContent();
            }
            return;
        }
        mData = data;
        if (data.size() == 0) {
            mViewManager.showEmpty();
        }
        mHasData = true;
        mAdapter.updateData(mData);
        mViewManager.showContent();
    }

    //加载更多数据后更新
    public void addMoreData(ArrayList<TimeData<IndexGiftNew>> data) {
        mHasData = data != null && data.size() >= mPageSize;
        mAdapter.addMoreData(data);
    }
    
    /**
     * 加载指定页礼包
     */
    private class LoadDataByPageRunnable implements Runnable {
        private JsonReqBase<ReqPageData> mReqPageObj;
        private Call<JsonRespLimitGiftList> mCallLoad;

        /**
         * @param page     指定加载页数
         * @param pageSize 指定每页大小
         */
        public LoadDataByPageRunnable(int page, int pageSize) {
            mReqPageObj = new JsonReqBase<ReqPageData>(new ReqPageData());
            mReqPageObj.data.page = page;
            mReqPageObj.data.pageSize = pageSize;
        }

        @Override
        public void run() {
            if (!mCanShowUI) {
                return;
            }
            if (!NetworkUtil.isConnected(getContext())) {
                if (mReqPageObj.data.page == 1) {
                    readCacheData();
                } else {
                    moreLoadFailEnd();
                }
                return;
            }
            if (mCallLoad != null) {
                mCallLoad.cancel();
            }
            mCallLoad = Global.getNetEngine().obtainGiftFreeByPage(mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespLimitGiftList>() {
                @Override
                public void onResponse(Call<JsonRespLimitGiftList> call, Response<JsonRespLimitGiftList> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()
                            && response.body() != null && response.body().isSuccess()) {
                        LimitGiftListData<TimeData<IndexGiftNew>> data = response.body().getData();
                        if (data.page == 1) {
                            //初始化成功
                            refreshSuccessEnd();
                            mData = data.data;
                            refreshLoadState(mData, false);//是否最后一页
                            mLastPage = 1;
                            refreshData(mData);
                            FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_FREE_BY_PAGE, mData);
                        } else {
                            //加载更多成功
                            setLoadState(data.data, data.isEndPage);
                            addMoreData(data.data);
                            moreLoadSuccessEnd();
                        }
                        return;
                    }
					if (mReqPageObj.data.page == 1) {
						//刷新失败
                        readCacheData();
					} else {
						//加载更多失败
						moreLoadFailEnd();
					}
                }

                @Override
                public void onFailure(Call<JsonRespLimitGiftList> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
					if (mReqPageObj.data.page == 1) {
						//刷新失败
                        readCacheData();
					} else {
						//加载更多失败
						moreLoadFailEnd();
					}
                }
            });
        }
    }

    @Override
    public String getPageName() {
        return "限时免费";
    }

}
