package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameNoticeAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.IndexGameNew;
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
public class GameNoticeFragment extends BaseFragment_Refresh<IndexGameNew> {

    private final static String PAGE_NAME = "游戏排行";
    private final static String KEY_DATA = "key_data";
    private final static long MAINTAIN_DATA_TIME = 5 * 1000;
    private final static int PAGE_SIZE = 20;
    private JsonReqBase<ReqPageData> mReqPageObj;

    private RecyclerView mDataView;
    private GameNoticeAdapter mAdapter;

    private boolean mInPage = false;
    private boolean mIsRunning = false;
    private Handler mHandler = new Handler();
    /**
     * 每隔5秒调起一次清除数据
     */
    private Runnable mClearDataTask = new Runnable() {
        @Override
        public void run() {
            if (!mCanShowUI) {
                return;
            }
            if (mInPage || mData == null || mData.size() < PAGE_SIZE) {
                if (mHandler != null) {
                    mHandler.postDelayed(this, MAINTAIN_DATA_TIME);
                }
                return;
            }
            ArrayList<IndexGameNew> remainData = new ArrayList<>(PAGE_SIZE);
            for (int i = 0; i < PAGE_SIZE; i++) {
                remainData.add(mData.get(i));
            }
            if (!mInPage || mIsRunning) {
                mNoMoreLoad = false;
                updateData(remainData);
            }
        }
    };

    public static GameNoticeFragment newInstance() {
        return new GameNoticeFragment();
    }

    public static GameNoticeFragment newInstance(ArrayList<IndexGameNew> data) {
        GameNoticeFragment fragment = new GameNoticeFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DATA, data);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_rv_container);
        mDataView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
//		mDataView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//				switch (newState) {
//					case RecyclerView.SCROLL_STATE_SETTLING:
//						ImageLoaderUtil.stop();
//						break;
//					case RecyclerView.SCROLL_STATE_IDLE:
//					case RecyclerView.SCROLL_STATE_DRAGGING:
//						ImageLoaderUtil.resume();
//						break;
//
//				}
//			}
//		});
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        ReqPageData data = new ReqPageData();
        data.pageSize = PAGE_SIZE;
        mReqPageObj = new JsonReqBase<ReqPageData>(data);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), llm.getOrientation());
        mDataView.setLayoutManager(llm);
        mDataView.addItemDecoration(decoration);
        mAdapter = new GameNoticeAdapter(getContext());
        mDataView.setAdapter(mAdapter);
        mViewManager.showContent();

        if (mHandler == null) {
            mHandler = new Handler();
        }
    }

    /**
     * 刷新游戏榜单列表数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    if (mCallRefresh != null) {
                        mCallRefresh.cancel();
                    }
                    mReqPageObj.data.page = 1;
                    mCallRefresh = Global.getNetEngine().obtainGameList(NetUrl.GAME_GET_INDEX_NOTICE, mReqPageObj);
                    mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
                        @Override
                        public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> call,
                                               Response<JsonRespBase<OneTypeDataList<IndexGameNew>>>
                                                       response) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            if (response != null && response.isSuccessful()) {
                                if (response.body() != null &&
                                        response.body().getCode() == NetStatusCode.SUCCESS) {
                                    refreshSuccessEnd();
                                    OneTypeDataList<IndexGameNew> backObj = response.body().getData();
                                    refreshLoadState(backObj.data, backObj.isEndPage);
                                    updateData(backObj.data);
                                    FileUtil.writeCacheByKey(getContext(), NetUrl.GAME_GET_INDEX_NOTICE, backObj);
                                    return;
                                }
                            }
                            AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                            readCacheData();
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> call,
                                              Throwable t) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                            readCacheData();
                        }
                    });
                } else {
                    readCacheData();
                }
            }
        });
    }

    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GAME_GET_INDEX_NOTICE,
                new CallbackListener<OneTypeDataList<IndexGameNew>>() {

                    @Override
                    public void doCallBack(OneTypeDataList<IndexGameNew> data) {
                        if (data != null) {
                            // 获取数据成功
                            refreshSuccessEnd();
                            refreshLoadState(data.data, data.isEndPage);
                            updateData(data.data);
                        } else {
                            refreshFailEnd();
                        }
                    }
                }, new TypeToken<OneTypeDataList<IndexGameNew>>() {
                }.getType());
    }

    /**
     * 加载更多游戏榜单内容的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> mCallLoad;

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (!mNoMoreLoad && !mIsLoadMore) {
            mIsLoadMore = true;
            mReqPageObj.data.page = mLastPage + 1;
            Global.THREAD_POOL.execute(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtil.isConnected(getContext())) {
                        if (mCallLoad != null) {
                            mCallLoad.cancel();
                        }
                        mCallLoad = Global.getNetEngine().obtainGameList(NetUrl.GAME_GET_INDEX_NOTICE, mReqPageObj);
                        mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexGameNew>>>() {
                            @Override
                            public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> call,
                                                   Response<JsonRespBase<OneTypeDataList<IndexGameNew>>>
                                                           response) {
                                if (!mCanShowUI || call.isCanceled()) {
                                    return;
                                }
                                if (response != null && response.isSuccessful()) {
                                    if (response.body() != null && response.body().isSuccess()) {
                                        moreLoadSuccessEnd();
                                        OneTypeDataList<IndexGameNew> backObj = response.body().getData();
                                        setLoadState(backObj.data, backObj.isEndPage);
                                        addMoreData(backObj.data);
                                        return;
                                    }
                                }
                                moreLoadFailEnd();
                            }

                            @Override
                            public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexGameNew>>> call, Throwable
                                    t) {
                                if (!mCanShowUI || call.isCanceled()) {
                                    return;
                                }
                                moreLoadFailEnd();
                            }
                        });
                    } else {
                        moreLoadFailEnd();
                    }
                }
            });
        }
    }

    public void updateData(ArrayList<IndexGameNew> data) {
        if (data == null || data.size() == 0) {
            mViewManager.showEmpty();
            return;
        }
        mViewManager.showContent();
        mHasData = true;
        mData = data;
        mAdapter.updateData(mData);
        mLastPage = 1;
    }

    private void addMoreData(ArrayList<IndexGameNew> moreData) {
        if (moreData == null) {
            return;
        }
        mData.addAll(moreData);
        mAdapter.updateData(mData);
        mLastPage += 1;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mIsRunning) {
            if (mHandler != null) {
                mHandler.postDelayed(mClearDataTask, MAINTAIN_DATA_TIME);
            }
            mIsRunning = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mInPage && mIsRunning) {
            if (mHandler != null) {
                mHandler.removeCallbacks(mClearDataTask);
            }
            mIsRunning = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mInPage = isVisibleToUser;
        if (isVisibleToUser) {
            if (mHandler != null) {
                mHandler.removeCallbacks(mClearDataTask);
            }
            mIsRunning = false;
        } else {
            if (mHandler != null) {
                mHandler.postDelayed(mClearDataTask, MAINTAIN_DATA_TIME);
            }
            mIsRunning = true;
        }
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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}
