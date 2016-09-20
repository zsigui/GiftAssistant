package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostAdapter;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqIndexPost;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.NetworkUtil;

import java.io.Serializable;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 活动Fragment
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostFragment extends BaseFragment_Refresh<IndexPostNew>{

    public static final int INDEX_HEADER = 0;

    // 页面控件
    private RecyclerView rvData;
    private PostAdapter mAdapter;

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_rv_container);
        rvData = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {
        ObserverManager.getInstance().addUserUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        LinearLayoutManager llp = new SnapLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvData.setLayoutManager(llp);
        mAdapter = new PostAdapter(getContext());
        rvData.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            Serializable s = savedInstanceState.getSerializable(KeyConfig.KEY_DATA);
            if (s != null) {
                mHasData = true;
                updateData((ArrayList<IndexPostNew>) s);
                mLastPage = savedInstanceState.getInt(KeyConfig.KEY_DATA_O);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KeyConfig.KEY_DATA, mData);
        outState.putInt(KeyConfig.KEY_DATA_O, mLastPage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeUserUpdateListener(this);
    }

    /**
     * 刷新首页数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                refreshInitConfig();
                if (mData == null) {
                    readCacheData();
                }
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }

                // 设置请求对象的值
                if (mReqPageObj == null) {
                    ReqIndexPost data = new ReqIndexPost();
                    data.pageSize = 20;
                    data.type = PostTypeUtil.TYPE_CONTENT_OFFICIAL;
                    mReqPageObj = new JsonReqBase<>(data);
                }
                mReqPageObj.data.page = PAGE_FIRST;

                mCallRefresh = Global.getNetEngine().obtainPostList(NetUrl.POST_GET_LIST, mReqPageObj);
                mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
                                           Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().isSuccess()) {
                                // 获取数据成功
                                refreshSuccessEnd();
                                ArrayList<IndexPostNew> data = response.body().getData().data;
                                addHeaderData(data);
                                updateData(mData);
                                FileUtil.writeCacheByKey(getContext(), NetUrl.POST_GET_INDEX, mData);
                                return;
                            }
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                        readCacheData();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
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

    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.POST_GET_INDEX,
                new CallbackListener<ArrayList<IndexPostNew>>() {

                    @Override
                    public void doCallBack(ArrayList<IndexPostNew> data) {
                        if (mData == null) {
                            if (data != null) {
                                // 获取数据成功
                                refreshSuccessEnd();
                                updateData(data);
                            } else {
                                refreshFailEnd();
                            }
                        } else {
                            refreshCacheFailEnd();
                        }
                    }
                }, new TypeToken<ArrayList<IndexPostNew>>(){}.getType());
    }

    /**
     * 将异步返回的首页活动数据转换为列表形式
     *
     */
    private void addHeaderData(ArrayList<IndexPostNew> data) {
        if (data == null) {
            mData = new ArrayList<>();
            return;
        } else {
            mData = data;
        }
        // 添加固定头
        mData.add(0, new IndexPostNew(PostTypeUtil.TYPE_HEADER));
        mData.add(1, new IndexPostNew(PostTypeUtil.TYPE_HEADER));

    }

    public void updateData(ArrayList<IndexPostNew> data) {
        if (data == null) {
            if (mData == null) {
                mViewManager.showErrorRetry();
            } else {
                mAdapter.updateData(data);
                mViewManager.showContent();
            }
            return;
        }
        if (data.isEmpty()) {
            mViewManager.showEmpty();
            return;
        }
        mAdapter.updateData(data);
        mLastPage = PAGE_FIRST;
        mViewManager.showContent();
    }

    private void addMoreData(ArrayList<IndexPostNew> moreData) {
        if (moreData == null) {
            return;
        }
        mAdapter.addMoreData(moreData);
        mLastPage += 1;
    }

    /**
     * 首页加载更多数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallLoad;
    private JsonReqBase<ReqIndexPost> mReqPageObj;

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
            mIsLoadMore = true;
            mReqPageObj.data.page = mLastPage + 1;
            if (mCallLoad != null) {
                mCallLoad.cancel();
            }
            mCallLoad = Global.getNetEngine().obtainPostList(NetUrl.POST_GET_LIST, mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
                @Override
                public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
                                       Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null && response.body().isSuccess()) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<IndexPostNew> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                    }
                    AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                    moreLoadFailEnd();
                }
            });
        }
    }

    @Override
    public void onUserUpdate(int action) {
        switch (action) {
            case ObserverManager.STATUS.USER_UPDATE_ALL:
            case ObserverManager.STATUS.USER_UPDATE_TASK:
                if (mAdapter != null && mAdapter.getItemCount() > 0) {
                    mAdapter.notifyItemChanged(0);
                }
                break;
        }
    }

    @Override
    public String getPageName() {
        return "首页活动";
    }

}
