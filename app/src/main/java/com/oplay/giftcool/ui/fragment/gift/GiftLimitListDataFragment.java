package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.LimitGiftListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.LimitGiftListData;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.json.JsonRespLimitGiftList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.ui.widget.stickylistheaders.StickyListHeadersListView;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新,上拉加载<br/>
 * <br/>
 * Created by mink on 16-03-04.
 */
public class GiftLimitListDataFragment extends BaseFragment_Refresh<TimeData<IndexGiftNew>> implements
        OnItemClickListener<IndexGiftNew> {

    private int mPageSize = 20;
    private StickyListHeadersListView mDataView;
    private View mLoadingView;

    private LimitGiftListAdapter mAdapter;

    private UpdateGiftRunnable mUpdateGiftRunnable;

    public static GiftLimitListDataFragment newInstance() {
        return new GiftLimitListDataFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_gift_limit_lv_container);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mDataView = getViewById(R.id.lv_content);

        mLoadingView = inflater.inflate(R.layout.view_item_footer, mDataView, false);
        mLoadingView.setVisibility(View.GONE);
        mDataView.addFooterView(mLoadingView);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addGiftUpdateListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        mAdapter = new LimitGiftListAdapter(getContext(), null);
        mAdapter.setListener(this);
        mDataView.setAdapter(mAdapter);
        mUpdateGiftRunnable = new UpdateGiftRunnable();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeGiftUpdateListener(this);
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();
        Global.THREAD_POOL.execute(new LoadDataByPageRunnable(1, mPageSize));
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
        if (data == null || data.size() == 0) {
            mViewManager.showEmpty();
        } else {
            mViewManager.showContent();
        }
        mHasData = true;
        mData = data;
        mAdapter.updateData(mData);
    }

    //加载更多数据后更新
    public void addMoreData(ArrayList<TimeData<IndexGiftNew>> data) {
        if (data == null) {
            return;
        }
        mHasData = data.size() >= mPageSize;
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
    public String getPageName() {
        return "礼包列表";
    }

    @Override
    public void onItemClick(IndexGiftNew gift, View v, int position) {
        switch (v.getId()) {
            case R.id.rl_recommend:
                IntentUtil.jumpGiftDetail(getContext(), gift.id);
                break;
            case R.id.btn_send:
                PayManager.getInstance().seizeGift(getActivity(), gift, (GiftButton) v);
                break;
        }
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
                    refreshFailEnd();
                } else {
                    moreLoadFailEnd();
                }
                return;
            }
            if (mCallLoad != null) {
                mCallLoad.cancel();
            }
            mCallLoad = Global.getNetEngine().obtainGiftLimitByPage(mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespLimitGiftList>() {
                @Override
                public void onResponse(Call<JsonRespLimitGiftList> call, Response<JsonRespLimitGiftList> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        LimitGiftListData<TimeData<IndexGiftNew>> data = response.body().getData();
                        if (data.page == 1) {
                            //初始化成功
                            refreshSuccessEnd();
                            mData = data.data;
                            refreshLoadState(mData, false);//是否最后一页
                            mLastPage = 1;
                            refreshData(mData);
                        } else {
                            //加载更多成功
                            setLoadState(data.data, (data.data == null || data.data.size() == 0 || data.data.size() <
                                    data.pageSize));
                            addMoreData(data.data);
                            moreLoadSuccessEnd();
                        }
                        return;
                    }
                    if (mReqPageObj.data.page == 1) {
                        //刷新失败
                        refreshFailEnd();
                        if (response != null && response.isSuccessful()) {
                            AppDebugConfig.warn(AppDebugConfig.TAG_WARN, response.body());
                            return;
                        }
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_WARN, response == null ? "返回失败" : response.code() + "-" + response.errorBody());

                        }
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
                        refreshFailEnd();
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

}
