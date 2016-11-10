package com.oplay.giftcool.ui.fragment.postbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostDetailAdapter;
import com.oplay.giftcool.adapter.PostReplyAdapter;
import com.oplay.giftcool.adapter.itemdecoration.DividerItemDecoration;
import com.oplay.giftcool.adapter.layoutmanager.SnapLinearLayoutManager;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.listener.OnBackPressListener;
import com.oplay.giftcool.listener.OnItemClickListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.req.ReqPostToken;
import com.oplay.giftcool.model.data.resp.CommentDetail;
import com.oplay.giftcool.model.data.resp.CommentDetailInfo;
import com.oplay.giftcool.model.data.resp.CommentDetailListItem;
import com.oplay.giftcool.model.data.resp.PostCommentList;
import com.oplay.giftcool.model.data.resp.PostDetail;
import com.oplay.giftcool.model.data.resp.PostToken;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.PostDetailNewActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment;
import com.oplay.giftcool.ui.widget.WithFooterRecyclerView;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.EmotionInputDetector;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.RequestUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.oplay.giftcool.util.ViewUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-11-3.
 */

public class PostDetailNewFragment extends BaseFragment implements WithFooterRecyclerView.OnLoadMoreListener,
        OnItemClickListener<CommentDetail>, TextWatcher, View.OnTouchListener,
        PostReplyAdapter.OnPickListener, OnBackPressListener {

    public static PostDetailNewFragment sPostDetailInstance;
    final int PAGE_SIZE = 20;

    private final String KEY_CUID = "cuid";
    private final String KEY_TOKEN = "token";
    private final String KEY_POST_ID = "activity_id";
    private final String KEY_CONTENT = "content";
    private final String KEY_IMGS = "imgs";

    private WithFooterRecyclerView rvContent;
    private EditText etContent;
    private ImageView ivImgAdd;
    private TextView tvSend;
    private LinearLayout llImgPreview;
    private TextView tvPickHint;
    private RecyclerView rlContainer;

    private PostDetailAdapter mAdapter;
    private PostReplyAdapter mPicAdapter;

    // 检测软键盘输入状态
    private EmotionInputDetector mDetector;
    protected boolean mIsLoadMore = false;
    private PostDetail mPostData;
    private int mPostId;
    private NoEncryptEngine mEngine;
    private int mPage = 1;
    private ArrayList<PhotoInfo> mPostImg = new ArrayList<>(Global.REPLY_IMG_COUNT);
    /**
     * 请求参数字典
     */
    private HashMap<String, Object> reqData = new HashMap<>();
    private ReqPostToken reqToken = new ReqPostToken();

    public static PostDetailNewFragment newInstance(int postId) {
        PostDetailNewFragment fragment = new PostDetailNewFragment();
        Bundle b = new Bundle();
        b.putInt(KeyConfig.KEY_DATA, postId);
        fragment.setArguments(b);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void initView(Bundle savedInstanceState) {
        initViewManger(R.layout.fragment_post_detail);
        rvContent = getViewById(R.id.rv_comment);
        etContent = getViewById(R.id.et_content);
        ivImgAdd = getViewById(R.id.iv_img_add);
        tvSend = getViewById(R.id.tv_send);
        llImgPreview = getViewById(R.id.ll_img_preview);
        tvPickHint = getViewById(R.id.tv_pick_hint);
        rlContainer = getViewById(R.id.rl_container);
    }

    @Override
    protected void setListener() {
        rvContent.setOnLoadListener(this);
        rvContent.setOnTouchListener(this);
        ivImgAdd.setOnClickListener(this);
        tvSend.setOnClickListener(this);
        etContent.addTextChangedListener(this);
        mAdapter.setListener(this);
        mAdapter.setTouchListener(this);
        mPicAdapter.setPickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_MISS_STATE);
            return;
        }
        mPostId = getArguments().getInt(KeyConfig.KEY_DATA);
        reqToken.postId = mPostId;
        reqData.put(KEY_POST_ID, mPostId);

        mAdapter = new PostDetailAdapter(getContext());
        rvContent.setAdapter(mAdapter);
        rvContent.setLayoutManager(new SnapLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvContent.setViewFooterNoMore(R.layout.view_post_comment_no_more);
        rvContent.setViewFooterLoading(R.layout.view_item_footer);
        rvContent.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL,
                ViewUtil.getColor(getContext(), R.color.co_common_content_bg),
                getResources().getDimensionPixelSize(R.dimen.di_list_item_gap_very_small)));
        mEngine = Global.getNoEncryptEngine();

        mPicAdapter = new PostReplyAdapter(getContext(), mPostImg);
        GridLayoutManager gld = new GridLayoutManager(getContext(), 4);
        rlContainer.setLayoutManager(gld);
        rlContainer.setAdapter(mPicAdapter);
        mPicAdapter.setTvPickHint(tvPickHint);

        sPostDetailInstance = this;

        int lastSoftInputHeight = getContext().getResources()
                .getDimensionPixelSize(R.dimen.di_default_soft_input_height);
        int height = AssistantApp.getInstance().getSoftInputHeight(getActivity());
        if (height > 0 && height != lastSoftInputHeight) {
            resetLayoutParam(height);
        }
        mDetector = EmotionInputDetector.with(getActivity())
                .setEmotionView(llImgPreview)
                .bindToContent(rvContent)
