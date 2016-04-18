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
import com.oplay.giftcool.model.data.req.ReqCommitReply;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-4-13.
 */
public class PostCommentFragment extends BaseFragment_WebView implements ShowBottomBarListener, View.OnTouchListener,
		ViewTreeObserver.OnGlobalLayoutListener, TextWatcher {

	static final String KEY_POST_ID = "key_id_post";
	static final String KEY_COMMENT_ID = "key_id_comment";

	private LinearLayout llBottom;
	private EditText etContent;
	private TextView tvSend;
	private View llBackground;
	private int mLastSoftInputHeight;
	private boolean mShowSoftInput = false;

	// 请求实体
	private JsonReqBase<ReqCommitReply> mReqData;


	public static PostCommentFragment newInstance(int postId, int commentId) {
		PostCommentFragment fragment = new PostCommentFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_POST_ID, postId);
		bundle.putInt(KEY_COMMENT_ID, commentId);
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
		final int postId = getArguments().getInt(KEY_POST_ID);
		final int commentId = getArguments().getInt(KEY_COMMENT_ID);
		showBar(true, null);
		ReqCommitReply data = new ReqCommitReply();
		data.postId = postId;
		data.commentId = commentId;
		mReqData = new JsonReqBase<>(data);
		setReplyTo(commentId, "楼主");
		mWebView.loadUrl(String.format("%s?post_id=%d&comment_id=%d",
				WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_COMMENT_LIST), postId, commentId));
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
		if (mReqData != null) {
			mReqData.data.commentId = commentId;
		}
		if (etContent != null) {
			etContent.setText(String.format("回复%s", name));
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


	private Call<JsonRespBase<PostToken>> mCallGetToken;
	/**
	 * 执行发送消息服务
	 */
	private void handleSend() {
		if (AccountManager.getInstance().isLogin()) {
			IntentUtil.jumpLogin(getContext());
			return;
		}
		mReqData.data.cuid = AccountManager.getInstance().getUserInfo().uid;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
					return;
				}
				DialogManager.getInstance().showLoadingDialog(getChildFragmentManager());
				if (mCallGetToken == null) {
					ReqPostToken reqToken = new ReqPostToken();
					reqToken.postId = mReqData.data.postId;
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
						DialogManager.getInstance().hideLoadingDialog();
						ToastUtil.showShort("获取Token失败");
					}

					@Override
					public void onFailure(Call<JsonRespBase<PostToken>> call, Throwable t) {
						if (!mCanShowUI || call.isCanceled()) {
							return;
						}
						DialogManager.getInstance().hideLoadingDialog();
					}
				});
			}
		});
	}

	private Call<JsonRespBase<Void>> mCallCommit;

	/**
	 * 执行token获取后的提交操作
	 */
	private void handleCommit(String token) {
		final  ReqCommitReply data = mReqData.data;
		data.token = token;
		data.content = etContent.getText().toString();
		if (mCallCommit != null) {
			mCallCommit.cancel();
		}
		mCallCommit = ((PostDetailActivity) getActivity()).getEngine().commitReply(mReqData);
		mCallCommit.enqueue(new Callback<JsonRespBase<Void>>() {
			@Override
			public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
				if (call.isCanceled() || !mCanShowUI) {
					return;
				}
				DialogManager.getInstance().hideLoadingDialog();
				if (response != null && response.isSuccessful()) {
					if (response.body() != null && response.body().isSuccess()) {
						mWebView.reload();
						return;
					}
					KLog.d(AppDebugConfig.TAG_WARN, response.body() == null ? "解析失败": response.body().error());
					return;
				}
				KLog.d(AppDebugConfig.TAG_WARN, response == null ? "返回失败": response.code() + ":" + response.message());
			}

			@Override
			public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
				if (call.isCanceled() || !mCanShowUI) {
					return;
				}
				DialogManager.getInstance().hideLoadingDialog();
				KLog.d(AppDebugConfig.TAG_WARN, t);
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.et_content:
				etContent.requestFocus();
				llBackground.setVisibility(View.VISIBLE);
				InputMethodUtil.showSoftInput(getActivity());
				mShowSoftInput = true;
				break;
			case R.id.wv_container:
				InputMethodUtil.hideSoftInput(getActivity());
				break;
		}
		return false;
	}


	@Override
	public void onGlobalLayout() {
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

		if (curHeight == 0) {
			if (mShowSoftInput) {
				llBackground.setVisibility(View.GONE);
				mShowSoftInput = false;
			}
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
