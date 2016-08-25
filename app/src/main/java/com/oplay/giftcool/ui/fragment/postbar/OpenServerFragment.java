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


    private final String STR_REFRESH_NEWSET = "当前已是最新数据";
    private final String STR_LOAD_OLDER = "无更多数据可加载";
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
    private LinearLayoutManager llm;
    private ArrayList<AnchorTag> mSortedTags;
    private int mType;
    private String mUrl;

    private static class AnchorTag {
        String tag = "";
        int anchor;
        CheckedTextView v;
        ArrayList<ServerInfo> data = new ArrayList<>();
        // 是否有更旧的数据，对于昨/今/明/以后则切换前一个tab
        boolean canLoad;
        // 是否有更新的数据，对于更早/昨/今/明则切换后一个tab
        boolean hasNew;
        // 保存当前页码
        int offset = 0;
        // 保存锚点时间
        String anchorTime = "";

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
        rvData.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItem = llm.findFirstVisibleItemPosition();
                if (mSortedTags != null) {
                    for (int i = 0; i < mSortedTags.size(); i++) {
                        if (firstItem >= mSortedTags.get(i).anchor
                                && (mSortedTags.size() == i + 1 || firstItem < mSortedTags.get(i + 1).anchor)) {
                            mSortedTags.get(i).v.setChecked(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            return;
        }
        mType = getArguments().getInt(KeyConfig.KEY_DATA, KeyConfig.TYPE_ID_OPEN_SERVER);
        mUrl = (mType == KeyConfig.TYPE_ID_OPEN_SERVER ? NetUrl.POST_GET_OPEN_SERVER : NetUrl.POST_GET_OPEN_TEST);
        mAdapter = new OpenServerAdapter(getContext(), mType);
        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
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

    JsonReqBase<ReqServerInfo> mReqData;
    Call<JsonRespBase<OneTypeDataList<ServerInfo>>> mCall;

    @Override
    protected void lazyLoad() {
        if (mIsLoading) {
            return;
        }
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                refreshInitConfig();
                // 判断网络情况
//        if (!NetworkUtil.isConnected(getContext())) {
//            refreshFailEnd();
//            return;
//        }
                if (mCall != null) {
                    mCall.cancel();
                }

                // 设置请求对象的值
                if (mReqData == null) {
                    ReqServerInfo data = new ReqServerInfo();
                    mReqData = new JsonReqBase<>(data);
                }

                mCall = Global.getNetEngine().obtainServerInfo(mUrl, mReqData);
                mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {

                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {

                    }
                });
            }
        });
    }

    public void doCheck(int index) {
        for (int i = 0; i < mSortedTags.size(); i++) {
            mSortedTags.get(i).v.setChecked(i == index);
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


    JsonReqBase<ReqServerInfo> mFocusReq;
    int mFocusLoadIndex = 0;
    int mFocusRefreshIndex = 0;

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
            // 刷新则跳转到下一Tab，已是最后tab则正常加载
            int index = getCheckedAnchorIndex();
            if (index != INDEX_AFTER && mSortedTags.get(index).hasNew) {
                // 跳转下一个TAB
                doCheck(index + 1);
                if (mSortedTags.get(index + 1).data != null) {
                    mAdapter.updateData(mSortedTags.get(index + 1).data);
                    rvData.scrollToPosition(mAdapter.getItemCount() - 1);
                    mRefreshLayout.setEnabled(true);
                    mIsSwipeRefresh = false;
                } else {
                    // 下一标签无数据，执行类网络加载操作
                    refreshData(mSortedTags.get(index + 1), false, null);
                }
            } else {
                // 正常加载
                refreshData(mSortedTags.get(index), false, null);
            }
        }
    }

    private void refreshFocusData() {
        if (mCall != null) {
            mCall.cancel();
        }

        // 设置请求对象的值
        if (mFocusReq == null) {
            mFocusRefreshIndex = 0;
            ReqServerInfo serverInfo = new ReqServerInfo();
            serverInfo.isFocus = true;
            serverInfo.offset = mFocusRefreshIndex;
            mFocusReq = new JsonReqBase<>(serverInfo);
        } else {
            mFocusRefreshIndex = mFocusRefreshIndex + 1;
            mFocusReq.data.offset = mFocusRefreshIndex;
        }

        mCall = Global.getNetEngine().obtainServerInfo(mUrl, mFocusReq);
        mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
            @Override
            public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                   Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                if (call.isCanceled()) {
                    return;
                }
                mIsSwipeRefresh = false;
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setRefreshing(false);
                if (response != null && response.isSuccessful()
                        && response.body() != null && response.body().isSuccess()) {
                    OneTypeDataList<ServerInfo> data = response.body().getData();
                    if (!data.data.isEmpty()) {
                        mAdapter.getData().addAll(0, data.data);
                        mAdapter.notifyItemRangeInserted(0, data.data.size());
                    }
                    if (data.isEndPage) {
                        // 已是最新
                        ToastUtil.showShort(STR_REFRESH_NEWSET);
                    }
                    if (mFocusRefreshIndex == 0) {
                        mRefreshLayout.setCanShowLoad(true);
                    }
                    return;
                }
                mFocusRefreshIndex -= 1;
            }

            @Override
            public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                ToastUtil.blurThrow(t);
                mIsSwipeRefresh = false;
                mRefreshLayout.setEnabled(true);
                mRefreshLayout.setRefreshing(false);
                if (mFocusRefreshIndex == 0) {
                    mRefreshLayout.setCanShowLoad(false);
                }
                mFocusRefreshIndex -= 1;
            }
        });
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
            // 加载到无数据则跳转到上一Tab，已是最后tab则结束
            int index = getCheckedAnchorIndex();
            if (index != INDEX_BEFORE && !mSortedTags.get(index).canLoad) {
                // 跳转上一个TAB
                doCheck(index - 1);
                if (mSortedTags.get(index - 1).data != null) {
                    mAdapter.updateData(mSortedTags.get(index - 1).data);
                    rvData.scrollToPosition(0);
                    mRefreshLayout.setEnabled(true);
                    mNoMoreLoad = !mSortedTags.get(index - 1).canLoad;
                    mIsLoadMore = false;
                } else {
                    // 上一标签无数据，执行类网络加载操作
                    //refreshData(mSortedTags.get(index - 1), false, null);
                }
            } else {
                // 正常加载
                //refreshData(mSortedTags.get(index), false, null);
            }
        }
    }

    private void loadFocusData() {
        if (mCall != null) {
            mCall.cancel();
        }

        // 设置请求对象的值
        if (mFocusReq == null) {
            mFocusLoadIndex = 0;
            ReqServerInfo serverInfo = new ReqServerInfo();
            serverInfo.isFocus = true;
            serverInfo.offset = mFocusLoadIndex;
            mFocusReq = new JsonReqBase<>(serverInfo);
        } else {
            mFocusLoadIndex = mFocusLoadIndex - 1;
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
                mIsLoadMore = false;
                mRefreshLayout.setLoading(false);
                if (response != null && response.isSuccessful()
                        && response.body() != null && response.body().isSuccess()) {
                    OneTypeDataList<ServerInfo> data = response.body().getData();
                    if (!data.data.isEmpty()) {
                        mAdapter.getData().addAll(data.data);
                        mAdapter.notifyItemRangeInserted(mAdapter.getData().size() - data.data.size(), data.data.size());
                    }
                    if (data.isEndPage) {
                        // 已是最新
                        ToastUtil.showShort(STR_LOAD_OLDER);
                    }
                    mNoMoreLoad = data.isEndPage;
                    return;
                }
                mFocusLoadIndex += 1;
            }

            @Override
            public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }
                ToastUtil.blurThrow(t);
                mIsLoadMore = false;
                mRefreshLayout.setLoading(false);
                mFocusLoadIndex -= 1;
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_before:
                doCheck(INDEX_BEFORE);
                mRefreshLayout.setLoading(true);
                break;
            case R.id.tv_yesterday:
                doCheck(INDEX_YESTERDAY);
                mRefreshLayout.setLoading(true);
                break;
            case R.id.tv_today:
                doCheck(INDEX_TODAY);
                // 由于today默认首次展示，所以默认不影响
                rvData.smoothScrollToPosition(mSortedTags.get(INDEX_TODAY).anchor);
                break;
            case R.id.tv_tomorrow:
                doCheck(INDEX_TOMORROW);
                break;
            case R.id.tv_after:
                doCheck(INDEX_AFTER);
                // 判断是否已经加载了数据
                final AnchorTag after = mSortedTags.get(INDEX_AFTER);
                refreshData(after, false, new CallbackListener<OneTypeDataList<ServerInfo>>() {

                    @Override
                    public void doCallBack(OneTypeDataList<ServerInfo> data) {
                        after.data.addAll(0, data.data);
                        after.offset += 1;
                        after.hasNew = !data.isEndPage;
                        mAdapter.updateData(after.data);
                    }
                });
                break;
        }
    }


    /**
     * 刷新新数据
     */
    public void refreshData(final AnchorTag data, final boolean focus, final
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
                refreshInitConfig();
                // 判断网络情况
                if (!NetworkUtil.isConnected(getContext())) {
                    refreshFailEnd();
                    return;
                }
                if (mCall != null) {
                    mCall.cancel();
                }

                // 设置请求对象的值
                ReqServerInfo serverInfo = new ReqServerInfo();
                JsonReqBase<ReqServerInfo> reqBase = new JsonReqBase<>(serverInfo);
                reqBase.data.isFocus = focus;
                reqBase.data.offset = data.offset + 1;
                reqBase.data.startDate = data.tag;

                mCall = Global.getNetEngine().obtainServerInfo(mUrl, reqBase);
                mCall.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                        if (call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            if (listener != null) {
                                listener.doCallBack(response.body().getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                        if (call.isCanceled()) {
                            return;
                        }
                        ToastUtil.blurThrow(t);
                    }
                });
            }
        });
    }


    @Override
    public void doCallBack(Boolean data) {
        if (mCall != null) {
            mCall.cancel();
        }
        if (data) {
            llAnchors.setVisibility(View.GONE);
            mFocusReq = null;
            mRefreshLayout.setCanShowLoad(false);
            if (!mRefreshLayout.isRefreshing())
                mRefreshLayout.setRefreshing(true);
        } else {
            llAnchors.setVisibility(View.VISIBLE);
            doCheck(INDEX_TODAY);
            mAdapter.updateData(mSortedTags.get(INDEX_TODAY).data);
            mRefreshLayout.setLoading(false);
            mRefreshLayout.setCanShowLoad(mSortedTags.get(INDEX_TODAY).canLoad);
            // 定位到今天
        }
    }

    @Override
    public String getPageName() {
        return null;
    }
}
