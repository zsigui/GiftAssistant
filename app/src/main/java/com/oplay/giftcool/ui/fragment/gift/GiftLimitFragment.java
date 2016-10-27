package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.LimitGiftListNewAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.LimitGiftListData;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 显示限量礼包数据的Fragment<br/>
 * 具备下拉刷新,上拉加载<br/>
 * <br/>
 * Created by mink on 16-03-04.
 */
public class GiftLimitFragment extends BaseFragment_Refresh<TimeData<IndexGiftNew>> implements
        OnItemClickListener<IndexGiftNew> {

    private int mPageSize = 20;
    private ListView mDataView;

    private LimitGiftListNewAdapter mAdapter;
    public static GiftLimitFragment newInstance() {
        return new GiftLimitFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_lv_container_with_white_bg);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addGiftUpdateListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        mAdapter = new LimitGiftListNewAdapter(getContext(), null);
        mAdapter.setListener(this);
        mDataView.setAdapter(mAdapter);
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
        }
    }


    @Override
    public void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            Global.THREAD_POOL.execute(new LoadDataByPageRunnable(++mLastPage, mPageSize));
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
    }


    /**
     * 加载指定页礼包
     */
    private class LoadDataByPageRunnable implements Runnable {
        private JsonReqBase<ReqPageData> mReqPageObj;
        private Call<JsonRespBase<LimitGiftListData<TimeData<IndexGiftNew>>>> mCallLoad;

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
            mCallLoad.enqueue(new Callback<JsonRespBase<LimitGiftListData<TimeData<IndexGiftNew>>>>() {
                @Override
                public void onResponse(Call<JsonRespBase<LimitGiftListData<TimeData<IndexGiftNew>>>> call,
                                       Response<JsonRespBase<LimitGiftListData<TimeData<IndexGiftNew>>>> response) {
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
                            setLoadState(data.data, data.isEndPage);
                            addMoreData(data.data);
                            moreLoadSuccessEnd();
                        }
                        return;
                    }
                    if (mReqPageObj.data.page == 1) {
                        //刷新失败
                        refreshFailEnd();
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                    } else {
                        //加载更多失败
                        moreLoadFailEnd();
                    }
                }

                @Override
                public void onFailure(Call<JsonRespBase<LimitGiftListData<TimeData<IndexGiftNew>>>> call, Throwable t) {
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

}
