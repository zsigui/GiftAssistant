package com.oplay.giftcool.ui.fragment.postbar;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oplay.giftcool.AssistantApp;
import com.oplay.giftcool.R;
import com.oplay.giftcool.adapter.PostReplyAdapter;
import com.oplay.giftcool.config.ConstString;
import com.oplay.giftcool.config.Global;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.config.WebViewUrl;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;
import com.oplay.giftcool.util.InputMethodUtil;
import com.oplay.giftcool.util.ToastUtil;

import java.util.ArrayList;

import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * Created by zsigui on 16-4-6.
 */
public class PostDetailFragment extends BaseFragment_WebView implements TextWatcher,
		ViewTreeObserver.OnGlobalLayoutListener {

	private final String PAGE_NAME = "活动详情";
	private final int REQ_ID_IMG_ADD = 0x12343;


	private int mSoftInputHeight = 0;

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

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_post_detail);
		etContent = getViewById(R.id.et_content);
		ivImgAdd = getViewById(R.id.iv_img_add);
		tvSend = getViewById(R.id.tv_send);
		llBottomBar = getViewById(R.id.ll_input_bar);
		llImgPreview = getViewById(R.id.ll_img_preview);
		rlContainer = getViewById(R.id.rl_container);
		tvPickHint = getViewById(R.id.tv_pick_hint);
	}

	@Override
	protected void setListener() {
		ivImgAdd.setOnClickListener(this);
		tvSend.setOnClickListener(this);
		etContent.addTextChangedListener(this);
		etContent.setOnClickListener(this);
		etContent.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			ToastUtil.showShort(ConstString.TEXT_ENTER_ERROR);
			return;
		}
		mId = getArguments().getInt(KeyConfig.KEY_DATA, 0);
		final String detailUrl = WebViewUrl.getWebUrl(WebViewUrl.ACTIVITY_DETAIL) + mId;
		loadUrl(detailUrl);

		mAdapter = new PostReplyAdapter(getContext(), mPostImg);
		GridLayoutManager gld = new GridLayoutManager(getContext(), 4);
		rlContainer.setLayoutManager(gld);
		rlContainer.setAdapter(mAdapter);
		mAdapter.setTvPickHint(tvPickHint);
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

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.iv_img_add:
				if (ivImgAdd.isSelected()) {
					ivImgAdd.setSelected(false);
					etContent.requestFocus();
					etContent.setSelection(etContent.getText().length());
					InputMethodUtil.showSoftInput(getActivity());
					llImgPreview.setVisibility(View.GONE);
				} else {
					ivImgAdd.setSelected(true);
					llImgPreview.setVisibility(View.VISIBLE);
					InputMethodUtil.hideSoftInput(getActivity());
				}
				break;
			case R.id.et_content:
				etContent.requestFocus();
				InputMethodUtil.showSoftInput(getActivity());
				ivImgAdd.setSelected(false);
				llImgPreview.setVisibility(View.GONE);
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

	private void handleSend() {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void onGlobalLayout() {
		int softInputHeight = AssistantApp.getInstance().getSoftInputHeight(getActivity());
		if (softInputHeight != mSoftInputHeight) {
			mSoftInputHeight = softInputHeight;
			resetLayoutParams();
		}
	}

	private void resetLayoutParams() {
		if (llImgPreview != null) {
			ViewGroup.LayoutParams lp = llImgPreview.getLayoutParams();
			lp.height = mSoftInputHeight;
			llImgPreview.setLayoutParams(lp);
		}
	}
}
