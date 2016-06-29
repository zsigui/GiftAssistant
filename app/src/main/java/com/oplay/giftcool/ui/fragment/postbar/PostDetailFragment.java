package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostReplyAdapter;
import com.oplay.giftcool.config.AppConfig;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.listener.ShowBottomBarListener;
import com.oplay.giftcool.manager.AccountManager;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.data.req.ReqPostToken;
import com.oplay.giftcool.model.data.resp.PostToken;
import com.oplay.giftcool.model.json.base.JsonReqBase;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.activity.PostDetailActivity;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.io.ByteArrayOutputStream;
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
 * Created by zsigui on 16-4-6.
 */
public class PostDetailFragment extends BaseFragment_WebView implements TextWatcher,
        ViewTreeObserver.OnGlobalLayoutListener, ShowBottomBarListener, View.OnTouchListener {

    private final String PAGE_NAME = "活动详情";

    private final String KEY_CUID = "cuid";
    private final String KEY_TOKEN = "token";
    private final String KEY_POST_ID = "activity_id";
    private final String KEY_CONTENT = "content";
    private final String KEY_IMGS = "imgs";

    private int mLastSoftInputHeight = 400;

    private EditText etContent;
    private ImageView ivImgAdd;
    private TextView tvSend;
    private LinearLayout llBottomBar;
    private LinearLayout llImgPreview;
    private RecyclerView rlContainer;
    private TextView tvPickHint;

    private PostReplyAdapter mAdapter;

    private ArrayList<PhotoInfo> mPostImg = new ArrayList<>(Global.REPLY_IMG_COUNT);
    /**
     * 请求参数字典
     */
    private HashMap<String, Object> reqData = new HashMap<>();
    private ReqPostToken reqToken = new ReqPostToken();

    public static PostDetailFragment newInstance(int postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle b = new Bundle();
        b.putInt(KeyConfig.KEY_DATA, postId);
        fragment.setArguments(b);
        return fragment;
    }

    public PostDetailFragment() {
//		mEngine = AssistantApp.getInstance().getRetrofit().create(NoEncryptEngine.class);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_post_detail);
        etContent = getViewById(R.id.et_content);
        ivImgAdd = getViewById(R.id.iv_img_add);
        tvSend = getViewById(R.id.tv_send);
        llBottomBar = getViewById(R.id.ll_bottom);
        llImgPreview = getViewById(R.id.ll_img_preview);
        rlContainer = getViewById(R.id.rl_container);
        tvPickHint = getViewById(R.id.tv_pick_hint);
    }

    @Override
    protected void setListener() {
        ivImgAdd.setOnClickListener(this);
        tvSend.setOnClickListener(this);
        etContent.addTextChangedListener(this);
        etContent.setOnTouchListener(this);
        etContent.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mWebView.setOnTouchListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        if (getArguments() == null) {
            ToastUtil.showShort(ConstString.TOAST_WRONG_PARAM);
            return;
        }
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        int postId = getArguments().getInt(KeyConfig.KEY_DATA, 0);
        mAdapter = new PostReplyAdapter(getContext(), mPostImg);
        GridLayoutManager gld = new GridLayoutManager(getContext(), 4);
        rlContainer.setLayoutManager(gld);
        rlContainer.setAdapter(mAdapter);
        mAdapter.setTvPickHint(tvPickHint);
        mAdapter.setFragment(this);
        etContent.requestFocus();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH)
            etContent.setShowSoftInputOnFocus(false);

        reqToken.postId = postId;
        reqData.put(KEY_POST_ID, postId);
//		showBar(true, null);
        loadUrl(String.format(WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_DETAIL), postId));
    }

    @Override
    protected void lazyLoad() {
        if (getActivity() != null) {
            ((PostDetailActivity) getActivity()).showRightBtn(View.INVISIBLE, ConstString.TEXT_POST_BTN);
        }
        reloadPage();
    }

    @Override
    public String getPageName() {
        return PAGE_NAME;
    }

    private long mLastClickTime;

    // 以下状态结合判断下端回复栏的显示
    // 实现略丑陋，逻辑已经不会理了
    private final int BIG_INCREASE = 5;
    private final int NORMAL_INCREASE = 1;
    private int imgFailStep = 0;
    private int wvTouchStep = 0;
    private int contentTouchStep = 0;
    private int replyToStep = 0;
    private int walkStep = BIG_INCREASE;

    @Override
    protected void doAtWebStart() {
        super.doAtWebStart();
        showBar(false, null);
    }

    @Override
    protected void doAfterWebViewInit() {
        super.doAfterWebViewInit();
        showBar(true, null);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_img_add:
                if (mPostImg.size() == 0) {
                    GalleryFinal.openGalleryMulti(mAdapter.REQ_ID_IMG_ADD, Global.REPLY_IMG_COUNT, mAdapter);
                    pickFailed();
                    return;
                }
                ivImgAdd.setSelected(true);
                walkStep += BIG_INCREASE;
                if (InputMethodUtil.getSoftInputHeight(getActivity()) != 0) {
                    InputMethodUtil.hideSoftInput(getActivity());
                } else if (llImgPreview.getVisibility() == View.VISIBLE) {
                    llImgPreview.setVisibility(View.GONE);
                } else {
                    llImgPreview.setVisibility(View.VISIBLE);
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

    /**
     * 选择图片成功返回的处理函数
     */
    public void pickSuccess() {
        ivImgAdd.setSelected(true);
        walkStep += BIG_INCREASE;
        if (InputMethodUtil.getSoftInputHeight(getActivity()) != 0) {
            InputMethodUtil.hideSoftInput(getActivity());
        } else {
            llImgPreview.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 未选择图片或执行失败或者删除完图片的处理函数
     */
    public void pickFailed() {
        mAdapter.setPicTextVal();
        ivImgAdd.setSelected(false);
        if (InputMethodUtil.getSoftInputHeight(getActivity()) != 0) {
            imgFailStep = walkStep;
            InputMethodUtil.hideSoftInput(getActivity());
        } else if (llImgPreview.getVisibility() == View.VISIBLE) {
            walkStep += BIG_INCREASE;
            llImgPreview.setVisibility(View.GONE);
        }
    }

    public void toReply() {
        if (etContent != null) {
            etContent.requestFocus();
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
        showLoading();
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtil.isConnected(getContext())) {
                    hideLoading();
                    ToastUtil.showShort(ConstString.TOAST_NET_ERROR);
                    return;
                }
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
        });
    }

    /**
     * 处理回复提交请求
     */
    private void handleCommit(final String token) {
        Global.THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                if (mCallPost != null) {
                    mCallPost.cancel();
                }
                reqData.put(KEY_TOKEN, token);
                reqData.put(KEY_CONTENT, etContent.getText().toString());
                reqData.put(KEY_CUID, AccountManager.getInstance().getUserInfo().uid);
                if (!mPostImg.isEmpty()) {
                    reqData.put(KEY_IMGS, evaluateImgParam());
                }
                mCallPost = ((PostDetailActivity) getActivity()).getEngine().commitReply(evaluateBody(reqData));
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
        });

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

    /**
     * 在提交回复后执行刷新UI操作
     */
    private void refreshAfterPost() {
        clearState();
        reloadPage();
//		if (mWebView != null) {
//			loadUrl(mWebView.getUrl() + "#comment");
//			reloadPage();
//		}
        hideLoading();
        ToastUtil.showShort(ConstString.TOAST_COMMENT_SUCCESS);
    }

    private void clearState() {
        etContent.setText("");
        ivImgAdd.setSelected(false);
        final int count = mPostImg.size();
        mPostImg.clear();
        mAdapter.notifyItemRangeRemoved(0, count);
        pickFailed();
    }

    @Override
    public boolean onBack() {
        clearState();
        return super.onBack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mWebView != null && BaseFragment_WebView.sScrollMap != null) {
            BaseFragment_WebView.sScrollMap.put(mWebView.getUrl(), mWebView.getScrollY());
        }
    }

    /**
     * 根据文件获取图片字节数组的Base64编码字符串
     */
    private String generateImageStringParam(String filePath) {
        ByteArrayOutputStream baos = BitmapUtil.getBitmapForBaos(filePath, AppConfig.REPLY_PIC_SIZE,
                AppConfig.REPLY_PIC_WIDTH, AppConfig.REPLY_PIC_HEIGHT);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void showLoading() {
        DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
    }

    private void hideLoading() {
        DialogManager.getInstance().hideLoadingDialog();
    }

    @Override
    public void release() {
        super.release();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            etContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        } else {
            etContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

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
    public void showBar(boolean isShow, Object param) {
        if (isShow) {
            llBottomBar.setVisibility(View.VISIBLE);
//			AnimationUtils.loadAnimation(getContext(), R.anim.show_fade_in);
        } else {
            llBottomBar.setVisibility(View.GONE);
//			AnimationUtils.loadAnimation(getContext(), R.anim.show_fade_out);
        }
    }

    private boolean mIsDown = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDown = true;
                break;
        }
        if (mIsDown) {
            switch (v.getId()) {
                case R.id.et_content:
                    // EditText点击打开软键盘之前，需要先显示llImgPreview才行，否则页面会被推上去
                    etContent.requestFocus();
                    if (InputMethodUtil.getSoftInputHeight(getActivity()) == 0) {
                        contentTouchStep = walkStep;
                        if (llImgPreview.getVisibility() == View.VISIBLE) {
                            walkStep += NORMAL_INCREASE;
                            InputMethodUtil.showSoftInput(getActivity());
                        } else {
                            llImgPreview.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case R.id.wv_container:
                    if (InputMethodUtil.getSoftInputHeight(getActivity()) == 0) {
                        walkStep += BIG_INCREASE;
                        if (llImgPreview.getVisibility() == View.VISIBLE) {
                            llImgPreview.setVisibility(View.GONE);
                        }
                    } else {
                        wvTouchStep = walkStep;
                        InputMethodUtil.hideSoftInput(getActivity());
                    }
                    break;
            }
            mIsDown = false;
        }
        return false;
    }

    @Override
    public void onGlobalLayout() {
        if (getActivity() == null) {
            return;
        }
        final int curHeight = InputMethodUtil.getSoftInputHeight(getActivity());
        int softInputHeight = AssistantApp.getInstance().getSoftInputHeight(getActivity());
        if (curHeight > 0 && curHeight != softInputHeight) {
            softInputHeight = curHeight;
            AssistantApp.getInstance().setSoftInputHeight(curHeight);
        }
        if (softInputHeight != mLastSoftInputHeight && softInputHeight > 0) {
            mLastSoftInputHeight = softInputHeight;
            resetLayoutParams();
        }

        if (contentTouchStep == walkStep) {
            walkStep += NORMAL_INCREASE;
            InputMethodUtil.showSoftInput(getActivity());
        } else if ((contentTouchStep == walkStep - NORMAL_INCREASE && curHeight == 0)
                || (imgFailStep == walkStep)
                || (wvTouchStep == walkStep)) {
            walkStep += BIG_INCREASE;
            llImgPreview.setVisibility(View.GONE);
        }
    }

    private void resetLayoutParams() {
        if (llImgPreview != null) {
            ViewGroup.LayoutParams lp = llImgPreview.getLayoutParams();
            lp.height = mLastSoftInputHeight;
            llImgPreview.setLayoutParams(lp);
        }
    }
}
