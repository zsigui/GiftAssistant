package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.config.AppDebugConfig;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
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
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.IntentUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostCommentFragment extends BaseFragment_WebView implements ShowBottomBarListener, View.OnTouchListener,
		ViewTreeObserver.OnGlobalLayoutListener, TextWatcher {

	static final String KEY_POST = "key_id_post";
	static final String KEY_COMMENT = "key_id_comment";
	private final String TAG_PREFIX = "提交评论失败";

	private final String KEY_POST_ID = "activity_id";
	private final String KEY_COMMENT_ID = "comment_id";
	private final String KEY_CUID = "cuid";
	private final String KEY_TOKEN = "token";
	private final String KEY_CONTENT = "content";
	private final String KEY_COMMMENT_TO_DEFAULT = "楼主";


	private LinearLayout llBottom;
	private EditText etContent;
	private TextView tvSend;
	private View llBackground;
	private int mLastSoftInputHeight;

	/**
	 * 请求参数字典
	 */
	private HashMap<String, Object> reqData = new HashMap<>();
	private ReqPostToken reqToken = new ReqPostToken();

	public static PostCommentFragment newInstance(int postId, int commentId) {
		PostCommentFragment fragment = new PostCommentFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_POST, postId);
		bundle.putInt(KEY_COMMENT, commentId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_post_reply_comment);
		llBottom = getViewById(R.id.ll_bottom);
		etContent = getViewById(R.id.et_content);
		tvSend = getViewById(R.id.tv_send);
		llBackground = getViewById(R.id.ll_img_preview);
	}

	@Override
	protected void setListener() {
		etContent.setOnTouchListener(this);
		etContent.getViewTreeObserver().addOnGlobalLayoutListener(this);
		mWebView.setOnTouchListener(this);
		tvSend.setOnClickListener(this);
		etContent.addTextChangedListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			getActivity().onBackPressed();
			return;
		}
		final int postId = getArguments().getInt(KEY_POST);
		final int commentId = getArguments().getInt(KEY_COMMENT);

		reqToken.postId = postId;
		reqData.put(KEY_POST_ID, postId);
		reqData.put(KEY_COMMENT_ID, commentId);
		setReplyTo(commentId, KEY_COMMMENT_TO_DEFAULT);
		showBar(true, null);
		loadUrl(String.format(WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_COMMENT_LIST), postId, commentId));
	}

	@Override
	protected void lazyLoad() {

	}

	@Override
	public void showBar(boolean isShow, Object param) {
		if (isShow) {
			llBottom.setVisibility(View.VISIBLE);
		} else {
			llBottom.setVisibility(View.GONE);
		}
	}

	public void setReplyTo(int commentId, String name) {
		if (reqData != null) {
			reqData.put(KEY_COMMENT_ID, commentId);
		}
		if (etContent != null) {
			walkStep += BIG_INCREASE;
			etContent.setHint(String.format("回复%s", name));
			etContent.requestFocus();
//			llBackground.setVisibility(View.VISIBLE);
		}
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
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
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
						ToastUtil.showShort("获取Token失败");
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
	 * 执行token获取后的提交操作
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
							hideLoading();
							ToastUtil.blurErrorMsg(TAG_PREFIX, response.body());
							return;
						}
						hideLoading();
						ToastUtil.blurErrorResp(TAG_PREFIX, response);
					}

					@Override
					public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
						hideLoading();
						if (AppDebugConfig.IS_DEBUG) {
							KLog.d(AppDebugConfig.TAG_FRAG, t);
						}
						ToastUtil.blurThrow(TAG_PREFIX);
					}
				});
			}
		});
	}

	/**
	 * 在提交回复后执行刷新UI操作
	 */
	private void refreshAfterPost() {
		ToastUtil.showShort("评论成功");
		etContent.setText("");
		replyStep = walkStep;
		InputMethodUtil.hideSoftInput(getActivity());
		reloadPage();
		hideLoading();
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
				if (AppDebugConfig.IS_DEBUG) {
					e.printStackTrace();
				}
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

	private final int BIG_INCREASE = 5;
	private final int NORMAL_INCREASE = 1;
	private int walkStep = BIG_INCREASE;
	private int wvTouchStep = 0;
	private int textStep = 0;
	private int replyStep = 0;
	private boolean mIsDown = false;
	private long mLastTouchTime = 0;


	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				long curTime = System.currentTimeMillis();
				if (curTime - mLastTouchTime < 100) {
					mLastTouchTime = curTime;
					break;
				}
				mIsDown = true;
				break;
		}
		if (mIsDown) {
			switch (v.getId()) {
				case R.id.et_content:
					etContent.requestFocus();
					if (InputMethodUtil.getSoftInputHeight(getActivity()) == 0) {
						textStep = walkStep;
						llBackground.setVisibility(View.VISIBLE);
					}
					break;
				case R.id.wv_container:
					wvTouchStep = walkStep;
					InputMethodUtil.hideSoftInput(getActivity());
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
		int softInputHeight = AssistantApp.getInstance().getSoftInputHeight(getActivity());
		final int curHeight = InputMethodUtil.getSoftInputHeight(getActivity());
		if (curHeight != 0 && softInputHeight != curHeight) {
			AssistantApp.getInstance().setSoftInputHeight(curHeight);
			softInputHeight = curHeight;
		}
		if (softInputHeight != mLastSoftInputHeight && softInputHeight != 0) {
			mLastSoftInputHeight = softInputHeight;
			resetLayoutParams();
		}
		if (walkStep == textStep) {
			walkStep += NORMAL_INCREASE;
			InputMethodUtil.showSoftInput(getActivity());
		} else if ((walkStep - NORMAL_INCREASE == textStep && curHeight == 0)
				|| walkStep == wvTouchStep) {
			walkStep += BIG_INCREASE;
			llBackground.setVisibility(View.GONE);
		}
	}

	private void resetLayoutParams() {
		if (llBackground != null) {
			ViewGroup.LayoutParams lp = llBackground.getLayoutParams();
			lp.height = mLastSoftInputHeight;
			llBackground.setLayoutParams(lp);
		}
	}


	@Override
	public String getPageName() {
		return "评论详情";
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
}
