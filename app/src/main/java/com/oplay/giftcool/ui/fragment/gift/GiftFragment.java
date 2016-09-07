package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GiftAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.BannerTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.listener.OnFinishListener;
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqGiftLike;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.req.ReqRefreshGift;
import com.oplay.giftcool.model.data.resp.GiftLikeList;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.data.resp.IndexGiftNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主页-礼包页面主要内容页
 *
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/13
 */
public class GiftFragment extends BaseFragment_Refresh implements OnItemClickListener {

    private final static String PAGE_NAME = "礼包首页";
    private static final int ID_UPDATE = 6;

    public static final int POS_BANNER = 0;
    public static final int POS_ZERO = 1;
    public static final int POS_LIKE = 2;
    public static final int POS_LIMIT = 3;
    public static final int POS_NEW = 4;

    private RecyclerView rvContainer;
    private GiftAdapter mAdapter;

    // 礼物界面数据
    private IndexGift mGiftData;
    // 请求后游戏键值的MD5串
    private String mGameKey;

    // ‘猜你喜欢’ 分离出来的数据
    private ArrayList<IndexGiftLike> mLikeData;
    private JsonReqBase<ReqIndexGift> mReqPageObj;
    // 每隔5分钟刷新一次
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mIsNotifyRefresh = true;
            lazyLoad();
            ThreadUtil.runOnUiThread(this, 5 * 60 * 1000);
        }
    };

    public static GiftFragment newInstance() {
        return new GiftFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_custome_rv_container);
        rvContainer = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        rvContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        startBanner();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        stopBanner();
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        ObserverManager.getInstance().addGiftUpdateListener(this);
    }

    private void stopBanner() {
        if (mAdapter != null) {
            mAdapter.stopBanner();
        }
    }

    private void startBanner() {
        if (mAdapter != null) {
            mAdapter.startBanner();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void processLogic(Bundle savedInstanceState) {
        SnapLinearLayoutManager llm = new SnapLinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                llm.getOrientation());
        rvContainer.setLayoutManager(llm);
        rvContainer.addItemDecoration(dividerItemDecoration);
        mAdapter = new GiftAdapter(getActivity());
        rvContainer.setAdapter(mAdapter);
        mIsPrepared = true;
        ThreadUtil.runOnUiThread(mRefreshRunnable, 5 * 60 * 1000);

        ReqIndexGift data = new ReqIndexGift();
        data.pageSize = 20;
        data.appNames = new HashSet<>();
        mReqPageObj = new JsonReqBase<ReqIndexGift>(data);
        mLastPage = PAGE_FIRST;
        readLikeCacheData();

        if (savedInstanceState != null) {
            Serializable s = savedInstanceState.getSerializable(KeyConfig.KEY_DATA);
            if (s != null) {
                mGiftData = (IndexGift) s;
                mHasData = true;
                updateData(mGiftData, 0, -1);
                mLastPage = savedInstanceState.getInt(KeyConfig.KEY_DATA_O);
            }
        }
    }

    /**
     * 刷新首页数据的网络请求声明
     */
    private Call<JsonRespBase<IndexGift>> mCallRefresh;

    public static long sCurrentTime;
    public static long sLastTime;

    @Override
    protected void lazyLoad() {

        sLastTime = System.currentTimeMillis();
        sCurrentTime = sLastTime;
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                refreshInitConfig();
                // 判断网络情况
                if (!NetworkUtil.isConnected(getContext())) {
                    readCacheData();
                    return;
                }
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }

                ReqIndexGift data = new ReqIndexGift();
                JsonReqBase<ReqIndexGift> reqData = new JsonReqBase<ReqIndexGift>(data);
                mCallRefresh = Global.getNetEngine().obtainIndexGift(reqData);
                mCallRefresh.enqueue(new Callback<JsonRespBase<IndexGift>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<IndexGift>> call, Response<JsonRespBase<IndexGift>>
                            response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful()) {
                            Global.sServerTimeDiffLocal = System.currentTimeMillis() - response.headers().getDate
                                    ("Date").getTime();
                            if (response.body() != null && response.body().getCode() == NetStatusCode.SUCCESS) {
                                // 获取数据成功
                                refreshSuccessEnd();
                                IndexGift data = response.body().getData();
                                updateData(data, 0, -1);
                                mLastPage = PAGE_FIRST;
                                FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_INDEX, data);
                                requestLikeData();
                                return;
                            }
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                        readCacheData();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<IndexGift>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                        readCacheData();
                    }
                });
            }
        });
    }

    private void readLikeCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GIFT_GET_ALL_LIKE,
                new CallbackListener<ArrayList<IndexGiftLike>>() {
                    @Override
                    public void doCallBack(ArrayList<IndexGiftLike> data) {
                        AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "加载的猜你喜欢数据: " + data);
                        mLikeData = data;
                    }
                }, new TypeToken<ArrayList<IndexGiftLike>>() {
                }.getType());
    }

    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GIFT_GET_INDEX,
                new CallbackListener<IndexGift>() {

                    @Override
                    public void doCallBack(IndexGift data) {
                        if (data != null) {
                            // 获取数据成功
                            refreshSuccessEnd();
                            updateData(data, 0, -1);
                            mLastPage = PAGE_FIRST;
                        } else {
                            refreshFailEnd();
                        }
                    }
                }, IndexGift.class);
    }

    private void addMoreData(ArrayList<IndexGiftNew> moreData) {
        if (moreData == null) {
            return;
        }
        int lastCount = mAdapter.getItemCount();
        mGiftData.news.addAll(moreData);
        mAdapter.updateData(mGiftData, lastCount, -1);
        mLastPage += 1;
    }

    /**
     * 首页加载更多数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> mCallLoad;

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            if (!NetworkUtil.isConnected(getContext())) {
                moreLoadFailEnd();
                return;
            }
            if (mCallLoad != null) {
                mCallLoad.cancel();
            }
            mIsLoadMore = true;
            mReqPageObj.data.page = mLastPage + 1;
            mCallLoad = Global.getNetEngine().obtainIndexGiftNew(mReqPageObj);
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
                    AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGiftNew>>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                    moreLoadFailEnd();
                }
            });
        }
    }

	/* 更新控件数据 start */

    public void updateData(IndexGift data, int start, int end) {
        if (data == null) {
            if (mGiftData == null) {
                mViewManager.showErrorRetry();
            } else {
                mAdapter.updateData(mGiftData, start, end);
                mViewManager.showContent();
            }
            return;
        }
        mGiftData = data;
        mHasData = true;

        if ((data.limit == null || data.limit.size() == 0)
                && (data.news == null || data.news.size() == 0)) {
            // 数据为空
            mViewManager.showEmpty();
            return;
        }


        data.like = mLikeData;
        mViewManager.showContent();
        mAdapter.updateData(mGiftData, start, end);
    }

    private boolean mIsLoadLike = false;

    private void requestLikeData() {


        if (!mIsLoadLike) {
            AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "请求猜你喜欢数据");
            mIsLoadLike = true;
            JsonReqBase<ReqGiftLike> req = new JsonReqBase<>();
            req.data = new ReqGiftLike();
            req.data.page = 1;
            req.data.appNames = Global.getInstalledAppNames();
            req.data.packageName = Global.getInstalledPackageNames();
            Global.getNetEngine().obtainGiftLike(req)
                    .enqueue(new Callback<JsonRespBase<GiftLikeList>>() {
                        @Override
                        public void onResponse(Call<JsonRespBase<GiftLikeList>> call,
                                               Response<JsonRespBase<GiftLikeList>> response) {
                            mIsLoadLike = false;
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                            if (response != null && response.isSuccessful()) {
                                if (response.body() != null && response.body().isSuccess()) {
                                    refreshSuccessEnd();
                                    GiftLikeList data = response.body().getData();
                                    Global.setInstalledPackageNames(data.packageNames);
                                    Global.setInstalledAppNames(data.appNames);
                                    mLikeData = data.data;
                                    AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "请求到猜你喜欢数据: " + mLikeData);
                                    updateData(mGiftData, 1, 1);
                                    FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_ALL_LIKE,
                                            mLikeData, true);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<GiftLikeList>> call, Throwable t) {
                            mIsLoadLike = false;
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                        }
                    });
        }
    }

    private boolean mIsResume = false;
    private boolean mIsVisible = false;

    @Override
    public void onResume() {
        super.onResume();
        if (mIsVisible) {
            if (mAdapter != null) {
                mAdapter.startBanner();
            }
        }
        AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(true);
        mIsResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.stopBanner();
        }
        mIsResume = false;
        AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(false);
    }


    @Override
    protected void onUserVisible() {
        super.onUserVisible();
        AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(true);
        if (mIsResume) {
            if (mAdapter != null) {
                mAdapter.startBanner();
            }
        }
        mIsVisible = true;
    }

    @Override
    protected void onUserInvisible() {
        super.onUserInvisible();
        AlarmClockManager.getInstance().setAllowNotifyGiftUpdate(false);
        if (mAdapter != null) {
            mAdapter.stopBanner();
        }
        mIsVisible = false;
    }


    /**
     * 轮询局部刷新礼包页面的网络请求声明
     */
    private Call<JsonRespBase<HashMap<String, IndexGiftNew>>> mCallRefreshCircle = null;

    @Override
    public void onGiftUpdate(int action) {
        if (!mIsPrepared) {
            mIsNotifyRefresh = mIsSwipeRefresh = false;
            return;
        }
        if (action == ObserverManager.STATUS.GIFT_UPDATE_ALL) {
            if (mIsSwipeRefresh) {
                return;
            }
            onRefresh();
            return;
        }
        if (action == ObserverManager.STATUS.GIFT_UPDATE_PART) {
            if (mIsSwipeRefresh || mIsNotifyRefresh || mGiftData == null) {
                return;
            }
            mIsNotifyRefresh = true;
            Global.THREAD_POOL.execute(new Runnable() {
                @Override
                public void run() {
//                    if (!NetworkUtil.isConnected(getContext())) {
//                        mIsNotifyRefresh = false;
//                        return;
//                    }
                    if (mCallRefreshCircle != null) {
                        mCallRefreshCircle.cancel();
                    }
                    HashSet<Integer> ids = new HashSet<Integer>();
                    if (mGiftData.limit != null) {
                        for (IndexGiftNew gift : mGiftData.limit) {
                            ids.add(gift.id);
                        }
                    }
                    if (mGiftData.news != null) {
                        for (IndexGiftNew gift : mGiftData.news) {
                            ids.add(gift.id);
                        }
                    }
                    ReqRefreshGift reqData = new ReqRefreshGift();
                    reqData.ids = ids;
                    mCallRefreshCircle = Global.getNetEngine().refreshGift(new JsonReqBase<ReqRefreshGift>(reqData));
                    try {
                        Response<JsonRespBase<HashMap<String, IndexGiftNew>>> response = mCallRefreshCircle.execute();
                        if (response != null && response.isSuccessful() && mCanShowUI) {
                            if (response.body() != null && response.body().isSuccess()) {
                                // 数据刷新成功，进行更新
                                HashMap<String, IndexGiftNew> respData = response.body().getData();
                                ArrayList<Integer> waitDelIndexs = new ArrayList<Integer>();
                                waitDelIndexs.clear();
                                updateCircle(respData, waitDelIndexs, mGiftData.limit);
                                delIndex(mGiftData.limit, waitDelIndexs);
                                waitDelIndexs.clear();
                                updateCircle(respData, waitDelIndexs, mGiftData.news);
                                delIndex(mGiftData.news, waitDelIndexs);
                                ThreadUtil.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int y = rvContainer.getScrollY();
//                                        refreshSuccessEnd();
                                        updateData(mGiftData, 1, -1);
                                        rvContainer.smoothScrollBy(0, y);
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mIsNotifyRefresh = false;
                    }
                }
            });
            return;
        }
        if (action == ObserverManager.STATUS.GIFT_UPDATE_LIKE) {
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    requestLikeData();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KeyConfig.KEY_DATA, mGiftData);
        outState.putInt(KeyConfig.KEY_DATA_O, mLastPage);
    }

    /**
     * 移到到指定位置
     *
     * @param type
     */
    public void scrollToPos(final int type) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case POS_BANNER:
                        if (rvContainer != null) {
                            rvContainer.smoothScrollToPosition(0);
                        }
                        break;
                    case POS_LIKE:
                        if (rvContainer != null) {
                            rvContainer.smoothScrollToPosition(1);
                        }
                        break;
                    case POS_LIMIT:
                        if (rvContainer != null) {
                            rvContainer.smoothScrollToPosition(2);
                        }
                        break;
                    case POS_NEW:
                        if (rvContainer != null) {
                            rvContainer.smoothScrollToPosition(3);
                        }
                }
            }
        });
    }

    /**
     * 删除已失效的索引
     *
     * @param data          已经有的数据列表
     * @param waitDelIndexs 待删除的下标索引列表
     */
    private void delIndex(ArrayList<IndexGiftNew> data, ArrayList<Integer> waitDelIndexs) {
        for (int i = waitDelIndexs.size() - 1; i >= 0; i--) {
            data.remove(waitDelIndexs.get(i).intValue());
        }
    }

    /**
     * 遍历更新所有礼包的状态
     */
    private void updateCircle(HashMap<String, IndexGiftNew> respData, ArrayList<Integer> waitDelIndexs,
                              ArrayList<IndexGiftNew> gifts) {
        int i = 0;
        for (IndexGiftNew gift : gifts) {
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

    /**
     * 更新礼包状态项内容
     *
     * @param toBeSet 待更新的礼包项
     * @param data    用于更新的数据
     */
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
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeGiftUpdateListener(this);
    }

    @Override
    public void release() {
        super.release();
        if (mAdapter != null && mAdapter instanceof OnFinishListener) {
            ((OnFinishListener) mAdapter).release();
        }
        mAdapter = null;
        rvContainer = null;
        if (mCallLoad != null) {
            mCallLoad.cancel();
            mCallLoad = null;
        }
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
            mCallRefresh = null;
        }
    }

    @Override
    public void onItemClick(int position) {
        if (mGiftData == null || mGiftData.banner == null || mGiftData.banner.size() <= position) {
            return;
        }
        IndexBanner banner = mGiftData.banner.get(position);
        StatisticsManager.getInstance().trace(getContext(), StatisticsManager.ID.GIFT_BANNER,
                StatisticsManager.ID.STR_GIFT_BANNER,
                String.format(Locale.CHINA, "第%d推广位，标题：%s", position, banner.title));
        BannerTypeUtil.handleBanner(getContext(), banner);
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }
}
