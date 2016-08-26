package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.OpenServerAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.model.data.req.ReqServerInfo;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.ServerInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.ServerInfoActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.DateUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-8-24.
 */
public class OpenServerFragment extends BaseFragment_Refresh<ServerInfo> implements CallbackListener<Boolean> {


    private final String STR_REFRESH_NEWEST = "当前已是最新数据";
    private final String STR_LOAD_OLDEST = "无更多数据可加载";
    private final int INDEX_BEFORE = 0;
    private final int INDEX_YESTERDAY = 1;
    private final int INDEX_TODAY = 2;
    private final int INDEX_TOMORROW = 3;
    private final int INDEX_AFTER = 4;
    private CheckedTextView tvBefore;
    private CheckedTextView tvYesterday;
    private CheckedTextView tvToday;
    private CheckedTextView tvTomorrow;
    private CheckedTextView tvAfter;
    private LinearLayout llAnchors;

    private RecyclerView rvData;
    private OpenServerAdapter mAdapter;
    private ArrayList<AnchorTag> mSortedTags;
    private String mUrl;
    private int mCurIndex = -1;

    private static class AnchorTag {
        String tag = "";
        int anchor;

        CheckedTextView v;
        ArrayList<ServerInfo> data = new ArrayList<>();
        // 是否有更多的数据，对于更早/昨/今/明则切换后一个tab
        boolean canLoad;
        // 是否有刷新的数据，对于昨/今/明/以后则切换前一个tab
        boolean canRefresh;

        // 保存当前刷新加载的页码
        int offsetLoad = 0;
        int offsetRefresh = 1;

