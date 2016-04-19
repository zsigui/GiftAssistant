package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Build;
import android.os.Bundle;
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
import android.view.animation.AnimationUtils;
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
import com.oplay.giftcool.model.data.req.ReqCommitReply;
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
import com.socks.library.KLog;

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
	private final String TAG_PREFIX = "提交回复";


	private int mLastSoftInputHeight = 400;

	private EditText etContent;
	private ImageView ivImgAdd;
	private TextView tvSend;
	private LinearLayout llBottomBar;
	private LinearLayout llImgPreview;
	private RecyclerView rlContainer;
	private TextView tvPickHint;

	private PostReplyAdapter mAdapter;
	// 请求实体
	private JsonReqBase<ReqCommitReply> mReqData;

	private ArrayList<PhotoInfo> mPostImg = new ArrayList<>(Global.REPLY_IMG_COUNT);
	private int mId;

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
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			return;
		}
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		mId = getArguments().getInt(KeyConfig.KEY_DATA, 0);

		mAdapter = new PostReplyAdapter(getContext(), mPostImg);
		GridLayoutManager gld = new GridLayoutManager(getContext(), 4);
		rlContainer.setLayoutManager(gld);
		rlContainer.setAdapter(mAdapter);
		mAdapter.setTvPickHint(tvPickHint);
		mAdapter.setFragment(this);
		etContent.requestFocus();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH)
			etContent.setShowSoftInputOnFocus(false);

		showBar(true, null);
		ReqCommitReply data = new ReqCommitReply();
		data.postId = mId;
		mReqData = new JsonReqBase<>(data);
		loadUrl(String.format("%s?activity_id=%d",
				WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_DETAIL), mId));
	}

	@Override
	protected void lazyLoad() {
		reloadPage();
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}

	private long mLastClickTime;

	// 以下状态结合判断下端回复栏的显示
	// 实现略丑陋，逻辑已经不会理了
	private int imgShowStep = 0;
	private int imgFailStep = 0;
	private int imgHideStep = 0;
	private int wvTouchStep = 0;
	private int contentTouchStep = 0;
	private int walkStep = 2;

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
				if (llImgPreview.getVisibility() == View.VISIBLE
						&& InputMethodUtil.getSoftInputHeight(getActivity()) == 0) {
					imgHideStep = walkStep++;
					etContent.requestFocus();
					InputMethodUtil.hideSoftInput(getActivity());
					llImgPreview.setVisibility(View.GONE);
				} else {
					imgShowStep = walkStep++;
					llImgPreview.setVisibility(View.VISIBLE);
					InputMethodUtil.hideSoftInput(getActivity());
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
		imgShowStep = walkStep++;
		ivImgAdd.setSelected(true);
		llImgPreview.setVisibility(View.VISIBLE);
		InputMethodUtil.hideSoftInput(getActivity());
	}

	/**
	 * 未选择图片或执行失败或者删除完图片的处理函数
	 */
	public void pickFailed() {
		mAdapter.setPicTextVal();
		imgFailStep = walkStep++;
		llImgPreview.setVisibility(View.GONE);
		InputMethodUtil.hideSoftInput(getActivity());
		ivImgAdd.setSelected(false);
		etContent.requestFocus();
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
		mReqData.data.cuid = AccountManager.getInstance().getUserInfo().uid;
		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (!NetworkUtil.isConnected(getContext())) {
					hideLoading();
					ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
					return;
				}
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

	private Call<JsonRespBase<Void>> mCallCommit;

	/**
	 * 执行token获取后的提交操作
	 */
//	private void handleCommit(String token) {
//		final ReqCommitReply data = mReqData.data;
//		KLog.d(AppDebugConfig.TAG_WARN, "token = " + token);
//		data.token = token;
//		data.content = etContent.getText().toString();
//		if (!mPostImg.isEmpty()) {
//			data.imgs = new ArrayList<>(mPostImg.size());
//			for (PhotoInfo photo : mPostImg) {
//				ByteArrayOutputStream baos = BitmapUtil.getBitmapForBaos(photo.getPhotoPath(), AppConfig
// .UPLOAD_PIC_SIZE,
//						AppConfig.UPLOAD_PIC_WIDTH, AppConfig.UPLOAD_PIC_HEIGHT);
//				data.imgs.add(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));
//			}
//		}
//		if (mCallCommit != null) {
//			mCallCommit.cancel();
//		}
//		mCallCommit = ((PostDetailActivity) getActivity()).getEngine().commitReply(mReqData);
//		mCallCommit.enqueue(new Callback<JsonRespBase<Void>>() {
//			@Override
//			public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
//				if (call.isCanceled() || !mCanShowUI) {
//					return;
//				}
//				DialogManager.getInstance().hideLoadingDialog();
//				if (response != null && response.isSuccessful()) {
//					if (response.body() != null && response.body().isSuccess()) {
//						mWebView.reload();
//						return;
//					}
//					KLog.d(AppDebugConfig.TAG_WARN, response.body() == null ? "解析失败": response.body().error());
//					return;
//				}
//				KLog.d(AppDebugConfig.TAG_WARN, response == null ? "返回失败": response.code() + ":" + response.message());
//			}
//
//			@Override
//			public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
//				if (call.isCanceled() || !mCanShowUI) {
//					return;
//				}
//				DialogManager.getInstance().hideLoadingDialog();
//				KLog.d(AppDebugConfig.TAG_WARN, t);
//			}
//		});
//	}
//	/**
//	 * 处理回复提交请求
//	 */
//	private void handleCommit(String token) {
//
//
//		if (mCallPost != null) {
//			mCallPost.cancel();
//		}
//		HashMap<String, RequestBody> reqData = new HashMap<>();
//		reqData.put("msg", RequestBody.create(MediaType.parse("text/plain; charset=UTF-8"), etContent.getText
//				().toString()));
//		int i = 0;
//		for (PhotoInfo p : mPostImg) {
//			reqData.put("imgs", MultipartBody.create(MediaType.parse("image/*"),
//					generateImageStringParam(p.getPhotoPath())));
//			i++;
//		}
//		mCallPost = ((PostDetailActivity) getActivity()).getEngine().postReply(reqData);
//		mCallPost.enqueue(new Callback<JsonRespBase<Void>>() {
//			@Override
//			public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
//				if (!mCanShowUI && call.isCanceled()) {
//					hideLoading();
//					return;
//				}
//				if (response != null && response.isSuccessful()) {
//					if (response.body() != null && response.body().isSuccess()) {
//						refreshAfterPost();
//						return;
//					}
//					hideLoading();
//					ToastUtil.blurErrorMsg(TAG_PREFIX, response.body());
//					return;
//				}
//				hideLoading();
//				ToastUtil.blurErrorResp(TAG_PREFIX, response);
//			}
//
//			@Override
//			public void onFailure(Call<JsonRespBase<Void>> call, Throwable t) {
//				hideLoading();
//				if (AppDebugConfig.IS_DEBUG) {
//					KLog.d(AppDebugConfig.TAG_FRAG, t);
//				}
//				ToastUtil.blurThrow(TAG_PREFIX);
//			}
//		});
//
//	}

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
				HashMap<String, Object> reqData = new HashMap<>();
				reqData.put("activity_id", 1);
				reqData.put("token", token);
				reqData.put("content", etContent.getText().toString());
				reqData.put("cuid", AccountManager.getInstance().getUserInfo().uid);
				if (!mPostImg.isEmpty()) {
					StringBuilder imgBuilder = new StringBuilder("[");
					for (PhotoInfo p : mPostImg) {
						imgBuilder.append("\"")
								.append(generateImageStringParam(p.getPhotoPath()).replaceAll("\\n", "")).append
								("\",");
					}
					imgBuilder.deleteCharAt(imgBuilder.length() - 1);
					imgBuilder.append("]");
					reqData.put("imgs", imgBuilder.toString());
				}
				StringBuilder b = new StringBuilder();
				for (Map.Entry<String, Object> entry : reqData.entrySet()) {
					try {
						b.append(entry.getKey()).append("=").append(URLEncoder.encode(String.valueOf(entry.getValue())
								, "UTF-8")).append("&");
					} catch (UnsupportedEncodingException e) {
						KLog.d(AppDebugConfig.TAG_WARN, e);
					}
				}
				if (b.length() > 0) {
					b.deleteCharAt(b.length() - 1);
				}
				mCallPost = ((PostDetailActivity) getActivity()).getEngine().commitReply(b.toString());
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
		etContent.setText("");
		ivImgAdd.setSelected(false);
		final int count = mPostImg.size();
		mPostImg.clear();
		mAdapter.notifyItemRangeRemoved(0, count);
		pickFailed();
		hideLoading();
		reloadPage();
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
			AnimationUtils.loadAnimation(getContext(), R.anim.show_fade_in);
		} else {
			llBottomBar.setVisibility(View.GONE);
			AnimationUtils.loadAnimation(getContext(), R.anim.show_fade_out);
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.et_content:
				// EditText点击打开软键盘之前，需要先显示llImgPreview才行，否则页面会被推上去
				KLog.d(AppDebugConfig.TAG_WARN, "etContent is touch");
				contentTouchStep = walkStep++;
				llImgPreview.setVisibility(View.VISIBLE);
				InputMethodUtil.showSoftInput(getActivity());
				break;
			case R.id.wv_container:
				KLog.d(AppDebugConfig.TAG_WARN, "WebView is touch");
				wvTouchStep = walkStep;
				walkStep += 2;
//				llImgPreview.setVisibility(View.GONE);
				InputMethodUtil.hideSoftInput(getActivity());
				llImgPreview.requestLayout();
				break;
		}
		return false;
	}

	@Override
	public void onGlobalLayout() {
		final int curHeight = InputMethodUtil.getSoftInputHeight(getActivity());
		int softInputHeight = AssistantApp.getInstance().getSoftInputHeight(getActivity());
		KLog.d(AppDebugConfig.TAG_WARN, "softInputHeight = " + softInputHeight + ", mLastInputHeight = " +
				mLastSoftInputHeight);
		if (curHeight > 0 && curHeight != softInputHeight) {
			softInputHeight = curHeight;
			AssistantApp.getInstance().setSoftInputHeight(curHeight);
		}
		if (softInputHeight != mLastSoftInputHeight && softInputHeight > 0) {
			mLastSoftInputHeight = softInputHeight;
			resetLayoutParams();
		}
		KLog.d(AppDebugConfig.TAG_WARN, "curHeight = " + curHeight
				+ " , walkStep = " + walkStep
				+ " , contentTouchStep = " + contentTouchStep
				+ " , imgHideStep = " + imgHideStep
				+ " , imgShowStep = " + imgShowStep
				+ ", imgFailStep = " + imgFailStep
				+ ", wvTouchStep = " + wvTouchStep);

		if (curHeight == 0) {

			if (contentTouchStep == walkStep - 1) {
//				llImgPreview.setVisibility(View.VISIBLE);
			} else if (contentTouchStep == walkStep - 2) {
				llImgPreview.setVisibility(View.GONE);
			} else if (imgHideStep == walkStep - 1) {
				llImgPreview.setVisibility(View.GONE);
			} else if (imgShowStep == walkStep - 1) {
				llImgPreview.setVisibility(View.VISIBLE);
			} else if (imgFailStep == walkStep - 1 && contentTouchStep == walkStep - 2) {
				// 未选取图，保留前一次状态
				llImgPreview.setVisibility(View.VISIBLE);
				InputMethodUtil.showSoftInput(getActivity());
			} else if (wvTouchStep == walkStep - 2) {
				llImgPreview.setVisibility(View.GONE);
			}
			walkStep++;
		} else {
			if (contentTouchStep == walkStep - 1) {
//				llImgPreview.setVisibility(View.VISIBLE);
				walkStep++;
			}
		}
	}

	private void resetLayoutParams() {
		KLog.d(AppDebugConfig.TAG_WARN, "resetLayoutParams = mLastSoftInputHeight =" + mLastSoftInputHeight
				+ ", llImgPreview = " + llImgPreview);
		if (llImgPreview != null) {
			ViewGroup.LayoutParams lp = llImgPreview.getLayoutParams();
			lp.height = mLastSoftInputHeight;
			llImgPreview.setLayoutParams(lp);
		}
	}
}
