package com.oplay.giftcool.ui.fragment.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.NestedGiftListAdapterNew;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.PayManager;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.ui.widget.button.GiftButton;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-5-31.
 */
public class MyGiftReservedFragment extends BaseFragment_Refresh<IndexGiftNew> implements
        OnItemClickListener<IndexGiftNew> {

    private ListView mDataView;
    private NestedGiftListAdapterNew mAdapter;
    private JsonReqBase<ReqPageData> mReqPageObj;

    public static MyGiftReservedFragment newInstance() {
        return new MyGiftReservedFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_top_hint_lv);
        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_top_hint_empty,
                (ViewGroup) mContentView.getParent(), false);
        TextView tv = getViewById(emptyView, R.id.tv_hint);
        if (tv != null) {
            tv.setText(getContext().getString(R.string.st_my_coupon_reserved_msg_hint));
        }
        mViewManager.setEmptyView(emptyView);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        mAdapter.setListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        ReqPageData data = new ReqPageData();
        data.giftType = KeyConfig.GIFT_TYPE_GIFT;
        data.type = KeyConfig.TYPE_KEY_RESERVED;
        mReqPageObj = new JsonReqBase<ReqPageData>(data);
        mAdapter = new NestedGiftListAdapterNew(getContext());
        mDataView.setAdapter(mAdapter);
    }

    /**
     * 刷新礼包列表信息的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallRefresh;

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
                mReqPageObj.data.giftType = KeyConfig.GIFT_TYPE_GIFT;
                mCallRefresh = Global.getNetEngine().obtainGiftList(NetUrl.USER_GIFT_SEIZED, mReqPageObj);
                mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
                                           Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            refreshSuccessEnd();
                            OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
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
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
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
    private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallLoad;

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
                mReqPageObj.data.giftType = KeyConfig.GIFT_TYPE_GIFT;
                mCallLoad = Global.getNetEngine().obtainGiftList(NetUrl.USER_GIFT_SEIZED, mReqPageObj);
                mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGiftNew>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call,
                                           Response<JsonRespBase<OneTypeDataList<IndexGiftNew>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<IndexGiftNew> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
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
        });

    }

    private int mCurY;

    public void updateData(ArrayList<IndexGiftNew> data) {
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

    private void addMoreData(ArrayList<IndexGiftNew> moreData) {
        if (moreData == null) {
            return;
        }
        if (mData == null) {
            mData = new ArrayList<>();
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

    @Override
    public void onItemClick(IndexGiftNew item, View view, int position) {
        switch (view.getId()) {
            case R.id.rl_recommend:
                IntentUtil.jumpGiftDetail(getContext(), item.id);
                break;
            case R.id.btn_send:
                PayManager.getInstance().seizeGift(getActivity(), item, (GiftButton) view);
                break;
        }
    }
}