        public AnchorTag(String tag, int anchor, CheckedTextView v) {
            this.tag = tag;
            this.anchor = anchor;
            this.v = v;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) {
                return true;
            } else if (o instanceof AnchorTag) {
                return ((AnchorTag) o).tag.equalsIgnoreCase(tag);
            }
            return false;
        }
    }

    public static OpenServerFragment newInstance(int type) {
        OpenServerFragment fragment = new OpenServerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KeyConfig.KEY_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_server_info);
        tvBefore = getViewById(R.id.tv_before);
        tvYesterday = getViewById(R.id.tv_yesterday);
        tvToday = getViewById(R.id.tv_today);
        tvTomorrow = getViewById(R.id.tv_tomorrow);
        tvAfter = getViewById(R.id.tv_after);
        rvData = getViewById(R.id.rv_content);
        llAnchors = getViewById(R.id.ll_anchors);
    }

    @Override
    protected void setListener() {
        tvBefore.setOnClickListener(this);
        tvYesterday.setOnClickListener(this);
        tvToday.setOnClickListener(this);
        tvTomorrow.setOnClickListener(this);
        tvAfter.setOnClickListener(this);
        if (getActivity() instanceof ServerInfoActivity) {
            ((ServerInfoActivity) getActivity()).addListener(this);
        }
//        rvData.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int firstItem = llm.findFirstVisibleItemPosition();
//                if (mSortedTags != null) {
//                    for (int i = 0; i < mSortedTags.size(); i++) {
//                        if (firstItem >= mSortedTags.get(i).anchor
//                                && (mSortedTags.size() == i + 1 || firstItem < mSortedTags.get(i + 1).anchor)) {
//                            mSortedTags.get(i).v.setChecked(true);
//                        }
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            return;
        }
        int type = getArguments().getInt(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_OPEN_SERVER);
        mUrl = (type == KeyConfig.TYPE_ID_OPEN_SERVER ? NetUrl.POST_GET_OPEN_SERVER : NetUrl.POST_GET_OPEN_TEST);
        mAdapter = new OpenServerAdapter(getContext(), type);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        rvData.addItemDecoration(decoration);
        rvData.setLayoutManager(llm);
        rvData.setAdapter(mAdapter);
        mSortedTags = new ArrayList<>(5);
        mSortedTags.add(new AnchorTag(DateUtil.getDate("yyyy-MM-dd", -2), 0, tvBefore));
        mSortedTags.add(new AnchorTag(DateUtil.getDate("yyyy-MM-dd", -1), 0, tvYesterday));
        mSortedTags.add(new AnchorTag(DateUtil.getDate("yyyy-MM-dd", 0), 0, tvToday));
        mSortedTags.add(new AnchorTag(DateUtil.getDate("yyyy-MM-dd", 1), 0, tvTomorrow));
        mSortedTags.add(new AnchorTag(DateUtil.getDate("yyyy-MM-dd", 2), 0, tvAfter));
    }

    Call<JsonRespBase<OneTypeDataList<ServerInfo>>> mCall;

    @Override
    protected void lazyLoad() {
        if (AccountManager.getInstance().isLogin()
                && AssistantApp.getInstance().isReadAttention()) {
            initFocusData();
        } else {
            AssistantApp.getInstance().setIsReadAttention(false);
            if (getActivity() instanceof ServerInfoActivity) {
                ((ServerInfoActivity) getActivity()).setTbState(false);
            }
            handleCheckClick(INDEX_TODAY);
        }
    }

    public int getCheckedAnchorIndex() {
        for (int i = 0; i < mSortedTags.size(); i++) {
            if (mSortedTags.get(i).v.isChecked()) {
                return i;
            }
        }
        // 正常走到这里代表执行错误
        AppDebugConfig.d(AppDebugConfig.TAG_DEBUG_INFO, "获取点击的标签错误");
        return -1;
    }

    @Override
    public void onRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(false);
        }
        if (mIsSwipeRefresh || mIsLoading) {
            return;
        }
        mIsSwipeRefresh = true;
        if (AssistantApp.getInstance().isReadAttention()) {
            // 正常加载
            refreshFocusData();
        } else {
            // 下拉加载则跳转到上一Tab，已是最后tab则正常加载
            int index = getCheckedAnchorIndex();
            if (index != INDEX_BEFORE && mSortedTags.get(index).canRefresh) {
                // 跳转上一个TAB
                if (mSortedTags.get(index - 1).data != null) {
                    mAdapter.updateData(mSortedTags.get(index - 1).data);
                    rvData.scrollToPosition(mAdapter.getItemCount() - 1);
                    mRefreshLayout.setEnabled(true);
                    mIsLoading = mIsSwipeRefresh = false;
                } else {
                    // 上一标签无数据，执行类网络加载操作
                    handleCheckClick(index - 1);
                }
            } else {
                // 正常加载
                handleTabRefresh(index);
            }
        }
    }

    @Override
    protected void loadMoreData() {
        super.loadMoreData();
        if (mIsLoadMore) {
            return;
        }
        if (AssistantApp.getInstance().isReadAttention()) {
            loadFocusData();
        } else {
            // 加载到无数据则跳转到下一Tab，已是最后tab则结束
            int index = getCheckedAnchorIndex();
            if (index != INDEX_AFTER && !mSortedTags.get(index).canLoad) {
                // 跳转下一个TAB
                tabCheck(index + 1);
                if (mSortedTags.get(index + 1).data != null) {
                    mAdapter.updateData(mSortedTags.get(index + 1).data);
                    rvData.scrollToPosition(0);
                    mNoMoreLoad = !mSortedTags.get(index + 1).canLoad;
                    mRefreshLayout.setEnabled(true);
                    mRefreshLayout.setLoading(false);
                    mIsLoading = mIsLoadMore = false;
                    mRefreshLayout.setCanShowLoad(mNoMoreLoad);
                } else {
                    // 上一标签无数据，执行类网络加载操作
                    handleTabLoad(index + 1);
                }
            } else {
                // 正常加载
                //refreshDataWithCallback(mSortedTags.get(index), false, null);
                handleTabLoad(index);
            }
        }
    }

    /* ----------------- 只看我关注的网络加载 START ------------------- */

    // 用于记录关注的
    JsonReqBase<ReqServerInfo> mFocusReq;
    int mFocusLoadIndex = 0;
    int mFocusRefreshIndex = 1;

    private void refreshFocusData() {
        if (mCall != null) {
            mCall.cancel();
        }

        // 设置请求对象的值
        if (mFocusReq == null) {
            mFocusRefreshIndex = 1;
            ReqServerInfo serverInfo = new ReqServerInfo();
            serverInfo.isFocus = 1;
            serverInfo.offset = mFocusRefreshIndex - 1;
            mFocusReq = new JsonReqBase<>(serverInfo);
        } else {
            mFocusReq.data.offset = mFocusRefreshIndex - 1;
        }
        if (mFocusRefreshIndex == 0) {
            mHasData = false;
        }

        mCall = Global.getNetEngine().obtainServerInfo(mUrl, mFocusReq);
        mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
            @Override
            public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                   Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                if (call.isCanceled()) {
                    return;
                }
                mIsLoading = mIsSwipeRefresh = false;
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setRefreshing(false);
                if (response != null && response.isSuccessful()
                        && response.body() != null && response.body().isSuccess()) {
                    OneTypeDataList<ServerInfo> data = response.body().getData();
                    mHasData = true;
                    mFocusRefreshIndex -= 1;
                    if (!data.data.isEmpty()) {
                        if (mFocusRefreshIndex == 0) {
                            mAdapter.updateData(data.data);
                        } else {
                            mAdapter.getData().addAll(0, data.data);
                            mAdapter.notifyItemRangeInserted(0, data.data.size());
                        }
                    } else {
                        // 已是最新
                        ToastUtil.showShort(STR_REFRESH_NEWEST);
                    }
                    if (mFocusRefreshIndex == 0) {
                        mRefreshLayout.setCanShowLoad(true);
                    }
                    return;
                }
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                ToastUtil.blurThrow(t);
                mIsLoading = mIsSwipeRefresh = false;
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setRefreshing(false);
                if (mFocusRefreshIndex == 0) {
                    mRefreshLayout.setCanShowLoad(false);
                }
            }
        });
    }

    private void loadFocusData() {
        if (mCall != null) {
            mCall.cancel();
        }

        // 设置请求对象的值
        if (mFocusReq == null) {
            mFocusLoadIndex = 0;
            ReqServerInfo serverInfo = new ReqServerInfo();
            serverInfo.isFocus = 1;
            serverInfo.offset = mFocusLoadIndex + 1;
            mFocusReq = new JsonReqBase<>(serverInfo);
        } else {
            mFocusLoadIndex = mFocusLoadIndex + 1;
            mFocusReq.data.offset = mFocusLoadIndex;
        }

        mCall = Global.getNetEngine().obtainServerInfo(mUrl, mFocusReq);
        mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
            @Override
            public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                   Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                if (call.isCanceled()) {
                    return;
                }
                mIsLoading = mIsLoadMore = false;
                mRefreshLayout.setLoading(false);
                if (response != null && response.isSuccessful()
                        && response.body() != null && response.body().isSuccess()) {
                    OneTypeDataList<ServerInfo> data = response.body().getData();
                    mFocusLoadIndex += 1;
                    if (!data.data.isEmpty()) {
                        mAdapter.getData().addAll(data.data);
                        mAdapter.notifyItemRangeInserted(mAdapter.getData().size() - data.data.size(), data.data.size
                                ());
                    }
                    if (data.isEndPage) {
                        // 已是最旧的数据
                        ToastUtil.showShort(STR_LOAD_OLDEST);
                    }
                    mNoMoreLoad = data.isEndPage;
                    return;
                }
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                ToastUtil.blurThrow(t);
                mIsLoading = mIsLoadMore = false;
                mRefreshLayout.setLoading(false);
            }
        });
    }

     /* ----------------- 只看我关注的网络加载 END ------------------- */

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_before:
                handleCheckClick(INDEX_BEFORE);
                break;
            case R.id.tv_yesterday:
                handleCheckClick(INDEX_YESTERDAY);
                break;
            case R.id.tv_today:
                handleCheckClick(INDEX_TODAY);
                break;
            case R.id.tv_tomorrow:
                handleCheckClick(INDEX_TOMORROW);
                break;
            case R.id.tv_after:
                handleCheckClick(INDEX_AFTER);
                break;
        }
    }


    /**
     * 刷新新数据
     */
    public void refreshDataWithCallback(final AnchorTag data, final int focus, final
    CallbackListener<OneTypeDataList<ServerInfo>>
            listener) {
        // 判断是否处于加载刷新中
        if (!mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(true);
        }
        mRefreshLayout.setCanShowLoad(false);
        mRefreshLayout.setLoading(false);
        // 加载数据
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                // 判断网络情况
                if (!NetworkUtil.isConnected(getContext())) {
                    refreshFailed();
                    return;
                }
                if (mCall != null) {
                    mCall.cancel();
                }

                // 设置请求对象的值
                ReqServerInfo serverInfo = new ReqServerInfo();
                JsonReqBase<ReqServerInfo> reqBase = new JsonReqBase<>(serverInfo);
                reqBase.data.isFocus = focus;
                reqBase.data.offset = data.offsetRefresh - 1;
                reqBase.data.startDate = data.tag;
                if (reqBase.data.offset == 0) {
                    mHasData = false;
                }

                mCall = Global.getNetEngine().obtainServerInfo(mUrl, reqBase);
                mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                        if (call.isCanceled()) {
                            return;
                        }
                        refreshFailed();
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            if (listener != null) {
                                mHasData = true;
                                listener.doCallBack(response.body().getData());
                            }
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                        if (call.isCanceled()) {
                            return;
                        }
                        refreshFailed();
                        ToastUtil.blurThrow(t);
                    }
                });
            }

            private void refreshFailed() {
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setRefreshing(false);
                mIsSwipeRefresh = mIsLoading = false;
            }
        });
    }


    /**
     * 刷新新数据
     */
    public void loadDataWithCallback(final AnchorTag data, final int focus, final
    CallbackListener<OneTypeDataList<ServerInfo>>
            listener) {
        // 判断是否处于加载刷新中
        mRefreshLayout.setCanShowLoad(true);
        mRefreshLayout.setLoading(true);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
        // 加载数据
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                // 判断网络情况
                if (!NetworkUtil.isConnected(getContext())) {
                    loadFailed();
                    return;
                }
                if (mCall != null) {
                    mCall.cancel();
                }

                // 设置请求对象的值
                ReqServerInfo serverInfo = new ReqServerInfo();
                JsonReqBase<ReqServerInfo> reqBase = new JsonReqBase<>(serverInfo);
                reqBase.data.isFocus = focus;
                reqBase.data.offset = data.offsetLoad + 1;
                reqBase.data.startDate = data.tag;

                mCall = Global.getNetEngine().obtainServerInfo(mUrl, reqBase);
                mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                        if (call.isCanceled()) {
                            return;
                        }
                        loadFailed();
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            if (listener != null) {
                                listener.doCallBack(response.body().getData());
                            }
                            return;
                        }
                        ToastUtil.blurErrorResp(response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                        if (call.isCanceled()) {
                            return;
                        }
                        loadFailed();
                        ToastUtil.blurThrow(t);
                    }
                });
            }

            private void loadFailed() {
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setLoading(false);
                mIsLoading = mIsLoadMore = false;
            }
        });
    }

    private void insertFirstReserve(ArrayList<ServerInfo> src, ArrayList<ServerInfo> extra) {
        ArrayList<ServerInfo> tmp = new ArrayList<>(extra.size());
        for (int i = extra.size() - 1; i >= 0; i--) {
            tmp.add(extra.get(i));
        }
        src.addAll(0, tmp);
    }

    private void tabCheck(int index) {
        for (int i = 0; i < mSortedTags.size(); i++) {
            mSortedTags.get(i).v.setChecked(i == index);
        }
    }

    private void handleTabLoad(int index) {
        final AnchorTag tag = mSortedTags.get(index);
        mRefreshLayout.setEnabled(false);

        // 进行数据刷新
        loadDataWithCallback(tag, 0, new CallbackListener<OneTypeDataList<ServerInfo>>() {
            @Override
            public void doCallBack(OneTypeDataList<ServerInfo> data) {
                tag.data.addAll(data.data);
                mAdapter.setData(tag.data);
                mAdapter.notifyItemRangeInserted(mAdapter.getData().size() - data.data.size(), data.data.size());
                tag.canLoad = data.isEndPage;
                tag.offsetLoad += 1;
                mRefreshLayout.setCanShowLoad(tag.canLoad);
            }
        });
    }

    private void handleTabRefresh(int index) {
        // 正常加载
        mRefreshLayout.setEnabled(false);
        final AnchorTag tag = mSortedTags.get(index);
        // 进行数据刷新
        refreshDataWithCallback(tag, 0, new CallbackListener<OneTypeDataList<ServerInfo>>() {
            @Override
            public void doCallBack(OneTypeDataList<ServerInfo> data) {
                // 数据倒着排，所以刷新时加到前面去
                if (data.data.isEmpty()) {
                    tag.canRefresh = false;
                }
                insertFirstReserve(tag.data, data.data);
                mAdapter.updateData(tag.data);
                tag.canLoad = data.isEndPage;
                tag.offsetRefresh -= 1;
                mRefreshLayout.setCanShowLoad(tag.canLoad);
            }
        });
    }

    /**
     * 处理Tab标签的点击事件
     */
    private void handleCheckClick(int index) {
        tabCheck(index);

        if (index == mCurIndex) {
            if (mIsLoading) {
                return;
            }
        } else {
            if (mCall != null) {
                mCall.cancel();
            }
        }
        final AnchorTag tag = mSortedTags.get(index);
        if (tag.data.isEmpty()) {
            // 进行数据刷新

            mRefreshLayout.setEnabled(false);
            handleTabRefresh(index);

        } else {
            if (mCurIndex != index) {
                // 滚动到第一条数据
                mAdapter.updateData(tag.data);
            }
            rvData.smoothScrollToPosition(0);
        }
        mCurIndex = index;
    }

    @Override
    public void doCallBack(Boolean data) {
        if (data) {
            if (AccountManager.getInstance().isLogin()) {
                initFocusData();
            } else {
                // 对于没有登录的，提示先登录才能显示关注
                ToastUtil.showShort(ConstString.TOAST_LOGIN_FIRST);
                AssistantApp.getInstance().setIsReadAttention(false);
                if (getActivity() instanceof ServerInfoActivity) {
                    ((ServerInfoActivity) getActivity()).setTbState(false);
                }
            }
        } else {
            llAnchors.setVisibility(View.VISIBLE);
            handleCheckClick(INDEX_TODAY);
        }
    }

    private void initFocusData() {
        llAnchors.setVisibility(View.GONE);
        mFocusReq = null;
        mRefreshLayout.setCanShowLoad(false);
        if (!mRefreshLayout.isRefreshing())
            mRefreshLayout.setRefreshing(true);
        refreshFocusData();
    }

    @Override
    public String getPageName() {
        return null;
    }
}
