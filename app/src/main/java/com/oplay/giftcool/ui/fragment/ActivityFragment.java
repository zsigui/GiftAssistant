package com.oplay.giftcool.ui.fragment;

import android.os.Bundle;

import com.oplay.giftcool.R;
import com.oplay.giftcool.config.KeyConfig;
import com.oplay.giftcool.ui.fragment.base.BaseFragment_WebView;

/**
 * Created by zsigui on 16-1-29.
 */
public class ActivityFragment extends BaseFragment_WebView {
	private static final String PAGE_NAME = "活动页面";

	public static ActivityFragment newInstance(String url) {
		ActivityFragment fragment = new ActivityFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KeyConfig.KEY_URL, url);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void initView(Bundle savedInstanceState) {
		initViewManger(R.layout.fragment_webview);
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void processLogic(Bundle savedInstanceState) {
		if (getArguments() == null) {
			mViewManager.showEmpty();
			return;
		}
		String url = getArguments().getString(KeyConfig.KEY_URL);
		loadUrl(url);
		mIsSwipeRefresh = true;
	}

	@Override
	protected void lazyLoad() {
		if (!mIsSwipeRefresh) {
			reloadPage();
		}
		mIsSwipeRefresh = false;
	}

	@Override
	public String getPageName() {
		return PAGE_NAME;
	}
}
