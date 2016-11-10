package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.CommentDetailAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.manager.ObserverManager;
import com.oplay.giftcool.model.data.req.ReqPostToken;
import com.oplay.giftcool.model.data.resp.CommentDetail;
import com.oplay.giftcool.model.data.resp.CommentDetailListItem;
import com.oplay.giftcool.model.data.resp.PostToken;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.CommentDetailActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.WithFooterRecyclerView;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.RequestUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-11-7.
 */

public class CommentDetailFragment extends BaseFragment implements WithFooterRecyclerView.OnLoadMoreListener, View
        .OnTouchListener, TextWatcher, OnItemClickListener<CommentDetailListItem> {


    final int PAGE_SIZE = 10;
    static final String KEY_POST = "key_id_post";
    static final String KEY_COMMENT = "key_id_comment";

    private final String KEY_POST_ID = "activity_id";
    private final String KEY_COMMENT_ID = "comment_id";
    private final String KEY_CUID = "cuid";
    private final String KEY_TOKEN = "token";
    private final String KEY_CONTENT = "content";

    private CommentDetailAdapter mAdapter;
    private WithFooterRecyclerView rvContent;
    private EditText etContent;
    private TextView tvSend;
    private int mPostId;
    private int mCommentId;
    private int mLoginUid;

    private NoEncryptEngine mEngine;
    private int mPage = 1;
    private boolean mIsLoadMore = false;
    private CommentDetail mDetailData;

    /**
     * 请求参数字典
     */
    private HashMap<String, Object> reqData = new HashMap<>();
    private ReqPostToken reqToken = new ReqPostToken();

    public static CommentDetailFragment newInstance(int postId, int commentId) {
        CommentDetailFragment fragment = new CommentDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_POST, postId);
        bundle.putInt(KEY_COMMENT, commentId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_comment_detail);
        rvContent = getViewById(R.id.rv_comment);
        etContent = getViewById(R.id.et_content);
        tvSend = getViewById(R.id.tv_send);
//        llBackground = getViewById(R.id.ll_img_preview);
    }

    @Override
    protected void setListener() {
        rvContent.setOnLoadListener(this);
        rvContent.setOnTouchListener(this);
        etContent.setOnTouchListener(this);
        tvSend.setOnClickListener(this);
        etContent.addTextChangedListener(this);
        ObserverManager.getInstance().addUserUpdateListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            getActivity().onBackPressed();
            return;
        }
        mPostId = getArguments().getInt(KEY_POST);
        mCommentId = getArguments().getInt(KEY_COMMENT);
        // 设置发送评论的数据
        reqToken.postId = mPostId;
        reqData.put(KEY_POST_ID, mPostId);
        reqData.put(KEY_COMMENT_ID, mCommentId);
        mLoginUid = AccountManager.getInstance().isLogin() ? AccountManager.getInstance().getUserInfo().uid : 0;
        setReplyTo(mCommentId, null);

        mAdapter = new CommentDetailAdapter(getContext());
        mAdapter.setListener(this);
        rvContent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvContent.setViewFooterNoMore(R.layout.xml_null);
        rvContent.setViewFooterLoading(R.layout.view_item_footer);
        rvContent.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rvContent.setAdapter(mAdapter);

        // 设置右上角的查看本帖
        CommentDetailActivity p = (CommentDetailActivity) getActivity();
        p.setPostId(mPostId);
        p.showRightBtn(View.VISIBLE, ConstString.TEXT_POST_BTN);
        mEngine = Global.getNoEncryptEngine();
    }

    /**
     * 设置评论框选择回复的对象
     *
     * @param atNick 为空表示不指定回复对象
     */
    private void setReplyTo(int commentId, String atNick) {
        reqData.put(KEY_COMMENT_ID, commentId);
        if (TextUtils.isEmpty(atNick)) {
            etContent.setHint("回复:");
        } else {
            etContent.setHint(String.format(Locale.CHINA, "回复%s:", atNick));
        }
        etContent.requestFocus();
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();
        if (!NetworkUtil.isAvailable(getContext())) {
            refreshFailEnd();
            return;
        }
        mPage = 1;
        mEngine.obtainCommentDetail(mPostId, mCommentId, mPage, PAGE_SIZE)
                .enqueue(new Callback<JsonRespBase<CommentDetail>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<CommentDetail>> call,
                                           Response<JsonRespBase<CommentDetail>> response) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }

                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            refreshSuccessEnd();
                            updateData(response.body().getData());
                            return;
                        }
                        refreshFailEnd();
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<CommentDetail>> call, Throwable t) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        refreshFailEnd();
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                    }
                });
    }

    private void updateData(CommentDetail data) {
        if (data == null) {
            mViewManager.showEmpty();
            return;
        }

        if (data.listInfo == null || data.listInfo.size() < 10) {
            rvContent.setLoadState(WithFooterRecyclerView.STATE_NO_MORE);
        } else {
            rvContent.setLoadState(WithFooterRecyclerView.STATE_NONE);
        }

        mDetailData = data;
        mAdapter.setDetailData(data);
        mAdapter.updateData(data.listInfo);
        mPage = 2;

        if (PostDetailNewFragment.sPostDetailInstance != null) {
            PostDetailNewFragment.sPostDetailInstance.updateItemData(mDetailData);
        }
    }


    @Override
    public void onLoadMore() {
        if (mIsLoadMore) {
            return;
        }
        mIsLoadMore = true;
        mEngine.obtainCommentDetail(mPostId, mCommentId, mPage, PAGE_SIZE)
                .enqueue(new Callback<JsonRespBase<CommentDetail>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<CommentDetail>> call,
                                           Response<JsonRespBase<CommentDetail>> response) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        mIsLoadMore = false;
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            CommentDetail list = response.body().getData();
                            addMoreData(list.listInfo);
                            return;
                        }
                        rvContent.setLoadState(WithFooterRecyclerView.STATE_ERROR);
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<CommentDetail>> call, Throwable t) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        mIsLoadMore = false;
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                        rvContent.setLoadState(WithFooterRecyclerView.STATE_ERROR);
                    }
                });
    }

    private void addMoreData(ArrayList<CommentDetailListItem> items) {
        if (items == null || items.size() < PAGE_SIZE) {
            rvContent.setLoadState(WithFooterRecyclerView.STATE_NO_MORE);
        } else {
            rvContent.setLoadState(WithFooterRecyclerView.STATE_NONE);
        }
        if (items != null && items.size() > 0) {
            mAdapter.addMoreData(items);
            mPage++;
        }
    }

    @Override
    public void release() {
        super.release();
        if (getActivity() != null) {
            CommentDetailActivity p = (CommentDetailActivity) getActivity();
            p.showRightBtn(View.GONE, ConstString.TEXT_POST_BTN);
        }
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }
        mDetailData = null;
    }

    private long mLastClickTime = 0;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_send:
                long curTime = System.currentTimeMillis();
                if (curTime - mLastClickTime < Global.CLICK_TIME_INTERVAL) {
                    mLastClickTime = curTime;
                    return;
                }
                handleSend();
                break;
        }
    }


    /**
     * 执行发表回复的网络请求
     */
    private Call<JsonRespBase<Void>> mCallPost;
    private Call<JsonRespBase<PostToken>> mCallGetToken;

    /**
     * 执行发送消息服务
     */
    private void handleSend() {
        if (!AccountManager.getInstance().isLogin()) {
            IntentUtil.jumpLogin(getContext());
            return;
        }
        if (!NetworkUtil.isConnected(getContext())) {
            ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
            return;
        }
        showLoading();
        if (mCallGetToken == null) {
            mCallGetToken = Global.getNetEngine().obtainReplyToken(new JsonReqBase<>(reqToken));
        } else {
            mCallGetToken.cancel();
            mCallGetToken = mCallGetToken.clone();
        }
        mCallGetToken.enqueue(new Callback<JsonRespBase<PostToken>>() {
            @Override
            public void onResponse(Call<JsonRespBase<PostToken>> call, Response<JsonRespBase<PostToken>>
                    response) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                if (response != null && response.isSuccessful()
                        && response.body() != null && response.body().isSuccess()) {
                    handleCommit(response.body().getData().token);
                    return;
                }
                hideLoading();
                ToastUtil.showShort(ConstString.TOAST_GET_TOKEN_FAILED);
            }

            @Override
            public void onFailure(Call<JsonRespBase<PostToken>> call, Throwable t) {
                if (!mCanShowUI || call.isCanceled()) {
                    return;
                }
                hideLoading();
            }
        });
    }

    /**
     * 执行token获取后的提交操作
     */
    private void handleCommit(final String token) {
        if (mCallPost != null) {
            mCallPost.cancel();
        }
        reqData.put(KEY_TOKEN, token);
        try {
            reqData.put(KEY_CONTENT, URLEncoder.encode(etContent.getText().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            AppDebugConfig.w(AppDebugConfig.TAG_FRAG, e);
        }
        reqData.put(KEY_CUID, AccountManager.getInstance().getUserInfo().uid);
        mCallPost = mEngine.commitReply(evaluateBody(reqData));
        mCallPost.enqueue(new Callback<JsonRespBase<Void>>() {
            @Override
            public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
                if (!mCanShowUI && call.isCanceled()) {
                    hideLoading();
                    return;
                }
                if (response != null && response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        refreshAfterPost();
                        return;
                    }
                }
                hideLoading();
                ToastUtil.blurErrorResp(response);
            }

            @Override
            public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
                hideLoading();
                ToastUtil.blurThrow(t);
            }
        });
    }

    /**
     * 在提交回复后执行刷新UI操作
     */
    private void refreshAfterPost() {
        ToastUtil.showShort(ConstString.TOAST_COMMENT_SUCCESS);
        etContent.setText("");
        InputMethodUtil.hideSoftInput(getActivity());
//        hideLoading();

        mEngine.obtainCommentDetail(mPostId, mCommentId, 1, PAGE_SIZE)
                .enqueue(new Callback<JsonRespBase<CommentDetail>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<CommentDetail>> call,
                                           Response<JsonRespBase<CommentDetail>> response) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            CommentDetail detail = response.body().getData();
                            if (detail != null) {
                                updateData(detail);
                                rvContent.smoothScrollToPosition(0);
                            }
                            return;
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<CommentDetail>> call, Throwable t) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        hideLoading();
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                    }
                });

    }

    /**
     * 根据HashMap的键值对构建Http请求实体
     */
    private String evaluateBody(HashMap<String, Object> reqData) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Object> entry : reqData.entrySet()) {
            try {
                b.append(entry.getKey()).append("=").append(URLEncoder.encode(String.valueOf(entry.getValue())
                        , "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                AppDebugConfig.w(AppDebugConfig.TAG_FRAG, e);
            }
        }
        if (b.length() > 0) {
            b.deleteCharAt(b.length() - 1);
        }
        return b.toString();
    }


    private void showLoading() {
        DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
    }

    private void hideLoading() {
        DialogManager.getInstance().hideLoadingDialog();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.et_content:
                InputMethodUtil.showSoftInput(getActivity());
                etContent.requestFocus();
                break;
            case R.id.rv_comment:
                InputMethodUtil.hideSoftInput(getActivity());
                etContent.clearFocus();
                break;
        }
        return false;
    }

