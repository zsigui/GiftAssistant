package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.config.util.PostTypeUtil;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqIndexPost;
import com.oplay.giftcool.model.data.resp.IndexPost;
import com.oplay.giftcool.model.data.resp.IndexPostNew;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.FileUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 活动Fragment
 * <p/>
 * Created by zsigui on 16-4-5.
 */
public class PostFragment extends BaseFragment_Refresh<IndexPostNew> implements CallbackListener<Boolean> {

    private final String TAG_NAME = "首页活动";
    private final String PREFIX_POST = "获取数据";

    public static final int INDEX_HEADER = 0;
    public static final int INDEX_OFFICIAL = 1;
    public static final int INDEX_NOTIFY = 2;

    private int mIndexOfficialHeader = 1;
    private int mIndexNotifyHeader = 2;
    private final int PAGE_SIZE = 20;

    // 页面控件
    private RecyclerView rvData;
    private PostAdapter mAdapter;

    private IndexPost mInitData;

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
        mAdapter.setCallbackListener(this);
        ObserverManager.getInstance().addUserUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        LinearLayoutManager llp = new SnapLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), llp.getOrientation());
        rvData.setLayoutManager(llp);
        rvData.addItemDecoration(itemDecoration);
        mAdapter = new PostAdapter(getContext());
        rvData.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().removeUserUpdateListener(this);
    }

    /**
     * 刷新首页数据的网络请求声明
     */
    private Call<JsonRespBase<IndexPost>> mCallRefresh;

    @Override
    protected void lazyLoad() {
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                refreshInitConfig();
                // 判断网络情况
//        if (!NetworkUtil.isConnected(getContext())) {
//            refreshFailEnd();
//            return;
//        }
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }

                // 设置请求对象的值
                final boolean isRead = AssistantApp.getInstance().isReadAttention();
                if (mReqPageObj == null) {
                    ReqIndexPost data = new ReqIndexPost();
                    data.pageSize = PAGE_SIZE;
                    data.isAttention = (isRead ? 1 : 0);
                    mReqPageObj = new JsonReqBase<ReqIndexPost>(data);
                }
                if (isRead) {
                    mReqPageObj.data.appNames = Global.getInstalledAppNames();
                } else {
                    mReqPageObj.data.appNames = null;
                }

                mCallRefresh = Global.getNetEngine().obtainIndexPost(mReqPageObj);
                mCallRefresh.enqueue(new Callback<JsonRespBase<IndexPost>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<IndexPost>> call, Response<JsonRespBase<IndexPost>>
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
                                final IndexPost data = response.body().getData();
                                transferIndexPostToArray(data);
                                updateData(mData);
                                if (data.notifyData == null || data.notifyData.isEmpty()) {
                                    // 关注快讯为空
                                    mAdapter.toggleButton(false, true);
                                }
                                FileUtil.writeCacheByKey(getContext(), NetUrl.POST_GET_INDEX, data);
                                return;
                            }
                            AccountManager.getInstance().judgeIsSessionFailed(response.body());
                        }
                        AppDebugConfig.warn(response);
//						refreshFailEnd();
                        readCacheData();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<IndexPost>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        AppDebugConfig.warn(t);
