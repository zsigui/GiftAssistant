package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftLikeListAdapter;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.req.ReqGiftLike;
import com.oplay.giftcool.model.data.resp.GiftLikeList;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 15-12-30.
 */
public class GiftLikeFragment extends BaseFragment_Refresh<IndexGiftLike> {

    private final static String PAGE_NAME = "猜你喜欢";

    private ListView mDataView;
    private GiftLikeListAdapter mAdapter;
    private JsonReqBase<ReqGiftLike> mReqPageObj;

    public static GiftLikeFragment newInstance() {
        return new GiftLikeFragment();
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
        ReqGiftLike data = new ReqGiftLike();
        data.appNames = Global.getInstalledAppNames();
        data.packageName = Global.getInstalledPackageNames();
        mReqPageObj = new JsonReqBase<>(data);

        mLastPage = 1;

        mReqPageObj.data.appNames = Global.getInstalledAppNames();
        mReqPageObj.data.packageName = Global.getInstalledPackageNames();
        mAdapter = new GiftLikeListAdapter(getContext(), null);
        mDataView.setAdapter(mAdapter);

    }

    /**
     * 刷新猜你喜欢数据的网络请求声明
     */
    private Call<JsonRespBase<GiftLikeList>> mCallRefresh;

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
        mCallRefresh = Global.getNetEngine().obtainGiftLike(mReqPageObj);
        mCallRefresh.enqueue(new Callback<JsonRespBase<GiftLikeList>>() {
            @Override
            public void onResponse(Call<JsonRespBase<GiftLikeList>> call,
                                   Response<JsonRespBase<GiftLikeList>> response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        refreshSuccessEnd();
                        GiftLikeList backObj = response.body().getData();
                        refreshLoadState(backObj.data, backObj.isEndPage);
                        Global.setInstalledAppNames(backObj.appNames);
                        Global.setInstalledPackageNames(backObj.packageNames);
                        mReqPageObj.data.appNames = backObj.appNames;
                        mReqPageObj.data.packageName = backObj.packageNames;
                        updateData(backObj.data);
                        FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_ALL_LIKE,
                                backObj.data, true);
                        return;
                    }
                }
                refreshFailEnd();
            }

            @Override
            public void onFailure(Call<JsonRespBase<GiftLikeList>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                refreshFailEnd();
            }
        });
    }

    /**
     * 加载更多猜你喜欢数据的网络请求声明
     */
    private Call<JsonRespBase<GiftLikeList>> mCallLoad;

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
            mCallLoad = Global.getNetEngine().obtainGiftLike(mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespBase<GiftLikeList>>() {
                @Override
                public void onResponse(Call<JsonRespBase<GiftLikeList>> call,
                                       Response<JsonRespBase<GiftLikeList>> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null && response.body().isSuccess()) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<IndexGiftLike> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                    }
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<GiftLikeList>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    moreLoadFailEnd();
                }

            });
        }
    }

    public void updateData(ArrayList<IndexGiftLike> data) {
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

    private void addMoreData(ArrayList<IndexGiftLike> moreData) {
        if (moreData == null) {
            return;
        }
        mData.addAll(moreData);
        mAdapter.updateData(mData);
        mLastPage += 1;
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
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
