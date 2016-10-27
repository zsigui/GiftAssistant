package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.widget.ListView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MyCouponListAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.MyCouponDetail;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的首充券‘已抢’‘已过期’页面
 * <p/>
 * Created by zsigui on 16-5-31.
 */
public class MyCouponListFragment extends BaseFragment_Refresh<MyCouponDetail> {

    private ListView mDataView;
    private MyCouponListAdapter mAdapter;
    private JsonReqBase<ReqPageData> mReqPageObj;
    private int mType;

    public static MyCouponListFragment newInstance(int type) {
        MyCouponListFragment fragment = new MyCouponListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KeyConfig.KEY_DATA, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_lv_container_with_white_bg);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        ReqPageData data = new ReqPageData();
        data.giftType = KeyConfig.GIFT_TYPE_COUPON;
        mReqPageObj = new JsonReqBase<>(data);

        if (getArguments() != null) {
            mType = getArguments().getInt(KeyConfig.KEY_DATA);
        }
        mAdapter = new MyCouponListAdapter(getContext(), null, getChildFragmentManager());
        mDataView.setAdapter(mAdapter);
    }

    /**
     * 刷新礼包列表信息的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    refreshFailEnd();
                    return;
                }
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }
                mReqPageObj.data.page = 1;
                mReqPageObj.data.type = mType;
                mReqPageObj.data.giftType = KeyConfig.GIFT_TYPE_COUPON;
                mCallRefresh = Global.getNetEngine().obtainMyCouponList(mReqPageObj);
                mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<MyCouponDetail>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> call,
                                           Response<JsonRespBase<OneTypeDataList<MyCouponDetail>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            refreshSuccessEnd();
                            OneTypeDataList<MyCouponDetail> backObj = response.body().getData();
                            refreshLoadState(backObj.data, backObj.isEndPage);
                            updateData(backObj.data);
                            return;
                        }
                        if (response != null) {
                            AccountManager.getInstance().judgeIsSessionFailed(response.body());
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                        refreshFailEnd();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                        refreshFailEnd();
                    }
                });
            }
        });
    }

    @Override
    protected void refreshLoadState(Object data, boolean isEndPage) {
        if (data != null && data instanceof List) {
            mRefreshLayout.setCanShowLoad(((List) data).size() > 4);
            mNoMoreLoad = isEndPage || ((List) data).size() < 10;
        } else {
            mNoMoreLoad = isEndPage || data == null;
        }
    }

    /**
     * 加载更多礼包列表网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> mCallLoad;

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (mNoMoreLoad || mIsLoadMore) {
            return;
        }
        mIsLoadMore = true;
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    moreLoadFailEnd();
                    return;
                }
                if (mCallLoad != null) {
                    mCallLoad.cancel();
                }
                mReqPageObj.data.page = mLastPage + 1;
                mReqPageObj.data.type = mType;
                mReqPageObj.data.giftType = KeyConfig.GIFT_TYPE_COUPON;
                mCallLoad = Global.getNetEngine().obtainMyCouponList(mReqPageObj);
                mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<MyCouponDetail>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> call,
                                           Response<JsonRespBase<OneTypeDataList<MyCouponDetail>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<MyCouponDetail> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                        moreLoadFailEnd();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<MyCouponDetail>>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        moreLoadFailEnd();
                    }
                });
            }
        });

    }

    private int mCurY;

    public void updateData(ArrayList<MyCouponDetail> data) {
        if (data == null || data.size() == 0) {
            mViewManager.showEmpty();
            return;
        }
        mViewManager.showContent();
        mHasData = true;
        mData = data;
        mCurY = mDataView.getScrollY();
        mAdapter.updateData(mData);
        mDataView.smoothScrollBy(mCurY, 0);
        mLastPage = 1;
    }

    private void addMoreData(ArrayList<MyCouponDetail> moreData) {
        if (moreData == null) {
            return;
        }
        mData.addAll(moreData);
        mCurY = mDataView.getScrollY();
        mAdapter.updateData(mData);
        mDataView.smoothScrollBy(mCurY, 0);
        mLastPage += 1;
    }

    @Override
    public String getPageName() {
        return null;
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
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }
        if (mDataView != null) {
            mDataView.setAdapter(null);
            mDataView = null;
        }
    }
}
