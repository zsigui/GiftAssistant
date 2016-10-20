package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

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
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.LimitGiftListData;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.json.JsonRespLimitGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersListView;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-5-23.
 */
public class GiftFreeFragment extends BaseFragment_Refresh<TimeData<IndexGiftNew>> {

    final int PAGE_SIZE = 20;

    private StickyListHeadersListView mDataView;
    private View mLoadingView;

    private FreeAdapter mAdapter;
    private int mPageSize = PAGE_SIZE;
    private UpdateGiftRunnable mUpdateGiftRunnable;

    public static GiftFreeFragment newInstance() {
        return new GiftFreeFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_gift_limit_lv_container);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mDataView = getViewById(R.id.lv_content);

        mLoadingView = inflater.inflate(R.layout.view_item_footer, mDataView, false);
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addGiftUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mAdapter = new FreeAdapter(getActivity(), mData);
        mDataView.setAdapter(mAdapter);
        mUpdateGiftRunnable = new UpdateGiftRunnable();
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
            case ObserverManager.STATUS.GIFT_UPDATE_PART:
                if (mIsSwipeRefresh || mIsNotifyRefresh || mData == null) {
                    return;
                }
                mIsNotifyRefresh = true;
                if (mUpdateGiftRunnable != null) {
                    Global.THREAD_POOL.execute(mUpdateGiftRunnable);
                }
                break;
        }
    }


    @Override
    public void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            if (mLoadingView != null) {
                mLoadingView.setVisibility(View.VISIBLE);
            }
            Global.THREAD_POOL.execute(new LoadDataByPageRunnable(++mLastPage, mPageSize));
        }
    }

    @Override
    protected void moreLoadSuccessEnd() {
        super.moreLoadSuccessEnd();
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void moreLoadFailEnd() {
        super.moreLoadFailEnd();
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
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

    private void delIndex(ArrayList<TimeData<IndexGiftNew>> data, ArrayList<Integer> waitDelIndexs) {
        for (int i = waitDelIndexs.size() - 1; i >= 0; i--) {
            data.remove(waitDelIndexs.get(i).intValue());
        }
    }

    private void updateCircle(HashMap<String, IndexGiftNew> respData, ArrayList<Integer> waitDelIndexs,
                              ArrayList<TimeData<IndexGiftNew>> timeDatas) {
        int i = 0;
        for (TimeData<IndexGiftNew> timedata : timeDatas) {
            //根据返回结果，更新原来数据中的礼包列表中的礼包信息
            IndexGiftNew gift = timedata.data;
            if (respData.get(gift.id + "") != null) {
                IndexGiftNew item = respData.get(gift.id + "");
                setGiftUpdateInfo(gift, item);
            } else {
                // 找不到，需要被移除
                waitDelIndexs.add(i);
            }
            i++;
        }
    }

    private void setGiftUpdateInfo(IndexGiftNew toBeSet, IndexGiftNew data) {
        toBeSet.status = data.status;
        toBeSet.seizeStatus = data.seizeStatus;
        toBeSet.searchCount = data.searchCount;
        toBeSet.searchTime = data.searchTime;
        toBeSet.totalCount = data.totalCount;
        toBeSet.remainCount = data.remainCount;
        toBeSet.code = data.code;
    }

    @Override
    public void release() {
        super.release();
        if (mUpdateGiftRunnable != null) {
            mUpdateGiftRunnable.clear();
            mUpdateGiftRunnable = null;
        }
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

    /**
     * 用于更新礼包的Runnable
     */
    private class UpdateGiftRunnable implements Runnable {

        private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mCallUpdate;

        @Override
        public void run() {
            if (!mCanShowUI) {
                return;
            }
            if (!NetworkUtil.isConnected(getContext())) {
                mIsNotifyRefresh = false;
                return;
            }
            HashSet<Integer> ids = new HashSet<Integer>();
            for (TimeData<IndexGiftNew> timedata : mData) {
                IndexGiftNew gift = timedata.data;
                ids.add(gift.id);
            }
            if (mCallUpdate != null) {
                mCallUpdate.cancel();
            }
            ReqRefreshGift reqData = new ReqRefreshGift();
            reqData.ids = ids;
            mCallUpdate = Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData));
            mCallUpdate.enqueue(new Callback<JsonRespBase<HashMap<String, IndexGiftNew>>>() {

                @Override
                public void onResponse(Call<JsonRespBase<HashMap<String, IndexGiftNew>>> call, Response<JsonRespBase
                        <HashMap<String, IndexGiftNew>>> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null && response.body().isSuccess()) {
                            // 数据刷新成功，进行更新
                            HashMap<String, IndexGiftNew> respData = response.body().getData();
                            ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
                            updateCircle(respData, waitDelIndexs, mData);
                            delIndex(mData, waitDelIndexs);
                            int y = mDataView.getScrollY();
                            refreshData(mData);
                            mDataView.smoothScrollBy(y, 0);
                        }
                    }
                    mIsNotifyRefresh = false;
                }

                @Override
                public void onFailure(Call<JsonRespBase<HashMap<String, IndexGiftNew>>> call, Throwable t) {
                    mIsNotifyRefresh = false;
                }
            });
        }

        public void clear() {

        }
    }

    @Override
    public String getPageName() {
        return "限时免费";
    }

}