//    private void resetLayoutParams() {
//        if (llBackground != null) {
//            ViewGroup.LayoutParams lp = llBackground.getLayoutParams();
//            lp.height = mLastSoftInputHeight;
//            llBackground.setLayoutParams(lp);
//        }
//    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().trim().length() > 0) {
            tvSend.setEnabled(true);
        } else {
            tvSend.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onUserUpdate(int action) {
        switch (action) {
            case ObserverManager.STATUS.USER_UPDATE_ALL:
                mLoginUid = AccountManager.getInstance().isLogin() ?
                        AccountManager.getInstance().getUserInfo().uid : 0;
                setReplyTo(mCommentId, "");
                break;
        }
    }

    @Override
    public String getPageName() {
        return "评论详情";
    }

    @Override
    public void onItemClick(CommentDetailListItem item, View view, int position) {
        if (position != 0 && item != null && item.userId != mLoginUid) {
            setReplyTo(item.id, item.nick);
        } else {
            setReplyTo(mCommentId, "");
        }
        switch (view.getId()) {
            case R.id.tv_comment:
                InputMethodUtil.showSoftInput(getActivity());
                break;
            case R.id.tv_admire:
                mDetailData.commentInfo.likeCount += (mDetailData.commentInfo.isLike? -1 : 1);
                mDetailData.commentInfo.isLike = !mDetailData.commentInfo.isLike;
                mAdapter.notifyItemChanged(position);
                RequestUtil.putAdmireState(mPostId, mCommentId, mDetailData.commentInfo.isLike);
                break;
        }
    }
}
