package com.oplay.giftcool.ui.fragment.game;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.GameSuperAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.resp.IndexGameSuper;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 15-12-30.
 */
public class GameSuperFragment extends BaseFragment_Refresh implements View.OnClickListener {

    private final static String PAGE_NAME = "游戏精品";
    private RecyclerView mRecyclerView;
    private GameSuperAdapter mAdapter;
    private IndexGameSuper mGameData;

    public static GameSuperFragment newInstance() {
        return new GameSuperFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_custome_rv_container);
        mRecyclerView = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        startBanner();
//						ImageLoaderUtil.resume();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        stopBanner();
//						ImageLoaderUtil.resume();
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        stopBanner();
//						ImageLoaderUtil.stop();
                        break;
                }
            }
        });
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

        // 设置RecyclerView的LayoutManager
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(itemDecoration);
        mAdapter = new GameSuperAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setCanShowLoad(false);
        mIsPrepared = mNoMoreLoad = true;
        mViewManager.showContent();
        if (savedInstanceState != null) {
            Serializable s = savedInstanceState.getSerializable(KeyConfig.KEY_DATA);
            if (s != null) {
                mHasData = true;
                updateData((IndexGameSuper) s);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KeyConfig.KEY_DATA, mGameData);
    }

    public void updateData(IndexGameSuper data) {
        if (data == null) {
            if (mGameData == null) {
                mViewManager.showErrorRetry();
            } else {
                mAdapter.updateData(mGameData);
                mViewManager.showContent();
            }
            return;
        }
        mGameData = data;
        mAdapter.updateData(data);
        mViewManager.showContent();
    }

    private boolean mIsResume = false;
    private boolean mIsVisible = false;

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null && mIsVisible) {
            mAdapter.startBanner();
        }
        mIsResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.stopBanner();
        }
        mIsResume = false;
    }

    @Override
    protected void onUserVisible() {
        super.onUserVisible();
        if (mAdapter != null && mIsResume) {
            mAdapter.startBanner();
        }
        mIsVisible = true;
    }

    @Override
    protected void onUserInvisible() {
        super.onUserInvisible();
        if (mAdapter != null) {
            mAdapter.stopBanner();
        }
        mIsVisible = false;
    }

    /**
     * 刷新精品游戏界面的网络请求声明
     */
    private Call<JsonRespBase<IndexGameSuper>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        refreshInitConfig();
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    if (mCallRefresh != null) {
                        mCallRefresh.cancel();
                        mCallRefresh = mCallRefresh.clone();
                    } else {
                        mCallRefresh = Global.getNetEngine().obtainIndexGameSuper(new JsonReqBase<Void>());
                    }
                    mCallRefresh.enqueue(new Callback<JsonRespBase<IndexGameSuper>>() {

                        @Override
                        public void onResponse(Call<JsonRespBase<IndexGameSuper>> call,
                                               Response<JsonRespBase<IndexGameSuper>> response) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            if (response != null && response.isSuccessful()) {
                                if (response.body() != null && response.body().getCode() == NetStatusCode
                                        .SUCCESS) {
                                    IndexGameSuper data = response.body().getData();
                                    updateData(data);
                                    refreshSuccessEnd();
                                    FileUtil.writeCacheByKey(getContext(), NetUrl.GAME_GET_INDEX_SUPER, data);
                                    return;
                                }
                            }
                            // 出错
//									refreshFailEnd();
                            readCacheData();
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<IndexGameSuper>> call, Throwable t) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
//									refreshFailEnd();
                            readCacheData();
                        }
                    });
                } else {
                    refreshFailEnd();
                }
            }
        });
    }

    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.GAME_GET_INDEX_SUPER,
                new CallbackListener<IndexGameSuper>() {

                    @Override
                    public void doCallBack(IndexGameSuper data) {
                        if (data != null) {
                            // 获取数据成功
                            updateData(data);
                            refreshSuccessEnd();
                        } else {
                            refreshFailEnd();
                        }
                    }
                }, IndexGameSuper.class);
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    @Override
    public void release() {
        super.release();
        if (mCallRefresh != null) {
            mCallRefresh.cancel();
            mCallRefresh = null;
        }
    }
}