//                .bindToEmotionButton(ivImgAdd)
                .bindToEditText(etContent)
                .build();

        ((PostDetailNewActivity) getActivity()).showShareBtn(View.GONE, null);
    }

    @Override
    protected void lazyLoad() {
        refreshInitConfig();

        if (!NetworkUtil.isAvailable(getContext())) {
            refreshFailEnd();
            return;
        }
        mEngine.obtainPostDetail(mPostId)
                .enqueue(new Callback<JsonRespBase<PostDetail>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<PostDetail>> call, Response<JsonRespBase<PostDetail>>
                            response) {
                        if (call.isCanceled() || !mCanShowUI) {
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
                    public void onFailure(Call<JsonRespBase<PostDetail>> call, Throwable t) {
                        if (call.isCanceled() || !mCanShowUI) {
                            return;
                        }
                        refreshFailEnd();
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                    }
                });
    }

    private void updateData(PostDetail data) {
        if (data == null) {
            mViewManager.showEmpty();
            return;
        }

        // 构造评论的模拟数据
        if (data.commentInfos != null && data.commentInfos.size() > 0) {
            ArrayList<CommentDetail> details = new ArrayList<>();
            CommentDetail detail;
            ArrayList<CommentDetailListItem> listItems;
            CommentDetailListItem item;
            for (CommentDetailInfo info : data.commentInfos) {
                detail = new CommentDetail();
                detail.commentInfo = info;
                int num = info.commentCount;
                if (num > 0) {
                    listItems = new ArrayList<>(num);
                    for (int i = 0; i < num; i++) {
                        item = new CommentDetailListItem();
                        item.atNick = (int) (Math.random() * 2) == 1 ? "坑小爹" : null;
                        item.nick = "小明" + i;
                        item.content = "测试内容随随便便了~";
                        listItems.add(item);
                    }
                    detail.listInfo = listItems;
                }
                details.add(detail);
            }
            mAdapter.setData(details);
            mPage = 2;
        }
        if (data.commentInfos == null || data.commentInfos.size() < 10) {
            rvContent.setLoadState(WithFooterRecyclerView.STATE_NO_MORE);
        }
        mPostData = data;
        mAdapter.setPostData(data);
        mAdapter.notifyDataSetChanged();
        mViewManager.showContent();
        // 显示分享按钮
        ((PostDetailNewActivity) getActivity()).showShareBtn(View.VISIBLE, data);
    }

    private void addMoreData(ArrayList<CommentDetailInfo> commentInfos) {
        if (commentInfos != null && commentInfos.size() > 0) {
            ArrayList<CommentDetail> details = new ArrayList<>();
            CommentDetail detail;
            ArrayList<CommentDetailListItem> listItems;
            CommentDetailListItem item;
            for (CommentDetailInfo info : commentInfos) {
                detail = new CommentDetail();
                detail.commentInfo = info;
                int num = info.commentCount;
                if (num > 0) {
                    listItems = new ArrayList<>(num);
                    for (int i = 0; i < num; i++) {
                        item = new CommentDetailListItem();
                        item.atNick = (int) (Math.random() * 2) == 1 ? "雄二" : null;
                        item.nick = "小明" + i;
                        item.content = "这个新增的测试内容!!!";
                        listItems.add(item);
                    }
                    detail.listInfo = listItems;
                }
                details.add(detail);
            }
            mAdapter.addMoreData(details);
            mPage++;
        }
    }

    @Override
    public void onLoadMore() {
        doLoadMore();
    }

    private void doLoadMore() {
        if (mIsLoadMore) {
            return;
        }
        mIsLoadMore = true;
        mEngine.obtainPostCommentList(mPostId, mPage, PAGE_SIZE)
                .enqueue(new Callback<JsonRespBase<PostCommentList>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<PostCommentList>> call,
                                           Response<JsonRespBase<PostCommentList>> response) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        mIsLoadMore = false;
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            PostCommentList list = response.body().getData();
                            if (list.commentInfos == null || list.commentInfos.size() < PAGE_SIZE) {
                                rvContent.setLoadState(WithFooterRecyclerView.STATE_NO_MORE);
                            } else {
                                rvContent.setLoadState(WithFooterRecyclerView.STATE_NONE);
                            }
                            addMoreData(list.commentInfos);
                            return;
                        }
                        rvContent.setLoadState(WithFooterRecyclerView.STATE_ERROR);
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<PostCommentList>> call, Throwable t) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        mIsLoadMore = false;
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                        rvContent.setLoadState(WithFooterRecyclerView.STATE_ERROR);
                    }
                });
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
     * 处理回复提交请求
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
        AppDebugConfig.d(AppDebugConfig.TAG_WARN, "put content = " + reqData.get(KEY_CONTENT));
        reqData.put(KEY_CUID, AccountManager.getInstance().getUserInfo().uid);
        if (!mPostImg.isEmpty()) {
            reqData.put(KEY_IMGS, evaluateImgParam());
        }
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
                t.printStackTrace();
            }
        });
    }

    /**
     * 在提交回复后执行刷新UI操作
     */
    private void refreshAfterPost() {
        clearState();
        ToastUtil.showShort(ConstString.TOAST_COMMENT_SUCCESS);
//        hideLoading();
        mEngine.obtainPostCommentList(mPostId, 1, PAGE_SIZE)
                .enqueue(new Callback<JsonRespBase<PostCommentList>>() {
                    @Override
                    public void onResponse(Call<JsonRespBase<PostCommentList>> call,
                                           Response<JsonRespBase<PostCommentList>> response) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        hideLoading();
                        if (response != null && response.isSuccessful()
                                && response.body() != null && response.body().isSuccess()) {
                            PostCommentList list = response.body().getData();
                            if (list.commentInfos == null || list.commentInfos.size() < PAGE_SIZE) {
                                rvContent.setLoadState(WithFooterRecyclerView.STATE_NO_MORE);
                            } else {
                                rvContent.setLoadState(WithFooterRecyclerView.STATE_NONE);
                            }
                            updatePubRefreshData(list);
                            return;
                        }
                        AppDebugConfig.warnResp(AppDebugConfig.TAG_ENCRYPT, response);
                    }

                    @Override
                    public void onFailure(Call<JsonRespBase<PostCommentList>> call, Throwable t) {
                        if (call == null || !mCanShowUI) {
                            return;
                        }
                        hideLoading();
                        AppDebugConfig.w(AppDebugConfig.TAG_ENCRYPT, t);
                    }
                });
    }

    /**
     * 帖子发表成功后重新刷新请求的首页数据
     */
    private void updatePubRefreshData(PostCommentList list) {
        // 构造评论的模拟数据
        if (list.commentInfos != null && list.commentInfos.size() > 0) {
            ArrayList<CommentDetail> details = new ArrayList<>();
            CommentDetail detail;
            ArrayList<CommentDetailListItem> listItems;
            CommentDetailListItem item;
            for (CommentDetailInfo info : list.commentInfos) {
                detail = new CommentDetail();
                detail.commentInfo = info;
                int num = info.commentCount;
                if (num > 0) {
                    listItems = new ArrayList<>(num);
                    for (int i = 0; i < num; i++) {
                        item = new CommentDetailListItem();
                        item.atNick = (int) (Math.random() * 2) == 1 ? "坑小爹" : null;
                        item.nick = "小明" + i;
                        item.content = "测试内容随随便便了~";
                        listItems.add(item);
                    }
                    detail.listInfo = listItems;
                }
                details.add(detail);
            }
            mAdapter.setData(details);
            mPage = 2;
            mPostData.postInfo.commentCount = list.commentInfos.get(0).floor;
            mPostData.commentInfos = list.commentInfos;
            mAdapter.setPostData(mPostData);
            mAdapter.notifyDataSetChanged();
            rvContent.smoothScrollToPosition(1);
        }
    }

    /**
     * 发表帖子成功后回复最初状态
     */
    private void clearState() {
        etContent.setText("");
        ivImgAdd.setSelected(false);
        final int count = mPostImg.size();
        mPostImg.clear();
        mAdapter.notifyItemRangeRemoved(0, count);
        InputMethodUtil.hideSoftInput(etContent);
        llImgPreview.setVisibility(View.GONE);
        etContent.clearFocus();
    }

    /**
     * 根据文件获取图片字节数组的Base64编码字符串
     */
    private String generateImageStringParam(String filePath) {
        ByteArrayOutputStream baos = BitmapUtil.getBitmapForBaos(filePath, AppConfig.REPLY_PIC_SIZE / mPostImg.size(),
                AppConfig.REPLY_PIC_WIDTH, AppConfig.REPLY_PIC_HEIGHT);
        if (baos != null) {
            String s = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }
        return "";
    }

    /**
     * 构造图片字节的字符串数组
     */
    @NonNull
    private String evaluateImgParam() {
        StringBuilder imgBuilder = new StringBuilder("[");
        for (PhotoInfo p : mPostImg) {
            imgBuilder.append("\"")
                    .append(generateImageStringParam(p.getPhotoPath()).replaceAll("\\n", "")).append
                    ("\",");
        }
        imgBuilder.deleteCharAt(imgBuilder.length() - 1);
        imgBuilder.append("]");
        return imgBuilder.toString();
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

    /**
     * 用于评论详情更新后能更新此处数据
     */
    public void updateItemData(CommentDetail data) {
        if (mPostData == null || rvContent == null
                || mPostData.commentInfos == null || data == null)
            return;
        CommentDetailInfo info;
        for (int i = 0; i < mPostData.commentInfos.size(); i++) {
            info = mPostData.commentInfos.get(i);
            if (info.floor == data.commentInfo.floor) {
                mPostData.commentInfos.set(i, data.commentInfo);
                mAdapter.getData().set(i, data);
                mAdapter.notifyItemChanged(i + 1);
//                rvContent.smoothScrollToPosition(i + 1);
                return;
            }
        }
    }

    private long mLastClickTime;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_img_add:
                // 由于实现逻辑原因，把以下本该private的方法改为public
                if (mPostImg.isEmpty()) {
                    InputMethodUtil.hideSoftInput(etContent);
                    llImgPreview.setVisibility(View.GONE);
                    ivImgAdd.setSelected(false);
                    GalleryFinal.openGalleryMulti(mPicAdapter.REQ_ID_IMG_ADD, Global.REPLY_IMG_COUNT, mPicAdapter);
                    return;
                }
                if (mDetector.isSoftInputShown()) {
                    InputMethodUtil.hideSoftInput(etContent);
                    etContent.clearFocus();
                } else {
                    if (llImgPreview.isShown()) {
                        llImgPreview.setVisibility(View.GONE);
                    } else {
                        llImgPreview.setVisibility(View.VISIBLE);
                    }
                }
                break;
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

    @Override
    public void onItemClick(CommentDetail item, View view, int position) {
        if (position == 0) {
            switch (view.getId()) {
                case R.id.tv_comment:
                    if (mAdapter.getItemCount() > 1)
                        rvContent.smoothScrollToPosition(1);
                    break;
            }
        } else {
            switch (view.getId()) {
                case R.id.iv_pic1:
                case R.id.iv_list_pic1:
                    GalleryFinal.openMultiPhoto(0, item.commentInfo.thumbImgs);
                    break;
                case R.id.iv_list_pic2:
                    GalleryFinal.openMultiPhoto(1, item.commentInfo.thumbImgs);
                    break;
                case R.id.iv_list_pic3:
                    GalleryFinal.openMultiPhoto(2, item.commentInfo.thumbImgs);
                    break;
                case R.id.iv_list_pic4:
                    GalleryFinal.openMultiPhoto(3, item.commentInfo.thumbImgs);
                    break;
                case R.id.iv_list_pic5:
                    GalleryFinal.openMultiPhoto(4, item.commentInfo.thumbImgs);
                    break;
                case R.id.iv_list_pic6:
                    GalleryFinal.openMultiPhoto(5, item.commentInfo.thumbImgs);
                    break;
                case R.id.tv_comment:
                case R.id.tv_comment_more:
                    IntentUtil.jumpPostReplyDetail(getContext(), mPostId, item.commentInfo.id);
                    break;
                case R.id.tv_admire:
                    item.commentInfo.likeCount += (item.commentInfo.isLike ? -1 : 1);
                    item.commentInfo.isLike = !item.commentInfo.isLike;
                    mAdapter.notifyItemChanged(position);
                    RequestUtil.putAdmireState(mPostId, item.commentInfo.id, item.commentInfo.isLike);
                    break;
            }
        }
    }

    @Override
    public void release() {
        super.release();
        sPostDetailInstance = null;
        if (mPicAdapter != null) {
            mPicAdapter.release();
            mPicAdapter = null;
        }
        if (mAdapter != null) {
            mAdapter.release();
            mAdapter = null;
        }
        rvContent = null;
        if (mCallGetToken != null) {
            mCallGetToken.cancel();
            mCallGetToken = null;
        }
        if (mCallPost != null) {
            mCallPost.cancel();
            mCallPost = null;
        }
        mEngine = null;
        if (mPostImg != null) {
            mPostImg.clear();
            mPostImg = null;
        }
        if (reqData != null) {
            reqData.clear();
            reqData = null;
        }
        if (mPostData != null) {
            mPostData.commentInfos = null;
            mPostData.postInfo = null;
        }
        mDetector = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        tvSend.setEnabled(s.toString().trim().length() > 0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean mIsDown;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsDown = true;
        }
        if (mIsDown) {
            switch (v.getId()) {
//                case R.id.et_content:
//                    // EditText点击打开软键盘之前，需要先显示llImgPreview才行，否则页面会被推上去
//                    if (llImgPreview.getVisibility() != View.INVISIBLE) {
//                        llImgPreview.setVisibility(View.INVISIBLE);
//                    }
//                    etContent.requestFocus();
//                    InputMethodUtil.showSoftInput(getActivity());
//                    mIsShowSoft = true;
//                    break;
                case R.id.rv_comment:
                case R.id.wv_content:
                    InputMethodUtil.hideSoftInput(getActivity());
                    etContent.clearFocus();
                    if (llImgPreview.getVisibility() != View.GONE) {
                        llImgPreview.setVisibility(View.GONE);
                    }
            }
            mIsDown = false;
        }
        return false;
    }

    private void resetLayoutParam(int height) {
        ViewGroup.LayoutParams lp = llImgPreview.getLayoutParams();
        lp.height = height;
        llImgPreview.setLayoutParams(lp);
    }

    @Override
    public void pickSuccess() {
        llImgPreview.setVisibility(View.VISIBLE);
        ivImgAdd.setSelected(true);
    }

    @Override
    public void pickFailed() {
        if (mPostImg.isEmpty()) {
            llImgPreview.setVisibility(View.GONE);
            ivImgAdd.setSelected(false);
        }
    }

    @Override
    public String getPageName() {
        return "活动详情页";
    }

    @Override
    public boolean onBack() {
        return mDetector != null && mDetector.interceptBackPress();
    }
}
