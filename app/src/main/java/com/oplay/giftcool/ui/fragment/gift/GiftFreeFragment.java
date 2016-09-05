package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.FreeAdapter;
import com.oplay.giftcool.config.Global;
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
import com.oplay.giftcool.util.NetworkUtil;

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
        if (data == null) {
            return;
        }
        if (data.size() == 0) {
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
                    refreshFailEnd();
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

    @Override
    public String getPageName() {
        return "限时免费";
    }

//    public ArrayList<TimeData<IndexGiftNew>> initTestData() {
//        ArrayList<TimeData<IndexGiftNew>> result = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_COUPON;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_UN_RESERVE;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试首充的今天显示";
//            o.gameName = "今天测试" + i * 10;
//            o.freeStartTime = (int) System.currentTimeMillis() - 3600;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 3; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_WAIT_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_NEVER;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试礼包明天免费抢";
//            o.gameName = "暴打味书屋";
//            o.name = "至尊礼包";
//            o.freeStartTime = System.currentTimeMillis() + 3600 * 24 * 1000;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 3; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_NEVER;
//            o.remainCount = 5;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试礼包明天免费抢";
//            o.gameName = "暴打味书屋";
//            o.name = "至尊礼包";
//            o.freeStartTime = System.currentTimeMillis() + 3600 * 24 * 1000;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 3; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEIZED;
//            o.remainCount = 3;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试礼包免费抢过程已抢号";
//            o.gameName = "暴打味书屋";
//            o.name = "至尊礼包";
//            o.freeStartTime = System.currentTimeMillis() - 3600;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 3; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_NEVER;
//            o.remainCount = 0;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试礼包免费已抢完,进行普通抢号";
//            o.gameName = "暴打味书屋";
//            o.name = "至尊礼包";
//            o.freeStartTime = System.currentTimeMillis() - 3600;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 5; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = (int)(Math.random() * 2) + 7;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_COUPON;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_RESERVED;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试首充的今天显示";
//            o.gameName = "今天测试" + i * 10;
//            o.freeStartTime =  System.currentTimeMillis() + 60 *3600;
//            o.id = 135;
//            o.reserveDeadline = "12:00";
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 5; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = (int) (Math.random() * 2) + 2;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_COUPON;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_SEIZED;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试首充的今天显示";
//            o.gameName = "今天测试" + i * 10;
//            o.freeStartTime = System.currentTimeMillis() - 3600;
//            o.id = 135;
//            o.reserveDeadline = "12:00";
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 5; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            data.date = "2016-05-27";
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_SEIZE;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_GIFT_LIMIT;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_NEVER;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.platform = "偶玩版";
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "元宝*188，真气*5000，橙色神秘碎片*5，高级丹药包*20，三级宝石袋*5";
//            o.gameName = "功夫少林" + i;
//            o.name = "独家至尊礼包";
//            o.freeStartTime = (int) System.currentTimeMillis() - 3600;
//            o.id = 135;
//            o.img = "http://owan-img.ymapp.com/app/10792/icon/icon_1451902693.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 10; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            int time = (int) (Math.random() * 3) + 28;
//            data.date = String.format("2016-05-%d", time);
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = (int) (Math.random() * 3) + 7;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_COUPON;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_UN_RESERVE;
//            o.remainCount = 10;
//            o.totalCount = 10;
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试首充的明天显示";
//            o.gameName = "明天测试" + i;
//            o.platform = "偶玩版";
//            o.freeStartTime = System.currentTimeMillis() + (time - 27) * 3600 * 24 * 1000;
//            o.id = 136;
//            o.img = "http://owan-img.ymapp.com/app/31/b5/10976/icon/icon_1442199434.png_128_128_70.png";
//            result.add(data);
//        }
//        for (int i = 0; i < 10; i++) {
//            TimeData<IndexGiftNew> data = new TimeData<>();
//            int time = (int) (Math.random() * 26) + 1;
//            data.date = String.format("2016-05-%d", time);
//            IndexGiftNew o = new IndexGiftNew();
//            data.data = o;
//            o.status = GiftTypeUtil.STATUS_FINISHED;
//            o.totalType = GiftTypeUtil.TOTAL_TYPE_COUPON;
//            o.giftType = GiftTypeUtil.GIFT_TYPE_LIMIT_FREE;
//            o.seizeStatus = GiftTypeUtil.SEIZE_TYPE_NEVER;
//            o.remainCount = 0;
//            o.totalCount = 10;
//            o.bean = (int) (Math.random() * 100);
//            o.score = 500;
//            o.content = "测试首充的昨天显示";
//            o.gameName = "昨天测试" + i;
//            o.platform = "偶玩版";
//            o.freeStartTime = System.currentTimeMillis() - (27 - time) * 3600 * 24 * 1000;
//            o.id = 136;
//            o.img = "http://owan-img.ymapp.com/app/11004/icon/icon_1460627334.png_128_128_70.png";
//            result.add(data);
//        }
//        return result;
//    }
}
