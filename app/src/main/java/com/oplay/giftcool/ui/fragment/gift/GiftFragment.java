package com.oplay.giftcool.ui.fragment.gift;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
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
import com.oplay.giftcool.manager.AlarmClockManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.manager.StatisticsManager;
import com.oplay.giftcool.model.data.req.ReqGiftLike;
import com.oplay.giftcool.model.data.req.ReqIndexGift;
import com.oplay.giftcool.model.data.resp.GiftLikeList;
import com.oplay.giftcool.model.data.resp.IndexBanner;
import com.oplay.giftcool.model.data.resp.IndexGift;
import com.oplay.giftcool.model.data.resp.IndexGiftLike;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ThreadUtil;

import java.io.Serializable;
import java.util.ArrayList;
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

    public static final int POS_BANNER = 0;
    public static final int POS_LIKE = 2;
    public static final int POS_LIMIT = 3;
    public static final int POS_NEW = 4;
    public static final int POS_FREE = 5;

    private RecyclerView rvContainer;
    private GiftAdapter mAdapter;

    // ‘猜你喜欢’ 分离出来的数据
    private IndexGift mGiftData;
    private ArrayList<IndexGiftLike> mLikeData;
    private boolean hasRequestFromNet = false;

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
        mAdapter.setOnBannerItemClickListener(this);
        rvContainer.setAdapter(mAdapter);
        readLikeCacheData();
        mRefreshLayout.setCanShowLoad(false);
        mRefreshLayout.setOnLoadListener(null);
        if (savedInstanceState != null) {
            Serializable s = savedInstanceState.getSerializable(KeyConfig.KEY_DATA);
            if (s != null) {
                mHasData = true;
                mGiftData = (IndexGift) s;
                mLikeData = mGiftData.like;
                updateData(mGiftData);
            }
        }
        mNeedHideToast = true;
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
        refreshInitConfig();
        // 判断网络情况
        if (!NetworkUtil.isConnected(getContext())) {
            readCacheData();
            return;
        }
        if (mData == null) {
            readCacheData();
        } else if (mLikeData == null) {
            requestLikeData();
        }
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
        }

        ReqIndexGift data = new ReqIndexGift();
        data.appNames = new HashSet<>();
        data.page = PAGE_FIRST;
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
                        data.like = mLikeData;
                        updateData(data);
                        mLastPage = PAGE_FIRST;
                        FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_INDEX, data);
                        if (!hasRequestFromNet) {
                            requestLikeData();
                        }
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

    private void readLikeCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GIFT_GET_ALL_LIKE,
                new CallbackListener<ArrayList<IndexGiftLike>>() {
                    @Override
                    public void doCallBack(ArrayList<IndexGiftLike> data) {
                        AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "加载的猜你喜欢数据: " + data);
                        if (data != null && data.size() > 0) {
                            for (int i = data.size() - 1; i > 3; i--) {
                                data.remove(i);
                            }
                            mLikeData = data;
                        }
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
                            updateData(data);
                            mLastPage = PAGE_FIRST;
                        } else {
                            if (mLikeData == null) {
                                refreshFailEnd();
                            } else {
                                refreshCacheFailEnd();
                            }
                        }

                    }
                }, IndexGift.class);
    }

    /* 更新控件数据 start */
    @SuppressWarnings("unchecked")
    public void updateData(IndexGift data) {
        if (data == null) {
            if (mData == null || mData.isEmpty()) {
                mHasData = false;
                mViewManager.showErrorRetry();
            } else {
                mAdapter.updateData(mData);
                mViewManager.showContent();
            }
            return;
        }
        mHasData = true;
        mGiftData = data;
        if ((data.free == null || data.free.isEmpty())
                && (data.limit == null || data.limit.isEmpty())
                && (data.news == null || data.news.isEmpty())) {
            // 数据为空
            mViewManager.showEmpty();
            return;
        }

        mPosArray.clear();

        if (mData == null) {
            mData = new ArrayList();
        } else {
            mData.clear();
        }
        if (data.banner != null) {
            mData.add(data.banner);
        } else {
            mData.add(new ArrayList<IndexBanner>());
        }
        mPosArray.append(POS_BANNER, 0);

        if (data.icons != null) {
            mData.add(data.icons);
        } else {
            mData.add(new ArrayList<String>());
        }

        int pos = 2;
        if (mLikeData != null) {
            data.like = mLikeData;
        }
        if (data.like != null && !data.like.isEmpty()) {
            mData.add(GiftAdapter.TYPE_HEADER_LIKE);
            mData.addAll(data.like);
            mPosArray.append(POS_LIKE, pos);
            pos += data.like.size() + 1;
        }
        if (data.limit != null && !data.limit.isEmpty()) {
            mData.add(GiftAdapter.TYPE_HEADER_LIMIT);
            mData.addAll(data.limit);
            mPosArray.append(POS_LIMIT, pos);
            pos += data.limit.size() + 1;
        }
        if (data.free != null && !data.free.isEmpty()) {
            mData.add(GiftAdapter.TYPE_HEADER_FREE);
            mData.addAll(data.free);
            mPosArray.append(POS_FREE, pos);
            pos += data.free.size() + 1;
        }
        if (data.news != null && !data.news.isEmpty()) {
            mData.add(GiftAdapter.TYPE_HEADER_NEW);
            mData.addAll(data.news);
            mPosArray.append(POS_NEW, pos);
        }
        mAdapter.updateData(mData);
        mViewManager.showContent();
    }

    private boolean mIsLoadLike = false;

    private void requestLikeData() {
        if (!mIsLoadLike && !hasRequestFromNet) {
            mIsLoadLike = true;
            JsonReqBase<ReqGiftLike> req = new JsonReqBase<>();
            req.data = new ReqGiftLike();
            req.data.page = 1;
            req.data.appNames = Global.getInstalledAppNames();
            req.data.pageSize = 4;
            req.data.packageName = Global.getInstalledPackageNames();
            Global.getNetEngine().obtainGiftLike(req)
                    .enqueue(new Callback<JsonRespBase<GiftLikeList>>() {
                        @Override
                        @SuppressWarnings("unchecked")
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
                                    Global.setInstalledAppNames(data.appNames);
                                    mLikeData = data.data;

                                    HashSet<String> arr = new HashSet<String>();
                                    for (IndexGiftLike o : mLikeData) {
                                        arr.add(o.packName);
                                    }
                                    Global.setInstalledPackageNames(arr);

                                    hasRequestFromNet = true;
                                    AppDebugConfig.d(AppDebugConfig.TAG_FRAG, "请求到猜你喜欢数据: " + mLikeData);
                                    FileUtil.writeCacheByKey(getContext(), NetUrl.GIFT_GET_ALL_LIKE,
                                            mLikeData, true);
                                    if (mGiftData != null) {
                                        mGiftData.like = mLikeData;
                                        updateData(mGiftData);
                                    }
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
            ThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRefresh();
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
        if (mLikeData != null) {
            outState.putSerializable(KeyConfig.KEY_DATA_O, mLikeData);
        }
        if (mGiftData != null) {
            outState.putSerializable(KeyConfig.KEY_DATA, mGiftData);
        }
    }

    private SparseIntArray mPosArray = new SparseIntArray();

    /**
     * 移到到指定位置
     *
     * @param type
     */
    public void scrollToPos(final int type) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rvContainer != null && mPosArray != null) {
                    rvContainer.smoothScrollToPosition(mPosArray.get(type));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeGiftUpdateListener(this);
    }

    @Override
    public void release() {
        super.release();
        if (mAdapter != null) {
            mAdapter.release();
        }
        mAdapter = null;
        rvContainer = null;
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
            mCallRefresh = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onItemClick(int position) {
        if (mData == null || mData.size() < 1) {
            return;
        }
        IndexBanner banner = ((ArrayList<IndexBanner>) mData.get(0)).get(position);
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
