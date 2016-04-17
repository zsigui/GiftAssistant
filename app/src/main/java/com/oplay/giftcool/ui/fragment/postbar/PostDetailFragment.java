package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.oplay.giftcool.engine.NoEncryptEngine;
import com.oplay.giftcool.listener.ShowBottomBarListener;
import com.oplay.giftcool.manager.DialogManager;
import com.oplay.giftcool.model.json.base.JsonRespBase;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.util.BitmapUtil;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.NetworkUtil;
import com.oplay.giftcool.util.ToastUtil;
import com.socks.library.KLog;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zsigui on 16-4-6.
 */
public class PostDetailFragment extends BaseFragment_WebView implements TextWatcher,
		ViewTreeObserver.OnGlobalLayoutListener, ShowBottomBarListener, View.OnTouchListener {

	private final String PAGE_NAME = "活动详情";
	private final NoEncryptEngine mEngine;
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
		mEngine = AssistantApp.getInstance().getRetrofit().create(NoEncryptEngine.class);
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
		final String detailUrl = WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_DETAIL) + mId;
		loadUrl(detailUrl);

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
	private boolean mIsInputShow = false;
	private boolean mIsInImg;

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.iv_img_add:
				mIsInImg = false;
				mIsInputShow = false;
				if (mPostImg.size() == 0) {
					GalleryFinal.openGalleryMulti(mAdapter.REQ_ID_IMG_ADD, Global.REPLY_IMG_COUNT, mAdapter);
					pickFailed();
					return;
				}
				ivImgAdd.setSelected(true);
				if (llImgPreview.getVisibility() == View.VISIBLE
						&& InputMethodUtil.getSoftInputHeight(getActivity()) == 0) {
					etContent.requestFocus();
					llImgPreview.setVisibility(View.GONE);
					InputMethodUtil.hideSoftInput(getActivity());
				} else {
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
		mIsInImg = false;
		mIsInPost = false;
		ivImgAdd.setSelected(true);
		llImgPreview.setVisibility(View.VISIBLE);
		InputMethodUtil.hideSoftInput(getActivity());
	}

	/**
	 * 未选择图片或执行失败或者删除完图片的处理函数
	 */
	public void pickFailed() {
		mAdapter.setPicTextVal();
		mIsInImg = false;
		mIsInputShow = false;
		InputMethodUtil.hideSoftInput(getActivity());
		llImgPreview.setVisibility(View.GONE);
		ivImgAdd.setSelected(false);
		etContent.requestFocus();
	}

	/**
	 * 执行发表回复的网络请求
	 */
	private Call<JsonRespBase<Void>> mCallPost;
	/**
	 * 防止重复提交回复的网络请求
	 */
	private boolean mIsInPost = false;

	/**
	 * 处理回复提交请求
	 */
	private void handleSend() {

		Global.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				if (mIsInPost) {
					return;
				}
				mIsInPost = true;
				if (!NetworkUtil.isConnected(getContext())) {
					ToastUtil.showShort(ConstString.TEXT_NET_ERROR);
					return;
				}
				showLoading();
				if (mCallPost != null) {
					mCallPost.cancel();
				}
				HashMap<String, RequestBody> reqData = new HashMap<>();
				reqData.put("msg", RequestBody.create(MediaType.parse("text/plain; charset=UTF-8"), etContent.getText
						().toString()));
				int i = 0;
				for (PhotoInfo p : mPostImg) {
					reqData.put("pic" + i, MultipartBody.create(MediaType.parse("image/*"),
							generateImageStringParam(p.getPhotoPath())));
					i++;
				}
				mCallPost = mEngine.postReply(reqData);
				mCallPost.enqueue(new Callback<JsonRespBase<Void>>() {
					@Override
					public void onResponse(Call<JsonRespBase<Void>> call, Response<JsonRespBase<Void>> response) {
						mIsInPost = false;
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
						mIsInPost = false;
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
	private byte[] generateImageStringParam(String filePath) {
		ByteArrayOutputStream baos = BitmapUtil.getBitmapForBaos(filePath, AppConfig.REPLY_PIC_SIZE,
				AppConfig.REPLY_PIC_WIDTH, AppConfig.REPLY_PIC_HEIGHT);
		return baos.toByteArray();
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
	public void onGlobalLayout() {
		int softInputHeight = AssistantApp.getInstance().getSoftInputHeight(getActivity());
		if (softInputHeight != mLastSoftInputHeight && softInputHeight != 0) {
			mLastSoftInputHeight = softInputHeight;
			resetLayoutParams();
		}
		final int curHeight = InputMethodUtil.getSoftInputHeight(getActivity());

		KLog.d(AppDebugConfig.TAG_WARN, "mIsInputShow = " + mIsInputShow + ", mIsInImg = " + mIsInImg
				+ ", llImgPreView.getVisibility() = " + llImgPreview.getVisibility() + ", curHeight = " + curHeight);
		if (curHeight == 0) {
			if (mIsInputShow && mIsInImg && llImgPreview.getVisibility() == View.VISIBLE) {
				llImgPreview.setVisibility(View.GONE);
				KLog.d(AppDebugConfig.TAG_WARN, "mIsInput = " + mIsInputShow + ", set show");
			} else if (!mIsInputShow && mIsInImg && llImgPreview.getVisibility() == View.VISIBLE) {
				llImgPreview.setVisibility(View.GONE);
				mIsInImg = false;
				KLog.d(AppDebugConfig.TAG_WARN, "mIsInImg = " + mIsInImg + ", set gone");
			}
		}
	}

	private void resetLayoutParams() {
		if (llImgPreview != null) {
			ViewGroup.LayoutParams lp = llImgPreview.getLayoutParams();
			lp.height = mLastSoftInputHeight;
			llImgPreview.setLayoutParams(lp);
		}
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
			case R.id.wv_container:
				pickFailed();
				break;
			case R.id.et_content:
				// EditText点击打开软键盘之前，需要先显示llImgPreview才行，否则页面会被推上去
				etContent.requestFocus();
				InputMethodUtil.showSoftInput(getActivity());
				llImgPreview.setVisibility(View.VISIBLE);
				mIsInputShow = true;
				mIsInImg = true;
				break;
		}
		return false;
	}
}
