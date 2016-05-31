package com.oplay.giftcool.ui.fragment.message;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MessageSystemAdapter;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.message.SystemMessage;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_Refresh;
import com.oplay.giftcool.util.NetworkUtil;
import com.socks.library.KLog;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-4-17.
 */
public class SystemMessageFragment extends BaseFragment_Refresh<SystemMessage> {

    private static final String TAG_PAGE = "系统消息界面";

    private RecyclerView rvContent;
    private MessageSystemAdapter mAdapter;
    private JsonReqBase<ReqPageData> mReqPageObj;

    private Call<JsonRespBase<OneTypeDataList<SystemMessage>>> mCallRefresh;
    private Call<JsonRespBase<OneTypeDataList<SystemMessage>>> mCallLoad;

    public static SystemMessageFragment newInstance() {
        return new SystemMessageFragment();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_rv_container);
        rvContent = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {


    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

        mAdapter = new MessageSystemAdapter(getContext());
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvContent.setLayoutManager(llm);
        rvContent.setAdapter(mAdapter);

        ReqPageData pageData = new ReqPageData();
        pageData.page = mLastPage;
        pageData.pageSize = 20;
        mReqPageObj = new JsonReqBase<>(pageData);
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    mReqPageObj.data.page = PAGE_FIRST;
                    if (mCallRefresh != null) {
                        mCallRefresh.cancel();
                    }
                    mCallRefresh = Global.getNetEngine().obtainSystemMessage(mReqPageObj);
                    mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<SystemMessage>>>() {
                        @Override
                        public void onResponse(Call<JsonRespBase<OneTypeDataList<SystemMessage>>> call,
                                               Response<JsonRespBase<OneTypeDataList<SystemMessage>>> response) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            if (response != null && response.isSuccessful() && response.body() != null &&
                                    response.body().getCode() == NetStatusCode.SUCCESS) {
                                refreshSuccessEnd();
                                OneTypeDataList<SystemMessage> backObj = response.body().getData();
                                refreshLoadState(backObj.data, backObj.isEndPage);
                                updateData(backObj.data);
                                return;
                            }
                            refreshFailEnd();
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<OneTypeDataList<SystemMessage>>> call, Throwable t) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            if (AppDebugConfig.IS_DEBUG) {
                                KLog.e(AppDebugConfig.TAG_FRAG, t);
                            }
                            refreshFailEnd();
                        }
                    });
                } else {
                    refreshFailEnd();
                }
            }
        });
    }

    /**
     * 加载更多数据
     */
    @Override
    protected void loadMoreData() {
        if (mNoMoreLoad || mIsLoadMore) {
            return;
        }
        mIsLoadMore = true;
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    moreLoadFailEnd();
                    return;
                }
                mReqPageObj.data.page = mLastPage + 1;
                if (mCallLoad != null) {
                    mCallLoad.cancel();
                }
                mCallLoad = Global.getNetEngine().obtainSystemMessage(mReqPageObj);
                mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<SystemMessage>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<SystemMessage>>> call,
                                           Response<JsonRespBase<OneTypeDataList<SystemMessage>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<SystemMessage> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                        moreLoadFailEnd();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<SystemMessage>>> call, Throwable t) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        moreLoadFailEnd();
                    }
                });
            }
        });

    }


    /**
     * 刷新当前数据
     */
    public void updateData(ArrayList<SystemMessage> data) {
        if (data == null || data.size() == 0) {
            mViewManager.showEmpty();
            Global.updateMsgCentralData(getContext(), KeyConfig.CODE_MSG_SYSTEM, 0, null);
            return;
        }
        Global.updateMsgCentralData(getContext(), KeyConfig.CODE_MSG_SYSTEM, 0, data.get(0).content);
//		AccountManager.getInstance().obtainUnreadPushMessageCount();
        mViewManager.showContent();
        mHasData = true;
        mData = data;
        mAdapter.updateData(mData);
        mLastPage = PAGE_FIRST;
    }

    /**
     * 添加更多数据
     */
    private void addMoreData(ArrayList<SystemMessage> moreData) {
        if (moreData == null) {
            return;
        }
        mAdapter.addMoreData(moreData);
        mLastPage += 1;
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
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }
    }

    @Override
    public String getPageName() {
        return TAG_PAGE;
    }
}