//						refreshFailEnd();
                        readCacheData();
                    }
                });
            }
        });
    }


    private void readCacheData() {
        FileUtil.readCacheByKey(getContext(), NetUrl.POST_GET_INDEX,
                new CallbackListener<IndexPost>() {

                    @Override
                    public void doCallBack(IndexPost data) {
                        if (data != null) {
                            // 获取数据成功
                            refreshSuccessEnd();
                            transferIndexPostToArray(data);
                            updateData(mData);
                            if (data.notifyData == null || data.notifyData.isEmpty()) {
                                // 关注快讯为空
                                mAdapter.toggleButton(false, true);
                            }
                        } else {
                            refreshFailEnd();
                        }
                    }
                }, IndexPost.class);
    }

    /**
     * 将异步返回的首页活动数据转换为列表形式
     *
     * @param data
     */
    private void transferIndexPostToArray(IndexPost data) {
        if (data == null) {
            mData = null;
            return;
        }
        mInitData = data;
        if (mData == null) {
            mData = new ArrayList<>();
        } else {
            mData.clear();
        }
        // 添加固定头
        IndexPostNew header = new IndexPostNew();
        header.showType = PostTypeUtil.TYPE_HEADER;
        mData.add(header);

        mIndexOfficialHeader = mData.size() - 1;
        // 添加官方活动部分
        IndexPostNew titleOne = new IndexPostNew();
        titleOne.showType = PostTypeUtil.TYPE_TITLE_OFFICIAL;
        mData.add(titleOne);
        if (data.officialData != null && !data.officialData.isEmpty()) {
            mData.addAll(data.officialData);
        }

        mIndexNotifyHeader = mData.size() - 1;
        IndexPostNew titleTwo = new IndexPostNew();
        titleTwo.showType = PostTypeUtil.TYPE_TITLE_GAME;
        mData.add(titleTwo);
        // 添加游戏快讯部分
        if (data.notifyData != null && !data.notifyData.isEmpty()) {
            mData.addAll(data.notifyData);
        }
        if (data.notifyData == null || data.notifyData.size() < 10) {
            mRefreshLayout.setCanShowLoad(false);
            mNoMoreLoad = true;
        }
    }

    public void updateData(ArrayList<IndexPostNew> data) {
        if (data == null) {
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
//            if (!NetworkUtil.isConnected(getContext())) {
//                moreLoadFailEnd();
//                return;
//            }
            mIsLoadMore = true;
            final boolean isRead = AssistantApp.getInstance().isReadAttention();
            if (isRead) {
                mReqPageObj.data.isAttention = 1;
                mReqPageObj.data.appNames = Global.getInstalledAppNames();
            } else {
                mReqPageObj.data.isAttention = 0;
                mReqPageObj.data.appNames = null;
            }
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
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_FRAG, (response.body() == null ?
                                    "解析错误" : response.body().error()));
                        }
                    }
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    moreLoadFailEnd();
                }
            });
        }
    }

    /**
     *
     */
    private Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> mCallChange;

    /**
     * 刷新游戏资讯列表数据
     */
    private void refreshNotifyData() {
        Global.THREAD_POOL.execute(new Runnable() {

            @Override
            public void run() {
                final boolean isRead = AssistantApp.getInstance().isReadAttention();
                if (isRead) {
                    mReqPageObj.data.appNames = Global.getInstalledAppNames();
                } else {
                    mReqPageObj.data.appNames = null;
                }
                mReqPageObj.data.isAttention = (isRead ? 1 : 0);
                mReqPageObj.data.page = PAGE_FIRST;
                mIsSwipeRefresh = true;
                mCallChange = Global.getNetEngine().obtainPostList(NetUrl.POST_GET_LIST, mReqPageObj);
                mCallChange.enqueue(new Callback<JsonRespBase<OneTypeDataList<IndexPostNew>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call,
                                           Response<JsonRespBase<OneTypeDataList<IndexPostNew>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        // 解除刷新和加载的状态锁定
                        mIsSwipeRefresh = mIsNotifyRefresh = mIsLoading = mIsLoadMore = false;
                        if (mRefreshLayout != null) {
                            mRefreshLayout.setRefreshing(false);
                            mRefreshLayout.setEnabled(true);
                            mRefreshLayout.setLoading(false);
                            mRefreshLayout.setCanShowLoad(true);
                        }
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().isSuccess()) {
                                final ArrayList<IndexPostNew> data = response.body().getData().data;
                                if (data != null && !data.isEmpty()) {

                                    mInitData.notifyData = data;
                                    transferIndexPostToArray(mInitData);
                                    mNoMoreLoad = false;
                                    mRefreshLayout.setCanShowLoad(true);
                                    updateData(mData);
                                } else {
                                    toggleFailed("没有相关关注快讯");
                                }
                                return;
                            }
                            if (AppDebugConfig.IS_DEBUG) {
                                KLog.d(AppDebugConfig.TAG_FRAG, response.body() != null ?
                                        response.body().error() : "解析失败");
                            }
//							return;
                        }
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_FRAG, response != null ?
                                    response.code() + ", " + response.message() : "返回失败");
                        }
                        toggleFailed("error~获取相关关注快讯失败");
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<IndexPostNew>>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (AppDebugConfig.IS_DEBUG) {
                            KLog.d(AppDebugConfig.TAG_FRAG, t);
                        }
                        toggleFailed("error~获取相关关注快讯失败");
                        mIsSwipeRefresh = mIsNotifyRefresh = mIsLoading = mIsLoadMore = false;
                        if (mRefreshLayout != null) {
                            mRefreshLayout.setRefreshing(false);
                            mRefreshLayout.setEnabled(true);
                            mRefreshLayout.setLoading(false);
                            mRefreshLayout.setCanShowLoad(true);
                        }
                    }
                });
            }
        });
    }

    private void toggleFailed(String msg) {
        mAdapter.toggleButton(false, false);
        if (!TextUtils.isEmpty(msg)) {
            ToastUtil.showShort(msg);
        }
    }

    @Override
    public void doCallBack(Boolean data) {
        if (data != null) {
            if (!mIsSwipeRefresh) {
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }
                if (mCallLoad != null) {
                    mCallLoad.cancel();
                }
                if (mCallChange != null) {
                    mCallChange.cancel();
                }
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(true);
                    mRefreshLayout.setEnabled(false);
                    mRefreshLayout.setCanShowLoad(false);
                }
                refreshNotifyData();
            }
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

    public void setPagePosition(final int type) {
//		ThreadUtil.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (rvData != null) {
//					switch (type) {
//						case INDEX_HEADER:
//							rvData.smoothScrollToPosition(0);
//							break;
//						case INDEX_OFFICIAL:
////							rvData.smoothScrollToPosition(mIndexOfficialHeader < mAdapter.getItemCount() ?
////									mIndexOfficialHeader : mAdapter.getItemCount() - 1);
//							break;
//						case INDEX_NOTIFY:
////							rvData.smoothScrollToPosition(mIndexNotifyHeader < mAdapter.getItemCount() ?
////									mIndexNotifyHeader : mAdapter.getItemCount() - 1);
//							break;
//					}
//
//				}
//			}
//		});
    }

    @Override
    public String getPageName() {
        return TAG_NAME;
    }

}
