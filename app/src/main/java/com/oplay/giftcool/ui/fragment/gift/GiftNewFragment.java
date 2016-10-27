package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftListAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-10-27.
 */
public class GiftNewFragment extends BaseFragment_Refresh<IndexGiftNew> {


    private ListView mDataView;
    private GiftListAdapter mAdapter;
    private JsonReqBase<ReqPageData> mReqPageObj;

    public static GiftNewFragment newInstance() {
        return new GiftNewFragment();
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_lv_container);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        mReqPageObj = new JsonReqBase<>(new ReqPageData());

        mLastPage = 1;
        mAdapter = new GiftListAdapter(getContext(), null);
        mDataView.setAdapter(mAdapter);

    }

    /**
     * 刷新新鲜出炉数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        if (!NetworkUtil.isConnected(getContext())) {
            refreshFailEnd();
            return;
        }
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
        }
        mReqPageObj.data.page = 1;
        mCallRefresh = Global.getNetEngine().obtainGiftNewByPage(mReqPageObj);
        mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
            @Override
            public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
                                   Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        refreshSuccessEnd();
                        updateData(response.body().getData().data);
                        return;
                    }
                }
                refreshFailEnd();
            }

            @Override
            public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                refreshFailEnd();
            }
        });
    }

    /**
     * 加载更多新鲜出炉礼包数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallLoad;

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            if (!NetworkUtil.isConnected(getContext())) {
                moreLoadFailEnd();
                return;
            }
            if (mCallLoad != null) {
                mCallLoad.cancel();
            }
            mReqPageObj.data.page = mLastPage + 1;
            mCallLoad = Global.getNetEngine().obtainGiftNewByPage(mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
                @Override
                public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
                                       Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null && response.body().isSuccess()) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                    }
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    moreLoadFailEnd();
                }

            });
        }
    }

    public void updateData(ArrayList<IndexGiftNew> data) {
        if (data.size() == 0) {
            mViewManager.showEmpty();
        } else {
            mViewManager.showContent();
        }
        mHasData = true;
        mData = data;
        mAdapter.updateData(mData);
        mLastPage = 1;
    }

    private void addMoreData(ArrayList<IndexGiftNew> moreData) {
        if (moreData == null) {
            return;
        }
        mData.addAll(moreData);
        mAdapter.updateData(mData);
        mLastPage += 1;
    }

    @Override
    public String getPageName() {
        return "新鲜出炉礼包列表";
    }

    @Override
    public void release() {
        super.release();
        if (mCallLoad != null) {
            mCallLoad.cancel();
            mCallLoad = null;
        }
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
            mCallRefresh = null;
        }
    }
}
