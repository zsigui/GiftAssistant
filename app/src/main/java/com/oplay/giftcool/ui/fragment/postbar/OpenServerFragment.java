package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.OpenServerAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.listener.CallbackListener;
import com.oplay.giftcool.model.data.req.ReqServerInfo;
import com.oplay.giftcool.model.data.resp.ServerInfo;
import com.oplay.giftcool.model.data.resp.TimeData;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.ServerInfoActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.ThreadUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-8-24.
 */
public class OpenServerFragment extends BaseFragment_Refresh<ServerInfo> implements CallbackListener<Boolean> {

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

    private RecyclerView rvData;
    private OpenServerAdapter mAdapter;
    private LinearLayoutManager llm;
    private ArrayList<AnchorTag> mSortedTags;
    private int mType;

    private static class AnchorTag {
        String tag = "";
        int anchor;
        CheckedTextView v;
        // 指示当前下标是否需要继续执行
        boolean needLoad = true;

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
                return ((AnchorTag)o).tag.equalsIgnoreCase(tag);
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
        mAdapter = new OpenServerAdapter(getContext(), mType);
        llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        rvData.addItemDecoration(decoration);
        rvData.setLayoutManager(llm);
        rvData.setAdapter(mAdapter);
        mSortedTags = new ArrayList<>(5);
        mSortedTags.add(new AnchorTag("更早", 0, tvBefore));
        mSortedTags.add(new AnchorTag("昨天", 0, tvYesterday));
        mSortedTags.add(new AnchorTag("今天", 0, tvToday));
        mSortedTags.add(new AnchorTag("明天", 0, tvTomorrow));
        mSortedTags.add(new AnchorTag("以后", 0, tvAfter));
    }

    JsonReqBase<ReqServerInfo> mReqData;
    Call<JsonRespBase<TimeData<ServerInfo>>> mCall;

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
                    data.type = mType;
                    mReqData = new JsonReqBase<>(data);
                }

                mCall = Global.getNetEngine().obtainServerInfo(mReqData);
                mCall.enqueue(new Callback<JsonRespBase<TimeData<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<TimeData<ServerInfo>>> call,
                                           Response<JsonRespBase<TimeData<ServerInfo>>> response) {

                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<TimeData<ServerInfo>>> call, Throwable t) {

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


    @Override
    public void onRefresh() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(false);
        }
        if (mIsSwipeRefresh || mIsLoading) {
            return;
        }
        mIsSwipeRefresh = true;

    }

    @Override
    protected void loadMoreData() {
        super.loadMoreData();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_before:
                doCheck(INDEX_BEFORE);
                mRefreshLayout.setLoading(true);
                ThreadUtil.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ThreadUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshLayout.setLoading(false);
                            }
                        });
                    }
                });
                break;
            case R.id.tv_yesterday:
                doCheck(INDEX_YESTERDAY);
                mRefreshLayout.setLoading(true);
                ThreadUtil.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ThreadUtil.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshLayout.setLoading(false);
                            }
                        });
                    }
                });
                break;
            case R.id.tv_today:
                doCheck(INDEX_TODAY);
                // 由于today默认首次展示，所以默认不影响
                rvData.smoothScrollToPosition(mSortedTags.get(INDEX_TODAY).anchor);
                break;
            case R.id.tv_tomorrow:
                doCheck(INDEX_TOMORROW);
                if (mSortedTags.get(INDEX_TOMORROW).needLoad && !mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.setRefreshing(true);
                    ThreadUtil.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ThreadUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    });
                } else {
                    rvData.smoothScrollToPosition(mSortedTags.get(INDEX_TOMORROW).anchor);
                }
                break;
            case R.id.tv_after:
                doCheck(INDEX_AFTER);
                if (mSortedTags.get(INDEX_AFTER).needLoad && !mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.setRefreshing(true);
                    ThreadUtil.runInThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ThreadUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    });
                } else {
                    rvData.smoothScrollToPosition(mSortedTags.get(INDEX_AFTER).anchor);
                }
                break;
        }
    }


    @Override
    public void doCallBack(Boolean data) {
        ToastUtil.showShort("回调事件：" + data);
    }

    @Override
    public String getPageName() {
        return "开服表界面";
    }
}
