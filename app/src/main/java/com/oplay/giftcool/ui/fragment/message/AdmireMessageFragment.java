package com.oplay.giftcool.ui.fragment.message;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.MessageReplyAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.NetStatusCode;
import com.oplay.giftcool.model.data.req.ReqChangeMessageStatus;
import com.oplay.giftcool.model.data.req.ReqPageData;
import com.oplay.giftcool.model.data.resp.OneTypeDataList;
import com.oplay.giftcool.model.data.resp.message.ReplyMessage;
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
 * 收到的赞页面
 * <p/>
 * Created by zsigui on 16-4-17.
 */
public class AdmireMessageFragment extends BaseFragment_Refresh<ReplyMessage> {


    private RecyclerView rvData;
    private MessageReplyAdapter mAdapter;

    private JsonReqBase<ReqPageData> mReqPageObj;
    private Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> mCallRefresh;
    private Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> mCallLoad;
    /**
     * 请求的地址
     */
    private String mUrl;

    public static AdmireMessageFragment newInstance() {
        AdmireMessageFragment fragment = new AdmireMessageFragment();
//		Bundle bundle = new Bundle();
//		bundle.putBoolean(KeyConfig.KEY_STATUS, isComment);
//		bundle.putString(KeyConfig.KEY_URL, url);
//		fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_refresh_rv_container_with_white_bg);
        rvData = getViewById(R.id.lv_content);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
//		if (getArguments() == null) {
//			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
//			return;
//		}
//		mUrl = getArguments().getString(KeyConfig.KEY_URL);
//		final boolean mIsComment = getArguments().getBoolean(KeyConfig.KEY_STATUS);

        mAdapter = new MessageReplyAdapter(getContext(), mData);
        mAdapter.setIsComment(false);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), llm.getOrientation());
        itemDecoration.setWriteBottom(true);
        rvData.setLayoutManager(llm);
        rvData.addItemDecoration(itemDecoration);
        rvData.setAdapter(mAdapter);
    }

    private void initReqPage() {
        if (mReqPageObj == null) {
            ReqPageData page = new ReqPageData();
            page.page = PAGE_FIRST;
            page.pageSize = 10;
            // 评论为0，点赞为1
            page.type = 1;
            mReqPageObj = new JsonReqBase<>(page);
        }
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (NetworkUtil.isConnected(getContext())) {
                    initReqPage();
                    mReqPageObj.data.page = PAGE_FIRST;
                    if (mCallRefresh != null) {
                        mCallRefresh.cancel();
                    }
                    mCallRefresh = Global.getNetEngine().obtainReplyMessage(mReqPageObj);
                    mCallRefresh.enqueue(new Callback<JsonRespBase<OneTypeDataList<ReplyMessage>>>() {
                        @Override
                        public void onResponse(Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> call,
                                               Response<JsonRespBase<OneTypeDataList<ReplyMessage>>> response) {
                            if (!mCanShowUI || call.isCanceled()) {
                                return;
                            }
                            if (response != null && response.isSuccessful() && response.body() != null &&
                                    response.body().getCode() == NetStatusCode.SUCCESS) {
                                refreshSuccessEnd();
                                OneTypeDataList<ReplyMessage> backObj = response.body().getData();
                                refreshLoadState(backObj.data, backObj.isEndPage);
                                updateData(backObj.data);
                                return;
                            }
                            refreshFailEnd();
                        }

                        @Override
                        public void onFailure(Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> call, Throwable t) {
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
                initReqPage();
                mReqPageObj.data.page = mLastPage + 1;
                if (mCallLoad != null) {
                    mCallLoad.cancel();
                }
                mCallLoad = Global.getNetEngine().obtainReplyMessage(mReqPageObj);
                mCallLoad.enqueue(new Callback<JsonRespBase<OneTypeDataList<ReplyMessage>>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> call,
                                           Response<JsonRespBase<OneTypeDataList<ReplyMessage>>> response) {
                        if (!mCanShowUI || call.isCanceled()) {
                            return;
                        }
                        if (response != null && response.isSuccessful() && response.body() != null &&
                                response.body().getCode() == NetStatusCode.SUCCESS) {
                            moreLoadSuccessEnd();
                            OneTypeDataList<ReplyMessage> backObj = response.body().getData();
                            setLoadState(backObj.data, backObj.isEndPage);
                            addMoreData(backObj.data);
                            return;
                        }
                        moreLoadFailEnd();
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<OneTypeDataList<ReplyMessage>>> call, Throwable t) {
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
    public void updateData(ArrayList<ReplyMessage> data) {
        if (data == null || data.size() == 0) {
            Global.updateMsgCentralData(getContext(), KeyConfig.CODE_MSG_ADMIRE, 0, null);
            mViewManager.showEmpty();
            return;
        }
        final ReplyMessage msg = data.get(0);
        final String s = String.format(getResources().getString(R.string.st_msg_central_get_a_admire), msg.name);
        Global.updateMsgCentralData(getContext(), KeyConfig.CODE_MSG_ADMIRE, 0, s);
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
    private void addMoreData(ArrayList<ReplyMessage> moreData) {
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
    public void onDestroyView() {
        super.onDestroyView();
        notifyAllRead();
    }

    private void notifyAllRead() {
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                JsonReqBase<ReqChangeMessageStatus> reqData = new JsonReqBase<ReqChangeMessageStatus>();
                Global.getNetEngine().changePushMessageStatus(reqData)
                        .enqueue(new Callback<JsonRespBase<Void>>() {
                            @Override
                            public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>>
                                    response) {
                                if (call.isCanceled()) {
                                    return;
                                }
                                if (response != null && response.isSuccessful()) {
                                    if (response.body() != null && response.body().isSuccess()) {
                                        if (AppDebugConfig.IS_DEBUG) {
                                            KLog.d(AppDebugConfig.TAG_FRAG, "修改成功");
                                        }
                                        return;
                                    }
                                    if (AppDebugConfig.IS_DEBUG) {
                                        KLog.d(AppDebugConfig.TAG_FRAG, response.body() == null ? "解析出错" :
                                                response.body().error());
                                    }
                                    return;
                                }
                                if (AppDebugConfig.IS_DEBUG) {
                                    KLog.d(AppDebugConfig.TAG_FRAG, response == null ? "服务器出错" :
                                            response.code() + ":" + response.body());
                                }
                            }

                            @Override
                            public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                                KLog.d(AppDebugConfig.TAG_FRAG, t);
                            }
                        });
            }
        });
    }

    @Override
    public String getPageName() {
        return "收到的赞";
    }

}
