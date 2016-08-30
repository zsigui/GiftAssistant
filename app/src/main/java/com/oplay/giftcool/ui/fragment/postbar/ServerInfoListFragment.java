package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.OpenServerAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetUrl;
import com.oplay.giftcool.model.data.req.ReqServerInfo;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.ServerInfo;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author JackieZhuang
 * @email zsigui@foxmail.com
 * @date 2016/8/30
 */
public class ServerInfoListFragment extends BaseFragment_Refresh<ServerInfo> {

    private RecyclerView rvData;
    private OpenServerAdapter mAdapter;
    private String mUrl;

    public static ServerInfoListFragment newInstance(int type, boolean isFocus, String startDate) {
        ServerInfoListFragment fragment = new ServerInfoListFragment();
        Bundle b = new Bundle();
        b.putInt(KeyConfig.KEY_TYPE, type);
        b.putBoolean(KeyConfig.KEY_DATA, isFocus);
        b.putString(KeyConfig.KEY_DATA_O, startDate);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_custome_rv_container);
        rvData = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {}

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            return;
        }
        int type = getArguments().getInt(KeyConfig.KEY_TYPE, KeyConfig.TYPE_ID_OPEN_SERVER);
        boolean isFocus = getArguments().getBoolean(KeyConfig.KEY_DATA, false);
        String startDate = getArguments().getString(KeyConfig.KEY_DATA_O, "");

        ReqServerInfo data = new ReqServerInfo();
        data.isFocus = (isFocus ? 1 : 0);
        data.startDate = startDate;
        mReqPageObj = new JsonReqBase<>(data);

        mUrl = (type == KeyConfig.TYPE_ID_OPEN_SERVER ? NetUrl.POST_GET_OPEN_SERVER : NetUrl.POST_GET_OPEN_TEST);
        mAdapter = new OpenServerAdapter(getContext(), type);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), llm.getOrientation());
        rvData.addItemDecoration(decoration);
        rvData.setLayoutManager(llm);
        rvData.setAdapter(mAdapter);

    }

    /**
     * 刷新首页数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<ServerInfo>>> mCallRefresh;
    private JsonReqBase<ReqServerInfo> mReqPageObj;


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
                if (!NetworkUtil.isConnected(getContext())) {
                    refreshFailEnd();
                    return;
                }
                if (mCallRefresh != null) {
                    mCallRefresh.cancel();
                }

                // 设置请求对象的值
                mReqPageObj.data.page = PAGE_FIRST;

                mCallRefresh = Global.getNetEngine().obtainServerInfo(mUrl, mReqPageObj);
                mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful()) {
                            if (response.body() != null && response.body().isSuccess()) {
                                // 获取数据成功
                                refreshSuccessEnd();
                                updateDate(response.body().getData().data);
                                return;
                            }
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                        refreshFailEnd();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                        refreshFailEnd();
                    }
                });
            }
        });
    }

    private void updateDate(ArrayList<ServerInfo> data) {
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
        if (mAdapter.getItemCount() < 5) {
            mNoMoreLoad = true;
            mRefreshLayout.setCanShowLoad(false);
        }
    }

    /**
     * 首页加载更多数据的网络请求声明
     */
    private Call<JsonRespBase<OneTypeDataList<ServerInfo>>> mCallLoad;

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
            mCallLoad = Global.getNetEngine().obtainServerInfo(mUrl, mReqPageObj);
            mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<ServerInfo>>>() {
                @Override
                public void onResponse(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call,
                                       Response<JsonRespBase<OneTypeDataList<ServerInfo>>> response) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    if (response != null && response.isSuccessful()) {
                        if (response.body() != null && response.body().isSuccess()) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<ServerInfo> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                    }
                    AppDebugConfig.warnResp(AppDebugConfig.TAG_FRAG, response);
                    moreLoadFailEnd();
                }

                @Override
                public void onFailure(Call<JsonRespBase<OneTypeDataList<ServerInfo>>> call, Throwable t) {
                    if (!mCanShowUI || call.isCanceled()) {
                        return;
                    }
                    AppDebugConfig.w(AppDebugConfig.TAG_FRAG, t);
                    moreLoadFailEnd();
                }
            });
        }
    }

    private void addMoreData(ArrayList<ServerInfo> moreData) {
        if (moreData == null) {
            return;
        }
        mAdapter.addMoreData(moreData);
        mLastPage += 1;
    }

    @Override
    public String getPageName() {
        return null;
    }
}
